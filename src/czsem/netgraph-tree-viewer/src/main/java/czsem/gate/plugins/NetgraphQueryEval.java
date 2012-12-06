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
	
	@Override
	public void execute() throws ExecutionException
	{
		initBeforeExecute();
						
		AnnotationSet tokensAndDependenciesAS = inputAS;
		TreeIndex index = new GateAwareTreeIndex(tokensAndDependenciesAS.get(null, Utils.setFromArray(new String [] {"args"})));

		QueryData data = new QueryData(index, new GateAnnotationsNodeAttributes(tokensAndDependenciesAS));
		
		Iterable<QueryMatch> results = queryObject.evaluate(data);
		
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
