package czsem.gate.plugins;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.util.List;

import czsem.gate.treex.TreexAnalyserBase;
import czsem.gate.treex.TreexServerExecution;

@CreoleResource(name = "TreexLocalAnalyser", comment = "Alyses givem corpus by Treex localy ( see http://ufal.mff.cuni.cz/treex/ )")
public class TreexLocalAnalyser extends TreexAnalyserBase {

	private static final long serialVersionUID = -3111101835623696930L;
		
	private int serverPortNumber;
	
	
	@Override
	public void cleanup() {
		if (serverConnection == null) return;
		serverConnection.terminateServer();
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		TreexServerExecution exec = new TreexServerExecution();
		exec.setPortNumber(getServerPortNumber());
		
		serverConnection = exec.getConnection();
		
		try {
			exec.start();
			serverConnection.initScenario(getLanguageCode(), getScenarioSetup().toArray(new String[0]));
		} catch (Exception e) {
			serverConnection.terminateServer();
			serverConnection = null;
			throw new ResourceInstantiationException(e);
		}
		
		return super.init();
	}


	@CreoleParameter(comment="List of blocks to be used in the analysis. Each element can be either a Treex block or a .scen file.",
			defaultValue="W2A::EN::Segment;W2A::EN::Tokenize;W2A::EN::NormalizeForms;W2A::EN::FixTokenization;W2A::EN::TagMorce;W2A::EN::FixTags;W2A::EN::Lemmatize")
	public void setScenarioSetup(List<String> scenarioSetup) {
		this.scenarioSetup = scenarioSetup;
	}

	@CreoleParameter(comment="LangCode must be valid ISO 639-1 code. E.g. en, de, cs",	defaultValue="en")			
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	@CreoleParameter(defaultValue="9090")
	public void setServerPortNumber(Integer serverPortNumber) {
		this.serverPortNumber = serverPortNumber;
	}

	public Integer getServerPortNumber() {
		return serverPortNumber;
	}
}
