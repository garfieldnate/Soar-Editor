package edu.umich.soar.editor.editors.datamap;

import java.util.ArrayList;
import java.util.List;

public class DatamapNode
{	
	public static enum NodeType
	{
		SOAR_ID,
		ENUMERATION,
		FLOAT_RANGE,
		INT_RANGE,
		STRING;
		
		public String getName()
		{
			switch (this)
			{
			case ENUMERATION:
				return "Enumeration";
			case FLOAT_RANGE:
				return "Float";
			case INT_RANGE:
				return "Integer";
			case SOAR_ID:
				return "Identifier";
			case STRING:
				return "String";
			}
			return toString();
		}
		
		public static NodeType get(String name)
		{
			if (name.startsWith("INT"))
			{
				return INT_RANGE;
			}
			return NodeType.valueOf(name);
		}
	}

	public NodeType type;
	public int id;
	public Datamap datamap;

	// For use only by enumerations
	public List<String> values;
	
	// For use only by floats
	public double floatMin;
	public double floatMax;
	
	// For use only by ints
	public int intMin;
	public int intMax;
	
	// For use only by identifiers
	private boolean hasState = false;
	private List<String> stateNames = new ArrayList<String>();
	
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
		this(NodeType.get(type), id, datamap);
	}
	
	@Override
	public String toString() {
		if (hasState())
		{
			return "state <s>";
		}
		return "Node, " + type + ", " + id;
	}
	
	public boolean hasState()
	{
		return type == NodeType.SOAR_ID && hasState;
	}
	
	public void setHasState(boolean hasState)
	{
		this.hasState = hasState;
	}
	
	public void addStateName(String stateName)
	{
		stateNames.add(stateName);
	}
	
	public String[] getStateNames()
	{
		return stateNames.toArray(new String[0]);
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
			sb.append(" " + floatMin + " " + floatMax);
			break;
		case INT_RANGE:
			sb.append(" " + intMin + " " + intMax);
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
	
	public void addLink(String name, DatamapNode child)
	{
		DatamapAttribute attribute = new DatamapAttribute(this.id, name, child.id, datamap);
		datamap.addAttribute(attribute);
		datamap.contentChanged(this);
	}
	
	public List<DatamapNode> getChildren()
	{
		List<DatamapNode> children = new ArrayList<DatamapNode>();
		for (DatamapAttribute attribute : datamap.getAttributesFrom(id))
		{
			DatamapNode child = datamap.getNode(attribute.to);
			children.add(child);
		}
		return children;
	}

	public List<DatamapNode> getChildren(String name)
	{
		List<DatamapNode> children = new ArrayList<DatamapNode>();
		for (DatamapAttribute attribute : datamap.getAttributesFrom(id))
		{
			if (attribute.name.equals(name))
			{
				DatamapNode child = datamap.getNode(attribute.to);
				if (child != null)
				{
					children.add(child);
				}
			}
		}
		return children;
	}

	public List<DatamapNode> getChildren(String name, NodeType type)
	{
		List<DatamapNode> children = new ArrayList<DatamapNode>();
		for (DatamapAttribute attribute : datamap.getAttributesFrom(id))
		{
			if (attribute.name.equals(name))
			{
				DatamapNode child = datamap.getNode(attribute.to);
				if (child != null && child.type == type)
				{
					children.add(child);
				}
			}
		}
		return children;
	}
	
	public List<DatamapNode> getChildren(NodeType type)
	{
		List<DatamapNode> children = new ArrayList<DatamapNode>();
		for (DatamapAttribute attribute : datamap.getAttributesFrom(id))
		{
			DatamapNode child = datamap.getNode(attribute.to);
			if (child != null && child.type == type)
			{
				children.add(child);
			}
		}
		return children;
	}

	public String tabName() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < stateNames.size(); ++i)
		{
			sb.append(stateNames.get(i));
			if (i + 1 < stateNames.size())
			{
				sb.append(", ");
			}
		}
		return sb.toString();
	}
}
