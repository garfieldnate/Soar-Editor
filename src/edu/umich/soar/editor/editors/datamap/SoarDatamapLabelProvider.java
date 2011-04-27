package edu.umich.soar.editor.editors.datamap;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class SoarDatamapLabelProvider extends LabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object obj, int column) {
		return null;
	}

	@Override
	public String getColumnText(Object obj, int column) {
		if (obj == null) return "NULL";
		return obj.toString();
	}

}
