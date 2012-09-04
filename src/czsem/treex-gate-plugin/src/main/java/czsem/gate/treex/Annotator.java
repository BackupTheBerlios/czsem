package czsem.gate.treex;

import org.apache.log4j.Logger;

import czsem.gate.tectomt.SequenceAnnotator;
import czsem.gate.tectomt.SequenceAnnotator.CannotAnnotateCharacterSequence;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

public class Annotator implements AnnotatorInterface {
	private static Logger logger = Logger.getLogger(Annotator.class);

	public static interface Annotable {
		public String getAnnotationType();
		public FeatureMap getFeatures();
		public void setGateAnnId(Integer gate_annotation_id);
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
	public void annotate(Annotable seqAnn, Long startOffset, Long endOffset) throws InvalidOffsetException {
    	Integer gate_annotation_id = as.add(
    			startOffset,
    			endOffset,
    			seqAnn.getAnnotationType(),
    			seqAnn.getFeatures());
    	
    	seqAnn.setGateAnnId(gate_annotation_id);    	
	}

	@Override
	public Annotation getAnnotation(Integer id) {
		return as.get(id);
	}

}
