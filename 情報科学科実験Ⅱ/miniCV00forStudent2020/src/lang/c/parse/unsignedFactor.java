package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class unsignedFactor extends CParseRule {
	// unsignedFactor ::= factorAMP | number | LPAR ecpression RPAR
	private CParseRule number;
	public unsignedFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk) || tk.getType() == CToken.TK_LPAR;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(FactorAmp.isFirst(tk)) {
			number = new FactorAmp(pcx);
			number.parse(pcx);
		} else if(Number.isFirst(tk)) {
			number = new Number(pcx);
			number.parse(pcx);
		} else if(tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
			if(Expression.isFirst(tk)) {
				number = new Expression(pcx);
				number.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				if(tk.getType() == CToken.TK_RPAR) {
					tk = ct.getNextToken(pcx);
				} else {
					pcx.fatalError(tk.toExplainString() + "expression の後ろが')'以外の文字になっています");
				}
			} else {
				pcx.fatalError(tk.toExplainString() + "'('の後ろはexpressionではないです");
			}
		}
	}
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(number.getCType());		// number の型をそのままコピー
			setConstant(number.isConstant());	// number は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; unsignedFactor starts");
		if (number != null) { number.codeGen(pcx); }
		o.println(";;; unsignedfactor completes");
	}
}