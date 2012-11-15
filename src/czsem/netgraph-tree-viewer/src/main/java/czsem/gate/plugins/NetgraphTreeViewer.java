package czsem.gate.plugins;
import javax.swing.JButton;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.metadata.CreoleResource;
import czsem.gate.gui.DialogBasedAnnotationEditor;


@CreoleResource(name = "Netgraph TreeViewer")
public class NetgraphTreeViewer extends DialogBasedAnnotationEditor {
	private static final long serialVersionUID = 7994333085799107767L;
	
	@Override
	public String getTitle() {
		return "Netgraph Tree Viewer";		
	}


	@Override
	protected void initGui() {
		this.add(new JButton("NG TV :-)"));
	}

	@Override
	public void editAnnotation(Annotation annotation, AnnotationSet annotation_set) {
		setAnnotation(annotation, annotation_set);
		
		dialog.setVisible(true);
	}

	
}
