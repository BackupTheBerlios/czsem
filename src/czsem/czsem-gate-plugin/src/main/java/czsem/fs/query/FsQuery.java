package czsem.fs.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import czsem.fs.FSTreeWriter.NodeAttributes;
import czsem.gate.utils.TreeIndex;

public class FsQuery {
	
	protected TreeIndex index;
	protected NodeAttributes nodeAttributes;

	public static class NodeMatch {
		protected int nodeId;
		protected QueryNode queryNode;

		public NodeMatch(int nodeId, QueryNode queryNode) {
			this.nodeId = nodeId;
			this.queryNode = queryNode;
		}
		
		@Override
		public String toString() {			
			return queryNode.toString() + ": " + nodeId;
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

	public class AttrRestrictioin extends Restrictioin {
		protected String attr, value;

		public AttrRestrictioin(String attr, String value) {
			this.attr = attr;
			this.value = value;
		}
	}

	public class EqualRestrictioin extends AttrRestrictioin {

		public EqualRestrictioin(String attr, String value) {
			super(attr, value);
		}

		public boolean evalaute(int nodeID) {
			return value.equals(nodeAttributes.getValue(nodeID, attr).toString());
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
		
		@Override
		public String toString() {
			return "QN_"+Integer.toString(hashCode(), Character.MAX_RADIX);
		}
	}

	public class ParentQueryNode extends QueryNode {
		protected List<QueryNode> children = new ArrayList<FsQuery.QueryNode>();

		@Override
		public Iterable<QueryMatch> getResultsFor(final int nodeId) {
			if (super.getResultsFor(nodeId) == null) return null;
			
			final Iterable<Integer> chDataNodes = index.getChildren(nodeId);
			
			if (chDataNodes == null && children.size() > 0) return null;
			
			final NodeMatch parentNodeMatch = new NodeMatch(nodeId, this);		
			return new Iterable<QueryMatch>(){

				@Override
				public Iterator<QueryMatch> iterator() {
					return new ParentQueryNodeIterator(parentNodeMatch, children, chDataNodes);
				}
				
			};
		}

		public void addChild(QueryNode queryNode) {
			children.add(queryNode);			
		}

		public void addRestriction(String comparartor, String arg1,	String arg2) {
			if (comparartor.equals("="))
			{
				restricitions.add(new EqualRestrictioin(arg1, arg2));
				return;
			}
			
			throw new RuntimeException(String.format("Restricition ont supported: %s", comparartor));
		}

	}

	public void setIndex(TreeIndex i) {
		index = i;		
	}
	
	public void setNodeAttributes(NodeAttributes nodeAttributes) {
		this.nodeAttributes = nodeAttributes;
	}




}
