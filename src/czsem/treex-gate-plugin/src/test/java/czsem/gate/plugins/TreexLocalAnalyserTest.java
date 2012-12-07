package czsem.gate.plugins;

import gate.Annotation;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.learning.PRSetup;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.utils.GateUtils;

public class TreexLocalAnalyserTest {
	
	@Test
	public void englishSimpleTest() throws Exception {
    	GateUtils.initGateInSandBox();
    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexLocalAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexLocalAnalyser.class);
	    }
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    		.putFeature("serverPortNumber", 9999)	
	    		.putFeatureList("scenarioSetup", 
	    					"W2A::EN::Segment",
	    					"W2A::EN::Tokenize")//,
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("englishSimpleTest");
		Document doc = Factory.newDocument("Hallo world! Life is great, isn't it?");
		corpus.add(doc);
		Document doc2 = Factory.newDocument("This is the second document in the corpus.");
		corpus.add(doc2);
		analysis.setCorpus(corpus);
		analysis.execute();
		analysis.cleanup();
		
		Assert.assertEquals(doc.getAnnotations().size(), 13);
		Assert.assertEquals(doc2.getAnnotations().size(), 10);
		
		GateUtils.deleteAllPublicGateResources();
	}
	

	@Test(groups = { "slow" })
	public void czechFullTest() throws Exception {
    	GateUtils.initGateInSandBox();
    	Utils.loggerSetup(Level.OFF);
    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexLocalAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexLocalAnalyser.class);
	    }
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("serverPortNumber", 9997)	
	    			.putFeature("languageCode", "cs")
	    			.putFeature("showTreexLogInConsole", true)
	    			.putFeatureList("scenarioSetup", "W2A::CS::Segment", "./dedek.scen")
//	    					"ccc.scen",
//	    					"devel\\analysis\\cs\\s_w2t_dedek.scen")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "czechFullTest");
		Corpus corpus = Factory.newCorpus("czechFullTest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012");
		corpus.add(doc);
		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		analysis.cleanup();
		GateUtils.deleteAllPublicGateResources();
	}

	@Test
	public void czechSimpleTest() throws Exception {
    	GateUtils.initGateInSandBox();
    	Utils.loggerSetup(Level.ALL);
    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexLocalAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexLocalAnalyser.class);
	    }
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("serverPortNumber", 9998)	
	    			.putFeature("languageCode", "cs")
	    			.putFeatureList("scenarioSetup",
	    					"W2A::CS::Segment")//,
//	    					"W2A::CS::Tokenize")//,
//	    					"W2A::CS::TagMorce",
//	    					"W2A::CS::FixMorphoErrors")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("czechSimpleTest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012");
		corpus.add(doc);
		
		String strDoc2 = 
		
		"\n   11d)  Zákon  č.  218/2000  Sb.,  o  rozpočtových  pravidlech  a o změně"
		+"\n   některých   souvisejících   zákonů   (rozpočtová  pravidla),  ve  znění"
		+"\n   pozdějších předpisů."
		+"\n"
		+"\n   Zákon  č. 250/2000 Sb., o rozpočtových pravidlech územních rozpočtů, ve"
		+"\n   znění pozdějších předpisů."
		+"\n"
		+"\n   11e) Nařízení (ES) č. 1606/2002 Evropského parlamentu a Rady ze dne 19."
		+"\n   července 2002, o používání Mezinárodních účetních standardů."
		+"\n";
		
		Document doc2 = Factory.newDocument(strDoc2);
		corpus.add(doc2);
		
		String strDoc3 = 
		 "\n"
		+"\n   Změna: 126/2008 Sb."
		+"\n"
		+"\n   Změna: 304/2008 Sb. (část)"
		+"\n"
		+"\n   Změna: 230/2009 Sb."
		+"\n"
		+"\n   Změna: 304/2008 Sb."
		+"\n"
		+"\n   Změna: 227/2009 Sb."
		+"\n";
		
		Document doc3 = Factory.newDocument(strDoc3);
		corpus.add(doc3);

		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		analysis.cleanup();

		List<Annotation> ords = gate.Utils.inDocumentOrder(doc3.getAnnotations().get("Sentence"));
		int index = 0;
		
		long off [][] = {
				{  5,  24},
				{ 29,  55},
				{ 60,  79},
				{ 84, 103},
				{108, 127},
		};
		
		for (Annotation s : ords) {
			System.out.format("%3d %30s %3d\n", s.getStartNode().getOffset(), GateUtils.getAnnotationContent(s, doc3), s.getEndNode().getOffset());
			Assert.assertEquals((long) s.getStartNode().getOffset(), off[index][0]);
			Assert.assertEquals((long) s.getEndNode().getOffset(), off[index][1]);
			index++;
		}
		
		Assert.assertEquals(doc.getAnnotations().get("Sentence").size(), 2);
		Assert.assertTrue(doc2.getAnnotations().get("Sentence").size() > 1, 
				String.format("Sentences should be more than 1 but was: %d", doc2.getAnnotations().get("Sentence").size()));
		
		
		GateUtils.deleteAllPublicGateResources();
	}

}
