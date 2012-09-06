package czsem.gate.tectomt;

import gate.AnnotationSet;
import gate.Document;
import gate.util.InvalidOffsetException;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import czsem.gate.externalannotator.SequenceAnnotator;
import czsem.gate.tectomt.SentenceInfoManager.Layer;

public class Annotator 
{
	private static Logger logger = Logger.getLogger(Annotator.class);
	private List<SentenceInfoManager> sentences;		
	private SequenceAnnotator seq_anot;
	private AnnotationSet as;
	
	public Annotator(List<SentenceInfoManager> sentences) throws ParserConfigurationException, SAXException
	{
		this.sentences = sentences;		
	}

	private void annotateSentence(SentenceInfoManager sentence) throws InvalidOffsetException
	{
		logger.debug(sentence.getString());
				
    	seq_anot.backup();

    	sentence.annotate(as, seq_anot);
    	    	
    	seq_anot.restore();
    	
    	annotateTokensSeq(sentence.getTokens(Layer.MORPHO));
    	Token[] aTokens = sentence.getTokens(Layer.ANALAYTICAL);
//   		sortTokensAccordingAOrd(aTokens);
//   		annotateTokensSeq(aTokens);
    	annotateTokens(sentence, aTokens);
    	annotateTokens(sentence, sentence.getTokens(Layer.NAMES));
    	annotateDenedecies(sentence, sentence.getDependencies(Layer.ANALAYTICAL));
    	annotateTokens(sentence, sentence.getTokens(Layer.TECTO));
    	annotateDenedecies(sentence, sentence.getDependencies(Layer.TECTO));
    	annotateDenedecies(sentence, sentence.getAuxRfDependencies());    	    	
	}


	public static Token[] sortTokensAccordingAOrd(Token[] tokens)
	{		
		Arrays.sort(tokens, new Comparator<Token>() {
			@Override
			public int compare(Token o1, Token o2) {
				return ((Integer)o1.getAOrd()).compareTo(o2.getAOrd());
			}
		});
		
		return tokens;
	}


	private void annotateTokens(SentenceInfoManager sentence, Token[] tokens) throws InvalidOffsetException
	{
		for (Token token : tokens)
		{
			token.annotate(as, sentence);
		}
	}
	
	private void annotateTokensSeq(Token[] tokens) throws InvalidOffsetException
	{				
		for (int i = 0; i < tokens.length; i++)
		{
			tokens[i].annotate(as, seq_anot);
		}
	}
		
	private void annotateDenedecies(SentenceInfoManager sentence, List<Dependency> depencies) throws InvalidOffsetException
	{
		for (Dependency dependency : depencies) {
			dependency.annotate(as, sentence);
		}
		
	}

	public void annotate(Document doc, String outputASName) throws InvalidOffsetException
	{
		seq_anot = new SequenceAnnotator(doc);
		as = doc.getAnnotations(outputASName);
		
		for (SentenceInfoManager sentence : sentences)
		{
			annotateSentence(sentence);			
		}
	}

	public void debugPrintDependecies(List<Dependency> dep, PrintStream out)
	{
		for (Dependency dependency : dep) {
			dependency.print(out);
		}			
	}

	
	public void debug_print(PrintStream out)
	{
	    out.println("------------------------------------------------------------");
//	    out.println(tmTFilename);	    
	    out.println("Sentences: " + sentences.size());
	    if (sentences.size() <= 0) return;
	    out.println("First Sentence string: " + sentences.get(0).getString());
	    out.println("First Sentence aTokens: " + sentences.get(0).getTokens(Layer.ANALAYTICAL).length);
	    out.println("First Sentence num tTokens: " + sentences.get(0).getTokens(Layer.TECTO).length);
	    int last_s = sentences.size()-1;
	    SentenceInfoManager last_sentence = sentences.get(last_s);
	    out.println("Last Sentence string: " + last_sentence.getString());
	    out.println("Last Sentence aTokens forms: ");
		Token[] tokens = sortTokensAccordingAOrd(last_sentence.getTokens(Layer.ANALAYTICAL));
		for (int i = 0; i < tokens.length; i++) {
			out.print(tokens[i].getString());
			out.print(' ');
		}
		out.println();
/**/
	    out.println("Last Sentence aTokens: " + last_sentence.getTokens(Layer.ANALAYTICAL).length);
	    out.println("Last Sentence num tTokens: " + last_sentence.getTokens(Layer.TECTO).length);
	    out.println("Last Sentence tTokens: ");
	    last_sentence.debug_printTokens(Layer.TECTO, out); 
	    out.println("-- tDependencies --");
	    debugPrintDependecies(last_sentence.getDependencies(Layer.TECTO), out); 
	    out.println("-- aux.rf --");
	    debugPrintDependecies(last_sentence.getAuxRfDependencies(), out);
/**/	     		
	}

	
}
