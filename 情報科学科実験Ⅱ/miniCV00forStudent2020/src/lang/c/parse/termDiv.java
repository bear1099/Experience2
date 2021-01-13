package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class termDiv extends CParseRule {
	// termDiv ::= DIV factor
	private CToken div;
	private CParseRule left,right;
	public termDiv(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_DIV;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("termDivの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		div = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);

		if(Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + " / の後ろはfactorです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		//除算の型計算
		final int s[][] = {
//				R	T_err				T_int				T_pint			T_intArray		T_pintArray		L
                {	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_err
                {	CType.T_err,	CType.T_int,	CType.T_err,	CType.T_err,	CType.T_err},	// T_int
                {	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	// T_pint
                {	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	//T_intArray
                {	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err,	CType.T_err},	//T_pintArray
		};
		if(left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			int lt = left.getCType().getType();
			int rt = right.getCType().getType();
			int nt = s[lt][rt];
			if(nt == CType.T_err) {
				pcx.fatalError(div.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は演算できません");
			}
			this.setCType(CType.getCType(nt));
			this.setConstant(left.isConstant() && right.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		if(left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tJSR\tDIV\t ; termDiv:サブルーチンを呼び出す");
			o.println("\tSUB\t#2, R6 ; termDiv:積んだ引数を捨てる");
			o.println("\tMOV\tR0, (R6)+; termDiv:除算の結果をスタックに積む");
		}
	}
}
