package czsem.gate.applet;

import gate.util.GateException;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;




@SuppressWarnings("serial")
public class GateApplet extends JApplet {
	
	public void init() {
		// Execute a job on the event-dispatching thread; creating this applet's
		// GUI.
		try {
			MainFrame.initGate();

			final MainFrame mf = new MainFrame(
					new URL(getParameter("gateDocumentUrl")),
					getParameter("defaultAnnotationSet"),
					getParameter("loadSchemas"),
					this);
			
			//file:/C:/data/czsem_coprus/Czech_Fireman_50_messages/analyzed_GATE_xml/jihomoravsky47443.txt.xml
//			final String label = new File("C:\\data\\czsem_coprus\\Czech_Fireman_50_messages\\analyzed_GATE_xml\\jihomoravsky47443.txt.xml").toURI().toURL().toString();
//			System.err.println(label);

			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					mf.initGui();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static void showWithDocument(URL docUrl, String defaultAnnotationSet, String schemas) {
		JFrame frame = new JFrame("Gate Applet");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        
		MainFrame mf = new MainFrame(docUrl, defaultAnnotationSet, schemas , frame);

		mf.initGui();                
        
		//Display the window.
        frame.pack();
        frame.setVisible(true);

		
	}

	
	public static void main(String[] args) throws GateException, MalformedURLException {
        MainFrame.initGate();
		
        
		URL docUrl;
		String defaultAnnotationSet;
		String schemas = null;
		
		//System.err.println(new File("date_schema.xml").toURI().toURL());

        
        if (args.length < 1)
        	docUrl = new URL("file:/C:/data/czsem_coprus/Czech_Fireman_50_messages/analyzed_GATE_xml/jihomoravsky47443.txt.xml");
        else 
        	docUrl = new URL(args[0]);
        
        if (args.length < 2)
        	defaultAnnotationSet = "accident";
        else 
        	defaultAnnotationSet = args[1];
		
        if (args.length > 2)
        {
        	schemas = args[2];
        }
        
        showWithDocument(docUrl, defaultAnnotationSet, schemas);
	}

	public static void streamReadTest(URL documentUrl, String asName) throws IOException
	{
		System.err.println();		
		System.err.println();		
		System.err.println(documentUrl);
		System.err.println(asName);
		System.err.println(		
		new BufferedReader(new InputStreamReader(documentUrl.openStream())).readLine());
		System.err.println(		
				new BufferedReader(new InputStreamReader(documentUrl.openStream())).readLine());
		System.err.println();		
		System.err.println();		
	}
}