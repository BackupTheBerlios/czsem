package czsem.fs.query.restrictions;

import java.util.Iterator;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.ParentQueryNodeIterator;
import czsem.fs.query.QueryNode;
import czsem.fs.query.SubtreeQueryNodeIterator;


public class IterateSubtreeEvaluator extends AbstractEvaluator {

	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		if (! RestrictioinsConjunctionEvaluator.evalRestricitons(data, queryNode, dataNodeId))		
			return null;
		
		final SubtreeQueryNodeIterator mainIterator = new SubtreeQueryNodeIterator(dataNodeId, data, queryNode);
		
		if (! mainIterator.hasNext()) return null;
		
		return new Iterable<QueryMatch>(){

			@Override
			public Iterator<QueryMatch> iterator() {
				return mainIterator.createCopyOfInitialIteratorState();
			}
			
		};		
	}
	
}
