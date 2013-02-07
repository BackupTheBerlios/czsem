package czsem.gate.plugins;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.metadata.CreoleResource;
import gate.gui.annedit.AnnotationDataImpl;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import cz.cuni.mff.mirovsky.trees.TNode;
import czsem.fs.FSSentenceStringBuilder;
import czsem.fs.query.FSQueryParser.SyntaxError;
import czsem.gate.gui.DialogBasedAnnotationEditor;
import czsem.gui.NgQueryDesigner;
import czsem.gui.NgResultsBrowser;
import czsem.gui.TreeVisualize;


@CreoleResource(name = "Netgraph TreeViewer")
public class NetgraphTreeViewer extends DialogBasedAnnotationEditor {
	private static final long serialVersionUID = 7994333085799107767L;
	

	protected TreeVisualize tv = new TreeVisualize() {
		private static final long serialVersionUID = 2801161651621415659L;
		
		@Override
		protected void fireTreeNodeSelected(TNode choosen_node) {
			AnnotationSet set = getAnnotationSetCurrentlyEdited();
			Integer annId = getIdAttrValue(choosen_node);
			Annotation ann = set.get(annId);
			getOwner().selectAnnotation(new AnnotationDataImpl(set, ann));
			editAnnotation(ann, set);
		}
	};

	private JTabbedPane tabs; 
	private NgQueryDesigner qd;


	private NgResultsBrowser rb;

	
	@Override
	public String getTitle() {
		return "Netgraph Tree Viewer";		
	}


	@Override
	protected void initGui() {
		
		tv.initComponents();
		//this.add(tv);

		tabs = new JTabbedPane(JTabbedPane.BOTTOM);
		tabs.addTab("Viewer", tv);
		
		qd = new NgQueryDesigner();
		qd.initComponents();
		qd.setQueryString("[string=hallo]");
		qd.addSearchButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {search();}});
		tabs.addTab("Query", qd);
		
	    rb = new NgResultsBrowser();
	    rb.initComponents();
		tabs.addTab("Results", rb);

	}
	
	protected void search() {
		rb.setSourceAS(getAnnotationSetCurrentlyEdited());
		rb.initIndex();
		try {
			rb.setResultsUsingQuery(qd.getQueryString());
		} catch (SyntaxError e) {
			throw new RuntimeException(e);
		}
		
		tabs.setSelectedComponent(rb);
		
	}


	@Override
	protected void updateInitDialog(JDialog dialog) {
//		dialog.add(tv);		
		dialog.add(tabs);		
	}


	@Override
	public void editAnnotation(Annotation annotation, AnnotationSet annotation_set) {
		if (annotation == null) return;
		
		if (! canDisplayAnnotationType(annotation.getType())) return;

		setAnnotation(annotation, annotation_set);
		
				
//		FSSentenceStringBuilder fssb = new FSSentenceStringBuilder(sas);
		FSSentenceStringBuilder fssb = new FSSentenceStringBuilder(annotation, annotation_set);
		tv.setForest(fssb.getAttributes(), fssb.getTree());
		tv.selectNode(annotation.getId());
		
		tabs.setSelectedComponent(tv);		
		dialog.setVisible(true);		
		dialog.repaint();
	}
	
	@Override
	public boolean canDisplayAnnotationType(String annotationType) {
		if (annotationType.equals("Token"))
			return true;
		if (annotationType.equals("tToken"))
			return true;
		if (annotationType.equals("t-node"))
			return true;
		if (annotationType.equals("Sentence"))
			return true;

		return false;
	}
	
}
