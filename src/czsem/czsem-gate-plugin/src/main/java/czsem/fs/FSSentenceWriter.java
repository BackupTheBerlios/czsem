package czsem.fs;

import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;

import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import czsem.fs.FSTreeWriter.NodeAttributes;
import czsem.utils.NetgraphConstants;

public class FSSentenceWriter
{
	public static String ordAttrName = NetgraphConstants.ORD_FEATURENAME;
	public static String hideAttrName = NetgraphConstants.HIDE_FEATURENAME;
	public static String idAttrName = NetgraphConstants.ID_FEATURENAME;
	
	public static class TokenDependecy {

		public TokenDependecy(String tokenTypeName, String depFeatureName) {
			this.tokenTypeName = tokenTypeName;
			this.depFeatureName = depFeatureName;
		}
		public String tokenTypeName;
		public String depFeatureName;
		
	}

	public static class Configuration {
		public Configuration(Iterable<String> dependencyNames,	Iterable<TokenDependecy> tokenDepDefs) {
			this.dependencyNames = dependencyNames;
			this.tokenDepDefs = tokenDepDefs;
		}
		
		Iterable<String> dependencyNames;
		Iterable<TokenDependecy> tokenDepDefs;
	};
	
	private AnnotationSet annotations;
	
	protected Configuration defaultConfig = 
		new Configuration (
			Arrays.asList(new String [] {
					"tDependency", "auxRfDependency", "Dependency", /* "aDependency" */}), 
			Arrays.asList(new TokenDependecy [] {
					new TokenDependecy("t-node", "lex.rf"),
					new TokenDependecy("tToken", "lex.rf"),})); 

	protected Configuration configuration = defaultConfig;
	
	private PrintWriter out;
	private FSTreeWriter tw;
	private Map<Integer, Integer> nodeOreder;

	protected NodeAttributes nodeAttributes = new NodeAttributes() {
		
		
		@Override
		public Iterable<Entry<String, Object>> get(int node_id) {
			
			FeatureMap fm = annotations.get(node_id).getFeatures();
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Set<Entry<String, Object>> f = (Set) fm.entrySet();

			ArrayList<Entry<String, Object>> ret = new ArrayList<Entry<String, Object>>(f);
			
			if (fm.get(ordAttrName) == null) ret.add(
					new AbstractMap.SimpleEntry<String, Object>(
							ordAttrName, nodeOreder.get(node_id)));
			
			if (fm.get(idAttrName) == null) {
				ret.add( new AbstractMap.SimpleEntry<String, Object>(
						idAttrName, node_id));				
			}
			
			if (fm.get(hideAttrName) == null) {
				String rootType = annotations.get(tw.getRootNode()).getType();
				if (! rootType.equals(annotations.get(node_id).getType()))
				{
					ret.add( new AbstractMap.SimpleEntry<String, Object>(
							hideAttrName, true));
				}				
			}
			
			return ret;
		}

		@Override
		public Object getValue(int node_id, String attrName) {
			return annotations.get(node_id).getFeatures().get(attrName);
		}
	};

	

	public FSSentenceWriter(AnnotationSet sentence_annotations, PrintWriter out)
	{
		this.out = out;		
		this.annotations = sentence_annotations;
		
		tw = new FSTreeWriter(out, nodeAttributes);
	}
		
	public void printTree()
	{		
		for (String depName : configuration.dependencyNames)
			tw.addDependecies(annotations.get(depName));

		for (TokenDependecy tocDep : configuration.tokenDepDefs)
			tw.addTokenDependecies(annotations.get(tocDep.tokenTypeName), tocDep.depFeatureName);
		
		setupNodeOrder();
		
		tw.printTree();
		out.println();
	}

	public class TokenOrderComprator implements Comparator<Integer>
	{
		public int compare(Annotation a1, Annotation a2) {
			return  a1.getStartNode().getOffset().compareTo(
					a2.getStartNode().getOffset());
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			Annotation a1 = annotations.get(o1);
			Annotation a2 = annotations.get(o2);
			return compare(a1, a2);
		}		
	}

	
	protected void setupNodeOrder() {
		Integer[] nodes = tw.getAllNodes().toArray(new Integer[0]);
		
		Arrays.sort(nodes, new TokenOrderComprator());
		
		nodeOreder = new HashMap<Integer, Integer>(nodes.length);
		
		for (int i = 0; i < nodes.length; i++) {
			nodeOreder.put(nodes[i], i);
		}
	}

	public Set<String> getAttributes() {
		return tw.getAttributes();
	}
	
}
