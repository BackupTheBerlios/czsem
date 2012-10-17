package czsem.gate.learning;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.persist.PersistenceException;
import gate.util.AnnotationDiffer;
import gate.util.GateException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import czsem.gate.learning.DataSet.DataSetImpl.Acquisitions;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.plugins.AnnotationDependencyRootMarker;
import czsem.gate.plugins.AnnotationDependencySubtreeMarker;
import czsem.gate.plugins.CreateMentionsPR;
import czsem.gate.utils.Config;
import czsem.gate.utils.GateUtils;
import czsem.utils.MultiSet;

public class MeasureNamedEntityOverlap
{


	private DataSet dataset;
	private Corpus loaded_corpus;


	public static String mentNE_AS = "ment_NamedEntity";
	
	final String tocType = "tToken";
	final String depType = "tDependency";
	final String neType = "Lookup";
//	final String neType = "NamedEntity";



	public static String rootAS(String annot_type)
	{
		return "root_"+annot_type;
	}

	public static String mentAS(String annot_type)
	{
		return "ment_"+annot_type;
	}

	public MeasureNamedEntityOverlap(DataSet dataset)
	{
		this.dataset = dataset;
		
	}
	
	public void loadDataset() throws PersistenceException, ResourceInstantiationException
	{
		loaded_corpus = dataset.getCorpus();
		for (int i=0; i<loaded_corpus.size(); i++)
		{
			loaded_corpus.get(i);
			
			if (i % (loaded_corpus.size()/10) == 0) System.err.println(i);
		}
	}

		
	public void compare(String annotType, GateUtils.CustomizeDiffer cd)
	{
		//first doc
		Document first_doc = (Document) loaded_corpus.get(0);
		cd.setAnnotType(annotType);
		AnnotationDiffer overall_differ = GateUtils.calculateSimpleDiffer(first_doc, cd);
		
		//remaining docs
		for (int i=1; i<loaded_corpus.size(); i++)
		{
			Document doc = (Document) loaded_corpus.get(i);
			AnnotationDiffer differ = GateUtils.calculateSimpleDiffer(doc, cd);

			AnnotationDiffer [] differs = new AnnotationDiffer[2];
			differs[0] = differ;
			differs[1] = overall_differ;
			overall_differ = new AnnotationDiffer(Arrays.asList(differs));
		}		
		

		System.err.format("A: %22s | B: %10s | ", cd.getReponseAS(), cd.getKeyAs()); 
		System.err.format("tottalA: %4d | tottalB: %4d |  match: %4d | overlap: %4d | A+: %4d | B+: %4d | A/B rec: %f | A/B prec: %f |\n", 
//				annotType,
				overall_differ.getCorrectMatches() + overall_differ.getSpurious() + overall_differ.getPartiallyCorrectMatches(),  
				overall_differ.getCorrectMatches() + overall_differ.getMissing() + overall_differ.getPartiallyCorrectMatches(),  
				overall_differ.getCorrectMatches(),
				overall_differ.getPartiallyCorrectMatches(),
				overall_differ.getSpurious(),
				overall_differ.getMissing(),
				overall_differ.getPrecisionStrict(),
				overall_differ.getRecallStrict());

	}

	
	public void compare(GateUtils.CustomizeDiffer cd)
	{
		for (String annot_type: dataset.getLearnigAnnotationTypes())
		{
			compare(annot_type, cd);
		}
	}

	public void compare()
	{
		//named entity 
		compare(new GateUtils.CustomizeDiffer()
		{
			@Override
			public String getKeyAs() {	return mentNE_AS;}
			@Override
			public String getReponseAS() {	return mentAS(annotType);}
			@Override
			public String getAnnotationType() {return CreateMentionsPR.defaultMentionAnntotationTypeName;}
		}
		);
		
	
		System.err.println("root subtree");
	
		//root subtree 
		compare(new GateUtils.CustomizeDiffer()
		{
			@Override
			public String getKeyAs() {	return dataset.getKeyAS();}
			@Override
			public String getReponseAS() {	return rootAS(annotType);}
			@Override
			public String getAnnotationType() {return annotType;}					
		}
		);

	}

	public void createAndAddRootSubtreePRs(SerialAnalyserController controller, String annot_type) throws ResourceInstantiationException
	{		
		ProcessingResource root_pr = new SinglePRSetup(
				AnnotationDependencyRootMarker.class, "cr_root_"+annot_type)
					.putFeature("inputASName", dataset.getKeyAS())
					.putFeature("outputASName", rootAS(annot_type))
					.putFeatureList("inputAnnotationTypeNames", annot_type)
					.putFeature("tokensAndDependenciesASName", dataset.getTectoMTAS())
					.putFeature("tokenAnnotationTypeName", tocType)
					.putFeature("dependencyAnnotationTypeName", depType)
				.createPR();
		
		controller.add(root_pr);

		
		ProcessingResource subtree_pr = new SinglePRSetup(
				AnnotationDependencySubtreeMarker.class, "cr_subtree_"+annot_type)
					.putFeature("inputASName", rootAS(annot_type))
					.putFeature("outputASName", rootAS(annot_type))
					.putFeatureList("inputAnnotationTypeNames", annot_type+"_root")
					.putFeature("tokensAndDependenciesASName", dataset.getTectoMTAS())
					.putFeature("tokenAnnotationTypeName", tocType)
					.putFeature("dependencyAnnotationTypeName", depType)
				.createPR();
		
		controller.add(subtree_pr);

		
	}

	

	public void countTokenOverlap()
	{
		for (String annot_type: dataset.getLearnigAnnotationTypes())
		{
			countTokenOverlap(annot_type);
		}	
	}

	public void countTokenOverlap(String annot_type)
	{
		MultiSet<Integer> token_stats = new MultiSet<Integer>();
		
		for (int i=0; i<loaded_corpus.size(); i++)
		{
			Document doc = (Document) loaded_corpus.get(i);
			
			AnnotationSet anns = doc.getAnnotations(dataset.getKeyAS()).get(annot_type);
			AnnotationSet tocs = doc.getAnnotations(dataset.getTectoMTAS()).get(tocType);
			
			for (Annotation a: anns)
			{
				AnnotationSet cont = tocs.getContained(a.getStartNode().getOffset(), a.getEndNode().getOffset());
				token_stats.add(cont.size());
				
				if (! GateUtils.testAnnotationsDisjoint(cont))					
				{
					System.err.format(
							"Warning: token annotations ar not dijoint: doc: '%s', top annotation: '%s' tokens: '%s'\n",
							doc.getName(), a.toString(), cont.toString());
				}
			}			
		}
		
		Integer[] keys = token_stats.toArray(new Integer[0]);

		int sum=0;
		int num=0;
		for (int i=0; i<keys.length; i++)
		{		
			int n = token_stats.get(keys[i]);
			num += n;
			sum += n*keys[i];
		}
		System.err.format("%s (avg. %f)\n", annot_type, ((double) sum) / num);

		
		Arrays.sort(keys);
		for (int i=0; i<keys.length; i++)
		{
			System.err.format("%10d-token annotations: %4d\n", keys[i], token_stats.get(keys[i]));
		}
	}

	public void createMentions() throws ResourceInstantiationException, ExecutionException
	{
		SerialAnalyserController controller = (SerialAnalyserController)	    	   
			Factory.createResource(SerialAnalyserController.class.getCanonicalName());
		
		for (String annot_type: dataset.getLearnigAnnotationTypes())
		{
			ProcessingResource km_pr = new SinglePRSetup(
					CreateMentionsPR.class, "cr_menations_"+annot_type)
						.putFeature("inputASName", dataset.getKeyAS())
						.putFeature("outputASName", mentAS(annot_type))
						.putFeatureList("inputAnnotationTypeNames", annot_type)
					.createPR();
			
			controller.add(km_pr);
			
			createAndAddRootSubtreePRs(controller, annot_type);
		}
				
		ProcessingResource nm_pr = new SinglePRSetup(
				CreateMentionsPR.class, "cr_menations_NamedEntity")
					.putFeature("inputASName", dataset.getTectoMTAS())
					.putFeature("outputASName", mentNE_AS)
					.putFeatureList("inputAnnotationTypeNames", neType)
				.createPR();
		
		controller.add(nm_pr);

		
		controller.setCorpus(loaded_corpus);
		
		controller.execute();		
	}

	public static void main(String[] args) throws URISyntaxException, IOException, GateException
	{
	    Locale.setDefault(Locale.ENGLISH);
		
		Config.getConfig().setGateHome();
	    Gate.init();
	    GateUtils.registerCzsemPlugin();
	
	
		
		MeasureNamedEntityOverlap m = new MeasureNamedEntityOverlap(new Acquisitions(Acquisitions.eval_annot_types));
//		MeasureNamedEntityOverlap m = new MeasureNamedEntityOverlap(new CzechFireman(CzechFireman.all_annot_types));
		
		m.loadDataset();
	
		System.err.println("in memory");
	
		m.createMentions();
				
		GateUtils.saveGateDocumentToXML((Document) m.loaded_corpus.get(0), "test_doc.xml");
			
		m.compare();
		
		System.err.println("tokens");

		m.countTokenOverlap();
		
		System.err.println("done");
	
	}


}
