package czsem.gate.learning;

import gate.CreoleRegister;
import gate.Document;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.creole.ExecutionException;
import gate.creole.SerialAnalyserController;
import gate.creole.ml.AdvancedMLEngine;
import gate.creole.ml.Attribute;
import gate.creole.ml.DatasetDefintion;
import gate.creole.ml.MachineLearningPR;
import gate.event.ProgressListener;
import gate.util.GateException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Element;

import czsem.gate.ilp.ILPSerializer;
import czsem.gate.plugins.CrossValidation;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.utils.GateUtils.CorpusDocumentCounter;

public class ILPWrapper implements AdvancedMLEngine
{
	static Logger logger = Logger.getLogger(ILPWrapper.class);
	
	private static Set<ILPWrapper> train_register = new HashSet<ILPWrapper>();
	
	protected int idIndex;
	protected int lastAttrIndex;
	protected int classIndex;
	protected String className;
	protected ILPSerializer ilpSer;
	protected CorpusDocumentCounter docCounter;
	
	/** there are training instances in cache, training should be executed... */  
	protected boolean triningInProgress = false;

	protected DatasetDefintion datasetDefinition = null;
	protected Element options;
	protected MachineLearningPR pr = null;

	@Override
	public boolean supportsBatchMode() {
		return true;
	}
	 

	@SuppressWarnings("unchecked")
	@Override
	public void addTrainingInstance(@SuppressWarnings("rawtypes") List attributes) throws ExecutionException {				
		try {
			serializeTrainingInstance(attributes);
		} catch (Throwable e) {
			throw new ExecutionException(e);
		}
	}

	protected boolean isLastInstanceInDocument(List<String> attributes)
	{
		return attributes.get(lastAttrIndex) == null;
	}
	
	protected void serializeCurrentDoc()
	{
		Document doc = pr.getDocument();
		
		if (docCounter == null) docCounter = new CorpusDocumentCounter(pr.getCorpus());
		if (docCounter.addDocument(doc))
		{
			ilpSer.serializeDocument(doc, pr.getInputASName());
//			logger.debug("SerializeDocument: " + doc.getName());
		}		
	}

	protected void startTraining() throws IOException, InterruptedException, URISyntaxException
	{
		if (! triningInProgress) return;
		
		ilpSer.flushAndClose();
		logger.info(String.format("ILP training on %d documents...", docCounter.getNumDocs()));
		logger.info("Learning instace types: " + ilpSer.getInstanceClassTypes().toFormatedString(", "));
		
		CrossValidation cv = findparentCrossValidationInstatnce();
		if (cv != null)
		{
			LearningEvaluator.CentralResultsRepository.repository.
				addNumberDocsAndTrainingInstances(
						pr.getName(), docCounter.getNumDocs(),
						cv.actual_fold_number, ilpSer.getInstanceClassTypes());			
		}
		
		ilpSer.train();
		
		triningInProgress = false;
		
		train_register.remove(this);
//		ilpSer.train(options.getChild("ilp").getChildText("learning_settings"));		
	}

	public static void executeAllTraingInRegister() throws IOException, InterruptedException, URISyntaxException
	{
		for (ILPWrapper wrap : train_register)
		{
			wrap.startTraining();			
		}
		
		train_register.clear();
	}
	
	protected void serializeTrainingInstance(List<String> attributes) throws IOException, InterruptedException, URISyntaxException
	{				
//		serializeCurrentDoc();
		
		ilpSer.serializeTrainingInstance(
				attributes.get(idIndex),
				pr.getDocument().getName(),
				className,
				attributes.get(classIndex));
		
		triningInProgress = true;
		train_register.add(this);
		
/**/
		if (docCounter.isLastDocument() && isLastInstanceInDocument(attributes))
		{
			startTraining();
		}
/**/		
	}

	public Collection<String>[] classifyInstances(List<List<String>> instances) throws IOException, InterruptedException, URISyntaxException
	{
		
		Document doc = pr.getDocument();
		String docName = doc.getName();

		ilpSer.setBackgroundSerializerFileName(docName);
		ilpSer.serializeDocument(doc, pr.getInputASName());
		ilpSer.closeBackgroundSerializer();
	
		
		String [] instancesGateIds = new String[instances.size()];
		for (int i = 0; i < instancesGateIds.length; i++)
		{
			instancesGateIds[i] = instances.get(i).get(idIndex);			
		}
		
		return ilpSer.classifyInstances(instancesGateIds, className);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List batchClassifyInstances(List instances) throws ExecutionException
	{
		try
		{
			return Arrays.asList(classifyInstances(instances));
		} 
		catch (Throwable t)
		{
			throw new ExecutionException(t);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object classifyInstance(List attributes) throws ExecutionException {
		throw new ExecutionException(new NoSuchMethodException("Use batchClassifyInstances instead!"));
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() throws GateException
	{		
		String configFileURI = pr.getConfigFileURL().getPath();
		File configFile = new File(configFileURI);
		String name_ext = configFile.getName();
		String name = name_ext.substring(0, name_ext.indexOf('.')); 
		try
		{
			ilpSer = new ILPSerializer(configFile.getParent(), name);
			ilpSer.initSerializer(options.getChild("serializer"));
			
			if (pr.getTraining())
			{
				ilpSer.initLearning(
						className,
						datasetDefinition.getInstanceType(),
						options.getChild("ilp").getChildText("learning_settings"));
			}
		}
		catch (Throwable e) {
			throw new GateException(e);
		}
		
		docCounter = null;
	}

	
	@Override
	public void setDatasetDefinition(DatasetDefintion definition) {
		this.datasetDefinition = definition ;
		
		List<Attribute> attrs = getDatasetAttributes();
		
		for (int i=0; i<attrs.size(); i++)
		{
			if (attrs.get(i).getName().equals("id")) idIndex = i;
			if (attrs.get(i).getName().equals("last")) lastAttrIndex = i;
		}
		classIndex = datasetDefinition.getClassIndex();
		String raw_name = datasetDefinition.getClassAttribute().getName();
		className = Character.toLowerCase(raw_name.charAt(0)) + raw_name.substring(1);
	}


	@SuppressWarnings("unchecked")
	private List<Attribute> getDatasetAttributes() {
		return datasetDefinition.getAttributes();
	}

	
	@Override
	public void setOptions(Element options) {
		this.options = options;
	}
	

	
	protected CrossValidation findparentCrossValidationInstatnce()
	{
		CreoleRegister reg = Gate.getCreoleRegister();
		
		for (ProcessingResource i : reg.getPrInstances(CrossValidation.class.getCanonicalName()))
		{
			CrossValidation cv = (CrossValidation) i;
			
			LanguageAnalyser la;
			if (pr.getTraining()) la = cv.getTrainingPR();
			else la = cv.getTestingPR();
			
			if (SerialAnalyserController.class.isInstance(la))
			{
				SerialAnalyserController sac = (SerialAnalyserController) la;
				for (Object child : sac.getPRs())
				{
					if (child == pr) return cv;					
				}
			}			
		}

		return null;		
	}
	
	@Override
	public void setOwnerPR(ProcessingResource owner_pr)
	{
		pr = (MachineLearningPR) owner_pr;
				
		pr.addProgressListener(new ProgressListener() {
			@Override
			public void progressChanged(int i)
			{
				if (pr.getTraining())
					serializeCurrentDoc();
			}
			
			@Override
			public void processFinished() {}
		});
	}

}
