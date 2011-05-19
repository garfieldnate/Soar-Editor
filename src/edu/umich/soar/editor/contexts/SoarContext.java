package edu.umich.soar.editor.contexts;

import org.eclipse.jface.text.templates.TemplateContextType;

import edu.umich.soar.editor.contexts.SoarTemplateResolver.Variable;

public class SoarContext extends TemplateContextType {

	public static final String CONTEXT_ID = "edu.umich.soar.editor.contexts.SoarContext";

	public SoarContext()
	{
		for (Variable variable : SoarTemplateResolver.Variable.values())
		{
			addResolver(new SoarTemplateResolver(variable));
		}
	}

}
