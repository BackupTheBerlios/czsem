package czsem.gate.plugins;

import java.util.Arrays;
import java.util.List;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.GateUtils;
import czsem.gate.learning.PRSetup;
import czsem.gate.learning.PRSetup.SinglePRSetup;

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
	    			.putFeatureList("scenarioSetup", 
	    					"W2A::EN::Segment",
	    					"W2A::EN::Tokenize")//,
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("englishSimpleTest");
		Document doc = Factory.newDocument("Hallo world! Life is great, isn't it?");
		corpus.add(doc);
		analysis.setCorpus(corpus);
		analysis.execute();
		
		Assert.assertEquals(doc.getAnnotations().size(), 13);
		
		GateUtils.deleteAllPublicGateResources();
	}
	
	@Test
	public void czechSimpleTest() throws Exception {
    	GateUtils.initGateInSandBox();
    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexLocalAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexLocalAnalyser.class);
	    }
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexLocalAnalyser.class)
	    			.putFeature("languageCode", "cs")
	    			.putFeatureList("scenarioSetup", 
	    					"W2A::CS::Segment",
	    					"W2A::CS::Tokenize")//,
//	    					"W2A::CS::TagMorce",
//	    					"W2A::CS::FixMorphoErrors")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "englishSimpleTest");
		Corpus corpus = Factory.newCorpus("englishSimpleTest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že?");
		corpus.add(doc);
		analysis.setCorpus(corpus);
		analysis.execute();
		
		Assert.assertEquals(doc.getAnnotations().size(), 11);
		
		GateUtils.deleteAllPublicGateResources();
	}

}
