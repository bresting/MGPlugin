package mgplugin.template;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import mgplugin.Activator;

/**
 * 패키지 구조에 따라 기본 샘플코드 템플릿을 설정한다.
 * @author jakekim84
 * @since  2019.11.25
 */
public class SampleCodeResolver extends TemplateVariableResolver {
    
    public SampleCodeResolver() {
        super("MG_sampleCode", "새마을금고 component sample code template");
    }
    
    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
        
        String type_name      = context.getVariable("type_name");
        String package_name   = context.getVariable("package_name");
        
        String baseTypeName = type_name;
        if ( 3 < type_name.length() ) {
            baseTypeName = type_name.substring(0, type_name.length() - 3);
        }
        
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("baseTypeName"  , baseTypeName       );
        root.put("typeName"      , type_name          );
        String templateSource = "";
        
        if ( package_name.contains(".controller") ) {
            
            /*
            @Autowired
            EgovSampleServiceImpl svc;
            
            @RequestMapping(value = "/selectSampleListWithMap.do")
            public NexacroResult selectVo(
                      @ParamDataSet(name = "ds_search"    , required = false) SampleVo       searchInfo
                    , @ParamDataSet(name = "ds_searchList", required = false) List<SampleVo> searchInfoList
            ) {
                ...
            }
            */
            
            templateSource = Activator.getTemplateSource("sampleCtrl.ftlh", root);
            
        } else if ( package_name.contains(".service") ) {
            templateSource = Activator.getTemplateSource("sampleSvc.ftlh", root);
        } else {
            // @EMPTY_BLOCK_OK
        }

        variable.setValue(templateSource);
    }
    
    public static void main(String[] args) {
        
        Activator.initThisPlugin("C:\\eclipse_rcp\\workspace\\.metadata\\mgplugin");
        
        TemplateVariable variable = new TemplateVariable("", "",new int[] {0});
        
        TemplateContext context = new TemplateContext(new TemplateContextType()) {
            @Override
            public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
                return null;
            }
            
            @Override
            public boolean canEvaluate(Template template) {
                return false;
            }
        };
        
        context.setVariable("type_name"   , "RT1000Ctr");
        context.setVariable("package_name", "tis.bc.service");
        //context.setVariable("package_name", "tis.bc.controller");
        
        SampleCodeResolver rsv = new SampleCodeResolver();
        rsv.resolve(variable, context);
        
        
        System.out.println(variable.getValues()[0]);
        
        Activator.closeThisPlugin();
        
    }
    
}
