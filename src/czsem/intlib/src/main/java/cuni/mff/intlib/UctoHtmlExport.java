package cuni.mff.intlib;

import gate.Gate;
import gate.creole.dumpingPR.DumpingPR;
import czsem.gate.utils.GateUtils;
import czsem.gate.utils.HtmlExport;

public class UctoHtmlExport {

	public static void main(String[] args) throws Exception {
		
		GateUtils.initGate();
		Gate.getCreoleRegister().registerComponent(DumpingPR.class);

		
		String fileName = "../intlib/documents/ucto_queryRes1.gate.xml";
		String outputDir = "target/export";
		String asName = "query";
		String [] annotationTypes = {"subject", "subject_subtree", "object", "object_subtree"};
		String[] colorNames = {"RoyalBlue", "Aqua", "Orange", "yellow"};
		

		
		HtmlExport.doExport(fileName, outputDir, asName, annotationTypes, colorNames);

	}

}
