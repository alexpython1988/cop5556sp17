/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 * 
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;

public class TypeCheckVisitorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAssignmentBoolLit0() throws Exception{
		String input = "p {\nboolean y \ny <- false;if(y){integer x x <- 4;}}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
		System.out.println(v.getSymTbl());
	}

	@Test
	public void testAssignmentBoolLitError0() throws Exception{
		String input = "p {\nboolean y \ny <- 3;}";
		testFuncWithError(input);	
	}		
	
	@Test
	public void testscopeError0() throws Exception{
		String input = "p {\nboolean y \ny <- true; if(y){integer x} while(y){boolean y x <- 123;}}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		program.visit(v, null);		
		System.out.println(v.getSymTbl());
	}	

	@Test
	public void testExpr1() throws Exception{
		String input = "p1 boolean x, integer y{"
				+ "x <- true; y <- 2; /*This is a comment*/ if(x){integer x x <- y; y <- (y + x*x);\n"
				+ "while(y > 10){\n integer i i <- 3 + (x - 10); y <- y-1;"
				+ "\n}"
				+ "\n}   "
				+ "\n}";
		
		testFuncWithoutError(input);
	}
	
	@Test
	public void testchain1() throws Exception{
		String input = "p2 ";
		
		testFuncWithoutError(input);
	}
	
	@Test
	public void testSymbolTable0() throws Exception{
		//TODO add string for symbol table test
		String input = "p file abc, boolean x {\n frame abc integer y \ny <- 3; if(y >= 3){image abc integer bcd}}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
//		Token t;
//		while((t = scanner.nextToken()) != null){
//			System.out.print(t.getText() + "   --->type: " + t.kind + " ---->    position: " + t.getLinePos());
//			System.out.println();	
//		}
//		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		//thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		System.out.println(v.getSymTbl());
	}
	
	// declare many variables in different scope
	@Test
	public void testSymbolTable() throws Exception{
		//TODO add string for symbol table test
		String input = "program1 integer a, boolean b, url x, file m \n"
				+ "{ /*this is comment test, it wont appear in anywhere except here 098*123 */ \n"
				+ "image k frame n integer f integer g boolean e e <- true; f <- 2; a <- 3; sleep \n"
				+ "f; while(e){\n"
				+ "g <- 10; frame x if((f+f*g) != a){\n"
				+ "f <- (f / (g + a)); image y y -> convolve -> x; }\n"
				+ "k->scale(12)->m;}"
				+ "x -> k -> n -> xloc; \n}";
		
		testFuncWithoutError(input);
	}

	@Test
	public void testSymbolTable1() throws Exception{
		//TODO add string for symbol table test
		String input = "p1 url u, file f\n"
				+ "{image u integer a integer b boolean c c <- false; a <- (b+b*3)-a; if(a != 3){c <- true; integer aa"
				+ "\n} while(c){f -> u -> gray -> scale(2) -> a -> f; "
				+ "if(c){while(b == a){frame k k -> move(20, 11) -> show; }\n}}"
				+ "scale (2) -> f; "
				+ "\n}";
		
		testFuncWithoutError(input);
	}
	
	
	private void testFuncWithError(String input) throws Exception{
		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		Scanner scanner = new Scanner(input);
		scanner.scan();
//		Token t;
//		while((t = scanner.nextToken()) != null){
//			System.out.print(t.getText() + "   --->type: " + t.kind + " ---->    position: " + t.getLinePos());
//			System.out.println();	
//		}
//		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		//System.out.println(v.getSymTbl());
	}
	
	private void testFuncWithoutError(String input) throws Exception{
		Scanner scanner = new Scanner(input);
		scanner.scan();
//		Token t;
//		while((t = scanner.nextToken()) != null){
//			System.out.print(t.getText() + "   --->type: " + t.kind + " ---->    position: " + t.getLinePos());
//			System.out.println();	
//		}
//		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		//thrown.expect(TypeCheckVisitor.TypeCheckException.class);
		System.out.println(v.getSymTbl());
	}
}
