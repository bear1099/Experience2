package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class termMult extends CParseRule {
	// termMult ::= MULT factor
	private CToken mult;
	private CParseRule left,right;
	public termMult(CParseContext pcx, CParseRule left) {
		this.left = left;
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MUL;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("termMULの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		mult = ct.getCurrentToken(pcx);
		CToken tk = ct.getNextToken(pcx);

		if(Factor.isFirst(tk)) {
			right = new Factor(pcx);
			right.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + " * の後ろはfactorです");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		//乗算の型計算
		final int s[][] = {
                //		T_err				T_int				T_pint			T_intArray		T_pintArray
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
				pcx.fatalError(mult.toExplainString() + "左辺の型[" + left.getCType().toString() + "]と右辺の型[" + right.getCType().toString() + "]は演算できません");
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
			o.println("\tJSR\tMULT\t ; termMult:サブルーチンを呼び出す");
			o.println("\tSUB\t#2, R6 ; termMult:積んだ引数を捨てる");
			o.println("\tMOV\tR0, (R6)+; termMult:乗算の結果をスタックに積む");
		}
	}
}
