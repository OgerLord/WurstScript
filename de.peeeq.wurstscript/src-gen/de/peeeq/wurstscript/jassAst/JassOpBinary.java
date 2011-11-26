//generated by parseq
package de.peeeq.wurstscript.jassAst;

public interface JassOpBinary extends JassAstElement, JassOp{
	JassAstElement getParent();
	<T> T match(Matcher<T> s);
	void match(MatcherVoid s);
	public interface Matcher<T> {
		T case_JassOpMult(JassOpMult jassOpMult);
		T case_JassOpLessEq(JassOpLessEq jassOpLessEq);
		T case_JassOpOr(JassOpOr jassOpOr);
		T case_JassOpEquals(JassOpEquals jassOpEquals);
		T case_JassOpLess(JassOpLess jassOpLess);
		T case_JassOpDiv(JassOpDiv jassOpDiv);
		T case_JassOpPlus(JassOpPlus jassOpPlus);
		T case_JassOpMinus(JassOpMinus jassOpMinus);
		T case_JassOpGreaterEq(JassOpGreaterEq jassOpGreaterEq);
		T case_JassOpGreater(JassOpGreater jassOpGreater);
		T case_JassOpUnequals(JassOpUnequals jassOpUnequals);
		T case_JassOpAnd(JassOpAnd jassOpAnd);
	}

	public interface MatcherVoid {
		void case_JassOpMult(JassOpMult jassOpMult);
		void case_JassOpLessEq(JassOpLessEq jassOpLessEq);
		void case_JassOpOr(JassOpOr jassOpOr);
		void case_JassOpEquals(JassOpEquals jassOpEquals);
		void case_JassOpLess(JassOpLess jassOpLess);
		void case_JassOpDiv(JassOpDiv jassOpDiv);
		void case_JassOpPlus(JassOpPlus jassOpPlus);
		void case_JassOpMinus(JassOpMinus jassOpMinus);
		void case_JassOpGreaterEq(JassOpGreaterEq jassOpGreaterEq);
		void case_JassOpGreater(JassOpGreater jassOpGreater);
		void case_JassOpUnequals(JassOpUnequals jassOpUnequals);
		void case_JassOpAnd(JassOpAnd jassOpAnd);
	}

	JassOpBinary copy();
	public abstract void accept(JassStatement.Visitor v);
	public abstract void accept(JassStmtCall.Visitor v);
	public abstract void accept(JassFunction.Visitor v);
	public abstract void accept(JassExprlist.Visitor v);
	public abstract void accept(JassExpr.Visitor v);
	public abstract void accept(JassStmtIf.Visitor v);
	public abstract void accept(JassStatements.Visitor v);
	public abstract void accept(JassStmtExitwhen.Visitor v);
	public abstract void accept(JassExprFunctionCall.Visitor v);
	public abstract void accept(JassStmtSetArray.Visitor v);
	public abstract void accept(JassExprBinary.Visitor v);
	public abstract void accept(JassExprUnary.Visitor v);
	public abstract void accept(JassExprVarArrayAccess.Visitor v);
	public abstract void accept(JassStmtLoop.Visitor v);
	public abstract void accept(JassOpBinary.Visitor v);
	public abstract void accept(JassStmtReturn.Visitor v);
	public abstract void accept(JassProg.Visitor v);
	public abstract void accept(JassStmtSet.Visitor v);
	public abstract void accept(JassFunctions.Visitor v);
	public abstract void accept(JassOp.Visitor v);
	public abstract void accept(JassExprAtomic.Visitor v);
	public interface Visitor {
		void visit(JassOpMult jassOpMult);
		void visit(JassOpLessEq jassOpLessEq);
		void visit(JassOpOr jassOpOr);
		void visit(JassOpPlus jassOpPlus);
		void visit(JassOpMinus jassOpMinus);
		void visit(JassOpUnequals jassOpUnequals);
		void visit(JassOpAnd jassOpAnd);
		void visit(JassOpEquals jassOpEquals);
		void visit(JassOpLess jassOpLess);
		void visit(JassOpDiv jassOpDiv);
		void visit(JassOpGreaterEq jassOpGreaterEq);
		void visit(JassOpGreater jassOpGreater);
	}
	public static abstract class DefaultVisitor implements Visitor {
		@Override public void visit(JassOpMult jassOpMult) {}
		@Override public void visit(JassOpLessEq jassOpLessEq) {}
		@Override public void visit(JassOpOr jassOpOr) {}
		@Override public void visit(JassOpPlus jassOpPlus) {}
		@Override public void visit(JassOpMinus jassOpMinus) {}
		@Override public void visit(JassOpUnequals jassOpUnequals) {}
		@Override public void visit(JassOpAnd jassOpAnd) {}
		@Override public void visit(JassOpEquals jassOpEquals) {}
		@Override public void visit(JassOpLess jassOpLess) {}
		@Override public void visit(JassOpDiv jassOpDiv) {}
		@Override public void visit(JassOpGreaterEq jassOpGreaterEq) {}
		@Override public void visit(JassOpGreater jassOpGreater) {}
	}
}