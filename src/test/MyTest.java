package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cop5556sp17.Scanner.Kind;

public class MyTest {
	
	
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
