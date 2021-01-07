package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Term extends CParseRule {
	// term ::= factor {termMult | termDiv}
	private CParseRule term;
	public Term(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Factor.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CParseRule list = null, factor = null;
		factor = new Factor(pcx);
		factor.parse(pcx);
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		while(termMult.isFirst(tk) || termDiv.isFirst(tk)) {
			if(termMult.isFirst(tk)) {
				list = new termMult(pcx,factor);
			}else if(termDiv.isFirst(tk)) {
				list = new termDiv(pcx,factor);
			}
			list.parse(pcx);
			factor = list;
			tk = ct.getCurrentToken(pcx);
		}
		term = factor;
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (term != null) {
			term.semanticCheck(pcx);
			this.setCType(term.getCType());		// factor の型をそのままコピー
			this.setConstant(term.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; term starts");
		if (term != null) { term.codeGen(pcx); }
		o.println(";;; term completes");
	}
}
