package mgplugin.handlers;

import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.part.EditorPart;

import mgplugin.Activator;

/**
 * <pre>
 * @programName : 프로그래명
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
            
            XMLInputFactory factory      = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = null;
            try {
                streamReader = factory.createXMLStreamReader(new FileReader(file.getRawLocation().toOSString()));
            } catch (FileNotFoundException | XMLStreamException e) {
                Activator.console(e);
            }
            
            IEditorSite iEditorSite = editorPart.getEditorSite();
            
            if (iEditorSite != null) {
                
                ISelectionProvider selectionProvider = iEditorSite.getSelectionProvider();
                
                if (selectionProvider != null) {
                    ISelection iSelection = selectionProvider.getSelection();
                    
                    int offset = ((ITextSelection) iSelection).getOffset();
                    Activator.console("offset : " + offset + " - TODO 쿼리문 추출해야됨");
                }
                
            }
            
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (XMLStreamException e) {
                    Activator.console(e);
                }
            }
        }
        
        return null;
    }

}
