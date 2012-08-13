package czsem.gate.learning;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import weka.core.Instances;
import weka.core.Range;
import weka.core.converters.CSVLoader;
import weka.experiment.PairedCorrectedTTester;
import weka.experiment.ResultMatrix;
import weka.experiment.ResultMatrixPlainText;
import weka.experiment.Tester;

public class WekaResultTests
{
	Tester m_TTester = new PairedCorrectedTTester();
	protected PrintStream out;
	protected PrintStream log;
	
	int first_test_attr = 7;
	
	boolean invert_columns = false;


	public WekaResultTests(PrintStream out, PrintStream log) {
		this.out = out;
		this.log = log;
		m_TTester.setShowStdDevs(true);
	}

	public WekaResultTests(PrintStream both) {
		this(both, both);
	}

	public static void main(String[] args) throws Exception
	{
		Locale.setDefault(Locale.ENGLISH);
		WekaResultTests t = new WekaResultTests(System.err, System.err);
		
//		t.loadInstances("weka_results_acq_ne_root_longer.csv");
		t.loadInstances("weka_results.csv");
		
		t.printBasicStats();
		
		System.err.println();
//		t.performAllTests();
		
		t.testsAttr(6);
		
		System.err.println("done");
	
	}

	public ResultMatrix testsAttr(int c) throws Exception
	{
		int cur_attr_index = c+first_test_attr;
		ResultMatrix ret = new ResultMatrixPlainText();
		m_TTester.setResultMatrix(ret);
		log.println( m_TTester.header(cur_attr_index));
		log.print(m_TTester.multiResultsetFull(0, cur_attr_index));
		
		return ret;
	}

	public void loadInstances(String filename) throws IOException
	{
		CSVLoader cnv = new CSVLoader();
		File f = new File(filename);
		cnv.setSource(f);
		Instances inst = cnv.getDataSet();
		m_TTester.setInstances(inst);
		
		m_TTester.setRunColumn(1);
		m_TTester.setFoldColumn(2);
		
		
		m_TTester.setDatasetKeyColumns(new Range("1"));
		m_TTester.setResultsetKeyColumns(new Range("4, 5, 6"));
			
	}

	public void printBasicStats()
	{
		out.println("ResultSets"); 
		for (int i = 0; i < m_TTester.getNumResultsets(); i++) {
	        out.println( m_TTester.getResultsetName(i));
	    }

		out.println("DataSets num"); 
		out.println( m_TTester.getNumDatasets());
		
	}
	
	private static class CommonPrefixAnalysis
	{
		private Instances inst;
		private int commonPrefixForNextRows = 0;
		private int maxCommonPrefixForNextRows = 0;
		private int current_row = 0;
		private String current_prefix = "";

		public CommonPrefixAnalysis(Instances inst, int first_attr) {
			this.inst = inst;
			current_row = first_attr;
			initNewPrefix();
		}

		private void initNewPrefix() {
			current_prefix = "";
			commonPrefixForNextRows = 0;
			for (int a=1; a<inst.numAttributes()-current_row; a++)
			{
				String [] array = new String[a+1];
				for (int b=0; b<=a; b++) 
					array[b] = inst.attribute(current_row+b).name().replace(' ', '§');
				String new_prefix = StringUtils.getCommonPrefix(array);
				if (new_prefix.equals("")) break;
				current_prefix = new_prefix;
				commonPrefixForNextRows++;
			}
			
			maxCommonPrefixForNextRows = commonPrefixForNextRows;
			
		}

		public void next() {
			commonPrefixForNextRows--;
			current_row++;
			if (commonPrefixForNextRows<0) initNewPrefix();
			
		}
		public String getCurrentPrefix() {
			return current_prefix;
		}
		public int getCurrentNextRows() {
			if (commonPrefixForNextRows == maxCommonPrefixForNextRows)
				return maxCommonPrefixForNextRows;
			else
				return 0;
		}

		
	}


	
	public void printTestAttr(int test_attr, String value_prefix) throws Exception
	{
		Instances inst = m_TTester.getInstances();
		int cur_attr_index = test_attr+first_test_attr;
		printAttrName(inst.attribute(cur_attr_index).name());
		
		printTestAttrTableOnly(test_attr, value_prefix);		
	}

	public void printTestAttrTableOnly(int test_attr, String value_prefix) throws Exception
	{
		Instances inst = m_TTester.getInstances();
		ResultMatrix results = testsAttr(test_attr);
		
		
		for (int d=0; d<m_TTester.getNumDatasets(); d++)
		{
			int[] sel = m_TTester.getDatasetKeyColumns().getSelection();
			int attr = sel[0];
			String dataset = inst.attribute(attr).value(d);
			printDatasetName(dataset);
			
			
			printValueRow(results, d, value_prefix);				
		}


		
	}
	
	protected void printValueRow(ResultMatrix results, int row, String value_prefix) {
		int col;
		if (invert_columns)
		{
			//inverted
			col = 1;
			printAttrValue(results.getMean(col, row), results.getStdDev(col, row), value_prefix);
			col=0;
			printAttrValue(results.getMean(col, row), results.getStdDev(col, row), value_prefix);
			
			//inverted
			printSignificance(3-results.getSignificance(1, row));								
		}
		else
		{
			col = 0;
			printAttrValue(results.getMean(col, row), results.getStdDev(col, row), value_prefix);
			col=1;
			printAttrValue(results.getMean(col, row), results.getStdDev(col, row), value_prefix);
			
			printSignificance(results.getSignificance(1, row));								
		}		
	}

	protected void printDatasetName(String dataset) {
		out.format("%15s ", dataset);				
	}

	public void performAllTests() throws Exception
		{
			
	//		int [] cols = {3,4,5};
	//		m_TTester.setDisplayedResultsets(null);
	//		ResultMatrix m_ResultMatrix = new ResultMatrixPlainText();
			
			
	//	    m_ResultMatrix.setShowStdDev(true);
	//	    m_ResultMatrix.setRemoveFilterName(ExperimenterDefaults.getRemoveFilterClassnames());
	//	    m_ResultMatrix.setShowAverage(true);
				

	
			
			Instances inst = m_TTester.getInstances();
			int num_tests = inst.numAttributes()-first_test_attr;
			
			ResultMatrix results [] = new ResultMatrix[num_tests];
	
			for (int attr=0; attr< num_tests ; attr++)
			{
				results [attr] = testsAttr(attr);
			}
			
			for (int d=0; d<m_TTester.getNumDatasets(); d++)
			{
				int[] sel = m_TTester.getDatasetKeyColumns().getSelection();
				int attr = sel[0];
				String dataset = inst.attribute(attr).value(d);
				printDatasetHeader(dataset);
				
				CommonPrefixAnalysis cpa = new CommonPrefixAnalysis(inst, first_test_attr);
				for (int c=0; c< num_tests ; c++)
				{
					int cur_attr_index = c+first_test_attr;
					printAttrName(inst.attribute(cur_attr_index).name(), cpa.getCurrentPrefix(), cpa.getCurrentNextRows());
					cpa.next();

					printValueRow(results[c], d, "");
				}
				
			}
			
	//		processResults(results);
	/*		
			out.println( 
					m_TTester.multiResultsetFull(0, compareCol));
	*/		
		}

	protected void printSignificance(int significance)
	{
		switch (significance) {
		case 1: out.print("up"); break;
		case 2: out.print("down");	break;
		}
		
		out.println();
		
	}

	public static String filterAttrName(String name)
	{
		if (name.charAt(0) == '[' && name.charAt(name.length()-1) == ']')
			name = name.substring(1, name.length()-1);
			
		return name.replace('_', ' ');
	}
	
	protected void printDatasetHeader(String dataset)
	{
		out.println("dataset: " + filterAttrName(dataset.substring(1, dataset.length()-1)));
	}
	
	protected void printAttrName(String name) {
		out.println(name);		
	}
	
	protected void printAttrName(String name, String commonPrefix, int commonPrefixForNextRows) {
		out.format("%40s | ", filterAttrName(name));
	}

	protected void printAttrValue(double avg, double stdev, String value_prefix)
	{
		out.format("%11.3f +/- %8.3f | ", avg, stdev);				
	}

}
