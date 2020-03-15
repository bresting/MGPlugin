package mgplugin;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2020.01.07   KIM_DO_JIN         최초생성
 *
 * </pre>
 */
public class TestMain {
   
    public static String readFile(String path, Charset encoding) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        } catch (IOException e) {
            throw new RuntimeException("파일 위치 확인!!!");
        }
    }
    
    public static void main(String[] args) throws ClassNotFoundException {
        
        
//        ASTParser parser = ASTParser.newParser(AST.JLS10);
//        
//        parser.setSource(content.toCharArray());
//        parser.setKind(ASTParser.K_COMPILATION_UNIT);
// 
//        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
// 
//        cu.accept(new ASTVisitor() {
// 
//            Set names = new HashSet();
// 
//            public boolean visit(VariableDeclarationFragment node) {
//                SimpleName name = node.getName();
//                this.names.add(name.getIdentifier());
//                
//                if(node.getParent() instanceof FieldDeclaration){
//                    FieldDeclaration declaration = ((FieldDeclaration) node.getParent());
//                    if(declaration.getType().isSimpleType()){
//                        //typeSimpleName = declaration.getType().toString();
//                        
//                        System.out.println(declaration.getType().toString() + " : " + name);
//                    }
//                }
//                
//                //System.out.println(name + ":" + node.);
//                //System.out.println("Declaration of '"+name+"' at line"+cu.getLineNumber(name.getStartPosition()));
//                return false; // do not continue to avoid usage info
//            }
//        });
        
        // org.eclipse.ui.editors.text.TextEditor
        
        //org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.DEFAULT_EDITOR_CONTEXT_MENU_ID
        
        
        // org.eclipse.wst.sse.ui.StructuredTextEditor
    }
    
    
    public static void setL(Long l) {
        System.out.println(l);
    }
    
    public static void setL2(long l) {
        System.out.println(l);
    }
    
    
}
