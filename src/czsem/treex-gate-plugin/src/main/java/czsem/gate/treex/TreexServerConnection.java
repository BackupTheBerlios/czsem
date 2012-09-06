package czsem.gate.treex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcClientLite;
import org.apache.xmlrpc.XmlRpcException;

public class TreexServerConnection {
	static Logger logger = Logger.getLogger(TreexServerConnection.class); 

	XmlRpcClientLite rpcClient;
	
	public TreexServerConnection(String hostname, int portNumber) throws MalformedURLException {
		rpcClient = new XmlRpcClientLite("localhost", 9090);
	}

	public TreexServerConnection(URL treexServerUrl) {
		rpcClient = new XmlRpcClientLite(treexServerUrl);
	}

	public void terminateServerSafe() {
		try {
			terminateServer();
		} catch (XmlRpcException e) {
			logger.error("Treex server termination problem.", e);
		}
	}

	public void terminateServer() throws XmlRpcException {
		try {
			rpcClient.execute("treex.terminate", new Vector<Object>());
		} catch (IOException e)
		{
			logger.info("Treex server termination rigistered.");
		}		
	}

	public Object encodeTreexFile(String treexFileName) throws XmlRpcException, IOException {
		Vector<String> params = new Vector<String>(1);
		params.add(treexFileName);
		Object ret = rpcClient.execute("treex.encodeDoc", params);

		return ret;
	}

	public void initScenario(String languageCode, String ... blocks) throws XmlRpcException, IOException {
		Vector<String> blockList = new Vector<String>(Arrays.asList(blocks));
		
		Vector<Object> params = new Vector<Object>(1);
		params.add(languageCode);
		params.add(blockList);
		rpcClient.execute("treex.initScenario", params);		
	}

	public Object analyzeText(String text) throws XmlRpcException, IOException {
		Vector<String> params = new Vector<String>(1);
		params.add(text);
		Object ret = rpcClient.execute("treex.analyzeText", params);

		return ret;
	}

}
