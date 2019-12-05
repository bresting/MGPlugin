package mgplugin.template;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * 패키지 구조에 따라 class component annotation 템플릿을 설정한다.
 * @author jakekim84
 * @since  2019.11.25
 */
public class ComponentResolver extends TemplateVariableResolver {
    
    public ComponentResolver() {
        super("MG_component", "패키지 구조에 따라 component(@Controller|@Service) 설정");
    }
    
    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
    	
        /**
	     * ${package_declaration}
         * ${type_declaration}
         * 
         * typecomment=...
         * type_name=RT1000
         * file_name=RT1000.java
         * package_name=controller
         * filecomment=...
         * project_name=Test
         * type_declaration=public class RT1000{ }
         * package_declaration=package controller;
         */
    	
        String package_name   = context.getVariable("package_name");
        List<String> textList = new ArrayList<String>();
        
        if ( package_name.contains("controller") ) {
        	textList.add("");
            textList.add("@Controller");
        } else if ( package_name.contains("service") ) {
        	textList.add("");
            textList.add("@Service");
        }
        variable.setValue(String.join(System.getProperty("line.separator"), textList));
    }
}
