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
import java.util.Map.Entry;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

import mgplugin.Activator;
import mgplugin.generator.entity.FieldTemplate;
import mgplugin.generator.entity.MethodTemplate;
import mgplugin.generator.entity.SourceTemplate;
import mgplugin.generator.entity.TableValue;
import mgplugin.generator.entity.XmlTagElement;

/**
 * <pre>
 * @programName : 프로그램명
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
    
    public static void main(String[] args) throws Exception {
        Activator.initThisPlugin("C:\\eclipse_rcp\\runtime-EclipseApplication\\.metadata\\mgplugin");
        // XML -> Interface
        {
            // SourceTemplate temp = SourceGenerator.mapperToInterface("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\XX1000Mapper.xml");
            // System.out.println(temp.getSource());
            // C:\eclipse_rcp\workspace\MgPlugin\resources\XX1000Mapper.xml
        }
        
        // Query -> vo
        {
            // XMLQuery query = SourceGenerator.getQueryAtOffset("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\XX1000Mapper.xml", 100);
            // SourceTemplate inSourceTemplate = new SourceTemplate();
            // SourceTemplate outSourceTemplate = new SourceTemplate();
            // getQueryVoFields(query, inSourceTemplate, outSourceTemplate);
            // 
            // System.out.println(inSourceTemplate.getSource());
            // System.out.println("=========================");
            // System.out.println(outSourceTemplate.getSource());
        }
        
        // DBIO
        {
            // List<String> tableList = new ArrayList<>();
            // tableList.add("CB2TB_BC701M");
            // 
            // List<SourceTemplate> resultVoList        = new ArrayList<>();
            // List<SourceTemplate> resultMapperList    = new ArrayList<>();
            // 
            // SourceGenerator.createDefaultDBIO(tableList, resultVoList, resultMapperList);
            // 
            // if (!resultMapperList.isEmpty()) {
            //     System.out.println(resultMapperList.get(0).getSource());
            // }
        }
        
        // Query
        {
            Map<String, List<String>> temp = SourceGenerator.getTypeFieldMap("C:\\eclipse_rcp\\workspace\\MGPlugin\\resources\\XX1000Mapper.xml");
            
            for (Entry<String, List<String>> map : temp.entrySet()) {
                System.out.println(map.getKey() + " :: "+ map.getValue());
            }
        }
        
        Activator.closeThisPlugin();
    }

    
    // MYBATIS 태그정보
    private static final String SELECT = "select";
    private static final String INSERT = "insert";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";
    
    private static final String PARAMETER_TYPE = "parameterType";
    private static final String RESULT_TYPE    = "resultType";
    
    /**
     * 옵셋위치 타입가져오기
     * 
     * @param filePath
     * @param offset
     * @return
     * @throws Exception 
     */
    public static XmlTagElement getTypeAtOffset(String filePath, int offset) throws Exception {
        
        XmlTagElement   xmlTagElement = new XmlTagElement();
        XMLInputFactory factory       = XMLInputFactory.newInstance();
        
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        // XML 파일명
        try (FileReader reader = new FileReader(filePath)) {
            
            XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
            
            String resultType    = "";
            String parameterType = "";
            String queryId       = "";
            
            while (streamReader.hasNext()) {
                int nextEventType = streamReader.next();  // streamReader.getEventType()
                
                // 태그시작
                if ( nextEventType == XMLStreamReader.START_ELEMENT ) {
                    if ( SELECT.equals(streamReader.getName().toString())
                      || INSERT.equals(streamReader.getName().toString())
                      || UPDATE.equals(streamReader.getName().toString())
                      || DELETE.equals(streamReader.getName().toString())
                    ) {
                        // 결과셋 저장
                        queryId       = getAttr(streamReader, "id"          );
                        parameterType = getAttr(streamReader, PARAMETER_TYPE);
                        resultType    = getAttr(streamReader, RESULT_TYPE   );
                    }
                }
                
                // 태그종료 - 커서위치 확인
                if ( nextEventType == XMLStreamReader.END_ELEMENT ) {
                    if ( SELECT.equals(streamReader.getName().toString())
                      || INSERT.equals(streamReader.getName().toString())
                      || UPDATE.equals(streamReader.getName().toString())
                      || DELETE.equals(streamReader.getName().toString())
                    ) {
                        Location location = streamReader.getLocation();
                        if ( offset < location.getCharacterOffset() ) {
                            
                            System.out.println("queryId      : " + queryId      );
                            System.out.println("parameterType: " + parameterType);
                            System.out.println("resultType   : " + resultType   );
                            
                            // 결과셋 담기
                            xmlTagElement.setParameterType(parameterType);
                            xmlTagElement.setResultType   (resultType   );
                            
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            throw e;
        }
        
        return xmlTagElement;
    }
    
    
    /**
     * 타입별 쿼리가져오기
     * @param filePath
     * @return
     * @throws Exception 
     */
    public static Map<String, List<String>> getTypeFieldMap(String filePath) throws Exception {
        XMLInputFactory           factory       = XMLInputFactory.newInstance();
        Map<String, List<String>> queryMapList  = new HashMap<>();
        List<String>              queryTextList = null;
        
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        try (FileReader reader = new FileReader(filePath)) {
            
            XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
            
            String resultType    = "";
            String parameterType = "";
            String queryId       = "";
            while (streamReader.hasNext()) {
                int nextEventType = streamReader.next();  // streamReader.getEventType()
                
                // 태그시작
                if ( nextEventType == XMLStreamReader.START_ELEMENT) {
                    if ( SELECT.equals(streamReader.getName().toString())
                      || INSERT.equals(streamReader.getName().toString())
                      || UPDATE.equals(streamReader.getName().toString())
                      || DELETE.equals(streamReader.getName().toString())
                    ) {
                        queryId = getAttr(streamReader, "id");
                        // 결과셋 저장
                        parameterType = getAttr(streamReader, PARAMETER_TYPE);
                        resultType    = getAttr(streamReader, RESULT_TYPE   );
                        
                        queryTextList = new ArrayList<String>();
                    }
                }
                
                // 쿼리저장
                if (queryTextList != null) {
                    if (nextEventType == XMLEvent.CHARACTERS) {
                        queryTextList.add(streamReader.getText());
                    }
                }
                
                // 태그종료 - 커서위치 확인
                if ( nextEventType == XMLStreamReader.END_ELEMENT ) {
                    if ( SELECT.equals(streamReader.getName().toString())
                      || INSERT.equals(streamReader.getName().toString())
                      || UPDATE.equals(streamReader.getName().toString())
                      || DELETE.equals(streamReader.getName().toString())
                    ) {
                        // 입출력 파라미터 추출
                        List<String> inFieldList = new ArrayList<>();
                        List<String> otFieldList = new ArrayList<>();
                        String query = String.join("", queryTextList);
                        
                        inFieldList = getInputFields (query);
                        
                        //if ( SELECT.equals(streamReader.getName().toString()) ) {
                        if ( ! StringUtils.defaultString( resultType).isEmpty() ) {
                            otFieldList = getOutputFields(query, queryId);
                        }
                        
                        /*
                        if ( queryTextList != null) {
                            String query = String.join("", queryTextList);
                            inFieldList = getInputFields (query);
                            otFieldList = getOutputFields(query);
                        }*/
                        
                        // 쿼리텍스트 초기화
                        queryTextList = null;
                        
                        // 파라미터_필드_셋팅 - java.lang.String
                        List<String> parameterFieldList = queryMapList.get(parameterType);
                        if ( parameterFieldList == null ) {
                            parameterFieldList = new ArrayList<String>();
                        }
                        
                        for (String inField : inFieldList) {
                            if ( ! parameterFieldList.contains(inField) ) {
                                parameterFieldList.add(inField);
                            }
                        }
                        
                        // 파라미터_필드_맵추가
                        queryMapList.put(parameterType, parameterFieldList);
                        
                        // 결과_필드_셋팅 - java.lang.String
                        List<String> resultFieldList = queryMapList.get(resultType);
                        if ( resultFieldList == null ) {
                            resultFieldList = new ArrayList<String>();
                        }
                        
                        for (String otField : otFieldList) {
                            if ( ! resultFieldList.contains(otField) ) {
                                resultFieldList.add(otField);
                            }
                        }
                        
                        // 결과_필드_맵추가
                        queryMapList.put(resultType   , resultFieldList   );
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
        
        return queryMapList;
    }
    
    /**
     * 
     * @param xmlTagElement
     * @param typeFieldMap
     * @param inSourceTemplate
     * @param outSourceTemplate
     */
    public static void getVoTemplate(XmlTagElement xmlTagElement, Map<String, List<String>> typeFieldMap, SourceTemplate inSourceTemplate, SourceTemplate outSourceTemplate) {
        
        String paramPackageName = xmlTagElement.getParameterType();
        String paramTypeName    = xmlTagElement.getParameterType();
        
        String resultPackageName = xmlTagElement.getResultType();
        String resultTypeName    = xmlTagElement.getResultType();
        
        if ( xmlTagElement.getParameterType().contains(".") ) {
            paramPackageName = xmlTagElement.getParameterType().substring(0, xmlTagElement.getParameterType().lastIndexOf(".")    );
            paramTypeName    = xmlTagElement.getParameterType().substring(   xmlTagElement.getParameterType().lastIndexOf(".") + 1);
        }
        
        if ( xmlTagElement.getResultType().contains(".") ) {
            resultPackageName = xmlTagElement.getResultType().substring(0, xmlTagElement.getResultType().lastIndexOf(".")    );
            resultTypeName    = xmlTagElement.getResultType().substring(   xmlTagElement.getResultType().lastIndexOf(".") + 1);
        }
        
        if ( xmlTagElement.getParameterType().equals(xmlTagElement.getResultType()) ) {
            
            List<String> inFieldList     = typeFieldMap.get(xmlTagElement.getParameterType());
            
            /**
             * 입력 & 결과
             */
            List<FieldTemplate> result = getMetaInfo(inFieldList);
            
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("userName"   , Activator.getProperty("user.name")); 
            root.put("packageName", paramPackageName   );
            root.put("typeName"   , paramTypeName      );
            root.put("fieldItems" , result             );
            
            String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
            
            inSourceTemplate .setSource(tmplSourceVo  );
            outSourceTemplate.setSource("입출력 같다.");
            
        } else {
            
            List<String> inFieldList = typeFieldMap.get(xmlTagElement.getParameterType());
            List<String> otFieldList = typeFieldMap.get(xmlTagElement.getResultType   ());
            
            /**
             * 입력
             */
            if (!StringUtils.isEmpty(xmlTagElement.getParameterType()) ) {
                List<FieldTemplate> result = getMetaInfo(inFieldList);
                
                Map<String, Object> root = new HashMap<String, Object>();
                root.put("userName"   , Activator.getProperty("user.name"));
                root.put("packageName", paramPackageName   );
                root.put("typeName"   , paramTypeName      );
                root.put("fieldItems" , result             );
                
                String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
                
                inSourceTemplate.setSource(tmplSourceVo);
            }
        
            /**
             * 결과
             */
            if (!StringUtils.isEmpty(xmlTagElement.getResultType()) ) {
                List<FieldTemplate> result = getMetaInfo(otFieldList);
                
                Map<String, Object> root = new HashMap<String, Object>();
                root.put("userName"   , Activator.getProperty("user.name"));
                root.put("packageName", resultPackageName  );
                root.put("typeName"   , resultTypeName     );
                root.put("fieldItems" , result             );
                
                String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
                outSourceTemplate.setSource(tmplSourceVo);
            }
        }
    }
    
    /**
     * XML QUERY -> VO
     */
    public static Pattern PATTERN_LINE_COMMENT     = Pattern.compile("--.*");
    public static Pattern PATTERN_SINGLE_QUOTATION = Pattern.compile("'.*?'"                         , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static Pattern PATTERN_SELECT_UNION     = Pattern.compile("UNION.*?SELECT"                , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static Pattern PATTERN_SELECT           = Pattern.compile("SELECT(.*?)(FROM|UNION_FROM|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static Pattern PATTERN_COLUMN           = Pattern.compile("(\\w+)(\\s*)(,|$)"             , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static Pattern PATTERN_BIND_COLUMN      = Pattern.compile("(\\$|#)\\{(\\w+).*?\\}");
    
    /**
     * 파라미터
     * 
     * @param query
     * @return
     */
    public static List<String> getInputFields(String query) {
        Matcher mat = PATTERN_BIND_COLUMN.matcher(query);
        List<String> columnList = new ArrayList<String>();
        while(mat.find()) {
            String bindStr = mat.group(2);
            if (!columnList.contains(bindStr)) {
                 columnList.add(mat.group(2));
            }
        }
        
        return columnList;
    }
    
    public static List<String> getOutputFields(String query, String queryId) {
        
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
            }
            
            if ( ! stkMultiComment.isEmpty() ) {
                Activator.console("주석 /* ... */ 시작, 종료가 맞지 않습니다. - " + queryId);
            }
            
        } catch ( ArrayIndexOutOfBoundsException | EmptyStackException e) {
            Activator.console("주석 /* ... */ 시작, 종료가 맞지 않습니다. - " + queryId);
        }
        
        // 라인주석 제거
        query = PATTERN_LINE_COMMENT.matcher(String.valueOf(arrQuery)).replaceAll("");
        
        // 싱글쿼테이션 내용 제거
        query = PATTERN_SINGLE_QUOTATION.matcher(String.valueOf(arrQuery)).replaceAll("");
        
        arrQuery = query.toCharArray();
        
        try {
            for (int idx = 0; idx < arrQuery.length; idx++) {
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
            
            if ( ! stkBracketComment.isEmpty() ) {
                Activator.console("괄호 ( ... ) 시작, 종료가 맞지 않습니다. - " + queryId);
            }
        } catch ( ArrayIndexOutOfBoundsException | EmptyStackException e) {
            Activator.console("괄호 ( ... ) 시작, 종료가 맞지 않습니다. - " + queryId);
        }
        
        query = String.valueOf(arrQuery);
        
        /**
         * 복합쿼리영역(multiple query block) 사용시 마지막 SELECT 구문이 출력 영역이다.
         * 
         * -------------------------------------------------------------------------------------------------------------
         * INSERT INTO TMP SELECT COL_A FROM A
         * 
         * SELECT ... FROM A1
         * UNION
         * SELECT ... FROM A2
         *
         * SELECT ... FROM TBL1 <- 첫번째 SELECT 영역이 AS 컬럼 부분이다.
         * UNION ALL
         * SELECT ... FROM TBL2
         * -------------------------------------------------------------------------------------------------------------
         * 
         * 위 쿼리는 UNION 구문 삭제 후 마지막 영역 SELECT를 캡쳐해서 사용한다.
         * INSERT INTO TMP SELECT COL_A FROM A
         * 
         * SELECT ... FROM A1
         * 
         * SELECT ... FROM TBL1
         */
        query = PATTERN_SELECT_UNION.matcher(query).replaceAll("UNION_FROM");
        
        /**
         * SELECT ... FROM DUAL <- 첫번째 SELECT
         * UNION ALL
         * SELECT ... FROM DUAL
         */
        Matcher selectMatcher = PATTERN_SELECT.matcher(query);
        while( selectMatcher.find() ) {
            query = selectMatcher.group(1);
        }
        
        List<String> columnList = new ArrayList<String>();
        
        if ( ! query.isEmpty()) {
            Matcher columnMatcher = PATTERN_COLUMN.matcher(query);
            while (columnMatcher.find()) {
                String column = columnMatcher.group(1);
                if (columnList.contains(column)) {
                    Activator.console("SELECT [" + column + "] 중복컬럼이 존재합니다. 동적쿼리 사용시 무시 - " + queryId);
                }
                columnList.add(column);
            }
        }
        
        return columnList;
    }
    
    /**
     * xml mapper TO java interface
     * 
     * @param filePath
     * @return
     * @throws Exception 
     */
    public static SourceTemplate mapperToInterface(String filePath) throws Exception {
        
        XMLInputFactory      factory            = XMLInputFactory.newInstance();
        SourceTemplate       sourceTemplate     = new SourceTemplate();
        List<MethodTemplate> methodTemplateList = new ArrayList<>();
        
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        try ( FileInputStream fileStream = new FileInputStream( filePath )  ){
            XMLStreamReader streamReader = null;
            streamReader = factory.createXMLStreamReader(
                    new InputStreamReader( fileStream, Charset.forName( "UTF8" ) )
            );
            
            String namespace        = "";
            String namespaceComment = "";
            String nodeComment      = "";
            
            while (streamReader.hasNext()) {
                int eventType = streamReader.next();
                
                if ( eventType == XMLStreamReader.COMMENT ) {
                    nodeComment = streamReader.getText().trim();
                }
                
                if ( eventType == XMLStreamReader.START_ELEMENT ) {
                    
                    // 패키지
                    if ( streamReader.getName().toString().equals("mapper") ){
                        namespace        = getAttr(streamReader, "namespace");
                        namespaceComment = nodeComment;
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
                        
                        // Map 기반 generic
                        String generic = "";
                        List<String> genericList = new ArrayList<>();
                        if (resultType.equals("java.util.Map")) {
                            resultType = "java.util.Map<K1,V1>";
                            genericList.add("K1,V1");
                        }
                        
                        if (parameterType.equals("java.util.Map")) {
                            parameterType = "java.util.Map<K2,V2>";
                            genericList.add("K2,V2");
                        }
                        if (!genericList.isEmpty()) {
                            generic += "<" + String.join(",", genericList) + ">";
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
                        template.setSqlCommandType(streamReader.getName().toString());
                        template.setComment       (nodeComment  );
                        template.setMethodName    (id           );
                        template.setGeneric       (generic      );
                        template.setReturnType    (resultType   );
                        template.setParameterType (parameterType);
                        template.setParameterName (parameterName);
                        
                        methodTemplateList.add(template);
                        
                        // 노드 코멘트 초기화
                        nodeComment = "";
                    }
                }
            }
            
            String packageName = namespace.substring(0, namespace.lastIndexOf(".")    );
            String typeName    = namespace.substring(   namespace.lastIndexOf(".") + 1);
            
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("userName"      , Activator.getProperty("user.name"));
            root.put("programComment", namespaceComment   );
            root.put("packageName"   , packageName        );
            root.put("typeName"      , typeName           );
            root.put("methodItems"   , methodTemplateList );
            
            String tmplSourceVo = Activator.getTemplateSource("mapper_interface.ftlh", root);
            
            tmplSourceVo = tmplSourceVo.replace("&lt;", "<").replace("&gt;", ">");
            
            sourceTemplate.setPackageName(packageName);
            sourceTemplate.setTypeName   (typeName   );
            sourceTemplate.setSource     (tmplSourceVo);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
    
    // TODO - 표준화 이후 변경
    public static List<FieldTemplate> getMetaInfo(List<String> columnList) {
        
        List<FieldTemplate> resultList = new ArrayList<>();

        if (columnList == null) {
            return resultList;
        }
        
        List<String> queryTermsList = new ArrayList<>();
        queryTermsList.add("SELECT A.TERMS_PHYCS_NAME                      ");
        queryTermsList.add("     , A.TERMS_LOGIC_NAME                      ");
        queryTermsList.add("     , B.DOMAIN_NAME                           ");
        queryTermsList.add("     , B.DOMAIN_DATA_TYPE                      ");
        queryTermsList.add("     , B.DOMAIN_DATA_SIZE                      ");
        queryTermsList.add("     , B.DOMAIN_DATA_SCALE                     ");
        queryTermsList.add("  FROM METADB.DBO.TERMS_DIC  A WITH(NOLOCK)    ");
        queryTermsList.add("  JOIN METADB.DBO.DOMAIN_DIC B WITH(NOLOCK)    ");
        queryTermsList.add("    ON A.DOMAIN_NAME      = B.DOMAIN_NAME      ");
        queryTermsList.add(" WHERE A.TERMS_PHYCS_NAME = ':TERMS_PHYCS_NAME'");  // 바인딩
        
        List<String> queryWordList = new ArrayList<>();
        queryWordList.add("SELECT A.WORD_PHYCS_NAME                     ");
        queryWordList.add("     , A.WORD_LOGIC_NAME                     ");
        queryWordList.add("  FROM METADB.DBO.WORD_DIC A WITH(NOLOCK)    ");
        queryWordList.add(" WHERE A.WORD_PHYCS_NAME = ':WORD_PHYCS_NAME'");  // 바인딩
        
        
        for (String column : columnList) {
            
            try (Statement stmt = Activator.getConnection().createStatement()) {
                
                String    srchColumn = column.trim().replaceAll("\\d+$", "");  // 숫자로 끝나는 경우 제거
                String    endNumber  = column.trim().replace(srchColumn, "");
                          srchColumn = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, srchColumn);  // corpCode -> CORP_CODE
                          
                String    execQuery  = String.join("\n", queryTermsList).replace(":TERMS_PHYCS_NAME", srchColumn);
                ResultSet resultSet  = stmt.executeQuery(execQuery);
                
                FieldTemplate fieldTemplate = new FieldTemplate();
                TableValue tableValue = new TableValue();
                if (resultSet.next()) {
                    //tableValue.COLUMN_NAME        = resultSet.getString("TERMS_PHYCS_NAME" );
                    tableValue.COLUMN_DESCRIPTION = resultSet.getString("TERMS_LOGIC_NAME" ) + endNumber;
                    tableValue.TYPE               = resultSet.getString("DOMAIN_DATA_TYPE" );
                    tableValue.LENGTH             = resultSet.getString("DOMAIN_DATA_SIZE" );
                    tableValue.SCALE              = resultSet.getString("DOMAIN_DATA_SCALE");
                } else {
                    // column = column + "???";
                    // tableValue.COLUMN_DESCRIPTION = "미 등록 용어입니다.";
                    
                    StringBuilder sbDesc = new StringBuilder();
                    
                    // 용어없는경우 단어조합 검색
                    String []splitSrchColumn = srchColumn.split("_");
                    int idx = 0;
                    boolean isNotWord = false;
                    for(String tmpSrchColumn : splitSrchColumn) {
                        execQuery = String.join("\n", queryWordList).replace(":WORD_PHYCS_NAME", tmpSrchColumn);
                        resultSet = stmt.executeQuery(execQuery);
                        if (resultSet.next()) {
                            sbDesc.append(resultSet.getString("WORD_LOGIC_NAME"));
                        } else {
                            
                            isNotWord = true;
                            
                            tmpSrchColumn = tmpSrchColumn.toLowerCase();
                            if (0 < idx) {
                                tmpSrchColumn = StringUtils.capitalize(tmpSrchColumn);
                            }
                            sbDesc.append(tmpSrchColumn);
                        }
                        
                        idx++;
                    }
                    
                    sbDesc.append(endNumber);
                    if (isNotWord) {
                        sbDesc.append(" - 미등록_단어");
                    } else {
                        sbDesc.append(" - 미등록_용어");
                    }
                    
                    tableValue.COLUMN_DESCRIPTION = sbDesc.toString();
                    tableValue.TYPE               = "java.lang.String";
                    tableValue.SCALE              = "0";
                }
                
                fieldTemplate.setName     (column                       );  // 물리명
                fieldTemplate.setComment  (tableValue.COLUMN_DESCRIPTION);  // 논리명
                fieldTemplate.setType     (getJavaType(tableValue)      );  // 타입
                fieldTemplate.setNexaType (getNexaType(tableValue)      );  // 타입
                
                resultList.add(fieldTemplate);
                
            } catch (SQLException e) {
                e.printStackTrace();
                Activator.console(e.toString());
            }
        }
        
        int maxTypeSpace = 0;
        int maxNameSpace = 0;
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
    
    private static List<TableValue> getTableValue(String table) {

        List<TableValue> tableValueList = new ArrayList<>();
        
        try (Statement stmt = Activator.getConnection().createStatement();) {
            Map<String, Object> descRoot = new HashMap<String, Object>();
            descRoot.put("tableName", table);
            
            String descQuery = Activator.getTemplateSource("desc_query.ftlh", descRoot);
            
            ResultSet rs = stmt.executeQuery(descQuery);
            
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
                tableValue.PRIMARYKEY_YN      = rs.getString("PRIMARYKEY_YN"     );
                tableValue.IDENTITY_YN        = rs.getString("IDENTITY_YN"       );
                
                // 자바관련
                tableValue.JAVA_TYPE = getJavaType(tableValue);
                tableValue.JAVA_NAME = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, tableValue.COLUMN_NAME);
                
                tableValueList.add(tableValue);
            }
            
            if (tableValueList.isEmpty()) {
                Activator.console(table + " 테이블 정보를 DB에서 찾지 못했습니다. 테이블이 존재 하는지 확인하세요.");
                Activator.console(descQuery);
                Activator.console("");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            Activator.console(e.toString());
        }
        
        return tableValueList;
    }
    
    public static List<String> createDefaultQuery(List<String> tableList) {
        
        List<String> outList = new ArrayList<>();
        for (String table : tableList ) {
            
            List<TableValue> tableValueList = getTableValue(table.trim());
            
            // 테이블
            if (tableValueList.isEmpty()) {
                continue;
            }
            
            String tableName    = tableValueList.get(0).TABLE_NAME;
            String tableComment = StringUtils.defaultString( tableValueList.get(0).TABLE_DESCRIPTION);
            
            Map<String, Object> root = new HashMap<String, Object>();
            
            List<FieldTemplate> fieldList = getFieldMapping(tableValueList);
            
            root.put("typeName"    , tableName   );
            root.put("tableComment", tableComment);
            root.put("fieldItems"  , fieldList   );
            
            String tmplSource = Activator.getTemplateSource("mapper_xml_cosole.ftlh", root);
            
            outList.add(tmplSource);
        }
        
        return outList;
    }
    
    public static List<String> createDefaultVo(List<String> tableList) {
        
        List<String> outList = new ArrayList<>();
        for (String table : tableList ) {
            
            List<TableValue> tableValueList = getTableValue(table.trim());
            
            // 테이블
            if (tableValueList.isEmpty()) {
                continue;
            }
            
            String tableName    = tableValueList.get(0).TABLE_NAME;
            String tableComment = StringUtils.defaultString( tableValueList.get(0).TABLE_DESCRIPTION);
            
            Map<String, Object> root = new HashMap<String, Object>();
            
            List<FieldTemplate> fieldList = getFieldMapping(tableValueList);
            
            root.put("typeName"    , tableName   );
            root.put("tableComment", tableComment);
            root.put("fieldItems"  , fieldList   );
            
            String tmplSource = Activator.getTemplateSource("table_vo.ftlh", root);
            
            outList.add(tmplSource);
        }
        
        return outList;
    }
    
    public static void createDefaultDBIO(List<String> tableList, List<SourceTemplate> voList, List<SourceTemplate> mapperList) {
        
        for (String table : tableList ) {
            
            List<TableValue> tableValueList = getTableValue(table.trim());
            
            // 테이블
            if (tableValueList.isEmpty()) {
                continue;
            }
            
            // ROOT
            String tableName    = tableValueList.get(0).TABLE_NAME;
            String tableComment = StringUtils.defaultString( tableValueList.get(0).TABLE_DESCRIPTION);
            
            // JAVA 파일 위치 - tis.biz.[업무영역].dbio
            String bizPackage = Activator.getProperty("project.rootPackage") + ".biz.";
            bizPackage       += tableName.substring(tableName.indexOf("TB_") + 3, tableName.indexOf("TB_") + 5).toLowerCase();  // TB_[BC]001M -> bc
            bizPackage       += ".dbio";
            
            // XML 파일 위치  - tis.dbio.[업무영역]
            String resourceDir ="mybatis\\dbio\\";
            resourceDir       += tableName.substring(tableName.indexOf("TB_") + 3, tableName.indexOf("TB_") + 5).toLowerCase();  // TB_[BC]001M -> bc
            
            // XXX - VF 휴양시설 영역 다름
            if ( tableName.contains("TB_VF") ) {
                bizPackage  = Activator.getProperty("project.rootPackage") + ".externaldb.web";
                resourceDir = "mybatis\\externaldb\\web\\";
            }
            
            Map<String, Object> root = new HashMap<String, Object>();
            
            List<FieldTemplate> fieldList = getFieldMapping(tableValueList);
            
            root.put("userName"    , Activator.getProperty("user.name"));
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
            sourceTemplateMapper.setPackageName (resourceDir );
            sourceTemplateMapper.setTypeName    (tableName   );
            sourceTemplateMapper.setTableComment(tableComment);
            sourceTemplateMapper.setSource      (tmplSourceMapper);
            
            // 추가
            voList.add    (sourceTemplateVo    );
            mapperList.add(sourceTemplateMapper);
            
        }
    }
    
    public static List<FieldTemplate> getFieldMapping(List<TableValue> tableValueList) {
        
        List<FieldTemplate> fieldList = new ArrayList<>();
        
        int maxTypeSpace       = 0;
        int maxNameSpace       = 0;
        int maxColumnNameSpace = 0;
        int maxCommentSpace    = 0;
        
        int maxPkNameSpace       = 0;
        int maxPkColumnNameSpace = 0;
        int maxPkCommentSpace    = 0;
        
        try {
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
                if ("Y".equals(table.PRIMARYKEY_YN) ) {
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
            
            // 삭제
            for(TableValue table : tableValueList) {
                FieldTemplate template = new FieldTemplate();
                
                template.setType      (table.JAVA_TYPE                                    );  // 타입
                template.setComment   (StringUtils.defaultString(table.COLUMN_DESCRIPTION));  // 설명
                template.setPkYn      (table.PRIMARYKEY_YN                                );  // PK
                template.setIdentityYn(table.IDENTITY_YN                                  );
                template.setName      (table.JAVA_NAME                                    );  // 필드명
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
                if ("Y".equals(table.PRIMARYKEY_YN) ) {
                    int tmpPkNameSpace       = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, table.COLUMN_NAME).length();
                    int tmpPkColumnNameSpace = table.COLUMN_NAME.length();
                    int tmpPkCommentSpace    = StringUtils.defaultString(table.COLUMN_DESCRIPTION).getBytes("MS949").length;
                    
                    template.setPkNameSpace      (makeSpace(maxPkNameSpace       - tmpPkNameSpace      ));  // 필드명
                    template.setPkColumnNameSpace(makeSpace(maxPkColumnNameSpace - tmpPkColumnNameSpace));  // 컬럼(필드명)
                    template.setPkCommentSpace   (makeSpace(maxPkCommentSpace    - tmpPkCommentSpace   ));  // 설명
                }
                
                fieldList.add(template);
            }
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Activator.console(e.toString());
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
            if ( "0".equals(table.SCALE) ) {
                returnType = "Long";
            } else {
                returnType = "java.math.BigDecimal";
            }
            break;
        
        case "int":
            returnType = "Integer";
            break;
            
        case "datetime":
            returnType = "java.util.Date";
            break;
            
        case "bigint":
        case "java.lang.long":
            returnType = "Long";
            break;
            
        case "java.lang.bigdecimal":
            returnType = "java.lang.BigDecimal";
            break;
        /*
        case "":
            returnType = "Object";
            break;
        */
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
        case "bigint":
        case "java.lang.long":
        case "java.lang.bigdecimal":
            returnType = "BIGDECIMAL";
            break;

        case "datetime":
            returnType = "DATETIME";
            break;
            
        default:
            returnType = "STRING";
            break;
        }
        
        return returnType;
    }
    
    
    
    
    
    
    
//    /**
//     * 
//     * @param query
//     * @param inSourceTemplate
//     * @param outSourceTemplate
//     */
//    @Deprecated
//    public static void getQueryVoFields(XMLQuery query, SourceTemplate inSourceTemplate, SourceTemplate outSourceTemplate) {
//        
//        List<String> inList  = getInputFields (query.getQuery());
//        List<String> outList = getOutputFields(query.getQuery());
//        
//        String paramPackageName = query.getParameterType();
//        String paramTypeName    = query.getParameterType();
//        
//        String resultPackageName = query.getResultType();
//        String resultTypeName    = query.getResultType();
//        
//        if ( query.getParameterType().contains(".") ) {
//            paramPackageName = query.getParameterType().substring(0, query.getParameterType().lastIndexOf(".")    );
//            paramTypeName    = query.getParameterType().substring(   query.getParameterType().lastIndexOf(".") + 1);
//        }
//        
//        if ( query.getResultType().contains(".") ) {
//            resultPackageName = query.getResultType().substring(0, query.getResultType().lastIndexOf(".")    );
//            resultTypeName    = query.getResultType().substring(   query.getResultType().lastIndexOf(".") + 1);
//        }
//        
//        if ( query.getParameterType().equals(query.getResultType()) ) {
//            
//            for (String tmp : outList) {
//                if (!inList.contains(tmp)) {
//                    inList.add(tmp);
//                }
//            }
//            
//            /**
//             * 입력 & 결과
//             */
//            List<FieldTemplate> result = getMetaInfo(inList);
//            
//            Map<String, Object> root = new HashMap<String, Object>();
//            root.put("userName"   , Activator.getProperty("user.name")); 
//            root.put("packageName", paramPackageName   );
//            root.put("typeName"   , paramTypeName      );
//            root.put("fieldItems" , result             );
//            
//            String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
//            
//            inSourceTemplate .setSource(tmplSourceVo  );
//            outSourceTemplate.setSource("입출력 같다.");
//            
//        } else {
//            
//            /**
//             * 입력
//             */
//            if (!StringUtils.isEmpty(query.getParameterType()) ) {
//                List<FieldTemplate> result = getMetaInfo(inList);
//                
//                Map<String, Object> root = new HashMap<String, Object>();
//                root.put("userName"   , Activator.getProperty("user.name"));
//                root.put("packageName", paramPackageName   );
//                root.put("typeName"   , paramTypeName      );
//                root.put("fieldItems" , result             );
//                
//                String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
//                
//                inSourceTemplate.setSource(tmplSourceVo);
//            }
//        
//            /**
//             * 결과
//             */
//            if (!StringUtils.isEmpty(query.getResultType()) ) {
//                List<FieldTemplate> result = getMetaInfo(outList);
//                
//                Map<String, Object> root = new HashMap<String, Object>();
//                root.put("userName"   , Activator.getProperty("user.name"));
//                root.put("packageName", resultPackageName  );
//                root.put("typeName"   , resultTypeName     );
//                root.put("fieldItems" , result             );
//                
//                String tmplSourceVo = Activator.getTemplateSource("query_vo.ftlh", root);
//                outSourceTemplate.setSource(tmplSourceVo);
//            }
//        }
//    }
//    
//    /**
//     * 지정옵셋으로 부터 쿼리 추출
//     * @param filePath
//     * @param offset
//     * @return
//     */
//    @Deprecated
//    public static XMLQuery getQueryAtOffset(String filePath, int offset) {
//        
//        XMLInputFactory factory  = XMLInputFactory.newInstance();
//        List<String>    textList = null;
//        
//        XMLQuery xmlQuery = new XMLQuery();
//        
//        // XML 파일명
//        // xmlQuery.setFileName(filePath.substring(filePath.lastIndexOf("\\") + 1));
//        
//        try (FileReader reader = new FileReader(filePath)) {
//            
//            XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
//            
//            String resultType    = "";
//            String parameterType = "";
//            while (streamReader.hasNext()) {
//                int evt = streamReader.next();
//                
//                // 시작점
//                if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT ) {
//                    if ( "select".equals( streamReader.getName().toString())
//                      || "insert".equals( streamReader.getName().toString())
//                      || "update".equals( streamReader.getName().toString())
//                      || "delete".equals( streamReader.getName().toString())
//                    ) {
//                        parameterType = getAttr(streamReader, "parameterType");
//                        resultType    = getAttr(streamReader, "resultType"   );
//                        
//                        textList = new ArrayList<String>();
//                    }
//                }
//                
//                if (textList != null) {
//                    if ( evt == XMLEvent.CHARACTERS) {
//                        textList.add(streamReader.getText());
//                    }
//                }
//                
//                if (streamReader.getEventType() == XMLStreamReader.END_ELEMENT ) {
//                    if ( "select".equals( streamReader.getName().toString())
//                      || "insert".equals( streamReader.getName().toString())
//                      || "update".equals( streamReader.getName().toString())
//                      || "delete".equals( streamReader.getName().toString())
//                    ) {
//                        Location location = streamReader.getLocation();
//                        if ( offset < location.getCharacterOffset() ) {
//                            xmlQuery.setParameterType(parameterType);
//                            xmlQuery.setResultType   (resultType   );
//                            break;
//                        }
//                    }
//                }
//            }
//            
//        } catch (Exception e) {
//            Activator.console(e);
//        } 
//        
//        if ( textList != null) {
//            xmlQuery.setQuery(String.join("", textList));
//        }
//        return xmlQuery;
//    }
    
    
    // https://mirwebma.tistory.com/181
/*
SELECT D.COLORDER                 AS COLUMN_IDX            -- Column Index                           
 , A.NAME                     AS TABLE_NAME            -- Table Name                             
 , C.VALUE                    AS TABLE_DESCRIPTION     -- Table Description                      
 , D.NAME                     AS COLUMN_NAME           -- Column Name                            
 , E.VALUE                    AS COLUMN_DESCRIPTION    -- Column Description                     
 , F.DATA_TYPE                AS TYPE                  -- Column Type                            
 , F.CHARACTER_OCTET_LENGTH   AS LENGTH                -- Column Length                          
 , F.NUMERIC_SCALE            AS SCALE                 -- Column SCALE                           
 , F.IS_NULLABLE              AS IS_NULLABLE           -- Column Nullable                        
 , F.COLLATION_NAME           AS COLLATION_NAME        -- Column Collaction Name                 
 , ( SELECT COALESCE(MAX('Y'), 'N')                                                              
       FROM [:DB_NAME].INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE                                
      WHERE TABLE_NAME = A.NAME                                                                  
        AND CONSTRAINT_NAME = (                                                                  
            SELECT CONSTRAINT_NAME                                                               
              FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS                                          
             WHERE TABLE_NAME = A.NAME                                                           
               AND CONSTRAINT_TYPE = 'PRIMARY KEY'                                               
               AND COLUMN_NAME = D.NAME                                                          
        )                                                                                        
 ) AS PRIMARYKEY_YN                                                                              
 , CASE WHEN COLUMNPROPERTY(A.ID, D.NAME, 'isIdentity') = 1 THEN 'Y' ELSE 'N' END AS IDENTITY_YN 
FROM SYSOBJECTS A WITH (NOLOCK)                                                                   
INNER JOIN SYSUSERS B WITH (NOLOCK)          ON A.UID = B.UID                                      
INNER JOIN SYSCOLUMNS D WITH (NOLOCK)        ON D.ID = A.ID                                        
INNER JOIN INFORMATION_SCHEMA.COLUMNS F WITH (NOLOCK)                                              
 ON A.NAME = F.TABLE_NAME                                                                        
AND D.NAME = F.COLUMN_NAME                                                                       
LEFT OUTER JOIN SYS.EXTENDED_PROPERTIES C WITH (NOLOCK)                                           
 ON C.MAJOR_ID = A.ID                                                                            
AND C.MINOR_ID = 0                                                                               
AND C.NAME = 'MS_Description'                                                                    
LEFT OUTER JOIN SYS.EXTENDED_PROPERTIES E WITH (NOLOCK)                                           
 ON E.MAJOR_ID = D.ID                                                                            
AND E.MINOR_ID = D.COLID                                                                         
AND E.NAME = 'MS_Description'                                                                    
WHERE 1=1                                                                                          
AND A.TYPE = 'U'                                                                                 
AND A.NAME = ':TABLE_NAME'                                                                       
ORDER BY D.COLORDER                                                                                
*/
}
