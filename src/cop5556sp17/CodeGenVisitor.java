package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import com.sun.javafx.binding.SelectBinding.AsInteger;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int paramDecCounter = 0;
	int slotInRun = 0;
	int currentScope = 0;
	ArrayList<Dec> localVariable = new ArrayList<Dec>();
	//ArrayList<Label> runMethodLabelList = new ArrayList<Label>(); 

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv; // visitor of fields declared as global fields (in scope 0 in symbol table)
	
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);
		
		//visit all the fields in scope 0
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params){
			TypeName tn = dec.getTypeName();
			String decIdent = dec.getIdent().getText();
			if(tn.isType(TypeName.INTEGER)){
				fv = cw.visitField(0, decIdent, "I", null, null);
				fv.visitEnd();
			}else if(tn.isType(BOOLEAN)){
				fv = cw.visitField(0, decIdent, "Z", null, null);
				fv.visitEnd();
			}			
		}

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		//ArrayList<ParamDec> params = program.getParams();
		mv.visitVarInsn(ALOAD, 1);
		mv.visitInsn(ARRAYLENGTH);
		Label l2 = new Label();
		mv.visitJumpInsn(IFEQ, l2);
		
		for (ParamDec dec : params){
			dec.visit(this, null);
		}

		mv.visitLabel(l2);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		
		//add code to take the args in slot 0 and store it in slot 1 to assign to instance
		mv.visitVarInsn(ASTORE, 1);
		Label largs = new Label();
		mv.visitLabel(largs);
		
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		//used to track the slot use in local variable array
		//add this to slot 0
		slotInRun ++;
		
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		
		//visit local variables
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		for(Dec ld: localVariable){
			//ident name
			String dn = ld.getIdent().getText();
			//slot number
			int sn = ld.getSlot(); 
			//System.out.println("name: " + dn + " solt: " + sn);
			//label
			Label sl = ld.getStartLabel();
			Label el = ld.getEndLabel();
			if (sl == null)
				sl = startRun;
			if (el == null)
				el = endRun;
			//type
			String tn = null;
			TypeName tN = ld.getTypeName(); 
			switch (tN) {
			case INTEGER:{
				tn = "I";
			}
				break;
			
			case BOOLEAN:{
				tn = "Z";
			}
				break;
			
//			case IMAGE:{
//				tn = "";
//			}
//				break;
//			
//			case FRAME:{
//				tn = "";
//			}
//				break;
//			
//			case FILE:{
//				tn = "";
//			}
//				break;
//			
//			case URL:{
//				tn = "";
//			}
//				break;
				
			default:{
				//Object
				tn = "Ljava/lang/Object";
			}
				break;
			}
			
			mv.visitLocalVariable(dn, tn, null, sl, el, sn);
//			if(sl == null){
//				//mv.visitLocalVariable(ld.getIdent().getText(), , null, startRun, endRun, ld.getSlot());
//				mv.visitLocalVariable(dn, arg1, null, startRun, endRun, sn);
//			}else{
//				//mv.visitLocalVariable(, , null, localDec.getStartLabel(), localDec.getEndLabel(), localDec.getSlot());
//				mv.visitLocalVariable(dn, arg1, arg2, arg3, , sn);
//			}
		}
		
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
//		if(assignStatement.getVar().getDec().getSlot() == -2)
//			mv.visitVarInsn(ALOAD, 0);
	
		assignStatement.getE().visit(this, null);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, null);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		binaryExpression.getE0().visit(this, null);
		binaryExpression.getE1().visit(this, null);
		if (arg == null){
			Label l1 = new Label();
			Label l2 = new Label();
			Kind k = binaryExpression.getOp().kind;
			switch (k) {
			case PLUS: mv.visitInsn(IADD);		
				break;
			case MINUS: mv.visitInsn(ISUB);
				break;
			case TIMES: mv.visitInsn(IMUL);
				break; 
			case DIV: mv.visitInsn(IDIV);
				break;
			case AND: mv.visitInsn(IAND);
				break;
			case OR: mv.visitInsn(IOR);
				break;
			case MOD: mv.visitInsn(IREM);
				break;
			case LT: {
					mv.visitJumpInsn(IF_ICMPGE, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}
				break;
			case LE: {
				mv.visitJumpInsn(IF_ICMPGT, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
				break;
			case GT: {
				mv.visitJumpInsn(IF_ICMPLE, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
				break;
			case GE: {
				mv.visitJumpInsn(IF_ICMPLT, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
				break;
			case EQUAL: {
				mv.visitJumpInsn(IF_ICMPNE, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
				break;
			case NOTEQUAL: {
				mv.visitJumpInsn(IF_ICMPEQ, l1);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, l2);
				mv.visitLabel(l1);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(l2);
			}
				break; 
			default:
				assert false: "Undefine";
				break;
			}
		}else if((int)arg == 1){
				Kind k = binaryExpression.getOp().kind;
				switch (k) {
				case PLUS: mv.visitInsn(IADD);		
					break;
				case MINUS: mv.visitInsn(ISUB);
					break;
				case TIMES: mv.visitInsn(IMUL);
					break; 
				case DIV: mv.visitInsn(IDIV);
					break;
				case AND: mv.visitInsn(IAND);
					break;
				case OR: mv.visitInsn(IOR);
					break;
				case MOD: mv.visitInsn(IREM);
					break;
				default:
					assert false: "Undefine";
					break;
			}
		}
			
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		currentScope ++;		
		
		if(currentScope > 1){
			Label nls = new Label();
			mv.visitLabel(nls);
		
			for(Dec dec: block.getDecs()){
				dec.visit(this, arg);
				localVariable.add(dec);
				slotInRun ++;
			}
			
			for(Statement stat: block.getStatements()){
//				if(stat instanceof AssignmentStatement){
//					AssignmentStatement as = (AssignmentStatement) stat;
//					if(as.getVar().getDec() instanceof ParamDec){
//						mv.visitVarInsn(ALOAD, 0);
//					}
//				}
				stat.visit(this, arg);
			}
			
			Label nle = new Label();
			mv.visitLabel(nle);
			
			for(Dec dec: block.getDecs()){
				dec.setStartLabel(nls);
				dec.setEndLabel(nle);
			}	
			
		}else{
			for(Dec dec: block.getDecs()){
				dec.visit(this, arg);
				localVariable.add(dec);
				slotInRun ++;
			}
			
			for(Statement stat: block.getStatements()){
//				if(stat instanceof AssignmentStatement){
//					AssignmentStatement as = (AssignmentStatement) stat;
//					if(as.getVar().getDec() instanceof ParamDec){
//						mv.visitVarInsn(ALOAD, 0);
//					}
//				}
				stat.visit(this, arg);
			}
		}

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		if(booleanLitExpression.getValue()){
			mv.visitInsn(ICONST_1);
		}else{
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setSlot(slotInRun);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		int ieSlot = identExpression.getDec().getSlot();
		String varName = identExpression.getDec().getIdent().getText();
		TypeName tn = identExpression.getTypeName();
		String type = null;
		
		//CodeGenUtils.genPrint(DEVEL, mv, "slot: " + ieSlot);
		 
		if(tn.isType(TypeName.INTEGER)){
			type = "I";
		}else if(tn.isType(BOOLEAN)){
			type = "Z";
		}
		//when slot number is -2, the dec is paramDec
		if(identExpression.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, varName, type);
		}else{
			mv.visitVarInsn(ILOAD, ieSlot);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		int ivSlot = identX.getDec().getSlot();
		String varName = identX.getDec().getIdent().getText();
		TypeName tn = identX.getDec().getTypeName();
		String type = null;
		
		if(tn.isType(TypeName.INTEGER)){
			type = "I";
		}else if(tn.isType(BOOLEAN)){
			type = "Z";
		}
		//when slot number is -2, the dec is paramDec
		if(identX.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, varName, type);	
		}else{
			mv.visitVarInsn(ISTORE, ivSlot);
		}
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//Label l = (Label) ifStatement.getE().visit(this, null);
		ifStatement.getE().visit(this, 1);
		
		Label l = new Label();
		if(ifStatement.getE() instanceof BinaryExpression){
			BinaryExpression be = (BinaryExpression) ifStatement.getE();
			Kind k = be.getOp().kind;
			switch(k){	
			case LT: mv.visitJumpInsn(IF_ICMPGE, l);
				break;
			case LE: mv.visitJumpInsn(IF_ICMPGT, l);
				break;
			case GT: mv.visitJumpInsn(IF_ICMPLE, l);
				break;
			case GE: mv.visitJumpInsn(IF_ICMPLT, l);
				break;
			case EQUAL: mv.visitJumpInsn(IF_ICMPNE, l);
				break;
			case NOTEQUAL: mv.visitJumpInsn(IF_ICMPEQ, l);
				break;
			default:
				assert false: "Undefine";
				break;
			}
		}else if(ifStatement.getE() instanceof IdentExpression || 
				ifStatement.getE() instanceof BooleanLitExpression){
			mv.visitJumpInsn(IFEQ, l);
		}
		
		ifStatement.getB().visit(this, null);
		mv.visitLabel(l);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(intLitExpression.getIntLitVar());
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//For assignment 5, only needs to handle integers and booleans
		TypeName type = paramDec.getTypeName();		
	//	Label lb = new Label();
		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(paramDecCounter);
		mv.visitInsn(AALOAD);
		paramDecCounter ++;
		
		if(type.isType(TypeName.INTEGER)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);			
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}else if(type.isType(BOOLEAN)){
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}		
		
//		Label lb = new Label();
		//mv.visitLabel(lb);
		//all the paramDecs will be loaded from field so we set their slot number to -2 to indicate they are fields
		//paramDec.setSlot(-2);
		
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label l1 = new Label();
		mv.visitJumpInsn(GOTO, l1);
		Label l = new Label();
		mv.visitLabel(l);		
		whileStatement.getB().visit(this, null);		
		mv.visitLabel(l1);
		whileStatement.getE().visit(this, 1);
		if(whileStatement.getE() instanceof BinaryExpression){
			BinaryExpression be = (BinaryExpression) whileStatement.getE();
			Kind k = be.getOp().kind;
			switch(k){	
			case LT: mv.visitJumpInsn(IF_ICMPLT, l);
				break;
			case LE: mv.visitJumpInsn(IF_ICMPLE, l);
				break;
			case GT: mv.visitJumpInsn(IF_ICMPGT, l);
				break;
			case GE: mv.visitJumpInsn(IF_ICMPGE, l);
				break;
			case EQUAL: mv.visitJumpInsn(IF_ICMPEQ, l);
				break;
			case NOTEQUAL: mv.visitJumpInsn(IF_ICMPNE, l);
				break;
			default:
				assert false: "Undefine";
				break;
			}
		}else if(whileStatement.getE() instanceof IdentExpression || 
				whileStatement.getE() instanceof BooleanLitExpression){
			mv.visitJumpInsn(IFNE, l);
		}
		return null;
	}

}
