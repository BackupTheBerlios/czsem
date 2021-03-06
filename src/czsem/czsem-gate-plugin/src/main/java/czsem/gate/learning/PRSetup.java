package czsem.gate.learning;

import java.util.Arrays;
import java.util.List;

import czsem.gate.plugins.LearningEvaluator;

import gate.Factory;
import gate.FeatureMap;
import gate.ProcessingResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;

public abstract class PRSetup
{
	public static class SinglePRSetup extends PRSetup 
	{
		private String pr_className;
		private FeatureMap fm;
		private String name = null;
	
		public SinglePRSetup(String className, String name)
		{
			pr_className = className;
			fm = Factory.newFeatureMap();
			this.name = name;
			
		}

		public SinglePRSetup(Class<?> cl, String name)
		{
			this(cl.getCanonicalName(), name);			
		}

		public SinglePRSetup(Class<?> cl)
		{
			this(cl, null);
		}

		public SinglePRSetup(String classneme)
		{
			this(classneme, null);
		}
				
		public SinglePRSetup putFeature(Object key, Object value)
		{
			fm.put(key, value);
			return this;
		}
		public SinglePRSetup putFeatureList(Object key, String ... strig_list)
		{
			if (strig_list == null)
				fm.put(key, null);
			else
				fm.put(key, Arrays.asList(strig_list));
			return this;
		}
	
		public ProcessingResource createPR() throws ResourceInstantiationException
		{
			return(ProcessingResource) Factory.createResource(pr_className, fm, null, name);			
		}				
	}
	
	public static class MLEvaluateSetup extends SinglePRSetup
	{

		private List<LearningEvaluator> evaluation_register;

		public MLEvaluateSetup(List<LearningEvaluator> evaluation_register)
		{
			super(LearningEvaluator.class);
			this.evaluation_register = evaluation_register;
		}

		@Override
		public ProcessingResource createPR() throws ResourceInstantiationException
		{
			ProcessingResource ret = super.createPR();
			evaluation_register.add((LearningEvaluator) ret);
			return ret;
		}

	}


	public abstract ProcessingResource createPR() throws ResourceInstantiationException;

	public static SerialAnalyserController buildGatePipeline(PRSetup [] prs, String name) throws ResourceInstantiationException	{
		return buildGatePipeline(Arrays.asList(prs), name);
	}

	public static SerialAnalyserController buildGatePipeline(List<PRSetup> prs, String name) throws ResourceInstantiationException
	{
		SerialAnalyserController controller = (SerialAnalyserController)	    	   
			Factory.createResource(SerialAnalyserController.class.getCanonicalName());
		
		controller.setName(name);
	
		
		for (int i = 0; i < prs.size(); i++)
		{
			controller.add(prs.get(i).createPR());			
		}
				
		return controller;		
	}		
}