
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;

import cz.cuni.mff.mirovsky.*;

/**
 * Class ServerNetCommunication sends and receives messages to/from the server via the internet.
 *  It is a low-level implementation of the communication.
 */
public class ServerNetCommunication {

    private Socket s;  // objekty schranek
    private InputStream is;
    private BufferedInputStream bis;
    private OutputStream os;
    private BufferedOutputStream bos;
    private boolean connected; // signalizuje, zda je vytvořeno spojení se serverem

    private int error; // typ chyby při připojování k serveru nebo při přihlašování uživatele
    private String error_message; // zprava k dane chybe

    private ShowMessagesAble mess; // objekt, který se bude starat o zobrazování mých hlášek
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám

    public ServerNetCommunication(ShowMessagesAble p_mess, ResourceBundle p_i18n) {
        mess = p_mess;
        i18n = p_i18n;
        connected = false;
    }

    //public void setMessagesShower (ShowMessagesAble p_mess) { // nastaví objekt, který bude vypisovat hlášky
    //    mess = p_mess;
    //}

    private void debug (String message) { // vypíše hlášku pomocí externího objektu, pOKud je nastaven
        if (mess != null) {
            mess.debug(message);
        }
        else System.out.print(message);
    }

    private void inform (String message) { // vypíše hlášku pomocí externího objektu, pOKud je nastaven
        if (mess != null) {
            mess.inform (message);
        }
    }

    /**
     * It creates a connection with the server - a socket and two streams
     */
    public int connect(String server_name, int server_port) {
        // vytvoření spojení
        error = 0;
        disconnect(); // zruším případné dřívější spojení

        try {
            s = new Socket (server_name, server_port);
            //debug ("\nServerNetCommunication.initialization(): Vytvářím spojení se serverem.");
        }
        catch (Exception e) {
            debug ("\nServerNetCommunication.initialization(): Chyba při vytváření spojení se serverem!");
            error = 2;
            error_message = i18n.getString("CONNECT_ERROR_MESSAGE_SERVER_UNREACHABLE");
            return error;
        }

        // vytvoření input proudu
        try {
            is = s.getInputStream();
        }
        catch (Exception e) {
            debug ("\nServerNetCommunication.initialization(): Chyba při vytváření vstupního proudu!");
            error = 3;
            error_message = i18n.getString("CONNECT_ERROR_MESSAGE_SERVER_UNREACHABLE");
            disconnect(); // zavřu, co už se otevřelo
            return error;
        }

        // vytvoření output proudu
        try {
            os = s.getOutputStream();
        }
        catch( Exception e ) {
            debug ("\nServerNetCommunication.initialization(): Chyba při vytváření výstupního proudu!");
            error = 4;
            error_message = i18n.getString("CONNECT_ERROR_MESSAGE_SERVER_UNREACHABLE");
            disconnect(); // zavřu, co už se otevřelo
            return error;
        }

        bis = new BufferedInputStream(is);
        bos = new BufferedOutputStream(os);
        connected = true;

        return 0;
    } // connect

    public void disconnect() { // uzavre komunikacni objekt Socket; volana interpretem z objektu NGClient
        try {
            bis.close(); // zavřu vstupní proud
            is.close();
            bis.close(); // zavřu výstupní proud
            os.close();
            s.close(); // zavřu socket
        } catch (Exception e) {
            // System.out.println( "ServerNetCommunication.disconnect(): An error occured during closing the socket!");
            // debug ( "\nServerNetCommunication.disconnect(): An error occured during closing the socket!");
        }
        connected = false;
    } // disconnect

    public boolean isConnected() {
        return connected;
    }

    public int getError() { // vrátí typ chyby, ke které naposledy došlo
        return error;
    }

    public String getErrorMessage() { // vrátí popis chyby, ke které naposledy došlo
        return error_message;
    }

    public void send(byte buffer[], int delka) throws ServerNetCommunicationException { // zapise do vystupniho proudu schranky data z pole 'buffer' o delce 'delka'
        // volana programem, kdykoliv je treba poslat zpravu serveru
        // 'buffer' - vstup - pole se zpravou urcenou k zapisu
        // 'delka' - vstup - delka zpravy
        try {
            bos.write (buffer, 0, delka);
            bos.flush();
        }
        catch (Exception e) {
            //System.out.println("ServerNetCommunication.send: An error occured during sending data to the server!");
            //debug ("\nServerNetCommunication.send: An error occured during sending data to the server!");
            throw new ServerNetCommunicationException();
        }
    } // sendData

    public int receive(byte buffer[], byte EOM) throws ServerNetCommunicationException { // cte data ze vstupniho proudu schranky a uklada je do pole 'buffer'; cte az do hodnoty 'EOM' vcetne
        // volana programem, kdykoliv je treba cist data prichozi od serveru
        // 'buffer' - vystup - pole, do nehoz se bude cist
        // vraci pocet prectenych bajtu
        int index = -1;
        try {
            do {
                index++;
                bis.read (buffer, index, 1);
            } while (buffer[index] != EOM);
        }
        catch (Exception e) {
            //System.out.println("ServerNetCommunication.receive: An error occured during recieving data from the server!");
            //debug ("\nServerNetCommunication.receive: An error occured during recieving data from the server!");
            throw new ServerNetCommunicationException();
        }
        return index+1;
    } // receiveData

} // ServerNetCommunication