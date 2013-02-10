package czsem.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Config extends AbstractConfig {
	
	public static Config getConfig() throws ConfigLoadEception
	{
		return (Config) new Config().getAbstractConfig();
	}
	
	public static class DependencyConfig implements Serializable {
		private static final long serialVersionUID = -6751646590097569415L;

		private Set<String> dependencyTypesSelected = new HashSet<String>(); 
		private Set<String> dependencyTypesAvailable = new HashSet<String>(); 
		private Set<String> tokenDependenciesSelected = new HashSet<String>(); 
		private Set<String> tokenDependenciesAvailable = new HashSet<String>(); 		

		public Set<String> getDependencyTypesSelected() {return dependencyTypesSelected;}
		public void setDependencyTypesSelected(Set<String> dependencyTypesSelected) {
			this.dependencyTypesSelected = dependencyTypesSelected;
		}
		public Set<String> getDependencyTypesAvailable() {return dependencyTypesAvailable;}
		public void setDependencyTypesAvailable(Set<String> dependencyTypesAvailable) {
			this.dependencyTypesAvailable = dependencyTypesAvailable;
		}
		public Set<String> getTokenDependenciesSelected() {	return tokenDependenciesSelected;}
		public void setTokenDependenciesSelected(Set<String> tokenDependenciesSelected) {
			this.tokenDependenciesSelected = tokenDependenciesSelected;
		}
		public Set<String> getTokenDependenciesAvailable() {return tokenDependenciesAvailable;}
		public void setTokenDependenciesAvailable(Set<String> tokenDependenciesAvailable) {
			this.tokenDependenciesAvailable = tokenDependenciesAvailable;
		}
		public void clear() {
			dependencyTypesSelected.clear();
			dependencyTypesAvailable.clear();
			tokenDependenciesSelected.clear();
			tokenDependenciesAvailable.clear();
		}
	}
	
	public DependencyConfig getDependencyConfig() {
		DependencyConfig ret = (DependencyConfig) getObj("dependencyConfig");
		if (ret == null) {
			ret = new DependencyConfig();
			setDependencyConfig(ret);
		}
		return ret;
	}

	public void setDependencyConfig(DependencyConfig dependencyConfig) {
		set("dependencyConfig", dependencyConfig);
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
