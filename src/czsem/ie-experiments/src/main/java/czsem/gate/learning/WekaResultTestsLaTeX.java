package czsem.gate.learning;

import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;

import weka.core.Range;

public class WekaResultTestsLaTeX extends WekaResultTests {

	public WekaResultTestsLaTeX(PrintStream out, PrintStream log) {
		super(out, log);
	}
	
	protected boolean first_row_in_dataset = false;
	protected boolean na_val = false;
	protected boolean time = false;

	public static void main(String[] args) throws Exception {
		Locale.setDefault(Locale.ENGLISH);
		WekaResultTestsLaTeX t = new WekaResultTestsLaTeX(System.out, new PrintStream(new NullOutputStream()));
		
/*
		t.loadInstances("gate-learning/acquisitions-v1.1/results/weka_results_acq_complete.csv");
		t.setInvertColumns(true);
/**/
		t.loadInstances("gate-learning/czech_fireman/results/weka_results_cars.csv");
		t.setInvertColumns(true);
/*		
		t.loadInstances("weka_results.csv");
/**/		

		t.printBasicStats();
		
		System.err.println();
		t.performAllTests();

		//overall
		System.out.println("\n\n\n---------------------- overall ----------------------");
		t.m_TTester.setDatasetKeyColumns(new Range("5"));
		t.performAllTests();

		
		//t.testsAttr(6);
		
		
		System.out.println("\n\n\n---------------------- selected attributes ----------------------");
		t.printTestAttrWithOverall(4, "&"); //Strict Precision
		t.printTestAttrWithOverall(5, "&"); //Strict Recall
		t.printTestAttrWithOverall(6, "&"); //Strict $F_1$
		
		System.out.println("\n");

		t.time = true;
		t.printTestAttrWithOverall(11, "&"); //Time Training
		t.printTestAttrWithOverall(12, "&"); //Time Testing

		System.out.println("done");

	}

	public void setInvertColumns(boolean b) {
		invert_columns = b;		
	}

	@Override
	protected void printSignificance(int significance) {
		if (! na_val)
		{		
			switch (significance) {
			case 1: out.print("$\\circ$"); break;
			case 2: out.print("$\\bullet$");	break;
			}
		}
		
		out.println(" \\\\");
		
		first_row_in_dataset = false;
	}

	@Override
	protected void printDatasetHeader(String dataset) {
		out.format("\\hline\n\\hline\n\\multirow{11}{*}{\\begin{sideways}%s\\end{sideways} }\n", 
				filterAttrName(dataset));
		
		first_row_in_dataset = true;
	}
	
	protected void setupTimeAttr(String attr_name)
	{
		if (StringUtils.startsWithIgnoreCase(attr_name, "time")) 
			time = true;
		else time = false;
	}
	
	@Override
	protected void printAttrName(String name) {
		setupTimeAttr(name);

		out.format("\\multicolumn{11}{c}{%s}\\\\\n", name);
		out.println("\\hline");
		out.println("Task && \\multicolumn{3}{c}{ILP}  && \\multicolumn{3}{c}{PAUM} && \\\\");
		out.println("\\hline");
	}


	@Override
	protected void printAttrName(String name, String commonPrefix, int commonPrefixForNextRows) {
		setupTimeAttr(name);
		
		//super.printAttrName(name, commonPrefix, commonPrefixForNextRows);
		String array[] = new String [] {name, commonPrefix};
		String finall_prefix = StringUtils.getCommonPrefix(array);
		String finall_sufix = name.substring(finall_prefix.length());
		

		if (commonPrefix.equals(""))
		{
			String[] split = name.split(" ");
			if (split.length > 1)
			{
				finall_prefix = split[0];
				finall_sufix = name.substring(finall_prefix.length());
			}
			
		} else if (commonPrefixForNextRows <= 0) finall_prefix = "";
				

		
/**/
		int cline = 2;
		
		if (commonPrefix.length() > 0 && commonPrefixForNextRows == 0) cline = 3;
		
		if (! first_row_in_dataset) out.format("\\cline{%d-10} ", cline);
		else out.print("             ");

		
		out.print("& ");
		if (commonPrefixForNextRows>0)
			out.format("\\multirow{%d}{*}{%10s} ", commonPrefixForNextRows+1, finall_prefix);
		else
			out.format("%27s ", finall_prefix);
		
		out.format("& %15s & ", finall_sufix);
/**/
		
		//out.format("'%s'\t\t'%s'\t\t%d\t", finall_prefix, finall_sufix, commonPrefixForNextRows);
		
		na_val = false;
	}

	@Override
	protected void printAttrValue(double avg, double stdev, String value_prefix) {
		out.print(value_prefix);
		if (time)
		{
			out.format("%11s &  $\\pm$  & %11s & ", formatTime(avg), formatTime(stdev));
		}		
		else if (avg < 0)
		{
			out.print("   \\multicolumn{3}{c|}{n/a}         & ");
			na_val = true;
		} else if (avg > 9000)
			out.format("%11.0f &  $\\pm$  & %11.0f & ", avg, stdev);				
		else
			out.format("%11.3f &  $\\pm$  & %11.3f & ", avg, stdev);				
	}

	public static String formatTime(double value) {
		 
		long millis = (long) value + 50;
		
		return String.format("%2d:%02d.%d", 
				TimeUnit.MILLISECONDS.toMinutes(millis),
			    TimeUnit.MILLISECONDS.toSeconds(millis) - 
			    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
			    ((millis - TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(millis)))) /100);
	}

	@Override
	protected void printDatasetName(String dataset)
	{
		out.format("%18s &", filterAttrName(dataset));
	}
	
	public void printTestAttrWithOverall(int test_attr, String value_prefix) throws Exception
	{
		m_TTester.setDatasetKeyColumns(new Range("1"));
		printTestAttr(test_attr, value_prefix);
		m_TTester.setDatasetKeyColumns(new Range("5"));
		out.println("\\hline");
		printTestAttrTableOnly(test_attr, value_prefix);
		out.println("\\hline");
		out.println("\\\\\n");


	}


}
