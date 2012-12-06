package czsem.gate.plugins;


import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.learning.PRSetup;
import czsem.gate.utils.GateUtils;

public class NetgraphQueryEvalTest {
	
	private PRSetup[] nlpAnalysisResourcesSetup = {
			new PRSetup.SinglePRSetup("gate.creole.splitter.SentenceSplitter"),
			new PRSetup.SinglePRSetup("gate.creole.tokeniser.DefaultTokeniser"),
			new PRSetup.SinglePRSetup("gate.stanford.Parser"),
			new PRSetup.SinglePRSetup(NetgraphQueryEval.class)
				.putFeature("outputASName", "ng")
	};

	
	@Test
	public void testExecute() throws Exception {
		GateUtils.initGate();
		
		if (! GateUtils.isPrCalssRegisteredInCreole("gate.stanford.Parser")) {
			GateUtils.registerPluginDirectory("Parser_Stanford");			
		}		

		if (! GateUtils.isPrCalssRegisteredInCreole("gate.creole.tokeniser.DefaultTokeniser"))	{
			GateUtils.registerPluginDirectory("ANNIE");			
		}		
		
		Gate.getCreoleRegister().registerComponent(NetgraphQueryEval.class);
		
		SerialAnalyserController pipeline = PRSetup.buildGatePipeline(
				Arrays.asList(nlpAnalysisResourcesSetup), "netgraphQueryEvalTest");

		
		Document d = Factory.newDocument("9 of 10 people recommend Czsem!");
		Corpus c = Factory.newCorpus("netgraphQueryEvalTest");
		c.add(d);
		
		pipeline.setCorpus(c);		
		pipeline.execute();
		
		AnnotationSet ngSet = d.getAnnotations("ng");

		Assert.assertEquals(ngSet.getAllTypes(), Utils.setFromArray(new String[] {"child", "numberParent"}));
	}
}
