package czsem.utils;

import java.util.Iterator;

import cz.cuni.mff.mirovsky.ShowMessagesAble;
import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGTree;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import cz.cuni.mff.mirovsky.trees.TAHLine;
import cz.cuni.mff.mirovsky.trees.TNode;

public class CZSemTree {
    /** Array of all tree nodes, ordered by DepthOrder */
	private TNode [] node_array;
	private NGTree tree;
	
	public static int id_attr = 23;
	public static int deepord_attr = 5;


	public NGTree getNGTree()
	{
		return tree;
	}
	
	public CZSemTree(ShowMessagesAble p_mess) {
		tree = new NGTree(p_mess);
	}

	public CZSemTree() {
		this((ShowMessagesAble) null);
	}

	public CZSemTree(NGTree tree) {
		this.tree = tree;
		initNodeArray();		
	}
	
	public int getCountOfNodes()
	{
		return node_array.length;
	}
	
	private static class BrotherIterator implements Iterator<TNode>, Iterable<TNode>
	{		
		private TNode node;
		
		BrotherIterator(TNode node)
		{
			this.node = node; 
		}

		public boolean hasNext() {
			return node != null;
		}
		
		public TNode next() {
			TNode ret = node;
			node = node.brother;
			return ret;
		}

		public void remove() {}

		public Iterator<TNode> iterator() {
			return this;
		}		
	}
	
	public static Iterable<TNode> getNodeSons(TNode node)
	{
		return new BrotherIterator(node.first_son);
	}
	
	/**
	 * @return array of edges - each edge is represented with two deepord indexes of beginning and ending node  
	 */
	public int[][] getEdges()
	{
		if (getCountOfNodes() <= 0) return null;
		
		int [][] ret = new int[getCountOfNodes()-1][];
		
		getEdgesDeepOrd(ret, node_array[0], 0, -1);
		
		return ret;		
	}
	
	/**
	 * 
	 * @param edges
	 * @param node - currently processed node 
	 * @param position - deep order of currently processed node
	 * @param parent - deep order of parent of currently processed node
	 * @return - deep order of next processed node
	 */
	private int getEdgesDeepOrd(int[][] edges, TNode node, int position, int parent)
	{		
		int next_pos = position; // position of next processed node
		
		if (node.first_son != null)
		{
			edges[position] = createEdge(position, position+1);
			next_pos = getEdgesDeepOrd(edges, node.first_son, position+1, position);
		}
		
		if (node.brother != null)
		{
			edges[next_pos] = createEdge(parent, next_pos+1);
			next_pos = getEdgesDeepOrd(edges, node.brother, next_pos+1, parent);			
		}		
		
		return next_pos;		
	}

	
	private static int[] createEdge(int a, int b)
	{
		int[] ret = new int[2];
		ret[0] = a;
		ret[1] = b;
		
		return ret;
	}
	

	/**
	 * Replace the original value of the node attribute by a new one. 
	 * @param node_index - depthOrder index of the node
	 * @param attr_index - attribute index
	 * @param value - new value to set
	 */
	public void setNodeAttributeValue(int node_index, int attr_index, String value)
	{
    	TAHLine val = new TAHLine();
    	val.Value = value;

    	node_array[node_index].getSetOfAttributes(0).AHTable[attr_index] = val;        	        	        			
	}

	public String getFirstNodeAttributeValue(int node_index, int attr_index)
	{
    	return node_array[node_index].getValue(0, attr_index, 0);        	        	        			
	}
	
	public TNode getNodeByDeepOrd(int deep_ord)
	{
		return node_array[deep_ord];		
	}
	
	/**
	 * @param attr_index - index of attribute to match
	 * @param value - desired value
	 * @return - if success, the return value is deep order index of the first matching node, otherwise -1 .
	 */
	public int findFirstNodeByAttributeValue(int attr_index, String value)
	{
		for (int i=0; i<node_array.length; i++) {
			String actual_valeu = getFirstNodeAttributeValue(i, attr_index);  
			if (actual_valeu != null && actual_valeu.equals(value)) return i;
		}
		
		return -1;
	}

	public static int findFirstNodeByAttributeValueInForest(NGForest forest, int attr_index, String value)
	{
		int ret = 0;
		for (NGTree tree : forest.getTrees()) {
			CZSemTree czsem_tree = new CZSemTree(tree);
			
			int i = czsem_tree.findFirstNodeByAttributeValue(attr_index, value);
			if (i == -1) ret += czsem_tree.tree.getNumberOfNodes();
			else return ret + i;
		}
		
		return -1;
	}
	
	protected void initNodeArray()
	{
		node_array = tree.getNodesArray();
	}
    
	public int readTree (NGTreeHead head, String source)
	{
		return readTree(head, source.toCharArray(), 0, head.getSize());
	}

	public int readTree (NGTreeHead head, char[] p_source, int start_position, int p_number_of_attributes)
	{
		int ret = tree.readTree(head, p_source, start_position, p_number_of_attributes);
		initNodeArray();
		return ret;
	}

	public String getSentenceString(NGTreeHead head) {
		return tree.getSentenceString(head);
	}
	
	public String getNodeID(int node_index)
	{
		String root_id = getFirstNodeAttributeValue(0, id_attr);
		String deep_ord = getFirstNodeAttributeValue(node_index, deepord_attr);
				
//		return Maker.makeAtomString(root_id+ "d" +deep_ord);		
		return root_id+ "d" +deep_ord;		
	}


	
}
