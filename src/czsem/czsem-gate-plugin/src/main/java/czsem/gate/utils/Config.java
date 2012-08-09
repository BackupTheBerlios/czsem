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
import czsem.utils.AbstractConfig;


public class Config extends AbstractConfig
{
	private static Config config = null;
	public static ClassLoader classLoader = null;
	private static final String config_filename = "czsem_config.xml";
	private static final String czsem_plugin_dir_name = "czsem_GATE_plugins";
	
	private String wekaJarPath;
	private String alephPath;
	private String prologPath;
	private String ilpSerialProjestsPath;
	private String wekaRunFuzzyILPClassPath;
	private String tmtRoot;
	private String tredRoot;
	private String gateHome;
	private String tmtSerializationDirectoryPath;
	private String logFileDirectoryPath;
	private String czsemPluginDir;
	private String learnigConfigDirectoryForGate;


	
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
//		ps.setInstallDefaults();
//		ps.saveToFile(czsem_plugin_dir_name+ '/' +config_filename_install);
		ps.saveToFile(czsem_plugin_dir_name+ '/' +config_filename);
		
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
	
	public static void loadConfig(ClassLoader classLoader) throws IOException, URISyntaxException
	{
		try
		{
			try
			{
				config = loadFromFile(czsem_plugin_dir_name+ '/' +config_filename, classLoader);
			} 
			catch (FileNotFoundException e)
			{
				try
				{
					config = loadFromFile("../../czsem/"+czsem_plugin_dir_name+ '/' +config_filename, classLoader);
				} 
				catch (FileNotFoundException e2)
				{
					config = loadFromFile(config_filename, classLoader);				
				}
			}
		} 
		catch (FileNotFoundException e)
		{
			if (Gate.isInitialised())
			{
				URL url = findCzesemPluginDirectoryURL();
				config = loadFromFile(
						Utils.URLToFilePath(url)+ '/' +config_filename, classLoader);
			}
			else throw e; 
		}
	}

	public static void loadConfig() throws IOException, URISyntaxException
	{
		if (Gate.isInitialised() && Config.classLoader == null)
			Config.classLoader = Gate.getClassLoader();
		
		loadConfig(classLoader);
	}
		
	public static Config getConfig() throws URISyntaxException, IOException
	{
		if (config == null) loadConfig();

/*
		if (config == null)
			try {
				loadConfig();
			} catch (IOException e) {
				System.err.println(new File(".").getAbsolutePath());
				e.printStackTrace();
			}
*/			
		return config;
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
	}

	public static Config loadFromFile(String filename, ClassLoader classLoader) throws IOException
	{
		Config c = (Config) loadAbstaractConfigFromFile(filename, classLoader);
		c.czsemPluginDir = new File(new File(filename).getParent()).getCanonicalPath();
		return c;
	}
	
	public String getCzsemPluginDir() {
		return czsemPluginDir;
	}

	public String getWekaJarPath() {
		return wekaJarPath;
	}

	public void setWekaJarPath(String wekaJarPath) {
		this.wekaJarPath = wekaJarPath;
	}

	public String getAlephPath() {
		return alephPath;
	}

	public void setAlephPath(String alephPath) {
		this.alephPath = alephPath;
	}

	public String getPrologPath() {
		return prologPath;
	}

	public void setPrologPath(String prologPath) {
		this.prologPath = prologPath;
	}

	public String getIlpProjestsPath() {
		return ilpSerialProjestsPath;
	}

	public void setIlpProjestsPath(String ilpProjestsPath) {
		this.ilpSerialProjestsPath = ilpProjestsPath;
	}

	public String getWekaRunFuzzyILPClassPath() {
		return wekaRunFuzzyILPClassPath;
	}

	public void setWekaRunFuzzyILPClassPath(String myClassPath) {
		this.wekaRunFuzzyILPClassPath = myClassPath;
	}
	
	public String getTmtRoot() {
		return tmtRoot;
	}

	public void setTmtRoot(String tmtRoot) {
		this.tmtRoot = tmtRoot;
	}

	public String getTredRoot() {
		return tredRoot;
	}

	public void setTredRoot(String tredRoot) {
		this.tredRoot = tredRoot;
	}


	public void setGateHome(String gateHome) {
		this.gateHome = gateHome;
	}


	public String getGateHome() {
		return gateHome;
	}

	public void setGateHome()
	{
		if (Gate.getGateHome() == null)
			Gate.setGateHome(new File(getGateHome()));
	}


	public void setTmtSerializationDirectoryPath(String tmtSerializationDirectoryPath) {
		this.tmtSerializationDirectoryPath = tmtSerializationDirectoryPath;
	}


	public URL getTmtSerializationDirectoryURL() throws MalformedURLException
	{
		return new File(getTmtSerializationDirectoryPath()).toURI().toURL();
	}

	public String getTmtSerializationDirectoryPath() {
		return tmtSerializationDirectoryPath;
	}


	public void setLogFileDirectoryPath(String logFileDirectoryPath) {
		this.logFileDirectoryPath = logFileDirectoryPath;
	}


	public String getLogFileDirectoryPath() {
		return logFileDirectoryPath;
	}


	public void setLearnigConfigDirectoryForGate(String learnigConfigDirectoryForGate) {
		this.learnigConfigDirectoryForGate = learnigConfigDirectoryForGate;
	}


	public String getLearnigConfigDirectoryForGate() {
		return learnigConfigDirectoryForGate;
	}

}
