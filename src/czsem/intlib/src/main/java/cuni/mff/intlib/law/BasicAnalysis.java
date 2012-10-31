package cuni.mff.intlib.law;

import gate.Annotation;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.corpora.DocumentImpl;
import gate.persist.SerialDataStore;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Level;

import czsem.Utils;
import czsem.gate.utils.GateUtils;

public class BasicAnalysis {
	
	public static String storeAdr = "C:\\data\\law\\portal.gov.cz\\store";
	
	DataStore store;


	public BasicAnalysis(DataStore store) {
		this.store = store;
	}
	
	public static BasicAnalysis createDefaultAnalysisHandler() throws GateException, IOException, URISyntaxException
	{
		GateUtils.initGate(Level.DEBUG);
		
		DataStore store = Factory.openDataStore(SerialDataStore.class.getCanonicalName(), Utils.filePathToUrl(storeAdr).toString());
		
		return new BasicAnalysis(store);		
	}


	public static void main(String[] args) throws Exception {
		BasicAnalysis ba = createDefaultAnalysisHandler();
		
		ba.analyzeAll(new Analyzer() {
			
			@Override
			public void analyzeDoc(Document doc) throws InvalidOffsetException {
				List<Annotation> paras = gate.Utils.inDocumentOrder(doc.getAnnotations("Original markups").get("paragraph"));
				
				
				int a = 0;
				System.err.print(GateUtils.getAnnotationContent(paras.get(a), doc).replaceAll("[\\n\\r]", ""));
				System.err.print('|');
				a++;
				System.err.print(GateUtils.getAnnotationContent(paras.get(a), doc).replaceAll("[\\n\\r]", ""));
				System.err.print('|');
				a++;
				System.err.print(GateUtils.getAnnotationContent(paras.get(a), doc).replaceAll("[\\n\\r]", ""));
				System.err.print('|');
				a++;
				System.err.print(GateUtils.getAnnotationContent(paras.get(a), doc).replaceAll("[\\n\\r]", ""));
//				System.err.print('\t');
				a++;
				
				System.err.print('\n');				
			}
		});


	}
	
	public static interface Analyzer
	{

		void analyzeDoc(Document doc) throws Exception;
		
	}


	
	public void analyzeAll(Analyzer analyzer) throws Exception {
		
		@SuppressWarnings("rawtypes")
		List l = store.getLrIds(DocumentImpl.class.getCanonicalName());
		
		for (Object o : l)
		{
		
			Document doc = GateUtils.loadDocumentFormDatastore(store, o.toString());
			
			analyzer.analyzeDoc(doc);
						
			Factory.deleteResource(doc);
		}
		
	}

}
