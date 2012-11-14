package czsem.fs.query;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.ParentQueryNode;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryNode;

public class FSQueryTest {
	@Test
	public static void testQuery() {
		
		FSQuery q = buidQueryObject();
		

		ParentQueryNode qn1 = q.new ParentQueryNode();

		ParentQueryNode qn2 = q.new ParentQueryNode();
		
		qn1.addChild(qn2);
		qn2.addChild(q.new ParentQueryNode());
		
		evaluateQuery(qn1);
	}

	public static void evaluateQuery(QueryNode q) {
		int results[] = {
				0, 1, 3,  
				0, 1, 4,
				0, 2, 5};
		
		evaluateQuery(q, results);		
	}

	public static void evaluateQuery(QueryNode queryNode, int[] results) {
		
		Iterable<QueryMatch> res = queryNode.getResultsFor(0);
		int i = 0;
		for (QueryMatch queryMatch : res) {
			System.err.println(queryMatch.getMatchingNodes());
			
			for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
				Assert.assertEquals(nodeMatch.nodeId, results[i++]);
			}
		}		
	}

	public static FSQuery buidQueryObject() {
		TreeIndex index = new TreeIndex();
		
		index.addDependency(0,1);
		index.addDependency(0,2);
		index.addDependency(1,3);
		index.addDependency(1,4);
		index.addDependency(2,5);
		index.addDependency(3,6);
		index.addDependency(0,7);

		
		
		FSQuery q = new FSQuery();
		q.setIndex(index);

		return q;
	}
}
