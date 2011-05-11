package edu.umich.soar.editor.editors.datamap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import edu.umich.soar.editor.editors.datamap.DatamapNode.NodeType;

public class Datamap implements ITreeContentProvider {
	
	
	public DatamapNode makeNode(String line)
	{
		String[] tokens = line.split(" ");
		if (tokens.length < 2)
		{
			return null;
		}
		String type = tokens[0];
		int id;
		try
		{
			id = Integer.valueOf(tokens[1]);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return null;
		}
		DatamapNode node = new DatamapNode(type, id, this);
		if (node.type == NodeType.ENUMERATION)
		{
			for (int i = 3; i < tokens.length; ++i)
			{
				node.values.add(tokens[i]);
			}
		}
		else if (node.type == NodeType.FLOAT_RANGE)
		{
			try
			{
				node.floatMin = Double.valueOf(tokens[2]);
				node.floatMax = Double.valueOf(tokens[3]);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				valid = false;
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				valid = false;
			}
		}
		else if (node.type == NodeType.INT_RANGE)
		{
			try
			{
				node.intMin = Integer.valueOf(tokens[2]);
				node.intMax = Integer.valueOf(tokens[3]);
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				valid = false;
			}
			catch (ArrayIndexOutOfBoundsException e)
			{
				e.printStackTrace();
				valid = false;
			}
		}
		return node;
	}

	public DatamapAttribute makeAttrbute(String line)
	{
		String[] tokens = line.split(" ");
		if (tokens.length < 3) return null;
		int from = Integer.valueOf(tokens[0]);
		String name = tokens[1];
		int to = Integer.valueOf(tokens[2]);
		DatamapAttribute ret = new DatamapAttribute(from, name, to, this);
		return ret;
	}

	// Maps node.id onto that node.
	private Map<Integer, DatamapNode> nodes;
	
	// Maps attribute.from values onto a list of attributes with that .from value.
	private Map<Integer, ArrayList<DatamapAttribute>> attributes;
	
	// Maps attribute.to values onto a list of attributes with that .to value.
	private Map<Integer, ArrayList<DatamapAttribute>> reversedAttributes;
	private DatamapEditor editor;
	private int maxId = -1;
	private boolean valid = true;
	private String filename;
	
	// Maps the name of a state onto root nodes for that state.
	private Map<String, DatamapNode> stateNodeMap;
	private List<DatamapNode> stateNodeList;
	

	private Datamap(IFile input, DatamapEditor editor)
	{
		this.editor = editor;
		nodes = new HashMap<Integer, DatamapNode>();
		attributes = new HashMap<Integer, ArrayList<DatamapAttribute>>();
		reversedAttributes = new HashMap<Integer, ArrayList<DatamapAttribute>>();
		stateNodeMap = new HashMap<String, DatamapNode>();
		stateNodeList = new ArrayList<DatamapNode>();
		
		InputStream is;
		filename = input.getName();
		try {
			is = input.getContents();
			Scanner s = new Scanner(is);
			int num_nodes = -1;
			while(s.hasNextLine())
			{
				String line = s.nextLine().trim();
				if (line.trim().isEmpty()) continue;
				if (num_nodes == -1)
				{
					num_nodes = Integer.valueOf(line);
				}
				else
				{
					DatamapNode node = makeNode(line);
					if (node == null)
					{
						valid = false;
						continue;
					}
					checkId(node.id);
					nodes.put(node.id, node);
					--num_nodes;
					if (num_nodes == 0)
					{
						break;
					}
				}
			}
			int num_attributes = -1;
			while(s.hasNextLine())
			{
				String line = s.nextLine();
				if (line.trim().isEmpty()) continue;
				if (num_attributes == -1)
				{
					num_attributes = Integer.valueOf(line);
				}
				else
				{
					DatamapAttribute attribute = makeAttrbute(line);
					if (attribute == null)
					{
						valid = false;
						continue;
					}
					addAttribute(attribute);
					--num_attributes;
					if (num_attributes == 0)
					{
						break;
					}
				}
			}
			findStates();
		} catch (CoreException e) {
			e.printStackTrace();
			valid = false;
		}
	}
	
	public String[] getStateNames()
	{
		return stateNodeMap.keySet().toArray(new String[0]);
	}
	
	public List<DatamapNode> getStateNodes()
	{
		return stateNodeList;
	}
	
	private void findStates()
	{
		for (DatamapNode node : nodes.values())
		{
			if (node.type != NodeType.SOAR_ID) continue;
			List<DatamapNode> typeNodes = node.getChildren("type", NodeType.ENUMERATION);
			boolean foundTypeState = false;
			for (DatamapNode typeNode : typeNodes)
			{
				if (typeNode.values.contains("state"))
				{
					foundTypeState = true;
					break;
				}
			}
			if (!foundTypeState)
			{
				continue;
			}
			ArrayList<String> names = new ArrayList<String>();
			List<DatamapNode> nameNodes = node.getChildren("name", NodeType.ENUMERATION);
			for (DatamapNode nameNode : nameNodes)
			{
				names.addAll(nameNode.values);
			}
			if (nameNodes.size() == 0) continue;
			node.setHasState(true);
			stateNodeList.add(node);
			for (String name : names)
			{
				stateNodeMap.put(name, node);
				node.addStateName(name);
			}
		}
	}
	
	private void checkId(int id)
	{
		if (id >= maxId)
		{
			maxId = id + 1;
		}
	}
	
	public int newId()
	{
		maxId += 1;
		return maxId - 1;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	public void contentChanged(Object changed) {
		if (editor != null)
		{
			editor.contentChanged(changed);
		}
	}
	
	public void addAttribute(DatamapAttribute attribute)
	{
		ArrayList<DatamapAttribute> list = attributes.get(attribute.from);
		if (list == null)
		{
			list = new ArrayList<DatamapAttribute>();
			attributes.put(attribute.from, list);
		}
		list.add(attribute);
		list = reversedAttributes.get(attribute.to);
		if (list == null)
		{
			list = new ArrayList<DatamapAttribute>();
			reversedAttributes.put(attribute.to, list);
		}
		list.add(attribute);
	}
	
	public void removeAttribute(DatamapAttribute attribute)
	{
		ArrayList<DatamapAttribute> list = attributes.get(attribute.from);
		if (list != null)
		{
			list.remove(attribute);
		}
		list = reversedAttributes.get(attribute.to);
		if (list != null)
		{
			list.remove(attribute);
		}
	}
	
	public List<DatamapAttribute> getAttributesFrom(int fromId)
	{
		List<DatamapAttribute> ret = attributes.get(fromId);
		if (ret != null) return ret;
		return new ArrayList<DatamapAttribute>();
	}
	
	public List<DatamapAttribute> getAttributesTo(int toId)
	{
		List<DatamapAttribute> ret = reversedAttributes.get(toId);
		if (ret != null) return ret;
		return new ArrayList<DatamapAttribute>();
	}
	
	public void addNode(DatamapNode node)
	{
		nodes.put(node.id, node);
	}
	
	private void removeNode(DatamapNode node)
	{
		nodes.remove(node.id);
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof DatamapNode)
		{
			DatamapNode node = (DatamapNode) parent;
			Collection<DatamapAttribute> childAttributes = getAttributesFrom(node.id);
			return childAttributes.toArray();
		}
		else if (parent instanceof DatamapAttribute)
		{
			DatamapAttribute attribute = (DatamapAttribute) parent;
			DatamapNode child = nodes.get(attribute.to);
			Collection<DatamapAttribute> childAttributes = getAttributesFrom(child.id);
			if (childAttributes == null) return null;
			return childAttributes.toArray();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object parent) {
		if (parent instanceof Object[])
		{
			return (Object[]) parent;
		}
		return getChildren(parent);
	}

	@Override
	public Object getParent(Object parent) {
		return null;
	}

	@Override
	public boolean hasChildren(Object parent) {
		return getChildren(parent).length > 0;
	}

	public boolean writeToFile(IFile file, IProgressMonitor monitor) {
		StringBuffer sb = new StringBuffer();
		sb.append(nodes.size());
		sb.append('\n');
		for (DatamapNode node : nodes.values())
		{
			sb.append(node.getSaveString());
			sb.append('\n');
		}
		int numAttribtues = 0;
		for (ArrayList<DatamapAttribute> attributeList : attributes.values())
		{
			numAttribtues += attributeList.size();
		}
		sb.append(numAttribtues);
		sb.append('\n');
		for (ArrayList<DatamapAttribute> attributeList : attributes.values())
		{
			for (DatamapAttribute attribute : attributeList)
			{
				sb.append(attribute.getSaveString());
				sb.append('\n');
			}
		}
		ByteArrayInputStream is = new ByteArrayInputStream(sb.toString().getBytes());
		try {
			file.setContents(is, 0, monitor);
			return true;
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<Integer, ArrayList<DatamapAttribute>> getAttributes() {
		return attributes;
	}
	
	public Map<Integer, DatamapNode> getNodes() {
		return nodes;
	}

	public static Datamap read(IFile file, DatamapEditor editor) {
		Datamap datamap = new Datamap(file, editor);
		if (datamap.valid)
		{
			return datamap;
		}
		return null;
	}

	public DatamapNode getState(String stateName) {
		return stateNodeMap.get(stateName);
	}

	public DatamapNode getStateNode(String stateName) {
		return stateNodeMap.get(stateName);
	}

	public DatamapNode getNode(int to) {
		return nodes.get(to);
	}
}