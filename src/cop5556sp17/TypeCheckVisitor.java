package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;
import cop5556sp17.AST.Type.TypeName;
import java.util.ArrayList;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

/*
 * all the expression related visitor methods need to return their type name
 */

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();
	
	public SymbolTable getSymTbl(){
		return symtab;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, null);
		Token arrow = binaryChain.getArrow();
		binaryChain.getE1().visit(this, null);
		
		TypeName tn0 = binaryChain.getE0().getTypeName();
		ChainElem ce = binaryChain.getE1();
		
		switch (tn0) {
		case URL:
		case FILE:{
			if(binaryChain.getE1().getTypeName().isType(IMAGE) && arrow.isKind(ARROW)){
				binaryChain.setTypeName(IMAGE);
			}else{
				throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
						" The type of chain element is expected as IMAGE and "
						+ "the operator is expected to be ARROW, but get chain element as " + 
						binaryChain.getE1().getTypeName() + " and operator as" + arrow.kind );
			}
		}
			break;

		case FRAME:{
			if(arrow.isKind(ARROW)){
				if(ce.getFirstToken().isKind(KW_XLOC, KW_YLOC) && ce instanceof FrameOpChain){
					binaryChain.setTypeName(INTEGER);
				}else if(ce.getFirstToken().isKind(KW_SHOW, KW_HIDE, KW_MOVE) 
						&& ce instanceof FrameOpChain){
					binaryChain.setTypeName(FRAME);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
							" Chain element must be FrameOpChain"
							+  " and the tokens must start with [KW_XLOC, KW_YLOC, KW_SHOW, KW_HIDE, KW_MOVE]"
							+ " but get " + ce.getFirstToken().kind);
				}
			}else{
				throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + "Illegal operator: expected ARROW but get " + arrow.kind);
			}
		}		
			break;
		
		//code can be reorganized 
		case IMAGE:{
			if(ce.getFirstToken().isKind(OP_WIDTH, OP_HEIGHT) && ce instanceof ImageOpChain){
				if(arrow.isKind(ARROW)){
					binaryChain.setTypeName(IMAGE);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
							" Illegal operator. Expected ARROW but get " + arrow.kind);
				}
			}else if(ce.getTypeName().isType(FRAME)){
				if(arrow.isKind(ARROW)){
					binaryChain.setTypeName(FRAME);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
							" Illegal operator. Expected ARROW but get " + arrow.kind);
				}
			}else if(ce.getTypeName().isType(FILE)){
				if(arrow.isKind(ARROW)){
					binaryChain.setTypeName(NONE);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
							" Illegal operator. Expected ARROW but get " + arrow.kind);
				}
			}else if(ce.getFirstToken().isKind(OP_GRAY, OP_BLUR, OP_CONVOLVE) && ce instanceof FilterOpChain){
				if(arrow.isKind(ARROW, BARARROW)){
					binaryChain.setTypeName(IMAGE);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
							" Illegal operator. Expected ARROW or BARARROW but get " + arrow.kind);
				}
			}else if(ce.getFirstToken().isKind(KW_SCALE) && ce instanceof ImageOpChain){
				if(arrow.isKind(ARROW)){
					binaryChain.setTypeName(IMAGE);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
							" Illegal operator. Expected ARROW but get " + arrow.kind);
				}
			}else if(ce instanceof IdentChain){
				if(arrow.isKind(ARROW)){
					binaryChain.setTypeName(IMAGE);
				}else{
					throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() +
							" Illegal operator. Expected ARROW but get " + arrow.kind);
				}
			}else{
				throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() +
						" Illegal type for chain element." + ce.getFirstToken().getText());
			}
		}
			break;
		
		default:
			throw new TypeCheckException("At pos: " + binaryChain.getFirstToken().getLinePos() + 
					" Illegal type for chain. Expected [URL, FILE, FRAME, IMAGE]"
					+ " but get " + tn0);
		}
		
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		//XXX consider the logic order?
		TypeName binaryExprType = null;
		TypeName t0 = (TypeName) binaryExpression.getE0().visit(this, null);
		Token operator = binaryExpression.getOp();
		TypeName t1 = (TypeName) binaryExpression.getE1().visit(this, null);
		
		switch (t0) {
		case INTEGER:{
			if(t1.isType(INTEGER)){
				if(operator.isKind(PLUS, MINUS, TIMES, DIV)){
					binaryExprType = TypeName.INTEGER;
				}else if(operator.isKind(LT, GT, LE, GE)){
					binaryExprType = TypeName.BOOLEAN;
				}else{
					throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() +
							" Illegal operator, expected one of ops in "
							+ "[PLUS, MINUS, TIMES, DIV,LT, GT, LE, GE], but get " + operator.kind);
				}
			}else if(t1.isType(IMAGE)){
				if(operator.isKind(TIMES)){
					binaryExprType = TypeName.IMAGE;
				}else{
					throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
							" Illegal operator, expected PLUS, but get " + operator.kind);
				}
			}else{
				throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
						" Illegal type: expected one of types in [INTEGER, IMAGE] "
						+ "but get " + t1);
			}
		}
			break;
		
		case IMAGE:{
			if(t1.isType(IMAGE)){
				if(operator.isKind(PLUS, MINUS)){
					binaryExprType = TypeName.IMAGE;
				}else{
					throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
							" Illegal operator, expected PLUS or MINUS, but get " + operator.kind);
				}
			}else if(t1.isType(INTEGER)){
				if(operator.isKind(Kind.TIMES)){
					binaryExprType = TypeName.IMAGE;
				}else{
					throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
							" Illegal operator, expected TIMES, but get " + operator.kind);
				}
			}else{
				throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
						" Illegal type: expected one of types in [INTEGER, IMAGE] "
						+ "but get " + t1);
			}
		}
		 	break;
		
		case BOOLEAN:{
			if(t1.isType(BOOLEAN)){
				binaryExprType = TypeName.BOOLEAN;
			}else{
				throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
						" Illegal type: expected BOOLEAN, but get " + t1);
			}
		}
			break;
		 	
		default:
			if((operator.isKind(EQUAL) || operator.isKind(NOTEQUAL)) && t0.equals(t1)){
				binaryExprType = BOOLEAN;
			}else{
				throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos() + 
						" Illegal binary expression. Check the expression start with token: " +
						binaryExpression.getE0().getFirstToken().getText());
			}
			
			break;
		}
		
		binaryExpression.setTypeName(binaryExprType);
		return binaryExprType;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		
		ArrayList<Dec> decs = block.getDecs();
		for(Dec dec: decs){
			dec.visit(this, null);
		}
		
		ArrayList<Statement> stats = block.getStatements();
		for(Statement stat: stats){
			stat.visit(this, null);
		}
		
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setTypeName(BOOLEAN);
		return booleanLitExpression.getTypeName();
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		int tupleSize = (int) filterOpChain.getArg().visit(this, null);
		
		if(!(tupleSize == 0)){
			throw new TypeCheckException("At pos: " + filterOpChain.getFirstToken().getLinePos() + 
					" The tuple size should be 0 but get " + tupleSize);
		}
		
		filterOpChain.setTypeName(IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Token frameOp = frameOpChain.getFirstToken();
		frameOpChain.setKind(frameOp.kind);
		
		int tupleSize = (int) frameOpChain.getArg().visit(this, null);
		
		if(frameOp.isKind(KW_SHOW, KW_HIDE)){
			if(!(tupleSize == 0)){
				throw new TypeCheckException("At pos:" + frameOpChain.getFirstToken().getLinePos() + " The tuple size should be 0 but get " + tupleSize);
			}
			frameOpChain.setTypeName(NONE);
		}else if(frameOp.isKind(KW_XLOC, KW_YLOC)){
			if(!(tupleSize == 0)){
				throw new TypeCheckException("At pos:" + frameOpChain.getFirstToken().getLinePos() + 
						" The tuple size should be 0 but get " + tupleSize);
			}
			frameOpChain.setTypeName(INTEGER);
		}else if(frameOp.isKind(KW_MOVE)){
			if(!(tupleSize == 2)){
				throw new TypeCheckException("At pos:" + frameOpChain.getFirstToken().getLinePos() + 
						" The tuple size should be 0 but get " + tupleSize);
			}
			frameOpChain.setTypeName(NONE);
		}else{
			throw new TypeCheckException("At pos:" + frameOpChain.getFirstToken().getLinePos() + 
					" Parser Error!");
		}
			
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if(dec == null){
			throw new TypeCheckException("At pos: " + identChain.getFirstToken().getLinePos() + 
					" The ident: " + identChain.getFirstToken().getText() +
					"is not defined or visible in current scope.");
		}
		
		identChain.setTypeName(dec.getTypeName());
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		
		if(dec == null){
			throw new TypeCheckException("At pos: " + identExpression.getFirstToken().getLinePos() + 
					" The ident: " + identExpression.getFirstToken().getText() +
					"is not defined or visible in current scope.");
		}
		
		identExpression.setDec(dec);
		identExpression.setTypeName(dec.getTypeName());
		
		return identExpression.getTypeName();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		TypeName tn = (TypeName) ifStatement.getE().visit(this, null);
		
		if(!tn.isType(BOOLEAN)){
			throw new TypeCheckException("At pos: " + ifStatement.getFirstToken().getLinePos() + 
					" Illegal expression, the expression in if statement does not return BOOLEAN, "
					+ "but " + tn);
		}
		
		ifStatement.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setTypeName(INTEGER);
		return intLitExpression.getTypeName();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		TypeName tn = (TypeName) sleepStatement.getE().visit(this, null);
		if(!tn.isType(INTEGER))
			throw new TypeCheckException("At pos: " + sleepStatement.getFirstToken().getLinePos() + 
					" The type in sleep statement should be Integer but get " + tn);
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		TypeName tn = (TypeName) whileStatement.getE().visit(this, null);
		if(!tn.isType(BOOLEAN))
			throw new TypeCheckException("at pos: " + whileStatement.getFirstToken().getLinePos() + 
					" Illegal expression, the expression in while statement "
					+ "does not return BOOLEAN but " + tn);
		
		whileStatement.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setTypeName(declaration.getType());
		if(!symtab.insert(declaration.getIdent().getText(), declaration))
			throw new TypeCheckException("At pos: " + declaration.getFirstToken().getLinePos() + 
					"The identifier " + declaration.getIdent().getText() + " is already defined in "
							+ " the current scope." );
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> list = program.getParams();
		for(ParamDec pd: list){
			pd.visit(this, null);
		}
		
		program.getB().visit(this, arg);
			
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
//		assignStatement.getVar().visit(this, null);
//		assignStatement.getE().visit(this, null);
		TypeName tni = (TypeName) assignStatement.getVar().visit(this, null);
		TypeName tne = (TypeName) assignStatement.getE().visit(this, null);
		if(!tni.equals(tne))
			throw new TypeCheckException("At pos: " + assignStatement.getFirstToken().getLinePos() + 
					" The type of identVar is " + tni + " but the given type of expression is " + tne);
		
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//int currentScope = symtab.getCurrentScope();
		Dec dec = symtab.lookup(identX.getText());
		
		if(dec == null)
			throw new TypeCheckException("At pos: " + identX.getFirstToken().getLinePos() +
					" The ident: " + identX.getText() + " is not defined or visible in current scope.");
		
		identX.setDec(dec);
		return dec.getTypeName();
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec))
			throw new TypeCheckException("At pos: " + paramDec.getFirstToken().getLinePos() + 
					"The identifier " + paramDec.getIdent().getText() + " is already defined in "
							+ " the current scope." );
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypeName(INTEGER);
		return constantExpression.getTypeName();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Token imageOp = imageOpChain.getFirstToken();
		imageOpChain.setKind(imageOp.kind);
		int tupleSize = (int) imageOpChain.getArg().visit(this, null);
		
		if(imageOp.isKind(OP_WIDTH, OP_HEIGHT)){
			if(tupleSize == 0){
				imageOpChain.setTypeName(INTEGER);
			}else{
				throw new TypeCheckException("At pos: " + imageOpChain.getFirstToken().getLinePos() 
						+ " The tuple size of ImageOpChain is expected to be 0 but get " + tupleSize);
				
			}
		}else if(imageOp.isKind(KW_SCALE)){
			if(tupleSize == 1){
				imageOpChain.setTypeName(IMAGE);
			}else{
				throw new TypeCheckException("At pos: " + imageOpChain.getFirstToken().getLinePos()  +
						" The tuple size of ImageOpChain is expected to be 1 but get " + tupleSize);
			}
		}
				
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {	
		for(Expression e: tuple.getExprList()){
			TypeName tn = (TypeName) e.visit(this, null);
			if(!tn.isType(INTEGER)){
				throw new TypeCheckException("At pos: " + tuple.getFirstToken().getLinePos() + 
						" Expect a INTEGER for "  + 
						e.getFirstToken().getText() + " , but got " + e.getTypeName());
			}
		}
		
		return tuple.getExprList().size();
	}
}
