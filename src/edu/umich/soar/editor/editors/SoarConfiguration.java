package edu.umich.soar.editor.editors;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.texteditor.MarkerAnnotation;

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
	
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new IAnnotationHover() {
			@Override
			public String getHoverInfo(ISourceViewer viewer, int lineNumber) {
				IDocument document = viewer.getDocument();
				IAnnotationModel annotationModel = viewer.getAnnotationModel();
				Iterator<?> it = annotationModel.getAnnotationIterator();
				while(it.hasNext())
				{
					Object obj = it.next();
					if (!(obj instanceof MarkerAnnotation)) continue;
					MarkerAnnotation marker = (MarkerAnnotation) obj;
					try {
						int markerPosition = annotationModel.getPosition(marker).getOffset();
						int markerLine = document.getLineOfOffset(markerPosition);
						if (markerLine == lineNumber)
						{
							return marker.getMarker().getAttribute(IMarker.MESSAGE, (String) null);
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
	}
	
	public static class SoarTextHover implements ITextHover, ITextHoverExtension2
	{

		@Override
		public Object getHoverInfo2(ITextViewer viewer, IRegion region) {
			if (!(viewer instanceof ISourceViewer)) return null;
			IAnnotationModel annotationModel = ((ISourceViewer)viewer).getAnnotationModel();
			Iterator<?> it = annotationModel.getAnnotationIterator();
			while(it.hasNext())
			{
				Object obj = it.next();
				if (!(obj instanceof MarkerAnnotation)) continue;
				MarkerAnnotation marker = (MarkerAnnotation) obj;
				Position position = annotationModel.getPosition(marker);
				if (position.offset <= region.getOffset() && position.offset + position.length > region.getOffset() )
				{
					return marker.getMarker().getAttribute(IMarker.MESSAGE, (String) null);
				}
			}
			return null;
		}

		@Override
		public String getHoverInfo(ITextViewer arg0, IRegion arg1) {
			return null;
		}

		@Override
		public IRegion getHoverRegion(ITextViewer viewer, int offset) {
			return new Region(offset, 0);
		}
		
	}
	
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new SoarTextHover();
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