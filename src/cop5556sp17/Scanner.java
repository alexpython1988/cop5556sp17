package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		public String getText() {
			return text;
		}
	}
	
	private static enum State {
		START, IN_DIGIT, GOT_EQ, IN_INDENT, GOT_OR_DASH, /*|-*/ 
		GOT_LT /*<*/, GOT_GT /*>*/, GOT_DANG/*!*/, End_COMMENT,
		GOT_DASH /*-*/, GOT_OR /*|*/, GOT_DIV /*/*/, Start_COMMENT,
		/*GOT_ASTERISK*/
	}
	
	
	//create a hashmap of all the reserved words
	protected static final Map<String, Kind> reserved_words;
	static{
		reserved_words = new HashMap<String, Scanner.Kind>();
		
		for(Kind k: Kind.values()){
			String lit = k.getText();
			if(lit.matches("^[a-z]+"))
				reserved_words.put(lit, k);			
		}
	}
	
	
	/**
	 * justify if the token can be assigned as an identifier or a system reserved word
	 * @param str
	 * @return
	 */
	protected boolean isKeyWord(String str){
		return reserved_words.containsKey(str);
	}
	
	/**
	 * get the Kind associated with the string if the string is a reserved keyword in the system
	 * @param kindText
	 * @return keyword Kind
	 */
	protected Kind getKind(String kindText){
		return reserved_words.get(kindText);
	}
	
//	/**
//	 * justify if the token can be assigned as INT_LIT
//	 * @param c
//	 * @return
//	 */
//	private boolean isDigit(char c){
//		return ('0' <= c) && (c <= '9');
//	}
//	
	
	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			if(kind == Kind.EOF)
				return Kind.EOF.getText();
			return chars.substring(pos, pos + length);
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			return new LinePos(FindLineNumber(pos), pos - startPosForEachLine.get(FindLineNumber(pos)));
		}

		public Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			return Integer.parseInt(chars.substring(pos, pos + length));
		}

		public boolean isKind(Kind kind) {
			return this.kind.equals(kind);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Token)) {
				return false;
			}
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (kind != other.kind) {
				return false;
			}
			if (length != other.length) {
				return false;
			}
			if (pos != other.pos) {
				return false;
			}
			return true;
		}

		private Scanner getOuterType() {
			return Scanner.this;
		}
	}

	
	private int skipWhiteSpace(int pos, int len){
		int sPos = pos;
		
		if(sPos < len){
			while(Character.isSpaceChar(chars.charAt(sPos))){
				sPos ++;
				if(sPos == len)
					break;
			}
		}
		
		return sPos;
	}
	
	//TODO check -> make O(n) to O(logn)
	private int FindLineNumber(int pos){
//		int i = pos;
		//O(n)
		int i = 0;
		int targetPos;
		
		for(; i < startPosForEachLine.size(); i++){
			targetPos = startPosForEachLine.get(i);
			if(pos > targetPos){
				continue;
			}else if(pos == targetPos){
				return i;
			}else{
				break;
			}
		}
		
		return (i - 1);
		
		//O(dlogn) (not seems faster I expected for real tests, use the old code)
//		int x = -1;
//		int start = 0;
//		int end = startPosForEachLine.size() - 1;
//		while(i >= 0){
//			x = bianrySearch(start, end, startPosForEachLine, i);
//			if(x == -1){
//				i--;
//				continue;
//			}
//			
//			break;
//		}
//		return x;
	}
//
//	private int bianrySearch(int start, int end, List<Integer> al, int i){
//
//		while(start <= end){
//			int mid = (start + end)/2;
//			int midNum = al.get(mid);
//			//System.out.println("mid is" + mid);
//			if(i == midNum){
//				return al.indexOf(i);
//			}else if(i < midNum){
//				end = mid - 1;
//				continue;
//			}else{
//				start = mid + 1;
//			}
//		}
//		
//		return -1;
//	}

	
	
	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
	}

	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		int len = chars.length();
		int ch;
		int startPos = 0;
		startPosForEachLine.add(0);
		State state = State.START;

		while(pos <= len){
			ch = pos < len ? chars.charAt(pos) : -1; 
			
			switch (state) {
		
			case START:{
				pos = skipWhiteSpace(pos, len);
				startPos = pos;
				ch = pos < len ? chars.charAt(pos) : -1; 
				switch (ch) {
			
				case -1:{
					tokens.add(new Token(Kind.EOF, startPos, 0));
					//System.out.println("eof");
					pos++;
				}
					break;
					
				case '+':{
					tokens.add(new Token(Kind.PLUS, startPos, 1));
					//System.out.println("+");
					pos++;
				}
					break;
				
				case '%':{
					tokens.add(new Token(Kind.MOD, startPos, 1));
					//System.out.println("%");
					pos++;
				}
					break;
				
				case ';':{
					tokens.add(new Token(Kind.SEMI, startPos, 1));
					//System.out.println(";");
					pos++;
				}
					break;
				
				case ',':{
					tokens.add(new Token(Kind.COMMA, startPos, 1));
					//System.out.println(",");
					pos++;
				}
					break;
					
				case '&':{
					tokens.add(new Token(Kind.AND, startPos, 1));
					//System.out.println("&");
					pos++;
				}
					break;
					
				case '(':{
					tokens.add(new Token(Kind.LPAREN, startPos, 1));
					//System.out.println("(");
					pos++;
				}
					break;
					
				case ')':{
					tokens.add(new Token(Kind.RPAREN, startPos, 1));
					//System.out.println(")");
					pos++;
				}
					break;
					
				case '{':{
					tokens.add(new Token(Kind.LBRACE, startPos, 1));
					//System.out.println("{");
					pos++;
				}
					break;
					
				case '}':{
					tokens.add(new Token(Kind.RBRACE, startPos, 1));
					//System.out.println("}");
					pos++;
				}
					break;
					
				case '*':{
					//state = State.GOT_ASTERISK;
					tokens.add(new Token(Kind.TIMES, startPos, 1));
					//System.out.println("*");
					state = State.START;
					pos++;
				}
					break;
							
				case '!':{
					state = State.GOT_DANG;
					pos++;
				}
					break;
					
				case '=':{
					state = State.GOT_EQ;
					pos++;
				}
					break;
					
				case '<':{
					state = State.GOT_LT;
					pos++;
				}
					break;
					
				case '>':{
					state = State.GOT_GT;
					pos++;
				}
					break;
					
				case '-':{
					state = State.GOT_DASH;
					pos++;
				}
					break;
				
				case '|':{
					state = State.GOT_OR;
					pos++;
				}
					break;
					
				case '/':{
					state = State.GOT_DIV;
					pos++;
				}
					break;
				
				case '\n':{
					line++;
					pos++;
					startPosForEachLine.add(pos);
				}
					break;
				
				case '\t':{
					pos++;
				}
					break;

				case '\r':{
					//line++;
					pos++;
					//startPosForEachLine.add(pos);
				}
					break;
					
				case '\b':{
					pos++;
				}
					break;
					
				default:{
					if(Character.isDigit(ch)){
						if(ch == '0'){
//							int nextPos = pos + 1;
//							if(nextPos < chars.length()){
//								int nextChar = chars.charAt(nextPos);
//								if(Character.isDigit(nextChar) && nextChar != '0'){
//									throw new IllegalNumberException("At line: " + FindLineNumber(nextPos) + 
//											" at pos: " + (nextPos - startPosForEachLine.get(FindLineNumber(nextPos))) +
//											" '0" +  Character.toString((char)nextChar) + 
//											"' is a illegal number. Non zero numbers cannot start by 0");
//								}else{
//									tokens.add(new Token(Kind.INT_LIT, startPos, 1));
//									pos++;
//									state = State.START;
//								}
//								
//							}else{
//								tokens.add(new Token(Kind.INT_LIT, startPos, 1));
//								pos++;
//								state = State.START;
//							}
							tokens.add(new Token(Kind.INT_LIT, startPos, 1));
							pos++;
							state = State.START;
						}else{
							state = State.IN_DIGIT;
							pos++;
						}
					}else if(Character.isJavaIdentifierStart(ch)){
						state = State.IN_INDENT;
						pos++;
					}else{
						throw new IllegalCharException("At line: " + FindLineNumber(pos) + 
								" at pos: " + (pos - startPosForEachLine.get(FindLineNumber(pos))) + 
								" the character '" + Character.toString((char)ch) + 
								"' is not defined in the system.");
					}
				}
					break;
				}
			}
				break;
				
			case IN_DIGIT:{
				if(Character.isDigit(ch)){
					pos++;
				}else{
					String inputNum = chars.substring(startPos, pos);
					try{
						Integer.parseInt(inputNum);
						//System.out.println(inputNum);
						tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
						state = State.START;
					}catch (NumberFormatException e){
						throw new IllegalNumberException("At line: " + 
								FindLineNumber(pos) + " at pos: " + 
								(startPos - startPosForEachLine.get(FindLineNumber(pos))) + 
								" the imput number: " + inputNum + " exceed the limit of Integer range");
					}
					
				}
			}
				break;
					
			case IN_INDENT:{
				if(Character.isJavaIdentifierPart(ch)){
					pos++;
				}else{
					String indent = chars.substring(startPos, pos);
					//System.out.println(indent);
					if(isKeyWord(indent)){
						tokens.add(new Token(getKind(indent), startPos, pos - startPos));
					}else{
						tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
					}
					
					state = State.START;
				}
			}
				break;
					
			case GOT_OR:{
				if(ch == '-'){
					state = State.GOT_OR_DASH;
					pos++;
				}else{
					tokens.add(new Token(Kind.OR, startPos, 1));
					//System.out.println("|");
					state = State.START;
				}
			}
				break;
				
			case GOT_EQ:{
				if(ch == '='){
					state = State.START;
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					//System.out.println("==");
					pos++;
				}else{
					throw new IllegalCharException("At line: " + FindLineNumber(pos) + " at pos: " + 
							(pos - startPosForEachLine.get(FindLineNumber(pos))) + 
							" expected '=' but got '" + Character.toString((char)ch) + "'");
				}
			}
				break;
			
			case GOT_LT:{
				if(ch == '='){
					tokens.add(new Token(Kind.LE, startPos, 2));
					//System.out.println("<=");
					pos++;
				}else if(ch == '-'){
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					//System.out.println("<-");
					pos++;
				}else{
					tokens.add(new Token(Kind.LT, startPos, 1));
					//System.out.println("<");
				}
				
				state = State.START;
			}
				break;
				
			case GOT_GT:{
				if(ch == '='){
					tokens.add(new Token(Kind.GE, startPos, 2));
					//System.out.println(">=");
					pos++;
				}else{
					tokens.add(new Token(Kind.GT, startPos, 1));
					//System.out.println(">");
				}
				
				state = State.START;
			}
				break;
				
			case GOT_DASH:{
				if(ch == '>'){
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					//System.out.println("->");
					pos++;
				}else{
					tokens.add(new Token(Kind.MINUS, startPos, 1));
					//System.out.println("-");
				}
				
				state = State.START;
			}
				break;
				
			case GOT_DANG:{
				if(ch == '='){
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					//System.out.println("!=");
					pos++;
				}else{
					tokens.add(new Token(Kind.NOT, startPos, 1));
					//System.out.println("!");
				}
				
				state = State.START;
			}
				break;
				
			case GOT_DIV:{
				if(ch == '*'){
					state = State.Start_COMMENT;
					//System.out.println("/*");
					pos++;
				}else{
					tokens.add(new Token(Kind.DIV, startPos, 1));
					//System.out.println("/");
					state = State.START;
				}
			}
				break;
			
			case Start_COMMENT:{
				if(ch == '*'){
					state = State.End_COMMENT;
					pos++;
				}else if(ch == -1){
					state = State.START;
				}else if(ch == '\n' || ch == '\r'){
					line++;
					pos++;
					startPosForEachLine.add(pos);
				}else{
					pos++;
				}
			}
				break;
				
			case End_COMMENT:{
				if(ch == '/'){
					//System.out.println("*/");
					state = State.START;
					pos++;
//				}else if(ch == '*'){
					
				}else{
					state = State.Start_COMMENT;
				}	
			}
				break;
			
			case GOT_OR_DASH:{
				if(ch == '>'){
					tokens.add(new Token(Kind.BARARROW, startPos, 3));
					//System.out.println("|->");
					state = State.START;
					pos++;
				}else{
					tokens.add(new Token(Kind.OR, startPos, 1));
					tokens.add(new Token(Kind.MINUS, startPos + 1, 1));
					state = State.START;
					//throw new IllegalCharException("At pos:" + pos + " expected '>' but got '" + Character.toString((char)ch) + "'");
				}
			}
				break;
			
//			case GOT_ASTERISK:{
//				if(ch == '/'){
//					throw new IllegalCharException("At pos:" + pos + " the close comment token found without the start comment token defined first.");
//				}else{
//					tokens.add(new Token(Kind.TIMES, startPos, 1));
//					//System.out.println("*");
//					state = State.START;
//				}
//			}
//				break;
			
			//seems never reach here
			default:
				assert false : "Unknow state: " + state;
			}
		}
		
		//tokens.add(new Token(Kind.EOF, pos, 0));
		return this;  
	}


	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum = 0;
	
	//use line and startPosForEachLine to track the beginning pos of each line
	//the first line is line 0
	int line = 0;
	protected List<Integer> startPosForEachLine = new ArrayList<Integer>();

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	
	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}

}
