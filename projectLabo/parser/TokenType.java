package projectLabo.parser;

public enum TokenType {
	// used internally by the tokenizer, should never be accessed by the parser
	KEYWORD, SKIP, SYMBOL,
	// non singleton categories
	IDENT, NUM,
	// end-of-file
	EOF,
	// symbols
	AND, ASSIGN, CLOSE_BLOCK, CLOSE_DICT, CLOSE_PAR, EQ, MINUS, NOT, OPEN_BLOCK, OPEN_PAR, OPEN_DICT, PAIR_OP, PLUS, STMT_SEP, DICT_OP, TIMES,
	// keywords
	BOOL, ELSE, FST, IF, PRINT, SND, VAR, FOR, OF, DICT
}
