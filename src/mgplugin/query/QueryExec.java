package mgplugin.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import mgplugin.Activator;
import mgplugin.generator.entity.TableValue;

/**
 * <pre>
 * @programName : 프로그램명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.29   KIM_DO_JIN         최초생성
 *
 * </pre>
 */
public class QueryExec {
    
    public static TableValue getTerms(String phycsName) {
        
        TableValue tableValue = null;
        
        List<String> queryList = new ArrayList<>();
        queryList.add("SELECT A.TERMS_PHYCS_NAME                      ");
        queryList.add("     , A.TERMS_LOGIC_NAME                      ");
        queryList.add("     , B.DOMAIN_NAME                           ");
        queryList.add("     , B.DOMAIN_DATA_TYPE                      ");
        queryList.add("     , B.DOMAIN_DATA_SIZE                      ");
        queryList.add("     , B.DOMAIN_DATA_SCALE                     ");
        queryList.add("  FROM TERMS_DIC  A                            ");
        queryList.add("  JOIN DOMAIN_DIC B                            ");
        queryList.add("    ON A.DOMAIN_NAME      = B.DOMAIN_NAME      ");
        queryList.add(" WHERE A.TERMS_PHYCS_NAME = ':TERMS_PHYCS_NAME'");  // // 바인딩

        String query = String.join("\n", queryList);
        
        try (Statement stmt = Activator.getConnection().createStatement();) {
            
            String endNumber = String.valueOf(phycsName).trim();
            
            String srchColumn = String.valueOf(phycsName).trim();
            srchColumn        = srchColumn.replaceAll("\\d+$", "");  // 숫자로 끝나는 경우 제거
            
            endNumber = endNumber.replace(srchColumn, "");
            
            String exeQuery = query.replace(":TERMS_PHYCS_NAME", srchColumn);
            
            ResultSet rs = stmt.executeQuery(exeQuery);
            
            if (rs.next()) {
                
                tableValue = new TableValue();
                
                //tableValue.COLUMN_NAME        = srchColumn                       ;
                tableValue.COLUMN_DESCRIPTION = rs.getString("TERMS_LOGIC_NAME" ) + "" + endNumber;
                tableValue.TYPE               = rs.getString("DOMAIN_DATA_TYPE" );
                tableValue.LENGTH             = rs.getString("DOMAIN_DATA_SIZE" );
                tableValue.SCALE              = rs.getString("DOMAIN_DATA_SCALE");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Activator.console(e.toString());
        }
        
        return tableValue;
    }
}
