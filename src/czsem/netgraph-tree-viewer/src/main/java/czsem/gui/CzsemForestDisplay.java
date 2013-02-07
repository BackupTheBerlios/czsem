package czsem.gui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;
import cz.cuni.mff.mirovsky.trees.NGTree;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.fs.query.FSQuery.MatchingNode;
import czsem.utils.CzsemTree;
import czsem.utils.NetgraphConstants;

public class CzsemForestDisplay extends NGForestDisplay {
	private static final long serialVersionUID = 2814594462056633811L;
	private NGTreeHead head;
	
	private Set<Object> slectionCache = new HashSet<Object>();
	
	public boolean sorted = true;


	public CzsemForestDisplay() {
		super(null);
	}

	public void setHead(String[] attrs) {
		if (sorted) Arrays.sort(attrs);
			
		head = CzsemTree.createTreeHead(attrs);
	}

	public void setForest(String[] attrs, String forest) {
		setHead(attrs);
		setForest(forest);
	}

	public void setForest(String forest) {
		
		char[] chars = forest.toCharArray();

		NGTree tree = new NGTree(null);
		tree.readTree(head, chars, 0, head.getSize());
		
		NGForest ngf = new NGForest(null);
		ngf.setHead(head);
		ngf.addTree(tree);
			
		setForest(ngf);
	}
	
    @Override
	public void setForest(NGForest forest) {
		NGForest f = getForest();
		if (f != null)
		{
			NGTreeHead h = f.getHead();
	    	saveToCache(h, f.getVybraneAtributy());			
		}
    	
		super.setForest(forest);
		
		restoreFromCache(forest.getVybraneAtributy());
    }


	private void restoreFromCache(DefaultListModel selectedAttrs) {
		for (Object sel : slectionCache)
		{
			selectedAttrs.add(0, sel);
		}
	}

	private void saveToCache(NGTreeHead head, ListModel selectedAttrs) {
		
		//first: remove all known attributes
		for (int i=0; i<head.getSize(); i++)
		{
			slectionCache.remove(head.getAttributeAt(i).getName());
		}
		
		
		//second: add selected
		for (int i=0; i<selectedAttrs.getSize(); i++)
		{
			slectionCache.add(selectedAttrs.getElementAt(i));
		}
	}

	public void addShownAttribute(String attr) {
		DefaultListModel selected_attrs =  getForest().getVybraneAtributy();
		if (selected_attrs.contains(attr))
			return;
		else
			selected_attrs.add(0, attr);

		getForest().setFlagWholeForestChanged(true);
		repaint();				
	}

	public static class DepthOrderIdFinder
	{
		private int id_index;
		private CzsemTree czstree;

		public DepthOrderIdFinder(NGForest forest)
		{
			NGTree tree = forest.getChosenTree();
			id_index = forest.getHead().getIndexOfAttribute(NetgraphConstants.ID_FEATURENAME);				
			czstree = new CzsemTree(tree);			
		}
		
		public int findDepthOrder(int selectedNodeID)
		{
			return czstree.findFirstNodeByAttributeValue(id_index, Integer.toString(selectedNodeID));			
		}

		public NGTree getNGTree() {
			return czstree.getNGTree();
		}
		
	}
	
	public void selectNode(int selectedNodeID) {
		DepthOrderIdFinder dof = new DepthOrderIdFinder(getForest());
		dof.getNGTree().setChosenNodeByDepthOrder(dof.findDepthOrder(selectedNodeID)+1);
	}

	public void setMatchingNodes(List<? extends MatchingNode> matchingNodes) {
		DepthOrderIdFinder dof = new DepthOrderIdFinder(getForest());
		StringBuilder sb = new StringBuilder();
		
		for (MatchingNode m : matchingNodes)
		{			
			sb.append(dof.findDepthOrder(m.getNodeId()));
			sb.append(NGTree.MATCHING_NODES_TREES_DELIMITER);
		}
						
		dof.getNGTree().setMatchingNodes(sb.toString());		
	}
	
    @Override
	public void setShowHiddenNodes(boolean show) {
    	super.setShowHiddenNodes(show);
    	getForest().setFlagWholeForestChanged(true);
	}

}
