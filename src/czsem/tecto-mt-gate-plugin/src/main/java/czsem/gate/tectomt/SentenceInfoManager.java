package czsem.gate.tectomt;

import gate.Factory;
import gate.FeatureMap;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.NotImplementedException;

import czsem.gate.tectomt.TMTAnnotation.SeqAnnotation;

public class SentenceInfoManager extends SeqAnnotation
{
	public static abstract class Layer
	{		
		public Layer()
		{
			tokens = new ArrayList<Token>(30);
			dependencies = new ArrayList<Dependency>(30); 
		}

		protected List<Token> tokens;
		protected List<Dependency> dependencies;
		
		public static final int MORPHO = 0;  
		public static final int ANALAYTICAL = 1;  
		public static final int TECTO = 2;  
		public static final int NAMES = 3;
		
		public abstract Token newToken(String tmt_id);
		public Dependency newDependency(String parent_id, String child_id) {throw new NotImplementedException();}
	}

	private String sentence_string;
	private Layer [] layers;
	/** Maps token IDs to token information.*/
	private Map<String,Token> token_index = new HashMap<String, Token>(100);
	private Layer actual_layer = null;
	private Stack<Token> actual_token_stack = new Stack<Token>();;
	private List<Dependency> auxRfDependencies;

	public SentenceInfoManager()
	{		
		auxRfDependencies = new ArrayList<Dependency>(30);
		layers = new Layer[4];
		
		layers[Layer.TECTO] = new Layer()
		{			
			@Override
			public Token newToken(String tmt_id) {return new Token.tToken(tmt_id);}
			@Override
			public Dependency newDependency(String parent_id, String child_id) {
				return new Dependency(parent_id, child_id) {
					@Override
					public String getAnnotationType() {return "tDependency";}
				};
			}
		};
		layers[Layer.ANALAYTICAL] = new Layer()
		{			
			@Override
			public Token newToken(String tmt_id) {return new Token.aToken(tmt_id);}
			public Dependency newDependency(String parent_id, String child_id) {
				return new Dependency(parent_id, child_id) {
					@Override
					public String getAnnotationType() {return "aDependency";}
				};
			}
		};
		layers[Layer.MORPHO] = new Layer()
		{			
			@Override
			public Token newToken(String tmt_id) {return new Token.mToken(tmt_id);}
		};
		layers[Layer.NAMES] = new Layer()
		{			
			@Override
			public Token newToken(String tmt_id) {return new Token.nToken(tmt_id);}
		};
	}



	public void newToken(String parent_id, String child_id, boolean create_dependency)
	{
		Token toc = actual_layer.newToken(child_id);
		token_index.put(child_id, toc);
		actual_layer.tokens.add(toc);
		actual_token_stack.push(toc);

		if (create_dependency)
		{
			Dependency dep = actual_layer.newDependency(parent_id, child_id);
			actual_layer.dependencies.add(dep);
		}
		
	}

	public void newAuxRfDependency(String parent_id, String child_id) {
		auxRfDependencies.add(new Dependency(parent_id, child_id) {
			@Override
			public String getAnnotationType() {return "auxRfDependency";}
		});
	}

	public void addMRfDependency(String child_id)
	{
		actual_token_stack.peek().addMRfDependency(child_id);
	}

	public String[] getActualTokenFeatureList() {
		return actual_token_stack.empty() ? null : actual_token_stack.peek().getFeatureList();
	}

	public void updateActualTokenFeature(int feature_index,	String feature_value) {
		actual_token_stack.peek().features[feature_index] = feature_value;		
	}

	public void setActualLayer(int layer)
	{
		actual_layer = layers[layer];
		actual_token_stack.clear();
	}
	
	public void tryToCloseActualToken(String token_id)
	{	
		if (actual_token_stack.peek().tmt_id == token_id)
		actual_token_stack.pop();
	}

	public void setString(String sentence_string) {this.sentence_string = sentence_string;}

	@Override
	public String getString() {return sentence_string;}

	public Token[] getTokens(int layer) {
		return layers[layer].tokens.toArray(new Token[0]);
	}

	@Override
	public String getAnnotationType() {return "Sentence";}

	@Override
	public FeatureMap getFeatures() {return Factory.newFeatureMap();}

	public Token findToken(String token_id) {
		return token_index.get(token_id);
	}

	public List<Dependency> getDependencies(int layer) {
		return layers[layer].dependencies;
	}
	
	public List<Dependency> getAuxRfDependencies() {
		return auxRfDependencies;
	}
	
	public void debug_printTokens(int layer, PrintStream out)
	{
		for (Token token : layers[layer].tokens)			
		{
			out.println("### " + token.tmt_id + " ###");
			token.print(out);
		}
	}
}
