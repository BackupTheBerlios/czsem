package czsem.utils;

import java.io.IOException;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.net.NetgraphServerComunication;
import czsem.net.NetgraphProtocolConnection.NetgraphProtocolException;
import czsem.net.NetgraphServerComunication.LoadTreeResult;
import czsem.net.NetgraphServerComunication.TreeSubtypeChars;

public class NetgraphQuery {
	private String queryString;
	private boolean invertMatch = false;
	private boolean firstMatchOnly = false;
	private boolean abovePreviousQuery = false;
	private boolean matchLemmaVariants = true;
	private boolean matchLemmaComments = true;
	 
	/** @see NetgraphServerComunication.loadNextTree */
	private char resultTreeSubtype = TreeSubtypeChars.GET_TREE_SUBTYPE_OCCURENCE;

	private NetgraphServerComunication netgraph_comunication;
	
	public interface ResultProcessor
	{
		void startProcessing() throws Exception;
		void processSingleTreeResult(LoadTreeResult tree_result) throws Exception;
		void endProcessing(LoadTreeResult last_result) throws Exception;
	}
	
	public static class EmptyProcessor implements ResultProcessor 
	{
		public void startProcessing() throws Exception {}
		public void processSingleTreeResult(LoadTreeResult tree_result){}
		public void endProcessing(LoadTreeResult last_result) throws Exception {}		
	}

	
	public NetgraphQuery(){};

	public NetgraphQuery(String query_string, NetgraphServerComunication netgraph_comunication)
	{
		this.queryString = query_string;
		this.netgraph_comunication = netgraph_comunication;
	}
	
	public void startQueryAll() throws IOException, NetgraphProtocolException
	{
		netgraph_comunication.queryAll();
	}

	public void startTheQuery() throws IOException, NetgraphProtocolException
	{
		netgraph_comunication.query(queryString, invertMatch, firstMatchOnly, abovePreviousQuery, matchLemmaVariants, matchLemmaComments);
	}
	
	/**
	 * 
	 * @param rp
	 * @return number of loaded trees
	 * @throws Exception 
	 */
	public int processResult(ResultProcessor rp) throws Exception
	{
		rp.startProcessing();
		
		LoadTreeResult valid_res = null;
		
		int num = 0;
		for (;;num++)
		{
			LoadTreeResult res = netgraph_comunication.loadNextTree(resultTreeSubtype);
			if (res == null) break; 
			rp.processSingleTreeResult(res);
			valid_res = res;
		}
		
		rp.endProcessing(valid_res);		
		return num;		
	}
	
	public NGForest getQueryForest()
	{
		return getQueryForest(queryString, netgraph_comunication);
	}

	public static NGForest getQueryForest(String queryString, NetgraphServerComunication netgraph_comunication)
	{
		NGTreeHead global_head = netgraph_comunication.getGlobalHead();
		
		NGForest query_forest = new NGForest(null);
		query_forest.setHead(global_head);
		query_forest.readForest(queryString.toCharArray(), 0, global_head.getSize());

		//set all attributes as selected 
		for (int i = 0; i < global_head.getSize(); i++) {
			query_forest.getVybraneAtributy().addElement(global_head.getAttributeAt(i).getName());
		}
		
		return query_forest;
	}
	

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public boolean isInvertMatch() {
		return invertMatch;
	}

	public void setInvertMatch(boolean invertMatch) {
		this.invertMatch = invertMatch;
	}

	public boolean isFirstMatchOnly() {
		return firstMatchOnly;
	}

	public void setFirstMatchOnly(boolean firstMatchOnly) {
		this.firstMatchOnly = firstMatchOnly;
	}

	public boolean isAbovePreviousQuery() {
		return abovePreviousQuery;
	}

	public void setAbovePreviousQuery(boolean abovePreviousQuery) {
		this.abovePreviousQuery = abovePreviousQuery;
	}

	public boolean isMatchLemmaVariants() {
		return matchLemmaVariants;
	}

	public void setMatchLemmaVariants(boolean matchLemmaVariants) {
		this.matchLemmaVariants = matchLemmaVariants;
	}

	public boolean isMatchLemmaComments() {
		return matchLemmaComments;
	}

	public void setMatchLemmaComments(boolean matchLemmaComments) {
		this.matchLemmaComments = matchLemmaComments;
	}

	public char getResultTreeSubtype() {
		return resultTreeSubtype;
	}

	public void setResultTreeSubtype(char resultTreeSubtype) {
		this.resultTreeSubtype = resultTreeSubtype;
	}

	public NetgraphServerComunication getNetgraph_comunication() {
		return netgraph_comunication;
	}

	public void setNetgraphComunication(
			NetgraphServerComunication netgraph_comunication) {
		this.netgraph_comunication = netgraph_comunication;
	}
	
}
