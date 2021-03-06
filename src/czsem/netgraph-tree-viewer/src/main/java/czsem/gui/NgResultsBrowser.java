package czsem.gui;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import czsem.fs.DependencyConfiguration;
import czsem.fs.FSSentenceStringBuilder;
import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryData;
import czsem.fs.query.FSQuery.QueryMatch;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.gate.utils.GateAwareTreeIndex;
import czsem.gate.utils.GateUtils;

public class NgResultsBrowser extends Container {
	private static final long serialVersionUID = -6986937309062818005L;

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		
		
		String fileName = "../intlib/documents/ucto.gate.xml";

		/**/
		System.err.println("reading doc: " + fileName);
		long time = System.currentTimeMillis();
		Document doc = Factory.newDocument(new File(fileName).toURI().toURL(), "utf8");
		System.err.println("reading finished");
		
		GateAwareTreeIndex index = new GateAwareTreeIndex();
		
		System.err.println("fillnig index");
		
		AnnotationSet mainAs = doc.getAnnotations("tmt4");
		index.addDependecies(mainAs, DependencyConfiguration.getSelectedConfigurationFromConfigOrDefault());			
		System.err.format("finished in: %10.3fs\n", (System.currentTimeMillis() - time) * 0.001);
		
		
		QueryData data = new FSQuery.QueryData(index, new GateAnnotationsNodeAttributes(mainAs));
		
//		Iterable<QueryMatch> results = q.buildQuery("[lex.rf=32154]))").evaluate();
		Iterable<QueryMatch> results = FSQuery.buildQuery("[t_lemma=být]([t_lemma=povinný]([t_lemma=vést]))").evaluate(data);
//		Iterable<QueryMatch> results = q.buildQuery("[t_lemma=být]([]([t_lemma=2]),[t_lemma=povinný]([t_lemma=vést]))").evaluate();
		
		JFrame fr = new JFrame(NgResultsBrowser.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
	    NgResultsBrowser qd = new NgResultsBrowser();
		qd.initComponents();
		
		qd.asIndexHelper.setSourceAS(mainAs);
		qd.setResults(results);
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);
	}

	private TreeVisualize treeVisualize;

	protected ResultsWalker resultsWalker;

	public AsIndexHelper asIndexHelper = new AsIndexHelper(); 

	private JButton buttonNext;

	private JButton buttonPrevious;
	
	protected static class ResultsWalker {
		
		private Iterator<QueryMatch> results;
		private ListIterator<QueryMatch> cachePos;
		private int currentIndex = -1;

		public ResultsWalker(Iterator<QueryMatch> results) {
			this.results = results;
			LinkedList<QueryMatch> cache = new LinkedList<FSQuery.QueryMatch>();
			cachePos = cache.listIterator();
		}

		public boolean hasPrevious() {
			return cachePos.hasPrevious();
		}

		public QueryMatch previous() {
			if (cachePos.previousIndex() == currentIndex) cachePos.previous();
			currentIndex = cachePos.previousIndex();			
			return cachePos.previous();
		}

		public boolean hasNext() {
			if (cachePos.hasNext()) return true;
			if (results.hasNext()) return true;
			return false;
		}

		public QueryMatch next() {
			if (cachePos.nextIndex() == currentIndex) cachePos.next();

			if (cachePos.hasNext())
			{
				currentIndex = cachePos.nextIndex();			
				return cachePos.next();
			}
			if (results.hasNext()) {
				QueryMatch r = results.next();
				currentIndex = cachePos.nextIndex();			
				cachePos.add(r);
				return r;
			}
			throw new NoSuchElementException();
		}
		
	}
	
	public static class AsIndexHelper {
		private AnnotationSet as;
		private GateAwareTreeIndex index;
		
		public void setSourceAS(AnnotationSet as) {
			this.as = as;
		}

		public void initIndex() {
			index = new GateAwareTreeIndex();
			index.addDependecies(as, DependencyConfiguration.getSelectedConfigurationFromConfigOrDefault());;
		}

		public QueryData createQueryData() {
			return new FSQuery.QueryData(index, new GateAnnotationsNodeAttributes(as));
		}
	}



	public void setResults(Iterable<QueryMatch> results) {
		resultsWalker = new ResultsWalker(results.iterator());
		showNext();
		buttonPrevious.setEnabled(false);		
	}
	
	
	public void setResultsUsingQuery(String queryString) throws SyntaxError {
		QueryData data = asIndexHelper.createQueryData();
		
//		Iterable<QueryMatch> results = q.buildQuery("[lex.rf=32154]))").evaluate();
		Iterable<QueryMatch> results = FSQuery.buildQuery(queryString).evaluate(data);
		setResults(results);
	}


	public void initComponents() {
		setLayout(new BorderLayout());
		
		//treeVisualize
		treeVisualize = new TreeVisualize();
		treeVisualize.initComponents();		
		add(treeVisualize, BorderLayout.CENTER);
		
		
		JPanel southPanel = new JPanel(new FlowLayout());
		add(southPanel, BorderLayout.SOUTH);
				
		buttonPrevious = new JButton("< Previous");
		buttonPrevious.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { showPrevious(); }});
		southPanel.add(buttonPrevious);

		buttonNext = new JButton("Next >");
		buttonNext.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { showNext(); }});
		//same size as "Previous"
		buttonNext.setPreferredSize(buttonPrevious.getPreferredSize());

		southPanel.add(buttonNext);
		
		
		
	}

	protected void showMatch(QueryMatch match) {
		int id = match.getMatchingNodes().iterator().next().getNodeId();
		Annotation ra = asIndexHelper.as.get(id);
		
		FSSentenceStringBuilder fssb = new FSSentenceStringBuilder(ra, asIndexHelper.as);
		
		treeVisualize.setForest(fssb.getAttributes(), fssb.getTree());
		treeVisualize.selectNode(id);
		treeVisualize.setMatchingNodes(match.getMatchingNodes());		
	}

	protected void showNext() {
		if (resultsWalker.hasNext())
		{
			showMatch(resultsWalker.next());
			buttonPrevious.setEnabled(true);
		}
		
		buttonNext.setEnabled(resultsWalker.hasNext());
	}

	
	protected void showPrevious() {
		if (resultsWalker.hasPrevious())
		{
			showMatch(resultsWalker.previous());		
			buttonNext.setEnabled(true);
		}

		buttonPrevious.setEnabled(resultsWalker.hasPrevious());		
	}

}
