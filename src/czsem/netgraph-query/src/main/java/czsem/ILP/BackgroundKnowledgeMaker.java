package czsem.ILP;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.net.NetgraphServerComunication.LoadTreeResult;
import czsem.utils.CzsemTree;

public class BackgroundKnowledgeMaker extends DataMaker {
	private String last_filename;
	
	private Set<String>[] attribute_values_bufeer;
	
	public BackgroundKnowledgeMaker(){}

	public BackgroundKnowledgeMaker(String output_file) throws FileNotFoundException, UnsupportedEncodingException {
		super(output_file);
	}

	@SuppressWarnings("unchecked")
	protected void initAttributeValuesBufeer()
	{
		attribute_values_bufeer = new HashSet[PRINT_ATTRIBUTES.length];
		
		for (int i=0; i<attribute_values_bufeer.length; i++) {
			attribute_values_bufeer[i] = new HashSet<String>();
		}
	}

	protected void printModeDeclarations(NGTreeHead head)
	{
/*
		relevant_node(Node):-t_lemma(Node,L),member(L,['100','550','900','39.5','5','30','8','700','800','1.5','10','50'])
		
		%use_module(library(lists)).
		%relevant_node(Node):-t_lemma(Node,L),member(L,['100','550','900','39.5','5','30','8','700','800','1.5','10','50']).

		%B = [qT_jihomoravsky49921_txt_001_p2s2d7,qT_jihomoravsky51460_txt_001_p2s5d2,qT_jihomoravsky51460_txt_001_p2s5d20,qT_jihomoravsky51460_txt_001_p3s1d15,qT_jihomoravsky54536_txt_001_p2s6d7,qT_jihomoravsky56560_txt_001_p2s2d5,qT_jihomoravsky55629_txt_001_p1s5d15,qT_jihomoravsky50788_txt_001_p1s4d4,qT_jihomoravsky55918_txt_001_p4s2d6,qT_jihomoravsky55789_txt_001_p2s7d21,qT_jihomoravsky55789_txt_001_p2s7d26,qT_jihomoravsky56177_txt_001_p2s4d9,qT_jihomoravsky56177_txt_001_p2s10d16,qT_jihomoravsky49737_txt_001_p1s3d12,qT_jihomoravsky56184_txt_001_p1s7d17,qT_jihomoravsky56184_txt_001_p3s1d8,qT_jihomoravsky54637_txt_001_p1s1d4,qT_jihomoravsky51054_txt_001_p3s5d8,qT_jihomoravsky50885_txt_001_p1s7d5,qT_jihomoravsky48749_txt_001_p2s3d10].
		
relevant_node(qT_jihomoravsky49921_txt_001_p2s2d7).
relevant_node(qT_jihomoravsky51460_txt_001_p2s5d2).
relevant_node(qT_jihomoravsky51460_txt_001_p2s5d20).
relevant_node(qT_jihomoravsky51460_txt_001_p3s1d15).
relevant_node(qT_jihomoravsky54536_txt_001_p2s6d7).
relevant_node(qT_jihomoravsky56560_txt_001_p2s2d5).
relevant_node(qT_jihomoravsky55629_txt_001_p1s5d15).
relevant_node(qT_jihomoravsky50788_txt_001_p1s4d4).
relevant_node(qT_jihomoravsky55918_txt_001_p4s2d6).
relevant_node(qT_jihomoravsky55789_txt_001_p2s7d21).
relevant_node(qT_jihomoravsky55789_txt_001_p2s7d26).
relevant_node(qT_jihomoravsky56177_txt_001_p2s4d9).
relevant_node(qT_jihomoravsky56177_txt_001_p2s10d16).
relevant_node(qT_jihomoravsky49737_txt_001_p1s3d12).
relevant_node(qT_jihomoravsky56184_txt_001_p1s7d17).
relevant_node(qT_jihomoravsky56184_txt_001_p3s1d8).
relevant_node(qT_jihomoravsky54637_txt_001_p1s1d4).
relevant_node(qT_jihomoravsky51054_txt_001_p3s5d8).
relevant_node(qT_jihomoravsky50885_txt_001_p1s7d5).
relevant_node(qT_jihomoravsky48749_txt_001_p2s3d10).


*/
		


		tree_out.println();			
		tree_out.println("%:- set(evalfn,posonly).");			
		tree_out.println(":- set(noise,0).");			
		tree_out.println(":- set(clauselength,20).");			
		tree_out.println(":- set(i,15).");			
		tree_out.println();		
		tree_out.println("% orlemma(N,B,C) :- t_lemma(N,B), t_lemma(C), C\\=B.");
		tree_out.println("% orlemma(N,B,C) :- t_lemma(N,C), t_lemma(B), C\\=B.");
		tree_out.println();
		tree_out.println("optional_double_edge(N1,N2) :- edge(N1,N2).");
		tree_out.println("optional_double_edge(N1,N2) :- edge(N1,N),edge(N,N2).");
		tree_out.println();
		tree_out.println(":- mode(1,tree_root(+node)).");			
		tree_out.println(":- mode(1,relevant_node(+node)).");			
		tree_out.println(":- mode(1,root_in_file(+node,-file)).");			
		tree_out.println(":- mode(*,edge(+node,-node)).");			
		tree_out.println(":- mode(1,edge(-node,+node)).");			
		tree_out.println();			
		tree_out.println(":- mode(*,optional_double_edge(+node,-node)).");			
		tree_out.println(":- mode(1,optional_double_edge(-node,+node)).");			
		tree_out.println();			
		tree_out.println(":- determination(relevant_node/1,edge/2).");			
		tree_out.println(":- determination(relevant_node/1,root_in_file/2).");			
		tree_out.println(":- determination(relevant_node/1,optional_double_edge/2).");			
		tree_out.println();			

		for (int attr = 0; attr < PRINT_ATTRIBUTES.length; attr++) {
			String attr_name = makeAtomString(head.getAttributeAt(PRINT_ATTRIBUTES[attr]).getName()); 
			
			tree_out.print(":- mode(1,");			
			tree_out.print(attr_name);
			tree_out.print("(+node,#");			
			tree_out.print(attr_name);
			tree_out.println(")).");
			
			tree_out.print(":- determination(relevant_node/1,");			
			tree_out.print(attr_name);
			tree_out.println("/2).");			
		}				
	}

	protected void printAttributesOfNode(LoadTreeResult tree_result, int node_index)
	{
		CzsemTree tree = tree_result.tree;
		NGTreeHead head = tree_result.tree_head;
		
/*		
		for (int aa=0; aa<head.getSize(); aa++)
		{
			System.err.print(aa);				
			System.err.print(", //");				
			System.err.println(head.getAttributeAt(aa).getName());			
		}
*/
		
		for (int attr = 0; attr < PRINT_ATTRIBUTES.length; attr++) {
			String val = tree.getFirstNodeAttributeValue(node_index, PRINT_ATTRIBUTES[attr]);
			if (val != null)
			{
				printCaluseQuoted(
						head.getAttributeAt(PRINT_ATTRIBUTES[attr]).getName(),
						tree.getNodeID(node_index),
						val);
				
				attribute_values_bufeer[attr].add(val);
				
/*
				if (PRINT_ATTRIBUTES[attr] == 44) //   m/tag
				{
//					if (val.charAt(10) == 'N')
//						ilp_task.getTree_out().println("negation("+ getNodeStr(node_index) +").");

					for (int i = 0; i < 15; i++)
					{
						char ch = val.toLowerCase().charAt(i);
						if (ch != '-')
							ilp_task.getTree_out().print("m_tag"+ i +"("+tree.getNodeID(node_index)+",'" + ch +"'). const('"+ch+"'). ");			
					}
					ilp_task.getTree_out().println();
				}
*/
			}			
		}					
	}
	
	public void processSingleTreeResult(LoadTreeResult tree_result)
			throws Exception {
		
		tree_out.println("\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		tree_out.println("% " + tree_result.tree.getSentenceString(tree_result.tree_head));
		
		String cur_filename = stripFilename(tree_result.filename); 
		if (cur_filename.compareTo(last_filename) != 0)
		{
			printCaluse("file", cur_filename);
			last_filename = cur_filename;
		}
		printCaluse("tree_root", tree_result.tree.getNodeID(0));		
		printCaluse("root_in_file", tree_result.tree.getNodeID(0), stripFilename(tree_result.filename));

		
		for (int i = 0; i < tree_result.tree.getCountOfNodes(); i++)			
		{
			tree_out.println("%%%%%%%% NODE " + tree_result.tree.getNodeID(i) + " %%%%%%%%%%%%%%%%%%%");
			printCaluse("node", tree_result.tree.getNodeID(i));
			
			printAttributesOfNode(tree_result, i);
		} 
		
		//EDGES
		for (int i=0; i < tree_result.tree.getEdges().length; i++)
		{
			printCaluse("edge", 
					tree_result.tree.getNodeID(tree_result.tree.getEdges()[i][0]),
					tree_result.tree.getNodeID(tree_result.tree.getEdges()[i][1]));
		}		
	}

	public static void main(String[] args) throws Exception {
		BackgroundKnowledgeMaker m = new BackgroundKnowledgeMaker("C:\\WorkSpace\\aleph\\trees1.b");
		m.setSearchPath(makeSearchPath(train_set));
		m.performOutput();
	}

	public void startProcessing() throws Exception {
		last_filename = "";
		initAttributeValuesBufeer();		
	}
	
	public void endProcessing(LoadTreeResult last_result) throws Exception {
		
		for (int a=0; a< attribute_values_bufeer.length; a++) {
			String attr_name = last_result.tree_head.getAttributeAt(PRINT_ATTRIBUTES[a]).getName();
			
			for (String val : attribute_values_bufeer[a]) {
				printCaluseQuoted(attr_name, val);			
			}
		}
		
		printModeDeclarations(last_result.tree_head);
	}

}
