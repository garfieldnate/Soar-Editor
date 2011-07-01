package edu.umich.soar.editor.editors.datamap.actions;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TreeItem;

import edu.umich.soar.editor.editors.datamap.DatamapAttribute;
import edu.umich.soar.editor.editors.datamap.DatamapNode;
import edu.umich.soar.editor.editors.datamap.DatamapNode.NodeType;

public class DatamapDropAdapter extends ViewerDropAdapter {

	DatamapAttribute target;
	
	public DatamapDropAdapter(Viewer viewer) {
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data) {
		if (!(data instanceof StructuredSelection)) {
			return false;
		}
		
		StructuredSelection ss = (StructuredSelection) data;
		Object dataObj = ss.getFirstElement();
		
		if (!(dataObj instanceof TreeItem)) {
			return false;
		}
		
		TreeItem treeItem = (TreeItem) dataObj;
		Object draggedObject = treeItem.getData();
		DatamapAttribute draggedNode = null;

		if (draggedObject instanceof DatamapAttribute)
		{
			draggedNode = (DatamapAttribute) draggedObject;
		}
		
		if (draggedNode == null) {
			return false;
		}
		
		int operation = getCurrentOperation();
		
		if (operation == DND.DROP_MOVE) {
			if (!childCanBeMovedToParent(draggedNode, target)) {
				return false;
			}
			draggedNode.setFrom(target.to);
		}
		
		else if (operation == DND.DROP_LINK)
		{
			if (!childCanBeMovedToParent(draggedNode, target)) {
				return false;
			}
			DatamapNode original = draggedNode.getTarget();
			DatamapNode newParent = target.getTarget();
			if (newParent.type != NodeType.SOAR_ID)
			{
				return false;
			}
			newParent.addLink(draggedNode.name, original);
		}
		
		/*	
		 else if (operation == DND.DROP_LINK) {
			SoarDatabaseRow dataRow = (SoarDatabaseRow) treeItem.getData();
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			String title = "Link items?";
			org.eclipse.swt.graphics.Image image = shell.getDisplay().getSystemImage(SWT.ICON_QUESTION);
			String message = "Link items \"" + dataRow.getName() + "\" and \"" + target.getName() + "\"?";
			String[] labels = new String[] { "OK", "Cancel" };
			MessageDialog dialog = new MessageDialog(shell, title, image, message, MessageDialog.QUESTION, labels, 0);
			int result = dialog.open();
			if (result != 0) {
				return false;
			}

			LinkDatamapRowsAction action = new LinkDatamapRowsAction(dataRow, target);
			action.run();
		}
		*/
		
		return true;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData targetType) {
		this.target = null;
		
		/*
		
		System.out.println("Validate w/ operation: " + operation);
		
		Object obj = super.getSelectedObject();
		if (!(obj instanceof SoarDatabaseRow)) {
			return false;
		}
		SoarDatabaseRow first = (SoarDatabaseRow) obj;
		if (!first.isDatamapNode()) {
			return false;
		}
		if (!(target instanceof SoarDatabaseRow)) {
			return false;
		}
		SoarDatabaseRow second = (SoarDatabaseRow) target;
		if (!second.isDatamapNode()) {
			return false;
		}
		
		// first and second are both datamap rows
		
		if (operation == DND.DROP_MOVE) {
			// Move operation
			// Make sure second is a possible parent of first.
			if (!childCanBeMovedToParent(first, second)) {
				return false;
			}
		}
		*/
		/*
		else if (operation == DND.DROP_LINK) {
			// Link operation
			// Make sure they are both identifiers
			if (first.getTable() != Table.DATAMAP_IDENTIFIERS
					|| first.getTable() != second.getTable()) {
				return false;
			}
		}
		*/
		if (target instanceof DatamapAttribute)
		{
			DatamapAttribute attrTarget = (DatamapAttribute) target;
			if (attrTarget.getTarget().type == NodeType.SOAR_ID)
			{
				this.target = attrTarget;
				return true;
			}
		}
		return false;
	}
	
	private boolean childCanBeMovedToParent(DatamapAttribute child, DatamapAttribute parent) {
		if (child == null || parent == null) return false;
		return true;
	}
}
