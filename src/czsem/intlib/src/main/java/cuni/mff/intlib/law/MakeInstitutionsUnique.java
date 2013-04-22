package cuni.mff.intlib.law;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Gate;
import gate.Utils;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

import czsem.gate.learning.PRSetup;
import czsem.gate.plugins.CustomPR;
import czsem.gate.plugins.LevenshteinWholeLineMatchingGazetteer;
import czsem.gate.utils.GateUtils;
import czsem.utils.MultiSet;

public class MakeInstitutionsUnique {

	private static PrintStream lemmas_out;

	public static void main3(String[] args) throws Exception {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("inst_lemmas.txt"), "utf8"));
		
		for (;;) {
			String ln = in.readLine();
			if (ln == null) break;
			lemmaSet.add(normalizeLemmaInst(ln));
		}
		
		in.close();
		
		printSet(lemmaSet);

		
	}

	public static String normalizeLemmaInst(String lemmaIsntStr) {
		String ret = lemmaIsntStr.trim();
		
		/**/
		ret = ret.replace("-", " ");
		ret = ret.replace(",", " ");
		ret = ret.replace(" v ", " ");
		ret = ret.replace(" ČR", " ");
		ret = ret.replace(" Spolkový republika Německo", " ");
		ret = ret.replace(" Slovenský republika", " ");
		ret = ret.replace(" Český republika", " ");
		ret = ret.replace(" SR", " ");
		ret = ret.replace(" SSR", " ");
		ret = ret.replaceAll(" ČSR", " ");
		ret = ret.replace(" ČSSR", " ");
		ret = ret.replace("ÍNejvyšší", "Nejvyšší");
		ret = ret.replace("Ústavný súdu", "Ústavní soud");
		ret = ret.replace("Rychnov nad Kněžna", "Rychnov");
		ret = ret.replaceFirst("^Vysoký$", "Vysoký soud");		
		ret = ret.replaceFirst("^Krajský$", "Krajský soud");		
		ret = ret.replace("Rozsudek", "");
		ret = ret.replace(" pro ", " ");
		ret = ret.replace("Vary", "Var");
		ret = ret.replace("Frýdku", "Frýdek");
		ret = ret.replace("soda", "soud");
		ret = ret.replace("Okresného súdu Galant", "Okresní soud Galanta");
		ret = ret.replaceFirst("Fond ohrožený dítě.*", "Fond ohrožený dítě");
		ret = ret.replace("odvolací", "odvolat");
		ret = ret.replace("soud odvolat", "odvolat soud");
		ret = ret.replace("soud dovolací", "Dovolací soud");
		ret = ret.replace("Králová", "Králové");
		ret = ret.replace("MS Praha", "Městský soud Praha");
		ret = ret.replace("Praga", "Praha");
		ret = ret.replace("Evropský soud lidský právo Štrasburk", "Evropský soud lidský právo");
		ret = ret.replaceFirst(" v$", "");
		ret = ret.replace("soud krajský", "Krajský soud");
		
		ret = ret.replace("soud krajský", "Krajský soud");
		
		
		ret = LevenshteinWholeLineMatchingGazetteer.removeRedundantSpaces(ret);
		ret = ret.toUpperCase();
		
		/**/
		return ret.trim();
	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(CustomPR.class);
		
		Corpus corpus = GateUtils.loadCorpusFormDatastore(
				GateUtils.openDataStore("file:/C:/data/law/cross-validation_2013-03-28/gate_store"),
					"all___1365091864514___8479");
		

//		CzechLawDataSet dataset = new CzechLawDataSet(null);
//		Corpus corpus = dataset.getTestCorpus();
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
		
		lemmas_out = new PrintStream("inst_lemmas", "utf8");
		p.execute();
		
		lemmas_out.close();

		
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
			System.err.format("%2d '%s'\t\t\t\t%10d\n", a++, s, set.get(s));
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
					sb.append(t.getFeatures().get("clean_lemma"));
					sb.append(' ');
				}				
				lemmaSet.add(sb.toString());
				//lemmas_out.println(sb.toString());
				
				i.getFeatures().put("normalized", normalizeLemmaInst(sb.toString()));
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
