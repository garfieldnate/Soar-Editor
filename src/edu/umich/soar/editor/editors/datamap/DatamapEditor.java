package edu.umich.soar.editor.editors.datamap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

public class DatamapEditor extends MultiPageEditorPart {

	DatamapTreeEditor treeEditor;
	
	public DatamapEditor()
	{
		super();
	}
	
	@Override
	protected void createPages() {
		try {
			treeEditor = new DatamapTreeEditor();
			addPage(treeEditor, getEditorInput());
			addPage(new TextEditor(), getEditorInput());
			setPageText(0, "Datamap Tree");
			setPageText(1, "Raw Text");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		treeEditor.doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		doSave(null);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		setTitle(input.getName());
	}
}
