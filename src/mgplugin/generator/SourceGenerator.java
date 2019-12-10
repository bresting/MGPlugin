package mgplugin.generator;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

import mgplugin.Activator;
import mgplugin.generator.entity.FieldTemplate;
import mgplugin.generator.entity.MethodTemplate;
import mgplugin.generator.entity.SourceTemplate;
import mgplugin.generator.entity.TableValue;
import mgplugin.generator.entity.XMLQuery;

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
    
    
    public static void main(String[] args) {
        
        
        Activator.initThisPlugin("C:\\eclipse_rcp\\runtime-EclipseApplication\\.metadata\\mgplugin");
        
        //SourceTemplate temp = SourceGenerator.mapperToInterface("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\HR_HUMANEWNMapper.xml");
        //System.out.println(temp.getSource());
        
        //XMLQuery query = SourceGenerator.getQueryAtOffset("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\HR_HUMANEWNMapper.xml", 2000);

        XMLQuery query = SourceGenerator.getQueryAtOffset("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\XX1010Mapper.xml", 100);
        SourceTemplate inSourceTemplate = new SourceTemplate();
        SourceTemplate outSourceTemplate = new SourceTemplate();
        getQueryVoFields(query, inSourceTemplate, outSourceTemplate);
        
        
        System.out.println(inSourceTemplate.getSource());
        System.out.println("=========================");
        System.out.println(outSourceTemplate.getSource());
        
        Activator.closeThisPlugin();
        
    }
    
    public static void getQueryVoFields(XMLQuery query, SourceTemplate inSourceTemplate, SourceTemplate outSourceTemplate) {
        
        List<String> inList  = getInputFields (query.getQuery());
        List<String> outList = getOutputFields(query.getQuery());
        
        if ( query.getParameterType().startsWith( Activator.getProperty("project.rootPackage") )  // 입력
          || query.getResultType   ().startsWith( Activator.getProperty("project.rootPackage") )  // 결과
        ) {
            if ( query.getParameterType().equals(query.getResultType()) ) {
                
                for (String tmp : outList) {
                    if (!inList.contains(tmp)) {
                        inList.add(tmp);
                    }
                }
                
                /**
                 * 입력 & 결과
                 */
                List<FieldTemplate> result = getMetaInfo(inList);
                
                Map<String, Object> root = new HashMap<String, Object>();
                root.put("xmlFile"    , query.getFileName     ());
                root.put("queryId"    , query.getQueryId      ());
                root.put("packageName", query.getParameterType());
                root.put("typeName"   , query.getParameterType().substring(query.getParameterType().lastIndexOf(".") + 1));
                root.put("fieldItems", result);
                
                String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
                
                inSourceTemplate.setSource(tmplSourceVo);
                outSourceTemplate.setSource("입출력 같다.");
                
            } else {

                if ( query.getParameterType().startsWith( Activator.getProperty("project.rootPackage") ) ) {
                    /**
                     * 입력
                     */
                    List<FieldTemplate> result = getMetaInfo(outList);
                    
                    Map<String, Object> root = new HashMap<String, Object>();
                    root.put("xmlFile"    , query.getFileName     ());
                    root.put("queryId"    , query.getQueryId      ());
                    root.put("packageName", query.getParameterType());
                    root.put("typeName"   , query.getParameterType().substring(query.getParameterType().lastIndexOf(".") + 1));
                    root.put("fieldItems" , result);
                    
                    String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
                    
                    inSourceTemplate.setSource(tmplSourceVo);
                }
                
                if ( query.getResultType().startsWith( Activator.getProperty("project.rootPackage") ) ) {
                    /**
                     * 결과
                     */
                    List<FieldTemplate> result = getMetaInfo(inList);
                    
                    Map<String, Object> root = new HashMap<String, Object>();
                    root.put("xmlFile"    , query.getFileName     ());
                    root.put("queryId"    , query.getQueryId      ());
                    root.put("packageName", query.getResultType());
                    root.put("typeName"   , query.getResultType().substring(query.getResultType().lastIndexOf(".") + 1));
                    root.put("fieldItems" , result);
                    
                    String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
                    
                    outSourceTemplate.setSource(tmplSourceVo);
                }
            }
        } else {
            Activator.console("입력/결과 프로젝트 유형이 아님");
        }
    }
    
    
    public static XMLQuery getQueryAtOffset(String filePath, int offset) {
        
        XMLInputFactory factory  = XMLInputFactory.newInstance();
        List<String>    textList = null;
        
        XMLQuery xmlQuery = new XMLQuery();
        
        // XML 파일명
        xmlQuery.setFileName(filePath.substring(filePath.lastIndexOf("\\") + 1));
        
        XMLStreamReader streamReader = null;
        try {
            streamReader = factory.createXMLStreamReader(new FileReader(filePath));
            String queryId       = "";
            String resultType    = "";
            String parameterType = "";
            while (streamReader.hasNext()) {
                int evt = streamReader.next();
                
                // 시작점
                if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT ) {
                    if ( "select".equals( streamReader.getName().toString())
                      || "insert".equals( streamReader.getName().toString())
                      || "update".equals( streamReader.getName().toString())
                      || "delete".equals( streamReader.getName().toString())
                    ) {
                        queryId       = streamReader.getAttributeValue(null, "id"           );
                        resultType    = streamReader.getAttributeValue(null, "resultType"   );
                        parameterType = streamReader.getAttributeValue(null, "parameterType");
                        
                        textList = new ArrayList<String>();
                    }
                }
                
                if (textList != null) {
                    if ( evt == XMLEvent.CHARACTERS) {
                        textList.add(streamReader.getText());
                    }
                }
                
                if (streamReader.getEventType() == XMLStreamReader.END_ELEMENT ) {
                    if ( "select".equals( streamReader.getName().toString())
                      || "insert".equals( streamReader.getName().toString())
                      || "update".equals( streamReader.getName().toString())
                      || "delete".equals( streamReader.getName().toString())
                    ) {
                        Location location = streamReader.getLocation();
                        if ( offset < location.getCharacterOffset() ) {
                            xmlQuery.setQueryId      (queryId      );
                            xmlQuery.setResultType   (resultType   );
                            xmlQuery.setParameterType(parameterType);
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            Activator.console(e);
        } finally {
            if ( streamReader != null ) {
                try {
                    streamReader.close();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
        }
        
        xmlQuery.setQuery(String.join("", textList));
        return xmlQuery;
    }
    
    /**
     * XML QUERY -> VO
     */
    public static Pattern PAT_LINE_COMMENT = Pattern.compile("--.*");
    public static Pattern PAT_SELECT       = Pattern.compile("SELECT(.*?)(FROM|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static Pattern PAT_COLUMN       = Pattern.compile("(\\w+)(\\s*)(,|$)"  , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static Pattern PAR_BIND_COLUMN  = Pattern.compile("(\\$|#)\\{([\\w_]+).*?\\}");
    
    /**
     * 파라미터
     * 
     * @param query
     * @return
     */
    public static List<String> getInputFields(String query) {
        Matcher mat = PAR_BIND_COLUMN.matcher(query);
        List<String> columnList = new ArrayList<String>();
        while(mat.find()) {
            String bindStr = mat.group(2);
            if (!columnList.contains(bindStr)) {
                 columnList.add(mat.group(2));
            }
        }
        return columnList;
    }
    
    public static List<String> getOutputFields(String query) {
        
        // 라인주석 제거
        Matcher lineMatcher = PAT_LINE_COMMENT.matcher(query);
        StringBuffer sbReplace = new StringBuffer();
        while(lineMatcher.find()) {
            lineMatcher.appendReplacement(sbReplace, "");
        }
        lineMatcher.appendTail(sbReplace);
        
        // 블럭주석 & 괄호 제거
        Stack<Integer> stkMultiComment   = new Stack<Integer>();
        Stack<Integer> stkBracketComment = new Stack<Integer>();
        
        char[] arrQuery = query.toCharArray();
        try {
            for (int idx = 0; idx < arrQuery.length; idx++) {
                // 주석
                if ( arrQuery[idx] == '/' && arrQuery[idx + 1] == '*') {
                    stkMultiComment.push(idx);
                    idx++;
                } else if ( arrQuery[idx] == '*' && arrQuery[idx + 1] == '/') {
                    int rtn = stkMultiComment.pop();
                    for ( int x = rtn; x < (idx + 2); x++ ) {
                        arrQuery[x] = ' ';
                    }
                    idx++;
                }
                
                // 괄호
                if ( arrQuery[idx] == '(') {
                    stkBracketComment.push(idx);
                } else if ( arrQuery[idx] == ')') {
                    int rtn = stkBracketComment.pop();
                    // 중복 괄호인 경우 한번에 처리
                    if (stkBracketComment.isEmpty()) {
                        for ( int x = rtn; x <= idx; x++ ) {
                            arrQuery[x] = ' ';
                        }
                    }
                }
            }
        } catch ( ArrayIndexOutOfBoundsException | EmptyStackException e) {
            Activator.console("주석 또는 괄호, 시작, 종료가 맞지 않습니다. - " + e.toString());
        }
        
        List<String> columnList = new ArrayList<String>();
        
        // SELECT ... FROM 또는 SELECT 문장종료
        Matcher selectMatcher = PAT_SELECT.matcher(String.valueOf(arrQuery));
        if (selectMatcher.find()) {
            Matcher columnMatcher = PAT_COLUMN.matcher(selectMatcher.group(1));
            while (columnMatcher.find()) {
                String column = columnMatcher.group(1);
                if (columnList.contains(column)) {
                    Activator.console("SELECT [" + column + "] 중복컬럼이 존재합니다.");
                }
                
                columnList.add(columnMatcher.group(1));
            }
        }
        
        return columnList;
    }
    
    /**
     * xml mapper TO java interface
     * 
     * @param filePath
     * @return
     */
    public static SourceTemplate mapperToInterface(String filePath) {
        
        XMLInputFactory      factory            = XMLInputFactory.newInstance();
        SourceTemplate       sourceTemplate     = new SourceTemplate();
        List<MethodTemplate> methodTemplateList = new ArrayList<>();
        
        XMLStreamReader streamReader = null;
        try {
            streamReader = factory.createXMLStreamReader(
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
        } finally {
            if (streamReader != null) {
                try {
                    streamReader.close();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
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
    
    
    public static List<FieldTemplate> getMetaInfo(List<String> columnList) {
        
        List<FieldTemplate> resultList = new ArrayList<>();
        
        for (String column : columnList ) {
            
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
                
                String srchColumn = column.trim();
                srchColumn        = srchColumn.replaceAll("\\d+$", "");  // 숫자로 끝나는 경우 제거
                srchColumn        = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, srchColumn);  // corpCode -> CORP_CODE
                
                String exeQuery = query.replace(":TERMS_PHYCS_NAME", srchColumn);
                
                ResultSet rs = stmt.executeQuery(exeQuery);
                
                FieldTemplate fieldTemplate = new FieldTemplate();
                TableValue tableValue = new TableValue();
                if (rs.next()) {
                    tableValue.COLUMN_NAME        = rs.getString("TERMS_PHYCS_NAME" );
                    tableValue.COLUMN_DESCRIPTION = rs.getString("TERMS_LOGIC_NAME" );
                    tableValue.TYPE               = rs.getString("DOMAIN_DATA_TYPE" );
                    tableValue.LENGTH             = rs.getString("DOMAIN_DATA_SIZE" );
                    tableValue.SCALE              = rs.getString("DOMAIN_DATA_SCALE");
                } else {
                    column = column + "???";
                    tableValue.COLUMN_DESCRIPTION = "미 등록 용어입니다.";
                }
                
                fieldTemplate.setName     (column                                                    );  // 물리명
                fieldTemplate.setNameUpper(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, column) );
                fieldTemplate.setComment  (tableValue.COLUMN_DESCRIPTION                             );  // 논리명
                fieldTemplate.setType     (getJavaType(tableValue)                                   );  // 타입
                fieldTemplate.setNexaType (getNexaType(tableValue                                  ) );  // 타입
                
                resultList.add(fieldTemplate);
                
            } catch (SQLException e) {
                e.printStackTrace();
                Activator.console(e.toString());
            }
        }
        
        int maxTypeSpace    = 0;
        int maxNameSpace    = 0;
        for(FieldTemplate template : resultList) {
            int tmpTypeSpace = template.getType().length();
            int tmpNameSpace = template.getName().length();

            if (maxTypeSpace < tmpTypeSpace) {
                maxTypeSpace = tmpTypeSpace;
            }
            
            if (maxNameSpace < tmpNameSpace) {
                maxNameSpace = tmpNameSpace;
            }
        }
        
        for(FieldTemplate template : resultList) {
            
            int tmpTypeSpace = template.getType().length();
            int tmpNameSpace = template.getName().length();
            
            template.setTypeSpace(makeSpace(maxTypeSpace - tmpTypeSpace      ));  // 타입
            template.setNameSpace(makeSpace(maxNameSpace - tmpNameSpace      ));  // 필드명
        }
        
        return resultList;
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
                returnType = "Long";
            } else {
                returnType = "java.math.BigDecimal";
            }
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
}
