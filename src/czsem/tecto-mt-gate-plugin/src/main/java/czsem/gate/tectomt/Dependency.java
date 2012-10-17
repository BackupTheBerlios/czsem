package czsem.gate.tectomt;

import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

import java.io.PrintStream;

import czsem.gate.utils.GateUtils;

public abstract class Dependency extends TMTAnnotation
{
	private FeatureMap fm;

	@Override
	public FeatureMap getFeatures() {return fm;}

	@Override
	public void annotate(AnnotationSet as, SentenceInfoManager sentence) throws InvalidOffsetException
	{
		Integer gate_parent_id = sentence.findToken(parent_id).gate_annotation_id;
		Integer gate_child_id = sentence.findToken(child_id).gate_annotation_id;

		
		Annotation a1 = as.get(gate_parent_id);
		Annotation a2 = as.get(gate_child_id);
		
		if (a1 == null || a2 == null) return;
		
		Long ix1 = Math.min(a1.getStartNode().getOffset(), a2.getStartNode().getOffset());
		Long ix2 = Math.max(a1.getEndNode().getOffset(), a2.getEndNode().getOffset());
		
		fm = GateUtils.createDependencyArgsFeatureMap(gate_parent_id, gate_child_id);
		
		annotate(as, ix1, ix2);		
	}

	public Dependency(String parent_id, String child_id)
	{
		this.parent_id = parent_id;
		this.child_id = child_id;
	}

	protected String parent_id;
	protected String child_id;

	public void print(PrintStream out)
	{
		out.print("Depends: ");
		out.print(child_id);
		out.print(" on ");
		out.print(parent_id);
	}


}
