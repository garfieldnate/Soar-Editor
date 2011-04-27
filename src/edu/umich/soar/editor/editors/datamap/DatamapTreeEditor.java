package edu.umich.soar.editor.editors.datamap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import edu.umich.soar.editor.editors.datamap.actions.DatamapDragAdapter;
import edu.umich.soar.editor.editors.datamap.actions.DatamapDropAdapter;

public class DatamapTreeEditor extends EditorPart {

	private Composite parent;
	private TreeViewer tree;
	private Datamap datamap;
	private boolean dirty;
	
	public DatamapTreeEditor()
	{
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		IEditorInput input = getEditorInput();
		IFile file = ((FileEditorInput)input).getFile();
		boolean wrote = datamap.writeToFile(file, monitor);
		if (wrote)
		{
			setDirty(false);
		}
	}

	@Override
	public void doSaveAs() {
		doSave(null);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof FileEditorInput))
		{
			throw new PartInitException("Editor input not instanceof FileEditorInput.");
		}
		setSite(site);
		setInput(input);
		datamap = Datamap.read(((FileEditorInput)input).getFile(), this);
		if (datamap == null)
		{
			throw new PartInitException("File format invalid, " + input.getName());
		}
		dirty = false;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	private void setDirty(boolean dirty)
	{
		this.dirty = dirty;
		firePropertyChange(PROP_DIRTY);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		tree = new TreeViewer(parent); // , SWT.FULL_SELECTION /* | SWT.MULTI */ | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		//tree.getTree().setHeaderVisible(true);
		//tree.getTree().setLinesVisible(true);
        
		/*
		TreeColumn leftColumn = new TreeColumn(tree.getTree(), SWT.NONE);
		
		leftColumn.setWidth(500);
		leftColumn.setResizable(true);
		leftColumn.setText("Datamap Nodes");
		*/
		/*
		TreeColumn rightColumn = new TreeColumn(tree.getTree(), SWT.NONE);
		rightColumn.setWidth(200);
		rightColumn.setResizable(true);
		rightColumn.setText("Comments");
			*/
		
        // getSite().setSelectionProvider(tree);

		tree.setContentProvider(datamap);
		tree.setLabelProvider(new SoarDatamapLabelProvider());
		tree.setInput(datamap);
		
		/*
		tree.getControl().addKeyListener(new org.eclipse.swt.events.KeyListener() {
			
			@Override
			public void keyReleased(org.eclipse.swt.events.KeyEvent event) {
				
			}
			
			@Override
			public void keyPressed(org.eclipse.swt.events.KeyEvent event) {

			}
		});
		*/
		
		/*
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					// selectedRows = (IStructuredSelection) selection;
				}
			}
		});
		*/

		MenuManager manager = new MenuManager();
		manager.addMenuListener(new DatamapRightClickMenuListener(tree));
		Menu menu = manager.createContextMenu(tree.getTree());
		tree.getTree().setMenu(menu);
		
		tree.addDragSupport(DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] {LocalSelectionTransfer.getTransfer()}, new DatamapDragAdapter());
        tree.addDropSupport(DND.DROP_MOVE | DND.DROP_LINK, new Transfer[] {LocalSelectionTransfer.getTransfer()}, new DatamapDropAdapter(tree));
	}

	@Override
	public void setFocus() {
		
	}
	
	public void contentChanged(Object changed)
	{
		setDirty(true);
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					TreePath[] paths = tree.getExpandedTreePaths();
					tree.refresh();
					tree.setExpandedTreePaths(paths);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}
}
