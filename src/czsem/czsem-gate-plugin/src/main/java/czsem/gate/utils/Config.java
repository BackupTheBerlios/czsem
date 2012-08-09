package czsem.gate.utils;

import gate.Gate;
import gate.util.GateException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import czsem.Utils;


public class Config extends czsem.utils.Config
{
	private static Config config = null;
	public static ClassLoader classLoader = null;
	private static final String czsem_plugin_dir_name = "czsem-gate-plugin";
	
	public static void main(String[] args) throws IOException, GateException, URISyntaxException
	{
		/*
		Config cfg = getConfig();
//		cfg.setMyWinValues();
		cfg.setGateHome();
		Gate.init();
	    czsem.gate.GateUtils.registerCzsemPlugin();;
	    System.err.println(getConfig().getGateHome());
		/**/

/**/
		Config ps = new Config();
		ps.setMyWinValues();
		ps.save();
//		ps.setInstallDefaults();
//		ps.saveToFile(czsem_plugin_dir_name+ '/' +config_filename_install);
//		ps.saveToFile(czsem_plugin_dir_name+ '/' +config_filename);
		
/*				
		Config ps2 = new Config();
		ps2 = loadFromFile("config1.xml");
/**/				
	}


	protected static URL findCzesemPluginDirectoryURL()
	{
		@SuppressWarnings("unchecked")
		Set<URL> dirs = Gate.getCreoleRegister().getDirectories();
		for (Iterator<URL> iterator = dirs.iterator(); iterator.hasNext();)
		{
			URL url = iterator.next();
			if (url.toString().endsWith(czsem_plugin_dir_name + '/'))
				return url;
		}
		return null;		
	}
	

	public void loadConfig() throws IOException, URISyntaxException
	{
		if (Gate.isInitialised() && Config.classLoader == null)
			Config.classLoader = Gate.getClassLoader();
		
		loadConfig(classLoader);
	}

	@Override
	public void loadConfig(ClassLoader classLoader) throws IOException, URISyntaxException
	{
		try {
			super.loadConfig(classLoader);
		} catch (FileNotFoundException e){
			if (Gate.isInitialised())
			{
				URL url = findCzesemPluginDirectoryURL();
				super.loadConfig(
						Utils.URLToFilePath(url)+ '/' +config_filename, classLoader);
			}
			else throw e;
		}
	}

		
	public static synchronized Config getConfig() throws IOException, URISyntaxException
	{
		if (config == null) {
			config = new Config();
			config.loadConfig();
		}
		return (Config) config;
	}
			
	public void setInstallDefaults()
	{
		setAlephPath("$aleph");
		setIlpProjestsPath("$projects/ILP_serial_projects");
		setPrologPath("$prolog");
		setWekaJarPath("$weka");
		setWekaRunFuzzyILPClassPath("$INSTALL_PATH/FuzzyILP.jar");
	}

	public void setMyWinValues()
	{
		setAlephPath("C:\\Program Files\\aleph\\aleph.pl");
		setPrologPath("C:\\Program Files\\Yap\\bin\\yap.exe");
		setWekaJarPath("C:\\Program Files\\Weka-3-6\\weka.jar");
		setWekaRunFuzzyILPClassPath("C:\\workspace\\czsem\\src\\java\\czsem\\target\\classes");
		setTmtRoot("C:\\workspace\\tectomt");
		setTredRoot("C:\\tred");
		setGateHome("C:\\Program Files\\gate\\GATE-6.0");
		setTmtSerializationDirectoryPath(
								"C:\\workspace\\czsem\\src\\java\\czsem\\czsem_GATE_plugins\\TmT_serializations");
		setLogFileDirectoryPath("C:\\workspace\\czsem\\src\\java\\czsem\\czsem_GATE_plugins\\log");
		setIlpProjestsPath(		"C:\\workspace\\czsem\\src\\java\\czsem\\ILP_serializations");
		setLearnigConfigDirectoryForGate(	"C:\\workspace\\czsem\\src\\java\\czsem\\gate-learning");
		setCzsemPluginDir("C:\\workspace\\czsem\\src\\java\\czsem\\czsem_GATE_plugins");
	}

	public void setCzsemPluginDir(String czsemPluginDir) {
		set("czsemPluginDir", czsemPluginDir);
	}


	public String getCzsemPluginDir() {
		return get("czsemPluginDir");
	}

	public String getWekaJarPath() {
		return get("wekaJarPath");
	}

	public void setWekaJarPath(String wekaJarPath) {
		set("wekaJarPath", wekaJarPath);
	}


	public String getWekaRunFuzzyILPClassPath() {
		return get("wekaRunFuzzyILPClassPath");
	}

	public void setWekaRunFuzzyILPClassPath(String myClassPath) {
		set("wekaRunFuzzyILPClassPath", myClassPath);
	}
	
	public String getTmtRoot() {
		return get("tmtRoot");
	}

	public void setTmtRoot(String tmtRoot) {
		set("tmtRoot", tmtRoot);
	}

	public String getTredRoot() {
		return get("tredRoot");
	}

	public void setTredRoot(String tredRoot) {
		set("tredRoot", tredRoot);
	}


	public void setGateHome(String gateHome) {
		set("gateHome", gateHome);
	}


	public String getGateHome() {
		return get("gateHome");
	}

	public void setGateHome()
	{
		if (Gate.getGateHome() == null)
			Gate.setGateHome(new File(getGateHome()));
	}


	public void setTmtSerializationDirectoryPath(String tmtSerializationDirectoryPath) {
		set("tmtSerializationDirectoryPath", tmtSerializationDirectoryPath);
	}


	public URL getTmtSerializationDirectoryURL() throws MalformedURLException
	{
		return new File(getTmtSerializationDirectoryPath()).toURI().toURL();
	}

	public String getTmtSerializationDirectoryPath() {
		return get("tmtSerializationDirectoryPath");
	}


	public void setLogFileDirectoryPath(String logFileDirectoryPath) {
		set("logFileDirectoryPath", logFileDirectoryPath);
	}


	public String getLogFileDirectoryPath() {
		return get("logFileDirectoryPath");
	}


	public void setLearnigConfigDirectoryForGate(String learnigConfigDirectoryForGate) {
		set("learnigConfigDirectoryForGate", learnigConfigDirectoryForGate);
	}


	public String getLearnigConfigDirectoryForGate() {
		return get("learnigConfigDirectoryForGate");
	}

}
