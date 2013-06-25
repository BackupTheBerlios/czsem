package czsem.gate.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import czsem.Utils;
import czsem.Utils.Evidence;
import czsem.gate.utils.SpcDb.SpcRecord;

public class SpcExperiment {

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();

		Corpus origExpCorp = GateUtils.loadCorpusFormDatastore(
				GateUtils.openDataStore("file:/C:/Users/dedek/Desktop/DATLOWE/DATLOWE_gate_store/"),
				"experiment___1367568738829___3631");
		
		Set<String> origFiles = new HashSet<String>();
		
		for (Document d : origExpCorp) {
			origFiles.add(SpcInteractionSentences.stripDocName(d.getName()));
			Factory.deleteResource(d);			
		}

		
		
		SpcDb db = SpcDb.loadSpcDb();
		
		System.err.println(db.mapBySelectedDoc.get(null));

		db.mapBySelectedDoc.remove(null);
		
		for (String s : origFiles) {
			db.mapBySelectedDoc.remove(s);			
		}


		System.err.println(db.mapBySelectedDoc.get(null));
		
		Evidence<String>[] perm = Utils.createRandomPermutation(db.mapBySelectedDoc.keySet());
		
		for (int i=0; i<150; i++) {
			System.err.format("%3d %s\n", i, perm[i].element);
			if (origFiles.contains(perm[i].element)) throw new Exception(perm[i].element + " in train!");
				
		}

/*
		Corpus corpus = Factory.newCorpus("aa");
/**/
		Corpus corpus = GateUtils.loadCorpusFormDatastore(
				GateUtils.openDataStore("file:/C:/Users/dedek/Desktop/DATLOWE/DATLOWE_gate_store/"),
				"test___1368542833051___1701");
/**/		

		for (int i=0; i<150; i++) {
			File file = new File("C:/Users/dedek/Desktop/DATLOWE/SPC_all/" + perm[i].element);

			
			Document gateDoc = Factory.newDocument(file.toURI().toURL(), "utf8");
			System.err.println(gateDoc.getName());
			
			SpcRecord record = db.mapBySelectedDoc.get(perm[i].element);
			gateDoc.getFeatures().put("spcCode", record.spcCode);
			gateDoc.getFeatures().put("spcName", record.spcName);
			gateDoc.getFeatures().put("spcSupp", record.spcSupp);
			corpus.add(gateDoc);
		}
		
		corpus.sync();


		
		/*

		CsvReader in = new CsvReader("C:/Users/dedek/Desktop/DATLOWE/LP_SPC.csv", ';', Charset.forName("cp1250"));
		in.readHeaders();
		
		int num = 0;
		
		Corpus corpus = Factory.newCorpus("aa");
		
		while (in.readRecord()) {
			
			String spcCode = in.get(0);

			String doc = in.get(3);
			String pdf = in.get(4);
			
			String fileStr = pdf;
			File file = null;
			
			if (! fileStr.isEmpty()) {
				file = new File("C:/Users/dedek/Desktop/DATLOWE/SPC_all/" + fileStr);
				if (! file.exists()) fileStr = "";
			}

			if (fileStr.isEmpty()) {
				fileStr = doc;
			}

			if (! fileStr.isEmpty()) {
				file = new File("C:/Users/dedek/Desktop/DATLOWE/SPC_all/" + fileStr);
				if (! file.exists()) fileStr = "";
			}
			
			System.err.format("%4d %s record: %s %s %s\n", ++num, ProjectSetup.makeTimeStamp(), spcCode, in.get(1), fileStr);
			if (fileStr.isEmpty()) {
				System.err.format("WARNING ommiting record: %s %s\n",in.get(0), in.get(1));
				continue;
			}
			
			Document gateDoc = Factory.newDocument(file.toURI().toURL(), "utf8");
			gateDoc.getFeatures().put("spcCode", spcCode);
			gateDoc.getFeatures().put("spcName", in.get(1));
			gateDoc.getFeatures().put("spcSupp", in.get(2));
			//corpus.add(gateDoc);
			
			//GateUtils.saveGateDocumentToXML(gateDoc, "C:/Users/dedek/Desktop/DATLOWE/SPC_ananlyzed/"+spcCode+".xml");
			Factory.deleteResource(gateDoc);
				
		}
		
		in.close();
		
		
		GateUtils.deleteAllPublicGateResources();
		/**/
	}

}
