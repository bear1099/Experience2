package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class primaryMult extends CParseRule {
	// primaryMult ::= MUL variable
	private CToken op;
	private CParseRule child;
	public primaryMult(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MUL;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("primaryMultの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		op = tk;
		tk = ct.getNextToken(pcx);
		if(variable.isFirst(tk)) {
			child = new variable(pcx);
			child.parse(pcx);
		}else {
			pcx.error(tk.toExplainString() + " '*'の次にvariableがありません");
		}

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (child != null) {
			child.semanticCheck(pcx);
			if(child.getCType().getType() == CType.T_apint) {
				setCType(CType.getCType(CType.T_pint));
			}
			if(child.getCType().getType() == CType.T_aint){
				pcx.fatalError("エラー");
			}
			if(child.getCType().getType() == CType.T_apint){
				pcx.fatalError("エラー");
			}else {
				if(child.getCType().getType() != CType.T_int) {
					setCType(CType.getCType(CType.T_int));
				}else {
					pcx.fatalError("*をint型変数につけないでください。");
				}
			}
			setConstant(child.isConstant());
		}

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if (child != null) {
			child.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; PrimaryMult: アドレスを取り出して、内容を参照して、積む<"
					+ op.toExplainString() + ">");
			o.println("\tMOV\t(R0), (R6)+\t; PrimaryMult:");
		}
	}
}