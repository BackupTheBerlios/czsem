package czsem.gate.utils;

import gate.Corpus;
import gate.Document;
import gate.Factory;

public class SpcTokenCounter {

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Corpus corpus = GateUtils.loadCorpusFormDatastore(
				GateUtils.openDataStore("file:/C:/Users/dedek/Desktop/DATLOWE/DATLOWE_gate_store/"),
				"experiment___1367568738829___3631");
		
		int sum = 0;
		int num = 0;
		
		for (Document d: corpus) {
			int tocs = d.getAnnotations("Treex").get("Token").size();
			System.err.println(num++ + " " + d.getName() + " " + tocs + " " + sum);
			sum += tocs;
			
			Factory.deleteResource(d);
		}
		
		System.err.println(sum);

	}

}
