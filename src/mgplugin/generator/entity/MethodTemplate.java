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
public class MethodTemplate {
    
    private String sqlCommandType;
    private String returnType;
    private String parameterType;
    private String parameterName;
    private String methodName;
    private String comment;
    
    
    /**
     * @return the sqlCommandType
     */
    public String getSqlCommandType() {
        return sqlCommandType;
    }
    /**
     * @param sqlCommandType the sqlCommandType to set
     */
    public void setSqlCommandType(String sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }
    /**
     * @return the returnType
     */
    public String getReturnType() {
        return returnType;
    }
    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }
    /**
     * @return the parameterType
     */
    public String getParameterType() {
        return parameterType;
    }
    /**
     * @param parameterType the parameterType to set
     */
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
    /**
     * @return the parameterName
     */
    public String getParameterName() {
        return parameterName;
    }
    /**
     * @param parameterName the parameterName to set
     */
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }
    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }
    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    
    
}
