package cuni.mff.intlib.brat;

import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gate.util.GateException;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.gate.learning.PRSetup;
import czsem.gate.utils.GateUtils;

public class BratImporterTest {

	@Test
	public void testBratImporterTest() throws GateException {
		GateUtils.initGateInSandBox();
		
		Gate.getCreoleRegister().registerComponent(BratImporter.class);
		
		Corpus c = Factory.newCorpus("bratTest");
		
		new PRSetup.SinglePRSetup(BratImporter.class)
			.putFeature("outputCorpus", c)
			.putFeature("inputDirectory", getClass().getResource("testDocs"))
			.createPR().execute();
		
		Assert.assertEquals(
				c.get(0).getAnnotations(BratDocumentAnnotator.BRAT_AS_NAME).size(),
				63);
	}
}
