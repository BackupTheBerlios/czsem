package czsem.fs.query;

import java.util.Iterator;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.NodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;

public class FSQueryTest {
	@Test
	public static void getResultsFor() {
		QueryData data = buidQueryObject();
		
		QueryNode qn = new QueryNode();
		Iterable<QueryMatch> res = qn.getResultsFor(data, 0);
		Assert.assertNotEquals(res, null);
		Assert.assertTrue(res.iterator().hasNext());

		qn.addRestriction("=", "id", "xxx");
		
		res = qn.getResultsFor(data, 0);
		Assert.assertEquals(res, null);

		QueryNode qn1 = new QueryNode();
		QueryNode qn2 = new QueryNode();
		qn1.addChild(qn2);

		res = qn1.getResultsFor(data, 0);
		Assert.assertNotEquals(res, null);
		Assert.assertTrue(res.iterator().hasNext());

		qn2.addRestriction("=", "id", "xxx");

		res = qn1.getResultsFor(data, 0);
		Assert.assertEquals(res, null);
	}
	
	public static int getNextNodeId(Iterator<QueryMatch> i, int matchIndex)
	{
		NodeMatch n = null;
		Iterator<NodeMatch> next = i.next().getMatchingNodes().iterator();
		
		for (int a=0; a<matchIndex; a++)
			n= next.next();
		
		return n.getNodeId();		
	}

	@Test
	public static void getResultsForConcurentIterators() {		 
		QueryData data = buidQueryObject();
		
		QueryNode qn1 = new QueryNode();
		QueryNode qn2 = new QueryNode();
		qn1.addChild(qn2);
		QueryNode qn3 = new QueryNode();
		qn2.addChild(qn3);

		Iterable<QueryMatch> res = qn1.getResultsFor(data, 0);
		Assert.assertNotEquals(res, null);
		Assert.assertTrue(res.iterator().hasNext());
		
		Iterator<QueryMatch> i1 = res.iterator();
		Iterator<QueryMatch> i2 = res.iterator();
		
		
		Assert.assertEquals(getNextNodeId(i1,3), 3);
		Assert.assertEquals(getNextNodeId(i1,3), 4);
		Assert.assertEquals(getNextNodeId(i2,3), 3);
		Assert.assertEquals(getNextNodeId(i1,3), 5);
		Assert.assertEquals(getNextNodeId(i2,3), 4);
		Assert.assertEquals(getNextNodeId(i2,3), 5);
	}

	
	@Test
	public static void testQuery() {
		QueryNode qn1 = new QueryNode();

		QueryNode qn2 = new QueryNode();
		
		qn1.addChild(qn2);
		qn2.addChild(new QueryNode());
		
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
		 QueryData data = buidQueryObject();
		 evaluateQuery(data, queryNode, results);
	}

	public static void evaluateQuery(QueryData data, QueryNode queryNode, int[] results) {

		
		queryNode.reset();
		Iterable<QueryMatch> res = queryNode.getResultsFor(data, 0);
		int i = 0;
		int finishedNodeMatches = 0;
		if (res != null) {
			for (QueryMatch queryMatch : res) {
				System.err.println(queryMatch.getMatchingNodes());
				
				for (NodeMatch nodeMatch : queryMatch.getMatchingNodes()) {
					Assert.assertEquals(nodeMatch.nodeId, results[i++]);
					finishedNodeMatches++;
				}
			}
		}
		
		Assert.assertEquals(finishedNodeMatches, results.length);
	}

	public static QueryData buidQueryObject() {
		TreeIndex index = new TreeIndex();
		
		index.addDependency(0,1);
		index.addDependency(0,2);
		index.addDependency(1,3);
		index.addDependency(1,4);
		index.addDependency(2,5);
		index.addDependency(3,6);
		index.addDependency(0,7);

		return new FSQuery.QueryData(index, new NodeAttributes.IdNodeAttributes());
	}
}
