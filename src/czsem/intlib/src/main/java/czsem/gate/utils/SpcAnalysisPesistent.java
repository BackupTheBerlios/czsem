package czsem.gate.utils;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.Utils.StopRequestDetector;
import czsem.gate.utils.SpcDb.SpcRecord;

public class SpcAnalysisPesistent {
	
	static Logger logger = LoggerFactory.getLogger(SpcAnalysisPesistent.class);
	
	protected String inputDir;
	protected String errorDir;
	protected String gateApplicationFile;
	
	protected SpcDb spcDb;
	
	protected DataStoreWrapper dsWrapper;
	protected ConditionalSerialAnalyserController controller;
	
	protected Corpus corpus;


	public static void main(String[] args) throws Exception {
		GateUtils.initGate(Level.ALL);

		
		SpcAnalysisPesistent a = new SpcAnalysisPesistent();
		
		a.init();
		
		a.run();
	}

	public void run() throws ResourceInstantiationException, PersistenceException, IOException {
		StopRequestDetector srd = new StopRequestDetector();
		
		srd.addShutdownHook();
		srd.startDetector();
		
		int num = 0;
		
		Iterator<String> fileNameIterator = spcDb.mapBySelectedDoc.keySet().iterator();
		
		while (! srd.stop_requested && fileNameIterator.hasNext()) {
			
			String fileName = fileNameIterator.next();
			SpcRecord record = spcDb.mapBySelectedDoc.get(fileName);
			num++;
			
			if (
					dsWrapper.containsFile(fileName) ||
					new File(errorDir, fileName).exists()) continue;

			logger.info("index\t{}, name: {}, spcCodes: {}", new Object [] {num, fileName, record.spcCode});
			
			processSingleFile(fileName, record);
			
		}
		
		srd.terminate();

		
	}

	public void init() throws IOException, GateException, URISyntaxException {
		SpcAnalysisConfig config = SpcAnalysisConfig.getConfig();
		
		inputDir = config.getSpcAllDirectory();
		errorDir = config.getErrorFilesDirectory();
		logger.info("Loading SPC DB, file {} dir {}", config.getSpcCsvFileName(), inputDir);
				
		spcDb = SpcDb.loadSpcDb(config.getSpcCsvFileName(), config.getSpcAllDirectory());
		spcDb.mapBySelectedDoc.remove(null);

		
		logger.info("Openning data store, file {}", config.getDataStoreDir());
		dsWrapper = new DataStoreWrapper(config.getDataStoreDir());
		

		gateApplicationFile = config.getGateApplicationFile();
		logger.info("loading gate application, file {}", gateApplicationFile);
		//initController();
	}
	
	public void initController() throws PersistenceException, ResourceInstantiationException, IOException {
		GateUtils.deleteAllPublicGateResources();

		
		controller = (ConditionalSerialAnalyserController) PersistenceManager
				.loadObjectFromFile(new File(gateApplicationFile));
		
		//controller.add(ie.getPR());

		corpus = Factory.newCorpus("SpcCorp");
		controller.setCorpus(corpus);
	}

	
	public void processSingleFile(String filename, SpcRecord record) throws ResourceInstantiationException, PersistenceException, IOException {
		File file = new File(inputDir, filename);
		Document gateDoc = Factory.newDocument(file.toURI().toURL(), "utf8");
		
		gateDoc.setName(filename);
		gateDoc.getFeatures().put("spcCode", record.spcCode);
		gateDoc.getFeatures().put("spcName", record.spcName);
		gateDoc.getFeatures().put("spcSupp", record.spcSupp);

		corpus.add(gateDoc);
		
		try {
			controller.execute();
		} catch (ExecutionException e) {
			GateUtils.saveGateDocumentToXML(gateDoc, new File(errorDir, filename).getPath());
			
			logger.warn("----------------------- EXECUTION INTERUPTED -------------------");
			e.printStackTrace();
			initController();
			logger.warn("----------------------- EXECUTION RESTARTED -------------------");
		}
		
		corpus.clear();
		
		dsWrapper.persistDoc(gateDoc);
		
		Factory.deleteResource(gateDoc);

	}

}
