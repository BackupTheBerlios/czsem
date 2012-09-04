package czsem.gate.treex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gate.Annotation;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;
import czsem.gate.treex.Annotator.Annotable;
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
	
	protected class TreexEntity extends TreexNode implements SecondaryEntity {

		public TreexEntity(Map<String, Object> node) {
			super(node);
		}

		@Override
		public boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException {
			String a_lexrf = (String) node.get("a/lex.rf");
			if (a_lexrf != null)
			{
				Map<String, Object> ref_node = nodeMap.get(a_lexrf);
				Annotation gAnn = annotator.getAnnotation((Integer) ref_node.get("gateAnnId"));
				annotator.annotate(this, gAnn.getStartNode().getOffset(), gAnn.getEndNode().getOffset());
			}
			return true;
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
			final Deque<Map<String, Object>> nodeQueue = new LinkedList<Map<String,Object>>(nodes);
			
			RecursiveEntityAnnotator rea = new RecursiveEntityAnnotator() {
				
				@Override
				protected void storeForLater(SecondaryEntity entity) {
					TreexEntity te = (TreexEntity) entity;
					nodeQueue.addLast(te.node);					
				}
				
				@Override
				protected SecondaryEntity getNextUnprocessedEntity() {
					for(;;)
					{
						Map<String, Object> node = nodeQueue.pollFirst();
						if (node == null) return null;
						if (node.get("gateAnnId") == null) return new TreexEntity(node);
					}
				}
			};
			
			rea.annotateSecondaryEntities(annotator);
		}
		
		
		
	}

	protected List<Map<String, Object>> zones;
	protected static Set<String> exclude_attrs;
	protected Map<String, Map<String, Object>> nodeMap;
	
	public TeexAnnotationSource(List<Map<String, Object>> zones, Map<String, Map<String, Object>> nodeMap, Set<String> exclude_attrs) {
		this.zones = zones;
		this.nodeMap = nodeMap;
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
