package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;
import cz.cuni.mff.mirovsky.trees.NGTree;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.utils.CzsemTree;

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
		forestDisplay.getTreeProperties().setShowHiddenNodes(true);
		forestDisplay.getTreeProperties().setShowNullValues(false);
		forestDisplay.getTreeProperties().setShowMultipleSets(true);
		forestDisplay.getTreeProperties().setShowAttrNames(true);
		
		
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

	public static void main(String [] args)
	{
		TreeVisualize tv = new TreeVisualize();
		tv.pack();
		tv.setVisible(true);
		
		String attrs [] = {  
			"form",
			"sentence_order",
			"string",
			"hidden",
		};

		tv.setForest(attrs, "[string=a]([string=b])");
		tv.addShownAttribute("string");
		
		System.err.println("end");
	}

}
