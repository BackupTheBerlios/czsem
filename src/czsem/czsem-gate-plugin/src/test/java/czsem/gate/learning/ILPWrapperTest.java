package czsem.gate.learning;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.ml.MachineLearningPR;
import gate.util.AnnotationDiffer;
import gate.util.GateException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import czsem.gate.GateUtils;
import czsem.gate.plugins.CustomPR;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.plugins.LearningEvaluator.CentralResultsRepository;
import czsem.gate.plugins.LearningEvaluator.DocumentDiff;
import czsem.gate.plugins.PutTokenIdFeaturePR;
import czsem.gate.utils.Config;

public class ILPWrapperTest {
	
	private RandomSentencesMaker.MarkRelevantTokens markRelevantTokens = 
		new RandomSentencesMaker.MarkRelevantTokens();

	private SerialAnalyserController mainAnalyzerContainer;

	private MachineLearningPR machineLearnigPR;

	private PRSetup[] nlpAnalysisResourcesSetup = {
			new PRSetup.SinglePRSetup("gate.creole.tokeniser.DefaultTokeniser"),
			new PRSetup.SinglePRSetup("gate.creole.splitter.SentenceSplitter"),
			new PRSetup.SinglePRSetup(CustomPR.class).putFeature("executionDelegate", markRelevantTokens),		
			new PRSetup.SinglePRSetup("gate.stanford.Parser"),
			new PRSetup.SinglePRSetup(PutTokenIdFeaturePR.class),				
	};

	public ILPWrapperTest() throws IOException, URISyntaxException, GateException
	{
		init();
		
		mainAnalyzerContainer = PRSetup.buildGatePipeline(Arrays.asList(nlpAnalysisResourcesSetup), "TestAnalysis");
		
	}

	public static void init() throws IOException, URISyntaxException, GateException
	{
		Logger logger = Logger.getRootLogger();
	    logger.setLevel(Level.INFO);
		BasicConfigurator.configure();

		if (! Gate.isInitialised())
		{
			Config.getConfig().setGateHome();
			Gate.init();
		}
		
		if (! GateUtils.isPrCalssRegisteredInCreole(MachineLearningPR.class)) {
			GateUtils.registerPluginDirectory("Machine_Learning");
		}

		if (! GateUtils.isPrCalssRegisteredInCreole("gate.stanford.Parser")) {
			GateUtils.registerPluginDirectory("Parser_Stanford");			
		}		

		if (! GateUtils.isPrCalssRegisteredInCreole("gate.creole.tokeniser.DefaultTokeniser"))	{
			GateUtils.registerPluginDirectory("ANNIE");			
		}		
		
		if (! GateUtils.isPrCalssRegisteredInCreole(CustomPR.class)) {
			Gate.getCreoleRegister().registerComponent(CustomPR.class);			
		}

		if (! GateUtils.isPrCalssRegisteredInCreole(PutTokenIdFeaturePR.class)) {
			Gate.getCreoleRegister().registerComponent(PutTokenIdFeaturePR.class);			
		}
		
		if (! GateUtils.isPrCalssRegisteredInCreole(LearningEvaluator.class)) {
			Gate.getCreoleRegister().registerComponent(LearningEvaluator.class);			
		}
	}

	@Test
	public void ilpWrapperTest() throws ResourceInstantiationException, ExecutionException, IOException
	{		
		analyzeTestDocument(createTestDocument(20), true);
		analyzeTestDocument(createTestDocument(20), false);
		
		CentralResultsRepository repo = LearningEvaluator.CentralResultsRepository.repository;
		List<DocumentDiff> difs = repo.getDocumentDiffs(repo.getContent().iterator().next());
		AnnotationDiffer diff = difs.iterator().next().diff[0];
		
		
		AssertJUnit.assertTrue(String.format("fmeasure too small %f", diff.getFMeasureStrict(1.0)),
				diff.getFMeasureStrict(1.0) > 0.5);
		AssertJUnit.assertTrue(String.format("recall too small %f", diff.getRecallStrict()),
				diff.getRecallStrict() > 0.5);
		AssertJUnit.assertTrue(String.format("precision too small %f", diff.getPrecisionStrict()),
				diff.getPrecisionStrict() > 0.5);
		AssertJUnit.assertTrue(String.format("correctly classifed too small %d", diff.getCorrectMatches()),
				diff.getCorrectMatches() > 3);
	}
	
	
	public static Document createTestDocument(int sentsNum) throws ResourceInstantiationException, ExecutionException, IOException
	{
		
		RandomSentencesMaker rsm = new RandomSentencesMaker(sentsNum);
		
		Document doc = Factory.newDocument(rsm.createRandomSentences());
		doc.getFeatures().put("relevantSents", rsm.relevantSents);
		return doc;
	}

	public void analyzeTestDocument(Document doc, boolean training) throws ResourceInstantiationException, ExecutionException, IOException {
		markRelevantTokens.setTraining(training);
		
		if (training)
		{
			machineLearnigPR = (MachineLearningPR) new PRSetup.SinglePRSetup(MachineLearningPR.class)
				.putFeature("configFileURL", 
					ILPWrapperTest.class.getClassLoader().getResource("ILP_config.xml"))
					.putFeature("training", training).createPR();
			mainAnalyzerContainer.add(machineLearnigPR);		

		} else {
			machineLearnigPR.setTraining(training);
			//ml.reInit();
			mainAnalyzerContainer.add(new PRSetup.SinglePRSetup(LearningEvaluator.class).createPR());		
		}
		
		
		Corpus corpus = Factory.newCorpus("testCorpus");
		corpus.add(doc);
		
		mainAnalyzerContainer.setCorpus(corpus);
		mainAnalyzerContainer.execute();
		
		//GateUtils.saveGateDocumentToXML(doc, "doc.xml");
	}	
}
