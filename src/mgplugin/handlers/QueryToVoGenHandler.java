package mgplugin.handlers;

import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import mgplugin.Activator;
import mgplugin.generator.SourceGenerator;
import mgplugin.generator.entity.SourceTemplate;
import mgplugin.generator.entity.XmlTagElement;
import mgplugin.views.DBIOGenView;

/**
 * <pre>
 * @programName : 프로그램명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.04   김도진         최초생성
 *
 * </pre>
 */
public class QueryToVoGenHandler extends AbstractHandler {
    
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        IEditorPart editorPart = Activator.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getActivePage()
                .getActiveEditor();
        
        if ( editorPart instanceof EditorPart ) {
            
            IFile file = (IFile) editorPart.getEditorInput().getAdapter(IFile.class);
            
            IEditorSite iEditorSite = editorPart.getEditorSite();
            
            if (iEditorSite != null) {
                
                ISelectionProvider selectionProvider = iEditorSite.getSelectionProvider();
                
                if (selectionProvider != null) {
                    ISelection iSelection = selectionProvider.getSelection();
                    
                    int offset = ((ITextSelection) iSelection).getOffset();
                    
                    SourceTemplate inSourceTemplate  = new SourceTemplate();
                    SourceTemplate outSourceTemplate = new SourceTemplate();
                    
                    try {
                        // 1. 현재옵셋 input, ouput 가져오기
                        XmlTagElement xmlTagElement = SourceGenerator.getTypeAtOffset(file.getRawLocation().toOSString(), offset);
                        
                        // 2. parameterType, resultType 결과맵 가져오기
                        Map<String, List<String>> typeFieldMap = SourceGenerator.getTypeFieldMap(file.getRawLocation().toOSString());
                        
                        SourceGenerator.getVoTemplate(xmlTagElement, typeFieldMap, inSourceTemplate, outSourceTemplate);

                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(Activator.DBIO_GEN_VIEW_ID);
                        
                        if ( DBIOGenView.textParameter != null ) {
                            DBIOGenView.textParameter.setText( inSourceTemplate.getSource ()  );
                        } else {
                            Activator.console("MG 기본 DBIO생성기를 활성화 해주세요.");
                        }
                        
                        if ( DBIOGenView.textResult != null ) {
                            DBIOGenView.textResult.setText   ( outSourceTemplate.getSource() );
                        } else {
                            Activator.console("MG 기본 DBIO생성기를 활성화 해주세요.");
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        Activator.console(e);
                        MessageDialog.openError(editorPart.getSite().getShell(), "에러", "처리중 에러가 발생했습니다. console 확인 하세요.");

                        Activator.getDefault().showConsole(Activator.MG_PLUGIN_CONSOLE);
                    }
                }
            }
        }
        
        return null;
    }
}
