package mgplugin.generator.entity;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.09   김도진         최초생성
 *
 * </pre>
 */
public class XMLQuery {
    
    private String fileName      = "";
    private String query         = "";
    private String queryId       = "";
    private String resultType    = "";
    private String parameterType = "";
    

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }
    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }
    /**
     * @return the queryId
     */
    public String getQueryId() {
        return queryId;
    }
    /**
     * @param queryId the queryId to set
     */
    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }
    /**
     * @return the resultType
     */
    public String getResultType() {
        return resultType;
    }
    /**
     * @param resultType the resultType to set
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
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
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "XMLQuery [query=" + query + ", queryId=" + queryId + ", resultType=" + resultType + ", parameterType="
                + parameterType + "]";
    }
    
    
    
}
