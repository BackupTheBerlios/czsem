package cuni.mff.intlib.brat;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.creole.AbstractProcessingResource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.ExtensionFileFilter;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import czsem.Utils;

@CreoleResource
@SuppressWarnings("serial")
public class BratImporter extends AbstractProcessingResource {
	
	private Corpus outputCorpus;
	private URL inputDirectory;

	@CreoleParameter
	@RunTime
	public void setOutputCorpus(Corpus outputCorpus) {
		this.outputCorpus = outputCorpus;
	}

	public Corpus getOutputCorpus() {
		return outputCorpus;
	}

	@CreoleParameter
	@RunTime
	public void setInputDirectory(URL inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	public URL getInputDirectory() {
		return inputDirectory;
	}
	
	
	protected static void importDirectory(File inputDirectory, Corpus corpus) throws ResourceInstantiationException, IOException, NumberFormatException, InvalidOffsetException
	{
		File[] annFiles = inputDirectory.listFiles(new ExtensionFileFilter(null, "ann"));
		for (File annFile : annFiles) {
			corpus.add(importSingleDocument(annFile, 
					new File(annFile.getAbsolutePath().replaceFirst("\\.ann$", "\\.txt"))));
		}
	}

	protected static Document importSingleDocument(File annFile, File txtFile) throws ResourceInstantiationException, IOException, NumberFormatException, InvalidOffsetException {
		Document doc = Factory.newDocument(txtFile.toURI().toURL(), "utf8");
		annotateBratDocument(doc, annFile);
		return doc;
	}

	protected static void annotateBratDocument(Document doc, File annFile) throws IOException, NumberFormatException, InvalidOffsetException {
		BratDocumentAnnotator ba = new BratDocumentAnnotator(doc, annFile);
		ba.annotate();
	}

	@Override
	public void execute() throws ExecutionException {
		try {
		
			importDirectory(Utils.URLToFile(getInputDirectory()), getOutputCorpus());
		
		} catch (Exception e) {
			throw new ExecutionException(e); 
		}
	}; 

}
