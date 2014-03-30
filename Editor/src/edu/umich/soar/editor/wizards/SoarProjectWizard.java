package edu.umich.soar.editor.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * Short wizard for creating a new Soar project.
 * @author Nate Glenn
 *
 */
public class SoarProjectWizard extends Wizard implements INewWizard {
	private WizardNewProjectCreationPage pageOne;

	private static final String WIZARD_NAME = "Soar Project Wizard"; //$NON-NLS-1$
	private static final String PAGE_NAME = "Soar Project"; //$NON-NLS-1$
	
	public SoarProjectWizard() {
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPages() {
		super.addPages();

		pageOne = new WizardNewProjectCreationPage(
				PAGE_NAME);
		pageOne.setTitle(WizardMessages.SoarProjectWizard_Title_New_Soar_Project);
		pageOne.setDescription(WizardMessages.SoarProjectWizard_Description_Create_Soar_Project);

		addPage(pageOne);
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return true;
	}

}
