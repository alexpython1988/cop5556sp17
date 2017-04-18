package cop5556sp17;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.Scanner.Kind.BARARROW;

import java.security.AccessControlException;
import java.util.ArrayList;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
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
		
		//visit all the fields 
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params){
			TypeName tn = dec.getTypeName();
			String decIdent = dec.getIdent().getText();
			
			switch (tn) {
			case INTEGER:
				fv = cw.visitField(0, decIdent, "I", null, null);
				fv.visitEnd();
				break;
			
			case BOOLEAN:
				fv = cw.visitField(0, decIdent, "Z", null, null);
				fv.visitEnd();
				break;
			
			case FILE:
				fv = cw.visitField(0, decIdent,  "Ljava/io/File;", null, null);
				fv.visitEnd();
				break;
			
			case URL:
				fv = cw.visitField(0, decIdent, "Ljava/net/URL;", null, null);
				fv.visitEnd();
				break;

			default:
				assert false: "undefine";
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
			
			case IMAGE:{
				tn = "Ljava/awt/image/BufferedImage;";
			}
				break;
			
			case FRAME:{
				tn = "Lcop5556sp17/PLPRuntimeFrame;";
			}
				break;
		
			default:{
				assert false: "undefine";
			}
				break;
			}
			
			mv.visitLocalVariable(dn, tn, null, sl, el, sn);
		}
		
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		if(assignStatement.getE().getTypeName().isType(IMAGE)){
			assignStatement.getE().visit(this, 1);
			CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
			CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", PLPRuntimeImageOps.copyImageSig, false);
			assignStatement.getVar().visit(this, 1);				
		}else{
			assignStatement.getE().visit(this, null);
			CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
			CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
			assignStatement.getVar().visit(this, null);
		}

		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {	
		if(binaryChain.getE0() instanceof IdentChain){
			binaryChain.getE0().visit(this, 2);//chain element as identChain on left (indicated by 2)
		}else{
			binaryChain.getE0().visit(this, null);//chain
		}
		
//		TypeName tn0 = binaryChain.getE0().getTypeName();
//		String ident0 = binaryChain.getE0().getFirstToken().getText();
//		if(tn0.isType(URL)){
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, className, ident0, "Ljava/net/URL;");
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", "(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);
//		}else if(tn0.isType(FILE)){
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, className, ident0, "Ljava/io/File;");
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", "(Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
//		}
		
		Token op = binaryChain.getArrow();
		if(op.isKind(BARARROW)){
			binaryChain.getE1().visit(this, 3);//indicate bararrow
		}else{
			if(binaryChain.getE1() instanceof IdentChain){
				binaryChain.getE1().visit(this, 1);//chainElem and it is an identChain on right side(use 1 to indicate it is left)
			}else{
				binaryChain.getE1().visit(this, null);//chainElem and it is not identChain
			}
		}
			
		return null;
	}
	
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		int flag = 0;/*(int) arg;*/
		if(arg != null){
			flag = (int) arg; 
		}
		TypeName tn = identChain.getDec().getTypeName();
		String ident0 = identChain.getFirstToken().getText();
		if(flag == 1){
			switch (tn) {
			case INTEGER:{
				mv.visitInsn(DUP);
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, ident0, "I");
				}else{
					mv.visitVarInsn(ISTORE, identChain.getDec().getSlot());
				}
			}
				break;
			
			case IMAGE:{
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
			}
				break;
			
			case FILE:{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, ident0, "Ljava/io/File;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write", PLPRuntimeImageIO.writeImageDesc, false);
			}
				break;
			
			case FRAME:{
				mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlot());
			}	
				break;

			default:
				assert false: "undefine";
				break;
			}
		}else if(flag == 2){
			switch (tn) {
			case INTEGER:{
				if(identChain.getDec() instanceof ParamDec){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, ident0, "I");
				}else{
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlot());
				}
			}
				break;
			
			case IMAGE:{
				mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
			}
				break;
			//TODO check?????
			case FILE:{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, ident0, "Ljava/io/File;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);		
			}
				break;
			
			case FRAME:{
				mv.visitVarInsn(ALOAD, identChain.getDec().getSlot());
			}	
				break;
			
			case URL:{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, ident0, "Ljava/net/URL;");
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);			
			}	
				break;

			default:
				assert false: "undefine";
				break;
			}				
		}else{//remove else after finish the project (debug propose)
			throw new AccessControlException("identChain has a bug!");
		}
		
		return identChain.getDec().getSlot();
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Label l1 = new Label();
		Label l2 = new Label();
		if (arg == null){
			binaryExpression.getE0().visit(this, null);
			binaryExpression.getE1().visit(this, null);
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
				if(binaryExpression.getE0().getTypeName().isType(TypeName.INTEGER) ||
						binaryExpression.getE0().getTypeName().isType(BOOLEAN)){
					mv.visitJumpInsn(IF_ICMPNE, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}else{
					mv.visitJumpInsn(IF_ACMPNE, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}
			}
				break;
			case NOTEQUAL: {
				if(binaryExpression.getE0().getTypeName().isType(TypeName.INTEGER) ||
						binaryExpression.getE0().getTypeName().isType(BOOLEAN)){
					mv.visitJumpInsn(IF_ICMPEQ, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}else{
					mv.visitJumpInsn(IF_ACMPEQ, l1);
					mv.visitInsn(ICONST_1);
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitLabel(l2);
				}
			}
				break; 
			
			default:
				assert false: "Undefine";
				break;
			}	
		}else{
			int i = (int) arg;
			if(i == 1){
				binaryExpression.getE0().visit(this, 1);
				binaryExpression.getE1().visit(this, 1);
				if(binaryExpression.getE0().getTypeName().isType(IMAGE)){
					if(binaryExpression.getE1().getTypeName().isType(IMAGE)){
						Kind k = binaryExpression.getOp().kind;
						switch (k) {
						case PLUS: mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", PLPRuntimeImageOps.addSig, false);	
							break;
						
						case MINUS: mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", PLPRuntimeImageOps.subSig, false);
							break;
						
						case EQUAL:{
							mv.visitJumpInsn(IF_ACMPNE, l1);
							mv.visitInsn(ICONST_1);
							mv.visitJumpInsn(GOTO, l2);
							mv.visitLabel(l1);
							mv.visitInsn(ICONST_0);
							mv.visitLabel(l2);
						}
							break;
						
						case NOTEQUAL:{
							mv.visitJumpInsn(IF_ACMPEQ, l1);
							mv.visitInsn(ICONST_1);
							mv.visitJumpInsn(GOTO, l2);
							mv.visitLabel(l1);
							mv.visitInsn(ICONST_0);
							mv.visitLabel(l2);
						}
							break;
						
						default:
							assert false: "undefine";
							break;
						}
					}else if(binaryExpression.getE1().getTypeName().isType(TypeName.INTEGER)){
						Kind k = binaryExpression.getOp().kind;
						switch (k) {
						case TIMES: mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig, false);
							break;
						
						case DIV: mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", PLPRuntimeImageOps.divSig, false);
							break;
						
						case MOD: mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", PLPRuntimeImageOps.modSig, false);
							break;
							
						default:
							assert false: "undefine";
							break;
						}
					}else{
						throw new TypeCheckException("At pos: " + binaryExpression.getFirstToken().getLinePos()
								+ ". Illegal type met: expect Integer or Image " + "but get: " + binaryExpression.getE1().getTypeName());
					}
				}else{
					Token op = binaryExpression.getOp();
					if(op.isKind(Kind.TIMES)){
						mv.visitInsn(SWAP);
						mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig, false);
					}else{
						throw new TypeCheckException("At pos: " + binaryExpression.getOp().getLinePos()
								+ ". Illegal type met: expect TIMES " + "but get: " + binaryExpression.getOp().getText());
					}
				}
			}else{
				throw new TypeCheckException("Bug in assignmentstatement and bianryexpresssion. no other number than 1 should be passed");
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
				stat.visit(this, arg);
				if(stat instanceof BinaryChain){
					mv.visitInsn(POP);
				}
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
				stat.visit(this, arg);
				if(stat instanceof BinaryChain){
					mv.visitInsn(POP);
				}
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
		switch (constantExpression.getFirstToken().kind) {
		case KW_SCREENHEIGHT:{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}
			break;
		
		case KW_SCREENWIDTH:{
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
			break;
		
		default:
			assert false: "Must be kw_screenwidth or kw_screenheight";
			break;
		}
		
		return null;
	}
	
	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setSlot(slotInRun);
		
		if(declaration.getTypeName().isType(IMAGE, FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, slotInRun);
		}

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//NUll pointer exception!!!!
		Kind op = filterOpChain.getKind();
		filterOpChain.getArg().visit(this, null);
		
		int i = 0;
		if (arg != null){
			i = (int) arg;
		}
		
		if(i == 3 && filterOpChain.getFirstToken().isKind(Kind.OP_GRAY)){// indicate the op is bararrow
			mv.visitInsn(DUP);
//			System.out.println("!!!!!!!!!!!!!!!");
//			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", PLPRuntimeImageOps.copyImageSig, false);
//			mv.visitInsn(SWAP);
		}else{
			mv.visitInsn(ACONST_NULL);
		}
		
		switch (op) {
		case OP_BLUR:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", PLPRuntimeFilterOps.opSig, false);
			break;
		
		case OP_GRAY:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", PLPRuntimeFilterOps.opSig, false);
			break;
		
		case OP_CONVOLVE:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolve", PLPRuntimeFilterOps.opSig, false);
			break;

		default:
			assert false: "undefined";
			break;
		}
		
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Kind op = frameOpChain.getKind();
		frameOpChain.getArg().visit(this, null);
		
		switch (op) {
		case KW_SHOW:
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", PLPRuntimeFrame.showImageDesc, false);
			break;
		
		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", PLPRuntimeFrame.hideImageDesc, false);
			break;
		
		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", PLPRuntimeFrame.getXValDesc, false);
			break;
		
		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", PLPRuntimeFrame.getYValDesc, false);
			break;
		
		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
			break;

		default: assert false: "undefined";
			break;
		}
		
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		int ieSlot = identExpression.getDec().getSlot();
		String varName = identExpression.getDec().getIdent().getText();
		TypeName tn = identExpression.getTypeName();
		String type = null;
		
//		System.out.println("!!!!!" + arg);	
		if(arg == null){	 		
			switch (tn) {
			case INTEGER:
				type = "I";
				break;
			
			case BOOLEAN:
				type = "Z";
				break;	
			
			case IMAGE:
				type = "Ljava/awt/image/BufferedImage;";
				break;
			
			case FRAME:
				type = "Lcop5556sp17/PLPRuntimeFrame;";
				break;
			
			case URL:
				type = "Ljava/net/URL;";
				break;
			
			case FILE:
				type = "java/io/File";
				break;	
				
			default:
				break;
			}
			
			//when slot number is -2, the dec is paramDec
			if(identExpression.getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, varName, type);
			}else{
				if(tn.isType(FRAME))
					mv.visitVarInsn(ALOAD, ieSlot);
				else
					mv.visitVarInsn(ILOAD, ieSlot);
			}
		}else{
			int i = (int) arg;
			if(i == 1){
				mv.visitVarInsn(ALOAD, ieSlot);
			}else{
				throw new TypeCheckException("identExpression image error!");
			}
		}
		return null;
	}
	
	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		int ivSlot = identX.getDec().getSlot();
		String varName = identX.getDec().getIdent().getText();
		TypeName tn = identX.getDec().getTypeName();
		String type = null;
		
		if(arg == null){
			switch (tn) {
			case INTEGER:
				type = "I";
				break;
			
			case BOOLEAN:
				type = "Z";
				break;	
			
			case IMAGE:
				type = "Ljava/awt/image/BufferedImage;";
				break;
			
			case FRAME:
				type = "Lcop5556sp17/PLPRuntimeFrame;";
				break;
			
			case URL:
				type = "Ljava/net/URL;";
				break;
			
			case FILE:
				type = "java/io/File";
				break;	
				
			default:
				break;
			}
			
			//when slot number is -2, the dec is paramDec
			if(identX.getDec() instanceof ParamDec){
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, varName, type);	
			}else{
				if(tn.isType(FRAME))
					mv.visitVarInsn(ASTORE, ivSlot);
				else
					mv.visitVarInsn(ISTORE, ivSlot);
			}
		}else{
			int i = (int) arg;
			if(i == 1){
				mv.visitVarInsn(ASTORE, ivSlot);
			}else{
				throw new TypeCheckException("identValue image error!");
			}
		}
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//Label l = (Label) ifStatement.getE().visit(this, null);
		ifStatement.getE().visit(this, null);
		
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
//		if(ifStatement.getE() instanceof BinaryExpression){
//			BinaryExpression be = (BinaryExpression) ifStatement.getE();
//			if(be.getE0().getTypeName().isType(TypeName.INTEGER) && 
//					be.getE0().getTypeName().isType(TypeName.INTEGER)){
//				Kind k = be.getOp().kind;
//				switch(k){	
//				case LT: mv.visitJumpInsn(IF_ICMPGE, l);
//					break;
//				case LE: mv.visitJumpInsn(IF_ICMPGT, l);
//					break;
//				case GT: mv.visitJumpInsn(IF_ICMPLE, l);
//					break;
//				case GE: mv.visitJumpInsn(IF_ICMPLT, l);
//					break;
//				case EQUAL: mv.visitJumpInsn(IF_ICMPNE, l);
//					break;
//				case NOTEQUAL: mv.visitJumpInsn(IF_ICMPEQ, l);
//					break;
//				default:
//					assert false: "Undefine";
//					break;
//				}
//			}else{
//				Kind k = be.getOp().kind;
//				switch(k){	
//				case EQUAL: mv.visitJumpInsn(IF_ACMPNE, l);
//					break;
//				case NOTEQUAL: mv.visitJumpInsn(IF_ACMPEQ, l);
//					break;
//				default:
//					assert false: "Undefine";
//					break;
//				}
//			}
//		}else if(ifStatement.getE() instanceof BooleanLitExpression){
//			mv.visitJumpInsn(IFEQ, l);
//		}else if(ifStatement.getE() instanceof IdentExpression){
//			if(ifStatement.getE().getTypeName().isType(TypeName.INTEGER, BOOLEAN)){
//				mv.visitJumpInsn(IFEQ, l);
//			}else{
//				mv.visitJumpInsn(IFNULL, l);
//			}
//		}
		
		ifStatement.getB().visit(this, null);
		mv.visitLabel(l);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Kind op = imageOpChain.getKind();
		imageOpChain.getArg().visit(this, null);
		switch (op) {
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
			break;
		
		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
			break;
		
		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "scale", PLPRuntimeImageOps.scaleSig, false);
			break;

		default:
			assert false: "undefined";
			break;
		}
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
			
		switch (type) {
		case INTEGER:{
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDecCounter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);			
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
			break;
		
		case BOOLEAN:{
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDecCounter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
			break;
		
		case FILE:{
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDecCounter);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
		}
			break;
		
		case URL:{
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDecCounter);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
		}
			break;

		default:
			assert false: "undefine";
		}
		
		paramDecCounter ++;
//		if(type.isType(TypeName.INTEGER)){
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);			
//			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
//		}else if(type.isType(BOOLEAN)){
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
//			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
//		}else if(){
//			
//		}else if(){
//			
//		}
		
//		Label lb = new Label();
		//mv.visitLabel(lb);
		//all the paramDecs will be loaded from field so we set their slot number to -2 to indicate they are fields
		//paramDec.setSlot(-2);
		
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, null);		
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for(Expression ex: tuple.getExprList()){
			ex.visit(this, null);
		}
		
		//tuple.getExprList().forEach(ex -> ex.visit(this, null));
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
		whileStatement.getE().visit(this, null);
		mv.visitJumpInsn(IFNE, l);
		
//		if(whileStatement.getE() instanceof BinaryExpression){
//			BinaryExpression be = (BinaryExpression) whileStatement.getE();
//			if(be.getE0().getTypeName().isType(TypeName.INTEGER) && 
//					be.getE0().getTypeName().isType(TypeName.INTEGER)){
//				Kind k = be.getOp().kind;
//				switch(k){	
//				case LT: mv.visitJumpInsn(IF_ICMPLT, l);
//					break;
//				case LE: mv.visitJumpInsn(IF_ICMPLE, l);
//					break;
//				case GT: mv.visitJumpInsn(IF_ICMPGT, l);
//					break;
//				case GE: mv.visitJumpInsn(IF_ICMPGE, l);
//					break;
//				case EQUAL: mv.visitJumpInsn(IF_ICMPEQ, l);
//					break;
//				case NOTEQUAL: mv.visitJumpInsn(IF_ICMPNE, l);
//					break;
//				default:
//					assert false: "Undefine";
//					break;
//				}
//			}else{
//				Kind k = be.getOp().kind;
//				switch(k){	
//				case EQUAL: mv.visitJumpInsn(IF_ACMPEQ, l);
//					break;
//				case NOTEQUAL: mv.visitJumpInsn(IF_ACMPNE, l);
//					break;
//				default:
//					assert false: "Undefine";
//					break;
//				}
//			}
//		}else if(whileStatement.getE() instanceof BooleanLitExpression){
//			mv.visitJumpInsn(IFNE, l);
//		}else if(whileStatement.getE() instanceof IdentExpression){
//			if(whileStatement.getE().getTypeName().isType(TypeName.INTEGER, BOOLEAN)){
//				mv.visitJumpInsn(IFNE, l);
//			}else{
//				mv.visitJumpInsn(IFNONNULL, l);
//			}
//		}
		return null;
	}

}