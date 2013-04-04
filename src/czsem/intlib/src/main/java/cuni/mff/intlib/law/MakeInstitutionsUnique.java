package cuni.mff.intlib.law;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Gate;
import gate.Utils;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

import czsem.gate.learning.PRSetup;
import czsem.gate.learning.experiments.CzechLawLearningExperiment.CzechLawDataSet;
import czsem.gate.plugins.CustomPR;
import czsem.gate.utils.GateUtils;
import czsem.utils.MultiSet;

public class MakeInstitutionsUnique {

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(CustomPR.class);
		
		CzechLawDataSet dataset = new CzechLawDataSet(null);
		Corpus corpus = dataset.getTestCorpus();
//		Corpus corpus = dataset.getCorpus();
		
		PRSetup [] prs = {
				new PRSetup.SinglePRSetup(CustomPR.class)
				.putFeature("executionDelegate", new CustomPR.AnalyzeDocDelegate() {

					@Override
					public void analyzeDoc(Document doc) {
						makeInstitutionsUnique(doc);
						
					}
					
				})
		};
		
		SerialAnalyserController p = PRSetup.buildGatePipeline(prs, "export");
		p.setCorpus(corpus);
		p.execute();
		
		System.err.println("--- forms ---");
		printSet(formSet);
		System.err.println("--- lemma ---");
		printSet(lemmaSet);
		System.err.println("--- regexp ---");
		printSet(regexpSet);
	}
	
	public static void printSet(MultiSet<String> set) {
		ArrayList<String> sort = new ArrayList<String>(set.size());
		Iterables.addAll(sort, set);
		
		Collections.sort(sort);
		
		int a=1;
		for (String s : sort) {
			System.err.format("%2d '%-60s'%10d\n", a++, s, set.get(s));
		}
		System.err.println(set.size());
	}

	static MultiSet<String> formSet = new MultiSet<String>();
	static MultiSet<String> lemmaSet = new MultiSet<String>();
	static MultiSet<String> regexpSet = new MultiSet<String>();

	protected static void makeInstitutionsUnique(Document doc) {
		System.err.println("starting doc: " + doc.getName());
		
		AnnotationSet tocs = doc.getAnnotations("Treex").get("Token");

		AnnotationSet insts = doc.getAnnotations("Key").get("Instituce");
		
		
		for (Annotation i : insts) {
			try {
				String cont = GateUtils.getAnnotationContent(i, doc);
				formSet.add(cont);
				regexpSet.add(cont
						.replaceAll("([Kk]rajsk|[Mm]ěstsk)(?:ého|ému|ém|ým)", "$1ý")
						.replaceAll("([Nn]ejvyšší|[úÚ]stavní|[Oo]kresní|[Oo]bvodní)(?:[^\\s]*)", "$1")
						.replaceAll("(soud)(?:[^\\s]*)", "$1")
						);

				List<Annotation> ord = Utils.inDocumentOrder(tocs.getContained(i.getStartNode().getOffset(), i.getEndNode().getOffset()));
				StringBuilder sb = new StringBuilder();
				for (Annotation t : ord) { 
					sb.append(t.getFeatures().get("lemma"));
					sb.append(' ');
				}				
				lemmaSet.add(sb.toString());
			} catch (InvalidOffsetException e) {
				throw new RuntimeException(e);
			}
		}
/*			
		for (int t = 0; t < types.length; t++) {
			AnnotationSet anns = insts.get(types[t]);
			for (Annotation a : anns) {
				AnnotationSet contained = tocs.getContained(a.getStartNode().getOffset(), a.getEndNode().getOffset());
				for (Annotation toc: contained){
					insts.add(toc.getStartNode(), toc.getEndNode(), types[t]+"Token", Factory.newFeatureMap());
				}
			}
		}
		/**/			
	}
}
