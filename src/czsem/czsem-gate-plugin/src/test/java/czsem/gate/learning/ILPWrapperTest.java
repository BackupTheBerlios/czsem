package czsem.gate.learning;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Utils;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.ml.MachineLearningPR;
import gate.util.GateException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import czsem.gate.GateUtils;
import czsem.gate.plugins.CustomPR;
import czsem.gate.utils.Config;

public class ILPWrapperTest {
	
	@BeforeSuite
	public static void init() throws IOException, URISyntaxException, GateException
	{
		if (! Gate.isInitialised())
		{
			Config.getConfig().setGateHome();
			Gate.init();
		}
		
		if (! GateUtils.isPrCalssRegisteredInCreole(MachineLearningPR.class))
		{
			GateUtils.registerPluginDirectory("Machine_Learning");
		}

		if (! GateUtils.isPrCalssRegisteredInCreole("gate.stanford.Parser"))
		{
			GateUtils.registerPluginDirectory("Parser_Stanford");			
		}		

		if (! GateUtils.isPrCalssRegisteredInCreole("gate.creole.tokeniser.DefaultTokeniser"))
		{
			GateUtils.registerPluginDirectory("ANNIE");			
		}		
		if (! GateUtils.isPrCalssRegisteredInCreole(CustomPR.class))
		{
			Gate.getCreoleRegister().registerComponent(CustomPR.class);			
		}		
	}
	
	public static Random rand = new Random();
	
	static String [] subjects = {"John", "Marry", "Alice", "Joe", "Lukas"};
	static String [] verbs = {"had", "made", "killed", "loved", "kicked", "knew"};
	static String [] attrs = {"nice", "horrible", "great", "miserable", "casual"};
	static String [] objects = {"dog", "car", "friend", "cat", "table", "chair"};

	public static String getRandomWordPlusEmpty(String [] words)
	{
		if (rand.nextBoolean())
		{
			return "";
		} else {
			return getRandomWord(words);
		}
	}

	public static String getRandomWord(String [] words)
	{
		return words[rand.nextInt(words.length)];
	}

	public static String getRandomPrefixPhrase(String prefix, String word1, String sep)
	{
		if (rand.nextBoolean())
		{
			return word1;		
		} else {
			return prefix + sep + word1;					
		}		
	}
	
	public static String getRandomConjunctSubjectPhrase(RandomSetMaker sm, String sep)
	{
		if (rand.nextBoolean())
		{
			return sm.selectNewSubject();		
		} else {
			return sm.selectNewSubject() + sep + sm.selectNewSubject();					
		}		
	}

	public static String getRandomConjunctPhrase(String word1, String word2, String sep)
	{
		if (rand.nextBoolean())
		{
			return word1;		
		} else {
			return word1 + sep + word2;					
		}
	}


	
	public static class RandomSetMaker
	{
		public Set<Integer> relevantSents;
		int currentSent = 0;
		int relevantSubjIndex = 0; 
		int maxSentrs;

		public RandomSetMaker(int sentsNum)
		{
			relevantSents = new HashSet<Integer>(sentsNum);
			maxSentrs = sentsNum;
		}


		
		public String selectNewSubject()
		{
			int i = rand.nextInt(subjects.length);
			
			if (i == relevantSubjIndex) relevantSents.add(currentSent);
			
			return subjects[i];						
		}

		protected String createRandomSentence()
		{
			StringBuilder sb = new StringBuilder();

			sb.append(getRandomConjunctSubjectPhrase(this, " and "));
			sb.append(' ');
			sb.append(getRandomWord(verbs));
			sb.append(' ');
			sb.append(
					getRandomConjunctPhrase(
							"a " +getRandomPrefixPhrase(getRandomWord(attrs), getRandomWord(objects), " "),
							"a " +getRandomPrefixPhrase(getRandomWord(attrs), getRandomWord(objects), " "),
							" and "));
			sb.append(".\n");
			
			
			return sb.toString();		
		}

		public String createRandomSentences()
		{
			StringBuilder sb = new StringBuilder();
			
			for (currentSent = 0; currentSent < maxSentrs; currentSent++) {
				sb.append(createRandomSentence());
			}
						
			return sb.toString();
		}
	}
	
	@Test
	public static void createTestDocument() throws ResourceInstantiationException, ExecutionException, IOException
	{
		
		RandomSetMaker rsm = new RandomSetMaker(10);
		
		Document doc = Factory.newDocument(rsm.createRandomSentences());
		doc.getFeatures().put("relevantSents", rsm.relevantSents);
		
		analyzeTestDocument(doc);

	}
	
	public static class MarkRelevantTokens implements CustomPR.AnalyzeDocDelegate
	{
		@Override
		public void analyzeDoc(Document doc) {
			
			@SuppressWarnings("unchecked")
			Set<Integer> relevant = (Set<Integer>) doc.getFeatures().get("relevantSents");
			
			List<Annotation> sents = Utils.inDocumentOrder(doc.getAnnotations().get("Sentence"));
			for (int i = 0; i < sents.size(); i++) {
				if (relevant.contains(i))
				{
					Annotation s = sents.get(i);
					AnnotationSet tocs = doc.getAnnotations().get("Token").getContained(
							s.getStartNode().getOffset(), 
							s.getEndNode().getOffset());
					
					for (Annotation t : tocs) {
						String ts = (String) t.getFeatures().get("string");
						if (Arrays.asList(objects).contains(ts))
						{
							
							FeatureMap fm = Factory.newFeatureMap();
							fm.put("class", "true");
							doc.getAnnotations().add(t.getStartNode(), t.getEndNode(), "RelevantToken", fm );
						}
					}
				}
			}
			
			
			System.err.println(doc.getName());
			System.err.println();
		}
	};

	public static void analyzeTestDocument(Document doc) throws ResourceInstantiationException, ExecutionException, IOException {
		PRSetup[] setup = {
				new PRSetup.SinglePRSetup("gate.creole.tokeniser.DefaultTokeniser"),
				new PRSetup.SinglePRSetup("gate.creole.splitter.SentenceSplitter"),
				new PRSetup.SinglePRSetup(CustomPR.class).putFeature("executionDelegate", new MarkRelevantTokens()),		
				new PRSetup.SinglePRSetup("gate.stanford.Parser"),
				new PRSetup.SinglePRSetup(MachineLearningPR.class)
					.putFeature("configFileURL", 
							ILPWrapperTest.class.getClassLoader().getResource("ILP_config.xml"))
					.putFeature("training", true),
		};
		
		SerialAnalyserController prs = PRSetup.buildGatePipeline(Arrays.asList(setup), "TrainController");
		System.err.println("presetup");
		
		Corpus corpus = Factory.newCorpus("testCorpus");
		corpus.add(doc);
		
		prs.setCorpus(corpus);
		prs.execute();
		
		GateUtils.saveGateDocumentToXML(doc, "doc.xml");
	}
	
	@Test
	public void learningTest() {
		System.err.println("t0");
	}

	@Test
	public void learningTest2() {
		System.err.println("t1");
	}
}
