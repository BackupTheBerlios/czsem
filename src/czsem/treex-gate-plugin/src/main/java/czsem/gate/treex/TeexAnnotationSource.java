package czsem.gate.treex;

import gate.Annotation;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import czsem.gate.treex.Annotator.Annotable;
import czsem.gate.treex.Annotator.AnnotableDependency;
import czsem.gate.treex.Annotator.AnnotationSource;
import czsem.gate.treex.Annotator.Sentence;
import czsem.gate.treex.Annotator.SeqAnnotable;
import czsem.gate.treex.RecursiveEntityAnnotator.SecondaryEntity;

public class TeexAnnotationSource implements AnnotationSource {
	protected static class TreexNode implements Annotable {
		
		protected Map<String, Object> node;

		public TreexNode(Map<String, Object> node) {
			this.node = node;
		}

		@Override
		public String getAnnotationType() {			
			String type = (String) node.get("pml_type_name");
			return type.substring(0, type.indexOf(".type"));
		}

		@Override
		public FeatureMap getFeatures() {
			FeatureMap fm = Factory.newFeatureMap();
			for (String key : node.keySet())
			{
				if (! exclude_attrs.contains(key))
				{
					fm.put(key, node.get(key));
				}
			}
			return fm;
		}

		@Override
		public void setGateAnnId(Integer gate_annotation_id) {
			node.put("gateAnnId", gate_annotation_id);			
		}
		
	}
	
	protected class TreexNodeDependentEntity extends TreexNode implements SecondaryEntity {

		@Override
		public String getAnnotationType() {
			return super.getAnnotationType() + "_";
		}

		private Map<String, Object> parent;

		public TreexNodeDependentEntity(Map<String, Object> node, Map<String, Object> parent) {
			super(node);
			this.parent = parent;
		}

		@Override
		public boolean annotate(AnnotatorInterface annotator)	throws InvalidOffsetException {
			Annotation gAnn = annotator.getAnnotation((Integer) parent.get("gateAnnId"));
			annotator.annotate(this, gAnn.getStartNode().getOffset(), gAnn.getEndNode().getOffset());		
			return true;
		}
		
	}

	protected class TreexRelationDependentEntity extends TreexNode implements SecondaryEntity  {

		protected String relName;  
		
		public TreexRelationDependentEntity(Map<String, Object> node, String relName) {
			super(node);
			this.relName = relName;
		}

		@Override
		public boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException {
			String parentId = (String) node.get(relName);

			Map<String, Object> ref_node = nodeMap.get(parentId);
			Annotation gAnn = annotator.getAnnotation((Integer) ref_node.get("gateAnnId"));
			annotator.annotate(this, gAnn.getStartNode().getOffset(), gAnn.getEndNode().getOffset());
			
			return true;
		}		
	}

	protected class TreexMultiplyDependentEntity extends TreexRelationDependentEntity {
		public TreexMultiplyDependentEntity(Map<String, Object> node, String relName) {
			super(node, relName);
		}

		@Override
		public boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException {
			@SuppressWarnings("unchecked")
			List<String> parentIds = (List<String>) node.get(relName);			
			if (parentIds.isEmpty()) return true;
			
			Long startOffset = Long.MAX_VALUE;
			Long endOffset = Long.MIN_VALUE;

			for (String parentId : parentIds)
			{
				Map<String, Object> ref_node = nodeMap.get(parentId);
				Annotation gAnn = annotator.getAnnotation((Integer) ref_node.get("gateAnnId"));
				startOffset = Math.min(gAnn.getStartNode().getOffset(), startOffset);
				endOffset = Math.max(gAnn.getEndNode().getOffset(), endOffset);
			}

			annotator.annotate(this, startOffset, endOffset);
			
			return true;
		}		
	}

	protected class TreexDependencyFactory {
		public TreexDependency createGeneralDependency(String parentId, String childId, String depName) {
			if ((Boolean) nodeMap.get(parentId).get("czsemIsRoot")) return null;
			return new TreexDependency(parentId, childId, depName);
		}
		
		public TreexDependency createParentDependency(Map<String, Object> node) { 			
			TreexDependency ret = createIdAttrDependency(node, "parent_id");
			if (ret == null) return null;
			
			String type = (String) node.get("pml_type_name");
			ret.annType = type.charAt(0) + "Dependency";
			
			return ret;
		}
		
		public TreexDependency createIdAttrDependency(Map<String, Object> node, String depName) {
			return createGeneralDependency((String) node.get(depName), (String) node.get("id"), depName);}

		public List<TreexDependency> createListIdAttrDependencies(Map<String, Object> node, String depName) {
			
			@SuppressWarnings("unchecked")
			List<String> parentIds = (List<String>) node.get(depName);
			
			List<TreexDependency> ret = new ArrayList<TreexDependency>(parentIds.size());
			String childId = (String) node.get("id");
			
			for (String parentId : parentIds)
			{
				ret.add(createGeneralDependency(parentId, childId, depName));
			}
			
			return ret;
		}
	}

	protected class TreexDependency extends AnnotableDependency {
		protected String parentId;
		protected String childId;
		protected String annType;

		public TreexDependency(String parentId, String childId, String depName) {
			this.parentId = parentId;
			this.childId = childId;
			annType = depName;
		}

		@Override
		public String getAnnotationType() {
			return annType;
		}

		@Override
		public Integer getParentGateId() {
			return (Integer) nodeMap.get(parentId).get("gateAnnId");
		}

		@Override
		public Integer getChildGateId() {
			return (Integer) nodeMap.get(childId).get("gateAnnId");
		}
		
	}
	
	protected static class TreexToken extends TreexNode implements SeqAnnotable {

		public TreexToken(Map<String, Object> tokenMap) {
			super(tokenMap);
		}

		@Override
		public String getAnnotationType() {
			return "Token";
		}

		@Override
		public String getString() {
			return (String) node.get("form");
		}
		
	}

	protected class TreexSentence extends TreexNode implements Sentence {
		protected List<Map<String, Object>> nodes; 


		@SuppressWarnings("unchecked")
		public TreexSentence(Map<String, Object> zone) {
			super(zone);
			nodes = (List<Map<String, Object>>) zone.get("nodes");
		}

		@Override
		public String getString() {
			return (String) node.get("sentence");
		}

		@Override
		public String getAnnotationType() {
			return "Sentence";
		}

		@Override
		public Iterable<SeqAnnotable> getOrderedTokens() {
			List<Map<String, Object>> tocs = findSentenceTokenNodes();
			sortTokens(tocs);
			final Iterator<Map<String, Object>> iterator = tocs.iterator(); 
			return new Iterable<Annotator.SeqAnnotable>() {
				
				@Override
				public Iterator<SeqAnnotable> iterator() {
					return new Iterator<Annotator.SeqAnnotable>() {
						
						@Override
						public void remove() {
							throw new UnsupportedOperationException();							
						}
						
						@Override
						public SeqAnnotable next() {
							return new TreexToken(iterator.next());
						}
						
						@Override
						public boolean hasNext() {
							return iterator.hasNext();
						}
					};
				}
			};
		}

		protected List<Map<String, Object>> findSentenceTokenNodes() {
			List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();;
			
			for (Map<String, Object> node : nodes)
			{
				if (node.get("pml_type_name").equals("a-node.type")) ret.add(node);
				
			}
			return ret ;
		}
		
		@Override
		public void annotateSecondaryEntities(AnnotatorInterface annotator) throws InvalidOffsetException {
			RecursiveEntityAnnotator rea = new RecursiveEntityAnnotator();
			
			for (Map<String, Object> node : nodes) {
				addSecondaryEntities(rea, node);
			}
			
			rea.annotateSecondaryEntities(annotator);
		}
		
		public void addSecondaryEntities(RecursiveEntityAnnotator rea, Map<String, Object> nodeParam) {
			TreexDependencyFactory tdf = new TreexDependencyFactory();
			
			//lex.rf t-nodes
			if (nodeParam.get("a/lex.rf") != null) 
				rea.storeForLater(new TreexRelationDependentEntity(nodeParam, "a/lex.rf"));
			else {
				
				@SuppressWarnings("unchecked")
				List<String> arf = (List<String>) nodeParam.get("a.rf");
				
				if (arf != null && arf.size() > 0) { 
					//arf n-nodes
					rea.storeForLater(new TreexMultiplyDependentEntity(nodeParam, "a.rf"));
				}
				else if (nodeParam.get("gateAnnId") == null) {
					//remaining nodes
					rea.storeForLater(new TreexNodeDependentEntity(nodeParam, node));
				}
				
			}

			
			if (nodeParam.get("parent_id") != null) rea.storeForLater(tdf.createParentDependency(nodeParam));
			
			for (String idAttr : idAttrs)
			{
				if (nodeParam.get(idAttr) != null) rea.storeForLater(tdf.createIdAttrDependency(nodeParam, idAttr));
			}

			for (String idAttr : listAttrs)
			{
				if (nodeParam.get(idAttr) != null) rea.storeForLater(tdf.createListIdAttrDependencies(nodeParam, idAttr));
			}
		}
	}

	protected List<Map<String, Object>> zones;
	protected static Set<String> exclude_attrs;
	protected Map<String, Map<String, Object>> nodeMap;
	private Set<String> listAttrs;
	private Set<String> idAttrs;
	
	public TeexAnnotationSource(
			List<Map<String, Object>> zones, 
			Map<String, Map<String, Object>> nodeMap, 
			Set<String> exclude_attrs,
			Set<String> listAttrs,
			Set<String> idAttrs) 
	{
		this.zones = zones;
		this.nodeMap = nodeMap;
		this.listAttrs = listAttrs;
		this.idAttrs = idAttrs;
		TeexAnnotationSource.exclude_attrs = exclude_attrs;
		exclude_attrs.add("gateAnnId");
	}

	@Override
	public Iterable<Sentence> getOrderedSentences() {
		final Iterator<Map<String, Object>> zones_iterator = zones.iterator();
		return new Iterable<Annotator.Sentence>() {			
			@Override
			public Iterator<Sentence> iterator() {
				return new Iterator<Annotator.Sentence>() {					
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
					@Override
					public Sentence next() {
						return new TreexSentence(zones_iterator.next());
					}
					
					@Override
					public boolean hasNext() {
						return zones_iterator.hasNext();
					}
				};
			}
		};
	}

	public static void sortTokens(List<Map<String, Object>> tocs) {
		Collections.sort(tocs, new Comparator<Map<String, Object>>() {
	
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Integer ord1 = (Integer) o1.get("ord");
				Integer ord2 = (Integer) o2.get("ord");
				return  ord1.compareTo(ord2);
			}
		});
		
	}
}