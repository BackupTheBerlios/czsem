package czsem.fs.query;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.query.FsQuery.NodeMatch;
import czsem.fs.query.FsQuery.ParentQueryNode;
import czsem.fs.query.FsQuery.QueryMatch;
import czsem.gate.utils.TreeIndex;

public class FsQueryTest {
	@Test
	public static void testQuery() {
		TreeIndex index = new TreeIndex();
		
		index.addDependency(0,1);
		index.addDependency(0,2);
		index.addDependency(1,3);
		index.addDependency(1,4);
		index.addDependency(2,5);
		index.addDependency(3,6);
		index.addDependency(0,7);

		
		
		FsQuery q = new FsQuery();
		q.setIndex(index);
		

		ParentQueryNode qn1 = q.new ParentQueryNode();

		ParentQueryNode qn2 = q.new ParentQueryNode();
		
		qn1.addChild(qn2);
		qn2.addChild(q.new ParentQueryNode());
		
		int results[] = {
				0, 1, 3,  
				0, 1, 4,
				0, 2, 5};
		
		Iterable<QueryMatch> res = qn1.getResultsFor(0);
		int i = 0;
		for (QueryMatch queryMatch : res) {
			System.err.println(queryMatch.getMatchingNodes());
			
			for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
				Assert.assertEquals(nodeMatch.nodeId, results[i++]);
			}
		}
		
		
	}
}
