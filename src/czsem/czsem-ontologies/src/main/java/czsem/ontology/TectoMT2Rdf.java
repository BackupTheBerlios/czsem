package czsem.ontology;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

public class TectoMT2Rdf
{
	static Logger logger = Logger.getLogger(TectoMT2Rdf.class);
	
	protected Transformer transformer;
	protected String ontologyURIBase;

	public TectoMT2Rdf(String ontologyURIBase) throws TransformerConfigurationException
	{
		this.ontologyURIBase = ontologyURIBase;
		TransformerFactory tFactory = TransformerFactory.newInstance();
	    	   
	    transformer = tFactory.newTransformer(
	    		new StreamSource(getClass().getResourceAsStream("tmt2rdf.xsl")));
	    				
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, TransformerException
	{
//		TectoMT2Rdf tmt2rdf = new TectoMT2Rdf("http://czsem.berlios.de/ontologies/czech_fireman/tmt_files/");
		TectoMT2Rdf tmt2rdf = new TectoMT2Rdf("http://czsem.berlios.de/ontologies/acquisitions-v1.1/tmt_files/");
		tmt2rdf.transformDirectory(
				"czsem_GATE_plugins/TmT_serializations", 
				"czsem_GATE_plugins/TmT_serializations/owl");
//		tmt2rdf.transformFile("czsem_GATE_plugins/TmT_serializations/jihomoravsky47443.txt.xml_00034.tmt");
		
	}

	public void transformDirectory(String inputDir, String outputDir) throws FileNotFoundException, TransformerException
	{
		File dir = new File(inputDir);
		String[] file_list = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String file_name) {
				return file_name.endsWith("tmt");
			}
		});
		
		for (int i = 0; i < file_list.length; i++)
		{
			String file = file_list[i];
			logger.info(
					String.format("Transforming file %d/%d: %s",
					i+1, file_list.length,
					inputDir + "/" + file));
					 
			transformFile(inputDir + "/" + file, outputDir + "/" + file + ".owl");
		}
	}

	public void transformFile(String inputFileName) throws FileNotFoundException, TransformerException
	{
		transformFile(inputFileName, inputFileName+".owl");		
	}

	public void transformFile(String inputFileName, String outputFileName) throws FileNotFoundException, TransformerException
	{
		transformer.setParameter("ontology_uri_param", ontologyURIBase + new File(outputFileName).getName());
		
		transformer.transform(
	    		new StreamSource(inputFileName),
	    		new StreamResult(new FileOutputStream(outputFileName)));		
	}


}
