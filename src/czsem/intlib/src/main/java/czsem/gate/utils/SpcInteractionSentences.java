package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.creole.ConditionalSerialAnalyserController;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class SpcInteractionSentences {
	
	public static String stripDocName (String docName) {
		int underPos = docName.lastIndexOf('_');
		if (underPos == -1) return docName;
		return docName.substring(0, underPos);
	}

	public static void main(String[] args) throws Exception {
		
		GateUtils.initGate();
		
		ConditionalSerialAnalyserController controller = (ConditionalSerialAnalyserController) PersistenceManager
				.loadObjectFromFile(new File("C:/Users/dedek/Desktop/DATLOWE/gate_apps/segment_only.gapp"));
		
		Corpus corpus = Factory.newCorpus("SpcCorp");
		controller.setCorpus(corpus);



		
		CsvReader r = new CsvReader("SpcInteractionExport/SpcInteractionExport_20130426_175800.csv", ',', Charset.forName("utf8"));
		
		r.readHeaders();
		
		CsvWriter wr = new CsvWriter("SpcInteractionExport/SpcInteractionExport_20130426_175800_sentences.csv", '|', Charset.forName("utf8"));
		
		wr.writeRecord(new String [] {"spc_code", "doc_name", "sentence_id", "sentence_start_index","sentence_end_index", "sentence_text"});
		
		String lastDoc = "";
		String lastSpc = "";
		
		Document gateDoc = Factory.newDocument("");
		
		AnnotationSet sentences = null;

		Map<Annotation, Integer> sentence2origid = new HashMap<Annotation, Integer>();
		
		while (r.readRecord()) {
			// spc_code,doc_name,found_uri,label,dictionary,sentence_id,start_index,end_index
			
			String curDoc = stripDocName(r.get(1));
			String curSpc = r.get(0);
			
			if (! lastDoc.equals(curDoc))
			{
				//read new doc 
				
				lastDoc = curDoc;
				System.err.println(stripDocName(curDoc));
				System.err.flush();
				
				corpus.clear();
				Factory.deleteResource(gateDoc);
				
				File file = new File("C:/Users/dedek/Desktop/DATLOWE/SPC_all/"+curDoc);
				gateDoc = Factory.newDocument(file.toURI().toURL(), "utf8");
				corpus.add(gateDoc);
				
				controller.execute();
				
				sentences = gateDoc.getAnnotations("seg").get("Sentence");
			}
			
			if (! lastSpc.equals(curSpc)) {
				lastSpc = curSpc;
				sentence2origid.clear();				
			}

			
			Long startOffset = Long.parseLong(r.get(6));
			Long endOffset = Long.parseLong(r.get(7));;
			Annotation s = sentences.getCovering("Sentence", startOffset, endOffset).iterator().next();
			
			
			Integer sentenceID = Integer.parseInt(r.get(5));
			
			Integer prevSentId = sentence2origid.get(s);
			
			if (prevSentId == null) {
				sentence2origid.put(s, sentenceID);
//				System.err.println("SSS: "+s.getFeatures().get("sentence"));
				wr.writeRecord(new String [] {
						r.get(0), 
						curDoc, 
						sentenceID.toString(), 
						s.getStartNode().getOffset().toString(), 
						s.getEndNode().getOffset().toString(),
						s.getFeatures().get("sentence").toString()});
			} else {
				if (! prevSentId.equals(sentenceID)) throw new RuntimeException("not equls ");				
			}
			
// 			System.err.println("T: "+Utils.stringFor(gateDoc, startOffset, endOffset));
			
			
			
			
			
		}
		
		r.close();
		wr.close();

	}

}
