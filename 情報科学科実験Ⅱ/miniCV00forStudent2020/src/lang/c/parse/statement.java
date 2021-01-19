package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class statement extends CParseRule {
	// statement ::= statementAssign | statementIF | statementWhile | statemenetInput | statementOutput
	private CParseRule stm;

	public statement(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return lang.c.parse.statementAssign.isFirst(tk)||statementIf.isFirst(tk)||statementWhile.isFirst(tk)||
				statementInput.isFirst(tk)||statementOutput.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("statementの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(statementAssign.isFirst(tk)) {
			stm = new statementAssign(pcx);
		}else if(statementIf.isFirst(tk)) {
			stm = new statementIf(pcx);
		}else if(statementWhile.isFirst(tk)) {
			stm = new statementWhile(pcx);
		}else if(statementInput.isFirst(tk)) {
			stm = new statementInput(pcx);
		}else if(statementOutput.isFirst(tk)) {
			stm = new statementOutput(pcx);
		}
		stm.parse(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (stm != null) {
			stm.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statement starts");
		if (stm != null) {
			stm.codeGen(pcx);
		}
		o.println(";;; statement completes");
	}
}
