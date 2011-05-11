package edu.umich.soar.editor.editors.datamap.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.umich.soar.editor.editors.datamap.DatamapAttribute;
import edu.umich.soar.editor.editors.datamap.DatamapNode;
import edu.umich.soar.editor.editors.datamap.DatamapNode.NodeType;

public class CreateChildAction extends Action {

	private DatamapNode node = null;
	private NodeType nodeType;
	
	public CreateChildAction(DatamapAttribute attribute, NodeType nodeType)
	{
		super(nodeType.getName());
		this.node = attribute.getTarget();
		this.nodeType = nodeType;
	}
	
	public CreateChildAction(DatamapNode node, NodeType nodeType)
	{
		super(nodeType.getName());
		this.node = node;
		this.nodeType = nodeType;
	}
	
	@Override
	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String title = "Add Child " + nodeType.toString();
		String message = "Enter Name:";
		String initialValue = "";
		InputDialog dialog = new InputDialog(shell, title, message, initialValue, null);
		dialog.open();
		String result = dialog.getValue();
		if (result != null && result.length() > 0) {
			node.addChild(result, nodeType);
		}
	}
}
