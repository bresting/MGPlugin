package layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class DBControl {
    public static void main(String[] args) {
        
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        init(shell);
        
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
        }

    private static void init(Composite parent) {
        
        Composite mainRow = new Composite(parent, SWT.NONE);
        mainRow.setLayout(new GridLayout(2, false));
        mainRow.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite row1 = new Composite(mainRow, SWT.NONE);
        row1.setLayout(new GridLayout(1, false));
        GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
        gridData.widthHint = 300;
        row1.setLayoutData(gridData);
        
        Composite row2 = new Composite(mainRow, SWT.NONE);
        row2.setLayout(new GridLayout(1, false));
        row2.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        final Text txtContent = new Text(row2, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        txtContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite row = new Composite(row1, SWT.NONE);
        row.setLayout(new GridLayout(2, false));
        row.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        new Label(row, SWT.PUSH).setText("URL");;
        final Text txtUrl = new Text(row, SWT.BORDER);
        txtUrl.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        new Label(row, SWT.PUSH).setText("ID");;
        final Text txtId = new Text(row, SWT.BORDER);
        txtId.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        new Label(row, SWT.PUSH).setText("PW");;
        final Text txtPw = new Text(row, SWT.BORDER);
        txtPw.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        final Button btnConn = new Button(row, SWT.PUSH);
        btnConn.setText("연결");
        btnConn.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));  // COLSPAN 사용
        
        final Label lblConn = new Label(row, SWT.PUSH);
        lblConn.setText("연결하세요...");
        lblConn.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));  // COLSPAN 사용
        
        new Label(row, SWT.PUSH).setText("검색어");;
        final Text txtSearch = new Text(row, SWT.BORDER);
        txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
        
        final Table table = new Table(row1, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        TableColumn col1 = new TableColumn(table, SWT.NONE);
        TableColumn col2 = new TableColumn(table, SWT.NONE);
        TableColumn col3 = new TableColumn(table, SWT.NONE);
        
        col1.setText("물리명");
        col2.setText("논리명");
        col3.setText("비고");
        
        col1.pack();
        col2.pack();
        col3.pack();
        col1.setResizable(true);
        col2.setResizable(true);
        col3.setResizable(true);
        
        col1.setWidth(100);
        col2.setWidth(100);
        col3.setWidth(100);
        
        final TableItem item1 = new TableItem(table, SWT.NONE);
        item1.setText(new String[] {"TB_BC001M", "사용자정보", "20200501"});
        
        final TableItem item2 = new TableItem(table, SWT.NONE);
        item2.setText(new String[] {"TB_BC002M", "공통기본", "20201021"});
        
        final TableItem item3 = new TableItem(table, SWT.NONE);
        item3.setText(new String[] {"TB_BC080D", "공통상세", "20201021"});
        
        btnConn.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                System.out.println("마우스 클릭");
            }
        });
        
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 13) {
                    System.out.println(txtSearch);
                }
            }
        });
        
        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TableItem tableItem[] = table.getSelection();
                if ( tableItem.length <= 0 ) {
                    return;
                }
                
                System.out.println(tableItem[0].getText(0));
            }
        });
    }
    
}
