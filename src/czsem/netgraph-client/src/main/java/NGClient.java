/*
  Netgraph - client
*/

import java.awt.*;
import java.awt.event.*;
import static java.awt.event.KeyEvent.*;
//import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
//import java.util.Iterator;
import javax.swing.*;
import java.lang.*;
import java.security.*; // kodovani hesla
import java.applet.*;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import javax.swing.KeyStroke; // pro horké klávesy

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.trees.*;
/* ###zk začátek aplikačního kódu */
import cz.cuni.mff.mirovsky.properties.*;
/* ###kk konec aplikačního kódu */
import cz.cuni.mff.mirovsky.account.*;

// ====================================================================================================
//  															class NGClient
// ====================================================================================================


/**
 * The main class in the Netgraph client. It takes care of the initialization of the program, creates the GUI, and handles
 * the events from the main menu.
 */

/* ###zt začátek apletovského kódu
public class NGClient extends javax.swing.JApplet implements ActionListener, ShowMessagesAble {
/* ###kt konec apletovského kódu */

/* ###zk začátek aplikačního kódu */
public class NGClient extends JFrame implements ActionListener, WindowListener, ShowMessagesAble {
/* ###kk konec aplikačního kódu */

    private final static String client_version = "1.94 (15.7.2008)";
    private final static String server_required_version = "1.93";
    private final static String server_recommended_version = "1.94";

    private final static String server_default_name = "quest.ms.mff.cuni.cz";
//    private final static int server_default_port = 2120; // Arabic treebank
    private final static int server_default_port = 2200; // PDT 2.0

    private String last_login_name = ""; // načtou se z general properties
    private String last_server_name = server_default_name;
    private int last_server_port = server_default_port;


    private NGClient jaaa; // pro podtřídy
    private int i, k;

    private int ind;
    public int pocet_atr;
    private boolean lomeny;

    ServerCommunication kom_net; // objekt pro komunikaci se serverem

    UserAccount user_account; // objekt udržující informace o uživateli a jeho právech

    String language; // lokalizace
    String country;
    Locale currentLocale;
    ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám

    JTabbedPane hlavni_zalozky; // zalozky hlavnich ploch
    PanelFiles zalozka_files; // zalozka pro vyber souboru s daty
    PanelQuery zalozka_query; // zalozka pro kladeni dotazu
    PanelTrees zalozka_trees; // zalozka pro prohlizeni vysledku dotazu
    PanelDebug zalozka_debug;  // zalozka pro debugovaci informace
    InfoBar info_bar;  // řádkový výpis informací pro uživatele

    JMenuBar menu_bar; // hlavní menu
    JMenuItem menu_soubor_connect; // položka menu - připojení k serveru
    JMenuItem menu_soubor_disconnect; // položka menu - odpojení od serveru
/* ###zk začátek aplikačního kódu */
    JMenuItem menu_soubor_change_password; // položka menu - změna hesla
    JMenuItem menu_soubor_save_trees; // položka menu - uložení výsledku dotazu jakožto fs souboru
    JMenuItem menu_soubor_print; // položka menu - tisk stromu
    JMenuItem menu_soubor_exit; // ukončení aplikace
/* ###kk konec aplikačního kódu */
    JCheckBoxMenuItem menu_zobrazeni_internal_info; // položka menu - použít interní info lištu
/* ###zt začátek apletovského kódu
        JCheckBoxMenuItem menu_zobrazeni_external_info; // položka menu - použít info lištu prohlížeče
/* ###kt konec apletovského kódu */
    JRadioButtonMenuItem rbutton_query_coding_unicode; // kódování znaků v dotazech
    JRadioButtonMenuItem rbutton_query_coding_pseudo;

    JRadioButtonMenuItem menu_options_fontsize_query_6;
    JRadioButtonMenuItem menu_options_fontsize_query_8;
    JRadioButtonMenuItem menu_options_fontsize_query_10;
    JRadioButtonMenuItem menu_options_fontsize_query_12;
    JRadioButtonMenuItem menu_options_fontsize_query_14;
    JRadioButtonMenuItem menu_options_fontsize_query_16;
    JRadioButtonMenuItem menu_options_fontsize_query_18;
    JRadioButtonMenuItem menu_options_fontsize_query_20;
    JRadioButtonMenuItem menu_options_fontsize_query_22;
    JRadioButtonMenuItem menu_options_fontsize_query_24;
    JRadioButtonMenuItem menu_options_fontsize_query_26;
    JRadioButtonMenuItem menu_options_fontsize_query_28;
    JRadioButtonMenuItem menu_options_fontsize_query_30;
    JRadioButtonMenuItem menu_options_fontsize_query_32;

    JRadioButtonMenuItem menu_options_fontsize_result_6;
    JRadioButtonMenuItem menu_options_fontsize_result_8;
    JRadioButtonMenuItem menu_options_fontsize_result_10;
    JRadioButtonMenuItem menu_options_fontsize_result_12;
    JRadioButtonMenuItem menu_options_fontsize_result_14;
    JRadioButtonMenuItem menu_options_fontsize_result_16;
    JRadioButtonMenuItem menu_options_fontsize_result_18;
    JRadioButtonMenuItem menu_options_fontsize_result_20;
    JRadioButtonMenuItem menu_options_fontsize_result_22;
    JRadioButtonMenuItem menu_options_fontsize_result_24;
    JRadioButtonMenuItem menu_options_fontsize_result_26;
    JRadioButtonMenuItem menu_options_fontsize_result_28;
    JRadioButtonMenuItem menu_options_fontsize_result_30;
    JRadioButtonMenuItem menu_options_fontsize_result_32;

    JRadioButtonMenuItem menu_options_fontsize_sentence_6;
    JRadioButtonMenuItem menu_options_fontsize_sentence_8;
    JRadioButtonMenuItem menu_options_fontsize_sentence_10;
    JRadioButtonMenuItem menu_options_fontsize_sentence_12;
    JRadioButtonMenuItem menu_options_fontsize_sentence_14;
    JRadioButtonMenuItem menu_options_fontsize_sentence_16;
    JRadioButtonMenuItem menu_options_fontsize_sentence_18;
    JRadioButtonMenuItem menu_options_fontsize_sentence_20;
    JRadioButtonMenuItem menu_options_fontsize_sentence_22;
    JRadioButtonMenuItem menu_options_fontsize_sentence_24;
    JRadioButtonMenuItem menu_options_fontsize_sentence_26;
    JRadioButtonMenuItem menu_options_fontsize_sentence_28;
    JRadioButtonMenuItem menu_options_fontsize_sentence_30;
    JRadioButtonMenuItem menu_options_fontsize_sentence_32;

    JCheckBoxMenuItem menu_options_lemma_variants_show; // položka menu - zobrazovat varianty lemmat
    JCheckBoxMenuItem menu_options_lemma_variants_match; // položka menu - automaticky vyhledávat varianty lemmat (včetně vysvětlivek)
    JCheckBoxMenuItem menu_options_lemma_comments_show; // položka menu - zobrazovat vysvětlivky lemmat
    JCheckBoxMenuItem menu_options_lemma_comments_match; // položka menu - automaticky vyhledávat vysvětlivky lemmat

    JRadioButtonMenuItem rbutton_order_nodes_left_right; // položka menu - řazení uzlů ve stromech
    JRadioButtonMenuItem rbutton_order_nodes_right_left;
    //JRadioButtonMenuItem rbutton_order_words_left_right; // položka menu - řazení slov ve větě
    //JRadioButtonMenuItem rbutton_order_words_right_left;

/* ###zk začátek aplikačního kódu */
    JMenuItem menu_tools_external_command_start; // položka menu - spuštění externího příkazu
    JMenuItem menu_tools_external_command_edit; // položka menu - editace externího příkazu

    String external_command = ""; // externí příkaz
    // následující dva řetěžce jsou v externím příkazu před spuštěním nahrazeny příslušnými hodnotami týkajícími se aktuálního stromu 
    private final static String EXTERNAL_COMMAND_VAR_FILE_NAME = "%FILE_NAME%"; // když se to změní tady, musí se to změnit i v lokalizačních souborech
    private final static String EXTERNAL_COMMAND_VAR_TREE_NUMBER = "%TREE_NUMBER%"; // když se to změní tady, musí se to změnit i v lokalizačních souborech
    private final static String EXTERNAL_COMMAND_VAR_ROOT_ORDER = "%ROOT_ORDER%"; // když se to změní tady, musí se to změnit i v lokalizačních souborech
    private final static String EXTERNAL_COMMAND_VAR_CHOSEN_NODE_ORDER = "%CHOSEN_NODE_ORDER%"; // když se to změní tady, musí se to změnit i v lokalizačních souborech
/* ###kk konec aplikačního kódu */

    JMenuItem menu_help_manual; // položka menu - manuál
    JMenuItem menu_help_changelog; // položka menu - aktuální změny
    JMenuItem menu_help_home_page; // položka menu - domovská stránka Netgraphu

    boolean use_internal_info_bar = true; // použít interní info lištu?
/* ###zt začátek apletovského kódu
        boolean use_external_info_bar = false; // použít info lištu prohlížeče?
/* ###kt konec apletovského kódu */
    int coding_in_queries = CharCode.coding_unicode; // číslo kódování češtiny v dotazech
    int nodes_ordering_in_trees = NGTreeProperties.DIRECTION_LEFT_RIGHT; // implicitní řazení uzlů ve stromech
    //int nodes_ordering_in_trees = NGTreeProperties.DIRECTION_RIGHT_LEFT; // Arabic - implicitní řazení uzlů v arabských stromech
    //int words_ordering_in_sentences = NGTreeProperties.DIRECTION_LEFT_RIGHT; // implicitní řazení slov ve větách

    boolean lemma_variants_show = true; // zobrazovat varianty lemmat ve stromech
    boolean lemma_variants_match = true; // automaticky vyhledávat varianty lemmat (včetně komentářů)
    boolean lemma_comments_show = false; // zobrazovat vysvětlivky lemmat ve stromech
    boolean lemma_comments_match = true; // automaticky vyhledávat lemmata bez ohladu na vysvětlivky

    String client_install_url; // adresa, kde je nainstalován client (např. kvůli hledání dokumentace); defaultní hodnota v init()
    private int param_port; // port serveru
    private String param_adresa; // URL serveru
    private boolean param_port_specified; // určují, zda daný parametr byl dán z příkazové řádky
    private boolean param_adresa_specified;

/* ###zk začátek aplikačního kódu */
    private ServerConnectionDialog server_connection_dialog; // dialogové okno pro výběr serveru a login naem a hesla
    private ChangePasswordDialog change_password_dialog; // dialogové okno pro změnu

    private int main_window_width = 780; // implicitní šířka hlavního okna
    private int main_window_height = 560; // implicitní výška hlavního okna
/* ###kk konec aplikačního kódu */

    static String [] parametry; // parametry aplikace při spuštění
    static int pocet_parametru; // počet parametrů

    private final static Cursor cursor_default = new Cursor(Cursor.DEFAULT_CURSOR);
    private final static Cursor cursor_wait = new Cursor(Cursor.WAIT_CURSOR);

/* ###zt začátek apletovského kódu
        AppletContext applet_context; // pro pristup k prohlizeci, ve kterém applet běží
/* ###kt konec apletovského kódu */

/* ###zk začátek aplikačního kódu */
    Properties properties_general_properties; // properties pro obecná nastavení
/* ###kk konec aplikačního kódu */


    /**
     * Creates the new main object of Netgraph. Initializes the program and the communication with the server.
     */
    public NGClient() { // hlavní konstruktor

/* ###zk začátek aplikačního kódu */
        super ("Netgraph " + client_version);
        jaaa = this; // pro vnořené třídy

        System.out.println("Netgraph client version " + client_version);

        loadGeneralProperties(); // nahraji z disku uložená obecná nastavení aplikace (a přečtu nastavení pro objekt NGClient)
        init();
        // teď, když už existují záložky, mohu přečíst uložená obecná nastavení pro ně:
        zalozka_files.readGeneralProperties(properties_general_properties);
        zalozka_query.readGeneralProperties(properties_general_properties);
        zalozka_trees.readGeneralProperties(properties_general_properties);

        this.addWindowListener(this);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // zavřu si okno jedině, když to uživatel potvrdí
        this.setSize(main_window_width, main_window_height);
        //debug("\nwidth set to " + main_window_width + " and in fact is " + this.getSize().getWidth());
        //debug("\nheight set to " + main_window_height + " and in fact is " + this.getSize().getHeight());
        this.setVisible(true); // tímto se objeví hlavní okno aplikace

        setWaitCursor(); // tohle na tomto místě nějak nefunguje
        // nyní dám ještě uživateli možnost změnit server - otevřu dialogové okno
        server_connection_dialog = new ServerConnectionDialog(this, i18n.getString("DIALOG_SELECT_SERVER_TITLE"), true, this, i18n);
        change_password_dialog = new ChangePasswordDialog(this, i18n.getString("DIALOG_CHANGE_PASSWORD_TITLE"), true, this, i18n);

        if (param_adresa_specified) { // jestliže byla adresa serveru dána z příkazové řádky
            server_connection_dialog.addServerName(param_adresa);
        }
        if (! server_connection_dialog.isServerNameInCombo(last_server_name)) {
            server_connection_dialog.addServerName(last_server_name);
        }
        if (! server_connection_dialog.isServerNameInCombo(server_default_name)) {
            server_connection_dialog.addServerName(server_default_name);
        }
        if (! server_connection_dialog.isServerNameInCombo("localhost")) {
            server_connection_dialog.addServerName("localhost");
        }


        if (param_port_specified) { // jestliže byl port serveru dán z příkazové řádky
            server_connection_dialog.addServerPort(param_port);
        }
        if (! server_connection_dialog.isServerPortInCombo(last_server_port)) {
            server_connection_dialog.addServerPort(last_server_port);
        }
        if (! server_connection_dialog.isServerPortInCombo(server_default_port)) {
            server_connection_dialog.addServerPort(server_default_port);
        }

        user_account = new UserAccount(this, i18n);
        user_account.setLoginName(properties_general_properties.getStringProperty("connection to server", "last login name",user_account.getLoginName()));
        connectToServer();
        setDefaultCursor();
/* ###kk konec aplikačního kódu */

/* ###zt začátek apletovského kódu
        // This is a hack to avoid an ugly error message in 1.1.
                getRootPane().putClientProperty("defeatSystemEventQueueCheck",
                                        Boolean.TRUE);
                jaaa = this; // pro vnořené třídy
        user_account = new UserAccount(this, i18n);

 /* ###kt konec apletovského kódu */
    }

    /**
     * The entering point of the whole program.
      * @param args
     */
/* ###zk začátek aplikačního kódu */
    public static void main(String [] args) {
        parametry = args; // zachovám si parametry
        pocet_parametru = parametry.length; // počet parametrů
        NGClient app = new NGClient();
    }
/* ###kk konec aplikačního kódu */

    /**
     * Reads a program parameter.
     * @param name a name of the parameter
     * @return the value of the parameter
     */
/* ###zk začátek aplikačního kódu */
    String getParameter(String name) { // náhrada funkcí třídy JApplet - čtení parametrů programu
        int i;
        for (i = 0; i< pocet_parametru; i++) { // hledám správný parametr mezi všemi parametry programu
            if (parametry[i].startsWith(name)) { // našel jsem parametr
                int zacatek_hodnoty = parametry[i].indexOf("=") + 1; // najdu začátek hodnoty
                return new String(parametry[i].substring(zacatek_hodnoty, parametry[i].length()));
            }
        }
        return null;
    }
/* ###kk konec aplikačního kódu */

    /**
     * Initializes the application. Called from the constructor.
     */
    public void init() {

        // čtení parametrů lokalizace


        language = null;
        country = null;

        language = getParameter("lang");
        country = getParameter("country");

        if (language == null) {
            language = "en"; // implicitní lokalizace
            country = "US";
        }

        if (language.equalsIgnoreCase("en") && country == null) country = "US";
        if (language.equalsIgnoreCase("cs") && country == null) country = "CZ";
        if (country == null) country = new String();

        System.out.println("lang = " + language);
        System.out.println("country = " + country);

        // inicializace lokalizace

        currentLocale = new Locale(language, country);
        i18n = ResourceBundle.getBundle("PrekladZprav", currentLocale); // nastavení souboru se zprávami

        // vytvoření grafického rozhraní - záložky a info lišta a menu

        Container hlavni_plocha = getContentPane();
        hlavni_plocha.setLayout(new BorderLayout());
        hlavni_zalozky = new JTabbedPane(JTabbedPane.BOTTOM); // zalozky hlavnich ploch
        zalozka_files = new PanelFiles(this,this,i18n); // záložka pro výběr souborů
        hlavni_zalozky.addTab(i18n.getString("FILES_SELECTION"), zalozka_files);
        zalozka_query = new PanelQuery(this,this,i18n); // záložka pro kladení dotazů
        hlavni_zalozky.addTab(i18n.getString("QUERY_SETTING"), zalozka_query);
        zalozka_trees = new PanelTrees(this,this,i18n);  // záložka pro prohlížení stromů
        hlavni_zalozky.addTab(i18n.getString("TREES_VIEWING"), zalozka_trees);
        zalozka_debug = new PanelDebug(i18n);  // záložka pro ladicí informace
        hlavni_zalozky.addTab(i18n.getString("DEBUG_INFORMATIONS"), zalozka_debug);
        info_bar = new InfoBar();
        info_bar.setBorder(BorderFactory.createEtchedBorder());
        info_bar.setVisible(use_internal_info_bar);
        hlavni_plocha.add(hlavni_zalozky, BorderLayout.CENTER);
        hlavni_plocha.add(info_bar, BorderLayout.SOUTH);



        // hlavní menu:

        menu_bar = new JMenuBar();

        JMenu menu_soubor = new JMenu(i18n.getString("MENU_FILE"));
        menu_soubor_connect = new JMenuItem(i18n.getString("MENU_FILE_CONNECT"));
        menu_soubor_connect.addActionListener(this);
        menu_soubor_disconnect = new JMenuItem(i18n.getString("MENU_FILE_DISCONNECT"));
        menu_soubor_disconnect.addActionListener(this);
/* ###zk začátek aplikačního kódu */
        menu_soubor_change_password = new JMenuItem(i18n.getString("MENU_FILE_CHANGE_PASSWORD"));
        menu_soubor_change_password.addActionListener(this);
        menu_soubor_change_password.setEnabled(false); // jedině úspěšným nalogováním uživatele s příslušným právem se toto může změnit
        menu_soubor_save_trees = new JMenuItem(i18n.getString("MENU_FILE_SAVE_TREES"));
        menu_soubor_save_trees.addActionListener(this);
        menu_soubor_save_trees.setEnabled(false); // jedině úspěšným nalogováním uživatele s příslušným právem se toto může změnit
        menu_soubor_print = new JMenuItem(i18n.getString("MENU_FILE_PRINT"));
        menu_soubor_print.addActionListener(this);
        menu_soubor_exit = new JMenuItem(i18n.getString("MENU_FILE_EXIT"));
        menu_soubor_exit.addActionListener(this);
/* ###kk konec aplikačního kódu */
        menu_soubor.add(menu_soubor_connect);
        menu_soubor.add(menu_soubor_disconnect);
/* ###zk začátek aplikačního kódu */

        menu_soubor.addSeparator();
        menu_soubor.add(menu_soubor_change_password);
        menu_soubor.addSeparator();
        menu_soubor.add(menu_soubor_save_trees);
        menu_soubor.add(menu_soubor_print);
        menu_soubor.addSeparator();
        menu_soubor.add(menu_soubor_exit);
/* ###kk konec aplikačního kódu */

        JMenu menu_zobrazeni = new JMenu(i18n.getString("MENU_VIEW"));
        menu_zobrazeni_internal_info = new JCheckBoxMenuItem(i18n.getString("MENU_VIEW_INTERNAL_INFO"),use_internal_info_bar);
        menu_zobrazeni_internal_info.addActionListener(this);
/* ###zt začátek apletovského kódu
                        menu_zobrazeni_external_info = new JCheckBoxMenuItem (i18n.getString("MENU_VIEW_EXTERNAL_INFO"),use_external_info_bar);
                        menu_zobrazeni_external_info.addActionListener(this);
/* ###kt konec apletovského kódu */
        menu_zobrazeni.add(menu_zobrazeni_internal_info);
/* ###zt začátek apletovského kódu
                menu_zobrazeni.add (menu_zobrazeni_external_info);
/* ###kt konec apletovského kódu */

        JMenu menu_options = new JMenu(i18n.getString("MENU_OPTIONS"));
        JMenu menu_options_coding = new JMenu(i18n.getString("MENU_OPTIONS_CODING"));
        JMenu menu_options_coding_query = new JMenu(i18n.getString("MENU_OPTIONS_CODING_QUERY"));
        ButtonGroup query_coding_group = new ButtonGroup();
        rbutton_query_coding_unicode = new JRadioButtonMenuItem(i18n.getString("UNICODE"));
        rbutton_query_coding_pseudo = new JRadioButtonMenuItem(i18n.getString("ASCII(PSEUDO)"));
        query_coding_group.add(rbutton_query_coding_unicode);
        query_coding_group.add(rbutton_query_coding_pseudo);
        rbutton_query_coding_unicode.setSelected(coding_in_queries == CharCode.coding_unicode);
        rbutton_query_coding_pseudo.setSelected(coding_in_queries == CharCode.coding_pseudo);
        rbutton_query_coding_unicode.addActionListener(this);
        rbutton_query_coding_pseudo.addActionListener(this);
        menu_options_coding_query.add(rbutton_query_coding_unicode);
        menu_options_coding_query.add(rbutton_query_coding_pseudo);
        menu_options_coding.add(menu_options_coding_query);

        JMenu menu_options_fontsize = new JMenu(i18n.getString("MENU_OPTIONS_FONTSIZE"));

        JMenu menu_options_fontsize_query = new JMenu(i18n.getString("MENU_OPTIONS_FONTSIZE_QUERY"));
        ButtonGroup fontsize_query_group = new ButtonGroup();
        menu_options_fontsize_query_6 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_6"));
        menu_options_fontsize_query_8 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_8"));
        menu_options_fontsize_query_10 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_10"));
        menu_options_fontsize_query_12 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_12"));
        menu_options_fontsize_query_14 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_14"));
        menu_options_fontsize_query_16 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_16"));
        menu_options_fontsize_query_18 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_18"));
        menu_options_fontsize_query_20 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_20"));
        menu_options_fontsize_query_22 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_22"));
        menu_options_fontsize_query_24 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_24"));
        menu_options_fontsize_query_26 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_26"));
        menu_options_fontsize_query_28 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_28"));
        menu_options_fontsize_query_30 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_30"));
        menu_options_fontsize_query_32 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_32"));
        menu_options_fontsize_query_6.addActionListener(this);
        menu_options_fontsize_query_8.addActionListener(this);
        menu_options_fontsize_query_10.addActionListener(this);
        menu_options_fontsize_query_12.addActionListener(this);
        menu_options_fontsize_query_14.addActionListener(this);
        menu_options_fontsize_query_16.addActionListener(this);
        menu_options_fontsize_query_18.addActionListener(this);
        menu_options_fontsize_query_20.addActionListener(this);
        menu_options_fontsize_query_22.addActionListener(this);
        menu_options_fontsize_query_24.addActionListener(this);
        menu_options_fontsize_query_26.addActionListener(this);
        menu_options_fontsize_query_28.addActionListener(this);
        menu_options_fontsize_query_30.addActionListener(this);
        menu_options_fontsize_query_32.addActionListener(this);
        fontsize_query_group.add(menu_options_fontsize_query_6);
        fontsize_query_group.add(menu_options_fontsize_query_8);
        fontsize_query_group.add(menu_options_fontsize_query_10);
        fontsize_query_group.add(menu_options_fontsize_query_12);
        fontsize_query_group.add(menu_options_fontsize_query_14);
        fontsize_query_group.add(menu_options_fontsize_query_16);
        fontsize_query_group.add(menu_options_fontsize_query_18);
        fontsize_query_group.add(menu_options_fontsize_query_20);
        fontsize_query_group.add(menu_options_fontsize_query_22);
        fontsize_query_group.add(menu_options_fontsize_query_24);
        fontsize_query_group.add(menu_options_fontsize_query_26);
        fontsize_query_group.add(menu_options_fontsize_query_28);
        fontsize_query_group.add(menu_options_fontsize_query_30);
        fontsize_query_group.add(menu_options_fontsize_query_32);
        menu_options_fontsize_query_12.setSelected(true);
        menu_options_fontsize_query.add(menu_options_fontsize_query_6);
        menu_options_fontsize_query.add(menu_options_fontsize_query_8);
        menu_options_fontsize_query.add(menu_options_fontsize_query_10);
        menu_options_fontsize_query.add(menu_options_fontsize_query_12);
        menu_options_fontsize_query.add(menu_options_fontsize_query_14);
        menu_options_fontsize_query.add(menu_options_fontsize_query_16);
        menu_options_fontsize_query.add(menu_options_fontsize_query_18);
        menu_options_fontsize_query.add(menu_options_fontsize_query_20);
        menu_options_fontsize_query.add(menu_options_fontsize_query_22);
        menu_options_fontsize_query.add(menu_options_fontsize_query_24);
        menu_options_fontsize_query.add(menu_options_fontsize_query_26);
        menu_options_fontsize_query.add(menu_options_fontsize_query_28);
        menu_options_fontsize_query.add(menu_options_fontsize_query_30);
        menu_options_fontsize_query.add(menu_options_fontsize_query_32);

        JMenu menu_options_fontsize_result = new JMenu(i18n.getString("MENU_OPTIONS_FONTSIZE_RESULT"));
        ButtonGroup fontsize_result_group = new ButtonGroup();
        menu_options_fontsize_result_6 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_6"));
        menu_options_fontsize_result_8 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_8"));
        menu_options_fontsize_result_10 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_10"));
        menu_options_fontsize_result_12 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_12"));
        menu_options_fontsize_result_14 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_14"));
        menu_options_fontsize_result_16 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_16"));
        menu_options_fontsize_result_18 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_18"));
        menu_options_fontsize_result_20 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_20"));
        menu_options_fontsize_result_22 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_22"));
        menu_options_fontsize_result_24 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_24"));
        menu_options_fontsize_result_26 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_26"));
        menu_options_fontsize_result_28 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_28"));
        menu_options_fontsize_result_30 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_30"));
        menu_options_fontsize_result_32 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_32"));
        menu_options_fontsize_result_6.addActionListener(this);
        menu_options_fontsize_result_8.addActionListener(this);
        menu_options_fontsize_result_10.addActionListener(this);
        menu_options_fontsize_result_12.addActionListener(this);
        menu_options_fontsize_result_14.addActionListener(this);
        menu_options_fontsize_result_16.addActionListener(this);
        menu_options_fontsize_result_18.addActionListener(this);
        menu_options_fontsize_result_20.addActionListener(this);
        menu_options_fontsize_result_22.addActionListener(this);
        menu_options_fontsize_result_24.addActionListener(this);
        menu_options_fontsize_result_26.addActionListener(this);
        menu_options_fontsize_result_28.addActionListener(this);
        menu_options_fontsize_result_30.addActionListener(this);
        menu_options_fontsize_result_32.addActionListener(this);
        fontsize_result_group.add(menu_options_fontsize_result_6);
        fontsize_result_group.add(menu_options_fontsize_result_8);
        fontsize_result_group.add(menu_options_fontsize_result_10);
        fontsize_result_group.add(menu_options_fontsize_result_12);
        fontsize_result_group.add(menu_options_fontsize_result_14);
        fontsize_result_group.add(menu_options_fontsize_result_16);
        fontsize_result_group.add(menu_options_fontsize_result_18);
        fontsize_result_group.add(menu_options_fontsize_result_20);
        fontsize_result_group.add(menu_options_fontsize_result_22);
        fontsize_result_group.add(menu_options_fontsize_result_24);
        fontsize_result_group.add(menu_options_fontsize_result_26);
        fontsize_result_group.add(menu_options_fontsize_result_28);
        fontsize_result_group.add(menu_options_fontsize_result_30);
        fontsize_result_group.add(menu_options_fontsize_result_32);
        menu_options_fontsize_result_12.setSelected(true);
        menu_options_fontsize_result.add(menu_options_fontsize_result_6);
        menu_options_fontsize_result.add(menu_options_fontsize_result_8);
        menu_options_fontsize_result.add(menu_options_fontsize_result_10);
        menu_options_fontsize_result.add(menu_options_fontsize_result_12);
        menu_options_fontsize_result.add(menu_options_fontsize_result_14);
        menu_options_fontsize_result.add(menu_options_fontsize_result_16);
        menu_options_fontsize_result.add(menu_options_fontsize_result_18);
        menu_options_fontsize_result.add(menu_options_fontsize_result_20);
        menu_options_fontsize_result.add(menu_options_fontsize_result_22);
        menu_options_fontsize_result.add(menu_options_fontsize_result_24);
        menu_options_fontsize_result.add(menu_options_fontsize_result_26);
        menu_options_fontsize_result.add(menu_options_fontsize_result_28);
        menu_options_fontsize_result.add(menu_options_fontsize_result_30);
        menu_options_fontsize_result.add(menu_options_fontsize_result_32);

        JMenu menu_options_fontsize_sentence = new JMenu(i18n.getString("MENU_OPTIONS_FONTSIZE_SENTENCE"));
        ButtonGroup fontsize_sentence_group = new ButtonGroup();
        menu_options_fontsize_sentence_6 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_6"));
        menu_options_fontsize_sentence_8 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_8"));
        menu_options_fontsize_sentence_10 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_10"));
        menu_options_fontsize_sentence_12 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_12"));
        menu_options_fontsize_sentence_14 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_14"));
        menu_options_fontsize_sentence_16 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_16"));
        menu_options_fontsize_sentence_18 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_18"));
        menu_options_fontsize_sentence_20 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_20"));
        menu_options_fontsize_sentence_22 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_22"));
        menu_options_fontsize_sentence_24 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_24"));
        menu_options_fontsize_sentence_26 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_26"));
        menu_options_fontsize_sentence_28 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_28"));
        menu_options_fontsize_sentence_30 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_30"));
        menu_options_fontsize_sentence_32 = new JRadioButtonMenuItem(i18n.getString("FONTSIZE_32"));
        menu_options_fontsize_sentence_6.addActionListener(this);
        menu_options_fontsize_sentence_8.addActionListener(this);
        menu_options_fontsize_sentence_10.addActionListener(this);
        menu_options_fontsize_sentence_12.addActionListener(this);
        menu_options_fontsize_sentence_14.addActionListener(this);
        menu_options_fontsize_sentence_16.addActionListener(this);
        menu_options_fontsize_sentence_18.addActionListener(this);
        menu_options_fontsize_sentence_20.addActionListener(this);
        menu_options_fontsize_sentence_22.addActionListener(this);
        menu_options_fontsize_sentence_24.addActionListener(this);
        menu_options_fontsize_sentence_26.addActionListener(this);
        menu_options_fontsize_sentence_28.addActionListener(this);
        menu_options_fontsize_sentence_30.addActionListener(this);
        menu_options_fontsize_sentence_32.addActionListener(this);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_6);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_8);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_10);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_12);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_14);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_16);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_18);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_20);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_22);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_24);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_26);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_28);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_30);
        fontsize_sentence_group.add(menu_options_fontsize_sentence_32);
        menu_options_fontsize_sentence_12.setSelected(true);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_6);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_8);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_10);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_12);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_14);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_16);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_18);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_20);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_22);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_24);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_26);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_28);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_30);
        menu_options_fontsize_sentence.add(menu_options_fontsize_sentence_32);

        menu_options_fontsize.add(menu_options_fontsize_query);
        menu_options_fontsize.add(menu_options_fontsize_result);
        menu_options_fontsize.add(menu_options_fontsize_sentence);

        JMenu menu_options_lemma = new JMenu(i18n.getString("MENU_OPTIONS_LEMMA"));
        JMenu menu_options_lemma_variants = new JMenu(i18n.getString("MENU_OPTIONS_LEMMA_VARIANTS"));
        menu_options_lemma_variants_match = new JCheckBoxMenuItem(i18n.getString("MENU_OPTIONS_LEMMA_VARIANTS_MATCH"), lemma_variants_match);
        menu_options_lemma_variants_match.addActionListener(this);
        menu_options_lemma_variants_show = new JCheckBoxMenuItem(i18n.getString("MENU_OPTIONS_LEMMA_VARIANTS_SHOW"), lemma_variants_show);
        menu_options_lemma_variants_show.addActionListener(this);
        menu_options_lemma_variants.add(menu_options_lemma_variants_match);
        menu_options_lemma_variants.add(menu_options_lemma_variants_show);
        JMenu menu_options_lemma_comments = new JMenu(i18n.getString("MENU_OPTIONS_LEMMA_COMMENTS"));
        menu_options_lemma_comments_match = new JCheckBoxMenuItem(i18n.getString("MENU_OPTIONS_LEMMA_COMMENTS_MATCH"), lemma_comments_match);
        menu_options_lemma_comments_match.addActionListener(this);
        menu_options_lemma_comments_show = new JCheckBoxMenuItem(i18n.getString("MENU_OPTIONS_LEMMA_COMMENTS_SHOW"), lemma_comments_show);
        menu_options_lemma_comments_show.addActionListener(this);
        menu_options_lemma_comments.add(menu_options_lemma_comments_match);
        menu_options_lemma_comments.add(menu_options_lemma_comments_show);
        menu_options_lemma.add(menu_options_lemma_variants);
        menu_options_lemma.add(menu_options_lemma_comments);
        JMenu menu_options_order = new JMenu(i18n.getString("MENU_OPTIONS_ORDER"));
        JMenu menu_options_order_nodes = new JMenu(i18n.getString("MENU_OPTIONS_ORDER_NODES"));
        //JMenu menu_options_order_words = new JMenu(i18n.getString("MENU_OPTIONS_ORDER_WORDS"));
        ButtonGroup nodes_ordering_group = new ButtonGroup();
        //ButtonGroup words_ordering_group = new ButtonGroup();
        rbutton_order_nodes_left_right = new JRadioButtonMenuItem(i18n.getString("MENU_OPTIONS_ORDER_LEFT_RIGHT"));
        rbutton_order_nodes_right_left = new JRadioButtonMenuItem(i18n.getString("MENU_OPTIONS_ORDER_RIGHT_LEFT"));
        //rbutton_order_words_left_right = new JRadioButtonMenuItem(i18n.getString("MENU_OPTIONS_ORDER_LEFT_RIGHT"));
        //rbutton_order_words_right_left = new JRadioButtonMenuItem(i18n.getString("MENU_OPTIONS_ORDER_RIGHT_LEFT"));
        nodes_ordering_group.add(rbutton_order_nodes_left_right);
        nodes_ordering_group.add(rbutton_order_nodes_right_left);
        //words_ordering_group.add(rbutton_order_words_left_right);
        //words_ordering_group.add(rbutton_order_words_right_left);
	rbutton_order_nodes_left_right.setSelected(nodes_ordering_in_trees == NGTreeProperties.DIRECTION_LEFT_RIGHT);
	rbutton_order_nodes_right_left.setSelected(nodes_ordering_in_trees == NGTreeProperties.DIRECTION_RIGHT_LEFT);
	//rbutton_order_words_left_right.setSelected(words_ordering_in_sentences == NGTreeProperties.DIRECTION_LEFT_RIGHT);
	//rbutton_order_words_right_left.setSelected(words_ordering_in_sentences == NGTreeProperties.DIRECTION_RIGHT_LEFT);
  	menu_options_order_nodes.add(rbutton_order_nodes_left_right);
	menu_options_order_nodes.add(rbutton_order_nodes_right_left);
	//menu_options_order_words.add(rbutton_order_words_left_right);
	//menu_options_order_words.add(rbutton_order_words_right_left);
	//menu_options_order.add(menu_options_order_words);
	menu_options_order.add(menu_options_order_nodes);
	rbutton_order_nodes_left_right.addActionListener(this);
	rbutton_order_nodes_right_left.addActionListener(this);
	//rbutton_order_words_left_right.addActionListener(this);
	//rbutton_order_words_right_left.addActionListener(this);
        menu_options.add(menu_options_coding);
        menu_options.add(menu_options_fontsize);
        menu_options.add(menu_options_lemma);
        menu_options.add(menu_options_order);

/* ###zk začátek aplikačního kódu */
        JMenu menu_tools = new JMenu(i18n.getString("MENU_TOOLS"));
        JMenu menu_tools_external_command = new JMenu(i18n.getString("MENU_TOOLS_EXTERNAL_COMMAND"));
        menu_tools_external_command_start = new JMenuItem(i18n.getString("MENU_TOOLS_EXTERNAL_COMMAND_START"), KeyEvent.VK_F9);
        menu_tools_external_command_start.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,0));
        menu_tools_external_command_edit = new JMenuItem(i18n.getString("MENU_TOOLS_EXTERNAL_COMMAND_EDIT"));
        menu_tools_external_command.add(menu_tools_external_command_start);
        menu_tools_external_command.add(menu_tools_external_command_edit);
        menu_tools.add(menu_tools_external_command);
        menu_tools_external_command_start.addActionListener(this);
        menu_tools_external_command_edit.addActionListener(this);
/* ###kk konec aplikačního kódu */

        JMenu menu_help = new JMenu(i18n.getString("MENU_HELP"));
        menu_help_manual = new JMenuItem(i18n.getString("MENU_HELP_MANUAL"));
        menu_help_manual.addActionListener(this);
        menu_help_changelog = new JMenuItem(i18n.getString("MENU_HELP_CHANGELOG"));
        menu_help_changelog.addActionListener(this);
        menu_help_home_page = new JMenuItem(i18n.getString("MENU_HELP_HOME_PAGE"));
        menu_help_home_page.addActionListener(this);
        menu_help.add(menu_help_manual);
        menu_help.add(menu_help_changelog);
        menu_help.add(menu_help_home_page);

        menu_bar.add(menu_soubor);
        menu_bar.add(menu_zobrazeni);
        menu_bar.add(menu_options);
/* ###zk začátek aplikačního kódu */
        menu_bar.add(menu_tools);
/* ###kk konec aplikačního kódu */
        menu_bar.add(menu_help);

        menu_bar.setBorder(BorderFactory.createRaisedBevelBorder());

        hlavni_plocha.add(menu_bar, BorderLayout.NORTH);


        // čtení parametrů

        String param_c = null;
        debug("\nNGClient: init(): Načítám parametr programu: c_port (číslo portu, který klient osloví) ... ");
        param_c = getParameter("c_port");
        if( param_c != null ) {
            Integer pom = new Integer(param_c);
            param_port = pom.intValue();
            param_port_specified=true; // tento parametr byl z příkazové řádky dán
            debug(param_c);
        }
        else {
            debug("nespecifikován");
            param_port = server_default_port; // implicitní hodnota nespecifikovaného portu
            param_port_specified=false; // tento parametr nebyl dán z příkazové řádky
        }

        System.out.println("c_port = " + param_port);


        param_adresa = null;
        debug("\nNGClient: init(): Načítám parametr programu: server (adresa serveru) ... ");
        param_adresa = getParameter( "server" );
        if (param_adresa == null) {
            debug("nespecifikován");
            param_adresa = server_default_name;
            param_adresa_specified=false; // tento parametr nebyl dán z příkazové řádky
        }
        else {
            debug(param_adresa);
            param_adresa_specified=true; // tento parametr byl dán z příkazové řádky
        }
        System.out.println("server = " + param_adresa);

        String param_client_install_url = null; // adresa, kde je nainstalován klient (např. pro hledání dokumentace)
        debug("\nNGClient: init(): Načítám parametr programu: client_install_url (adresa, kde je nainstalován klient - aplet (a tedy i dokumentace)) ... ");
        param_client_install_url = getParameter( "client_install_url" );
        if (param_client_install_url == null) {
            client_install_url = new String("quest.ms.mff.cuni.cz/netgraph");
            debug("nespecifikován - použiji default: " + client_install_url);
        }
        else {
            client_install_url = param_client_install_url;
            debug(client_install_url);
        }

        System.out.println("client_install_url = " + client_install_url);

        // konec čtení parametrů

        zalozka_trees.strom_view.getTreeProperties().setDirection(nodes_ordering_in_trees);
/* ###zk začátek aplikačního kódu */
        zalozka_trees.forest_print.getTreeProperties().setDirection(nodes_ordering_in_trees);
/* ###kk konec aplikačního kódu */
	    zalozka_query.query_forest_view.getTreeProperties().setDirection(nodes_ordering_in_trees);


/* ###zt začátek apletovského kódu
        connectToServer();
/* ###kt konec apletovského kódu */
/*    debug ("\nPokouším se o spojení se serverem " + param_adresa + " na portu " + param_port);
    kom_net = new ServerNetCommunication (client_version, param_port, param_adresa, this);  // vytvoření spojení se serverem; nastaví proměnnou connected
    zalozka_files.naplnAdresareASoubory();  // naplni nektere prvky formulare pro vyber souboru s daty: aktualni cestu, podadresare a soubory v akt. ceste
        applet_context = getAppletContext(); // pro přístup k prohlížeči - např. otvírání okna s nápovědou
        if (applet_context == null) debug ("\nFunkce getAppletContext() vrátila null! Nebude možno zobrazit nápovědu!");
        if (kom_net.isConnected()) {
                zalozka_files.naplnServerInfo(); // naplní informace o připojeném serveru
                inform ("CONNECTION_OK");
                checkVersionsMatching();
        }
        else {
                zalozka_files.vyprazdniServerInfo();
                inform ("CONNECTION_KO");
        }
 */


    } // init / main


    // odchycení událostí


/* ###zk začátek aplikačního kódu */
    public void windowClosing(WindowEvent e) { // reakce na zavření okna uživatelem
        exitNetgraphClient();
    }

    public void windowDeactivated(WindowEvent e) {return;}
    public void windowActivated(WindowEvent e) {return;}
    public void windowDeiconified(WindowEvent e) {return;}
    public void windowIconified(WindowEvent e) {return;}
    public void windowClosed(WindowEvent e) {return;}
    public void windowOpened(WindowEvent e) {return;}
/* ###kk konec aplikačního kódu */

    public void actionPerformed(ActionEvent e) {
        Object zdroj = e.getSource();
        if (zdroj == menu_soubor_connect) {
            setWaitCursor();
            //debug ("\nVybrána položka 'connect' menu 'soubor'");
            connectToServer();
            setDefaultCursor();
        }
        else if (zdroj == menu_soubor_disconnect) {
            //debug ("\nVybrána položka 'disconnect' menu 'soubor'");
            setWaitCursor();
            disconnectFromServer();
            hlavni_zalozky.setSelectedIndex(0); // zobrazeni zalozky souboru
            setDefaultCursor();
        }
/* ###zk začátek aplikačního kódu */
        else if (zdroj == menu_soubor_change_password) {
            setWaitCursor();
            // debug ("\nVybrána položka 'change password' menu 'soubor'");
            changePassword(false); // funkce pro změnu hesla
            setDefaultCursor();
        }
        else if (zdroj == menu_soubor_save_trees) {
            setWaitCursor();
            //debug ("\nVybrána položka 'save' menu 'soubor'");
            zalozka_trees.saveResultDialog(); // uložit výsledné stromy
            setDefaultCursor();
        }
        else if (zdroj == menu_soubor_print) {
            setWaitCursor();
            //debug ("\nVybrána položka 'print' menu 'soubor'");
            int selected_tab = hlavni_zalozky.getSelectedIndex();
            //if (selected_tab == 1) { // jsem v záložce dotazu
            //    zalozka_query.printQuery(true); // zobrazit nejprve dialogové okno
            //}
            // else 
            if (selected_tab == 2) { // jsem v záložce stromů
                zalozka_trees.printTree(true); // zobrazit nejprve dialogové okno
            }
            setDefaultCursor();
        }
        else if (zdroj == menu_soubor_exit) { // ukončení aplikace
            //debug ("\nVybrána položka 'exit' menu 'soubor'");
            exitNetgraphClient();
        }
/* ###kk konec aplikačního kódu */
        // výběr informační lišty
        else if (zdroj == menu_zobrazeni_internal_info) {
            //debug ("\nPoužití interní lišty pro informační hlášky ");
            if (menu_zobrazeni_internal_info.getState()) { // použít
                //debug ("zapnuto");
                use_internal_info_bar = true;
            }
            else { // nepoužít
                //debug ("vypnuto");
                use_internal_info_bar = false;
            }
            info_bar.setVisible(use_internal_info_bar);
        }
/* ###zt začátek apletovského kódu
                else if (zdroj == menu_zobrazeni_external_info) {
                //debug ("\nPoužití externí lišty (webovského prohlížeče) pro informační hlášky ");
                if (menu_zobrazeni_external_info.getState()) { // použít
                                        //debug ("zapnuto");
                                        use_external_info_bar = true;
                                }
                                else { // nepoužít
                                        //debug ("vypnuto");
                                use_external_info_bar = false;
                                }
                }
/* ###kt konec apletovského kódu */
        // kódování češtiny v dotazech
        else if (zdroj == rbutton_query_coding_unicode) {
            //debug ("\nVybráno unicode kódování znaků v dotazech");
            coding_in_queries = CharCode.coding_unicode;
        }
        else if (zdroj == rbutton_query_coding_pseudo) {
            //debug ("\nVybráno pseudo kódování znaků v dotazech");
            coding_in_queries = CharCode.coding_pseudo;
        }
        // velikost fontu v dotazech
        else if (zdroj == menu_options_fontsize_query_6) {
            setFontsizeQuery(6);
        }
        else if (zdroj == menu_options_fontsize_query_8) {
            setFontsizeQuery(8);
        }
        else if (zdroj == menu_options_fontsize_query_10) {
            setFontsizeQuery(10);
        }
        else if (zdroj == menu_options_fontsize_query_12) {
            setFontsizeQuery(12);
        }
        else if (zdroj == menu_options_fontsize_query_14) {
            setFontsizeQuery(14);
        }
        else if (zdroj == menu_options_fontsize_query_16) {
            setFontsizeQuery(16);
        }
        else if (zdroj == menu_options_fontsize_query_18) {
            setFontsizeQuery(18);
        }
        else if (zdroj == menu_options_fontsize_query_20) {
            setFontsizeQuery(20);
        }
        else if (zdroj == menu_options_fontsize_query_22) {
            setFontsizeQuery(22);
        }
        else if (zdroj == menu_options_fontsize_query_24) {
            setFontsizeQuery(24);
        }
        else if (zdroj == menu_options_fontsize_query_26) {
            setFontsizeQuery(26);
        }
        else if (zdroj == menu_options_fontsize_query_28) {
            setFontsizeQuery(28);
        }
        else if (zdroj == menu_options_fontsize_query_30) {
            setFontsizeQuery(30);
        }
        else if (zdroj == menu_options_fontsize_query_32) {
            setFontsizeQuery(32);
        }
        // velikost fontu ve výsledcích
        else if (zdroj == menu_options_fontsize_result_6) {
            setFontsizeResult(6);
        }
        else if (zdroj == menu_options_fontsize_result_8) {
            setFontsizeResult(8);
        }
        else if (zdroj == menu_options_fontsize_result_10) {
            setFontsizeResult(10);
        }
        else if (zdroj == menu_options_fontsize_result_12) {
            setFontsizeResult(12);
        }
        else if (zdroj == menu_options_fontsize_result_14) {
            setFontsizeResult(14);
        }
        else if (zdroj == menu_options_fontsize_result_16) {
            setFontsizeResult(16);
        }
        else if (zdroj == menu_options_fontsize_result_18) {
            setFontsizeResult(18);
        }
        else if (zdroj == menu_options_fontsize_result_20) {
            setFontsizeResult(20);
        }
        else if (zdroj == menu_options_fontsize_result_22) {
            setFontsizeResult(22);
        }
        else if (zdroj == menu_options_fontsize_result_24) {
            setFontsizeResult(24);
        }
        else if (zdroj == menu_options_fontsize_result_26) {
            setFontsizeResult(26);
        }
        else if (zdroj == menu_options_fontsize_result_28) {
            setFontsizeResult(28);
        }
        else if (zdroj == menu_options_fontsize_result_30) {
            setFontsizeResult(30);
        }
        else if (zdroj == menu_options_fontsize_result_32) {
            setFontsizeResult(32);
        }
        // velikost fontu ve větách
        else if (zdroj == menu_options_fontsize_sentence_6) {
            setFontsizeSentence(6);
        }
        else if (zdroj == menu_options_fontsize_sentence_8) {
            setFontsizeSentence(8);
        }
        else if (zdroj == menu_options_fontsize_sentence_10) {
            setFontsizeSentence(10);
        }
        else if (zdroj == menu_options_fontsize_sentence_12) {
            setFontsizeSentence(12);
        }
        else if (zdroj == menu_options_fontsize_sentence_14) {
            setFontsizeSentence(14);
        }
        else if (zdroj == menu_options_fontsize_sentence_16) {
            setFontsizeSentence(16);
        }
        else if (zdroj == menu_options_fontsize_sentence_18) {
            setFontsizeSentence(18);
        }
        else if (zdroj == menu_options_fontsize_sentence_20) {
            setFontsizeSentence(20);
        }
        else if (zdroj == menu_options_fontsize_sentence_22) {
            setFontsizeSentence(22);
        }
        else if (zdroj == menu_options_fontsize_sentence_24) {
            setFontsizeSentence(24);
        }
        else if (zdroj == menu_options_fontsize_sentence_26) {
            setFontsizeSentence(26);
        }
        else if (zdroj == menu_options_fontsize_sentence_28) {
            setFontsizeSentence(28);
        }
        else if (zdroj == menu_options_fontsize_sentence_30) {
            setFontsizeSentence(30);
        }
        else if (zdroj == menu_options_fontsize_sentence_32) {
            setFontsizeSentence(32);
        }
        // automatické vyhledávání variant lemmat
        else if (zdroj == menu_options_lemma_variants_match) {
            //debug ("\nAutomatické vyhledávání variant lemmat");
            if (menu_options_lemma_variants_match.getState()) { // použít
                //debug (" zapnuto");
                lemma_variants_match = true;
                if (! lemma_comments_match) { // povolí-li se matchování variant, musí se povolit i matchování komentářů
                    menu_options_lemma_comments_match.setState(true);
                    lemma_comments_match = true;
                }
            }
            else { // nepoužít
                //debug (" vypnuto");
                lemma_variants_match = false;
            }
        }
        // zobrazování variant lemmat
        else if (zdroj == menu_options_lemma_variants_show) {
            //debug ("\nZobrazování variant lemmat");
            if (menu_options_lemma_variants_show.getState()) { // použít
                //debug (" zapnuto");
                lemma_variants_show = true;
                zalozka_trees.strom_view.getTreeProperties().setShowLemmaVariants(true);
/* ###zk začátek aplikačního kódu */
                zalozka_trees.forest_print.getTreeProperties().setShowLemmaVariants(true);
/* ###kk konec aplikačního kódu */
                zalozka_trees.strom_view.setFlagWholeForestChanged(true);
                zalozka_trees.strom_view.repaint();
            }
            else { // nepoužít
                //debug (" vypnuto");
                lemma_variants_show = false;
                if (lemma_comments_show) { // nemají-li se zobrazovat varianty, nemohou se zobrazovat ani vysvětlivky
                    menu_options_lemma_comments_show.setState(false);
                    lemma_comments_show = false;
                }
                zalozka_trees.strom_view.getTreeProperties().setShowLemmaVariants(false);
/* ###zk začátek aplikačního kódu */
                zalozka_trees.forest_print.getTreeProperties().setShowLemmaVariants(false);
/* ###kk konec aplikačního kódu */
                zalozka_trees.strom_view.setFlagWholeForestChanged(true);
                zalozka_trees.strom_view.repaint();
            }
        }
        // automatické vyhledávání komentářů lemmat
        else if (zdroj == menu_options_lemma_comments_match) {
            //debug ("\nAutomatické vyhledávání komentářů lemmat");
            if (menu_options_lemma_comments_match.getState()) { // použít
                //debug (" zapnuto");
                lemma_comments_match = true;
            }
            else { // nepoužít
                //debug (" vypnuto");
                lemma_comments_match = false;
                if (lemma_variants_match) { // nemají-li matchovat komentáře, nemohou matchovat ani varianty
                    menu_options_lemma_variants_match.setState(false);
                    lemma_variants_match = false;
                }
            }
        }
        // zobrazování komentářů lemmat
        else if (zdroj == menu_options_lemma_comments_show) {
            //debug ("\nZobrazování komentářů lemmat");
            if (menu_options_lemma_comments_show.getState()) { // použít
                //debug (" zapnuto");
                lemma_comments_show = true;
                if (! lemma_variants_show) { // mají-li se zobrazovat komentáře, musejí se zobrazovat i varianty
                    menu_options_lemma_variants_show.setState(true);
                    lemma_variants_show = true;
                }
                zalozka_trees.strom_view.getTreeProperties().setShowLemmaComments(true);
/* ###zk začátek aplikačního kódu */
                zalozka_trees.forest_print.getTreeProperties().setShowLemmaComments(true);
/* ###kk konec aplikačního kódu */
                zalozka_trees.strom_view.setFlagWholeForestChanged(true);
                zalozka_trees.strom_view.repaint();
            }
            else { // nepoužít
                //debug (" vypnuto");
                lemma_comments_show = false;
                zalozka_trees.strom_view.getTreeProperties().setShowLemmaComments(false);
/* ###zk začátek aplikačního kódu */
                zalozka_trees.forest_print.getTreeProperties().setShowLemmaComments(false);
/* ###kk konec aplikačního kódu */
                zalozka_trees.strom_view.setFlagWholeForestChanged(true);
                zalozka_trees.strom_view.repaint();
            }
        }
        // řazení uzlů ve stromech
        else if (zdroj == rbutton_order_nodes_left_right) {
            //debug ("\nVybráno levo-pravé řazení uzlů ve stromech");
            nodes_ordering_in_trees = NGTreeProperties.DIRECTION_LEFT_RIGHT;
            zalozka_trees.strom_view.getTreeProperties().setDirection(NGTreeProperties.DIRECTION_LEFT_RIGHT);
/* ###zk začátek aplikačního kódu */
            zalozka_trees.forest_print.getTreeProperties().setDirection(NGTreeProperties.DIRECTION_LEFT_RIGHT);
/* ###kk konec aplikačního kódu */
	    zalozka_query.query_forest_view.getTreeProperties().setDirection(NGTreeProperties.DIRECTION_LEFT_RIGHT);
	    zalozka_query.query_forest_view.setFlagWholeForestChanged(true);
	    zalozka_query.query_forest_view.repaint();
            zalozka_trees.strom_view.setFlagWholeForestChanged(true);
            zalozka_trees.strom_view.repaint();
            //zalozka_trees.prekresli();
        }
        else if (zdroj == rbutton_order_nodes_right_left) {
            //debug ("\nVybráno pravo-levé řazení uzlů ve stromech");
            nodes_ordering_in_trees = NGTreeProperties.DIRECTION_RIGHT_LEFT;
            zalozka_trees.strom_view.getTreeProperties().setDirection(NGTreeProperties.DIRECTION_RIGHT_LEFT);
/* ###zk začátek aplikačního kódu */
            zalozka_trees.forest_print.getTreeProperties().setDirection(NGTreeProperties.DIRECTION_RIGHT_LEFT);
/* ###kk konec aplikačního kódu */
	    zalozka_query.query_forest_view.getTreeProperties().setDirection(NGTreeProperties.DIRECTION_RIGHT_LEFT);
	    zalozka_query.query_forest_view.setFlagWholeForestChanged(true);
	    zalozka_query.query_forest_view.repaint();
            zalozka_trees.strom_view.setFlagWholeForestChanged(true);
            zalozka_trees.strom_view.repaint();
            //zalozka_trees.prekresli();
        }
        // řazení slov ve větách
        /*else if (zdroj == rbutton_order_words_left_right) {
            debug ("\nVybráno levo-pravé řazení slov ve větách");
            words_ordering_in_sentences = NGTreeProperties.DIRECTION_LEFT_RIGHT;
            zalozka_trees.prekresli();
        }
        else if (zdroj == rbutton_order_words_right_left) {
            debug ("\nVybráno pravo-levé řazení slov ve větách");
            words_ordering_in_sentences = NGTreeProperties.DIRECTION_RIGHT_LEFT;
            zalozka_trees.prekresli();
        }*/
        // tools
/* ###zk začátek aplikačního kódu */
        else if (zdroj == menu_tools_external_command_start) {
            //debug ("\nVybrána položka 'menu_tools_external_command_start' menu 'tools->external command'");
            //setWaitCursor();
            externalCommandStart();
            //setDefaultCursor();
        }
        else if (zdroj == menu_tools_external_command_edit) {
            //debug ("\nVybrána položka 'menu_tools_external_command_edit' menu 'tools->external command'");
            setWaitCursor();
            externalCommandEdit();
            setDefaultCursor();
        }
/* ###kk konec aplikačního kódu */
        // help
        else if (zdroj == menu_help_manual) {
            //debug ("\nVybrána položka 'menu_help_manual' menu 'help'");
            setWaitCursor();
            zobrazNapovedu(client_install_url + "/doc/netgraph_manual.html", i18n.getString("NETGRAPH_HELP"));
            setDefaultCursor();
        }
        else if (zdroj == menu_help_changelog) {
            //debug ("\nVybrána položka 'menu_help_changelog' menu 'help'");
            setWaitCursor();
            zobrazNapovedu(client_install_url + "/doc/ChangeLog.html", i18n.getString("NETGRAPH_CHANGELOG"));
            setDefaultCursor();
        }
        else if (zdroj == menu_help_home_page) {
            //debug ("\nVybrána položka 'menu_help_home_page' menu 'help'");
            setWaitCursor();
            zobrazNapovedu(client_install_url, i18n.getString("NETGRAPH_HOME_PAGE"));
            setDefaultCursor();
        }

    }



    // --------------------------------- obsluha událostí ----------------------------------

/* ###zk začátek aplikačního kódu */
    private void changePassword(boolean first) { // zobrazí se dialog pro změnu nebo první nastavení hesla (first == true znamená první nastavení)

        if (kom_net != null) {
            if (kom_net.isConnected()) {
                String login_name = user_account.getLoginName();
                if (user_account.getChangePasswordPermission()) { // pokud uživatel smí měnit heslo
                    boolean change;
                    String title = i18n.getString("DIALOG_CHANGE_PASSWORD_TITLE") + " " + login_name;
                    int x_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getWidth();
                    int y_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getHeight();

                    int x_pos = (int)(x_max/2 - change_password_dialog.getSize().getWidth()/2);
                    int y_pos = (int)(y_max/2 - change_password_dialog.getSize().getHeight()/2);

                    change_password_dialog.setLocation(x_pos, y_pos);

                    change = change_password_dialog.show(title, first); // dialogové okno
                    if (change) {
                        char[] old_passwd = change_password_dialog.getOldPassword();
                        char[] new_passwd = change_password_dialog.getNewPassword();
                        change_password_dialog.clearPasswords();
                        String old_passwd_enc, new_passwd_enc;
                        if (old_passwd.length == 0) { // prazdne heslo
                            old_passwd_enc = "";
                        }
                        else {
                            old_passwd_enc = encryptPassword(old_passwd);
                        }
                        new_passwd_enc = encryptPassword(new_passwd);
                        //debug ("\nPokouším se o změnu hesla uživatele " + login_name);

                        int error = kom_net.changePassword(login_name, old_passwd_enc, new_passwd_enc);
                        //debug("\nNávratová hodnota změny hesla je: " + r);

                        if (error == ServerCommunication.error_ok) { // pokud bez chyby
                            inform("CHANGE_PASSWORD_OK");
                            debug("\nZměna hesla uživatele " + login_name + " se zdařila.");
                        }
                        else { // pokud se vyskytla chyba
                            inform("CHANGE_PASSWORD_KO"); // ostatní chyby
                            addInfoNotLocalized(" - " + kom_net.getErrorMessage() + "!");
                        }
                    }
                }
                else { // uživatel nesmí měnit heslo
                    debug("\nUživatel " + login_name + " není oprávněn ke změně hesla.");
                    inform("CHANGE_PASSWORD_NOT_ALLOWED");
                }
            }
        }
    } // changePassword

/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private int exitQuery() { // vyvolá se dialogové okno dotázavší se na ukončení aplikace
        JFrame frame = new JFrame("");
        Object[] options = {i18n.getString("DIALOG_EXIT_YES"),
        i18n.getString("DIALOG_EXIT_NO")};
        int n = JOptionPane.showOptionDialog(frame,
        i18n.getString("DIALOG_EXIT_QUESTION"),
        i18n.getString("DIALOG_EXIT_HEAD"),
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,     //don't use a custom Icon
        options,  //the titles of buttons
        options[0]); //default button title
        return n;
    } // exitQuery

    private void exitNetgraphClient() { // dotáže se, jestli chce uživatel opravdu skončit, po kladné odpovědi uloží na disk potřebné informace a ukončí Netgraph client
        setWaitCursor();
        //debug ("\nUživatel chce zavřít hlavní okno.");
        int n = exitQuery(); // zeptáme se uživatele, jestli to míní vážně
        if (n == JOptionPane.YES_OPTION) {
            disconnectFromServer(); // uložím potřebné informace před spojením na disk a odpojím se
            saveActionsExit(); // uložím potřebné informace na disk
            setDefaultCursor();
            System.exit(0);
        }
        setDefaultCursor();
    } // exitNetgraphClient
/* ###kk konec aplikačního kódu */

    /**
     * It closes the communication with the server.
     */
    public void destroy() {  // uzavre komunikacni objekt Socket; volana dokonce interpretem (napr. Netscapem)
        if (kom_net != null) kom_net.destroy();
    } // destroy

    /**
     * It writes a debug message to a special tab or standard output.
     * @param s a debug message
     */
    public void debug(String s) {  // metoda interfacu ShowMessagesAble - v záložce zalozka_debug vypíše ladicí informaci
        if (zalozka_debug != null) zalozka_debug.debug(s);
        else System.out.print(s);
    }

    /**
     * It writes an informative message to a special info bar. The message is internationalized.
     * @param s a code of an internationalized message
     */
    public void inform(String s) {  // v InfoBaru vypíše informaci pro uživatele v lokalizovaném jazyce
        if (use_internal_info_bar)
            if (info_bar != null) info_bar.setText(i18n.getString(s), true); // v interní liště
/* ###zt začátek apletovského kódu
                if (use_external_info_bar) showStatus (i18n.getString(s)); // Totéž vypíše v infobaru prohlížeče
/* ###kt konec apletovského kódu */

    }

    /**
     * It adds an informative message to the acutal content of a special info bar. The message is internationalized.
     * @param s a code of an internationalized message
     * @param sep a string that will separate the actual content of the info bar and the new message. It is not localized.
     */
    public void addInfo(String s, String sep) {  // v InfoBaru připíše informaci pro uživatele v lokalizovaném jazyce
        // oddělí ji od předchozí nelokalizovaným řetězcem sep
        if (use_internal_info_bar)
            if (info_bar != null) info_bar.setText(sep + i18n.getString(s), false); // v interní liště
/* ###zt začátek apletovského kódu
                if (use_external_info_bar) showStatus (sep + i18n.getString(s)); // Totéž vypíše v infobaru prohlížeče
/* ###kt konec apletovského kódu */
    }

    /**
     * It adds an informative message to the acutal content of a special info bar. The message is not internationalized.
     * @param s a message
     */
    public void addInfoNotLocalized(String s) {  // v InfoBaru připíše nelokalizovanou informaci pro uživatele
        if (use_internal_info_bar) info_bar.setText(s, false); // v interní liště
/* ###zt začátek apletovského kódu
                if (use_external_info_bar) showStatus (s); // Totéž vypíše v infobaru prohlížeče
/* ###kt konec apletovského kódu */
    }


    private void disconnectFromServer() { // odpojí se od serveru
        if (kom_net != null) {
            if (kom_net.isConnected()) {
                debug("\nOdpojuji se od serveru.");
/* ###zk začátek aplikačního kódu */
                saveActionsDisconnect(); // provede ukládací akce při odpojování od serveru
/* ###kk konec aplikačního kódu */
                destroy();
                zalozka_files.disconnected();
                inform("YOU_HAVE_BEEN_DISCONNECTED");
            }
        }
    } // disconnectFromServer


    /**
     * This is the method which converts the any string value to MD5
format.
     *
     *@param passwd password
     *@return     encrypted password in MD5
     */
    private String encryptPassword(char[] passwd) {
        StringBuffer retString = new StringBuffer();
        byte bs[] = new byte[passwd.length];
        for (int i = 0; i < passwd.length; ++i) {
            bs[i] = (byte)passwd[i];
        }
        try {
            MessageDigest alg = MessageDigest.getInstance("MD5", "SUN");
            //String myVar = str;

            byte digest[] = alg.digest(bs);
            for (i = 0; i < digest.length; ++i) {
                retString.append(Integer.toHexString(0x0100 + (digest[i] & 0x00FF)).substring(1));
            }
        } catch (Exception e) {
            debug("\nNGClient: encryptPassword: there appears to have been an error " + e);
        }
        return retString.toString();
    } // encryptPassword


    private void connectToServer() { // pokusí se připojit k serveru a prihlasit uzivatele
        //setWaitCursor();

        boolean spojit;
/* ###zk začátek aplikačního kódu */
        server_connection_dialog.setLoginName(user_account.getLoginName());
        if (server_connection_dialog.getLoginName().equalsIgnoreCase("anonymous")) {
            server_connection_dialog.setPassword("anonymous");
        }
        if (server_connection_dialog.getLoginName().length()<1) { // login name has not been filled in
            server_connection_dialog.focusLogin(); // set focus to login name field
        }
        else { // a login name has been filled in - set focus to password field
            server_connection_dialog.focusPassword();
        }
        int x_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getWidth();
        int y_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getHeight();

        int x_pos = (int)(x_max/2 - server_connection_dialog.getSize().getWidth()/2);
        int y_pos = (int)(y_max/2 - server_connection_dialog.getSize().getHeight()/2);

        server_connection_dialog.setLocation(x_pos, y_pos);

        spojit = server_connection_dialog.show(0); // dialogové okno
/* ###kk konec aplikačního kódu */
/* ###zt začátek apletovského kódu
        spojit = true; // aplet si nemůže vybírat server, akce je rovnou provedena bez oslovení uživatele
/* ###kt konec apletovského kódu */
        if (! spojit) {
            debug("\nAkce spojení se serverem zrušena.");
        }
        else {
            if (kom_net != null && kom_net.isConnected()) {
                disconnectFromServer();
            }
            String log_name=user_account.getLoginName();
            char[] log_passwd;
/* ###zk začátek aplikačního kódu */
            param_adresa = server_connection_dialog.getServerName();
            param_port = server_connection_dialog.getServerPort();
            log_name = server_connection_dialog.getLoginName();
            log_passwd = server_connection_dialog.getPassword();
            server_connection_dialog.clearPassword();
            user_account.setLoginName(log_name);
/* ###kk konec aplikačního kódu */
/* ###zt začátek apletovského kódu
            log_passwd=new String("anonymous").toCharArray();
/* ###kt konec apletovského kódu */
            String log_passwd_enc;
            if (log_passwd.length == 0) { // prazdne heslo
                log_passwd_enc = "";
            }
            else {
                log_passwd_enc = encryptPassword(log_passwd);
            }

            debug("\nPokouším se o spojení se serverem " + param_adresa + " na portu " + param_port + " s přihlašovacím jménem " + log_name);
            kom_net = new ServerCommunication(client_version, param_port, param_adresa, user_account, log_passwd_enc, this, i18n, this);  // vytvoření spojení se serverem
            int error = kom_net.getError();
            if (error == ServerCommunication.error_ok) { // pokud bez chyby
                zalozka_files.naplnAdresareASoubory();  // naplni nektere prvky formulare pro vyber souboru s daty: aktualni cestu, podadresare a soubory v akt. ceste
                hlavni_zalozky.setSelectedIndex(0); // přepnu se do záložky souborů
                zalozka_files.naplnServerInfo(); // naplní informace o připojeném serveru
                inform("CONNECTION_OK");
                checkVersionsMatching();

                debug("\nUživatel je přihlášen pod uživatelským jménem " + user_account.getLoginName() + ".");
                debug("\nAutorizační údaje uživatele:");
                debug("\n  - typ konta: " + ((user_account.getAccountType() == UserAccount.account_type_user) ? "registrovaný uživatel" : "anonymní"));
                debug("\n  - výchozí (kořenový) adresář korpusu: " + user_account.getRootDirectory());
                long num = user_account.getMaxNumberOfTrees();
                String tr = ((num == 0) ? "neomezený" : ("" + num));
                debug("\n  - maximální počet stromů v odpovědi na jeden dotaz: " + tr);
                debug("\n  - povolení ukládat nalezené stromy na lokální disk: " + ((user_account.getClientSaveTreesPermission() == true) ? "ano" : "ne"));
                debug("\n  - povolení měnit přístupové heslo: " + ((user_account.getChangePasswordPermission() == true) ? "ano" : "ne"));
                debug("\n  - jméno uživatele: " + user_account.getUserName());

/* ###zk začátek aplikačního kódu */
                if (log_passwd.length == 0) { // pokud má uživatel prázdné heslo, tedy dosud nenastavené
                    if (user_account.getChangePasswordPermission()) { // pokud má uživatel právo měnit heslo (a tím pádem server podporuje logování)
                        changePassword(true); // zobrazí se dialog pro nastavení hesla
                    }
                }
                menu_soubor_save_trees.setEnabled(user_account.getClientSaveTreesPermission());
                menu_soubor_change_password.setEnabled(user_account.getChangePasswordPermission());
/* ###kk konec aplikačního kódu */
                if (kom_net.getServerVersion().compareToIgnoreCase("1.68")<0) { // pro kompatibilitu se staršími servery
                    zalozka_query.check_invert_match.setEnabled(false); // starší servery tuto vlastnost nepodporují
                }
                else {
                    zalozka_query.check_invert_match.setEnabled(true); // nové servery tuto vlastnost podporují
                }
                zalozka_files.connected();
            }
            else { // pokud se vyskytla chyba při připojování k serveru
                if (error == ServerCommunication.error_max_clients) {
                    inform("MAX_CLIENTS_REACHED"); // příliš mnoho připojených klientů k serveru
                }
                else {
                    inform("CONNECTION_KO"); // ostatní chyby
                    addInfoNotLocalized(" - " + kom_net.getErrorMessage() + "!");

                }
                zalozka_files.clearAllInfo();
            }
        }
        //setDefaultCursor();
    } // connectToServer

    /**
     * It sets a busy cursor in the main application window.
     */
    public void setWaitCursor() { // nastaví zaneprázdněný ukazatel myši na hlavním okně
/* ###zk začátek aplikačního kódu */
        setCursor(cursor_wait);
/* ###kk konec aplikačního kódu */
        return;
    }
    /**
     * It sets a default cursor in the main application window.
     */
    public void setDefaultCursor() { // nastaví normální ukazatel myši na hlavním okně
/* ###zk začátek aplikačního kódu */
        setCursor(cursor_default);
/* ###kk konec aplikačního kódu */
        return;
    }

    private boolean checkVersionsMatching() { // provede stringové porovnání verzí
        String client_recommended = kom_net.getClientRecommendedVersion();
        String client_required = kom_net.getClientRequiredVersion();
        String server_version = kom_net.getServerVersion();

        int match = 0;

        debug("\nVerze klienta je " + client_version);
        debug("\nPožadovaná verze serveru je " + server_required_version);
        debug("\nDoporučená verze serveru je " + server_recommended_version);


        if (server_recommended_version.compareToIgnoreCase(server_version)>0) { // server je starší, než je doporučováno
            match = -1;
            if (server_required_version.compareToIgnoreCase(server_version)>0) { // dokonce starší, než je požadováno
                match = -2;
            }
        }

        if (client_recommended.compareToIgnoreCase(client_version)>0) { // klient je starší, než je doporučováno
            match = 1;
            if (client_required.compareToIgnoreCase(client_version)>0) { // dokonce starší, než je požadováno
                match = 2;
            }
        }

        switch (match) { // podle vztahu verzí
            case 0: // verze matchují perfektně
                debug("\nVerze klienta a serveru k sobě dobře pasují.");
                addInfo("CLIENT_VERSION_OK"," ");
                break;
            case 1: // doporučuje se upgrade klienta
                debug("\nVerze klienta a serveru pasují, ale doporučuji upgradnout klienta alespoň na " + client_recommended);
                addInfo("CLIENT_VERSION_SMALL"," ");
                addInfoNotLocalized(" " + client_recommended);
                break;
            case 2: // vyžaduje se upgrade klienta
                debug("\nVerze klienta je příliš stará k verzi serveru! Je potřeba upgradnout alespoň na " + client_required + ", ale doporučuji alespoň na " + client_recommended);
                addInfo("CLIENT_VERSION_TOO_SMALL"," ");
                addInfoNotLocalized(" " + client_required);
                break;
            case -1: // doporučuje se upgrade serveru
                debug("\nVerze klienta a serveru pasují, ale doporučuji upgradnout server alespoň na " + server_recommended_version);
                addInfo("SERVER_VERSION_SMALL"," ");
                addInfoNotLocalized(" " + server_recommended_version);
                break;
            case -2: // vyžaduje se upgrade serveru
                debug("\nVerze serveru je příliš stará k verzi klienta! Je potřeba upgradnout server alespoň na " + server_required_version + ", ale doporučuji alespoň na " + server_recommended_version);
                addInfo("SERVER_VERSION_TOO_SMALL"," ");
                addInfoNotLocalized(" " + server_required_version);
                break;
        }

        if (match == 0) return true;
        else return false;
    }

    private void setFontsizeQuery(int size) {
        //debug("\nNGClient.setFontsizeQuery: A font size in queries has been set to " + size);
        zalozka_query.query_forest_view.setFontSize(size);
    }

    private void setFontsizeResult(int size) {
        //debug("\nNGClient.setFontsizeResult: A font size in results has been set to " + size);
        zalozka_trees.strom_view.setFontSize(size);
    }

    private void setFontsizeSentence(int size) {
        //debug("\nNGClient.setFontsizeSentence: A font size in sentences has been set to " + size);
        zalozka_trees.setFontSizeSentence(size);
    }

    public void setMenuOptionsFontsizeQuery(int size) {
        switch (size) {
          case 6: menu_options_fontsize_query_6.setSelected(true); break;
          case 8: menu_options_fontsize_query_8.setSelected(true); break;
          case 10: menu_options_fontsize_query_10.setSelected(true); break;
          case 12: menu_options_fontsize_query_12.setSelected(true); break;
          case 14: menu_options_fontsize_query_14.setSelected(true); break;
          case 16: menu_options_fontsize_query_16.setSelected(true); break;
          case 18: menu_options_fontsize_query_18.setSelected(true); break;
          case 20: menu_options_fontsize_query_20.setSelected(true); break;
          case 22: menu_options_fontsize_query_22.setSelected(true); break;
          case 24: menu_options_fontsize_query_24.setSelected(true); break;
          case 26: menu_options_fontsize_query_26.setSelected(true); break;
          case 28: menu_options_fontsize_query_28.setSelected(true); break;
          case 30: menu_options_fontsize_query_30.setSelected(true); break;
          case 32: menu_options_fontsize_query_32.setSelected(true); break;
          default: menu_options_fontsize_query_12.setSelected(false);  // tohle nezabírá, stejně to zůstane vybrané
        }
    }

    public void setMenuOptionsFontsizeResult(int size) {
        switch (size) {
          case 6: menu_options_fontsize_result_6.setSelected(true); break;
          case 8: menu_options_fontsize_result_8.setSelected(true); break;
          case 10: menu_options_fontsize_result_10.setSelected(true); break;
          case 12: menu_options_fontsize_result_12.setSelected(true); break;
          case 14: menu_options_fontsize_result_14.setSelected(true); break;
          case 16: menu_options_fontsize_result_16.setSelected(true); break;
          case 18: menu_options_fontsize_result_18.setSelected(true); break;
          case 20: menu_options_fontsize_result_20.setSelected(true); break;
          case 22: menu_options_fontsize_result_22.setSelected(true); break;
          case 24: menu_options_fontsize_result_24.setSelected(true); break;
          case 26: menu_options_fontsize_result_26.setSelected(true); break;
          case 28: menu_options_fontsize_result_28.setSelected(true); break;
          case 30: menu_options_fontsize_result_30.setSelected(true); break;
          case 32: menu_options_fontsize_result_32.setSelected(true); break;
          default: menu_options_fontsize_result_12.setSelected(false); // tohle nezabírá, stejně to zůstane vybrané
        }
    }

    public void setMenuOptionsFontsizeSentence(int size) {
        switch (size) {
          case 6: menu_options_fontsize_sentence_6.setSelected(true); break;
          case 8: menu_options_fontsize_sentence_8.setSelected(true); break;
          case 10: menu_options_fontsize_sentence_10.setSelected(true); break;
          case 12: menu_options_fontsize_sentence_12.setSelected(true); break;
          case 14: menu_options_fontsize_sentence_14.setSelected(true); break;
          case 16: menu_options_fontsize_sentence_16.setSelected(true); break;
          case 18: menu_options_fontsize_sentence_18.setSelected(true); break;
          case 20: menu_options_fontsize_sentence_20.setSelected(true); break;
          case 22: menu_options_fontsize_sentence_22.setSelected(true); break;
          case 24: menu_options_fontsize_sentence_24.setSelected(true); break;
          case 26: menu_options_fontsize_sentence_26.setSelected(true); break;
          case 28: menu_options_fontsize_sentence_28.setSelected(true); break;
          case 30: menu_options_fontsize_sentence_30.setSelected(true); break;
          case 32: menu_options_fontsize_sentence_32.setSelected(true); break;
          default: menu_options_fontsize_sentence_12.setSelected(false); // tohle nezabírá, stejně to zůstane vybrané
        }
    }

/* ###zk začátek aplikačního kódu */
    private String getPureFileName(String file_name) { // odstraní cestu a příponu daného jména souboru
        String path_separator_1 = "\\";
        String path_separator_2 = "/"; // ať nemusím zjišťovat, na jakém systému běží server
        int last_separator = Math.max(file_name.lastIndexOf(path_separator_1),file_name.lastIndexOf(path_separator_2));
        String file_only;
        if (last_separator>=0) {
            file_only = file_name.substring(last_separator+1);
        }
        else {
            file_only = file_name;
        }
        String file_pure;
        int last_dot = file_only.lastIndexOf(".");
        if (last_dot>=0) {
            file_pure = file_only.substring(0,last_dot);
        }
        else {
            file_pure = file_only;
        }
        return file_pure;
    }

    private String externalCommandSubstitute(String command) { // nahradí proměnné v externím příkazu skutečnými hodnotami
        //debug("\nNGClient.externalCommandSubstitute: Going to substitute variables in command: " + command);
        String substituted = new String(command);
        try {
            String file_name = zalozka_trees.getActualForest().getFileName();
            file_name = getPureFileName(file_name);
            substituted = substituted.replaceAll(EXTERNAL_COMMAND_VAR_FILE_NAME,file_name);
            String tree_number = ""+zalozka_trees.getActualForest().getForestNumber();
            substituted = substituted.replaceAll(EXTERNAL_COMMAND_VAR_TREE_NUMBER,tree_number);
            String root_order = ""+(zalozka_trees.getActualForest().getFirstMatchingNodeDepthOrder(false)-1);
            substituted = substituted.replaceAll(EXTERNAL_COMMAND_VAR_ROOT_ORDER,root_order);
            String chosen_node_order = ""+(zalozka_trees.getActualForest().getChosenNodeDepthOrder(false)-1);
            substituted = substituted.replaceAll(EXTERNAL_COMMAND_VAR_CHOSEN_NODE_ORDER,chosen_node_order);
        }
        catch (Exception e) {
            debug("\nNGClient.externalCommandSubstitute: An error occurred during substituting variables in the external command: " + substituted);
            e.printStackTrace();
        }
        //debug("\nNGClient.externalCommandSubstitute: After the substitution: " + substituted);
        return substituted;
    }

    private void externalCommandStart() { // spustí externí příkaz
        String external_command_substituted = externalCommandSubstitute(external_command);
        debug("\nStarting the external command: " + external_command_substituted);
        try {
            Runtime.getRuntime().exec(external_command_substituted);
        } catch (IOException e) {
            debug("\nNGClient.externalCommandStart: An error occurred during starting the external command: " + external_command_substituted);
            debug("\nError:" + e);
            //e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
        }
        inform("INFO_EXTERNAL_COMMAND_STARTED");
        addInfoNotLocalized(" " + external_command_substituted);
        //debug("\nNGClient.externalCommandStart: The external command has been started.");
    } // externalCommandStart

    private void externalCommandEdit() { // zobrazí dialogové okno pro úpravu externího příkazu
        String command = (String)JOptionPane.showInputDialog(
                    this,
                    i18n.getString("DIALOG_EXTERNAL_COMMAND_EDIT_LABEL"),
                    i18n.getString("DIALOG_EXTERNAL_COMMAND_EDIT_TITLE"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    external_command);
        if (command != null) {
            external_command = command;
        }
        //debug("\nNGClient.externalCommandEdit: The external command after the editation: " + external_command);        
    } // externalCommandEdit
/* ###kk konec aplikačního kódu */

    private void zobrazNapovedu(String adresa, String title) { // Zobrazí nápovědu v odděleném okně prohlížeče
        //URL url;
        String url_string;
        try {
            url_string = new String("http://" + adresa);
            //url_string = new String ("../../doc/netgraph_manual.html");
/* ###zt začátek apletovského kódu
            URL url = new URL(url_string);
                    if (url == null) debug("\nUrl pro nápovědu je null!");
                    applet_context = getAppletContext(); // pro přístup k prohlížeči - např. otvírání okna s nápovědou
                    if (applet_context == null) debug("\napplet_context je null!");
            applet_context.showDocument(url,title);
/* ###kt konec apletovského kódu */
/* ###zk začátek aplikačního kódu */
            WebViewer help_window = new WebViewer(title, url_string, i18n.getString("HELP_LOCATION"));
            //help_window.start();
/* ###kk konec aplikačního kódu */

        }
        catch (/*MalformedURL*/Exception e) {
            inform("NELZE_ZOBRAZIT_NAPOVEDU");
            debug("\nNelze zobrazit nápovědu! Nastala výjimka: " + e);
        }
    } // zobrazNapovedu


/* ###zk začátek aplikačního kódu */
    private void readGeneralProperties(Properties properties) { // přečtu properties pro hlavní objekt NGClient
        main_window_height = properties.getIntProperty("main window","height", main_window_height);
        main_window_width = properties.getIntProperty("main window","width", main_window_width);
        use_internal_info_bar = properties.getBooleanProperty("main menu","use internal info bar", use_internal_info_bar);
        lemma_variants_match = properties.getBooleanProperty("main menu","lemma variants match", lemma_variants_match);
        lemma_variants_show = properties.getBooleanProperty("main menu","lemma variants show", lemma_variants_show);
        lemma_comments_match = properties.getBooleanProperty("main menu","lemma comments match", lemma_comments_match);
        lemma_comments_show = properties.getBooleanProperty("main menu","lemma comments show", lemma_comments_show);
        coding_in_queries = CharCode.getCodingNumber(properties.getStringProperty("main menu","character coding in queries", CharCode.getCodingName(coding_in_queries)));
        last_login_name = properties.getStringProperty("connection to server","last login name","anonymous");
        last_server_name = properties.getStringProperty("connection to server","last server name",server_default_name);
        last_server_port = properties.getIntProperty("connection to server","last server port",server_default_port);
		//words_ordering_in_sentences = properties.getIntProperty("main menu","words ordering", words_ordering_in_sentences);
		nodes_ordering_in_trees = properties.getIntProperty("main menu","nodes ordering", nodes_ordering_in_trees);
        external_command = properties.getStringProperty("tools","external command", external_command);
    } // readGeneralProperties

    private void loadGeneralProperties() { // nahraji uložené properties z disku
        PropertiesLoader loader = new PropertiesLoader(jaaa);
        //Property p;
        properties_general_properties = loader.loadProperties("netgraph","general_properties");
        if (properties_general_properties != null) { // properties úspěšně načteny z disku
            readGeneralProperties(properties_general_properties);
        }
        else {
            properties_general_properties = new Properties();
        }
    } // loadGeneralProperties

    private void writeGeneralProperties(Properties properties) { // zapíše properties z tohoto hlavního objektu NGClient
        properties.updateProperty("main window","width",""+(int)this.getSize().getWidth(),"main window width (integer)");
        properties.updateProperty("main window","height",""+(int)this.getSize().getHeight(),"main window height (integer)");
        properties.updateProperty("main menu","use internal info bar",""+use_internal_info_bar,"(true, false)");
        properties.updateProperty("main menu","lemma variants match",""+lemma_variants_match,"match lemmas specified in queries with all their variants? (true, false)");
        properties.updateProperty("main menu","lemma variants show",""+lemma_variants_show,"show lemma variants in trees? (true, false)");
        properties.updateProperty("main menu","lemma comments match",""+lemma_comments_match,"match lemmas specified in queries regardles of their comments? (true, false)");
        properties.updateProperty("main menu","lemma comments show",""+lemma_comments_show,"show lemma comments in trees? (true, false)");
        properties.updateProperty("main menu","character coding in queries", CharCode.getCodingName(coding_in_queries),"how to code Czech accented characters in queries (unicode, pseudo)");
        properties.updateProperty("connection to server","last login name",user_account.getLoginName(),"last login name (string)");
        if (kom_net != null) {
            properties.updateProperty("connection to server","last server name",kom_net.getServerName(),"last server name (string)");
            properties.updateProperty("connection to server","last server port",""+kom_net.getServerPort(),"last server port (integer)");
        }
        //properties.updateProperty("main menu","words ordering",""+words_ordering_in_sentences,"words ordering in sentences (1 = left to right, 2 = right to left");
        properties.updateProperty("main menu","nodes ordering",""+nodes_ordering_in_trees,"nodes ordering in trees (1 = left to right, 2 = right to left");
        properties.updateProperty("tools","external command",external_command,"external command (string)");
    } // writeGeneralProperties

    private void saveGeneralProperties() { // uložím obecné properties na disk

        if (properties_general_properties == null) return; // není co ukládat - to by nemělo nastat

        if (properties_general_properties.getComment().length() <= 0) {
            properties_general_properties.setComment("Netgraph application client main configuration file - feel free to edit it.");
        }

        writeGeneralProperties(properties_general_properties);
        zalozka_files.writeGeneralProperties(properties_general_properties);
        zalozka_query.writeGeneralProperties(properties_general_properties);
        zalozka_trees.writeGeneralProperties(properties_general_properties);

        PropertiesLoader loader = new PropertiesLoader(jaaa);
        loader.saveProperties("netgraph","general_properties", properties_general_properties);
    } // saveGeneralProperties

    private void saveActionsDisconnect() { // ukládání informací na disk při odpojení od serveru
        zalozka_files.saveActionsDisconnect();
        zalozka_query.saveActionsDisconnect();
        zalozka_trees.saveActionsDisconnect();
    }

    private void saveActionsExit() { // ukládání informací na disk před ukončením aplikace
        zalozka_files.saveActionsExit();
        zalozka_query.saveActionsExit();
        zalozka_trees.saveActionsExit();
        saveGeneralProperties(); // uložím globální properties na disk
    }
/* ###kk konec aplikačního kódu */


}  // class NGClient


