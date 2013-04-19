package czsem.fs.query.restrictions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.QueryNode;
import czsem.fs.query.SubtreeQueryNodeIterator;


public class IterateSubtreeEvaluator extends AbstractEvaluator {
	
	public static final String META_ATTR_SUBTREE = "_subtree_eval";
	
	protected Map<Integer,Integer> parentIdsAlreadyEvaluatedOnChild = new HashMap<Integer,Integer>();
	
	@Override
	public void reset() {
		parentIdsAlreadyEvaluatedOnChild = new HashMap<Integer,Integer>();		
	}


	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		if (! RestrictioinsConjunctionEvaluator.evalRestricitons(data, queryNode, dataNodeId))		
			return null;
		
		Integer parentId = data.getIndex().getParent(dataNodeId);
		if (parentIdsAlreadyEvaluatedOnChild.containsKey(parentId) && parentIdsAlreadyEvaluatedOnChild.get(parentId) != dataNodeId)
		{
			return null;
		}
		parentIdsAlreadyEvaluatedOnChild.put(parentId, dataNodeId);
		
		final SubtreeQueryNodeIterator mainIterator = new SubtreeQueryNodeIterator(parentId, data, queryNode, false);
		
		if (! mainIterator.hasNext()) return null;
		
		return new Iterable<QueryMatch>(){

			@Override
			public Iterator<QueryMatch> iterator() {
				return mainIterator.createCopyOfInitialIteratorState();
			}
			
		};		
	}
	
}
