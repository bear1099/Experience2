package lang.c;

import lang.SimpleToken;

public class CToken extends SimpleToken {
	public static final int TK_PLUS			= 2;				// +
	public static final int TK_MINUS			= 3;				// -
	public static final int TK_AMP			= 4; 				// &
	public static final int TK_MUL			= 5;				// *
	public static final int TK_DIV			= 6;				// /
	public static final int TK_LPAR			= 7;				// (
	public static final int TK_RPAR			= 8;				// )
	public static final int TK_LBRA			= 9;				// [
	public static final int TK_RBRA			= 10;				// ]
	public static final int TK_IDENT 		= 11;				//識別子
	public static final int TK_ASSIGN		= 12;				// =
	public static final int TK_SEMI			= 13;				// ;
	public static final int TK_EQ			= 14;				// ==
	public static final int TK_LT			= 15;				// <
	public static final int TK_LE			= 16;				// <=
	public static final int TK_GT			= 17;				// >
	public static final int TK_GE			= 18;				// >=
	public static final int TK_NE			= 19;				// !=
	public static final int TK_TRUE			= 20;				// true
	public static final int TK_FALSE			= 21;				// false
	public static final int TK_LCUR			= 22;				// {
	public static final int TK_RCUR			= 23;				// }
	public static final int TK_INPUT			= 24;				// input
	public static final int TK_OUTPUT		= 25;				// output
	public static final int TK_IF			= 26;				// if
	public static final int TK_ELSE			= 27;				// else
	public static final int TK_WHILE			= 28;				// while



	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
