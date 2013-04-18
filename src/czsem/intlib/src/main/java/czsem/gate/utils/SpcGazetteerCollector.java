package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import czsem.gate.plugins.CustomPR;
import czsem.gate.plugins.CustomPR.AnalyzeDocDelegate;
import czsem.gate.plugins.LevenshteinWholeLineMatchingGazetteer;
import czsem.utils.MultiSet;


public class SpcGazetteerCollector {
	

	public static Corpus getSpcCorpus(boolean preloadDocs) throws PersistenceException, ResourceInstantiationException {
	    String dataStore = "file:/C:/Users/dedek/Desktop/DATLOWE/DATLOWE_gate_store";
	    String copusId = "all___1365490380628___9025";

	    DataStore ds = GateUtils.openDataStore(dataStore);
		Corpus corpus = GateUtils.loadCorpusFormDatastore(ds, copusId);
		
		if (preloadDocs) {
			for (Document d : corpus) {
				System.err.format("Openning doc: %s\n", d.getName());
			};
		}
		
		return corpus;
	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		GateUtils.registerComponentIfNot(CustomPR.class);
		
		Corpus corpus = getSpcCorpus(true);
		
		CustomPR pr = CustomPR.createInstance(new AnalyzeDocDelegate() {
			
			@Override
			public void analyzeDoc(Document doc) {
				myAnalyzeDoc(doc);
			}
		});
		
		pr.executeAnalysis(corpus);
		
		for (Entry<String, MultiSet<String>> e : map.entrySet()) {
			System.err.format("--- %s ---\n", e.getKey());
			e.getValue().printSorted(System.err, "\n");
		}
	}
	
	public static Map<String, MultiSet<String>> map = new HashMap<String, MultiSet<String>>();
	
	public static void myAnalyzeDoc(Document doc) {
		try {
		
			System.err.println(doc.getParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME));
			
			AnnotationSet hs = doc.getAnnotations("Sections").get("Heading");
			
			for (Annotation h: hs) {
				String num = (String) h.getFeatures().get("heading_number");
				MultiSet<String> set = map.get(num);
				if (set == null) {
					set = new MultiSet<String>();
					map.put(num, set);
				}
				
				set.add(
						LevenshteinWholeLineMatchingGazetteer.removeRedundantSpaces(
								GateUtils.getAnnotationContent(h, doc)
									.trim()
									.toLowerCase())
									
									.replaceAll("([0-9]\\.) ([0-9])", "$1$2")
									.replaceAll("([0-9]\\.[0-9])\\.", "$1")
									//.replaceAll("(\\.[0-9])\\.", "$1")
									
						);
			}

		
		} catch (Exception e) {
			throw new RuntimeException(e); 
		}
		
	}


}
