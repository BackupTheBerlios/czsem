package czsem.gate.tectomt;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import czsem.gate.plugins.AnnotationDependencySubtreeMarker.SubtreeMarkInfo;
import czsem.gate.tectomt.SentenceInfoManager.Layer;
import czsem.gate.tectomt.TMTAnnotation.SeqAnnotation;

public abstract class Token extends SeqAnnotation 
{
	public static class mToken extends Token
	{
		protected static final String[] feature_list = 
	    {
    		"form", "lemma", "tag"
	    };

		@Override
		public String getAnnotationType() {	return "Token";}

		@Override
		protected final String[] getFeatureList() {return feature_list;}

		public mToken(String tmt_id) {super(feature_list.length, tmt_id);}
		
		@Override
		public String getString() {return getAForm();}
	}

	
	public static class aToken extends Token
	{
		protected static final String[] feature_list = 
	    {
    		"form", "lemma", "tag", "afun", "ord" 	
	    };

		@Override
		public String getAnnotationType() {	return "Token";}

		@Override
		protected final String[] getFeatureList() {return feature_list;}

		public aToken(String tmt_id) {super(feature_list.length, tmt_id);}
		
		@Override
		public String getString() {return getAForm();}

		@Override
		public void annotate(AnnotationSet as, SentenceInfoManager sentence) throws InvalidOffsetException
		{
			assert mRfDependecies.size() == 1;
			String mt_id = mRfDependecies.get(0);
			Token mt = sentence.findToken(mt_id);
			gate_annotation_id = mt.gate_annotation_id;
			
			FeatureMap fm;			
			if (gate_annotation_id == null) fm = Factory.newFeatureMap();
			else fm	= as.get(gate_annotation_id).getFeatures();
			
			fm.putAll(getFeatures());
		}
	}
	
	
	public static class tToken extends Token
	{
		protected Integer gate_lexref = null;
		
		protected static final String[] feature_list = 
	    {
	    		"nodetype", "t_lemma", "functor", "deepord", "formeme",
	    		"sempos", "degcmp", "negation", "gender", "number",  
	    		"verbmod", "deontmod", "tense", "aspect", "resultative",
	    		"dispmod", "iterativeness", "lex.rf"
	    };

		public tToken(String tmt_id) {super(feature_list.length, tmt_id);}

		@Override
		public String getAnnotationType() {	return "tToken";}

		@Override
		protected final String[] getFeatureList() {return feature_list;}
		
		@Override
		public String getString() { throw new NotImplementedException();}

		@Override
		public void annotate(AnnotationSet as, SequenceAnnotator seq_anot) { throw new NotImplementedException();}

		@Override
		public void annotate(AnnotationSet as, SentenceInfoManager sentence) throws InvalidOffsetException
		{
			Token a_toknen = sentence.findToken(getTLexRf());
									
			Annotation a;
			if (a_toknen == null)
					a = as.get(sentence.gate_annotation_id);
			else
			{
					a = as.get(a_toknen.gate_annotation_id);
					
					gate_lexref = a_toknen.gate_annotation_id;
			}

			
			if (a == null)
			{
				a = as.get(sentence.getTokens(Layer.ANALAYTICAL)[0].gate_annotation_id);
			}
			
			Long start_off = a.getStartNode().getOffset();
			Long end_off = a.getEndNode().getOffset();
			
			annotate(as, start_off, end_off);			
		}

		@Override
		public FeatureMap getFeatures() {
			FeatureMap fm = super.getFeatures();
//			if (gate_lexref < 1) System.err.println(gate_lexref);
//			if (gate_lexref > 830) System.err.println(gate_lexref);
			if (gate_lexref != null)
			{
				String old_id = (String) fm.put("lex.rf", gate_lexref);
				assert old_id.equals(getTLexRf());
			}
			return fm;
		}
	};
	
	public static class nToken extends Token
	{
		protected static final String[] feature_list = 
	    {
    		"ne_type", "normalized_name"
	    };

		@Override
		public String getAnnotationType() {	return "NamedEntity";}

		@Override
		protected final String[] getFeatureList() {return feature_list;}

		public nToken(String tmt_id) {super(feature_list.length, tmt_id);}
		
		@Override
		public String getString() {return getAForm();}

		@Override
		public void annotate(AnnotationSet as, SentenceInfoManager sentence) throws InvalidOffsetException
		{
			
			SubtreeMarkInfo info = new SubtreeMarkInfo();
			for (String token_tmt_id : mRfDependecies)
			{
				Integer gate_id = sentence.findToken(token_tmt_id).gate_annotation_id;
				info.mergeWith(new SubtreeMarkInfo(as.get(gate_id)));			
			}
			
			annotate(as, info.start_offset, info.end_offset);

		}
	}
	

	
	
	protected String [] features;
	protected List<String> mRfDependecies = new ArrayList<String>(2);
	
	protected abstract String [] getFeatureList();

	@Override
	public FeatureMap getFeatures()
	{
    	FeatureMap fm = Factory.newFeatureMap();
    	for (int a=0; a<getFeatureList().length; a++)
    	{
        	String value = features[a];
        	
    		if (value != null)	fm.put(getFeatureList()[a], value);            		
    	}
    	return fm;
	}
	
	public Token(int fetures_num, String tmt_id)
	{
		this(fetures_num);
		this.tmt_id = tmt_id;
	}

	public Token(int fetures_num)
	{
		features = new String[fetures_num] ;
	}
	
	public void print(PrintStream out)
	{
		for (int i = 0; i < features.length; i++) {
			if (features[i] == null) out.println("null");
			else out.println(features[i]);				
		}
		for (String child : mRfDependecies) {
			out.println(child);			
		}
	}

	public String getTLexRf()
	{
		return features[17]; 
	}

	public int getAOrd()
	{
		return Integer.parseInt(features[4]); 
	}

	public String getAForm()
	{
		return features[0]; 
	}
	
	public void addMRfDependency(String child_id) {
		mRfDependecies.add(child_id);
	}


	


}
