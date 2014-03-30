package edu.umich.soar.editor.wizards;

import org.eclipse.osgi.util.NLS;

public class WizardMessages extends NLS {
	private static final String BUNDLE_NAME = "edu.umich.soar.editor.wizards.WizardMessages"; //$NON-NLS-1$
	public static String SoarProjectWizard_Description_Create_Soar_Project;
	public static String SoarProjectWizard_Title_New_Soar_Project;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, WizardMessages.class);
	}

	private WizardMessages() {
	}
}
