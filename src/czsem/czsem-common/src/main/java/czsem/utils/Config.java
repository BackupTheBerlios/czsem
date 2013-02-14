package czsem.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Config extends AbstractConfig {
	
	public static Config getConfig() throws ConfigLoadException
	{
		return (Config) new Config().getAbstractConfig();
	}
	
	public static class DependencyConfig implements Serializable {
		private static final long serialVersionUID = -6751646590097569415L;

		public static class DependencySetting implements Serializable {
			private static final long serialVersionUID = -927092769489657986L;

			private Set<String> dependencyTypes = new HashSet<String>(); 
			private Set<String> tokenDependencies = new HashSet<String>();
			
			public Set<String> getDependencyTypes() {
				return dependencyTypes;
			}
			public void setDependencyTypes(Set<String> dependencyTypes) {
				this.dependencyTypes = dependencyTypes;
			}
			public Set<String> getTokenDependencies() {
				return tokenDependencies;
			}
			public void setTokenDependencies(Set<String> tokenDependencies) {
				this.tokenDependencies = tokenDependencies;
			}
			public void clear() {
				dependencyTypes.clear();
				tokenDependencies.clear();
			}
		}
		
		private DependencySetting selected = new DependencySetting();
		private DependencySetting available = new DependencySetting();
		
		public DependencySetting getSelected() {return selected;}
		public void setSelected(DependencySetting selected) {this.selected = selected;}
		public DependencySetting getAvailable() {return available;}
		public void setAvailable(DependencySetting available) {this.available = available;};
	}
	
	public DependencyConfig getDependencyConfig() {
		DependencyConfig ret = (DependencyConfig) getObj("dependencyConfig");
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
