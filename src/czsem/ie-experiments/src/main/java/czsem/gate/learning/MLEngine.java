package czsem.gate.learning;

import gate.creole.annotransfer.AnnotationSetTransfer;
import gate.creole.ml.MachineLearningPR;
import gate.learning.LearningAPIMain;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import czsem.gate.learning.experiments.MachineLearningExperiment.TrainTest;
import czsem.gate.plugins.LearningEvaluator;
import czsem.utils.JDomUtils;

public abstract class MLEngine implements TrainTest
{
	public static class MLEngineConfig
	{
		public String experimentLearningConfigsDirectory; 
		public String inputAS;
		public String outputAS;
		public String keyAS;
		public String learnigAnnotationType = "Mention";
		/** To be translated to the 'class' feature of the 'Mention' learning annotation type. **/
		public List<String> originalLearnigAnnotationTypes;
		
		public List<LearningEvaluator> evaluation_register;
	}
		
	private String outputAS;
	protected String configFileName;
	
	@Override
	public boolean getClearOutputAsBeforeTesting() {
		return true;
	}

	@Override
	public void clearSevedFilesDirectory(MLEngineConfig config)
	{
		String configDirectory = config.experimentLearningConfigsDirectory + '/' + configFileName;
		configDirectory = configDirectory.substring(0, configDirectory.lastIndexOf('/'));
		File dir = new File(configDirectory + "/savedFiles");
		dir.mkdirs();
		File[] files = dir.listFiles();
		List<File> dirs = new ArrayList<File>(files.length);
		for (File f : files)
		{
			if (f.isDirectory()) dirs.add(f);
		}
		FileUtils.deleteQuietly(dir );			
		for (File d : dirs)
		{
			d.mkdirs();
		}
	}

	public MLEngine(String outputAS, String configFileName)
	{
		this.setOutputAS(outputAS);
		this.configFileName = configFileName;
	}

	protected URL getConfigURL(String experimentDirectory) throws MalformedURLException
	{
		return new File(experimentDirectory + '/' + configFileName).toURI().toURL(); 			
	}
	
	protected void setOutputAS(String outputAS) {
		this.outputAS = outputAS;
	}
	
	@Override
	public String getDefaultOutputAS() {
		return outputAS;
	}

	@Override
	public String getDefaultLearningAnnotationType() {
		return "Mention";
	}
	
	public static String renderPRNameTrain(String defaultOutputAS)
	{
		return "MLEngine" + defaultOutputAS + "_train";		
	}

	public static String renderPRNameTest(String defaultOutputAS)
	{
		return "MLEngine" + defaultOutputAS + "_apply";		
	}
	
	public String getNameTrain()
	{
		return renderPRNameTrain(getDefaultOutputAS());
	}

	public String getNameTest()
	{
		return renderPRNameTest(getDefaultOutputAS());
	}
	
	public static String readLearninigAnnotType(URL config_doc_url) throws JDOMException, IOException
	{
		Document config_dom = JDomUtils.getJdomDoc(config_doc_url);
		@SuppressWarnings("unchecked")
		List<Element> ch = config_dom .getRootElement().getChild("DATASET").getChildren("ATTRIBUTE");
		for (Element element : ch)
		{
			if (element.getChild("CLASS") != null)
			{
				return element.getChildText("TYPE");				
			}
			
		}
		return null;				
	}


	public static class FakeEngine extends MLEngine
	{
		public FakeEngine(String outputAS) {
			super(outputAS, null);
		}
		
		@Override
		public boolean getClearOutputAsBeforeTesting() {
			return false;
		}

		@Override
		public void clearSevedFilesDirectory(MLEngineConfig config) {}

		@Override
		public List<PRSetup> getTrainControllerSetup(MLEngineConfig config)	throws MalformedURLException {
			return new ArrayList<PRSetup>();
		}

		@Override
		public List<PRSetup> getTestControllerSetup(MLEngineConfig config) throws MalformedURLException {
			return new ArrayList<PRSetup>();
		}
		
	}

	public static class PaumEngine extends MLEngine
	{
		public PaumEngine(String configFileName)
		{			
			super(configFileName
					.substring(configFileName.indexOf('/')+1, configFileName.indexOf('.'))
					.replace("_config", ""), configFileName);
			
		}

		public PaumEngine() {
			super("Paum", "Paum_config.xml");
		}
		
		/*
		@Override
		public boolean getClearOutputAsBeforeTesting() {
			return false;
		}
		*/

		@Override
		public List<PRSetup> getTrainControllerSetup(MLEngineConfig config) throws MalformedURLException
		{
			List<PRSetup> prs = new ArrayList<PRSetup>();

			//Paum train
			prs.add(new PRSetup.SinglePRSetup(LearningAPIMain.class, getNameTrain())
				.putFeature("inputASName", config.inputAS)
				.putFeature("outputASName", config.outputAS)
				.putFeature("configFileURL", getConfigURL(config.experimentLearningConfigsDirectory))
				.putFeature("learningMode", "TRAINING"));
			
			return prs;
		}

		@Override
		public List<PRSetup> getTestControllerSetup(MLEngineConfig config) throws MalformedURLException
		{
			List<PRSetup> prs = new ArrayList<PRSetup>();
			
			//delete mentions
			/*
			prs.add(new PRSetup.SinglePRSetup(AnnotationDeletePR.class, "delete mentions")
				.putFeatureList("annotationTypes", config.learnigAnnotationType)
				.putFeatureList("setsToRemove", config.outputAS));
			/**/						


			//Paum Application
			prs.add(new PRSetup.SinglePRSetup(LearningAPIMain.class, getNameTest())
				.putFeature("configFileURL", getConfigURL(config.experimentLearningConfigsDirectory))
				.putFeature("inputASName", config.inputAS)
				.putFeature("outputASName", config.outputAS)
				.putFeature("learningMode", "APPLICATION"));

			return prs;
		}

		@Override
		public String getDefaultLearningAnnotationType() {
			return "Mention"+getDefaultOutputAS();
		}
		
	}

	public static class ILPEngine extends MLEngine
	{

		public ILPEngine(String configFileName)
		{			
//			super("ILP", configFileName);
			super(configFileName.substring(0, configFileName.indexOf('.')), configFileName);
			
		}

		public ILPEngine() {
			this("ILP_config.xml");
		}

		@Override
		public List<PRSetup> getTrainControllerSetup(MLEngineConfig config) throws MalformedURLException
		{
			List<PRSetup> prs = new ArrayList<PRSetup>();
						
			prs.add(new PRSetup.SinglePRSetup(MachineLearningPR.class, getNameTrain())
				.putFeature("inputASName", config.inputAS)
				.putFeature("configFileURL", getConfigURL(config.experimentLearningConfigsDirectory))
				.putFeature("training", true));
			
			return prs;

		}

		@Override
		public List<PRSetup> getTestControllerSetup(MLEngineConfig config)	throws MalformedURLException
		{
			List<PRSetup> prs = new ArrayList<PRSetup>();

			//ILP Apply
			prs.add(new PRSetup.SinglePRSetup(MachineLearningPR.class, getNameTest())
				.putFeature("configFileURL", getConfigURL(config.experimentLearningConfigsDirectory))
				.putFeature("inputASName", config.inputAS)
				.putFeature("training", false));
			
			if (! config.inputAS.equals(config.outputAS))
			{
				prs.add(new PRSetup.SinglePRSetup(AnnotationSetTransfer.class)
					.putFeature("inputASName", config.inputAS)
					.putFeature("outputASName", config.outputAS)
					.putFeature("copyAnnotations", false)
					.putFeatureList("annotationTypes", config.learnigAnnotationType));
			}
			
			return prs;
		}
		
	}
}
