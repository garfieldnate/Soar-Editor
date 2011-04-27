package edu.umich.soar.editor.editors.datamap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.TreeEditor;

import edu.umich.soar.editor.editors.datamap.Datamap.NodeType;

public class Datamap implements ITreeContentProvider {
	
	public static enum NodeType
	{
		SOAR_ID,
		ENUMERATION,
		FLOAT_RANGE,
		INT_RANGE,
		STRING,
	}
	
	public static class DatamapNode
	{	
		public NodeType type;
		public int id;
		public Datamap datamap;

		// For use only by enumerations
		public List<String> values;
		
		// For use only by floats
		public double float_min;
		public double float_max;
		
		// For use only by ints
		public int int_min;
		public int int_max;
		
		public DatamapNode(NodeType type, int id, Datamap datamap)
		{
			this.type = type;
			this.id = id;
			this.datamap = datamap;	
			if (type == NodeType.ENUMERATION)
			{
				values = new ArrayList<String>();
			}
		}
		
		public DatamapNode(String type, int id, Datamap datamap)
		{
			this(NodeType.valueOf(type), id, datamap);
		}
		
		@Override
		public String toString() {
			if (id == 0)
			{
				return "state <s>";
			}
			return "Node, " + type + ", " + id;
		}

		public Object getSaveString() {
			StringBuilder sb = new StringBuilder();
			sb.append(type.toString() + " " + id);
			switch (type)
			{
			case ENUMERATION:
				sb.append(" " + values.size());
				for (String value : values)
				{
					sb.append(" " + value);
				}
				break;
			case FLOAT_RANGE:
				sb.append(" " + float_min + " " + float_max);
				break;
			case INT_RANGE:
				sb.append(" " + int_min + " " + int_max);
				break;
			case SOAR_ID:
				
				break;
			case STRING:
				
				break;
			default:
				
				break;
			}
			return sb.toString();
		}

		public void addChild(String name, NodeType nodeType) {
			DatamapNode child = new DatamapNode(nodeType, datamap.newId(), datamap);
			DatamapAttribute attribute = new DatamapAttribute(this.id, name, child.id, datamap);
			datamap.addNode(child);
			datamap.addAttribute(attribute);
			datamap.contentChanged(this);
		}
	}
	
	public static class DatamapAttribute
	{
		public int from;
		public int to;
		public String name;
		public Datamap datamap;
		
		public DatamapAttribute(int from, String name, int to, Datamap datamap)
		{
			this.from = from;
			this.name = name;
			this.to = to;
			this.datamap = datamap;
		}
		
		@Override
		public String toString() {
			DatamapNode dest = datamap.nodes.get(to);
			if (dest == null) return name;
			if (dest.type == NodeType.SOAR_ID)
			{
				return name + " <" + dest.id + ">";
			}
			else if (dest.type == NodeType.ENUMERATION)
			{
				StringBuffer sb = new StringBuffer();
				sb.append(name + " (");
				for (int i = 0; i < dest.values.size(); ++i)
				{
					String value = dest.values.get(i);
					sb.append(value);
					if (i + 1 < dest.values.size())
					{
						sb.append(", ");
					}
				}
				sb.append(")");
				return sb.toString() + "(" + from + " to " + to + ")";
			}
			else if (dest.type == NodeType.INT_RANGE)
			{
				return name + " [" + dest.int_min + ", " + dest.int_max + "]";
			}
			else if (dest.type == NodeType.FLOAT_RANGE)
			{
				return name + " [" + dest.float_min + ", " + dest.float_max + "]";
			}
			return name;
		}
		
		public DatamapNode getTarget()
		{
			return datamap.nodes.get(to);
		}

		public void setFrom(int from) {
			datamap.removeAttribute(this);
			this.from = from;
			datamap.addAttribute(this);
			datamap.contentChanged(this);
		}

		public Object getSaveString() {
			return "" + from + " " + name + " " + to;
		}

		public void setName(String name) {
			this.name = name;
			datamap.contentChanged(this);
		}

		public void delete()
		{
			datamap.removeAttribute(this);
			datamap.contentChanged(this);
		}
	}
	
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
				node.float_min = Double.valueOf(tokens[2]);
				node.float_max = Double.valueOf(tokens[3]);
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
				node.int_min = Integer.valueOf(tokens[2]);
				node.int_max = Integer.valueOf(tokens[3]);
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

	private Map<Integer, DatamapNode> nodes;
	private Map<Integer, ArrayList<DatamapAttribute>> attributes;
	private DatamapTreeEditor editor;
	private int maxId = -1;
	private boolean valid = true;

	private Datamap(IFile input, DatamapTreeEditor editor)
	{
		this.editor = editor;
		nodes = new HashMap<Integer, DatamapNode>();
		attributes = new HashMap<Integer, ArrayList<DatamapAttribute>>();
		InputStream is;
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
		} catch (CoreException e) {
			e.printStackTrace();
			valid = false;
		}
	}
	
	private void checkId(int id)
	{
		if (id >= maxId)
		{
			maxId = id + 1;
		}
	}
	
	private int newId()
	{
		maxId += 1;
		return maxId - 1;
	}
	
	public void contentChanged(Object changed) {
		if (editor != null)
		{
			editor.contentChanged(changed);
		}
	}
	
	private void addAttribute(DatamapAttribute attribute)
	{
		ArrayList<DatamapAttribute> list = attributes.get(attribute.from);
		if (list == null)
		{
			list = new ArrayList<Datamap.DatamapAttribute>();
			attributes.put(attribute.from, list);
		}
		list.add(attribute);
	}
	
	private void removeAttribute(DatamapAttribute attribute)
	{
		ArrayList<DatamapAttribute> list = attributes.get(attribute.from);
		if (list == null)
		{
			return;
		}
		list.remove(attribute);
	}
	
	private void addNode(DatamapNode node)
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
			return attributes.get(node.id).toArray();
		}
		else if (parent instanceof DatamapAttribute)
		{
			DatamapAttribute attribute = (DatamapAttribute) parent;
			DatamapNode child = nodes.get(attribute.to);
			List<DatamapAttribute> childAttributes = attributes.get(child.id);
			if (childAttributes == null) return null;
			return childAttributes.toArray();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object parent) {
		return new Object[] { nodes.get(0) };
	}

	@Override
	public Object getParent(Object parent) {
		return null;
	}

	@Override
	public boolean hasChildren(Object parent) {
		return getChildren(parent) != null;
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

	public static Datamap read(IFile file, DatamapTreeEditor editor) {
		Datamap datamap = new Datamap(file, editor);
		if (datamap.valid)
		{
			return datamap;
		}
		return null;
	}
}
