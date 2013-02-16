package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import czsem.Utils;
import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.TreeIndex;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.NodeMatch;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQuery.QueryObject;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;
import czsem.gate.utils.GateAwareTreeIndex;

@CreoleResource(name = "czsem NetgraphQueryEval", comment = 
"Evaluates a Netgrpah query on input documents and creates correponding annotations to query nodes with filled '_name' attribute.")
public class NetgraphQueryEval extends AbstractLanguageAnalyserWithInputOutputAS {
	private static final long serialVersionUID = 8077171868187327054L;

	protected String queryString = null;

	protected QueryObject queryObject;
	
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		try {
			queryObject = FSQuery.buildQuery(getQueryString());
		} catch (SyntaxError e) {
			throw new ResourceInstantiationException(e);
		}
		return super.init();
	}
	
	public static String encodeIntAlpha(int h){
		int mod = ('Z'-'A'+1) * 2;
		if (h < 0) h = -h;
		
		StringBuilder sb = new StringBuilder();
		
		do {
			int resid = h % mod;
			
			if (resid > 'Z'-'A')
			{
				sb.insert(0, (char) ('a'+resid-mod/2));								
			} else {
				sb.insert(0, (char) ('A'+resid));				
			}
			
			h /= mod;			
		} while (h > 0);

		
		return sb.toString();		
	}

	public static String buildQueryStringHash(String qString){		
		return encodeIntAlpha(qString.hashCode());		
	}
	
	public static void main (String [] args) {
		System.err.println(buildQueryStringHash("aabbaabbaabb"));
		System.err.println(buildQueryStringHash("aabbaabbaa"));
		System.err.println(buildQueryStringHash("bb"));
		System.err.println(buildQueryStringHash(""));
		System.err.println(buildQueryStringHash("aabbxaabbaabb"));
		System.err.println(buildQueryStringHash("aafbbaabbaa"));
		System.err.println(buildQueryStringHash("bnb"));
		System.err.println(buildQueryStringHash("Q"));
		System.err.println("----");
		for (int a=0; a < 140; a++)
		{
			System.err.format("%s, ", encodeIntAlpha(a));
		}
	}
	
	@Override
	public void execute() throws ExecutionException
	{
		initBeforeExecute();
						
		AnnotationSet tokensAndDependenciesAS = inputAS;
		TreeIndex index = new GateAwareTreeIndex(tokensAndDependenciesAS.get(null, Utils.setFromArray(new String [] {"args"})));

		QueryData data = new QueryData(index, new GateAnnotationsNodeAttributes(tokensAndDependenciesAS));
		
		Iterable<QueryMatch> results = queryObject.evaluate(data);
		
		int queryMatchOrd = 0;
		
		for (QueryMatch result : results)
		{
			for (NodeMatch match : result.getMatchingNodes())
			{
				String name = match.getQueryNode().getName();
				if (name != null)
				{
					Annotation matchingAnnot = tokensAndDependenciesAS.get(match.getNodeId());
					FeatureMap fm = Factory.newFeatureMap();
					fm.put("matchingNodeId", match.getNodeId());
					fm.put("queryMatchId", String.format("%s_%03d", buildQueryStringHash(getQueryString()), queryMatchOrd++));
					outputAS.add(
							matchingAnnot.getStartNode(),
							matchingAnnot.getEndNode(),
							name, fm);					
				}
			}			
		}
	}

	public String getQueryString() {
		return queryString;
	}

	@CreoleParameter(comment="String representation of the Netgraph query.", defaultValue="[string~=\\[0-9\\]*,_name=numberParent]([_name=child])")
	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

}
