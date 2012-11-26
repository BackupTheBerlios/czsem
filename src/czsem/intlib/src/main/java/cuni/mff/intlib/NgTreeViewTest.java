package cuni.mff.intlib;

import gate.Gate;

import java.io.File;

import czsem.gate.applet.GateApplet;
import czsem.gate.applet.MainFrame;
import czsem.gate.gui.DialogBasedAnnotationEditor;
import czsem.gate.plugins.NetgraphTreeViewer;
import czsem.gate.plugins.OldNetgraphTreeViewer;
import czsem.gate.utils.GateUtils;

public class NgTreeViewTest {

	public static void main(String[] args) throws Exception {
		GateUtils.initGateInSandBox();
		
		
		String fileName = "C:/data/czsem_coprus/Czech_Fireman_50_messages/analyzed_GATE_xml/jihomoravsky47443.txt.xml";

		
		MainFrame.defaultEncoding = "cp1250";
		
		Gate.getCreoleRegister().registerComponent(OldNetgraphTreeViewer.class);
		Gate.getCreoleRegister().registerComponent(DialogBasedAnnotationEditor.class);
		Gate.getCreoleRegister().registerComponent(NetgraphTreeViewer.class);
		GateApplet.showWithDocument(new File(fileName).toURI().toURL(), "TectoMT", null);
	}

}
