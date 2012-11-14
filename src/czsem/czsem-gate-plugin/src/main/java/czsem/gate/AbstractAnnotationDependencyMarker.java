package czsem.gate;

import gate.AnnotationSet;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;


import czsem.gate.utils.GateAwareTreeIndex;

public abstract class AbstractAnnotationDependencyMarker extends AbstractLanguageAnalyserWithInputAnnotTypes
{
	private static final long serialVersionUID = 1L;
	
	protected String tokensAndDependenciesASName = null;

	
	protected AnnotationSet inputTokensAS = null;
	protected AnnotationSet inputDependenciesAS = null;
	protected String tokenAnnotationTypeName;
	protected String dependencyAnnotationTypeName;
	protected GateAwareTreeIndex treeIndex;

	public String getTokenAnnotationTypeName() {
		return tokenAnnotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="Token")
	public void setTokenAnnotationTypeName(String tokenAnnotationTypeName) {
		this.tokenAnnotationTypeName = tokenAnnotationTypeName;
	}
	public String getDependencyAnnotationTypeName() {
		return dependencyAnnotationTypeName;
	}
	@RunTime
	@CreoleParameter(defaultValue="aDependency")
	public void setDependencyAnnotationTypeName(String dependencyAnnotationTypeName) {
		this.dependencyAnnotationTypeName = dependencyAnnotationTypeName;
	}
	
	protected void initBeforeExecute()
	{
		super.initBeforeExecute();
		
		if (getTokensAndDependenciesASName() == null) setTokensAndDependenciesASName(getInputASName());
		
		AnnotationSet tocDepAS = document.getAnnotations(getTokensAndDependenciesASName());
		
		inputTokensAS = tocDepAS.get(tokenAnnotationTypeName);
		inputDependenciesAS = tocDepAS.get(dependencyAnnotationTypeName);
		treeIndex = new GateAwareTreeIndex(inputDependenciesAS);
	}

	public String getTokensAndDependenciesASName() {
		return tokensAndDependenciesASName;
	}
	
	@RunTime
	@Optional
	@CreoleParameter(comment="If not set, inputAS is taken.")
	public void setTokensAndDependenciesASName(String tokensAndDependenciesASName) {
		this.tokensAndDependenciesASName = tokensAndDependenciesASName;
	}


}
