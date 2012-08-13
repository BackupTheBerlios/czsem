package czsem.ontology;

import org.semanticweb.HermiT.Reasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class CzsemRulesBenchmark {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception
	{
		String [] acquisitions = 
		{
				"C:\\workspace\\czsem\\src\\java\\czsem\\gate-learning\\acquisitions-v1.1\\rules\\ILP_config_rules_noise30",
				"C:\\data\\czsem_coprus\\acquisitions-v1.1_RDF_OWL",
				"false"
		};

		String [] czech_fireman = 
		{
				"C:\\workspace\\czsem\\src\\java\\czsem\\gate-learning\\czech_fireman\\rules\\ILP_config_rules",
				"C:\\data\\czsem_coprus\\czech_fireman_RDF_OWL",
				"false"
		};
		
		runBenchmark(args);
//		runBenchmark(czech_fireman);
//		runBenchmark(acquisitions);
	}
	
	public static void runBenchmark(String[] args) throws Exception
	{
		System.out.println("usage: <rules file without extension> <ontology dir> <runhermit (true/false)>");
		boolean runHermit = Boolean.parseBoolean(args[2]);

		
		long stats[][] = new long[3][3] ;
		
		int cur = 0;
/**/
		//Jena
		stats[cur][0] = System.nanoTime();
		stats[cur][2] = Pml2GateReasoning.processDirectory(
				new CzsemJenaRuleEngine(),
				args[1],
				args[0] + ".jena");
		stats[cur][1] = System.nanoTime();
/**/
		cur++;		
		
		//HermiT
		stats[cur][0] = System.nanoTime();		
		if (runHermit)
		{
		stats[cur][2] = Pml2GateReasoning.processDirectory(
				new OwlApiRulesEngine(new Reasoner.ReasonerFactory()),
				args[1],
				args[0] + ".owl");
		}		
		stats[cur][1] = System.nanoTime();

		
		cur++;

		//Pellet
		stats[cur][0] = System.nanoTime();
		stats[cur][2] = Pml2GateReasoning.processDirectory(
				new OwlApiRulesEngine(PelletReasonerFactory.getInstance()),
				args[1],
				args[0] + ".owl");
		stats[cur][1] = System.nanoTime();

/*
		cur++;

		//Fact++
		stats[cur][0] = System.nanoTime();
		stats[cur][2] = Pml2GateReasoning.processDirectory(
				new OwlApiRulesEngine(new FaCTPlusPlusReasonerFactory()),
				args[1],
				args[0] + ".owl");
		stats[cur][1] = System.nanoTime();
/**/				
		for (int i = 0; i < stats.length; i++)
		{
			System.out.println("-----------------------------------------------");			
			System.out.format("start time: %d\n", stats[i][0]);
			System.out.format("end time: %d\n", stats[i][1]);
			System.out.format("duration: %d\n", stats[i][1] - stats[i][0]);
			System.out.format("result count: %d\n", stats[i][2]);
			System.out.println("-----------------------------------------------");			
			
		}

	}
}
