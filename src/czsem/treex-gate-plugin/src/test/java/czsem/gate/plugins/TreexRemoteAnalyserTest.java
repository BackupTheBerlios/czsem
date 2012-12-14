package czsem.gate.plugins;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.GateConstants;
import gate.creole.SerialAnalyserController;

import java.net.URL;
import java.util.Arrays;

import org.apache.log4j.Level;
import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
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

	@Test(groups = { "treexRemote", "slow" })
	public void czechFullTest() throws Exception {
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
	    			.putFeature("languageCode", "cs")
	    			.putFeatureList("scenarioSetup", 
	    					"W2A::CS::Segment",
	    					"devel/analysis/cs/s_w2t.scen")//,
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "czechFullTest");
		Corpus corpus = Factory.newCorpus("czechFullTest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že?");
		corpus.add(doc);
		analysis.setCorpus(corpus);
		analysis.execute();
		
		int annsNum = doc.getAnnotations().size();
		
		GateUtils.deleteAllPublicGateResources();

		Assert.assertEquals(annsNum, 33);
	}

	@Test(groups = { "treexRemote", "slow" })
	public void czechFullLawTest() throws Exception {
	    String sever_addr = "http://192.168.0.161:9090";
	    
	    System.err.println("Testing remote server at: " + sever_addr);

	    GateUtils.initGateInSandBox();

	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexRemoteAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexRemoteAnalyser.class);
	    }
	    
		PRSetup[] prs= {
	    		new SinglePRSetup(TreexRemoteAnalyser.class)
	    			.putFeature("languageCode", "cs")
	    			.putFeature("treexServerUrl", new URL(sever_addr))
	    			.putFeature("resetServerScenario", true)
	    			.putFeature("terminateServerOnCleanup", true)
	    			.putFeatureList("scenarioSetup", "W2A::CS::Segment", "devel/analysis/cs/s_w2t.scen")
	    };
	    
		
		Gate.getUserConfig().put(GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME, false);
		
		URL url = getClass().getResource("/czsem/gate/treex/1-1537-10_1.vxml");
		Document d = Factory.newDocument(url, "utf8");
		Assert.assertEquals(d.getContent().size(), (Long) 8934L);

		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "czechFullLawTest");
		
		Corpus c = Factory.newCorpus("czechFullLawTest");
		c.add(d);
		
		analysis.setCorpus(c);
		analysis.execute();
		
		GateUtils.deleteAllPublicGateResources();

		Assert.assertEquals(d.getAnnotations().size(), 99999);
	}

	@Test(groups = { "treexRemote", "slow" })
	public void englishFullTest() throws Exception {
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
	    					"devel/analysis/en/s_w2t_best.scen")//,
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "englishFullTest");
		Corpus corpus = Factory.newCorpus("englishFullTest");
		Document doc = Factory.newDocument("Hallo world! Life is great, isn't it?");
		corpus.add(doc);
		analysis.setCorpus(corpus);
		analysis.execute();
		
		int annsNum = doc.getAnnotations().size();
		
		GateUtils.deleteAllPublicGateResources();

		Assert.assertEquals(annsNum, 41);
	}
	
	@Test(groups = { "slow" })
	public void czechNETest() throws Exception {
    	GateUtils.initGateInSandBox();
    	Utils.loggerSetup(Level.OFF);
    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TreexRemoteAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TreexRemoteAnalyser.class);
	    }
	    
	    PRSetup[] prs= {
	    		new SinglePRSetup(TreexRemoteAnalyser.class)
//	    			.putFeature("serverPortNumber", 9994)
	    			.putFeature("treexServerUrl", new URL("http://192.168.0.161:9090"))
	    			
	    			.putFeature("languageCode", "cs")
//	    			.putFeature("showTreexLogInConsole", true)
	    			.putFeatureList("scenarioSetup", "W2A::CS::Segment", "devel/analysis/cs/s_w2n_dedek.scen")
	    };
	    
		SerialAnalyserController analysis = PRSetup.buildGatePipeline(Arrays.asList(prs), "czechNETest");
		Corpus corpus = Factory.newCorpus("czechNETest");
		Document doc = Factory.newDocument("Ahoj světe! Život je krásný, že? 5. listopadu 2012 v Hradci Králové, Česká republika (ČR), Česko");
		corpus.add(doc);
		
		analysis.setCorpus(corpus);
		analysis.execute();
		
		GateUtils.saveGateDocumentToXML(doc, "NETest.gate.xml");
		
		analysis.cleanup();
		GateUtils.deleteAllPublicGateResources();
	}

}
