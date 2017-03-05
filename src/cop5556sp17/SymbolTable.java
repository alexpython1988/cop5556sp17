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
			int cScope = st.peek();
			for(Map<String, Object> each: idents){			
				if(cScope == (int) each.get("scope")){
					System.out.println("The " + ident + "is already defined.");
					return false;
				}
			}
			attributes = new HashMap<String, Object>();
			attributes.put("scope", st.peek());
			attributes.put("info", dec);
			varNames.get(ident).add(attributes);
		}else{
			//insert new ident into the symboltable
			Token type = dec.getType();
			dec.setTypeName(type);
			attributes = new HashMap<String, Object>();
			attributes.put("scope", st.peek());
			attributes.put("info", dec);
			attrList = new LinkedList<Map<String, Object>>();
			attrList.add(attributes);
			varNames.put(ident, attrList);
		}
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		Dec dec = null;
		int cScope = st.peek();
		if(varNames.contains(ident)){
			for(Map<String, Object> each: varNames.get(ident)){
				
			}
		}
		
		return dec;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
		st = new Stack<Integer>();
		varNames = new Hashtable<String, LinkedList<Map<String, Object>>>();
		scopeNum = 0;
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		//print the whole table
		
		return "";
	}
	

	public Integer getCurrentScope() {
		return st.peek();
	}
	
}
