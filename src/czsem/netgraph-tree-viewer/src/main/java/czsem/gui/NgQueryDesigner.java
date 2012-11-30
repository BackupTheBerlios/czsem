package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

public class NgQueryDesigner extends Container  {
	private static final long serialVersionUID = 3771937513564105054L;

	public static void main(String[] args) {
		JFrame fr = new JFrame(NgQueryDesigner.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
	    NgQueryDesigner qd = new NgQueryDesigner();
		qd.initComponents();
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);

	}

	private CzsemForestDisplay forestDispaly;
	private JTextPane queryString;

	protected void initComponents() {
		setLayout(new BorderLayout());

        //forest
        forestDispaly = new CzsemForestDisplay();
        forestDispaly.setPreferredSize(new Dimension(500,500));
        forestDispaly.setEmphasizeChosenNode(true);
        forestDispaly.setHead(new String [] {"string"});
        forestDispaly.getTreeProperties().setShowAttrNames(true); 
        forestDispaly.getTreeProperties().setShowNullValues(false); // Necessary for correct painting
        JScrollPane query_tree_view_scroll_pane = new JScrollPane(forestDispaly);
        query_tree_view_scroll_pane.setPreferredSize(new Dimension(500,400));
        query_tree_view_scroll_pane.setBorder(BorderFactory.createTitledBorder("query tree:"));
        add(query_tree_view_scroll_pane, BorderLayout.CENTER);
        
        
        JPanel panelBottom = new JPanel(new BorderLayout());
        add(panelBottom, BorderLayout.SOUTH);
        
        //query string
		queryString = new JTextPane();        
        queryString.setText("[string=query]");
		JPanel panel_query = new JPanel(new BorderLayout());
        panel_query.setBorder(BorderFactory.createTitledBorder("query string:"));
        panel_query.setLayout(new BorderLayout());
        panel_query.add(queryString, BorderLayout.CENTER);        
        panelBottom.add(panel_query, BorderLayout.CENTER);

        //buttonUpdate
        JButton buttonUpdate = new JButton("Update");
        buttonUpdate.addActionListener(new ActionListener() {
        	@Override public void actionPerformed(ActionEvent e) {updateQuery();}});
        panelBottom.add(buttonUpdate, BorderLayout.EAST);

        //buttonSearch
        JButton buttonSearch = new JButton("Search!");
        buttonSearch.addActionListener(new ActionListener() {
        	@Override public void actionPerformed(ActionEvent e) {search();}});
        JPanel p = new JPanel();
        p.add(buttonSearch);
        panelBottom.add(p, BorderLayout.SOUTH);
        


		
	}

	protected void search() {
		// TODO Auto-generated method stub
		
	}

	protected void updateQuery() {
		forestDispaly.setForest(queryString.getText());
        forestDispaly.addShownAttribute("string");
		forestDispaly.repaint();		
	}

}
