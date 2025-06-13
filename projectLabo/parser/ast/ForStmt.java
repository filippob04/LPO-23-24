package projectLabo.parser.ast;

import static java.util.Objects.requireNonNull;
import projectLabo.visitors.Visitor;

public class ForStmt implements Stmt {
    private final Variable var; // non-optional field 
	private final Exp exp; // non-optional field 
	private final Block forBlock; // non-optional field

	public ForStmt(Variable var, Exp exp, Block forBlock) { // Costruttore 
        this.var = requireNonNull(var);
		this.exp = requireNonNull(exp);
		this.forBlock = requireNonNull(forBlock);
	}

	@Override
	public String toString() { // toString
		return String.format("%s(%s,%s,%s)", getClass().getSimpleName(), var, exp, forBlock);
	}

	@Override
    public <T> T accept(Visitor<T> visitor) { // Visitor
        return visitor.visitForStmt(var, exp, forBlock);
    }
}