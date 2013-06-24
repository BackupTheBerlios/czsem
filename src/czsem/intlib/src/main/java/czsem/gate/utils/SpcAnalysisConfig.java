package czsem.gate.utils;

import java.io.IOException;

import czsem.utils.AbstractConfig;

public class SpcAnalysisConfig extends AbstractConfig{
	
	public static SpcAnalysisConfig getConfig() throws ConfigLoadException
	{
		return (SpcAnalysisConfig) new SpcAnalysisConfig().getAbstractConfig();
	}
	
	@Override
	protected String getConfigKey() { return "spc_analysis_config";}
	
	
	@Override
	protected String getDefaultLoc() {
		return "../" +config_dir+ '/' +"spc_analysis_config.xml";
	}

	public static void main(String[] args) throws IOException
	{
		
		SpcAnalysisConfig ps = new SpcAnalysisConfig();
		ps.initNullConfig();
		ps.setMyWinValues();
		ps.save();
		System.err.format("MyWinValues saved to '%s' !", ps.getDefaultLoc());
	}

	public void setMyWinValues() {
		setSpcAllDirectory(    "C:/Users/dedek/Desktop/DATLOWE/SPC_all/");
		setSpcCsvFileName(     "C:/Users/dedek/Desktop/DATLOWE/LP_SPC.csv");
		setGateApplicationFile("C:/Users/dedek/Desktop/DATLOWE/gate_apps/all.gapp");
		setErrorFilesDirectory("C:/Users/dedek/Desktop/DATLOWE/SPC_err/");
		setDataStoreDir(	   "C:/Users/dedek/Desktop/DATLOWE/SPC_store/");
		
	}

	public String getSpcCsvFileName() {
		return get("spcCsvFileName");
	}

	public String getSpcAllDirectory() {
		return get("spcAllDirectory");
	}
	
	public String getDataStoreDir() {
		return get("dataStoreDir");
	}
	
	public String getGateApplicationFile() {
		return get("gateApplicationFile");
	}

	public void setSpcCsvFileName(String spcCsvFileName) {
		set("spcCsvFileName", spcCsvFileName);
	}

	public void setSpcAllDirectory(String spcAllDirectory) {
		set("spcAllDirectory", spcAllDirectory);
	}

	public void setDataStoreDir(String dataStoreDir) {
		set("dataStoreDir", dataStoreDir);
	}

	public void setGateApplicationFile(String gateApplicationFile) {
		set("gateApplicationFile", gateApplicationFile);
	}

	public String getErrorFilesDirectory() {
		return get("errorFilesDirectory");
	}

	public void  setErrorFilesDirectory(String errorFilesDirectory) {
		set("errorFilesDirectory", errorFilesDirectory);
	}
}
