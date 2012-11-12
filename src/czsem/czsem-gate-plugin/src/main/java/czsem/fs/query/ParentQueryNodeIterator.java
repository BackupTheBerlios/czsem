package czsem.fs.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import czsem.fs.query.FsQuery.NodeMatch;
import czsem.fs.query.FsQuery.QueryMatch;
import czsem.fs.query.FsQuery.QueryNode;

public class ParentQueryNodeIterator implements Iterator<QueryMatch> {
	protected List<QueryNode> queryNodes;
	protected Iterator<Integer>[] dataNodesIterators;
	protected Iterator<QueryMatch>[] resultsIterators;
	protected QueryMatch[] lastMatches;
	protected NodeMatch parentNodeMatch;
	private boolean empty = false;
	private boolean foundNext = false;
	

	@SuppressWarnings("unchecked")
	public ParentQueryNodeIterator(NodeMatch parentNodeMatch, List<QueryNode> queryNodes, Iterable<Integer> dataNodes) {
		this.queryNodes = queryNodes;
		this.parentNodeMatch = parentNodeMatch;
		
		dataNodesIterators = new Iterator[queryNodes.size()];		
		for (int i = 0; i < dataNodesIterators.length; i++) {
			dataNodesIterators[i] = dataNodes.iterator();			
		}
		
		resultsIterators = new Iterator[queryNodes.size()];
		for (int i = 0; i < resultsIterators.length; i++) {
			resultsIterators[i] = findNewResultIterator(i);
			
			if (resultsIterators[i] == null) empty = true;
		}
		
		if (! empty) {
			lastMatches = new QueryMatch[queryNodes.size()];
			for (int i = 0; i < resultsIterators.length; i++) {
				lastMatches[i] = resultsIterators[i].next();
			}
		}
		

	}


	private Iterator<QueryMatch> findNewResultIterator(int i) {
		while (dataNodesIterators[i].hasNext())
		{
			Iterable<QueryMatch> res = queryNodes.get(i).getResultsFor(dataNodesIterators[i].next());			
			if (res != null) return res.iterator();
		}
		
		return null;
	}
	

	@Override
	public boolean hasNext() {
		if (empty) return false;		
		if (foundNext) return true;
		
		
		for (int i = 0; i < dataNodesIterators.length; i++) {
			if (tryMove(i)) {
				return foundNext = true;
			}
		}
		
		return false;
	}


	private boolean tryMove(int i) {
		if (resultsIterators[i].hasNext())
		{
			lastMatches[i] = resultsIterators[i].next();
			return true;
		}
		
		resultsIterators[i] = findNewResultIterator(i);
		if (resultsIterators[i] == null) return false;
		
		return tryMove(i);		
	}


	@Override
	public QueryMatch next() {
		foundNext = false;

		List<NodeMatch> matchingNodes = new ArrayList<FsQuery.NodeMatch>();
		
		matchingNodes.add(parentNodeMatch);
		
		for (int i = 0; i < lastMatches.length; i++) {
			matchingNodes.addAll(lastMatches[i].getMatchingNodes());
		}
		
		return new QueryMatch(matchingNodes);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
