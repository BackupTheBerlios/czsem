package czsem.gate;

import gate.AnnotationSet;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

public abstract class AbstractLanguageAnalyserWithInputOutputAS extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = 1L;
	protected String inputASName = null;
	protected AnnotationSet inputAS = null;
	protected String outputASName = null;
	protected AnnotationSet outputAS = null;

	public AbstractLanguageAnalyserWithInputOutputAS() {
		super();
	}

	public String getInputASName() {
		return inputASName;
	}

	@Optional
	@RunTime
	@CreoleParameter
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	public String getOutputASName() {
		return outputASName;
	}

	@Optional
	@RunTime
	@CreoleParameter
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}

	protected void initBeforeExecute() {
		inputAS = document.getAnnotations(inputASName);
		outputAS = document.getAnnotations(outputASName);	
	}

}