package czsem.gate.externalannotator;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

import org.apache.log4j.Logger;

import czsem.gate.GateUtils;
import czsem.gate.externalannotator.RecursiveEntityAnnotator.SecondaryEntity;
import czsem.gate.externalannotator.SequenceAnnotator.CannotAnnotateCharacterSequence;

public class Annotator implements AnnotatorInterface {
	private static Logger logger = Logger.getLogger(Annotator.class);

	public static interface Annotable {
		public String getAnnotationType();
		public FeatureMap getFeatures();
		public void setGateAnnId(Integer gate_annotation_id);
	}
	
	public static abstract class AnnotableDependency implements SecondaryEntity {

		public abstract Integer getParentGateId();
		public abstract Integer getChildGateId();

		@Override
		public FeatureMap getFeatures() {
			return GateUtils.createDependencyArgsFeatureMap(
					getParentGateId(), 
					getChildGateId());
		}

		@Override
		public boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException {
			if (getParentGateId() == null) return false;
			if (getChildGateId() == null) return false;
			annotator.annotateDependecy(this);
			return true;
		}
		
		@Override
		public void setGateAnnId(Integer gate_annotation_id) {}
	}
	
	public static interface SeqAnnotable extends Annotable {
		public String getString();
	}

	public static interface Sentence extends SeqAnnotable {
		Iterable<SeqAnnotable> getOrderedTokens();
		void annotateSecondaryEntities(AnnotatorInterface annotator) throws InvalidOffsetException;		
	}

	
	public static interface AnnotationSource {
		Iterable<Sentence> getOrderedSentences();		
	}

	private SequenceAnnotator seq_anot;
	private AnnotationSet as;
	
	public void annotate(
			AnnotationSource annotationSource,
			Document doc,
			String outputASName) throws InvalidOffsetException
	{
		seq_anot = new SequenceAnnotator(doc);
		as = doc.getAnnotations(outputASName);

		for (Sentence s : annotationSource.getOrderedSentences())
		{
			annotateSentence(s);
		}
	}

	protected void annotateSentence(Sentence s) throws InvalidOffsetException {
    	seq_anot.backup();

    	safeAnnotateSeq(s);
    	    	
    	seq_anot.restore();
    	
		for (SeqAnnotable token : s.getOrderedTokens())
		{
			safeAnnotateSeq(token);
		}
		
		s.annotateSecondaryEntities(this);
	}

	protected void safeAnnotateSeq(SeqAnnotable seqAnn) throws InvalidOffsetException {
		try {
			annotateSeq(seqAnn);
		} catch (CannotAnnotateCharacterSequence e) {
			logger.error(String.format("SeqAnnotation error in document: %s", as.getDocument().getName()));
			logger.error(this, e);
		}
	}

	protected void annotateSeq(SeqAnnotable seqAnn) throws InvalidOffsetException {
    	seq_anot.nextToken(seqAnn.getString());
    	annotate(seqAnn, seq_anot.lastStart(), seq_anot.lastEnd());
	}

	@Override
	public void annotate(Annotable ann, Long startOffset, Long endOffset) throws InvalidOffsetException {
    	Integer gate_annotation_id = as.add(
    			startOffset,
    			endOffset,
    			ann.getAnnotationType(),
    			ann.getFeatures());
    	
    	ann.setGateAnnId(gate_annotation_id);    	
	}

	@Override
	public Annotation getAnnotation(Integer id) {
		return as.get(id);
	}

	@Override
	public void annotateDependecy(AnnotableDependency dAnn) throws InvalidOffsetException {
		Integer gate_parent_id = dAnn.getParentGateId();
		Integer gate_child_id = dAnn.getChildGateId();

		
		Annotation a1 = as.get(gate_parent_id);
		Annotation a2 = as.get(gate_child_id);
		
		if (a1 == null || a2 == null) return;
		
		Long ix1 = Math.min(a1.getStartNode().getOffset(), a2.getStartNode().getOffset());
		Long ix2 = Math.max(a1.getEndNode().getOffset(), a2.getEndNode().getOffset());
		
		annotate(dAnn, ix1, ix2);
		
	}

}
