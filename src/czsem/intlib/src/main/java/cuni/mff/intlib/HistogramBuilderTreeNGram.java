package cuni.mff.intlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryObject;

public class HistogramBuilderTreeNGram extends HistogramBuilder {

	protected QueryObject headSelector;
	private String attrName;
	private int n;
	
	public HistogramBuilderTreeNGram(QueryObject headSelector, String attrName, int n) {
		super();
		this.headSelector = headSelector;
		this.attrName = attrName;
		this.n = n;
	}

	
	public void add(QueryData data) {
		Iterable<QueryMatch> results = headSelector.evaluate(data);
		
		for (QueryMatch r : results) {
			NodeMatch m = r.getMatchingNodes().iterator().next();
			
			addHead(m.getNodeId(), data);
			
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
	
	public static class NGramComposite implements Comparable<NGramComposite> {
		protected String string;
		protected int ord;
		protected List<NGramComposite> left = new ArrayList<NGramComposite>(1);
		protected List<NGramComposite> right = new ArrayList<NGramComposite>(1);

		public NGramComposite(String string, int ord) {
			this.string = string;
			this.ord = ord;
		}
		
		public void addChild(NGramComposite ch) {
			if (ch.ord < ord) {
				int pos = Collections.binarySearch(left, ch);
				left.add(pos, ch);
			} else {
				int pos = Collections.binarySearch(right, ch);
				right.add(pos, ch);				
			}
		}
		
		public String print() {
			StringBuilder sb = new StringBuilder();
			sb.append('(');
			for (NGramComposite l : left) sb.append(l.print());
			sb.append(string);
			for (NGramComposite r : right) sb.append(r.print());
			sb.append(')');
			return sb.toString();
		}

		@Override
		public int compareTo(NGramComposite o) {
			return new Integer(ord).compareTo(o.ord);
		}
	}

	protected NGramComposite buildComposite(int nodeId, QueryData queryData) {
		NGramComposite thisComposite = new NGramComposite(
				queryData.getNodeAttributes().getValue(nodeId, attrName).toString(),
				Integer.parseInt((String) queryData.getNodeAttributes().getValue(nodeId, "ord")));

		return thisComposite;
	}
	
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

}
