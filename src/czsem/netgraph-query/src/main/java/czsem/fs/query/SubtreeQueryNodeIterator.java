package czsem.fs.query;

import java.util.Collection;
import java.util.List;

import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;

public class SubtreeQueryNodeIterator extends ParentQueryNodeIterator {

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


}
