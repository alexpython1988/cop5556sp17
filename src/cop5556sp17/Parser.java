package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import java.util.ArrayList;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

/*
grammar for LL(2) parser design:

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
done		pass		arg ::= epsil | ( expression (   ,expression)* )
done		pass		expression ::= term ( relOp term)*
done		pass		term ::= elem ( weakOp  elem)*
done		pass		elem ::= factor ( strongOp factor)*
done		pass		factor ::= IDENT | INT_LIT | KW_TRUE | KW_FALSE | KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )   		       			
done		pass		relOp ::=  LT | LE | GT | GE | EQUAL | NOTEQUAL 
done		pass		weakOp  ::= PLUS | MINUS | OR   
done		pass		strongOp ::= TIMES | DIV | AND | MOD 

Based on the design, we can find the predict is not unique so it is not a LL(1)

abstract syntax:

	Program::= List<ParamDec> Block
	ParamDec::= type ident
	Block::= List<Dec>  List<Statement>
	Dec::= type ident
	Statement::= SleepStatement | WhileStatement | IfStatement | Chain
      		| AssignmentStatement
	SleepStatement::= Expression
	AssignmentStatement::= IdentLValue Expression
	Chain::= ChainElem | BinaryChain
	ChainElem ::= IdentChain | FilterOpChain | FrameOpChain | ImageOpChain
	IdentChain::= ident
	FilterOpChain::= filterOp Tuple
	FrameOpChain::= frameOp Tuple
	ImageOpChain::= imageOp Tuple
	BinaryChain::= Chain (arrow | bararrow)  ChainElem
	WhileStatement::= Expression Block
	IfStatement::= Expression Block
	Expression::= IdentExpression | IntLitExpression | BooleanLitExpression
  		| ConstantExpression | BinaryExpression
	IdentExpression::= ident
	IdentLValue::= ident
	IntLitExpression::= intLit
	BooleanLitExpression::= booleanLiteral
	ConstantExpression::= screenWidth | screenHeight
	BinaryExpression::= Expression op Expression
	Tuple ::=List<Expression>
		op::= relOp | weakOp | strongOp
	type::= integer | image | frame | file | boolean | url
*/

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
	Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	
	Expression expression() throws SyntaxException {
		//TODO
		//System.out.println("expr");
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = term();
		while(relOp(t)){
			Token op = t;
			consume();
			e1 = term();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression term() throws SyntaxException {
		//TODO
		//System.out.println("term");
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = elem();
		while(weakOp(t)){
			Token op = t;
			consume();
			e1 = elem();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression elem() throws SyntaxException {
		//TODO
		//System.out.println("elem");
		Expression e0 = null;
		Expression e1 = null;
		Token firstToken = t;
		e0 = factor();
		
		while(strongOp(t)){
			Token op = t;
			consume();
			e1 = factor();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression factor() throws SyntaxException {
		Expression e = null;
		//System.out.println("factor");
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(consume());
		}
			break;
		
		case INT_LIT: {
			e = new IntLitExpression(consume());
		}
			break;
		
		case KW_TRUE:
		case KW_FALSE: {
			e = new BooleanLitExpression(consume());
		}
			break;
		
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(consume());
		}
			break;
		
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor: " + kind + 
					". The illegal toke is at " + scanner.getLinePos(t) + 
					" expected one of [IDENT,INT_LIT,KW_TRUE,KW_FALSE,KW_SCREENWIDTH,KW_SCREENHEIGHT,LPARENT");
		}
		
		return e;
	}

	Block block() throws SyntaxException {
		//TODO
		//System.out.println("block");
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		
		Token firstToken = match(LBRACE);
		//Token firstToken = t;
		while(true){
			if(isDec(t)){
				decs.add(dec());
				continue;
			}else if(isStatement(t)){
				statements.add(statement());
				continue;
			}else{
				break;
			}
		}
		
		match(RBRACE);
		
		return new Block(firstToken, decs, statements);
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

	Program program() throws SyntaxException {
		//TODO
		//System.out.println("program");
		Program p = null;
		Token firstToken = match(IDENT);
		ArrayList<ParamDec> paramList = new ArrayList<ParamDec>();
		
		Kind kind = t.kind;
		switch (kind) {
		case LBRACE:{
			p = new Program(firstToken, null, block());
		}
			break;
		
		case KW_URL:
		case KW_FILE:
		case KW_INTEGER:
		case KW_BOOLEAN:{
			//System.out.println(t.kind);
			paramList.add(paramDec());
			while(t.isKind(COMMA)){
				consume();
				//System.out.println(t.kind);
				paramList.add(paramDec());
			}
			p = new Program(firstToken, paramList, block());
		}
			break;

		default:
			throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) +
					". Saw " + kind + " expected one from [LBRACE, KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN]");
		}
		
		return p;
	}
	
	/* Important!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * In functions paramDec(), dec() and assign() 
	 * use cosume() method to replace the first match() method in real production
	 * match() is unnecessary for double safety check 
	 * use match here is only aimed for pass the course test 
	 */
	
	ParamDec paramDec() throws SyntaxException {
		//TODO
		//System.out.println("parmdec");
		Token firstToken = match(KW_URL, KW_FILE, KW_INTEGER, KW_BOOLEAN);
		Token ident = match(IDENT);
		return new ParamDec(firstToken, ident);
	}

	Dec dec() throws SyntaxException {
		//TODO
		//System.out.println("dec");
//		consume();
		Token firstToken = match(KW_INTEGER, KW_BOOLEAN, KW_IMAGE, KW_FRAME);
		Token ident = match(IDENT);
		
		return new Dec(firstToken, ident);
	}

	Statement statement() throws SyntaxException {
		//TODO
		//System.out.println("statement");
		Statement statment  = null;
		Kind kind = t.kind;
		Token nextToken = scanner.peek(); //nextToken = t.next
		
		switch (kind) {
		case OP_SLEEP:{
			Token firstToken = consume();
			Expression e = expression();
			match(SEMI);
			statment = new SleepStatement(firstToken, e);
		}
			break;
		
		case KW_WHILE:
		case KW_IF:{
			statment = if_while_statement();
		}
			break;
			
		case IDENT:{
			if(nextToken.isKind(ASSIGN)){
				statment = assign();
				match(SEMI);
			}else if(arrowOp(nextToken)){
				statment = chain();
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
			statment = chain();
			match(SEMI);
		}
			break;

		//default will never be reached since the input will always be legal
		default:
			throw new SyntaxException("illegal factor: " + kind + 
					". The illegal token is at " + scanner.getLinePos(t));
		}
		
		return statment;
	}

	Chain chain() throws SyntaxException {
		//TODO
		//System.out.println("chain");
		Token firstToken = t;
		ChainElem ce0 = chainElem();
		Token arrow = match(ARROW, BARARROW);
		ChainElem ce1 = chainElem();
		
		BinaryChain bc = new BinaryChain(firstToken, ce0, arrow, ce1); 
		
		while(arrowOp(t)){
			arrow = consume();
			ChainElem ce2 = chainElem();
			bc = new BinaryChain(firstToken, bc, arrow, ce2);
		}
		
		return bc;
	}
	
	AssignmentStatement assign() throws SyntaxException {
		//TODO
		//System.out.println("assign");
//		consume();
		Token firstToken = match(IDENT);
		IdentLValue var = new IdentLValue(firstToken);
		match(ASSIGN);
		Expression e = expression();
		
		return new AssignmentStatement(firstToken, var, e);
	}
	
	ChainElem chainElem() throws SyntaxException {
		//TODO
		//System.out.println("chainElem");
		ChainElem ce = null;
		Token firstToken = t;
		
		if(t.isKind(IDENT)){
			consume();
			ce = new IdentChain(firstToken);
		}else if(filterOp(t)){
			consume();
			Tuple arg = arg();
			ce = new FilterOpChain(firstToken, arg);
		}else if(frameOp(t)){
			consume();		
			Tuple arg = arg();
			ce = new FrameOpChain(firstToken, arg);
		}else if(imageOp(t)){
			consume();
			Tuple arg = arg();
			ce = new ImageOpChain(firstToken, arg);
		}else{
			throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
				" saw " + t.kind + "expected [IDENT, filterOp, frameOp, imageOp]");
		}
		
		return ce;
	}
	
	Statement if_while_statement() throws SyntaxException {
		//TODO
		//System.out.println("if_while");
		Token firstToken = consume();
		
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		
		if(firstToken.isKind(KW_IF)){
			return new IfStatement(firstToken, e, b);
		}else{
			return new WhileStatement(firstToken, e, b);
		}
	}

	Tuple arg() throws SyntaxException {
		//TODO
		//System.out.println("arg");
		Token firstToken = null;
		ArrayList<Expression> tuples = new ArrayList<Expression>();
		
		if(t.isKind(LPAREN)){
			firstToken = consume();
			tuples.add(expression());
			while(t.isKind(COMMA)){
				consume();
				tuples.add(expression());
			}
			match(RPAREN);
//		}else if(t.isKind(SEMI) || arrowOp(t)){
//			//pass
//		}else{
//			throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
//					" saw " + t.kind + " expected [LPAREN, SEMI, ARROW, BARARROW]");
		}
		
		
		return new Tuple(firstToken, tuples);
		
//		match(LPAREN);
//		expression();
//		while(t.isKind(COMMA)){
//			consume();
//			expression();
//		}
//		match(RPAREN);
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
				" saw " + t.kind + " expected " + kind);
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
		for(int i = 0; i < kinds.length - 1; i++){
			sb.append(kinds[i] + ", ");
		}
		
		sb.append(kinds[kinds.length - 1] + "]");
		
		throw new SyntaxException("The illegal token is at " + scanner.getLinePos(t) + 
				" saw " + t.kind + " expected one of" + sb.toString()); //replace this statement
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
