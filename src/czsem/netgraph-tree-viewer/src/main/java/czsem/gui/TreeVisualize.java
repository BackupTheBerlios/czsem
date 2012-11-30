package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import cz.cuni.mff.mirovsky.trees.TNode;
import czsem.fs.query.FSQuery.MatchingNode;
import czsem.utils.NetgraphConstants;

public class TreeVisualize extends Container {
	private static final long serialVersionUID = -1059534175810175035L;
	
	private CzsemForestDisplay forestDisplay;
	private NGTableModel dataModel;


	public void initComponents() { // make the dialog
		setLayout(new BorderLayout());

		final JPopupMenu pm = new JPopupMenu();
		final JMenuItem mi_show_hiddden = new JCheckBoxMenuItem("Show hidden nodes", true);
		mi_show_hiddden.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				forestDisplay.getTreeProperties().setShowHiddenNodes(mi_show_hiddden.isSelected());
				forestDisplay.repaint();
			}
		});
		pm.add(mi_show_hiddden);
		pm.add(new JSeparator());
		pm.add(new JMenuItem("123456789"));

		forestDisplay = new CzsemForestDisplay();
		setDefaultLook();
		
		JScrollPane forestScrollpane = new JScrollPane(forestDisplay);
		forestDisplay.addMouseListener(new MouseListener()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
					pm.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					pm.show(e.getComponent(), e.getX(), e.getY());
				else
				{
					int node = forestDisplay.selectNode(e);
					if (node != 0)
					{
						NGForest forest = forestDisplay.getForest();
						TNode choosen_node = forest.getChosenNode();
						fireTreeNodeSelected(choosen_node);
					}
					repaint();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});

		dataModel = new NGTableModel();
		JTable table = new JTable(dataModel);
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMinWidth(21);
		column.setMaxWidth(21);
		column.setPreferredWidth(21);
		column.setResizable(false);

		JScrollPane tableScrollpane = new JScrollPane(table);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				tableScrollpane, forestScrollpane);
		split.setDividerLocation(200);
		add(split);
	}


	protected void fireTreeNodeSelected(TNode choosen_node) {
		// TODO Auto-generated method stub
		
	}
	
	public void setDefaultLook()
	{
		forestDisplay.getTreeProperties().setShowHiddenNodes(true);
		forestDisplay.getTreeProperties().setShowNullValues(false);
		forestDisplay.getTreeProperties().setShowMultipleSets(true);
		forestDisplay.getTreeProperties().setShowAttrNames(false);		
	}
	
	public void setForest(NGForest forest)
	{		
		checkAttributes(forest);
		forestDisplay.setForest(forest);

		dataModel.setForestDisplay(forestDisplay);
				
		repaint();
	}
	
	public void updateForest()
	{
		setForest(forestDisplay.getForest());		
	}

	
	public void setForest(String[] attrs, String forest)
	{
		forestDisplay.setForest(attrs, forest);
		updateForest();
	}
	
	public void addShownAttribute(String attr) {
		forestDisplay.addShownAttribute(attr);
	}

	
	public void selectNode(int selectedNodeID) {
		forestDisplay.selectNode(selectedNodeID);
	}

	
	private void checkAttributes(NGForest new_forest)
	{
		NGForest old = forestDisplay.getForest();
		if (old == null) return;
		
		new_forest.getVybraneAtributy().clear();
		
		DefaultListModel old_attrs = old.getVybraneAtributy();
		DefaultListModel new_attrs = new_forest.getVybraneAtributy();
		for (int i=0; i<old_attrs.size(); i++)
		{
			new_attrs.add(i, old_attrs.get(i));
		}
		
		
		/*
		ngf.getVybraneAtributy().add(0, "id");
		ngf.getVybraneAtributy().add(0, "t_lemma");
		ngf.getVybraneAtributy().add(0, "form");
		ngf.getVybraneAtributy().add(0, "string");
		ngf.getVybraneAtributy().add(0, FSFileWriter.ORD_FEATURENAME);
*/

	}
	
	
	public static class NGTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private NGForestDisplay forestDisplay;
		private NGForest forest;
		private NGTreeHead head;

		private int rowCount = 0;

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				String attr = head.getAttributeAt(rowIndex).getName();
				DefaultListModel selected_attrs = forest.getVybraneAtributy();
				if (selected_attrs.contains(attr))
					selected_attrs.remove(selected_attrs.indexOf(attr));
				else
					selected_attrs.add(0, attr);

				forest.setFlagWholeForestChanged(true);
				forestDisplay.repaint();
			}
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		public void setForestDisplay(NGForestDisplay forestDisplay) {
			this.forestDisplay = forestDisplay;
			forest = forestDisplay.getForest();
			head = forest.getHead();

			rowCount = head.getSize();
		}

		@Override
		public int getRowCount() {
			return rowCount;
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch (col) {
			case 0:
				return forest.getVybraneAtributy().contains(
						head.getAttributeAt(row).getName());
			case 1:
				return head.getAttributeAt(row).getName();
			case 2:
				TNode node = forest.getChosenNode();
				if (node == null)
					return null;
				if (node.values.AHTable[row] == null)
					return null;
				return node.values.AHTable[row].Value;
			}
			return -1;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0;
		}

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return ">";
			case 1:
				return "Attribute";
			default:
			case 2:
				return "Value";
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			default:
				return String.class;
			}
		}
	}
	
	public int getIdAttrValue(TNode node) {
		NGForest forest = forestDisplay.getForest();
		int idAttrIndex = forest.getHead().getIndexOfAttribute(NetgraphConstants.ID_FEATURENAME);				
		String id = node.getValue(0, idAttrIndex, 0);
		return Integer.parseInt(id);
	}


	public static void main(String [] args)
	{
		JFrame fr = new JFrame(TreeVisualize.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
		TreeVisualize tv = new TreeVisualize();
		tv.initComponents();
		
		String attrs [] = {"string"};
	
		String fsTree = "[string=a]([string=\\\\b],[string=c])";
	
		tv.setForest(attrs, fsTree);
	
		fr.add(tv);
		
		fr.pack();
		fr.setVisible(true);
	
	}


	public void setMatchingNodes(List<? extends MatchingNode> matchingNodes) {
		forestDisplay.setMatchingNodes(matchingNodes);
	}

}
