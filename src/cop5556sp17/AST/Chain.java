package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	public Chain(Token firstToken) {
		super(firstToken);
	}
	
	TypeName typeName;
	
	public TypeName getTypeName(){
		return typeName;
	}

	public void setTypeName(Token t) throws SyntaxException {
		this.typeName = Type.getTypeName(t);
	}

}
