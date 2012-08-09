package czsem.gate.tectomt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import czsem.gate.tectomt.SentenceInfoManager.Layer;

public class TMTSAXParser extends DefaultHandler
{
	private String language = "english";
	
	public static class Context
	{
		String qname;
		String id;
		int children;

		public Context(String qname, String id) {
			this.qname = qname;
			this.id = id;
			children = 0;
		}
	}
	
	private Stack<Context> context_stack = new Stack<Context>();
	private Stack<String> id_valid_stack = new Stack<String>();
	
	private StringBuilder last_characters = null; 


	private SentenceInfoManager actual_sentence;
	
	private List<SentenceInfoManager> sentences = new ArrayList<SentenceInfoManager>();
	
	private SAXParser sax_parser;
	
	public static class Constants
	{
		public static final int DOC_DEPTH_SENTENCES = 4; 
		public static final int DOC_DEPTH_TREES = 5; 
		public static final int DOC_DEPTH_TREE_ROOTS = 6; 
	}
	
	public TMTSAXParser (String language) throws ParserConfigurationException, SAXException
	{
		this.language = language;
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setValidating(false); // No validation required
	    sax_parser = spf.newSAXParser();

	}
	
	public List<SentenceInfoManager> parse(String tmTFilename) throws SAXException, IOException
	{
		init();
		
		InputSource input = new InputSource(new FileInputStream(tmTFilename));
	    id_valid_stack.push("TToPP");
	    
	    sax_parser.parse(input, this);
	    
	    assert(id_valid_stack.peek().equals("TToPP"));

		
		return sentences;		
	}
	
	protected void init()
	{
		id_valid_stack.clear();
		context_stack.clear();
		sentences.clear();
	}
		
	private void newSentence()
	{
		SentenceInfoManager sent = new SentenceInfoManager();
		sentences.add(sent);
		actual_sentence = sent;
		
	}
	
	private void newToken(String parent_id, String child_id)
	{
		actual_sentence.newToken(parent_id, child_id, context_stack.size() > Constants.DOC_DEPTH_TREE_ROOTS+1);		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		Context current_context = context_stack.pop();
		String current_qname = current_context.qname;
		assert current_qname.equals(qName);
		String prewious_qname = context_stack.empty() ? null : context_stack.peek().qname;
		
		String id = current_context.id;
		if (id != null) id_valid_stack.pop();
		
		if (context_stack.size() < Constants.DOC_DEPTH_SENTENCES-1) return;
		
		context_stack.peek().children++;

				
		if (current_qname.equalsIgnoreCase(language+"_source_sentence"))
		{
			actual_sentence.setString(stringFromLastCahrs());
		}		
		if ((current_qname.equals("LM") && prewious_qname.equals("aux.rf"))||
			(current_qname.equals("aux.rf") && current_context.children == 0))
		{
			actual_sentence.newAuxRfDependency(id_valid_stack.peek(), stringFromLastCahrs());
		}
		if ((current_qname.equals("LM") && prewious_qname.equals("m.rf"))||
				(current_qname.equals("m.rf") && current_context.children == 0))
			{
				actual_sentence.addMRfDependency(stringFromLastCahrs());
			}
		if (context_stack.size() >= Constants.DOC_DEPTH_TREE_ROOTS)
		{
			updateFeatures(current_qname, actual_sentence.getActualTokenFeatureList());
			actual_sentence.tryToCloseActualToken(id);
		}
		
	}
	
	private void updateFeatures(String qName, String [] feature_list)
	{
		int feature_index = fetureIndexFromFeatureName(qName, feature_list);
		if (feature_index != -1) actual_sentence.updateActualTokenFeature(feature_index, stringFromLastCahrs());		
	}

	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		String new_id = attributes.getValue("id");
		String prew_id = id_valid_stack.peek(); 
		
		if (new_id != null) id_valid_stack.push(new_id);
		context_stack.push(new Context(qName, new_id));
		
		//System.err.printf("%3d %s\n", qname_stack.size(), qName);
						
		switch (context_stack.size())
		{
			case 1:
			case 2:
//			case 3:	
				return;
			
			case Constants.DOC_DEPTH_SENTENCES-1:
				if (qName.equalsIgnoreCase("LM"))
				{
					newSentence();
					return;
				}

			case Constants.DOC_DEPTH_SENTENCES:
				if (qName.equalsIgnoreCase(language+"_source_sentence"))
				{
//					newSentence();
					break;
				}

				
			case Constants.DOC_DEPTH_TREES:
				if (qName.equalsIgnoreCase('S'+language+'T'))
				{
					actual_sentence.setActualLayer(Layer.TECTO);
					return;
				}
				if (qName.equalsIgnoreCase('S'+language+'A'))
				{
					actual_sentence.setActualLayer(Layer.ANALAYTICAL);
					return;
				}
				if (qName.equalsIgnoreCase('S'+language+'M'))
				{
					actual_sentence.setActualLayer(Layer.MORPHO);
					return;					
				}
				if (qName.equalsIgnoreCase('S'+language+'N'))
				{
					actual_sentence.setActualLayer(Layer.NAMES);
					return;					
				}
//				System.err.println(qName);
//				actual_sentence.setActualLayer(Layer.DUMMY);
				return;

			default:
				break;
		}
		
		if ((qName.equals("LM") || qName.equals("children")) && (new_id != null))
		{			
			newToken(prew_id, new_id);
		}		

		if (shouldReadContentOfCurrentElement()) last_characters = new StringBuilder();
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (shouldReadContentOfCurrentElement())
		{
			last_characters.append(ch, start, length);		
		}
	}

	
	private boolean shouldReadContentOfCurrentElement()
	{
		if (context_stack.size() < Constants.DOC_DEPTH_SENTENCES) return false;		
		String[] actual_token_featurelist = actual_sentence.getActualTokenFeatureList();		
		String current_qname = context_stack.peek().qname;
		

		
		return
			current_qname.equalsIgnoreCase(language+"_source_sentence") ||
			(current_qname.equals("LM") && prewious_qname().equals("aux.rf")) || //aux.rf
			(current_qname.equals("aux.rf") && context_stack.peek().children == 0) || //aux.rf
			(current_qname.equals("LM") && prewious_qname().equals("m.rf")) || //m.rf
			(current_qname.equals("m.rf") && context_stack.peek().children == 0) || //m.rf
			(		actual_token_featurelist != null &&
					fetureIndexFromFeatureName(current_qname, actual_token_featurelist) != -1);
		
	}

	private String prewious_qname()
	{
		Context current_qname = context_stack.pop();
		String prewious_qname = context_stack.peek().qname;
		context_stack.push(current_qname);
		return prewious_qname;
	}

	private int fetureIndexFromFeatureName(String qName, String [] feature_list)
	{
		for (int i = 0; i < feature_list.length; i++) {
			if (feature_list[i].equals(qName))
			{
				return i;
			}
		}
		return -1;
	}
	
	private String stringFromLastCahrs()
	{
		String ret = last_characters.toString();
		return  ret;
	}


	



}
