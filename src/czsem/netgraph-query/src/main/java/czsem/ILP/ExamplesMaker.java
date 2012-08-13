package czsem.ILP;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import czsem.net.NetgraphServerComunication.LoadTreeResult;
import czsem.net.NetgraphServerComunication.TreeSubtypeChars;
import czsem.utils.SimpleXMLQueryProcessor.Selector;

public class ExamplesMaker extends DataMaker {

	public ExamplesMaker(String output_file) throws FileNotFoundException, UnsupportedEncodingException {
		super(output_file);
	}

	public ExamplesMaker() {};

	public void endProcessing(LoadTreeResult last_result) throws Exception {}

	public void processSingleTreeResult(LoadTreeResult tree_result)
			throws Exception {
		
		
		Selector s_damage_lemma = new Selector("damage", "t_lemma", current_query);
//		Selector s_id = new Selector("damage", "id", current_query);
		
		tree_out.println("\n% " + tree_result.tree.getSentenceString(tree_result.tree_head));
		tree_out.println("% " + s_damage_lemma.getFirstMatchingValue(tree_result));
		printCaluse("relevant_node", 
				tree_result.tree.getNodeID(
						s_damage_lemma.getFirstMatch(tree_result).matching_node_index));
	}

	public void startProcessing() throws Exception {}

	
	public static void main(String[] args) throws Exception {
		ExamplesMaker m = new ExamplesMaker("C:\\WorkSpace\\aleph\\trees1.f");
//		ExamplesMaker m = new ExamplesMaker();
		m.setSearchPath(makeSearchPath(train_set));
//		m.setQueryString("[t_lemma=vyčíslit|stanovit|odhadnout|dosahovat]([t_lemma=tisíc|milión,_transitive=true]([gram/sempos=adj.quant.def,_name=damage],[t_lemma=koruna,_optional=true,functor=MAT]),[_optional=true]([functor=PAT|ACT|RSTR,_optional=false,t_lemma=škoda|Škoda]),[t_lemma=vyšetřovatel,functor=ACT,_optional=true])");
		m.setQueryString("[t_lemma=vyčíslit|stanovit|odhadnout|dosahovat]([t_lemma=tisíc|milión,_transitive=true]([gram/sempos=adj.quant.def,_name=damage]))");
		
//		m.current_query.setResultTreeSubtype(TreeSubtypeChars.GET_TREE_SUBTYPE_TREE);
		m.current_query.setResultTreeSubtype(TreeSubtypeChars.GET_TREE_SUBTYPE_OCCURENCE);
		m.performOutput();
	}
}
