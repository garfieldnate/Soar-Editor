package edu.umich.soar.editor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import edu.umich.soar.editor.editors.datamap.Datamap;

public class SoarEditor extends TextEditor {
	
	private ColorManager colorManager;
	private List<IFile> datamapFiles;
	private List<Datamap> datamaps;

	public SoarEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new SoarConfiguration(colorManager, this));
		setDocumentProvider(new SoarDocumentProvider());
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		findDatamaps(input);
	}
	
	public List<Datamap> getDatamaps()
	{
		if (datamaps == null)
		{
			buildDatamaps();
		}
		return datamaps;
	}
	
	private void buildDatamaps()
	{
		datamaps = new ArrayList<Datamap>();
		for (IFile file : datamapFiles)
		{
			Datamap datamap = Datamap.read(file, null);
			if (datamap != null)
			{
				datamaps.add(datamap);
			}
		}
	}
	
	private void findDatamaps(IEditorInput input)
	{
		datamapFiles = new ArrayList<IFile>();
		if (!(input instanceof FileEditorInput))
		{
			return;
		}
		IFile file = ((FileEditorInput)input).getFile();
		IContainer parent = file.getParent();
		if (parent == null) return;
		try {
			for (IResource member : parent.members())
			{
				if (member instanceof IFile && member.getFileExtension().equalsIgnoreCase("dm"))
				{
					datamapFiles.add((IFile)member);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		if (datamapFiles.size() != 0) return;
		parent = parent.getParent();
		if (parent == null) return;
		try {
			for (IResource member : parent.members())
			{
				if (member instanceof IFile && member.getFileExtension().equalsIgnoreCase("dm"))
				{
					datamapFiles.add((IFile)member);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
}
