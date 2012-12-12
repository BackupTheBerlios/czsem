package czsem.gate.utils;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.corpora.DocumentContentImpl;
import gate.creole.SerialAnalyserController;
import gate.creole.dumpingPR.DumpingPR;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.util.Arrays;

import czsem.gate.learning.PRSetup;

public class HtmlExport {

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		Gate.getCreoleRegister().registerComponent(DumpingPR.class);
		
		//GateUtils.registerPluginDirectory("Tools");
		
		
		String fileName = "../intlib/documents/ucto.gate.xml";
		String outputDir = "target/export";
		String asName = "tmt2";
		String [] annotationTypes = {"Sentence"};
		String[] colorNames = {"red"};
		
		new File(outputDir).mkdirs();
		
		System.err.println("reading doc: " + fileName);
		Document doc = Factory.newDocument(new File(fileName).toURI().toURL(), "utf8");
		System.err.println("reading finished");
		
		addExportHeader(doc, annotationTypes, colorNames);
		
		
		PRSetup[] setup = new PRSetup [] {
				new PRSetup.SinglePRSetup(DumpingPR.class)
				.putFeature("includeFeatures", true)
				.putFeature("useStandOffXML", false)
				.putFeature("useSuffixForDumpFiles", true)
				.putFeature("suffixForDumpFiles", ".html")
				.putFeature("outputDirectoryUrl", new File(outputDir).toURI().toURL())
				.putFeature("annotationSetName", asName)
				.putFeatureList("annotationTypes", annotationTypes)
				.putFeatureList("dumpTypes")
				
				
		};
		
		SerialAnalyserController pipeline = PRSetup.buildGatePipeline(Arrays.asList(setup), "export pipeline");
		
		Corpus c = Factory.newCorpus("export");
		c.add(doc);		
		pipeline.setCorpus(c);
		pipeline.execute();
	}

	public static void addExportHeader(Document doc, String[] annotationTypes, String[] colorNames) throws InvalidOffsetException {
		String prefix ="\n\n\n"+"body	{white-space: pre-wrap;}\n";
		
		StringBuilder sb = new StringBuilder(prefix);
		for (int i = 0; i < annotationTypes.length; i++) {
			sb.append(annotationTypes[i]);
			sb.append("	{background: ");
			sb.append(colorNames[i]);
			sb.append(";}\n");
		}
		sb.append("\n");
		
		String docPrefix = sb.toString();
		DocumentContent replacement = new DocumentContentImpl(docPrefix);
		doc.edit(0L, 0L, replacement);
		
		AnnotationSet markupAs = doc.getAnnotations("Original markups");
		
		FeatureMap f = Factory.newFeatureMap();
		markupAs.add(0L, doc.getContent().size(), "html", f);
		markupAs.add(1L, (long) docPrefix.length(), "head", f);
		markupAs.add(2L, docPrefix.length()-1L, "style", f);
		markupAs.add((long) docPrefix.length(), doc.getContent().size(), "body", f);
		
	}

}

