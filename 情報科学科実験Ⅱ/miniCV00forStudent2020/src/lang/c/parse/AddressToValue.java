package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

public class AddressToValue extends CParseRule {
	// addressToValue ::= primary

	private CParseRule primary;
	public AddressToValue(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return lang.c.parse.primary.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		//System.out.println("AddressToValueの構文解析中です");
		primary = new primary(pcx);
		primary.parse(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(primary != null) {
			primary.semanticCheck(pcx);
			setCType(primary.getCType());
			setConstant(primary.isConstant());	// number は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; addressToValue starts");
		if (primary != null) { primary.codeGen(pcx); }
		o.println("\tMOV\t-(R6), R0\t; AddressToValue: スタックからアドレスを取り出す");
		o.println("\tMOV\t(R0), (R6)+\t; AddressToValue: アドレスから値を取り出しスタックへ");
		o.println(";;; addressToValue completes");
	}
}