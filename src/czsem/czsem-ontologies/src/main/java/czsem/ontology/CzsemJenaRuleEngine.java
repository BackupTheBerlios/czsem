package czsem.ontology;

import java.io.File;
import java.io.OutputStream;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class CzsemJenaRuleEngine implements CzsemRulesBenchmarkEngine
{
	private OntModel m;
	private GenericRuleReasoner reasoner;
	private InfModel inf;


	@Override
	public void newEngineFromRules(String rules_file_path) throws Exception
	{
		m = ModelFactory.createOntologyModel();
		
		m.read(getClass().getResource("PMT2GATE_ontology_utils.owl").toString());

		List<Rule> rules = Rule.rulesFromURL(getClass().getResource("PMT2GATE_ontology_utils.jena").toString());
		rules.addAll(
				Rule.rulesFromURL(
						new File(rules_file_path).toURI().toURL().toString()));

		reasoner = new GenericRuleReasoner(rules);		
	}

	@Override
	public void addOntology(String ontology_path) throws Exception {
		m.read(new File(ontology_path).toURI().toURL().toString());		
	}

	@Override
	public void attachReasoner() throws Exception {
		inf = ModelFactory.createInfModel(reasoner, m);		
	}

	@Override
	public int printResults() {
		ResIterator list_propobj = inf.listSubjectsWithProperty(inf.createProperty("http://ufal.mff.cuni.cz/pdt/pml/mention_root"));
        int count = 0;
		while (list_propobj.hasNext())
		{
			System.out.println(list_propobj.next().toString());			
            count ++;
		}
		return count;
	}

	@Override
	public void saveResutls(OutputStream output)
	{
		inf.write(output);		
		
	}

}
