package czsem.fs.query;

import java.util.Stack;

import org.apache.log4j.Logger;

import czsem.fs.query.restrictions.ChildrenEvaluator;
import czsem.fs.query.restrictions.RestrictioinsConjunctionEvaluator;

public class FSQueryBuilder {
	
	public FSQueryBuilder() {
		curentParent = new QueryNode(); 
		curentNode = curentParent;
	}
	
	Logger loger = Logger.getLogger(FSQueryBuilder.class);
	
	protected Stack<QueryNode> nodeStack = new Stack<QueryNode>();
	
	protected QueryNode curentParent; 
	protected QueryNode curentNode;

	public void addNode() {
		loger.debug("addNode");

		curentNode = new QueryNode(RestrictioinsConjunctionEvaluator.instance);
		curentParent.addChild(curentNode);		
	}

	public void beginChildren() {
		loger.debug("beginChildren");
		
		curentNode.setEvaluator(ChildrenEvaluator.instance);
		
		nodeStack.push(curentParent);
		curentParent = curentNode;		
	}

	public void endChildren() {
		loger.debug("endChildren");
		
		curentParent = nodeStack.pop();
	}

	public void addRestriction(String comparartor, String arg1,	String arg2) {
		loger.debug(String.format("addRestriction %s %s %s", arg1, comparartor, arg2));
		curentNode.addRestriction(comparartor, arg1, arg2);		
	}

	public QueryNode getRootNode() {
		return curentParent.children.iterator().next();
	}

}
