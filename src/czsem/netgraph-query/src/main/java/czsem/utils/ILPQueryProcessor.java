package czsem.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import cz.cuni.mff.mirovsky.trees.TNode;
import czsem.ILP.ProgolWork1;
import czsem.net.NetgraphServerComunication.LoadTreeResult;
import czsem.net.NetgraphServerComunication.LoadTreeResult.SingleMatch;
import czsem.utils.NetgraphQuery.ResultProcessor;

public class ILPQueryProcessor implements ResultProcessor
{
	private ProgolWork1 ilp_task;
	private String last_tree_id = ""; 
	
	public ILPQueryProcessor(ProgolWork1 ilp_task)
	{
		this.ilp_task = ilp_task;
	}
	
	public static class MaxTreesException extends Exception
	{
		public MaxTreesException(int num_trees)
		{
			super("Maximum nuber of trees (" + num_trees + ") reached.");			
		}
		
		private static final long serialVersionUID = 1L;
	};
	
	int tree_num = 0;
	int max_trees = 50000;
	
	boolean neagtive = false;
	
	public static int[] interest_attr = {6, 20, 35, 40, 42, 43, 44};
	
	public static String ASCIINormalise(String src)
	{
		if (src == null) return "null";
		String norm = Normalizer.normalize(src, Form.NFD); 
		norm = norm.replaceAll("[^\\p{ASCII}]", "");		
		return norm.toLowerCase();
		
	}

	public static String VarNormalise(String src)
	{
		String norm = ASCIINormalise(src); 
//		norm = norm.replaceAll("[-+^#*()!?,.:`';/{} \\\\\"\\[\\]]", "_");
		norm = norm.replaceAll("[^\\p{Alpha}]", "_");
		
		if (! Character.isLetter((norm.charAt(0))))
		{
			try
			{
				Integer.parseInt(norm); 
			}
			catch (NumberFormatException e)
			{
				norm = "x" + norm;
			}
		}
		return norm;
		
	}


	/**
	 * @deprecated Use CZSemTree.getNodeID instead.
	 * @param node_index
	 */
	private String getNodeStr(int node_index)
	{
		return "node" +tree_num+ "_" + node_index;
	}
	
	private void printAttributes(NGTreeHead head, CzsemTree tree, int node_index)	
	{
		for (int attr = 0; attr < interest_attr.length; attr++) {
			String val = tree.getFirstNodeAttributeValue(node_index, interest_attr[attr]);
			if (val != null)
			{
				ilp_task.getTree_out().print(VarNormalise(head.getAttributeAt(interest_attr[attr]).getName()));
				ilp_task.getTree_out().print("(" + tree.getNodeID(node_index) + ", ");
				ilp_task.getTree_out().print(VarNormalise(val) + ").");

				ilp_task.getTree_out().println("         const(" + VarNormalise(val) + ").");
				
				if (interest_attr[attr] == 44) //   m/tag
				{
/**					if (val.charAt(10) == 'N')
						ilp_task.getTree_out().println("negation("+ getNodeStr(node_index) +").");
/**/
					for (int i = 0; i < 15; i++)
					{
						char ch = val.toLowerCase().charAt(i);
						if (ch != '-')
							ilp_task.getTree_out().print("m_tag"+ i +"("+tree.getNodeID(node_index)+",'" + ch +"'). const('"+ch+"'). ");			
					}
					ilp_task.getTree_out().println();

				}
			}			
		}		
	}
/*	
	public void processSingleTreeResult3(LoadTreeResult tree_result) throws Exception
	{
		//negative
		if (isNeagtive()) ilp_task.getTree_out().print(":- ");

		ilp_task.getTree_out().println("injured("+VarNormalise(tree_result.tree.getFirstNodeAttributeValue(0, 23)) +").");

		tree_num++;
		
		if (tree_num > getMaxTrees()) 
			throw new Exception("Maximum nuber of trees reached (" + tree_num + ")."); 		
	}
*/
	public void processSingleTreeResult(LoadTreeResult tree_result) throws MaxTreesException
	{
		String cur_tree_id = tree_result.tree.getNodeID(0);
		
		if (cur_tree_id.compareTo(last_tree_id) != 0)
		{
			printSingleTreeResult(tree_result);
			last_tree_id = cur_tree_id;
		}
		
		printSingleTreeResultMatches(tree_result);

		tree_num++;
		
		if (tree_num > getMaxTrees())
			throw new MaxTreesException(tree_num-1); 				
	}

	public void printSingleTreeResultMatches(LoadTreeResult tree_result)
	{
		for (SingleMatch tree_match : tree_result.query_match) {
			//printtSingleTreeResultMatch
			ilp_task.getQuery_match_out().print(
					":- contains_num_injured(" +
					tree_result.tree.getNodeID(tree_match.matching_node_index) +
					").");

			
			//printtSingleTreeResultMatcheInfo
			ilp_task.getQuery_match_out().print(
					" % " + ASCIINormalise(
								tree_result.tree.getFirstNodeAttributeValue(
									tree_match.matching_node_index, 35)) /*t_lemma*/+
					" (");			
			//print all hidden (analytical) sons
			for (TNode son : CzsemTree.getNodeSons(
					tree_result.tree.getNodeByDeepOrd(
							tree_match.matching_node_index)))
			{
				String hide = son.getValue(0, 49 /*hide*/, 0);
				if (hide!=null && hide.compareTo("hide")==0)
				{
					ilp_task.getQuery_match_out().print(
							ASCIINormalise(son.getValue(0, 42 /*m/form*/, 0)) + " ");					
				}				
			}
			ilp_task.getQuery_match_out().println(")");						
		}			
	}

	public void printSingleTreeResult(LoadTreeResult tree_result)
	{
		ilp_task.getTree_out().println("\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		ilp_task.getTree_out().println       ("% " + ASCIINormalise(tree_result.tree.getSentenceString(tree_result.tree_head)));
		ilp_task.getQuery_match_out().println("% " + ASCIINormalise(tree_result.tree.getSentenceString(tree_result.tree_head)));
		
		ilp_task.getTree_out().println("tree_root("+tree_result.tree.getNodeID(0)+").");
		
		//negative
//		if (isNeagtive()) ilp_task.getTree_out().print(":- ");
			
//		ilp_task.getTree_out().println("injured("+getNodeStr(0)+").");
			
		
		for (int i = 0; i < tree_result.tree.getCountOfNodes(); i++)			
		{
			ilp_task.getTree_out().println("%%%%%%%% " + getNodeStr(i) + " %%%%%%%%%%%%%%%%%%%");
			ilp_task.getTree_out().println("node("+tree_result.tree.getNodeID(i)+").");
			
			printAttributes(tree_result.tree_head, tree_result.tree, i);

//			ilp_task.getTree_out().println("node_attr(node" +tree_num+ "_"+i+","+ ASCIINormalise(tree_result.tree.getFirstNodeAttributeValue(i, 35)) +").");
//			ilp_task.getTree_out().println("const(" + ASCIINormalise(tree_result.tree.getFirstNodeAttributeValue(i, 35)) +").");
		} 

		
		//EDGES
		for (int i=0; i < tree_result.tree.getEdges().length; i++)
		{
			ilp_task.getTree_out().println(
					"edge(" +
					tree_result.tree.getNodeID(
							tree_result.tree.getEdges()[i][0])
					+ ", " +
					tree_result.tree.getNodeID(
							tree_result.tree.getEdges()[i][1])
					+ ").");
		}
	}

	public void startProcessing()
	{}
	
	public void endProcessing(LoadTreeResult last_result)
	{}

	public boolean isNeagtive() {
		return neagtive;
	}

	public void setNeagtive(boolean neagtive) {
		this.neagtive = neagtive;
	}

	public int getMaxTrees() {
		return max_trees;
	}

	public void setMaxTrees(int max_trees) {
		this.max_trees = max_trees;
	}
}
