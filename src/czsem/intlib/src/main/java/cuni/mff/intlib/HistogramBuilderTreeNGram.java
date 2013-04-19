package cuni.mff.intlib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryObject;

public class HistogramBuilderTreeNGram extends HistogramBuilder {

	protected QueryObject selector;
	private String attrName;
	private int n;
	
	public HistogramBuilderTreeNGram(QueryObject selector, String attrName, int n) {
		super();
		this.selector = selector;
		this.attrName = attrName;
		this.n = n;
	}

	
	public void add(QueryData data) {
		Iterable<QueryMatch> results = selector.evaluate(data);
		
		for (QueryMatch r : results) {			
			addResult(r, data);
			
		}
	}
	
	/*
	public static class NGramCompositeBuilder {
		protected Deque<NGramComposite> stack = new LinkedList<NGramComposite>();
		

		public String print() {
			return stack.peekLast().print();
		}

		public void addChild(NGramComposite nGramComposite) {
			if (stack.isEmpty()) {
				pushParent(nGramComposite);
				return;
			}
			
			stack.peek().addChild(nGramComposite);
		}

		public void pushParent(NGramComposite nGramComposite) {
			stack.push(nGramComposite);
		}

		
	}
	*/
	
	protected void addResult(QueryMatch r, QueryData queryData) {
		Set<Integer> matching_nodes = new HashSet<Integer>(r.getMatchingNodes().size());
		
		for (NodeMatch m : r.getMatchingNodes()) {
			matching_nodes.add(m.getNodeId());
		}
		
		int root = r.getMatchingNodes().get(0).getNodeId();
		
		NGramComposite rootComposite = buildComposite(root, queryData);

		Stack<NGramComposite> childernStack = new Stack<NGramComposite>();
		
		childernStack.add(rootComposite);
		
		while (! childernStack.isEmpty()) {
			NGramComposite top = childernStack.pop();

			Collection<Integer> children = queryData.getIndex().getChildren(top.id);

			if (children == null) continue;
			
			for (int ch : children) {
				if (! matching_nodes.contains(ch)) continue;

				NGramComposite comp = buildComposite(ch, queryData);
				top.addChild(comp);				
				childernStack.push(comp);
			}
		}
		
		add(rootComposite.print("", ""));
	}

	public static class NGramComposite implements Comparable<NGramComposite> {
		protected String string;
		protected int ord;
		protected int id;
		protected List<NGramComposite> left = new ArrayList<NGramComposite>(1);
		protected List<NGramComposite> right = new ArrayList<NGramComposite>(1);

		public NGramComposite(int id, String string, int ord) {
			this.string = string;
			this.ord = ord;
			this.id = id;
		}
		
		public void addChild(NGramComposite ch) {
			if (ch.ord < ord) {
				int pos = -(Collections.binarySearch(left, ch)+1);
				left.add(pos, ch);
			} else {
				int pos = -(Collections.binarySearch(right, ch)+1);
				right.add(pos, ch);				
			}
		}
		
		public String print(String pefix, String suffix ) {
			boolean hasChildren = ! (left.isEmpty() && right.isEmpty()); 
			
			StringBuilder sb = new StringBuilder();
			sb.append(pefix);
			if (hasChildren) sb.append("(");
			for (int l=0; l<left.size()-1; l++)
				sb.append(left.get(l).print("", ""));
			
			if (left.size() > 0)
				sb.append(left.get(left.size()-1).print("", "("));
			else if (hasChildren)
				sb.append("(");
			
			sb.append(string);

			if (right.size() > 0)
				sb.append(right.get(0).print(")", ""));
			else if (hasChildren)
				sb.append(")");

			for (int r=1; r<right.size(); r++)
				sb.append(right.get(r).print("", ""));
			
			if (hasChildren) sb.append(")");
			sb.append(suffix);
			return sb.toString();
		}

		@Override
		public int compareTo(NGramComposite o) {
			return new Integer(ord).compareTo(o.ord);
		}
	}

	protected NGramComposite buildComposite(int nodeId, QueryData queryData) {
		NGramComposite thisComposite = new NGramComposite(
				nodeId,
				queryData.getNodeAttributes().getValue(nodeId, attrName).toString(),
				Integer.parseInt((String) queryData.getNodeAttributes().getValue(nodeId, "ord")));

		return thisComposite;
	}
	
	/*
	protected void addHeadRecurive(int nodeId, QueryData queryData, int remainigN, NGramComposite parent) {
		if (remainigN == 0) {
			add(parent.print());
			//b.remove();
			return;
		}
		
		NGramComposite thisComposite = buildComposite(nodeId, queryData);
		parent.addChild(thisComposite);
		//b.pushParent();
		

		
		Iterable<Integer> children = queryData.getIndex().getChildren(nodeId);

		
		for (int ch : children)
		{
			addHeadRecurive(ch, queryData, n-1, thisComposite);				
		}

		//b.popParent();
		
		
	}

	protected void addHead(int nodeId, QueryData queryData) {		
		addHeadRecurive(nodeId, queryData, n, new NGramComposite("", -1)); 
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
/**/
}
