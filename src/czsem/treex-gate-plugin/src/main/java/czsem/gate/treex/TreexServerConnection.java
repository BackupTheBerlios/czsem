package czsem.gate.treex;

import gate.creole.ResourceInstantiationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class TreexServerConnection {
	static Logger logger = Logger.getLogger(TreexServerConnection.class); 

	XmlRpcClient rpcClient;
	
	public TreexServerConnection(String hostname, int portNumber) throws MalformedURLException, ResourceInstantiationException {
		this(new URL("http", hostname, portNumber, "/RPC2"));
	}

	public TreexServerConnection(URL treexServerUrl) throws ResourceInstantiationException {
		try {
			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
			config.setServerURL(treexServerUrl);
			
			rpcClient = new XmlRpcClient();
			rpcClient.setConfig(config);
		} 
		catch (IncompatibleClassChangeError e) {
			throw new ResourceInstantiationException( String.format(
					"Filed to start Treex server, due to IncompatibleClassChangeError (%s), " +
					"this is usually caused by the presence of a different version of XML-RPC library, " +
					"e.g. if tecto-mt-gate-plugin is loaded in the same time...", e.toString()));						
		}
	}

	public void terminateServer() {
		String handshake = "";
		try {
			handshake = handshake();
			rpcClient.execute("treex.terminate", new Vector<Object>());
		} catch (XmlRpcException e) {
			logger.info(String.format("Treex server termination registered. url: %s handshake code: '%s'", 
					((XmlRpcClientConfigImpl)rpcClient.getClientConfig()).getServerURL(),
					handshake));
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

	public Object analyzePreprocessedDoc(String docText, Map<String, Object>[] inputDocData) throws XmlRpcException {
		Vector<Object> params = new Vector<Object>(2);
		params.add(docText);
		params.add(inputDocData);
		Object ret = rpcClient.execute("treex.analyzePreprocessedDoc", params);

		return ret;		
	}

}
