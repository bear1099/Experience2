package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class minusFactor extends CParseRule {
	// minusFactor ::= MINUS unsignedFactor
	private CToken minus;
	private CParseRule factor;
	public minusFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MINUS;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		System.out.println("minusFactorの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		minus = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);
		if(unsignedFactor.isFirst(tk)) {
			factor = new unsignedFactor(pcx);
			factor.parse(pcx);
		}else {
			pcx.fatalError(tk.toExplainString() + "-の後ろはunsignedFactorです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (factor != null) {
			factor.semanticCheck(pcx);
			if(factor.getCType().getType() == CType.T_int) {
			setCType(factor.getCType());		// number の型をそのままコピー
			setConstant(factor.isConstant());	// number は常に定数
			}else {
				System.out.println(factor.getCType());
				pcx.fatalError(minus.toExplainString() + "アドレス値の前に-は付けられません");
		}
	}
}
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; minusFactor starts");
		if (factor != null) {
			factor.codeGen(pcx);
		o.println("\tMOV\t#0, (R6)+\t; 符号の反転");
		o.println("\tMOV\t-(R6), R0\t; 符号の反転");
		o.println("\tSUB\t-(R6), R0\t; 符号の反転");
		o.println("\tMOV\tR0, (R6)+\t; 符号の反転");
		}
		o.println(";;; minusFactor completes");
	}
}