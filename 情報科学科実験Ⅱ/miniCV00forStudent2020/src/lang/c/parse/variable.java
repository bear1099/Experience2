package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class variable extends CParseRule {
	// variable ::= ident [ array ]
	private CParseRule ident;
	private CParseRule array;
	public variable(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return lang.c.parse.ident.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		System.out.println("variableの構文解析中です");
		array = null;
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(lang.c.parse.ident.isFirst(tk)) {
			ident = new ident(pcx);
			ident.parse(pcx);
		}
		tk = ct.getCurrentToken(pcx);
		if(Array.isFirst(tk)) {
			array = new Array(pcx);
			array.parse(pcx);
		}

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
			if (array != null) {
				array.semanticCheck(pcx);
				if(ident.getCType().getType() == CType.T_apint) {
					setCType(CType.getCType(CType.T_pint));
				}else if(ident.getCType().getType() == CType.T_aint){
					setCType(CType.getCType(CType.T_int));
				}else {
					pcx.fatalError("不適切な型です");
				}
			}else {
				setCType(ident.getCType());
			}
		}
		setConstant(ident.isConstant());
	}

	public CParseRule getCPR() {
		return array;
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; variable starts");
		if (ident != null) { ident.codeGen(pcx); }
		if (array != null) {
			array.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; Variable: 値を取り出して");
			o.println("\tADD\t-(R6), R0\t; Variable: 配列の先頭番地を取り出し、格納されている番地を計算");
			o.println("\tMOV\tR0, (R6)+\t; Variable: 番地の値をスタックに戻す");
		}
		o.println(";;; variable completes");
	}
}