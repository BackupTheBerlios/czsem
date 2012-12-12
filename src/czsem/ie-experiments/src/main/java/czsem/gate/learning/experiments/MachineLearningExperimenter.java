package czsem.gate.learning.experiments;

import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialController;
import gate.learning.LogService;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.profile.Profiler;
import gate.util.reporting.exceptions.BenchmarkReportInputFileFormatException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import czsem.gate.learning.DataSet;
import czsem.gate.learning.DataSet.DataSetImpl.Acquisitions;
import czsem.gate.learning.DataSet.DataSetImpl.CzechFireman;
import czsem.gate.learning.DataSet.DataSetReduce;
import czsem.gate.learning.DataSet.DatasetFactory;
import czsem.gate.learning.MLEngine.ILPEngine;
import czsem.gate.learning.MLEngine.PaumEngine;
import czsem.gate.learning.MLEngineEncapsulate.CreatePersistentMentions;
import czsem.gate.learning.MLEngineEncapsulate.CreateTemporaryMentions;
import czsem.gate.learning.MLEngineEncapsulate.CreateTemporaryMentionsReferredMentionsPostprocessing;
import czsem.gate.learning.MLEngineEncapsulate.CreateTemporaryMentionsRootSubtree;
import czsem.gate.learning.MLEngineEncapsulate.MLEvaluate;
import czsem.gate.learning.WekaResultExporter;
import czsem.gate.learning.WekaResultTests;
import czsem.gate.learning.experiments.MachineLearningExperiment.EngineFactory;
import czsem.gate.learning.experiments.MachineLearningExperiment.TrainTest;
import czsem.gate.plugins.AnnotationDependencyRootMarker;
import czsem.gate.plugins.CrossValidation;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.TimeBenchmarkUtils;

public class MachineLearningExperimenter
{
/*	
	public static MachineLearningExperiment acquisitions() throws URISyntaxException, IOException
	{
		return
			new MachineLearningExperiment(
	    		new DataSetReduce(new Acquisitions("acquired"), 0.1),
//	    		new MLEvaluate(new CreateTemporaryMentions(new ILPEngine()))
	    		new MLEvaluate(new CreateTemporaryMentionsRootSubtree(new ILPEngine())),
	    		new MLEvaluate(
	    				new CreateTemporaryMentions(
	    	    				new MentionsSubtreePostprocessing(
	    	    						new ILPEngine("ILP_config_NE_roots_subtree.xml")))),	    		
	    		new MLEvaluate(
    	    				new CreateTemporaryMentionsReferedMentionsPostprocessing(
    	    						new ILPEngine("ILP_config_NE_roots.xml"))),
	    	    new MLEvaluate(new CreatePersistentMentions(new PaumEngine())));
//	    		new ILPEngine()

		
	}
/**/	
	public static EngineFactory getAcquisitionsIlpEngineFactory() {
		return new EngineFactory() {
			@Override
			public TrainTest createEngine(String annot_type) {
				if (annot_type.equalsIgnoreCase("dlramt"))
				{
					return new MLEvaluate(
							new CreateTemporaryMentionsRootSubtree(    	    				
									new ILPEngine()));			
				} 
				else
				{
					return new MLEvaluate(
		    				new CreateTemporaryMentionsReferredMentionsPostprocessing(
		    						new ILPEngine("ILP_config_NE_roots.xml")));
				}
			}
		};
	}

	public static EngineFactory getCzechFiremanIlpEngineFactory() {
		return new EngineFactory() {			
			@Override
			public TrainTest createEngine(String annot_type) {
				if (
						annot_type.equalsIgnoreCase("damage") ||
						annot_type.equalsIgnoreCase("end_subtree") || 
						annot_type.equalsIgnoreCase("cars"))
				{
					return new MLEvaluate(new CreateTemporaryMentionsRootSubtree(new ILPEngine("ILP_root_subtree.xml")));			
				} 
				else
				{
					return new MLEvaluate(new CreateTemporaryMentions(new ILPEngine()));			
				}
			}
		};
	}
/*
	public static MachineLearningExperiment czechFiremanSimple(DataSet dataset, String annot_type) throws URISyntaxException, IOException
	{
		TrainTest ilp_engine;
		
		
		return 	    
			new MachineLearningExperiment(
	    		dataset,
	    		ilp_engine,
//	    		new MLEvaluate(new CreateTemporaryMentionsRootSubtree(new ILPEngine("ILP_root_subtree.xml"))),
/*
	    		new MLEvaluate(
	    				new CreateTemporaryMentions(
	    	    				new MentionsSubtreePostprocessing(
	    	    						new ILPEngine("ILP_config_NE_roots_subtree.xml")))),	    		
	    		new MLEvaluate(
    	    				new CreateTemporaryMentionsReferedMentionsPostprocessing(
    	    						"Lookup_root.origRootID",
    	    						new ILPEngine("ILP_config_NE_roots.xml"))),
/**    	    						
//	    		new MLEvaluate(new CreateTemporaryMentions(new SubsequentAnnotationMerge(new ILPEngine()))),
	    	    new MLEvaluate(new CreatePersistentMentions(new PaumEngine()))

/*
	    		new MLEvaluate(
	    				new CreateTemporaryMentions(
	    	    				new MentionsSubtreePostprocessing(
	    	    						new ILPEngine("ILP_config_NE_roots_subtree.xml")))),	    		
	    		new MLEvaluate(
    	    				new CreateTemporaryMentionsReferedMentionsPostprocessing(
    	    						new ILPEngine("ILP_config_NE_roots.xml"))),
	    	    new MLEvaluate(new CreatePersistentMentions(new PaumEngine()))
*	    	    
//	    		new ILPEngine()
			);

		
	}
	
/*
	public static void main(String [] args) throws GateException, URISyntaxException, IOException, JDOMException, BenchmarkReportInputFileFormatException
	{
		for (int i = 0; i < 5; i++)
		{
			main2(args);			
		}
	}
/**/
	
    static Logger logger = Logger.getLogger(MachineLearningExperimenter.class);

    public static void initEnvironment() throws GateException, URISyntaxException, IOException
	{
    	GateUtils.initGate(Level.ALL);
    
	    @SuppressWarnings("unchecked")
		Enumeration<Appender> apps = logger.getAllAppenders();
	    while (apps.hasMoreElements())
	    {
	    	Appender app = apps.nextElement();
	    	if (app.getName() == null)
	    	{
	    		logger.removeAppender(app);
	    	}
	    }

	    logger = Logger.getLogger(Profiler.class);
	    logger.setLevel(Level.OFF);
	    logger = Logger.getLogger(SerialController.class);
	    logger.setLevel(Level.OFF);
	    logger = Logger.getLogger(Gate.class);
	    logger.setLevel(Level.OFF);
	    logger = Logger.getLogger(AnnotationDependencyRootMarker.class);
	    logger.setLevel(Level.ALL);
	    logger = Logger.getLogger(CrossValidation.class);
	    logger.setLevel(Level.INFO);
	    logger = Logger.getLogger(WekaResultExporter.class);
	    logger.setLevel(Level.INFO);
		
	    GateUtils.registerPluginDirectory("Machine_Learning");
	    GateUtils.registerPluginDirectory("ANNIE");
	    GateUtils.registerPluginDirectory("Tools");
	    GateUtils.registerPluginDirectory("Learning");
	    
	    GateUtils.registerCzsemPlugin();
		    		    
	    LogService.minVerbosityLevel = 0;		
	}

	
	public static void saveResults(String results_file_name) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
	    WekaResultExporter ex = new WekaResultExporter();
	    ex.initFromLearningEvaluatorCentralResultsRepository();
	    ex.addInfoFromTimeBechmark();
	    ex.saveAll(results_file_name);		    		
	}
	
/*
	public static MachineLearningExperiment acqCommonExperiment(DataSet dataset)
	{
		MachineLearningExperiment experiment = new MachineLearningExperiment(
	    		dataset,
	    		new MLEvaluate(
	    				new CreateTemporaryMentionsReferedMentionsPostprocessing(
	    						new ILPEngine("ILP_config_NE_roots.xml"))),
	    		/*
	    		new MLEvaluate(
	    				new CreateTemporaryMentions(
	    	    				new MentionsSubtreePostprocessing(
	    	    						new ILPEngine("ILP_config_NE_roots_subtree.xml")))),	    		
	    		new MLEvaluate(new CreateTemporaryMentions(
	    	    		new SubsequentAnnotationMerge(new ILPEngine()))),
/*
	    	    new MLEvaluate(new CreatePersistentMentions(new PaumEngine()))
	    );
		return experiment;
	}

	/*
	public static MachineLearningExperiment acqDlrAmountExperiment(DataSet dataset)
	{
		MachineLearningExperiment experiment = new MachineLearningExperiment(
	    		dataset,
	    		new MLEvaluate(
	    				new CreateTemporaryMentionsRootSubtree(
	    	    				new ILPEngine())),	    		
	    	    new MLEvaluate(new CreatePersistentMentions(new PaumEngine()))
	    );
		return experiment;
	}

/*	
	public static void bigFiremanExperiment(String results_file_name) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException, ExecutionException, ResourceInstantiationException, PersistenceException, JDOMException
	{
		for (String annot_type : CzechFireman.eval_annot_types)
		{
		    LearningEvaluator.CentralResultsRepository.repository.clear();
		    
//		    GateUtils.deleteGateTimeBenchmarkFile();
		    GateUtils.enableGateTimeBenchmark();
		    
//			DataSet dataset = new Acquisitions(annot_type);
			DataSet dataset = new DataSetReduce(new CzechFireman(annot_type), 1.0);
			dataset.clearSevedFilesDirectory();
			

			MachineLearningExperiment experiment = czechFiremanSimple(dataset, annot_type);
			

			experiment.crossValidation(8);
			
			
		    logger.info("saving results, counting time statistics...");
		    saveResults(results_file_name);
			
		   
		    GateUtils.deleteAllPublicGateResources();
		}		
	}

	/*
	
	public static void bigAcquisitionsExperiment(String results_file_name) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException, ExecutionException, ResourceInstantiationException, PersistenceException, JDOMException
	{
		for (String annot_type : Acquisitions.eval_annot_types)
		{
		    LearningEvaluator.CentralResultsRepository.repository.clear();
		    
//		    GateUtils.deleteGateTimeBenchmarkFile();
		    GateUtils.enableGateTimeBenchmark();
		    
//			DataSet dataset = new Acquisitions(annot_type);
			DataSet dataset = new DataSetReduce(new Acquisitions(annot_type), 1.0);
			dataset.clearSevedFilesDirectory();
			
			MachineLearningExperiment experiment =
				annot_type.equalsIgnoreCase("dlramt") 
				? 
				acqDlrAmountExperiment(dataset) 
				:
				acqCommonExperiment(dataset);
				

			experiment.crossValidation(2);
			
			
		    logger.info("saving results, counting time statistics...");
		    saveResults(results_file_name);
			
		   
		    GateUtils.deleteAllPublicGateResources();
		}		
	}
    /**/
    
	
	/**
	 * @param numFolds (1 == train only)
	 * @see Acquisitions#getFactory()
	 * @see #getAcquisitionsIlpEngineFactory()
	 */
	public static void performLargeExperiment(
    		DatasetFactory ds_factory,
    		double ds_reduce_retio,
    		String [] eval_annot_types,
    		EngineFactory engineFactory,
    		int repeatCount,
    		int numFolds,
    		String results_file_name) throws URISyntaxException, IOException, ExecutionException, ResourceInstantiationException, PersistenceException, JDOMException, BenchmarkReportInputFileFormatException
    {
		for (int a=0; a<repeatCount; a++)
		{
			for (String annot_type : eval_annot_types)
			{
			    LearningEvaluator.CentralResultsRepository.repository.clear();
			    TimeBenchmarkUtils.enableGateTimeBenchmark();
			    
				final DataSet dataset =  new DataSetReduce(
						ds_factory.createDataset(annot_type),
						ds_reduce_retio);
				dataset.clearSevedFilesDirectory();
				
				TrainTest engine = engineFactory.createEngine(annot_type);
				
				MachineLearningExperiment experiment = new MachineLearningExperiment(
						dataset,
						engine,
						new MLEvaluate(new CreatePersistentMentions(new PaumEngine())));
				
				if (numFolds == 1)
					experiment.trainOnly();
				else
				{
					experiment.crossValidation(numFolds, new Runnable() {						
						@Override
						public void run() {
							try {
								dataset.clearSevedFilesDirectory();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					});
				    
					logger.info("saving results, counting time statistics...");
				    saveResults(results_file_name);
				}
				
				
							   
			    GateUtils.deleteAllPublicGateResources();
			}			
		}    	
    }
	
	public static void main(String [] args) throws Exception
	{
    	initEnvironment();
    	/**/
		
		String results_file_name = "weka_results.csv";
		
		new File(results_file_name).delete();
		
/**/
//		bigFiremanExperiment
		performLargeExperiment(
				CzechFireman.getFactory(),
				1.0,
				CzechFireman.eval_annot_types,
				getCzechFiremanIlpEngineFactory(),
				2, //repeats
				2, //folds
				results_file_name);
/*
		// trin only
		performLargeExperiment(
				CzechFireman.getFactory(),
				1.0,
//				CzechFireman.eval_annot_types,
				new String[] {"amateur_unit"},
				getCzechFiremanIlpEngineFactory(),
				1, //repeats
				1, //folds, 1 == train only 
				results_file_name);


/*		

		
//		bigAcquisitionsExperiment
		performLargeExperiment(
				Acquisitions.getFactory(),
				1.0,
				Acquisitions.eval_annot_types,
				getAcquisitionsIlpEngineFactory(),
				5, //repeats
				2,  //folds
				results_file_name);
/**/		
		WekaResultTests t = new WekaResultTests(System.err);
		t.loadInstances(results_file_name);
		t.testsAttr(6); //F1

/*	    
	    new MachineLearningExperiment(
	    		new Acquisitions("acquired"),
//	    		new DataSetReduce(new Acquisitions("acquired"), 0.05),
//	    		new CzechFireman("damage"),
//	    		new MLEvaluate(new CreateTemporaryMentions(new ILPEngine()))
	    		new MLEvaluate(new CreateTemporaryMentionsRootSubtree(new ILPEngine())),
	    		new MLEvaluate(
	    				new CreateTemporaryMentions(
	    	    				new MentionsSubtreePostprocessing(
	    	    						new ILPEngine("ILP_config_NE_roots_subtree.xml")))),	    		
	    		new MLEvaluate(
    	    				new CreateTemporaryMentionsReferedMentionsPostprocessing(
    	    						new ILPEngine("ILP_config_NE_roots.xml"))),
	    	    new MLEvaluate(new CreatePersistentMentions(new PaumEngine()))
//	    		new ILPEngine()
	    ).crossValidation(2);
/*	    
	    
	    czechFiremanSimple().trainOnly();
	}
	    
	    
//	    logger.info("time statistics:\n"+GateUtils.createGateTimeBenchmarkReport());	    
	    
	    

	    
	    
	    //	    trainOnly(new TrainTestGateOnCzech(false, true));
//	    trainOnly(new TrainTestAcquisitions(new ILPEngine()));
//	    trainOnly(new TrainTestCzechFireman(new ILPEngine()));
//	
/**/				
	}

	
	
	
	
	
	
	
	
	

	@SuppressWarnings("rawtypes")
	public static void jape_pok (gate.Document doc, java.util.Map bindings, 
			                     gate.AnnotationSet annotations, 
			                     gate.AnnotationSet inputAS, gate.AnnotationSet outputAS, 
			                     gate.creole.ontology.Ontology ontology)
	{
		gate.AnnotationSet binding_as = (gate.AnnotationSet) bindings.get("anot_tmp");
		gate.Annotation annot = binding_as.iterator().next();
		annot.getFeatures().put("rule", "damage_add_learninig_feature");
		
		
		
		String lemma = "lema";
		String form = "form";
		
		if (lemma.startsWith("&"))	
			lemma = StringEscapeUtils.unescapeXml(lemma);

		int slash =  lemma.indexOf('-', 1);
		int under =  lemma.indexOf('_', 1);
		
		int substr_end = lemma.length();
		
		if (slash > 0) substr_end = slash;
		if (under > 0) substr_end = Math.min(substr_end, under);

		
		char first_ch = lemma.charAt(0);
		
		if (Character.isUpperCase(form.charAt(0)))
		{
			first_ch = Character.toUpperCase(first_ch);			
		}

		//String clean_lemma = first_ch + lemma.substring(1, substr_end);

		
		
/*		
		gate.AnnotationSet binding_as = (gate.AnnotationSet) bindings.get("anot_tmp");
		gate.Annotation annot = binding_as.iterator().next();
		String typename = annot.getType();
		String new_typename = typename.replaceFirst("Dependecy", "Dependency");
		FeatureMap fm = annot.getFeatures();
		outputAS.add(annot.getStartNode(), annot.getEndNode(), new_typename, fm);
		inputAS.remove(annot);
/*
		gate.AnnotationSet b_as = (gate.AnnotationSet) bindings.get("dep_tmp");
		gate.Annotation dep_an = b_as.iterator().next();
		
		Object dep_kind = dep_an.getFeatures().get("kind");
		List<Integer> args = (List<Integer>) dep_an.getFeatures().get("args");
			
		gate.Annotation token_an = inputAS.get(args.get(1));
		gate.FeatureMap fm = token_an.getFeatures();
		fm.put("depency_type", dep_kind);
/**/				
	}
}
