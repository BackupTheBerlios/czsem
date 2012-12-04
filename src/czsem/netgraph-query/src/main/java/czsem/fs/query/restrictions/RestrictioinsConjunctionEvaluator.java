package czsem.fs.query.restrictions;

import java.util.Arrays;
import java.util.List;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.QueryNode;

public class RestrictioinsConjunctionEvaluator extends AbstractEvaluator {
	
	public static RestrictioinsConjunctionEvaluator instance = new RestrictioinsConjunctionEvaluator();
	
	@Override
	public Iterable<QueryMatch> getResultsFor(QueryData data, QueryNode node, int nodeId) {
		for (Restrictioin r : node.getRestricitions())
		{
			if (! r.evalaute(data, nodeId)) return null;
		}

		List<NodeMatch> matchingNodes = Arrays.asList(
				new NodeMatch[] {new NodeMatch(nodeId, node)} );
		
		return Arrays.asList(
				new QueryMatch [] {
						new QueryMatch(matchingNodes)}); 
	}


}
