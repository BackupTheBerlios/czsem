package czsem.gate.treex;

import gate.Document;
import gate.creole.ExecutionException;

import java.util.List;

import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;

@SuppressWarnings("serial")
public abstract class TreexAnalyserBase extends AbstractLanguageAnalyserWithInputOutputAS {

	protected String languageCode;
	protected List<String> scenarioSetup;
	protected TreexServerConnection serverConnection = null;

	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		try {
			TreexInputDocPrepare ip = new TreexInputDocPrepare(doc, getInputASName());
			Object treexRet = serverConnection.analyzePreprocessedDoc(doc.getContent().toString(), ip.createInputDocData());
			TreexReturnAnalysis tra = new TreexReturnAnalysis(treexRet);
			tra.annotate(doc, getOutputASName());
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	public List<String> getScenarioSetup() {
		return scenarioSetup;
	}

	public String getLanguageCode() {
		return languageCode;
	}

}