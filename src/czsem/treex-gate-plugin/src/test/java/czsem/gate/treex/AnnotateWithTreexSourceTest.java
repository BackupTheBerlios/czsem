package czsem.gate.treex;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.util.InvalidOffsetException;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.GateUtils;

public class AnnotateWithTreexSourceTest {
	

	void compareSentencesAndZones(Document doc, Object treex_ret) throws InvalidOffsetException
	{
		List<Map<String, Object>> zones = Utils.objectArrayToGenericList(treex_ret);
		AnnotationSet sents = doc.getAnnotations("treex").get("Sentence");
		
		Iterator<Map<String, Object>> i = zones.iterator();
		
		for (Annotation sentence : sents)
		{
			Map<String, Object> zone = i.next();						
			Assert.assertEquals(zone.get("sentence"), GateUtils.getAnnotationContent(sentence, doc));			
		}
	}
	
	void annotateUsingTeexReturnTest(Object treex_ret, URL gateXmlUrl) throws Exception
	{
		TreexReturnAnalysis tra = new TreexReturnAnalysis(treex_ret);
		
		GateUtils.initGateInSandBox();
		Document doc = Factory.newDocument(gateXmlUrl, "utf8");
		
		compareSentencesAndZones(doc, treex_ret);

		
		String text = doc.getContent().toString();

		Document retDoc = Factory.newDocument(text);

		tra.annotate(retDoc, "treex");

		assertDocumentsAreSame(retDoc, doc);
		
	}
	
	public static void assertDocumentsAreSame(Document actual, Document expected) {
		//TODO Should be less strict...
		AnnotationSet asAct = actual.getAnnotations("treex");
		AnnotationSet asExpect = expected.getAnnotations("treex");

		Assert.assertEquals(actual.getContent().toString(), expected.getContent().toString());

		Assert.assertEquals(asAct.get("Sentence"), asExpect.get("Sentence"));

		Assert.assertEquals(asAct.getAllTypes(), asExpect.getAllTypes());

		Assert.assertEquals(asAct.size(), asExpect.size());
		
		
				
	}

	void annotateUsingSerializedDataTest(String serializedModelResourceName, String gateXmlResourceName) throws Exception
	{
		Object treex_ret = Utils.deserializeFromStram(getClass().getResourceAsStream(serializedModelResourceName));

		URL gateXmlUrl = getClass().getResource(gateXmlResourceName);
		annotateUsingTeexReturnTest(treex_ret, gateXmlUrl);
	}

	//@Test
	public void annotateUsingSerializedData() throws Exception {
		annotateUsingSerializedDataTest("demo_en.ser", "demo_en.gate.xml");
		annotateUsingSerializedDataTest("demo_cs.ser", "demo_cs.gate.xml");
	}

	@Test
	public void annotateUsingTeexFile() throws Exception {
		annotateUsingTeexFileTest("demo_en.treex", "demo_en.gate.xml");
		annotateUsingTeexFileTest("demo_cs.treex", "demo_cs.gate.xml");
	}

	private void annotateUsingTeexFileTest(String treexFileName, String gateXmlResourceName) throws Exception {
		TreexServerExecution tse = new TreexServerExecution();
		tse.start();
		
		TreexServerConnection tsConn = tse.getConnection();

		URL treexFileUrl = getClass().getResource(treexFileName);
		String treexFilePath = Utils.URLToFilePath(treexFileUrl);
		Object treex_ret = tsConn.encodeTreexFile(treexFilePath);
		
		tsConn.terminateServer();
		
		URL gateXmlUrl = getClass().getResource(gateXmlResourceName);
		annotateUsingTeexReturnTest(treex_ret, gateXmlUrl);
		
	}
}
