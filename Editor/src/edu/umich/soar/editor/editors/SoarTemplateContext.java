package edu.umich.soar.editor.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;

public class SoarTemplateContext extends TemplateContext
{
    
    public static final String ID = "edu.umich.soar.editor.editors.SoarTemplateContext";

    protected SoarTemplateContext(TemplateContextType contextType)
    {
        super(contextType);
    }

    @Override
    public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canEvaluate(Template template)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
