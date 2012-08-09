import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.account.*;
import cz.cuni.mff.mirovsky.trees.*;

/**
 * Title: ServerCommunication - communication with server
 * Description: it is a front-end for sending and receiving messages to/from the server
 * Copyright: Copyright (c) 2002
 * Company: Charles University in Prague, CKL
 * @author Jiří Mírovský
 * @version 1.0
 */

/**
 * Class ServerCommunication provides the interface for communication with the server. All types of messages that are
 * understood by the server are created here. It uses class ServerNetCommunication for the actual sending of the messages.
 * Other classes can only communicate with the server through this class.
 */
public class ServerCommunication implements ActionListener {

	public final static int maxlenmes = 250000;  // max. delka zpravy
	public byte zprava[];  // vlastni zprava
	public final static byte EOM = 0;  // ukoncovaci znak zpravy; pozor! kopie v NGTree
	public final static byte EOL = 13;  // ukoncovaci znak radku ve zprave; pozor! kopie v NGTree a v PropertiesLoader
	public final static byte OK = 'O'; // odpověď v pořádku
	public static byte oddelovac = (byte)'\t'; // tento znak bude užit pro oddělování položek při posílání seznamu souborů
    // až do verze 1.85 to byla mezera, což dělalo problém u souborů obsahujících mezeru v názvu...
    public final static byte GET_TREE_SUBTYPE_CONTEXT = (byte)'c'; // typ žádosti o další/předchozí strom: další/předchozí kontext
        public final static byte GET_TREE_SUBTYPE_OCCURENCE = (byte)'o'; // typ žádosti o další/předchozí strom: hned další/předchozí výskyt
        public final static byte GET_TREE_SUBTYPE_TREE = (byte)'t'; // typ žádosti o další/předchozí strom: první další/předchozí výskyt ve stromě
        public final static byte GET_TREE_SUBTYPE_FIRST = (byte)'f'; // typ žádosti o první výskyt v prvním stromě

        private final static int TIMER_LOAD_TREE_DELAY_INITIAL = 600; // tolik ms před opakovanou žádostí o zaslání stromu; výchozí hodnota
        private final static int TIMER_LOAD_TREE_DELAY_CHANGE = 200; // o tolik se bude interval prodlužovat

	private int error; // typ chyby při připojování k serveru nebo při přihlašování uživatele
    private String error_message; // zprava k dane chybe

    // chyby při připojování k serveru
	public final static int error_ok = 0; // typ chyby - žádná chyba, vše OK
	public final static int error_max_clients = 1; // typ chyby - maximum povolených klientů k serveru bylo již dosaženo
	public final static int error_login_failed = 2; // typ chyby - nezdařilo se přihlášení uživatele
    // chyby při přihlašování uživatele nebo změně hesla
    public final static int ERROR_OK = 0;
    public final static int ERROR_CANNOT_READ_FILE = -1;
    public final static int ERROR_CANNOT_WRITE_FILE = -2;
    public final static int ERROR_USER_DOES_NOT_EXIST = -3;
    public final static int ERROR_ACCOUNT_DISABLED = -4;
    public final static int ERROR_WRONG_PASSWORD = -5;
    public final static int ERROR_WRONG_OLD_PASSWORD = -6;
    public final static int ERROR_NO_CHANGE_PASSWORD_PERMISSION = -7;
    public final static int ERROR_CANNOT_ALLOCATE_MEMORY = -8;
    public final static int ERROR_ANOTHER_ERROR = -9;

    // chyby při zpracování dotazu
    public final static int ERROR_REGEXP_COMPILATION = (int)'r';



	private int index;

	private String server_name; // adresa serveru
	private int server_port = 2100;  // defaultní číslo portu pro spojení
	private String server_version = "1.0"; // implicitní verze serveru
	private String client_required_version; // minimální verze klienta k této verzi serveru
	private String client_recommended_version; // minimální doporučená verze klienta k této verzi serveru
        private String corpus_identifier; // identifikátor korpusu
	private boolean connected; // signalizuje, zda jsme pripojeni k serveru

	private String client_version; // verze tohoto klienta

    private String login_name;
    private String encoded_password;

    private String path_initial; // výchozí cesta treebanku dodaná konstruktoru
    private String path_actual; // aktuální cesta v treebanku při procházení adresářové struktury

    private DefaultListModel coreference_patterns; // jaké a jak malovat koreference

    private ShowMessagesAble mess; // objekt, který se bude starat o zobrazování mých hlášek
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám

    private ServerNetCommunication server_net_com; // objekt pro zasílání zpráv serveru a příjímání zpráv od serveru po internetu

    // pro čtení statistik o prohledávání korpusu
    private long number_of_actual_occurrence;
    private long number_of_actual_tree;
    private long number_of_found_occurences;
    private long number_of_found_trees;
    private long number_of_searched_trees;
    private long number_buffer; // pomocná proměnná pro předávání parametru typu long

    javax.swing.Timer timer_load_tree; // timer pro opakované žádosti o poslání stromu
    int timer_load_tree_delay_initial; // iniciální interval pro opakování žádosti o strom (v ms)
    int timer_load_tree_delay_actual; // aktuální interval pro opakování žádosti o strom (v ms)
    int timer_load_tree_delay_change; // o kolik se při každém průchodu zvýší interval
    char timer_load_tree_type; // načítat následující strom ('N') nebo předchozí? Pro předchozí možná není nutné
    byte timer_load_tree_subtype; // načítat další/předchozí výskyt či strom či první strom
    int timer_load_tree_counter; // pro sledování počtu opakovaných žádostí
    TreeLoadedListener last_tree_loaded_listener; // pro uschování listeneru pro opakované volání funkce loadNextTree timerem

    private NGForest last_loaded_forest;
    private NGClient jaaa;

	ServerCommunication (String this_client_version, int param_port, String param_adresa, UserAccount p_user_account, String encoded_password, ShowMessagesAble sma, ResourceBundle p_i18n, NGClient p_jaaa) {

		mess = sma;
        i18n = p_i18n;
		initialization(this_client_version, param_port, param_adresa, p_user_account, encoded_password, p_jaaa);

	} // KomNet

	ServerCommunication (String this_client_version, int param_port, String param_adresa, UserAccount p_user_account, String encoded_password, ResourceBundle p_i18n, NGClient p_jaaa) {

		mess = null;
        i18n = p_i18n;
		initialization(this_client_version, param_port, param_adresa, p_user_account, encoded_password, p_jaaa);

	} // KomNet

	private void initialization (String this_client_version, int param_port, String param_adresa, UserAccount p_user_account, String p_encoded_password, NGClient p_jaaa) {

		client_version = this_client_version;
        login_name = p_user_account.getLoginName();
        encoded_password = p_encoded_password;
        error = error_ok;
        jaaa = p_jaaa;

		zprava = new byte[maxlenmes]; // pole, do ktereho se ctou zpravy od serveru.

		// nastavení parametrů spojení podle parametrů konstruktoru

		if (param_port != -1) server_port = param_port; // jestliže specifikován parametrem appletu nebo programu, tak použij ten specifikovaný
    		if (param_adresa == null) server_name = new String ("quest.ms.mff.cuni.cz");
    		else server_name = new String (param_adresa); // stejně tak tady

        server_net_com = new ServerNetCommunication(mess, i18n);
        error = server_net_com.connect(server_name, server_port);
        error_message = server_net_com.getErrorMessage();

	    if (error != 0) { // nastala chyba pri otvirani spojeni, nastaví se nulové hodnoty
			connected = false;
            path_actual = "";
            path_initial = "";
            server_name = "";
            server_port = -1;
            server_version = "";
            client_required_version = "";
            return;
		}

        coreference_patterns = new DefaultListModel();
        
        // pokud chyba nenastala, zjistí se iniciální informace ze serveru
		int i = read5InitialInformations();
		if (i == 1) { // server odmítl spojení kvůli dosažení maxima povolených klientů
			debug("\nMaximální povolený počet připojených klientů již byl dosažen. Server odmítl spojení.");
			error = error_max_clients; // typ chyby
            // nastavim opet nulove hodnoty
    		connected = false;
            path_actual = "";
            path_initial = "";
            server_name = "";
            server_port = -1;
            server_version = "";
            client_required_version = "";
            return;
		}

        // pokud zatim vse v poradku, prihlasim se jakozto urcity uzivatel
		connected = true;
        path_initial = path_actual; // to uz je ale nyni pro jednotlive uzivatele zvlast
        if (server_version.compareToIgnoreCase("1.52")>=0) { // server jiz podporuje logovani pomoci uzivatelskeho jmena a hesla
            i = login();
        	if (i != 0) { // prihlaseni uzivatele se nezdarilo
            	debug("\nPřihlášení uživatele s přihlašovacím jménem " + login_name + " se nezdařilo.");
                debug("\n  - nastala chyba: ");
                switch (i) {
                    case ERROR_USER_DOES_NOT_EXIST:
                        debug("uživatel " + login_name + " neexistuje.");
                        error_message = i18n.getString("ERROR_MESSAGE_USER_DOES_NOT_EXIST");
                    break;
                    case ERROR_ACCOUNT_DISABLED:
                        debug("konto uživatele " + login_name + " je zablokováno.");
                        error_message = i18n.getString("ERROR_MESSAGE_ACCOUNT_DISABLED");
                    break;
                    case ERROR_WRONG_PASSWORD:
                        debug("zadané heslo uživatele " + login_name + " nebylo správné.");
                        error_message = i18n.getString("ERROR_MESSAGE_WRONG_PASSWORD");
                    break;
                    case ERROR_CANNOT_READ_FILE:
                        debug("chyba na straně serveru - server nemůže číst soubor hesel.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    case ERROR_CANNOT_ALLOCATE_MEMORY:
                        debug("chyba na straně serveru - server nemohl alokovat paměť.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    case ERROR_ANOTHER_ERROR:
                        debug("chyba na straně serveru - nespecifikovaná chyba.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    default:
                        debug("neznámá chyba.");
                        error_message = i18n.getString("ERROR_MESSAGE_UNKNOWN_ERROR");
                    break;
                }
        		error = i; // typ chyby
                // nastavim opet nulove hodnoty
                connected = false;
                path_actual = "";
                path_initial = "";
                server_name = "";
                server_port = -1;
                server_version = "";
                client_required_version = "";
                return;
    		}
            else {
            	debug("\nPřihlášení uživatele s přihlašovacím jménem " + login_name + " se zdařilo.");
                try {
                    int n = p_user_account.readFromBytes(zprava,1);
                }
                catch (ServerCommunicationFormatErrorException e) {
                    p_user_account.setDefaultValues();
                    debug("\nPři čtení autorizačních informací nastala chyba " + e);
                }
                if (p_user_account.getRootDirectory().equalsIgnoreCase("default")) {

                }
                else {
                    path_initial = p_user_account.getRootDirectory();
                    path_actual = path_initial;
                }
            }
        } // if server umi logovani
        else { // server neumi logovani
            debug("\nTato verze serveru ještě nepodporuje přihlašování pomocí jména a hesla - budete přihlášen jako uživatel anonymous.");
            p_user_account.setDefaultValues();
        }

        timer_load_tree_delay_initial = TIMER_LOAD_TREE_DELAY_INITIAL; // tolik ms před opakovanou žádostí o zaslání stromu; výchozí hodnota
        timer_load_tree_delay_change = TIMER_LOAD_TREE_DELAY_CHANGE; // o tolik se bude interval prodlužovat
        timer_load_tree = new javax.swing.Timer(timer_load_tree_delay_initial, this); // timer pro opakovanou žádost o zaslání stromu
        timer_load_tree.setRepeats(false); // pouze jedenkrát vyvolat událost

        last_loaded_forest = null;
	} // initialization

	public void setMessagesShower (ShowMessagesAble sma) { // nastaví objekt, který bude vypisovat hlášky
	    mess = sma;
	}

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

    private void addInfoNotLocalized (String message) { // připíše nelokalizovanou hlášku pomocí externího objektu; předchozí hláška není smazána
      if (mess != null) {
         mess.addInfoNotLocalized(message);
      }
    }

    public String getServerName() { // vrátí jméno serveru, ke kterému je připojen
        return server_name;
    }

    public int getServerPort() { // vrátí port na serveru, ke kterému je připojen
        return server_port;
    }

    public String getServerVersion() { // vrátí verzi serveru, ke kterému je připojen
        return server_version;
    }

    public String getClientRequiredVersion() { // vrátí minimální verzi klienta požadovanou serverem, ke kterému je připojen
        return client_required_version;
    }

    public String getClientRecommendedVersion() { // vrátí minimální verzi klienta doporučovanou serverem, ke kterému je připojen
        return client_recommended_version;
    }

    public boolean isConnected() { // vrátí true, pokud je připojen k serveru, jinak false
        return connected;
    }

    public int getError() { // vrátí typ chyby, ke které naposledy došlo
		return error;
	}

    public String getErrorMessage() { // vrátí popis chyby, ke které naposledy došlo
		return error_message;
	}

        /**
         * Returns the String representation of the rest of the array pole encoded in UTF-8; it
         * starts ad position odkud and goes until it finds EOM (which should be zero in order to
         * work with UTF-8)
         * @param pole byte[]
         * @param odkud int
         * @return String
         */
        public static String getWholeString(byte[] pole, int odkud) {
          int pos = findByte(pole, odkud, EOM);
          return getString(pole, odkud, pos);
        }

	public static String getString(byte[] pole, int odkud, byte termination_char) { // vytvoří string z bytů v poli od místa odkud až do term_char nebo EOM nevčetně
	    String ret = new String();
            int pozice = odkud;
            byte prvek;
            prvek = pole[pozice++];
            while (prvek != termination_char && prvek != EOM) {
		ret += (char)prvek;
		prvek = pole[pozice++];
            }
            return ret;
	}

	private int putString(byte[] pole, int kam, String src) { // zapíše string do pole bajtů od pozice kam, vrátí délku zapsaného stringu
		// předpOKládá dost místa v poli
		int pozice = kam;
		int length = src.length();
		byte prvek;
		for (int i=0; i<length; i++) {
			prvek = (byte)src.charAt(i);
		    pole[pozice++] = prvek;
		}
		return length;
	}

/*    public DefaultListModel getReferencePatterns() {
        return reference_patterns;
    }
*/
    private void addCoreferencePattern(ReferencePattern pattern) {
        if (!coreference_patterns.contains(pattern)) {
            //debug("\nServerCommunication.addCoreferencePattern: Adding a coreference pattern");
            coreference_patterns.addElement(pattern);
        }
    }

    private void clearCoreferencePatterns() {
        coreference_patterns.clear();
    }

    private int readCoreferences (byte[] source, int start_position) { // nacita ze source koreference
        // vrací počet přečtených znaků
        // očekávám co řádka, to jedna koreference; seznam bude ukončen prázdnou řádkou. Znak konce řádky je EOL
        //debug("\nJsem v readCoreferences(byte...)");
        clearCoreferencePatterns();
        ReferencePattern pattern;
        int position = start_position; // pozice ve vstupním poli při načítání koreferencí
        int source_length = source.length;
        StringBuffer line = new StringBuffer();
        int line_length = readLine(line, source, position);
        //debug("\nline_length = " + line_length);
        position += line_length;
        while (line_length > 1) { // another coreference pattern has been read
            //debug("\nNačtena řádka: " + line);
            pattern = new ReferencePattern(mess);
            pattern.readPatternFromString(line.toString().trim());
            addCoreferencePattern(pattern);
            //debug("\nA coreference pattern has been added: " +  pattern);
            if (position >= source_length) break; // konec vstupu
            line = new StringBuffer();
            line_length = readLine(line, source, position);
            position += line_length;
        }
        //debug("\nOpouštím readCoreferences(byte...)");
        return position - start_position;
    }

/*    public int readCoreferences (char[] p_source, int start_position) { // nacita ze source koreference
        // vrací počet přečtených znaků
        // očekávám co řádka, to jedna koreference; seznam bude ukončen prázdnou řádkou. Znak konce řádky je EOL
        //debug("\nJsem v readCoreferences(char...)");
        clearCoreferencePatterns();
        ReferencePattern pattern;
        int position = start_position; // pozice ve vstupním poli při načítání koreferencí
        int source_length = p_source.length;
        StringBuffer line = new StringBuffer();
        int line_length = readLine(line, p_source, position);
        //debug("\nline_length = " + line_length);
        position += line_length;
        while (line_length > 1) { // another coreference pattern has been read
            //debug("\nNačtena řádka: " + line);
            pattern = new ReferencePattern(mess);
            pattern.readPatternFromString(line.toString().trim());
            addCoreferencePattern(pattern);
            //debug("\nA coreference pattern has been added: " +  pattern);
            if (position >= source_length) break; // konec vstupu
            line = new StringBuffer();
            line_length = readLine(line, p_source, position);
            position += line_length;
        }
        //debug("\nOpouštím readCoreferences(byte...)");
        return position - start_position;
    }
*/

    private int readLine(StringBuffer target, byte[] source, int start_position) { // čte bajty z source do StringBufferu, dokud nepřečte EOL
        // vrací počet načtených bajtů
        int position = start_position;
        int length = source.length;
        char ch;
        //debug("\nNGTree.readLine: the source length is " + length + ", the position is " + position);
        while ((ch = (char)source[position]) != EOM) {
            //debug("\nznak " + ch);
            //System.out.print("\nA character has been read: '" + ch + "'");
            position ++;
            if (ch == EOL) break; // konec řádky
            target.append(ch);
        }
        return position - start_position;
    }

/*    private int readLine(StringBuffer target, char[] source, int start_position) { // čte bajty z source do StringBufferu, dokud nepřečte EOL
        // vrací počet načtených bajtů
        int position = start_position;
        int length = source.length;
        char ch;
        //debug("\nNGTree.readLine: the source length is " + length + ", the position is " + position);
        while ((ch = source[position]) != EOM) {
            //debug("\nznak " + ch);
            //System.out.print("\nA character has been read: '" + ch + "'");
            position ++;
            if (ch == EOL) break; // konec řádky
            target.append(ch);
        }
        return position - start_position;
    }
*/
    public void readServerVersion() { // pošle serveru žádost o zaslání verze serveru
		zprava[0] = (byte)'V'; // posilam GETVERSION
		zprava[1] = (byte)'s'; // jde mi o SERVER_VERSION
		zprava[2] = EOM;
		send(zprava, 3);
		receive(zprava, EOM);  // ctu verzi serveru
		server_version = getString(zprava,1,EOM); // vytvoří string z bytů ve zprávě od místa 1 až do EOM
		debug ("\nServer zaslal informaci o své verzi; je to: " + server_version);
	}

	public void readClientRequiredVersion() { // pošle serveru žádost o zaslání požadované minimální verze klienta
		zprava[0] = (byte)'V'; // posilam GETVERSION
		zprava[1] = (byte)'c'; // jde mi o CLIENT_REQUIRED_VERSION
		zprava[2] = EOM;
		send(zprava, 3);
		receive(zprava, EOM);  // ctu pozadovanou verzi clienta
		client_required_version = getString(zprava,1,EOM); // vytvoří string z bytů ve zprávě od místa 1 až do EOM
		debug ("\nServer zaslal informaci o minimální požadované verzi klienta; je to: " + client_required_version);
	}

	public void readActualPath() {  // posle serveru zadost o zaslani aktualni cesty pro prochazeni adr. struktury; odpoved je nactena do pole zpravy
            //debug("\nJsem ve funkci readActualPath().");
            zprava[0] = (byte)'A';  // posilam PWD
            zprava[1] = EOM;
            send(zprava, 2);
            receive(zprava, EOM);  // ctu cestu
            path_actual = getString(zprava,1,EOM);
	}

    private String getStringFromBytes(byte[] src, boolean verbose) { // vrati Stringovou reprezentaci pole bytu

        /*StringBuffer buf = new StringBuffer();
        byte b;
        int position = 0;
        while ((b = src[position++]) != EOM) {
            if (verbose) {
                debug("\nznak " + b);
            }
            buf.append((char)b);
        }
        return buf.toString();*/
        int position = 0;
        while (src[position] != EOM) position++; // counting the length of the message
        String encoded;
        debug(" "+ position);
        try {
            encoded = new String(src, 0, position, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e) {
            encoded = "Unsupported Encoding Exception occured";
        }
        return encoded;
    }

	public int read5InitialInformations() { // pošle serveru žádost o zaslání jeho verze, požadované a doporučené verze klienta,
        // výchozího adresáře, identifikátoru korpusu a schémat koreferencí
		// vrací 0, pokud v pořádku, 1, pokud server odmítl spojení kvůli dosažení maxima klientů
		zprava[0] = (byte)'I';  // posilam GET_INIT_INFO
        int position = 1;
		position += putString(zprava, position, client_version); // přitom zašlu serveru moji verzi
		zprava[position++] = EOM;
        send(zprava, position);
		receive(zprava, EOM);  // čtu odpověď
		if (zprava[0] == OK) { // server odpověděl podle očekávání
			int start = 1; // začnu číst od pozice 1
		    debug ("\nServer zaslal následující úvodní informace: ");

		    server_version = getString(zprava,start,EOL); // vytvoří string z bytů ve zprávě od místa start až do EOL
		    start += server_version.length() + 1; // posunu se o přečtený řetězec a ukončovací znak řádku
		    debug ("\n  - verze serveru je: " + server_version);

			client_required_version = getString(zprava,start,EOL);
			start += client_required_version.length() + 1; // posunu se o přečtený řetězec a ukončovací znak řádku
			debug ("\n  - minimální požadovaná verze klienta je: " + client_required_version);

            path_actual = getString(zprava,start,EOL);
			start += path_actual.length() + 1; // posunu se o přečtený řetězec a ukončovací znak řádku

			client_recommended_version = getString(zprava,start,EOL);
			if (client_recommended_version.length() < 1) {
				client_recommended_version = client_required_version;
				debug ("\n  - minimální doporučená verze klienta není specifikována, použiji minimální požadovanou.");
		    }
			else {
				debug ("\n  - minimální doporučená verze klienta je: " + client_recommended_version);
		    }
            start += client_recommended_version.length() + 1; // posunu se o přečtený řetězec a ukončovací znak řádku

            corpus_identifier = getString(zprava,start,EOL);
            start += corpus_identifier.length() + 1; // posunu se o přečtený řetězec a ukončovací znak řádku (raději dřív než s tím řetězcem budu čachrovat)
            corpus_identifier = corpus_identifier.replace('\\', '_'); // nahrazuji případné výskyty zpětného lomítka v cestě podtržítky
            corpus_identifier = corpus_identifier.replace('/', '_'); // nahradím výskyty lomítka v cestě podtržítky
            debug ("\n  - identifikátor korpusu je: " + corpus_identifier);

            start += readCoreferences (zprava, start); // nacte ze source koreference
            jaaa.zalozka_query.setCoreferencePatterns(coreference_patterns);
            jaaa.zalozka_trees.setCoreferencePatterns(coreference_patterns);
            
        }
		else { // tzn., že je moc připojených klientů nebo že je asi server verze < 1.3, nerozpoznal direktivu 'I' a neodpověděl (jinak než vrácením otázky)
		    if (zprava[0] == 'M') { // příliš mnoho připojených klientů - odpojím se od serveru (stejně tak se odpojí server)
                server_net_com.disconnect();
		        connected = false;
				return 1;
		    }
			else { // tzn., že je asi server verze < 1.3, nerozpoznal direktivu 'I' a neodpověděl (jinak než vrácením otázky)
			    readServerVersion();
			    readClientRequiredVersion();
                readActualPath();
				client_recommended_version = client_required_version;
			}
		}
		return 0;
	} // read4InitialInformations

	public int login() { // pošle serveru login name a zakódované heslo a chce po něm autentikaci a autorizaci
		// vrací 0, pokud OK, jinak typ chyby
		zprava[0] = (byte)'L';  // posilam LOGIN
        zprava[1] = (byte)'a';  // podtyp LOGIN_AUTHENTIZE
        int position = 2;
        position += putString(zprava, position, login_name);
        zprava[position++] = EOL;
        position += putString(zprava, position, encoded_password);
        zprava[position++] = EOL;
		zprava[position++] = EOM;
        //debug("\nlogin - odesilana zprava: " + getStringFromBytes(zprava,true));

		send(zprava, position);
		receive(zprava, EOM);  // čtu odpověď
        //debug("\nlogin - prijata odpoved: " + getStringFromBytes(zprava,false));
		if (zprava[0] == OK) { // prihlaseni probehlo bezchybne
    		return 0;
        }
        else { // prihlaseni se nezdarilo
            return zprava[1]; // vraci typ chyby
        }
	} // login


    public String getActualPath() { // vrátí aktuální cestu (co si pamatuje, nekomunikuje se serverem)
        return path_actual;
    }

    public String getInitialPath() { // vrátí výchozí cestu (jak si ji pamatuje, nekomunikuje se serverem)
        return path_initial;
    }

    public String getCorpusIdentifier() { // vrátí identifikátor korpusu, tak jak ho server poslal při přihlášení
        return corpus_identifier;
    }

	public void precti_adr (boolean prvni) {  // posle serveru zadost o zaslani jmen podadresaru aktualni cesty; odpoved je nactena do pole zpravy
		// parametr prvni signalizuje, zda očekávám první z případně více částí nebo již pOKračování předchozích částí
		zprava[0] = (byte)'B';  // posilam GETDIR
		if (prvni)
			zprava[1] = (byte)'f';  // očekávám první část seznamu
	    else
			zprava[1] = (byte)'m';  // očekávám pokračování seznamu
		zprava[2] = EOM;
		send(zprava, 3);
		receive(zprava, EOM);
	}

	public void precti_sbr (boolean prvni) {  // posle serveru zadost o zaslani jmen souboru (ne adresaru) v aktualni ceste. Odpoved je nactena do pole zpravy
		// parametr prvni signalizuje, zda očekávám první z případně více částí nebo již pokračování předchozích částí
		zprava[0] = (byte)'C';  // posilam GETFILES
		if (prvni)
			zprava[1] = (byte)'f';  // očekávám první část seznamu
	    else
			zprava[1] = (byte)'m';  // očekávám pOKračování seznamu
		zprava[2] = EOM;
		//debug("\nkom_net: message subtype = " + (char)zprava[1]);
		send(zprava, 3);
		receive(zprava, EOM);
	}

	public void changeDir (String adr) {  // posle serveru zadost o zmenu aktualni cesty na cestu obsazenou v parametru 'adr'. Odpoved je nactena do pole zpravy
		// 'adr' - vstup - nova aktualni cesta
		int length;
		zprava[0] = (byte)'D'; // CD
		// původně tu byla deprecated funkce adr.getBytes( 0, adr.length(), zprava, 1 );
                length = convertToBytes(zprava, 1, adr);
		zprava[length + 1] = EOM;
		send(zprava, length + 2);
		receive(zprava, EOM);
		if (zprava[1] == EOM) { // asi server starší než 1.3, musím se tedy zvlášť zeptat na novou cestu
			readActualPath();
		}
        else { // jen přečtu novou aktuální cestu ze zprávy
			path_actual = getString(zprava,1,EOM);
        }
	} // changeDir

        private int convertToBytes(byte[] dst, int position, String src) { // converts String to bytes
            int length,j,i;
            length=src.length();
            j=position;
            for (i=0; i<length; i++) {
		dst[j] = (byte)src.charAt(i);
		j++;
            }
            return length;
        }

	public int changePassword (String login_name, String encoded_old_password, String encoded_new_password) {  // posle serveru zadost o zmenu hesla
            // vraci 0, pokud OK, jinak typ chyby
            // 'login_name' - vstup - login name
            // 'old_password' - vstup - dosavadni heslo
            // 'new_password' - vstup - nove heslo
            // vsechny polozky museji obsahovat jen ascii znaky
            int pozice;
            zprava[0] = (byte)'L'; // LOGIN
            zprava[1] = (byte)'c'; // LOGIN_CHANGE_PASSWORD
            pozice = 2;
            pozice += convertToBytes(zprava, pozice, login_name);
            zprava[pozice] = EOL;
            pozice++;
            pozice += convertToBytes(zprava, pozice, encoded_old_password);
            zprava[pozice] = EOL;
            pozice++;
            pozice += convertToBytes(zprava, pozice, encoded_new_password);
            zprava[pozice] = EOL;
            pozice++;
            zprava[pozice] = EOM;

            send(zprava, pozice + 1);
            receive(zprava, EOM);
            if (zprava[0] == OK) { // zmena hesla probehla v poradku
                return error_ok;
            }
            else { // chyba pri zmene hesla
                int i = zprava[1];
            	debug("\nZměna hesla uživatele " + login_name + " se nezdařila.");
                debug("\n  - nastala chyba: ");
                switch (i) {
                    case ERROR_USER_DOES_NOT_EXIST:
                        debug("uživatel " + login_name + " neexistuje.");
                        error_message = i18n.getString("ERROR_MESSAGE_USER_DOES_NOT_EXIST");
                    break;
                    case ERROR_ACCOUNT_DISABLED:
                        debug("konto uživatele " + login_name + " je zablokováno.");
                        error_message = i18n.getString("ERROR_MESSAGE_ACCOUNT_DISABLED");
                    break;
                    case ERROR_NO_CHANGE_PASSWORD_PERMISSION:
                        debug("uživatel " + login_name + " nemá právo měnit heslo.");
                        error_message = i18n.getString("ERROR_MESSAGE_NO_CHANGE_PASSWORD_PERMISSION");
                    break;
                    case ERROR_WRONG_OLD_PASSWORD:
                        debug("zadané staré heslo uživatele " + login_name + " nebylo správné.");
                        error_message = i18n.getString("ERROR_MESSAGE_WRONG_OLD_PASSWORD");
                    break;
                    case ERROR_CANNOT_READ_FILE:
                        debug("chyba na straně serveru - server nemůže číst soubor hesel.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    case ERROR_CANNOT_WRITE_FILE:
                        debug("chyba na straně serveru - server nemůže zapisovat do souboru hesel.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    case ERROR_CANNOT_ALLOCATE_MEMORY:
                        debug("chyba na straně serveru - server nemohl alokovat paměť.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    case ERROR_ANOTHER_ERROR:
                        debug("chyba na straně serveru - nespecifikovaná chyba.");
                        error_message = i18n.getString("ERROR_MESSAGE_SERVER_ERROR");
                    break;
                    default:
                        debug("neznámá chyba.");
                        error_message = i18n.getString("ERROR_MESSAGE_UNKNOWN_ERROR");
                    break;
                }
        		error = i; // typ chyby
                return i;
            }
        } // changePassword


   	public void stopTheQuery () { // pošle serveru pOKyn k ukončení provádění dotazu

        stopLoadTreeTimer(); // pokud se čekalo na nalezení dalšího stromu z minulého dotazu, zruší se to

		zprava[0] = (byte)'S'; // STOP
		zprava[1] = EOM;
		send(zprava, 2);
        receive(zprava, EOM);
        inform ("QUERY_STOPPED");
        debug ("\nUživatel zastavil provádění dotazu.");
        readStatistics(zprava, 1);
    }

    public boolean nastavVsechnyStromy () { // pošle serveru dotaz bez kritéria výběru

        stopLoadTreeTimer(); // pokud se čekalo na nalezení dalšího stromu z minulého dotazu, zruší se to

		zprava[0] = (byte)'Q'; // DOTAZ
		zprava[1] = (byte)'a'; // všechny stromy
		zprava[2] = EOM;

		send(zprava, 3);
		receive(zprava, EOM);
		if (zprava[0] == OK) {
			inform ("ALL_TREES_SET_OK");
			//debug ("\nDotaz na všechny stromy nastaven OK");
            last_tree_loaded_listener = jaaa.zalozka_trees;
            startLoadTreeTimer(); // spustí časovač pro zaslání stromu

   			return true; // OK
		}
		else {
			inform ("ALL_TREES_SET_KO");
			debug ("\nChyba při dotazu na všechny stromy!");
			return false; // ko
		}
	}

	public int nastavStromyDleDotazu (String sp, boolean above_result, boolean match_lemma_variants, boolean match_lemma_comments, boolean first_only, boolean invert_match) { // pošle serveru dotaz s kritériem výběru sp
		// above_result určuje, zda se bude hledat nad všemi vybranými soubory (false) nebo nad výsledkem minulého dotazu (true)
		// vrací 0, pOKud v pořádku
		// vrací 1, pOKud je potřeba opakovat žádost o dotaz nad výsl. min. dotazu
		// vrací 2, pOKud server je příliš starý na obsloužení dotazu s above_result==true
		// vracá -1, pOKud nějaká chyba
		int j;

        stopLoadTreeTimer(); // pokud se čekalo na nalezení dalšího stromu z minulého dotazu, zruší se to
		String props;
	    if (getServerVersion().compareToIgnoreCase("1.45")<0) { // pro kompatibilitu se staršími servery
			if (above_result) return 2; // vrátím chybu, staré servery tuto funkci nepodporují
			props = ""; // starší servery nemají na 2. pozici rozlišovací znak, ani další vlastnosti dotazu na dalších pozicích
		}
		else { // novější servery
			String above = above_result ? "r" : "f";
			byte query_position = (byte)7; // vlastní dotaz bude začínat na této pozici zprávy
			char match_variants = match_lemma_variants ? '1' : '0';
	    	char match_comments = match_lemma_comments ? '1' : '0';
            char char_invert_match = invert_match ? '1' : '0';
            char char_first_only = first_only ? '1' : '0';

		    props = above + (char)query_position + match_variants + match_comments + char_invert_match + char_first_only;
		}
		String s = "Q" + props + sp;  // DOTAZ
		//debug("\ndotaz je: " + s);

		/*char pole[] = s.toCharArray();*/
        byte pole[];
        try {
            pole = s.getBytes("UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e) {
            pole = (new String("Unsupported Encoding Exception")).getBytes();
        }

		for (j = 0; j < pole.length; j++) {
			zprava[j] = pole[j];
        }
        zprava[j] = EOM;

		send(zprava, j+1);
		receive(zprava, EOM);

		switch (zprava[0]) {
		case OK: // OK
			debug ("\nDotaz dle kritéria výběru nastaven OK");
			inform ("TREES_BY_QUERY_SET_OK");
			//debug("\n- druhý znak odpovědi je:" + (char)zprava[1]);
            last_tree_loaded_listener = jaaa.zalozka_trees;
            startLoadTreeTimer(); // spustí časovač pro zaslání stromu
       		return 0;
			//break; // zbytečné
   		case (byte)'R': // nutno opakovat dotaz nad výsl. min. dotazu (minulý ještě neskončil)
		    return 1;
		    //break;
		default:
			debug ("\nChyba při dotazu dle kritéria výběru");
                        inform ("TREES_BY_QUERY_SET_KO");
                        String short_info = readError(zprava,1);
                        addInfoNotLocalized (short_info);
   			return -1; // chyba
			//break;
   		}
	}

   private String getUTF8PositionFromBytePosition(String regexp, String pos) { // z pozice v řetězci bytů vypočítá pozici v ekvivalentním daném řetězci UTF-8
     String ret_value = pos; // když se to nepovede
     String substr;
     byte substr_bytes[];
     int substr_bytes_length;
     int byte_position;
      try {
        byte_position = Integer.parseInt(pos);
      }
      catch (Exception e) {
        debug("\nServerCommunication.getUTF8PositionFromBytePosition: nevyšel převod řetězce " + pos + " na číslo.");
        byte_position = -1;
      }
      if (byte_position <= 1) {
        return pos; // nevyšel převod pos na integer nebo jde o malé číslo, tak vrátím pos jako nejlepší typ nebo jako správné malé číslo
      }
      int length = regexp.length();
      for (int i=2; i<=length; i++) {
        substr = regexp.substring(0,i);
        try {
          substr_bytes = substr.getBytes("UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e) {
          debug("\nServerCommunication.getUTF8PositionFromBytePosition: nevyšel převod UTF-8 řetězce na bajty.");
          return pos; // opět něco nevyšlo, vrátím původní hodnotu jako nejlepší odhad
        }
        substr_bytes_length = substr_bytes.length;
        if (substr_bytes_length >= byte_position) { // zdá se, že to máme
          return "" + i;
        }
      }
      return "" + length; // sem bych se dostat neměl; tak vracím celkovou délku v UTF-8 jako nejlepší odhad
   }

   private String readError(byte zprava[], int offset) { // přečte a zpracuje chybovou hlášku od serveru
     StringBuffer short_info = new StringBuffer();
     String buf;
     String buf2;
     int pos;
     char type = (char)zprava[offset++];
     switch (type) {
       case ERROR_REGEXP_COMPILATION:
         debug("\n" + i18n.getString("TREES_BY_QUERY_SET_KO"));

         String messages = getWholeString(zprava, offset);
         offset = 0;

         pos = messages.indexOf(EOL, offset);
         String regexp = messages.substring(offset, pos);
         offset = pos + 1; // posunu se za přečtený řetězec a ukončovací znak řádku
         buf2 = i18n.getString("ERROR_REGEXP_COMPILATION_INTRO") + ": " + regexp;
         short_info.append(" " + buf2);
         debug("\n   " + buf2);

         pos = messages.indexOf(EOL, offset);
         buf = messages.substring(offset, pos);
         offset = pos + 1; // posunu se za přečtený řetězec a ukončovací znak řádku
         String real_comp_pos = getUTF8PositionFromBytePosition(regexp, buf);
         buf2 = i18n.getString("ERROR_REGEXP_COMPILATION_POSITION") + " " + real_comp_pos;
         short_info.append(" " + buf2);
         debug("\n   " + buf2 + " (in bytes: " + buf + ")");

         pos = messages.indexOf(EOL, offset);
         buf = messages.substring(offset, pos);
         //offset = pos + 1; // posunu se za přečtený řetězec a ukončovací znak řádku
         buf2 = i18n.getString("ERROR_REGEXP_COMPILATION_DESCRIPTION") + ": " + buf;
         short_info.append("; " + buf2);
         debug("\n   " + buf2);
       break;
       default:
         short_info.append(" " + i18n.getString("ERROR_UNSPECIFIED"));
       break;
     }
     return short_info.toString();
   }
    // --------------------------------- odchycení událostí ----------------------------------


    public void actionPerformed(ActionEvent e) { // akce (doubleclick nebo mezerník nebo enter)
        Object zdroj = e.getSource();
        if (zdroj == timer_load_tree) { // je čas pro opakovanou žádost o zaslání stromu
            //debug ("\nUdálost " + timer_load_tree_type + " vyvolaná timerem");
            timerLoadTreePerformed();
        }
    } // actionPerformed

    private void startLoadTreeTimer() { // spustí časovač pro opakovanou žádost o zaslání stromu
        clearStatistics();

        timer_load_tree_subtype = GET_TREE_SUBTYPE_OCCURENCE;
        timer_load_tree_counter = 0; // půjde o první žádost o strom
        timer_load_tree_type = 'N'; // načtení následujícího stromu
        timer_load_tree_delay_actual = timer_load_tree_delay_initial;
        timer_load_tree.setDelay(timer_load_tree_delay_initial); // iniciální interval
        timer_load_tree.setInitialDelay(timer_load_tree_delay_initial);
        timer_load_tree.start();
    } // startLoadTreeTimer

    private void restartLoadTreeTimer() { // znovu spustí časovač pro opakovanou žádost o zaslání stromu
        timer_load_tree_type = 'N'; // načtení následujícího stromu
        timer_load_tree_counter++; // počítadlo opakovaných žádostí
        timer_load_tree.setDelay(timer_load_tree_delay_actual);
        timer_load_tree.setInitialDelay(timer_load_tree_delay_actual);
        timer_load_tree_delay_actual += timer_load_tree_delay_change; // prodloužení intervalu
        //debug("\nČasovač má aktuální interval: " + timer_load_tree_delay_actual);
        timer_load_tree.start();
    } // restartLoadTreeTimer

    private void stopLoadTreeTimer() { // zastaví časovač pro opakovanou žádost o zaslání stromu
        timer_load_tree.stop();
    } // stopLoadTreeTimer


    private void timerLoadTreePerformed() { // server se má znovu požádat o zaslání stromu
        stopLoadTreeTimer(); // zastavit timer, pokud běží
        if (timer_load_tree_type == 'N') // má se načíst následující strom
            loadNextTree(last_tree_loaded_listener, timer_load_tree_subtype, true); // (opakovaná) žádost
    }

    public int loadPrevTree(byte subtype) {
        // vrací 0, pokud načetl předchozí strom, 2, pokud už žádný předchozí neexistuje
        stopLoadTreeTimer(); // pokud běží timer, zrušit ho

        zprava[0] = (byte)'P';  // PREV TREE
        zprava[1] = subtype;
        zprava[2] = EOM;

        send(zprava, 3);
        receive(zprava, EOM);

        if (zprava[0] == (byte)'O') {
            switch (subtype) { // hláška se bude lišit podle typu žádosti o další strom
                case GET_TREE_SUBTYPE_TREE:
                    inform("PREV_TREE_OK");
                    break;
                case GET_TREE_SUBTYPE_OCCURENCE:
                    inform("PREV_OCCURENCE_OK");
                    break;
                case GET_TREE_SUBTYPE_CONTEXT:
                    inform("PREV_CONTEXT_OK");
                    break;
                default:
                    inform("PREV_TREE_OK");
                    break;
            }
        }
        else if (zprava[0] == (byte)'G') { // při posílání kontextu došlo k dosažení hranice souboru
            inform("NO_PREV_CONTEXT");
            readStatistics(zprava, 1); // žádný předchozí kontextový strom není, přečtu aspoň statistiky o průběhu prohledávání
            return 2; // vracím  - žádný předchozí kontextový strom není
            // tady nemusí být timer, předchozí kontextový strom buď je nebo není, nemusí se na něj nikdy čekat
        }
        else { // chyba, která reprezentuje i překročení začátku výsledků
            inform("NO_PREV_TREE");
            readStatistics(zprava, 1); // žádný předchozí strom není, přečtu aspoň statistiky o průběhu prohledávání
            return 2; // vracím  - žádný předchozí strom není
            // tady nemusí být timer, předchozí strom buď je nebo není, nemusí se na něj nikdy čekat
        }
        loadTree(); // zpracuji došlou zprávu
        return 0; // vracím 0 - strom načten
    } // loadPrevTree


    public int loadNextTree(TreeLoadedListener tree_loaded_listener, byte subtype, boolean repeat) { // vrací 0, pokud načetl další strom, 1, pokud ještě není k dispozici, 2, pokud už žádný další nevyhovuje dotazu
        // repeat znamená, zda má případně spustit timer pro opakovanou žádost
        // subtype určuje, jaký další strom se má načíst (další výskyt, první výskyt v dalším stromě)

        stopLoadTreeTimer(); // pokud běží timer, zrušit ho

        last_tree_loaded_listener = tree_loaded_listener; // uschovám to pro případné příští volání timerem

        zprava[0] = (byte)'N';   // NEXT TREE
        zprava[1] = subtype;
        zprava[2] = EOM;

        send(zprava, 3);
        receive(zprava, EOM);

        //debug("\nzprava:\n" + getStringFromBytes(zprava, false));

        int return_value;
        int statistics_position; // odkud ve zprávě začínají statistiky

        if (zprava[0] == (byte)'O') { // OK
            switch (subtype) { // hláška se bude lišit podle typu žádosti o další strom
                case GET_TREE_SUBTYPE_FIRST:
                    inform("PREV_FIRST_TREE_OK");
                    break;
                case GET_TREE_SUBTYPE_TREE:
                    inform("NEXT_TREE_OK");
                    break;
                case GET_TREE_SUBTYPE_OCCURENCE:
                    inform("NEXT_OCCURENCE_OK");
                    break;
                case GET_TREE_SUBTYPE_CONTEXT:
                    inform("NEXT_CONTEXT_OK");
                    break;
                default:
                    inform("NEXT_TREE_OK");
                    break;
            }
            timer_load_tree_counter = 0; // příště začít počítat od nuly
            return_value = 0;
        }
        else {
            if (zprava[0] == (byte)'E') {
                if (zprava[1] == (byte)'b') {
                    inform("NO_NEXT_TREE_MAX_REACHED");
                }
                else {
                    inform("NO_NEXT_TREE_END_REACHED");
                }
                return_value = 2;
                statistics_position = 2;
            }
            else if (zprava[0] == (byte)'G') { // při posílání kontextu došlo k dosažení hranice souboru
                inform("NO_NEXT_CONTEXT");
                return_value = 2;
                statistics_position = 1;
                // tady nemusí být timer, následující kontextový strom buď je nebo není, nemusí se na něj nikdy čekat
            }
            else { // server dosud nezpracoval další strom
                if (timer_load_tree_counter == 0) {
                    inform("NEXT_TREE_KO");
                }
                else {
                    jaaa.addInfo("DOT",""); // opakovaná žádost - vypiš jen další tečku
                }
                if (repeat) {
                  timer_load_tree_subtype = subtype; // uschování subtypu žádosti pro příští volání timerem
                  restartLoadTreeTimer(); // timer má vyvolat událost načtení dalšího stromu
                }
                return_value = 1;
                statistics_position = 1;
            }
            statistics_position += readStatistics(jaaa.kom_net.zprava, statistics_position);
            if (tree_loaded_listener != null) {
                tree_loaded_listener.statisticsLoaded(); // upozorním žadatele o načtení stromu, že byly zatím načteny alespoň pouze statistiky
            }

            return return_value;
        }
        loadTree(); // zpracuji příchozí zprávu
        if (tree_loaded_listener != null) {
            tree_loaded_listener.treeLoaded(); // upozorním žadatele o načtení stromu, že už je načtený
        }
        return return_value;
    } // loadNextTree

    public int removeOccurrence(TreeLoadedListener tree_loaded_listener, boolean repeat) { // vrací 0, pokud načetl další strom, 1, pokud ještě není k dispozici, 2, pokud už žádný další nevyhovuje dotazu
        // repeat znamená, zda má případně spustit timer pro opakovanou žádost

        stopLoadTreeTimer(); // pokud běží timer, zrušit ho

        last_tree_loaded_listener = tree_loaded_listener; // uschovám to pro případné příští volání timerem

        zprava[0] = (byte)'K';   // REMOVE ACT. OCCURRENCE AND SEND THE NEXT TREE
        zprava[1] = EOM;

        send(zprava, 2);
        receive(zprava, EOM);

        //debug("\nzprava:\n" + getStringFromBytes(zprava, false));

        int return_value;
        int statistics_position; // odkud ve zprávě začínají statistiky

        switch (zprava[0]) {

            case (byte)'O': // OK
                inform("TREE_ERASED_NEXT_TREE_OK");
                return_value = 0;
                break;

            case (byte)'P': // OK_PREV (End of Searching, previous tree loaded)
                if (zprava[1] == (byte)'b') {
                    inform("TREE_ERASED_NO_NEXT_TREE_MAX_REACHED");
                }
                else {
                    inform("TREE_ERASED_NO_NEXT_TREE_END_REACHED");
                }
                return_value = 2;
                //statistics_position = 2;
                break;

            default: // server dosud nezpracoval další strom
                if (timer_load_tree_counter == 0) {
                    inform("TREE_ERASED_NEXT_TREE_KO");
                }
                else {
                    jaaa.addInfo("DOT",""); // opakovaná žádost - vypiš jen další tečku
                }
                if (repeat) {
                  timer_load_tree_subtype = GET_TREE_SUBTYPE_OCCURENCE; // uschování subtypu žádosti pro příští volání timerem
                  restartLoadTreeTimer(); // timer má vyvolat událost načtení dalšího stromu
                }
                return_value = 1;
                statistics_position = 1;

                statistics_position += readStatistics(jaaa.kom_net.zprava, statistics_position);
                if (tree_loaded_listener != null) {
                    tree_loaded_listener.statisticsLoaded(); // upozorním žadatele o načtení stromu, že byly zatím načteny alespoň pouze statistiky
                 }

                return return_value;
        } // switch

        timer_load_tree_counter = 0; // příště začít počítat od nuly
        loadTree(); // zpracuji příchozí zprávu
        if (tree_loaded_listener != null) {
            tree_loaded_listener.treeLoaded(); // upozorním žadatele o načtení stromu, že už je načtený
        }
        return return_value;
    } // removeOccurrence


    public static int findByte(byte[] zprava, int position, byte character) { // vrati pozici znaku character ve zprave (vzdalenost od position)
        int dist=position;
        while (zprava[dist] != character) dist++;
        return dist-position;
    }

    public static String getString(byte[] zprava, int position, int length) { // vrati pole znaku ziskanych konverzi z pole bajtu v UTF-8; zacina od position, bere lentgh bajtu
        String temp;
        try {
            temp = new String(zprava, position, length, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException e) {
            temp = "Unsupported Encoding Exception";
        }
        return temp;
    }


    public NGForest loadTree() { // přečte strom ze zprávy včetně všech informací jdoucích se stromem

        int position = 2; // obsah zprávy začíná na pozici 2
        // nejprve se přečte jméno souboru, ze kterého je nalezený strom
        String jmeno_souboru_se_stromem = new String(""); // sem se bude psát jméno souboru
        String poradi_lesa_v_souboru_string = new String(""); // sem se zapíše pořadí lesa v souboru jako řetězec
        int poradi_lesa_v_souboru = 0; // sem se zapíše pořadí lesa v souboru jako číslo
        while (zprava[position] != '\n') {
            jmeno_souboru_se_stromem += (char)zprava[position];
            position++;
        }
        //debug ("\nStrom je ze souboru: " + jmeno_souboru_se_stromem);
        // jméno souboru načteno
        position++;

        if (getServerVersion().compareToIgnoreCase("1.93")>=0) { // server od verze 1.93 posílá i pořadí stromu v souboru
            //debug("\nGoing to read number of the forest in the original file from the server...");
            while (zprava[position] != '\n') {
                poradi_lesa_v_souboru_string += (char)zprava[position];
                position++;
            }
            //debug ("\nLes má v souboru toto pořadí: " + poradi_lesa_v_souboru_string);
            // pořadí lesa načteno
            position++;
            try {
                poradi_lesa_v_souboru = Integer.parseInt(poradi_lesa_v_souboru_string);
            }
            catch (NumberFormatException e) {
                poradi_lesa_v_souboru = 0;
            }
            //debug("done: " + poradi_lesa_v_souboru);
        }

        // dale se nacte vrchol matchujici s korenem dotazu (jeho ord, resp. dord - to asi neni pravda)
        String matchujici_vrcholy = new String(""); // a sem matchující vrcholy (jejich pořadí jakožto řetězec čísel oddělených čárkou)
        if ((char)zprava[position]=='\n') matchujici_vrcholy = "0"; // matchující vrcholy neurčeny - nemělo by se stát
        else { // dotaz s kritériem - přečtou se matchující vrcholy
            while (zprava[position] != '\n') {
                matchujici_vrcholy += (char)zprava[position];
                position++;
            }
            // matchující vrcholy načteny
        }
        //debug ("\nMatchující vrcholy: " + matchujici_vrcholy);

        position++;

        // pokud je verze serveru >= 1.53, načtou se nyní statistiky o dosavadním prohledávání
        position += readStatistics(zprava, position);
        //debug("\nPořadí načteného stromu mezi nalezenými výskyty dotazu: " + number_of_actual_occurrence);
        //debug("\nPočet nalezených výskytů dotazu: " + number_of_found_occurences);
        //debug("\nPočet stromů, ve kterých byl dotaz nalezen: " + number_of_found_trees);
        //debug("\nPočet doposud prohledaných stromů: " + number_of_searched_trees);
        // teď se načte hlavička
        NGTreeHead head = new NGTreeHead(mess);
        int dist = findByte(zprava, position, EOM); // vrati pozici znaku EOM ve zprave (vzdalenost od position)
        char[] chars = getString(zprava, position, dist+1).toCharArray();
        position = 0; // odted ukazuje position do pole charu chars
        position += head.readTreeHead(chars, position);
        DefaultListModel model_list_actual_head = head.getModel(); // seznam objektů atributů

        if (head.N == -1) {
            head.N = head.W; // zřejmě analytický strom - pořadí slov ve větě a uzlů ve stromu je shodné
        }
        if (head.W == -1) {
            head.W = head.N; // zřejmě analytický strom - pořadí slov ve větě a uzlů ve stromu je shodné
        }

        // zde je také místo pro budoucí defaultní výběr atributů...

        NGTree tree = new NGTree(mess);
        NGForest forest = new NGForest(mess);
        forest.addTree(tree);

        forest.setFileName(jmeno_souboru_se_stromem);
        forest.setForestNumber(poradi_lesa_v_souboru);
        forest.setHead(head);

        // cteni stromu
        int pocet_atr = model_list_actual_head.getSize();

        position += tree.readTree(head, chars, position, pocet_atr);

        // nyní zjistím, který vrchol matchoval s kořenem dotazu, abych ho mohl vybrat
        int mat_vrch;
        try {
            // nejprve oddělím první číslo
            String match_vrchol;
            int carka = matchujici_vrcholy.indexOf(NGTree.MATCHING_NODES_DELIMITER);
            int dvojtecka = matchujici_vrcholy.indexOf(NGTree.MATCHING_NODES_QUERY_DELIMITER);

            if (carka > 0) {
                if (dvojtecka > 0 && dvojtecka < carka) {
                  match_vrchol = matchujici_vrcholy.substring(0, dvojtecka);
                }
                else {
                  match_vrchol = matchujici_vrcholy.substring(0, carka);
                }
            }
            else {
                if (dvojtecka > 0) {
                  match_vrchol = matchujici_vrcholy.substring(0, dvojtecka);
                }
                else {
                  match_vrchol = matchujici_vrcholy;
                }
            }

            // to pak převedu na číslo
            mat_vrch = Integer.parseInt(match_vrchol); // prevedu String na int - tj. získám pořadí prvního matchujícího vrcholu - kořene dotazu
        }
        catch (Exception e) {
            mat_vrch = 0; // zaškrtnu kořen dotazu
        }
        //debug("\nPořadí vrcholu matchujícího s kořenem dotazu: " + mat_vrch);
        tree.setChosenNodeByDepthOrder(mat_vrch+1); // tato funkce to počítá od jedničky
        tree.setMatchingNodes(matchujici_vrcholy); // nastavím ještě ostatní vrcholy matchující s dotazem
        //debug("\nzprava:\n" + getStringFromBytes(zprava, false));
        //vytiskniZnakyACisla(getStringFromBytes(zprava, false));
        // následující dva řádky četly u starších klientů koreferenční schémata
        //position ++; // tady bych měl přeskočit jeden EOL (prázdnou řádku)
        //position += forest.readCoreferences(chars, position); // načtení koreferenčních schémat

        //last_loaded_tree = tree;
        last_loaded_forest = forest;
        return forest;

    } // loadTree

    private void vytiskniZnakyACisla(String s) {
        //debug("\nrozepsano na cisla:\n");
        int length = s.length();
        int d;
        for (int i=0; i<length; i++) {
            d = s.charAt(i);
            debug(" " + s.charAt(i) + "-" + d);
        }
    }

    public NGForest getForest() {
        return last_loaded_forest;
    }

    private int readNumber(byte[] src, int start_position) { // it reads one long integer from byte[]
        String str;
        int length;
        str = ServerCommunication.getString(src, start_position, ServerCommunication.EOL);
        length = str.length();
        //debug("\nČtu číslo: " + str);
        try {
            number_buffer = Long.parseLong(str); // globální proměnná
        }
        catch (NumberFormatException e) {
            debug("\nNGClient.PanelTrees.readNumber: chyba " + e + "při převodu řetězce na číslo!");
        }
        //debug("\nPřečteno číslo: " + dst);
        return length + 1; // počítám i znak konce řádku
    } // readNumber

    private void clearStatistics() { // vynuluje statistiky o prohledávání korpusu
        number_of_actual_occurrence = 0;
        number_of_actual_tree = 0;
        number_of_found_occurences = 0;
        number_of_found_trees = 0;
        number_of_searched_trees = 0;
    } // clearStatistics


    public void getStatistics() { // přečte statistiky ze serveru
        if (getServerVersion().compareToIgnoreCase("1.53")<0) { // server ještě nepodporuje zasílání statistik
            return;
        }
        // nyní se pošle serveru žádost o zaslání statistik o prohledávání
        zprava[0] = (byte)'Y'; // GET_STATISTICS
        zprava[1] = EOM;
        send(zprava, 2);
        receive(zprava, EOM);  // ctu statistiky
        readStatistics(zprava, 1); // přečtu statistiky ze zprávy
    } // getStatistics

    private int readStatistics(byte[] src, int start_position) {
        if (getServerVersion().compareToIgnoreCase("1.53")<0) { // server ještě nepodporuje zasílání statistik
            return 0;
        }
        int length = 0; // number of read characters
        if (getServerVersion().compareToIgnoreCase("1.75")<0) { // server ještě nepodporuje zasílání pořadí nalezeného stromu (jen výskytu)
            length += readNumber(src, start_position + length);
            number_of_actual_occurrence = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_found_occurences = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_found_trees = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_searched_trees = number_buffer;
        }
        else { // server už podporuje zasílání kompletních statistik
            length += readNumber(src, start_position + length);
            number_of_actual_occurrence = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_actual_tree = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_found_occurences = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_found_trees = number_buffer;
            length += readNumber(src, start_position + length);
            number_of_searched_trees = number_buffer;
        }
        return length;
    } // readStatistics

    public long getNumberOfActualOccurrence() {
        //debug("\nNumber of actual occurrence is: " + number_of_actual_occurrence);
        return number_of_actual_occurrence;
    }
    public long getNumberOfActualTree() {
        //debug("\nNumber of actual tree is: " + number_of_actual_tree);
        return number_of_actual_tree;
    }
    public long getNumberOfFoundOccurences() {
        return number_of_found_occurences;
    }
    public long getNumberOfFoundTrees() {
        return number_of_found_trees;
    }
    public long getNumberOfSearchedTrees() {
        return number_of_searched_trees;
    }

    public String getFileTail() { // pošle serveru žádost o zaslání informací za posledním stromem v souboru, ze kterého byl posledně poslán strom
        // vrací načtený řetězec
        String file_tail = "";
        zprava[0] = (byte)'T';  // posilam GET_TAIL
        zprava[1] = EOM;
        send(zprava, 2);
        receive(zprava, EOM);  // čtu odpověď až do znaku EOM
        if (zprava[0] == OK) { // server odpověděl podle očekávání
            int start = 1; // začnu číst od pozice 1
            file_tail = getString(zprava,start,EOM); // vytvoří string z bytů ve zprávě od místa start až do EOM
            debug ("\nServer zaslal následující informace zpoza stromů v souboru: \n" + file_tail);
        }
        return file_tail;
    } // getFileTail

    public void send(byte buffer[], int delka) { // zapise do vystupniho proudu schranky data z pole 'buffer' o delce 'delka'
        // volana programem, kdykoliv je treba poslat zpravu serveru
        // 'buffer' - vstup - pole se zpravou urcenou k zapisu
        // 'delka' - vstup - delka zpravy
        try {
            server_net_com.send(buffer, delka);
        }
        catch (ServerNetCommunicationException e) {
            System.out.println("ServerCommunication.send: An error occured during sending data to the server!");
            debug ("\nServerCommunication.send: An error occured during sending data to the server!");
        }
    } // sendData

    public int receive(byte buffer[], byte EOM) { // cte data ze vstupniho proudu schranky a uklada je do pole 'buffer'; cte az do hodnoty 'EOM' vcetne
        // volana programem, kdykoliv je treba cist data prichozi od serveru
        // 'buffer' - vystup - pole, do nehoz se bude cist
        // vraci pocet prectenych bajtu
        int number = 0;
        try {
            number = server_net_com.receive(buffer, EOM);
        }
        catch (ServerNetCommunicationException e) {
            System.out.println("ServerCommunication.receive: An error occured during recieving data from the server!");
            debug ("\nServerCommunication.receive: An error occured during recieving data from the server!");
        }
        return index+1;
    } // receiveData


    public void destroy() { // ukončí spojení se serverem; volaná interpretem z objektu NGClient (patrně z apletu)
        if (connected) {
            stopLoadTreeTimer();
            connected = false;
            zprava[0] = (byte)'Z';
            zprava[1] = EOM;
            send(zprava,2);
            // receive(zprava, EOM); to už mě nezajímá
            server_net_com.disconnect();
        }
    } // destroy

} // class ServerCommunication
