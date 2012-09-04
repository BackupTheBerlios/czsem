package czsem.gate.treex;

import gate.util.InvalidOffsetException;
import czsem.gate.treex.Annotator.Annotable;

public abstract class RecursiveEntityAnnotator {
	
	public static interface SecondaryEntity extends Annotable {
		boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException;		
	}
	
	protected abstract void storeForLater(SecondaryEntity entity);
	protected abstract SecondaryEntity getNextUnprocessedEntity();
	
	public void annotateSecondaryEntities(AnnotatorInterface annotator) throws InvalidOffsetException
	{
		for (;;) {
			
			SecondaryEntity entity = getNextUnprocessedEntity();
			if (entity == null) break;
			
			if (! entity.annotate(annotator))
			{
				storeForLater(entity);				
			}			
		}			
	}
}
