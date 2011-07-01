package edu.umich.soar.editor.contexts;

import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;

public class SoarTemplateResolver extends SimpleTemplateVariableResolver {

	public static enum Variable
	{
		PROBLEMSPACE,
		OPERATOR;
		
		public String getName()
		{
			return toString().toLowerCase();
		}
	}
	
	private Variable variable;
	
	public SoarTemplateResolver(Variable variable) {
		super("Soar Template Resolver", "Resolver for Soar rule templates.");
		this.variable = variable;
		setEvaluationString(variable.getName());
	}

}
