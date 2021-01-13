package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;

public class statement extends CParseRule {
	// statement ::= statementAssign
	private CParseRule statementAssign;

	public statement(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return lang.c.parse.statementAssign.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("Programの構文解析中です");
		statementAssign = new statementAssign(pcx);
		statementAssign.parse(pcx);

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (statementAssign != null) {
			statementAssign.semanticCheck(pcx);
			this.setCType(statementAssign.getCType());
			this.setConstant(statementAssign.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statement starts");
		if (statementAssign != null) {
			statementAssign.codeGen(pcx);
		}
		o.println(";;; statement completes");
	}
}
