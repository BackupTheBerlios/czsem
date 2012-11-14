package czsem.fs.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

import czsem.fs.TreeIndex;
import czsem.fs.NodeAttributes;
import czsem.fs.query.FSQueryParser.SyntaxError;

public class FSQuery {
	
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

		public int getNodeId() {
			return nodeId;
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
		protected List<Restrictioin> restricitions = new ArrayList<FSQuery.Restrictioin>();
		
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
		protected List<QueryNode> children = new ArrayList<FSQuery.QueryNode>();

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

	public class QueryObject {
		protected QueryNode queryNode;

		public QueryObject(QueryNode queryNode) {
			this.queryNode = queryNode;
		}
		
		public Iterable<QueryMatch> evaluate() {
			List<Iterable<QueryMatch>> res = new ArrayList<Iterable<QueryMatch>>();
			
			for (int id : index.getAllNodes())
			{
				Iterable<QueryMatch> i = queryNode.getResultsFor(id);
				if (i != null) res.add(i);
			}
			
			@SuppressWarnings("unchecked")
			Iterable<QueryMatch>[] gt = (Iterable<QueryMatch>[]) new Iterable[0];
			
			return Iterables.concat(res.toArray(gt));
		}
		
	}
	
	public QueryObject buildQuery(String queryString) throws SyntaxError {
		FSQueryBuilder b = new FSQueryBuilder(this);
		FSQueryParser p = new FSQueryParser(b);
		p.parse(queryString);
		
		return new QueryObject(b.getRootNode());		
	}




}
