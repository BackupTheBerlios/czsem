package czsem.gate.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.csvreader.CsvReader;

import czsem.utils.MultiSet;

public class SpcDb {
	
	Map<String, SpcRecord> mapByCode = new HashMap<String, SpcDb.SpcRecord>();
	Map<String, SpcRecord> mapBySelectedDoc = new HashMap<String, SpcRecord>();

	public static class SpcRecord {
		Set<String> spcCode = new HashSet<String>();
		String spcName;
		String spcSupp;
		String doc;
		String pdf;
		String selectedDoc;
		boolean docIsMissing;
		boolean pdfIsMissing;

		
		public SpcRecord(String spcCode, String spcName, String spcSupp,
				String doc, String pdf,
				boolean docIsMissing, boolean pdfIsMissing) {
			this.spcCode.add(spcCode);
			this.spcName = spcName;
			this.spcSupp = spcSupp;
			this.doc = doc;
			this.pdf = pdf;
			this.docIsMissing = docIsMissing;
			this.pdfIsMissing = pdfIsMissing;
			
			this.selectedDoc = doc;
			
			if (docIsMissing) {
				this.selectedDoc = pdf;				
				if (pdfIsMissing) {
					this.selectedDoc = null;				
				}
			}

		}


		public void mergeWith(SpcRecord record) {
			if (selectedDoc != record.selectedDoc)
			{
				if (selectedDoc == null || ! selectedDoc.equals(record.selectedDoc)) {
					throw new RuntimeException(
						String.format("Cannot merge records %s %s\n slected docs don't match %s %s",
								spcCode, record.spcCode, selectedDoc, record.selectedDoc));
				}
			}
			
			
			if (! spcCode.addAll(record.spcCode)) {
				throw new RuntimeException(
						String.format("Cannot merge records %s %s\n slected docs don't match %s %s",
								spcCode, record.spcCode, selectedDoc, record.selectedDoc));
			}
			
		}
	}

	public static SpcDb loadSpcDb() throws IOException {
		return loadSpcDb(
				"C:/Users/dedek/Desktop/DATLOWE/LP_SPC.csv",
				"C:/Users/dedek/Desktop/DATLOWE/SPC_all/");
	}

	public static SpcDb loadSpcDb(String spcCsvFileName, String spcAllDirectory) throws IOException {
		CsvReader in = new CsvReader(spcCsvFileName, ';', Charset.forName("cp1250"));
		in.readHeaders();

		SpcDb db = new SpcDb();
		db.buildFromCsvAndDocsDir(in, new File(spcAllDirectory));

		in.close();
		
		return db;
	}

	public static void main(String[] args) throws Exception {
		
		SpcDb db = loadSpcDb();
		
		MultiSet<Integer> docsByCode = new MultiSet<Integer>();
		
		for (Entry<String, SpcRecord> e : db.mapBySelectedDoc.entrySet()) {
			docsByCode.add(e.getValue().spcCode.size());
		}
		
		docsByCode.printSorted(System.err, "\n");
		System.err.println("----");
		
		for (int dbc : docsByCode) {
			int cnt = docsByCode.get(dbc);
			
			if (cnt == 1) {
				for (Entry<String, SpcRecord> e : db.mapBySelectedDoc.entrySet()) {
					if (e.getValue().spcCode.size() == dbc) {
						System.err.format("%d %s %s \n", dbc, e.getKey(), e.getValue().spcCode);
						break;
					}
				}
			}
			
		}

		System.err.println("----");
		System.err.format("docs: %d    null docs: %d \n", 
				db.mapBySelectedDoc.size(), 
				db.mapBySelectedDoc.get(null).spcCode.size(),
				db.mapBySelectedDoc.size() - 
				db.mapBySelectedDoc.get(null).spcCode.size());

	}

	public void buildFromCsvAndDocsDir(CsvReader in, File docsDir) throws IOException {

		while (in.readRecord()) {
			
			String spcCode = in.get(0);

			String doc = in.get(3);
			String pdf = in.get(4);
			
/*			String fileStr = pdf;
			File file = null;
			
			if (! fileStr.isEmpty()) {
				file = new File(docsDir, fileStr);
				if (! file.exists()) fileStr = "";
			}

			if (fileStr.isEmpty()) {
				fileStr = doc;
			}

			if (! fileStr.isEmpty()) {
				file = new File(docsDir, fileStr);
				if (! file.exists()) fileStr = "";
			}
			
			System.err.format("%4d %s record: %s %s %s\n", ++num, ProjectSetup.makeTimeStamp(), spcCode, in.get(1), fileStr);
			if (fileStr.isEmpty()) {
				System.err.format("WARNING ommiting record: %s %s\n", in.get(0), in.get(1));
			}
*/			
			SpcRecord record = new SpcRecord(spcCode, in.get(1), in.get(2), doc, pdf,
					doc.isEmpty() || ! new File(docsDir, doc).exists(),
					pdf.isEmpty() || ! new File(docsDir, pdf).exists());
			
			add(record);
			
			/*
			Document gateDoc = Factory.newDocument(file.toURI().toURL(), "utf8");
			gateDoc.getFeatures().put("spcCode", spcCode);
			gateDoc.getFeatures().put("spcName", in.get(1));
			gateDoc.getFeatures().put("spcSupp", in.get(2));
			//corpus.add(gateDoc);
			
			//GateUtils.saveGateDocumentToXML(gateDoc, "C:/Users/dedek/Desktop/DATLOWE/SPC_ananlyzed/"+spcCode+".xml");
			Factory.deleteResource(gateDoc);
				*/
		}
		
		
	}

	public void add(SpcRecord record) {
		String code = record.spcCode.iterator().next();
		if (mapByCode.containsKey(code)) {
			throw new RuntimeException("SPC code already known " + code);
		}
		
		SpcRecord old = mapBySelectedDoc.get(record.selectedDoc);
		
		if (old == null) {
			mapBySelectedDoc.put(record.selectedDoc, record);
			old = record;
		} else {
			old.mergeWith(record);			
		}
		
		mapByCode.put(code, old);
		
	}

}
