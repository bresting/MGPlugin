package mgplugin.erd.entity;

import java.util.ArrayList;
import java.util.List;

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
public class TableData implements Comparable<TableData>{
    
    private String tableName                = "";
    private String tableComment             = "";
    private List<ColumnData> columnDataList = null;
    
    private List<String> indexIXList = new ArrayList<String>();
    private List<String> indexUKList = new ArrayList<String>();
    
    private ArrayList<String> sequenceList = new ArrayList<String>();
    
    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }
    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
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
     * @return the columnDataList
     */
    public List<ColumnData> getColumnDataList() {
        return columnDataList;
    }
    /**
     * @param columnDataList the columnDataList to set
     */
    public void setColumnDataList(List<ColumnData> columnDataList) {
        this.columnDataList = columnDataList;
    }
    
    /**
     * @return the indexIXList
     */
    public List<String> getIndexIXList() {
        return indexIXList;
    }
    /**
     * @param indexIXList the indexIXList to set
     */
    public void setIndexIXList(List<String> indexIXList) {
        this.indexIXList = indexIXList;
    }
    /**
     * @return the indexUKList
     */
    public List<String> getIndexUKList() {
        return indexUKList;
    }
    /**
     * @param indexUKList the indexUKList to set
     */
    public void setIndexUKList(List<String> indexUKList) {
        this.indexUKList = indexUKList;
    }
    
    /**
     * @return the sequenceList
     */
    public ArrayList<String> getSequenceList() {
        return sequenceList;
    }
    /**
     * @param sequenceList the sequenceList to set
     */
    public void setSequenceList(ArrayList<String> sequenceList) {
        this.sequenceList = sequenceList;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TableData o) {
        
        if (this.tableName.compareTo(o.tableName) > 0 ) {
            return 1;
        } else if (this.tableName.compareTo(o.tableName) < 0 ) {
            return -1;
        } else {
            return 0;
        }
    }
    
}
