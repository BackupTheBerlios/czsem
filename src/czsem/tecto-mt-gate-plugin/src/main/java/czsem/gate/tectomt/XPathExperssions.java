package czsem.gate.tectomt;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

@Deprecated
public class XPathExperssions
{
    public static XPathExpression sentece_segments; 
    public static XPathExpression sentece_text;
    public static XPathExpression a_nodes_ord; 
    public static XPathExpression a_m_form;
    public static XPathExpression a_root;
    public static XPathExpression t_root;
    public static XPathExpression children;
    public static XPathExpression lexrf;
    public static XPathExpression auxrf;
    
    public static XPathExpression[]  a_token_features_paths;
    public static XPathExpression[]  t_node_features_paths;

	public static void init() throws XPathExpressionException
	{
		XPath xpath = XPathFactory.newInstance().newXPath();

        sentece_segments = xpath.compile("/tmt_document/bundles[1]/LM"); 
        sentece_text = xpath.compile("czech_source_sentence"); 
        a_nodes_ord = xpath.compile("trees/SCzechA//LM/ord"); 
        a_m_form = xpath.compile("m/form");
        a_root = xpath.compile("trees/SCzechA/children/LM");
        t_root = xpath.compile("trees/SCzechT/children/LM");
		children = xpath.compile("children/LM");
		lexrf = xpath.compile("a/lex.rf");
		auxrf = xpath.compile("a/aux.rf/LM");
		
		a_token_features_paths = new XPathExpression[TMTTreeAnnotator.a_token_features.length];
        for (int a=0; a<TMTTreeAnnotator.a_token_features.length;a++)
        	a_token_features_paths[a] = xpath.compile(TMTTreeAnnotator.a_token_features[a]);

		t_node_features_paths = new XPathExpression[TMTTreeAnnotator.t_token_features.length];
        for (int a=0; a<TMTTreeAnnotator.t_token_features.length;a++)
        	t_node_features_paths[a] = xpath.compile(TMTTreeAnnotator.t_token_features[a]);
	}
}
