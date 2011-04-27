package edu.umich.soar.editor.editors;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SoarConfiguration extends SourceViewerConfiguration {
	
	// private XMLDoubleClickStrategy doubleClickStrategy;
	// private XMLTagScanner tagScanner;
	private SoarScanner scanner = new SoarScanner();
	SoarAutoEditStrategy strategy = new SoarAutoEditStrategy();
	private SoarEditor editor;
	// private ColorManager colorManager;

	public SoarConfiguration(ColorManager colorManager, SoarEditor editor) {
		// this.colorManager = colorManager;
		this.editor = editor;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			XMLPartitionScanner.XML_COMMENT,
			XMLPartitionScanner.XML_TAG };
	}
	
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) 
	{
		return new IAutoEditStrategy[] {strategy};
	}
	
	/*
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new XMLDoubleClickStrategy();
		return doubleClickStrategy;
	}
	*/

	/*
	protected SoarScanner getSoarScanner() {
		if (scanner == null) {
			scanner = new SoarScanner();
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IXMLColorConstants.DEFAULT))));
		}
		return scanner;
	}
	*/
	
	/*
	protected XMLTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new XMLTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IXMLColorConstants.TAG))));
		}
		return tagScanner;
	}
	*/
	
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return new SoarContentAssistant(this);
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);
        reconciler.setDamager( dr, IDocument.DEFAULT_CONTENT_TYPE );
        reconciler.setRepairer( dr, IDocument.DEFAULT_CONTENT_TYPE );
        return reconciler;
	}
	
	public SoarEditor getEditor()
	{
		return editor;
	}

}