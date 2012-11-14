package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;
import cz.cuni.mff.mirovsky.trees.NGTree;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.utils.CzsemTree;
import czsem.utils.NetgraphConstants;

@SuppressWarnings("serial")
public class TreeVisualize extends JFrame {
	private NGForestDisplay forestDisplay; 
	
	public TreeVisualize() {
        super ("TreeVisualize");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setSize(700, 500);
        setLayout(new BorderLayout());
        
        forestDisplay = new NGForestDisplay(null);
        forestDisplay.setPreferredSize(new Dimension(500,500));
        add(forestDisplay, BorderLayout.CENTER);
        
	}
	
	public void setForest(NGForest forest)
	{
		forestDisplay.setForest(forest);
		forestDisplay.getTreeProperties().setShowHiddenNodes(false);
		forestDisplay.getTreeProperties().setShowNullValues(false);
		forestDisplay.getTreeProperties().setShowMultipleSets(false);
		forestDisplay.getTreeProperties().setShowAttrNames(false);
		
		
		forestDisplay.repaint();
	}
	
	public void setForest(String[] attrs, String forest)
	{
		char[] chars = forest.toCharArray();
		
		NGTreeHead th = CzsemTree.createTreeHead(attrs);

		NGTree tree = new NGTree(null);
		tree.readTree(th, chars, 0, th.getSize());
		
		NGForest ngf = new NGForest(null);
		ngf.setHead(th);
		ngf.addTree(tree);
			
		setForest(ngf);
	}

	public void addShownAttribute(String attr) {
		DefaultListModel selected_attrs =  forestDisplay.getForest().getVybraneAtributy();
		if (selected_attrs.contains(attr))
			return;
		else
			selected_attrs.add(0, attr);

		forestDisplay.getForest().setFlagWholeForestChanged(true);
		forestDisplay.repaint();
	}

	public static TreeVisualize showTree(String[] attrs, String forest, int selectedNode) {
		TreeVisualize tv = showTree(attrs, forest);
		
		tv.selectNode(selectedNode);
		
		return tv;		
	}

	public void selectNode(int selectedNodeID) {
		NGTree tree = forestDisplay.getForest().getChosenTree();
		int id_index = forestDisplay.getForest().getHead().getIndexOfAttribute(NetgraphConstants.ID_FEATURENAME);				
		CzsemTree czstree = new CzsemTree(tree);
		int deep_ord = czstree.findFirstNodeByAttributeValue(id_index, Integer.toString(selectedNodeID));
		tree.setMatchingNodes(Integer.toString(deep_ord));
	}

	public static TreeVisualize showTree(String[] attrs, String forest) {
		TreeVisualize tv = new TreeVisualize();
		tv.pack();
		tv.setVisible(true);
		
		tv.setForest(attrs, forest);
//		tv.addShownAttribute("string");		
//		tv.addShownAttribute("form");		
		tv.addShownAttribute("t_lemma");
		tv.addShownAttribute("functor");		
		
		return tv;
	}

	public static void showTreeAndWait(String[] attrs, String forest, int selectedNode) throws InterruptedException {
		TreeVisualize tv = showTree(attrs, forest, selectedNode);
		
		final CountDownLatch signal = new CountDownLatch(1);
		
		tv.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				System.err.println("closed");
				signal.countDown();
			}
			
		});
		
		signal.await();
		System.err.println("RETURNED");
	}

	public static void main(String [] args)
	{
		String attrs [] = {
				"category", "orth", "dependencies", "string", "length", "kind",
				"form",
				"sentence_order",
				"hidden",
			};

		showTree(attrs, "[string=a]([string=\\\\b],[string=c])");
		showTree(attrs, "[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],orth=lowercase,category=VBN,length=10,sentence_order=4]([string=annotations,kind=word,dependencies=\\[amod(1)\\],orth=lowercase,category=NNS,length=11,sentence_order=1]([string=Dependency,kind=word,orth=upperInitial,category=JJ,length=10,sentence_order=0]),[string=can,kind=word,orth=lowercase,category=MD,length=3,sentence_order=2],[string=be,kind=word,orth=lowercase,category=VB,length=2,sentence_order=3],[string=by,kind=word,dependencies=\\[pobj(15)\\],orth=lowercase,category=IN,length=2,sentence_order=5]([string=tool,kind=word,dependencies=\\[det(13)\\],orth=lowercase,category=NN,length=4,sentence_order=7]([string=this,kind=word,orth=lowercase,category=DT,length=4,sentence_order=6])))");
		
		System.err.println("end");
	}

}
