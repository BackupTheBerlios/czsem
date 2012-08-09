package czsem.gate.tectomt;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import czsem.gate.GateUtils;

@Deprecated
public class TMTTreeAnnotator
{
	public static String[] a_token_features = 
    {
    		"m/form", "m/lemma", "m/tag", "afun", "ord" 	
    };

	
	public static String[] t_token_features = 
    {
    		"nodetype", "t_lemma", "functor", "deepord", "formeme",
    		"gram/sempos", "gram/degcmp", "gram/negation", "gram/gender", "gram/number",  
    		"gram/verbmod", "gram/deontmod", "gram/tense", "gram/aspect", "gram/resultative",
    		"gram/dispmod", "gram/iterativeness"
    };

	
	protected Node sentece_segment;
	protected Document document;
	protected AnnotationSet as;
	protected Node[] a_nodes_array; 
	protected Map<String, Integer> a_nodes_id_index;

	public TMTTreeAnnotator(Document document, Node sentece_segment)
	{
		this.document = document;
		this.sentece_segment = sentece_segment;
		as = document.getAnnotations();
	}
	
	public void initSortedANodesArray() throws XPathExpressionException
	{
    	NodeList a_nodes_ord = (NodeList) XPathExperssions.a_nodes_ord.evaluate(sentece_segment, XPathConstants.NODESET); 
        a_nodes_array = new Node[a_nodes_ord.getLength()]; 
        for (int j=0; j<a_nodes_array.length; j++)
        {
        	Node a_node_ord = a_nodes_ord.item(j);
        	int ord = Integer.parseInt(a_node_ord.getTextContent());
        	a_nodes_array[ord-1] = a_node_ord.getParentNode();            	
        }		
	}
	
	public static FeatureMap loadFeatures(String [] feature_names, XPathExpression [] x_expressions, Node node) throws XPathExpressionException
	{
    	FeatureMap fm = Factory.newFeatureMap();
    	for (int a=0; a<feature_names.length; a++)
    	{
        	String value = (String) x_expressions[a].evaluate(node, XPathConstants.STRING);
        	
    		if (! value.equals(""))	fm.put(feature_names[a], value);            		
    	}
    	return fm;
	}
	
//todo: optimize - share SequnceAnnotator 
	public void ATokensSequnceAnnotation(int start_index) throws XPathExpressionException, InvalidOffsetException
	{
        SequenceAnnotator token_sa = new SequenceAnnotator(document, start_index);    	
        a_nodes_id_index = new HashMap<String, Integer>(a_nodes_array.length);
        

        for (int j=0; j<a_nodes_array.length; j++)
        {
        	String token = (String) XPathExperssions.a_m_form.evaluate(a_nodes_array[j], XPathConstants.STRING);
        	System.out.print(token);            	
        	System.out.print(' ');
        	
        	token_sa.nextToken(token);
        	
        	Integer gate_id = as.add(token_sa.lastStart(), token_sa.lastEnd(), "Token", 
        			loadFeatures(a_token_features, XPathExperssions.a_token_features_paths, a_nodes_array[j]));
        	a_nodes_array[j].setUserData("id", gate_id, null);
        	String tmt_id = ((Element) a_nodes_array[j]).getAttribute("id");
        	a_nodes_id_index.put(tmt_id, gate_id);        	
        }
    	
        System.out.println();		
	}
	
	public void addDependencyAnnotation(Integer id1, Integer id2, String dependecy_type) throws InvalidOffsetException
	{
		Annotation a1 = as.get(id1);
		Annotation a2 = as.get(id2);
		
		Long ix1 = Math.min(a1.getStartNode().getOffset(), a2.getStartNode().getOffset());
		Long ix2 = Math.max(a1.getEndNode().getOffset(), a2.getEndNode().getOffset());

		as.add(ix1, ix2, dependecy_type, GateUtils.createDependencyArgsFeatureMap(id1, id2));
	}
	
	public void addADependencies(Node parent) throws XPathExpressionException, InvalidOffsetException
	{		
		NodeList children = (NodeList) XPathExperssions.children.evaluate(parent, XPathConstants.NODESET);
		
		for (int a=0; a<children.getLength(); a++)
		{
			Integer id1 = (Integer) parent.getUserData("id"); 
			Integer id2 = (Integer) children.item(a).getUserData("id"); 
			
			addDependencyAnnotation(id1, id2, "aDependency");
			
			addADependencies(children.item(a));
		}
	}


	public Integer addTAnnotation(Node t_node) throws XPathExpressionException, InvalidOffsetException
	{
		String a_lex_node_id = (String) XPathExperssions.lexrf.evaluate(t_node, XPathConstants.STRING);		
		Integer a_gate_annotation_id = a_nodes_id_index.get(a_lex_node_id);

		FeatureMap fm = loadFeatures(t_token_features, XPathExperssions.t_node_features_paths, t_node);
		fm.put("lexrf", a_gate_annotation_id);
		

		Integer new_annot_id = -1;
		
		if (a_gate_annotation_id == null)
		{
			 new_annot_id = as.add(new Long(0), new Long(0), "tNode", fm);			
		}
		else
		{
		
			 new_annot_id = as.add(
					as.get(a_gate_annotation_id).getStartNode(),
					as.get(a_gate_annotation_id).getEndNode(),
					"tNode", fm);
		}
		
		NodeList a_aux_nodes_ids = (NodeList) XPathExperssions.auxrf.evaluate(t_node, XPathConstants.NODESET);
		for (int a=0; a<a_aux_nodes_ids.getLength(); a++)
		{			
			addDependencyAnnotation(new_annot_id, a_nodes_id_index.get(
					a_aux_nodes_ids.item(a).getTextContent()), "aux_rf");
		}
		
		
		
		return new_annot_id;
	}

	public void addTNodesAndDependencies(Node child, Integer parent_id) throws XPathExpressionException, InvalidOffsetException
	{
		Integer child_id = addTAnnotation(child);
		addDependencyAnnotation(parent_id, child_id, "tDependency");

		NodeList children = (NodeList) XPathExperssions.children.evaluate(child, XPathConstants.NODESET);		
		for (int a=0; a<children.getLength(); a++)
		{
			addTNodesAndDependencies(children.item(a), child_id);
		}
	}

	public void addTNodesAndDependencies(Node parent) throws XPathExpressionException, InvalidOffsetException
	{
		Integer parent_id = addTAnnotation(parent);
		
		NodeList children = (NodeList) XPathExperssions.children.evaluate(parent, XPathConstants.NODESET);
		
		for (int a=0; a<children.getLength(); a++)
		{
			addTNodesAndDependencies(children.item(a), parent_id);
		}		
	}


	

}
