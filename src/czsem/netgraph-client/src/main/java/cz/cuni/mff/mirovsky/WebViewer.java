package cz.cuni.mff.mirovsky;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.swing.event.*;
import java.net.*;


/**
 * This is a simple web browser with back functionality implemented. It is used to display the manual for the client, as well as the changelog and to-do list.
 */

public class WebViewer extends JFrame implements ActionListener, Runnable { // upraveno z knihy Learning Java (O'Reilly)
	protected JEditorPane mEditorPane;
	protected JTextField mURLField;
	JButton button_back;
	Stack history;
	String actual; // adresa právě zobrazené stránky
	private Thread work = null;
	private String urlS; // adresa stránky, která se má zobrazit po vytvoření
	private String labelS; // label location ve stránce, která se má zobrazit po vytvoření

  public WebViewer(String title, String urlString, String label_location) {
    super(title);
		actual = null;
		urlS = new String(urlString);
		labelS = new String(label_location);
    createUI(urlS, labelS);
	  // initialize stack of visited URLs (stack of Strings)
		history = new Stack();

    // go to a new location when enter is pressed in the URL field
    mURLField.addActionListener(this);

    // add the plumbing to make links work
    mEditorPane.addHyperlinkListener(new LinkActivator());
    setVisible(true);
		start();
  }

	public void start() {
		work = new Thread(this);
		work.start();
	}

	public void run() {
	  //System.out.println ("\nNastartovalo vlákno prohlížeče nápovědy.");
		openURL(urlS);
		repaint();
	}

  public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if (source == button_back) { // vrátím se v historii prohlížení
			if (!history.empty()) {
				String last = (String)history.pop();
				actual = null; // signál pro openURL, aby při vracení nedával nic do zásobníku
			  urlS=last;
				start();
			}
		}
		else if (source == mURLField) { // zobrazí se obsah nového URL
			openURL(ae.getActionCommand());
		}
  }

  protected void createUI(String urlString, String label_location) {
    setSize(770, 600);
    center();
    Container content = getContentPane();
    content.setLayout(new BorderLayout());

    // add the URL control
    JToolBar urlToolBar = new JToolBar();
	  urlToolBar.setFloatable(false);
    mURLField = new JTextField(urlString, 40);
		button_back = new JButton (" < ");
		button_back.addActionListener(this);
		urlToolBar.add(button_back);
		JLabel lab_loc = new JLabel (label_location);
		lab_loc.setBorder(BorderFactory.createEmptyBorder(1,7,1,4));
    urlToolBar.add(lab_loc);
    urlToolBar.add(mURLField);
    content.add(urlToolBar, BorderLayout.NORTH);

    // add the editor pane
    mEditorPane = new JEditorPane();
    mEditorPane.setEditable(false);
    content.add(new JScrollPane(mEditorPane), BorderLayout.CENTER);
    setSize(700, 600);
  }

  protected void center() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension us = getSize();
    int x = (screen.width - us.width) / 2;
    int y = (screen.height - us.height) / 2;
    setLocation(x, y);
  }

  protected void openURL(String urlString) {
    try {
		  if (actual != null) history.push(actual);
			actual = urlString;
			URL url = new URL(urlString);
			mEditorPane.setPage(url);
			mURLField.setText(url.toExternalForm());
    }
		catch (Exception e) {
      System.out.println("Couldn't open " + urlString + ":" + e);
    }
  }

  class LinkActivator implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent he) {
      HyperlinkEvent.EventType type = he.getEventType();
      if (type == HyperlinkEvent.EventType.ENTERED)
        mEditorPane.setCursor(
        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      else if (type == HyperlinkEvent.EventType.EXITED)
        mEditorPane.setCursor(Cursor.getDefaultCursor());
      else if (type == HyperlinkEvent.EventType.ACTIVATED) { // zobrazí se obsah odkazovaného dokumentu
        urlS = he.getURL().toExternalForm();
				start();
		  }
    }
  }
}
