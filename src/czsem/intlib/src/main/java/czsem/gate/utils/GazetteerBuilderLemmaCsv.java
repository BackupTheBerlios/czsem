package czsem.gate.utils;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.corpora.DocumentContentImpl;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.InvalidOffsetException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import com.csvreader.CsvReader;

import czsem.gate.learning.PRSetup;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.plugins.TreexLocalAnalyser;

public class GazetteerBuilderLemmaCsv {
	
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
		
	}

	public static class	LemmatizationHandler {
		private Document doc;
		
		public void prepareDocument(InputHandler inputHandler, int culumnIndex) throws IOException, InvalidOffsetException, ResourceInstantiationException {
			
			doc = Factory.newDocument("");
			AnnotationSet as = doc.getAnnotations("InputSentences");
			long docEnd = 0;
			while (inputHandler.readRecord()) {
				String r = inputHandler.getColumnValue(culumnIndex);
				long rLength = r.length();
				
				doc.edit(docEnd, docEnd+rLength+1, new DocumentContentImpl(r + "\n"));
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

	public static void main(String[] args) {
		

	}

}
