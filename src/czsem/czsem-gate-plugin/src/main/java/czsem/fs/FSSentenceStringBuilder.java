package czsem.fs;

import gate.AnnotationSet;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FSSentenceStringBuilder {
	
	private StringWriter wr;
	private FSSentenceWriter fswr;

	public FSSentenceStringBuilder(AnnotationSet sentence_annotations)	 {
		wr = new StringWriter();
		fswr = new FSSentenceWriter(sentence_annotations, new PrintWriter(wr));
		fswr.printTree();
	}

	
	
	public String getTree() {
		return wr.toString();
	}



	public String[] getAttributes() {
		return fswr.getAttributes().toArray(new String [0]);
	}

}
