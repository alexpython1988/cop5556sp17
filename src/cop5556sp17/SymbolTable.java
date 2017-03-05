package cop5556sp17;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	//TODO  add fields
	Stack<Integer> st;
	int scopeNum;
	//check MyTest for hashmap implementation
	Hashtable<String, LinkedList<Map<String, Object>>> varNames;
	LinkedList<Map<String, Object>> attrList;
	Map<String, Object> attributes;
	 
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
		if(varNames.containsKey(ident)){
			LinkedList<Map<String, Object>> idents = varNames.get(ident);
			int cScope = getCurrentScope();
			for(Map<String, Object> each: idents){
				int identScope = (int) each.get("scope");
				if(cScope == identScope){
					System.out.println("The " + ident + "is already defined.");
					return false;
				}
			}
			
			
		}else{
			//insert new ident into the symboltable
			Token type = dec.getType();
			dec.setTypeName(type);
			attributes = new HashMap<String, Object>();
			attributes.put("scope", st.peek());
			attributes.put("info", dec);
			
		}
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		st = new Stack<Integer>();
		varNames = new Hashtable<String, LinkedList<Map<String, Object>>>();
		attrList = new LinkedList<Map<String, Object>>();
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
	
//	private String strScopeNum(int scopeNum){
//		return Integer.toString(scopeNum);
//	}
}
