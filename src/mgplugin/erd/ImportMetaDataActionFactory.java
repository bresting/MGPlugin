package mgplugin.erd;

import java.text.Format;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
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

    public static final Format DATE_FORMAT = FastDateFormat.getInstance( "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            
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
                if ( ERDiagramActivator.showConfirmDialog("물리명을 기준으로 META와 동기화 하시겠습니까?") == false ) {
                    return;
                }
                
                ERDiagram diagram = this.getDiagram();
                
                // 콘솔 클리어
                Activator.console("\n\n물리명을 기준 동기화 합니다... - " + DATE_FORMAT.format(new Date()) );
                
                List<ERTable> erTableList = this.getDiagram().getDiagramContents().getContents().getTableSet().getList();
                
                Map<String, TableValue> tmpDate = new HashMap<>();
                
                for (ERTable erTable : erTableList) {
                    
                    for ( NormalColumn column : erTable.getNormalColumns() ) {
                        
                        // 기본 5대 컬럼 기본 nullable
                        if ( "FRST_REG_ID"     .equals(column.getPhysicalName() )  // 최초등록ID
                          || "FRST_REG_DTTM"   .equals(column.getPhysicalName() )  // 최초등록일시
                          || "LAST_PROC_ID"    .equals(column.getPhysicalName() )  // 최종처리ID
                          || "LAST_PROC_DTTM"  .equals(column.getPhysicalName() )  // 최종처리일시
                          || "LAST_PROC_PGM_ID".equals(column.getPhysicalName() )  // 최종처리프로그램ID
                        ) {
                            column.setNotNull(false);
                        }
                        
                        // TODO
                        // 필드_CHAR    NOT NULL DEFAULT " "
                        // 필드_DECIMAL NOT NULL DEFAULT 0
                        
                        
                        //columnTableMap.put(column.getPhysicalName(), tableNameList);
                        
                        // 외래키 연결시 null, 테이블에 단어없음 연결 부모값 변경으로 같이 공유한다.
                        Word word = column.getWord();
                        if (word == null) {
                            continue;
                        }
                        
                        
                        TableValue tableValue = tmpDate.get(word.getPhysicalName());
                        
                        if ( tableValue == null ) {
                            tableValue = QueryExec.getTerms(word.getPhysicalName());
                            tmpDate.put(word.getPhysicalName(), tableValue);
                        }
                        //TableValue tableValue = QueryExec.getTerms(word.getPhysicalName());
                        
                        if ( tableValue == null ) {
                            Activator.console("[" + word.getPhysicalName() + ":" + word.getLogicalName() + "] 메타시스템에 없는 용어입니다. ERD 정보 유지합니다. - " + erTable.getPhysicalName());
                            continue;
                        }
                        
                        // 논리명 셋팅
                        word.setLogicalName(tableValue.COLUMN_DESCRIPTION.trim());
                        
                        // 타입셋팅
                        boolean isSetType = ImportMetaDataActionFactory.syncMetainfo(tableValue, word, erTable.getPhysicalName());
                        
                        if (isSetType == false) {
                            //String tableName = String.join(", ", columnTableMap.get(word.getPhysicalName()));
                            Activator.console("[" + word.getPhysicalName() + ":" + word.getLogicalName() + "] 매핑타입 없습니다. ERD 타입 유지합니다. - " + tableValue.TYPE + " - " + erTable.getPhysicalName());
                        }
                    }
                }
                
                //diagram.getDiagramContents().getContents().getTableSet().getList()
                
                Settings settings = diagram.getDiagramContents().getSettings().clone();
                ChangeSettingsCommand command = new ChangeSettingsCommand(diagram, settings, true);
                execute(command);
                
                
            } catch (Exception e) {
                Activator.console(e);
                throw e;
            }
            
            Activator.getDefault().showConsole(Activator.MG_PLUGIN_CONSOLE);
            
            ERDiagramActivator.showMessageDialog("완료 되었습니다.");
        }
    }

    public static boolean syncMetainfo(TableValue table, Word word, String tableName) {
        
        boolean isChanged = false;
        
        Word org = new Word(word);
        
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
            
            isChanged = true;
            break;
        
        case "bigint":
            word.setType(SqlType.valueOf("SQLServer", "bigint"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            
            isChanged = true;
            break;

        case "int":
            word.setType(SqlType.valueOf("SQLServer", "int"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            
            isChanged = true;
            break;
            
        case "datetime":
            word.setType(SqlType.valueOf("SQLServer", "datetime"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            
            isChanged = true;
            break;
            
        case "char":
            word.setType(SqlType.valueOf("SQLServer", "char(n)"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (Integer.parseInt(table.LENGTH));
            word.getTypeData().setDecimal(null                          );
            
            isChanged = true;
            break;

        case "varchar":
            word.setType(SqlType.valueOf("SQLServer", "varchar(n)"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (Integer.parseInt(table.LENGTH));
            word.getTypeData().setDecimal(null                          );
            
            isChanged = true;
            break;
            
        case "text":
            word.setType(SqlType.valueOf("SQLServer", "text"), word.getTypeData(), "SQLServer");
            word.getTypeData().setLength (null);
            word.getTypeData().setDecimal(null);
            
            isChanged = true;
            break;
        default:
            break;
        }
        
        
        String org_typ = String.valueOf(org.getType());
        String org_len = String.valueOf(org.getTypeData().getLength ());
        String org_dec = String.valueOf(org.getTypeData().getDecimal());
        
        String chg_tye = String.valueOf(word.getType());
        String chg_len = String.valueOf(word.getTypeData().getLength ());
        String chg_dec = String.valueOf(word.getTypeData().getDecimal());

        if (org_typ.equals("null")) {
            org_typ = "";
        }
        if (org_len.equals("null")) {
            org_len = "";
        }
        if (org_dec.equals("null")) {
            org_dec = "";
        }
        
        // 
        if (chg_tye.equals("null")) {
            chg_tye = "";
        }
        if (chg_len.equals("null")) {
            chg_len = "";
        }
        if (chg_dec.equals("null")) {
            chg_dec = "";
        }
        
        if ( org_typ.equals(chg_tye)
          && org_len.equals(chg_len)
          && org_dec.equals(chg_dec)
        ) {
            // 같음
        } else {
            
            if ("TIMESTAMP".equalsIgnoreCase(org_typ)) {
                org_typ = "DATETIME";
            }
            if ("character(n)".equalsIgnoreCase(org_typ)) {
                org_typ = "CHAR(n)";
            }
            if ("INTEGER".equalsIgnoreCase(org_typ)) {
                org_typ = "INT";
            }
            
            // 
            if ("TIMESTAMP".equalsIgnoreCase(chg_tye)) {
                chg_tye = "DATETIME";
            }
            if ("character(n)".equalsIgnoreCase(chg_tye)) {
                chg_tye = "CHAR(n)";
            }
            if ("INTEGER".equalsIgnoreCase(chg_tye)) {
                chg_tye = "INT";
            }
            
            Activator.console("변경: " + word.getPhysicalName() + "." + word.getName() + "[" + org_typ +" / "+ org_len + " / " + org_dec + " => " + chg_tye +" / "+ chg_len + " / " + chg_dec + "] - " + tableName);
        }
        
        return isChanged;
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
