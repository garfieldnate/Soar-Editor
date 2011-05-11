package edu.umich.soar.editor.editors.datamap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

public class DatamapEditor extends MultiPageEditorPart {

	List<DatamapTreeEditor> treeEditors = new ArrayList<DatamapTreeEditor>();
	Datamap datamap;
	
	public DatamapEditor()
	{
		super();
	}
	
	@Override
	protected void createPages() {
		try {
			datamap = Datamap.read(((FileEditorInput)getEditorInput()).getFile(), this);
			List<DatamapNode> stateNodes = datamap.getStateNodes();
			int i = 0;
			for (DatamapNode stateNode : stateNodes)
			{
				DatamapTreeEditor treeEditor = new DatamapTreeEditor(datamap, stateNode);
				addPage(treeEditor, getEditorInput());
				setPageText(i++, stateNode.tabName());
				treeEditors.add(treeEditor);
			}
			addPage(new TextEditor(), getEditorInput());
			setPageText(getPageCount() - 1, "Raw Text");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IEditorInput input = getEditorInput();
		IFile file = ((FileEditorInput)input).getFile();
		boolean wrote = datamap.writeToFile(file, monitor);
		if (wrote)
		{
			for (int i = 0; i < getPageCount() - 1; ++i)
			{
				IEditorPart part = getEditor(i);
				((DatamapTreeEditor) part).setDirty(false);
			}
		}
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
		if (!(input instanceof FileEditorInput))
		{
			throw new PartInitException("Editor input not instanceof FileEditorInput.");
		}
		setTitle(input.getName());
	}
	
	public void contentChanged(Object changed)
	{
		for (int i = 0; i < getPageCount(); ++i)
		{
			IEditorPart part = getEditor(i);
			if (part instanceof DatamapTreeEditor)
			{
				((DatamapTreeEditor)part).contentChanged(changed);
			}
		}
	}
}
