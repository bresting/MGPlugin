package mgplugin.erd;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2020.06.22   KIM_DO_JIN         최초생성
 *
 * </pre>
 */
public class ExportDialog extends Dialog {
    private Text txtTisdb;
    private String dbValue = "";;
    public ExportDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        Label lblDatabase = new Label(container, SWT.NONE);
        lblDatabase.setText("스크립트 생성 Database");
        
        Button btnRadio_DB01 = new Button(container, SWT.RADIO);
        Button btnRadio_DB02 = new Button(container, SWT.RADIO);
        btnRadio_DB01.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnRadio_DB01.setSelection(true);
                btnRadio_DB02.setSelection(false);
                
                txtTisdb.setEditable(false);
                txtTisdb.setText("*");
            }
        });
        btnRadio_DB01.setSelection(true);
        btnRadio_DB01.setText("TIS_DB 기본 / 시노님");
        
        btnRadio_DB02.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                btnRadio_DB01.setSelection(false);
                btnRadio_DB02.setSelection(true);
                
                txtTisdb.setEditable(true);
                txtTisdb.setText("");
            }
        });
        btnRadio_DB02.setText("사용자입력");
        
        txtTisdb = new Text(container, SWT.BORDER);
        txtTisdb.setEditable(false);
        txtTisdb.setText("*");
        txtTisdb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        return container;
    }

    // overriding this methods allows you to set the
    // title of the custom dialog
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Selection dialog");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(450, 220);
    }
    
    @Override
    protected void okPressed() {
        dbValue = txtTisdb.getText();
        super.okPressed();
    }
    
    public String getDbValue() {
        return dbValue;
    }
    
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);

        ExportDialog dialog = new ExportDialog(shell);
        int resultCode = dialog.open();
        System.out.println(resultCode);
        
        if ( resultCode == Window.OK) {
            String resultValue =  dialog.getDbValue();
            System.out.println(resultValue);
        }
    }
}
