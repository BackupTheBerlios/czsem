package cuni.mff.intlib.brat;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class BratDocumentAnnotator {
	
	public static final String BRAT_AS_NAME = "Brat";
	protected BufferedReader in = null;
	protected AnnotationSet outAs;

	public BratDocumentAnnotator(Document doc, File annFile) throws UnsupportedEncodingException, FileNotFoundException {
		in = new BufferedReader(new InputStreamReader(new FileInputStream(annFile), "utf8"));
		outAs = doc.getAnnotations(BRAT_AS_NAME);
	}

	public void annotate() throws IOException, NumberFormatException, InvalidOffsetException {
		for (;;)
		{
			String line = in.readLine();
			if (line == null) break;
			
			String[] split = line.split("\t");
			
			addBratAnnotation(split);
			
		}		
	}

	protected void addBratAnnotation(String[] split) throws NumberFormatException, InvalidOffsetException {
		if (split[0].startsWith("T"))
			addEntityAnnotation(split);
	}

	protected void addEntityAnnotation(String[] split) throws NumberFormatException, InvalidOffsetException {
		String[] pos = split[1].split(" ");
		
		
		FeatureMap fm = Factory.newFeatureMap();
		fm.put("bratAnnId", split[0]);
		fm.put("string", split[2]);
		
		outAs.add(new Long(pos[1]), new Long(pos[2]), pos[0], fm);
		
	}

}
