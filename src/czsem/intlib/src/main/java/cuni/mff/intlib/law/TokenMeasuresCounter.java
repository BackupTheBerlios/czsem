package cuni.mff.intlib.law;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;
import czsem.gate.learning.PRSetup;
import czsem.gate.learning.experiments.CzechLawLearningExperiment.CzechLawDataSet;
import czsem.gate.plugins.CustomPR;
import czsem.gate.utils.GateUtils;

public class TokenMeasuresCounter {

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(CustomPR.class);
		
		CzechLawDataSet dataset = new CzechLawDataSet(null);
		Corpus corpus = dataset.getTestCorpus();
//		Corpus corpus = dataset.getCorpus();
		
		PRSetup [] prs = {
				new PRSetup.SinglePRSetup(CustomPR.class)
				.putFeature("executionDelegate", new CustomPR.AnalyzeDocDelegate() {

					@Override
					public void analyzeDoc(Document doc) {
						analyzeDocumentTocMeasures(doc);
						
					}
					
				})
				/*
				.putFeature("annotationSetName", set)
				.putFeatureList("annotationTypes", 
													"Cenovy_vymer",
													"Document",
													"Dokument",
													"Instituce",
													"Plne_zneni",
													"Rozhodnuti_soudu",
													"Ucinnost",
													"Vyhlaska",
													"Zakon",
													"Zkratka")
				.putFeatureList("dumpTypes")
				.putFeature("outputDirectoryUrl", new File("export/"+set+"/data").toURI().toURL())
				.putFeature("dataStore", corpus.getDataStore())
				.putFeature("useSuffixForDumpFiles", false)
				*/								
		};
		
		SerialAnalyserController p = PRSetup.buildGatePipeline(prs, "export");
		p.setCorpus(corpus);
		p.execute();
	}

	protected static void analyzeDocumentTocMeasures(Document doc) {
		System.err.println("starting doc: " + doc.getName());
		
		AnnotationSet tocs = doc.getAnnotations("Treex").get("Token");
		doc.getFeatures().put("numOfTokens", tocs.size());
		
		String [] types = {"Instituce", "Rozhodnuti_soudu", "Ucinnost", "Zakon"};
		String [] sets = {"Paum_small", "Paum", "Paum_pos", "Paum_pos_orth_sent", "JTagger", "Key"};
		
		for (int s = 0; s < sets.length; s++) {
			AnnotationSet as = doc.getAnnotations(sets[s]);
			
			for (int t = 0; t < types.length; t++) {
				AnnotationSet anns = as.get(types[t]);
				for (Annotation a : anns) {
					AnnotationSet contained = tocs.getContained(a.getStartNode().getOffset(), a.getEndNode().getOffset());
					for (Annotation toc: contained){
						as.add(toc.getStartNode(), toc.getEndNode(), types[t]+"Token", Factory.newFeatureMap());
					}
				}
			}			
		}

		
		
		
	}

}
