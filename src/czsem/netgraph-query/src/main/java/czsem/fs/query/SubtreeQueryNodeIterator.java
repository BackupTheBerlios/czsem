package czsem.fs.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Iterators;

import czsem.Utils;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;

public class SubtreeQueryNodeIterator implements Iterator<QueryMatch> {
	
	public static class LimitedStack<E> extends Stack<E> {
		private static final long serialVersionUID = 6697907066897731316L;
		
	}
	
	private LimitedStack<Integer> stack = new LimitedStack<Integer>();
	private QueryData data;
	private int dataNodeId;
	private QueryNode queryNode;
	private SubtreeQueryNodeIterator [] childrenIterators;
	protected QueryMatch[] lastResults;

	
	public abstract class State { 
		public abstract State nextState();
		public boolean hasNext() {return true; };
		public abstract QueryMatch next();
	}
	
	public class FirstState extends State {
		@Override
		public State nextState() {
			return thirdState;
		}

		@Override
		public QueryMatch next() {
			return new QueryMatch(new ArrayList<FSQuery.NodeMatch>(0));
		}
	}

	public class SecondState extends State {
		@Override
		public State nextState() {
			return thirdState;
		}

		@Override
		public QueryMatch next() {
			return new QueryMatch(Arrays.asList(new NodeMatch [] { new NodeMatch(dataNodeId, queryNode)}));
		}
	}

	public class ThirdState extends State {
		@Override
		public State nextState() {
			return thirdState;
		}
		
		public boolean hasNext() {
			return ! finish; 
		}

		@Override
		public QueryMatch next() {
			List<NodeMatch> ret = new ArrayList<FSQuery.NodeMatch>();
			ret.add(new NodeMatch(dataNodeId, queryNode));
			for (QueryMatch m : lastResults) {
				ret.addAll(m.getMatchingNodes());
			}
			
			finish = ! tryMove(childrenIterators.length-1);
			
			return new QueryMatch(ret);
		}
	}
	
	State firstState = new FirstState();
	State secondState = new SecondState();
	State thirdState = new ThirdState();
	
	State state = firstState;
	
	protected boolean finish = false;
	private int[] children;

	public SubtreeQueryNodeIterator(int dataNodeId, QueryData data, QueryNode queryNode) {
		this.data = data;
		this.dataNodeId = dataNodeId;
		this.queryNode = queryNode;
		
		Collection<Integer> childrenCollection = data.getIndex().getChildren(dataNodeId);
		if (childrenCollection == null) childrenCollection = new ArrayList<Integer>(0); 
		
		childrenIterators = new SubtreeQueryNodeIterator[childrenCollection.size()];
		lastResults = new QueryMatch[childrenCollection.size()];
		children = new int [childrenCollection.size()];
		
		int i = 0;
		for (int ch : childrenCollection) {
			childrenIterators[i] = new SubtreeQueryNodeIterator(ch, data, queryNode);
			children[i] = ch;
			lastResults[i] = childrenIterators[i].next();
			i++;
		}
	}

	public boolean tryMove(int i) {
		if (i < 0) return false;

		if (childrenIterators[i].hasNext()) {
			lastResults[i] = childrenIterators[i].next();
			return true;
		}
		
		//rewind
		childrenIterators[i] = new SubtreeQueryNodeIterator(children[i], data, queryNode);
		lastResults[i] = childrenIterators[i].next();
			
		return tryMove(i-1);
	}

	@Override
	public boolean hasNext() {
		return state.hasNext();
	}

	@Override
	public QueryMatch next() {
		QueryMatch ret = state.next();
		
		state = state.nextState();
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public SubtreeQueryNodeIterator createCopyOfInitialIteratorState() {
		return new SubtreeQueryNodeIterator(dataNodeId, data, queryNode);
	}

	/*
	
	public SubtreeQueryNodeIterator(
			NodeMatch parentNodeMatch,
			List<QueryNode> queryNodes, 
			Collection<Integer> dataNodes,
			QueryData data)
	{
		super(parentNodeMatch, queryNodes, dataNodes, data);
		
	}
	

	@Override
	protected void initDataNodeIterators() {
		if (dataNodes.size() < queryNodes.size())
		{
			empty = true;
			return;
		}

		for (int i = 0; i < dataNodesIterators.length; i++) {
			dataNodesIterators[i] = dataNodes.listIterator(i);			
		}		
	}
	
	@Override
	protected boolean rewindDataNodesIterator(int i) {
		
		//first iterator will never be rewinded
		if (i <= 0) return false;
		
		int ni = dataNodesIterators[i-1].nextIndex();
		
		//there is no space to move
		if (ni+1 >= dataNodes.size()) return false;
		
		dataNodesIterators[i] = dataNodes.listIterator(ni+1);		
		return true;
	}

*/
}
