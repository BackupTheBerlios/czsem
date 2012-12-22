package czsem.fs.query;

import java.util.Stack;

import org.apache.log4j.Logger;

import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.fs.query.restrictions.ChildrenEvaluator;
import czsem.fs.query.restrictions.OptionalEvaluator;
import czsem.fs.query.restrictions.RestrictioinsConjunctionEvaluator;

public class FSQueryBuilderImpl implements FSQueryBuilder {
	
	public FSQueryBuilderImpl() {
		curentParent = new QueryNode(); 
		curentNode = curentParent;
	}
	
	Logger loger = Logger.getLogger(FSQueryBuilderImpl.class);
	
	protected Stack<QueryNode> nodeStack = new Stack<QueryNode>();
	
	protected QueryNode curentParent; 
	protected QueryNode curentNode;

	@Override
	public void addNode() {
		loger.debug("addNode");

		curentNode = new QueryNode(RestrictioinsConjunctionEvaluator.restrictioinsConjunctionEvaluatorInstance);
		curentParent.addChild(curentNode);		
	}

	@Override
	public void beginChildren() {
		loger.debug("beginChildren");
		
		curentNode.setEvaluator(ChildrenEvaluator.childrenEvaluatorInstance);
		
		nodeStack.push(curentParent);
		curentParent = curentNode;		
	}

	@Override
	public void endChildren() {
		loger.debug("endChildren");
		
		curentParent = nodeStack.pop();
	}

	@Override
	public void addRestriction(String comparartor, String arg1,	String arg2) {
		loger.debug(String.format("addRestriction %s %s %s", arg1, comparartor, arg2));
		
		if (NGTreeHead.META_ATTR_NODE_NAME.equals(arg1))
			curentNode.setName(arg2);
		else if (NGTreeHead.META_ATTR_OPTIONAL.equals(arg1) && NGTreeHead.META_ATTR_OPTIONAL_TRUE.equals(arg2))
			curentNode.setEvaluator(new OptionalEvaluator());
		else
		{
			curentNode.addRestriction(comparartor, arg1, arg2);					
		}		
	}

	public QueryNode getRootNode() {
		return curentParent.children.iterator().next();
	}

}
