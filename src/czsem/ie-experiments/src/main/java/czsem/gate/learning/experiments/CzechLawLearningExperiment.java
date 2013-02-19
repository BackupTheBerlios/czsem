package czsem.gate.learning.experiments;

import gate.Corpus;
import gate.DataStore;
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
import czsem.gate.learning.DataSet.DataSetReduce;
import czsem.gate.learning.DataSet.DatasetFactory;
import czsem.gate.learning.MLEngine.*;
import czsem.gate.learning.MLEngineEncapsulate.*;
import czsem.gate.learning.experiments.MachineLearningExperiment.TrainTest;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.TimeBenchmarkUtils;


public class CzechLawLearningExperiment {
    static Logger logger = Logger.getLogger(CzechLawLearningExperiment.class);
	
	public static class CzechLawDataSet extends DataSetImpl {

		public CzechLawDataSet(String[] learnigAnnotationTypes) throws URISyntaxException, IOException {
			super(
					"file:/C:/data/law/corpus_references_2012-12-03/GATE_store",
					"train___1354802337250___6855",
					"Key",
					"Treex",
					"czech_law",
					learnigAnnotationTypes);
		}
		
		public Corpus getTestCorpus() throws PersistenceException, ResourceInstantiationException {
		    DataStore ds = GateUtils.openDataStore(dataStore);
		    Corpus corpus = GateUtils.loadCorpusFormDatastore(ds, "test___1354799449330___3473");			
		    return corpus; 
		}

		public static DatasetFactory getFactory() {
			return new DatasetFactory() {
				
				@Override
				public DataSet createDataset(String ... learnigAnnotationTypes)	throws URISyntaxException, IOException {
					return new CzechLawDataSet(learnigAnnotationTypes);
				}
			};
		}
		
	}
	
	public static void performExperiment(
    		DatasetFactory ds_factory,
    		double ds_reduce_retio,
    		String [] eval_annot_types,
    		int repeatCount,
    		int numFolds,
    		String results_file_name, 
    		TrainTest ... engines) throws URISyntaxException, IOException, ExecutionException, ResourceInstantiationException, PersistenceException, JDOMException, BenchmarkReportInputFileFormatException
    {
		for (int a=0; a<repeatCount; a++)
		{
			for (String annot_type : eval_annot_types)
			{
				logger.info(String.format("Performing evaluation for annotation type: %s", annot_type));
				
			    LearningEvaluator.CentralResultsRepository.repository.clear();
			    TimeBenchmarkUtils.enableGateTimeBenchmark();
			    
				final DataSet dataset =  new DataSetReduce(
						ds_factory.createDataset(annot_type),
						ds_reduce_retio);
				
				MachineLearningExperiment experiment = new MachineLearningExperiment(dataset, engines);
				
				if (numFolds == 1)
				{
					Corpus testCorpus = ((CzechLawDataSet) ds_factory.createDataset(annot_type)).getTestCorpus();

					experiment.trainOnly();

					experiment.testOnly(testCorpus);
					
					logger.info("saving results, counting time statistics...");
					MachineLearningExperimenter.saveResults(results_file_name);
				}
				else
				{
					//experiment.crossValidation(numFolds);
					experiment.controlledCrossValidation(10, 
							new File("../intlib/train-10-fold-cross/").toURI().toURL(), null, false);
				    
					logger.info("saving results, counting time statistics...");
					MachineLearningExperimenter.saveResults(results_file_name);
				}

				GateUtils.deleteAllPublicGateResources();
			}			
		}    	
    }



	public static void main(String[] args) throws Exception {
		MachineLearningExperimenter.initEnvironment();
		//Logger.getLogger(CrossValidation.class).setLevel(Level.DEBUG);
		
		String results_file_name = "weka_results_token_bseline_test.csv";
		
		new File(results_file_name).delete();
		
		String[] learnigAnnotationTypes = {
				/*
				//Cenovy_vymer	1
				//Document	83
				//Dokument	7
				"Instituce",		//2491
				//"Plne_zneni",		//18
				"Rozhodnuti_soudu",	//899
				"Ucinnost", 		//114
				//Vyhlaska	1
				"Zakon",			//1105
				//"Zkratka",			//163
				
				/**/
				"InstituceToken",		
				"Rozhodnuti_souduToken",
				"UcinnostToken", 		
				"ZakonToken",
				/**/

		};
		
		//MainFrame.getInstance().setVisible(true);

		performExperiment(
				CzechLawDataSet.getFactory(), 1.0, learnigAnnotationTypes, 1, 1, results_file_name, 
//				CzechLawDataSet.getFactory(), 1.0, learnigAnnotationTypes, 1, 10, results_file_name,

//				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_small/Paum_small.xml"))),
				new MLEvaluate(new FakeEngine("baseline"))
				/*
				new MLEvaluate(new FakeEngine("JTagger")),
				new MLEvaluate(new FakeEngine("Paum_small")),
				new MLEvaluate(new FakeEngine("Paum")),
				new MLEvaluate(new FakeEngine("Paum_pos")),
				new MLEvaluate(new FakeEngine("Paum_pos_orth_sent"))


				/*
				new MLEvaluate(new FakeEngine("JTagger")),

				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_small/Paum_small.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum/Paum_config.xml"))),
//				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_afun/Paum_config_afun.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos/Paum_config_pos.xml"))),
				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos_orth_sent/Paum_config_pos_orth_sent.xml")))
				/**/
//				new MLEvaluate(new CreatePersistentMentions(new PaumEngine("Paum_pos_orth_sent_tolerant/Paum_config_pos_orth_sent_tolerant.xml")))
				
		);
		
	}

}
