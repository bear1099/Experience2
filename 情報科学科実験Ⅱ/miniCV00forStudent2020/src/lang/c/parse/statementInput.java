package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class statementInput extends CParseRule {
	// statement ::= Input (primary|factorAMP) SEMI
	private CParseRule primary;
	private CParseRule factorAMP;

	public statementInput(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_INPUT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("StatementInputの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		tk = ct.getNextToken(pcx);
		if(lang.c.parse.primary.isFirst(tk)) {
			primary = new primary(pcx);
			primary.parse(pcx);
		}else if(FactorAmp.isFirst(tk)){
			factorAMP = new FactorAmp(pcx);
			factorAMP.parse(pcx);
		}else {
			pcx.error(tk.toExplainString() + " PrimaryかfactorAMPでなければなりません");
		}
		tk = ct.getCurrentToken(pcx);
		if(tk.getType() == CToken.TK_SEMI) {
			ct.getNextToken(pcx);
		}else {
			pcx.error(tk.toExplainString() + ";がありません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null) {primary.semanticCheck(pcx);}
		if(factorAMP != null) {factorAMP.semanticCheck(pcx);}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementInput starts");
		if(primary != null) { primary.codeGen(pcx); }
		if(factorAMP != null) { factorAMP.codeGen(pcx); }
		o.println("\tMOV\t#0xFFE0, R1\t; StatementInput: 0xFFE0をR1に");
		o.println("\tMOV\t-(R6), R0\t; StatementInput:");
		o.println("\tMOV\t(R1), (R0)\t; StatementInput: 入力値を指定番地に入れる");
		o.println(";;; statementInput completes");
	}
}
