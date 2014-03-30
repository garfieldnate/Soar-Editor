package edu.umich.soar.editor.wizards;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import edu.umich.soar.editor.projects.SoarProjectSupport;

/**
 * Short wizard for creating a new Soar project.
 * @author Nate Glenn
 *
 */
public class SoarProjectWizard extends Wizard implements INewWizard, IExecutableExtension {
	private WizardNewProjectCreationPage pageOne;
	private IConfigurationElement configurationElement;

	private static final String WIZARD_NAME = "Soar Project Wizard"; //$NON-NLS-1$
	private static final String PAGE_NAME = "Soar Project"; //$NON-NLS-1$
	
	public SoarProjectWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	/**
	 * There's only one page in this wizard, and it simply allows the user to 
	 * create a new project with a given name.
	 */
	@Override
	public void addPages() {
		super.addPages();

		pageOne = new WizardNewProjectCreationPage(
				PAGE_NAME);
		pageOne.setTitle(WizardMessages.SoarProjectWizard_Title_New_Soar_Project);
		pageOne.setDescription(WizardMessages.SoarProjectWizard_Description_Create_Soar_Project);

		addPage(pageOne);
	}

	/**
	 * Create the default project structure and update the perspective
	 */
	@Override
	public boolean performFinish() {
	    String name = pageOne.getProjectName();
	    URI location = null;
	    if (!pageOne.useDefaults()) {
	        location = pageOne.getLocationURI();
	    } // else location == null
	 
	    SoarProjectSupport.createProject(name, location);
	    BasicNewProjectResourceWizard.updatePerspective(configurationElement);
	    return true;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		configurationElement = config;
		
	}

}
