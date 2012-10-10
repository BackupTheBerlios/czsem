package czsem.gate.treex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class TreexServerConnection {
	static Logger logger = Logger.getLogger(TreexServerConnection.class); 

	XmlRpcClient rpcClient;
	
	public TreexServerConnection(String hostname, int portNumber) throws MalformedURLException {
		this(new URL("http", hostname, portNumber, "/RPC2"));
	}

	public TreexServerConnection(URL treexServerUrl) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(treexServerUrl);
		
		rpcClient = new XmlRpcClient();
		rpcClient.setConfig(config);
	}

	public void terminateServer() {
		try {
			rpcClient.execute("treex.terminate", new Vector<Object>());
		} catch (XmlRpcException e) {
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

	public String handshake() throws XmlRpcException {
		Object ret = rpcClient.execute("treex.handshake", new Object[0]);
		return (String) ret;
	}

}
