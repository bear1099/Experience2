package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class plusFactor extends CParseRule {
	//plusFactor ::= PLUS unsignedFactor
	private CToken plus;
	private CParseRule factor;
	public plusFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_PLUS;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		System.out.println("plusFactorの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		plus = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);
		if(unsignedFactor.isFirst(tk)) {
			factor = new unsignedFactor(pcx);
			factor.parse(pcx);
		}else {
			pcx.fatalError(tk.toExplainString() + "+の後ろはunsignedFactorです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			if(factor.getCType().getType() == CType.T_int) {
			this.setCType(factor.getCType());		// number の型をそのままコピー
			this.setConstant(factor.isConstant());	// number は常に定数
			} else {
				pcx.fatalError(plus.toExplainString() + "型が違います");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; plusfactor starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; plusfactor completes");
	}
}