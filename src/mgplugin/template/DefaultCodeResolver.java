package mgplugin.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * 패키지 구조에 따라 class component annotation 템플릿을 설정한다.
 * @author jakekim84
 * @since  2019.11.26
 */
public class DefaultCodeResolver extends TemplateVariableResolver {
    public DefaultCodeResolver() {
        super("MG_defaultCode", "패키지 구조에 따라 기본코드 설정");
    }

    public void resolve(TemplateVariable variable, TemplateContext context) {
        
        String       package_name = context.getVariable("package_name");
        String       type_name    = context.getVariable("type_name");
        List<String> textList     = new ArrayList<String>();
        
        if ( package_name.contains("controller") ) {
            textList.add("private static final Logger logger = LoggerFactory.getLogger(" + type_name + ".class);");
        } else if ( package_name.contains("service") ) {
            textList.add("private static final Logger logger = LoggerFactory.getLogger(" + type_name + ".class);");
        }
        
        variable.setValue(String.join(System.getProperty("line.separator"), textList));
    }
}
