package czsem.gate;

import gate.FeatureMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import czsem.Utils;
import czsem.gate.tectomt.Annotator;
import czsem.gate.tectomt.Dependency;
import czsem.gate.tectomt.SentenceInfoManager;
import czsem.gate.tectomt.SentenceInfoManager.Layer;
import czsem.gate.tectomt.TMTSAXParser;
import czsem.gate.tectomt.Token;

import static org.testng.AssertJUnit.*;


public class SAXTMTParserTest
{
	
	@Test
	public void testParseEglishMorpho() throws SAXException, IOException, URISyntaxException, ParserConfigurationException
	{
		TMTSAXParser parser = new TMTSAXParser("english");
		List<SentenceInfoManager> sentences = parser.parse(Utils.URLToFilePath(getClass().getResource("/english_morphology.tmt")));
		assertEquals(sentences.size(), 1);
		
		SentenceInfoManager s1 = sentences.get(0);
		assertEquals(s1.getString(), "The BBC's Bethany Bell in Jerusalem says many people face shortages of food, medicine and fuel.");
		Token[] mTokens = s1.getTokens(Layer.MORPHO);
		assertEquals(mTokens.length, 19);
		assertEquals(mTokens[7].getString(), "says");
		assertEquals(mTokens[7].getAnnotationType(), "Token");
		FeatureMap fm = mTokens[7].getFeatures();
		assertEquals(fm.get("lemma"), "say");
		assertEquals(fm.get("tag"), "VBZ");

		List<Dependency> mDependencies = s1.getDependencies(Layer.MORPHO);
		assertEquals(mDependencies.size(), 0);

	}

	@Test
	public void testParseEglishFull() throws ParserConfigurationException, SAXException, IOException, URISyntaxException
	{
		TMTSAXParser parser = new TMTSAXParser("english");
		List<SentenceInfoManager> sentences = parser.parse(Utils.URLToFilePath(getClass().getResource("/english_full.tmt")));
		assertEquals(sentences.size(), 10);
		
		SentenceInfoManager s1 = sentences.get(0);
		assertEquals(s1.getString(), "The BBC's Bethany Bell in Jerusalem says many people face shortages of food, medicine and fuel.");
		Token[] mTokens = s1.getTokens(Layer.MORPHO);
		assertEquals(mTokens.length, 19);
		assertEquals(mTokens[7].getString(), "says");
		assertEquals(mTokens[7].getAnnotationType(), "Token");
		FeatureMap mt_fm = mTokens[7].getFeatures();
		assertEquals(mt_fm.get("lemma"), "say");
		assertEquals(mt_fm.get("tag"), "VBZ");
		
		List<Dependency> mDependencies = s1.getDependencies(Layer.MORPHO);
		assertEquals(mDependencies.size(), 0);


		Token[] aTokens = s1.getTokens(Layer.ANALAYTICAL);
		Annotator.sortTokensAccordingAOrd(aTokens);
		assertEquals(aTokens.length, 19);
		FeatureMap at_fm = aTokens[7].getFeatures();
		assertEquals(at_fm.get("lemma"), "say");
		assertEquals(at_fm.get("tag"), "VBZ");
		assertEquals(at_fm.get("afun"), "Pred");
		assertEquals(at_fm.get("ord"), "8");
		
		List<Dependency> aDependencies = s1.getDependencies(Layer.ANALAYTICAL);
		assertEquals(aDependencies.size(), 17);
		assertEquals(aDependencies.get(0).getAnnotationType(), "aDependency");
		
		AssertPrinter apr = new AssertPrinter();
		aDependencies.get(0).print(apr.getPrStr());
		apr.assertPrinted("Depends: SEnglishA-s1w5 on SEnglishA-s1w8");
		
		Token[] tTokens = s1.getTokens(Layer.TECTO);
		assertEquals(tTokens.length, 13);
		assertEquals(tTokens[0].getAnnotationType(), "tToken");
		FeatureMap tfm = tTokens[0].getFeatures();
		assertEquals(tfm.size(), 14);
		assertEquals(tfm.get("t_lemma"), "say");
		assertEquals(tfm.get("formeme"), "v:fin");
		assertEquals(tfm.get("functor"), "PRED");
		assertEquals(tfm.get("lex.rf"), "SEnglishA-s1w8");
		assertEquals(s1.findToken(tTokens[0].getTLexRf()), aTokens[7]); 
		
		List<Dependency> tDependencies = s1.getDependencies(Layer.TECTO);
		assertEquals(tDependencies.size(), 12);
		tDependencies.get(0).print(apr.getPrStr());
		apr.assertPrinted("Depends: SEnglishT-s1w5 on SEnglishT-s1w8");

		Token[] nTokens = s1.getTokens(Layer.NAMES);
		assertEquals(nTokens.length, 3);
		FeatureMap nfm = nTokens[1].getFeatures();
		assertEquals(nfm.size(), 2);
		assertEquals(nfm.get("normalized_name"), "Bethany Bell");
		assertEquals(nfm.get("ne_type"), "i_");
		nTokens[1].print(apr.getPrStr());
		apr.assertPrinted("i_\nBethany Bell\nSEnglishM-s1w4\nSEnglishM-s1w5\n");
		nTokens[0].print(apr.getPrStr());
		apr.assertPrinted("i_\nBBC\nSEnglishM-s1w2\n");
		
		assertEquals(s1.getAuxRfDependencies().size(), 7);		
	}
		
	public static class AssertPrinter
	{
		ByteArrayOutputStream buf;
		PrintStream pr;

		public AssertPrinter() {
			buf = new ByteArrayOutputStream();
			pr = new PrintStream(buf);
		}
		
		public PrintStream getPrStr()
		{
			buf.reset();
			return pr;
		}
		
		public void assertPrinted(String what)
		{
			assertEquals(buf.toString(), 
					what.replaceAll("\n", System.getProperty("line.separator")));			
		}
		
	}
}
