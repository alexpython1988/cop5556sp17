package cop5556sp17;

import java.util.Hashtable;
import java.util.Stack;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	//TODO  add fields
	Stack<Integer> st;
	int scopeNum;
	//check MyTest for hashmap implementation

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
		scopeNum++;	
		st.push(scopeNum);
	}
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
		st.pop();
		scopeNum--;
	}
	
	public boolean insert(String ident, Dec dec) throws SyntaxException{
		//TODO:  IMPLEMENT THIS
		Token type = dec.getType();
		dec.setTypeName(type);
		
		
		
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		st = new Stack<Integer>();
		scopeNum = 0;
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	

	public Integer getCurrentScope() {
		return st.peek();
	}
}
