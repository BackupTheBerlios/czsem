package czsem.gate;

import gate.Document;
import gate.Factory;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.annotations.Test;

import czsem.gate.utils.GateUtils;

public class FSSentenceWriterTest {

	@Test
	public static void testPrintTree() throws Exception {
		GateUtils.initGateInSandBox();
		
		Document doc = Factory.newDocument(
			FSSentenceWriterTest.class.getResource("/stanford.gate.xml"));
		
		StringWriter out = new StringWriter();
		FSSentenceWriter wr = new FSSentenceWriter(doc.getAnnotations(), new PrintWriter(out));
		
		wr.printTree();
		
		System.err.println(out.toString());
		System.err.println(wr.getAttributes());
	}
}
