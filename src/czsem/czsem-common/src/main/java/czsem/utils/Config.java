package czsem.utils;

import java.io.IOException;
import java.net.URISyntaxException;


public class Config extends AbstractConfig {
	
	public static synchronized Config getConfig() throws IOException, URISyntaxException
	{
		if (config == null) {
			config = new Config();
			config.loadConfig();
		}
		return (Config) config;
	}

	
	public String getAlephPath() {
		return get("alephPath");
	}

	public void setAlephPath(String alephPath) {
		set("alephPath", alephPath);
	}

	public String getPrologPath() {
		return get("prologPath");
	}

	public void setPrologPath(String prologPath) {
		set("prologPath", prologPath);
	}

	public String getIlpProjestsPath() {
		return get("ilpSerialProjestsPath");
	}

	public void setIlpProjestsPath(String ilpProjestsPath) {
		set("ilpSerialProjestsPath", ilpProjestsPath);
	}


	public String getPrologRuleXmlSerializer() {
		return 	getCzsemResourcesDir()+ "/ILP/rule_xml_serializer.yap";
	}
	
	public String getWekaJarPath() {
		return get("wekaJarPath");
	}

	public void setWekaJarPath(String wekaJarPath) {
		set("wekaJarPath", wekaJarPath);
	}


	public String getWekaRunFuzzyILPClassPath() {
		return get("wekaRunFuzzyILPClassPath");
	}

	public void setWekaRunFuzzyILPClassPath(String myClassPath) {
		set("wekaRunFuzzyILPClassPath", myClassPath);
	}
	
	public void setCzsemResourcesDir(String czsemResourcesDir) {
		set("czsemResourcesDir", czsemResourcesDir);
	}

	public String getCzsemResourcesDir() {
		return get("czsemResourcesDir");
	}

	public void setGateHome(String gateHome) {
		set("gateHome", gateHome);
	}

	public String getGateHome() {
		return get("gateHome");
	}

}
