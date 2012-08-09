

// ====================================================================================================
//		class PanelTrees		prohlížení stromů - výsledku dotazu
// ====================================================================================================

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*; // printing on a printer
import java.awt.font.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.ResourceBundle;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.trees.*;
/* ###zk začátek aplikačního kódu */
import cz.cuni.mff.mirovsky.properties.*;
/* ###kk konec aplikačního kódu */


/**
 * Class PanelTrees creates a panel for displaying results of the search. The tree and the sentence are displayed,
 * attributes can be selected to be displayed at the nodes, the result can be navigated through using buttons.
 */
public class PanelTrees extends JPanel implements ActionListener, MouseListener, TreeLoadedListener
/* ###zk začátek aplikačního kódu */
    ,Printable
/* ###kk konec aplikačního kódu */
    {

    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    private NGClient jaaa;
    private int font_size_sentence; // velikost fontu ve větě
    private int font_height; // výška řádku ve větě
    static final private int max_lines_sentence = 3; // kolik maximálně řádek se má zobrazit ve větě najednou (pro víc řádek se zobrazí ScrollPane)

        private NGForest forest_actual; // aktuální strom
/* ###zk začátek aplikačního kódu */
        private NGForest forest_printed; // strom tisknutý na tiskárnu
/* ###kk konec aplikačního kódu */


        JEditorPane edit_veta;  // editační pole pro zobrazení věty
        JScrollPane edit_veta_scroll_pane; // scroll_pane pro edit_veta

        JLabel label_jmeno_souboru; // popisek následujícího editačního pole
        JTextField edit_jmeno_souboru; // editační pole pro zobrazní názvu souboru, ze kterého je věta

        JButton button_prev_context;  // předchozí kontext aktuálního stromu
        JButton button_next_context;  // následující kontext aktuálního stromu

        JButton button_prev_occurrence;  // předchozí výskyt dotazu
        JButton button_next_occurrence;  // následující výskyt dotazu

        JButton button_prev_tree;  // první výskyt v předchozím stromě
        JButton button_next_tree;  // první výskyt v následujícím stromě

        JButton button_first_tree;  // první výskyt dotazu

        JButton button_actions; // pro různé akce se stromem

        JPopupMenu menu_actions;
        JMenuItem menu_actions_remove_occurrence;

        JButton button_show_hide; // na zobrazení/skrytí skrývaných vrcholů

        JPopupMenu menu_show_hide;
        JCheckBoxMenuItem menu_show_hide_hidden_nodes;
        JMenu menu_show_hide_references;

        JButton button_statistics; // na zobrazení statistik o prohledávání

        JSplitPane split_atributy_strom_view;  // SplitPane mezi atributy a vykresleným stromem

        JSplitPane split_atributy; // split_pane mezi tabulkou atributů a zobrazovanými atributy
        double split_atributy_divider_location; // procentuálně pozice rozdělovače

        JPanel panel_atributy;  // panel pro výběr zobrazovaných atributů

        //JButton button_prev_sada; // volba předchozí sady atributů
        //JButton button_next_sada; // volba následující sady atributů

        //JTextField text_sada; // zvolená sada atributů
        //JPanel panel_sada; // sdružení tlačítek a zvolené sady

        DefaultListModel model_list_actual_head; // seznam objektů atributů

        int sirka_druheho_sloupce; // pro uchování šířky druhého sloupce tabulky během jejího plnění
        int sirka_tretiho_sloupce; // pro uchování šířky tretiho sloupce tabulky během jejího plnění
        JTable table_atributy;
        JScrollPane table_atributy_scroll_pane;

        DefaultListModel model_list_vybrane_atributy;  // model pro list_vybrane_atributy
        JList list_vybrane_atributy;  // vybrané atributy - zobrazované u vrcholů stromu
        JScrollPane list_vybrane_atributy_scroll_pane; // scroll_pane pro list_vybrane_atributy

        public NGForestDisplay strom_view;  // panel pro kreslení stromů
        JScrollPane strom_view_scroll_pane; // scroll_pane pro strom_view

        GridBagLayout layout_panel_trees; // layout pro zalozku pro prohlizeni stromu

/* ###zk začátek aplikačního kódu */
        // pro tisk na tiskárnu:
        PrintTreeDialog print_dialog; // objekt pro dialogové okno nastavení tisku
        PrinterJob printer_job; // pro tisk na tiskárnu
        NGForestPrint forest_print; // objekt pro tisk stromů na tiskárnu
        double im_height, im_width, im_y, im_x; // rozměry a umístění tisknutelné plochy

        // pro ukládání na lokální disk:
        public ResultTreesLocalSaver result_trees_local_saver; // objekt pro ukládání výsledných stromů na lokální disk
/* ###kk konec aplikačního kódu */

        public long number_of_actual_occurrence;
        public long number_of_actual_tree;
        public long number_of_found_occurences;
        public long number_of_found_trees;
        long number_of_searched_trees;

        private long number_buffer; // pomocná proměnná pro předávání parametru typu long

        public PanelTrees(NGClient p_jaaa, ShowMessagesAble p_mess, ResourceBundle p_i18n) { // konstruktor

            mess = p_mess;
            i18n = p_i18n;
            jaaa = p_jaaa;

            edit_veta = new JEditorPane("text/html",""); // zobrazená věta
            edit_veta.setEditable(false);
            edit_veta.setBorder(BorderFactory.createEmptyBorder(1,4,3,3));
            edit_veta_scroll_pane = new JScrollPane(edit_veta);
            edit_veta_scroll_pane.setMinimumSize(new Dimension(36,36));
            edit_veta_scroll_pane.setPreferredSize(new Dimension(36,36));
            //edit_veta_scroll_pane.setMaximumSize(new Dimension(2000,36)); // nefunguje, řeším v napln_vetu
            edit_veta_scroll_pane.setBorder(BorderFactory.createEmptyBorder());

            label_jmeno_souboru = new JLabel(i18n.getString("LABEL_JMENO_SOUBORU")); // popisek názvu souboru, ze kterého je věta
            label_jmeno_souboru.setBorder(BorderFactory.createEmptyBorder(0,7,0,5));
            edit_jmeno_souboru = new JTextField(""); // název souboru, ze kterého je věta
            edit_jmeno_souboru.setEditable(false);
            edit_jmeno_souboru.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));


            button_prev_context = new JButton(i18n.getString("BUTTON_PREVIOUS_CONTEXT"));
            button_prev_context.setToolTipText(i18n.getString("BUTTON_PREVIOUS_CONTEXT_TOOLTIP"));
            button_prev_context.setMargin(new Insets(1,4,1,4));
            button_next_context = new JButton(i18n.getString("BUTTON_NEXT_CONTEXT"));
            button_next_context.setToolTipText(i18n.getString("BUTTON_NEXT_CONTEXT_TOOLTIP"));
            button_next_context.setMargin(new Insets(1,4,1,4));

            button_prev_occurrence = new JButton(i18n.getString("BUTTON_PREVIOUS_OCCURRENCE"));
            button_prev_occurrence.setToolTipText(i18n.getString("BUTTON_PREVIOUS_OCCURRENCE_TOOLTIP"));
            button_next_occurrence = new JButton(i18n.getString("BUTTON_NEXT_OCCURRENCE"));
            button_next_occurrence.setToolTipText(i18n.getString("BUTTON_NEXT_OCCURRENCE_TOOLTIP"));

            button_prev_tree = new JButton(i18n.getString("BUTTON_PREVIOUS_TREE"));
            button_prev_tree.setToolTipText(i18n.getString("BUTTON_PREVIOUS_TREE_TOOLTIP"));
            button_next_tree = new JButton(i18n.getString("BUTTON_NEXT_TREE"));
            button_next_tree.setToolTipText(i18n.getString("BUTTON_NEXT_TREE_TOOLTIP"));

            button_first_tree = new JButton(i18n.getString("BUTTON_FIRST_TREE"));
            button_first_tree.setToolTipText(i18n.getString("BUTTON_FIRST_TREE_TOOLTIP"));

            button_actions = new JButton(i18n.getString("BUTTON_ACTIONS"));
            button_actions.setToolTipText(i18n.getString("BUTTON_ACTIONS_TOOLTIP"));

            menu_actions = new JPopupMenu(i18n.getString("BUTTON_ACTIONS"));
            menu_actions_remove_occurrence = new JMenuItem(i18n.getString("MENU_ACTIONS_REMOVE_OCCURRENCE"));
            menu_actions_remove_occurrence.setToolTipText(i18n.getString("MENU_ACTIONS_REMOVE_OCCURRENCE_TOOLTIP"));
            menu_actions.add(menu_actions_remove_occurrence);

            button_show_hide = new JButton(i18n.getString("BUTTON_SHOW_HIDE"));
            button_show_hide.setToolTipText(i18n.getString("BUTTON_SHOW_HIDE_TOOLTIP"));

            menu_show_hide = new JPopupMenu(i18n.getString("BUTTON_SHOW_HIDE"));
            menu_show_hide_hidden_nodes = new JCheckBoxMenuItem(i18n.getString("MENU_SHOW_HIDE_HIDDEN_NODES"),true);
            menu_show_hide_references = new JMenu(i18n.getString("MENU_SHOW_HIDE_REFERENCES"));
            menu_show_hide.add(menu_show_hide_hidden_nodes);
            menu_show_hide_hidden_nodes.setSelected(false); // musí souhlasit s konstruktorem v \!
            menu_show_hide.add(menu_show_hide_references);
            //menu_show_hide_coreferences.setSelected(true); // musí souhlasit asi s konstruktorem v NGForestView

            button_statistics = new JButton(i18n.getString("BUTTON_STATISTICS"));
            button_statistics.setToolTipText(i18n.getString("BUTTON_STATISTICS_TOOLTIP"));

            /*button_prev_sada = new JButton(i18n.getString("BUTTON_PREV_SET"));
            button_prev_sada.setToolTipText(i18n.getString("BUTTON_PREV_SET_TOOLTIP"));
            button_next_sada = new JButton(i18n.getString("BUTTON_NEXT_SET"));
            button_next_sada.setToolTipText(i18n.getString("BUTTON_NEXT_SET_TOOLTIP"));
            panel_sada = new JPanel(new BorderLayout());
            panel_sada.add(button_prev_sada, BorderLayout.WEST);
            panel_sada.add(button_next_sada, BorderLayout.EAST);*/

            table_atributy = new JTable(new ModelTableAtributy(new DefaultListModel()));//model_table_atributy);
            table_atributy_scroll_pane = new JScrollPane(table_atributy);

            model_list_vybrane_atributy = new DefaultListModel();
            list_vybrane_atributy = new JList(model_list_vybrane_atributy);
            list_vybrane_atributy.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list_vybrane_atributy_scroll_pane = new JScrollPane(list_vybrane_atributy);
            list_vybrane_atributy_scroll_pane.setBorder(BorderFactory.createTitledBorder(i18n.getString("DISPLAYED_ATTRIBUTES")));

            split_atributy = new JSplitPane(JSplitPane.VERTICAL_SPLIT, table_atributy_scroll_pane, list_vybrane_atributy_scroll_pane);
            split_atributy_divider_location = 0.8; // pozici rozdělovače nastavuji ve funkci zalozka_files.nastavSoubory, tedy ve chvíli, kdy jsou známy rozměry komponent
            split_atributy.setOneTouchExpandable(true);
            panel_atributy = new JPanel(new BorderLayout());
            panel_atributy.setBorder(BorderFactory.createEtchedBorder());
            panel_atributy.add(split_atributy, BorderLayout.CENTER);

            strom_view = new NGForestDisplay(jaaa);
            //strom_view.setPreferredSize (new Dimension (800,800));
            strom_view_scroll_pane = new JScrollPane();
            strom_view_scroll_pane.getViewport().add(strom_view);
            strom_view.getTreeProperties().setShowMultipleSets(false);
            strom_view.getTreeProperties().setShowMultipleSetsChosen(false);
            strom_view.getTreeProperties().setShowLemmaVariants(jaaa.lemma_variants_show);
            strom_view.getTreeProperties().setShowLemmaComments(jaaa.lemma_comments_show);
            strom_view.getTreeProperties().setHighlightOptionalNodes(false);
            strom_view.getTreeProperties().setHighlightTransitiveEdges(false);
            strom_view.getTreeProperties().setHighlightZeroOccurrenceNodes(false);

            split_atributy_strom_view = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel_atributy, strom_view_scroll_pane);
            split_atributy_strom_view.setDividerLocation(165);
            split_atributy_strom_view.setOneTouchExpandable(true);

            GridBagLayout layout_panel_trees=new GridBagLayout();
            GridBagConstraints constraints = new GridBagConstraints();
            setConstraints(constraints, 0, 0, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, GridBagConstraints.REMAINDER);
            layout_panel_trees.setConstraints(edit_veta_scroll_pane, constraints);
            setConstraints(constraints, 1, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(label_jmeno_souboru, constraints);
            setConstraints(constraints, 2, 2, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(edit_jmeno_souboru, constraints);
            setConstraints(constraints, 3, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_actions, constraints);
            setConstraints(constraints, 4, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_show_hide, constraints);
            setConstraints(constraints, 5, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_first_tree, constraints);
            setConstraints(constraints, 6, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_prev_tree, constraints);
            setConstraints(constraints, 7, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_prev_occurrence, constraints);
            setConstraints(constraints, 8, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_prev_context, constraints);
            setConstraints(constraints, 9, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.EAST, 1);
            layout_panel_trees.setConstraints(button_statistics, constraints);
            setConstraints(constraints, 10, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            layout_panel_trees.setConstraints(button_next_context, constraints);
            setConstraints(constraints, 11, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.EAST, 1);
            layout_panel_trees.setConstraints(button_next_occurrence, constraints);
            setConstraints(constraints, 12, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.EAST, 1);
            layout_panel_trees.setConstraints(button_next_tree, constraints);
            setConstraints(constraints, 0, 1, 1.0, 1.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, GridBagConstraints.REMAINDER);
            layout_panel_trees.setConstraints(split_atributy_strom_view, constraints);
            //setConstraints(constraints, 0, 2, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.WEST, 1);
            //layout_panel_trees.setConstraints(panel_sada, constraints);

            setLayout(layout_panel_trees);
            add(edit_veta_scroll_pane);
            add(label_jmeno_souboru);
            add(edit_jmeno_souboru);
            add(button_first_tree);
            add(button_prev_tree);
            add(button_prev_occurrence);
            add(button_prev_context);
            add(button_statistics);
            add(button_next_context);
            add(button_next_occurrence);
            add(button_next_tree);
            add(button_show_hide);
            add(button_actions);
            add(split_atributy_strom_view);
            //add(panel_sada);

            // nastaveni udalosti

            button_prev_context.addActionListener(this);
            button_next_context.addActionListener(this);
            button_prev_occurrence.addActionListener(this);
            button_next_occurrence.addActionListener(this);
            button_prev_tree.addActionListener(this);
            button_next_tree.addActionListener(this);
            button_first_tree.addActionListener(this);
            button_show_hide.addActionListener(this);
            menu_show_hide_hidden_nodes.addActionListener(this);
            button_actions.addActionListener(this);
            menu_actions_remove_occurrence.addActionListener(this);
            menu_show_hide_references.addActionListener(this);
            button_statistics.addActionListener(this);
            //button_prev_sada.addActionListener(this);
            //button_next_sada.addActionListener(this);
            list_vybrane_atributy.addMouseListener(this);
            strom_view.addMouseListener(this);

            // konec nastaveni udalosti

/* ###zk začátek aplikačního kódu */
            // pro tisk na tiskárnu:
            forest_print = new NGForestPrint(jaaa); // objekt pro tisk stromů na tiskárnu
            forest_print.getTreeProperties().setShowMultipleSets(true);
            forest_print.getTreeProperties().setShowMultipleSetsChosen(true);
            forest_print.getTreeProperties().setShowLemmaVariants(jaaa.lemma_variants_show);
            forest_print.getTreeProperties().setShowLemmaComments(jaaa.lemma_comments_show);

            //forest_print.getPrintProperties().setCharacterCoding(CharCode.coding_unicode);

            print_dialog = new PrintTreeDialog(jaaa,i18n.getString("DIALOG_PRINT_TREE_TITLE"),true,
            forest_print.getPrintProperties(), jaaa, i18n); // nechci to modální kvůli nápovědě, ale je to takto snadnější

            // pro ukládání výsledných stromů na lokální disk:
            result_trees_local_saver = new ResultTreesLocalSaver(jaaa, jaaa, i18n);
/* ###kk konec aplikačního kódu */

            setFontSizeSentence(12);
            
        } // PanelTrees (konstruktor)


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

        public DefaultListModel getVybraneAtributy() {
            return model_list_vybrane_atributy;
        }

        // --------------------------------- odchycení událostí ----------------------------------


        public void actionPerformed(ActionEvent e) { // akce (doubleclick nebo mezerník nebo enter)
            Object zdroj = e.getSource();
            if (jaaa.kom_net.isConnected()) { // pokud jsem vůbec připojen k nějakému serveru
                if (zdroj == button_prev_context) { // zobrazení předchozího kontextu aktuálního stromu
                    //debug ("\nStisknuto tlačítko 'button_prev_context' (zobrazení předchozího kontextu aktuálního stromu)");
                    loadPrevTree(ServerCommunication.GET_TREE_SUBTYPE_CONTEXT);
                }
                else if (zdroj == button_next_context) { // zobrazení následujícího kontextu aktuálního stromu
                    //debug ("\nStisknuto tlačítko 'button_next_context' (zobrazení následujícího kontextu aktuálního stromu)");
                    loadNextTree(ServerCommunication.GET_TREE_SUBTYPE_CONTEXT);
                }
                else if (zdroj == button_prev_occurrence) { // zobrazení předchozího výskytu dotazu
                    //debug ("\nStisknuto tlačítko 'button_prev_occurrence' (zobrazení předchozího výskytu dotazu)");
                    loadPrevTree(ServerCommunication.GET_TREE_SUBTYPE_OCCURENCE);
                }
                else if (zdroj == button_next_occurrence) { // zobrazení následujícího výskytu dotazu
                    //debug ("\nStisknuto tlačítko 'button_next_occurrence' (zobrazení následujícího výskytu dotazu)");
                    loadNextTree(ServerCommunication.GET_TREE_SUBTYPE_OCCURENCE);
                }
                if (zdroj == button_prev_tree) { // zobrazení předchozího prvního výskytu ve stromu
                    //debug ("\nStisknuto tlačítko 'button_prev_tree' (zobrazení předchozího prvního výskytu ve stromu)");
                    loadPrevTree(ServerCommunication.GET_TREE_SUBTYPE_TREE);
                }
                else if (zdroj == button_next_tree) { // zobrazení následujícího prvního výskytu ve stromu
                    //debug ("\nStisknuto tlačítko 'button_next_tree' (zobrazení následujícího prvního výskytu ve stromu)");
                    loadNextTree(ServerCommunication.GET_TREE_SUBTYPE_TREE);
                }
                if (zdroj == button_first_tree) { // zobrazení prvního výskytu dotazu
                    //debug ("\nStisknuto tlačítko 'button_first_tree' (zobrazení prvního výskytu dotazu)");
                    loadNextTree(ServerCommunication.GET_TREE_SUBTYPE_FIRST);
                }
                else if (zdroj == button_actions) { // zobrazení menu s akcemi
                    //debug ("\nStisknuto tlačítko 'button_actions' (zobrazení menu s akcemi)");
                    menu_actions.show(button_actions,-30,-22);
                }
                else if (zdroj == menu_actions_remove_occurrence) { // odstranění aktuálního výskytu dotazu z výsledku
                    //debug ("\nVybráno menu odstranění aktuálního výskytu dotazu z výsledku");
                    removeOccurrence();
                }
                else if (zdroj == button_show_hide) { // zobrazení/skrytí skrývaných vrcholů či koreferencí
                    //debug ("\nStisknuto tlačítko 'button_show_hide' (zobrazení/skrytí skrývaných vrcholů či koreferencí)");
                    menu_show_hide.show(button_show_hide,-30,-40);
                }
                else if (zdroj == menu_show_hide_hidden_nodes) { // zobrazení/skrytí skrývaných vrcholů
                    //debug ("\nVybráno menu zobrazení/skrytí skrývaných vrcholů");
                    showHideHiddenNodes();
                }
                else if (zdroj == button_statistics) { // zobrazení statistik o prohledávání dotazu
                    //debug ("\nStisknuto tlačítko 'button_statistics' (zobrazení statistik o prohledávání dotazu)");
                    getAndDisplayStatistics();
                }
                else {
                    for (int i=0; i<menu_show_hide_references.getItemCount(); i++) { // zobrazení/skrytí koreferencí
                        JCheckBoxMenuItem item = (JCheckBoxMenuItem)menu_show_hide_references.getItem(i);
                        if (zdroj == item) { // zobrazení/skrytí této koreference
                            boolean new_status = changeCoreferenceStatus(i);
                            //debug("\nnew status = " + new_status);
                            item.setSelected(new_status);
                            break;
                        }
                    }
                }
                /*else if (zdroj == button_prev_sada) { // výběr předchozí sady atributů
                    //debug ("\nStisknuto tlačítko 'button_prev_sada' (výběr předchozí sady atributů)");
                    vyberPredchoziSaduAtributu();
                }
                else if (zdroj == button_next_sada) { // výběr následující sady atributů
                    //debug ("\nStisknuto tlačítko 'button_next_sada' (výběr následující sady atributů)");
                    vyberNasledujiciSaduAtributu();
                }*/
            }
            else {
                inform("YOU_ARE_DISCONNECTED");
            }
        }	// actionPerformed



        // --------------------------------- odchycení myších událostí ----------------------------------


        public void mousePressed(MouseEvent e) { // stisknuto tlačítko myši - označí se příslušný vrchol a zobrazí se hodnoty jeho atributů

            Object zdroj = e.getSource();
            if (zdroj == strom_view) {
                strom_view.selectNode(e); // zjistí se a zobrazí vrchol, na který se kliklo

                                /*if (list_global_head.getSelectedIndex() == -1) { // neni zvolena polozka
                                        return;
                                }*/

                strom_view.repaint();
                napln_hodnoty_atributu_v_tabulce(forest_actual.getChosenTree().getChosenNode().getSetOfAttributes(0));
                int modif = e.getModifiers();
                                /*if ((modif & InputEvent.BUTTON3_MASK) != 0) {
                                        debug("\nThe right button on the mouse was pressed.");
                                }*/

            }
            return;
        }

        public void mouseClicked(MouseEvent e) { // kliknutí myší

            Object zdroj = e.getSource();
            int pocet_kliku = e.getClickCount();

            if (zdroj == list_vybrane_atributy) {
                if (pocet_kliku == 2) { // odstranění atributu ze zobrazovaných
                    if (list_vybrane_atributy.isSelectionEmpty() || model_list_vybrane_atributy.getSize() <= 0) return;
                    String o_atribut = new String(list_vybrane_atributy.getSelectedValue().toString());
                    //debug("\nUživatel odebírá atribut " + o_atribut);
                    odskrtniAtribut(o_atribut);
                    odstranAtribut(o_atribut);
                }
            }
        } // mouseClicked

        public void mouseExited(MouseEvent e) {return;}
        public void mouseReleased(MouseEvent e) {return;}
        public void mouseEntered(MouseEvent e) {return;}

        // ------------------------------ konec odchycení myších událostí -------------------------------

        // ------------------------------ konec odchycení událostí -------------------------------


/* ###zk začátek aplikačního kódu */
        public void saveActionsDisconnect() { // ukládání informací na disk při odpojování od serveru
            return;
        }

        public void saveActionsExit() { // ukládání informací na disk (jiných než globálních properties) před ukončením aplikace; akce z saveActionsDisconnect by se tu neměly opakovat
            return;
        }

        public void readGeneralProperties(Properties properties) { // přečtu properties pro tento objekt
            int font_size_trees = properties.getIntProperty("main menu","font size in results",12);
            strom_view.setFontSize(font_size_trees);
            jaaa.setMenuOptionsFontsizeResult(font_size_trees);
            setFontSizeSentence(properties.getIntProperty("main menu","font size in the sentence",12));
            jaaa.setMenuOptionsFontsizeSentence(font_size_sentence);
            result_trees_local_saver.readGeneralProperties(properties);
        } // readGeneralProperties

        public void writeGeneralProperties(Properties properties) { // zapíše properties z tohoto objektu
            properties.updateProperty("main menu","font size in results", ""+strom_view.getFontSize(),"font size in result trees (integer)");
            properties.updateProperty("main menu","font size in the sentence", ""+font_size_sentence,"font size in the sentences (integer)");
            result_trees_local_saver.writeGeneralProperties(properties);
        } // writeGeneralProperties


/* ###kk konec aplikačního kódu */

        public void setFontSizeSentence(int size) {
            font_size_sentence = size;
            font_height = (int)Math.round(size * 1.5); // tady to 1.5 je hausnumero
            //debug ("\nPanelTrees.setFontSizeSentence: Výška fontu spočítána na: " + font_height);
            prekresliKodovani();

        }

        void prekresliKodovani() { // v novém kódování češtiny překreslí potřebné komponenty
            if (forest_actual == null) return;
            napln_vetu(forest_actual.getTrees().get(0), forest_actual.getHead(),edit_veta);
            napln_hodnoty_atributu_v_tabulce(forest_actual.getChosenTree().getChosenNode().getSetOfAttributes(0)); // naplní v tabulce atributů hodnoty atributů
            repaint();
        }

        void prekresli() { // s novým nastavením řazení uzlů ve stromě nebo slov ve větě překreslí potřebné komponenty
            if (forest_actual == null) return;
            repaint();
        }


        void pridejAtribut(String pridany_atribut) { // přidá atribut k atributům zobrazovaným ve stromě
            //debug ("\nPřidání atributu " + pridany_atribut + " k zobrazovaným");
            model_list_vybrane_atributy.addElement(pridany_atribut);
            forest_actual.setFlagWholeForestChanged(true);
            forest_actual.getVybraneAtributy().addElement(pridany_atribut);
            strom_view.repaint();
        }

        void odskrtniAtribut(String odstraneny_atribut) { // zruší zaškrtnutí atributu v tabulce atributů
            // voláno při odstranění atributu ze seznamu zobrazovaných dobleclickem na seznamu zobrazovaných
            int poradi = forest_actual.getHead().getIndexOfAttribute(odstraneny_atribut);
            //debug("\nPořadí tohoto atributu v hlavičce stromu je " + poradi);
            table_atributy.getModel().setValueAt(new Boolean(false), poradi, 0);
            table_atributy.repaint();
        }

        void odstranAtribut(String odstraneny_atribut) { // odstraní atribut ze seznamu atributů zobrazovaných ve stromě
            //debug ("\nOdstranění atributu " + odstraneny_atribut + " ze zobrazovaných");
            model_list_vybrane_atributy.removeElement(odstraneny_atribut);
            forest_actual.getVybraneAtributy().removeElement(odstraneny_atribut);
            forest_actual.setFlagWholeForestChanged(true);
            strom_view.repaint();
        }

        void zaskrtniAtribut(String zaskrtavany_atribut) { // zaškrtne atribut v tabulce atributů
            // voláno při načtení stromu pro sladění se seznamem vybraných atributů
            int poradi = forest_actual.getHead().getIndexOfAttribute(zaskrtavany_atribut);
            //debug ("\nPořadí atributu " + zaskrtavany_atribut + " = " + poradi);
            if (poradi != -1) { // je-li atribut pro daný strom definován
                table_atributy.getModel().setValueAt(new Boolean(true), poradi, 0);
            } // není-li, vzápětí se v nadřazené funkci odstraní, aniž by se zde opětovně přidal
        }

        void zaskrtniAtributy() { // zaškrtne v tabulce atributů všechny atributy ze seznamu vybraných atributů
            // voláno při načtení stromu pro sladění se seznamem vybraných atributů
            int pocet = model_list_vybrane_atributy.getSize();
            //debug ("\nPočet vybraných atributů je: " + pocet + "; zaškrtávám...");
            for (int i = 0; i < pocet; i++) {
                zaskrtniAtribut(model_list_vybrane_atributy.getElementAt(0).toString());
                model_list_vybrane_atributy.removeElementAt(0);
                //debug("\n" + i+1 + ". hotov.");
                // zaskrtniAtribut totiz vola setValueAt u model_table_atributy, coz zpusobi pridani atributu k vybranym
            }
        }

        void showHideHiddenNodes() { // reakce na menu tlačítka show/hide; zobrazení/skrytí skrývaných vrcholů
            if (strom_view.getShowHiddenNodes()) {
              strom_view.setShowHiddenNodes(false);
/* ###zk začátek aplikačního kódu */
              forest_print.setShowHiddenNodes(false);
/* ###kk konec aplikačního kódu */
            }
            else {
              strom_view.setShowHiddenNodes(true);
/* ###zk začátek aplikačního kódu */
              forest_print.setShowHiddenNodes(true);
/* ###kk konec aplikačního kódu */

            }
            forest_actual.setFlagWholeForestChanged(true); // bude třeba překreslit strom
            strom_view.updateUI();
            strom_view.repaint();
        }

        public NGForest getActualForest() { // vrátí aktuální strom
            return forest_actual;
        }

        boolean changeCoreferenceStatus(int i) { // reakce na menu tlačítka show/hide; zobrazení/skrytí i-té koreference
            // překlopí stav zobrazení i-té koreference
            // vrací výsledný stav; může být stejný s původním, pokud koreferenční schéma neumožňuje změnu (pak by ovšem nebylo v menu)
            boolean ret = strom_view.changeReferenceStatus(i);
            forest_actual.setFlagWholeForestChanged(true); // bude třeba překreslit strom
            // u forest_print se to menit nemusi; maji sice kazdy svuj reference_patterns, ale ty jednotlive patterny sdileji
            strom_view.updateUI();
            strom_view.repaint();
            return ret;
        }

        public void setCoreferencePatterns(DefaultListModel patterns) {
            strom_view.setReferencePatterns(patterns);
/* ###zk začátek aplikačního kódu */
            forest_print.setReferencePatterns(patterns);
/* ###kk konec aplikačního kódu */
            menu_show_hide_references.removeAll(); // odstraním všechny případné předchozí položky
            for (int i=0; i<patterns.size(); i++) {
                ReferencePattern pattern = (ReferencePattern)patterns.elementAt(i);
                if (pattern.isEditable()) {
                    String name = pattern.getName();
                    boolean display = pattern.getDisplay();
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(name,display);
                    menu_show_hide_references.add(item);
                    item.addActionListener(this);
                }
            }
        }

        private void clearStatistics() { // vynuluje statistiky o prohledávání korpusu
            number_of_actual_occurrence = 0;
            number_of_actual_tree = 0;
            number_of_found_occurences = 0;
            number_of_found_trees = 0;
            number_of_searched_trees = 0;
        } // clearStatistics

        private void readStatistics() { // přečte statistiky o prohledávání korpusu z objektu kom_net
            number_of_actual_occurrence = jaaa.kom_net.getNumberOfActualOccurrence();
            number_of_actual_tree = jaaa.kom_net.getNumberOfActualTree();
            number_of_found_occurences = jaaa.kom_net.getNumberOfFoundOccurences();
            number_of_found_trees = jaaa.kom_net.getNumberOfFoundTrees();
            number_of_searched_trees = jaaa.kom_net.getNumberOfSearchedTrees();;
        } // clearStatistics

        private void getAndDisplayStatistics() { // získá statistiky od serveru a zobrazí je
            jaaa.kom_net.getStatistics();
            readStatistics();
            displayStatistics();
        } // getAndDisplayStatistics

        public void displayStatistics() { // zobrazí statistiky o prohledávání dotazu
            readStatistics(); // přečtu statistiky od objektu kom_net
            String statistics;
            statistics = "[" + number_of_actual_occurrence + "/"
            + number_of_found_occurences + "]  ["
            + number_of_actual_tree + "/"
            + number_of_found_trees + "/"
            + number_of_searched_trees + "]";
            button_statistics.setText(statistics);
        } // displayStatistics

        private void loadPrevTree(byte subtype) { // zavolám funkci pro načtení předchozího stromu
            int loaded = jaaa.kom_net.loadPrevTree(subtype);
            if (loaded == 0) { // strom načten
                //inform("PREV_TREE_OK");
                treeLoaded(); // zobrazím vše
            }
            else { // strom nenačten - žádný předchozí strom už není
                //inform("NO_PREV_TREE");
                displayStatistics();
            }
        } // loadPrevTree

        public void loadNextTree(byte subtype) {
            jaaa.kom_net.loadNextTree(this, subtype, true); // zavolám funkci pro načtení následujícího stromu a předám objekt, který se má upozornit po načtení
        }

        private void removeOccurrence() {
            jaaa.kom_net.removeOccurrence(this, true); // zavolám funkci pro odstranění aktuálního výskytu dotazu z výsledků a načtení nádledujícího stromu a předám objekt, který se má upozornit po načtení
        }

        public void statisticsLoaded() {
            displayStatistics();
        }

        public void treeLoaded() {

            boolean display = true; // pozůstatek - display znamená, zda má po načtení strom zobrazit
            // nastavím barevné schéma
            strom_view.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_DEFAULT);

            forest_actual = new NGForest(jaaa);
            forest_actual = jaaa.kom_net.getForest(); // vezmu načtený strom (jako jednoprvkový les)

            if (display) {
                String jmeno_souboru_se_stromem = forest_actual.getFileName(); // vezmu to z prvního a jediného stromu tohoto lesa
                edit_jmeno_souboru.setText(jmeno_souboru_se_stromem); // zobrazím jej
                edit_jmeno_souboru.setToolTipText(jmeno_souboru_se_stromem); // zobrazím jej celý jako tooltip
                label_jmeno_souboru.setToolTipText(jmeno_souboru_se_stromem); // zobrazím jej celý jako tooltip
            }

            if (display) displayStatistics();

            NGTreeHead head = forest_actual.getHead();
            model_list_actual_head = head.getModel();

            // teď se nastaví šířky sloupců tabulky
            if (display) setAttributesTableColumns();

            if (head.W == -1) {
                head.W = head.N; // zřejmě analytický strom - pořadí slov ve větě a stromu shodné
            } // zde je také místo pro budoucí defaultní výběr atributů...


            if (display) {
                zaskrtniAtributy(); // v tabulce atributů se zaškrtnou atributy ze seznamu vybraných atributů
                forest_actual.setVybraneAtributy(model_list_vybrane_atributy);
                forest_actual.setFlagWholeForestChanged(true); // je potřeba vypočítat nakreslení
                strom_view.setForest(forest_actual); // ze strom_view budu ukazovat na ten samý strom
            }

            // cteni stromu
            jaaa.pocet_atr = model_list_actual_head.getSize();

            String [] veta = new String[forest_actual.getNumberOfNodes()];

            if (display) {
                if (head.W != -1 && head.V != -1) {
                    napln_vetu(forest_actual.getTrees().get(0), forest_actual.getHead(), edit_veta);
                }
                else {
                    edit_veta.setText(" ");
                }
                napln_hodnoty_atributu_v_tabulce(forest_actual.getChosenTree().getChosenNode().getSetOfAttributes(0)); // naplní se hodnoty atributů vybraného vrcholu

                strom_view.vypocti_nakresleni(strom_view_scroll_pane.getViewport().getGraphics()); // potřebuju to vypočíst v tuto chvíli (repaint to dělá prapodivně bůhvíkdy)
                Rectangle matching_nodes_rectangle = strom_view.getMatchingNodesRectangle();

                if (matching_nodes_rectangle != null) {
                    //debug("\nPanelTrees.treeLoaded: Matchujici vrcholy (po vypocti_nakresleni a pred repaint) se nachazeji v obdelniku: " + matching_nodes_rectangle.getMinX() + "," + matching_nodes_rectangle.getMinY() + "," + matching_nodes_rectangle.getMaxX() + "," + matching_nodes_rectangle.getMaxY());

                    strom_view.scrollRectToVisible(new Rectangle(0,0,1,1));
                    strom_view.scrollRectToVisible(matching_nodes_rectangle);
                    strom_view.scrollRectToVisible(forest_actual.getChosenNode().getRectangle());

                    //debug("\nPanelTrees.treeLoaded: The desired rectangle is: " + matching_nodes_rectangle);
                    //debug("\nPanelTrees.treeLoaded: The visible rectangle is: " + strom_view.getVisibleRect());

                }
                else {
                    debug("\nPanelTrees.treeLoaded: Nedefinovany obdelnik podstromu matchujiciho s dotazem, veta = " + edit_veta.getText());
                }

                repaint();
            }
        } // treeLoaded

        private void setAttributesTableColumns() {

            TableColumn druhy_sloupec = table_atributy.getColumnModel().getColumn(1); // model druhého sloupce
            sirka_druheho_sloupce = druhy_sloupec.getWidth(); // zjistí se předchozí šířka
            TableColumn treti_sloupec = table_atributy.getColumnModel().getColumn(2); // model tretiho sloupce
            sirka_tretiho_sloupce = treti_sloupec.getWidth(); // zjistí se předchozí šířka

            ModelTableAtributy mta = new ModelTableAtributy(model_list_actual_head);
            table_atributy.setModel(mta);
            TableColumn column = null;
            column = table_atributy.getColumnModel().getColumn(0);
            column.setMinWidth(21);
            column.setMaxWidth(21);
            column.setPreferredWidth(21);
            column.setResizable(false);

            druhy_sloupec = table_atributy.getColumnModel().getColumn(1);
            //druhy_sloupec.setMinWidth(sirka_druheho_sloupce);
            //druhy_sloupec.setMaxWidth(sirka_druheho_sloupce);
            druhy_sloupec.setPreferredWidth(sirka_druheho_sloupce);
            druhy_sloupec.setWidth(sirka_druheho_sloupce);
            druhy_sloupec.setResizable(true);

            treti_sloupec = table_atributy.getColumnModel().getColumn(2);
            //treti_sloupec.setMinWidth(sirka_tretiho_sloupce);
            //treti_sloupec.setMaxWidth(sirka_tretiho_sloupce);
            treti_sloupec.setPreferredWidth(sirka_tretiho_sloupce);
            treti_sloupec.setWidth(sirka_tretiho_sloupce);
            treti_sloupec.setResizable(true);

                        /*JComboBox comboBox = new JComboBox();
                        comboBox.addItem(new JLabel("Tady"));
                        comboBox.addItem(new JLabel("budou"));
                        comboBox.addItem(new JLabel ("barvy"));
                        druhy_sloupec.setCellEditor(new DefaultCellEditor(comboBox));

                        //Set up tool tips for the name cells
                        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                        renderer.setToolTipText("Click to change the colour");
                        druhy_sloupec.setCellRenderer(renderer);

                        //Set up tool tip for the name column header.
                        TableCellRenderer headerRenderer = druhy_sloupec.getHeaderRenderer();
                        if (headerRenderer instanceof DefaultTableCellRenderer) {
                            ((DefaultTableCellRenderer)headerRenderer).setToolTipText(
                                "Click an attribute to change its colour");
                        }
                         */

        }

        private void napln_vetu(NGTree tree, NGTreeHead head, JEditorPane t) {
            t.setText("<div style=\"font-size: " + font_size_sentence + "pt; font-family: dialog\">" + tree.getSentenceString(head) + "</div>");
            t.setCaretPosition(0);
            Dimension dim = t.getPreferredSize();
            //debug("\nt.getPreferredSize() = " + dim);
            if (dim.getHeight() > font_height * max_lines_sentence) {
                dim.setSize(dim.getWidth(),(double)(font_height * max_lines_sentence));
            }
            edit_veta_scroll_pane.setMinimumSize(dim);
            edit_veta_scroll_pane.setPreferredSize(dim);
        }

        public void napln_hodnoty_atributu_v_tabulce(TValue sada) { /* zobrazi v tabulce atributu retezce vytvorene z toho seznamu TAHLine, ktery je v sade 'sada'
                                                jednotlive prvky seznamu oddeli v retezci mezerou */
            // pouzivana programem pro zobrazovani hodnot atributu v tabulce atributu

            TAHLine v;
            int pocet = model_list_actual_head.getSize();
            
            for (int i = 0; i < pocet; i++) { // pro všechny atributy
                String pom = "";
                v = sada.AHTable[i]; // i-tý atribut
                while (v != null) {
                    pom += v.Value + " ";
                    v = v.Next;
                }
                //table_atributy.setValueAt (kodujString (pom, use_ascii_in_trees), i, 2);
                /*String pom_coded;
                if (jaaa.coding_in_trees == CharCode.coding_ascii) pom_coded = CharCode.isolatin2ToAscii(pom);
                else pom_coded = CharCode.isolatin2ToUnicode(pom);*/
                table_atributy.setValueAt(pom, i, 2);
            }
            table_atributy.revalidate();
            table_atributy.repaint();
        }

        /*void vyberPredchoziSaduAtributu() {
            if (forest_actual.chosen_set == null)
                return;
            if (forest_actual.getChosenNode().values == forest_actual.chosen_set)
                return;	// slo o prvni sadu
            TValue pom = forest_actual.getChosenNode().values;
            while (pom.Next != forest_actual.chosen_set)
                pom = pom.Next;
            forest_actual.chosen_set = pom;
            napln_hodnoty_atributu_v_tabulce(forest_actual.chosen_set);
        }

        void vyberNasledujiciSaduAtributu() {
            if (forest_actual.chosen_set == null)
                return;
            if (forest_actual.chosen_set.Next == null)
                return; // slo o posledni sadu
            forest_actual.chosen_set = forest_actual.chosen_set.Next;
            napln_hodnoty_atributu_v_tabulce(forest_actual.chosen_set);
        }*/

/* ###zk začátek aplikačního kódu */
        public void saveResultDialog() { // zobrazí dialogové okno pro uložení nalezených stromů na lokální disk
            result_trees_local_saver.saveResultDialog();
        }
/* ###kk konec aplikačního kódu */

        /**
         * Prints sentence (in multiple lines if too long) to the given position in g2
         * @param g2
         * @param sentence
         * @param pos_x
         * @param pos_y
         * @return used vertical space (font height * number of lines)
         */
/* ###zk začátek aplikačního kódu */
        private int printSentence(Graphics2D g2, String sentence, int pos_x, int pos_y) {
            String font_family = forest_print.getPrintProperties().getFontFamily();
            int font_size = forest_print.getPrintProperties().getFontSize();
            Font font_veta = new Font(font_family, Font.PLAIN, font_size);

            g2.setFont(font_veta);
            g2.setPaint(Color.black);

            // nyní vytisknu větu tak, aby byla rozdělena do více řádků, když je příliš dlouhá

            FontRenderContext frc = g2.getFontRenderContext();
            AttributedString attribString = new AttributedString(sentence);
            //attribString.addAttribute(TextAttribute.FOREGROUND, Color.blue, 0, sentence.length()); // Start and end indexes.
            attribString.addAttribute(TextAttribute.FONT, font_veta, 0, sentence.length()+pos_x);
            AttributedCharacterIterator styledText = attribString.getIterator();
            // let styledText be an AttributedCharacterIterator containing at least
            // one character
            LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);
            float wrappingWidth = (float)im_width * 0.90f; // hausnumero, aby mi to nelezlo přes okraj vpravo
            float x,y;
            x = (float)pos_x;
            y = (float)pos_y;
            while (measurer.getPosition() < sentence.length()) {
                TextLayout layout = measurer.nextLayout(wrappingWidth);
                y += (layout.getAscent());
                float dx = layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance());
                layout.draw(g2, x + dx, y);
                y += layout.getDescent() + layout.getLeading();
            }
            return Math.round(y);
        }
/* ###kk konec aplikačního kódu */

        /**
         * Prints file_name in one line (scaled if too long) to the given position in g2
         * @param g2
         * @param file_name
         * @param pos_x
         * @param pos_y
         * @return used vertical space (font height)
         */
/* ###zk začátek aplikačního kódu */
        private int printFileName(Graphics2D g2, String file_name, int pos_x, int pos_y) {
            Font font_soubor;
            int font_size = forest_print.getPrintProperties().getFontSize();
            double file_name_width = (double)g2.getFontMetrics().stringWidth(file_name) + pos_x;
            double scale_file_name_x = 1.0;
            if (file_name_width > im_width) scale_file_name_x = im_width / file_name_width;
            if (scale_file_name_x != 1.0) {
                debug("\nProvádím x-ový scale pro tisk jména souboru hodnotou " + scale_file_name_x);
                g2.scale(scale_file_name_x,1.0);
            }
            font_soubor = new Font("DialogInput", Font.PLAIN, font_size - 1);
            g2.setFont(font_soubor);
            int vyska_pisma2 = font_size - 1; //g2.getFontMetrics().getHeight();
            if (forest_print.getPrintProperties().getBlackWhite()) { // černobílý tisk
                g2.setPaint(Color.gray);
            }
            else { // barevný tisk
                g2.setPaint(Color.green);
            }
            g2.drawString(file_name, pos_x, pos_y + vyska_pisma2); // vytisknu jméno souboru; to bývá bez háčků a čárek
            if (scale_file_name_x != 1.0) {
                g2.scale(1.0/scale_file_name_x,1.0); // vracím scale zpět
            }
            return vyska_pisma2;
        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        public int print(Graphics g, PageFormat pf, int page_index) throws PrinterException {
            int distance_sentence_file = 6;
            int distance_file_tree = 8;
            int odsazeni_vlevo_file = 1;

            if (page_index > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D)g; // pro větší možnosti, např. scale

            g2.translate((int)im_x,(int)im_y); // posunu levý horní roh do tisknutelné oblasti

            String sentence = forest_printed.getChosenTree().getSentenceString(forest_printed.getHead());
            if (sentence == null) { // věta nesestavena
                sentence = " "; // nastavím prázdnou větu, ale aspoň mezeru, ať to dál nepadá
            }
            if (sentence.length()==0) { // věta nesestavena
                sentence = " "; // nastavím prázdnou větu, ale aspoň mezeru, ať to dál nepadá
            }

            // nyní tu větu vytisknu; když bude potřeba, tak na několik řádek

            int sentence_height = printSentence(g2, sentence, 0, 0);

            g2.translate(0,sentence_height + distance_sentence_file); // posunu levý horní roh pod vytisknutou větu

            // nyní vytisknu cestu a název souboru, kde se strom nalezl; když bude potřeba, zůží se

            String file_name = forest_printed.getFileName();
            int file_name_height = printFileName(g2,file_name, odsazeni_vlevo_file, 0);

            // a nyní vytisknu strom

            g2.translate(0, file_name_height + distance_file_tree); // posunu levý horní roh pod jméno souboru

            // teď spočítám velikost zbývající tisknutelné plochy
            double t_x = im_width;
            double t_y = im_height - (double)sentence_height - (double)file_name_height - (double)distance_sentence_file - (double)distance_file_tree;

            String font_family = forest_print.getPrintProperties().getFontFamily();
            int font_size = forest_print.getPrintProperties().getFontSize();
            Font font_strom = new Font(font_family, Font.PLAIN, font_size);
            g2.setFont(font_strom);

            forest_print.print(g2,t_x,t_y); // vytisknutí vlastního stromu; vejít se musí do t_x * t_y

            return PAGE_EXISTS;
        } // print
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        void printTree(boolean dialog) { // vytiskne strom na tiskárnu; vyvolá dialogové okno, pokud dialog=true
            printer_job = PrinterJob.getPrinterJob();
            boolean print = true; // předpokládá se, že uživatel chce tisknout

            if (dialog) { // zobrazí dialogové okno pro výběr vlastností tisku
                int x_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getWidth();
                int y_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getHeight();

                int x_pos = (int)(x_max/2 - print_dialog.getSize().getWidth()/2);
                int y_pos = (int)(y_max/2 - print_dialog.getSize().getHeight()/2);

                print_dialog.setLocation(x_pos, y_pos);
                print = print_dialog.show(printer_job);
            }

            //debug ("\nNastavení tisku: \n   font family = " + print_properties.getFontFamily());
            //debug ("\n   font size = " + print_properties.getFontSize());
            //debug ("\n   character coding = " + print_properties.getCharacterCoding());
            //debug ("\n   center = " + print_properties.getCenter());
            //debug ("\n   keep ratio = " + print_properties.getKeepRatio());
            //debug ("\n   background = " + print_properties.getBackground());

            //debug("\nJsem po dialogu pro tisk a print = " + print);
            if (print) { // pokud to uživatel nezrušil
                forest_printed = forest_actual.getClone();
                forest_print.setForest(forest_printed);
                if (forest_print.getPrintProperties().getBlackWhite()) { // má se tisknout černobíle
                    //debug("\nSetting black and white for printing trees.");
                    forest_print.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_BLACK_AND_WHITE);
                }
                else { // má se tisknout barevně
                    //debug("\nSetting colors for printing trees.");
                    forest_print.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_DEFAULT);
                }
                im_height = forest_print.getPrintProperties().getPageFormat().getImageableHeight();
                im_width = forest_print.getPrintProperties().getPageFormat().getImageableWidth();
                im_x = forest_print.getPrintProperties().getPageFormat().getImageableX();
                im_y = forest_print.getPrintProperties().getPageFormat().getImageableY();

                printer_job.setPrintable(this, forest_print.getPrintProperties().getPageFormat());
                if (dialog) print = printer_job.printDialog();

                //forest_printed = forest_actual.getClone(); // vytvořím kopii stromu

                if (print) { // pokud stále nezrušeno uživatelem, tak už opravdu budu tisknout
                    if (forest_print.getPrintProperties().getBackground()) { // vytiskne se v novém vlákně
                        Thread printThread = new Thread() {
                            public void run() {
                                jaaa.menu_soubor_print.setEnabled(false); // nepovolím další tisk, dokud nevytisknu toto
                                printNow();
                                jaaa.menu_soubor_print.setEnabled(true);
                            }
                        };
                        printThread.start();
                    }
                    else { // vytiskne se v aktuálním vlákně
                        printNow();
                    }
                }
            }
        } // printTree
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        void printNow() {
            try {
                printer_job.print();
            }
            catch (PrinterException e) {
                debug("\nPři tisku nastala chyba " + e);
                inform("PRINTING_FINISHED_KO");
            }
            inform("PRINTING_FINISHED");
        }

/* ###kk konec aplikačního kódu */

        // ====================================================================================================
        //		class ModelTableAtributy		výpis a výběr atributů
        // ====================================================================================================

        class ModelTableAtributy extends AbstractTableModel {

            DefaultListModel data; // seznam objektů Attribute
            String[] columnNames; // nadpisy sloupců

            ModelTableAtributy(DefaultListModel p_data) { // konstruktor
                columnNames = new String[] { i18n.getString("TABLE_ATRIBUTY_ZOBRAZIT"), // nadpisy sloupců
                i18n.getString("TABLE_ATRIBUTY_ATRIBUT"),
                i18n.getString("TABLE_ATRIBUTY_HODNOTA")};
                data = p_data;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                int size = 0;
                try { // padalo při startu apletu
                    size = data.getSize();
                }
                catch (Exception e) {
                    size = 0;
                };
                return size;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public Object getValueAt(int row, int col) {
                Attribute attr = (Attribute)data.getElementAt(row);
                if (attr == null) return null; // špatný řádek
                Object obj;
                switch (col) {
                    case 0: { // zobrazení atributu
                        obj = new Boolean(attr.getDisplayed());
                        break;
                    }
                    case 1: { // jméno atributu
                        obj = attr.getName();
                        break;
                    }
                    case 2: { // hodnota atributu
                        obj = attr.getValue();
                        break;
                    }
                    default: { // špatný sloupec
                        obj = null;
                        break;
                    }
                }
                return obj;
            }

                /*
                 * JTable uses this method to determine the default renderer/
                 * editor for each cell.  If we didn't implement this method,
                 * then the last column would contain text ("true"/"false"),
                 * rather than a check box.
                 */
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }

                /*
                 * Don't need to implement this method unless your table's
                 * editable.
                 */
            public boolean isCellEditable(int row, int col) {
                //Note that the data/cell address is constant,
                //no matter where the cell appears onscreen.
                if (col <= 1) {
                    return true;
                } else {
                    return false;
                }
            }

                /*
                 * Don't need to implement this method unless your table's
                 * data can change.
                 */
            public void setValueAt(Object value, int row, int col) {

                //debug("\nAtribut ve sloupci " + row + " = ");
                Attribute attr = (Attribute)data.getElementAt(row);
                //debug("" + attr);
                if (attr == null) return; // špatný řádek

                switch (col) {
                    case 0: { // zobrazení atributu
                        Boolean b = (Boolean)value;
                        attr.setDisplayed(b.booleanValue());
                        if (attr.getDisplayed()) { // atribut přidáván k zobrazovaným
                            pridejAtribut(attr.getName());
                        }
                        else { // atribut odstraňován ze zobrazovaných
                            odstranAtribut(attr.getName());
                        }
                        break;
                    }
                    case 1: { // jméno atributu
                        attr.setName(value.toString());
                        break;
                    }
                    case 2: { // hodnota atributu
                        attr.setValue(value.toString());
                        break;
                    }
                    default: { // špatný sloupec
                        break;
                    }
                }
            } // setValueAt

        } // class ModelTableAtributy

    } // class PanelTrees

