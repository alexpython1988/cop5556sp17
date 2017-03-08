package cop5556sp17.AST;

import cop5556sp17.Parser;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;

public class Type  {
	
	public static TypeName getTypeName(Token token) throws SyntaxException{
		switch (token.kind){
		case KW_INTEGER: {return TypeName.INTEGER;} 
		case KW_BOOLEAN: {return TypeName.BOOLEAN;} 
		case KW_IMAGE: {return TypeName.IMAGE;} 
		case KW_FRAME: {return TypeName.FRAME;} 
		case KW_URL: {return TypeName.URL;} 
		case KW_FILE: {return TypeName.FILE;} 
		default: throw new Parser.SyntaxException("illegal type, expected [Integer, Boolean, Image, "
				+ "Frame, URL, File], but get " + token.kind + " at pos: " + token.getLinePos());
		}		
	}

	public static enum TypeName {
		INTEGER("tn_integer"), 
		BOOLEAN("tn_boolean"), 
		IMAGE("tn_image"), 
		FRAME("tn_frame"),
	    URL("tn_url"), 
	    FILE("tn_file"), 
	    NONE("tn_none");
		
		final String text;

		public String getText() {
			return text;
		}
		
		TypeName(String text){
			this.text = text;
		}
		
		public boolean isType(TypeName... types){
			for (TypeName type: types){
				if (type.equals(this)) return true;
			}
			return false;
		}
		
		public boolean isType(TypeName type){
			if (type.equals(this)) 
				return true;
			else
				return false;
		}
	}
}
