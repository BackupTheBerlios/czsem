package cuni.mff.intlib.law;

import gate.Corpus;
import gate.DataStore;
import gate.creole.dumpingPR.DumpingPR;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.io.File;
import java.net.URL;

@CreoleResource
public class CorpusNameAwareExporter extends DumpingPR {
	private static final long serialVersionUID = 3474963034298527876L;
	private DataStore dataStore;
	

	@Override
	public void setCorpus(Corpus corpus) {
		super.setCorpus(corpus);
		
		if (corpus == null) return;
		

		try {
			URL newUrl = new URL(getOutputDirectoryUrl(), getCorpus().getName().replace(' ', '_'));
			new File(newUrl.toURI()).mkdirs();
			setOutputDirectoryUrl(newUrl);
			
			/*
			DataStore ds = getDataStore();
			if (ds != null)
			{
				LanguageResource corpusPers = ds.adopt(corpus, null);
				ds.sync(corpusPers);
			}
			*/
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public DataStore getDataStore() {
		return dataStore;
	}


	@CreoleParameter(defaultValue="null")
	@RunTime
	@Optional
	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
}