package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.Tokenizer;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	@SuppressWarnings("unused")
	private CTokenRule	rule;
	private int			lineNo, colNo;
	private char		backCh;
	private boolean		backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.rule = rule;
		lineNo = 1; colNo = 1;
	}

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
//		System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}
	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}
	// 次のトークンを読んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
//		System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}
	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
			case 0:					// 初期状態
				ch = readChar();
				if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
				} else if (ch == (char) -1) {	// EOF
					startCol = colNo - 1;
					state = 1;
				} else if (ch >= '1' && ch <= '9') {
					startCol = colNo - 1;
					text.append(ch);
					state = 3;
				} else if(ch == '0') {
					startCol = colNo - 1;
					text.append(ch);
					state = 13;
				}else if(ch == '+') {
					startCol = colNo - 1;
					text.append(ch);
					state = 5;
				} else if (ch == '-') {
					startCol = colNo - 1;
					text.append(ch);
					state = 6;
				} else if (ch == '/') {
					text.append(ch);
					state = 7;
				} else if (ch == '&'){
					startCol = colNo - 1;
					text.append(ch);
					state = 12;
				} else if (ch == '*') {
					startCol = colNo - 1;
					text.append(ch);
					state = 17;
				} else if (ch == '(') {
					startCol = colNo - 1;
					text.append(ch);
					state = 18;
				} else if (ch == ')') {
					startCol = colNo - 1;
					text.append(ch);
					state = 19;
				} else {			// ヘンな文字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 2;
				}
				break;
			case 1:					// EOFを読んだ
				tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
				accept = true;
				break;
			case 2:					// ヘンな文字を読んだ
				tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
				accept = true;
				break;
			case 3:					// 数（10進数）の開始
				ch = readChar();
				if (Character.isDigit(ch)) {
					text.append(ch);
				} else {
					backChar(ch);
					state = 4;
				}
				break;
			case 4:					//数が終わった状態
				tk = new CToken(CToken.TK_NUM,lineNo,startCol,text.toString());
				if(Integer.decode(text.toString()) > 65535) {
					tk = new CToken(CToken.TK_ILL,lineNo,startCol,text.toString() + " 16bitで表現できません");
				}
				accept = true;
				break;
			case 5:					// +を読んだ
				tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
				accept = true;
				break;
			case 6:					// -を読んだ
				tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
				accept = true;
				break;
			case 7:					// /を読んだ
				ch = readChar();
				if(ch == '/') {
					text.deleteCharAt(0);
					state = 8;
				} else if (ch == '*') {
					text.deleteCharAt(0);
					state = 9;
				} else if ((ch >= '0' && ch <= '9' )|| ch == '(' || ch == '-'){
					backChar(ch);
					tk = new CToken(CToken.TK_DIV, lineNo, startCol, "/");
					accept = true;
				} else {
					state = 2;
				}
				break;
			case 8:					// 一行コメントアウトの状態
				ch = readChar();
				if(ch == '\n' || ch == '\r') {
					state = 0;
				} else if(ch == (char) - 1) {
					state = 11;
				} else {
					state = 8;
				}
				break;
			case 9:					// 複数行コメントアウトの状態
				ch = readChar();
				if(ch == '*') {
					state = 10;
				} else if(ch == (char) - 1) {
					state = 11;
				} else {
					state = 9;
				}
				break;
			case 10:
				ch = readChar();
				if(ch == '*'){
					state = 10;
				} else if(ch == (char) - 1) {
					state = 11;
				} else if(ch == '\n' || ch == '\r') {
					state = 0;
				} else {
					state = 9;
				}
				break;
			case 11:
				tk = new CToken(CToken.TK_ILL,lineNo,startCol,text.toString());
				accept = true;
				System.err.println("[ERROR] コメントアウト中にファイルが終了しました");
				break;
			case 12:
				tk = new CToken(CToken.TK_AMP,lineNo,startCol,"&");
				accept = true;
				break;
			case 13:			//0を読み込んだ状態
				ch = readChar();
				if(ch >= '0' && ch <= '7') {
					text.append(ch);
					state = 15;
				}else if(ch == 'x' || ch == 'X') {
					text.append(ch);
					state = 14;
				}else {
					backChar(ch);
					state = 4;
				}
				break;
			case 14:
				ch = readChar();
				if((ch>='0'&&ch<='9') || (ch>='a'&&ch<='f') || (ch>='A'&&ch<='F')) {
					text.append(ch);
					state = 16;
				}else {
//					backChar(ch);
					state = 2;
				}
				break;
			case 15:
				ch = readChar();
				if(ch>='0'&&ch<='7') {
					text.append(ch);
					state = 15;
				}else {
					state = 4;
				}
				break;
			case 16:
				ch = readChar();
				if((ch>='0'&&ch<='9') || (ch>='a'&&ch<='f') || (ch>='A'&&ch<='F')) {
					text.append(ch);
					state = 16;
				}else {
					state = 4;
				}
				break;
			case 17:
				tk = new CToken(CToken.TK_MUL,lineNo,startCol,"*");
				accept = true;
				break;
			case 18:
				tk = new CToken(CToken.TK_LPAR,lineNo,startCol,"(");
				accept = true;
				break;
			case 19:
				tk = new CToken(CToken.TK_RPAR,lineNo,startCol,")");
				accept = true;
				break;
			}
		}
		return tk;
	}
}
