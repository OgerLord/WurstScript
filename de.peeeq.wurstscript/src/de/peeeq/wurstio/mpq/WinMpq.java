package de.peeeq.wurstio.mpq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.peeeq.wurstscript.WLogger;

public class WinMpq implements MpqEditor {

	private final String winmpqExe;

	public WinMpq() {
		this("./winmpq/WinMPQ.exe");
	}
	
	public WinMpq(String exe) {
		this.winmpqExe = exe;
	}
	
	@Override
	public File extractFile(File mpqArchive, String fileToExtract)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		File tempFile1 = new File("./temp/" + fileToExtract);
		File tempFolder = new File(tempFile1.getParent());
		String[] commands = {winmpqExe, "extract", mpqArchive.getAbsolutePath(), fileToExtract, tempFolder.getAbsolutePath()};
		
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
		if (!tempFile1.exists()) {
			throw new Error("could not extract file");
		}
		
		
		return tempFile1;
		
		
	}

	@Override
	public void insertFile(File mpqArchive, String filenameInMpq, File tempFile)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {winmpqExe, "add", mpqArchive.getAbsolutePath(), tempFile.getAbsolutePath(), filenameInMpq};
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}

	@Override
	public void deleteFile(File mpqArchive, String filenameInMpq)
			throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		String[] commands = {winmpqExe, "delete", mpqArchive.getAbsolutePath(), filenameInMpq};
		
		Process proc = rt.exec(commands);
		InputStream procOut = proc.getInputStream();
		BufferedReader procOutReader = new BufferedReader(new InputStreamReader(procOut));
		proc.waitFor();
		String line;
		while ((line = procOutReader.readLine()) != null) {
			WLogger.info(line);
		}
		
	}

	@Override
	public void compactArchive(File mpqArchive) throws IOException,
			InterruptedException {
		// TODO Auto-generated method stub
		
	}

}
