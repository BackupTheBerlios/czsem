package cuni.mff.intlib;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import czsem.fs.FSFileWriter;
import czsem.fs.FSSentenceStringBuilder;
import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.gate.applet.GateApplet;
import czsem.gate.plugins.NetgraphTreeViewer;
import czsem.gate.utils.GateAwareTreeIndex;
import czsem.gate.utils.GateUtils;
import czsem.gui.TreeVisualizeFrame;
import czsem.utils.MultiSet;

public class HistogramBuilder {

	
	
	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		
		
		String fileName = "documents/ucto.gate.xml";

		/**/
		System.err.println("reading doc: " + fileName);
		Document doc = Factory.newDocument(new File(fileName).toURI().toURL(), "utf8");
		System.err.println("reading finished");
		
		showTree(doc);
		
		//BuildHistogram(doc);
		
		//FsExport(doc);
		/**/
		
		//viewAnnot(fileName);

	}

	public static void showTree(Document doc) throws InterruptedException, SyntaxError {
		GateAwareTreeIndex index = new GateAwareTreeIndex();
		
		System.err.println("fillnig index");
		long time = System.currentTimeMillis();
		
		AnnotationSet mainAs = doc.getAnnotations("tmt4");
		index.addDependecies(mainAs.get("tDependency"));			
		
		/*
		AnnotationSet ss = mainAs.get("Sentence");
		for (Annotation s : ss) {
			AnnotationSet cont = mainAs.get("tDependency").getContained(
					s.getStartNode().getOffset(), 
					s.getEndNode().getOffset());
			
			index.addDependecies(cont);			
		}
		*/

		System.err.format("fillnig index finished in: %10.3fs\n", (System.currentTimeMillis() - time) * 0.001);
		
		FSQuery q = new FSQuery();
		q.setIndex(index);
		q.setNodeAttributes(new GateAnnotationsNodeAttributes(mainAs));
		
		/**/
		
		for (QueryMatch result : q.buildQuery("[t_lemma=být]").evaluate())
		{
			NodeMatch res = result.getMatchingNodes().iterator().next();
			System.err.println(res);
			Annotation ra = mainAs.get(res.getNodeId());
			
			FSSentenceStringBuilder fssb = new FSSentenceStringBuilder(ra, mainAs);
			TreeVisualizeFrame.showTreeAndWait(fssb.getAttributes(), fssb.getTree(), res.getNodeId());		

		}
		
		/*
		
		Iterator<Annotation> i = ss.iterator();
		
		for (int a=0; a<32; a++)
		{
			i.next();
		}
		
		Annotation s = i.next();
		
		AnnotationSet sas = doc.getAnnotations("tmt4").getContained(
				s.getStartNode().getOffset(), 
				s.getEndNode().getOffset());
		

		AnnotationSet sas = null;
		FSSentenceStringBuilder fssb = new FSSentenceStringBuilder(sas);
		TreeVisualize.showTreeAndWait(fssb.getAttributes(), fssb.getTree());		
		*/
	}

	public static void viewAnnot(String fileName) throws Exception {
		Gate.getCreoleRegister().registerComponent(NetgraphTreeViewer.class);
		GateApplet.showWithDocument(new File(fileName).toURI().toURL(), "tmt2", null);
	}

	public static void BuildHistogram(Document doc) throws ResourceInstantiationException, MalformedURLException {
		System.err.println(doc.getAnnotationSetNames());
		
		BuildHistogram(doc.getAnnotations("tmt4").get("tToken"));
		
	}

	public static void FsExport(Document doc) throws FileNotFoundException, UnsupportedEncodingException {
		FSFileWriter fsw = new FSFileWriter("documents/ucto.fs");
		fsw.PrintAll(doc.getAnnotations("tmt4"));
		fsw.close();
	}

	public static void BuildHistogram(AnnotationSet annotationSet) {
		System.err.println(annotationSet.getAllTypes());
		
		System.err.println("annotations: " + annotationSet.size());
		
		MultiSet<String> features = new MultiSet<String>();
		MultiSet<String> sempos = new MultiSet<String>();
		MultiSet<String> verbs = new MultiSet<String>();
		
		for (Annotation a : annotationSet) {
			
			FeatureMap feats = a.getFeatures();
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Iterable<String> keys = (Iterable) feats.keySet();
			
			features.addAll(keys);
			
			sempos.add((String) feats.get("sempos"));
			if ("v".equals(feats.get("sempos")))
			{
				verbs.add((String) feats.get("t_lemma"));
			}
		}
		
		features.print(System.err);
		System.err.println();
		sempos.print(System.err);
		System.err.println();
		verbs.printSorted(System.err, "\n");
		
	}

}
