package mgplugin.views;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import mgplugin.Activator;
import mgplugin.generator.SourceGenerator;
import mgplugin.generator.entity.SourceTemplate;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   ------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   ------------------------------------------------------------------
 * 2019.12.02   김도진         최초생성
 *
 * </pre>
 */
public class DBIOGenView extends ViewPart {

    public static final String ID = "mgplugin.views.DBIOGenView"; //$NON-NLS-1$
    private Text textDBIO;
    private Label lblConnStatus;
    
    public DBIOGenView() {
    }

    /**
     * Create contents of the view part.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        
        parent.setLayout(new FormLayout());
        textDBIO = new Text(parent, SWT.BORDER | SWT.MULTI);
        FormData fd_textDBIO = new FormData();
        fd_textDBIO.left = new FormAttachment(0, 10);
        fd_textDBIO.right = new FormAttachment(100, -10);
        textDBIO.setLayoutData(fd_textDBIO);
        
        Button btnCreateDBIO = new Button(parent, SWT.NONE);
        fd_textDBIO.bottom = new FormAttachment(btnCreateDBIO, -6);
        btnCreateDBIO.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                
                if ( Activator.getConnection() == null) {
                    MessageDialog.openWarning(parent.getShell(), "확인", "DB 연결정보가 없습니다.");
                    return;
                }
                
                // 테이블 목록
                String value = textDBIO.getText().trim();
                if (StringUtils.isEmpty(value)) {
                    MessageDialog.openInformation(parent.getShell(), "확인", "대상이 없습니다.");
                    return;
                }
                
                List<String> tableList = Arrays.asList(value.split("\n"));
                int tableCnt = tableList.size();
                boolean result = MessageDialog.openQuestion(parent.getShell(),"기본DBIO 생성", tableCnt+ "건 생성[tis.dbio..테이블] 하시겠습니까?\n\n파일 존재하는 경우 덮어쓰게 됩니다.");
                if (result) {
                    
                    List<SourceTemplate> resultVoList     = new ArrayList<>();
                    List<SourceTemplate> resultMapperList = new ArrayList<>();
                    
                    // 생성
                    SourceGenerator.createDefaultDBIO(tableList, resultVoList, resultMapperList);
                    
                    /**
                     * DBIO VO.java 생성
                     */
                    for (SourceTemplate sourceTemplate : resultVoList) {
                        String srcPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath();  // WORKSPACE PATH
                        srcPath       += "\\" + Activator.getProperty("project.name"      );                               // 프로젝트명
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
                            Activator.console(targetFile.getAbsolutePath() + "생성...");
                        } catch (Exception e1) {
                            Activator.console(e1.toString());
                            e1.printStackTrace();
                        }
                    }
                    
                    /**
                     * Mapper.xml & Mapper.java 생성
                     */
                    for (SourceTemplate sourceTemplate : resultMapperList) {
                        // tis.xxx.xxx -> mapper.xxx.xxx
                        String mapperPath = sourceTemplate.getPackageName().replace(Activator.getProperty("project.rootPackage") + ".", "mapper.").replace(".", "\\");
                        
                        String srcPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath();  // WORKSPACE PATH
                        srcPath       += "\\" + Activator.getProperty("project.name"        );                               // 프로젝트명
                        srcPath       += "\\" + Activator.getProperty("project.resourcePath");
                        srcPath       += "\\" + mapperPath;
                        
                        // 디렉토리 없는 경우 생성
                        File targetDir = new File(srcPath);
                        if (!targetDir.exists()) {
                            targetDir.mkdirs();
                        }
                        
                        // 대상파일
                        File targetFile = new File(srcPath, sourceTemplate.getTypeName() + "Mapper.xml");
                        
                        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile.getPath()), "UTF8"))) {
                            output.write(sourceTemplate.getSource());
                            
                            Activator.console(targetFile.getAbsolutePath() + "생성...");
                        } catch (Exception e1) {
                            Activator.console(e1.toString());
                            e1.printStackTrace();
                        }
                        
                        /**
                         * Mapper.xml 읽어서 Mapper.java 생성
                         */
                        SourceTemplate interfaceSourceTemplate = SourceGenerator.mapperToInterface(targetFile.getAbsolutePath());
                        srcPath  = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getAbsolutePath();  // WORKSPACE PATH
                        srcPath += "\\" + Activator.getProperty("project.name"      );                                 // 프로젝트명
                        srcPath += "\\" + Activator.getProperty("project.sourcePath");
                        srcPath += "\\" + sourceTemplate.getPackageName().replace(".", "\\");
                        
                        // 디렉토리 없는 경우 생성
                        targetDir = new File(srcPath);
                        if (!targetDir.exists()) {
                            targetDir.mkdirs();
                        }
                        
                        // 대상파일
                        targetFile = new File(srcPath, interfaceSourceTemplate.getTypeName() + ".java");
                        
                        try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile.getPath()), "UTF8"))) {
                            output.write(interfaceSourceTemplate.getSource());
                            Activator.console(targetFile.getAbsolutePath() + "생성...");
                        } catch (Exception e1) {
                            Activator.console(e1.toString());
                            e1.printStackTrace();
                        }
                    }
                    
                    MessageDialog.openInformation(parent.getShell(), "확인", "처리내용 console 확인 후 폴더 새로고침 하세요.");
                }
            }
        });
        
        FormData fd_btnCreateDBIO = new FormData();
        fd_btnCreateDBIO.bottom = new FormAttachment(100, -10);
        fd_btnCreateDBIO.left = new FormAttachment(0, 10);
        fd_btnCreateDBIO.top = new FormAttachment(100, -35);
        btnCreateDBIO.setLayoutData(fd_btnCreateDBIO);
        btnCreateDBIO.setText("기본 DBIO생성");
        
        lblConnStatus = new Label(parent, SWT.NONE);
        fd_textDBIO.top = new FormAttachment(lblConnStatus, 6);
        FormData fd_lblConnStatus = new FormData();
        fd_lblConnStatus.left = new FormAttachment(0, 10);
        fd_lblConnStatus.top = new FormAttachment(0, 10);
        lblConnStatus.setLayoutData(fd_lblConnStatus);
        lblConnStatus.setText("DB...");
        
        createActions();
        initializeToolBar();
        initializeMenu();
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        
        if ( Activator.getConnection()== null) {
            lblConnStatus.setText("DB 접속실패");
        } else {
            lblConnStatus.setText("DB " + Activator.getConnection().toString());
        }
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
    
    public static void main(String[] args) throws Exception {
        
//        try {
//            Activator.CONNECTION = DriverManager.getConnection("jdbc:sqlserver://192.168.0.103:1433;databaseName=ERPFRM;user=sa;password=wjdtnsdyd~1");
//        }
//        catch (SQLException e) {
//            System.err.println("DB Connection error...");
//            e.printStackTrace();
//        }
        
        Activator.initThisPlugin("C:\\eclipse_rcp\\runtime-EclipseApplication\\.metadata\\mgplugin");
        
        List<String> tableList = new ArrayList<>();
        tableList.add("TB_XX002M");
        
        List<SourceTemplate> resultVoList        = new ArrayList<>();
        List<SourceTemplate> resultMapperList    = new ArrayList<>();
        
        SourceGenerator.createDefaultDBIO(tableList, resultVoList, resultMapperList);
        
        System.out.println(resultVoList.get(0).getSource());
        System.out.println("==================================");
        System.out.println("==================================");
        //System.out.println(resultMapperList.get(0).getSource());
        
        Activator.closeThisPlugin();
        
        
    }
}
