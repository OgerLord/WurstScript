package de.peeeq.wurstscript.intermediateLang.interpreter;

import java.io.PrintStream;

import de.peeeq.wurstscript.intermediateLang.ILconst;

public class NativeFunctions implements NativesProvider {

	@Override
	public ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException {
		throw new NoSuchNativeException();
	}

	@Override
	public void setOutStream(PrintStream outStream) {
		// TODO Auto-generated method stub
	}

}
