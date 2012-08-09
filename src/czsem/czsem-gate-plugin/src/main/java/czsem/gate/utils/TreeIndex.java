package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import czsem.gate.GateUtils;

public class TreeIndex
{
	private Map<Integer, Integer> parentIndex;
	private Map<Integer, Set<Integer>> childIndex;
	
	public Integer getParent(Integer child)
	{
		return parentIndex.get(child);
	}

	public Iterable<Integer> getChildren(Integer parent)
	{
		return childIndex.get(parent);
	}
	
	protected void addDependency(Integer[] dep)
	{
		addDependency(dep[0], dep[1]);
	}

	protected void addDependency(Integer parent, Integer child)
	{
		//parentIndex
		parentIndex.put(child, parent);
		Set<Integer> children = childIndex.get(parent);
		
		//childIndex
		if (children == null) children = new HashSet<Integer>();
		children.add(child);
		childIndex.put(parent, children);
	}
	
	protected void addDependency(Annotation a)
	{
		Integer[] dep = GateUtils.decodeEdge(a);
		addDependency(dep[0], dep[1]);
	}; 
	
	protected void addTokenDpendency(Annotation a, String feature_name)
	{
		Integer child = (Integer) a.getFeatures().get(feature_name);
		if (child == null) return;
		addDependency(a.getId(), child);		
	}; 

	public void fillDependecies(AnnotationSet dependenciesAS)
	{
		for (Annotation dep : dependenciesAS)
		{
			addDependency(dep);
		}							
	}
	
	public void fillTokenDependecies(AnnotationSet tokenAS, String feature_name)
	{
		for (Annotation toc : tokenAS)
		{
			addTokenDpendency(toc, feature_name);
		}							
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


	/** <b>Not including root!!!</b> **/
	public List<Annotation> getAllCildrenAnnotations(AnnotationSet annotations)
	{
		List<Annotation> ret = new ArrayList<Annotation>();
		
		for (Set<Integer> children_ids : childIndex.values())
		{
			for (Integer id : children_ids)
			{
				ret.add(annotations.get(id));
			}			
		}

		return ret;
	}

	public TreeIndex ()
	{
		parentIndex = new HashMap<Integer, Integer>();
		childIndex = new HashMap<Integer, Set<Integer>>();		
	}

	
	public TreeIndex (AnnotationSet dependencyAnnotatons)
	{
		this();		
		fillDependecies(dependencyAnnotatons);							
	}
}
