package czsem.ILP;

import java.util.Random;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class FuzzyILPClassifierTest {
	
	public static Instances buildDataset(int maxA, int maxB, int divClass)
	{
		//header
		FastVector attInfo = new FastVector(3);
		attInfo.addElement(new Attribute("A"));
		attInfo.addElement(new Attribute("B"));
		int maxClass = (maxA+maxB)/divClass;
		FastVector classVals = new FastVector(maxClass+1);
		for (int i = 0; i <= maxClass; i++) {
			classVals.addElement(Integer.toString(i));			
		}
		attInfo.addElement(new Attribute("C", classVals ));
		Instances data = new Instances("TestData", attInfo, maxA*maxB);
		data.setClassIndex(2);
		
		//data
		for (int a = 0; a <= maxA; a++) {
			for (int b = 0; b <= maxB; b++) {
				double vals[] = new double [] {a, b, (a+b)/divClass};
				data.add(new Instance(1.0, vals));
				//System.err.format("%3d %3d %3d\n", a, b, (a+b)/divClass);
			}			
		}
		
		return data;		
	}

	@Test
	public void testFuzzyILP() throws Exception {
		
		Instances data = buildDataset(10, 9, 5);
		
		Evaluation eval = new Evaluation(data);
		FuzzyILPClassifier classifier = new FuzzyILPClassifier();
		eval.crossValidateModel(classifier,	data, 4, new Random());
		
		AssertJUnit.assertTrue(String.format("percent correct too small %f", eval.pctCorrect()), eval.pctCorrect() > 60.0);
		AssertJUnit.assertTrue(String.format("percent incorrect too large %f", eval.pctIncorrect()), eval.pctIncorrect() < 40.0);
		AssertJUnit.assertTrue(String.format("kappa too small %f", eval.kappa()), eval.kappa() > 0.4);
	}
}
