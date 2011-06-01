package edu.umich.soar.editor.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import com.soartech.soar.ide.core.ast.SoarProductionAst;

import edu.umich.soar.editor.editors.SoarRuleParser.SoarParseError;
import edu.umich.soar.editor.editors.datamap.Correction;
import edu.umich.soar.editor.editors.datamap.Datamap;
import edu.umich.soar.editor.editors.datamap.DatamapUtil;
import edu.umich.soar.editor.editors.datamap.Triple;
import edu.umich.soar.editor.editors.datamap.TripleExtractor;

public class SoarEditor extends TextEditor {

	public static final String ID = "edu.umich.soar.editor.editors.SoarEditor";
	
    private ColorManager colorManager;
	private List<IFile> datamapFiles;
	private List<Datamap> datamaps;
	String folderName;

	public SoarEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new SoarConfiguration(colorManager, this));
		setDocumentProvider(new SoarDocumentProvider());
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		findDatamaps(input);

		FileEditorInput fileInput = (FileEditorInput) input;
		IFile file = fileInput.getFile();
		folderName = file.getParent().getName();
		if (folderName.equals("elaborations"))
		{
			IContainer container = file.getParent().getParent();
			String containerName = container.getName();
			folderName = containerName;
		}
		findProblems(getProgressMonitor(), file);
	}

	public List<Datamap> getDatamaps() {
		if (datamaps == null) {
			buildDatamaps();
		}
		return datamaps;
	}

	private void buildDatamaps() {
		datamaps = new ArrayList<Datamap>();
		for (IFile file : datamapFiles) {
			if (file.getName().equals("comment.dm")) {
				continue;
			}
			Datamap datamap = Datamap.read(file, null);
			if (datamap != null) {
				datamaps.add(datamap);
			}
		}
	}

	private void findDatamaps(IEditorInput input) {
		datamapFiles = new ArrayList<IFile>();
		if (!(input instanceof FileEditorInput)) {
			return;
		}
		IFile file = ((FileEditorInput) input).getFile();
		IContainer parent = file.getParent();
		if (parent == null)
			return;
		try {
			for (IResource member : parent.members()) {
				if (member instanceof IFile
						&& member.getFileExtension().equalsIgnoreCase("dm")) {
					datamapFiles.add((IFile) member);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
		if (datamapFiles.size() != 0)
			return;
		parent = parent.getParent();
		if (parent == null)
			return;
		try {
			for (IResource member : parent.members()) {
				if (member instanceof IFile
						&& member.getFileExtension().equalsIgnoreCase("dm")) {
					datamapFiles.add((IFile) member);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			return;
		}
	}

	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		IEditorInput input = getEditorInput();
		if (input == null)
			return;
		if (!(input instanceof FileEditorInput))
			return;
		FileEditorInput fileInput = (FileEditorInput) input;
		IFile file = fileInput.getFile();
		findProblems(progressMonitor, file);
	}

	private void findProblems(IProgressMonitor monitor, IResource resource) {
		try {
			resource.deleteMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_INFINITE);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		String text = getDocumentProvider().getDocument(getEditorInput()).get();
		List<SoarParseError> errors = new ArrayList<SoarParseError>();
		List<SoarProductionAst> asts = new ArrayList<SoarProductionAst>();
		SoarRuleParser.parseRules(text, monitor, errors, asts);
		for (SoarParseError error : errors) {
			System.out.println("ERROR, " + error.message + ", " + error.start);
			IMarker marker;
			try {
				marker = (resource.createMarker(IMarker.PROBLEM));
				marker.setAttribute(IMarker.CHAR_START, error.start);
				marker.setAttribute(IMarker.CHAR_END, error.start
						+ error.length);
				marker.setAttribute(IMarker.MESSAGE, error.message);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		ArrayList<String> stateVariables = new ArrayList<String>();
		for (SoarProductionAst ast : asts) {
			System.out.println("ast " + ast.getName() + ", "
					+ ast.getRuleOffset());
			stateVariables.clear();
			List<Triple> triples = TripleExtractor.makeTriples(ast,
					stateVariables);
			List<Correction> corrections = null;
			boolean first = true;
			for (Datamap datamap : getDatamaps()) {
				if (first) {
					first = false;
					corrections = DatamapUtil.getCorrections(triples, datamap,
							stateVariables, folderName);
				} else {
					List<Correction> newCorrections = DatamapUtil
							.getCorrections(triples, datamap, stateVariables, folderName);
					List<Correction> toRemove = new ArrayList<Correction>();
					for (Correction correction : corrections) {
						if (!newCorrections.contains(correction)) {
							toRemove.add(correction);
						}
					}
					corrections.removeAll(toRemove);
				}
			}
			if (corrections != null) {
				for (Correction correction : corrections) {
					System.out.println(correction);
					IMarker marker;
					try {
						marker = (resource.createMarker(IMarker.PROBLEM));
						marker.setAttribute(IMarker.CHAR_START, ast.getRuleOffset()	+ correction.getErrorOffset() - 1); // 1-indexed to 0-indexed
						marker.setAttribute(IMarker.CHAR_END, ast.getRuleOffset() + correction.getErrorOffset() + correction.getErrorLength() - 1);
						marker.setAttribute(IMarker.MESSAGE, correction.toString(datamaps, folderName));
						marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
						marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
