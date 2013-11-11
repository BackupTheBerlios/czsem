package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.csvreader.CsvWriter;

import czsem.gate.learning.PRSetup;
import czsem.gate.plugins.CustomPR;

public class SpcInteractionExport {

	private CsvWriter out;
	private FeatureMap sectionFM;
	private String gazetteerAsName = "Gazetteer";
	private String lastSpcCode = "?";
	
	public SpcInteractionExport(String outputFileName) throws IOException {
		sectionFM = Factory.newFeatureMap();
		sectionFM.put("heading_number", "4.5");

		out = new CsvWriter(outputFileName, ',', Charset.forName("utf8"));
		out.writeRecord(new String [] {"spc_code", "doc_name", "found_uri", "label", "dictionary", "sentence_id", "start_index", "end_index"});
	}

	public void close() {
		out.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(CustomPR.class);
		
		Corpus corpus = GateUtils.loadCorpusFormDatastore(
				GateUtils.openDataStore("file:/C:/Users/dedek/Desktop/DATLOWE/DATLOWE_gate_store"),
					"all___1365490380628___9025");
		
		SerialAnalyserController p = (SerialAnalyserController)	    	   
				Factory.createResource(SerialAnalyserController.class.getCanonicalName());
		
		SpcInteractionExport ie = new SpcInteractionExport("SpcInteractionExport.csv");
		
		p.add(ie.getPR());

		
		p.setCorpus(corpus);
		
		p.execute();
		
		ie.close();
	}

	public ProcessingResource getPR() throws ResourceInstantiationException {
		return new PRSetup.SinglePRSetup(CustomPR.class)
		.putFeature("executionDelegate", new CustomPR.AnalyzeDocDelegate() {

			@Override
			public void analyzeDoc(Document doc) throws IOException {
				exportDoc(doc);
			}
			
		}).createPR();
	}

	protected void exportDoc(Document doc) throws IOException {
		System.err.println("analysing doc: " + doc.getName());
		
		Iterator<Annotation> i = doc.getAnnotations("Sections").get("Section", sectionFM).iterator();
		if (! i.hasNext()) return;
		
		Annotation section = i.next();
		
		AnnotationSet containedLookups = doc.getAnnotations(gazetteerAsName).get("Lookup").getContained(
				section.getStartNode().getOffset(),
				section.getEndNode().getOffset());
		
		for (Annotation l : containedLookups) {
			FeatureMap fm = l.getFeatures();
			
			out.write(lastSpcCode);
			out.write(doc.getName());
			out.write((String) fm.get("uri"));
			out.write((String) fm.get("label"));
			out.write((String) fm.get("majorType"));
			
			Iterator<Annotation> si = doc.getAnnotations("Treex").getCovering("Sentence",
					l.getStartNode().getOffset(),
					l.getEndNode().getOffset()).iterator();
			if (si.hasNext()) {
				out.write(si.next().getId().toString());				
			}
			
			out.write(l.getStartNode().getOffset().toString());
			out.write(l.getEndNode().getOffset().toString());
			out.endRecord();
		}
		
		
	}

	public void setSpcCode(String spcCode) {
		lastSpcCode = spcCode;		
	}

}
