package cop5556sp17;

import static cop5556sp17.Scanner.Kind.*;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;


public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}

	@Test
	public void testFactor2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "true";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BooleanLitExpression.class, ast.getClass());
	}
	
	@Test
	public void testFactor3() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "screenwidth";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(ConstantExpression.class, ast.getClass());
	}

	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}

	@Test
	public void testBinaryExpr1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "1+(2*3)&abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		BinaryExpression be1 = (BinaryExpression) be.getE1();
		BinaryExpression be2 = (BinaryExpression) be1.getE0();
		
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(BinaryExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
		
		assertEquals(IdentExpression.class, be1.getE1().getClass());
		assertEquals(BinaryExpression.class, be1.getE0().getClass());
		assertEquals(AND, be1.getOp().kind);
		
		assertEquals(IntLitExpression.class, be2.getE1().getClass());
		assertEquals(IntLitExpression.class, be2.getE0().getClass());
		assertEquals(TIMES, be2.getOp().kind);
	}
	
	@Test
	public void teststatementif0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "if(true){}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(IfStatement.class, ast.getClass());
		
		Statement s =  (Statement) ast;
		IfStatement is = (IfStatement) s;
		
		assertEquals(KW_IF, is.getFirstToken().kind);

	}
	
	@Test
	public void teststatementwhile0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "while(true){}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(WhileStatement.class, ast.getClass());
		
		Statement s =  (Statement) ast;
		WhileStatement is = (WhileStatement) s;
		
		assertEquals(KW_WHILE, is.getFirstToken().kind);
	}
	
	@Test
	public void testprogram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "program0 {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		assertEquals(Program.class, ast.getClass());
		
		Program p =  (Program) ast;
		Token t = p.getFirstToken();
		Block b = p.getB();
		
		System.out.println(t.getText());
		System.out.println(b);
	}
	
	@Test
	public void testprogram1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "program0 url abc, file def, integer gkm, boolean cde   {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		assertEquals(Program.class, ast.getClass());
		
		Program p =  (Program) ast;
		Token t = p.getFirstToken();
		Block b = p.getB();
		List<ParamDec> list =  p.getParams();
		
		System.out.println(t.getText());
		System.out.println(b);
		for(ParamDec pd: list){
			System.out.println(pd);
		}
	}
	
	@Test
	public void testTuple() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "(abc * (123 + 456) != eda, bac % eda == 0)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.arg();
		assertEquals(Tuple.class, ast.getClass());
		
		Tuple t = (Tuple) ast;
		List<Expression> list = t.getExprList();
		
		//System.out.println(t.getFirstToken().getText());
		
		Expression e11 = list.get(0);
		
		BinaryExpression be1 = (BinaryExpression) e11;
		
		BinaryExpression be11 = (BinaryExpression) be1.getE0();
		System.out.println(be11.getE0().getFirstToken().getText());
		System.out.println(be11.getOp().getText());
		BinaryExpression be12 = (BinaryExpression) be11.getE1();
		System.out.println(be12.getE0().getFirstToken().getText());
		System.out.println(be12.getOp().getText());
		System.out.println(be12.getE1().getFirstToken().getText());
		System.out.println(be1.getOp().getText());
		System.out.println(be1.getE1().getFirstToken().getText());
		
		Expression e12 = list.get(1);
		BinaryExpression be = (BinaryExpression) e12;
		BinaryExpression be21 = (BinaryExpression) be.getE0();
		System.out.println(be21.getE0().getFirstToken().getText());
		System.out.println(be21.getOp().getText());
		System.out.println(be21.getE1().getFirstToken().getText());
		System.out.println(be.getOp().getText());
		System.out.println(be.getE1().getFirstToken().getText());
		
		System.out.println(t.getFirstToken().getText());
		System.out.println(be11.getFirstToken().getText());
		System.out.println(be12.getFirstToken().getText());
		System.out.println(be.getFirstToken().getText());
		System.out.println(be21.getFirstToken().getText());
	}
	
	@Test
	public void testDec() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String[] strs = new String[]{"integer abc", "boolean _abc", "image $abc", "frame abc123"};
		
		for(String s: strs){
			String input = s;
			Scanner scanner = new Scanner(input);
			scanner.scan();
			Parser parser = new Parser(scanner);
			ASTNode ast = parser.dec();
			assertEquals(Dec.class, ast.getClass());
	//		
			Dec d =  (Dec) ast;
			
			System.out.println(d.getType().getText());
			System.out.println(d.getIdent().getText());
			System.out.println(d.getFirstToken().getText());
		}
	}
	
	@Test
	public void testStatmentSleep() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "sleep abc;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(SleepStatement.class, ast.getClass());
		
		SleepStatement ss =  (SleepStatement) ast;
		System.out.println(ss.getFirstToken().getText());
		IdentExpression ie = (IdentExpression) ss.getE();
		System.out.println(ie.getFirstToken().getText());
	}
	
	@Test
	public void testStatmentAssign() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc <- 123;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(AssignmentStatement.class, ast.getClass());
		
		AssignmentStatement as = (AssignmentStatement) ast;
		System.out.println(as.getVar().getText());
		System.out.println(as.getFirstToken().getText());
		
		IntLitExpression ile = (IntLitExpression) as.getE(); 
		System.out.println(ile.getFirstToken().getText());
	}
	
	@Test
	public void testStatmentChain0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc -> a123;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		
		BinaryChain bc = (BinaryChain) ast;
		ChainElem ce = (ChainElem) bc.getE0();
		System.out.println(ce.getFirstToken().getText());
		System.out.println(bc.getArrow().getText());
		IdentChain ce1 = (IdentChain) bc.getE1();
		System.out.println(bc.getE1().getClass());
		System.out.println(ce1.getFirstToken().getText());
	}
	
	@Test
	public void testStatmentChain1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc -> gray |-> _abc1 -> hide (abc,123) -> scale ;";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.statement();
		assertEquals(BinaryChain.class, ast.getClass());
		
		BinaryChain bc0 = (BinaryChain) ast;
		BinaryChain bc1 = (BinaryChain) bc0.getE0();
		BinaryChain bc2 = (BinaryChain) bc1.getE0();
		BinaryChain bc3 = (BinaryChain) bc2.getE0();
		
		System.out.println(bc3.getE0().getClass());
		System.out.println(bc3.getArrow().getText());
		System.out.println(bc3.getE1().getClass());
		
		System.out.println(bc2.getArrow().getText());
		System.out.println(bc2.getE1().getClass());

		System.out.println(bc1.getArrow().getText());
		System.out.println(bc1.getE1().getClass());
		FrameOpChain foc = (FrameOpChain) bc1.getE1();
		Tuple t = foc.getArg();
		List<Expression> list = t.getExprList();
		IdentExpression ie1 = (IdentExpression) list.get(0);
		IntLitExpression ie2 = (IntLitExpression) list.get(1);
		System.out.println(ie1.getFirstToken().getText());
		System.out.println(ie2.getFirstToken().getText());
		System.out.println(bc0.getArrow().getText());
		System.out.println(bc0.getE1().getClass());
	}
	
	@Test
	public void testBlock0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		//String input = "{}";
		String input = "{integer r0 integer r1 r0 <-12; r1 <- 34;\n"
						+ "xloc (screenwidth|(r0), screenheight&(r1))"
						+ " -> yloc(screenwidth+(r1), screenheight*(r0)) |-> convolve;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.block();
		assertEquals(Block.class, ast.getClass());
		
		Block b = (Block) ast;
		System.out.println(b);
	}
	
	@Test
	public void testP() throws IllegalCharException, IllegalNumberException, SyntaxException {
		//String input = "{}";
		String input = "program {}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.parse();
		assertEquals(Program.class, ast.getClass());
		
		Program p = (Program) ast;
		System.out.println(p);
	}
	
	
}
