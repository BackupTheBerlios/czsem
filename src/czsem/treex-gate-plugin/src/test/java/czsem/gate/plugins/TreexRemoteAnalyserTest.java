package czsem.gate.plugins;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;

import java.net.URL;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.learning.PRSetup;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.utils.GateUtils;

public class TreexRemoteAnalyserTest {

	@Test(groups = { "treexRemote" })
	public void englishSimpleTest() throws Exception {
	    String sever_addr = "http://192.168.167.13:9090";
	    
	    System.err.println("Testing remote server at: " + sever_addr);

	    GateUtils.initGateInSandBox();
    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexRemoteAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexRemoteAnalyser.class);
	    }
	    
		PRSetup[] prs= {
	    		new SinglePRSetup(TreexRemoteAnalyser.class)
	    			.putFeature("treexServerUrl", new URL(sever_addr))
	    			.putFeature("resetServerScenario", true)
	    			.putFeature("terminateServerOnCleanup", true)
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
		
		int annsNum = doc.getAnnotations().size();
		
		GateUtils.deleteAllPublicGateResources();

		Assert.assertEquals(annsNum, 13);
	}
}
