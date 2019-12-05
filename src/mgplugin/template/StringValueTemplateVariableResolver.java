package mgplugin.template;

import org.eclipse.core.variables.IValueVariable;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class StringValueTemplateVariableResolver extends TemplateVariableResolver{

    /** @see #resolve(TemplateContext) */
    private final IValueVariable variable;

    /**
     * The constructor.
     * 
     * @param variable is the {@link IValueVariable}.
     */
    public StringValueTemplateVariableResolver(IValueVariable variable) {
        super(variable.getName(), variable.getDescription());
        this.variable = variable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String resolve(TemplateContext context) {
        return this.variable.getValue();
    }
}
