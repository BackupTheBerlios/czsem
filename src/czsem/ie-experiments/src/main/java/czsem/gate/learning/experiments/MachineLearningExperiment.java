package czsem.gate.learning.experiments;

import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.annotdelete.AnnotationDeletePR;
import gate.persist.PersistenceException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.JDOMException;

import czsem.gate.learning.DataSet;
import czsem.gate.learning.MLEngine;
import czsem.gate.learning.PRSetup;
import czsem.gate.learning.MLEngine.MLEngineConfig;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.plugins.ControlledCrossValidation;
import czsem.gate.plugins.CrossValidation;
import czsem.gate.plugins.LearningEvaluator;


public class MachineLearningExperiment
{
	public static interface TrainTest
	{
		List<PRSetup> getTrainControllerSetup(MLEngine.MLEngineConfig config) throws MalformedURLException;
		List<PRSetup> getTestControllerSetup(MLEngine.MLEngineConfig config) throws MalformedURLException;
		String getDefaultOutputAS();
		String getDefaultLearningAnnotationType();
		void clearSevedFilesDirectory(MLEngineConfig config);
		boolean getClearOutputAsBeforeTesting();
	}
	
	public static interface EngineFactory 
	{
		TrainTest createEngine(String annot_type);		
	}

	
	/** Usually TectoMT, loaded form dataSet.tectoMTAS **/
	protected String inputLearninigAS;
	
	protected DataSet dataSet;
	protected MachineLearningExperiment.TrainTest[] engines;

	private List<LearningEvaluator> evaluation_register;
	
	public MachineLearningExperiment(DataSet dataSet, MachineLearningExperiment.TrainTest ... engines)
	{
		this.dataSet = dataSet;
		this.engines = engines;
		
		this.evaluation_register = new ArrayList<LearningEvaluator>();
		
		inputLearninigAS = dataSet.getTectoMTAS();
	}

	
	
	public List<PRSetup> getTrainControllerSetup() throws JDOMException, IOException
	{
		List<PRSetup> prs = new ArrayList<PRSetup>();
		

		return addTrainMLEngines(prs);
	}
	
	public List<PRSetup> getTestControllerSetup() throws JDOMException, IOException
	{
		List<PRSetup> prs = new ArrayList<PRSetup>();
		
		//Reset engines outASs		
		String [] outAS = new String[engines.length];
		for (int i = 0; i < outAS.length; i++) {
			if (engines[i].getClearOutputAsBeforeTesting())
				outAS[i] = engines[i].getDefaultOutputAS();
		}
		prs.add(new PRSetup.SinglePRSetup(AnnotationDeletePR.class)
			.putFeature("setsToRemove", Arrays.asList(outAS)));		
		
		return addTestMLEngines(prs);
	}

	
	protected MLEngineConfig getMLEngineConfig(MachineLearningExperiment.TrainTest engine)
	{
		MLEngineConfig ret = new MLEngineConfig();
		ret.experimentLearningConfigsDirectory = dataSet.getLearnigConfigDirectory();
		ret.inputAS = inputLearninigAS;
		ret.outputAS = engine.getDefaultOutputAS();
		ret.learnigAnnotationType = engine.getDefaultLearningAnnotationType();
		ret.keyAS = dataSet.getKeyAS();
		ret.originalLearnigAnnotationTypes = Arrays.asList(dataSet.getLearnigAnnotationTypes());
		
		ret.evaluation_register = this.evaluation_register;
		return ret;
	}

	protected void clearEnginesSevedFiles() {
		for (int i = 0; i < engines.length; i++)
		{
			engines[i].clearSevedFilesDirectory(getMLEngineConfig(engines[i]));				
		}
	}


	protected List<PRSetup> addTrainMLEngines(List<PRSetup> prs) throws JDOMException, IOException
	{
		for (int i = 0; i < engines.length; i++)
		{
			//TectoMT reset
			prs.add(new PRSetup.SinglePRSetup(AnnotationDeletePR.class)
				.putFeatureList("annotationTypes", engines[i].getDefaultLearningAnnotationType())
				.putFeatureList("setsToRemove", inputLearninigAS));		

			prs.addAll(engines[i].getTrainControllerSetup(getMLEngineConfig(engines[i])));				
		}
		return prs;
	}

	protected List<PRSetup> addTestMLEngines(List<PRSetup> prs) throws JDOMException, IOException
	{
		for (int i = 0; i < engines.length; i++)
		{
			//TectoMT reset
			prs.add(new PRSetup.SinglePRSetup(AnnotationDeletePR.class)
				.putFeatureList("annotationTypes", engines[i].getDefaultLearningAnnotationType())
				.putFeatureList("setsToRemove", inputLearninigAS));		

			
			prs.addAll(engines[i].getTestControllerSetup(getMLEngineConfig(engines[i])));				
		}
		return prs;
	}

	
	public SerialAnalyserController getTrainController() throws ResourceInstantiationException, JDOMException, IOException
	{
		return PRSetup.buildGatePipeline(getTrainControllerSetup(), "TrainPipeline");		
	}

	public SerialAnalyserController getTestController() throws ResourceInstantiationException, JDOMException, IOException
	{
		return PRSetup.buildGatePipeline(getTestControllerSetup(), "TestPipeline");		
	}

	
	public void trainOnly() throws PersistenceException, ResourceInstantiationException, JDOMException, IOException, ExecutionException
	{
	    SerialAnalyserController train_controller = getTrainController();
	    
	    train_controller.setCorpus(dataSet.getCorpus());			    	    	    
	    train_controller.execute();
	}
	
	public void testOnly() throws ResourceInstantiationException, JDOMException, IOException, PersistenceException, ExecutionException
	{
	    SerialAnalyserController test_controller = getTestController();
	    
	    test_controller.setCorpus(dataSet.getCorpus());			    	    	    
	    test_controller.execute();
	}

	
	public void crossValidation(int numOfFolds) throws ExecutionException, ResourceInstantiationException, PersistenceException, JDOMException, IOException
	{
		crossValidation(numOfFolds, null);
	}


	public void controlledCrossValidation(int numOfFolds, URL foldDefinitionDirectoryUrl, Runnable beforeTrainingCallback, boolean syncDocuments) throws ResourceInstantiationException, PersistenceException, ExecutionException, JDOMException, IOException
	{
		SinglePRSetup crossvalidSetup = new PRSetup.SinglePRSetup(ControlledCrossValidation.class)
			.putFeature("foldDefinitionDirectoryUrl", foldDefinitionDirectoryUrl)
			.putFeature("syncDocuments", syncDocuments);
		
		crossValidation(numOfFolds, beforeTrainingCallback, crossvalidSetup);		
	}

	public void crossValidation(int numOfFolds, Runnable beforeTrainingCallback, SinglePRSetup crossvalidSetup) throws ResourceInstantiationException, JDOMException, IOException, PersistenceException, ExecutionException
	{
	    SerialAnalyserController train_controller = getTrainController();
	    SerialAnalyserController test_controller = getTestController();
	    
		CrossValidation crossvalid = (CrossValidation) crossvalidSetup
			.putFeature("corpus", dataSet.getCorpus())
			.putFeature("numberOfFolds", numOfFolds)
			.putFeature("trainingPR", train_controller)
			.putFeature("testingPR", test_controller).createPR();
		
		crossvalid.addBeforeTrainingCallback(new Runnable() {
			@Override
			public void run() {
				clearEnginesSevedFiles();
			}
		});
		crossvalid.addBeforeTrainingCallback(beforeTrainingCallback);
		
		crossvalid.evaluation_register = evaluation_register;
		crossvalid.execute();
		
		LearningEvaluator.CentralResultsRepository.repository.logAll();		
		
	}

	public void crossValidation(int numOfFolds, Runnable beforeTrainingCallback) throws ResourceInstantiationException, PersistenceException, ExecutionException, JDOMException, IOException
	{		
		crossValidation(numOfFolds, beforeTrainingCallback, 
				new PRSetup.SinglePRSetup(CrossValidation.class));		
	}
}
