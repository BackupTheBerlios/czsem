package czsem.gui;

import javax.swing.DefaultListModel;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;
import cz.cuni.mff.mirovsky.trees.NGTree;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import czsem.utils.CzsemTree;

public class CzsemForestDisplay extends NGForestDisplay {
	private static final long serialVersionUID = 2814594462056633811L;
	private NGTreeHead head;

	public CzsemForestDisplay() {
		super(null);
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

	public void setForest(String[] attrs, String forest) {
		setHead(attrs);
		setForest(forest);
	}

	public void setHead(String[] attrs) {
		head = CzsemTree.createTreeHead(attrs);
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

}
