package czsem.utils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import cz.cuni.mff.mirovsky.account.UserAccount;
import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGTree;
import czsem.net.NetgraphServerComunication;
import czsem.net.NetgraphServerComunication.LoadTreeResult;
import czsem.net.NetgraphServerComunication.NetgraphConnectionInfo;
import czsem.net.NetgraphServerComunication.QueryStatistics;
import czsem.net.NetgraphServerComunication.TreeSubtypeChars;
import czsem.net.NetgraphServerComunication.LoadTreeResult.SingleMatch;
import czsem.utils.NetgraphQuery.ResultProcessor;


public class SimpleXMLQueryProcessor implements ResultProcessor {
	public static class Selector
	{
		public int query_tree_index;
		public int query_node_index;
		public String node_name;		
		public String attribute_name;
		
		public int attribute_index;

		public Selector(String node_name, String attribute_name, NetgraphQuery nq)
		{
			this.node_name = node_name;
			this.attribute_name = attribute_name;
			this.query_node_index = -1;
			this.query_tree_index = -1;					
			
			int actual_tree = 0;
			NGForest query_forest = nq.getQueryForest();			
			for (NGTree tree : query_forest.getTrees()) {
				CZSemTree czsem_tree = new CZSemTree(tree);
				int found = czsem_tree.findFirstNodeByAttributeValue(AttributeIndexes.NAME, node_name);
				if (found != -1)
				{
					this.query_node_index = found;
					this.query_tree_index = actual_tree;					
				}				
				actual_tree++;
			}
			
			 attribute_index = query_forest.getHead().getIndexOfAttribute(attribute_name);
		}
		
		public String getMatchingValue(CZSemTree tree, SingleMatch tree_match)
		{
			return tree.getFirstNodeAttributeValue(
							tree_match.matching_node_index,
							attribute_index);
		}

		public SingleMatch getFirstMatch(LoadTreeResult tree_result)
		{
			// find corresponding node in the result tree			
			for (SingleMatch tree_match : tree_result.query_match) {
				if (matchAQuery(tree_match))
				{
					return tree_match;
				}
			}
			
			return null;
		}

		public String getFirstMatchingValue(LoadTreeResult tree_result)
		{
			return getMatchingValue(tree_result.tree, getFirstMatch(tree_result));
		}
		
		
		public boolean matchAQuery(SingleMatch tree_match)
		{
			return 
				tree_match.query_tree_index == query_tree_index &&
				tree_match.query_node_index == query_node_index;
		}
	}

	public static class AttributeIndexes
	{
		public static int ID;		
		public static int NAME;		

		public static void initAttributeIndexes(NetgraphServerComunication nc)
		{
			ID = nc.getGlobalHead().getIndexOfAttribute("id");
			if (ID == -1) ID = nc.getGlobalHead().getIndexOfAttribute("ann_id");
			
			NAME = nc.getGlobalHead().getIndexOfAttribute("_name");
		}
	}

	private XMLStreamWriter out;
	private NetgraphServerComunication nc;
	private String [] selectors_str;
	private ArrayList<Selector> selectors;
	
	public SimpleXMLQueryProcessor(String [] selectors, OutputStream out, NetgraphServerComunication nc) throws XMLStreamException
	{
		this.selectors_str = selectors;
		XMLOutputFactory factory = XMLOutputFactory.newInstance();		
		this.out = factory.createXMLStreamWriter(out, "UTF-8");
		this.nc = nc;
	}
	
	protected void parseSelectors(NetgraphQuery nq)
	{
		this.selectors = new ArrayList<Selector>();
		
		for (String sel : selectors_str) {
			StringTokenizer st = new StringTokenizer(sel, ".");
			
			this.selectors.add(new Selector(st.nextToken(), st.nextToken(), nq));
		}
	}
	
	public void stratXMLOtput() throws XMLStreamException
	{
		out.writeStartDocument("UTF-8", "1.0");
		out.writeStartElement("QueryResult");
	}
	
	public void closeXMLOtput() throws XMLStreamException
	{
		out.writeEndDocument();
		out.close();
	}
	
	public void writeUserInfo (UserAccount i) throws XMLStreamException
	{
		out.writeStartElement("UserAccount");

//	    out.writeAttribute("login_name", i.getLogin_name()); //returns always anonymous
	    out.writeAttribute("user_name", i.getUserName()); 
		out.writeAttribute("max_number_of_trees", Long.toString(i.getMaxNumberOfTrees()));
				
		out.writeEndElement();		
	}

	public void writeQueryInfo (String search_path, NetgraphQuery nq) throws XMLStreamException
	{
		AttributeIndexes.initAttributeIndexes(nc); 
		parseSelectors(nq);

		out.writeStartElement("Query");
		out.writeAttribute("time_stamp", new Date().toString());

		out.writeAttribute("search_path", search_path);
		out.writeAttribute("invert_match", Boolean.toString(nq.isInvertMatch()));
		out.writeAttribute("first_match_only", Boolean.toString(nq.isFirstMatchOnly()));
		out.writeAttribute("above_previous_query", Boolean.toString(nq.isAbovePreviousQuery()));
		out.writeAttribute("match_lemma_variants", Boolean.toString(nq.isMatchLemmaVariants()));
		out.writeAttribute("match_lemma_comments", Boolean.toString(nq.isMatchLemmaComments()));
		
		switch (nq.getResultTreeSubtype()) {
		case TreeSubtypeChars.GET_TREE_SUBTYPE_CONTEXT:
			out.writeAttribute("result_tree_subtype", "GET_TREE_SUBTYPE_CONTEXT");
			break;
		case TreeSubtypeChars.GET_TREE_SUBTYPE_OCCURENCE:
			out.writeAttribute("result_tree_subtype", "GET_TREE_SUBTYPE_OCCURENCE");
			break;
		case TreeSubtypeChars.GET_TREE_SUBTYPE_TREE:
			out.writeAttribute("result_tree_subtype", "GET_TREE_SUBTYPE_TREE");
			break;
		case TreeSubtypeChars.GET_TREE_SUBTYPE_FIRST:
			out.writeAttribute("result_tree_subtype", "GET_TREE_SUBTYPE_FIRST");
			break;
		default:
			out.writeAttribute("result_tree_subtype", Character.toString(nq.getResultTreeSubtype()));			
		}
			 
		out.writeStartElement("QueryString");
		out.writeCharacters(nq.getQueryString());
		out.writeEndElement();		

		
		out.writeStartElement("Selectors");
		
		for (String selector : selectors_str) {
			out.writeStartElement("Selector");
			out.writeCharacters(selector);
			out.writeEndElement();					
		}
		out.writeEndElement();		
	}

	public void writeNetgraphQueryStatistics (QueryStatistics i) throws XMLStreamException
	{
		out.writeStartElement("QueryStatistics");

		out.writeAttribute("time_stamp", new Date().toString());
		
		out.writeAttribute("number_of_actual_occurrence", Integer.toString(i.number_of_actual_occurrence));
		out.writeAttribute("number_of_actual_tree", Integer.toString(i.number_of_actual_tree));
		out.writeAttribute("number_of_found_occurences", Integer.toString(i.number_of_found_occurences));
		out.writeAttribute("number_of_found_trees", Integer.toString(i.number_of_found_trees));
		out.writeAttribute("number_of_searched_trees", Integer.toString(i.number_of_searched_trees));		

		out.writeEndElement();
	}

	public void writeNetgraphConnectionInfo (
			NetgraphConnectionInfo i) throws XMLStreamException
	{
		out.writeStartElement("NetgraphConnectionInfo");
		
	    out.writeAttribute("server_address", nc.getInetAddress());
	    out.writeAttribute("server_port", Integer.toString(nc.getPort()));
	    out.writeAttribute("server_version", i.server_version);
	    out.writeAttribute("client_required_version", i.client_required_version);
	    out.writeAttribute("path_actual", i.path_actual);
	    out.writeAttribute("client_recommended_version", i.client_recommended_version);
	    out.writeAttribute("corpus_identifier", i.corpus_identifier);		

		out.writeEndElement();
	}	
	
	public void processSingleTreeResult(LoadTreeResult tree_result) throws XMLStreamException {
		out.writeStartElement("Match");
	    out.writeAttribute("file_name", tree_result.filename);
	    out.writeAttribute("root_id", tree_result.tree.getFirstNodeAttributeValue(0, AttributeIndexes.ID));
	    out.writeAttribute("match_string", tree_result.query_match_str);
		
		out.writeStartElement("Sentence");
		out.writeCharacters(tree_result.tree.getSentenceString(nc.getGlobalHead()));		
		out.writeEndElement();
		
		out.writeStartElement("Data");
		for (Selector selector : selectors) {
			
			
			// find corresponding node in the result tree			
			for (SingleMatch tree_match : tree_result.query_match) {
				if (selector.matchAQuery(tree_match))
				{
					//corresponding found - write its value to the output 					
					out.writeStartElement("Value");
					out.writeAttribute("variable_name", selector.node_name);
					out.writeAttribute("attribute_name", selector.attribute_name);

					out.writeCharacters(selector.getMatchingValue(tree_result.tree, tree_match));

					out.writeEndElement();			
				}
			}			
		}
		out.writeEndElement();
		
		out.writeEndElement();		
	}

	public void endProcessing(LoadTreeResult last_result) throws XMLStreamException {
		out.writeEndElement();		
	}

	public void startProcessing() throws XMLStreamException {
		out.writeStartElement("QueryMatches");		
	}

}
