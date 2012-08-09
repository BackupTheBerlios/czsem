

// ====================================================================================================
//		class PanelFiles              vyber souboru pro dotazovani
// ====================================================================================================

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.ResourceBundle;
import java.util.Iterator;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.trees.*;
/* ###zk začátek aplikačního kódu */
import cz.cuni.mff.mirovsky.properties.*;
/* ###kk konec aplikačního kódu */
import cz.cuni.mff.mirovsky.account.*;


/**
 * Class PanelFiles creates a panel for selecting files with trees for searching. It communicates with the server and lets
 * the user navigate through the accessible directory structure of the server. The user can select individual files or
 * directories and make a list of files and directories for searching. The selected list can be saved to the local disc
 * and loaded back next time Netgraph starts.
 */
public class PanelFiles extends JPanel implements ActionListener, MouseListener, TreeExpansionListener, TreeWillExpandListener {

    final static private int DIRECTORY = 0;
    final static private int FILE = 1;

    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    private NGClient jaaa;

    JLabel label_akt_cesta;  // aktualni cesta; byvaly label3
    JTextField label_login_name; // přihlašovací jméno uživatele
    JTextField label_server_name; // jméno serveru, ke kterému jsme připojeni
    JTextField label_server_port; // číslo portu serveru, ke kterému jsme připojeni
    JTextField label_server_version; // verze serveru, ke kterému jsme připojeni
    DefaultTreeModel model_tree_adresare;  // model pro tree_adresare
    JTree tree_adresare;  // seznam adresaru v aktualni ceste; byvaly list1
    DefaultTreeModel model_tree_soubory; // model pro tree_soubory
    JTree tree_soubory; // seznam souboru v aktualni ceste
    JScrollPane adresare_scroll_pane; // scroll_pane pro tree_adresare
    JScrollPane soubory_scroll_pane; // scroll_pane pro tree_soubory
    JLabel label_adresare; // nadpis vypisu adresaru v aktualni ceste
    JLabel label_soubory; // nadpis vypisu souboru v aktualni ceste
    JButton button_up_dir; // tlacitko pro prechod na nadrazeny adresar; byvaly button1
    JButton button_add_marked_directories; // tlacitko pro přidání označených adresářů v aktuální cestě k vybraným
    JButton button_add_all_directories; // tlacitko pro přidání všech adresářů v aktuální cestě k vybraným
    JButton button_add_marked_files; // tlacitko pro přidání označených souborů v aktuální cestě k vybraným
    JButton button_add_all_files; // tlacitko pro přidání všech souborů v aktuální cestě k vybraným
    GridBagLayout layout_adresare; // layout pro tree_adresare a label_adresare a button_up_dir
    GridBagLayout layout_soubory; // layout pro tree_soubory a label_soubory
    JPanel panel_adresare; // panel pro layout_adresare
    JPanel panel_soubory; // panel pro layout_soubory
    JSplitPane split_adresare_soubory; // splitpane pro panel_adresare a panel_soubory
    JLabel label_vybrane_soubory; // nadpis vypisu vybranych souboru
    JButton button_clear_selected_files; // tlačítko pro smazání seznamu vybraných souborů
    DefaultListModel model_list_vybrane_soubory;  // model pro list_vybrane_soubory
    JList list_vybrane_soubory; // seznam vybranych souboru; byvaly list3
    JScrollPane vybrane_soubory_scroll_pane; // scrollpane pro list_vybrane_soubory
    GridBagLayout layout_vybrane_soubory; // layout pro list_vybrane_soubory a label_vybrane_soubory
    JPanel panel_vybrane_soubory; // panel pro layout_vybrane_soubory
    JSplitPane split_vypsane_vybrane; // splitpane pro  split_adresare_soubory a panel_vybrane_soubory
    GridBagLayout layout_zalozka_files; // layout pro zalozku pro vyber souboru
/* ###zk začátek aplikačního kódu */
    JTextField edit_auto_load;  // editacni pole pro vlozeni jmena subkorpusu, ktery se ma automaticky pouzit k prohledavani po startu Netgraphu
    JCheckBox check_auto_load;  // volba pro automaticke nahrani subkorpusu po startu Netgraphu
    JButton button_select_auto_load; // tlačítko pro výběr subkorpusu pro automatické vybrání při příštím přihlášení
/* ###kk konec aplikačního kódu */
    JButton button_select_files; // nastaveni souboru urcenych k dotazu; byvaly button6

/* ###zk začátek aplikačního kódu */
    JButton button_load_selected_files; // tlačítko pro nahrání uloženého seznamu vybraných souborů
    JButton button_save_selected_files; // tlačítko pro uložení seznamu vybraných souborů
    Properties properties_saved_selected_files; // properties pro ukládání seznamu vybraných souborů
    private final static String saved_selected_files_prefix = "file_"; // automaticky generovaná jména jednotlivých uložených property - souborů budou začínat tímto prefixem
/* ###kk konec aplikačního kódu */


    public PanelFiles(NGClient p_jaaa, ShowMessagesAble p_mess, ResourceBundle p_i18n) { // konstruktor

        mess = p_mess;
        i18n = p_i18n;
        jaaa = p_jaaa;

        JLabel label_l_login_name = new JLabel(i18n.getString("LABEL_LOGIN_NAME"));
        label_login_name = new JTextField();  // jméno připojeného serveru
        label_login_name.setEnabled(false);
        label_login_name.setDisabledTextColor(Color.black);
        JPanel panel_login_name = new JPanel();
        panel_login_name.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel_login_name.add(label_l_login_name);
        panel_login_name.add(label_login_name);
        JLabel label_l_server_name = new JLabel(i18n.getString("LABEL_SERVER_NAME"));
        label_server_name = new JTextField();  // jméno připojeného serveru
        label_server_name.setEnabled(false);
        label_server_name.setDisabledTextColor(Color.black);
        JPanel panel_server_name = new JPanel();
        panel_server_name.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel_server_name.add(label_l_server_name);
        panel_server_name.add(label_server_name);
        JLabel label_l_server_port = new JLabel(i18n.getString("LABEL_SERVER_PORT"));
        label_server_port = new JTextField();  // číslo portu připojeného serveru
        label_server_port.setEnabled(false);
        label_server_port.setDisabledTextColor(Color.black);
        JPanel panel_server_port = new JPanel();
        panel_server_port.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel_server_port.add(label_l_server_port);
        panel_server_port.add(label_server_port);
        JLabel label_l_server_version = new JLabel(i18n.getString("LABEL_SERVER_VERSION"));
        label_server_version = new JTextField();  // jméno připojeného serveru
        label_server_version.setEnabled(false);
        label_server_version.setDisabledTextColor(Color.black);
        JPanel panel_server_version = new JPanel();
        panel_server_version.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel_server_version.add(label_l_server_version);
        panel_server_version.add(label_server_version);
        JPanel panel_server = new JPanel();
        //panel_server.setLayout(new FlowLayout(FlowLayout.CENTER, 30,0));
        panel_server.setLayout(new BoxLayout(panel_server, BoxLayout.X_AXIS));
        panel_server.setBorder(BorderFactory.createTitledBorder(i18n.getString("LABEL_SERVER")));
        panel_server.add(panel_login_name);
        panel_server.add(panel_server_name);
        panel_server.add(panel_server_port);
        panel_server.add(panel_server_version);
        JLabel label_l_akt_cesta = new JLabel(i18n.getString("LABEL_ACTUAL_PATH"));
        label_l_akt_cesta.setAlignmentX(0f);
        label_akt_cesta = new JLabel();  // aktualni cesta
        //label_akt_cesta.setEnabled(false);
        label_akt_cesta.setAlignmentX(0f);
        JPanel panel_akt_cesta = new JPanel();
        panel_akt_cesta.add(label_l_akt_cesta);
        panel_akt_cesta.add(label_akt_cesta);
        model_tree_adresare = new DefaultTreeModel(new DefaultMutableTreeNode());
        tree_adresare = new JTree(model_tree_adresare);  // seznam adresaru v aktualni ceste; byvaly list1
        tree_adresare.setRootVisible(false);
        model_tree_soubory = new DefaultTreeModel(new DefaultMutableTreeNode());
        tree_soubory = new JTree(model_tree_soubory);  // seznam souboru v aktualni ceste; byvaly list2
        tree_soubory.setRootVisible(false);
        adresare_scroll_pane = new JScrollPane(tree_adresare);
        soubory_scroll_pane= new JScrollPane(tree_soubory);
        label_adresare = new JLabel(i18n.getString("LABEL_DIRECTORIES"));
        label_adresare.setAlignmentX(0.5f);
        label_adresare.setMinimumSize(new Dimension(50,25));
        label_adresare.setHorizontalTextPosition(JLabel.CENTER);
        label_soubory = new JLabel(i18n.getString("LABEL_FILES"));
        label_soubory.setAlignmentX(0.5f);
        label_soubory.setHorizontalTextPosition(JLabel.CENTER);
        label_soubory.setMinimumSize(new Dimension(50,25));
        label_adresare.setMinimumSize(new Dimension(50,25));
        button_add_marked_directories = new JButton(i18n.getString("BUTTON_ADD_MARKED_DIRECTORIES")); // tlacitko pro přidání všech adresářů z aktuální cesty k vybraným
        button_add_marked_directories.setToolTipText(i18n.getString("BUTTON_ADD_MARKED_DIRECTORIES_TOOLTIP")); // tlacitko pro přidání všech adresářů z aktuální cesty k vybraným
        button_add_all_directories = new JButton(i18n.getString("BUTTON_ADD_ALL_DIRECTORIES")); // tlacitko pro přidání všech adresářů z aktuální cesty k vybraným
        button_add_all_directories.setToolTipText(i18n.getString("BUTTON_ADD_ALL_DIRECTORIES_TOOLTIP")); // tlacitko pro přidání všech adresářů z aktuální cesty k vybraným
        button_up_dir = new JButton(i18n.getString("BUTTON_UP")); // tlacitko pro prechod na nadrazeny adresar; byvaly button1
        button_up_dir.setToolTipText(i18n.getString("BUTTON_UP_TOOLTIP"));
        button_add_marked_files = new JButton(i18n.getString("BUTTON_ADD_MARKED_FILES")); // tlacitko pro přidání všech souborů z aktuální cesty k vybraným
        button_add_marked_files.setToolTipText(i18n.getString("BUTTON_ADD_MARKED_FILES_TOOLTIP")); // tlacitko pro přidání všech souborů z aktuální cesty k vybraným
        button_add_all_files = new JButton(i18n.getString("BUTTON_ADD_ALL_FILES")); // tlacitko pro přidání všech souborů z aktuální cesty k vybraným
        button_add_all_files.setToolTipText(i18n.getString("BUTTON_ADD_ALL_FILES_TOOLTIP")); // tlacitko pro přidání všech souborů z aktuální cesty k vybraným
        layout_adresare= new GridBagLayout();
        layout_soubory=new GridBagLayout();
        GridBagConstraints constraint_nadpis_button=new GridBagConstraints();
        GridBagConstraints constraint_nadpis_button2=new GridBagConstraints();
        GridBagConstraints constraint_nadpis_button3=new GridBagConstraints();
        GridBagConstraints constraint_nadpis_right=new GridBagConstraints();
        GridBagConstraints constraint_vypis_double=new GridBagConstraints();
        setConstraints(constraint_nadpis_button, 0, 0, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, -1);
        setConstraints(constraint_nadpis_button2, 2, 0, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.EAST, -1);
        setConstraints(constraint_nadpis_button3, 3, 0, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.EAST, -1);
        setConstraints(constraint_nadpis_right, 1, 0, 1.0, 0.0, -1, GridBagConstraints.CENTER, -1);
        setConstraints(constraint_vypis_double, 0, 1, 1.0, 1.0,  GridBagConstraints.BOTH, -1, GridBagConstraints.REMAINDER);
        layout_adresare.setConstraints(button_up_dir, constraint_nadpis_button);
        layout_adresare.setConstraints(label_adresare, constraint_nadpis_right);
        layout_adresare.setConstraints(button_add_marked_directories, constraint_nadpis_button2);
        layout_adresare.setConstraints(button_add_all_directories, constraint_nadpis_button3);
        layout_adresare.setConstraints(adresare_scroll_pane, constraint_vypis_double);
        layout_soubory.setConstraints(button_add_marked_files, constraint_nadpis_button);
        layout_soubory.setConstraints(button_add_all_files, constraint_nadpis_button2);
        layout_soubory.setConstraints(label_soubory, constraint_nadpis_right);
        layout_soubory.setConstraints(soubory_scroll_pane, constraint_vypis_double);
        panel_adresare = new JPanel();
        panel_adresare.setLayout(layout_adresare);
        panel_soubory = new JPanel();
        panel_soubory.setLayout(layout_soubory);
        panel_adresare.add(button_up_dir); // vlozeni tlacitka pro prechod do nadrazeneho adresare
        panel_adresare.add(label_adresare);
        panel_adresare.add(button_add_marked_directories);
        panel_adresare.add(button_add_all_directories);
        panel_adresare.add(adresare_scroll_pane);
        panel_soubory.add(button_add_marked_files); // vložení tlačítka pro přidání označených souborů v adresáři
        panel_soubory.add(button_add_all_files); // vložení tlačítka pro přidání všech souborů v adresáři
        panel_soubory.add(label_soubory);
        panel_soubory.add(soubory_scroll_pane);
        split_adresare_soubory = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel_adresare, panel_soubory);
        //split_adresare_soubory.setDividerSize(5);
        split_adresare_soubory.setContinuousLayout(true);
        split_adresare_soubory.setOneTouchExpandable(false);
        split_adresare_soubory.setDividerLocation(180);
        //Provide a preferred size for the split pane
        split_adresare_soubory.setPreferredSize(new Dimension(400, 150));
        label_vybrane_soubory = new JLabel(i18n.getString("LABEL_SELECTED_FILES"));
        label_vybrane_soubory.setAlignmentX(0.5f);
        label_vybrane_soubory.setHorizontalTextPosition(JLabel.CENTER);
        //label_vybrane_soubory.setPreferredSize(new Dimension(150,25));
        label_vybrane_soubory.setMinimumSize(new Dimension(50,25));
        button_clear_selected_files = new JButton(i18n.getString("BUTTON_CLEAR_SELECTED_FILES")); // tlacitko pro smazání seznamu vybraných souborů
        button_clear_selected_files.setToolTipText(i18n.getString("BUTTON_CLEAR_SELECTED_FILES_TOOLTIP"));
/* ###zk začátek aplikačního kódu */
        button_load_selected_files = new JButton(i18n.getString("BUTTON_LOAD_SELECTED_FILES")); // tlacitko pro smazání seznamu vybraných souborů
        button_load_selected_files.setToolTipText(i18n.getString("BUTTON_LOAD_SELECTED_FILES_TOOLTIP"));
        button_save_selected_files = new JButton(i18n.getString("BUTTON_SAVE_SELECTED_FILES")); // tlacitko pro smazání seznamu vybraných souborů
        button_save_selected_files.setToolTipText(i18n.getString("BUTTON_SAVE_SELECTED_FILES_TOOLTIP"));
/* ###kk konec aplikačního kódu */
        model_list_vybrane_soubory = new DefaultListModel();
        list_vybrane_soubory = new JList(model_list_vybrane_soubory); // vybrane soubory; byvaly list3
        list_vybrane_soubory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_vybrane_soubory.setSelectionBackground(list_vybrane_soubory.getBackground()); // aby výběr nebyl vidět
        list_vybrane_soubory.setSelectionForeground(list_vybrane_soubory.getForeground());
        //list_vybrane_soubory.setSelectedIndex(0);
        //list_vybrane_soubory.setNextFocusableComponent(button_select_files); // nefunguje
        //list_vybrane_soubory.setRequestFocusEnabled(false);
        vybrane_soubory_scroll_pane= new JScrollPane(list_vybrane_soubory);
        layout_vybrane_soubory=new GridBagLayout();
        layout_vybrane_soubory.setConstraints(button_clear_selected_files, constraint_nadpis_button);
/* ###zk začátek aplikačního kódu */
        layout_vybrane_soubory.setConstraints(button_load_selected_files, constraint_nadpis_button2);
        layout_vybrane_soubory.setConstraints(button_save_selected_files, constraint_nadpis_button3);
/* ###kk konec aplikačního kódu */
        layout_vybrane_soubory.setConstraints(label_vybrane_soubory, constraint_nadpis_right);
        layout_vybrane_soubory.setConstraints(vybrane_soubory_scroll_pane, constraint_vypis_double);
        panel_vybrane_soubory = new JPanel();
        panel_vybrane_soubory.setLayout(layout_vybrane_soubory);
        panel_vybrane_soubory.add(button_clear_selected_files);
        panel_vybrane_soubory.add(label_vybrane_soubory);
/* ###zk začátek aplikačního kódu */
        panel_vybrane_soubory.add(button_load_selected_files);
        panel_vybrane_soubory.add(button_save_selected_files);
/* ###kk konec aplikačního kódu */
        panel_vybrane_soubory.add(vybrane_soubory_scroll_pane);
        JPanel panel_files = new JPanel();
        panel_files.setLayout(new BorderLayout());
        split_vypsane_vybrane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,  split_adresare_soubory, panel_vybrane_soubory);
        split_vypsane_vybrane.setDividerSize(15);
        split_vypsane_vybrane.setContinuousLayout(true);
        split_vypsane_vybrane.setOneTouchExpandable(false);
        split_vypsane_vybrane.setDividerLocation(400);
        panel_files.add(split_vypsane_vybrane, BorderLayout.CENTER);
        layout_zalozka_files = new GridBagLayout();
        setLayout(layout_zalozka_files);
        JPanel panel_buttons_bottom = new JPanel();
/* ###zk začátek aplikačního kódu */
        panel_buttons_bottom.setLayout(new BoxLayout(panel_buttons_bottom, BoxLayout.X_AXIS));
/* ###kk konec aplikačního kódu */
/* ###zt začátek apletovského kódu
         panel_buttons_bottom.setLayout(new BorderLayout());
/* ###kt konec apletovského kódu */

        GridBagConstraints constraint_zalozka_files = new GridBagConstraints(); // pro vkladani vice komponent do zalozky
        // constraints pro panel_server_name
        setConstraints(constraint_zalozka_files, 0, 0, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 4);
        layout_zalozka_files.setConstraints(panel_server, constraint_zalozka_files);
        // constraints pro panel_akt_cesta
        setConstraints(constraint_zalozka_files, 0, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 2);
        layout_zalozka_files.setConstraints(panel_akt_cesta, constraint_zalozka_files); // vlozeni label_l_akt_cesta
        // constraints pro split_vybrane_vypsane
        setConstraints(constraint_zalozka_files, 0, 2, 1.0, 1.0, GridBagConstraints.BOTH, GridBagConstraints.WEST, GridBagConstraints.REMAINDER);
        layout_zalozka_files.setConstraints(panel_files, constraint_zalozka_files); // vlozeni split_vypsane_vybrane
        // constraints pro panel_buttons_bottom
        setConstraints (constraint_zalozka_files, 0, 3, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, GridBagConstraints.REMAINDER);
        layout_zalozka_files.setConstraints (panel_buttons_bottom, constraint_zalozka_files); // vlozeni panel_buttons_bottom
/* ###zk začátek aplikačního kódu */
        edit_auto_load = new JTextField();  // subkorpus pro pristi start Netgraphu
        check_auto_load = new JCheckBox(i18n.getString("CHECK_AUTO_LOAD_SUBCORPUS"));
        check_auto_load.setToolTipText(i18n.getString("CHECK_AUTO_LOAD_SUBCORPUS_TOOLTIP"));
        button_select_auto_load = new JButton(i18n.getString("SELECT_AUTO_LOAD_SUBCORPUS"));
        button_select_auto_load.setToolTipText(i18n.getString("SELECT_AUTO_LOAD_SUBCORPUS_TOOLTIP"));
        panel_buttons_bottom.add(check_auto_load);
        panel_buttons_bottom.add(button_select_auto_load);
        panel_buttons_bottom.add(edit_auto_load);
/* ###kk konec aplikačního kódu */
        button_select_files = new JButton(i18n.getString("BUTTON_SET_SELECTED_FILES")); // nastaveni souboru urcenych k dotazu; byvaly button6
        button_select_files.setToolTipText(i18n.getString("BUTTON_SET_SELECTED_FILES_TOOLTIP"));
/* ###zk začátek aplikačního kódu */
        panel_buttons_bottom.add(button_select_files);
/* ###kk konec aplikačního kódu */
/* ###zt začátek apletovského kódu
        panel_buttons_bottom.add(button_select_files, BorderLayout.EAST);
/* ###kt konec apletovského kódu */
        add(panel_server);
        add(panel_akt_cesta);
        add(panel_files);
        add(panel_buttons_bottom);

        // nastaveni udalosti

        button_up_dir.addActionListener(this); // prechod do nadrazeneho adresare
        button_add_marked_directories.addActionListener(this); // přidání označených adresářů v aktuální cestě k vybraným
        button_add_all_directories.addActionListener(this); // přidání všech adresářů v aktuální cestě k vybraným
        button_add_marked_files.addActionListener(this); // přidání označených souborů v aktuální cestě k vybraným
        button_add_all_files.addActionListener(this); // přidání všech souborů v aktuální cestě k vybraným
        tree_adresare.addTreeExpansionListener(this); // prechod do podadresare
        tree_adresare.addTreeWillExpandListener(this); // prechod do podadresare
        list_vybrane_soubory.addMouseListener(this); // odstraneni souboru z vybranych
        tree_soubory.addMouseListener(this); // vyber souboru dvojkliknutim mysi
/* ###zk začátek aplikačního kódu */
        check_auto_load.addActionListener(this);
/* ###kk konec aplikačního kódu */
        button_select_files.addActionListener(this); // nastavení souborů k dotazu
        button_clear_selected_files.addActionListener(this); // smazání seznamu vybraných souborů
/* ###zk začátek aplikačního kódu */
        button_select_auto_load.addActionListener(this); // výběr subkorpusu pro příští přihlášení
        button_load_selected_files.addActionListener(this); // nahrání uloženého seznamu vybraných souborů
        button_save_selected_files.addActionListener(this); // uložení seznamu vybraných souborů
/* ###kk konec aplikačního kódu */

        // konec nastaveni udalosti



    } // PanelFiles (konstruktor)


    private void debug (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
        if (mess != null) {
            mess.debug(message);
        }
    }

    private void inform (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
        if (mess != null) {
            mess.inform (message);
        }
    }

    private void setConstraints(GridBagConstraints constraints, int gridx, int gridy, double weightx, double weighty, int fill, int anchor, int gridwidth) {
        // nastavi vlastnosti objektu constraints pro GridBagLayout dle dalsich parametru, ktere nejsou nastaveny na -1 (tzn. nenastavovat)
        if (gridx != -1) constraints.gridx = gridx;
        if (gridy != -1) constraints.gridy = gridy;
        if (weightx != -1.0) constraints.weightx = weightx;
        if (weighty != -1.0) constraints.weighty = weighty;
        if (fill != -1) constraints.fill = fill;
        if (anchor != -1) constraints.anchor = anchor;
        if (gridwidth != -1) constraints.gridwidth = gridwidth;
    }


    // --------------------------------- odchycení událostí ----------------------------------


    public void mouseClicked(MouseEvent e) { // kliknutí myší

        Object zdroj = e.getSource();
        int pocet_kliku = e.getClickCount();

        if (zdroj == tree_soubory) {
            int selRow = tree_soubory.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree_soubory.getPathForLocation(e.getX(), e.getY());
            if(selRow != -1) {
                if (pocet_kliku == 2) {
                    String vybrany_soubor = label_akt_cesta.getText() + "/"
                    + selPath.getLastPathComponent().toString();
                    addFile(vybrany_soubor);
                }
            }
        }

        else if (zdroj == list_vybrane_soubory) {
            if (pocet_kliku == 2) { // odstraneni souboru z vybranych
                if (list_vybrane_soubory.isSelectionEmpty() || model_list_vybrane_soubory.getSize() <= 0) {
                    return;
                }
                String odstraneny_soubor = new String(list_vybrane_soubory.getSelectedValue().toString());
                //debug ("\nodstranění souboru " + odstraneny_soubor + " z vybraných");
                model_list_vybrane_soubory.removeElement(list_vybrane_soubory.getSelectedValue());
                list_vybrane_soubory.transferFocus();
            }
            if (pocet_kliku == 1) { // jen se zbavím focusu, abych nezvýrazňoval vybraný soubor
                list_vybrane_soubory.transferFocus();
            }
        }

    } // mouseClicked


    public void mousePressed(MouseEvent e) {return;}
    public void mouseExited(MouseEvent e) {return;}
    public void mouseReleased(MouseEvent e) {return;}
    public void mouseEntered(MouseEvent e) {return;}


    public void actionPerformed(ActionEvent e) { // akce (doubleclick nebo mezerník)
        Object zdroj = e.getSource();

        jaaa.setWaitCursor();

        if (zdroj == button_add_marked_directories) { // přidání označených adresářů z aktuální cesty k vybraným
            //debug ("\nStisknuto tlačítko 'button_add_marked_directories' (přidání označených adresářů v aktuální cestě k vybraným)");
            addMarkedFilesOrDirectories(DIRECTORY);
        }

        else if (zdroj == button_add_all_directories) { // přidání všech adresářů z aktuální cesty k vybraným
            //debug ("\nStisknuto tlačítko 'button_add_all_directories' (přidání všech adresářů v aktuální cestě k vybraným)");
            addAllFilesOrDirectories(DIRECTORY);
        }

        else if (zdroj == button_up_dir) { // prechod na nadrazeny adresar
            //debug ("\nStisknuto tlačítko 'button_up_dir' (přechod do nadřazeného adresáře)");
            changeDirectory("..");
        }

        else if (zdroj == button_add_marked_files) { // přidání označených souborů z aktuální cesty k vybraným
            //debug ("\nStisknuto tlačítko 'button_add_marked_files' (přidání označených souborů v aktuální cestě k vybraným)");
            addMarkedFilesOrDirectories(FILE);
        }

        else if (zdroj == button_add_all_files) { // přidání všech souborů z aktuální cesty k vybraným
            //debug ("\nStisknuto tlačítko 'button_add_all_files' (přidání všech souborů v aktuální cestě k vybraným)");
            addAllFilesOrDirectories(FILE);
        }

        else if (zdroj == button_clear_selected_files) { // smazání seznamu vybraných souborů
            //debug ("\nStisknuto tlačítko 'button_clear_selected_files' (smazání seznamu vybraných souborů)");
            model_list_vybrane_soubory.clear();
        }

/* ###zk začátek aplikačního kódu */
        else if (zdroj == button_load_selected_files) { // nahrání uloženého seznamu vybraných souborů
            //debug ("\nStisknuto tlačítko 'button_load_selected_files' (nahrání uloženého seznamu vybraných souborů)");
            selectedFilesLoadDialog();
        }

        else if (zdroj == button_save_selected_files) { // uložení seznamu vybraných souborů
            //debug ("\nStisknuto tlačítko 'button_save_selected_files' (uložení seznamu vybraných souborů)");
            selectedFilesSaveDialog();
        }

        else if (zdroj == check_auto_load) { // automatické nahrávání korpusu po startu
            //debug ("\nStisknuto tlačítko 'check_auto_load' (automatické nahrávání korpusu po startu)");
            checkAutoLoadChanged();
        }

        else if (zdroj == button_select_auto_load) { // výběr korpusu pro automatické nahrávání po připojení
            //debug ("\nStisknuto tlačítko 'button_select_auto_load' (výběr korpusu pro automatické nahrávání po připojení)");
            selectedFilesAutoLoadDialog();
        }
/* ###kk konec aplikačního kódu */

        else if (zdroj == button_select_files) { // nastavení souborů určených k dotazu
            //debug ("\nStisknuto tlačítko 'button_select_files' (nastavení souborů určených k dotazu)");
            if (jaaa.kom_net.isConnected()) {
                nastavSoubory();
            }
            else {
                inform("YOU_ARE_DISCONNECTED");
            }
        }

        jaaa.setDefaultCursor();

    }	// actionPerformed


    public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException {
        Object zdroj = e.getSource();

        if (zdroj == tree_adresare) { // přechod do podadresáře
            jaaa.setWaitCursor();
            try { // kolabovalo při startu apletu
                TreePath selPath = e.getPath();
                String podadresar = new String(selPath.getLastPathComponent().toString());
                //debug ("\nPřechod do podadresáře " + podadresar);
                changeDirectory(podadresar);
                throw new ExpandVetoException(e); // zrušení expanze
            }
            catch (Exception ex) {};
            jaaa.setDefaultCursor();
        }
    }

    public void treeWillCollapse(TreeExpansionEvent e) {return;}
    public void treeExpanded(TreeExpansionEvent e) {return;}
    public void treeCollapsed(TreeExpansionEvent e) {return;}

/* ###zk začátek aplikačního kódu */
    public void saveActionsDisconnect() { // ukládání informací na disk při odpojování od serveru
        saveSavedSelectedFiles(); // uložím uložené subkorpusy na disk
    }

    public void saveActionsExit() { // ukládání informací na disk před ukončením aplikace (akce z saveActionsDisconnect by se tu neměly opakovat)
        return;
    }

    public void readGeneralProperties(Properties properties) { // přečtu properties pro tento objekt
        //check_auto_load.setSelected(properties.getBooleanProperty("auto load","check load subcorpus", check_auto_load.isSelected()));
        //edit_auto_load.setText(properties.getStringProperty("auto load","load subcorpus name", edit_auto_load.getText()));
        //checkAutoLoadChanged();
        return;
    } // readGeneralProperties

    public void writeGeneralProperties(Properties properties) { // zapíše properties z tohoto objektu
        //properties.updateProperty("auto load","check load subcorpus",""+check_auto_load.isSelected(),"load automaticaly a subcorpus after Netgraph has started? (true, false)");
        //properties.updateProperty("auto load","load subcorpus name",edit_auto_load.getText(),"the name of subcorpus to be automaticaly loaded after start (string)");
        return;
    } // writeGeneralProperties
/* ###kk konec aplikačního kódu */

     public void connected() { // tato funkce je volána z třídy NGClient bezprostředně po připojení k serveru
         jaaa.setWaitCursor();
/* ###zk začátek aplikačního kódu */
         String auto_load_name;
         try {
             auto_load_name = loadAutoLoadSubcorpusName(); // získám případné jméno subkorpusu, který má být automaticky použit k prohledávání
         }
         catch (Exception e) {
             auto_load_name = "";
         }
         if (auto_load_name.length() > 0) { // pokud takové jméno je určeno
             check_auto_load.setSelected(true);
             edit_auto_load.setText(auto_load_name);
             boolean success = loadSubcorpus(auto_load_name);
             if (success) nastavSoubory();
         }
         else {
             check_auto_load.setSelected(false);
             edit_auto_load.setText("");
         }
/* ###kk konec aplikačního kódu */
/* ###zt začátek apletovského kódu
         if (jaaa.kom_net.getServerVersion().compareToIgnoreCase("1.73") >= 0) { // server umí zpracovat adresář v seznamu vybraných souborů
             model_list_vybrane_soubory.addElement(jaaa.kom_net.getInitialPath()); // applet si automaticky nastaví celý korpus k prohledávání
             nastavSoubory();
         }
/* ###kt konec apletovského kódu */
         jaaa.setDefaultCursor();
         return;
     } // connected

     public void disconnected() { // tato funkce je volána z třídy NGClient bezprostředně po odpojení od serveru
         clearAllInfo();
     }

/* ###zk začátek aplikačního kódu */
    private boolean loadSubcorpus(String name) {
        boolean success = false;
        PropertiesSection subcorpus = properties_saved_selected_files.getSection(name); // vezmu si tu vybranou sekci
        if (subcorpus == null) {
            debug("\nPanelFiles.loadSubcorpus: subkorpus se jménem " + name + " neexistuje!");
            return false;
        }
        Iterator iterator = subcorpus.getIteratorOverValues(); // iterátor přes prvky subkorpusu - jména souborů
        model_list_vybrane_soubory.clear(); // smažu bývalý seznam vybraných souborů
        Property file; // sem budu načítat jednotlivá jména souborů
        String file_name; // sem jejich stringovou podobu
        while (iterator.hasNext()) { // přes všechny soubory a adresáře
            file = (Property)iterator.next(); // další soubor či adresář
            file_name = file.getValue(); // obsah property - vlastní jméno souboru či adresáře
            addDirectory(file_name); // zařadím tento soubor či adresář do seznamu vybraných souborů a adresářů - addDirecotory přidá soubory i adresáře
            success = true;
            // !!! tady není potřeba ověřit oprávněnost přístupu k danému souboru, to se ověřuje v serveru?
        }
        return success;
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */

    private String openSelectedFilesLoadDialog() { // výběr uloženého subkorpusu

        if (properties_saved_selected_files == null) loadSavedSelectedFiles(); // první volání - nahraji subkorpusy z disku

        ItemSelectionDialog sd = new ItemSelectionDialog(jaaa,properties_saved_selected_files,i18n,"SELECTED_FILES_LOAD_DIALOG_");
        sd.setPreview(false); // seznamy souborů mohou být dlouhé a zobrazování může být pomalé
        //debug("\nOtvírám dialogové okno pro výběr subkorpusu...");
        boolean selected = sd.showOpenDialog();
        if (selected) { // uživatel vybral položku
            //debug("\nVybrán subkorpus se jménem " + sd.getSelectedName());
        }
        else { // uživatel zrušil výběr
            //debug("\nUživatel zrušil výběr.");
            return null;
        }
        return sd.getSelectedName();
    }
/* ###kk konec aplikačního kódu */


/* ###zk začátek aplikačního kódu */
    private void selectedFilesAutoLoadDialog() { // výběr uloženého subkorpusu pro automatické nahrání při příštím přihlášení
        String subcorpus_name = openSelectedFilesLoadDialog();
        if (subcorpus_name != null) {
            edit_auto_load.setText(subcorpus_name);
            check_auto_load.setSelected(true);
            checkAutoLoadChanged();
        }
    }
/* ###kk konec aplikačního kódu */


/* ###zk začátek aplikačního kódu */
    private void selectedFilesLoadDialog() { // výběr a nahrání uloženého seznamu vybraných souborů

        String subcorpus_name = openSelectedFilesLoadDialog();
        if (subcorpus_name != null) {
            loadSubcorpus(subcorpus_name);
        }
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private void selectedFilesSaveDialog() { // výběr jména a komentáře a uložení seznamu vybraných souborů
        int number_of_files = model_list_vybrane_soubory.getSize(); // počet prvků v ukládaném subkorpusu
        if (number_of_files == 0) return; // není co ukládat

        if (properties_saved_selected_files == null) loadSavedSelectedFiles(); // první volání - nahraji subkorpusy z disku

        ItemSelectionDialog sd = new ItemSelectionDialog(jaaa,properties_saved_selected_files,i18n,"SELECTED_FILES_SAVE_DIALOG_");
        //debug("\nOtvírám dialogové okno pro výběr subkorpusu...");
        boolean selected = sd.showSaveDialog();
        if (selected) { // uživatel vybral položku
            //debug("\nVybrán subkorpus se jménem " + sd.getSelectedName());
        }
        else { // uživatel zrušil výběr
            //debug("\nUživatel zrušil výběr.");
            return;
        }
        // subkorpus se uloží jako jedna sekce
        String subcorpus_name = sd.getSelectedName(); // toto se uloží jako jméno sekce
        String subcorpus_comment = sd.getSelectedComment(); // komentář sekce
        if (subcorpus_comment == null) subcorpus_comment = "";

        String file_value; // zde se bude vždy uchovávat jedno jméno souboru
        String file_name_name; // zde se bude generovat jméno jména souboru
        Property file; // zde se vytvoří vždy jedna property pro jedno jméno souboru v subkorpusu
        for (int i=0; i<number_of_files; i++) { // přes všechny soubory v subkorpusu
            file_value = (String)model_list_vybrane_soubory.elementAt(i); // jeden soubor subkorpusu
            file_name_name = saved_selected_files_prefix + (number_of_files - i); // vytvořím jméno jména souboru (s pořadovým číslem jako odlišujícím prvkem); sestupné pořadí zachová pořádí v seznamu
            file = new Property(subcorpus_name,file_name_name,file_value); // vytvořím property uchovávající jedno jméno souboru
            properties_saved_selected_files.setProperty(file); // uložím property
        }
        properties_saved_selected_files.getSection(subcorpus_name).setComment(subcorpus_comment); // na závěr přidám komentář
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private void addWholeCorpusToListOfSavedSubcorpora() { // k seznamu uložených subkorpusů přidá celý korpus
        String file_value = jaaa.kom_net.getInitialPath(); // jeden soubor subkorpusu
        String file_name_name = saved_selected_files_prefix + "1"; // vytvořím jméno jména souboru (je to jediný adresář)
        Property file = new Property("whole corpus",file_name_name,file_value); // vytvořím property uchovávající jedno jméno souboru
        properties_saved_selected_files.setProperty(file); // uložím property
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private void removeWholeCorpusFromListOfSavedSubcorpora() { // ze seznamu uložených subkorpusů odstraní celý korpus
        properties_saved_selected_files.removeSection("whole corpus");
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private void removeAutoLoadFromListOfSavedSubcorpora() { // ze seznamu uložených subkorpusů odstraní případný odkaz na korpus, který má být nahrán automaticky
        properties_saved_selected_files.removeSection("auto load subcorpus");
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private Properties createDefaultListOfSavedSubcorpora() {
        //debug("\nVytvářím implicitní seznam uložených subkorpusů.");
        Properties properties = new Properties();
        return properties;
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private String readAutoLoadSubcorpusName() { // v paměti uložených subkorpusech najdu jméno subkorpusu, který má být automaticky použit k prohledávání, pokud je uvedeno
        if (properties_saved_selected_files == null) {
            return "";
        }
        String name = properties_saved_selected_files.getStringProperty("auto load subcorpus", "name", "");
        return name;
    } // loadAutoLoadSubcorpusName
/* ###kk konec aplikačního kódu */


/* ###zk začátek aplikačního kódu */
    private String loadAutoLoadSubcorpusName() { // nahraji uložené subkorpusy z disku a najdu v nich jméno subkorpusu, který má být automaticky použit k prohledávání, pokud je uvedeno
        PropertiesLoader loader = new PropertiesLoader(jaaa);
        properties_saved_selected_files = loader.loadProperties("netgraph","saved_subcorpora_" + jaaa.kom_net.getCorpusIdentifier());
        if (properties_saved_selected_files == null) {
            properties_saved_selected_files = createDefaultListOfSavedSubcorpora();
        }
        String name = readAutoLoadSubcorpusName();
        removeAutoLoadFromListOfSavedSubcorpora();
        addWholeCorpusToListOfSavedSubcorpora();
        return name;
    } // loadAutoLoadSubcorpusName
/* ###kk konec aplikačního kódu */


/* ###zk začátek aplikačního kódu */
    private void loadSavedSelectedFiles() { // nahraji uložené subkorpusy z disku
        PropertiesLoader loader = new PropertiesLoader(jaaa);
        properties_saved_selected_files = loader.loadProperties("netgraph","saved_subcorpora_" + jaaa.kom_net.getCorpusIdentifier());
        if (properties_saved_selected_files == null) {
            properties_saved_selected_files = createDefaultListOfSavedSubcorpora();
        }
        removeAutoLoadFromListOfSavedSubcorpora();
        addWholeCorpusToListOfSavedSubcorpora();
    } // loadSavedSelectedFiles
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private void saveSavedSelectedFiles() { // uložím uložené subkorpusy na disk
        PropertiesLoader loader;
        if (properties_saved_selected_files == null) { // pokud v paměti neexistuje seznam uložených subkorpusů
          loader = new PropertiesLoader(jaaa);
          properties_saved_selected_files = loader.loadProperties("netgraph",
              "saved_subcorpora_" + jaaa.kom_net.getCorpusIdentifier());
        }
        if (properties_saved_selected_files == null) { // pokud stále v paměti neexistuje seznam uložených subkorpusů
            if (!check_auto_load.isSelected()) { // a pokud se nemá uložit ani jméno subkorpusu pro automatické načtení po příštím přihlášení
                return; //není co ukládat
            }
            else {
                //debug("\nVytvářím implicitní seznam uložených subkorpusů.");
                properties_saved_selected_files = new Properties();
            }
        }
        if (check_auto_load.isSelected()) {
            Property file = new Property("auto load subcorpus", "name", edit_auto_load.getText()); // vytvořím property uchovávající jedno jméno souboru
            properties_saved_selected_files.setProperty(file); // uložím property
            properties_saved_selected_files.getSection("auto load subcorpus").setComment(
              "this subcorpus will be automaticaly used for searching at the start of next connection to the server"); // na závěr přidám komentář
        }
        removeWholeCorpusFromListOfSavedSubcorpora();
        loader = new PropertiesLoader(jaaa);
        loader.saveProperties("netgraph","saved_subcorpora_" + jaaa.kom_net.getCorpusIdentifier(), properties_saved_selected_files);
        properties_saved_selected_files = null; // zapomenu properties z tohoto přihlášení, kdybych se teď znovu připojoval jinam
    }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
    private void checkAutoLoadChanged() { // uživatel změnil volbu, zda automaticky nahrávat korpus při startu
        if (check_auto_load.isSelected()) { // uživatel volbu aktivoval
            edit_auto_load.setEnabled(true);
        }
        else { // uživatel volbu zrušil
          edit_auto_load.setEnabled(false);
        }
    }
/* ###kk konec aplikačního kódu */

    private boolean jeVSeznamu(DefaultListModel lm, String s, int last_index) { // vrátí true, pokud řetezec 's' je již v seznamu 'lm' do pozice last_index, jinak false
        String ps; // prvek seznamu
        int size = lm.getSize();
        if (last_index >= 0 && last_index < size) size = last_index + 1; // omezím prohledávání jen do last_indexu
        for (int i=0; i<size; i++) { // přes celý seznam
            ps = (String)lm.getElementAt(i);
            //debug("\nPorovnávám prvky " + s + " a " + ps);
            if (s.equalsIgnoreCase(ps)) return true; // prvek nalezen
        }
        return false; // prvek nebyl nalezen
    }

    private boolean jeVSeznamu(DefaultListModel lm, String s) { // vrátí true, pokud řetezec 's' je již v seznamu 'lm', jinak false
        return jeVSeznamu(lm,s,-1);
    }

    public void clearAllInfo() { // vyprázdní všechna pole a seznamy
        vyprazdniServerInfo();
        model_list_vybrane_soubory.clear();
        model_tree_adresare.setRoot(new DefaultMutableTreeNode());
        model_tree_soubory.setRoot(new DefaultMutableTreeNode());
/* ###zk začátek aplikačního kódu */
        check_auto_load.setSelected(false);
        edit_auto_load.setText("");
        checkAutoLoadChanged();
/* ###kk konec aplikačního kódu */
    }

    void naplnTreeSouboru(JTree t, DefaultTreeModel tm, byte buffer[]) { // naplni seznam 'tm' polozkami obsazenymi v poli 'buffer'
        // predpoklada, ze polozky jsou oddeleny znakem ServerCommunication.oddelovac
        // volana programem pro zpracovani nekterych odpovedi serveru
        // 'tm' - vstup/vystup - seznam, ktery bude naplnen
        // 't' - vstup - strom zobrazujici model 'tm'
        // 'buffer' - vstup - pole obsahujici data pro naplneni

        boolean pokracovani = false; // identifikátor stavu čtení více částí dlouhého seznamu souborů
        boolean prvni = true; // následující část bude první (možná i poslední)

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(); // novy root stromu tm - maze se obsah
        String name; // jmeno zarazovaneho souboru
        int kam_do_stromu=0; // poradi zarazovanych uzlu
        int odkud; // ukazatel do bufferu

        tm.setRoot(root);

        while (pokracovani || prvni) { // dokud jsem nezpracoval všechny části seznamu (může jich být víc u dlouhých seznamů)

            jaaa.kom_net.precti_sbr(prvni); // zazadam o soubory v aktualni ceste

            //debug ("\nIndikátor úplnosti seznamu = " + (char)buffer[1]);

            if (buffer[0] != (byte)'O') {
                debug("\nNGClient.PanelFiles.naplnTreeSouboru: Chyba v komunikaci se serverem při načítání seznamu souborů.");
                inform("SERVER_COMMUNICATION_ERROR");
                return;
            }

            if (prvni && buffer[1]!=(byte)'f' && buffer[1]!=(byte)'o') { // chyba
                debug("\nNGClient.PanelFiles.naplnTreeSouboru: Chyba 1 synchronizace částí seznamu při načítání seznamu souborů.");
                inform("SERVER_COMMUNICATION_ERROR");
                return;
            }

            if (pokracovani && buffer[1]!=(byte)'m' && buffer[1]!=(byte)'l') { // chyba
                debug("\nNGClient.PanelFiles.naplnTreeSouboru: Chyba 2 synchronizace částí seznamu při načítání seznamu souborů.");
                inform("SERVER_COMMUNICATION_ERROR");
                return;
            }

            prvni=false;
            if (buffer[1] == (byte)'m' || buffer[1] == (byte)'f') pokracovani = true; // bude následovat další část od serveru
            else pokracovani = false; // toto je poslední část seznamu souborů od serveru

            // čtu jednotlivé soubory a přidávám je do stromu
            odkud = 2;
            for (int poz = 2; buffer[poz] != ServerCommunication.EOM; poz++) {
                if (buffer[poz] == (byte)ServerCommunication.oddelovac) {
                  name = new String(buffer, odkud, poz-odkud);
                  tm.insertNodeInto(new DefaultMutableTreeNode(name), root, kam_do_stromu);
                  kam_do_stromu++;
                  // tady původně byl deprecated constructor String(buffer, 0, odkud, poz-odkud)
                  odkud = poz + 1;
                }
            }
        } // while přes části seznamu posílané zvlášť
        t.setRootVisible(true);
        t.expandRow(0);
        t.setRootVisible(false);
    } // naplnTreeSouboru


    void naplnTreeAdresaru(JTree t, DefaultTreeModel tm, byte buffer[]) { // naplni seznam 'tm' polozkami obsazenymi v poli 'buffer'
        // predpoklada, ze polozky jsou oddeleny znakem ServerCommunication.oddelovac
        // volana programem pro zpracovani nekterych odpovedi serveru (naplneni seznamu adresaru)
        // 'tm' - vstup/vystup - seznam, ktery bude naplnen
        // 't' - vstup - strom zobrazujici model 'tm'
        // 'buffer' - vstup - pole obsahujici data pro naplneni

        boolean pokracovani = false; // identifikátor stavu čtení více částí dlouhého seznamu souborů
        boolean prvni = true; // následující část bude první (možná i poslední)

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(); // novy root stromu tm - maze se obsah
        DefaultMutableTreeNode adr;

        int odkud;
        int kam_do_stromu=0; // poradi zarazovanych uzlu

        tm.setRoot(root);

        while (pokracovani || prvni) { // dokud jsem nezpracoval všechny části seznamu (může jich být víc u dlouhých seznamů)

            jaaa.kom_net.precti_adr(prvni); // zazadam o adresare v aktualni ceste

            if (buffer[0] != (byte)'O') {
                debug("\nNGClient.PanelFiles.naplnTreeSouboru: Chyba v komunikaci se serverem při načítání seznamu souborů.");
                inform("SERVER_COMMUNICATION_ERROR");
                return;
            }

            if (prvni && buffer[1]!=(byte)'f' && buffer[1]!=(byte)'o') { // chyba
                debug("\nNGClient.PanelFiles.naplnTreeAdresaru: Chyba 1 synchronizace částí seznamu při načítání seznamu adresářů.");
                return;
            }
            if (pokracovani && buffer[1]!=(byte)'m' && buffer[1]!=(byte)'l') { // chyba
                debug("\nNGClient.PanelFiles.naplnTreeAdresaru: Chyba 2 synchronizace částí seznamu při načítání seznamu adresářů.");
                return;
            }

            prvni=false;
            if (buffer[1] == (byte)'m' || buffer[1] == 'f') pokracovani = true; // bude následovat další část od serveru
            else pokracovani = false; // toto je poslední část seznamu souborů od serveru

            // čtu jednotlivé adresáře a přidávám je do stromu
            odkud = 2;
            for (int poz = 2; buffer[poz] != jaaa.kom_net.EOM; poz++) {
                if (buffer[poz] == (byte)ServerCommunication.oddelovac) {
                    adr = new DefaultMutableTreeNode(new String(buffer, odkud, poz-odkud));
                    // tady původně byl deprecated constructor String (buffer, 0, odkud, poz-odkud)
                    //adr.setAllowsChildren(false);
                    tm.insertNodeInto(adr, root, kam_do_stromu++);
                    tm.insertNodeInto(new DefaultMutableTreeNode(new String("")), adr, 0);
                    odkud = poz + 1;
                }
            }
            tm.insertNodeInto(adr = new DefaultMutableTreeNode(new String("..")), root, 0);
            tm.insertNodeInto(new DefaultMutableTreeNode(new String("")), adr, 0);
        } // while přes části seznamu posílané zvlášť

        t.setRootVisible(true);
        t.expandRow(0);
        t.setRootVisible(false);
    } // naplnTreeAdresaru



    void naplnAdresareASoubory() {  // naplni nektere prvky formulare pro vyber souboru s daty: aktualni cestu, seznam podadresaru a seznam souboru
        // volana pri inicializaci programu a pri zmene adresare

        String path = jaaa.kom_net.getActualPath(); // do pole zpravy nacte aktualni cestu a jeji obsah

        label_akt_cesta.setText(path); // zobrazim aktualni cestu

        // zazadam o adresare v aktualni ceste
        naplnTreeAdresaru(tree_adresare, model_tree_adresare, jaaa.kom_net.zprava);

        // zazadam o soubory v aktualni ceste
        naplnTreeSouboru(tree_soubory, model_tree_soubory, jaaa.kom_net.zprava);
    } // naplnAdresareASoubory

    void naplnServerInfo() { // naplni informace o pripojenem serveru
        label_login_name.setText(jaaa.user_account.getLoginName());
        label_server_name.setText(jaaa.kom_net.getServerName());
        label_server_port.setText(new Integer(jaaa.kom_net.getServerPort()).toString());
        label_server_version.setText(jaaa.kom_net.getServerVersion());
    }

    void vyprazdniServerInfo() { // vyprazdni informace o pripojenem serveru
        label_login_name.setText("");
        label_server_name.setText("");
        label_server_port.setText("");
        label_server_version.setText("");
        label_akt_cesta.setText("");
    }

    private void changeDirectory(String directory) { // přechod do vybraného adresáře
        // volano pri udalosti vyberu adresare
        // 'directory' - vstup - jméno adresáře (rel. cesta)
        jaaa.kom_net.changeDir(directory);
        naplnAdresareASoubory();
    } // PrejdiDoAdresare

    private boolean filtrujTyp(String soubor, String pripona) { // zjistí, zda soubor má příponu pripona
        boolean konci;
        konci = soubor.endsWith(pripona);
        return konci;
    }

    private void addDirectory(String directory) { // přidá adresář k vybraným
        addFileOrDirectory(DIRECTORY, directory, -1);
    }

    private void addFile(String soubor) { // přidá soubor k vybraným
        addFileOrDirectory(FILE, soubor, -1);
    }

    private void addFileOrDirectory(int file_or_dir, String soubor, int last_index) { // přidá soubor nebo adresář k vybraným, kontroluje až do last_indexu, jestli tam už není
        // pokud last_index == 0, tak nekontroluje, jestli tam už není
        //debug ("\nhromadný výběr souboru či adresáře " + soubor);
        if (file_or_dir == FILE) {
            if (!filtrujTyp(soubor, ".fs")) { // jestliže nejde o soubor s příponou fs
                debug("\nSoubor " + soubor + " vynechán - není typu fs");
                return;
            }
        }
        if (!jeVSeznamu(model_list_vybrane_soubory, soubor, last_index)) { // jestliže tam ještě není
            model_list_vybrane_soubory.addElement(soubor);
        }
        else debug("\nSoubor či adresář " + soubor + " již v seznamu vybraných souborů je.");
    }

    private void addMarkedFilesOrDirectories(int file_or_dir) { // přidá označené soubory nebo adresáře v aktuální cestě k vybraným
        JTree tree;
        if (file_or_dir == DIRECTORY) {
            tree = tree_adresare;
        }
        else {
            tree = tree_soubory;
        }
        int poc_row = tree.getRowCount(); // počet listů (tj. souborů)
        int poradi = 0; // seznam souborů začnu procházet od prvního
        String soubor;
        String last_path_component;
        int last_index = model_list_vybrane_soubory.size() - 1; // index posledního již dříve vybraného souboru
        if (last_index < 0) last_index = 0; // -1 by znamenala kontrolu přes všechny již dříve přidané soubory
        while (poradi < poc_row) { // než projdu všechny soubory
            if (tree.isRowSelected(poradi)) {
                TreePath cesta = tree.getPathForRow(poradi); // stromová cesta k dalšímu souboru
                last_path_component = cesta.getLastPathComponent().toString();
                soubor = label_akt_cesta.getText() + "/"
                + last_path_component;
                if (last_path_component.charAt(0) == '.') {
                    debug("\nSoubor či adresář " + soubor + " začíná znakem '.' - vynechán.");
                }
                else {
                    addFileOrDirectory(file_or_dir, soubor, last_index);
                }
            }
            poradi++;
        }
    }

    private void addAllFilesOrDirectories(int file_or_dir) { // přidá všechny soubory nebo adresáře v aktuální cestě k vybraným
        JTree tree;
        if (file_or_dir == DIRECTORY) {
            tree = tree_adresare;
        }
        else {
            tree = tree_soubory;
        }
        int poc_row = tree.getRowCount(); // počet listů (tj. souborů)
        int poradi = 0; // seznam souborů začnu procházet od prvního
        String soubor;
        String last_path_component;
        int last_index = model_list_vybrane_soubory.size() - 1; // index posledního již dříve vybraného souboru
        if (last_index < 0) last_index = 0; // -1 by znamenala kontrolu přes všechny již dříve přidané soubory
        while (poradi < poc_row) { // než projdu všechny soubory
            TreePath cesta = tree.getPathForRow(poradi); // stromová cesta k dalšímu souboru
            last_path_component = cesta.getLastPathComponent().toString();
            soubor = label_akt_cesta.getText() + "/"
            + last_path_component;
            if (last_path_component.charAt(0) == '.') {
                debug("\nSoubor či adresář " + soubor + " začíná znakem '.' - vynechán.");
            }
            else {
                addFileOrDirectory(file_or_dir, soubor, last_index);
            }
            poradi ++;
        }
    }

    public void nastavSoubory() { // pošle serveru žádost o nastavení souborů pro dotaz
        if (model_list_vybrane_soubory.isEmpty()) { // žádné vybrané soubory
            inform("NO_SELECTED_FILES");
            return;
        }
        int i,j;
        byte [] output = jaaa.kom_net.zprava;
        byte oddelovac = jaaa.kom_net.oddelovac;
        int maxdelka = jaaa.kom_net.maxlenmes - 2; // 1 místo pro endznak, 1 místo rezerva
        String polozka;
        char [] polozka_chars;
        int delka_polozky;
        int poradi_casti = 0; // kolikátou část seznamu posílám serveru
        output[0] = (byte)'F'; // SETFILES
        int delka = 2; // první místo je 'F', na druhém bude rozlišení, zda seznam je kompletní
        //String s = "Fc"; // SETFILES (with flag 'complete')

        inform("SETTING_FILES..."); // tohle se nezobrazí v interní liště, chtělo by to jiný thread; kupodivu při automatickém načtení korpusu při startu se to zobrazí

        try {
            for (i = 0; i < model_list_vybrane_soubory.getSize(); i++) {
                polozka = (String)model_list_vybrane_soubory.getElementAt(i);
                delka_polozky = polozka.length(); // zde nepočítám i oddělovač položek
                // v násl. podmínce už připočítávám oddělovač položek
                if (delka + 1 + delka_polozky >= maxdelka) { // je potřeba odeslat neúplný seznam
                    poradi_casti++;
                    debug("\nSeznam souborů je velmi dlouhý; odesílám " + poradi_casti + ". část.");
                    if (poradi_casti == 1) { // posílám první z více částí
                        output[1] = (byte)'f'; // PART_FIRST
                    }
                    else { // posílám střední (ani první ani poslední) z více částí)
                        output[1] = (byte)'m'; // PART_MIDDLE
                    }

                    output[delka++] = jaaa.kom_net.EOM; // ukončovací znak zprávy

                    jaaa.kom_net.send(jaaa.kom_net.zprava, delka);
                    jaaa.kom_net.receive(jaaa.kom_net.zprava, jaaa.kom_net.EOM);

                    if (jaaa.kom_net.zprava[0] != (byte)'O') {
                        debug("\nError in setting files!");
                        inform("FILES_SET_KO");
                        return;
                    }
                    else debug("\nCast souboru odeslana v poradku - server potvrdil.");
                    // a pokračuji od začátku pole output
                    output[0] = (byte)'F'; // SETFILES
                    delka = 2;
                }
                else {
                    output[delka++] = oddelovac; // oddělovací znak
                }
                polozka_chars = polozka.toCharArray();
                for (j = 0; j < delka_polozky; j++) { // nyní přidám položku do zprávy
                    output[delka++] = (byte)polozka_chars[j];
                }
            } // for přes všechny položky seznamu souborů
            poradi_casti++;
            debug("\nOdesílám poslední z " + poradi_casti + " částí.");
            if (poradi_casti == 1) { // posílám jedinou část
                output[1] = (byte)'o'; // PART_ONLY
            }
            else { // posílám poslední z více částí)
                output[1] = (byte)'l'; // PART_LAST
            }
            output[delka++] = jaaa.kom_net.EOM; // ukončovací znak zprávy
        }
        catch (ArrayIndexOutOfBoundsException e) { // přeteklo pole
            debug("\nError in setting files - přetečení pole - to by vůbec nemělo nastat!");
            inform("FILES_SET_KO");
            return;
        }

        jaaa.kom_net.send(jaaa.kom_net.zprava, delka);
        jaaa.kom_net.receive(jaaa.kom_net.zprava, jaaa.kom_net.EOM);

        if (jaaa.kom_net.zprava[0] == (byte)'O') {
            jaaa.zalozka_trees.split_atributy.setDividerLocation(jaaa.zalozka_trees.split_atributy_divider_location);
            // v tuto chvíli je už aplet vykreslen, a tak mohu
            // nastavit pozici rozdělovače u atributů, což jinak s tímto místem nijak nesouvisí
            debug("\nFiles set OK");
            inform("FILES_SET_OK");
        }
        else	{
            debug("\nError in setting files!");
            inform("FILES_SET_KO");
            return;
        }

        int dist = ServerCommunication.findByte(jaaa.kom_net.zprava, 1, ServerCommunication.EOM); // vrati pozici znaku EOM ve zprave (vzdalenost od pozice 1)
        String vysledek = ServerCommunication.getString(jaaa.kom_net.zprava, 1, dist); // odriznu EOM
        vysledek = vysledek + "\n" + (char)ServerCommunication.EOM; // a pridam novy radek a EOM (cteni hlavicky konci prazdnym radkem)

        /*String vysledek = "";
        int b;
        char pom[];
        pom = new char[1];
        for (j = 1; jaaa.kom_net.zprava[j] != jaaa.kom_net.EOM; j++) {
            b = jaaa.kom_net.zprava[j];
            if (b < 0) b += 256; // jednobajtový unsigned char u serveru, zde to čtu do signed integer; proto tahle konverze
            pom[0] = (char)b;
            vysledek += new String(pom);
        }*/
        //System.out.println("výsledek = " + vysledek);
        jaaa.zalozka_query.naplnGlobalniHlavicku(vysledek);
        jaaa.hlavni_zalozky.setSelectedIndex(1); // prepnuti zalozky na dotazovani
    }



} // class PanelFiles

