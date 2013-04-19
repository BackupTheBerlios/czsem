package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.creole.ResourceInstantiationException;
import cuni.mff.intlib.HistogramBuilder;
import cuni.mff.intlib.HistogramBuilderTreeNGram;
import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryData;
import czsem.gate.plugins.CustomPR;
import czsem.gate.plugins.CustomPR.AnalyzeDocDelegate;


public class SpcHistogramBuilder {

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		GateUtils.registerComponentIfNot(CustomPR.class);

//		HistogramBuilder.buildHistogram(SpcGazetteerCollector.getSpcCorpus(false).get(0).getAnnotations("Treex"));

		
		String queryString = "[t_lemma=tableta]([_subtree_eval=true])";
//		String queryString = "[t_lemma=tableta]([]([]))";
		final HistogramBuilderTreeNGram hb = new HistogramBuilderTreeNGram(FSQuery.buildQuery(queryString), "t_lemma", 3);
		
		CustomPR pr = CustomPR.createInstance(new AnalyzeDocDelegate() {
			
			@Override
			public void analyzeDoc(Document doc) {
				System.err.println(doc.getName());
				/**/
				AnnotationSet tocs = doc.getAnnotations("Treex");
				AnnotationSet deps = doc.getAnnotations("Treex").get("tDependency");
				GateAwareTreeIndex index = new GateAwareTreeIndex();
				index.addDependecies(deps);
				QueryData data = new QueryData(index, new GateAnnotationsNodeAttributes(tocs));
				hb.add(data);
				
				
				/*
				AnnotationSet anns = doc.getAnnotations("Treex").get("t-node");
				
				for (Annotation a : anns) {
					FeatureMap fm = a.getFeatures();
					if ("v".equals(fm.get("gram/sempos"))) {
						hb.add(fm.get("t_lemma"));
					}
				}
				/**/
			}
		});
		
		pr.executeAnalysis(SpcGazetteerCollector.getSpcCorpus(false));
		
		hb.printSorted(System.err, "\n");

	}

}
