package czsem.ontology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import org.apache.log4j.Logger;
import org.semanticweb.HermiT.Reasoner;



public class Pml2GateReasoning
{
	static Logger logger = Logger.getLogger(Pml2GateReasoning.class);
	
	public static int processFile(CzsemRulesBenchmarkEngine engine, String ontology_path, String rules_file_path) throws Exception
	{
		engine.newEngineFromRules(rules_file_path);
		engine.addOntology(ontology_path);
		engine.attachReasoner();

		return engine.printResults();
	}
	
	public static int processFileAndSaveResults(
			CzsemRulesBenchmarkEngine engine,
			String ontology_path,
			String rules_file_path,
			String outptuPath) throws Exception
	{
		engine.newEngineFromRules(rules_file_path);
		engine.addOntology(ontology_path);
		engine.attachReasoner();

		int ret = engine.printResults();
		
		engine.saveResutls(new FileOutputStream(outptuPath));
		
		return ret;
	}


	public static int processDirectory(
			CzsemRulesBenchmarkEngine engine,
			String directory_path,
			String rules_file_path) throws Exception
	{
		File dir = new File(directory_path);
		String[] file_list = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String file_name) {
				return file_name.endsWith("owl");
			}
		});
		
		int instance_count = 0;
		for (int i = 0; i < file_list.length; i++)
		{
			String file = file_list[i];
			logger.info(
					String.format("Reasoning on file %d/%d: %s",
					i+1, file_list.length,
					file));
					 
			engine.newEngineFromRules(rules_file_path);
//			Pml2GateReasoning r = new Pml2GateReasoning(new File(rules_file_path));
			engine.addOntology(directory_path +'/'+ file);
//			r.addOntology(new File(directory_path +'/'+ file));
			
			engine.attachReasoner();
			
//			instance_count += r.printClassInstances(IRI.create("http://czsem.berlios.de/ontologies/PMT2GATE_ontology_utils.owl#MentionRoot"));
			instance_count += engine.printResults();
		}
		
		System.out.format("total number of instances: %d", instance_count);
		return instance_count;
	}
	
	public static void main(String[] args) throws Exception
	{
		System.out.println("usage: <rules file path> <ontology file path>");
		/**/
		processFile(
				new OwlApiRulesEngine(new Reasoner.ReasonerFactory()),
				args[1],
				args[0]);
		
		/*
		processDirectory(
				new CzsemJenaRules(),
				"czsem_GATE_plugins/TmT_serializations/owl",
				"gate-learning/acquisitions-v1.1/rules/ILP_config_rules_noise30.jena");

		/*

		processDirectory(
//				new OwlApiRulesEngine(PelletReasonerFactory.getInstance()),
				new OwlApiRulesEngine(new Reasoner.ReasonerFactory()),
				"czsem_GATE_plugins/TmT_serializations/owl",
				"gate-learning/acquisitions-v1.1/rules/ILP_config_rules_noise30.owl");
		/*		
		Pml2GateReasoning r = new Pml2GateReasoning(new File("gate-learning/czech_fireman/rules/learned_rules_test1.owl"));
		r.addOntology(new File("czsem_GATE_plugins/TmT_serializations/owl/jihomoravsky47443.txt.xml_00034.tmt.owl"));
		
		r.attachReasoner();
		
		r.printClassInstances(IRI.create("http://czsem.berlios.de/ontologies/PMT2GATE_ontology_utils.owl#MentionRoot"));
		/*
		processFileAndSaveResults(
				new OwlApiRulesEngine(PelletReasonerFactory.getInstance()),
				"C:/data/czsem_coprus/czech_fireman_RDF_OWL/jihomoravsky47443.txt.xml_00034.tmt.owl",
				"gate-learning/czech_fireman/rules/ILP_config_rules.owl",
				"results.owl");
		/**/

	
	}

}