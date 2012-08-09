package czsem.gate;

import gate.Annotation;
import gate.AnnotationSet;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import czsem.Utils;
import czsem.gate.utils.TreeIndex;
import czsem.utils.NetgraphConstants;

public class SentenceFSWriter
{
	private AnnotationSet annotations;
	private String [] attributes = null;
	
	private PrintStream out = System.out;

	public static class TreeBuilder extends TreeIndex
	{
		private PrintStream out;
		private AnnotationSet annotations;
		private String [] attributes;

		private int root_id; 		

		public TreeBuilder(AnnotationSet annotations) {
			this.annotations = annotations;
		}


		private void printCildren(int father_id)
		{
			Iterable<Integer> childern = getChildren(father_id);
			if (childern == null) return;

			char delim = '('; 
			for (int child_id : childern)
			{
				out.print(delim);
				delim = ',';
				printNode(child_id);
			}			
			if (delim == ',') out.print(')');
		}

		
		private void printAttribute(String attr_name, Object attr_value)		
		{
			out.print(attr_name);
			out.print('=');
			
			String str_value = attr_value.toString();
			String functional_chars = "\\=,[]|<>!";
			
			for (int i=0; i<str_value.length(); i++)
			{
				char ch = str_value.charAt(i);
				if (functional_chars.indexOf(ch) != -1) out.print('\\');
				out.print(ch);
			}
		}
		
		private void printNode(int node_id)
		{
			Annotation node = annotations.get(node_id);		
			if (node == null) return;
			
			out.print('[');
						
			for (int a=0; a<attributes.length; a++)
			{
				Object f = node.getFeatures().get(attributes[a]);

				//print ID
				if (attributes[a].equals(NetgraphConstants.ID_FEATURENAME)) f = node_id;
								
				if (f != null)
				{
					printAttribute(attributes[a], f);
					if (a+1<attributes.length) out.print(',');										
				}
				
			}
			out.print(']');
					
			printCildren(node_id);
		}

/*		
		public void printNode(int id, String prefix)
		{
			out.print(prefix);
			out.println(id);
			
			List<Integer> children = dependencies.get(id);
			
			if (children == null) return;
			
			for (Integer child : children)
			{
				printNode(child, prefix+ "   ");				
			}
		}
*/		
		public void printTree(PrintStream out, String [] attributes)
		{
			this.out = out;
			this.attributes = attributes;
			
//			out.print("Dependencies: ");			
//			out.println(dependencies.keySet().size());
						
			root_id = findRoot();
			
			updateSentenceTokens();

			printNode(root_id);

		}
		
		public static class tokenOrderComprator implements Comparator<Annotation>
		{

			@Override
			public int compare(Annotation a1, Annotation a2) {
				return  a1.getStartNode().getOffset().compareTo(
						a2.getStartNode().getOffset());
			}
			
		}
		
		public void updateSentenceTokens()
		{
			List<Annotation> token_id_list = getAllCildrenAnnotations(annotations);
			token_id_list.add(annotations.get(root_id));
			
			
			Annotation [] token_annots = token_id_list.toArray(new Annotation[0]);
			
			numberSentenceTokens(token_annots);
			hideSentenceTokens(token_annots);									
		}

		private void hideSentenceTokens(Annotation[] token_annots)
		{
			String root_type = annotations.get(root_id).getType();
			
			for (int i = 0; i < token_annots.length; i++)
			{
				if (! token_annots[i].getType().equals(root_type))
				{
					token_annots[i].getFeatures().put(NetgraphConstants.HIDE_FEATURENAME, true);
				}
			}			
		}

		private void numberSentenceTokens(Annotation [] token_annots)
		{
			Arrays.sort(token_annots, new tokenOrderComprator());
			
			for (int i = 0; i < token_annots.length; i++) {
				token_annots[i].getFeatures().put(NetgraphConstants.ORD_FEATURENAME, i);				
			}
		}


	}
	
/*	
	private void initAttributes(String [] additional_attributes)
	{
		attributes = GateUtils.arrayConcatenate(FSFileWriter.default_attributes, additional_attributes);			
	}
*/	
	public SentenceFSWriter(AnnotationSet sentence_annotations, PrintStream out)
	{
		this(sentence_annotations, out, FSFileWriter.guessAtttributes(sentence_annotations));

		/*
		String[] attrs = guessAtttributes();
		
		initAttributes(attrs);
		*/
	}

	public SentenceFSWriter(AnnotationSet sentence_annotations, PrintStream out, String [] attributes)
	{
		this.out = out;		
		this.annotations = sentence_annotations;
		this.attributes = attributes;
		
//		initAttributes(additional_attributes.toArray(new String[0]));
		
//		dependenciesAS = annotations.get("Dependency");
		

/*		
		for (int a=0; a<FSFileWriter.dependency_annotation_types.length; a++)
		{
			TreeBuilder tb = new TreeBuilder();
			
			AnnotationSet dependenciesAS = sentence_annotations.get(
					setFromArray(FSFileWriter.dependency_annotation_types[a]));
		
			tb.fillDependecies(dependenciesAS);
			

			if (FSFileWriter.tokendependency_annotation_types[a] != null)
			{
				tb.fillTokenDependecies(sentence_annotations.get(FSFileWriter.token_annotation_types[a]), 
						FSFileWriter.tokendependency_annotation_types[a]);
			}
			
			tb.printTree(System.err, attrs, annotations);
		}
*/		
	}
		
	/**
	 * @param dependency_annotation_type 
	 * @see FSFileWriter#dependency_annotation_types
	 * @return true - if a tree is printed, false - if the tree cannot be printed with the given dependency_annotation_type 
	 */
	public boolean printTree(int dependency_annotation_type)
	{
//		for (int dependency_annotation_type=0; dependency_annotation_type<FSFileWriter.dependency_annotation_types.length; dependency_annotation_type++)
//		{
			
			AnnotationSet dependenciesAS = annotations.get(
					Utils.setFromArray(
							FSFileWriter.dependency_annotation_types[dependency_annotation_type]));
			
			if (dependenciesAS.isEmpty()) return false;		
			TreeBuilder tb = new TreeBuilder(annotations);
			tb.fillDependecies(dependenciesAS);
			

			if (FSFileWriter.tokendependency_annotation_types[dependency_annotation_type] != null)
			{
				tb.fillTokenDependecies(annotations.get(FSFileWriter.token_annotation_types[dependency_annotation_type]), 
						FSFileWriter.tokendependency_annotation_types[dependency_annotation_type]);
			}
			
			
//			if (attributes == null) initAttributes(guessAtttributes());

			
			tb.printTree(out, attributes);
//		}

		
		out.println();
		return true;
	}

	public String[] getAttributes() {
		return attributes;
	}
	
}
