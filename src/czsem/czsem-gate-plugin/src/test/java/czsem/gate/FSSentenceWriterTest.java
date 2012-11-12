package czsem.gate;

import gate.Document;
import gate.Factory;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.Assert;
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
		
		Assert.assertEquals(out.toString().replace("\r\n", "\n"), 
				"[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],orth=lowercase,category=VBN,length=10,sentence_order=4,ann_id=9]([string=annotations,kind=word,dependencies=\\[amod(1)\\],orth=lowercase,category=NNS,length=11,sentence_order=1,ann_id=3]([string=Dependency,kind=word,orth=upperInitial,category=JJ,length=10,sentence_order=0,ann_id=1]),[string=can,kind=word,orth=lowercase,category=MD,length=3,sentence_order=2,ann_id=5],[string=be,kind=word,orth=lowercase,category=VB,length=2,sentence_order=3,ann_id=7],[string=by,kind=word,dependencies=\\[pobj(15)\\],orth=lowercase,category=IN,length=2,sentence_order=5,ann_id=11]([string=tool,kind=word,dependencies=\\[det(13)\\],orth=lowercase,category=NN,length=4,sentence_order=7,ann_id=15]([string=this,kind=word,orth=lowercase,category=DT,length=4,sentence_order=6,ann_id=13])))\n");
		
		System.err.println(out.toString());
		System.err.println(wr.getAttributes());
	}
}
