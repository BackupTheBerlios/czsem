package czsem.gate.utils;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.Utils;
import gate.corpora.DocumentContentImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

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

		public String getHeader(int columnIndex) throws IOException {
			return reader.getHeader(columnIndex).toLowerCase().replace(' ', '_');
		}
		
	}

	public static class	LemmatizationHandler {
		private Document doc;
		private AnnotationSet tocs = null;
		
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
			
			initTocs();
		};

		public void save(String filename) throws IOException {
			GateUtils.saveGateDocumentToXML(doc, filename);
		};

		public void load(URL sourceUrl) throws ResourceInstantiationException {
			doc = Factory.newDocument(sourceUrl);
			initTocs();
		};
		
		protected void initTocs() {
			tocs = doc.getAnnotations().get("Token");
		}
		
				
		public String getLemma(int id) {
			Annotation sentence = doc.getAnnotations("InputSentences").get(id);
			List<Annotation> ordTocs = Utils.inDocumentOrder(
				tocs.getContained(
						sentence.getStartNode().getOffset(),
						sentence.getEndNode().getOffset()));
			StringBuilder sb = new StringBuilder();
			
			for (Annotation toc : ordTocs) {
				sb.append(toc.getFeatures().get("lemma"));
				sb.append(' ');
			}
			
			return sb.toString().substring(0, sb.length()-1);			
		}
	}

	private String fileName;
	private char inputDelimiter;
	private char outputDelimiter = '|';
	private String charsetName;
	private int gazetteerCulumnIndex;
	private int backupCulumnIndex;
	private LemmatizationHandler lh;
	
	protected InputHandler initInputHandler() throws IOException {
		InputHandler ih = new InputHandler(fileName, inputDelimiter, charsetName);
		ih.readHeader();
		return ih;
	}
	
	protected void buildLemmatizatiuon() throws InvalidOffsetException, ResourceInstantiationException, IOException, ExecutionException {		
		InputHandler ih = initInputHandler();
		
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
	

	public void printWithCheck(PrintWriter out, String string) {
		if (string.contains(""+outputDelimiter)) 
			throw new IllegalArgumentException(
					String.format("Gazetteer value '%s' cannot contain delimiter '%c'", string, outputDelimiter));
		out.print(string);
	}
	
	public void buildGazetter(String output_file_name, int ... columns) throws IOException {
		PrintWriter out = new PrintWriter(
				new OutputStreamWriter(new BufferedOutputStream(
						new FileOutputStream(output_file_name)), "utf8"));
	
		InputHandler inputHandler = initInputHandler();
		
		int id = 0;
		
		while (inputHandler.readRecord()) {
			
			printWithCheck(out, lh.getLemma(id++));
			
			for (int c=0; c < columns.length; c++)
			{				
				String value = inputHandler.getColumnValue(columns[c]);
				if (value == null || value.isEmpty()) continue; 

				out.print(outputDelimiter);
				printWithCheck(out, inputHandler.getHeader(columns[c]));
				out.print('=');
				printWithCheck(out, value);
			}

			out.println();
		}

	
		out.println();
		out.close();
	}

	public GazetteerBuilderLemmaCsv(String fileName, char delimiter, String charsetName, int gazetteerCulumnIndex) {
		this.fileName = fileName;
		this.inputDelimiter = delimiter;
		this.charsetName = charsetName;
		this.gazetteerCulumnIndex = gazetteerCulumnIndex;
	}

	public int getBackupCulumnIndex() {
		return backupCulumnIndex;
	}

	public void setBackupCulumnIndex(int backupCulumnIndex) {
		this.backupCulumnIndex = backupCulumnIndex;
	}



	public void buildGazetter(int ... columns) throws IOException {
		buildGazetter(fileName.replaceFirst("\\.[^\\.]*$", ".lst"), columns);
	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox(Level.INFO);
		
		//MainFrame.getInstance().setVisible(true);
		
		Gate.getCreoleRegister().registerComponent(TreexLocalAnalyser.class);
		
		GazetteerBuilderLemmaCsv gb = new GazetteerBuilderLemmaCsv(
				"C:\\Users\\dedek\\Desktop\\DATLOWE\\gazetteer\\CIS_UCLAT.csv",
				';', "cp1250", 3);
		
		gb.setBackupCulumnIndex(1);
		
		gb.initLemmatizatiuon();

		gb.buildGazetter(0, 1, 2, 3);	
	}

}
