package czsem.gate;

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
import gate.util.Benchmark;
import gate.util.GateException;
import gate.util.reporting.PRTimeReporter;
import gate.util.reporting.exceptions.BenchmarkReportInputFileFormatException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import czsem.gate.utils.Config;

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
	
	public static String getTimeBenchmarkLogFileName() throws URISyntaxException, IOException
	{
		return Config.getConfig().getLogFileDirectoryPath()+"/benchmark.txt";
	}

	public static void deleteGateTimeBenchmarkFile() throws URISyntaxException, IOException
	{
		new File(getTimeBenchmarkLogFileName()).delete();
	}

	
	public static void enableGateTimeBenchmark() throws URISyntaxException, IOException
	{
		RollingFileAppender appender = new RollingFileAppender();
		appender.setThreshold(Level.DEBUG);
		appender.setFile(getTimeBenchmarkLogFileName());
		appender.setAppend(false);
		appender.setMaxFileSize("10MB");
		appender.setMaxBackupIndex(1);
		appender.setLayout(new PatternLayout("%m%n"));
		
		appender.activateOptions();
		
		Logger bl = Logger.getLogger(Benchmark.class);
		bl.removeAllAppenders();
		bl.addAppender(appender);
		bl.setAdditivity(false);
		bl.setLevel(Level.DEBUG);
		
		
		Benchmark.setBenchmarkingEnabled(true);
	}

	public static void doGateTimeBenchmarkReport(TimeBenchmarkReporter reporter) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
		doGateTimeBenchmarkReport(
				reporter,
				getTimeBenchmarkLogFileName(),
				Config.getConfig().getLogFileDirectoryPath()+"/benchmark_report.txt",
				PRTimeReporter.MEDIA_TEXT,
				PRTimeReporter.SORT_EXEC_ORDER
				);		
		
	}

	public static String createGateTimeBenchmarkReport() throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 
		 doGateTimeBenchmarkReport(new TimeBenchmarkPrintMapSimple(new PrintStream(out)));
		 
		 return out.toString();			
	}

	private static class TimeBenchmarkPrintMapSimple implements TimeBenchmarkReporter
	{
		public TimeBenchmarkPrintMapSimple(PrintStream out) {this.out = out;}

		private PrintStream out;

		@SuppressWarnings("unchecked")
		private void printMap(Map<String, Object> map, String prefix)
		{
			
			for (String pr_name : map.keySet()) {
				if (pr_name.startsWith("doc") || pr_name.equals("systotal"))
					continue;

				out.print(prefix + pr_name);

				Object child = map.get(pr_name);
				Map<String, Object> ch_map = null;
								
				if (child instanceof String)
				{
					out.println("\t" + child);
					continue;
				}
				else
				{
					ch_map = (Map<String, Object>) child;
					out.println("\t" + ch_map.get("systotal"));
				}
				
				
				//recursive call
				printMap(ch_map, prefix + "\t");

			}
		}

		@Override
		public void report(Map<String, Object> report1Container1)
		{
			printMap(report1Container1, "");
		}
	}
	
	public interface TimeBenchmarkReporter
	{

		void report(Map<String, Object> report1Container1);
		
	}
	
	@SuppressWarnings("unchecked")
	public static void doGateTimeBenchmarkReport(TimeBenchmarkReporter reporter, String benchmarkInputFileName, String reportFileName, String outputMediaType, String sortOrder) throws BenchmarkReportInputFileFormatException
	{
		// Report on processing resources
		// http://gate.ac.uk/sale/tao/splitch11.html#x15-30600011.4.4

		// 1. Instantiate the Class PRTimeReporter
		PRTimeReporter report = new PRTimeReporter();
		// 2. Set the input benchmark file
		File benchmarkFile = new File(benchmarkInputFileName);
		report.setBenchmarkFile(benchmarkFile);
		// 3. Set the output report file
		File reportFile = new File(reportFileName);
		report.setReportFile(reportFile);
		// 4. Set the output format: in html or text format (default:
		// MEDIA_HTML)
		report.setPrintMedia(outputMediaType);
		// 5. Set the sorting order: Sort in order of execution or descending
		// order of time taken (default: EXEC_ORDER)
		report.setSortOrder(sortOrder);
		// 6. Set if suppress zero time entries: True/False (default: True).
		// Parameter ignored if SortOrder specified is ‘SORT_TIME_TAKEN’
		report.setSuppressZeroTimeEntries(true);
		// 7. Set the logical start: A string indicating the logical start to be
		// operated upon for generating reports
		// report.setLogicalStart("");
		// 8. Generate the text/html report
//		report.executeReport();
		
		 Object report1Container1 = report.store(report.getBenchmarkFile());
//		 System.err.println(report1Container1);
		 		 
		 reporter.report((Map<String, Object>) report1Container1);		 		 
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


	
	public static void main(String [] args) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{		
		System.out.println(createGateTimeBenchmarkReport());
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

	public static void initGateInSandBox() throws GateException {
		if (Gate.isInitialised()) return;
		
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(Level.OFF);
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


}
