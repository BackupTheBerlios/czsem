package cuni.mff.intlib.law;

import gate.DataStore;
import gate.Document;
import gate.Factory;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.persist.SerialDataStore;
import gate.security.SecurityException;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import czsem.Utils;
import czsem.encoding.HttpTargetContentDetector;
import czsem.gate.utils.GateUtils;

public class CzechLawDownloader {
	
	public interface UrlGenerator {

		String getNextUrl();

	}
	
	public static Logger logger = Logger.getLogger(CzechLawDownloader.class); 



	public static String storeAdr = "C:\\data\\law\\raw-list\\store";
	
/*
	public static class PortalGovCzUrlGenerator implements UrlGenerator
	{
		static String urlFormatBase = "http://portal.gov.cz/app/zakony/download?idBiblio=%s&nr=%d~2F%d&ft=txt";
		
		
//		http://portal.gov.cz/app/zakony/download?nr=1~2F2011&ft=txt		
//		http://portal.gov.cz/app/zakony/download?nr=1~2F2011~20Sb.&ft=txt
//		http://portal.gov.cz/app/zakony/download?idBiblio=73427&nr=3~2F2011~20Sb.&ft=txt
//		http://portal.gov.cz/app/zakony/download?idBiblio=73425&nr=1~2F2011&ft=txt
//		http://portal.gov.cz/app/zakony/download?idBiblio=76232&nr=1~2F2012~20Sb.&ft=txt

		public static String urlForLaw(int year, int num, String session)
		{
			return String.format(urlFormatBase, session, num, year);
		}
		
		int year = 2011;
		int numFirst = 1;
		int numLast = 471;
		int numCurrent = numFirst;
		String session = "73425"; 
		
		

		@Override
		public String getNextUrl() {
			if (numCurrent > numLast)
				return null;
			else
				return urlForLaw(year, numCurrent++, session);
		}
		
	}
	*/
	

	public static void main(String[] args) throws Exception {
		GateUtils.initGate(Level.DEBUG);
		
/*
		deleteStore();		
		DataStore store = Factory.createDataStore(SerialDataStore.class.getCanonicalName(), Utils.filePathToUrl(storeAdr).toString());
/**/
		DataStore store = Factory.openDataStore(SerialDataStore.class.getCanonicalName(), Utils.filePathToUrl(storeAdr).toString());
/**/

		//CzechLawDownloader cld = new CzechLawDownloader(store, new PortalGovCzUrlGenerator());
		
		
		CzechLawDownloader cld = new CzechLawDownloader(store, new UrlGenerator() {
			
//			String urlBase = "http://www.zakonynawebu.cz/cgi-bin/khm.cgi?akce=Vyhledat4&typ=1&pr=1&no=1&ms=1&platne=0&indexcis=1&zneni=0&rous=1&ronss=1&rons=1&oblastv=6&razeni=1&pozfile=";
			String urlBase = "http://portal.gov.cz/app/zakony/download?ft=txt&idBiblio=";
			
//first 2011 
//			int suffix = 73425;

//first 2012 
//			int suffix = 74780;
			
			
// current last
			int suffix = 75676;
			
			int increment = 1;
			
			@Override
			public String getNextUrl() {
				String ret = urlBase + suffix;
				suffix+= increment;
				return ret ;				
			}
		});
		cld.downloadBatch(76232 - 73425 + 50);
		
		store.close();
		
		

	}



	protected long delay = 1000*1;
	protected DataStore dataStore;
	protected UrlGenerator urlGenerator;
	protected int docNumber = 0;



	public void downloadBatch(int numDocs) throws InterruptedException, PersistenceException, ResourceInstantiationException, IOException, SecurityException {
		for (int i = 0; i < numDocs; i++) {
			downloadNextDocument();
		}		
	}



	protected void downloadNextDocument() throws InterruptedException, PersistenceException, ResourceInstantiationException, IOException, SecurityException {
		Thread.sleep(getDelay());
		String url = urlGenerator.getNextUrl();
		Document doc = HttpTargetContentDetector.gateDocFromUrl(url);
		
		String strCont = doc.getContent().toString().replace("\r\n", "\n");
		int beginIndex = 0;
		if (strCont.startsWith("\n")) beginIndex = 1;
		
		String title = strCont.substring(beginIndex, strCont.indexOf('\n', beginIndex));
		
		if (! title.startsWith("<aspi><error"))
		{
			doc.setName(title);
			
			doc.setDataStore(dataStore);
			
			dataStore.sync(doc);
			
		}
				
		logger.info(String.format("doc%7d size: %7d %s url: %s", docNumber++, strCont.length(), title, url));
		Factory.deleteResource(doc);
		
	}



	public CzechLawDownloader(DataStore dataStore, UrlGenerator urlGenerator) {
		this.dataStore = dataStore;
		this.urlGenerator = urlGenerator;
	}



	protected static void deleteStore() throws IOException {
		FileUtils.deleteDirectory(new File(storeAdr));		
	}



	public void setDelay(long delay) {
		this.delay = delay;
	}



	public long getDelay() {
		return delay;
	}

}
