package czsem.fs.query;

import java.util.Stack;

import org.apache.log4j.Logger;

import czsem.fs.query.FsQuery.QueryNode;

public class FsQueryBuilder {
	
	protected FsQuery query;

	public FsQueryBuilder(FsQuery q) {
		query = q;
		curentParent = query.new ParentQueryNode(); 
		curentNode = curentParent;
	}
	
	Logger loger = Logger.getLogger(FsQueryBuilder.class);
	
	Stack<FsQuery.ParentQueryNode> nodeStack = new Stack<FsQuery.ParentQueryNode>();
	
	FsQuery.ParentQueryNode curentParent; 
	FsQuery.ParentQueryNode curentNode;

	public void addNode() {
		loger.debug("addNode");

		curentNode = query.new ParentQueryNode();
		curentParent.addChild(curentNode);		
	}

	public void beginChildren() {
		loger.debug("beginChildren");
		
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
