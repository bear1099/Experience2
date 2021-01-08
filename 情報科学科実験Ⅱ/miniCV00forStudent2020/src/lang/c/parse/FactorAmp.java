package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class FactorAmp extends CParseRule {
	// factorAmp ::= Amp ( number | primary )
	private CParseRule number;
	private CParseRule primary;
	private CParseRule temp;
	private CToken amp;
	private int type;
	public FactorAmp(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AMP;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		//System.out.println("FactorAMPの構文解析中です");
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		amp = tk;
		tk = ct.getNextToken(pcx);
		if(Number.isFirst(tk)){
			number = new Number(pcx);
			number.parse(pcx);
		}else if(lang.c.parse.primary.isFirst(tk)) {
			primary = new primary(pcx);
			primary.parse(pcx);
		}else {
			pcx.error(tk.toExplainString() + " '&'の次にnumberまたはprimaryがありません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(CType.getCType(CType.T_pint));
			setConstant(number.isConstant());	// number は常に定数
		}
		if (primary != null) {
			primary.semanticCheck(pcx);
			temp = ((primary)primary).getCPR();
			//System.out.println(temp);
			if(temp instanceof primaryMult) {
				pcx.fatalError("&の後ろに*を置くことはできません");
			}else if(temp instanceof variable) {
				type = ((variable) temp).getIdent().getType();
				if(type == 2 || type == 4) {
					pcx.fatalError("&の後ろにポイント型のIdentを置くことはできません");
				}
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factorAmp starts");
		if (number != null) { number.codeGen(pcx); }
		if( primary != null) { primary.codeGen(pcx); }
		o.println(";;; factorAmp completes");
	}
}