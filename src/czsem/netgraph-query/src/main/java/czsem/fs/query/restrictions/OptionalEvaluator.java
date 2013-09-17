package czsem.fs.query.restrictions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.QueryNode;

/** Optional node always returns success, but only one time for the same parent and children nodes must succeed.
 *  If there is not corresponding matching node in data, the result does contain any optional node counterpart.  **/
public class OptionalEvaluator extends ChildrenEvaluator {
	
	protected Map<Integer, Integer> lastNodeOmmitedResultsWasForParentIds = new HashMap<Integer, Integer>();
	
	@Override
	public void reset() {
		lastNodeOmmitedResultsWasForParentIds = new HashMap<Integer, Integer>();		
	}
	
	public Iterable<QueryMatch> getChildrenInsteadResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		if (queryNode.getChildren().size() <= 0) return null;
		
		int parentId = data.getIndex().getParent(dataNodeId);
		Iterable<QueryMatch> parentResult = getChildernResultsFor(null, data, queryNode, parentId);
		return parentResult;		
	}
	
	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode queryNode, int dataNodeId) {
		Integer parentId = data.getIndex().getParent(dataNodeId);
		if (parentId == null) return null;

		Iterable<QueryMatch> ordinaryResult = super.getResultsFor(data, queryNode, dataNodeId);
		if (ordinaryResult != null)
		{
			lastNodeOmmitedResultsWasForParentIds.put(parentId, dataNodeId);
			return ordinaryResult;
		}
				
		//Optional node always returns success if the result does not contain any matching nodes, but only one time for the same parent
		Integer prevDataNodeId = lastNodeOmmitedResultsWasForParentIds.get(parentId);
		if (prevDataNodeId != null && prevDataNodeId != dataNodeId) {
				return null;
		}

		//But before "this node omitted" result is generated look for possible ordinary ones generated by siblings
		for (int ch : data.getIndex().getChildren(parentId))
		{
			ordinaryResult = super.getResultsFor(data, queryNode, ch);
			if (ordinaryResult != null) return null;			
		}
		
		//we are going to generate "this node omitted" result
		lastNodeOmmitedResultsWasForParentIds.put(parentId, dataNodeId);
		
		Iterable<QueryMatch> childrenInsteadResult = getChildrenInsteadResultsFor(data, queryNode, dataNodeId);
		if (childrenInsteadResult != null) return childrenInsteadResult; 
		
		//can not generate empty result if there are unsatisfied children
		if (queryNode.getChildren().size() > 0) return null;
		
		//return empty result
		return Arrays.asList(
				new QueryMatch [] {
						new QueryMatch(
								new ArrayList<NodeMatch>(0))}); 			
		
	}


}
