package mgplugin.erd.entity;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.30   KIM_DO_JIN         최초생성
 *
 * </pre>
 */
public class ColumnData {
    
    private String columnName           = "";
    private String dataType             = ""; // VARCHAR(10), NUMERIC(9,5)
    private String dataNull             = ""; // NOT NULL
    private String dataDefault          = "";
    private String columnComment        = "";
    private String pkYn                 = "N";
    private String autoIncrementSetting = "";

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }
    /**
     * @param columnName the columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }
    /**
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    /**
     * @return the dataNull
     */
    public String getDataNull() {
        return dataNull;
    }
    /**
     * @param dataNull the dataNull to set
     */
    public void setDataNull(String dataNull) {
        this.dataNull = dataNull;
    }
    /**
     * @return the dataDefault
     */
    public String getDataDefault() {
        return dataDefault;
    }
    /**
     * @param dataDefault the dataDefault to set
     */
    public void setDataDefault(String dataDefault) {
        this.dataDefault = dataDefault;
    }
    /**
     * @return the columnComment
     */
    public String getColumnComment() {
        return columnComment;
    }
    /**
     * @param columnComment the columnComment to set
     */
    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
    
    /**
     * @return the pkYn
     */
    public String getPkYn() {
        return pkYn;
    }
    /**
     * @param pkYn the pkYn to set
     */
    public void setPkYn(String pkYn) {
        this.pkYn = pkYn;
    }
    /**
     * @return the autoIncrementSetting
     */
    public String getAutoIncrementSetting() {
        return autoIncrementSetting;
    }
    /**
     * @param autoIncrementSetting the autoIncrementSetting to set
     */
    public void setAutoIncrementSetting(String autoIncrementSetting) {
        this.autoIncrementSetting = autoIncrementSetting;
    }
    
    
}
