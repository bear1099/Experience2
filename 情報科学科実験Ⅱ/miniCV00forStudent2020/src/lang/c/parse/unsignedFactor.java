package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class unsignedFactor extends CParseRule {
	// unsignedFactor ::= factorAMP | child | LPAR expression RPAR | addreessToValue
	private CParseRule child;
	public unsignedFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk) || tk.getType() == CToken.TK_LPAR;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(FactorAmp.isFirst(tk)) {
			child = new FactorAmp(pcx);
			child.parse(pcx);
		} else if(Number.isFirst(tk)) {
			child = new Number(pcx);
			child.parse(pcx);
		} else if(tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
			if(Expression.isFirst(tk)) {
				child = new Expression(pcx);
				child.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				if(tk.getType() == CToken.TK_RPAR) {
					tk = ct.getNextToken(pcx);
				} else {
					pcx.fatalError(tk.toExplainString() + "expression の後ろが')'以外の文字になっています");
				}
			} else {
				pcx.fatalError(tk.toExplainString() + "'('の後ろがexpressionではないです");
			}
		}else if(AddressToValue.isFirst(tk)) {
			child = new AddressToValue(pcx);
			child.parse(pcx);
		}
	}
	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (child != null) {
			child.semanticCheck(pcx);
			setCType(child.getCType());		// child の型をそのままコピー
			setConstant(child.isConstant());	// child は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; unsignedFactor starts");
		if (child != null) { child.codeGen(pcx); }
		o.println(";;; unsignedfactor completes");
	}
}