package czsem.gate.applet;

import gate.Document;
import gate.gui.docview.AnnotationListView;
import gate.gui.docview.AnnotationSetsView;
import gate.gui.docview.AnnotationSetsView.TypeHandler;
import gate.gui.docview.DocumentEditor;
import gate.gui.docview.DocumentView;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class DefaultDocumentEditor extends DocumentEditor {

	private AnnotationSetsView asw;
	private JToggleButton button;

	public void selectAnnotationType(String asName, String annType,
			boolean selected) {
		TypeHandler handler = asw.getTypeHandler(asName, annType);
		if (handler != null)
			handler.setSelected(selected);
	}

	@Override
	protected void initViews() {
		super.initViews();

		List<DocumentView> vs = getVerticalViews();
		for (int i = 0; i < vs.size(); i++) {
			DocumentView view = vs.get(i);
			if (view instanceof AnnotationSetsView) {
				asw = (AnnotationSetsView) view;
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
		URL url = getClass().getResource("/save-32.gif");
		Icon icon = new ImageIcon(url);
		button = new JToggleButton("Save!", icon);
		button.setBackground(Color.YELLOW);
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

	public Document getDocument() {
		return document;
	}
}
