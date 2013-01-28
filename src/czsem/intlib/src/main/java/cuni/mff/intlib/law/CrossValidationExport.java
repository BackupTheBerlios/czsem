package cuni.mff.intlib.law;

import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;

import java.io.File;

import czsem.gate.learning.PRSetup;
import czsem.gate.learning.experiments.CzechLawLearningExperiment.CzechLawDataSet;
import czsem.gate.plugins.CrossValidation;
import czsem.gate.utils.GateUtils;

public class CrossValidationExport {
	
	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		GateUtils.registerPluginDirectory("Copy_Annots_Between_Docs");
		
		Gate.getCreoleRegister().registerComponent(CorpusNameAwareExporter.class);
		Gate.getCreoleRegister().registerComponent(CrossValidation.class);
		
		CzechLawDataSet dataset = new CzechLawDataSet(null);
		Corpus corpus = dataset.getCorpus();
		
		String set = "Paum";
		
		PRSetup [] prs = {
				new PRSetup.SinglePRSetup(CorpusNameAwareExporter.class)
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
		};
		
		SerialAnalyserController p = PRSetup.buildGatePipeline(prs, "export");
		
		new PRSetup.SinglePRSetup(CrossValidation.class)
			.putFeature("testingPR", p)
			.putFeature("trainingPR", Factory.createResource(SerialAnalyserController.class.getCanonicalName()))
			.putFeature("numberOfFolds", 10)
			.putFeature("corpus", corpus)
			.createPR().execute();
	}

}
