package czsem.gate.plugins;

import gate.Document;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.Optional;
import gate.util.InvalidOffsetException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import czsem.Utils;
import czsem.gate.AbstractLanguageAnalyserWithInputOutputAS;
import czsem.gate.tectomt.Annotator;
import czsem.gate.tectomt.SentenceInfoManager;
import czsem.gate.tectomt.TMTSAXParser;
import czsem.gate.utils.Config;
import czsem.utils.ProcessExec;

public class TectoMTAbstractAnalyser extends AbstractLanguageAnalyserWithInputOutputAS
{
	private static final long serialVersionUID = 1L;
	
	private URL scenarioFilePath = null;
	private List<String> blocks = null;
	private boolean loadScenarioFromFile = true;
	private String language = "english";

	
	@Optional
	@CreoleParameter(defaultValue="file:../resources/TectoMT/analysis_scenarios/english_full_blocks.scen")
	public void setScenarioFilePath (URL scenarioFilePath ) {
		
		this.scenarioFilePath  = scenarioFilePath ;
	}

	public URL getScenarioFilePath() {
		return scenarioFilePath;
	}

	
	@Optional
	@CreoleParameter(comment="List of blocks to be used in the analysis", defaultValue="SEnglishW_to_SEnglishM::Sentence_segmentation;SEnglishW_to_SEnglishM::Tokenization")
	public void setBlocks(List<String> blocks) {
		this.blocks = blocks;
	}
	
	public List<String> getBlocks() {
		return blocks;
	}

	public Boolean getLoadScenarioFromFile() {
		return loadScenarioFromFile;
	}


	@CreoleParameter(defaultValue="false")
	public void setLoadScenarioFromFile(Boolean loadScenarioFromFile) {
		this.loadScenarioFromFile = loadScenarioFromFile;
	}


	public String getLanguage() {
		return language;
	}


	@CreoleParameter(defaultValue="english") //czech
	public void setLanguage(String language) {
		this.language = language;
	}
		
	public static void main(String [] args) throws Exception
	{
		System.err.println("xx"+getPerlArchName()+"xx");
	}

	public static String getPerlArchName() throws InterruptedException, FileNotFoundException, URISyntaxException, IOException
	{
		ProcessExec tmt_proc = new ProcessExec();
		String[] args = {"perl", "-MConfig", "-e", "print $Config{archname}"};
		tmt_proc.exec(args);
		tmt_proc.startErrReaderThread();
		String ret = tmt_proc.readLine();
		tmt_proc.waitFor();		

		return ret;		
	}

	
	protected static String [] getTredEnvp() throws URISyntaxException, IOException, InterruptedException
	{
		Config cfg = Config.getConfig();
		String path_sep = System.getProperty( "path.separator" );

/*
 * ${TMT_ROOT%/}/share/installed_libs/lib/perl5
 * ${TMT_ROOT%/}/share/installed_libs/lib/perl5/$(perl -MConfig -e 'print $Config{archname}')
 * ${TMT_ROOT%/}/libs/core
 * ${TMT_ROOT%/}/libs/blocks
 * ${TMT_ROOT%/}/libs/other
 * ${TMT_ROOT%/}/treex/lib
 * ${TRED_DIR%/}/tredlib
 * ${TRED_DIR%/}/tredlib/libs/fslib
 * ${TRED_DIR%/}/tredlib/libs/pml-base
 * ${TRED_DIR%/}/tredlib/libs/backends
 * $PERL5LIB"  				
 */
		String perllib = 				
			cfg.getTmtRoot()+"/share/installed_libs/lib/perl5" 	+path_sep+
			cfg.getTmtRoot()+"/share/installed_libs/lib/perl5/"+ getPerlArchName() +path_sep+
			cfg.getTmtRoot()+"/libs/core" 	+path_sep+
			cfg.getTmtRoot()+"/libs/blocks" +path_sep+
			cfg.getTmtRoot()+"/treex/lib" +path_sep+
			cfg.getTredRoot()+"/tredlib" +path_sep+
			cfg.getTredRoot()+"/tredlib/libs/fslib" +path_sep+
			cfg.getTredRoot()+"/tredlib/libs/pml-base" +path_sep+
			cfg.getTredRoot()+"/tredlib/libs/backends" +path_sep+
			cfg.getTmtRoot()+"/libs/other"; 
		
		String tredEnvp [] =
		{				
				"PERLLIB="+perllib,
				"PERL5LIB="+perllib,
				"TRED_DIR="+cfg.getTredRoot(),
				"TMT_ROOT="+cfg.getTmtRoot()+"/",
				"JAVA_HOME="+System.getProperty("java.home"),
				"SystemRoot="+System.getenv("SystemRoot"),
				"Path="+System.getenv("Path"),
	/*		    'conll_mcd_order2.model'      => '2600m',    # tested on sol1, sol2 (64bit)
			    'conll_mcd_order2_0.01.model' => '540m',     # tested on sol2 (64bit) , cygwin (32bit win), java-1.6.0(64bit)
			    'conll_mcd_order2_0.03.model' => '540m',     # load block tested on cygwin notebook (32bit win), java-1.6.0(64bit)
			    'conll_mcd_order2_0.1.model'  => '540m',     # load block tested on cygwin notebook (32bit win), java-1.6.0(64bit)
				"TMT_PARAM_MCD_EN_MODEL=conll_mcd_order2_0.01.model", */
				"TMT_SHARED="+cfg.getTmtRoot()+"/share",
				"CYGWIN=nodosfilewarning"
		};
		
		return tredEnvp;
	}

	protected String getBrunBlocksScriptFile() throws URISyntaxException, IOException
	{
		Config cfg = Config.getConfig();
		return cfg.getTmtRoot()+ "/tools/general/runblocks.btred";		
	}
	
	protected List<String> buildTredCmdArray(String[] additional_args) throws IOException, URISyntaxException
	{
		Config cfg = Config.getConfig();	
		String[] cmdarray = 
		{
				"perl",
				cfg.getTredRoot() + "/btred",
				"-q",
				"-c",
				cfg.getTmtRoot() + "/config/.tredrc",
				"-Z",
				cfg.getTmtRoot() + "/pml_schemas/" +System.getProperty( "path.separator" )
					+cfg.getTredRoot()+"/resources/"+System.getProperty( "path.separator" ),
				"-0",
				"-m",
				getBrunBlocksScriptFile(),
				"-S", "-o"
		};
		
		List<String> cmd_list = new ArrayList<String>(Arrays.asList(cmdarray));
		
		if (additional_args != null) cmd_list.addAll(Arrays.asList(additional_args));
		
		URL scen = getScenarioFilePath();
		if (getLoadScenarioFromFile())
		{
			cmd_list.add("--scen");			
			cmd_list.add(Utils.URLToFilePath(scen));			
		}
		else
		{
			List<String> blocks = getBlocks();
			cmd_list.addAll(blocks);
		}
				
		cmd_list.add("--");
		
		return cmd_list;
	}
	
	public static class CannotAnnotateDocumentException extends Exception
	{
		private static final long serialVersionUID = 3849507670402431902L;
		Document doc;
		String tmt_filepath;

		public CannotAnnotateDocumentException(Document doc, String tmt_filepath, Throwable cause)
		{
			super(
					String.format("Cannot annotate document '%s' according tmt file '%s'",
							doc.getName(), tmt_filepath),
					cause);
			this.doc = doc;
			this.tmt_filepath = tmt_filepath;
		}		
	}

	
	protected void annotateGateDocumentAcordingtoTMTfile(Document doc, String tmt_filepath) throws ParserConfigurationException, SAXException, IOException, InvalidOffsetException, CannotAnnotateDocumentException
	{
		try
		{
			TMTSAXParser parser = new TMTSAXParser(getLanguage());
			List<SentenceInfoManager> sentences = parser.parse(tmt_filepath);
			Annotator tmt_annot = new Annotator(sentences);
	    	tmt_annot.annotate(doc, outputASName);
    	}
		catch (Exception e)
		{
			throw new CannotAnnotateDocumentException(doc, tmt_filepath, e);
		}
	}
	


}
