package projectLabo.parser;

import java.io.IOException;
import static java.util.Objects.requireNonNull;
import static projectLabo.parser.TokenType.*;
import projectLabo.parser.ast.*;
import projectLabo.parser.ast.Dict.DictOp;

/*
	Prog ::= StmtSeq EOF
	StmtSeq ::= Stmt (STMT_SEP StmtSeq)?
	Stmt ::= VAR? IDENT ASSIGN Exp | PRINT Exp | IF OPEN_ROUND_PAR Exp CLOSE_ROUND_PAR Block (ELSE Block)? | FOR OPEN_ROUND_PAR VAR IDENT OF Exp CLOSE_ROUND_PAR Block
	Block ::= OPEN_BLOCK StmtSeq CLOSE_BLOCK
	Exp ::= And (PAIR_OP And)*
	And ::= Eq (AND Eq)*
	Eq ::= Add (EQ Add)*
	Add ::= Mul (PLUS Mul)*
	Mul::= Unary (TIMES Unary)*
	Unary ::= FST Unary | SND Unary | MINUS Unary | NOT Unary | Dict
	Dict :: = Atom (OPEN_SQUARE_PAR Exp(COLUMNS Exp?)? CLOSE_SQUARE_PAR)* 
	Atom ::= OPEN_SQUARE_PAR Exp COLONS Exp CLOSE_SQUARE_PAR | BOOL | NUM | IDENT | OPEN_ROUND_PAR Exp CLOSE_ROUND_PAR
*/

public class Parser implements ParserInterface {

	private final Tokenizer tokenizer; // the tokenizer used by the parser

	// decorates error message with the corresponding line number
	private String lineErrMsg(String msg) {
		return String.format("on line %s: %s", tokenizer.getLineNumber(), msg);
	}

	/*
	 * checks whether the token type of the currently recognized token matches
	 * 'expected'; if not, it throws a corresponding ParserException
	 */
	private void match(TokenType expected) throws ParserException {
		final var found = tokenizer.tokenType();
		if (found != expected)
			throw new ParserException(
					lineErrMsg(String.format("Expecting %s, found %s('%s')", expected, found, tokenizer.tokenString())));
	}

	/*
	 * checks whether the token type of the currently recognized token matches
	 * 'expected'; if so, it reads the next token, otherwise it throws a
	 * corresponding ParserException
	 */
	private void consume(TokenType expected) throws ParserException {
		match(expected);
		tokenizer.next();
	}

	// throws a ParserException because the current token was not expected
	private <T> T unexpectedTokenError() throws ParserException {
		throw new ParserException(lineErrMsg(
				String.format("Unexpected token %s ('%s')", tokenizer.tokenType(), tokenizer.tokenString())));
	}

	// associates the parser with a corresponding non-null  tokenizer
	public Parser(Tokenizer tokenizer) {
		this.tokenizer = requireNonNull(tokenizer);
	}

	/*
	 * parses a program Prog ::= StmtSeq EOF
	 */
	@Override
	public Prog parseProg() throws ParserException {
		tokenizer.next(); // one look-ahead symbol
		final var prog = new LangProg(parseStmtSeq());
		match(EOF); // last token must have type EOF
		return prog;
	}

	@Override
	public void close() throws IOException {
		if (tokenizer != null)
			tokenizer.close();
	}

	/*
	 * parses a non-empty sequence of statements, binary operator STMT_SEP is right
	 * associative StmtSeq ::= Stmt (STMT_SEP StmtSeq)?
	 */
	private StmtSeq parseStmtSeq() throws ParserException {
		final var stmt = parseStmt();
		StmtSeq stmtSeq;
		if (tokenizer.tokenType() == STMT_SEP) {
			tokenizer.next();
			stmtSeq = parseStmtSeq();
		} else
			stmtSeq = new EmptyStmtSeq();
		return new NonEmptyStmtSeq(stmt, stmtSeq);
	}

	/*
	 * parses a statement
	 * Stmt ::= VAR? IDENT ASSIGN Exp | PRINT Exp | IF OPEN_ROUND_PAR Exp CLOSE_ROUND_PAR Block (ELSE Block)? | FOR OPEN_ROUND_PAR VAR IDENT COLONS Exp CLOSE_ROUND_PAR Block
	 */
	private Stmt parseStmt() throws ParserException {
		return switch (tokenizer.tokenType()) {
		case VAR -> parseVarStmt();
		case IDENT -> parseAssignStmt();
		case PRINT -> parsePrintStmt();
		case IF -> parseIfStmt();
		case FOR -> parseForStmt(); // Stmt ciclo for
		default -> unexpectedTokenError();
		};
	}

	/*
	 * parses the print statement
	 * Stmt ::= PRINT Exp
	 */
	private PrintStmt parsePrintStmt() throws ParserException {
		consume(PRINT); // or tokenizer.next() if the method is only called by parseStmt()
		return new PrintStmt(parseExp());
	}

	/*
	 * parses the var statement
	 * Stmt ::= VAR IDENT ASSIGN Exp
	 */
	private VarStmt parseVarStmt() throws ParserException {
		consume(VAR); // or tokenizer.next() if the method is only called by parseStmt()
		final var var = parseVariable();
		consume(ASSIGN);
		return new VarStmt(var, parseExp());
	}

	/*
	 * parses the assignment statement
	 * Stmt ::= IDENT ASSIGN Exp
	 */
	private AssignStmt parseAssignStmt() throws ParserException {
		final var var = parseVariable();
		consume(ASSIGN);
		return new AssignStmt(var, parseExp());
	}

	/*
	 * parses the if-then-else statement
	 * Stmt ::= IF OPEN_PAR Exp CLOSE_PAR Block (ELSE Block)?
	 */
	private IfStmt parseIfStmt() throws ParserException {
		consume(IF); // or tokenizer.next() since IF has already been recognized
		final var exp = parseRoundPar();
		final var thenBlock = parseBlock();
		if (tokenizer.tokenType() != ELSE)
			return new IfStmt(exp, thenBlock);
		tokenizer.next();
		return new IfStmt(exp, thenBlock, parseBlock());
	}

	/*
	 * parses the for cycle statement
	 * Stmt ::= FOR OPEN_ROUND_PAR VAR IDENT COLONS Exp CLOSE_ROUND_PAR Block
	 */
	private ForStmt parseForStmt() throws ParserException {

		consume(FOR); // or tokenizer.next() since FOR has already been recognized

		consume(OPEN_PAR);
		consume(VAR);
		final var variable = parseVariable(); // Memorizziamo la Variabile IDENT

		consume(OF);
		final var iterable = parseExp(); // Memorizziamo l'Espressione
		consume(CLOSE_PAR);

		final var body = parseBlock(); // Passiamo al blocco successivo 
    
    	return new ForStmt(variable, iterable, body); // Ritorniamo l'AST del for
	}
	
	/*
	 * parses a block of statements
	 * Block ::= OPEN_BLOCK StmtSeq CLOSE_BLOCK
	 */
	private Block parseBlock() throws ParserException {
		consume(OPEN_BLOCK);
		final var stmts = parseStmtSeq();
		consume(CLOSE_BLOCK);
		return new Block(stmts);
	}

	/*
	 * parses expressions, starting from the lowest precedence operator PAIR_OP
	 * which is left-associative
	 * Exp ::= And (PAIR_OP And)*
	 */

	private Exp parseExp() throws ParserException {
		var exp = parseAnd();
		while (tokenizer.tokenType() == PAIR_OP) {
			tokenizer.next();
			exp = new PairLit(exp, parseAnd());
		}
		return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator AND which is
	 * left-associative
	 * And ::= Eq (AND Eq)*
	 */
	private Exp parseAnd() throws ParserException {
		var exp = parseEq();
		while (tokenizer.tokenType() == AND) {
			tokenizer.next();
			exp = new And(exp, parseEq());
		}
		return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator EQ which is
	 * left-associative
	 * Eq ::= Add (EQ Add)*
	 */
	private Exp parseEq() throws ParserException {
		var exp = parseAdd();
		while (tokenizer.tokenType() == EQ) {
			tokenizer.next();
			exp = new Eq(exp, parseAdd());
		}
		return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator PLUS which
	 * is left-associative
	 * Add ::= Mul (PLUS Mul)*
	 */
	private Exp parseAdd() throws ParserException {
		var exp = parseMul();
		while (tokenizer.tokenType() == PLUS) {
			tokenizer.next();
			exp = new Add(exp, parseMul());
		}
		return exp;
	}

	/*
	 * parses expressions, starting from the lowest precedence operator TIMES which
	 * is left-associative
	 * Mul::= Unary (TIMES Unary)*
	 */
	private Exp parseMul() throws ParserException {
		var exp = parseUnary();
		while (tokenizer.tokenType() == TIMES) {
			tokenizer.next();
			exp = new Mul(exp, parseUnary());
		}
		return exp;
	}

	/*
	 * parses expressions of type Unary
	 * Unary ::= FST Unary | SND Unary | MINUS Unary | NOT Unary | Dict
	 */
	private Exp parseUnary() throws ParserException {
		return switch (tokenizer.tokenType()) {
		case FST -> parseFst();
		case SND -> parseSnd();
		case MINUS -> parseMinus();
		case NOT -> parseNot();
		/*****CASI GESTITI IN DICT*****/
		case OPEN_DICT, BOOL, NUM, IDENT, OPEN_PAR -> parseDict(); 
		/*******************************/
		default -> unexpectedTokenError();
		};
	}

	/*
	 * parses expressions of type Dict
	 * Dict ::= Atom (OPEN_SQUARE_PAR Exp(COLONS Exp?)? CLOSE_SQUARE_PAR)* 
	 */
	private Exp parseDict() throws ParserException{
		Exp exp = parseAtom(); // Atom

		if(tokenizer.tokenType() != OPEN_DICT){return exp;} // Se solo Atom

		while(tokenizer.tokenType() == OPEN_DICT){ // Se * > 0
			consume(OPEN_DICT);
			Exp key = parseExp(); // Indice Dizionario

			if(tokenizer.tokenType() == DICT_OP){ // Se non e' Get
				consume(DICT_OP);
				if(tokenizer.tokenType() != CLOSE_DICT){ // Se Esiste un exp dopo i due punti, Update
					Exp value = parseExp();
					consume(CLOSE_DICT);
					exp = new Dict(DictOp.UPDATE, exp, key, value);
				} else{
					consume(CLOSE_DICT); // Delete
					exp = new Dict(DictOp.DELETE, exp, key);
				}
			} else{
				consume(CLOSE_DICT);
				exp = new Dict(DictOp.GET, exp, key);
			}
		}

		return exp; 
	}


	/*
	 * parses expressions of type Atom
	 * Atom ::= OPEN_SQUARE_PAR Exp COLONS Exp CLOSE_SQUARE_PAR | BOOL | NUM | IDENT | OPEN_ROUND_PAR Exp CLOSE_ROUND_PAR
	 */
	private Exp parseAtom() throws ParserException {
		return switch (tokenizer.tokenType()) {
		case OPEN_DICT -> parseSquarePar(); // Nuovo Caso [ Exp : Exp ] 
		case BOOL -> parseBoolean();
		case NUM -> parseNum();
		case IDENT -> parseVariable();
		case OPEN_PAR -> parseRoundPar();
		default -> unexpectedTokenError();
		};
	}

	// parses number literals
	private IntLiteral parseNum() throws ParserException {
		match(NUM); // can be omitted if the method is only called by parseAtom()
		final var val = tokenizer.intValue();
		tokenizer.next(); // if tokenizer.intValue() does not throw an exception, then NUM has been recognized
		return new IntLiteral(val);
	}

	// parses boolean literals
	private BoolLiteral parseBoolean() throws ParserException {
		match(BOOL); // can be omitted if the method is only called by parseAtom()
		final var val = tokenizer.boolValue();
		tokenizer.next(); // if tokenizer.boolValue() does not throw an exception, then BOOL has been recognized
		return new BoolLiteral(val);
	}

	// parses variable identifiers
	private Variable parseVariable() throws ParserException {
		final var name = tokenizer.tokenString();
		consume(IDENT); // this check is necessary for parsing correctly the VAR statement
		return new Variable(name);
	}

	/*
	 * parses expressions with unary operator MINUS
	 * Atom ::= MINUS Atom
	 */
	private Minus parseMinus() throws ParserException {
		consume(MINUS); // can be omitted if the method is only called by parseAtom()
		return new Minus(parseDict());// Modificato per gestire casi -var[dict]
	}

	/*
	 * parses expressions with unary operator FST
	 * Atom ::= FST Atom
	 */
	private Fst parseFst() throws ParserException {
		consume(FST); // can be omitted if the method is only called by parseAtom()
		return new Fst(parseAtom());
	}

	/*
	 * parses expressions with unary operator SND
	 * Atom ::= SND Atom
	 */
	private Snd parseSnd() throws ParserException {
		consume(SND); // can be omitted if the method is only called by parseAtom()
		return new Snd(parseAtom());
	}

	/*
	 * parses expressions with unary operator NOT
	 * Atom ::= NOT Atom
	 */
	private Not parseNot() throws ParserException {
		consume(NOT); // can be omitted if the method is only called by parseAtom()
		return new Not(parseDict()); // Modificato per gestire casi !var[dict]
	}

	/*
	 * parses expressions delimited by round parentheses
	 * Atom ::= OPEN_ROUND_PAR Exp CLOSE_ROUND_PAR
	 */
	private Exp parseRoundPar() throws ParserException {
		consume(OPEN_PAR); // this check is necessary for parsing correctly the if-then-else statement
		final var exp = parseExp();
		consume(CLOSE_PAR);
		return exp;
	}

	/*
	 * parses expressions delimited by square parentheses
	 * Atom ::= OPEN_SQUARE_PAR Exp COLONS Exp CLOSE_SQUARE_PAR
	 */
	private Exp parseSquarePar() throws ParserException {
		consume(OPEN_DICT);
		Exp key = parseExp(); // Otteniamo la prima espressione

		consume(DICT_OP);

		Exp value = parseExp(); // Otteniamo la seconda espressione
		consume(CLOSE_DICT);

		return new Dict(DictOp.CREATE, null, key, value); // Creo il dizionario 
	}
}