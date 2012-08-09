package czsem.gate.plugins;

import gate.Document;
import gate.Gate;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.metadata.CreoleResource;
import gate.persist.PersistenceException;
import gate.util.GateException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import czsem.gate.GateUtils;
import czsem.gate.learning.DataSet;
import czsem.gate.learning.DataSet.DataSetImpl.Acquisitions;
import czsem.gate.learning.DataSet.DataSetReduce;
import czsem.gate.learning.PRSetup;
import czsem.gate.utils.Config;

@CreoleResource(name = "czsem CustomPR", comment = "Usable within GETE embeded only.")
public class CustomPR extends AbstractLanguageAnalyser {
	private static final long serialVersionUID = -1412485629846167332L;
	protected AnalyzeDocDelegate delegate;
	private DataSet ds;
	private List<PRSetup> preprocess = new ArrayList<PRSetup>();
	
	public interface AnalyzeDocDelegate
	{
		public void analyzeDoc(Document doc);
	}
	
	@Override
	public void execute() throws ExecutionException
	{
		delegate.analyzeDoc(getDocument());				
	}

	public void executeAnalysis(DataSet data) throws PersistenceException, ResourceInstantiationException, ExecutionException
	{
		
		
		SerialAnalyserController a = PRSetup.buildGatePipeline(preprocess, "CustomPR pipeline"); 
		
		a.add(this);
		
		a.setCorpus(data.getCorpus());
		a.execute();
	}
    
	public static CustomPR createInstance(AnalyzeDocDelegate delegate) throws ResourceInstantiationException {
		CustomPR ret = (CustomPR) new PRSetup.SinglePRSetup(CustomPR.class).createPR();
		ret.delegate = delegate;
		return ret;		
	}

	public static void main(String [] args) throws URISyntaxException, IOException, GateException
    {
		System.err.println("ups");
		
	    Config.getConfig().setGateHome();
	    Gate.init();

	    GateUtils.registerCzsemPlugin();
		
		CustomPR pr = CustomPR.createInstance(
				new AnalyzeDocDelegate()
				{					
					@Override
					public void analyzeDoc(Document doc) {
						System.err.println(doc.getName());						
					}
				});
		
		pr.executeAnalysis(new DataSetReduce(new Acquisitions(), 0.1));
		
		System.err.println("spu");
    	
    }

	public void setDataset(DataSet ds) {
		this.ds = ds;
	}

	public void executeAnalysis() throws PersistenceException, ResourceInstantiationException, ExecutionException {
		executeAnalysis(ds);
	}

	public void setPreprocess(PRSetup ... preprocess) {
		setPreprocessList(Arrays.asList(preprocess));
	}

	public void setPreprocessList(List<PRSetup> preprocess) {
		this.preprocess = preprocess;
	}

	public DataSet getDataset() {
		return ds;
	}



}
