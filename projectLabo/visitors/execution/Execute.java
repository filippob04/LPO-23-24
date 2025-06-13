package projectLabo.visitors.execution;

import java.io.PrintWriter;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import projectLabo.environments.EnvironmentException;
import projectLabo.parser.ast.Block;
import projectLabo.parser.ast.Dict;
import projectLabo.parser.ast.Exp;
import projectLabo.parser.ast.Stmt;
import projectLabo.parser.ast.StmtSeq;
import projectLabo.parser.ast.Variable;
import projectLabo.visitors.Visitor;

public class Execute implements Visitor<Value> {

	private final DynamicEnv env = new DynamicEnv();
	private final PrintWriter printWriter; // output stream used to print values

	public Execute() {
		printWriter = new PrintWriter(System.out, true);
	}

	public Execute(PrintWriter printWriter) {
		this.printWriter = requireNonNull(printWriter);
	}

	// dynamic semantics for programs; no value returned by the visitor

	@Override
	public Value visitLangProg(StmtSeq stmtSeq) {
		try {
			stmtSeq.accept(this);
			// possible runtime errors
			// EnvironmentException: undefined variable
		} catch (EnvironmentException e) {
			throw new InterpreterException(e);
		}
		return null;
	}

	// dynamic semantics for sequences of statements
	// no value returned by the visitor

	@Override
	public Value visitEmptyStmtSeq() {
		return null;
	}

	@Override
	public Value visitNonEmptyStmtSeq(Stmt first, StmtSeq rest) {
		first.accept(this);
		rest.accept(this);
		return null;
	}

	// dynamic semantics for statements; no value returned by the visitor

	@Override
	public Value visitIfStmt(Exp exp, Block thenBlock, Block elseBlock) {
		if (exp.accept(this).toBool())
			thenBlock.accept(this);
		else if (elseBlock != null)
			elseBlock.accept(this);
		return null;
	}

	@Override
	public Value visitPrintStmt(Exp exp) {
		printWriter.println(exp.accept(this));
		return null;
	}

	@Override
	public Value visitVarStmt(Variable var, Exp exp) {
		env.dec(var, exp.accept(this));
		return null;
	}

	@Override
	public Value visitBlock(StmtSeq stmtSeq) {
		env.enterLevel();
		stmtSeq.accept(this);
		env.exitLevel();
		return null;
	}

	// dynamic semantics of expressions; a value is returned by the visitor

	@Override
	public IntValue visitAdd(Exp left, Exp right) {
		return new IntValue(left.accept(this).toInt() + right.accept(this).toInt());
	}

	@Override
	public BoolValue visitBoolLiteral(boolean value) {
		return new BoolValue(value);
	}

	@Override
	public BoolValue visitEq(Exp left, Exp right) {
		return new BoolValue(left.accept(this).equals(right.accept(this)));
	}

	@Override
	public Value visitFst(Exp exp) {
		return exp.accept(this).toPair().fstVal();
	}

	@Override
	public IntValue visitIntLiteral(int value) {
		return new IntValue(value);
	}

	@Override
	public IntValue visitMinus(Exp exp) {
		return new IntValue(-exp.accept(this).toInt());
	}

	@Override
	public IntValue visitMul(Exp left, Exp right) {
		return new IntValue(left.accept(this).toInt() * right.accept(this).toInt());
	}

	@Override
	public PairValue visitPairLit(Exp left, Exp right) {
		return new PairValue(left.accept(this), right.accept(this));
	}

	@Override
	public Value visitSnd(Exp exp) {
		return exp.accept(this).toPair().sndVal();
	}

	@Override
	public Value visitVariable(Variable var) {
		return env.lookup(var);
	}

	@Override
	public BoolValue visitNot(Exp exp){
		return new BoolValue(!exp.accept(this).toBool()); // Ritorniamo un oggetto di tipo Booleano ! con il check toBool
	}

	@Override
	public BoolValue visitAnd(Exp left, Exp right){
		return new BoolValue(left.accept(this).toBool() && right.accept(this).toBool()); // Ritorniamo un oggetto di tipo Booleano controllato con && e il check toBool
	}

	@Override
	public Value visitAssignStmt(Variable var, Exp exp) { // Simile a visitVarStmt ma piuttosto che dichiararla la aggiorniamo
		env.update(var, exp.accept(this)); // Update gestisce autonomamente il caso in cui var non sia stata dichiarata in precedenza
		return null;
	}

	//********************************/NUOVI METODI IMPLEMENTATI/*******************************************//

	@Override
	public Value visitForStmt(Variable var, Exp exp, Block forBlock){

		Value expVal = exp.accept(this);
		if(!(expVal instanceof DictValue dictVal)) {
			throw new InterpreterException("expected a dictionary in 'for' loop.");
		}

		env.enterLevel(); // Entra in nuovo scope (lo scope del ciclo)

		for(Map.Entry<Integer, Value> i : dictVal.getMap().entrySet()){ // Gestito in questo modo poiche' le chiavi non sono in ordine contiguo (crescente ma non contiguo)
			Value pair = new PairValue(new IntValue(i.getKey()), i.getValue()); // Crea coppia chiave-valore

			try{
            	env.dec(var, pair); 	// Dichiarazione iniziale
        	} catch (EnvironmentException e) {
            	env.update(var, pair); // Se già dichiarata, aggiorna
        	}

			forBlock.accept(this); // Esegui il blocco del ciclo con questa associazione
		}

		env.exitLevel(); // Esci dallo scope del ciclo

		return null;
	}


	@Override
	public Value visitDict(Dict.DictOp op, Exp dictExp, Exp keyExp, Exp valueExp){
		switch(op){
			case CREATE -> { // Per prima cosa verifico se si tratta di una create (dictExp e' null)
				Value keyVal = keyExp.accept(this);
				Value valueVal = valueExp.accept(this);
				if (!(keyVal instanceof IntValue)) {
					throw new InterpreterException("dict keys must be integers");
				}
				DictValue dict = new DictValue(); // Creo il nuovo dizionario
				dict.update(keyVal.toInt(), valueVal);
				return dict; // ritorno il nuovo dizionario
			}
			default -> {
				if(dictExp == null){ // se non si tratta di ua create e dict e' null
					throw new InterpreterException("dictExp is null in visitDict");
				}

				// System.out.println("dictExp class: " + (dictExp == null ? "null" : dictExp.getClass().getSimpleName()));
				Value dictVal = dictExp.accept(this);
				if (dictVal == null) {
					throw new InterpreterException("dictVal is null in visitDict");
				}
				// System.out.println("dictVal class: " + dictVal.getClass().getSimpleName());
				if(!(dictVal instanceof DictValue dict)){ // se non e' un dizionario
					throw new InterpreterException("Expected a dictionary in dict operation.");
				}

				Value keyVal = keyExp.accept(this);
				if(!(keyVal instanceof IntValue)){ // se la key non e' un INT
					throw new InterpreterException("dict keys must be integers");
				}

				int key = keyVal.toInt();

				switch (op){
					case GET -> {
						if(!dict.containsKey(key)){
							throw new InterpreterException("Missing key " + key);
						}
						return dict.get(key);
					}

					case DELETE -> {
						if(!dict.containsKey(key)){
							throw new InterpreterException("Missing key " + key);
						}
						
						DictValue newDict = dict.copy();  
						newDict.delete(key);              // modifica la copia
						return newDict;                   // ritorna la copia
					}

					case UPDATE -> {
						if(valueExp == null){
							throw new InterpreterException("Missing value for UPDATE operation");
						}
						Value value = valueExp.accept(this);

						DictValue newDict = dict.copy();  
						newDict.update(key, value);       // modifica la copia
						return newDict;                   // ritorna la copia
					}

					default -> throw new InterpreterException("Unsupported operation on dict");
				}
			}
		}
	}
}
