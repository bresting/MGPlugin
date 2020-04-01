package mgplugin.erd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.insightech.er.ERDiagramActivator;
import org.insightech.er.editor.ERDiagramEditor;
import org.insightech.er.editor.controller.editpart.element.node.ERTableEditPart;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.diagram_contents.element.node.table.index.Index;
import org.insightech.er.editor.view.action.AbstractBaseAction;
import org.insightech.er.extention.IERDiagramActionFactory;

import mgplugin.Activator;
import mgplugin.erd.entity.ColumnData;
import mgplugin.erd.entity.TableData;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.29   KIM_DO_JIN         최초생성
 *
 * </pre>
 */
public class ExportTableDDLActionFactory implements IERDiagramActionFactory {

    @Override
    public IAction createIAction(ERDiagramEditor arg0) {
        return new Action(arg0);
    }
    
    class Action extends AbstractBaseAction {
        
        public Action(ERDiagramEditor editor) {
            super(Action.class.getName(), "MG 테이블DDL생성", editor);
        }
        
        @Override
        public void execute(Event event) throws Exception {
            
            MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
                    , "대상확인", null
                    , "DDL 대상 DB를 선택하세요.", MessageDialog.INFORMATION
                    , new String[] { "기본DB","MIG DB", "취소" }, 0);
            
            int databaseType = dialog.open();
            
            if ( databaseType == 2 ) {
                return;
            }
            
            Activator.getDefault().getConsole(Activator.MG_PLUGIN_CONSOLE).clearConsole();
            
            try {
                List<ERTable>   erTableList   = new ArrayList<>();
                List<TableData> tableDataList = new ArrayList<TableData>();
                
                // 선택 대상 추출
                IStructuredSelection structuredSelection = (IStructuredSelection) this.getEditorPart().getSite().getSelectionProvider().getSelection();
                List<?> selectionList = structuredSelection.toList();
                for (Object selection : selectionList) {
                    if (selection instanceof ERTableEditPart) {
                        ERTableEditPart erTableEditPart = (ERTableEditPart) selection;
                        if ( erTableEditPart.getModel() instanceof ERTable) {
                            erTableList.add((ERTable)erTableEditPart.getModel());
                        }
                    }
                }
                
                // 선택 대상 없는 경우 전체
                if ( erTableList.isEmpty() ) {
                    erTableList = this.getDiagram().getDiagramContents().getContents().getTableSet().getList();
                }
                
                boolean isError = false;
                for (ERTable erTable : erTableList) {
                    for (NormalColumn normalColumn : erTable.getNormalColumns()) {
                        if (normalColumn.getType() == null || normalColumn.getType().getId().isEmpty()) {
                            isError = true;
                            Activator.console("형식이 정의 되어 있지 않습니다. 테이블명 :" + erTable.getPhysicalName() +" 컬럼 :" + normalColumn.getPhysicalName() );
                        }
                    }
                }
                
                if (isError) {
                    ERDiagramActivator.showErrorDialog("오류가 있습니다. 콘솔 로그 확인하세요.");
                    Activator.getDefault().showConsole(Activator.MG_PLUGIN_CONSOLE);
                    return;
                }
                
                for (ERTable erTable : erTableList) {
                    TableData tableData = new TableData();
                    tableData.setTableName   (erTable.getPhysicalName());
                    tableData.setTableComment(erTable.getLogicalName ());
                    List<ColumnData> columnDataList = new ArrayList<ColumnData>();
                    for (NormalColumn normalColumn : erTable.getNormalColumns()) {
                        
                        String dataType = String.valueOf(normalColumn.getType());
                        
                        /**
                         * DATETIME   - TIMEZONE 없이 입력일시 저장
                         * TIMESTAMP  - TIMEZONE 포함 입력일시 저장
                         */
                        if ("TIMESTAMP".equalsIgnoreCase(dataType)) {
                            dataType = "DATETIME";
                        }
                        
                        /**
                         * 둘다 동일함, 그냥 문자열만 변경해서 출력한다.
                         * character(n) -> char(n)
                         * INTEGER      -> int
                         */
                        if ("character(n)".equalsIgnoreCase(dataType)) {
                            dataType = "CHAR(n)";
                        }
                        if ("INTEGER".equalsIgnoreCase(dataType)) {
                            dataType = "INT";
                        }
                        
                        if ( dataType.contains("(n)") || dataType.contains("(p)") ) {
                            dataType = dataType.substring(0, dataType.lastIndexOf("("));
                            dataType = dataType + "(" + normalColumn.getTypeData().getLength() + ")";
                        } else if ( dataType.contains("(p,s)") ) {
                            dataType = dataType.substring(0, dataType.lastIndexOf("("));
                            dataType = dataType + "(" + normalColumn.getTypeData().getLength() + ","+ normalColumn.getTypeData().getDecimal() +")";
                        }
                        
                        dataType = dataType.toUpperCase();
                        
                        ColumnData columnData = new ColumnData();
                        columnData.setColumnName   (normalColumn.getPhysicalName());
                        columnData.setColumnComment(normalColumn.getLogicalName ());
                        columnData.setDataType     (dataType);
                        
                        if (normalColumn.isAutoIncrement()) {
                            // identity(1,1)  시작, 증감
                            long start     = 1;
                            long increment = 1;
                            if (normalColumn.getAutoIncrementSetting().getStart() != null) {
                                start = normalColumn.getAutoIncrementSetting().getStart();
                            }
                            if (normalColumn.getAutoIncrementSetting().getIncrement() != null) {
                                increment = normalColumn.getAutoIncrementSetting().getIncrement();
                            }
                            String tmp = "IDENTITY(" + start + "," + increment + ")";
                            columnData.setAutoIncrementSetting(tmp);
                        }
                        if (normalColumn.isNotNull()) {
                            columnData.setDataNull("NOT NULL");
                        }
                        /*
                        if ( ! StringUtils.isEmpty( normalColumn.getDefaultValue() ) ) {
                            if ( dataType.contains("CHAR")
                              || dataType.contains("TEXT")
                            ) {
                                columnData.setDataDefault("'" + normalColumn.getDefaultValue() + "'" );
                            } else {
                                columnData.setDataDefault(normalColumn.getDefaultValue());
                            }
                        }
                        */
                        columnData.setDataDefault(normalColumn.getDefaultValue());
                        
                        if (normalColumn.isPrimaryKey()) {
                            columnData.setPkYn("Y");
                        }
                        
                        columnDataList.add(columnData);
                    }

                    /**
                     * 인덱스
                     */
                    List<String> indexIXList = new ArrayList<String>();
                    List<String> indexUKList = new ArrayList<String>();
                    for (Index index : erTable.getIndexes()) {
                        List<String> tmpColumnList = new ArrayList<>();
                        for (NormalColumn nmColumn : index.getColumns()) {
                            tmpColumnList.add(nmColumn.getPhysicalName());
                        }
                        
                        if ( index.isNonUnique() ) {
                            indexIXList.add( "CREATE INDEX "        + index.getName() + " ON " + index.getTable().getPhysicalName() + "(" + String.join(",", tmpColumnList ) + ");" );
                            
                        } else {
                            indexUKList.add( "CREATE UNIQUE INDEX " + index.getName() + " ON " + index.getTable().getPhysicalName() + "(" + String.join(",", tmpColumnList ) + ");" );
                        }
                    }
                    
                    tableData.setColumnDataList(columnDataList);
                    tableData.setIndexIXList(indexIXList);
                    tableData.setIndexUKList(indexUKList);
                    tableDataList.add(tableData);
                }
                
                Collections.sort(tableDataList);
                
                Map<String, Object> root = new HashMap<String, Object>();
                root.put("userName"     , Activator.getProperty("user.name"));
                root.put("tableDataList", tableDataList   );
                root.put("databaseType" , databaseType   );
                
                String tmpSource = Activator.getTemplateSource("erd_ddl.ftlh", root);
                tmpSource = tmpSource.replace("&#39;", "'");
                
                Activator.console(tmpSource);
                Activator.getDefault().showConsole(Activator.MG_PLUGIN_CONSOLE);
            } catch (Exception e) {
                Activator.console(e);
                throw e;
            }
        }
    }
}
