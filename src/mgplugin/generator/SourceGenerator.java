package mgplugin.generator;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

import mgplugin.Activator;
import mgplugin.generator.entity.FieldTemplate;
import mgplugin.generator.entity.MethodTemplate;
import mgplugin.generator.entity.SourceTemplate;
import mgplugin.generator.entity.TableValue;

/**
 * <pre>
 * @programName : 프로그래명
 * @description : 프로그램_처리내용
 * @history
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 수정일       수정자            수정내용
 * ----------   ---------------   --------------------------------------------------------------------------------------
 * 2019.12.02   김도진            최초생성
 *
 * </pre>
 */
public class SourceGenerator {

    // private static final SimpleDateFormat format1 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    
    public static SourceTemplate mapperToInterface(String filePath) {
        
        XMLInputFactory      factory            = XMLInputFactory.newInstance();
        SourceTemplate       sourceTemplate     = new SourceTemplate();
        List<MethodTemplate> methodTemplateList = new ArrayList<>();
        
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(
                    new InputStreamReader( new FileInputStream( filePath ), Charset.forName( "UTF8" ) )
            );
            
            String namespace   = "";
            String nodeComment = "";
            
            while (streamReader.hasNext()) {
                int eventType = streamReader.next();
                
                if ( eventType == XMLStreamReader.COMMENT ) {
                    nodeComment = streamReader.getText().trim();
                }
                
                if ( eventType == XMLStreamReader.START_ELEMENT ) {
                    
                    // 패키지
                    if ( streamReader.getName().toString().equals("mapper") ){
                        namespace = getAttr(streamReader, "namespace");
                    }
                    
                    // 선언문
                    if ( streamReader.getName().toString().equals("select")
                      || streamReader.getName().toString().equals("insert")
                      || streamReader.getName().toString().equals("update")
                      || streamReader.getName().toString().equals("delete")
                    ) {
                        
                        String resultType    = getAttr(streamReader, "resultType");
                        String id            = getAttr(streamReader, "id");
                        String parameterType = getAttr(streamReader, "parameterType");
                        
                        if ( streamReader.getName().toString().equals("insert")
                          || streamReader.getName().toString().equals("update")
                          || streamReader.getName().toString().equals("delete")
                        ) {
                            resultType = "int";
                        }
                        
                        if (resultType.isEmpty()) {
                            resultType = "void";
                        }
                        
                        String parameterName = "";
                        if ( ! parameterType.isEmpty() ) {
                            parameterName = "value";
                            if (parameterType.startsWith(Activator.getProperty("project.rootPackage"))) {
                                parameterName = "in" + parameterType.substring(parameterType.lastIndexOf(".") + 1);
                            }
                        }
                        
                        // import 구문 사용시 apache.StringUtil / spring.StringUtil 차이를 알 수 없음
                        // 타입은 전체 구분으로 함
                        MethodTemplate template = new MethodTemplate();
                        template.setComment      (nodeComment  );
                        template.setMethodName   (id           );
                        template.setReturnType   (resultType   );
                        template.setParameterType(parameterType);
                        template.setParameterName(parameterName);
                        
                        methodTemplateList.add(template);
                        
                        // 노드 코멘트 초기화
                        nodeComment = "";
                    }
                }
            }
            
            String packageName = namespace.substring(0, namespace.lastIndexOf(".")    );
            String typeName    = namespace.substring(   namespace.lastIndexOf(".") + 1);
            
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("packageName"   , packageName        );
            root.put("typeName"      , typeName           );
            root.put("tableComment"  , ""                 );
            root.put("methodItems"   , methodTemplateList );
            
            String tmplSourceVo = Activator.getTemplateSource("mapper_interface.ftlh", root);
            
            sourceTemplate.setPackageName(packageName);
            sourceTemplate.setTypeName   (typeName   );
            sourceTemplate.setSource     (tmplSourceVo);
            
        } catch (Exception e) {
            e.printStackTrace();
            Activator.console(e.toString());
        }
        
        return sourceTemplate;
    }
    
    public static String getAttr(XMLStreamReader stream, String attr) {
        for (int i = 0 ; i < stream.getAttributeCount(); i++) {
            if ( stream.getAttributeName(i).getLocalPart().equals(attr) ) {
                return stream.getAttributeValue(i);
            }
        }
        return "";
    }
    
    public static void createDefaultDBIO(List<String> tableList, List<SourceTemplate> voList, List<SourceTemplate> mapperList) {
     // https://mirwebma.tistory.com/181
        List<String> queryList = new ArrayList<>();
        queryList.add("SELECT D.COLORDER                 AS COLUMN_IDX            -- Column Index          ");
        queryList.add("     , A.NAME                     AS TABLE_NAME            -- Table Name            ");
        queryList.add("     , C.VALUE                    AS TABLE_DESCRIPTION     -- Table Description     ");
        queryList.add("     , D.NAME                     AS COLUMN_NAME           -- Column Name           ");
        queryList.add("     , E.VALUE                    AS COLUMN_DESCRIPTION    -- Column Description    ");
        queryList.add("     , F.DATA_TYPE                AS TYPE                  -- Column Type           ");
        queryList.add("     , F.CHARACTER_OCTET_LENGTH   AS LENGTH                -- Column Length         ");
        queryList.add("     , F.NUMERIC_SCALE            AS SCALE                 -- Column SCALE          ");
        queryList.add("     , F.IS_NULLABLE              AS IS_NULLABLE           -- Column Nullable       ");
        queryList.add("     , F.COLLATION_NAME           AS COLLATION_NAME        -- Column Collaction Name");
        queryList.add("     , ( SELECT COALESCE(MAX('PK'), '')                                             ");
        queryList.add("           FROM INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE                          ");
        queryList.add("          WHERE TABLE_NAME = A.NAME                                                 ");
        queryList.add("            AND CONSTRAINT_NAME = (                                                 ");
        queryList.add("                SELECT CONSTRAINT_NAME                                              ");
        queryList.add("                  FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS                         ");
        queryList.add("                 WHERE TABLE_NAME = A.NAME                                          ");
        queryList.add("                   AND CONSTRAINT_TYPE = 'PRIMARY KEY'                              ");
        queryList.add("                   AND COLUMN_NAME = D.NAME                                         ");
        queryList.add("            )                                                                       ");
        queryList.add("     ) AS IS_PRIMARYKEY                                                             ");
        queryList.add("   FROM SYSOBJECTS A WITH (NOLOCK)                                                  ");
        queryList.add("  INNER JOIN SYSUSERS B WITH (NOLOCK)          ON A.UID = B.UID                     ");
        queryList.add("  INNER JOIN SYSCOLUMNS D WITH (NOLOCK)        ON D.ID = A.ID                       ");
        queryList.add("  INNER JOIN INFORMATION_SCHEMA.COLUMNS F WITH (NOLOCK)                             ");
        queryList.add("     ON A.NAME = F.TABLE_NAME                                                       ");
        queryList.add("    AND D.NAME = F.COLUMN_NAME                                                      ");
        queryList.add("   LEFT OUTER JOIN SYS.EXTENDED_PROPERTIES C WITH (NOLOCK)                          ");
        queryList.add("     ON C.MAJOR_ID = A.ID                                                           ");
        queryList.add("    AND C.MINOR_ID = 0                                                              ");
        queryList.add("    AND C.NAME = 'MS_Description'                                                   ");
        queryList.add("   LEFT OUTER JOIN SYS.EXTENDED_PROPERTIES E WITH (NOLOCK)                          ");
        queryList.add("     ON E.MAJOR_ID = D.ID                                                           ");
        queryList.add("    AND E.MINOR_ID = D.COLID                                                        ");
        queryList.add("    AND E.NAME = 'MS_Description'                                                   ");
        queryList.add("  WHERE 1=1                                                                         ");
        queryList.add("    AND A.TYPE = 'U'                                                                ");
        queryList.add("    AND A.NAME = ':TABLE_NAME'                                                      ");  // 바인딩
        queryList.add("  ORDER BY D.COLORDER                                                               ");
        
        String query = String.join("\n", queryList);
        
        try (Statement stmt = Activator.getConnection().createStatement();) {
            
            for (String table : tableList ) {
                
                table = table.trim();
                
                String exeQuery = query.replace(":TABLE_NAME", table);
                
                ResultSet rs = stmt.executeQuery(exeQuery);
                
                List<TableValue> tableValueList = new ArrayList<>();
                while (rs.next()) {
                    TableValue tableValue = new TableValue();
                    tableValue.COLUMN_IDX         = rs.getString("COLUMN_IDX"        );
                    tableValue.TABLE_NAME         = rs.getString("TABLE_NAME"        );
                    tableValue.TABLE_DESCRIPTION  = rs.getString("TABLE_DESCRIPTION" );
                    tableValue.COLUMN_NAME        = rs.getString("COLUMN_NAME"       );
                    tableValue.COLUMN_DESCRIPTION = rs.getString("COLUMN_DESCRIPTION");
                    tableValue.TYPE               = rs.getString("TYPE"              );
                    tableValue.LENGTH             = rs.getString("LENGTH"            );
                    tableValue.SCALE              = rs.getString("SCALE"             );
                    tableValue.IS_NULLABLE        = rs.getString("IS_NULLABLE"       );
                    tableValue.COLLATION_NAME     = rs.getString("COLLATION_NAME"    );
                    tableValue.IS_PRIMARYKEY      = rs.getString("IS_PRIMARYKEY"     );
                    
                    // 자바관련
                    tableValue.JAVA_TYPE       = getJavaType(tableValue);
                    tableValue.JAVA_NAME       = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableValue.COLUMN_NAME);
                    tableValue.JAVA_NAME_UPPER = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableValue.COLUMN_NAME);
                    
                    // 넥사
                    tableValueList.add(tableValue);
                }
                
                // 테이블
                if (tableValueList.isEmpty()) {
                    Activator.console(table + " 테이블 정보를 DB에서 찾지 못했습니다. 테이블이 존재 하는지 확인하세요.");
                } else {
                    
                    // ROOT
                    String tableName    = tableValueList.get(0).TABLE_NAME;
                    String tableComment = StringUtils.defaultString( tableValueList.get(0).TABLE_DESCRIPTION);
                    String bizPackage   = Activator.getProperty("project.rootPackage") + ".dbio.";
                    bizPackage         += tableName.substring(tableName.indexOf("_") + 1, tableName.indexOf("_") + 3).toLowerCase();  // TB_[BC]001M -> bc
                    
                    Map<String, Object> root = new HashMap<String, Object>();
                    
                    List<FieldTemplate> fieldList = getFieldMapping(tableValueList);
                    
                    root.put("packageName" , bizPackage  );
                    root.put("typeName"    , tableName   );
                    root.put("tableComment", tableComment);
                    root.put("fieldItems"  , fieldList   );
                    
                    String tmplSourceVo     = Activator.getTemplateSource("dbio_vo.ftlh"   , root);
                    String tmplSourceMapper = Activator.getTemplateSource("mapper_xml.ftlh", root);
                    
                    // vo
                    SourceTemplate sourceTemplateVo = new SourceTemplate();
                    sourceTemplateVo.setPackageName (bizPackage  );
                    sourceTemplateVo.setTypeName    (tableName   );
                    sourceTemplateVo.setTableComment(tableComment);
                    sourceTemplateVo.setSource      (tmplSourceVo);
                    
                    // mapper
                    SourceTemplate sourceTemplateMapper = new SourceTemplate();
                    sourceTemplateMapper.setPackageName (bizPackage  );
                    sourceTemplateMapper.setTypeName    (tableName   );
                    sourceTemplateMapper.setTableComment(tableComment);
                    sourceTemplateMapper.setSource      (tmplSourceMapper);
                    
                    // 추가
                    voList.add    (sourceTemplateVo    );
                    mapperList.add(sourceTemplateMapper);
                }
            }
        } catch (SQLException | UnsupportedEncodingException e) {
            e.printStackTrace();
            Activator.console(e.toString());
        }
    }
    
    
    public static List<FieldTemplate> getFieldMapping(List<TableValue> tableValueList) throws UnsupportedEncodingException {
        
        List<FieldTemplate> fieldList = new ArrayList<>();
        
        int maxTypeSpace       = 0;
        int maxNameSpace       = 0;
        int maxColumnNameSpace = 0;
        int maxCommentSpace    = 0;
        
        
        int maxPkNameSpace       = 0;
        int maxPkColumnNameSpace = 0;
        int maxPkCommentSpace    = 0;
        
        for(TableValue table : tableValueList) {
            
            int tmpTypeSpace       = table.JAVA_TYPE.length();
            int tmpColumnNameSpace = table.COLUMN_NAME.length();
            int tmpNameSpace       = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, table.COLUMN_NAME).length();
            int tmpCommentSpace    = StringUtils.defaultString(table.COLUMN_DESCRIPTION).getBytes("MS949").length;  // 영문 1, 한글 2

            if (maxTypeSpace < tmpTypeSpace) {
                maxTypeSpace = tmpTypeSpace;
            }
            
            
            if (maxNameSpace < tmpNameSpace) {
                maxNameSpace = tmpNameSpace;
            }
            
            if (maxColumnNameSpace < tmpColumnNameSpace) {
                maxColumnNameSpace = tmpColumnNameSpace;
            }
            
            if (maxCommentSpace < tmpCommentSpace) {
                maxCommentSpace = tmpCommentSpace;
            }
            
            
            // PK
            if ("PK".equals(table.IS_PRIMARYKEY) ) {
            int tmpPkColumnNameSpace = table.COLUMN_NAME.length();
            int tmpPkNameSpace       = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, table.COLUMN_NAME).length();
            int tmpPkCommentSpace    = StringUtils.defaultString(table.COLUMN_DESCRIPTION).getBytes("MS949").length;  // 영문 1, 한글 2
                if (maxPkNameSpace < tmpPkNameSpace) {
                    maxPkNameSpace = tmpPkNameSpace;
                }
                
                if (maxPkColumnNameSpace < tmpPkColumnNameSpace) {
                    maxPkColumnNameSpace = tmpPkColumnNameSpace;
                }
                
                if (maxPkCommentSpace < tmpPkCommentSpace) {
                    maxPkCommentSpace = tmpPkCommentSpace;
                }
            }
        }
        
        for(TableValue table : tableValueList) {
            FieldTemplate template = new FieldTemplate();
            
            template.setType      (table.JAVA_TYPE                                    );  // 타입
            template.setComment   (StringUtils.defaultString(table.COLUMN_DESCRIPTION));  // 설명
            template.setPk        (table.IS_PRIMARYKEY                                );  // PK
            template.setName      (table.JAVA_NAME                                    );  // 필드명
            template.setNameUpper (table.JAVA_NAME_UPPER                              );  // 대문자시작_필드명
            template.setBindName  ("#{" + table.JAVA_NAME + "}"                       );  // 바인드_필드명
            template.setColumnName(table.COLUMN_NAME                                  );  // DB컬럼명
            
            template.setNexaType  (getNexaType(table)                                 );
            
            // SPACE 영역
            int tmpTypeSpace       = table.JAVA_TYPE.length();
            int tmpNameSpace       = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, table.COLUMN_NAME).length();
            int tmpColumnNameSpace = table.COLUMN_NAME.length();
            int tmpCommentSpace    = StringUtils.defaultString(table.COLUMN_DESCRIPTION).getBytes("MS949").length;
            
            template.setTypeSpace      (makeSpace(maxTypeSpace       - tmpTypeSpace      ));  // 타입
            template.setNameSpace      (makeSpace(maxNameSpace       - tmpNameSpace      ));  // 필드명
            template.setColumnNameSpace(makeSpace(maxColumnNameSpace - tmpColumnNameSpace));  // 컬럼(필드명)
            template.setCommentSpace   (makeSpace(maxCommentSpace    - tmpCommentSpace   ));  // 설명
            
            //PK - QUERY XML에서만 사용
            if ("PK".equals(table.IS_PRIMARYKEY) ) {
                int tmpPkNameSpace       = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, table.COLUMN_NAME).length();
                int tmpPkColumnNameSpace = table.COLUMN_NAME.length();
                int tmpPkCommentSpace    = StringUtils.defaultString(table.COLUMN_DESCRIPTION).getBytes("MS949").length;
                
                template.setPkNameSpace      (makeSpace(maxPkNameSpace       - tmpPkNameSpace      ));  // 필드명
                template.setPkColumnNameSpace(makeSpace(maxPkColumnNameSpace - tmpPkColumnNameSpace));  // 컬럼(필드명)
                template.setPkCommentSpace   (makeSpace(maxPkCommentSpace    - tmpPkCommentSpace   ));  // 설명
            }
            
            fieldList.add(template);
        }
        
        return fieldList;
    }
    
    private static String makeSpace(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
    
    // https://docs.microsoft.com/ko-kr/sql/connect/jdbc/using-basic-data-types?view=sql-server-ver15
    private static String getJavaType(TableValue table) {
        String returnType = "";
        switch (table.TYPE.toLowerCase()) {
        case "decimal":
        case "numeric":
            
            if ( table.SCALE == "0" ) {
                returnType = "java.lang.Long";
            } else {
                returnType = "java.math.BigDecimal";
            }
            break;

        case "bigint":
            returnType = "long";
            break;
            
        case "integer":
            returnType = "int";
            break;
            
        default:
            returnType = "String";
            break;
        }
        
        return returnType;
    }
    
    private static String getNexaType(TableValue table) {
        
        String returnType = "";
        switch (table.TYPE.toLowerCase()) {
        case "decimal":
        case "numeric":
            
            returnType = "BIGDECIMAL";
            break;
            
        default:
            returnType = "STRING";
            break;
        }
        
        return returnType;
    }
    
    
    public static void main(String[] args) {
        
        Activator.initThisPlugin("C:\\eclipse_rcp\\runtime-EclipseApplication\\.metadata\\mgplugin");
        
        SourceTemplate temp = SourceGenerator.mapperToInterface("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\HR_HUMANEWNMapper.xml");
        System.out.println(temp.getSource());
        
        Activator.closeThisPlugin();

    }
}
