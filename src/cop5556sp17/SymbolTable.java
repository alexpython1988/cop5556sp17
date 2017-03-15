package cop5556sp17;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import cop5556sp17.AST.Dec;


public class SymbolTable {
	
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
		scopeNum++;	
		st.push(scopeNum);
	}
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		st.pop();
		scopeNum--;
	}
	
	public boolean insert(String ident, Dec dec){
		if(varNames.containsKey(ident)){
			LinkedList<Map<String, Object>> idents = varNames.get(ident);
			int cScope = st.peek();
			for(Map<String, Object> each: idents){			
				if(cScope == (int) each.get("scope")){
					//System.out.println("The " + ident + "is already defined.");
					return false;
				}
			}
			attributes = new HashMap<String, Object>();
			attributes.put("scope", cScope);
			attributes.put("info", dec);
			idents.addFirst(attributes);
		}else{
			//insert new ident into the symbol table
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
		Dec dec = null; 
		//int cScope = st.peek();
		if(varNames.keySet().contains(ident)){
			int min = -2;
			for(Map<String, Object> each: varNames.get(ident)){
				int res = st.search((int) each.get("scope"));
				Dec temp = (Dec) each.get("info");
				if(min == -2 || min >= res){ 
					min = res;
					dec = temp;
				}
			}
//			
//			if(min == -1){
//				return null;
//			}
//		}else{
//			return null;
		}
		
		return dec;
	}
		
	public SymbolTable() {
		st = new Stack<Integer>();
		varNames = new Hashtable<String, LinkedList<Map<String, Object>>>();
		scopeNum = 0;
		st.push(scopeNum);
	}


	@Override
	public String toString() {
		String lines = "variable\t\tinformation\n";
		Iterator<Entry<String, LinkedList<Map<String, Object>>>> itr =  varNames.entrySet().iterator();
		while(itr.hasNext()){
			Entry<String, LinkedList<Map<String, Object>>> en  = itr.next();
			String varName = en.getKey();
			for(Map<String, Object> inf:  en.getValue()){
				lines += varName + "\t\t" + inf + "\n";
			}
		}
		return "Symbol Table\n\n" + lines;
	}
	

	public Integer getCurrentScope() {
		return st.peek();
	}
}
