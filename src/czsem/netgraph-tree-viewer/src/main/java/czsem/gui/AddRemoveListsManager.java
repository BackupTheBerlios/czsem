package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.wordpress.tips4java.ListAction;

import czsem.gate.utils.Config;

public class AddRemoveListsManager extends Container {
	private static final long serialVersionUID = -5447255357679644238L;
	private static final int defaultInset = 5;
	private static final Insets defaultInsets = new Insets(defaultInset, defaultInset, defaultInset, defaultInset);

	public static void main(String[] args) throws Exception {
		JFrame fr = new JFrame(AddRemoveListsManager.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
	    AddRemoveListsManager qd = new AddRemoveListsManager();
		qd.initComponents();
		
		qd.addLeftModelSynchronization(Config.getConfig().getDependencyConfig().getDependencyTypesSelected());
		qd.addRightModelSynchronization(Config.getConfig().getDependencyConfig().getDependencyTypesAvailable());
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);

	}


	private JList listRight;
	private JList listLeft;
	private DefaultListModel modelRight;
	private DefaultListModel modelLeft;


	public static void addMiddleButton(Container panel, String caption, ActionListener actionListener){
		JButton b = new JButton(caption);
		b.addActionListener(actionListener);
		b.setAlignmentX(0.5f);
		panel.add(b);
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
	}

	public static JList createList(ListModel model, Action actionEvent) {
		JList listRet = new JList(model);
		new ListAction(listRet, actionEvent);
		listRet.setPreferredSize(new Dimension(100, 50));
		return listRet;
	}

	protected void addListLabel(String textLabel, int gridx){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = gridx;
		c.gridy = 0;
		c.weighty = 0;
		c.weightx = 0;
		c.anchor = GridBagConstraints.SOUTHWEST;
		c.insets = new Insets(defaultInset, defaultInset, 0, 0);
		add(new JLabel(textLabel), c);		
	}
	protected void addList(Component listComponenet, int gridx){
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = gridx;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = defaultInsets;
		add(listComponenet, c);
	}
	
	@SuppressWarnings("serial")
	public void initComponents() {
		setLayout(new GridBagLayout());
		
		listRight = createList(modelRight = new DefaultListModel(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) { commandAdd(); }});

		listLeft = createList(modelLeft = new DefaultListModel(), new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {commandRemove();}}); 
		
		
		addListLabel("Selected:", 0);
		addList(listLeft, 0);
		
		//Add button
		JPanel panelRight = new JPanel(new BorderLayout());
		JPanel panelAdd = new JPanel(new BorderLayout());
		
		panelRight.add(listRight, BorderLayout.CENTER);
		panelRight.add(panelAdd, BorderLayout.SOUTH);
		addListLabel("Available:", 2);
		addList(panelRight, 2);

		final JTextField textFieldAdd = new JTextField();
		panelAdd.add(textFieldAdd, BorderLayout.CENTER);
		JButton buttonAdd = new JButton("Add");
		buttonAdd.setMargin(new Insets(1, 1, 1, 1));
		buttonAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addTextToRightList(textFieldAdd.getText());
			}
		});
		panelAdd.add(buttonAdd, BorderLayout.EAST);
		
		//Middle Buttons
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 2;
		c.insets = defaultInsets;
		JPanel panelMiddle = new JPanel();
		panelMiddle.setLayout(new BoxLayout(panelMiddle, BoxLayout.Y_AXIS));
		
		addMiddleButton(panelMiddle, "<", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { commandAdd(); }});
		addMiddleButton(panelMiddle, ">", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { commandRemove(); }});
		addMiddleButton(panelMiddle, "<<", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) { commandAddAll(); }});
		addMiddleButton(panelMiddle, ">>", new ActionListener() {;
		@Override
		public void actionPerformed(ActionEvent e) { commandRemoveAll(); }});
		add(panelMiddle, c);
		
		
		
	}

	protected void commandRemoveAll() {
		moveAllElements(listLeft, listRight);
	}

	protected void commandAddAll() {
		moveAllElements(listRight, listLeft);
	}

	public static void moveAllElements(JList removeFrom, JList addTo) {
		for (int i = 0, size = removeFrom.getModel().getSize(); i < size; i++) {
			moveElement(removeFrom, addTo, removeFrom.getModel().getElementAt(0));
		}
	}

	public static void moveSelectedElement(JList removeFrom, JList addTo) {
		Object sel = removeFrom.getSelectedValue();
		moveElement(removeFrom, addTo, sel);
	}

	public static void moveElement(JList removeFrom, JList addTo, Object value) {
		((DefaultListModel) removeFrom.getModel()).removeElement(value);
		((DefaultListModel) addTo.getModel()).addElement(value);		
	}

	
	protected void commandRemove() {
		moveSelectedElement(listLeft, listRight);
	}

	protected void commandAdd() {
		moveSelectedElement(listRight, listLeft);
	}

	protected void addTextToRightList(String text) {
		if (modelRight.contains(text)) {
			modelRight.removeElement(text);
		}
		if (modelLeft.contains(text)) {
			modelLeft.removeElement(text);
		}
		modelRight.addElement(text);		
	}
	
	public void addLeftModelSynchronization(Set<String> set) {
		addModelSynchronization(modelLeft, set);
	}

	public void addRightModelSynchronization(Set<String> set) {
		addModelSynchronization(modelRight, set);
	}
	
	public static class ListDataSynchronizer implements ListDataListener {
		protected ListDataSynchronizer(Set<String> set, DefaultListModel model) {
			this.set = set;
			this.model = model;
		}
		private Set<String> set;
		private DefaultListModel model;
		
		private boolean lock = false;
		
		public void synchronizeToSet() {
			if (lock) return;
			
			set.clear();
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Collection<String> list = (Collection) Arrays.asList(model.toArray());
			
			set.addAll(list);
		}

		public void synchronizeToModel() {
			lock = true;
			model.clear();
			for (String s : set) {
				model.addElement(s);
			}
			lock = false;
		}
		
		@Override
		public void intervalRemoved(ListDataEvent e) {synchronizeToSet();}			
		@Override
		public void intervalAdded(ListDataEvent e) {synchronizeToSet();}			
		@Override
		public void contentsChanged(ListDataEvent e) {synchronizeToSet();}
		
	}

	ListDataSynchronizer synchronizers [] = new ListDataSynchronizer[2];  
	
	public void addModelSynchronization(DefaultListModel model, Set<String> set) {
		if (synchronizers[0] == null) {
			model.addListDataListener(synchronizers[0] = new ListDataSynchronizer(set, model));			
		} else {
			model.addListDataListener(synchronizers[1] = new ListDataSynchronizer(set, model));
		}
	}

	public void synchronizeModels() {
		synchronizers[0].synchronizeToModel();
		synchronizers[1].synchronizeToModel();
	}

}
