package czsem.gate.learning;

import gate.util.AnnotationDiffer;
import gate.util.reporting.exceptions.BenchmarkReportInputFileFormatException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import czsem.gate.TimeBenchmarkLogAnalysis;
import czsem.gate.plugins.LearningEvaluator;
import czsem.gate.plugins.LearningEvaluator.CentralResultsRepository;
import czsem.gate.plugins.LearningEvaluator.DiffCondition;
import czsem.gate.plugins.LearningEvaluator.DocumentDiff;
import czsem.gate.utils.TimeBenchmarkUtils;
import czsem.utils.ProjectSetup;

public class WekaResultExporter
{
	Logger logger = Logger.getLogger(WekaResultExporter.class);

/*	
	private static class TimeBenchmarkWekaReporter implements TimeBenchmarkReporter
	{
		Logger logger = Logger.getLogger(TimeBenchmarkWekaReporter.class);

		private WekaResultExporter wekaResultExporter;

		public TimeBenchmarkWekaReporter(WekaResultExporter wekaResultExporter) {
			this.wekaResultExporter = wekaResultExporter;
		}

		@SuppressWarnings("unchecked")
		private void searchMap(Map<String, Object> map)
		{
			
			for (String pr_name : map.keySet()) {
				if (pr_name.startsWith("doc") || pr_name.equals("systotal"))
					continue;

//				out.print(prefix + pr_name);

				Object child = map.get(pr_name);
				Map<String, Object> ch_map = null;
								
				if (child instanceof String)
				{
					for (int i = 0; i < wekaResultExporter.results.length; i++)
					{
						Result r = wekaResultExporter.results[i];
						if (pr_name.contains(
								MLEngine.renderPRNameTrain(
										r.getResponsesASName())))
						{
							logger.debug(String.format("train: %s %s", pr_name, child));
							r.setTrainTime((String) child);
							continue;
						}

						if (pr_name.contains(
								MLEngine.renderPRNameTest(
										r.getResponsesASName())))
						{
							logger.debug(String.format("test: %s %s", pr_name, child));
							r.setTestTime((String) child);
						}
						
						
					}

//					out.println("\t" + child); //time
					continue;
				}
				else
				{
					ch_map = (Map<String, Object>) child;
//					out.println("\t" + ch_map.get("systotal"));
				}
				
				
				//recursive call
				searchMap(ch_map);

			}
		}

		@Override
		public void report(Map<String, Object> report1Container1) {
			searchMap(report1Container1);			
		}
	}

	*/

	
	public static final String[] header =
	{		
		"Key_Dataset",
		"Key_Run",
		"Key_Fold",
		"Key_Scheme",
		"Key_Scheme_options",
		"Key_Scheme_version_ID",
		"DateTime",
//		"Number_of_testing_instances",
		"Number Correct",
		"Number Missing",
		"Number Spurious",
		"Number Overlap",
//		"Percent_correct",
//		"Percent_incorrect",
//		"Percent_unclassified",
		"Strict Precision",
		"Strict Recall",
		"Strict $F_1$",
		"Lenient Precision",
		"Lenient Recall",
		"Lenient $F_1$",
		"Average Precision",
		"Average Recall",
		"Average $F_1$",
		
		"Accuracy",
		"Tokens",
		
		"Number Training Inst",
		"Number Training Docs",
//		"Area_under_ROC",
//		"Weighted_avg_true_positive_rate",
		
		//always the last two! 
		"Time Training",
		"Time Testing",
	};
	
	protected static class Result
	{
		String data[];
		int fold_number;

		public void setTestTime(String time) {
			setField(data.length-1, time);			
		}
		public void setTrainTime(String time) {
			setField(data.length-2, time);						
		}
		public String getResponsesASName() {
			return data[3];
		}	
		/** Counted form 1 not form 0 ! **/
		public String getFoldNumber() {
			return data[2];
		}	
		/** Counted form 1 not form 0 ! **/
		public int getIntFoldNumber() {
			return fold_number;
		}	
		public void setRunNumber(int run_number) {
			setField(1, run_number);
		}

		public Result()
		{
			data = new String[header.length];			
		}

		protected void setField(int index, Object value)
		{
			data[index] = value.toString();
		}
		


		public Result(Object ... data)
		{
			this();
			
			for (int i = 0; i < data.length; i++)
			{
				if (i >= this.data.length) break;
				
				if (data[i] != null)
					this.data[i] = data[i].toString();
			}
			
		}
		
		public Result(LearningEvaluator learningEvaluator, AnnotationDiffer diff, int tokensTotal, int numDocs, int numTrainInst, int fold_number, String timestamp)
		{
			this(					
					learningEvaluator.getAnnotationTypes().toString(), //Key_Dataset
					learningEvaluator.actualRunNumber,//Key_Run
					fold_number,//Key_Fold
					learningEvaluator.getResponseASName(),//Key_Scheme
					learningEvaluator.getKeyASName(),//Key_Scheme_options
					"a",//Key_Scheme_version_ID
					timestamp,//Date_time
					//Number_of_training_instances
					//Number_of_testing_instances
					
					diff.getCorrectMatches(),
					diff.getMissing(),
					diff.getSpurious(),
					diff.getPartiallyCorrectMatches(),
					
					diff.getPrecisionStrict(),
					diff.getRecallStrict(),
					diff.getFMeasureStrict(1),
					
					diff.getPrecisionLenient(),
					diff.getRecallLenient(),
					diff.getFMeasureLenient(1),
					
					diff.getPrecisionAverage(),
					diff.getRecallAverage(),
					diff.getFMeasureAverage(1),
					
					tokensTotal == 0 ? 0 :
						(tokensTotal-diff.getMissing()-diff.getSpurious()) / 
						(double) tokensTotal,
					tokensTotal,
					
					numTrainInst,
					numDocs
					//Number_unclassified
					//Percent_correct
					//Percent_incorrect
					//Percent_unclassified
					//Kappa_statistic
					//Mean_absolute_error
					//Root_mean_squared_error
					//Relative_absolute_error
					//Root_relative_squared_error
					//SF_prior_entropy
					//SF_scheme_entropy
					//SF_entropy_gain
					//SF_mean_prior_entropy
					//SF_mean_scheme_entropy
					//SF_mean_entropy_gain
					//KB_information
					//KB_mean_information
					//KB_relative_information
					//True_positive_rate
					//Num_true_positives
					//False_positive_rate
					//Num_false_positives
					//True_negative_rate
					//Num_true_negatives
					//False_negative_rate
					//Num_false_negatives
					//IR_precision
					//IR_recall
					//F_measure
					//Area_under_ROC
					//Weighted_avg_true_positive_rate
					//Weighted_avg_false_positive_rate
					//Weighted_avg_true_negative_rate
					//Weighted_avg_false_negative_rate
					//Weighted_avg_IR_precision
					//Weighted_avg_IR_recall
					//Weighted_avg_F_measure
					//Weighted_avg_area_under_ROC
					//Elapsed_Time_training
					//Elapsed_Time_testing
					//UserCPU_Time_training
					//UserCPU_Time_testing
					//Serialized_Model_Size
					//Serialized_Train_Set_Size
					//Serialized_Test_Set_Size
					//Summary
					//measureNumRules
			);
			this.fold_number = fold_number;
		}

		
	}
	
	Result [] results = null;
	
	
	public WekaResultExporter(String [][] data)
	{
		results = new Result[data.length];
		
		for (int i = 0; i < data.length; i++) {
			results[i] = new Result((Object[])data[i]);
		}
	}
	
	
	public WekaResultExporter()
	{
		//initFromLearningEvaluatorCentralResultsRepository();
	}
	
	public void initFromLearningEvaluatorCentralResultsRepository()
	{
		String timestamp = ProjectSetup.makeTimeStamp();

		Collection<LearningEvaluator> rep_contnet = LearningEvaluator.CentralResultsRepository.repository.getContent();
		
		if (rep_contnet.isEmpty()) return;
		
		//all learning evaluators should be set to the last fold number, which is equal to the total number of folds
		int num_of_folds = rep_contnet.iterator().next().actualFoldNumber;		
		
		results = new Result[rep_contnet.size()*num_of_folds]; 
		int a=0;
		for (LearningEvaluator learningEvaluator : rep_contnet)
		{
			for (int fold=0; fold < num_of_folds; fold++)
			{
				final int fold_number = fold+1;
				
				CentralResultsRepository repository = LearningEvaluator.CentralResultsRepository.repository;

				DiffCondition foldDiffCond = new DiffCondition() {
					@Override
					public boolean evaluate(DocumentDiff diff) {
						return diff.foldNumber == fold_number;
					}
				};

				AnnotationDiffer eval = 
//					repository.getOveralResults(learningEvaluator, new AllDiffsCondition());
					repository.getOveralResults(learningEvaluator, foldDiffCond);

				String trainPRName = MLEngine.renderPRNameTrain(learningEvaluator.getResponseASName());
				int numDocs = repository.getNumDocs(trainPRName, fold);
				int numTrainInst = repository.getNumTrainInst(trainPRName, learningEvaluator.getAnnotationTypes(), fold);;
				int tokensTotal = repository.getNumberOfTokens(learningEvaluator, foldDiffCond);
				results[a*num_of_folds+fold] = new Result(
						learningEvaluator, eval, tokensTotal, numDocs, numTrainInst, fold_number, timestamp );
			}
			a++;
		}				
	}
	
	public void addInfoFromTimeBechmark() throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
		TimeBenchmarkLogAnalysis a = new TimeBenchmarkLogAnalysis(
				TimeBenchmarkUtils.getTimeBenchmarkLogFileName());
		a.parse();
		
		for (int i = 0; i < results.length; i++)
		{
			Result r = results[i];
			int fold = r.getIntFoldNumber()-1;
			String pr_name = r.getResponsesASName();
			int trn_time = a.getTrainTimeForMLEngine(pr_name, fold);
			int tst_time = a.getTestTimeForMLEngine(pr_name, fold);
			logger.debug(String.format("train: %s %d", pr_name, trn_time));
			r.setTrainTime(Integer.toString(trn_time));
			logger.debug(String.format("test: %s %d", pr_name, tst_time));
			r.setTestTime(Integer.toString(tst_time));						
		}
		
		//slow variant:
		//GateUtils.doGateTimeBenchmarkReport(new TimeBenchmarkWekaReporter(this));
	}
	
	public void saveAll(String filename) throws IOException
	{
		boolean append = new File(filename).exists();
		
		FileOutputStream out = new FileOutputStream(filename, append);
		CsvWriter wr = new CsvWriter(out, ',', Charset.defaultCharset());
		
		int run_number;
		if (append)
		{
			run_number = lastRunNumberFromData(filename)+1;
		}
		else
		{
			run_number = 1;
			wr.writeRecord(header);
		}
		
		for (int i = 0; i < results.length; i++)
		{
			Result res = results[i];
			res.setRunNumber(run_number);
			wr.writeRecord(res.data);
			
		}
		
		wr.close();		
	}

	private int lastRunNumberFromData(String filename) throws IOException
	{
		int ret = 0;
		
		FileInputStream in = new FileInputStream(filename);
		CsvReader rd = new CsvReader(in, ',', Charset.defaultCharset());
		
		rd.readHeaders();
		while (rd.readRecord())
		{
			String cur = rd.get("Key_Run");
			int cur_int = Integer.parseInt(cur);
			ret = Math.max(ret, cur_int);
		}
		
		rd.close();
		
		return ret;
	}


	public static void main(String [] args) throws BenchmarkReportInputFileFormatException, URISyntaxException, IOException
	{
		String[][] data = 
		{
				{"data","2","1","Paum"},
				{"data","2","1","ILP_config_NE_roots"},
				{"data","2","1","ILP_config_NE_roots_subtree"},
				{"data","2","1","ILP_config"},		
				{"data","2","2","Paum"},
				{"data","2","2","ILP_config_NE_roots"},
				{"data","2","2","ILP_config_NE_roots_subtree"},
				{"data","2","2","ILP_config"},		
		};
		
		System.err.println(TimeBenchmarkUtils.createGateTimeBenchmarkReport());

		WekaResultExporter ex = new WekaResultExporter(data);
		ex.addInfoFromTimeBechmark();
		
		ex.saveAll("main.csv");
		
	}


}
