package mgplugin.generator.entity;

/**
 * <pre>
 * @programName : 프로그램명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.09   김도진         최초생성
 *
 * </pre>
 */
public class XmlTagElement {
    
    private String resultType    = "";
    private String parameterType = "";
    
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
}
