package czsem.gate.treex;

import gate.creole.ResourceInstantiationException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import org.apache.xmlrpc.XmlRpcException;

import czsem.Utils;
import czsem.gate.utils.Config;
import czsem.utils.EnvMapHelper;
import czsem.utils.FirstOfTwoTasksKillsTheSecond;
import czsem.utils.FirstOfTwoTasksKillsTheSecond.HandShakeResult;
import czsem.utils.FirstOfTwoTasksKillsTheSecond.Task;
import czsem.utils.ProcessExec;

public class TreexServerExecution {
	
	protected static class TreexHandShake
	{

		protected ProcessExec process;
		protected TreexServerConnection connection;
		protected String handshake_code;
		protected String handshake_return = null;

		public TreexHandShake(ProcessExec process, TreexServerConnection connection, String handshake_code) {
			this.process = process;
			this.connection = connection;
			this.handshake_code = handshake_code;
		}
		
		protected boolean doServerHandShake() throws InterruptedException {
			for (long sleep = 1 ;; sleep *= 2) {
				try {
					
					handshake_return = connection.handshake();
//					System.err.println(sleep);
					return handshake_code.equals(handshake_return);
					
				} catch (XmlRpcException e) {
					
					Thread.sleep(sleep);
					continue;
					
				}
			}			
		}

		public void doHandShake() throws Exception {
			Task<HandShakeResult> taskHandShake = new Task<HandShakeResult>() {
				@Override
				public HandShakeResult run() throws InterruptedException, XmlRpcException, IOException {
					return 
						doServerHandShake()
							? HandShakeResult.HandShakeOK
							: HandShakeResult.HandShakeKO; 
				}
			};
			Task<HandShakeResult> taskWaitProc = new Task<HandShakeResult>() {
				@Override
				public HandShakeResult run() throws InterruptedException, XmlRpcException, IOException {
					process.waitFor();
					return HandShakeResult.ProcessTrminated;
				}
			};
			
			FirstOfTwoTasksKillsTheSecond<HandShakeResult> tt
				= new FirstOfTwoTasksKillsTheSecond<HandShakeResult>(
						taskHandShake, taskWaitProc);  

			HandShakeResult result = tt.executeWithTimeout(0);

			if (result == null) result = HandShakeResult.TimeOut;
			
			String logPath = Config.getConfig().getLogFileDirectoryPath() + "/TREEX_err.log'.";
			
			if (result != HandShakeResult.HandShakeOK)			
			{
				if (process.isRunning()) process.destroy();
				
				switch (result) {
				case HandShakeKO:
					throw new ResourceInstantiationException(
							String.format("Handshake with Treex server failed!\n" +
									"Another server already running on the same port?\n" +
									"Expected hash: '%s'\n" +
									"Returned hash: '%s'\n" +
									"See also '"+logPath, handshake_code, handshake_return));			
				case ProcessTrminated:
					throw new ResourceInstantiationException("Error during run of Treex server, see '"+logPath);			
				case TimeOut:
					throw new ResourceInstantiationException("Treex server run out of time dutring start up, see '"+logPath);			
				}		
				
			}
			
		}
	}

	private int portNumber = 9090;

	public void start() throws Exception {
		if (! Utils.portAvailable(getPortNumber()))
		{
			throw new ResourceInstantiationException("Filed to start Treex server, port nuber: "+getPortNumber()+" is not available.");						
		}
		
		String path_sep = System.getProperty( "path.separator" );
		
		Config cfg = Config.getConfig();
		
		String handshake_code = Long.toHexString(new Random().nextLong());

		String[] cmdarray = {
				"perl", 
				cfg.getCzsemResourcesDir()+"/Treex/treex_online.pl",
				Integer.toString(getPortNumber()),
				handshake_code};
/*		
		String[] env = {
				"PERL5LIB="+cfg.getCzsemResourcesDir()+"/Treex" + path_sep +
				cfg.getTreexDir() + "/lib" + path_sep +
				cfg.getTreexDir() + "/oldlib",
				"SystemRoot="+System.getenv("SystemRoot"),
				"Path="+System.getenv("Path"),
				"TMT_ROOT="+cfg.getTmtRoot(),
				"JAVA_HOME="+System.getProperty("java.home"),};
*/				
//				Map<String, String> env2 = System.getenv();
		
//		String[] env3 = getTredEnvp();
		
		
		ProcessBuilder pb = new ProcessBuilder(cmdarray);
		pb.directory(new File(cfg.getTreexDir()));
		EnvMapHelper eh = new EnvMapHelper(pb.environment());
		eh.append("PERL5LIB", path_sep + cfg.getCzsemResourcesDir()+"/Treex"); 
		eh.append("PERL5LIB", path_sep + cfg.getTreexDir() + "/lib"); 
		eh.append("PERL5LIB", path_sep + cfg.getTreexDir() + "/oldlib");
		eh.setIfEmpty("TMT_ROOT", cfg.getTmtRoot());
		eh.setIfEmpty("JAVA_HOME", System.getProperty("java.home"));

		ProcessExec tmt_proc = new ProcessExec();
		//tmt_proc.exec(cmdarray, env, new File(cfg.getTreexDir()));
		tmt_proc.execWithProcessBuilder(pb);
		
		//tmt_proc.startStdoutReaderThreads();
		tmt_proc.startReaderThreads(Config.getConfig().getLogFileDirectoryPathExisting() + "/TREEX_");

		
		
		TreexHandShake th = new TreexHandShake(tmt_proc, getConnection(), handshake_code);
		
		th.doHandShake();
		
	}

	public TreexServerConnection getConnection() {
		try {
			return new TreexServerConnection("localhost", getPortNumber());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public int getPortNumber() {
		return portNumber;
	}

}
