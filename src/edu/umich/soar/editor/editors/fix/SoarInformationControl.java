package edu.umich.soar.editor.editors.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;

import edu.umich.soar.editor.editors.SoarEditor;
import edu.umich.soar.editor.editors.datamap.Correction;

public class SoarInformationControl extends AbstractInformationControl implements IInformationControlExtension2
{

    private SoarEditor editor;
    private Object input;
    private Label label;
    private Composite parent;

    public SoarInformationControl(SoarEditor editor, Shell shell)
    {
        super(shell, true);
        this.editor = editor;
        create();
    }

    @Override
    public boolean hasContents()
    {
        return input != null;
    }

    @Override
    public void setInput(Object input)
    {
        this.input = input;
        if (input instanceof String)
        {
            label.setText((String) input);
        }
        else if (input instanceof MarkerAnnotation)
        {
            MarkerAnnotation annotation = (MarkerAnnotation) input;
            IMarker marker = annotation.getMarker();
            String text = (marker.getAttribute(IMarker.MESSAGE, ""));
            label.setText(text);
            Correction correction = editor.findCorrection(marker);
            if (correction != null)
            {
                for (int i = 0; i < correction.getNumSolutions(); ++i)
                {
                    Link link = new Link(parent, SWT.NONE);
                    link.setText("<a>" + correction.getSolutionText(editor, i) + "</a>");
                    link.setBackground(parent.getBackground());
                    link.setForeground(parent.getForeground());
                    link.addSelectionListener(correction);
                    link.setData(new Integer(i));
                }
            }
        }
    }

    @Override
    protected void createContent(Composite parent)
    {
        this.parent = parent;
        parent.setLayout(new GridLayout(1, false));
        setBackgroundColor(parent.getBackground());
        setForegroundColor(parent.getForeground());
        label = new Label(parent, SWT.NONE);
        label.setBackground(parent.getBackground());
        label.setForeground(parent.getForeground());

        // new Button(parent, 0);
    }

    @Override
    public IInformationControlCreator getInformationPresenterControlCreator()
    {
        return new IInformationControlCreator()
        {

            @Override
            public IInformationControl createInformationControl(Shell shell)
            {
                return new SoarInformationControl(editor, shell);
            }
        };
    }
}
