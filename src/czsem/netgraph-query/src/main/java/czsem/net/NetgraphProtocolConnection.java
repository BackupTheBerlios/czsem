package czsem.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;

public class NetgraphProtocolConnection {

	public static final class SepartorCahrs
	{
		/** end of the message */
		public final static byte EOM = 0;
		/** end of line - string terminator */
		public final static byte EOL = 13;
		/** OK message reply code */
		public final static byte OK = 'O';
		/** Field separator */
		public final static byte FS = (byte)'\t';
		/** New line separator */
		public final static byte NL = (byte) '\n';
		/** "1.91 (12.11.2007)" */
	}
		
	public static class NetgraphProtocolException extends Exception
	{
		
		NetgraphProtocolException(char error_msg, int error_code)
		{
			super("error msg: " + error_msg +" error code: " + error_code);
		}
	
		public NetgraphProtocolException(String string) {
			super(string);
		}
	
		private static final long serialVersionUID = 1L;
		
	}


	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;

	/** maximum length of a Netgraph message. */
	public final static int maxlenmes = 250000;
	private byte message[] = new byte[maxlenmes];

	
	public String getInetAddress()
	{
		return socket.getInetAddress().toString();		
	}

	public int getPort()
	{
		return socket.getPort();		
	}
	
	protected void open(String server_name, int port) throws UnknownHostException, IOException
	{
		socket = new Socket(server_name, port);
		
		in = new BufferedInputStream(socket.getInputStream());
		out =  new BufferedOutputStream(socket.getOutputStream());		
	}
	
	
	
	protected void close() throws IOException, NetgraphProtocolException
	{
		if (socket == null) return;		

		socket.close();
		socket = null;
	}
	
	protected void skip(int numBytesToSkip) throws IOException
	{		
		if (in.read(message, 0, numBytesToSkip) != numBytesToSkip)
			new NetgraphProtocolException("Unexpected end of data");
	}

	protected byte [] waitForMessageEnd() throws NetgraphProtocolException, IOException
	{		
		int red = 0;
		int ret = 0;
		
		do {
			ret = in.read(message, red, maxlenmes-red);
			
			if (ret == -1) throw new NetgraphProtocolException("Unexpected end of connection");
			
			red += ret;
		}
		while (ret == 0 || message[red-1] != SepartorCahrs.EOM);
						
		return message;
	}
	

	protected void assertReplyOK() throws IOException, NetgraphProtocolException
	{
		int ret = in.read();
		if (ret != SepartorCahrs.OK) throw new NetgraphProtocolException((char) ret, in.read());		
	}
	
	protected void assertGetChar(char ch) throws IOException, NetgraphProtocolException
	{
		if (in.read() != ch) throw new NetgraphProtocolException("expected char: " + ch);				
	}
	
	
	protected char getChar() throws IOException
	{
		return (char) in.read();				
	}

	protected byte getByte() throws IOException
	{
		return (byte) in.read();
	}
	
	protected int getInt() throws NetgraphProtocolException, IOException
	{		
		String s = getString(SepartorCahrs.EOL);
		return Integer.parseInt(s);
	}

	protected String getString(byte separator) throws NetgraphProtocolException, IOException
	{
		int i=0;
		
		for(;;)
		{
			byte b = getByte();
			if (b == -1) new NetgraphProtocolException("Unexpected end of connection");
			if (b == separator) break;
			message[i++] = b;
		}
		
		return new String(message, 0, i, "UTF-8");		
	}

	protected void putChar(char ch) throws IOException
	{
		out.write(ch);		
	}

	
	protected void putString(String s) throws IOException
	{
		out.write(s.getBytes("UTF-8"));		
	}

	/**
	 * Writes the string and EOL character to the output. 
	 * @param s
	 * @throws IOException
	 */
	protected void putStringLn(String s) throws IOException
	{
		putString(s);
		out.write(SepartorCahrs.EOL);
	}
	
	/**
	 * write(EOM), flush()
	 * @throws IOException
	 */
	protected void sendMessage() throws IOException
	{
		out.write(SepartorCahrs.EOM);
		out.flush();
	}

	
    public static String encryptPassword(String passwd) {
        try {
			MessageDigest encrypt_alg = MessageDigest.getInstance("MD5", "SUN");
			
			byte digest[] = encrypt_alg.digest(passwd.getBytes());
			
			return byteArrayToHexString(digest);
			
		} catch (Exception e) {}

        return null;
    }
    
    public static String byteArrayToHexString(byte[] b){
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++){
          int v = b[i] & 0xff;
          if (v < 16) {
            sb.append('0');
          }
          sb.append(Integer.toHexString(v));
        }
        return sb.toString();//.toUpperCase();
    }
}
