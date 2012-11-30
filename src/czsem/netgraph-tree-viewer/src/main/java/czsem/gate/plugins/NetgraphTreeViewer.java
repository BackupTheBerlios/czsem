package czsem.gate.plugins;
import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.metadata.CreoleResource;
import gate.gui.annedit.AnnotationDataImpl;

import javax.swing.JDialog;

import cz.cuni.mff.mirovsky.trees.TNode;
import czsem.fs.FSSentenceStringBuilder;
import czsem.gate.gui.DialogBasedAnnotationEditor;
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
	
	@Override
	public String getTitle() {
		return "Netgraph Tree Viewer";		
	}


	@Override
	protected void initGui() {
		tv.initComponents();
		//this.add(tv);
	}
	
	@Override
	protected void updateInitDialog(JDialog dialog) {
		dialog.add(tv);		
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
