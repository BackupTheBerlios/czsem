package czsem.gate;

import gate.AnnotationSet;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import czsem.gate.utils.TreeIndex;

public class FSTreeWriter	{

	public static interface NodeAttributes 
	{
		Iterable<Map.Entry<String, Object>> get(int node_id);
	}
		
	private PrintWriter out;
	private NodeAttributes nodeAttributes;
	private TreeIndex index = new TreeIndex();
	private Set<String> attributes = new HashSet<String>();
	private int rootNode = -1 ;


	public FSTreeWriter(PrintStream out, NodeAttributes nodeAttributes) {
		this(
				new PrintWriter(new OutputStreamWriter(out)),
				nodeAttributes);
	}

	public FSTreeWriter(PrintWriter out, NodeAttributes nodeAttributes) {
		this.out = out;
		this.nodeAttributes = nodeAttributes;
	}


	public void printTree()
	{		
		printNode(getRootNode());
	}


	private void printCildren(int father_id)
	{
		Iterable<Integer> childern = index.getChildren(father_id);
		if (childern == null) return;

		char delim = '('; 
		for (int child_id : childern)
		{
			out.print(delim);
			delim = ',';
			printNode(child_id);
		}			
		if (delim == ',') out.print(')');
	}

	
	private void printAttribute(String attr_name, Object attr_value)		
	{
		attributes.add(attr_name);
		
		out.print(attr_name);
		out.print('=');
		
		String str_value = attr_value.toString();
		String functional_chars = "\\=,[]|<>!";
		
		for (int i=0; i<str_value.length(); i++)
		{
			char ch = str_value.charAt(i);
			if (functional_chars.indexOf(ch) != -1) out.print('\\');
			out.print(ch);
		}
	}
	
	private void printNode(int node_id)
	{
		out.print('[');
					
		Iterator<Entry<String, Object>> i = nodeAttributes.get(node_id).iterator();

		if (i.hasNext())
		{
			for (;;)
			{
				Entry<String, Object> entry = i.next();
				Object value = entry.getValue();
				
				if (value != null)
				{
					printAttribute(entry.getKey(), value);
					
					if (! i.hasNext()) break;
					
					out.print(',');										
				}
				
			}
		}
		
		out.print(']');
				
		printCildren(node_id);
	}

	public void addDependency(Integer parent, Integer child) {
		index.addDependency(parent, child);
	}

	public void addDependecies(AnnotationSet dependenciesAS) {
		index.addDependecies(dependenciesAS);
	}

	public void addTokenDependecies(AnnotationSet tokenAS, String feature_name) {
		index.addTokenDependecies(tokenAS, feature_name);
	}

	public Set<String> getAttributes() {
		return attributes;
	}

	public Set<Integer> getAllNodes() {
		return index.getAllNodes();
	}
	
	public int getRootNode() {
		if (rootNode < 0)
		{
			rootNode = index.findRoot();			
		}
		return rootNode;
	}
}