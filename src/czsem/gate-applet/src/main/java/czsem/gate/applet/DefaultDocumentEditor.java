package czsem.gate.applet;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.gui.docview.AnnotationListView;
import gate.gui.docview.AnnotationSetsView;
import gate.gui.docview.DocumentEditor;
import gate.gui.docview.DocumentView;
import gate.util.InvalidOffsetException;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class DefaultDocumentEditor extends DocumentEditor {

	public DefaultDocumentEditor() {
		URL url = getClass().getResource("/save-32.gif");
		Icon icon = new ImageIcon(url);
		button = new JToggleButton("Save!", icon);
		button.setBackground(Color.YELLOW);		
	}
	
	//private AnnotationSetsView asw;
	private JToggleButton button;
	private String defaultAsName;
	private Integer debugAnnId;

	/*
	public void selectAnnotationType(String asName, String annType,
			boolean selected) {
		TypeHandler handler = asw.getTypeHandler(asName, annType);
		if (handler != null)
			handler.setSelected(selected);
	}
	 */
	
	@Override
	protected void initViews() {
		super.initViews();

		List<DocumentView> vs = getVerticalViews();
		for (int i = 0; i < vs.size(); i++) {
			DocumentView view = vs.get(i);
			if (view instanceof AnnotationSetsView) {
				//asw = (AnnotationSetsView) view;
				setRightView(i);
			}
		}

		List<DocumentView> hs = getHorizontalViews();
		for (int i = 0; i < hs.size(); i++) {
			DocumentView view = hs.get(i);
			if (view instanceof AnnotationListView) {
				setBottomView(i);
			}
		}

		setEditable(false);
		
		saveSettings();
		initAndSaveAnnotationSetsSettings();		
		restoreSettings();
		
		AnnotationSet anns = getDocument().getAnnotations(defaultAsName);
		anns.remove(anns.get(debugAnnId));
		
	}

	protected void initAndSaveAnnotationSetsSettings() {
		//defAs.remove(id);
		
		Object key = AnnotationSetsView.class.getName() + ".types";
		LinkedHashSet<String> set = Gate.getUserConfig().getSet(key);
		addDefaultAsTypes(set);
		Gate.getUserConfig().put(key, set);
		
		key = DocumentEditor.class.getName() + ".setTypeSet";
		set = Gate.getUserConfig().getSet(key);
		addDefaultAsTypes(set);
		Gate.getUserConfig().put(key, set);


		/*
		for (Object o : Gate.getUserConfig().keySet())
		{
			System.err.println(o);
			System.err.println((Gate.getUserConfig().get(o)));
			System.err.println("---");
		}
		*/
	}

	@Override
	protected void addView(DocumentView view, String name) {
		/*
		if (view instanceof CorefEditor)
			return;
		 */
		if (view instanceof AnnotationSetsView) {
			addSaveButton();
		}

		super.addView(view, name);
	}

	protected void addSaveButton() {
		topBar.add(button);
		topBar.addSeparator();

		addSaveButtonListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JToggleButton src = (JToggleButton) e.getSource();
				src.setSelected(false);
			}
		});

	}

	public void addSaveButtonListener(ActionListener actionListener) {
		button.addActionListener(actionListener);
	}
	

	protected void addDefaultAsTypes(Set<String> target) {
			AnnotationSet defAs = getDocument().getAnnotations(defaultAsName);
			Set<String> types = defAs.getAllTypes();
			for (String annType : types) {
				target.add(defaultAsName +"."+ annType);
			}
	}

	public Document getDocument() {
		return document;
	}

	public void setDefaultAsName(String asName) {
		defaultAsName = asName;		

		try {
			AnnotationSet defAs = getDocument().getAnnotations(defaultAsName);
			debugAnnId = defAs.add(0L, 0L, "debugAnn", Factory.newFeatureMap());
		} catch (InvalidOffsetException e) {
			throw new RuntimeException(e);
		}
	}
}
