package czsem.utils;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AbstractConfig {
	public void saveToFile(String filename) throws IOException
	{
		// Create output stream.
		FileOutputStream fos = new FileOutputStream(filename);

		// Create XML encoder.
		XMLEncoder xenc = new XMLEncoder(fos);

		// Write object.
		xenc.writeObject(this);
		xenc.flush();
		xenc.close();
		fos.close();
	}

	public static AbstractConfig loadAbstaractConfigFromFile(String filename, ClassLoader classLoader) throws IOException
	{
		FileInputStream os = new FileInputStream(filename);
		XMLDecoder decoder = new XMLDecoder(os, null, null, classLoader);
		AbstractConfig c = (AbstractConfig)decoder.readObject();
		decoder.close();
		os.close();
		return c;
	}


}
