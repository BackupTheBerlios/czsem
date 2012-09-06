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
			Object treexRet = serverConnection.analyzeText(doc.getContent().toString());
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