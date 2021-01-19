package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class statementWhile extends CParseRule {
	// statementWhile ::= WHILE LPAR condition RPAR LCUR {statement} RCUR
	private CParseRule condition;
	private CParseRule statement;
	private ArrayList<CParseRule> list;
	private int seq;

	public statementWhile(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_WHILE;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("StatementWhileの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		tk = ct.getNextToken(pcx);
		list = new ArrayList<CParseRule>();
		if(tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
			if(Condition.isFirst(tk)) {
				condition = new Condition(pcx);
				condition.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				if(tk.getType() == CToken.TK_RPAR) {
					tk = ct.getNextToken(pcx);
					if(tk.getType() == CToken.TK_LCUR) {
						tk = ct.getNextToken(pcx);
						while(lang.c.parse.statement.isFirst(tk)) {
							statement = new statement(pcx);
							statement.parse(pcx);
							list.add(statement);
							tk = ct.getCurrentToken(pcx);
						}
						tk = ct.getCurrentToken(pcx);
						if(tk.getType() == CToken.TK_RCUR) {
							ct.getNextToken(pcx);
						}else {
							pcx.fatalError(tk.toExplainString() + " '}'がありません");
						}
					}else {
						pcx.fatalError(tk.toExplainString() + " '{'がありません");
					}
				}else {
					pcx.fatalError(tk.toExplainString() + " ')'がありません");
				}
			}else {
				pcx.fatalError(tk.toExplainString() + " condition がありません");
			}
		}else {
			pcx.fatalError(tk.toExplainString() + " '('がありません");
		}

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
		}
		for(CParseRule stm : list) {
			if(stm != null) stm.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		int tmpseq;
		o.println(";;; statementWhile starts");
		seq = pcx.getSeqId();
		tmpseq = seq;
		o.println("WHILE" + seq + ":\t; StatementWhile:");
		if(condition != null) { condition.codeGen(pcx); }
		o.println("\tMOV\t-(R6), R0\t; StatementWhile: フラグを取り出す");
		o.println("\tCMP\t#0x0000, R0\t; StatementWhile: 0と比較");
		seq = pcx.getSeqId();
		o.println("\tBRZ\tELSE" + seq + "\t; StatementWhile: falseならジャンプ");
		for(CParseRule item : list) {
			if(item != null) item.codeGen(pcx);
		}
		o.println("\tJMP\tWHILE"+ tmpseq +"\t;StatementWhile: ");
		o.println("Else" + seq + ":\t; StatementWhile:");
		o.println(";;; statementWhile completes");
	}
}
