package czsem.gate.plugins;

import gate.Gate;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.net.URL;
import java.util.List;

import czsem.gate.treex.TreexAnalyserBase;
import czsem.gate.treex.TreexServerExecution;

@CreoleResource(name = "czsem TreexLocalAnalyser", comment = "Alyses givem corpus by Treex localy ( see http://ufal.mff.cuni.cz/treex/ )", 
	helpURL="https://czsem-suite.atlassian.net/wiki/display/DOC/Treex+GATE+Plugin")
public class TreexLocalAnalyser extends TreexAnalyserBase {

	private static final long serialVersionUID = -3111101835623696930L;
		
	private int serverPortNumber;
	private boolean showTreexLogInConsole;

	private TreexServerExecution treexExec;

	
	
	@Override
	public void cleanup() {
		if (serverConnection == null) return;
		serverConnection.terminateServer();
	}

	@Override
	public Resource init() throws ResourceInstantiationException {
		//debugClassloader();
		
		treexExec = new TreexServerExecution();
		treexExec.show_treex_output = getShowTreexLogInConsole();
		treexExec.setPortNumber(getServerPortNumber());
		
		serverConnection = treexExec.getConnection();
		
		try {
			treexExec.start();
			initScenario();
		} catch (Exception e) {
			serverConnection.terminateServer();
			serverConnection = null;
			throw new ResourceInstantiationException(e);
		}
		
		return super.init();
	}


	public void debugClassload(String name) {
		URL url = getClass().getClassLoader().getResource(name.replace('.', '/') + ".class");
		System.err.println(url);

		url = Gate.getClassLoader().getResource(name.replace('.', '/') + ".class");
		System.err.println(url);

		url = Gate.getClassLoader().getParent().getResource(name.replace('.', '/') + ".class");
		System.err.println(url);
		
		System.err.println("---");
		try {
			getClass().getClassLoader().loadClass(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void debugClassloader() {
		debugClassload("org.apache.log4j.Logger");
		debugClassload("org.xml.sax.SAXException");
		debugClassload("org.xml.sax.ContentHandler");
		debugClassload("org.xml.sax.Attributes");
		debugClassload("org.xml.sax.helpers.AttributesImpl");
		debugClassload("org.xml.sax.InputSource");
		debugClassload("javax.xml.parsers.ParserConfigurationException");
		debugClassload("javax.xml.parsers.SAXParserFactory");
		debugClassload("javax.xml.parsers.SAXParser");
		debugClassload("org.xml.sax.SAXParseException");
		debugClassload("org.xml.sax.XMLReader");
	}

	@CreoleParameter(comment="List of blocks to be used in the analysis. Each element can be either a Treex block or a .scen file.",
			defaultValue="W2A::CS::Segment;W2A::CS::Tokenize;W2A::CS::TagFeaturama lemmatize=1;W2A::CS::FixMorphoErrors")
	public void setScenarioSetup(List<String> scenarioSetup) {
		this.scenarioSetup = scenarioSetup;
	}

	@CreoleParameter(comment="LangCode must be valid ISO 639-1 code. E.g. en, de, cs",	defaultValue="cs")			
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

	public Boolean getShowTreexLogInConsole() {
		return showTreexLogInConsole;
	}

	@CreoleParameter(defaultValue="false")
	public void setShowTreexLogInConsole(Boolean showTreexLogInConsole) {
		this.showTreexLogInConsole = showTreexLogInConsole;
	}

	@Override
	protected String getHandshakeCode() {
		return treexExec.getHandshakeCode();
	}
	
	
}
