package czsem.gate.plugins;

import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;

import java.net.URL;
import java.util.List;

import czsem.gate.treex.TreexAnalyserBase;
import czsem.gate.treex.TreexServerConnection;

@CreoleResource(name = "czsem TreexRemoteAnalyser", comment = "Alyses givem corpus by Treex remote (or local) server.", 
	helpURL="https://czsem-suite.atlassian.net/wiki/display/DOC/Treex+GATE+Plugin")
public class TreexRemoteAnalyser extends TreexAnalyserBase {	
	
	private static final long serialVersionUID = -5182317059444320543L;

	private boolean resetServerScenario;
	private boolean terminateServerOnCleanup;
	private URL treexServerUrl;
	
	@Override
	public void cleanup() {
		if (getTerminateServerOnCleanup() && serverConnection != null)
		{
			serverConnection.terminateServer();			
		}
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		serverConnection = new TreexServerConnection(getTreexServerUrl());
		
		try {
			if (getResetServerScenario())
			{
				String lang = getLanguageCode();
				List<String> scenSetup = getScenarioSetup();
				if (lang != null && lang != "" && scenSetup != null && scenSetup.size() != 0)
				{
					initScenario();
				}
			}
		} catch (Exception e) {
			serverConnection = null;
			throw new ResourceInstantiationException(e);
		}
		
		return super.init();
	}

	
	
	@Optional
	@CreoleParameter(comment="List of blocks to be used in the analysis. Each element can be either a Treex block or a .scen file.",
			defaultValue="W2A::CS::Segment;W2A::CS::Tokenize;W2A::CS::TagFeaturama lemmatize=1;W2A::CS::FixMorphoErrors")
	public void setScenarioSetup(List<String> scenarioSetup) {
		this.scenarioSetup = scenarioSetup;
	}

	@Optional
	@CreoleParameter(comment="LangCode must be valid ISO 639-1 code. E.g. en, de, cs",	defaultValue="cs")			
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	@CreoleParameter(comment="The remote Treex server can already have an initialized scenario ready, do you want to replace it with the current one?",	
			defaultValue="true")			
	public void setResetServerScenario(Boolean resetServerScenario) {
		this.resetServerScenario = resetServerScenario;
	}

	public Boolean getResetServerScenario() {
		return resetServerScenario;
	}


	@CreoleParameter(defaultValue="true")			
	public void setTerminateServerOnCleanup(Boolean terminateServerOnCleanup) {
		this.terminateServerOnCleanup = terminateServerOnCleanup;
	}

	public Boolean getTerminateServerOnCleanup() {
		return terminateServerOnCleanup;
	}

	@CreoleParameter(defaultValue="http://localhost:9090")			
	public void setTreexServerUrl(URL treexServerUrl) {
		this.treexServerUrl = treexServerUrl;
	}

	public URL getTreexServerUrl() {
		return treexServerUrl;
	}
}
