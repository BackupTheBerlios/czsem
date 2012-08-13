package czsem.ILP;

import java.io.PrintStream;

import czsem.net.NetgraphServerComunication;
import czsem.net.NetgraphServerComunication.TreeSubtypeChars;
import czsem.utils.ILPQueryProcessor;
import czsem.utils.NetgraphQuery;
import czsem.utils.ILPQueryProcessor.MaxTreesException;

public class ProgolWork1 {
	
	private String query_string;
	
	private PrintStream tree_out;
	private PrintStream query_match_out;
	
	public ProgolWork1(String query_string, PrintStream tree_out, PrintStream query_match_out)
	{
		this.tree_out = tree_out;
		this.query_match_out = query_match_out;
		this.query_string = query_string;
	}
	
	public void printHeadTrees()
	{
		tree_out.println("%%%%%%%%%%%%%%%%%%%%%%%");
		tree_out.println("% Declarations");
		tree_out.println("%:- set(nodes,500000)?");
		tree_out.println("%:- set(h,1000000)?");
		tree_out.println("%:- set(r,10000)?");
		tree_out.println(":- set(c,2),set(i,2),  set(inflate,800)?");
		tree_out.println(":- set(verbose,2)?");
		tree_out.println(":- modeh(1,tree_root(+node))?");
//		tree_out.println(":- modeh(1,negation(#node))?");
//		tree_out.println(":- modeh(1,injured(+const))?");
		tree_out.println(":- modeh(1,contains_num_injured(+node))?");		
		tree_out.println(":- modeb(*,edge(-node,+node))?");
		tree_out.println(":- modeb(*,edge(+node,-node))?");
		tree_out.println(":- modeb(*,edge(+node,+node))?");
		tree_out.println();		
		for (int i = 0; i < 15; i++)
		{
			tree_out.println(":- modeb(1,m_tag"+ i +"(+node,#const))?");			
		}
		tree_out.println();		
	}
	
	public void printAttributes(NetgraphServerComunication nc)
	{
		tree_out.println("% begin of definitions of linguistic attributes");			
		for (int i = 0; i < ILPQueryProcessor.interest_attr.length; i++)			
		{
			tree_out.println(":- modeb(1,"+ ILPQueryProcessor.VarNormalise(
					nc.getGlobalHead().getAttributeAt(ILPQueryProcessor.interest_attr[i]).getName()) +"(+node,#const))?");			
		}
		tree_out.println("% end of definitions of linguistic attributes");					
	}

	
	
	public void performOutput(int max_trees) throws Exception
	{
		NetgraphServerComunication nc = new NetgraphServerComunication();
		nc.openConnection("localhost", 2000);
		nc.login("dedek", "50eb3b47fbee4df59eaef6368261063b");
		
		nc.setSearchPathAndInitializeGlobalHead(nc.getCurrentDirectory());
		

		printHeadTrees();
		printAttributes(nc);
		
		NetgraphQuery prev_q = new NetgraphQuery("[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner,_optional=true],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant,_transitive=true]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity,_optional=true]))", nc);
		prev_q.startTheQuery();
		prev_q.processResult(new NetgraphQuery.EmptyProcessor());
		

		NetgraphQuery nq = new NetgraphQuery(query_string, nc);
		nq.setAbovePreviousQuery(true);
//		nq.setResultTreeSubtype(TreeSubtypeChars.GET_TREE_SUBTYPE_TREE);
		nq.setResultTreeSubtype(TreeSubtypeChars.GET_TREE_SUBTYPE_OCCURENCE);
				
		nq.startTheQuery();


		ILPQueryProcessor ilp_qp = new ILPQueryProcessor(this);
		ilp_qp.setMaxTrees(max_trees);

		try {
		
			nq.processResult(ilp_qp);
		
		} catch (MaxTreesException e) {
			//e.printStackTrace();
			System.err.println(e.getMessage());
		}
				
		nc.close();		
	}

	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
//		ILP ilp = new ILP("[deepord=0]", new PrintStream(args[1]), new PrintStream(args[2]));
		if (args.length < 2)
		{
			System.err.println("usage: ILP background_knowledge_file examples_file");
			return;
		}

		ProgolWork1 ilp = new ProgolWork1("[gram/sempos=*.quant.*]", new PrintStream(args[0]), new PrintStream(args[1]));
//		ILP ilp = new ILP("[gram/sempos=*.quant.*]", new PrintStream("backg.pl"), System.err);
		 
//		ILP ilp = new ILP("[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner,_optional=true],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant,_transitive=true]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity,_optional=true]))");
//		ILP ilp = new ILP("[t_lemma=Škoda]([hide=hide,m/lemma=Škoda-1,a/ord>1])");
//		ILP ilp = new ILP("[t_lemma=Trabant]([t_lemma=zdemolovaný])");
		
		
//		ilp.performOutput(Integer.MAX_VALUE);
		ilp.performOutput(500);
	}

	public PrintStream getTree_out() {
		return tree_out;
	}

	public PrintStream getQuery_match_out() {
		return query_match_out;
	}

}

/*
0: atree.rf
1: compl.rf
2: coref_gram.rf
3: coref_special
4: coref_text.rf
5: deepord
6: functor
7: gram/aspect
8: gram/degcmp
9: gram/deontmod
10: gram/dispmod
11: gram/gender
12: gram/indeftype
13: gram/iterativeness
14: gram/negation
15: gram/number
16: gram/numertype
17: gram/person
18: gram/politeness
19: gram/resultative
20: gram/sempos
21: gram/tense
22: gram/verbmod
23: id
24: is_dsp_root
25: is_generated
26: is_member
27: is_name_of_person
28: is_parenthesis
29: is_state
30: nodetype
31: quot/set_id
32: quot/type
33: sentmod
34: subfunctor
35: t_lemma
36: tfa
37: val_frame.rf
38: sentence
39: a/ref_type
40: a/afun
41: a/is_member
42: m/form
43: m/lemma
44: m/tag
45: w/token
46: w/no_space_after
47: a/ord
48: a/parent_id
49: hide
50: _transitive
51: _optional
52: _#sons
53: _#hsons
54: _#descendants
55: _#lbrothers
56: _#rbrothers
57: _depth
58: _#occurrences
59: _name
60: _sentence
61: _querymatch
*/


/*

System.out.println("\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
System.out.println("% N E G A T I V E      E X A M P L E S");

nq.setInvertMatch(true);
nq.startTheQuery();

//ilp_qp = new ILPQueryProcessor();
ilp_qp.setNeagtive(true);
ilp_qp.setMaxTrees(ilp_qp.getMaxTrees()*2);

try {

	nq.processResult(ilp_qp);

} catch (Exception e) {
	//e.printStackTrace();
	System.err.println("%-end of trees - negative-");
}

/**/
