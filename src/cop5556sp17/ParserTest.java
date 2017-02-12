package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		String input = "abc -> gray |-> _abc1 -> hide (abc,123) -> scale ;";
//		String input = "abc + 123 == 12";
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
		String input = "prog0 url _anc, file $abc, integer mm, boolean ft{}";

		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}
	
	@Test
	public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input1 = "prog1 {integer _abc}";
		String input2 = "prog1 {boolean abc}";
		String input3 = "prog1 {image abc012}";
		String input4 = "prog1 {if((screenwidth & screenheight)|(true % false) == 12/20 != _abc){image _mm123 sleep _mm123 + 7 > 5;} while(true){}}";
		String input5 =  "prog1 }";
		
		List<String> list = new ArrayList<String>();
		list.add(input1);
		list.add(input2);
		list.add(input3);
		list.add(input4);
		list.add(input5);
		
		testCases(list);
		
	}
	
	@Test
	public void testChainElem0() throws IllegalCharException, IllegalNumberException, SyntaxException{
//		String input = "abc";
		String input = "blur (a + b > 5)";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.chainElem();
	}
	
	@Test
	public void testChain0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "abc |-> move (screenwidth == 12) -> scale (true)";
//		String input = "yloc -> xloc";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.chain();
		Scanner scanner1 = new Scanner(input);
		scanner1.scan();
		Token t;
		while((t=scanner1.nextToken()) != null){
			System.out.println(t.kind);	
		}
	}
	
	@Test
	public void testAssign1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "_abc <- 2 * 3 - 5";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.assign();
		
		Scanner scanner1 = new Scanner(input);
		scanner1.scan();
		Token t;
		while((t=scanner1.nextToken()) != null){
			System.out.println(t.kind);
		}
	}
	
	@Test
	public void testStatement1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		//String input = "sleep 123;";
		//String input = "if (1){}";
		//String input = "while (1){}";
		//String input  = "abc |-> convolve(123) ->hide |-> move;";
		String input = "bac -> a123;";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.statement();
		
		Scanner scanner1 = new Scanner(input);
		scanner1.scan();
		Token t;
		while((t=scanner1.nextToken()) != null){
			System.out.println(t.kind);
		}
	}
	
	@Test
	public void testblock0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		//String input = "{integer abc if(true){}}";
		String input = "{sleep _abc; if(true){boolean _ab23} abc <- (2 + 4)*5; }";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.block();
		
		Scanner scanner1 = new Scanner(input);
		scanner1.scan();
		Token t;
		while((t=scanner1.nextToken()) != null){
			System.out.println("*" + t.kind);
		}
	}
	
	
	@Test
	public void testparse123() throws IllegalCharException, IllegalNumberException, SyntaxException{
		List<String> list = new ArrayList<String>();
		
		list.add("program0 url google, file google_drive \n"
				+ "{boolean abc abc <- true;\n"
				+ "move (abc) |-> google_drive; sleep(110)              ; \n"
				+ "while(abc < 10){abc <- abc - 1; abc -> google -> show;}\n"
				+ "}");	
		list.add("/*this is a test for grammer check\n*/"
				+ "program0 {image _abc _abc <-(2+3)*5;\n "
				+ "_abc |-> show (123, 456) -> blue;\n"
				+ "sleep (2*5 != (3+5)*2);\n"
				+ "/*comment line again*/\n"
				+ "if(bac > 12){integer r0 integer r1 r0 <-12; r1 <- 34;\n"
				+ "xloc (screenwidth|(r0), screenheight&(r1)) -> yloc(screenwidth+(r1), screenheight*(r0)) |-> convolve;}\n"
				+ "}");
		list.add("pro123 integer a1, boolean b1{\n"
				+ "frame $xyz123 sleep(true);\n"
				+ "image img  while(img != false){img <- ((4+6)*10 == 100);\n"
				+ "img -> hide;}\n"
				+ "integer abc if((abc-1)>(bcd*5)){abc -> xloc -> bcd -> yloc |-> width(123,456) |-> height(456,123);\n"
				+ "abc <- abc == 20; bcd <- bcd != 20;}\n"
				+ "}");
		list.add("aaa{}");
			
		testCases(list);
	}
	
	private void testCases(List<String> arr){
		Map<Integer, String> err = new HashMap<Integer, String>();
		boolean flag = true;
		for(int i = 0; i < arr.size(); i++){
			try{
				Parser parser = new Parser(new Scanner(arr.get(i)).scan());
				parser.parse();
			}catch (Exception e) {
				if(flag)
					flag = false;
				err.put(i, e.getMessage());
				continue;
			}
		}
		
		if(flag)
			System.out.println("No error found!");
		else{
			Iterator<Entry<Integer, String>> itr = err.entrySet().iterator();
			for(;itr.hasNext();){
				Entry<Integer, String> en = itr.next();
				int i = en.getKey();
				String s = en.getValue();
				System.out.println("Test case " + (i+1) + " report error: " + s);
			}
		}
	}
	
}









































































