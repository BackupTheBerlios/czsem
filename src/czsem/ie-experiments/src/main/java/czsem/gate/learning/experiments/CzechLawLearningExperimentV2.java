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
					"file:/C:/data/law/cross-validation_2013-03-28/gate_store",
					"nscr___1365091888007___8978",
					"Key",
					"Treex",
					"czech_law",
					learnigAnnotationTypes);
		}
		
		/*
		public Corpus getTestCorpus() throws PersistenceException, ResourceInstantiationException {
		    DataStore ds = GateUtils.openDataStore(dataStore);
		    Corpus corpus = GateUtils.loadCorpusFormDatastore(ds, "test___1354799449330___3473");			
		    return corpus; 
		}
		*/

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
					"file:/C:/data/law/cross-validation_2013-03-28/gate_store",
					"uscr___1365091880223___9866",
					"Key",
					"Treex",
					"czech_law",
					learnigAnnotationTypes);
		}
		
		/*
		public Corpus getTestCorpus() throws PersistenceException, ResourceInstantiationException {
		    DataStore ds = GateUtils.openDataStore(dataStore);
		    Corpus corpus = GateUtils.loadCorpusFormDatastore(ds, "test___1354799449330___3473");			
		    return corpus; 
		}
		*/

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
					new File(controlledCrossDir).toURI().toURL(), null, true);
		    
			logger.info("saving results, counting time statistics...");
			MachineLearningExperimenter.saveResults(results_file_name);

			GateUtils.deleteAllPublicGateResources();
		}			
    }
	
	

	public static String[] learnigAnnotationTypes = {
			"Dokument",//			0	262		0	0
			"Instituce",//			0	8550	0	0
			"Plne_zneni",//			0	463		0	0
			"Rozhodnuti_soudu",//	0	3245	0	0
			"Ucinnost",//			0	468		0	0
			"Zakon",//				0	6653	0	0
			"Zkratka",//			0	499		0	0
			
	};
	
	public static String  crossValidationDefNscr = "C:/data/law/cross-validation_2013-03-28/nscr"; 
	public static String  crossValidationDefUscr = "C:/data/law/cross-validation_2013-03-28/uscr"; 

	public static void main(String[] args) throws Exception {
		
		MachineLearningExperimenter.initEnvironment();

		TrainTest [] engines = {
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_small/Paum_small.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum/Paum_config.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos/Paum_config_pos.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos_orth_sent/Paum_config_pos_orth_sent.xml")))
		};
		
		
		performExperiment(
				NsCrLawDataSet.getFactory(),
				learnigAnnotationTypes,
				"nscr_results_10_cross.csv",
				crossValidationDefNscr,
				engines);

		performExperiment(
				UsCrLawDataSet.getFactory(),
				learnigAnnotationTypes,
				"uscr_results_10_cross.csv",
				crossValidationDefUscr,
				engines);

	}

}
