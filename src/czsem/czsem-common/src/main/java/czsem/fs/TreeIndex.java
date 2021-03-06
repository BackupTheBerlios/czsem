package czsem.fs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TreeIndex {
	
	protected Map<Integer, Integer> parentIndex;
	protected Map<Integer, Set<Integer>> childIndex;
	protected Set<Integer> nodes = new HashSet<Integer>();
	
	public TreeIndex ()
	{
		parentIndex = new HashMap<Integer, Integer>();
		childIndex = new HashMap<Integer, Set<Integer>>();		
	}



	public Integer getParent(Integer child)
	{
		return parentIndex.get(child);
	}

	public Collection<Integer> getChildren(Integer parent)
	{
		return childIndex.get(parent);
	}
	
	protected void addDependency(Integer[] dep)
	{
		addDependency(dep[0], dep[1]);
	}

	public void addDependency(Integer parent, Integer child)
	{
		nodes.add(parent);
		nodes.add(child);
		
		//parentIndex
		parentIndex.put(child, parent);
		Set<Integer> children = childIndex.get(parent);
		
		//childIndex
		if (children == null) children = new HashSet<Integer>();
		children.add(child);
		childIndex.put(parent, children);
	}
	
	public int findRoot()
	{
		if (parentIndex.entrySet().isEmpty()) return -1;
		
		int root = -1;
		for (Integer i = parentIndex.entrySet().iterator().next().getValue(); i != null; i = getParent(i))
		{
			//System.err.println(i);
			root = i;
		}
		
		return root;			
	}

	public Set<Integer> getAllNodes() {
		return nodes;
	}
}
