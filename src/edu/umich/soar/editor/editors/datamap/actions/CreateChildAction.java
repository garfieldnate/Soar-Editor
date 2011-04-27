package edu.umich.soar.editor.editors.datamap.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.umich.soar.editor.editors.datamap.Datamap.DatamapAttribute;
import edu.umich.soar.editor.editors.datamap.Datamap.NodeType;

public class CreateChildAction extends Action {

	private DatamapAttribute attribute;
	private NodeType nodeType;
	
	public CreateChildAction(DatamapAttribute attribute, NodeType nodeType)
	{
		super(nodeType.toString());
		this.attribute = attribute;
		this.nodeType = nodeType;
	}
	
	@Override
	public void run() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String title = "Add Child " + nodeType.toString();
		String message = "Enter Name:";
		String initialValue = attribute.name;
		InputDialog dialog = new InputDialog(shell, title, message, initialValue, null);
		dialog.open();
		String result = dialog.getValue();
		if (result != null && result.length() > 0) {
			attribute.getTarget().addChild(result, nodeType);
		}
	}
}
