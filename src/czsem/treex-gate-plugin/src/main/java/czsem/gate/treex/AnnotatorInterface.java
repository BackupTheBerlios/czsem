package czsem.gate.treex;

import gate.Annotation;
import gate.util.InvalidOffsetException;
import czsem.gate.treex.Annotator.Annotable;

public interface AnnotatorInterface {
	void annotate(Annotable ann, Long startOffset, Long endOffset) throws InvalidOffsetException;
	Annotation getAnnotation(Integer id);
}
