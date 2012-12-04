package czsem.fs.query.restrictions;

import java.util.Iterator;
import java.util.List;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.ParentQueryNodeIterator;
import czsem.fs.query.QueryNode;

public class ChildrenEvaluator extends RestrictioinsConjunctionEvaluator {
	
	public static ChildrenEvaluator instance = new ChildrenEvaluator();

	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode node, int nodeId) {
		if (super.getResultsFor(data, node, nodeId) == null) return null;
		
		Iterable<Integer> chDataNodes = data.getIndex().getChildren(nodeId);
		
		List<QueryNode> children = node.getChildren();
		
		if (chDataNodes == null && children.size() > 0) return null;
		
		NodeMatch parentNodeMatch = new NodeMatch(nodeId, node);
		
		final ParentQueryNodeIterator mainIterator = new ParentQueryNodeIterator(parentNodeMatch, children, chDataNodes, data);
		
		if (! mainIterator.hasNext()) return null;
		
		return new Iterable<QueryMatch>(){

			@Override
			public Iterator<QueryMatch> iterator() {
				return mainIterator.createCopyOfInitialIteratorState();
			}
			
		};
	}
}
