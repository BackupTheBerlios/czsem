package czsem.gate.treex;

import gate.Document;
import gate.Factory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;
import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.Utils;
import czsem.gate.GateUtils;
import czsem.gate.externalannotator.Annotator;

public class AnnotateWithTreexSourceTest {
	
	static Document annotate(
			List<Map<String,Object>> zones,
			Map<String,Map<String, Object>> nodeMap,
			Set<String> exclude_attrs,
			String text,
			Set<String> listAttrs,
			Set<String> idAttrs) throws Exception {
		
		
		Document doc = Factory.newDocument(text);
		
		Annotator annotator = new Annotator();
//		listAttrs.remove("a.rf");
		idAttrs.remove("id");
		idAttrs.remove("parent_id");
//		idAttrs.remove("a/lex.rf");
		annotator.annotate(new TeexAnnotationSource(zones, nodeMap, exclude_attrs, listAttrs, idAttrs), doc, "treex");
		
		return doc;
	}

	void annotateUsingTeexReturnTest(Object treex_ret, URL gateXmlUrl) throws Exception
	{
		TreexReturnAnalysis tra = new TreexReturnAnalysis(treex_ret);
		
		GateUtils.initGateInSandBox();
		Document doc = Factory.newDocument(gateXmlUrl, "utf8");

		
		String text = doc.getContent().toString();
		Document retDoc = annotate(
				tra.getZones(), 
				tra.getNodeMap(), 
				tra.getExcludeAttributes(), 
				text, 
				tra.getListAttributes(), 
				tra.getIdAttributes());
		
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
