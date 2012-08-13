package czsem.ontology;

import java.io.OutputStream;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public interface CzsemRulesBenchmarkEngine
{

	void newEngineFromRules(String rules_file_path) throws Exception;

	void addOntology(String ontology_path) throws Exception;

	void attachReasoner() throws Exception;

	int printResults();

	void saveResutls(OutputStream output) throws OWLOntologyCreationException, OWLOntologyStorageException;

}
