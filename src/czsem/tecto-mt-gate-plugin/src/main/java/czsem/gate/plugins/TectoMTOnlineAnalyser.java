package czsem.gate.plugins;

import gate.Document;
import gate.Resource;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.AsyncCallback;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.apache.xmlrpc.XmlRpcException;

import czsem.Utils;
import czsem.gate.tectomt.TMTDocumentHelper;
import czsem.gate.utils.Config;
import czsem.utils.FirstOfTwoTasksKillsTheSecond.HandShakeResult;
import czsem.utils.ProcessExec;
import czsem.utils.FirstOfTwoTasksKillsTheSecond;
import czsem.utils.FirstOfTwoTasksKillsTheSecond.Task;

@CreoleResource(name = "czsem TectoMTOnlineAnalyser", comment = "Alyses givem corpus by TMT tools")
public class TectoMTOnlineAnalyser extends TectoMTAbstractAnalyser
{
	private static final long serialVersionUID = 2036059373106358909L;

	static Logger logger = Logger.getLogger(TectoMTOnlineAnalyser.class);
	protected ProcessExec tmtProcess = null;
	
	protected int serverPortNumber = 9090;
	private int serverTimeOut = 0;
	
	protected TectMTServerConnection serverConnection = null;
	
	@Override
	protected String getBrunBlocksScriptFile() throws URISyntaxException, IOException
	{
		Config cfg = Config.getConfig();
//		return cfg.getTmtRoot()+ "/tools/general/runblocks.btred";
		return cfg.getCzsemResourcesDir() + "/TectoMT/online_runblocks.btred";

	}

	public boolean isServerRunning()
	{
		if (tmtProcess == null) return false;
		
		return tmtProcess.isRunning();
	}
	
	
	protected void startTMTAnalysisServer() throws Exception
	{				 
		if (! Utils.portAvailable(getServerPortNumber()))
		{
			throw new ResourceInstantiationException("Filed to start TectoMT server, port nuber: "+getServerPortNumber()+" is not available.");						
		}
		
		String handshake_code = Long.toHexString(new Random().nextLong());

		Calendar cal_start = Calendar.getInstance();
		String [] additional_args = {getServerPortNumber().toString(), handshake_code};
		
		List<String> cmd_list = buildTredCmdArray(additional_args);			
		
		tmtProcess = new ProcessExec();
		tmtProcess.exec(cmd_list.toArray(new String[0]), getTredEnvp());
		tmtProcess.startReaderThreads(Config.getConfig().getLogFileDirectoryPathExisting() + "/TMT_GATE_");
		
		for (int i = 0; i < 30; i++)
		{
			if (isServerRunning())
			{
				logger.debug(String.format("waited %d ms", i*100));
				break;				
			}
			Thread.sleep(100);
		}
		if (! isServerRunning())
		{
			throw new ResourceInstantiationException("Filed to start TectoMT server, see 'czsem_GATE_plugins/log/TMT_GATE_err.log'.");			
		}		

		serverConnection = new TectMTServerConnection(getServerPortNumber(), handshake_code);
		
		
		doHandShake();
		
		long time_dif = Calendar.getInstance().getTime().getTime() - cal_start.getTime().getTime() ;        		
		logger.info(String.format("TectoMT server initialization took %d:%d.%d",
				time_dif/(1000*60), (time_dif/(1000))%1000, time_dif%1000));

	}

		
	protected void doHandShake() throws Exception
	{
		
		Task<HandShakeResult> taskHandShake = new Task<HandShakeResult>() {
			@Override
			public HandShakeResult run() throws InterruptedException, XmlRpcException, IOException {
				return 
					serverConnection.handShake()
						? HandShakeResult.HandShakeOK
						: HandShakeResult.HandShakeKO; 
			}
		};
		Task<HandShakeResult> taskWaitProc = new Task<HandShakeResult>() {
			@Override
			public HandShakeResult run() throws InterruptedException, XmlRpcException, IOException {
				tmtProcess.waitFor();
				return HandShakeResult.ProcessTrminated;
			}
		};
		
		FirstOfTwoTasksKillsTheSecond<HandShakeResult> tt
			= new FirstOfTwoTasksKillsTheSecond<HandShakeResult>(
					taskHandShake, taskWaitProc);  

		HandShakeResult result = tt.executeWithTimeout(1000 * getServerTimeOut());

		if (result == null) result = HandShakeResult.TimeOut;
		
		if (result != HandShakeResult.HandShakeOK)			
		{
			if (isServerRunning()) tmtProcess.destroy();
			switch (result) {
			case HandShakeKO:
				throw new ResourceInstantiationException("Handshake with TectoMT server failed (Another server already running on the same port?), see also 'czsem_GATE_plugins/log/TMT_GATE_err.log'.");			
			case ProcessTrminated:
				throw new ResourceInstantiationException("Error during run of TectoMT server, see 'czsem_GATE_plugins/log/TMT_GATE_err.log'.");			
			case TimeOut:
				throw new ResourceInstantiationException("TectoMT server run out of time dutring start up, see 'czsem_GATE_plugins/log/TMT_GATE_err.log'.");			
			}		
			
		}

	}



	public static class TectMTServerConnection
	{
		private XmlRpcClientLite client = null;
		private String handshake_code = Long.toHexString(new Random().nextLong());

		
		public TectMTServerConnection(int port, String handshake_code) throws MalformedURLException
		{
			this.handshake_code = handshake_code;
			client = new XmlRpcClientLite("localhost", port);
			client.setMaxThreads(1);
		}
		
		public boolean handShake() throws XmlRpcException, IOException, InterruptedException
		{
			String hash = Integer.toString(this.hashCode());
			String res = (String) executeMethodAsyncWithTimeOut(0, "tectoMT.handshake", hash);
//			String res = (String) executeMethod("tectoMT.handshake", hash);
//			System.err.println(hash);
//			System.err.println(res);
			return res != null && res.equals("ok"+hash+"ok_"+handshake_code);
		}
		
		
		protected static class MyAsyncCallback implements AsyncCallback
		{
			private Boolean done = false;
			private Object ret = null;
			
			@Override
			public synchronized void handleResult(Object result, URL url, String method) {
					ret = result;
					done = true;
//					System.err.println("nb");
					notify();
//					System.err.println("ne");
			}
			@Override
			public synchronized void handleError(Exception exception, URL url, String method) {
//					System.err.println("err");
					exception.printStackTrace();
					done = true;										
					notify();
			}
			/**
			 * @param timeout if == 0 then waits without any timeout
			 */
			public synchronized Object waitForRet(int timeout) throws InterruptedException
			{
//					System.err.println("waits");						
					if (! done)
					{
						if (timeout == 0) wait();
						else wait(timeout);
					}
//					System.err.println("waite");
					return ret;
			}			
		}
		
		
		/**
		 * @param timeout if == 0 then waits without any timeout
		 */
		protected Object executeMethodAsyncWithTimeOut(int timeout, String methodName, Object ... args) throws InterruptedException
		{
			Vector<Object> params = new Vector<Object>(args.length);
			params.addAll(Arrays.asList(args));
			
			MyAsyncCallback cb = new MyAsyncCallback();
			client.executeAsync(methodName, params, cb);				
			
			return cb.waitForRet(timeout);			
		}

		protected Object executeMethod(String methodName, Object ... args) throws XmlRpcException, IOException
		{
			Vector<Object> params = new Vector<Object>(args.length);
			params.addAll(Arrays.asList(args));
			
			return client.execute(methodName, params);			
		}

		public void analyseFile(String file_path) throws XmlRpcException, IOException
		{
	        String result = (String) executeMethod("tectoMT.analyzeFile", file_path);
	        assert (result.equals("succes"));			
		}

		public void terminate()
		{
			try {
				executeMethod("tectoMT.terminate");
			} catch (Exception e)
			{
				Logger.getLogger(getClass()).info("TectoMT server terminated");
			}
		}
		
	}
	
	public static List<String> getTempBlocks() {
		String [] blocks =
		{
				"SEnglishW_to_SEnglishM::Sentence_segmentation",
				"SEnglishW_to_SEnglishM::Tokenization"
		};
		
		return Arrays.asList(blocks);
	}


	public Integer getServerPortNumber() {
		return serverPortNumber;
	}

	@CreoleParameter(defaultValue="9090")
	public void setServerPortNumber(Integer serverPortNumber) {
		this.serverPortNumber = serverPortNumber;
	}

	@Override
	public void cleanup() {
		serverConnection.terminate();
		super.cleanup();
	}
	
	protected void analyseFile(String file_path) throws XmlRpcException, IOException
	{
		serverConnection.analyseFile(file_path);		
	}


	@Override
	public Resource init() throws ResourceInstantiationException
	{
		try {
			startTMTAnalysisServer();
		} catch (Exception e) {
			throw new ResourceInstantiationException(e);
		}
		return super.init();
	}
	
	public static void main(String [] rgs) throws Exception
	{
		System.err.println("start");
		TectoMTOnlineAnalyser ta = new TectoMTOnlineAnalyser();
		
		ta.setLoadScenarioFromFile(false);
		ta.setBlocks(getTempBlocks());

		ta.startTMTAnalysisServer();
		
//		TectMTServerConnection con = new TectMTServerConnection(8080);
		
		ta.analyseFile("czsem_GATE_plugins/TmT_serializations/1032.xml_0001A.tmt~");
		ta.analyseFile("czsem_GATE_plugins/TmT_serializations/10784.xml_00025.tmt~");
		ta.analyseFile("czsem_GATE_plugins/TmT_serializations/9809.xml_00262.tmt");
		ta.cleanup();

		
/*		ta.startTMTAnalysisServer();
		
		XmlRpcClientLite client = new XmlRpcClientLite("localhost", 8080);
//		client.execute("dedek_online.Name", new Vector<String>(0));
		

		
		Vector<String> params = new Vector<String>(1);
        params.addElement("czsem_GATE_plugins/TmT_serializations/9809.xml_00262.tmt");
        String result = (String) client.execute("tectoMT.analyzeFile", params);
        System.out.println("Result: " + result);

        params = new Vector<String>(1);
        params.addElement("czsem_GATE_plugins/TmT_serializations/10025.xml_00014.tmt");
        result = (String) client.execute("tectoMT.analyzeFile", params);
        System.out.println("Result: " + result);

		
		try {
			client.execute("tectoMT.terminate", new Vector<String>(0));
		} catch (IOException e) {System.err.println("server terminated");}
*/
        System.err.println("end");
		
		
	}

	@CreoleParameter(defaultValue="0", comment="in seconds, 0 means no timeout is used - wait infinitely.")
	public void setServerTimeOut(Integer serverTimeOut) {
		this.serverTimeOut = serverTimeOut;
	}

	public Integer getServerTimeOut() {
		return serverTimeOut;
	}

	@Override
	public void execute() throws ExecutionException
	{
		Document doc = getDocument();
		try {
			TMTDocumentHelper dh = new TMTDocumentHelper(doc, getInputASName(), getLanguage(), 
					Config.getConfig().getTmtSerializationDirectoryURL());
			analyseFile(dh.getTMTFilePath());
			annotateGateDocumentAcordingtoTMTfile(doc, dh.getTMTFilePath());
		} catch (Exception e)
		{
			throw new ExecutionException(e);
		}
	}

}


/*


sub run2
{
    my $methods;
    $methods = {
      'tectoMT.analyzeFile' => \&analyzeFile,
      'tectoMT.terminate' => \&terminate };
    my $server;
    $server = Frontier::Daemon->new(LocalPort => 8080, methods => $methods)
        or die "Couldn't start HTTP server: $!";
}

 * */
