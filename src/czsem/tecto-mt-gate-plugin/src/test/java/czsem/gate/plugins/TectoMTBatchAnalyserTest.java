package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.Resource;
import gate.TextualDocument;
import gate.corpora.DocumentXmlUtils;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;


import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import czsem.Utils;
import czsem.gate.GateUtils;
import czsem.gate.plugins.TectoMTAbstractAnalyser.CannotAnnotateDocumentException;
import czsem.gate.utils.Config;

import static org.testng.AssertJUnit.*;

public class TectoMTBatchAnalyserTest //extends TestCase
{
	Corpus corpus = null;
	Document document = null;	
	
	public static String [] english_sentences =
	{
		"The BBC's Bethany Bell in Jerusalem says many people face shortages of food, medicine and fuel.",
		"Chancellor Alistair Darling says the new longer-term agreement will guarantee earnings growth for 5.5 million workers and will allow departments to plan more effectively."
	};

	public static String [] czech_sentences =
	{
		"Požár byl operačnímu středisku HZS ohlášen dnes ve 2.13 hodin, na místo vyjeli profesionální hasiči ze stanice v Židlochovicích a dobrovolní hasiči z Židlochovic, Žabčic a Přísnotic.",
		"Oheň, který zasáhl elektroinstalaci u chladícího boxu, hasiči dostali pod kontrolu ve 2.32 hodin a uhasili tři minuty po třetí hodině.",
		"Příčinou vzniku požáru byla technická závada, škodu vyšetřovatel předběžně vyčíslil na osm tisíc korun."
	};
		
	
	protected Corpus gateCorpusFromDoc(Document doc) throws ResourceInstantiationException
	{
		Corpus corpus = Factory.newCorpus(null);
		corpus.add(doc);
	    return corpus;		
	}

	protected void initCorpusAndDocFromFile(URL file_url) throws ResourceInstantiationException
	{
		document = Factory.newDocument(file_url, "utf8");
		corpus = gateCorpusFromDoc(document);		
	}

	protected void initCorpusAndDocFromSentencesWithSentenceAnnotation(String [] sentences, int num) throws ResourceInstantiationException, InvalidOffsetException
	{
		initCorpusAndDocFromSentences(sentences, num);
		
		AnnotationSet as = document.getAnnotations();
	    
	    long last_end = 0; 
		for (int i = 0; i < num; i++)
	    {
	    	as.add(last_end, last_end+sentences[i].length(), "Sentence", Factory.newFeatureMap());
	    	last_end += sentences[i].length()+1;
		}

	}

	protected void initCorpusAndDocFromSentences(String [] sentences, int num) throws ResourceInstantiationException
	{
		StringBuilder sb= new StringBuilder();
	    for (int i = 0; i < num; i++)
	    {
	    	sb.append(sentences[i]);
			sb.append(' ');
		}
	    
		document = Factory.newDocument(sb.toString());
		corpus = gateCorpusFromDoc(document);
	}



	public TectoMTBatchAnalyserTest() throws Exception
	{
/*		
		String dir = "czsem_GATE_plugins/tmt_analysis_scenarios/";
		writeTMTscenario(czech_full_blocks, dir+"czech_full_blocks.scen");
		writeTMTscenario(english_full_blocks, dir+"english_full_blocks.scen");
/**/		
		
	    if (! Gate.isInitialised())
	    {
			Config.getConfig().setGateHome();
			Gate.init();
	    }
	    
	    if (! GateUtils.isPrCalssRegisteredInCreole(TectoMTBatchAnalyser.class))
	    {
			Gate.getCreoleRegister().registerComponent(TectoMTBatchAnalyser.class);	    	
	    }
	}
	
	@Test
	public void testIsTmtCreoleRegistered()
	{
		assertTrue("tmt analyzer is not in creole register", 
				GateUtils.isPrCalssRegisteredInCreole(TectoMTBatchAnalyser.class));
	}

	
	protected void executeTmtOnCurrentCorpus(FeatureMap fm) throws ResourceInstantiationException, ExecutionException
	{
		Resource tmt_analyzer = Factory.createResource(TectoMTBatchAnalyser.class.getCanonicalName(), fm);

		
		SerialAnalyserController controller = (SerialAnalyserController)	    	   
			Factory.createResource(SerialAnalyserController.class.getCanonicalName());
	
		controller.setCorpus(corpus);
		controller.add((ProcessingResource) tmt_analyzer);
		controller.execute();				
	}

	protected void executeTmtOnCurrentCorpus(String language, String [] blocks) throws ResourceInstantiationException, ExecutionException
	{
		FeatureMap fm = Factory.newFeatureMap();
		fm.put("loadScenarioFromFile", "false");
		fm.put("language", language);
		fm.put("blocks", Arrays.asList(blocks));
		
		executeTmtOnCurrentCorpus(fm);
	}

	protected void executeTmtOnCurrentCorpus(String language, String blocks_filename) throws ResourceInstantiationException, ExecutionException, MalformedURLException
	{
		FeatureMap fm = Factory.newFeatureMap();
		fm.put("loadScenarioFromFile", "true");
		fm.put("language", language);
		fm.put("scenarioFilePath", new File(blocks_filename).toURI().toURL());
		
		executeTmtOnCurrentCorpus(fm);
	}

	@Test
	public void testAnnotateGateDocumentAcordingtoTMTfileBmc07504729() throws ResourceInstantiationException, InvalidOffsetException, ParserConfigurationException, SAXException, IOException, URISyntaxException, CannotAnnotateDocumentException
	{
		initCorpusAndDocFromFile(getClass().getResource("/bmc07504729.xml"));

		assertEquals(48586, (long) document.getContent().size());
		
		document.getAnnotations().clear();
		AnnotationSet as = document.getAnnotations();
		assertEquals(as.size(), 0);
		AnnotationSet as_orig = document.getAnnotations("Original markups");
		assertEquals(24, as_orig.size());
		
		
		
		TectoMTBatchAnalyser tmt_ba = new TectoMTBatchAnalyser();
		tmt_ba.setLanguage("czech");
		tmt_ba.annotateGateDocumentAcordingtoTMTfile(
				document,
				Utils.URLToFilePath(getClass().getResource("/bmc07504729.tmt")));
		
//		saveDocumentToFile("test_out.xml");
		assertEquals(10994, as.size());		
	}

	@Test
	public void testAnnotateGateDocumentAcordingtoTMTfileAcquisitions10473() throws ResourceInstantiationException, InvalidOffsetException, ParserConfigurationException, SAXException, IOException, URISyntaxException, CannotAnnotateDocumentException
	{
		initCorpusAndDocFromFile(getClass().getResource("/Acquisitions10473.xml"));

		assertEquals(2644, (long) document.getContent().size());
		
		document.getAnnotations().clear();
		AnnotationSet as = document.getAnnotations();
		assertEquals(as.size(), 0);
		AnnotationSet as_orig = document.getAnnotations("Original markups");
		assertEquals(37, as_orig.size());
		
		
		
		TectoMTBatchAnalyser tmt_ba = new TectoMTBatchAnalyser();
		tmt_ba.setLanguage("english");
		tmt_ba.annotateGateDocumentAcordingtoTMTfile(
				document,
				Utils.URLToFilePath(getClass().getResource("/Acquisitions10473.tmt")));
		
		//saveDocumentToFile("test_out.xml");
		assertEquals(1703, as.size());
		
	}

	@Test(groups = { "slow" })
	public void testAnnotateGateDocumentAcordingtoTMTfileFull() throws InvalidOffsetException, ParserConfigurationException, SAXException, IOException, URISyntaxException, ResourceInstantiationException, CannotAnnotateDocumentException
	{
		initCorpusAndDocFromFile(getClass().getResource("/english_full.txt"));
		
		document.getAnnotations().clear();
		AnnotationSet as = document.getAnnotations();
		assertEquals(as.size(), 0);
		assertEquals(1302, (long) document.getContent().size());

		TectoMTBatchAnalyser tmt_ba = new TectoMTBatchAnalyser();
		tmt_ba.setLanguage("english");
		tmt_ba.annotateGateDocumentAcordingtoTMTfile(
				document,
				Utils.URLToFilePath(getClass().getResource("/english_full.tmt")));
		assertEquals(as.size(), 913);
		
				
		validateAsType(as, "Sentence", 10, 0);
		validateAsType(as, "Token", 251, 5);
		validateAsType(as, "NamedEntity", 16, 2);
		validateAsType(as, "aDependency", 231, 1);
		validateAsType(as, "tDependency", 159, 1);
		validateAsType(as, "auxRfDependency", 77, 1);
		validateAsType(as, "tToken", 169, (Integer) null);
		
		FeatureMap tfm = as.get("tToken").get((long) 0).iterator().next().getFeatures();
		assertEquals(8, tfm.size());
		assertEquals("BBC", tfm.get("t_lemma"));
		assertEquals("n:poss", tfm.get("formeme"));
		assertEquals("APP", tfm.get("functor"));
		assertEquals(3, tfm.get("lex.rf"));
		
		
		FeatureMap fm1 = Factory.newFeatureMap();
		fm1.put("t_lemma", "face");
		fm1.put("tense", "sim");
		AnnotationSet aParent = as.get("tToken", fm1);
		assertEquals(1, aParent.size());

		FeatureMap fm2 = Factory.newFeatureMap();
		fm2.put("t_lemma", "shortage");
		AnnotationSet aChild = as.get("tToken", fm2);
		assertEquals(1, aChild.size());

		Integer parentId = aParent.iterator().next().getId();
		Integer childId = aChild.iterator().next().getId();
		
		AnnotationSet tds = as.get("tDependency");
		Iterator<Annotation> tds_iter = tds.iterator();
		int arg_cnt = 0;
		boolean found = false;
		while (tds_iter.hasNext())
		{
			Annotation atd = tds_iter.next();
			@SuppressWarnings("unchecked")
			List<Integer> atdArgs = (List<Integer>) atd.getFeatures().get("args");
			if (atdArgs.get(0).equals(parentId))
			{
				arg_cnt++;
				if (atdArgs.get(1).equals(childId)) found = true;
				
			}
		}
		
		assertEquals(2, arg_cnt);
		assertTrue(found);
		
		Integer lexrf = (Integer) as.get(parentId).getFeatures().get("lex.rf");
		assertEquals("Token", as.get(lexrf).getType());
		
		
		
		AnnotationSet auxrf = as.get("auxRfDependency");
		@SuppressWarnings("unchecked")
		List<Integer> lexrfArgs = (List<Integer>) auxrf.iterator().next().getFeatures().get("args");
		Integer lexParentID = lexrfArgs.get(0);
		Integer lexChildID = lexrfArgs.get(1);
		
		assertEquals("tToken", as.get(lexParentID).getType());
		assertEquals("Token", as.get(lexChildID).getType());

	}
	
	@Test(groups = { "slow" })
	public void testAnnotateGateDocumentAcordingtoTMTfileMorpho() throws URISyntaxException, InvalidOffsetException, ParserConfigurationException, SAXException, IOException, ResourceInstantiationException, CannotAnnotateDocumentException
	{
		initCorpusAndDocFromSentences(english_sentences, 1);
		document.getAnnotations().clear();
		AnnotationSet as = document.getAnnotations();
		assertEquals(as.size(), 0);
		
		TectoMTBatchAnalyser tmt_ba = new TectoMTBatchAnalyser();
		tmt_ba.setLanguage("english");
		tmt_ba.annotateGateDocumentAcordingtoTMTfile(
				document,
				Utils.URLToFilePath(getClass().getResource("/english_morphology.tmt")));
		
		
		assertEquals(20, as.size());
		validateAsType(as, "Token", 19, 3);		
	}
	
	protected void validateAsType(AnnotationSet as, String an_type, int num_annots, Integer num_features)
	{
		AnnotationSet at = as.get(an_type);
		assertEquals(num_annots, at.size());
		for (Annotation a : at) {
			FeatureMap fm = a.getFeatures();
			if (num_features != null) assertEquals(num_features, (Integer) fm.size());
			for (Object key : fm.keySet()) {
				Object value = fm.get(key);
				assertNotNull(value);
				assertFalse(value.equals("null"));
				assertFalse(value.equals("<null>"));
			}
		}

	}

	@Test(groups = { "slow" })
	public void testExecuteCzechMorphology() throws ResourceInstantiationException, ExecutionException
	{
		String [] blocks = {
				"SCzechW_to_SCzechM::TextSeg_tokenizer_and_segmenter",
				"SCzechW_to_SCzechM::Tokenize_joining_numbers",
				"SCzechW_to_SCzechM::TagMorce"
				};

		initCorpusAndDocFromSentences(czech_sentences, 1);
		executeTmtOnCurrentCorpus("czech", blocks);

		AnnotationSet as = document.getAnnotations();
		
		assertEquals(31, as.size());
		validateAsType(as, "Token", 30, 3);
		
		FeatureMap fm = as.get((long) czech_sentences[0].indexOf("ohlášen")).iterator().next().getFeatures();
		assertEquals("ohlášen",  fm.get("form"));
		assertEquals("ohlásit",  fm.get("lemma"));
		assertEquals("VsYS---XX-AP---", fm.get("tag"));
		assertEquals(fm.get("afun"), null);
		assertEquals(fm.get("ord"), null);		
	}
	
	public static final String [] czech_full_blocks = {
			"SCzechW_to_SCzechM::TextSeg_tokenizer_and_segmenter",
			"SCzechW_to_SCzechM::Tokenize_joining_numbers",
			"SCzechW_to_SCzechM::TagMorce",
//			"SCzechM_to_SCzechN::SVM_ne_recognizer",
//			"SCzechM_to_SCzechN::Embed_instances",
//			"SCzechM_to_SCzechN::Geo_ne_recognizer",
//			"SCzechM_to_SCzechN::Embed_instances",
			"SCzechM_to_SCzechA::McD_parser_local TMT_PARAM_MCD_CZ_MODEL=pdt20_train_autTag_golden_latin2_pruned_0.02.model",
			"SCzechM_to_SCzechA::Fix_atree_after_McD",
			"SCzechM_to_SCzechA::Fix_is_member",

			"SCzechA_to_SCzechT::Mark_edges_to_collapse",
			"SxxA_to_SxxT::Build_ttree                    LANGUAGE=Czech",
			"SCzechA_to_SCzechT::Rehang_unary_coord_conj",
			"SxxA_to_SxxT::Fill_is_member                 LANGUAGE=Czech",
			
//			"SCzechA_to_SCzechT::Mark_auxiliary_nodes",
//			"SCzechA_to_SCzechT::Build_ttree",
//			"SCzechA_to_SCzechT::Fill_is_member",
//			"SCzechA_to_SCzechT::Rehang_unary_coord_conj",
			"SCzechA_to_SCzechT::Assign_coap_functors",
//			"SCzechA_to_SCzechT::Fix_is_member",
			"SCzechA_to_SCzechT::Distrib_coord_aux",
			"SCzechA_to_SCzechT::Mark_clause_heads",
			"SCzechA_to_SCzechT::Mark_relclause_heads",
			"SCzechA_to_SCzechT::Mark_relclause_coref",
			"SCzechA_to_SCzechT::Fix_tlemmas",
			"SCzechA_to_SCzechT::Recompute_deepord",
			"SCzechA_to_SCzechT::Assign_nodetype",
			"SCzechA_to_SCzechT::Assign_grammatemes",
			"SCzechA_to_SCzechT::Detect_formeme",
			"SCzechA_to_SCzechT::Add_PersPron",
			"SCzechA_to_SCzechT::Mark_reflpron_coref",
			"SCzechA_to_SCzechT::TBLa2t_phaseFd",
			"XAnylang1X_to_XAnylang2X::Normalize_ordering LAYER=SCzechT"
/**/		};

	protected void saveDocumentToFile(String filename) throws FileNotFoundException
	{
		new PrintStream(filename)
		.print(
				DocumentXmlUtils.toXml(
						(TextualDocument) document));		
	}
	
	public void _testExecuteCzechFullAllIncidents() throws PersistenceException, ResourceInstantiationException, ExecutionException
	{
	    DataStore ds = GateUtils.openDataStore("file:/C:/Users/dedek/AppData/GATE/ISWC");			
		corpus = GateUtils.loadCorpusFormDatastore(ds, "ISWC___1274943456887___5663");
		executeTmtOnCurrentCorpus("czech", czech_full_blocks);				
	}
	
	@Test(groups = { "slow" })
	public void testExecuteCzechFull() throws ResourceInstantiationException, ExecutionException, MalformedURLException, URISyntaxException, IOException
	{
//		String [] s2  = {"Rychlost roste."};
//		initCorpusAndDocFromSentences(s2, 1);

		initCorpusAndDocFromSentences(czech_sentences, 1);
//		executeTmtOnCurrentCorpus("czech", czech_full_blocks);
		executeTmtOnCurrentCorpus("czech", Config.getConfig().getCzsemPluginDir() + "/tmt_analysis_scenarios/czech_full_blocks.scen");


		AnnotationSet as = document.getAnnotations();
		
		//saveDocumentToFile("test_out.xml");
		
		
		assertEquals(110, as.size());
		validateAsType(as, "Token", 30, 5);
		validateAsType(as, "aDependency", 28, 1);
		validateAsType(as, "tToken", 22, null);
		validateAsType(as, "tDependency", 21, 1);
		validateAsType(as, "auxRfDependency", 8, 1);
		
		FeatureMap fm = as.get("Token").get((long) czech_sentences[0].indexOf("ohlášen")).iterator().next().getFeatures();
		assertEquals("ohlášen",  fm.get("form"));
		assertEquals("ohlásit",  fm.get("lemma"));
		assertEquals("VsYS---XX-AP---", fm.get("tag"));
		assertEquals("Pred", fm.get("afun"));
		assertEquals("6", fm.get("ord"));

		
	}

	@Test
	public void testExecuteEnglishSentenceSegmentation() throws GateException, MalformedURLException
	{		
		
		String [] blocks = {"SEnglishW_to_SEnglishM::Sentence_segmentation"};

		
		
		initCorpusAndDocFromSentences(english_sentences, 2);
		executeTmtOnCurrentCorpus("english", blocks);		
		
		AnnotationSet sents = document.getAnnotations().get("Sentence");
		assertEquals(sents.size(), 2);
		Iterator<Annotation> siter = sents.iterator();
		Annotation s1 = siter.next();
		assertEquals((long) s1.getStartNode().getOffset(), 0);
		assertEquals((long) s1.getEndNode().getOffset(), 95);
		Annotation s2 = siter.next();
		assertEquals((long) s2.getStartNode().getOffset(), 96);
		assertEquals((long) s2.getEndNode().getOffset(), 266);
	}


	@Test
	public void testParseSentecesFormGateToTectoMTEnglish() throws ResourceInstantiationException, InvalidOffsetException, ExecutionException
	{
		String [] blocks = {"SEnglishW_to_SEnglishM::Tokenization"};
		initCorpusAndDocFromSentencesWithSentenceAnnotation(english_sentences, english_sentences.length);
//		initCorpusAndDocFromSentences(english_sentences, english_sentences.length);
		executeTmtOnCurrentCorpus("english", blocks);
		
		AnnotationSet sents = document.getAnnotations().get("Sentence");
		assertEquals(english_sentences.length*2, sents.size());		

		AnnotationSet tokens = document.getAnnotations().get("Token");
		assertEquals(44, tokens.size());		
	}

	
	@Test(groups = { "slow" })
	public void testExecuteEnglishMorphology() throws ResourceInstantiationException, ExecutionException
	{
		String [] blocks = {
				"SEnglishW_to_SEnglishM::Sentence_segmentation",
				"SEnglishW_to_SEnglishM::Penn_style_tokenization",
				
				"SEnglishW_to_SEnglishM::TagMorce",
				"SEnglishW_to_SEnglishM::Fix_mtags",
				"SEnglishW_to_SEnglishM::Lemmatize_mtree"
				};
		
		
		initCorpusAndDocFromSentences(english_sentences, 1);
		executeTmtOnCurrentCorpus("english", blocks);
		
		AnnotationSet as = document.getAnnotations();
		
		assertEquals(20, as.size());
		
		validateAsType(as, "Token", 19, 3);
		FeatureMap fm = as.get((long) english_sentences[0].indexOf("says")).iterator().next().getFeatures();
		assertEquals(fm.get("form"), "says");
		assertEquals(fm.get("lemma"), "say");
		assertEquals(fm.get("tag"), "VBZ");
		assertEquals(fm.get("afun"), null);
		assertEquals(fm.get("ord"), null);

	}

	public static final String [] english_full_blocks = {
			"SEnglishW_to_SEnglishM::Sentence_segmentation",
			"SEnglishW_to_SEnglishM::Tokenization",
			"SEnglishW_to_SEnglishM::Normalize_forms",
			"SEnglishW_to_SEnglishM::Fix_tokenization",
			"SEnglishM_to_SEnglishN::Stanford_named_entities TMT_PARAM_NER_EN_MODEL=ner-eng-ie.crf-3-all2008.ser.gz",
			"SEnglishW_to_SEnglishM::TagMorce",
			"SEnglishW_to_SEnglishM::Fix_mtags",
			"SEnglishW_to_SEnglishM::Lemmatize_mtree",
			"SxxM_to_SxxA::Clone_atree LANGUAGE=English",
//		    'conll_mcd_order2.model'      => '2600m',    # tested on sol1, sol2 (64bit)
//		    'conll_mcd_order2_0.01.model' => '540m',     # tested on sol2 (64bit) , cygwin (32bit win), java-1.6.0(64bit)
//		    'conll_mcd_order2_0.03.model' => '540m',     # load block tested on cygwin notebook (32bit win), java-1.6.0(64bit)
//		    'conll_mcd_order2_0.1.model'  => '540m',     # load block tested on cygwin notebook (32bit win), java-1.6.0(64bit)
			"SEnglishM_to_SEnglishA::McD_parser TMT_PARAM_MCD_EN_MODEL=conll_mcd_order2_0.01.model",
			"SEnglishM_to_SEnglishA::Fill_is_member_from_deprel",
			"SEnglishM_to_SEnglishA::Fix_tags_after_parse",
			"SEnglishM_to_SEnglishA::McD_parser TMT_PARAM_MCD_EN_MODEL=conll_mcd_order2_0.01.model REPARSE=1",
			"SEnglishM_to_SEnglishA::Fill_is_member_from_deprel",
			"SEnglishM_to_SEnglishA::Fix_McD_topology",
			"SEnglishM_to_SEnglishA::Fix_is_member",
			"SEnglishM_to_SEnglishA::Fix_atree",
			"SEnglishM_to_SEnglishA::Fix_multiword_prep_and_conj",
			"SEnglishM_to_SEnglishA::Fix_dicendi_verbs",
			"SEnglishM_to_SEnglishA::Fill_afun_AuxCP_Coord",
			"SEnglishM_to_SEnglishA::Fill_afun",
			"SEnglishA_to_SEnglishT::Mark_edges_to_collapse",
			"SEnglishA_to_SEnglishT::Mark_edges_to_collapse_neg",
			"SxxA_to_SxxT::Build_ttree                    LANGUAGE=English",
			"SxxA_to_SxxT::Fill_is_member                 LANGUAGE=English",
			"SEnglishA_to_SEnglishT::Move_aux_from_coord_to_members",
			"SEnglishA_to_SEnglishT::Mark_named_entities TMT_PARAM_NER_EN_MODEL=ner-eng-ie.crf-3-all2008.ser.gz",
			"SEnglishA_to_SEnglishT::Fix_tlemmas",
			"SEnglishA_to_SEnglishT::Assign_coap_functors",
			"SEnglishA_to_SEnglishT::Fix_is_member",
			"SEnglishA_to_SEnglishT::Mark_clause_heads",
			"SEnglishA_to_SEnglishT::Mark_passives",
			"SEnglishA_to_SEnglishT::Assign_functors",
			"SEnglishA_to_SEnglishT::Mark_infin",
			"SEnglishA_to_SEnglishT::Mark_relclause_heads",
			"SEnglishA_to_SEnglishT::Mark_relclause_coref",
			"SEnglishA_to_SEnglishT::Mark_dsp_root",
			"SEnglishA_to_SEnglishT::Mark_parentheses",
			"SEnglishA_to_SEnglishT::Recompute_deepord",
			"SEnglishA_to_SEnglishT::Assign_nodetype",
			"SEnglishA_to_SEnglishT::Assign_grammatemes",
			"SEnglishA_to_SEnglishT::Detect_formeme",
			"SEnglishA_to_SEnglishT::Detect_voice",
			"SEnglishA_to_SEnglishT::Fill_is_name_of_person",
			"SEnglishA_to_SEnglishT::TBLa2t_phaseFx"
		};

	@Test(groups = { "slow" })
	public void testExecuteEnglishFull() throws ResourceInstantiationException, ExecutionException, MalformedURLException, URISyntaxException, IOException
	{
		
		initCorpusAndDocFromSentences(english_sentences, 1);
		executeTmtOnCurrentCorpus("english", Config.getConfig().getCzsemPluginDir() + "/tmt_analysis_scenarios/english_full_blocks.scen");
				
		AnnotationSet as = document.getAnnotations();
		
		assertEquals(74, as.size());		
	}
	
	@Test(groups = { "slow" })
	public void testExecuteEnglishFullAcquisitions10473() throws ResourceInstantiationException, ExecutionException, FileNotFoundException
	{
		initCorpusAndDocFromFile(getClass().getResource("/Acquisitions10473.xml"));
		executeTmtOnCurrentCorpus("english", english_full_blocks);

		AnnotationSet as = document.getAnnotations();		
		assertEquals(1703, as.size());
		
		saveDocumentToFile("test_out.xml");


		
	}

	
	@Test(groups = { "slow" })
	public void testExecuteEnglishFullFilms() throws ResourceInstantiationException, ExecutionException, FileNotFoundException
	{
		String [] film_s = 
		{
				"James Bond is back and he is alive and well. Any questions about Daniel Craig's worthiness are thrown out almost immediately as we are handed a film filled to the brim with exquisite action and explosive emotion. I squirmed in my seat with delight as I have not done since I was a child. What \"Batman Begins\" did for that franchise, \"Casino Royale\" does, and more, for Bond. For a while it seemed that he might not be able to well exist outside the confines of the cold war, but here we are given an entirely modern Bond with enough nods to the original that we can't be too upset. Maybe it's because this is the last novel yet to be filmed in the traditional Bond manner and it is Ian Fleming who has stolen our hearts not this incarnation of the super spy. However I like to think that someone actually just got their act together and concentrated on the film itself as opposed to who they could get the most product placement money out of. Congratulations. James Bond will live on for at least one more generation, and maybe forever. Great set pieces and one of the best chase sequences not involving cars ever put on screen, blended with beautiful locations and even more lovely women add up to the perfect cocktail with the twisting story line acting as the lemon peel in the martini, holding it all together. Many will come out saying that this is the best Bond film ever and I can not rightly say they are wrong at this point. Only time will tell that tale. However every fan can be assured that this ranks amongst the very upper crust of Bond movies, and Craig is no Lazenby. He lends a harsh wit and a thuggish charm to the character and by the end he's no longer the new guy, he is Bond, James Bond. A masterpiece of popular film-making and the movie we have been waiting for all year. See it early and often as it is sure not to diminish upon reviewing."
		};
		
		initCorpusAndDocFromSentences(film_s, 1);
		executeTmtOnCurrentCorpus("english", english_full_blocks);

		
		saveDocumentToFile("test_film_out.xml");

		AnnotationSet as = document.getAnnotations();		
		assertEquals(1364, as.size());

		
	}


	public static void writeTMTscenario(String [] scenario, String file_path) throws FileNotFoundException
	{
		PrintStream s = new PrintStream(file_path);
		for (int i = 0; i < scenario.length; i++)
		{
			s.println(scenario[i]);			
		}		
	}
/*	
	public static junit.framework.Test suite(){
		return new TestSuite(TectoMTBatchAnalyserTest.class);
	}
*/
}
