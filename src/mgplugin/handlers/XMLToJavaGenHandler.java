package mgplugin.handlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;

import mgplugin.Activator;
import mgplugin.generator.SourceGenerator;
import mgplugin.generator.entity.SourceTemplate;

public class XMLToJavaGenHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        /*
        // IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        
        IWorkbenchPart workbenchPart = window.getActivePage().getActivePart();
        IFile file = (IFile) workbenchPart.getSite().getPage().getActiveEditor().getEditorInput().getAdapter(IFile.class);
        if ( file == null ) {
            return null;
        }
        */
        
        IEditorPart editorPart = Activator.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getActivePage()
                .getActiveEditor();
        
        IFile file = (IFile) editorPart.getEditorInput().getAdapter(IFile.class);
        
        if ( file == null ) {
            return null;
        }
        
        SourceTemplate sourceTemplate = SourceGenerator.mapperToInterface(file.getRawLocation().toOSString());
        
        if (sourceTemplate.getPackageName().isEmpty()) {
            MessageDialog.openWarning(editorPart.getSite().getShell(), "확인", "mybatis mapper 형식의 XML이 아닙니다.");
            return false;
        }
        
        boolean result = MessageDialog.openQuestion(editorPart.getSite().getShell(), "XML -> Interface", sourceTemplate.getPackageName() + "." +sourceTemplate.getTypeName() +" 생성 하시겠습니까?\n\n파일 존재하는 경우 덮어쓰게 됩니다.");
        
        if ( result ) {
            
            String srcPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath();  // WORKSPACE PATH
            srcPath       += "\\" + Activator.getProperty("project.name"      );                                 // 프로젝트명
            srcPath       += "\\" + Activator.getProperty("project.sourcePath");
            srcPath       += "\\" + sourceTemplate.getPackageName().replace(".", "\\");
            
            // 디렉토리 없는 경우 생성
            File targetDir = new File(srcPath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
            // 대상파일
            File targetFile = new File(srcPath, sourceTemplate.getTypeName() + ".java");
            
            try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile.getPath()), "UTF8"))) {
                output.write(sourceTemplate.getSource());
                Activator.console(targetFile.getAbsolutePath() + " 생성...");
            } catch (Exception e1) {
                e1.printStackTrace();
                Activator.console(e1.toString());
            }
            
            MessageDialog.openInformation(editorPart.getSite().getShell(), "확인", "처리내용 console 확인 후 폴더 새로고침 하세요.");
        }

        return null;
    }
}
