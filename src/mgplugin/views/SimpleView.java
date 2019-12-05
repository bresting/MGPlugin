package mgplugin.views;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   ------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   ------------------------------------------------------------------
 * 2019.12.03   김도진         최초생성
 *
 * </pre>
 */
public class SimpleView extends ViewPart {

    public static final String ID = "mgplugin.views.SimpleView"; //$NON-NLS-1$
    public static Text text;

    public SimpleView() {
    }

    /**
     * Create contents of the view part.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FormLayout());
        
        text = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
        FormData fd_text = new FormData();
        fd_text.bottom = new FormAttachment(100, -10);
        fd_text.right = new FormAttachment(100, -10);
        fd_text.top = new FormAttachment(0, 10);
        fd_text.left = new FormAttachment(0, 10);
        text.setLayoutData(fd_text);

        createActions();
        initializeToolBar();
        initializeMenu();
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        // Create the actions
    }

    /**
     * Initialize the toolbar.
     */
    private void initializeToolBar() {
        @SuppressWarnings("unused")
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    }

    /**
     * Initialize the menu.
     */
    private void initializeMenu() {
        @SuppressWarnings("unused")
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
    }

    @Override
    public void setFocus() {
        // Set the focus
    }
}
