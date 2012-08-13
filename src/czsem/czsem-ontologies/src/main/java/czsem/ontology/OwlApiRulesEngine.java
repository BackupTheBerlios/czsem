package czsem.ontology;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.IllegalConfigurationException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

public class OwlApiRulesEngine implements CzsemRulesBenchmarkEngine
{
	static Logger logger = Logger.getLogger(OwlApiRulesEngine.class);
	
	private OWLOntologyManager manager;

	private OWLReasoner reasoner = null;

	private OWLReasonerFactory reasonerFactory;

	public OwlApiRulesEngine(OWLReasonerFactory reasonerFactory) {
		this.reasonerFactory = reasonerFactory;
	}

	public void initWithRulesFile(File rules_onto_file) throws OWLOntologyCreationException
	{		
        manager = OWLManager.createOWLOntologyManager();

        addOntology(Pml2GateReasoning.class.getResourceAsStream("PMT2GATE_ontology_utils.owl"));
		addOntology(rules_onto_file);
	}

	public void addOntology(InputStream onto_stream) throws OWLOntologyCreationException
	{
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(onto_stream);      		  
        logger.debug("Loaded " + ont.getOntologyID());		
	}

	public void addOntology(File onto_file) throws OWLOntologyCreationException
	{
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(onto_file);      		  
        logger.debug("Loaded " + ont.getOntologyID());		
	}
	
	/**
	 * @param mergedOntologyIRI - can be null.
	 */
	public OWLOntology getMergedOntology(IRI mergedOntologyIRI) throws OWLOntologyCreationException
	{
        OWLOntologyMerger merger = new OWLOntologyMerger(manager);
        return merger.createMergedOntology(manager, mergedOntologyIRI);		
	}
	
	public OWLReasoner attachDefaultReasoner() throws IllegalConfigurationException, OWLOntologyCreationException
	{
		return attachReasoner(reasonerFactory); 
//		return attachReasoner(PelletReasonerFactory.getInstance()); //Pellet
//		return attachReasoner(new Reasoner.ReasonerFactory()); //HermiT
//		return attachReasoner(new FaCTPlusPlusReasonerFactory()); //FaCTpp
	}
	
	public OWLReasoner attachReasoner(OWLReasonerFactory reasonerFactory) throws IllegalConfigurationException, OWLOntologyCreationException
	{
		if (reasoner != null) reasoner.dispose();
		
		ReasonerProgressMonitor progressMonitor = new NullReasonerProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        return reasoner = reasonerFactory.createReasoner(getMergedOntology(null), config);        
	}
	
	public void doInference()
	{
		reasoner.precomputeInferences();
	}
	
	/**
	 * @return number of all matching instances
	 */
	public int printClassInstances(IRI className)
	{
		OWLDataFactory fac = manager.getOWLDataFactory();
        OWLClass cls = fac.getOWLClass(className);

        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(cls, false);

        int count = 0;
        for (Node<OWLNamedIndividual> instace : instances)
        {
            System.out.println(instace.getRepresentativeElement().getIRI());
            count ++;
		}
        
        return count;        
	}

	@Override
	public void newEngineFromRules(String rules_file_path) throws Exception {
		initWithRulesFile(new File(rules_file_path));		
	}

	@Override
	public void addOntology(String ontology_path) throws Exception {
		addOntology(new File(ontology_path));
	}

	@Override
	public void attachReasoner() throws Exception {
		attachDefaultReasoner();
	}

	@Override
	public int printResults() {
		return printClassInstances(IRI.create("http://czsem.berlios.de/ontologies/PMT2GATE_ontology_utils.owl#MentionRoot"));
	}

	@Override
	public void saveResutls(OutputStream output) throws OWLOntologyCreationException, OWLOntologyStorageException
	{
		reasoner.flush();
		
		OWLOntology exportedOntology = manager.createOntology( IRI.create("http://czsem.berlios.de/ontologies/infered_export.owl"));
				
		InferredOntologyGenerator g = new InferredOntologyGenerator(reasoner);
		g.fillOntology(manager, exportedOntology);
		
		manager.saveOntology( exportedOntology, new RDFXMLOntologyFormat(), output);
		
	}	
}
