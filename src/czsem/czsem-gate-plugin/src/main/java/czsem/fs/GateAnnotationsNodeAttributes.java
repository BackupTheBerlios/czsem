package czsem.fs;

import gate.AnnotationSet;
import gate.FeatureMap;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public class GateAnnotationsNodeAttributes implements NodeAttributes {
	protected AnnotationSet annotations;

	public GateAnnotationsNodeAttributes(AnnotationSet annotations) {
		this.annotations = annotations;
	}

	@Override
	public Collection<Entry<String, Object>> get(int node_id) {
		
		FeatureMap fm = annotations.get(node_id).getFeatures();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<Entry<String, Object>> f = (Set) fm.entrySet();

		return f;
	}

	@Override
	public Object getValue(int node_id, String attrName) {
		return annotations.get(node_id).getFeatures().get(attrName);
	}
}
