package mgplugin.erd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.insightech.er.ERDiagramActivator;
import org.insightech.er.db.sqltype.SqlType;
import org.insightech.er.editor.ERDiagramEditor;
import org.insightech.er.editor.controller.command.settings.ChangeSettingsCommand;
import org.insightech.er.editor.model.ERDiagram;
import org.insightech.er.editor.model.diagram_contents.element.node.table.ERTable;
import org.insightech.er.editor.model.diagram_contents.element.node.table.column.NormalColumn;
import org.insightech.er.editor.model.diagram_contents.not_element.dictionary.Word;
import org.insightech.er.editor.model.settings.Settings;
import org.insightech.er.editor.view.action.AbstractBaseAction;
import org.insightech.er.extention.IERDiagramActionFactory;

import mgplugin.Activator;
import mgplugin.generator.entity.TableValue;
import mgplugin.query.QueryExec;

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
public class ImportMetaDataActionFactory implements IERDiagramActionFactory {

    @Override
    public IAction createIAction(ERDiagramEditor arg0) {
        return new Action(arg0);
    }
    
    class Action extends AbstractBaseAction {
        
        public Action(ERDiagramEditor editor) {
            super(Action.class.getName(), "MG META동기화", editor);
        }
        
        @Override
        public void execute(Event event) throws Exception {
            
            try {
                ERDiagramActivator.showConfirmDialog("물리명을 기준으로 META와 동기화 하시겠습니까?");
                ERDiagram diagram = this.getDiagram();
                
                List<Word> wordList = diagram.getDiagramContents().getDictionary().getWordList();
                
                List<ERTable> erTableList = this.getDiagram().getDiagramContents().getContents().getTableSet().getList();
                Map<String, List<String>> columnTableMap = new HashMap<>();
                for (ERTable erTable : erTableList) {
                    for ( NormalColumn column : erTable.getNormalColumns() ) {
                        List<String> tableNameList = columnTableMap.get(column.getPhysicalName());
                        if (tableNameList == null) {
                            tableNameList = new ArrayList<String>();
                            tableNameList.add(erTable.getPhysicalName());
                        } else {
                            tableNameList.add(erTable.getPhysicalName());
                        }
                        
                        if ( "FRST_REG_ID"     .equals(column.getPhysicalName() )  // 최초등록ID
                          || "FRST_REG_DTTM"   .equals(column.getPhysicalName() )  // 최초등록일시
                          || "LAST_PROC_ID"    .equals(column.getPhysicalName() )  // 최종처리ID
                          || "LAST_PROC_DTTM"  .equals(column.getPhysicalName() )  // 최종처리일시
                          || "LAST_PROC_PGM_ID".equals(column.getPhysicalName() )  // 최종처리프로그램ID
                        ) {
                            column.setNotNull(true);
                        }
                        
                        columnTableMap.put(column.getPhysicalName(), tableNameList);
                    }
                }
                
                for (Word word : wordList) {
                    
                    TableValue tableValue = QueryExec.getTerms(word.getPhysicalName());
                    
                    if ( tableValue == null ) {
                        String tableName = String.join(", ", columnTableMap.get(word.getPhysicalName()));
                        Activator.console("[" + word.getPhysicalName() + ":" + word.getLogicalName() + "] 메타시스템에 없는 용어입니다. ERD 정보 유지합니다. - " + tableName);
                        continue;
                    }
                    
                    // 논리명 셋팅
                    word.setLogicalName(tableValue.COLUMN_DESCRIPTION.trim());
                    
                    // 타입셋팅
                    boolean isSetType = ImportMetaDataActionFactory.syncMetainfo(tableValue, word);
                    
                    if (isSetType == false) {
                        String tableName = String.join(", ", columnTableMap.get(word.getPhysicalName()));
                        Activator.console("[" + word.getPhysicalName() + ":" + word.getLogicalName() + "] 매핑타입 없습니다. ERD 타입 유지합니다. - " + tableValue.TYPE + " - " + tableName);
                    }
                }
                
                Settings settings = diagram.getDiagramContents().getSettings().clone();
                ChangeSettingsCommand command = new ChangeSettingsCommand(diagram, settings, true);
                
                execute(command);
                
            } catch (Exception e) {
                Activator.console(e);
                throw e;
            }
            
            Activator.getDefault().showConsole(Activator.MG_PLUGIN_CONSOLE);
        }
        
    }
    
    public static boolean syncMetainfo(TableValue table, Word word) {
        
        switch (table.TYPE.toLowerCase()) {
        case "decimal":
        case "numeric":
            if ( "0".equals(table.SCALE) ) {
                word.setType(SqlType.valueOf("SQLServer", "decimal(p)"), word.getTypeData(), "SQLServer");
                word.getTypeData().setLength (Integer.parseInt(table.LENGTH));
                word.getTypeData().setDecimal(null                          );
            } else {
                word.setType(SqlType.valueOf("SQLServer", "decimal(p,s)"), word.getTypeData(), "SQLServer");
                word.getTypeData().setLength (Integer.parseInt(table.LENGTH));
                word.getTypeData().setDecimal(Integer.parseInt(table.SCALE ));
            }
            return true;
        
        case "bigint":
            word.setType(SqlType.valueOf("SQLServer", "bigint"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            return true;

        case "int":
            word.setType(SqlType.valueOf("SQLServer", "int"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            return true;
            
            
        case "datetime":
            word.setType(SqlType.valueOf("SQLServer", "datetime"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            return true;
            
        case "char":
            word.setType(SqlType.valueOf("SQLServer", "char(n)"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (Integer.parseInt(table.LENGTH));
            word.getTypeData().setDecimal(null                          );
            return true;

        case "varchar":
            word.setType(SqlType.valueOf("SQLServer", "varchar(n)"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (Integer.parseInt(table.LENGTH));
            word.getTypeData().setDecimal(null                          );
            return true;
            
        default:
            break;
        }
        
        return false;
    }

/*
####SQLServer
bigint
binary
binary(n)
bit
char
char(n)
date
datetime
datetime2
datetime2(p)
datetimeoffset
decimal
decimal(p)
decimal(p,s)
float
float(p)
geometry
hierarchyid 
image
int
money
nchar
nchar(n)
ntext
numeric
numeric(p)
numeric(p,s)
nvarchar(max)
nvarchar(n)
real
rowversion 
smalldatetime
smallint
smallmoney
text
time
time(p)
tinyint
uniqueidentifier 
uniqueidentifier rowguidcol
varbinary(max)
varbinary(n)
varchar(max)
varchar(n)
xml
*/
}
