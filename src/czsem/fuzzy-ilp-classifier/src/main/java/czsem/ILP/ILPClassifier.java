package czsem.ILP;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Capabilities.Capability;
import czsem.ILP.Serializer.Relation;
import czsem.ILP.WekaSerializer.Condition;
import czsem.utils.Config;
import czsem.utils.ProjectSetup;

public abstract class ILPClassifier extends Classifier
{
	private static final long serialVersionUID = 2895303597421297747L;

	protected Relation [] crisp_relations;
	
	/** used for positive examples generation **/
	protected String [] class_values = null;
	
	protected ProjectSetup project_setup = null;
	
	protected int last_test_id = 10000;
	
	protected transient ILPExec testing_ILP_proecess = null;
	
	protected final String options[] = 
	{
			":- set(noise,0).",//0 
			":- set(i,2).",//1 
			":- set(depth,10).",//2 
			":- set(clauselength,4).",//3 
			":- set(search,bf).",//4 
			":- set(evalfn,coverage).",//5 
			":- set(samplesize,0).",//6 
			"",//7 
			"",//8 
			"",//9 
			"",//10 
			"",//11 
			"",//12 
			"",//13 
			"",//14 
			":- set(verbosity,0).",//15 
	};
			
	/** Initializes class_values array, which is alter used for positive examples generation. **/
	protected void initClassValues(Instances instances)
	{
		Attribute class_attr = instances.classAttribute(); 
		if (class_attr.isNumeric())
		{
			Set<String> vals_set = new HashSet<String>();

			for (int i=0; i<instances.numInstances(); i++)
			{
				vals_set.add(instances.instance(i).toString(class_attr));
			}
						
			class_values = vals_set.toArray(new String[0]);
		}
		else	//non-numeric values
		{
			class_values = new String [class_attr.numValues()];
			
			for (int a=0; a<class_values.length; a++)
			{
				class_values[a] = class_attr.value(a);
			}
		}		
	}
	
	
	public Capabilities getCapabilities()
	{
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(3);
		result.setOwner(this);

		return result;
	}	
	
	protected void learnRules() throws IOException, InterruptedException, URISyntaxException
	{
		ILPExec exec = new ILPExec(project_setup);
		exec.startILPProcess();
		
		if (getDebug())
			exec.startReaderThreads("learning");
		else
			exec.startNullReaderThreads();			
		
		exec.induceAndWriteRules();
		exec.close();
	}

	protected void setupProject(String project_name) throws URISyntaxException, IOException
	{
		project_setup = new ProjectSetup();
		project_setup.dir_for_projects = Config.getConfig().getIlpProjestsPath()+'/';
		project_setup.project_name = project_name;
		project_setup.init_project();		
	}

	protected void putCripsModes(Serializer backg_ser)
	{
		backg_ser.putCommentLn("--- C R I S P     M O D E S ---");
		for (int i = 0; i < crisp_relations.length; i++)
		{
			backg_ser.putBinaryMode(crisp_relations[i], "1", '+', '#');

			//			monot.: "*" instead of "1"
			//			ilp_ser.putBinaryMode(atl_ser, "*", '+', '#');			
		}
	}
	
	protected void serializeToILP(Instances instances) throws FileNotFoundException, UnsupportedEncodingException
	{
		WekaSerializer backg_ser = new WekaSerializer(project_setup.renderProjectFileName(".b"));
		WekaSerializer pos_ser = new WekaSerializer(project_setup.renderProjectFileName(".f"));
		WekaSerializer neg_ser = new WekaSerializer(project_setup.renderProjectFileName(".n"));
		
		crisp_relations = backg_ser.addAttributeRelations(instances.firstInstance());
		
		pos_ser.addAttributeRelation(instances.classAttribute().name());
		neg_ser.addAttributeRelation(instances.classAttribute().name());
		
		
		StringBuilder learn_set_sb = new StringBuilder();
		for (int i = 0; i < options.length; i++)
		{
			learn_set_sb.append(options[i]);
			learn_set_sb.append('\n');			
		}
		
		backg_ser.putLearningSettings(learn_set_sb.toString());
		putModes(backg_ser);
		putLearningAxioms(backg_ser, instances.classIndex());
		putDeteminations(backg_ser, instances.classIndex());
		
		
		
		backg_ser.putCommentLn("");
		backg_ser.putCommentLn("--- T U P L E S ---");
		
		for (int i = 0; i < instances.numInstances(); i++)
		{
			backg_ser.putCommentLn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			backg_ser.putBkgTuplesForInstance(instances.instance(i), "id_"+i, crisp_relations);
			

			//E X A M P L E S	
			pos_ser.putInstanceExamples(
					instances.instance(i),
					class_values,
					"id_"+i,
					getTargetCalssRelation(instances.classIndex()),
					getPositiveExmplesCondition());

			neg_ser.putInstanceExamples(
					instances.instance(i),
					class_values,
					"id_"+i,
					getTargetCalssRelation(instances.classIndex()),
					new WekaSerializer.NegCondition(getPositiveExmplesCondition()));									
		}
		
				
		backg_ser.putCommentLn("");
		backg_ser.putCommentLn("--- V A L U E S ---");		
		backg_ser.outputAllTypes();		
	}
	

	protected void putDeteminations(WekaSerializer backg_ser, int class_index, Relation [] rels)
	{
		for (int i = 0; i < rels.length; i++)
		{						
			if (i == class_index) continue;
			backg_ser.putDetermination(rels[class_index], rels[i]);
		}				
	}
	
	protected void startTestingILPProcess(int class_index) throws IOException, URISyntaxException
	{
		WekaSerializer test_ser = new WekaSerializer(project_setup.renderProjectFileName(".test"));
		putTestingAxioms(test_ser, class_index);
		
		testing_ILP_proecess = new ILPExec(project_setup);
		testing_ILP_proecess.startPrologProcess(testing_ILP_proecess.getRulesFileName());
		
		if (getDebug())
		{
			System.out.print("ILPClassifier.startTestingILPProcess(): ");
			System.err.println("loading test ILP process");			
			testing_ILP_proecess.startErrReaderThread("run_test");
		}
		else
			testing_ILP_proecess.startNullErrReaderThread();			
		
		testing_ILP_proecess.consultFile(project_setup.project_name + ".test");
	}
	
	@Override
	public double classifyInstance(Instance instance) throws Exception
	{		
		if (testing_ILP_proecess == null) startTestingILPProcess(instance.classIndex());
				
		String test_id = "test_id_" + last_test_id;
//		System.err.println(test_id);

		WekaSerializer test_ser = new WekaSerializer(testing_ILP_proecess.getOutputStream());
		test_ser.assertBkgTuplesForInstance(instance, test_id, crisp_relations);
		
		test_ser.putTestClassPredicate(test_id, crisp_relations[instance.classIndex()]);
						
		String predicted = testing_ILP_proecess.readLine();
		
/*begin testing*		
		System.err.println("NEW predicted: "+ predicted);
		String old_prediciton = __classifyInstance_old(instance);
		if (! predicted.equals(old_prediciton))
		{
			System.err.printf("Old prediciton is different! old: %s <> new: %s\n");
		}
		System.err.println("------------------------------------");
/*end testing*/		
		last_test_id++;

		

		
		if (predicted.equals("END")) return Instance.missingValue();
		
		if (instance.classAttribute().isNumeric()) return Double.parseDouble(predicted);
		
		return instance.classAttribute().indexOfValue(predicted);
	}
	
	public String __classifyInstance_old(Instance instance) throws Exception
	{		
		
		WekaSerializer log_ser = new WekaSerializer(project_setup.renderProjectFileName("_old.test_log"), true);

		WekaSerializer test_ser = new WekaSerializer(project_setup.renderProjectFileName("_old.test"));
//		crisp_relations = test_ser.addAttributeRelations(instance);
		
		String test_id = "old_test_id_" + last_test_id;

		test_ser.putBkgTuplesForInstance(instance, test_id, crisp_relations);
		log_ser.putBkgTuplesForInstance(instance, test_id, crisp_relations);
		putTestingAxioms(test_ser, instance.classIndex());
		
		test_ser.print(":-");
		test_ser.putTestClassPredicate(test_id, crisp_relations[instance.classIndex()]);
		test_ser.close();
		
		ILPExec test_exec = new ILPExec(project_setup);
		test_exec.startPrologProcess(test_exec.getRulesFileName());
		test_exec.startErrReaderThread("run_old_test");
//		test_exec.consultFile(project_setup.project_name + ".b");
		test_exec.consultFile(project_setup.project_name + "_old.test");
				

		String predicted = test_exec.readLine();
		test_exec.close();

		System.err.println("OLD predicted: "+ predicted);
		
		return predicted;
		
/*		
		
		
		if (predicted.equals("END")) return Instance.missingValue();
		
		if (instance.classAttribute().isNumeric()) return Double.parseDouble(predicted);
		
		return instance.classAttribute().indexOfValue(predicted);
*/		
	}



	@Override
	public String toString()
	{
		if (project_setup == null) return super.toString();
		
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append('\n');
		
		try
		{		
			sb.append(project_setup.renderProjectFileName(""));
			sb.append('\n');
		
			//print rules
			char [] buf = new char[1000];
			BufferedReader is = new BufferedReader(new FileReader(ILPExec.renderRulesFilePathName(project_setup)));
			for (int i=is.read(buf); i>=0; i=is.read(buf))
			{
				sb.append(buf, 0, i);
			}
			is.close();
		}
		catch (Exception e)
		{
			sb.append(e.toString());
		}
						
		return sb.toString();
	}


	protected void putTestingAxioms(WekaSerializer testSer, int classIndex) {}


	protected void putLearningAxioms(WekaSerializer backg_ser, int class_attribute_index) {};
	protected abstract Condition getPositiveExmplesCondition();
	protected abstract void putDeteminations(WekaSerializer backg_ser, int class_index);
	protected abstract void putModes(WekaSerializer backg_ser);
	protected abstract Relation getTargetCalssRelation(int class_attribute_index);


	@Override
	public abstract void buildClassifier(Instances data) throws Exception;

	public void buildClassifier(String project_name, Instances instances) throws Exception
	{
		last_test_id =  (int) Math.pow(10, ((int) Math.log10(instances.numInstances())) +1)   ;
		
		setupProject(project_name);
		
		initClassValues(instances);
		
		serializeToILP(instances);
		
		learnRules();
	}



	@Override
	protected void finalize() throws Throwable
	{
		if (testing_ILP_proecess != null) testing_ILP_proecess.close();
	}
	
	

	@Override
	public String[] getOptions()
	{
		String [] ret = new String [options.length+1];
		for (int i = 1; i < ret.length; i++)
		{
			ret[i] = options[i-1];			
		}
		ret[0] = "-O";
		return ret;
	}

	@Override
	public void setOptions(String[] new_options) throws Exception
	{
		for (int i = 0; i < options.length; i++) {
			options[i] = "";
		}

		if (new_options.length < 2) 
		{
			return;
		}
	    
		assert new_options[0].equals("-O");

		for (int i = 1; i < new_options.length; i++) {
			options[i-1] = new_options[i];
		}
	}

	@Override
	public Enumeration<Option> listOptions()
	{			
		return new Enumeration<Option>() {
			@Override
			public Option nextElement() {				
				return new Option(
						"Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'",
						"O", options.length, "-O <options>");
			}			
			boolean more = true; 
			@Override
			public boolean hasMoreElements() {
				boolean ret = more;
				more = false;
				return ret;
			}
		};
	}
	
	public String opt00TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt01TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt02TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt03TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt04TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt05TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt06TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt07TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt08TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt09TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt10TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt11TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt12TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt13TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt14TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }
	public String opt15TipText() { return "Arbitratry options for ILP learner. e.g.: ':-set(noise,3).'"; }

	public String getOpt00() { return options[0]; }
	public String getOpt01() { return options[1]; }
	public String getOpt02() { return options[2]; }
	public String getOpt03() { return options[3]; }
	public String getOpt04() { return options[4]; }
	public String getOpt05() { return options[5]; }
	public String getOpt06() { return options[6]; }
	public String getOpt07() { return options[7]; }
	public String getOpt08() { return options[8]; }
	public String getOpt09() { return options[9]; }
	public String getOpt10() { return options[10]; }
	public String getOpt11() { return options[11]; }
	public String getOpt12() { return options[12]; }
	public String getOpt13() { return options[13]; }
	public String getOpt14() { return options[14]; }
	public String getOpt15() { return options[15]; }
	
	public void   setOpt00(String new_val) { options[0] = new_val; }
	public void   setOpt01(String new_val) { options[1] = new_val; }
	public void   setOpt02(String new_val) { options[2] = new_val; }
	public void   setOpt03(String new_val) { options[3] = new_val; }
	public void   setOpt04(String new_val) { options[4] = new_val; }
	public void   setOpt05(String new_val) { options[5] = new_val; }
	public void   setOpt06(String new_val) { options[6] = new_val; }
	public void   setOpt07(String new_val) { options[7] = new_val; }
	public void   setOpt08(String new_val) { options[8] = new_val; }
	public void   setOpt09(String new_val) { options[9] = new_val; }
	public void   setOpt10(String new_val) { options[10] = new_val; }
	public void   setOpt11(String new_val) { options[11] = new_val; }
	public void   setOpt12(String new_val) { options[12] = new_val; }
	public void   setOpt13(String new_val) { options[13] = new_val; }
	public void   setOpt14(String new_val) { options[14] = new_val; }
	public void   setOpt15(String new_val) { options[15] = new_val; }

	
	public static void main(String[] args)	
	{		
		weka.gui.GUIChooser.main(args);	
	}

}
