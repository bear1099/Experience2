package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class statementOutput extends CParseRule {
	// statement ::= OUTPUT expression SEMI
	private CParseRule expression;

	public statementOutput(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_OUTPUT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		System.out.println("StatementOutputの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		tk = ct.getNextToken(pcx);
		if(Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
		}else {
			pcx.error(tk.toExplainString() + " expressionがありません ");
		}
		tk = ct.getCurrentToken(pcx);
		if(tk.getType() == CToken.TK_SEMI) {
			ct.getNextToken(pcx);
		}else {
			pcx.error(tk.toExplainString() + ";がありません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {expression.semanticCheck(pcx);}
	}
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementOutput starts");
		if(expression != null) { expression.codeGen(pcx); }
		o.println("\tMOV\t#0xFFE0, R1\t; StatementOutput: 0xFFE0をR1に");
		o.println("\tMOV\t-(R6), (R1)\t; StatementOutput: 出力値を#0xFFE0を番地を参照して出力");
		o.println(";;; statementOutput completes");
	}
}
