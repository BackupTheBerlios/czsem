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
		// TODO Auto-generated method stub
		return 	 "/rule_xml_serializer.yap";
	}
}
