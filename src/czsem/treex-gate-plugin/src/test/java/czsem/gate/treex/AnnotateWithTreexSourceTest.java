package czsem.gate.treex;

import gate.Document;
import gate.Factory;

import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.GateUtils;

public class AnnotateWithTreexSourceTest {
	

	void annotateUsingTeexReturnTest(Object treex_ret, URL gateXmlUrl) throws Exception
	{
		TreexReturnAnalysis tra = new TreexReturnAnalysis(treex_ret);
		
		GateUtils.initGateInSandBox();
		Document doc = Factory.newDocument(gateXmlUrl, "utf8");

		
		String text = doc.getContent().toString();

		Document retDoc = Factory.newDocument(text);

		tra.annotate(retDoc, "treex");

		assertDocumentsAreSame(retDoc, doc);
		
	}
	
	public static void assertDocumentsAreSame(Document actual, Document expected) {
		//TODO Should be less strict...
		Assert.assertEquals(
				actual.getAnnotations("treex"),
				expected.getAnnotations("treex"));
		
		
				
	}

	void annotateUsingSerializedDataTest(String serializedModelResourceName, String gateXmlResourceName) throws Exception
	{
		Object treex_ret = Utils.deserializeFromStram(getClass().getResourceAsStream(serializedModelResourceName));

		URL gateXmlUrl = getClass().getResource(gateXmlResourceName);
		annotateUsingTeexReturnTest(treex_ret, gateXmlUrl);
	}

	@Test
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
