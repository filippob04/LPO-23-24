package projectLabo.visitors.typechecking;

import projectLabo.environments.EnvironmentException;
import projectLabo.parser.ast.Block;
import projectLabo.parser.ast.Dict;
import projectLabo.parser.ast.Exp;
import projectLabo.parser.ast.Stmt;
import projectLabo.parser.ast.StmtSeq;
import projectLabo.parser.ast.Variable;
import projectLabo.visitors.Visitor;
import static projectLabo.visitors.typechecking.AtomicType.*;

public class Typecheck implements Visitor<Type> {

	private final StaticEnv env = new StaticEnv();

	// useful to typecheck binary operations where operands must have the same type
	private void checkBinOp(Exp left, Exp right, Type type) {
		type.checkEqual(left.accept(this));
		type.checkEqual(right.accept(this));
	}

	// static semantics for programs; no value returned by the visitor

	@Override
	public Type visitLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
		} catch (EnvironmentException e) { // undeclared variable
			throw new TypecheckerException(e);
		}
		return null;
	}

	// static semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Type visitEmptyStmtSeq() {
		return null;
	}

	@Override
	public Type visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}
	
	// static semantics for statements; no value returned by the visitor

	@Override
	public Type visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		BOOL.checkEqual(exp.accept(this));
		thenBlock.accept(this);
		if (elseBlock != null)
			elseBlock.accept(this);
		return null;
	}
	
	@Override
	public Type visitPrintStmt(Exp exp) {
		exp.accept(this);
		return null;
	}

	@Override
	public Type visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Type visitBlock(StmtSeq stmtSeq) {
		env.enterLevel();
		stmtSeq.accept(this);
		env.exitLevel();
		return null;
	}

	// static semantics of expressions; a type is returned by the visitor

	@Override
	public AtomicType visitAdd(Exp left, Exp right) {
		checkBinOp(left, right, INT);
		return INT;
	}

	@Override
	public AtomicType visitBoolLiteral(boolean value) {
		return BOOL;
	}

	@Override
	public AtomicType visitEq(Exp left, Exp right) {
		left.accept(this).checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public Type visitFst(Exp exp) {
		return exp.accept(this).toPairType().fstType();
	}

	@Override
	public AtomicType visitIntLiteral(int value) {
		return INT;
	}

	@Override
	public AtomicType visitMinus(Exp exp) {
		INT.checkEqual(exp.accept(this));
		return INT;
	}
	
	@Override
	public AtomicType visitMul(Exp left, Exp right) {
		checkBinOp(left, right, INT);
		return INT;
	}

	@Override
	public PairType visitPairLit(Exp left, Exp right) {
		return new PairType(left.accept(this), right.accept(this));
	}

	@Override
	public Type visitSnd(Exp exp) {
		return exp.accept(this).toPairType().sndType();
	}
	
	@Override
	public Type visitVariable(Variable var) {
		return env.lookup(var);
	}

	@Override
	public AtomicType visitNot(Exp exp) {
		BOOL.checkEqual(exp.accept(this)); // Verifico che sia un booleano e lo ritorno
		return BOOL;
	}

	@Override
	public AtomicType visitAnd(Exp left, Exp right) {
		// Entrambi devono avere lo stesso tipo, bool
		BOOL.checkEqual(left.accept(this));
		BOOL.checkEqual(right.accept(this));
		return BOOL;
	}

	@Override
	public Type visitAssignStmt(Variable var, Exp exp) {
		Type expected = env.lookup(var); // Tipo atteso se già dichiarato precedentemente
		Type actual = exp.accept(this); // Tipo effettivo dell’espressione
		expected.checkEqual(actual);   // Controllo dei due tipi
		return null;
	}

	//********************************/NUOVI METODI IMPLEMENTATI/*******************************************//

	@Override
	public Type visitForStmt(Variable var, Exp exp, Block forBlock){ // Visitor per Ciclo For
		
		Type expType = exp.accept(this);
		if(!(expType instanceof DictType)){// Verifico che exp sia di tipo Dict
			throw new TypecheckerException("Found " + expType +" , expected DICT");
		} 

		DictType dictType = expType.toDictType(expType);
		PairType pairType = new PairType(dictType.getKeyType(), dictType.getValueType()); // Creo la coppia (chiave, valore)

		env.enterLevel();				// Entro nello scope del ciclo for
		env.dec(var, pairType); 	    // Aggiungo la variabile all'ambiente di tipo pairType (Chiave, Valore)

		forBlock.accept(this); 			// Controllo che il blocco del ciclo sia valido, passa a visitBlock
		env.exitLevel(); 				// Esco nello scope del ciclo for

		return null;
	}

	@Override
	public Type visitDict(Dict.DictOp op, Exp dictExp, Exp keyExp, Exp valueExp){ // Visitor per Tipo Dictionary
	
		INT.checkEqual(keyExp.accept(this)); // chiave deve essere INT
		if(op == Dict.DictOp.CREATE){
			Type valueType = valueExp.accept(this);
			return new DictType(INT, valueType);
		}
		// Per le altre operazioni (GET, UPDATE, DELETE) serve un dizionario su cui operare
		if (dictExp == null)
			throw new TypecheckerException("dict expression is null for op: " + op);

		Type dictType = dictExp.accept(this); // Verifico che dictExp sia di tipo DictType
		if (!(dictType instanceof DictType)){
			throw new TypecheckerException("Found " + dictType +" , expected DICT");
		}

		DictType dict = dictType.toDictType(dictType); // Cast esplicito a DictType

		switch (op) {
			case GET -> {
				return dict.getValueType();
						}
			case DELETE -> {
				return dict;
						}
			case UPDATE -> {
				Type valueType = valueExp.accept(this);
				dict.getValueType().checkEqual(valueType);
				return dict;
						}
			default -> throw new TypecheckerException("Unsupported operation on DICT");
		}
			
	} 
} 
	
