
/*
 * ServerNetCommunicationException.java
 *
 * Created on 21. november 2002
 */

/**
 * An almost empty class representing an exception in communication with the server.
 */
public class ServerNetCommunicationException extends java.lang.Exception {

    /**
     * Creates new <code>ServerNetCommunicationException</code> without a detail message.
     */
    public ServerNetCommunicationException() {
    }


    /**
     * Constructs a <code>ServerNetCommunicationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ServerNetCommunicationException(String msg) {
        super(msg);
    }
}

