package cop5556sp17.AST;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	
	TypeName typeName;
	
	public TypeName getTypeName(){
		return typeName;
	}

	public void setTypeName(Token t) throws SyntaxException {
		this.typeName = Type.getTypeName(t);
	}

	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
