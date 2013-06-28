package czsem.gate.treex;

import gate.Document;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

import java.util.List;

import org.apache.xmlrpc.XmlRpcException;

import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;
import czsem.gate.utils.Config;
import czsem.utils.AbstractConfig.ConfigLoadException;

@SuppressWarnings("serial")
public abstract class TreexAnalyserBase extends AbstractLanguageAnalyserWithInputOutputAS {
	protected String languageCode;
	protected List<String> scenarioSetup;
	protected TreexServerConnection serverConnection = null;
	private boolean verifyOnInit = false;

	
	@Optional
	@RunTime
	@Override
	@CreoleParameter(comment="Annotation set name from which sentences and tokens will be exported for Treex.", defaultValue="treex input AS")
	public void setInputASName(String inputASName) {
		super.setInputASName(inputASName);
	}

	
	public static String constructLogPathPrefix(String handshakeCode) {
		String prefix; 
		try {
			prefix = Config.getConfig().getLogFileDirectoryPathExisting() + "/";
		} catch (ConfigLoadException e) {
			prefix = ""; 
		}
		
		return prefix + "TREEX_" + handshakeCode+"_";
	}

	
	public String getLogPath() {
		return constructLogPathPrefix(getHandshakeCode())+"err.log";
	}

	protected String getHandshakeCode() {
		try {
			return serverConnection.handshake();
		} catch (XmlRpcException e) {
			return "#default";
		}
	}


	@Override
	public void execute() throws ExecutionException {
		Document doc = getDocument();
		Object treexRet = null;
		
		try {
			TreexInputDocPrepare ip = new TreexInputDocPrepare(doc, getInputASName());
			treexRet = serverConnection.analyzePreprocessedDoc(doc.getContent().toString(), ip.createInputDocData());
		} catch (Exception e) {
			throw new ExecutionException("Error occured during run of Treex server.\nSee remote server's output, or local server's log file: " + getLogPath(), e);
		}
			
		TreexReturnAnalysis tra = new TreexReturnAnalysis(treexRet);
		
		try {
			tra.annotate(doc, getOutputASName());
		} catch (InvalidOffsetException e) {
			throw new ExecutionException(e);
		}
	}
	
	protected void initScenario() throws ResourceInstantiationException
	{
		try {
			serverConnection.initScenario(getLanguageCode(), getScenarioSetup().toArray(new String[0]));
			
			if (getVerifyOnInit()) {
				Object ret = serverConnection.analyzeText("robot");
				if (ret == null) throw new NullPointerException("Server returned null response!");
			}
		} catch (Exception e) {
			throw new ResourceInstantiationException(
					"Error occured during Treex server init.\nSee remote server's output, or local server's log file: " + getLogPath(), e);
		}
	}

	public List<String> getScenarioSetup() {
		return scenarioSetup;
	}

	public String getLanguageCode() {
		return languageCode;
	}


	public Boolean getVerifyOnInit() {
		return verifyOnInit;
	}


	@CreoleParameter(defaultValue="false")
	public void setVerifyOnInit(Boolean verifyOnInit) {
		this.verifyOnInit = verifyOnInit;
	}

}