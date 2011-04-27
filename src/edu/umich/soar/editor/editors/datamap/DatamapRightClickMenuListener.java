package edu.umich.soar.editor.editors.datamap;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionGroup;

import edu.umich.soar.editor.editors.datamap.Datamap.DatamapAttribute;
import edu.umich.soar.editor.editors.datamap.Datamap.NodeType;
import edu.umich.soar.editor.editors.datamap.actions.CreateChildAction;
import edu.umich.soar.editor.editors.datamap.actions.DeleteAttributeAction;
import edu.umich.soar.editor.editors.datamap.actions.RenameAttributeAction;

public class DatamapRightClickMenuListener implements IMenuListener {

	private TreeViewer tree;
	
	public DatamapRightClickMenuListener(TreeViewer tree)
	{
		this.tree = tree;
	}
	
	@Override
	public void menuAboutToShow(IMenuManager manager) {
		manager.removeAll();
		ISelection selection = tree.getSelection();
		if (!(selection instanceof IStructuredSelection)) return;
		IStructuredSelection ss = (IStructuredSelection) selection;
		Object element = ss.getFirstElement();
		if (element instanceof DatamapAttribute)
		{
			DatamapAttribute attr = (DatamapAttribute) element;
			manager.add(new RenameAttributeAction(attr));
			manager.add(new DeleteAttributeAction(attr));
			MenuManager addChildManager = new MenuManager("Add Child");
			
			if (attr.getTarget().type == NodeType.SOAR_ID)
			{
				for (NodeType nodeType : NodeType.values())
				{
					addChildManager.add(new CreateChildAction(attr, nodeType));
				}
				manager.add(addChildManager);
			}
		}
	}
}
