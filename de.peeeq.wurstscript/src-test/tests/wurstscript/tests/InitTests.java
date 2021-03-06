package tests.wurstscript.tests;

import org.junit.Test;

public class InitTests extends WurstScriptTest {
	
	
	
	@Test
	public void initOnePackageVars() {
		testAssertErrorsLines(false, "used before it is initialized", 
				"package test",
				"	int i = j",
				"	int j = 5",
				"endpackage"
			);
	}

	
	@Test
	public void initOnePackageBlock() {
		testAssertErrorsLines(false, "used before it is initialized", 
				"package test",
				"	int i",
				"	init",
				"		i = j",
				"	int j = 5",
				"endpackage"
			);
	}
	
	@Test
	public void initTwoPackagesCyclic() {
		testAssertErrorsLines(false, "not yet initialized", 
				"package A",
				"	import B",
				"	int i = j",
				"endpackage",
				"package B",
				"	import A",
				"	public int j = 5",
				"endpackage"
			);
	}
	
	@Test
	public void initTwoPackagesAcyclic() {
		testAssertOkLines(false, 
				"package A",
				"	import B",
				"	int i = j",
				"endpackage",
				"package B",
				"	public int j = 5",
				"endpackage"
			);
	}
	
	
}
