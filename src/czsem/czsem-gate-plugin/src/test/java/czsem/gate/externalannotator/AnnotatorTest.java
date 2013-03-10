package czsem.gate.externalannotator;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import czsem.gate.externalannotator.Annotator.SeqAnnotable;
import czsem.gate.externalannotator.SequenceAnnotator.CannotAnnotateCharacterSequence;
import czsem.gate.utils.GateUtils;

public class AnnotatorTest {
	
	public static class SeqAnnotableTest implements SeqAnnotable {
		protected SeqAnnotableTest(String string) {
			this.string = string;
		}

		private String string;
		@Override
		public String getAnnotationType() {	return "testType";}

		@Override
		public FeatureMap getFeatures() {return Factory.newFeatureMap();}

		@Override
		public void setGateAnnId(Integer gate_annotation_id) {}

		@Override
		public String getString() {	return string;
		}		
	};

	@Test(expectedExceptions = CannotAnnotateCharacterSequence.class)
	public static void safeAnnotateIterableSeq() throws Exception {
		Logger.getLogger(Annotator.class).setLevel(Level.ERROR);
		
		String docStr = "aaa bbb";
		
		GateUtils.initGateInSandBox();
		
		
		Annotator a = new Annotator();
		
		SeqAnnotable[] saa = new SeqAnnotableTest [] {
				new SeqAnnotableTest("aa"),
				new SeqAnnotableTest("1"),
				new SeqAnnotableTest("bb"),
		};
		List<SeqAnnotable> sa = Arrays.asList(saa);
		
		Document doc = Factory.newDocument(docStr);
		a.setAS(doc.getAnnotations());
		a.setSeqAnot(new SequenceAnnotator(doc));
//		a.safeAnnotateIterableSeq(sa);
		a.annotateIterableSeq(sa);
		
	}

	@Test
	public static void safeAnnotateIterableSeqDot() throws Exception {
		Logger.getLogger(Annotator.class).setLevel(Level.ERROR);
		
		String docStr = ". konec";
		
		GateUtils.initGateInSandBox();
		
		
		Annotator a = new Annotator();
		
		SeqAnnotable[] saa = new SeqAnnotableTest [] {
				new SeqAnnotableTest("<"),
				new SeqAnnotableTest("<"),
				new SeqAnnotableTest("<"),
				new SeqAnnotableTest("DOT"),
				new SeqAnnotableTest(">"),
				new SeqAnnotableTest(">"),
				new SeqAnnotableTest("konec"),
		};
		List<SeqAnnotable> sa = Arrays.asList(saa);
		
		Document doc = Factory.newDocument(docStr);
		a.setAS(doc.getAnnotations());
		a.setSeqAnot(new SequenceAnnotator(doc));
//		a.safeAnnotateIterableSeq(sa);
		a.annotateIterableSeq(sa);
	}
}
