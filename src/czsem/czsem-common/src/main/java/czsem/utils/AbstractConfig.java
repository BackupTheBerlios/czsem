package czsem.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class AbstractConfig {
	
	protected static AbstractConfig config = null; 
	
	
	protected Map<String, Object> property_map = new HashMap<String, Object>();
	protected String config_filename = "czsem_config.xml";
	protected String config_dir = "configuration";

	
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

	public void loadConfig(ClassLoader classLoader) throws IOException, URISyntaxException
	{
		try {
			loadConfig(getDefaultLoc(), classLoader);
		} catch (FileNotFoundException e)
		{
			loadConfig("../" +getDefaultLoc(), classLoader);			
		}
	}

	protected String getDefaultLoc() {
		return "../" +config_dir+ '/' +config_filename;
	}


	public void loadConfig() throws IOException, URISyntaxException {
		loadConfig(null);
	}



}
