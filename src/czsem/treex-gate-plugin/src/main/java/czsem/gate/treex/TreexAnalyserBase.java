package czsem.gate.treex;

import gate.Document;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.util.List;

import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;
import czsem.gate.utils.Config;

@SuppressWarnings("serial")
public abstract class TreexAnalyserBase extends AbstractLanguageAnalyserWithInputOutputAS {

	
	@Optional
	@RunTime
	@Override
	@CreoleParameter(comment="Annotation set name from which sentences and tokens will be exported for Treex.", defaultValue="treex input AS")
	public void setInputASName(String inputASName) {
		super.setInputASName(inputASName);
	}

	protected String languageCode;
	protected List<String> scenarioSetup;
	protected TreexServerConnection serverConnection = null;

	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		String logPath = "<path is not available>";
		try {
			logPath = Config.getConfig().getLogFileDirectoryPath() + "/TREEX_err.log";

			TreexInputDocPrepare ip = new TreexInputDocPrepare(doc, getInputASName());
			Object treexRet = serverConnection.analyzePreprocessedDoc(doc.getContent().toString(), ip.createInputDocData());
			TreexReturnAnalysis tra = new TreexReturnAnalysis(treexRet);
			tra.annotate(doc, getOutputASName());
		} catch (Exception e) {
			throw new ExecutionException("Error occured during run of Treex server.\nSee remote server's output, or local server's log file: " + logPath, e);
		}
	}

	public List<String> getScenarioSetup() {
		return scenarioSetup;
	}

	public String getLanguageCode() {
		return languageCode;
	}

}