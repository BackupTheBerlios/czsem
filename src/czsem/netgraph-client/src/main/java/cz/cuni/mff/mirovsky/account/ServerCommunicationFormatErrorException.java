/*
 * ServerCommunicationFormatErrorException.java
 *
 * Created on 5. červen 2002, 15:11
 */

package cz.cuni.mff.mirovsky.account;

/**
 * An exception representing an error in the format in the communication with the server (used with the authentication of the users)
 */
public class ServerCommunicationFormatErrorException extends java.lang.Exception {

    /**
     * Creates new <code>ServerCommunicationFormatErrorException</code> without detail message.
     */
    public ServerCommunicationFormatErrorException() {
    }


    /**
     * Constructs an <code>ServerCommunicationFormatErrorException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ServerCommunicationFormatErrorException(String msg) {
        super(msg);
    }
}


