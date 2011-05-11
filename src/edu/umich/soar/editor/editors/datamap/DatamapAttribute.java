package edu.umich.soar.editor.editors.datamap;

import java.util.List;

import edu.umich.soar.editor.editors.datamap.DatamapNode.NodeType;

public class DatamapAttribute
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
		DatamapNode dest = datamap.getNode(to);
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
			return sb.toString() + " <" + to + ">";
		}
		else if (dest.type == NodeType.INT_RANGE)
		{
			return name + " [" + dest.intMin + ", " + dest.intMax + "]";
		}
		else if (dest.type == NodeType.FLOAT_RANGE)
		{
			return name + " [" + dest.floatMin + ", " + dest.floatMax + "]";
		}
		return name;
	}
	
	public DatamapNode getFrom()
	{
		return datamap.getNode(from);
	}
	
	public DatamapNode getTarget()
	{
		return datamap.getNode(to);
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

	public boolean isLinked() {
		return datamap.getAttributesTo(to).size() > 1;
	}

	public List<DatamapAttribute> getLinkedAttributes() {
		return datamap.getAttributesTo(to);
	}

	/**
	 * 
	 * @return A path from state <s> to this node.
	 */
	public Object getPathString() {
		int maxLength = 20;
		StringBuffer sb = new StringBuffer();
		sb.append(toString());
		DatamapAttribute current = this;
		for (int i = 0; i < maxLength; ++i)
		{
			if (current.getFrom().hasState())
			{
				DatamapNode from = current.getFrom();
				StringBuilder name = new StringBuilder();
				String[] names = from.getStateNames();
				for (int j = 0; j < names.length; ++j)
				{
					name.append(names[j]);
					if (j + 1 < names.length)
					{
						name.append("/");
					}
				}
				return name.toString() + "." + sb.toString();
			}
			List<DatamapAttribute> parents = datamap.getAttributesTo(current.from);
			if (parents == null || parents.size() == 0)
			{
				return "..." + sb.toString();
			}
			current = parents.get(0);
			sb.insert(0, current.name + ".");
		}
		return "..." + sb.toString();
	}
}
