package czsem.gate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import czsem.Utils;
import czsem.gate.learning.MLEngine;

import 	org.apache.log4j.Logger;


public class TimeBenchmarkLogAnalysis {
	

	static Logger logger = Logger.getLogger(TimeBenchmarkLogAnalysis.class);

	private BufferedReader in;
	
	Map<String, Integer> stats = new HashMap<String, Integer>();
	Map<String, Integer> stats_with_fold_numbers = new HashMap<String, Integer>();


	public TimeBenchmarkLogAnalysis(String filen_name) throws FileNotFoundException
	{
		in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(filen_name))));
	}

	public int getTrainTimeForMLEngine(String reponsesASName)
	{
		return stats.get(statsKeyTrain(reponsesASName));
	}

	public int getTestTimeForMLEngine(String reponsesASName)
	{
		return stats.get(statsKeyTest(reponsesASName));
	}

	/**
	 * @param fold_number Counted form 0 not from 1 !
	 */
	public int getTestTimeForMLEngine(String reponsesASName, int fold_number)
	{
		String key = foldStatsKey(statsKeyTest(reponsesASName), Integer.toString(fold_number));
		return stats_with_fold_numbers.get(key); 
	}

	/**
	 * @param fold_number Counted form 0 not from 1 !
	 */
	public int getTrainTimeForMLEngine(String reponsesASName, int fold_number)
	{
		String key = foldStatsKey(statsKeyTrain(reponsesASName), Integer.toString(fold_number));
		return stats_with_fold_numbers.get(key); 
	}

	public void print()
	{
		for (String key : stats.keySet())
		{
			System.err.format("%45s: %8d\n", key, stats.get(key));			
		}

		System.err.println();			

		for (String key : stats_with_fold_numbers.keySet())
		{
			System.err.format("%45s: %8d\n", key, stats_with_fold_numbers.get(key));			
		}
	}
	
	public void parse() throws IOException
	{
		int total = 0;
		int lines = 0;
				
		for (String line = in.readLine(); line != null; line = in.readLine())
		{
			if (line.length() == 0) continue;
			String[] split = line.split(" ");
			if (split[1].equals("0")) continue;
			if (split[1].equals("START")) continue;
			
			lines++;
			int time =  Integer.parseInt(split[1]);
			total += time;
			
			String[] pr_split = split[2].split("\\.");
			
			String prname= pr_split[pr_split.length-1];
			
			Utils.increaseCountingMap(stats, prname, time);
			
			if (split.length > 8 && split[7].equals("fold"))
			{
				String fold_num = split[8].substring(0, split[8].length()-1);
				Utils.increaseCountingMap(stats_with_fold_numbers, foldStatsKey(prname, fold_num), time);				
			}
			
		}
		in.close();
		
		logger.debug(String.format("total time: %d", total));
		logger.debug(String.format("lines: %d", lines));
	}

	public static String statsKeyTrain(String reponsesASName)
	{
		return "pr_" + MLEngine.renderPRNameTrain(reponsesASName);
	}

	public static String statsKeyTest(String reponsesASName)
	{
		return "pr_" + MLEngine.renderPRNameTest(reponsesASName);
	}

	/**
	 * @param statKey Obtained from methods {@link #statsKeyTest(String)} or {@link #statsKeyTrain(String)}
	 */
	public static String foldStatsKey(String statKey, String fold_num)
	{
		return statKey + "_f" + fold_num;
	}

	public static void main(String[] args) throws IOException
	{
		TimeBenchmarkLogAnalysis a = new TimeBenchmarkLogAnalysis("debug_benchmark.txt");
		
		a.parse();
		a.print();
		String e_n = "ILP_config";
		System.err.format("trn %s: %d\n", e_n, a.getTrainTimeForMLEngine(e_n));
		System.err.format("tst %s: %d\n", e_n, a.getTestTimeForMLEngine(e_n));
		System.err.format("f0 trn %s: %d\n", e_n, a.getTrainTimeForMLEngine(e_n, 0));
		System.err.format("f1 trn %s: %d\n", e_n, a.getTrainTimeForMLEngine(e_n, 1));
		System.err.format("f0 tst %s: %d\n", e_n, a.getTestTimeForMLEngine(e_n, 0));
		System.err.format("f1 tst %s: %d\n", e_n, a.getTestTimeForMLEngine(e_n, 1));
		e_n = "Paum";
		System.err.format("trn %s: %d\n", e_n, a.getTrainTimeForMLEngine(e_n));
		System.err.format("tst %s: %d\n", e_n, a.getTestTimeForMLEngine(e_n));
		System.err.format("f0 trn %s: %d\n", e_n, a.getTrainTimeForMLEngine(e_n, 0));
		System.err.format("f1 trn %s: %d\n", e_n, a.getTrainTimeForMLEngine(e_n, 1));
		System.err.format("f0 tst %s: %d\n", e_n, a.getTestTimeForMLEngine(e_n, 0));
		System.err.format("f1 tst %s: %d\n", e_n, a.getTestTimeForMLEngine(e_n, 1));
	}

}
