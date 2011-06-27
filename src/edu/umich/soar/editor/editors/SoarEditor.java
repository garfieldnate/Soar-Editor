package edu.umich.soar.editor.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.soartech.soar.ide.core.ast.SoarProductionAst;

import edu.umich.soar.editor.editors.SoarRuleParser.SoarParseError;
import edu.umich.soar.editor.editors.datamap.Correction;
import edu.umich.soar.editor.editors.datamap.Datamap;
import edu.umich.soar.editor.editors.datamap.Datamap.DatamapChangedListener;
import edu.umich.soar.editor.editors.datamap.DatamapUtil;
import edu.umich.soar.editor.editors.datamap.Triple;
import edu.umich.soar.editor.editors.datamap.TripleExtractor;

public class SoarEditor extends TextEditor implements DatamapChangedListener
{

    public static final String ID = "edu.umich.soar.editor.editors.SoarEditor";

    private Map<String, Correction> correctionMap = new HashMap<String, Correction>();

    private ColorManager colorManager;
    private List<IFile> datamapFiles;
    private List<Datamap> datamaps;
    String folderName;

    public SoarEditor()
    {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new SoarConfiguration(colorManager, this));
        setDocumentProvider(new SoarDocumentProvider());
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
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

    public List<Datamap> getDatamaps()
    {
        findDatamaps(getEditorInput());
        buildDatamaps();
        return datamaps;
    }

    private void buildDatamaps()
    {
        datamaps = new ArrayList<Datamap>();
        for (IFile file : datamapFiles)
        {
            if (file.getName().equals("comment.dm"))
            {
                continue;
            }

            // Datamap datamap = null;
            /*
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorPart part = page.findEditor(new FileEditorInput(file));
            if (part != null && part instanceof DatamapEditor)
            {
                DatamapEditor editor = (DatamapEditor) part;
                datamap = editor.getDatamap();
            }
            */

            //if (datamap == null)
            //{
            Datamap datamap = Datamap.read(file);
            //}
            if (datamap != null)
            {
                datamaps.add(datamap);
                datamap.addDatamapChangedListener(this);
                
            }
        }
    }

    private void findDatamaps(IEditorInput input)
    {
        datamapFiles = new ArrayList<IFile>();
        if (!(input instanceof FileEditorInput))
        {
            return;
        }
        IFile file = ((FileEditorInput) input).getFile();
        IContainer parent = file.getParent();
        if (parent == null) return;
        try
        {
            for (IResource member : parent.members())
            {
                if (member instanceof IFile && member.getFileExtension().equalsIgnoreCase("dm"))
                {
                    datamapFiles.add((IFile) member);
                }
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            return;
        }
        if (datamapFiles.size() != 0) return;
        parent = parent.getParent();
        if (parent == null) return;
        try
        {
            for (IResource member : parent.members())
            {
                if (member instanceof IFile && member.getFileExtension().equalsIgnoreCase("dm"))
                {
                    datamapFiles.add((IFile) member);
                }
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            return;
        }
    }

    public void dispose()
    {
        colorManager.dispose();
        super.dispose();
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor)
    {
        super.doSave(progressMonitor);
        IEditorInput input = getEditorInput();
        if (input == null) return;
        if (!(input instanceof FileEditorInput)) return;
        FileEditorInput fileInput = (FileEditorInput) input;
        IFile file = fileInput.getFile();
        findProblems(progressMonitor, file);
    }

    private boolean findProblems(IProgressMonitor monitor, IResource resource)
    {
        try
        {
            IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
            for (IMarker marker : markers)
            {
                removeCorrection(marker);
            }
        }
        catch (CoreException e2)
        {
            e2.printStackTrace();
        }
        try
        {
            resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        }
        catch (CoreException e1)
        {
            e1.printStackTrace();
        }
        IDocumentProvider docProvider = getDocumentProvider();
        IEditorInput editorInput = getEditorInput();
        if (docProvider == null)
        {
            docProvider = DocumentProviderRegistry.getDefault().getDocumentProvider(editorInput);
        }
        if (docProvider == null)
        {
            return false;
        }
        IDocument doc = docProvider.getDocument(editorInput);
        if (doc == null) return false;
        String text = doc.get();
        List<SoarParseError> errors = new ArrayList<SoarParseError>();
        List<SoarProductionAst> asts = new ArrayList<SoarProductionAst>();
        SoarRuleParser.parseRules(text, monitor, errors, asts);
        addErrors(resource, errors);
        ArrayList<String> stateVariables = new ArrayList<String>();
        for (SoarProductionAst ast : asts)
        {
            System.out.println("ast " + ast.getName() + ", " + ast.getRuleOffset());
            stateVariables.clear();
            List<Triple> triples = TripleExtractor.makeTriples(ast, stateVariables);
            List<Correction> corrections = null;
            boolean first = true;
            List<Datamap> datamaps = getDatamaps();
            
            if (stateVariables.size() == 0)
            {
                addError(resource, new SoarParseError("No state variables found in rule " + ast.getName(), ast.getRuleOffset(), 0));
            }
            
            for (Datamap datamap : datamaps)
            {
                if (first)
                {
                    first = false;
                    corrections = DatamapUtil.getCorrections(triples, datamap, stateVariables, folderName);
                }
                else
                {
                    List<Correction> newCorrections = DatamapUtil.getCorrections(triples, datamap, stateVariables, folderName);
                    List<Correction> toRemove = new ArrayList<Correction>();
                    for (Correction correction : corrections)
                    {
                        if (!newCorrections.contains(correction))
                        {
                            toRemove.add(correction);
                        }
                    }
                    corrections.removeAll(toRemove);
                }
            }
            if (corrections != null)
            {
                addCorrections(resource, corrections, ast);
            }
        }
        return true;
    }
    
    private static void addErrors(IResource resource, List<SoarParseError> errors)
    {
        for (SoarParseError error : errors)
        {
            addError(resource, error);
        }
    }
    
    private static void addError(IResource resource, SoarParseError error)
    {
        System.out.println("ERROR, " + error.message + ", " + error.start);
        IMarker marker;
        try
        {
            marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.CHAR_START, error.start);
            marker.setAttribute(IMarker.CHAR_END, error.start + error.length);
            marker.setAttribute(IMarker.MESSAGE, error.message);
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
    }
    
    private void addCorrections(IResource resource, List<Correction> corrections, SoarProductionAst ast)
    {
        for (Correction correction : corrections)
        {
            addCorrection(resource, correction, ast);
        }
    }
    
    private void addCorrection(IResource resource, Correction correction, SoarProductionAst ast)
    {
        System.out.println(correction);
        IMarker marker;
        try
        {
            marker = resource.createMarker(IMarker.PROBLEM);
            marker.setAttribute(IMarker.CHAR_START, ast.getRuleOffset() + correction.getErrorOffset() - 1); // 1-indexed
                                                                                                            // to
                                                                                                            // 0-indexed
            marker.setAttribute(IMarker.CHAR_END, ast.getRuleOffset() + correction.getErrorOffset() + correction.getErrorLength() - 1);
            marker.setAttribute(IMarker.MESSAGE, correction.toString(datamaps, folderName));
            marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
            addCorrection(marker, correction);
            correction.node.datamap.addDatamapChangedListener(this);
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }
    }


    private static String keyForMarker(IMarker marker)
    {
        return marker.getResource().toString() + "_" + marker.getId();
    }

    public Correction findCorrection(IMarker marker)
    {
        return findCorrection(keyForMarker(marker));
    }

    private void addCorrection(IMarker marker, Correction correction)
    {
        correctionMap.put(keyForMarker(marker), correction);
    }

    private void removeCorrection(IMarker marker)
    {
        correctionMap.remove(marker);
    }

    private Correction findCorrection(String key)
    {
        if (!correctionMap.containsKey(key)) return null;
        return correctionMap.get(key);
    }

    public String getFolderName()
    {
        return folderName;
    }

    @Override
    public boolean onDatamapChanged(Datamap datamap, Object changed)
    {
        FileEditorInput fileInput = (FileEditorInput) getEditorInput();
        IFile file = fileInput.getFile();
        return findProblems(getProgressMonitor(), file);
    }
}
