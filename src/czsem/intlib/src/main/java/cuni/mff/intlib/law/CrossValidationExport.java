package cuni.mff.intlib.law;

import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;

import java.io.File;

import czsem.gate.learning.DataSet;
import czsem.gate.learning.PRSetup;
import czsem.gate.learning.experiments.CzechLawLearningExperimentV2;
import czsem.gate.plugins.ControlledCrossValidation;
import czsem.gate.plugins.CrossValidation;
import czsem.gate.utils.GateUtils;

public class CrossValidationExport {
	
	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		GateUtils.registerPluginDirectory("Copy_Annots_Between_Docs");
		
		Gate.getCreoleRegister().registerComponent(CorpusNameAwareExporter.class);
		Gate.getCreoleRegister().registerComponent(CrossValidation.class);
		Gate.getCreoleRegister().registerComponent(ControlledCrossValidation.class);

/*		
		DataSet dataset = new CzechLawLearningExperimentV2.NsCrLawDataSet(null);
		String crossValidationDef = CzechLawLearningExperimentV2.crossValidationDefNscr;
/**/
		DataSet dataset = new CzechLawLearningExperimentV2.UsCrLawDataSet(null);		
		String crossValidationDef = CzechLawLearningExperimentV2.crossValidationDefUscr;
/**/		
		
		
		Corpus corpus = dataset.getCorpus();
		
/*
		String set = "Key";
		
		String set = "Paum_small";
		String set = "Paum";
		String set = "Paum_pos";
		String set = "Paum_pos_orth_sent";
*/
		String set = "Paum_pos_orth_sent";
		
		PRSetup [] prs = {
				new PRSetup.SinglePRSetup(CorpusNameAwareExporter.class)
				.putFeature("annotationSetName", set)
				.putFeatureList("annotationTypes", CzechLawLearningExperimentV2.learnigAnnotationTypes)
				.putFeatureList("dumpTypes")
				.putFeature("outputDirectoryUrl", new File("export/"+dataset.getClass().getName()+"/"+set+"/data").toURI().toURL())
				.putFeature("dataStore", corpus.getDataStore())
				.putFeature("useSuffixForDumpFiles", false)								
		};
		
		SerialAnalyserController p = PRSetup.buildGatePipeline(prs, "export");
		
		new PRSetup.SinglePRSetup(ControlledCrossValidation.class)
			.putFeature("testingPR", p)
			.putFeature("trainingPR", Factory.createResource(SerialAnalyserController.class.getCanonicalName()))
			.putFeature("numberOfFolds", 10)
			.putFeature("corpus", corpus)
			.putFeature("foldDefinitionDirectoryUrl", new File(crossValidationDef).toURI().toURL())
			.createPR().execute();

/*		
		new PRSetup.SinglePRSetup(CrossValidation.class)
			.putFeature("testingPR", p)
			.putFeature("trainingPR", p)
			.putFeature("numberOfFolds", 10)
			.putFeature("corpus", corpus)
			.createPR().execute();
/**/			
	}

}
