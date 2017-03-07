package cop5556sp17.AST;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;

public abstract class ChainElem extends Chain {
	Kind kind;
	
	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public ChainElem(Token firstToken) {
		super(firstToken);
	}
}
