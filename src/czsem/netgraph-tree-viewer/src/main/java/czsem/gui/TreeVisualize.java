package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;

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
	
	public static void main(String [] args)
	{
		TreeVisualize tv = new TreeVisualize();
		tv.pack();
		tv.setVisible(true);
		
		System.err.println("end");
	}
}
