package edu.umich.soar.editor.wizards;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class SoarFileWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;
    private SoarFileWizardPage page;
    private SoarFileTemplatesWizardPage templatesPage;
    private IWorkbench workbench;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
        setWindowTitle("New Soar File");
        this.workbench = workbench;
        this.selection = selection;
	}
	
    @Override
    public void addPages() {
        page = new SoarFileWizardPage(selection, this);
        addPage(page);
        templatesPage = new SoarFileTemplatesWizardPage(this);
        addPage(templatesPage);
    }

	@Override
	public boolean performFinish() {
		IFile file = page.createNewFile();
        if (file != null)
            return true;
        else
            return false;
    }

	public String getFileName() {
		// TODO Auto-generated method stub
		return page.getFileName();
	}
	
	public String getFolderName()
	{
		return page.getContainerFullPath().lastSegment();
	}

	public String getTemplatesString() {
		return templatesPage.getTemplates();
	}
}
