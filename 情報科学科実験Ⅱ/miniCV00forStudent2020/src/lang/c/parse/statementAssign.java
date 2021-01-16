package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class statementAssign extends CParseRule {
	// statementAssign ::= primary ASSIGN expression SEMI
	private CParseRule primary,expression;

	public statementAssign(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return lang.c.parse.primary.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		System.out.println("StatementAssignの構文解析中です");
		primary = new primary(pcx);
		primary.parse(pcx);
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (tk.getType() == CToken.TK_ASSIGN) {
			tk = ct.getNextToken(pcx);
			if(Expression.isFirst(tk)) {
				expression = new Expression(pcx);
				expression.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				if(tk.getType() == CToken.TK_SEMI) {
					ct.getNextToken(pcx);
				}else {
					pcx.fatalError("expressionの次は';'でなければなりません");
				}
			}else {
				pcx.fatalError("'='の次はexpressionでなければなりません");
			}
		}else {
			pcx.fatalError("primaryの次は'='でなければなりません");
		}
		//System.out.println("StatementAssignの構文解析が終わりました");
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null && expression != null) {
			primary.semanticCheck(pcx);
			expression.semanticCheck(pcx);
			setCType(expression.getCType());
			setConstant(expression.isConstant());
			if(primary.getCType() != expression.getCType()) {
				pcx.fatalError("(" + primary.getCType() + ") = (" + expression.getCType() + ")は左辺と右辺の型が合致しません");
			}
			if(primary.isConstant()) {
				pcx.fatalError("定数に対して代入はできません");
			}
		}else {
			pcx.fatalError("primaryかexpressionがnullと判定されました");
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementAssign starts");
		if (primary != null && expression != null) {
			primary.codeGen(pcx);
			expression.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; StatementAssign: 右辺を取り出す");
			o.println("\tMOV\t-(R6), R1\t; StatementAssign: 左辺を取り出す");
			o.println("\tMOV\tR0, (R1)\t; StatementAssign: 左辺で取り出したアドレス値に右辺の値を代入");
		}
		o.println(";;; statementAssign completes");
	}
}
