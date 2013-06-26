package czsem.gate.utils;

import gate.Document;
import gate.Factory;
import gate.LanguageResource;
import gate.corpora.DocumentImpl;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;

import java.io.File;
import java.net.MalformedURLException;

public class DataStoreWrapper {
	
	protected SerialDataStore ds;
	protected String dataStoreDir;


	public DataStoreWrapper(String dataStoreDir) {
		this.dataStoreDir = dataStoreDir;
	}

	public void openExisting() throws PersistenceException, MalformedURLException {
		ds = (SerialDataStore) Factory.openDataStore(
				SerialDataStore.class.getCanonicalName(), 
				new File(dataStoreDir).toURI().toURL().toString());
	}

	public void createNew() throws PersistenceException, MalformedURLException {
		ds = (SerialDataStore) Factory.createDataStore(
				SerialDataStore.class.getCanonicalName(), 
				new File(dataStoreDir).toURI().toURL().toString());
	}
	
	public void openOrCreate() throws MalformedURLException, PersistenceException {
		try {
			openExisting();
		} catch (PersistenceException e) {
			createNew();
		}
	}
	
	public static String lrNameToLRPersistenceId(String lrName) {
		//return lrName + "___" + new Date().getTime() + "___" + random();
		
		return lrName + "___" + "0" + "___" + ".ser";
	}

	public void persistDoc(Document gateDoc) throws PersistenceException, SecurityException {
		gateDoc.setLRPersistenceId(lrNameToLRPersistenceId(gateDoc.getName()));
		ds.adopt(gateDoc, null);
		ds.sync(gateDoc);
		
	}

	public boolean containsGateDocument(String fileName) {
	    return containsResource(DocumentImpl.class, fileName);
	}

	public boolean containsResource(Class<? extends LanguageResource> c, String fileName) {
	    File resourceTypeDir = new File(ds.getStorageDir(), c.getCanonicalName());
	    
	    if(! resourceTypeDir.exists()) return false;
	    
	    File resourceFile = new File(resourceTypeDir, lrNameToLRPersistenceId(fileName)); 

	    return resourceFile.exists();
	}

	public void close() throws PersistenceException {
		ds.close();
	}

}
