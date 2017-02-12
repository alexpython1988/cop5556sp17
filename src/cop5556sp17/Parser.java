package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	void parse() throws SyntaxException {
		program();
		matchEOF();
		return;
	}
	
/*
grammar for LL(1) parser design:

implement?	check?		grammer
done	 	pass		program ::=  IDENT block
done		pass		program ::=  IDENT param_dec ( , param_dec )*   block
done		pass		param_dec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)   IDENT
done		pass		block ::= { ( dec | statement) * }
done		pass		dec ::= (  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME)    IDENT
done		pass		statement ::= OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
done		pass		assign ::= IDENT ASSIGN expression
done		pass		chain ::=  chainElem arrowOp chainElem ( arrowOp  chainElem)*
done		pass		whileStatement ::= KW_WHILE ( expression ) block
done		pass		ifStatement ::= KW_IF ( expression ) block
done		pass		arrowOp ::= ARROW   |   BARARROW
done		pass		chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg
done		pass		filterOp ::= KW_BLUR |KW_GRAY | KW_CONVOLVE
done		pass		frameOp ::= KW_SHOW | KW_HIDE | KW_MOVE | KW_XLOC |KW_YLOC
done		pass		imageOp ::= KW_WIDTH |KW_HEIGHT | KW_SCALE
done		pass		arg ::= ¦Å | ( expression (   ,expression)* )
done		pass		expression ::= term ( relOp term)*
done		pass		term ::= elem ( weakOp  elem)*
done		pass		elem ::= factor ( strongOp factor)*
done		pass		factor ::= IDENT | INT_LIT | KW_TRUE | KW_FALSE | KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )   		       			
done		pass		relOp ::=  LT | LE | GT | GE | EQUAL | NOTEQUAL 
done		pass		weakOp  ::= PLUS | MINUS | OR   
done		pass		strongOp ::= TIMES | DIV | AND | MOD 

Based on the design, we can find the predict is not unique so it is not a LL(1)
	 */
	
//	expression ::= term ( relOp term)*
	void expression() throws SyntaxException {
		//TODO
		//System.out.println("expr");
		term();
		while(relOp(t)){
			consume();
			term();
		}
	}

//	term ::= elem ( weakOp  elem)*
	void term() throws SyntaxException {
		//TODO
		//System.out.println("term");
		elem();
		while(weakOp(t)){
			consume();
			elem();
		}
	}

//	elem ::= factor ( strongOp factor)*
	void elem() throws SyntaxException {
		//TODO
		//System.out.println("elem");
		factor();
		
		while(strongOp(t)){
			consume();
			factor();
		}
	}

//	factor ::= IDENT | INT_LIT | KW_TRUE | KW_FALSE
//	       	| KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
	void factor() throws SyntaxException {
		//System.out.println("factor");
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			consume();
		}
			break;
		
		case INT_LIT: {
			consume();
		}
			break;
		
		case KW_TRUE:
		case KW_FALSE: {
			consume();
		}
			break;
		
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			consume();
		}
			break;
		
		case LPAREN: {
			consume();
			expression();
			match(RPAREN);
		}
			break;
		
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor: " + kind + 
					". The illegal toke is at " + scanner.getLinePos(t));
		}
	}

//  block ::= { ( dec | statement) * }
	void block() throws SyntaxException {
		//TODO
		//System.out.println("block");
		match(LBRACE);
		while(true){
			if(isDec(t)){
				dec();
				continue;
			}else if(isStatement(t)){
				statement();
				continue;
			}else{
				break;
			}
		}
		
		match(RBRACE);
	}
	
	/*
	 * used to check in the block which method should be invoked(statement() vs dec())
	 * this implementation has a drawback that the efficiency is not optimized
	 * In order to improve the efficiency, switch-case should be used
	 */
	private boolean isStatement(Token t) {	
			return (t.isKind(OP_SLEEP) || t.isKind(KW_WHILE) || t.isKind(KW_IF) 
					|| t.isKind(IDENT) || filterOp(t) || frameOp(t) || imageOp(t));
		}

	private boolean isDec(Token t) {
			return (t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE)
					|| t.isKind(KW_FRAME));
		}

//	program ::=  IDENT block
//	program ::=  IDENT param_dec ( , param_dec )*   block
	void program() throws SyntaxException {
		//TODO
		//System.out.println("program");
		match(IDENT);
		Kind kind = t.kind;
		switch (kind) {
		case LBRACE:{
			block();
		}
			break;
		
		case KW_URL:
		case KW_FILE:
		case KW_INTEGER:
		case KW_BOOLEAN:{
			//System.out.println(t.kind);
			paramDec();
			while(t.isKind(COMMA)){
				consume();
				//System.out.println(t.kind);
				paramDec();
			}
			block();
		}
			break;

		default:
			throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) +
					". Saw " + kind + " expected one from [LBRACE, KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN]");
		}
	}
	
	/* Important!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * In functions paramDec(), dec() and assign(), use cosume() method instead of match in real production
	 * match() is unnecessary for double safety check and slow down the parser speed
	 * use match here is only aimed for pass the course test 
	 */
	
//  param_dec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)   IDENT
	void paramDec() throws SyntaxException {
		//TODO
		//System.out.println("parmdec");
		match(KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN);
		match(IDENT);
	}

//	dec ::= (  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME)    IDENT
	void dec() throws SyntaxException {
		//TODO
		//System.out.println("dec");
//		consume();
		match(KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME);
		match(IDENT);
	}

//	statement ::=   OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
	void statement() throws SyntaxException {
		//TODO
		//System.out.println("statement");
		Kind kind = t.kind;
		Token nextToken = scanner.peek(); //nextToken = t.next
		
		switch (kind) {
		case OP_SLEEP:{
			consume();
			expression();
			match(SEMI);
		}
			break;
		
		case KW_WHILE:
		case KW_IF:{
			if_while_statement();
		}
			break;
			
		case IDENT:{
			if(nextToken.isKind(ASSIGN)){
				assign();
				match(SEMI);
			}else if(arrowOp(nextToken)){
				chain();
				match(SEMI);
			}else{
				throw new SyntaxException("illegal factor in statement saw " + nextToken.kind + 
						" expected [ASSIGN, ARROW, BARARROW]. The illegal token is at " + scanner.getLinePos(nextToken));
			}
		}
			break;
		
		case OP_BLUR:
		case OP_GRAY:
		case OP_CONVOLVE:
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE:{
			chain();
			match(SEMI);
		}
			break;

		//default will never be reached since the input will always be legal
		default:
			throw new SyntaxException("illegal factor: " + kind + 
					". The illegal token is at " + scanner.getLinePos(t));
		}
	}

//	chain ::=  chainElem arrowOp chainElem ( arrowOp  chainElem)*
	void chain() throws SyntaxException {
		//TODO
		//System.out.println("chain");
		chainElem();
		match(ARROW, BARARROW);
		chainElem();
		while(arrowOp(t)){
			consume();
			chainElem();
		}
	}
	
//	assign ::= IDENT ASSIGN expression
	void assign() throws SyntaxException {
		//TODO
		//System.out.println("assign");
//		consume();
		match(IDENT);
		match(ASSIGN);
		expression();
	}
	
//	chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg
	void chainElem() throws SyntaxException {
		//TODO
		//System.out.println("chainElem");
		if(t.isKind(IDENT)){
			consume();
		}else if(filterOp(t) || frameOp(t) || imageOp(t)){
			consume();
			arg();
		}else{
			throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
				" saw " + t.kind + "expected [IDENT, filterOp, frameOp, imageOp]");
		}
	}
	
//	ifStatement ::= KW_IF ( expression ) block
//	whileStatement ::= KW_WHILE ( expression ) block
	void if_while_statement() throws SyntaxException {
		//TODO
		//System.out.println("if_while");
		consume();
		match(LPAREN);
		expression();
		match(RPAREN);
		block();
	}

//	arg ::= ¦Å | ( expression (,expression)* )
	void arg() throws SyntaxException {
		//TODO
		//System.out.println("arg");
		if(t.isKind(LPAREN)){
			consume();
			expression();
			while(t.isKind(COMMA)){
				consume();
				expression();
			}
			match(RPAREN);
//		}else if(t.isKind(SEMI) || arrowOp(t)){
//			//pass
//		}else{
//			throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
//					" saw " + t.kind + " expected [LPAREN, SEMI, ARROW, BARARROW]");
		}
	}
	
	/*
	 * judge the given token is in one of the legal kind in its Operation catalog 
	 */
	private boolean strongOp(Token t){
		return (t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND)|| t.isKind(MOD));
	}
	
	private boolean weakOp(Token t){
		return (t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR));
	}
	
	private boolean arrowOp(Token t){
		return (t.isKind(ARROW) || t.isKind(BARARROW));
	}
	
	private boolean relOp(Token t){
		return (t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) 
				|| t.isKind(EQUAL) || t.isKind(NOTEQUAL));
	}
	
	private boolean filterOp(Token t){
		return (t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE));
	}
	
	private boolean frameOp(Token t){
		return (t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE)
				|| t.isKind(KW_XLOC) || t.isKind(KW_YLOC));
	}
	
	private boolean imageOp(Token t){
		return (t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE));
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		} 
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
				" saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		for(Kind k: kinds){
			if(t.isKind(k)){
				return consume();
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int i = 0; i < kinds.length - 1;i++){
			sb.append(kinds[i] + ", ");
		}
		
		sb.append(kinds[kinds.length - 1] + "]");
		
		throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
				" saw " + t.kind + "expected one of" + sb.toString()); //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}
}
