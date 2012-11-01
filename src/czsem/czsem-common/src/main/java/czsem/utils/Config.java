package czsem.utils;

public class Config extends AbstractConfig {
	
	public static Config getConfig() throws ConfigLoadEception
	{
		return (Config) new Config().getAbstractConfig();
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
