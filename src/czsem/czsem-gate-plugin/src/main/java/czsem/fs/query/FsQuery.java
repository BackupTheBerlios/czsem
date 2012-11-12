package czsem.fs.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import czsem.gate.utils.TreeIndex;

public class FsQuery {
	
	TreeIndex index;
	
	public static class NodeMatch {
		protected int nodeId;
		protected QueryNode queryNode;

		public NodeMatch(int nodeId, QueryNode queryNode) {
			this.nodeId = nodeId;
			this.queryNode = queryNode;
		}
	}

	public static class QueryMatch {
		private List<NodeMatch> matchingNodes;		
		public QueryMatch( List<NodeMatch> matchingNodes ) {this.matchingNodes = matchingNodes; }
		public List<NodeMatch> getMatchingNodes() {return matchingNodes; }
	}
	
	public class Restrictioin {
		public boolean evalaute(int nodeID) {
			return true;
		}
	}

	
	public class QueryNode {
		protected List<Restrictioin> restricitions = new ArrayList<FsQuery.Restrictioin>();
		
		public boolean evalaute(int nodeID) {
			return true;
		}
		
		public Iterable<QueryMatch> getResultsFor(int nodeId) {
			for (Restrictioin r : restricitions)
			{
				if (! r.evalaute(nodeId)) return null;
			}
			
			List<NodeMatch> matchingNodes = Arrays.asList(
					new NodeMatch[] {new NodeMatch(nodeId, this)} );
			
			return Arrays.asList(
					new QueryMatch [] {
							new QueryMatch(matchingNodes)}); 
		}
	}

	public class ParentQueryNode extends QueryNode {
		protected List<QueryNode> children = new ArrayList<FsQuery.QueryNode>();

		@Override
		public Iterable<QueryMatch> getResultsFor(final int nodeId) {
			if (super.getResultsFor(nodeId) == null) return null;
			
			final NodeMatch parentNodeMatch = new NodeMatch(nodeId, this);		
			return new Iterable<QueryMatch>(){

				@Override
				public Iterator<QueryMatch> iterator() {
					return new ParentQueryNodeIterator(parentNodeMatch, children, index.getChildren(nodeId));
				}
				
			};
		}

	}



}
