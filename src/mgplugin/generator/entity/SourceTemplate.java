package mgplugin.generator.entity;

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
public class SourceTemplate {

    // 묶음 처리
    private String packageName  = "";
    private String typeName     = "";
    private String baseTypeName = "";
    private String tableComment = "";
    private String source       = "";
    
    /**
     * @return the baseTypeName
     */
    public String getBaseTypeName() {
        return baseTypeName;
    }
    /**
     * @param baseTypeName the baseTypeName to set
     */
    public void setBaseTypeName(String baseTypeName) {
        this.baseTypeName = baseTypeName;
    }
    /**
     * @return the packageName
     */
    public String getPackageName() {
        return packageName;
    }
    /**
     * @param packageName the packageName to set
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    /**
     * @return the typeName
     */
    public String getTypeName() {
        return typeName;
    }
    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    /**
     * @return the tableComment
     */
    public String getTableComment() {
        return tableComment;
    }
    /**
     * @param tableComment the tableComment to set
     */
    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }
    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }
}
