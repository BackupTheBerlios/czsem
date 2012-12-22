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

import czsem.fs.query.AttrsCollectorFSQB;
import czsem.fs.query.FSQueryParser.SyntaxError;

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

	public void initComponents() {
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
        	@Override public void actionPerformed(ActionEvent e) {onUpdateQueryButton();}});
        panelBottom.add(buttonUpdate, BorderLayout.EAST);

        /*
        //buttonSearch
        JButton buttonSearch = new JButton("Search!");
        buttonSearch.addActionListener(new ActionListener() {
        	@Override public void actionPerformed(ActionEvent e) {search();}});
        JPanel p = new JPanel();
        p.add(buttonSearch);
        panelBottom.add(p, BorderLayout.SOUTH);
        /**/


		
	}

	protected void search() {
		// TODO Auto-generated method stub
		
	}
	
	public String getQueryString()
	{
		return queryString.getText();
	}

	protected void onUpdateQueryButton() {
		updateQuery();
	}

	protected void updateQuery() {
		try {
			String[] attrs = AttrsCollectorFSQB.collectAttributes(getQueryString());
			forestDispaly.setForest(attrs, getQueryString());
			
			//">="
			
			for (int i = 0; i < attrs.length; i++) {
		        forestDispaly.addShownAttribute(attrs[i]);				
			}
			
			forestDispaly.repaint();		
		} catch (SyntaxError e) {
			throw new RuntimeException(e);
		}
	}

	public void setQueryString(String queryString) {
		this.queryString.setText(queryString);
		updateQuery();
	}

}
