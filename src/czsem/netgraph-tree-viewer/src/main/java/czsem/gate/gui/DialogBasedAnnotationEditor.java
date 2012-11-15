package czsem.gate.gui;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.gui.MainFrame;
import gate.gui.annedit.AnnotationDataImpl;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class DialogBasedAnnotationEditor extends AbstractAnnotationEditor {
	private static final long serialVersionUID = 7994333085799107767L;
	
	protected JDialog dialog;
	
	@Override
	public String getTitle() {
		return "Dialog AnnEdit";
	}


	@Override
	public Resource init() throws ResourceInstantiationException {
		initGui();
		initDialog();
		
		return super.init();
	}

	protected void initDialog() {
		Window parentWindow = SwingUtilities.windowForComponent(getOwner().getTextComponent());

		if (parentWindow != null) {
			dialog = new JDialog(parentWindow, getTitle());
			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			MainFrame.getGuiRoots().add(dialog);
		}
		
		dialog.add(this);
		dialog.pack();		
	}

	
/* these should be overridden START */
	protected void initGui() {
		JTextArea debugOut = new JTextArea("some text");
		add(debugOut);
		
		JButton b = new JButton("Next");
		b.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {				
				
				nextAnnotation();
				
			}
			
			protected void nextAnnotation() {
				AnnotationSet set = getAnnotationSetCurrentlyEdited();
				AnnotationSet tocs = set.get("Token");
						
				if (tocs.size() <= 0) return;
				
				int r = new Random().nextInt(tocs.size());

				Iterator<Annotation> i = tocs.iterator();
				Annotation ann = i.next();
				
				for (int a=1; a<r; a++)
				{
					ann = i.next();
				}
				
				getOwner().selectAnnotation(new AnnotationDataImpl(set, ann));
				editAnnotation(ann, set);
			}

		});
		
		add(b);
	}
		
	@Override
	public void editAnnotation(Annotation annotation, AnnotationSet annotation_set)
	{
		setAnnotation(annotation, annotation_set);
		
		String txt = "?";
		
		if (annotation == null)
		{
			txt = "null";			
		} else {
			txt = String.format("%s %d", annotation.getType(), annotation.getId());
		}
		
		Component[] comps = this.getComponents();
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof JTextArea)
			{
				JTextArea debugOut = (JTextArea) comps[i];
				debugOut.setText(debugOut.getText() + "\n" + txt);
			}
		}
		
		dialog.setVisible(true);
	}
/* these should be overridden END */


	@Override
	public void placeDialog(int start, int end) {
		dialog.setVisible(true);
	}
}
