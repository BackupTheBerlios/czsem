package czsem.gate.utils;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.csvreader.CsvReader;

import czsem.Utils.StopRequestDetector;
import czsem.utils.ProjectSetup;

public class SpcAnalysis {

	private static ConditionalSerialAnalyserController controller;
	private static SpcInteractionExport ie;
	private static Corpus corpus;


	public static void initController() throws PersistenceException, ResourceInstantiationException, IOException {
		GateUtils.deleteAllPublicGateResources();

		
		controller = (ConditionalSerialAnalyserController) PersistenceManager
				.loadObjectFromFile(new File("C:/Users/dedek/Desktop/DATLOWE/gate_apps/all.gapp"));
		
		controller.add(ie.getPR());

		corpus = Factory.newCorpus("SpcCorp");
		controller.setCorpus(corpus);
	}

	
	public static void main(String[] args) throws Exception {
		
		GateUtils.initGate();

		ie = new SpcInteractionExport("SpcInteractionExport/SpcInteractionExport_"+ProjectSetup.makeTimeStamp()+".csv");

		/**/
		
		initController();

		/**/
		

		CsvReader in = new CsvReader("C:/Users/dedek/Desktop/DATLOWE/LP_SPC.csv", ';', Charset.forName("cp1250"));
		in.readHeaders();
		
		StopRequestDetector srd = new StopRequestDetector();
		srd.startDetector();
		
		int num = 0;
		
		while (! srd.stop_requested) {
			if (! in.readRecord()) break;
			
			String spcCode = in.get(0);
			ie.setSpcCode(spcCode);

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
			corpus.add(gateDoc);
			
			try {
				controller.execute();
			} catch (ExecutionException e) {
				System.err.println("----------------------- EXECUTION INTERUPTED -------------------");
				e.printStackTrace();
				initController();
				System.err.println("----------------------- EXECUTION RESTARTED -------------------");
			}
			
			corpus.clear();
			//GateUtils.saveGateDocumentToXML(gateDoc, "C:/Users/dedek/Desktop/DATLOWE/SPC_ananlyzed/"+spcCode+".xml");
			Factory.deleteResource(gateDoc);
				
		}
		
		in.close();
		ie.close();
		
		
		GateUtils.deleteAllPublicGateResources();
	}
}
