package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.persist.PersistenceException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvWriter;

import czsem.Utils;
import czsem.utils.ProjectSetup;

public class SpcInteractionExportAndSentences {
	
	static Logger logger = LoggerFactory.getLogger(SpcInteractionExportAndSentences.class);

	private DataStoreWrapper dsWrapper;

	private String outputDir = ".";
	
	private CsvWriter outSpc;
	private CsvWriter outSentences;

	private FeatureMap sectionFM;
	
	private String gazetteerAsName = "Gazetteer";
	
	private Set<Integer> processedSentenceIds = new HashSet<Integer>();

	
	public SpcInteractionExportAndSentences() {
		sectionFM = Factory.newFeatureMap();
		sectionFM.put("heading_number", "4.5");
	}


	public static void main(String[] args) throws Exception {
		GateUtils.initGate(Level.INFO);
		
		SpcInteractionExportAndSentences export = new SpcInteractionExportAndSentences();
		if (args.length > 0) export.setOutputDir(args[0]);
		export.init();

		export.run();
		
		export.close();
	}

	public void run() throws IOException {
		
		int index = 0;
		for (Document d : dsWrapper.iterateAllDocuments()) {
			index++;
			logger.info("before doc index: {} doc name: {}", index, d.getName());
			
			processSingleDocument(d);
			
			Factory.deleteResource(d);
		}
		
	}

	public void close() {
		outSpc.close();
		outSentences.close();
	}

	public void init() throws PersistenceException, IOException {
		SpcAnalysisConfig config = SpcAnalysisConfig.getConfig();
		
		logger.info("Openning data store, file {}", config.getDataStoreDir());
		dsWrapper = new DataStoreWrapper(config.getDataStoreDir());
		dsWrapper.openExisting();

		logger.info("Writing output files to directory '{}'", getOutputDir());
		
		String stamp = ProjectSetup.makeTimeStamp();
		String outSpcName = "SpcInteraction_"+stamp+"_Export.csv";
		logger.info("SpcInteractionExport file: {}", outSpcName);
		
		outSpc = new CsvWriter(getOutputDir()+"/"+outSpcName, '|', Charset.forName("utf8"));
		outSpc.writeRecord(new String [] {"doc_name", "spc_codes", "firstSpcName", "found_uri", "label", "dictionary", "sentence_id", "start_index", "end_index"});

				
		String outSentencesName = "SpcInteraction_"+stamp+"_Sentences.csv";
		logger.info("SpcInteractionSentences file: {}", outSentencesName);

		outSentences = new CsvWriter(getOutputDir()+"/"+outSentencesName, '|', Charset.forName("utf8"));
		outSentences.writeRecord(new String [] {"doc_name", "sentence_id", "sentence_start_index","sentence_end_index", "sentence_text"});
		
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	protected void processSingleDocument(Document doc) throws IOException {
		FeatureMap docFeatures = doc.getFeatures();
		
		processedSentenceIds.clear();
		
		Iterator<Annotation> i = doc.getAnnotations("Sections").get("Section", sectionFM).iterator();
		if (! i.hasNext()) return;
		
		Annotation section = i.next();
		
		AnnotationSet containedLookups = doc.getAnnotations(gazetteerAsName).get("Lookup").getContained(
				section.getStartNode().getOffset(),
				section.getEndNode().getOffset());
		
		for (Annotation l : containedLookups) {
			FeatureMap fm = l.getFeatures();
			
			outSpc.write(doc.getName());

			@SuppressWarnings("unchecked")
			Collection<String> codes = (Collection<String>) docFeatures.get("spcCode");
			
			outSpc.write(
					Utils.collectionToString(codes, ':'));

			outSpc.write(docFeatures.get("spcName").toString());

			outSpc.write((String) fm.get("uri"));
			outSpc.write((String) fm.get("label"));
			outSpc.write((String) fm.get("majorType"));
			
			Iterator<Annotation> si = doc.getAnnotations("Treex").getCovering("Sentence",
					l.getStartNode().getOffset(),
					l.getEndNode().getOffset()).iterator();
			if (si.hasNext()) {
				Annotation s = si.next();
				outSpc.write(s.getId().toString());
				exportSentence(doc.getName(), s);
			}
			
			outSpc.write(l.getStartNode().getOffset().toString());
			outSpc.write(l.getEndNode().getOffset().toString());
			outSpc.endRecord();
		}
	}


	protected void exportSentence(String docName, Annotation s) throws IOException {
		
		Integer sentenceID = s.getId();
		
		if (! processedSentenceIds.add(sentenceID)) return ;

		outSentences.writeRecord(new String [] {
				docName, 
				sentenceID.toString(), 
				s.getStartNode().getOffset().toString(), 
				s.getEndNode().getOffset().toString(),
				s.getFeatures().get("sentence").toString()});

	}

}
