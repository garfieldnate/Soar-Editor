package edu.umich.soar.editor.editors.datamap.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.umich.soar.editor.editors.datamap.Datamap.DatamapAttribute;

public class DeleteAttributeAction extends Action {

	private DatamapAttribute attribute;
	
	public DeleteAttributeAction(DatamapAttribute attribute)
	{
		super("Delete");
		this.attribute = attribute;
	}
	
	@Override
	public void run() {
		attribute.delete();
	}
}
