package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.junit.Test;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.Type.TypeName;

public class MyTest {
	@Test
	public void testref(){
		String s = null;
		changeString(s);
		System.out.println(s);
	}
	
	public void changeString(Object s){
		String a = (String) s;
		a = "alex";
	}
	
	@Test
	public void testpp(){
		int[] a = {1,2,3};
		int i = 1;
		// prove that java evaluation expression from left to right
		a[i] = i++ + 1;
		for(int k : a)
			System.out.print(k + " ");
	}
	
	@Test
	public void testLinkedList(){
		LinkedList<Integer> ll = new LinkedList<Integer>();
		int[] a = new int[]{1,2,3,4,5};
		for(int each: a)
			ll.add(0, each);
		
		System.out.println(ll);
		
	}
	
	@Test
	public void testSwitch(){
		int[] arr = new int[]{1,2,5,6,7};
		for(int i: arr){
			switch1(i);
		}
 	}
	
	private void switch1(int i){	
		switch (i) {
		case 1:
		case 2:{
			System.out.println("a");
		}		
			break;
		case 3:
			System.out.println("b");
			break;
		case 4:
			System.out.println("c");
			break;

		default:
			switch (i*i) {
			case 25:
				System.out.println("d");
				break;
			case 36:
				System.out.println("e");
				break;

			default:
				System.out.println("f");
				break;
			}
		}
	}
	
	@Test
	public void testStack(){
		Stack<Integer> st = new  Stack<Integer>();
		st.push(1);
		st.push(2);
		st.push(3);
		st.push(4);
		
		System.out.println(st.search(3));
	}
	
    @Test
    public void testprintframe(){
    	System.out.println("a \t b \t c \t d \ne \t f \t h\t m");
    }
	
	@Test
	public void testtypename1(){
		Hashtable<String, LinkedList<Map<String, Object>>> v = new Hashtable<>();
		LinkedList<Map<String, Object>> ll = new LinkedList<Map<String, Object>>();
		
		Map<String, Object> a = new HashMap<String, Object>();
		a.put("scope", 4);
		a.put("typename", TypeName.IMAGE);
		Map<String, Object> a1 = new HashMap<String, Object>();
		a1.put("scope", 2);
		a1.put("typename", TypeName.INTEGER);
		Map<String, Object> a2 = new HashMap<String, Object>();
		a2.put("scope", 3);
		a2.put("typename", TypeName.BOOLEAN);
		
		ll.add(a1);
		ll.add(a);
		ll.add(a2);
		
		v.put("t", ll);
		
		
		ll = new LinkedList<Map<String, Object>>();
		ll.add(a);
		v.put("t1",ll);
		
		System.out.println(v);
		System.out.println(v.get("t"));
		System.out.println(v.keySet().contains("t"));
		
		lookup("t1", v);
		
//		LinkedList<Map<String, Object>> l1 = v.get("t");
//		for(int i = 0; i < l1.size(); i++){
//			System.out.println(l1.get(i).get("scope"));
//		}
		
//		for(Map<String, Object> each: l1){
//			System.out.println(each.get("typename"));
//		}
		
//		Iterator<Entry<String, LinkedList<Map<String, Object>>>> itr = v.entrySet().iterator();
//		while(itr.hasNext()){
//			Entry<String, LinkedList<Map<String, Object>>> en =  itr.next();
//			String name = en.getKey();
//			for(Map<String, Object> m :en.getValue()){
//				System.out.println(name + "\t\t" + m);
//			}
//		}
	}
	
	void lookup(String s, Hashtable<String, LinkedList<Map<String, Object>>> v){
		String info = "no found!";
		int min = -2;
		if(v.keySet().contains(s)){
			System.out.println(1);
			for(Map<String, Object> each: v.get(s)){
				if(min == -2 || min >= (int)each.get("scope")){
					min = (int)each.get("scope");
					info = "" + (TypeName)each.get("typename");
				}
			}
		}
		
		System.out.println(info);
	}
	
	@Test
	public void testtypename(){
		//System.out.println(TypeName.BOOLEAN.getText());
		//String str = "t_url";
		Hashtable<String, Map<String, Object>> v = new Hashtable<>();
		Map<String, Object> a = new HashMap<String, Object>();
		a.put("scope", 1);
		a.put("typename", TypeName.IMAGE);
		Map<String, Object> a1 = new HashMap<String, Object>();
		a1.put("scope", 2);
		a1.put("typename", TypeName.INTEGER);
		
		v.put("v1", a);
		v.put("v1", a1);
		v.put("v2", a1);
		
		System.out.println(v);
		
		
		int i = (int) v.get("v2").get("scope");
		TypeName tn = (TypeName) v.get("v1").get("typename");
		System.out.println(i);
		System.out.println(tn);

		//System.out.println(v.get("a1").get("scope"));
		//System.out.println(v.get("a1").get("typename"));
	}
	
	static enum TypeName {
		INTEGER("t_integer"), 
		BOOLEAN("t_boolean"), 
		IMAGE("t_image"), 
		FRAME("t_frame"),
	    URL("t_url"), 
	    FILE("t_file"), 
	    NONE("t_none");
		
		final String text;

		public String getText() {
			return text;
		}
		
		TypeName(String text){
			this.text = text;
		}
	}
	
	@Test
	public void testtable(){
		Hashtable<String, Map<String, String>> ht = new Hashtable<String, Map<String, String>>();
		HashMap<String, String> hm1 = new HashMap<String, String>(); 
		hm1.put("hm11", "hm11a");
		hm1.put("hm12", "hm12a");
		HashMap<String, String> hm2 = new HashMap<String, String>(); 
		hm2.put("hm21", "hm21a");
		hm2.put("hm22", "hm22a");
		HashMap<String, String> hm3 = new HashMap<String, String>(); 
		hm3.put("hm31", "hm31a");
		hm3.put("hm32", "hm32a");
		ht.put("t1", hm1);
		ht.put("t2", hm2);
		
		if(ht.containsKey("t1")){
			
		}
		
		System.out.println(ht);
	}
	
	@Test
	public void testprint(){
		System.out.println(TypeName.FILE);
		Stack<Integer> st = new Stack<Integer>();
		
		int j = 0;
		
		for(int i = 1; i < 5; i++){
			j++;
			st.push(j);
			
			System.out.println(st.peek());
			
			
		}
		System.out.println();
		
		for(int i = 1; i < 5; i++){
			System.out.println(st.peek());
			st.pop();
			j--;
			//System.out.println(st.peek());
		}
		
		System.out.println();
		System.out.println(j);
		System.out.println(st.isEmpty());
		
	}
	
	@Test
	public void testSep(){
		int i = 1;
		int j = 2;
		
		int c = i++ + j++;
		System.out.println(c +i+j +"");
		System.out.println(c +""+i+j );
		System.out.println(c +i+""+j );
	}
	
	@Test
	public void testDotArgs(){
		mytest(1,2,3,4,5);
	}
	
	public void mytest(int... i){
		for(int k: i){
			System.out.println(k);
		}
	}

	@Test
	public void testHashMapWithList(){
		Map<Integer, List<Mytest>> map = new HashMap<Integer, List<Mytest>>();
		int j = 0;
		while(j < 5){
			List<Mytest> list = new LinkedList<Mytest>();
			for(int i = 0; i < 5; i++){
				list.add(new Mytest(i, "alex" + i + j));
			}
			
			map.put(j, list);
			j++;
		}
		
		List<Mytest> l = map.get(3);
		Mytest mt = l.get(2);
		System.out.println(mt.getPos());
		System.out.println(mt.getName());
		Mytest mt1 = l.get(3);
		System.out.println(mt1.getPos());
		System.out.println(mt1.getName());
		
		List<Mytest> l1 = map.get(4);
		Mytest mt2 = l1.get(0);
		System.out.println(mt2.getPos());
		System.out.println(mt2.getName());
		Mytest mt3 = l1.get(1);
		System.out.println(mt3.getPos());
		System.out.println(mt3.getName());
	}
	
	//bean
	private class Mytest{
		int pos;
		String name;
		
		public Mytest(int pos, String name) {
			super();
			this.pos = pos;
			this.name = name;
		}
		public int getPos() {
			return pos;
		}

		public String getName() {
			return name;
		}
	}
	
	//efficiency is very bad
	@Test
	public void testFind(){
		List<Integer> al = new ArrayList<Integer>();
		al.add(0);
		al.add(4);
		al.add(8);
		al.add(12);
		al.add(16);
//		
		int[] arr = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,100};
		for(int i: arr){
			int start = 0;
			int end = al.size() - 1;
			while(i >= 0){
				int x = bianrySearch(start, end, al, i);
				if(x == -1){
					i--;
				}else{
					System.out.println(x);
					break;
				}
			}
		}
	}
	
	private int bianrySearch(int start, int end, List<Integer> al, int i){

		while(start <= end){
			int mid = (start + end)/2;
			int midNum = al.get(mid);
			//System.out.println("mid is" + mid);
			if(i == midNum){
				return al.indexOf(i);
				
			}else if(i < midNum){
				
				end = mid - 1;
				continue;
			}else{
				start = mid + 1;
			}
		}
		
		return -1;
	}
	

	@Test
	public void testNR(){
		String s = "abc\ndef\rhij\topqr";
		System.out.println(s);
		System.out.println(s.charAt(12));
	}
	
	@Test
	public void testTryCathchOrder(){
		try{
			System.out.println("1");
			int i = 1/1;
			//int i =1/0;
			System.out.println(i);
			System.out.println("2");
			System.out.println("3");
		}catch(Exception e){
			
		}
	}
	
	@Test
	public void testSpecialChar() throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(new File("1.txt")));
		
		String line;
		//char ch;
		
		while((line = br.readLine()) != null){
			line += "\n";
			for(int i = 0; i < line.length(); i++)
				System.out.print(line.charAt(i));
			
		}
		
		br.close();
	}
	
	@Test 
	public void testWhiteSpace(){
//		char ch1 = '\t';
//		char ch2 = ' ';
//		System.out.println(Character.isSpaceChar(ch2) + "  " +Character.isSpaceChar(ch1));
		
		String str = "abc\ndefg\nhigkl\nnmopqs\n /*   */";
		//int line;
		int pos = 16;
		char ch;
		List<Integer> startPosForEachLine = new ArrayList<Integer>();
		startPosForEachLine.add(0);
		for(int i = 0; i < str.length(); i++){
			ch = str.charAt(i);
			if(ch == '\n' && i != (str.length() -1)){
				startPosForEachLine.add(i+1);
			}
			
			System.out.print(ch);
		}
		
		int i = 0;

		//use pos to find which line it belongs to
		for(; i < startPosForEachLine.size(); i++){
			if(pos > startPosForEachLine.get(i)){
				continue;
			}
			else if (pos == startPosForEachLine.get(i)){
				System.out.println(i);
				break;
			}
			else{
				break;
			}
		}
		
		System.out.println(i-1);
		
		
//		System.out.println(Arrays.binarySearch(startPosForEachLine.toArray(), 10));
//		System.out.println(Arrays.binarySearch(startPosForEachLine.toArray(), 7));
//		System.out.println(Arrays.binarySearch(startPosForEachLine.toArray(), 16));
	}
	
	@Test
	public void testSkipWhite(){
		//String s1 = "   string";
		//String s2 = "  string";
		//String s3 = " string";
		String s4 = "string";
		String s5 = "        ";
		//System.out.println(s5.length());
		//skipwhite(s1);
		//skipwhite(s2);
		//skipwhite(s3);
		skipwhite(s4, s4.length());
		skipwhite(s5, s5.length());
		
//		State state = State.START;
//		System.out.println("hello" + state);
	}

	@Test
	public void testEnum(){
		for(Kind k: Kind.values()){
			System.out.println(k.getText());
		}
	}
	
	//test word when change then reserved_words from protected to public
	//test produced the expected results 
//	@Test
//	public void testReservedWordsHashMap(){
//		for(Kind k: Kind.values()){
//			if(Scanner.reserved_words.containsKey(k.getText()))
//				continue;
//			else 
//				System.out.println(k.getText());
//		}
//	}
	int k;
	ArrayList<String> k1;
	
	@Test
	public void testCOunt(){
		k ++;
		k1 = new ArrayList<String>();
		k1.add("a");
		sum();
		k1.add("e");
		for(String each: k1)
			System.out.println(each);
	}
	
	public void sum1(String s){
		k1.add(s);
	}
	
	public void sum(int i, int c, int j){		
		c += i;
		System.out.println(c + "  " + j + "  " + k);
		k++;
	}
	public void sum(){		
		k1.add("b");
		String[] s = {"c", "d"};
		for(String each : s)
			sum1(each);
	}
	
	@Test
	public void testSubString(){
		String str = "1234";
		
		String sstr = str.substring(0,1);
		
		System.out.println(sstr);
	}
	
	private static void skipwhite(String s, int len){
		int sPos = 0;
		while(Character.isSpaceChar(s.charAt(sPos))){
			sPos ++;
			
			if(sPos == len)
				break;
		}
		
		System.out.println(sPos);
		//System.out.println(s.charAt(sPos));
	}
}

class Test1{
	static int x;
	String y;
	
	public static void main(String[] args){
		test1(1,2);
		
		System.out.println(x + 10);
	}
	
	static void test1(int a, int b){
		x = a + b;
	}
	
	String getTest1(String str){
		return y + str;
	}
	
	public void method1(){
		int i = 0;
		boolean b;
		b = true;
		if(b)
			i ++;
		System.out.println(i);
	}
	
}

class Name implements Runnable{
    //variables declared in List<ParamDec> are instance variables of the class
	//int i;
//	int i1;	
//	boolean j;
	int a;
//	int b;
//	int c;
//	boolean bool0;
	
	public Name(String[] args){
       //initialize instance variables with values from args.
	    	//a = Integer.parseInt(args[0]);
//	    	i1 = Integer.parseInt(args[2]);
//	    	j = Boolean.parseBoolean(args[1]);
			//a = 3;
        }
   
	public static void main(String[] args){
    	Name instance = new Name(args);
               instance.run();
        }
	public void run(){
//		int i;
//		int k;
//		i = 2;
//		while(i == 1){
//			k = 2;
//			bool0 = i1 > 2;
//		}
		
		int i = 2;
		a = i + 3;
		
		int j = a;
	}
//        public void run(){
//        	a = 4; 
//        	b = 5; 
//        	boolean boolA; 
//        	boolean boolB; 
//        	boolA = true; 
//        	boolB = false; 
//        	if(boolA == true) 
//        	{boolean a; a = boolA; 
//        	bool0 = false;
//        	while(a != boolB)
//        	{int d; int e; c = 3 + 5; 
//        	d = 10 - 1; 
//        	c = c * d; 
//        	e = d / 3; 
//        	a = boolB;
//        	if(c > d) 
//        	{ c = d; 
//        	if(c <= d) { boolA = false; }; 
//        	if(boolA) { c = 0; };
//        	};
//        	}; 
//        	}; 
//        	if(c >= 1) { /*boolB <- bool0 | true;*/} a = 7;
//        }

       //declarations and statements from block
//        int m = 10;
//        int n = 0;
//        
//        while(m > n){
//        	int k =10;
//        	int p = k + n; 
//        	i = p;
//        }
//        
//        if(m > n){
//        	int k =10;
//        	int p = k + n; 
//        	i = p;
//        }
//        
//        System.out.println(i);
//        }
//        public void go(){ 
//        	  int m = 10;
//              int n = 0;
//        	if(i > i1){
//        		int p = i;
//        		i = p*10;
//        	}
//        	 if(j){
//             	i = m + (2*5);
//             	n = m + (2*5);
//             	boolean j1 = true;
//             	System.out.println(j1);
//             }
//        }
	
	
	
	
}







