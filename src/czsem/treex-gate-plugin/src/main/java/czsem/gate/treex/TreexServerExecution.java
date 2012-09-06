package czsem.gate.treex;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import czsem.gate.utils.Config;
import czsem.utils.ProcessExec;

public class TreexServerExecution {

	private int portNumber = 9090;

	public void start() throws IOException, URISyntaxException {
		String path_sep = System.getProperty( "path.separator" );
		
		Config cfg = Config.getConfig();

		
		String[] cmdarray = {"perl", cfg.getCzsemResourcesDir()+"/Treex/treex_online.pl"};
		String[] env = {
				"PERL5LIB="+cfg.getCzsemResourcesDir()+"/Treex" + path_sep +
				cfg.getTreexDir() + "/lib" + path_sep +
				cfg.getTreexDir() + "/oldlib",
				"SystemRoot="+System.getenv("SystemRoot"),
				"Path="+System.getenv("Path"),
				"TMT_ROOT="+cfg.getTmtRoot(),
				"JAVA_HOME="+System.getProperty("java.home"),};
//				Map<String, String> env2 = System.getenv();
		
		ProcessExec tmt_proc = new ProcessExec();
//		String[] env3 = getTredEnvp();
		tmt_proc.exec(cmdarray, env, new File(cfg.getTreexDir()));
		tmt_proc.startStdoutReaderThreads();		
	}

	public TreexServerConnection getConnection() throws MalformedURLException {
		return new TreexServerConnection("localhost", portNumber);
	}

}
