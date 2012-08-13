package czsem.gate.learning;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Gate;
import gate.Utils;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import czsem.gate.GateUtils;
import czsem.gate.learning.DataSet.DataSetImpl.Acquisitions;
import czsem.gate.plugins.AnnotationDependencyRootMarker;
import czsem.gate.plugins.CustomPR;
import czsem.gate.plugins.CustomPR.AnalyzeDocDelegate;
import czsem.gate.utils.Config;

public class AcqDlramtStatisitcs implements AnalyzeDocDelegate {
	private CustomPR pr;

	public void exec() throws ResourceInstantiationException, URISyntaxException, IOException, PersistenceException, ExecutionException
	{
		pr = CustomPR.createInstance(this);		
//		DataSet acq = new Acquisitions();
		DataSet acq = new DataSet.DataSetReduce(new Acquisitions(), 1.0);
		pr.setDataset(acq);
		pr.setPreprocess(
				new PRSetup.SinglePRSetup(AnnotationDependencyRootMarker.class)
				.putFeature("inputASName", acq.getKeyAS())
				.putFeature("tokensAndDependenciesASName", acq.getTectoMTAS())
				.putFeature("outputASName", acq.getTectoMTAS())
				.putFeatureList("inputAnnotationTypeNames", "dlramt"));
		pr.executeAnalysis();		
	}
	
	public static void main(String [] args) throws URISyntaxException, IOException, GateException
    {
		System.err.println("ups");
		
	    Config.getConfig().setGateHome();
	    Gate.init();

	    GateUtils.registerCzsemPlugin();
		
	    AcqDlramtStatisitcs ads = new AcqDlramtStatisitcs();
	    ads.exec();
		
		System.err.println("spu");
    	
    }

	int count = 0;
	
	@Override
	public void analyzeDoc(Document doc) {
		System.err.format("-------%03d--------------------------------------------------------%s\n", count++, doc.getName());
		AnnotationSet dls = doc.getAnnotations(pr.getDataset().getKeyAS()).get("dlramt");
		AnnotationSet roots = doc.getAnnotations(pr.getDataset().getTectoMTAS()).get("dlramt_root");
		AnnotationSet tocs = doc.getAnnotations(pr.getDataset().getTectoMTAS()).get("Token");
		for (Annotation a : dls)
		{
			AnnotationSet cont = tocs.getContained(a.getStartNode().getOffset(),
					a.getEndNode().getOffset());
			
			List<Annotation> ordered = Utils.inDocumentOrder(cont);
			
			for (Annotation oa : ordered)
			{
				FeatureMap fm = oa.getFeatures();
				System.err.format("%s ", fm.get("form"));	
			}

			System.err.println(cont.size());
		}
		for (Annotation r : roots)
		{
			Integer root_id = (Integer) r.getFeatures().get("rootID");
			FeatureMap fm = tocs.get(root_id).getFeatures();
			System.err.format("root: %s\n", fm.get("form"));	
		}
	}
}
