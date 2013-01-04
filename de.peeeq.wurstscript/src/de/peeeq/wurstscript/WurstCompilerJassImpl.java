package de.peeeq.wurstscript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import java_cup.runtime.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.ast.WPos;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.ErrorHandler;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.mpq.LadikMpq;
import de.peeeq.wurstscript.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.parser.ExtendedParser;
import de.peeeq.wurstscript.parser.ScannerError;
import de.peeeq.wurstscript.parser.WurstScriptScanner;
import de.peeeq.wurstscript.translation.imoptimizer.ImOptimizer;
import de.peeeq.wurstscript.translation.imtojass.ImToJassTranslator;
import de.peeeq.wurstscript.translation.imtranslation.AssertProperty;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.FileReading;
import de.peeeq.wurstscript.utils.LineOffsets;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;

public class WurstCompilerJassImpl implements WurstCompiler {

	private List<File> files = Lists.newArrayList();
	private Map<String, Reader> otherInputs = Maps.newHashMap();
	private JassProg prog;
	private WurstGui gui;
	private boolean hasCommonJ;
	private RunArgs runArgs;
	private File mapFile;
	private ErrorHandler errorHandler;

	
	public WurstCompilerJassImpl(WurstGui gui, RunArgs runArgs) {
		this.gui = gui;
		this.runArgs = runArgs;
		this.errorHandler = new ErrorHandler(gui);
	}

	@Override
	public void loadFiles(String ... filenames) {
		gui.sendProgress("Loading Files", 0.01);
		for (String filename : filenames) {
			File file = new File(filename);
			if (!file.exists()) {
				throw new Error("File " + filename + " does not exist.");
			}
			files.add(file);
		}
	}
	
	@Override
	public void loadFiles(File ... files) {
		gui.sendProgress("Loading Files", 0.01);
		for (File file : files) {
			loadFile(file);
		}
	}

	private void loadFile(File file) throws Error {
		if (file == null) {
			throw new Error("File must not be null");
		}
		if (!file.exists()) {
			throw new Error("File " + file + " does not exist.");
		}
		this.files.add(file);
	}
	
	public void loadWurstFilesInDir(File dir) {
		for (File f : dir.listFiles()) {
			if (f.isDirectory()) {
				loadWurstFilesInDir(f);
			} else if (f.getName().endsWith(".wurst")) {
				loadFile(f);
			}
		}
	}
	
	@Override public WurstModel parseFiles() {

		// search mapFile
		for (File file : files) {
			if (file.getName().endsWith(".w3x") || file.getName().endsWith(".w3m")) {
				mapFile = file;
			}
		}
		
		// import wurst folder if it exists
		if (mapFile != null) {
			File relativeWurstDir = new File(mapFile.getParentFile().getAbsolutePath() + "/wurst/");
			if (relativeWurstDir.exists()) {
				WLogger.info("Importing wurst files from " + relativeWurstDir);
				loadWurstFilesInDir(relativeWurstDir);
			} else {
				WLogger.info("No wurst folder found in " + relativeWurstDir);
			}
		}
		
		
		gui.sendProgress("Parsing Files", 0.02);
		// parse all the files:
		List<CompilationUnit> compilationUnits = new NotNullList<CompilationUnit>();
		
		for (File file : files) {
			if (file.getName().endsWith(".w3x") || file.getName().endsWith(".w3m")) {
				CompilationUnit r = processMap(file);
				compilationUnits.add(r );				
			} else {
				if (file.getName().endsWith("common.j")) {
					hasCommonJ = true;
				}
				compilationUnits.add(parseFile(file));
			}
		}
		for (Entry<String, Reader> in : otherInputs.entrySet()) {
			compilationUnits.add(parse(in.getValue(), in.getKey()));
			
		}
		
		try {
			if (hasCommonJ) {
				loadLibPackage(compilationUnits, "Wurst");
			}
			addImportedLibs(compilationUnits);
		} catch (CompileError e) {
			gui.sendError(e);
			return null;
		}
		
		if (errorHandler.getErrorCount() > 0) return null;
		
		// merge the compilationUnits:
		WurstModel merged = mergeCompilationUnits(compilationUnits);
		StringBuilder sb = new StringBuilder();
		for (CompilationUnit cu:merged) {
			sb.append(cu.getFile() + ", ");
		}
		WLogger.info("Compiling compilation units: " + sb);
		
		checkAndTranslate(merged);
		gui.sendProgress("finished parsing", .9);
		
		return merged;
	}
	

	

	/**
	 * this method scans for unsatisfied imports and tries to find them in the lib-path 
	 */
	public void addImportedLibs(List<CompilationUnit> compilationUnits) {
		Set<String> packages = Sets.newHashSet();
		Map<String, WImport> imports = Maps.newHashMap();
		for (CompilationUnit c : compilationUnits) {
			c.setCuErrorHandler(errorHandler);
			for (WPackage p : c.getPackages()) {
				packages.add(p.getName());
				for (WImport i : p.getImports()) {
					imports.put(i.getPackagename(), i);
				}
			}
		}	
		
		for (WImport imp : imports.values()) {
			resolveImport(compilationUnits, packages, imports, imp);
		}
		
		
	}

	private void resolveImport(List<CompilationUnit> compilationUnits, Set<String> packages, Map<String, WImport> imports, WImport imp)
			throws CompileError {
//		WLogger.info("resolving import: " + imp.getPackagename());
		if (!packages.contains(imp.getPackagename())) {
			if (getLibs().containsKey(imp.getPackagename())) {
				CompilationUnit lib = loadLibPackage(compilationUnits, imp.getPackagename());
				boolean foundPackage = false;
				for (WPackage p : lib.getPackages()) {
					packages.add(p.getName());
					if (p.getName().equals(imp.getPackagename())) {
						foundPackage = true;
					}
					for (WImport i : p.getImports()) {
						resolveImport(compilationUnits, packages, imports, i);
					}
				}
				if (!foundPackage) {
					throw new CompileError(imp.getSource(), "The import " + imp.getPackagename() + " could not be found in file " + lib.getFile());
				}
			} else {
				if (imp.getPackagename().equals("Wurst")) {
					throw new CompileError(imp.getSource(), "The standard library could not be imported.");
				}
				if (imp.getPackagename().equals("NoWurst")) {
					// ignore this package
				} else {
					throw new CompileError(imp.getSource(), "The import '" + imp.getPackagename() + "' could not be resolved.\n" + 
						"Available packages: " + Utils.join(getLibs().keySet(), ", "));
				}
			}
		} else {
//			WLogger.info("already imported: " + imp.getPackagename());
		}
	}

	private CompilationUnit loadLibPackage(List<CompilationUnit> compilationUnits, String imp) {
		File file = getLibs().get(imp);
		if (file == null) {
			gui.sendError(new CompileError(Ast.WPos("", 0, 0, 0), "Could not find lib-package " + imp));
			return Ast.CompilationUnit("", errorHandler, Ast.JassToplevelDeclarations(), Ast.WPackages());
		} else {
			CompilationUnit lib = parseFile(file);
			compilationUnits.add(lib);
			return lib;
		}
	}

	private Map<String, File> libCache = null;
	private ImProg imProg;
	
	public Map<String, File> getLibs() {
		if (libCache == null) {
			libCache = Maps.newHashMap();
			String[] libFolders = WurstConfig.get().getSetting("lib").split(";");
			for (String libDirName : libFolders) {
				if (libDirName.length() == 0) { 
					continue;
				}
				File libDir = new File(libDirName);
				addLibDir(libDir);
			}
		}
		return libCache;
	}

	private void addLibDir(File libDir) throws Error {
		if (!libDir.exists() || !libDir.isDirectory()) {
			throw new Error("Library folder " + libDir + " does not exist.");
		}
		for (File f : libDir.listFiles()) {
			if (f.isDirectory()) {
				// recursively scan directory
				addLibDir(f);
			}
			if (f.getName().endsWith(".wurst")) {
				String libName = f.getName().replaceAll("\\.wurst$", "");
				libCache.put(libName, f);
			}
		}
	}

	public void checkAndTranslate(WurstModel root) {
		checkProg(root);
		
		
		if (errorHandler.getErrorCount() > 0) {
			return;
		}
		
		try {
			prog = translateProg(root);
		} catch (CompileError e) {
			WLogger.severe(e);
			errorHandler.sendError(e);
		}
	}

	public void checkProg(WurstModel root) {
		gui.sendProgress("Checking Files", 0.2);
		
		if (errorHandler.getErrorCount() > 0) return;
		
		attachErrorHanlder(root);
		
		expandModules(root);
		
		if (errorHandler.getErrorCount() > 0) return;
		
		// compute the flow attributes
		WurstValidator.computeFlowAttributes(root);
		
		
		// validate the resource:
		WurstValidator validator = new WurstValidator(root);
		validator.validate();
	}

	private void attachErrorHanlder(WurstModel root) {
		for (CompilationUnit cu : root) {
			cu.setCuErrorHandler(errorHandler);
		}
	}

	private void expandModules(WurstModel root) {
		for (CompilationUnit cu : root) {
			new ModuleExpander().expandModules(cu);
		}
	}

	public JassProg translateProg(WurstModel root) {
		// translate wurst to intermediate lang:
		ImTranslator imTranslator = new ImTranslator(root, errorHandler.isUnitTestMode());
		imProg = imTranslator.translateProg();
		int stage = 1;
		
		printDebugImProg("./test-output/im " + stage++ + ".im");
		
		ImOptimizer optimizer = new ImOptimizer(imTranslator);
		
		
		if (runArgs.isInline()) {
			optimizer.doInlining();
			
			printDebugImProg("./test-output/im " + stage++ + "_afterinline.im");
		}
		
		if (runArgs.isNullsetting()) {
			optimizer.doNullsetting();
			printDebugImProg("./test-output/im " + stage++ + "_afternullsetting.im");
		}
		
		
		printDebugImProg("./test-output/test_opt.im");
	
		// eliminate tuples
		imProg.eliminateTuples(imTranslator);
		imTranslator.assertProperties(AssertProperty.NOTUPLES);
		
		printDebugImProg("./test-output/im " + stage++ + "_withouttuples.im");
		
		// flatten
		imProg.flatten(imTranslator);
		imTranslator.assertProperties(AssertProperty.NOTUPLES, AssertProperty.FLAT);
		
		printDebugImProg("./test-output/im " + stage++ + "_flat.im");
		
		
		if (runArgs.isOptimize()) {
			optimizer.optimize();
			
			printDebugImProg("./test-output/im " + stage++ + "_afteroptimize.im");
		}
		
		// translate flattened intermediate lang to jass:
		
		ImToJassTranslator translator = new ImToJassTranslator(imProg, imTranslator.getCalledFunctions()
				, imTranslator.getMainFunc(), imTranslator.getConfFunc());
		JassProg p = translator.translate();
		if (errorHandler.getErrorCount() > 0) {
			return null;
		}
		return p;
	}

	private void printDebugImProg(String debugFile) {
		if (!errorHandler.isUnitTestMode()) {
			// output only in unit test mode
			return;
		}
		try {
			// TODO remove test output
			StringBuilder sb = new StringBuilder();
			imProg.print(sb, 0);
			File file = new File(debugFile);
			file.getParentFile().mkdirs();
			Files.write(sb.toString(), file, Charsets.UTF_8);
		} catch (IOException e) {
			ErrorReporting.handleSevere(e, getCompleteSourcecode());
		}
	}

	

	private void removeSyntacticSugar(CompilationUnit root) {
		new SyntacticSugar().removeSyntacticSugar(root, hasCommonJ);
	}

	

	

	

	
//	private List<ModuleDef> getAllModules(CompilationUnit root) {
//		List<ModuleDef> result = Lists.newArrayList();
//		for (TopLevelDeclaration t : root) {
//			if (t instanceof WPackage) {
//				WPackage p = (WPackage) t;
//				for (WEntity e : p.getElements()) {
//					if (e instanceof ModuleDef) {
//						result.add((ModuleDef) e);
//					}
//				}
//			}
//		}
//		return result;
//	}
	
	private WurstModel mergeCompilationUnits(List<CompilationUnit> compilationUnits) {
		gui.sendProgress("Merging Files", 0.22);
		WurstModel result = Ast.WurstModel();
		for (CompilationUnit compilationUnit : compilationUnits) {
			// remove from old parent
			compilationUnit.setParent(null);
			result.add(compilationUnit);
		}
		return result;
	}

	private CompilationUnit processMap(File file) {
		gui.sendProgress("Processing Map " + file.getName(), 0.05);		

		// extract mapscript:
		if ( MpqEditorFactory.getFilepath().equals("")) {
			MpqEditorFactory.setFilepath("./mpqedit/mpqeditor.exe");
		}
		LadikMpq mpqEditor = null;
		try {
			mpqEditor = MpqEditorFactory.getEditor();
			File tempFile = mpqEditor.extractFile(file, "war3map.j");
			return parseFile(tempFile);
		} catch (Exception e) {
			throw new Error(e);
		}

	}

	private CompilationUnit parseFile(File file) {
		gui.sendProgress("Parsing File " + file.getName(), 0.05);
		String source = file.getAbsolutePath();
		Reader reader = null;
		try {
			reader = FileReading.getFileReader(file);
			
			// scanning
			return parse(reader, source);
			
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (FileNotFoundException e) {
			gui.sendError(new CompileError(Ast.WPos(source, LineOffsets.dummy, 0, 0), "File not found."));
			return emptyCompilationUnit();
		} catch (IOException e) {
			gui.sendError(new CompileError(Ast.WPos(source, LineOffsets.dummy, 0, 0), "Could not read file."));
			return emptyCompilationUnit();
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (IOException e) {
			}
		}
	}

	private CompilationUnit emptyCompilationUnit() {
		return Ast.CompilationUnit("<empty compilation unit>", errorHandler, Ast.JassToplevelDeclarations(), Ast.WPackages());
	}

	public CompilationUnit parse(Reader reader, String source) {
		try {
			WurstScriptScanner scanner = new WurstScriptScanner(reader);
			ExtendedParser parser = new ExtendedParser(scanner, errorHandler);
			parser.setFilename(source);
			Symbol sym = parser.parse();
			
			if (sym.value instanceof CompilationUnit) {
				CompilationUnit root = (CompilationUnit) sym.value;
				removeSyntacticSugar(root);
				WPos p = root.attrErrorPos().copy();
				p.setFile(source);
				for (ScannerError err : scanner.getErrors()) {
					CompileError ce = err.makeCompilerError(p);
					gui.sendError(ce);
				}
				return root;
			}
			return emptyCompilationUnit();
		} catch (CompileError e) {
			gui.sendError(e);
			return emptyCompilationUnit();
		} catch (Exception e) {
			gui.sendError(new CompileError(Ast.WPos(source, LineOffsets.dummy, 0, 0), "This is a bug and should not have happened.\n" + e.getMessage()));
			WLogger.severe(e);
			throw new Error(e);
		}
	}

	
	public JassProg getProg() {
		return prog;
	}

	public void loadReader(String name, Reader input) {
		otherInputs.put(name, input);
	}

	public void setHasCommonJ(boolean hasCommonJ) {
		this.hasCommonJ = hasCommonJ;
	}

	public ImProg getImProg() {
		return imProg;
	}

	public File getMapFile() {
		return mapFile;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public String getCompleteSourcecode() {
		
		StringBuilder sb = new StringBuilder();
		try {
			for (File f: files) {
				sb.append(" //######################################################\n");
				sb.append(" // File " + f.getAbsolutePath() + "\n");
				sb.append(" //######################################################\n");
				sb.append(Files.toString(f, Charsets.UTF_8));
			}
			
			for (Entry<String, Reader> entry : otherInputs.entrySet()) {
				sb.append(" //######################################################\n");
				sb.append(" // Input " + entry.getKey() + "\n");
				sb.append(" //######################################################\n");
				Reader reader = entry.getValue();
				char[] buffer = new char[1024];
				while (true) {
					int len = reader.read(buffer);
					if (len < 0) {
						break;
					}
					sb.append(buffer, 0, len);
				}
			}
		} catch (Throwable t) {
			sb.append(t.getMessage());
			sb.append(Utils.printStackTrace(t.getStackTrace()));
			WLogger.severe(t);
		}
		return sb.toString();
	}

	
}
