package mgplugin.generator.entity;

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
public class TableValue {
    
    public String COLUMN_IDX         = "";
    public String TABLE_NAME         = "";
    public String TABLE_DESCRIPTION  = "";
    public String COLUMN_NAME        = "";
    public String COLUMN_DESCRIPTION = "";
    public String TYPE               = "";
    public String LENGTH             = "";
    public String SCALE              = "";
    public String IS_NULLABLE        = "";
    public String COLLATION_NAME     = "";
    public String PRIMARYKEY_YN      = "N";
    public String IDENTITY_YN        = "N";
    
    public String JAVA_TYPE          = "";
    public String JAVA_NAME          = "";
}
