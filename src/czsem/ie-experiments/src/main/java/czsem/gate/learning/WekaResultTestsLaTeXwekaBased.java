package czsem.gate.learning;

import java.io.PrintStream;
import java.util.Locale;

import weka.experiment.ResultMatrix;
import weka.experiment.ResultMatrixLatex;

public class WekaResultTestsLaTeXwekaBased extends WekaResultTests {

	public WekaResultTestsLaTeXwekaBased(PrintStream out, PrintStream log) {
		super(out, log);
	}
	
	@Override
	public ResultMatrix testsAttr(int c) throws Exception
	{
		int cur_attr_index = c+first_test_attr;
		ResultMatrix ret = new ResultMatrixLatex();
		m_TTester.setResultMatrix(ret);
		log.println("\\begin{verbatim}");
		log.println( m_TTester.header(cur_attr_index));
		log.println("\\end{verbatim}");
		log.print(m_TTester.multiResultsetFull(0, cur_attr_index));
		log.println("\\clearpage");
		
		return ret;
	}


	public static void main(String[] args) throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		WekaResultTestsLaTeXwekaBased t = new WekaResultTestsLaTeXwekaBased(System.err, System.err);
		
//		t.loadInstances("weka_results_acq_ne_root_longer.csv");
		t.loadInstances("weka_results_long.csv");
		
		t.printBasicStats();
		
		System.err.println();
		t.performAllTests();
		
//		t.testsAttr(6);
		
		System.err.println("done");

	}

}
