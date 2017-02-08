package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;


public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void teststatement0_ifwhile() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "while(abc >= 10){sleep true; abc <- 12 + 23;}";
		//String input = "if(true){abc <- 12 + 1; abc |-> bcd;}";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.statement();
//		Scanner scanner1 = new Scanner(input);
//		scanner1.scan();
//		Token t;
//		while((t=scanner1.nextToken()) != null){
//			System.out.println(t.getText());
//		}
	}
	
	@Test
	public void teststatement0_chain() throws IllegalCharException, IllegalNumberException, SyntaxException {
		//String input = "abc -> gray |-> _abc1 -> hide (abc,123) -> scale ;";
		String input = "abc + 123 == 12";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.statement();
		Scanner scanner1 = new Scanner(input);
		scanner1.scan();
		Token t;
		while((t=scanner1.nextToken()) != null){
			System.out.println(t.getText());
		}
	}
	
	@Test
	public void teststatement0_mix() throws IllegalCharException, IllegalNumberException, SyntaxException {
		//String input = "abc -> gray |-> _abc1 -> hide (abc,123) -> scale ;";
		String input = "if((screenwidth & screenheight)|(true % false) == 12/20 != _abc){image _mm123 sleep _mm123 + 7 > 5;} \n ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.statement();
		Scanner scanner1 = new Scanner(input);
		scanner1.scan();
		Token t;
		while((t=scanner1.nextToken()) != null){
			System.out.println(t.getText());
		}
	}
	
	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
	}
	
	@Test
	public void testParamDec()  throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "url abc";
		String input1 = "file abc";
		String input2 = "integer abc";
		String input3 = "boolean abc";
		String input4 = "123 abc";
		String input5 = "true abc";
		Scanner scanner = new Scanner(input3);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.paramDec();
	}
	
	

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		//String input = "  (3,5) ";
		//String input1 = "  (3,5 ";
		//String input = ";";
		String input = "((abc * 123 | true == screenheight != screenwidth), abc, abc >= 123)";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		System.out.println(scanner);
		Parser parser = new Parser(scanner);
        parser.arg();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}


	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 {}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException{
//		String input1 = "prog1 {integer _abc}";
//		String input2 = "prog1 {boolean abc}";
//		String input3 = "prog1 {image abc012}";
		String input4 = "prog1 {if((screenwidth & screenheight)|(true % false) == 12/20 != _abc){image _mm123 sleep _mm123 + 7 > 5;} while(true){}}";
//		String input5 =  "prog1 }";
		
		Parser parser = new Parser(new Scanner(input4).scan());
		parser.parse();
	}
}









































































