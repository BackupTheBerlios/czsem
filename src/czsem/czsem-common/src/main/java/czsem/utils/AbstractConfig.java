package czsem.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractConfig {
	
	protected static AbstractConfig config = null; 
	
	
	protected Map<String, Object> property_map = new HashMap<String, Object>();
	protected String config_filename = "czsem_config.xml";
	protected String config_dir = "configuration";
	protected String config_envp = "CZSEM_CONFIG";

	
	protected void save() throws IOException {
		saveToFile(getDefaultLoc());		
	}

	
	protected void set(String key, Object value)
	{
		property_map.put(key, value);
	}

	protected String get(String key)
	{
		return (String) property_map.get(key);
	}

	protected Object getObj(String key)
	{
		return property_map.get(key);
	}
		
	public void saveToFile(String filename) throws IOException
	{
		// Create output stream.
		new File(filename).getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(filename);

		// Create XML encoder.
		XMLEncoder xenc = new XMLEncoder(fos);

		// Write object.
		xenc.writeObject(property_map);
		xenc.flush();
		xenc.close();
		fos.close();
	}

	public static Map<String, Object> loadFromFile(String filename, ClassLoader classLoader) throws IOException
	{
		FileInputStream os = new FileInputStream(filename);
		XMLDecoder decoder = new XMLDecoder(os, null, null, classLoader);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) decoder.readObject();
		decoder.close();
		os.close();
		return map;
	}
	
	public void loadConfig(String filename, ClassLoader classLoader) throws IOException
	{
		property_map = loadFromFile(filename, classLoader);		
	}

	public void loadConfig(ClassLoader classLoader) throws ConfigLoadEception
	{
		ConfigLoadEception fe = new ConfigLoadEception();
		
		//first: try env
		try {
			String env = System.getenv(config_envp);
			if (env == null) throw new FileNotFoundException(String.format("Environment property  '%s' not set.", config_envp));
			loadConfig(env, classLoader);
			return;
		}
		catch (Exception e)	{fe.add(e);}

		//second: try default loc
		try {
			loadConfig(getDefaultLoc(), classLoader);				
			return;
		} catch (Exception e) {fe.add(e);}

		//third: try ../default loc
		try {
			loadConfig("../" +getDefaultLoc(), classLoader);			
			return;
		} catch (Exception e) {fe.add(e);}
		
		throw fe;
	}

	protected String getDefaultLoc() {
		return "../" +config_dir+ '/' +config_filename;
	}
	
	public static class ConfigLoadEception extends FileNotFoundException
	{
		private static final long serialVersionUID = -5616178151757529473L;
		
		protected List<Exception> causes = new ArrayList<Exception>();
		
		public void add(Exception e) {
			causes.add(e);
		}

		@Override
		public String getMessage() {
			StringBuilder sb = new StringBuilder(String.format("Configuration file could not be loaded for following (%d) reasons:\n", causes.size()));
			for (int a=0; a<causes.size(); a++)
			{
				sb.append(String.format("%d: %s\n", a+1, causes.get(a).toString()));
			}
			return sb.toString();
		}
		
	}


	public void loadConfig() throws IOException, URISyntaxException {
		loadConfig(null);
	}



}
