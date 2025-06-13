package projectLabo.visitors;

import projectLabo.parser.ast.*;

public interface Visitor<T> {
	

	T visitLangProg(StmtSeq stmtSeq);

	T visitEmptyStmtSeq();

	T visitNonEmptyStmtSeq(Stmt first, StmtSeq rest);

	T visitIfStmt(Exp exp, Block thenBlock, Block elseBlock);

	T visitPrintStmt(Exp exp);

	T visitVarStmt(Variable var, Exp exp);

	T visitBlock(StmtSeq stmtSeq);

	T visitAdd(Exp left, Exp right);

	T visitBoolLiteral(boolean value);

	T visitEq(Exp left, Exp right);

	T visitFst(Exp exp);

	T visitIntLiteral(int value);

	T visitMinus(Exp exp);

	T visitMul(Exp left, Exp right);

	T visitPairLit(Exp left, Exp right);

	T visitSnd(Exp exp);

	T visitVariable(Variable var); // only in this case more efficient then T visitVariable(String name)

	T visitNot(Exp exp);

	T visitAnd(Exp left, Exp right);

	T visitAssignStmt(Variable var, Exp exp);
	
	T visitForStmt(Variable var, Exp exp, Block forBlock); // Visitor per Ciclo For

	T visitDict(Dict.DictOp op, Exp dictExp, Exp keyExp, Exp valueExp); // Visitor per Dictionary

}
