package czsem.fs.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import czsem.Utils;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryNode;

public class ParentQueryNodeIterator implements Iterator<QueryMatch> {

	/* immutable properties */
	protected final List<QueryNode> queryNodes;
	private final Iterable<Integer> dataNodes;
	protected QueryMatch[] lastMatches;
	protected final NodeMatch parentNodeMatch;
	private boolean empty = false;
	private boolean foundNext = false;

	/* mutable properties */
	protected Iterator<Integer>[] dataNodesIterators;
	protected Iterator<QueryMatch>[] resultsIterators;
	

	public ParentQueryNodeIterator(NodeMatch parentNodeMatch, List<QueryNode> queryNodes, Iterable<Integer> dataNodes) {
		this.queryNodes = queryNodes;
		this.parentNodeMatch = parentNodeMatch;
		this.dataNodes = dataNodes;
		
		dataNodesIterators = Utils.convertToGenericArray(new Iterator[queryNodes.size()]);		
		for (int i = 0; i < dataNodesIterators.length; i++) {
			dataNodesIterators[i] = dataNodes.iterator();			
		}
		
		resultsIterators = Utils.convertToGenericArray(new Iterator[queryNodes.size()]);
		for (int i = 0; i < resultsIterators.length; i++) {
			resultsIterators[i] = findNewResultIterator(i);
			
			if (resultsIterators[i] == null) empty = true;
		}
		
		if (! empty) {
			lastMatches = new QueryMatch[queryNodes.size()];
			for (int i = 0; i < resultsIterators.length; i++) {
				lastMatches[i] = resultsIterators[i].next();
			}
			foundNext = true;
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
		
		
		if (tryMove(dataNodesIterators.length-1)) {
			return foundNext = true;
		}
	
		return false;
	}
	

	private boolean tryMove(int i) {
		if (i < 0) return false;
		
		if (resultsIterators[i] == null) return false;

		//try next result for same data
		if (resultsIterators[i].hasNext())
		{
			lastMatches[i] = resultsIterators[i].next();
			return true;
		}
		
		//try new result for new data
		resultsIterators[i] = findNewResultIterator(i);		
		if (resultsIterators[i] != null) {			
			lastMatches[i] = resultsIterators[i].next();
			return true;
		}

		//rewind and try new result for previous iterator group
		dataNodesIterators[i] = dataNodes.iterator();
		resultsIterators[i] = findNewResultIterator(i);		
		if (resultsIterators[i] == null) return false; //rewind failed
		lastMatches[i] = resultsIterators[i].next();
		
		//try new result for previous iterator group
		return tryMove(i-1);		
	}


	@Override
	public QueryMatch next() {
		if (! hasNext()) throw new NoSuchElementException();
		
		foundNext = false;

		List<NodeMatch> matchingNodes = new ArrayList<FSQuery.NodeMatch>();
		
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
	
	public ParentQueryNodeIterator createCopyOfInitialIteratorState() {
		return new ParentQueryNodeIterator(parentNodeMatch, queryNodes, dataNodes);
	}

}
