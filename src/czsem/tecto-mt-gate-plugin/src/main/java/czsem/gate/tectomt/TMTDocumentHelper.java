package czsem.gate.tectomt;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import czsem.Utils;
import gate.creole.AbstractResource;
import gate.util.InvalidOffsetException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import com.generationjava.io.xml.SimpleXmlWriter;
import com.generationjava.io.xml.XmlWriter;


public class TMTDocumentHelper
{
	private Document gate_doc;
	private String tmt_filepath;
	private AnnotationSet inputAS;
	
	public TMTDocumentHelper(Document doc, String input_AS_name, String language, URL serilizationDirectory) throws IOException, URISyntaxException, InvalidOffsetException
	{
		this.gate_doc = doc;
		inputAS = doc.getAnnotations(input_AS_name);
		
		createTMTFile(serilizationDirectory, language);
	}
	
	protected String findTMTFileName(URL outputDirectory) throws IOException, URISyntaxException
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Utils.URLToFilePath(outputDirectory));
		sb.append('/');
		sb.append(((AbstractResource) gate_doc).getName());
		sb.append(".tmt");
		
		String dest_filename = Utils.findAvailableFileName(sb.toString());
		
		return dest_filename;	
	}

	public String createTMTFile(URL outputDirectory, String language) throws IOException, URISyntaxException, InvalidOffsetException 
	{
		File out_dir = Utils.URLToFile(outputDirectory);
		if (! out_dir.exists()) out_dir.mkdirs(); 
		
		tmt_filepath = findTMTFileName(outputDirectory);

		Writer fwr = new OutputStreamWriter(new FileOutputStream(tmt_filepath), "utf-8");
		XmlWriter xmlwriter = new SimpleXmlWriter(fwr);
		xmlwriter.writeXmlVersion("1.0", "utf-8");
		xmlwriter.writeEntity("tmt_document");
		xmlwriter.writeAttribute("xmlns", "http://ufal.mff.cuni.cz/pdt/pml/");
		xmlwriter.writeEntity("head");
		xmlwriter.writeEntity("schema");
		xmlwriter.writeAttribute("href", "tmt_schema.xml");
		xmlwriter.endEntity();//schema
		xmlwriter.endEntity();//head
		xmlwriter.writeEntity("meta");
		xmlwriter.writeEntityWithText(language+"_source_text", gate_doc.getContent().toString());
		xmlwriter.endEntity();//meta

		//write sentences
		xmlwriter.writeEntity("bundles");
		AnnotationSet sentences = inputAS.get("Sentence");
		List<Annotation> ord_sents = gate.Utils.inDocumentOrder(sentences);
		
		for (int a=0; a< ord_sents.size(); a++)
		{
			Annotation sentece = ord_sents.get(a);
			xmlwriter.writeEntity("LM");
			xmlwriter.writeAttribute("id", "s"+a+1);
			xmlwriter.writeEntityWithText(
					language+"_source_sentence", 
					gate_doc.getContent().getContent(
							sentece.getStartNode().getOffset(),
							sentece.getEndNode().getOffset()).toString()
					);			
			xmlwriter.endEntity();//LM
		}
		
		xmlwriter.endEntity();//bundles
		
		
		xmlwriter.endEntity();//tmt_document
		xmlwriter.close();
		fwr.close();
		
		return tmt_filepath;
	}

	public String getTMTFilePath() {
		return tmt_filepath;
	}

	public Document getDocument() {
		return gate_doc;
	}

}
