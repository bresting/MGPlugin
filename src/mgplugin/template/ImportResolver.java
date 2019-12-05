package mgplugin.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * 패키지 구조에 따라 import 템플릿을 설정한다.
 * @author jakekim84
 * @since  2019.11.25
 */
public class ImportResolver extends TemplateVariableResolver {
    
    public ImportResolver() {
        super("MG_import", "패키지 구조에 따라 import 설정");
    }
    
    public void resolve(TemplateVariable variable, TemplateContext context) {
        
        String       package_name = context.getVariable("package_name");
        List<String> textList     = new ArrayList<String>();
        
        if ( package_name.contains("controller") ) {
        	textList.add("");
        	textList.add("");
        	textList.add("import org.slf4j.Logger;");
        	textList.add("import org.slf4j.LoggerFactory;");
        	textList.add("");
            textList.add("import org.springframework.stereotype.Controller;");
            //textList.add("import org.springframework.web.bind.annotation.RequestMapping;");
            
        } else if ( package_name.contains("service") ) {
        	textList.add("");
        	textList.add("");
        	textList.add("import org.slf4j.Logger;");
        	textList.add("import org.slf4j.LoggerFactory;");
        	textList.add("");
            textList.add("import org.springframework.stereotype.Service;");
        }
        
        variable.setValue(String.join(System.getProperty("line.separator"), textList));
    }
}
