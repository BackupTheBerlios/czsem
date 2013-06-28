package czsem.gate.utils;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.creole.ConditionalSerialAnalyserController;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.Utils.StopRequestDetector;
import czsem.gate.utils.SpcDb.SpcRecord;

public class SpcAnalysisPersistent {
	
	static Logger logger = LoggerFactory.getLogger(SpcAnalysisPersistent.class);
	
	protected String inputDir;
	protected String errorDir;
	protected String gateApplicationFile;
	
	protected SpcDb spcDb;
	
	protected DataStoreWrapper dsWrapper;
	protected ConditionalSerialAnalyserController controller;
	
	protected Corpus corpus;


	public static void main(String[] args) throws Exception {
		GateUtils.initGate(Level.INFO);

		
		SpcAnalysisPersistent a = new SpcAnalysisPersistent();
		
		
		try {

			a.init();
			
			a.run();
			
		} finally {
			
			a.close();
			GateUtils.deleteAllPublicGateResources();
		}
		
	}

	public void run() throws ResourceInstantiationException, PersistenceException, IOException, SecurityException {
		StopRequestDetector srd = new StopRequestDetector();
		
		srd.addShutdownHook();
		srd.startDetector();
		
		int num = 0;
		
		Iterator<String> fileNameIterator = spcDb.mapBySelectedDoc.keySet().iterator();
		
		try {
		
			while (! srd.stop_requested && fileNameIterator.hasNext()) {
				
				String fileName = fileNameIterator.next();
				SpcRecord record = spcDb.mapBySelectedDoc.get(fileName);
				num++;
				
				if (
						dsWrapper.containsGateDocument(fileName) ||
						new File(errorDir, fileName+".xml").exists()
				) continue;
		
				logger.info("index\t{}, name: {}, spcCodes: {}", new Object [] {num, fileName, record.spcCode});
				
				processSingleFile(fileName, record);
				
			}
			
		} finally {		
			srd.terminate();
		}

		
	}

	public void init() throws IOException, GateException, URISyntaxException {
		SpcAnalysisConfig config = SpcAnalysisConfig.getConfig();
		
		inputDir = config.getSpcAllDirectory();
		logger.info("Loading SPC DB, file {} dir {}", config.getSpcCsvFileName(), inputDir);
		
		errorDir = config.getErrorFilesDirectory();
		new File(errorDir).mkdirs();
		logger.info("Using errorDir {}", errorDir);

		spcDb = SpcDb.loadSpcDb(config.getSpcCsvFileName(), config.getSpcAllDirectory());
		spcDb.mapBySelectedDoc.remove(null);

		
		logger.info("Openning data store, file {}", config.getDataStoreDir());
		dsWrapper = new DataStoreWrapper(config.getDataStoreDir());
		dsWrapper.openOrCreate();
		

		gateApplicationFile = config.getGateApplicationFile();
		logger.info("loading gate application, file {}", gateApplicationFile);
		initController();
	}
	
	public void close() throws PersistenceException {
		dsWrapper.close();
	}
	
	public void initController() throws PersistenceException, ResourceInstantiationException, IOException {
		GateUtils.deleteAllPublicGateResources();

		
		controller = (ConditionalSerialAnalyserController) PersistenceManager
				.loadObjectFromFile(new File(gateApplicationFile));
		
		//controller.add(ie.getPR());

		corpus = Factory.newCorpus("SpcCorp");
		controller.setCorpus(corpus);
	}

	
	public void processSingleFile(String filename, SpcRecord record) throws ResourceInstantiationException, IOException, PersistenceException, SecurityException {
		File file = new File(inputDir, filename);
		
		Document gateDoc = null;
		
		boolean shouldRestartOnException = false;
		
		try {
			shouldRestartOnException = false;
			gateDoc = Factory.newDocument(file.toURI().toURL(), "utf8");
			
			gateDoc.setName(filename);
			gateDoc.getFeatures().put("spcCode", record.spcCode);
			gateDoc.getFeatures().put("spcName", record.spcName);
			gateDoc.getFeatures().put("spcSupp", record.spcSupp);

			corpus.add(gateDoc);

			shouldRestartOnException = true;
			controller.execute();

			shouldRestartOnException = false;
			dsWrapper.persistDoc(gateDoc);
			
		} catch (GateException e) {
			
			if (gateDoc == null)
				gateDoc = Factory.newDocument(ExceptionUtils.getStackTrace(e));
			
			GateUtils.saveGateDocumentToXML(gateDoc, new File(errorDir, filename+".xml").getPath());
			
			if (shouldRestartOnException) {			
				logger.warn("----------------------- EXECUTION INTERUPTED -------------------", e);
				initController();
				logger.warn("----------------------- EXECUTION RESTARTED -------------------");
			}
		}
		
		corpus.clear();
		
		
		Factory.deleteResource(gateDoc);

	}

}
