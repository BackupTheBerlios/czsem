package czsem.gui;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import czsem.fs.FSSentenceStringBuilder;
import czsem.fs.GateAnnotationsNodeAttributes;
import czsem.fs.query.FSQuery;
import czsem.fs.query.FSQuery.QueryMatch;
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
		index.addDependecies(mainAs.get("tDependency"));			
		System.err.format("finished in: %10.3fs\n", (System.currentTimeMillis() - time) * 0.001);
		
		
		FSQuery q = new FSQuery();
		q.setIndex(index);
		q.setNodeAttributes(new GateAnnotationsNodeAttributes(mainAs));
		
		Iterable<QueryMatch> results = q.buildQuery("[t_lemma=být]([t_lemma=povinný])").evaluate();
		
		JFrame fr = new JFrame(NgResultsBrowser.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
	    NgResultsBrowser qd = new NgResultsBrowser();
		qd.initComponents();
		
		qd.setSourceAS(mainAs);
		qd.setResults(results);
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);
	}

	private TreeVisualize treeVisualize;

	private Iterator<QueryMatch> results;

	private AnnotationSet as;

	public void setSourceAS(AnnotationSet as) {
		this.as = as;
	}

	public void setResults(Iterable<QueryMatch> results) {
		this.results = results.iterator();
		
	}

	protected void initComponents() {
		setLayout(new BorderLayout());
		
		//treeVisualize
		treeVisualize = new TreeVisualize();
		treeVisualize.initComponents();		
		add(treeVisualize, BorderLayout.CENTER);
		
		
		JButton buttonNext = new JButton("Next");
		buttonNext.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) { showNext(); }});
		add(buttonNext, BorderLayout.SOUTH);
		
		
		
	}

	protected void showNext() {
		if (! results.hasNext()) return;
		
		QueryMatch match = results.next();
		
		int id = match.getMatchingNodes().iterator().next().getNodeId();
		Annotation ra = as.get(id);
		
		FSSentenceStringBuilder fssb = new FSSentenceStringBuilder(ra, as);
		
		treeVisualize.setForest(fssb.getAttributes(), fssb.getTree());
		treeVisualize.selectNode(id);
		treeVisualize.setMatchingNodes(match.getMatchingNodes());
//		TreeVisualizeFrame.showTreeAndWait(fssb.getAttributes(), fssb.getTree(), res.getNodeId());
		
		//repaint();
	}

}
