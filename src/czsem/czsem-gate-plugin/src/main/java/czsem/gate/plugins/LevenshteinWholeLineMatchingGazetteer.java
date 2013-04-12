package czsem.gate.plugins;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.creole.gazetteer.DefaultGazetteer;
import gate.creole.gazetteer.FSMState;
import gate.creole.gazetteer.GazetteerList;
import gate.creole.gazetteer.GazetteerNode;
import gate.creole.gazetteer.LinearNode;
import gate.creole.gazetteer.Lookup;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.HiddenCreoleParameter;
import gate.creole.metadata.RunTime;
import gate.gui.MainFrame;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import czsem.gate.learning.PRSetup;
import czsem.gate.learning.PRSetup.SinglePRSetup;
import czsem.gate.utils.GateUtils;

@CreoleResource
public class LevenshteinWholeLineMatchingGazetteer extends DefaultGazetteer {

	/** This parameter is disabled **/
	@Override
	@HiddenCreoleParameter
	public void setLongestMatchOnly(Boolean longestMatchOnly) {
		super.setLongestMatchOnly(longestMatchOnly);
	}

	/** This parameter is disabled **/
	@Override
	@HiddenCreoleParameter
	public void setWholeWordsOnly(Boolean wholeWordsOnly) {
		super.setWholeWordsOnly(wholeWordsOnly);
	}

	private static final long serialVersionUID = 2828791942110531799L;
	
	protected Map<LinearNode,Map<String,Lookup>> lookups = new HashMap<LinearNode, Map<String,Lookup>>();
	private Map<String, Lookup> currentlyReadingNodeMap;
	
	private double minDistance = 0.2; 
	private boolean removeAllSpaces = false;
	private boolean removeRedundantSpaces = true;
	
	@Override
	protected void readList(LinearNode node, boolean add) throws ResourceInstantiationException {
		currentlyReadingNodeMap = new HashMap<String, Lookup>();
		lookups.put(node, currentlyReadingNodeMap);
		super.readList(node, add);
	}

	@Override
	public void addLookup(String text, Lookup lookup) {
		currentlyReadingNodeMap.put(text, lookup);
		super.addLookup(text, lookup);
	}
	
	public static class StringLineIterator implements Iterator<String> {
		private String content;
		
		private int lastStrat = -1; 
		private int lastEnd = -1; 

		public StringLineIterator(String content) {
			this.content = content;
		}

		@Override
		public boolean hasNext() {
			return lastEnd < content.length();
		}

		@Override
		public String next() {
			lastStrat = lastEnd+1;
			lastEnd = content.indexOf('\n', lastStrat);
			
			if (lastEnd == -1) {
				lastEnd = content.length();
			}
			
			return content.substring(lastStrat, lastEnd);
		}

		@Override
		public void remove() {
			
		}

		public int getLastStrat() {
			return lastStrat;
		}


		public int getLastEnd() {
			return lastEnd;
		}


	}

	
	@Override
	public void execute() throws ExecutionException {
	    interrupted = false;
	    AnnotationSet annotationSet;
	    //check the input
	    if(document == null) {
	      throw new ExecutionException(
	        "No document to process!"
	      );
	    }

	    if(annotationSetName == null ||
	       annotationSetName.equals("")) annotationSet = document.getAnnotations();
	    else annotationSet = document.getAnnotations(annotationSetName);

	    fireStatusChanged("Performing look-up in " + document.getName() + "...");
	    String content = document.getContent().toString();
	    
	    StringLineIterator iter = new StringLineIterator(content);
	    
	    while (iter.hasNext()) {
			String srcText = iter.next();

			for (Object objNode : definition) {
	    		LinearNode curNode = (LinearNode) objNode;
				GazetteerList list = (GazetteerList) definition.getListsByNode().get(objNode);
				
				double minDistance = Double.MAX_VALUE;
				FSMState minState = null;
				
				for (Object entry : list) {
					GazetteerNode gEntry = (GazetteerNode) entry;
					String entryText = gEntry.getEntry();
					
					
					double currentApplicableMin = Math.min(minDistance, getMinDistance());
					Distance distance = countDistanceOptimized(srcText, entryText, currentApplicableMin); 							
					
					if (distance.normalizeDistance <= currentApplicableMin)
					{					
						
						FSMState state = new FSMState(this);
						Lookup lookup = lookups.get(curNode).get(gEntry.getEntry());
						
						@SuppressWarnings("unchecked")
						Map<Object, Object> f = lookup.features;						
						f.put("distance", distance.diatnce);
						f.put("normalizedDistance", distance.normalizeDistance);
						f.put("matchedText", entryText);
						/* debug*/
						f.put("srcText", srcText);
						/**/
						
						state.addLookup(lookup);					

						minDistance = distance.normalizeDistance;
						minState = state;
					}
				}
				
				if (minDistance <= getMinDistance())				{
					createLookups(minState, iter.getLastStrat(), iter.getLastEnd()-1, annotationSet);
				}
			}
			if(isInterrupted()) throw new ExecutionInterruptedException(
		            "The execution of the " + getName() +
		            " gazetteer has been abruptly interrupted!");
		}

	    fireProcessFinished();
	    fireStatusChanged("Look-up complete!");
	}

	public static double normalizeDistance(int distance, int maxLength) {
		return distance / (double) maxLength;
	}
	
	public static class Distance {
		int diatnce;
		double normalizeDistance;
		String text1, text2;

		public Distance(int diatnce, double normalizeDistance, String text1, String text2) {
			this.diatnce = diatnce;
			this.normalizeDistance = normalizeDistance;
			this.text1 = text1;
			this.text2 = text2;
		}
	}
	
	public Distance countDistanceOptimized(String text1, String text2, double minInterstingNormalizedDistance) {
		
		//if( Character.isSpaceChar(currentChar) || Character.isWhitespace(currentChar) )
		
		if (removeAllSpaces)
		{
			text1 = text1.replaceAll("[\\s\\u00a0]", "");
			text2 = text2.replaceAll("[\\s\\u00a0]", "");
		} else {
			if (removeRedundantSpaces) {
				text1 = text1.replaceAll("[\\s\\u00a0]+", " ");				
				text2 = text2.replaceAll("[\\s\\u00a0]+", " ");				
			}
		}
		
		int l1 = text1.length(); int l2 = text2.length();
		int lMin, lMax;		
		if (l1 > l2) {
			lMin = l2; lMax = l1;
		} else {
			lMin = l1; lMax = l2;			
		}
		
		int minDistance = lMax - lMin;
		double minNormDistance = normalizeDistance(minDistance, lMax);
		
		if (minNormDistance > minInterstingNormalizedDistance) 
			return new Distance(minDistance, minNormDistance, text1, text2);
			
		if (! caseSensitive) {
			text1 = text1.toLowerCase();
			text2 = text2.toLowerCase();
		}
		
		int countedDist = StringUtils.getLevenshteinDistance(text1, text2);
		return new Distance(countedDist, 
				normalizeDistance(countedDist, lMax), text1, text2);
	}

	@CreoleParameter(defaultValue="0.2")
	@RunTime
	public void setMinDistance(Double minDistance) {
		this.minDistance = minDistance;
	}

	public Double getMinDistance() {
		return minDistance;
	}

	public Boolean getRemoveAllSpaces() {
		return removeAllSpaces;
	}

	@CreoleParameter(defaultValue="false")
	@RunTime
	public void setRemoveAllSpaces(Boolean removeAllSpaces) {
		this.removeAllSpaces = removeAllSpaces;
	}

	public Boolean getRemoveRedundantSpaces() {
		return removeRedundantSpaces;
	}

	@CreoleParameter(defaultValue="true")
	@RunTime
	public void setRemoveRedundantSpaces(Boolean removeRedundentSpaces) {
		this.removeRedundantSpaces = removeRedundentSpaces;
	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(LevenshteinWholeLineMatchingGazetteer.class);
		
		MainFrame.getInstance().setVisible(true);
		
		PRSetup [] s = {
				new SinglePRSetup(LevenshteinWholeLineMatchingGazetteer.class)
			.putFeature("minDistance", 0.1) 
			.putFeature(DEF_GAZ_CASE_SENSITIVE_PARAMETER_NAME, false) 
			.putFeature(DEF_GAZ_LISTS_URL_PARAMETER_NAME, 
					new File("C:/Users/dedek/Desktop/DATLOWE/gazetteer/datlowe_gaz_nolemma.def").toURI().toURL())
			.putFeature(DEF_GAZ_FEATURE_SEPARATOR_PARAMETER_NAME, "|"),
		};
		
		SerialAnalyserController pipe = PRSetup.buildGatePipeline(s, "gaz");
		
		Corpus c = Factory.newCorpus("gaz");
		String docStr =
				"9. DATUM PRVNÍ REGISTRACE/PRODLOUŽENÍ REGISTRACE|order=27|heading_number=9\n" +
				"9. DATUM PRVNÍ REGISTRACE / PRODLOUŽENÍ REGISTRACE|order=27|heading_number=9\n" +
				"9. DATUM PRVNÍ REGISTRACE A DATUM PRODLOUŽENÍ REGISTRACE|order=27|heading_number=9\n" +
				"9. datum první registrace/prodloužení registrace";
		Document d = Factory.newDocument(docStr);
		c.add(d);
		pipe.setCorpus(c);
		pipe.execute();
		
		System.err.println(d.getAnnotations());
	}

}
