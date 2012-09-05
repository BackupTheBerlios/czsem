package czsem.gate.treex;

import gate.Annotation;
import gate.util.InvalidOffsetException;
import czsem.gate.treex.Annotator.Annotable;
import czsem.gate.treex.Annotator.AnnotableDependency;

public interface AnnotatorInterface {
	void annotate(Annotable ann, Long startOffset, Long endOffset) throws InvalidOffsetException;
	void annotateDependecy(AnnotableDependency dAnn) throws InvalidOffsetException;
	Annotation getAnnotation(Integer id);
}
