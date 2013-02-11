package czsem.fs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import czsem.fs.FSSentenceWriter.TokenDependecy;
import czsem.gate.utils.Config;
import czsem.utils.AbstractConfig.ConfigLoadEception;
import czsem.utils.Config.DependencyConfig;

public class DependencyConfiguration {
	public DependencyConfiguration(Collection<String> dependencyNames,	Iterable<TokenDependecy> tokenDepDefs) {
		this.dependencyNames = dependencyNames;
		this.tokenDepDefs = tokenDepDefs;
	}
	
	Collection<String> dependencyNames;
	Iterable<TokenDependecy> tokenDepDefs;
	public static DependencyConfiguration defaultConfig = 
	new DependencyConfiguration (
		Arrays.asList(new String [] {
				"tDependency", "auxRfDependency", "Dependency", /* "aDependency" */}), 
		Arrays.asList(new TokenDependecy [] {
				new TokenDependecy("t-node", "lex.rf"),
				new TokenDependecy("tToken", "lex.rf"),}));
	
	public void putToConfig() throws ConfigLoadEception {
		DependencyConfig depsCfg = Config.getConfig().getDependencyConfig();
		depsCfg.clear();
		
		depsCfg.getDependencyTypesSelected().addAll(dependencyNames);
		Set<String> tocs = depsCfg.getTokenDependenciesSelected();
		for (TokenDependecy tocDep :tokenDepDefs)
		{
			tocs.add(tocDep.tokenTypeName +"."+ tocDep.depFeatureName);				
		}
		
	}

	public static DependencyConfig getDependencyConfig() throws ConfigLoadEception {
		Config cfg = Config.getConfig();
		DependencyConfig depsCfg = cfg.getDependencyConfig();
		
		if (depsCfg == null) {
			cfg.setDependencyConfig(new DependencyConfig());
			defaultConfig.putToConfig();
			depsCfg = cfg.getDependencyConfig();
		}
		
		return depsCfg;
	}

	public static DependencyConfiguration getFromConfig() throws ConfigLoadEception {
		DependencyConfig depsCfg = getDependencyConfig();


		List<TokenDependecy> tokenDepDefs = new ArrayList<FSSentenceWriter.TokenDependecy>(depsCfg.getTokenDependenciesSelected().size());
		
		for (String s : depsCfg.getTokenDependenciesSelected()) {
			String[] split = s.split("\\.");
			if (split.length < 2) continue;
			tokenDepDefs.add(new TokenDependecy(split[0], split[1]));
		}
		
		return new DependencyConfiguration(depsCfg.getDependencyTypesSelected(), tokenDepDefs);
	}
}