package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Annotation;
import de.peeeq.wurstscript.ast.AstElementWithModifiers;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.HasModifier;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.ModAbstract;
import de.peeeq.wurstscript.ast.ModConstant;
import de.peeeq.wurstscript.ast.ModOverride;
import de.peeeq.wurstscript.ast.ModStatic;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.VisibilityPrivate;
import de.peeeq.wurstscript.ast.VisibilityProtected;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.VisibilityPublicread;

public class ModifiersHelper {

	public static boolean isPublic(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), VisibilityPublic.class);
	}

	public static boolean isProtected(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), VisibilityProtected.class);
	}

	public static boolean isPublicRead(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), VisibilityPublicread.class);
	}

	public static boolean isPrivate(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), VisibilityPrivate.class);
	}

	public static boolean isStatic(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), ModStatic.class);
	}

	public static boolean isOverride(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), ModOverride.class);
	}

	public static boolean isAbstract(AstElementWithModifiers e) {
		if (e instanceof FuncDef
				&& e.attrNearestStructureDef() instanceof InterfaceDef) {
			FuncDef f = (FuncDef) e;
			// functions in interfaces are always abstract if they have no implementation ...
			return f.attrHasEmptyBody();
		}
		return containsType(e.getModifiers(), ModAbstract.class);
	}

	public static boolean isConstant(AstElementWithModifiers e) {
		return containsType(e.getModifiers(), ModConstant.class);
	}

	static boolean containsType(de.peeeq.wurstscript.ast.Modifiers modifiers, Class<? extends Modifier> class1) {
		for (Modifier m : modifiers) {
			if (m.getClass().getName().startsWith(class1.getName())) {
				return true;
			}
		}
		return false;
	}


	static void checkAllowedModifiers(HasModifier e) {
		// TODO check allowed modifiers and call this method from checker
	}

	public static boolean isCompiletime(HasModifier e) {
		return hasAnnotation(e, "compiletime");
	}

	public static boolean hasAnnotation(AstElementWithModifiers e, String name) {
		return hasAnnotation(e.getModifiers(), name);
	}


	private static boolean hasAnnotation(Modifiers modifiers, String string) {
		for (Modifier m : modifiers) {
			if (m instanceof Annotation) {
				Annotation annotation = (Annotation) m;
				if (annotation.getAnnotationType().equals("@" + string)) {
					return true;
				}
			}
		}
		return false;
	}
	
}