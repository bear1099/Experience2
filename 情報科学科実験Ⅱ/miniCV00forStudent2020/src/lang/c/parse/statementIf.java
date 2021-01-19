package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class statementIf extends CParseRule {
	// statementWhile ::= IF LPAR condition RPAR LCUR {statement} RCUR [statementElse]
	private CParseRule condition;
	private CParseRule statement;
	private CParseRule statementelse;
	private ArrayList<CParseRule> list;
	private int seq;

	public statementIf(CParseContext pcx) {

	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_IF;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("statementIFの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		tk = ct.getNextToken(pcx);
		list = new ArrayList<CParseRule>();
		if(tk.getType() == CToken.TK_LPAR) {
			//System.out.println("(を確認しました");
			tk = ct.getNextToken(pcx);
			//System.out.println("次のトークンを解析します");
			if(Condition.isFirst(tk)) {
				//System.out.println("Conditionを確認しました");
				condition = new Condition(pcx);
				condition.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				//System.out.println("次のトークンを解析します");
				if(tk.getType() == CToken.TK_RPAR) {
					//System.out.println(")を確認しました");
					tk = ct.getNextToken(pcx);
					if(tk.getType() == CToken.TK_LCUR) {
						//System.out.println("{を確認しました");
						tk = ct.getNextToken(pcx);
						while(lang.c.parse.statement.isFirst(tk)) {
							//System.out.println("Statementを確認しました");
							statement = new statement(pcx);
							statement.parse(pcx);
							list.add(statement);
							tk = ct.getCurrentToken(pcx);
						}
						tk = ct.getCurrentToken(pcx);
						if(tk.getType() == CToken.TK_RCUR) {
							//System.out.println("}を確認しました");
							tk = ct.getNextToken(pcx);
							if(statementElse.isFirst(tk)) {
								//System.out.println("StatementElseの構文解析を始めます");
								statementelse = new statementElse(pcx);
								statementelse.parse(pcx);
							}
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
		if(statementelse != null) {
			statementelse.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementIf starts");
		if(condition != null) { condition.codeGen(pcx); }
		seq = pcx.getSeqId();
		int tmpseq = seq;
		o.println("\tMOV\t-(R6), R0\t; StatementIf: フラグを取り出す");
		o.println("\tCMP\t#0x0000, R0\t; StatementIf: 0と比較");
		o.println("\tBRZ\tELSE" + seq + "\t; StatementIf: falseならジャンプ");
		for(CParseRule stm : list) {
			stm.codeGen(pcx);
		}
		seq = pcx.getSeqId();
		o.println("\tJMP\tEND" + seq + "\t; StatementIf: 強制的にジャンプ");
		o.println("ELSE" + tmpseq + ":\t; StatementIf: falseならジャンプ");
		if(statementelse != null) { statementelse.codeGen(pcx); }
		o.println("END" + seq + ":\t; StatementIf:");
		o.println(";;; statementIf completes");
	}
}
