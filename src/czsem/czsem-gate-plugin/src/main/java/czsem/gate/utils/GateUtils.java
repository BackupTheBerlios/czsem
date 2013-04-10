package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Controller;
import gate.Corpus;
import gate.CreoleRegister;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageResource;
import gate.ProcessingResource;
import gate.Resource;
import gate.Utils;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.AnnotationDiffer;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class GateUtils
{
	@SuppressWarnings("unchecked")
	public static Integer[] decodeEdge(Annotation a)
	{
		Integer [] ret = new Integer[2];
		ArrayList<Integer> list = (ArrayList<Integer>) a.getFeatures().get("args");
		ret[0] = list.get(0);
		ret[1] = list.get(1);
		return ret;
	}
	
	public static FeatureMap createDependencyArgsFeatureMap(Integer parent_id, Integer child_id)
	{
		FeatureMap fm = Factory.newFeatureMap();
		ArrayList<Integer> args = new ArrayList<Integer>(2);

		args.add(parent_id);
		args.add(child_id);
		fm.put("args", args);
		
		return fm;
	}

	public static Document loadDocumentFormDatastore(DataStore ds, String docId) throws ResourceInstantiationException {
		return (Document) loadResourceFormDatastore(ds, "gate.corpora.DocumentImpl", docId);
	}

	public static Corpus loadCorpusFormDatastore(DataStore ds, String copusId) throws ResourceInstantiationException {
		return (Corpus) loadResourceFormDatastore(ds, "gate.corpora.SerialCorpusImpl", copusId);
	}

	public static DataStore openDataStore(String storage_url) throws PersistenceException
	{
		return Factory.openDataStore("gate.persist.SerialDataStore", storage_url); 
	}

	
	public static void printStoredIds(DataStore ds) throws PersistenceException
	{
		for (Object o : ds.getLrTypes())
		{
			System.err.println(o);			
			for (Object string : ds.getLrIds((String) o)) {
				System.err.print("     ");
				System.err.println(string);
			}
		}		
	}
	
	public static Resource loadResourceFormDatastore(DataStore ds, String calassName, String obj_id) throws ResourceInstantiationException
	{
		FeatureMap docFeatures = Factory.newFeatureMap();
		
		docFeatures.put(DataStore.LR_ID_FEATURE_NAME, obj_id);
		docFeatures.put(DataStore.DATASTORE_FEATURE_NAME, ds);		

		return Factory.createResource(calassName, docFeatures);
	}
	
	public static class CorpusDocumentCounter
	{
		protected Corpus copus;
		protected Set<String> seenDocuments;
		private int numDocs; 

		public CorpusDocumentCounter(Corpus corpus) {
			this.copus = corpus;
			setNumDocs(corpus.size());
			
			seenDocuments = new HashSet<String>(getNumDocs());
		}
		
		public boolean isLastDocument()
		{
//			System.err.format("%d %d\n", numDocs, seenDocuments.size());
			return getNumDocs() <= seenDocuments.size();			
		}

		/**
		 * @return false if the document is already present in the collection  
		 */
		public boolean addDocument(Document doc)
		{
			return addDocument(doc.getName());
		}

		/**
		 * @return false if the document is already present in the collection  
		 */
		public boolean addDocument(String name) {
			return seenDocuments.add(name);
		}
		
		public Set<String> getDocumentSet()		
		{
			return seenDocuments;
		}

		public void setNumDocs(int numDocs) {
			this.numDocs = numDocs;
		}

		public int getNumDocs() {
			return numDocs;
		}

		
	}
	
	public static abstract class CustomizeDiffer
	{
		public abstract String getKeyAs();
		public abstract String getReponseAS();
		public abstract String getAnnotationType();		
	
		public String annotType;
		public void setAnnotType(String annotType)
		{
			this.annotType = annotType;
		}
	}

	public static void safeDeepReInitPR_or_Controller(ProcessingResource processingResource) throws ResourceInstantiationException
	{
		if (processingResource instanceof Controller)
			deepReInitController((Controller) processingResource);
		else
			processingResource.reInit();
	}

	@SuppressWarnings("unchecked")
	public static void deepReInitController(Controller contoler) throws ResourceInstantiationException
	{
		Collection<ProcessingResource> prs = contoler.getPRs();
		for (ProcessingResource processingResource : prs)
		{
			safeDeepReInitPR_or_Controller(processingResource);
			//processingResource.reInit();			
		}		
	}

	public static void deleteAllPublicGateResources()
	{
		CreoleRegister reg = Gate.getCreoleRegister();
		
		for (ProcessingResource i : reg.getPublicPrInstances())
		{
			Factory.deleteResource(i);			
		}
	
		for (LanguageResource l : reg.getPublicLrInstances())
		{
			Factory.deleteResource(l);			
		}
	
	}

	/** One level only, not full recursion! **/
	public static void deepDeleteController(Controller contoler) {
		
		@SuppressWarnings("unchecked")
		Collection<ProcessingResource> prs = contoler.getPRs();
		
		while (prs.iterator().hasNext())
		{
			Factory.deleteResource(prs.iterator().next());
		}
		Factory.deleteResource(contoler);
	}

	public static void registerCzsemPlugin() throws GateException, URISyntaxException, IOException
	{
		registerPluginDirectory(new File(Config.getConfig().getCzsemPluginDir()));		
	}

	public static void registerPluginDirectory(File pluginDirectory) throws MalformedURLException, GateException
	{
	    Gate.getCreoleRegister().registerDirectories( 
	    		pluginDirectory.toURI().toURL());		
	}

	public static void registerPluginDirectory(String pluginDirectoryName) throws MalformedURLException, GateException
	{
		registerPluginDirectory(
    		    new File(Gate.getPluginsHome(), pluginDirectoryName));		
	}
	

	public static void saveGateDocumentToXML(Document doc, String filename) throws IOException
	{
		Writer out = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(filename)), "utf8");
		out.write(doc.toXml());
		out.close();
		Logger l = Logger.getLogger(GateUtils.class);
		l.debug(String.format("saveGateDocumentToXML done: %s", filename));

	}

	
	public static void saveBMCDocumentToDirectory(Document doc, String directory, String nameFeature) throws IOException
	{
		//if (doc.getAnnotations("TectoMT").size() <= 0) throw new RuntimeException("No TectoMT annotations present in document!");
		
		String filename = (String) doc.getFeatures().get(nameFeature);
		
		saveGateDocumentToXML(doc, directory+"/"+filename+".xml");		
	}

	public static void saveBMCCorpusToDirectory(Corpus corpus, String directory, String nameFeature) throws IOException
	{
		
		for (Object doc_o : corpus)
		{
			Document doc = (Document) doc_o;
			saveBMCDocumentToDirectory(doc, directory, nameFeature);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void deleteAndCelarCorpusDocuments(Corpus corpus)
	{		
		//delete documents				
		for (Iterator iter = corpus.iterator(); iter.hasNext(); )
		{
			Object doc = iter.next();
			iter.remove();
			Factory.deleteResource((Resource) doc);
		}		
	}


	
	public static AnnotationDiffer calculateSimpleDiffer(Document doc, CustomizeDiffer cd)
	{
		return calculateSimpleDiffer(doc, cd.getKeyAs(), cd.getReponseAS(), cd.getAnnotationType());		
	}

	public static AnnotationDiffer calculateSimpleDiffer(Document doc, String keyAS, String responseAS, String annotationType)
	{
		return calculateSimpleDiffer(
				doc.getAnnotations(keyAS).get(annotationType), 
				doc.getAnnotations(responseAS).get(annotationType)); // compare
	}
	
	public static AnnotationDiffer calculateSimpleDiffer(AnnotationSet keyAS, AnnotationSet responseAS)
	{
		AnnotationDiffer differ = new AnnotationDiffer();
		differ.setSignificantFeaturesSet(new HashSet<String>());
		differ.calculateDiff(keyAS,	responseAS); // compare
		return differ;
	}
	

	public static boolean testAnnotationsDisjoint(AnnotationSet annots)
	{
		List<Annotation> ordered = Utils.inDocumentOrder(annots);
		for (int i=0; i<ordered.size()-1; i++)
		{
			Annotation a = ordered.get(i);
			Annotation next = ordered.get(i+1);
			
			if (a.getEndNode().getOffset() > next.getStartNode().getOffset()) return false;			
		}
		return true;
	}

	public static void initGate() throws GateException, IOException, URISyntaxException {
		initGate(Level.OFF);
	}

	public static void initGate(Level logLevel) throws GateException, IOException, URISyntaxException {
		if (Gate.isInitialised()) return;
		
		czsem.Utils.loggerSetup(logLevel);		
		
		Config.getConfig().setGateHome();

		Gate.init();						
	}

	
	public static void initGateInSandBox() throws GateException 
	{ initGateInSandBox(Level.OFF); }

	public static void initGateInSandBox(Level logLevel) throws GateException {
		if (Gate.isInitialised()) return;
		
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(logLevel);
		BasicConfigurator.configure();

		Gate.runInSandbox(true);
		Gate.init();				
	}
	
	public static boolean isPrCalssRegisteredInCreole(String classname)
	{
		Set<String> types = Gate.getCreoleRegister().getPrTypes();
		return types.contains(classname);				
	}

	public static boolean isPrCalssRegisteredInCreole(Class<? extends ProcessingResource> clazz)
	{
		return isPrCalssRegisteredInCreole(clazz.getCanonicalName());
	}

	public static String getAnnotationContent(Annotation annotation, Document doc) throws InvalidOffsetException {
		return doc.getContent().getContent(annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset()).toString();
	}


}
