package czsem.gate.applet;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.creole.AnnotationSchema;
import gate.creole.ResourceInstantiationException;
import gate.gui.annedit.SchemaAnnotationEditor;
import gate.mimir.tool.WebUtils;
import gate.util.GateException;

import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MainFrame {

	private DefaultDocumentEditor documentEditor;
	private URL documentUrl;
	private String asName;
	private Container parent;
	private String[] loadSchemas;

	public MainFrame(URL documentUrl, String asName, String loadSchemas, Container parent)
			throws HeadlessException {
		this.documentUrl = documentUrl;
		this.asName = asName;
		this.parent = parent;
		this.loadSchemas = loadSchemas == null ? null : loadSchemas.split("\\|");
	}
	
	public static void unloadAllSchemas() throws GateException {
		List<Resource> instances = 
			Gate.getCreoleRegister().getAllInstances(
					AnnotationSchema.class.getCanonicalName());
		for (Resource r : instances)
		{
			Factory.deleteResource(r);
		}
	}

	public static void disableGateSchemaEditor() {
		Gate.getCreoleRegister().remove(SchemaAnnotationEditor.class.getCanonicalName());		
	}

	
	public static boolean loadGateSchema(String schemaUrlStr) throws ResourceInstantiationException
	{
		try {
		
			URL schemaUrl = new URL(schemaUrlStr);
			FeatureMap fm = Factory.newFeatureMap();
			fm.put(AnnotationSchema.FILE_URL_PARAM_NAME, schemaUrl);
			Factory.createResource(AnnotationSchema.class.getCanonicalName(), fm);
			return true;		
		} catch (Exception e) {
			return false;
		}			
	}

	public static void enableGateSchemaEditor() throws GateException
	{
		Gate.getCreoleRegister().registerComponent(SchemaAnnotationEditor.class);		
	}
	


	public static void initGate() throws GateException, MalformedURLException {
		/*
		URL creoleUrl = MainFrame.class.getClassLoader().getResource("gate/resources/creole/creole.xml");
		System.err.println(creoleUrl);
		
		String urlStr = creoleUrl.toString();
		String base = urlStr.substring(0, urlStr.lastIndexOf('/')+1)	;
		System.err.println(base);
		
		Gate.setBuiltinCreoleDir(new URL(base));

		System.err.println(MainFrame.class.getClassLoader().getResource("gate/resources/creole/"));
		
		System.err.println(Files.getGateResource("/creole/"));

		*/
		if (Gate.isInitialised()) return;
		
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(Level.OFF);
		BasicConfigurator.configure();

		Gate.runInSandbox(true);
		Gate.init();		
	}
	
	public static Document downloadGateDocument(URL documentUrl) throws ResourceInstantiationException {
		Document doc;
		
		WebUtils wu = new WebUtils();
		
		try {
			doc = (Document) wu.getObject(documentUrl.toString());
			//doc = Factory.newDocument(documentUrl, "utf8");
		} catch (Exception e)
		{
			doc = Factory.newDocument(
					String.format("Requested document could not be initialized.\nurl: %s\nerror message: %s",
							documentUrl.toString(), e.getLocalizedMessage()));
			
		}
		return doc;
	}


	/**
	 * @return null is succeeds
	 */
	public static Exception uploadGateDocuemnt(Document document, URL url) {
		WebUtils wu = new WebUtils();
				
		try {
			wu.postObject(url.toString(), document);
			/*		
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			DocumentHandler.writeGateDocXml(document, conn.getOutputStream());
	
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				System.err.println(line);
			}
			rd.close();
*/		
		} catch (Exception e)
		{
			return e;
		}
		return null;
	}

	
	
	public void initDocuemtEditor() throws IOException, GateException
	{												
		unloadAllSchemas();
		disableGateSchemaEditor();			
		
		if (loadSchemas != null)
		{
			enableGateSchemaEditor();
			for (int i = 0; i < loadSchemas.length; i++) {
				loadGateSchema(loadSchemas[i]);
			}
		} 
		
		documentEditor = new DefaultDocumentEditor();
	
		Document doc = downloadGateDocument(documentUrl);
		documentEditor.setTarget(doc);
		documentEditor.setDefaultAsName(asName);
		
		documentEditor.setSize(parent.getSize());
		documentEditor.setPreferredSize(parent.getSize());
		documentEditor.init();
				
		parent.add(new JTabbedPane().add(documentEditor));
		
		documentEditor.addSaveButtonListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				try {
					saveDocuemnt();
				} catch (IOException exception) {
					throw new RuntimeException(exception);
				}
			}
		});
		
	}

	/*
	protected void saveDocuemnt() throws IOException {
		System.err.println("---save start---");
		
		//DefaultDocumentEditor.initAndSaveAnnotationSetsSettings();

		//documentEditor.selectDefaultAS();
		LinkedHashSet<String> set = Gate.getUserConfig().getSet(
			      AnnotationSetsView.class.getName() + ".types");
		System.err.println(set);
		
		for (Object o : set)
		{
			System.err.println(o.getClass().getCanonicalName());
		}
		
		documentEditor.restoreSettings();
		System.err.println("---save end---");
	}
	/*****/

	protected void saveDocuemnt() throws IOException {
		
		Exception ret = uploadGateDocuemnt(documentEditor.getDocument(), documentUrl);
		
		if (ret != null)
		{
			JOptionPane.showMessageDialog(parent, ret, ret.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE);
			
		}		
	}

	public void initGui() {		
		try {
			initDocuemtEditor();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}