package czsem.gate.plugins;
import gate.Annotation;
import gate.AnnotationSet;
import gate.DataStore;
import gate.Document;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.gui.MainFrame;
import gate.gui.annedit.AnnotationData;
import gate.gui.annedit.AnnotationEditorOwner;
import gate.gui.annedit.OwnedAnnotationEditor;
import gate.util.GateException;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;

import cz.cuni.mff.mirovsky.trees.NGForest;
import cz.cuni.mff.mirovsky.trees.NGForestDisplay;
import cz.cuni.mff.mirovsky.trees.NGTree;
import cz.cuni.mff.mirovsky.trees.NGTreeHead;
import cz.cuni.mff.mirovsky.trees.TNode;
import czsem.gate.FSSentenceWriter;
import czsem.gate.utils.GateUtils;
import czsem.utils.CzsemTree;
import czsem.utils.NetgraphConstants;


@CreoleResource(name = "Netgraph TreeViewer")
public class NetgraphTreeViewer extends AbstractVisualResource implements  OwnedAnnotationEditor 
{

	private static final long serialVersionUID = -369474822338049027L;
	
	private AnnotationEditorOwner owner;
	protected JDialog dialog;


	private NGForestDisplay forestDisplay;
	private NGTableModel dataModel;
	private Annotation annotation; 
	private AnnotationSet annotation_set;

	
	protected void fireSelectedAnnotation()
	{
		final NGForest forest = forestDisplay.getForest();
		final TNode choosen_node = forest.getChosenNode();
		
		if (choosen_node == null) return;

		owner.selectAnnotation(new AnnotationData() {					
			@Override
			public AnnotationSet getAnnotationSet() {return annotation_set;}					
			@Override
			public Annotation getAnnotation()
			{
				int id_index = forest.getHead().getIndexOfAttribute(NetgraphConstants.ID_FEATURENAME);				
				String id = choosen_node.getValue(0, id_index, 0);
				return annotation_set.get(Integer.parseInt(id)); 
			}
		});		
	}
	
	public void initComponents() { // make the dialog
		Window parentWindow = SwingUtilities.windowForComponent(owner
				.getTextComponent());
		if (parentWindow != null) {
			dialog = parentWindow instanceof Frame ? new JDialog(
					(Frame) parentWindow, "Netgraph Tree Viewer", false)
					: new JDialog((Dialog) parentWindow,
							"Netgraph Tree Viewer", false);
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			MainFrame.getGuiRoots().add(dialog);

		}

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

		forestDisplay = new NGForestDisplay(null);
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
					forestDisplay.selectNode(e);
					fireSelectedAnnotation();
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

		dialog.add(this);
		dialog.pack();
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
	
	public void setForest(NGForest forest)
	{		
		checkAttributes(forest);
		forestDisplay.setForest(forest);
		forestDisplay.getTreeProperties().setShowHiddenNodes(true);
		forestDisplay.getTreeProperties().setShowNullValues(false);
		forestDisplay.getTreeProperties().setShowMultipleSets(true);
		forestDisplay.getTreeProperties().setShowAttrNames(false);

		dataModel.setForestDisplay(forestDisplay);
				
		repaint();
	}

	@Override
	public void editAnnotation(Annotation annotation, AnnotationSet annotation_set)
	{
		if (annotation == null) return;		
		this.annotation = annotation;
		this.annotation_set = annotation_set;
		if (! canDisplayAnnotationType(annotation.getType())) return;
				
		Annotation sentence = annotation; 

		if (! annotation.getType().equals("Sentence"))
		{
			AnnotationSet sentences = annotation_set.getCovering("Sentence", 
					annotation.getStartNode().getOffset(), 
					annotation.getEndNode().getOffset());
			
			Iterator<Annotation> iter = sentences.iterator();
			if (iter.hasNext())	sentence = iter.next();
		}

		
		NGTree tree = new NGTree(null);
		NGForest ngf = new NGForest(null);

		AnnotationSet sentence_set = annotation_set.getContained(
				sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
		
		StringWriter out = new StringWriter();
		
		FSSentenceWriter wr = new FSSentenceWriter(sentence_set, new PrintWriter(out));
		
		wr.printTree();
		
//		System.err.println(annotation.getId());
				
		NGTreeHead th = CzsemTree.createTreeHead(wr.getAttributes().toArray(new String[0]));
		tree.readTree(th, out.toString().toCharArray(), 0, th.getSize());
		
		CzsemTree czstree = new CzsemTree(tree);
		int id_index = th.getIndexOfAttribute(NetgraphConstants.ID_FEATURENAME);				
		int deep_ord = czstree.findFirstNodeByAttributeValue(id_index, annotation.getId().toString());
		tree.setMatchingNodes(Integer.toString(deep_ord));
		tree.setChosenNodeByDepthOrder(deep_ord+1);
		
		ngf.setHead(th);
		ngf.addTree(tree);
			
		setForest(ngf);
		
		placeDialog(0,0);
		
		fireSelectedAnnotation();
		
//		System.out.println(tree.toFSString(false, th));
	}

	@Override
	public boolean canDisplayAnnotationType(String annotationType) {
		if (annotationType.equals("Token"))
			return true;
		if (annotationType.equals("tToken"))
			return true;
		if (annotationType.equals("Sentence"))
			return true;

		return false;
	}

	@Override
	public boolean editingFinished() {
		return true;
	}

	@Override
	public Annotation getAnnotationCurrentlyEdited() {
		return annotation;
	}

	@Override
	public AnnotationSet getAnnotationSetCurrentlyEdited() {
		return annotation_set;
	}

	@Override
	public boolean isActive() {
		return dialog.isVisible();
	}

	@Override
	public void okAction() throws GateException {
	}

	@Override
	public void cancelAction() throws GateException {
	}

	@Override
	public boolean supportsCancel() {
		return true;
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		super.init();
		initComponents();
		return this;
	}

	@Override
	public AnnotationEditorOwner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(AnnotationEditorOwner owner) {
		this.owner = owner;
	}

	@Override
	public void placeDialog(int start, int end) {
		dialog.pack();
		dialog.setVisible(true);
	}

	@Override
	public void setEditingEnabled(boolean isEditingEnabled) {
	}

	@Override
	public void setPinnedMode(boolean pinned) {
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

	
	
	public static void main(String[] args) throws Exception
	{
		GateUtils.initGateInSandBox();
		
		JFrame frame = new JFrame();

		// INITIALISE THE FRAME, ETC.
		frame.setEnabled(true);
		frame.setTitle("SyntaxTree Viewer");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(400, 400));
		final JTextArea textt_area = new JTextArea();
		frame.getContentPane().add(textt_area, BorderLayout.CENTER);


		// DISPLAY FRAME
		frame.pack();
		frame.setVisible(true);

		NetgraphTreeViewer syntaxTreeViewer1 = new NetgraphTreeViewer();
		syntaxTreeViewer1.setOwner(	new AnnotationEditorOwner()
		{
			@Override
			public void selectAnnotation(AnnotationData aData) {}
			@Override
			public JTextComponent getTextComponent() {return textt_area;}
			@Override
			public Annotation getPreviousAnnotation() {return null;}
			@Override
			public Annotation getNextAnnotation() {return null;}
			@Override
			public Document getDocument() {return null;}
			@Override
			public void annotationChanged(Annotation ann, AnnotationSet set, String oldType) {}
		});
		syntaxTreeViewer1.init();

		DataStore ds = GateUtils.openDataStore("file:/C:/Users/dedek/AppData/GATE/data_store/");
		ds.open();
		GateUtils.printStoredIds(ds);

		// Document doc = TectoMTAnalyser.loadDocumentFormDatastore(ds,
		// "jihomoravsky58029.txt.xml_00A36___1268383940724___4619");
		// AnnotationSet as = doc.getAnnotations("TectoMT");
		Document doc = GateUtils.loadDocumentFormDatastore(ds, "english___1268833529545___270");
		AnnotationSet as = doc.getAnnotations();

		Iterator<Annotation> sentences_iterator = as.get("Token").iterator();
		Annotation sentece_annot = sentences_iterator.next();

		syntaxTreeViewer1.editAnnotation(sentece_annot, as);

		Annotation sentece_annot2 = sentences_iterator.next();

		syntaxTreeViewer1.editAnnotation(sentece_annot2, as);

		ds.close();

	}// public static void main

	@Override
	public void changeOrientation(ComponentOrientation orientation) {
		// TODO Auto-generated method stub
		
	}
}
