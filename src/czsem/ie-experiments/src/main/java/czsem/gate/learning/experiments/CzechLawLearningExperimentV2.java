package czsem.gate.learning.experiments;

import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.reporting.exceptions.BenchmarkReportInputFileFormatException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import czsem.gate.learning.DataSet;
import czsem.gate.learning.DataSet.DataSetImpl;
import czsem.gate.learning.DataSet.DatasetFactory;
import czsem.gate.learning.MLEngine.FakeEngine;
import czsem.gate.learning.MLEngine.PaumEngine;
import czsem.gate.learning.MLEngineEncapsulate.CreatePersistentMentions;
import czsem.gate.learning.MLEngineEncapsulate.MLEvaluate;
import czsem.gate.learning.experiments.MachineLearningExperiment.TrainTest;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.TimeBenchmarkUtils;


public class CzechLawLearningExperimentV2 {
	static Logger logger = Logger.getLogger(CzechLawLearningExperimentV2.class);
	
	public static class NsCrLawDataSet extends DataSetImpl {

		public NsCrLawDataSet(String[] learnigAnnotationTypes) throws URISyntaxException, IOException {
			super(
					"file:/C:/data/law/cross-validation_2013-04-23/gate_store",
					"nscr___1366788673730___9889",
					"Key",
					"Treex",
					"czech_law",
					learnigAnnotationTypes);
		}
		
		public static DatasetFactory getFactory() {
			return new DatasetFactory() {
				
				@Override
				public DataSet createDataset(String ... learnigAnnotationTypes)	throws URISyntaxException, IOException {
					return new NsCrLawDataSet(learnigAnnotationTypes);
				}
			};
		}
		
	}

	public static class UsCrLawDataSet extends DataSetImpl {

		public UsCrLawDataSet(String[] learnigAnnotationTypes) throws URISyntaxException, IOException {
			super(
					"file:/C:/data/law/cross-validation_2013-04-23/gate_store",
					"uscr___1366743006461___6854",
					"Key",
					"Treex",
					"czech_law",
					learnigAnnotationTypes);
		}
		
		public static DatasetFactory getFactory() {
			return new DatasetFactory() {
				
				@Override
				public DataSet createDataset(String ... learnigAnnotationTypes)	throws URISyntaxException, IOException {
					return new UsCrLawDataSet(learnigAnnotationTypes);
				}
			};
		}
		
	}


	public static void performExperiment(
    		DatasetFactory ds_factory,
    		String [] eval_annot_types,
    		String results_file_name,
    		String controlledCrossDir,
    		boolean syncDocs,
    		TrainTest ... engines) throws URISyntaxException, IOException, ExecutionException, ResourceInstantiationException, PersistenceException, JDOMException, BenchmarkReportInputFileFormatException
    {
		for (String annot_type : eval_annot_types)
		{
			logger.info(String.format("Performing evaluation for annotation type: %s", annot_type));
			
		    LearningEvaluator.CentralResultsRepository.repository.clear();
		    TimeBenchmarkUtils.enableGateTimeBenchmark();
		    
			final DataSet dataset =  ds_factory.createDataset(annot_type);
			
			MachineLearningExperiment experiment = new MachineLearningExperiment(dataset, engines);
			
			experiment.controlledCrossValidation(10, 
					new File(controlledCrossDir).toURI().toURL(), null, syncDocs);
		    
			logger.info("saving results, counting time statistics...");
			MachineLearningExperimenter.saveResults(results_file_name);

			GateUtils.deleteAllPublicGateResources();
		}			
    }
	
	

	public static String[] learnigAnnotationTypes = {
//			"Dokument",//			0	262		0	0
			"Instituce",//			0	8550	0	0
//			"Plne_zneni",//			0	463		0	0
			"Rozhodnuti_soudu",//	0	3245	0	0
			"Ucinnost",//			0	468		0	0
			"Zakon",//				0	6653	0	0
			"Zkratka",//			0	499		0	0

//			"DokumentToken",//			0	262		0	0
			"InstituceToken",//			0	8550	0	0
//			"Plne_zneniToken",//		0	463		0	0
			"Rozhodnuti_souduToken",//	0	3245	0	0
			"UcinnostToken",//			0	468		0	0
			"ZakonToken",//				0	6653	0	0
			"ZkratkaToken",//			0	499		0	0

	};
	
	public static String  crossValidationDefNscr = "C:/data/law/cross-validation_2013-04-23/nscr"; 
	public static String  crossValidationDefUscr = "C:/data/law/cross-validation_2013-04-23/uscr"; 

	public static void main(String[] args) throws Exception {
		
		MachineLearningExperimenter.initEnvironment();

		TrainTest [] paumEngines = {
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_small/Paum_small.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum/Paum_config.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos/Paum_config_pos.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos_orth_sent/Paum_config_pos_orth_sent.xml")))
		};

		TrainTest [] fakeEngines = {
				new MLEvaluate(new FakeEngine("baseline")),
/*
				new MLEvaluate(new FakeEngine("JTagger")),
				new MLEvaluate(new FakeEngine("Paum_small")),
				new MLEvaluate(new FakeEngine("Paum")),
				new MLEvaluate(new FakeEngine("Paum_pos")),
				new MLEvaluate(new FakeEngine("Paum_pos_orth_sent"))
*/				
		};
		
		TrainTest[] engines = fakeEngines;		
		boolean syncSocs = false;
		
		/**/
		performExperiment(
				NsCrLawDataSet.getFactory(),
				learnigAnnotationTypes,
//				"nscr_results_10_cross_tocs.csv",
				"nscr_baseline.csv",
				crossValidationDefNscr,
				syncSocs,
				engines);
/**/
		performExperiment(
				UsCrLawDataSet.getFactory(),
				learnigAnnotationTypes,
				"uscr_baseline.csv",
//				"nscr_results_10_cross_tocs.csv",
				crossValidationDefUscr,
				syncSocs,
				engines);

	}

}
