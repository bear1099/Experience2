package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ident extends CParseRule {
	// ident ::= IDENT
	private CToken ident;
	public ident(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_IDENT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		System.out.println("identの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		ident = tk;
		//System.out.println("ident : " + ident.toExplainString());
		tk = ct.getNextToken(pcx);
		//System.out.println("NextToken : " + tk.toExplainString());
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(ident != null) {
			this.setCType(CType.getCType(CType.T_int));
			this.setConstant(false);
		}
	}

	public void codeGen(CParseContext pcx) {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; ident starts");
		if (ident != null) {
			o.println("\tMOV\t#" + ident.getText() + ", (R6)+\t; Ident: 変数アドレスを積む<"
					+ ident.toExplainString() + ">");
		}
		o.println(";;; ident completes");
	}
}
