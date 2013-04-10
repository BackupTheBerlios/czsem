package czsem.gate.utils;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.corpora.DocumentContentImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

import czsem.Utils;
import czsem.gate.learning.PRSetup;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.plugins.TreexLocalAnalyser;

public class GazetteerBuilderLemmaCsv {
	
	static Logger logger = Logger.getLogger(GazetteerBuilderLemmaCsv.class);
	
	public static class	InputHandler {
		private CsvReader reader;

		public InputHandler(CsvReader reader) {
			this.reader = reader;
		}

		public InputHandler(String fileName, char delimiter, String charsetName) throws FileNotFoundException {			
			this(new CsvReader(fileName, delimiter, Charset.forName(charsetName)));
		}
		
		public boolean readHeader() throws IOException {
			return reader.readHeaders();
		}
		
		public boolean readRecord() throws IOException {
			return reader.readRecord();
		}

		public String getColumnValue(int columnIndex) throws IOException {
			return reader.get(columnIndex);
		}

		public void close() {
			reader.close();
		}
		
	}

	public static class	LemmatizationHandler {
		private Document doc;
		
		public void prepareDocument(InputHandler inputHandler, int culumnIndex, int backupCulumnIndex) 
				throws IOException, InvalidOffsetException, ResourceInstantiationException {
			
			doc = Factory.newDocument("");
			AnnotationSet as = doc.getAnnotations("InputSentences");
			long docEnd = 0;
			while (inputHandler.readRecord()) {
				String r = inputHandler.getColumnValue(culumnIndex);
				if (r == null || r.isEmpty()) 
					r = inputHandler.getColumnValue(backupCulumnIndex);
				
				long rLength = r.length();
				
				doc.edit(docEnd, docEnd, new DocumentContentImpl(r + "\n"));
				as.add(docEnd, docEnd+rLength, "Sentence", Factory.newFeatureMap());
				docEnd+= rLength+1;				
			}
		};
		
		public void analyze() throws ResourceInstantiationException, ExecutionException {
			PRSetup[] prs =  {
					new SinglePRSetup(TreexLocalAnalyser.class)
						.putFeatureList("scenarioSetup", 
								"W2A::CS::Tokenize",
								"W2A::CS::TagFeaturama lemmatize=1",
								"W2A::CS::FixMorphoErrors")
						.putFeature("verifyOnInit", false)
						.putFeature("inputASName", "InputSentences")
						.putFeature("showTreexLogInConsole", true),
			};
			
			SerialAnalyserController pipe = PRSetup.buildGatePipeline(prs, "lemmatization");
			
			Corpus c = Factory.newCorpus("lemmatization corpus");
			c.add(doc);
			pipe.setCorpus(c);
			pipe.execute();
			
			GateUtils.deepDeleteController(pipe);
		};

		public void save(String filename) throws IOException {
			GateUtils.saveGateDocumentToXML(doc, filename);
		};

		public void load(URL sourceUrl) throws ResourceInstantiationException {
			doc = Factory.newDocument(sourceUrl);			
		};
		
		public String getLemma() {
			return null;			
		}
	}

	private String fileName;
	private char delimiter;
	private String charsetName;
	private int gazetteerCulumnIndex;
	private int backupCulumnIndex;
	private LemmatizationHandler lh;
	
	protected void buildLemmatizatiuon() throws InvalidOffsetException, ResourceInstantiationException, IOException, ExecutionException {
		InputHandler ih = new InputHandler(fileName, delimiter, charsetName);
		ih.readHeader();
		
		lh = new LemmatizationHandler();
		lh.prepareDocument(ih, gazetteerCulumnIndex, backupCulumnIndex);
		ih.close();
		lh.analyze();
	}
	
	public void initLemmatizatiuon() throws ResourceInstantiationException, InvalidOffsetException, ExecutionException, IOException {
		File gateFile = new File(fileName + ".gate.xml");
		
		if (gateFile.exists()) {
			logger.info(String.format("Lemmatization file '%s' exists, trying to use it.", gateFile.toString()));
			
			lh = new LemmatizationHandler();
			lh.load(gateFile.toURI().toURL());			
		} else {
			logger.info(String.format("Lemmatization file '%s' does not exist, building new one.", gateFile.toString()));

			buildLemmatizatiuon();

			logger.info(String.format("Trying to save lemmatization file '%s'.", gateFile.toString()));
			lh.save(gateFile.getAbsolutePath());
			
			logger.info(String.format("Lemmatization file '%s' saved.", gateFile.toString()));
		}
		
	}
	

	public GazetteerBuilderLemmaCsv(String fileName, char delimiter, String charsetName, int gazetteerCulumnIndex) {
		this.fileName = fileName;
		this.delimiter = delimiter;
		this.charsetName = charsetName;
		this.gazetteerCulumnIndex = gazetteerCulumnIndex;
	}

	public int getBackupCulumnIndex() {
		return backupCulumnIndex;
	}

	public void setBackupCulumnIndex(int backupCulumnIndex) {
		this.backupCulumnIndex = backupCulumnIndex;
	}



	public static void main(String[] args) throws Exception {
		Utils.main(null);
		
		Config.getConfig().setGateHome();
		Gate.init();
		
		//MainFrame.getInstance().setVisible(true);
		
		Gate.getCreoleRegister().registerComponent(TreexLocalAnalyser.class);
		
		GazetteerBuilderLemmaCsv gb = new GazetteerBuilderLemmaCsv(
				"C:\\Users\\dedek\\Desktop\\DATLOWE\\gazetteer\\CIS_UCLAT.csv",
				';', "cp1250", 3);
		
		gb.setBackupCulumnIndex(1);
		
		gb.initLemmatizatiuon();
	
	}

}
