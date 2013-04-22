package czsem.gate.utils;

import java.io.PrintStream;

import gate.AnnotationSet;
import gate.Document;
import cuni.mff.intlib.HistogramBuilderTreeNGram;
import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryData;
import czsem.gate.plugins.CustomPR;
import czsem.gate.plugins.CustomPR.AnalyzeDocDelegate;


public class SpcHistogramBuilder {

	public static void buildHistogram(String queryString, String fileName) throws Exception {
		PrintStream out = new PrintStream(fileName, "utf8");
		buildHistogram(queryString, out);
		out.close();
	}

	public static void buildHistogram(String queryString, PrintStream output) throws Exception {
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
		
		hb.printSorted(output, "\n");
	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		GateUtils.registerComponentIfNot(CustomPR.class);

//		HistogramBuilder.buildHistogram(SpcGazetteerCollector.getSpcCorpus(false).get(0).getAnnotations("Treex"));
		
		buildHistogram("[gram/sempos~=^n\\..*]([])", "histograms/nouns_single_branch_1.txt");

		buildHistogram("[gram/sempos~=^n\\..*]([]([]))", "histograms/nouns_single_branch_2.txt");

		buildHistogram("[gram/sempos~=^n\\..*]([]([]([])))", "histograms/nouns_single_branch_3.txt");
		
		buildHistogram("[gram/sempos~=^n\\..*]([_subtree_eval_depth=2])", "histograms/nouns_subtree_2.txt");

		buildHistogram("[t_lemma=být]([])", "histograms/byt_single_branch_1.txt");

		buildHistogram("[gram/sempos=v]", "histograms/verbs.txt");
		
		/*
		
		String queryString = "[t_lemma=tableta]([_subtree_eval_depth=3])";
//		String queryString = "[t_lemma=tableta]([]([]))";

		/**/
	}

}
