package czsem.fs.query;

import java.util.ArrayList;
import java.util.List;

import czsem.fs.query.FSQuery.AbstractEvaluator;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.restrictions.ChildrenEvaluator;
import czsem.fs.query.restrictions.Restrictioin;

public class QueryNode  {
	
	private List<Restrictioin> restricitions = new ArrayList<Restrictioin>();
	protected List<QueryNode> children = new ArrayList<QueryNode>();
	protected AbstractEvaluator evaluator;
	
	public QueryNode(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public QueryNode() {this(ChildrenEvaluator.instance);}

	public void setEvaluator(AbstractEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public Iterable<QueryMatch> getResultsFor(QueryData data, int nodeId) {
		return evaluator.getResultsFor(data, this, nodeId);
	}

	public void addChild(QueryNode queryNode) {
		children.add(queryNode);			
	}

	public void addRestriction(String comparartor, String arg1,	String arg2) {
		addRestriction(Restrictioin.createRestriction(comparartor, arg1, arg2));
	}
	
	public void addRestriction(Restrictioin restrictioin) {
		restricitions.add(restrictioin);			
	}

	@Override
	public String toString() {
		return "QN_"+Integer.toString(hashCode(), Character.MAX_RADIX);
	}

	public List<Restrictioin> getRestricitions() {
		return restricitions;
	}

	public List<QueryNode> getChildren() {
		return children;
	}
	
}