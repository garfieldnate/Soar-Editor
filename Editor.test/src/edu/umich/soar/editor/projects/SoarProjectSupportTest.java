package edu.umich.soar.editor.projects;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
 
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.umich.soar.editor.natures.ProjectNature;
import edu.umich.soar.editor.projects.SoarProjectSupport;

public class SoarProjectSupportTest {
	@Rule
	public TemporaryFolder folder1 = new TemporaryFolder();
    @SuppressWarnings("nls")
    @Test
    public void testCreateProjectWithDifferentLocationArg() throws URISyntaxException, DocumentException, CoreException, IOException {
        
    	File workspaceFilePath = folder1.newFolder();
    	File workspace = createTempWorkspace(workspaceFilePath.getCanonicalPath());
 
        String projectName = "delete-me"; //$NON-NLS-1$
        File projectPath = new File(workspaceFilePath, projectName);
        URI location = projectPath.toURI();
        
        assertProjectDotFileAndStructureAndNatureExist(projectPath.getCanonicalPath(), 
        		projectName, location);
 
        deleteTempWorkspace(workspace);
    }
 
    @Test
    public void testCreateProjectWithEmptyNameArg() {
        String projectName = " "; //$NON-NLS-1$
        assertCreateProjectWithBadNameArg(projectName);
    }
 
    @Test
    public void testCreateProjectWithNullNameArg() {
        String projectName = null;
        assertCreateProjectWithBadNameArg(projectName);
    }

	@Rule
	public TemporaryFolder folder2 = new TemporaryFolder();
    @SuppressWarnings("nls")
    @Test
    public void testCreateProjectWithGoodArgs() throws DocumentException, CoreException, IOException {
        // This is the default workspace for this plug-in
    	String workspaceFilePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
        String projectName = "delete-me";
        File project = new File(workspaceFilePath, projectName);
 
        URI location = null;
        assertProjectDotFileAndStructureAndNatureExist(project.getCanonicalPath(), 
        		projectName, location);
    }
 
    @SuppressWarnings("nls")
    private void assertProjectDotFileAndStructureAndNatureExist(String projectPath, String name, URI location) throws DocumentException,
            CoreException {
        System.out.println(name + " " + location);
        IProject project = SoarProjectSupport.createProject(name, location);
 
        String projectFilePath = projectPath + "/" + ".project";
        String[] emptyNodes = { "/projectDescription/comment", "/projectDescription/projects", "/projectDescription/buildSpec" };
        String[] nonEmptyNodes = { "/projectDescription/name", "/projectDescription/natures/nature" };
 
        Assert.assertNotNull(project);
        assertFileExists(projectFilePath);
        assertAllElementsEmptyExcept(projectFilePath, emptyNodes, nonEmptyNodes);
        assertNatureIn(project);
        assertFolderStructureIn(projectPath);
 
        project.delete(true, null);
    }
 
    @SuppressWarnings("nls")
    private void assertFolderStructureIn(String projectPath) {
        String[] paths = { "parent/child1-1/child2", "parent/child1-2/child2/child3" };
        for (String path : paths) {
            File file = new File(projectPath + "/" + path);
            if (!file.exists()) {
                Assert.fail("Folder structure " + path + " does not exist.");
            }
        }
    }
 
    private void assertNatureIn(IProject project) throws CoreException {
        IProjectDescription descriptions = project.getDescription();
        String[] natureIds = descriptions.getNatureIds();
        if (natureIds.length != 1) {
            Assert.fail("No natures found in project."); //$NON-NLS-1$
        }
 
        if (!natureIds[0].equals(ProjectNature.NATURE_ID)) {
            Assert.fail("CustomProject natures not found in project."); //$NON-NLS-1$
        }
    }
 
    private void assertAllElementsEmptyExcept(String projectFilePath, String[] emptyNodes, String[] nonEmptyNodes) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(projectFilePath);
        int strLength;
        for (String emptyNode : emptyNodes) {
            strLength = document.selectSingleNode(emptyNode).getText().trim().length();
            if (strLength != 0) {
                Assert.fail("Node " + emptyNode + " was non-empty!"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
 
        for (String nonEmptyNode : nonEmptyNodes) {
            strLength = document.selectSingleNode(nonEmptyNode).getText().trim().length();
            if (strLength == 0) {
                Assert.fail("Node " + nonEmptyNode + " was empty!"); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
    }
 
    private void assertFileExists(String projectFilePath) {
        File file = new File(projectFilePath);
 
        if (!file.exists()) {
            Assert.fail("File " + projectFilePath + " does not exist."); //$NON-NLS-1$//$NON-NLS-2$
        }
    }
 
    private void assertCreateProjectWithBadNameArg(String name) {
        URI location = null;
        try {
            SoarProjectSupport.createProject(name, location);
            Assert.fail("The call to CustomProjectSupport.createProject() did not fail!"); //$NON-NLS-1$
        } catch (AssertionFailedException e) {
            // An exception was thrown as expected; the test passed.
        }
    }
 
    private void deleteTempWorkspace(File workspace) {
        boolean deleted = workspace.delete();
        if (!deleted) {
            Assert.fail("Unable to delete the new workspace dir at " + workspace); //$NON-NLS-1$
        }
    }
 
    private File createTempWorkspace(String pathToWorkspace) {
        File workspace = new File(pathToWorkspace);
        if (!workspace.exists()) {
            boolean dirCreated = workspace.mkdir();
            if (!dirCreated) {
                Assert.fail("Unable to create the new workspace dir at " + workspace); //$NON-NLS-1$
            }
        }
 
        return workspace;
    }
 
}