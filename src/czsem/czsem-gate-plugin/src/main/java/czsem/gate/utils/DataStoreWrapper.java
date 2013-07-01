package czsem.gate.utils;

import gate.Document;
import gate.Factory;
import gate.LanguageResource;
import gate.Resource;
import gate.corpora.DocumentImpl;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
	
	public class ResourceIterator <T extends Resource> implements Iterator<T> {

		private Iterator<String> lrIdsIterator;
		private Class<T> cls;

		@SuppressWarnings("unchecked")
		public ResourceIterator(Class<T> cls) throws PersistenceException {
			this.cls = cls;
			lrIdsIterator = ds.getLrIds(cls.getCanonicalName()).iterator();
		}

		@Override
		public boolean hasNext() {
			return lrIdsIterator.hasNext();
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			try {
				return (T) GateUtils.loadResourceFormDatastore(ds, cls.getCanonicalName(), lrIdsIterator.next());
			} catch (ResourceInstantiationException e) {
				throw new NoSuchElementException(e.toString());
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("not implemented");
		}
		
	}

	public Iterable<? extends Document> iterateAllDocuments() {
		return new Iterable<DocumentImpl>() {
			
			@Override
			public Iterator<DocumentImpl> iterator() {
				try {
					return new ResourceIterator<DocumentImpl>(DocumentImpl.class);
				} catch (PersistenceException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

}
