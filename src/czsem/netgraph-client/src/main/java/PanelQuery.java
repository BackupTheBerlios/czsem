

// ====================================================================================================
//		class PanelQuery			vkládání dotazu
// ====================================================================================================

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.Iterator;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.trees.*;
/* ###zk začátek aplikačního kódu */
import cz.cuni.mff.mirovsky.properties.*;
/* ###kk konec aplikačního kódu */

/**
 * Class PanelQuery creates a panel for creating a query. The user can choose attributes and their values from a list and use
 * buttons to create the structure of the query. The graphical representation of the query is displayed, as the query is being created.
 * The textual version of the query can be edited as well. A history of queries is kept. The history, as well as individual queries,
 * can be saved to a local disc and loaded back later.
 */
public class PanelQuery extends JPanel implements ActionListener, MouseListener, ListSelectionListener
/* ###zk začátek aplikačního kódu */
,Printable
/* ###kk konec aplikačního kódu */
{

    private String default_node_name = "N";

    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    private NGClient jaaa;
    public NGTreeHead ngt_global_head; // globální hlavička v objektové podobě
    private boolean timer_first_only; // uchová se informace o tom, zda se má hledat jen první výskyt v každém stromě, pro opakování dotazu timerem
    private boolean timer_invert_match; // uchová se informace o tom, zda se má invertovat matchování, pro opakování dotazu timerem
    private boolean and_or; // uchovává aktuální nastavení logického významu vícestromého dotazu (true==AND, false=OR)
    private final static boolean and_or_default = true; // implicitní hodnota je AND
    private Color box_orig_foreground; // tady se schová původní barva popisku JCheckBoxu, než ji změním po zaškrtnutí na červenou
        JTextPane pane_query; // sem se bude psát dotaz
        JComboBox combo_query_history;  // combobox pro historii dotazů
        JButton button_query_history_delete; // tlačítko pro smazání historie dotazů
        JButton button_select_all;  // dotaz na všechny stromy
        JButton button_select_query;  // dotaz na specifikované stromy
        JButton button_select_query_above_result;  // dotaz na specifikované stromy nad výsledkem minulého dotazu
        JButton button_stop_query;  // zastavi vykonavani dotazu
        JCheckBox check_first_only; // pro nastaveni hledani jen prvniho vyskytu v kazdem strome
        JCheckBox check_invert_match; // pro nastaveni invertniho matchovani dotazu
        JPanel panel_dotaz;  // pro  spojení dvou komponent do jedné (label_dotaz a edit_dotaz)
        GridBagLayout layout_panel_query; // layout pro zalozku pro tvoreni dotazu
        JList ngt_global_head_attr_names; // jména atributů
        JButton button_select_name_set; // tlačítko pro použití jména atributu
        JButton button_select_name_remove; // tlačítko pro odstranění jména atributu
        JList ngt_global_head_attr_values; // hodnoty atributů
        JPanel panel_ref_factory; // továrna na tvoření referenčního odkazu
        JButton button_ref_factory_overwrite; // tlačítko na zkopírování vytvořeného referenčního odkazu do editační řádky hodnoty atributu - přemaže se původní obsah
        JButton button_ref_factory_insert; // tlačítko na zkopírování vytvořeného referenčního odkazu do editační řádky hodnoty atributu - vloží se na aktuální místo původního obsahu
        JComboBox combo_ref_factory_node_name; // combo box pro výběr/vložení jména vrcholu, na který se odkazuje
        JComboBox combo_ref_factory_attr_name; // combo box pro výběr/vložení jména atributu, na jehož hodnotu se odkazuje
        JComboBox combo_ref_factory_char_order; // combo box pro výběr/vložení pořadí znaku hodnoty, na kterou se odkazuje
        JComboBox attr_values_user_combo;  // combobox pro historii vkládaných hodnot atributů
        JButton attr_values_user_combo_delete; // tlačítko pro smazání historie vkládaných hodnot atributů
        JButton button_select_value_regexp_replace; // tlačítko pro nastavení hodnoty atributu jako regulárního výrazu
        JButton button_select_value_regexp_add; // tlačítko pro přidání hodnoty atributu jako regulárního výrazu
        JButton button_select_value_replace; // tlačítko pro nastavení hodnoty atributu
        JButton button_select_value_add; // tlačítko pro přidání hodnoty atributu
        JRadioButton button_select_relation_eq; // tlačítko pro výběr relace rovnosti
        JRadioButton button_select_relation_neq; // tlačítko pro výběr relace nerovnosti
        JRadioButton button_select_relation_lt; // tlačítko pro výběr relace mensi nez
        JRadioButton button_select_relation_lteq; // tlačítko pro výběr relace mensi nez nebo rovno
        JRadioButton button_select_relation_gt; // tlačítko pro výběr relace vetsi nez
        JRadioButton button_select_relation_gteq; // tlačítko pro výběr relace vetsi nez nebo rovno
        ButtonGroup button_group_relations; // sgrupování tlačítek pro výběr relace
        JButton button_query_factory_new_query; // tlačítko pro vytvoření nového dotazu (starý se smaže)
        JButton button_query_factory_add_tree; // tlačítko pro vytvoření nového stromu v dotazu (přidá se)
        JButton button_query_factory_brother; // tlačítko pro vytvoření bratra vrcholu v dotazu
        JButton button_query_factory_subtree; // tlačítko pro vytvoření podstromu vrcholu v dotazu
        JButton button_query_factory_father; // tlačítko pro vytvoření otce vrcholu v dotazu
        JButton button_query_factory_or_node; // tlačítko pro vytvoření alternativního vrcholu
        JButton button_query_factory_remove_node; // tlačítko pro smazání vrcholu (a podstromu) z dotazu
        JButton button_query_factory_name_node; // tlačítko pro pojmenování vrcholu
        JTextField text_query_factory_node_name; // textové pole pro vložení jména vrcholu

        Stack stack_query_undo; // zásobník pro operaci undo
        int stack_query_undo_size_max = 40; // maximální počet položek v zásobníku pro operaci undo (mělo by to být sudé číslo - ukládají se vždy dvě položky)
        JButton button_query_undo; // tlačítko pro operaci undo
        JButton button_query_show_tree; // tlačítko pro zobrazení stromu dotazu
        JButton button_query_and_or; // tlačítko pro přepnutí logického významu vícestromého dotazu
        NGForestDisplay query_forest_view; // zde se bude zobrazovat dotaz
        JScrollPane query_tree_view_scroll_pane;

/* ###zk začátek aplikačního kódu */
        JButton button_query_load; // tlačítko pro nahrání uloženého dotazu
        JButton button_query_save; // tlačítko pro uložení dotazu
        Properties properties_saved_queries; // properties pro ukládání dotazů
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        JButton button_query_history_load; // tlačítko pro nahrání uložené historie
        JButton button_query_history_save; // tlačítko pro uložení historie
        Properties properties_saved_query_histories; // properties pro ukládání historií dotazů
        private final static String saved_history_query_prefix = "query_"; // automaticky generovaná jména jednotlivých uložených property - dotazů budou začínat tímto prefixem
/* ###kk konec aplikačního kódu */

        javax.swing.Timer timer_repeat_query; // timer pro opakované zaslání dotazu nad výsledkem minulého dotazu
        int timer_repeat_query_delay_initial; // výchozí interval pro opakování posílání dotazu (v ms)
        int timer_repeat_query_delay_actual; // aktuální interval pro opakování posílání dotazu (v ms)
        int timer_repeat_query_delay_change; // změna intervalu pro opakování posílání dotazu (v ms)
        int timer_repeat_query_counter; // pro sledování počtu opakovaných žádostí
        String repeated_query; // uschovaný dotaz pro jeho opakované poslání
        String repeated_query_transcoded; // uschovaný překódovaný dotaz pro jeho opakované poslání

        JPopupMenu popup_menu_node; // popup menu po kliku pravým tlačítkem na vrcholu
        JCheckBoxMenuItem popup_menu_node_transitive_edge; // položka popup menu - má být rodičovská hrana tranzitivní?
        JCheckBoxMenuItem popup_menu_node_optional_node; // položka menu popup - má být vrchol optional?

/* ###zk začátek aplikačního kódu */
        // pro tisk na tiskárnu:
        private PrintTreeDialog print_dialog; // objekt pro dialogové okno nastavení tisku
        private PrinterJob printer_job; // pro tisk na tiskárnu
        private NGForestPrint query_print; // objekt pro tisk dotazu na tiskárnu
        private NGForest query_printed; // strom dotazu tisknutý na tiskárnu
        private double im_height, im_width, im_y, im_x; // rozměry a umístění tisknutelné plochy
        private String text_query_printed; // textová verze dotazu k tisku
/* ###kk konec aplikačního kódu */

        public PanelQuery(NGClient p_jaaa, ShowMessagesAble p_mess, ResourceBundle p_i18n) { // konstruktor

            mess = p_mess;
            i18n = p_i18n;
            jaaa = p_jaaa;

            // následujících pět řádků je součástí nakonec neimplementovaného popup menu
            popup_menu_node = new JPopupMenu(i18n.getString("POPUP_MENU_NODE"));
            popup_menu_node_transitive_edge = new JCheckBoxMenuItem(i18n.getString("POPUP_MENU_NODE_TRANSITIVE_EDGE"));
            popup_menu_node_optional_node = new JCheckBoxMenuItem(i18n.getString("POPUP_MENU_NODE_OPTIONAL_NODE"));
            popup_menu_node.add(popup_menu_node_transitive_edge);
            popup_menu_node.add(popup_menu_node_optional_node);

            and_or = and_or_default; // inicializace and_or - logického významu vícestromého dotazu

            JPanel panel_ngt_global_head = new JPanel();
            panel_ngt_global_head.setLayout(new BoxLayout(panel_ngt_global_head, BoxLayout.X_AXIS));
            panel_ngt_global_head.setBorder(BorderFactory.createTitledBorder(i18n.getString("LABEL_GLOBAL_HEAD")));
            JPanel panel_attributes_names = new JPanel();
            panel_attributes_names.setLayout(new BoxLayout(panel_attributes_names, BoxLayout.Y_AXIS));
            panel_attributes_names.setBorder(BorderFactory.createTitledBorder(i18n.getString("GLOBAL_HEAD_ATTRIBUTES_NAMES")));
            ngt_global_head_attr_names = new JList(new DefaultListModel());
            ngt_global_head_attr_names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane attr_names_scroll = new JScrollPane(ngt_global_head_attr_names);
            button_select_name_set = new JButton(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_NAME_SET"));
            //button_select_name_set.setBorder(BorderFactory.createEtchedBorder());
            button_select_name_set.setMargin(new Insets(1,4,1,4));
            button_select_name_set.setToolTipText(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_NAME_SET_TOOLTIP"));
            button_select_name_remove = new JButton(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_NAME_REMOVE"));
            button_select_name_remove.setMargin(new Insets(1,4,1,4));
            button_select_name_remove.setToolTipText(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_NAME_REMOVE_TOOLTIP"));
            JPanel attr_names_buttons = new JPanel();
            attr_names_buttons.setLayout(new BoxLayout(attr_names_buttons,BoxLayout.X_AXIS));
            attr_names_buttons.add(button_select_name_set);
            attr_names_buttons.add(button_select_name_remove);
            panel_attributes_names.add(attr_names_scroll);
            panel_attributes_names.add(attr_names_buttons);
            JPanel panel_attributes_values = new JPanel();
            panel_attributes_values.setLayout(new BoxLayout(panel_attributes_values, BoxLayout.Y_AXIS));
            panel_attributes_values.setBorder(BorderFactory.createTitledBorder(i18n.getString("GLOBAL_HEAD_ATTRIBUTES_VALUES")));
            ngt_global_head_attr_values = new JList(new DefaultListModel());
            ngt_global_head_attr_values.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane attr_values_scroll = new JScrollPane(ngt_global_head_attr_values);
            attr_values_user_combo = new JComboBox();
            attr_values_user_combo.setEditor(new CaretControlEnabledComboBoxEditor());
            attr_values_user_combo.setEditable(true);
            attr_values_user_combo_delete = new JButton(i18n.getString("BUTTON_ATTR_VALUES_COMBO_DELETE"));
            attr_values_user_combo_delete.setToolTipText(i18n.getString("BUTTON_ATTR_VALUES_COMBO_DELETE_TOOLTIP"));
            JPanel panel_user_text = new JPanel();
            panel_user_text.setLayout(new BoxLayout(panel_user_text, BoxLayout.Y_AXIS));
            panel_user_text.setBorder(BorderFactory.createTitledBorder(i18n.getString("USER_VALUE_BORDER_TITLE")));
            panel_user_text.add(attr_values_user_combo);
            JPanel panel_values_center = new JPanel();
            panel_values_center.setLayout(new BorderLayout());
            attr_values_scroll.setPreferredSize(new Dimension(100,1000)); // tohle potřebuji roztáhnout na výšku, aby se smrskly další části panelu panel_attributes_values
            panel_values_center.add(attr_values_scroll, BorderLayout.CENTER);
            button_select_value_regexp_replace = new JButton(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_REGEXP_REPLACE"));
            button_select_value_regexp_replace.setToolTipText(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_REGEXP_REPLACE_TOOLTIP"));
            button_select_value_regexp_add = new JButton(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_REGEXP_ADD"));
            button_select_value_regexp_add.setToolTipText(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_REGEXP_ADD_TOOLTIP"));
            button_select_value_replace = new JButton(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_REPLACE"));
            button_select_value_replace.setToolTipText(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_REPLACE_TOOLTIP"));
            button_select_value_add = new JButton(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_ADD"));
            button_select_value_add.setToolTipText(i18n.getString("BUTTON_GLOBAL_HEAD_SELECT_VALUE_ADD_TOOLTIP"));
            button_select_relation_eq = new JRadioButton(i18n.getString("RADIO_RELATION_EQ"));
            button_select_relation_eq.setToolTipText(i18n.getString("RADIO_RELATION_EQ_TOOLTIP"));
            button_select_relation_neq = new JRadioButton(i18n.getString("RADIO_RELATION_NEQ"));
            button_select_relation_neq.setToolTipText(i18n.getString("RADIO_RELATION_NEQ_TOOLTIP"));
            button_select_relation_lt = new JRadioButton(i18n.getString("RADIO_RELATION_LT"));
            button_select_relation_lt.setToolTipText(i18n.getString("RADIO_RELATION_LT_TOOLTIP"));
            button_select_relation_lteq = new JRadioButton(i18n.getString("RADIO_RELATION_LTEQ"));
            button_select_relation_lteq.setToolTipText(i18n.getString("RADIO_RELATION_LTEQ_TOOLTIP"));
            button_select_relation_gt = new JRadioButton(i18n.getString("RADIO_RELATION_GT"));
            button_select_relation_gt.setToolTipText(i18n.getString("RADIO_RELATION_GT_TOOLTIP"));
            button_select_relation_gteq = new JRadioButton(i18n.getString("RADIO_RELATION_GTEQ"));
            button_select_relation_gteq.setToolTipText(i18n.getString("RADIO_RELATION_GTEQ_TOOLTIP"));
            button_group_relations = new ButtonGroup();
            button_group_relations.add(button_select_relation_eq);
            button_group_relations.add(button_select_relation_neq);
            button_group_relations.add(button_select_relation_lt);
            button_group_relations.add(button_select_relation_lteq);
            button_group_relations.add(button_select_relation_gt);
            button_group_relations.add(button_select_relation_gteq);
            button_select_relation_eq.setSelected(true);
            JPanel attr_relations_radios = new JPanel();
            attr_relations_radios.setLayout(new BoxLayout(attr_relations_radios,BoxLayout.Y_AXIS));
            attr_relations_radios.add(button_select_relation_eq);
            attr_relations_radios.add(button_select_relation_neq);
            attr_relations_radios.add(button_select_relation_lt);
            attr_relations_radios.add(button_select_relation_lteq);
            attr_relations_radios.add(button_select_relation_gt);
            attr_relations_radios.add(button_select_relation_gteq);
            JPanel panel_attr_values_buttons = new JPanel();
            panel_attr_values_buttons.setLayout(new BoxLayout(panel_attr_values_buttons,BoxLayout.X_AXIS));
            panel_attr_values_buttons.add(button_select_value_replace);
            panel_attr_values_buttons.add(button_select_value_add);
            panel_attr_values_buttons.add(attr_values_user_combo_delete);
            JPanel panel_attr_values_buttons_regexp = new JPanel();
            panel_attr_values_buttons_regexp.setLayout(new BoxLayout(panel_attr_values_buttons_regexp,BoxLayout.X_AXIS));
            panel_attr_values_buttons_regexp.add(button_select_value_regexp_replace);
            panel_attr_values_buttons_regexp.add(button_select_value_regexp_add);

            panel_ref_factory = new JPanel();
            panel_ref_factory.setLayout(new BoxLayout(panel_ref_factory, BoxLayout.Y_AXIS)); // továrna na tvoření referenčního odkazu
            panel_ref_factory.setBorder(BorderFactory.createTitledBorder(i18n.getString("REF_FACTORY_BORDER_TITLE")));
            JPanel panel_reference_value = new JPanel();
            //panel_reference_value.setLayout(new BoxLayout(panel_reference_value, BoxLayout.X_AXIS));
            panel_reference_value.setLayout(new FlowLayout(FlowLayout.LEADING));
            JPanel panel_reference_buttons = new JPanel();
            //panel_reference_buttons.setLayout(new BoxLayout(panel_reference_buttons, BoxLayout.X_AXIS));
            panel_reference_buttons.setLayout(new FlowLayout(FlowLayout.LEADING));
            button_ref_factory_overwrite = new JButton(i18n.getString("REF_FACTORY_BUTTON_OVERWRITE")); // tlačítko na zkopírování vytvořeného referenčního odkazu do editační řádky hodnoty atributu - přemaže se původní obsah
            button_ref_factory_overwrite.setMargin(new Insets(1,4,1,4));
            button_ref_factory_overwrite.setToolTipText(i18n.getString("REF_FACTORY_BUTTON_OVERWRITE_TOOLTIP"));
            button_ref_factory_insert = new JButton(i18n.getString("REF_FACTORY_BUTTON_INSERT")); // tlačítko na zkopírování vytvořeného referenčního odkazu do editační řádky hodnoty atributu - vloží se na aktuální místo do původního obsahu
            button_ref_factory_insert.setMargin(new Insets(1,4,1,4));
            button_ref_factory_insert.setToolTipText(i18n.getString("REF_FACTORY_BUTTON_INSERT_TOOLTIP"));
            combo_ref_factory_node_name = new JComboBox(); // combo box pro výběr/vložení jména vrcholu, na který se odkazuje
            combo_ref_factory_node_name.setToolTipText(i18n.getString("REF_FACTORY_COMBO_NODE_NAME_TOOLTIP"));
            combo_ref_factory_node_name.setEditable(true);
            combo_ref_factory_node_name.setPreferredSize(new Dimension(80,25));
            combo_ref_factory_attr_name = new JComboBox(); // combo box pro výběr/vložení jména atributu, na jehož hodnotu se odkazuje
            combo_ref_factory_attr_name.setToolTipText(i18n.getString("REF_FACTORY_COMBO_ATTR_NAME_TOOLTIP"));
            combo_ref_factory_attr_name.setEditable(true);
            combo_ref_factory_attr_name.setPreferredSize(new Dimension(110,25));
            combo_ref_factory_char_order = new JComboBox(); // combo box pro výběr/vložení pořadí znaku hodnoty, na kterou se odkazuje
            combo_ref_factory_char_order.setToolTipText(i18n.getString("REF_FACTORY_COMBO_CHAR_ORDER_TOOLTIP"));
            fillComboRefCharOrder(combo_ref_factory_char_order);
            combo_ref_factory_char_order.setEditable(true);
            combo_ref_factory_char_order.setPreferredSize(new Dimension(60,25));

            panel_reference_value.add(combo_ref_factory_node_name);
            panel_reference_value.add(combo_ref_factory_attr_name);
            //panel_reference_value.add(combo_ref_factory_char_order);
            panel_reference_buttons.add(button_ref_factory_overwrite);
            panel_reference_buttons.add(button_ref_factory_insert);
            panel_reference_buttons.add(combo_ref_factory_char_order);
            panel_ref_factory.add(panel_reference_value);
            panel_ref_factory.add(panel_reference_buttons);

            JPanel panel_values_south = new JPanel();
            panel_values_south.setLayout(new BoxLayout(panel_values_south, BoxLayout.Y_AXIS));
            panel_values_south.add(panel_ref_factory);
            panel_values_south.add(panel_user_text);
            panel_values_south.add(panel_attr_values_buttons);
            panel_values_south.add(panel_attr_values_buttons_regexp);

            //panel_attributes_values.setMaximumSize(new Dimension(300,1000));
            panel_ngt_global_head.setMaximumSize(new Dimension(500,1000));

            panel_attributes_values.add(panel_values_center, BorderLayout.CENTER);
            panel_attributes_values.add(panel_values_south, BorderLayout.SOUTH);
            panel_ngt_global_head.add(panel_attributes_names);
            panel_ngt_global_head.add(attr_relations_radios);
            panel_ngt_global_head.add(panel_attributes_values);

            button_query_undo = new JButton(i18n.getString("BUTTON_QUERY_UNDO"));
            button_query_undo.setMargin(new Insets(1,3,1,3));
            button_query_undo.setToolTipText(i18n.getString("BUTTON_QUERY_UNDO_TOOLTIP"));
            button_query_show_tree = new JButton(i18n.getString("BUTTON_QUERY_SHOW_TREE"));
            button_query_show_tree.setMargin(new Insets(1,4,1,4));
            button_query_show_tree.setToolTipText(i18n.getString("BUTTON_QUERY_SHOW_TREE_TOOLTIP"));
            button_query_and_or = new JButton(getButtonAndOrLabel(and_or));
            button_query_and_or.setMargin(new Insets(1,4,1,4));
            button_query_and_or.setToolTipText(i18n.getString("BUTTON_QUERY_AND_OR_TOOLTIP"));

            button_query_factory_new_query = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_NEW_QUERY"));
            button_query_factory_new_query.setMargin(new Insets(1,4,1,4));
            button_query_factory_new_query.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_NEW_QUERY_TOOLTIP"));
            button_query_factory_add_tree = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_ADD_TREE"));
            button_query_factory_add_tree.setMargin(new Insets(1,4,1,4));
            button_query_factory_add_tree.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_ADD_TREE_TOOLTIP"));
            button_query_factory_brother = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_BROTHER"));
            button_query_factory_brother.setMargin(new Insets(1,4,1,4));
            button_query_factory_brother.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_BROTHER_TOOLTIP"));
            button_query_factory_subtree = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_SUBTREE"));
            button_query_factory_subtree.setMargin(new Insets(1,4,1,4));
            button_query_factory_subtree.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_SUBTREE_TOOLTIP"));
            button_query_factory_father = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_FATHER"));
            button_query_factory_father.setMargin(new Insets(1,4,1,4));
            button_query_factory_father.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_FATHER_TOOLTIP"));
            button_query_factory_or_node = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_OR_NODE"));
            button_query_factory_or_node.setMargin(new Insets(1,4,1,4));
            button_query_factory_or_node.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_OR_NODE_TOOLTIP"));
            button_query_factory_remove_node = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_REMOVE_NODE"));
            button_query_factory_remove_node.setMargin(new Insets(1,4,1,4));
            button_query_factory_remove_node.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_REMOVE_NODE_TOOLTIP"));
            button_query_factory_name_node = new JButton(i18n.getString("BUTTON_QUERY_FACTORY_NAME_NODE"));
            button_query_factory_name_node.setToolTipText(i18n.getString("BUTTON_QUERY_FACTORY_NAME_NODE_TOOLTIP"));
            button_query_factory_name_node.setMargin(new Insets(1,4,1,4));
            text_query_factory_node_name = new JTextField();

            setTextFieldFirstFreeDefaultNodeName(); // nastavím nabízené defaultní jméno

            JPanel panel_query_factory_1 = new JPanel();
            panel_query_factory_1.setLayout(new FlowLayout(FlowLayout.LEADING));
            panel_query_factory_1.add(button_query_factory_new_query);
            panel_query_factory_1.add(button_query_factory_add_tree);
            panel_query_factory_1.add(button_query_factory_subtree);
            panel_query_factory_1.add(button_query_factory_father);
            JPanel panel_query_factory_2 = new JPanel();
            panel_query_factory_2.setLayout(new FlowLayout(FlowLayout.LEADING));
            panel_query_factory_2.add(button_query_factory_brother);
            panel_query_factory_2.add(button_query_factory_or_node);
            panel_query_factory_2.add(button_query_factory_remove_node);
            JPanel panel_query_factory_3 = new JPanel();
            panel_query_factory_3.setLayout(new BoxLayout(panel_query_factory_3, BoxLayout.X_AXIS));
            panel_query_factory_3.add(button_query_factory_name_node);
            panel_query_factory_3.add(text_query_factory_node_name);
            JPanel panel_query_factory_4 = new JPanel();
            panel_query_factory_4.setLayout(new FlowLayout(FlowLayout.LEADING));
            panel_query_factory_4.add(button_query_undo);
            panel_query_factory_4.add(button_query_show_tree);
            panel_query_factory_4.add(button_query_and_or);
            JPanel panel_query_factory = new JPanel();
            panel_query_factory.setLayout(new BoxLayout(panel_query_factory, BoxLayout.Y_AXIS));
            panel_query_factory.setBorder(BorderFactory.createTitledBorder(i18n.getString("LABEL_QUERY_FACTORY")));
            panel_query_factory.add(panel_query_factory_1);
            panel_query_factory.add(panel_query_factory_2);
            panel_query_factory.add(panel_query_factory_3);
            panel_query_factory.add(panel_query_factory_4);

            query_forest_view = new NGForestDisplay(jaaa);
            query_forest_view.setPreferredSize(new Dimension(500,500));
            query_forest_view.setEmphasizeChosenNode(true);
            query_tree_view_scroll_pane = new JScrollPane(query_forest_view);
            query_tree_view_scroll_pane.setPreferredSize(new Dimension(1000,1000));
            query_tree_view_scroll_pane.setBorder(BorderFactory.createTitledBorder(i18n.getString("LABEL_QUERY_TREE_VIEW")));
            JPanel panel_query_right = new JPanel();
            panel_query_right.setLayout(new BorderLayout());
            panel_query_right.setPreferredSize(new Dimension(1000,1000));
            panel_query_right.add(query_tree_view_scroll_pane, BorderLayout.CENTER);
            panel_query_right.add(panel_query_factory, BorderLayout.SOUTH);

            // panel dotazu:
            pane_query = new JTextPane();
            //JScrollPane scroll_pane_query = new JScrollPane(pane_query);

/* ###zk začátek aplikačního kódu */
            button_query_load = new JButton(i18n.getString("BUTTON_QUERY_LOAD"));
            button_query_load.setToolTipText(i18n.getString("BUTTON_QUERY_LOAD_TOOLTIP"));

            button_query_save = new JButton(i18n.getString("BUTTON_QUERY_SAVE"));
            button_query_save.setToolTipText(i18n.getString("BUTTON_QUERY_SAVE_TOOLTIP"));

            JPanel panel_query_load_save = new JPanel();
            panel_query_load_save.setLayout(new FlowLayout());
            panel_query_load_save.add(button_query_load, null);
            panel_query_load_save.add(button_query_save, null);
/* ###kk konec aplikačního kódu */

            JPanel panel_query = new JPanel();
            panel_query.setBorder(BorderFactory.createTitledBorder(i18n.getString("LABEL_QUERY")));
            panel_query.setLayout(new BorderLayout());
            panel_query.add(pane_query, BorderLayout.CENTER);
/* ###zk začátek aplikačního kódu */
            panel_query.add(panel_query_load_save, BorderLayout.EAST);
/* ###kk konec aplikačního kódu */

            // panel historie dotazů:

            combo_query_history = new JComboBox(); // vkládání dotazu
            combo_query_history.setEditable(false);

            button_query_history_delete = new JButton(i18n.getString("BUTTON_QUERY_HISTORY_DELETE"));
            button_query_history_delete.setToolTipText(i18n.getString("BUTTON_QUERY_HISTORY_DELETE_TOOLTIP"));

/* ###zk začátek aplikačního kódu */
            button_query_history_load = new JButton(i18n.getString("BUTTON_QUERY_HISTORY_LOAD"));
            button_query_history_load.setToolTipText(i18n.getString("BUTTON_QUERY_HISTORY_LOAD_TOOLTIP"));

            button_query_history_save = new JButton(i18n.getString("BUTTON_QUERY_HISTORY_SAVE"));
            button_query_history_save.setToolTipText(i18n.getString("BUTTON_QUERY_HISTORY_SAVE_TOOLTIP"));
/* ###kk konec aplikačního kódu */

            JPanel panel_query_history_load_save_delete = new JPanel();
            panel_query_history_load_save_delete.setLayout(new FlowLayout());
/* ###zk začátek aplikačního kódu */
            panel_query_history_load_save_delete.add(button_query_history_load, null);
            panel_query_history_load_save_delete.add(button_query_history_save, null);
/* ###kk konec aplikačního kódu */
            panel_query_history_load_save_delete.add(button_query_history_delete, null);

            JPanel panel_query_history = new JPanel();
            panel_query_history.setBorder(BorderFactory.createTitledBorder(i18n.getString("LABEL_QUERY_HISTORY")));
            panel_query_history.setLayout(new BorderLayout());
            panel_query_history.add(combo_query_history, BorderLayout.CENTER);
            panel_query_history.add(panel_query_history_load_save_delete, BorderLayout.EAST);

            // panel dotazu a historie dotazů:

            panel_dotaz = new JPanel(new BorderLayout());
            panel_dotaz.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
            panel_dotaz.add(panel_query, BorderLayout.CENTER);
            panel_dotaz.add(panel_query_history, BorderLayout.SOUTH);
            button_select_all = new JButton(i18n.getString("BUTTON_SELECT_ALL_TREES"));
            button_select_all.setToolTipText(i18n.getString("BUTTON_SELECT_ALL_TREES_TOOLTIP"));
            button_select_query = new JButton(i18n.getString("BUTTON_SELECT_TREES_BY_QUERY"));
            button_select_query.setToolTipText(i18n.getString("BUTTON_SELECT_TREES_BY_QUERY_TOOLTIP"));
            button_select_query_above_result = new JButton(i18n.getString("BUTTON_SELECT_TREES_BY_QUERY_ABOVE_RESULT"));
            button_select_query_above_result.setToolTipText(i18n.getString("BUTTON_SELECT_TREES_BY_QUERY_ABOVE_RESULT_TOOLTIP"));
            button_stop_query = new JButton(i18n.getString("BUTTON_STOP_QUERY"));
            button_stop_query.setToolTipText(i18n.getString("BUTTON_STOP_QUERY_TOOLTIP"));
            check_first_only = new JCheckBox(i18n.getString("CHECK_FIRST_ONLY"));
            check_first_only.setToolTipText(i18n.getString("CHECK_FIRST_ONLY_TOOLTIP"));
            check_first_only.setSelected(false);
            check_invert_match = new JCheckBox(i18n.getString("CHECK_INVERT_MATCH"));
            check_invert_match.setToolTipText(i18n.getString("CHECK_INVERT_MATCH_TOOLTIP"));
            check_invert_match.setSelected(false);

            JPanel panel_buttons_select_trees = new JPanel();
            panel_buttons_select_trees.setLayout(new FlowLayout());

            panel_buttons_select_trees.add(button_stop_query, null);
            panel_buttons_select_trees.add(check_first_only, null);
            panel_buttons_select_trees.add(check_invert_match, null);
            panel_buttons_select_trees.add(button_select_query_above_result, null);
            panel_buttons_select_trees.add(button_select_query, null);
            panel_buttons_select_trees.add(button_select_all, null);

            JPanel panel_bottom_buttons = new JPanel();
            panel_bottom_buttons.setLayout(new BorderLayout());
            //panel_bottom_buttons.add(panel_load_save, BorderLayout.WEST);
            panel_bottom_buttons.add(panel_buttons_select_trees, BorderLayout.EAST);

            setLayout(new BorderLayout());
            add(panel_ngt_global_head, BorderLayout.WEST);
            add(panel_query_right, BorderLayout.CENTER);

            JPanel panel_bottom = new JPanel();
            panel_bottom.setLayout(new BoxLayout(panel_bottom, BoxLayout.Y_AXIS));

            panel_bottom.add(panel_dotaz);
            panel_bottom.add(panel_bottom_buttons);

            add(panel_bottom, BorderLayout.SOUTH);

            popup_menu_node_transitive_edge.addActionListener(this);
            popup_menu_node_optional_node.addActionListener(this);
            button_select_all.addActionListener(this);
            button_select_query.addActionListener(this);
            button_select_query_above_result.addActionListener(this);
            button_stop_query.addActionListener(this);

            combo_query_history.addActionListener(this);
            button_query_history_delete.addActionListener(this);
            ngt_global_head_attr_names.addMouseListener(this);
            ngt_global_head_attr_names.addListSelectionListener(this);
            button_select_name_set.addActionListener(this);
            button_select_name_remove.addActionListener(this);
            ngt_global_head_attr_values.addMouseListener(this);
            ngt_global_head_attr_values.addListSelectionListener(this);
            button_select_value_replace.addActionListener(this);
            button_select_value_add.addActionListener(this);
            button_select_value_regexp_replace.addActionListener(this);
            button_select_value_regexp_add.addActionListener(this);
            attr_values_user_combo_delete.addActionListener(this);
            button_query_factory_new_query.addActionListener(this);
            button_query_factory_add_tree.addActionListener(this);
            button_query_factory_subtree.addActionListener(this);
            button_query_factory_father.addActionListener(this);
            button_query_factory_brother.addActionListener(this);
            button_query_factory_or_node.addActionListener(this);
            button_query_factory_remove_node.addActionListener(this);
            button_query_factory_name_node.addActionListener(this);
            button_query_undo.addActionListener(this);
            button_query_show_tree.addActionListener(this);
            button_query_and_or.addActionListener(this);
            query_forest_view.addMouseListener(this);
            pane_query.addMouseListener(this);
/* ###zk začátek aplikačního kódu */
            button_query_load.addActionListener(this);
            button_query_save.addActionListener(this);
            button_query_history_load.addActionListener(this);
            button_query_history_save.addActionListener(this);
/* ###kk konec aplikačního kódu */
            button_ref_factory_overwrite.addActionListener(this);
            button_ref_factory_insert.addActionListener(this);
            check_first_only.addActionListener(this);
            check_invert_match.addActionListener(this);
            // konec nastaveni udalosti
            box_orig_foreground = check_invert_match.getForeground(); // uchovám puvodni barvu checkboxu, abych ji mohl menit na cervenou a zpet

            stack_query_undo = new Stack(); // zásobník pro operaci undo

            timer_repeat_query_delay_initial = 250; // výchozích 250 ms před opakovaným zasláním dotazu nad výsledkem min. dotazu
            timer_repeat_query_delay_change = 50; // změna intervalu (v ms) před opakovaným zasláním dotazu nad výsledkem min. dotazu
            timer_repeat_query = new javax.swing.Timer(timer_repeat_query_delay_initial, this); // timer pro opakované zaslání dotazu nad výsl. min. dotazu
            timer_repeat_query.setRepeats(false); // pouze jedenkrát vyvolat událost

/* ###zk začátek aplikačního kódu */
            // pro tisk na tiskárnu:
            query_print = new NGForestPrint(jaaa); // objekt pro tisk dotazu na tiskárnu
            query_print.getTreeProperties().setShowMultipleSets(true);
            query_print.getTreeProperties().setShowMultipleSetsChosen(true);
            query_print.getTreeProperties().setShowLemmaVariants(true);
            query_print.getTreeProperties().setShowLemmaComments(true);

            print_dialog = new PrintTreeDialog(jaaa,i18n.getString("DIALOG_PRINT_TREE_TITLE"),true,
            query_print.getPrintProperties(), jaaa, i18n); // nechci to modální kvůli nápovědě, ale je to takto snadnější
/* ###kk konec aplikačního kódu */


        } // PanelQuery (konstruktor)

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


/* ###zk začátek aplikačního kódu */
        public void saveActionsDisconnect() { // ukládání informací na disk při odpojování od serveru
            saveSavedQueries(); // uložím uložené dotazy na disk
            saveSavedQueryHistories(); // uložím uložené historie dotazů na disk
        }

        public void saveActionsExit() { // ukládání informací na disk před ukončením aplikace (akce z saveActionsDisconnect by se tu neměly opakovat)
            return;
        }

        public void readGeneralProperties(Properties properties) { // přečtu properties pro tento objekt
            int font_size = properties.getIntProperty("main menu","font size in queries",12);
            query_forest_view.setFontSize(font_size);
            jaaa.setMenuOptionsFontsizeQuery(font_size);
            return;
        } // readGeneralProperties

        public void writeGeneralProperties(Properties properties) { // zapíše properties z tohoto objektu
            properties.updateProperty("main menu","font size in queries", ""+query_forest_view.getFontSize(),"font size in query trees (integer)");
            return;
        } // writeGeneralProperties


/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void saveSavedQueries() { // uložím uložené dotazy na disk
            if (properties_saved_queries == null) return; // není co ukládat
            PropertiesLoader loader = new PropertiesLoader(jaaa);
            loader.saveProperties("netgraph","saved_queries", properties_saved_queries);
        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void loadSavedQueries() { // nahraji uložené dotazy z disku
            PropertiesLoader loader = new PropertiesLoader(jaaa);
            properties_saved_queries = loader.loadProperties("netgraph","saved_queries");
            if (properties_saved_queries == null) {
                //debug("\nVytvářím implicitní seznam uložených dotazů.");
                properties_saved_queries = new Properties();
            }

        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void loadSavedQueryHistories() { // nahraji uložené historie dotazů z disku
            PropertiesLoader loader = new PropertiesLoader(jaaa);
            properties_saved_query_histories = loader.loadProperties("netgraph","saved_query_histories");
            if (properties_saved_query_histories == null) {
                //debug("\nVytvářím implicitní seznam uložených historií dotazů.");
                properties_saved_query_histories = new Properties();
            }

        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void saveSavedQueryHistories() { // uložím uložené historie dotazů na disk
            if (properties_saved_query_histories == null) return; // není co ukládat
            PropertiesLoader loader = new PropertiesLoader(jaaa);
            loader.saveProperties("netgraph","saved_query_histories", properties_saved_query_histories);
        }
/* ###kk konec aplikačního kódu */

        private int findEndOfValue(String src_text, int position) { // najdu konec dané hodnoty začínající na position v definici dotazu src_text
            int index;
            int index_carka = src_text.indexOf(',',position);
            int index_konec_uzlu = src_text.indexOf(']',position);
            if (index_carka == -1) { // carka nenalezena
                if (index_konec_uzlu == -1) { // to by nemělo být, syntaktická chyba, vezmu to do konce řetězce
                    return src_text.length();
                }
                else { // nalezen jen konec uzlu
                    return index_konec_uzlu;
                }
            }
            else { // carka nalezena
                if (index_konec_uzlu == -1) { // nalezena jen carka
                    return index_carka;
                }
                else { // nalezeno oboje
                    if (index_carka < index_konec_uzlu) return index_carka;
                    else return index_konec_uzlu;
                }
            }
        } // findEndOfValue

        /**
         * It puts the escape character in front of every character serving as functional in fs structure.
         * It means that it escapes '[', ']', '|', '(', ')', ',', and '"', but not '.' and '*'.
         * @param value String
         * @return String
         */
        private String escapeRegexp(String value) {
            StringBuffer result = new StringBuffer();
            result.append('"');
            for (int i=0; i<value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                  case '[': result.append("\\["); break;
                  case ']': result.append("\\]"); break;
                  case '|': result.append("\\|"); break;
                  case '(': result.append("\\("); break;
                  case ')': result.append("\\)"); break;
                  case ',': result.append("\\,"); break;
                  case '=': result.append("\\="); break;
                  case '"': result.append("\\\""); break;
                  case '\\': result.append("\\\\"); break;
                  default: result.append(c);
                }
            }
            result.append('"');
            return result.toString();
        } // escapeRegexp

        /**
         * It puts the escape character in front of every character serving as functional in fs structure.
         * It means that it escapes '[', ']', '|', '(', ')', ',', and '"', but not '.' and '*'.
         * It also adds starting end ending character of a regular expression.
         * @param value String
         * @return String
         */
        private String escapeValue(String value) {
            StringBuffer result = new StringBuffer();
            for (int i=0; i<value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                  case '[': result.append("\\["); break;
                  case ']': result.append("\\]"); break;
                  case '|': result.append("\\|"); break;
                  case '(': result.append("\\("); break;
                  case ')': result.append("\\)"); break;
                  case ',': result.append("\\,"); break;
                  case '=': result.append("\\="); break;
                  case '"': result.append("\\\""); break;
                  case '\\': result.append("\\\\"); break;
                  default: result.append(c);
                }
            }
            return result.toString();
        } // escapeValue

        private String getButtonAndOrLabel(boolean and_or) {
            StringBuffer label = new StringBuffer(i18n.getString("BUTTON_QUERY_AND_OR") + " ");
            if (and_or) {
                label.append(i18n.getString("BUTTON_QUERY_AND"));
            }
            else {
                label.append(i18n.getString("BUTTON_QUERY_OR"));
            }
            return label.toString();
        }

        private void fillWithAttrValues(DefaultComboBoxModel combo_box_model, String attr_name, String src_text) { // naplní daný model nalezenými hodnotami daného atributu v daném řetězci
            int pos=0; // začnu řetězec prohledávat od začátku
            int attr_name_length = attr_name.length();
            int pos_attr, pos_end_val;
            String value;
            //debug("\nPanelQuery.fillWithAttrValues: looking for values of attribute" + attr_name + "in the string " + src_text);
            while ((pos_attr = src_text.indexOf(attr_name, pos)) != -1) { // dokud nacházím dané jméno atributu
                //debug("\nPanelQuery.fillWithAttrValues: the attr name has been found");
                pos = pos_attr + attr_name_length; // ukážu přesně za jméno toho atributu
                //debug("\nPanelQuery.fillWithAttrValues: the character after the name is " + src_text.charAt(pos));
                switch (src_text.charAt(pos)) {
                    case '=': // šlo o relaci rovnost
                        pos ++;
                        break;
                    case '!':
                        pos ++;
                        if (src_text.charAt(pos) == '=') { // šlo o relaci nerovnost
                            pos ++;
                            break;
                        }
                        else { // nešlo o žádnou relaci (vykřičník bez rovnítka)
                            continue; // zkouším hledat dál od místa za vykřičníkem
                        }
                    case '<':
                    case '>':
                        pos ++;
                        if (src_text.charAt(pos) == '=') { // šlo o relaci menší nebo rovno
                            pos ++;
                            break;
                        }
                        else { // šlo o relaci menší než
                            break;
                        }
                    default: // za jménem atributu nebylo znaménko relace
                        continue; // pokračuji v hledání od místa za jménem atributu
                } // switch
                // nyní jsem na začátku hodnoty nalezeného atributu (index pos)
                //debug("\nPanelQuery.fillWithAttrValues: looking for the end of a value");
                pos_end_val = findEndOfValue(src_text, pos); // najdu konec dané hodnoty
                //debug("\nPanelQuery.fillWithAttrValues: the value ends at the position " + pos_end_val);
                if (pos_end_val != pos) { // pokud délka hodnoty je nenulová
                    value = src_text.substring(pos, pos_end_val); // vezmu jen tu hodnotu
                    //debug("\nPanelQuery.fillWithAttrValues: the value is " + value);
                    combo_box_model.addElement(value);
                }
                pos = pos_end_val; // hledám další
            } // while
        } // fillWithAttrValues

        private void fillComboRefCharOrder(JComboBox combo) { // naplní combo box pro výběr určitého znaku referencované hodnoty
            combo.removeAllItems(); // odstraním případné vše předchozí
            combo.addItem(NGTreeHead.NODE_REFERENCE_CHARACTER_ORDER_UNSPECIFIED); // nejprve vložím položku pro neomezování na určitý znak
            for (int i=1; i<=20; i++) { // naplním combo box čísly 1 - 20
                combo.addItem(""+i);
            }
            //combo.setPreferredSize(new Dimension(35,25));
            combo.setSelectedIndex(0); // vyberu první položku
        } // fillComboRefCharOrder

        private void fillComboRefAttrName(JComboBox combo) { // naplní combo box pro výběr jména atributu referencované hodnoty
            Attribute atr;
            String atr_name;
            combo.removeAllItems(); // odstraním případné vše předchozí
            int gh_size = ngt_global_head.getSize(); // počet atributů v globální hlavičce
            for (int i=0; i<gh_size; i++) { // naplním combo box jmény atributů z globální hlavičky
                atr = ngt_global_head.getAttributeAt(i);
                atr_name = atr.getName();
                if (atr_name.equals("_transitive") || atr_name.equals("_optional") || atr_name.equals("_name") || atr_name.equals("_#occurrences")) { // tyto atributy nemohou být použity v referenčních odkazech
                    continue;
                }
                else {
                    combo.addItem(atr);
                }
            }
            //combo.setPreferredSize(new Dimension(35,20));
            combo.setSelectedIndex(0); // vyberu první položku
        } // fillComboRefCharOrder

        private void fillComboRefNodeName(JComboBox combo) { // naplní combo box pro výběr jména uzlu referencované hodnoty
            combo.removeAllItems(); // odstraním případné vše předchozí
            // teď musím projít napsaný text dotazu a sebrat všechna jména atributů (někdo tam mohl psát ručně)
            DefaultComboBoxModel combo_box_model = new DefaultComboBoxModel();
            fillWithAttrValues(combo_box_model, "_name", pane_query.getText()); // naplní daný model nalezenými hodnotami daného atributu v daném řetězci
            combo.setModel(combo_box_model);
            //combo.setPreferredSize(new Dimension(35,25));
            if (combo.getModel().getSize()!=0) { // pokud není combo box se jmény vrcholů prázdný
                combo.setSelectedIndex(0); // vyberu první položku
            }
        } // fillComboRefCharOrder

        public void naplnGlobalniHlavicku(String gh) { // naplní globální hlavičku

            ngt_global_head = new NGTreeHead(jaaa);
            char[] gha = gh.toCharArray();

            ngt_global_head.readTreeHead(gha,0);

            ngt_global_head_attr_names.setModel(ngt_global_head.getModel());
            fillComboRefAttrName(combo_ref_factory_attr_name); // naplním také combo box pro tvorbu referenčních odkazů
        }


        // --------------------------------- odchycení událostí ----------------------------------


        public void actionPerformed(ActionEvent e) { // akce (doubleclick nebo mezerník nebo enter)
            Object zdroj = e.getSource();

            if (jaaa.kom_net.isConnected()) { // pokud jsem vůbec připojen k nějakému serveru

                //				if (zdroj == popup_menu_node_transitive_edge) { // přepnutí tranzitivnosti rodičovské hrany vrcholu
                //					debug ("\nPřepnut radio button 'popup_menu_node_transitive_edge'");
                //		    	}
                //			    if (zdroj == popup_menu_node_optional_node) { // přepnutí vynechatelnosti vrcholu (optional node)
                //					debug ("\nPřepnut radio button 'popup_menu_node_optional_node'");
                //		    	}
                if (zdroj == button_select_all) { // vybrání všech stromů
                    //debug ("\nStisknuto tlačítko 'button_select_all'");
                    selectAllTrees();
                }
                else if (zdroj == button_select_query) { // vybrání stromů dle dotazu
                    //debug ("\nStisknuto tlačítko 'button_select_query' (vybrání stromů dle dotazu)");
                    selectTreesByTheQuery(false); // false == nechci hledat nad výsledkem minulého dotazu, nýbrž nad všemi vybranými soubory
                }
                else if (zdroj == button_select_query_above_result) { // vybrání stromů dle dotazu nad výsledkem minulého dotazu
                    //debug ("\nStisknuto tlačítko 'button_select_query_above_result' (vybrání stromů dle dotazu nad výsledkem minulého dotazu)");
                    selectTreesByTheQuery(true); // true == chci hledat nad výsledkem minulého dotazu
                }
                else if (zdroj == button_stop_query) { // zastaví provádění dotazu
                    //debug ("\nStisknuto tlačítko 'button_stop_query' (zastavení provádění dotazu)");
                    stopTheQuery();
                }
                else if (zdroj == button_select_name_set) { //
                    //debug ("\nStisknuto tlačítko k nastavení atributu " + o_atribut);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    if (ngt_global_head_attr_names.isSelectionEmpty()) return;
                    String o_atribut = new String(ngt_global_head_attr_names.getSelectedValue().toString());
                    queryNameSet(o_atribut);
                    queryShowTree();
                }
                else if (zdroj == button_select_name_remove) { //
                    //debug ("\nStisknuto tlačítko ke smazání atributu " + o_atribut);
                    if (ngt_global_head_attr_names.isSelectionEmpty()) return;
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    String o_atribut = new String(ngt_global_head_attr_names.getSelectedValue().toString());
                    queryNameRemove(o_atribut);
                    queryShowTree();
                }
                else if (zdroj == button_select_value_regexp_replace) { //
                    String value = attr_values_user_combo.getSelectedItem().toString();
                    String o_value = escapeRegexp(value);
                    addValueToValuesCombo(value);
                    //debug ("\nStisknuto tlačítko k nastavení hodnoty atributu regulárním výrazem " + o_value);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryValue(o_value, true);
                    queryShowTree();
                }
                else if (zdroj == button_select_value_regexp_add) { //
                    String value = attr_values_user_combo.getEditor().getItem().toString();
                    String o_value = escapeRegexp(value);
                    addValueToValuesCombo(value);
                    //debug ("\nStisknuto tlačítko k nastavení hodnoty atributu regulárním výrazem " + o_value);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryValue(o_value, false);
                    queryShowTree();
                }
                else if (zdroj == button_select_value_replace) { //
                    String value = attr_values_user_combo.getEditor().getItem().toString();
                    String o_value = escapeValue(value);
                    addValueToValuesCombo(value);
                    //debug ("\nStisknuto tlačítko k nastavení hodnoty atributu " + o_value);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryValue(o_value, true);
                    queryShowTree();
                }
                else if (zdroj == button_select_value_add) { //
                    String value = attr_values_user_combo.getEditor().getItem().toString();
                    String o_value = escapeValue(value);
                    addValueToValuesCombo(value);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    //debug ("\nStisknuto tlačítko k přidání hodnoty atributu " + o_value);
                    queryValue(o_value, false);
                    queryShowTree();
                }
                else if (zdroj == attr_values_user_combo_delete) { //
                    //debug ("\nStisknuto tlačítko pro smazání historie ručně vkládaných hodnot atributů.);
                    attr_values_user_combo.removeAllItems();
                }
                else if (zdroj == combo_query_history) { // vybrání prvku historie
                    //debug ("\nAkce v combo_query_history - kopiruji vybrany prvek do aktualniho dotazu");
                    if (combo_query_history.getSelectedIndex() == -1) return; // nic není vybráno; možná prázdný seznam
                    String s = combo_query_history.getSelectedItem().toString().trim();
                    pane_query.setText((String)combo_query_history.getSelectedItem());
                    queryShowTree();
                }
                else if (zdroj == button_query_history_delete) { // smaže historii dotazů
                    //debug ("\nStisknuto tlačítko ke smazání historie dotazů.");
                    combo_query_history.removeAllItems();
                }
                else if (zdroj == button_query_factory_new_query) { // nový prázdný dotaz
                    //debug ("\nStisknuto tlačítko k vytvoření nového prázdného dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryNewForest();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_add_tree) { // nový strom se přidá k dotazu
                    //debug ("\nStisknuto tlačítko k přidání nového prázdného stromu k dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryAddTree();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_subtree) { // podstrom aktuálního vrcholu v dotazu
                    //debug ("\nStisknuto tlačítko k vytvoření podstromu v dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    querySubtree();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_father) { // nový otec aktuálního vrcholu v dotazu
                    //debug ("\nStisknuto tlačítko k vložení nového otce v dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryFather();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_brother) { // bratr aktuálního vrcholu v dotazu
                    //debug ("\nStisknuto tlačítko k vytvoření bratra v dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryBrother();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_or_node) { // alternativní vrchol aktuálního vrcholu v dotazu
                    //debug ("\nStisknuto tlačítko k vytvoření alternativního vrcholu k aktuálnímu vrcholu v dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryOrNode();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_remove_node) { // alternativní vrchol aktuálního vrcholu v dotazu
                    //debug ("\nStisknuto tlačítko pro vymazání aktuálního vrcholu a jeho podstromu z dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryRemoveNode();
                    queryShowTree();
                }
                else if (zdroj == button_query_factory_name_node) { // pojmenování aktuálního vrcholu v dotazu
                    //debug ("\nStisknuto tlačítko pro pojmenování aktuálního vrcholu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryNameNode();
                    queryShowTree();
                }
                else if (zdroj == button_query_undo) { // návrat k předchozímu stavu dotazu
                    //debug ("\nStisknuto tlačítko k návratu k předchozímu stavu dotazu.");
                    queryUndo();
                    queryShowTree();
                }
                else if (zdroj == button_query_show_tree) { // zobrazení dotazu jako stromu
                    //debug ("\nStisknuto tlačítko k zobrazení dotazu jako stromu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryShowTree();
                }
                else if (zdroj == button_query_and_or) { // přepnutí logického významu vícestromého dotazu
                    //debug ("\nStisknuto tlačítko ke změně logického významu vícestromého dotazu.");
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryAndOr();
                }
                else if (zdroj == button_ref_factory_overwrite) { // nastaví editační hodnotu atributu na referenční odkaz
                    //debug ("\nStisknuto tlačítko k přepsání editační hodnoty atributu referenčním odkazem.");
                    referenceCopy(true);
                }
                else if (zdroj == button_ref_factory_insert) { // vloží do editační hodnoty atributu referenční odkaz
                    //debug ("\nStisknuto tlačítko k vložení referenčního odkazu do editační hodnoty atributu.");
                    referenceCopy(false);
                }
/* ###zk začátek aplikačního kódu */
                else if (zdroj == button_query_load) { // vybrání uloženého dotazu
                    jaaa.setWaitCursor();
                    //debug ("\nStisknuto tlačítko k výběru uloženého dotazu.");
                    queryLoadDialog();
                    jaaa.setDefaultCursor();
                }
                else if (zdroj == button_query_save) { // uložení dotazu
                    jaaa.setWaitCursor();
                    //debug ("\nStisknuto tlačítko k uložení dotazu.");
                    querySaveDialog();
                    jaaa.setDefaultCursor();
                }
                else if (zdroj == button_query_history_load) { // vybrání uložené historie dotazů
                    jaaa.setWaitCursor();
                    //debug ("\nStisknuto tlačítko k výběru uložené historie dotazů.");
                    queryHistoryLoadDialog();
                    jaaa.setDefaultCursor();
                }
                else if (zdroj == button_query_history_save) { // uložení historie dotazů
                    jaaa.setWaitCursor();
                    //debug ("\nStisknuto tlačítko k uložení historie dotazů.");
                    queryHistorySaveDialog();
                    jaaa.setDefaultCursor();
                }
/* ###kk konec aplikačního kódu */
                else if (zdroj == timer_repeat_query) { // je čas pro opakované zaslání dotazu nad výsl. min. dotazu
                    //debug ("\nUdálost vyvolaná timerem timer_repeat_query");
                    timerRepeatQueryPerformed();
                }
                else if (zdroj == check_first_only) { // zmena zaskrtnuti
                    //debug ("\nUdálost vyvolaná checkboxem check_first_only");
                    colorize(check_first_only);
                }
                else if (zdroj == check_invert_match) { // zmena zaskrtnuti
                    //debug ("\nUdálost vyvolaná checkboxem check_invert_match");
                    colorize(check_invert_match);
                }
            }
            else {
                inform("YOU_ARE_DISCONNECTED");
            }
        }	// actionPerformed


        public void mouseClicked(MouseEvent e) { // kliknutí myší

            Object zdroj = e.getSource();
            int pocet_kliku = e.getClickCount();

            if (zdroj == ngt_global_head_attr_names) {
                if (pocet_kliku == 2) { // použití atributu do dotazu
                    String o_atribut = new String(ngt_global_head_attr_names.getSelectedValue().toString());
                    //debug ("\nDouble click na seznamu jmen atributů - na atributu " + o_atribut);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    queryNameSet(o_atribut);
                    queryShowTree();
                }
            }
            else if (zdroj == ngt_global_head_attr_values) {
                if (pocet_kliku == 2) { // použití hodnoty atributu do dotazu
                    String value = new String(ngt_global_head_attr_values.getSelectedValue().toString());
                    //debug ("\nDouble click na seznamu hodnot atributů - na hodnotě " + value);
                    queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
                    String o_value = escapeValue(value);
                    queryValue(o_value,false);
                    queryShowTree();
                }
            }
        } // mouseClicked

        public void mousePressed(MouseEvent e) {
            Object zdroj = e.getSource();

            if (zdroj == query_forest_view) {
                //debug ("\nStisknuto tlačítko myši na stromu dotazu.");
                int order_of_selected_node = query_forest_view.selectNode(e);
                //debug (" - vybraný vrchol je " + order_of_selected_node + ". při průchodu stromem do hloubky.");
                if (order_of_selected_node > 0) { // byl-li vybran vrchol

                    query_forest_view.repaint();

                    String q = pane_query.getText();
                    queryUndoPush(q, pane_query.getCaretPosition());
                    StringBuffer query = new StringBuffer(q); // vezmu dotaz
                    int position = queryMoveToNodeByDepthOrder(query, order_of_selected_node); // umístí kurzor v dotazu na ten vrchol
                    if (position > 0) { // jestliže nalezen začátek správného vrcholu
                        //pane_query.setText(query.toString()); // nový dotaz zobrazím
                        pane_query.setCaretPosition(position); // umístím kurzor na novou pozici
                    }
                    pane_query.requestFocus();

                    int modif = e.getModifiers();
                    //if ((modif & InputEvent.BUTTON3_MASK) != 0) {
                    //debug("\nThe right button on the mouse was pressed.");
                    //	displayQueryNodePopupMenu(query_tree_view.getTree().getChosenNode());
                    //}
                }
            }

            else if (zdroj == pane_query) {
                //debug ("\nStisknuto tlačítko myši na textu dotazu.");
                String q = pane_query.getText();
                int p = pane_query.getCaretPosition();
                queryUndoPush(q,p);
                int order = queryGetDepthOrderOfNode(new StringBuffer(q),p); // zjistí se, na kterém vrcholu v dotazu je kurzor
                query_forest_view.setChosenNodeByDepthOrder(order); // ten se pak vybere ve stromu
                query_forest_view.repaint();
            }

        } // mousePressed

        public void mouseExited(MouseEvent e) {return;}
        public void mouseReleased(MouseEvent e) {return;}
        public void mouseEntered(MouseEvent e) {return;}

        public void valueChanged(ListSelectionEvent e) {
            Object zdroj = e.getSource();
            if (zdroj == ngt_global_head_attr_names) { // vybrání atributu; je potřeba zobrazit jeho případné možné hodnoty
                if (ngt_global_head_attr_names.isSelectionEmpty()) return;
                Attribute attr = (Attribute)ngt_global_head_attr_names.getSelectedValue();
                String o_atribut = new String(attr.toString());
                //debug ("\nSingle click na seznamu jmen atributů - na atributu " + o_atribut);
                DefaultListModel list = attr.getListOfValues();
                if (list == null) list = new DefaultListModel();
                ngt_global_head_attr_values.setModel(list);
            }
            if (zdroj == ngt_global_head_attr_values) { // zkopírování hodnoty atributu do editační řádky
                if (ngt_global_head_attr_values.isSelectionEmpty()) return;
                String o_value = new String(ngt_global_head_attr_values.getSelectedValue().toString());
                setTextToValuesCombo(o_value);
            }
        }


        // ------------------------------ konec odchycení událostí -------------------------------

        private void startRepeatQueryTimer() { // spustí časovač pro opakované zaslání dotazu nad výsledkem minulého dotazu
            timer_repeat_query_counter = 0; // půjde o první opakované zaslání
            inform("REPEAT_QUERY");
            timer_repeat_query_delay_actual = timer_repeat_query_delay_initial; // startovací hodnota
            timer_repeat_query.setDelay(timer_repeat_query_delay_initial);
            timer_repeat_query.setInitialDelay(timer_repeat_query_delay_initial);
            timer_repeat_query.start(); // po události vyvolané timerem
        } // startRepeatQueryTimer

        private void restartRepeatQueryTimer() { // znovu spustí časovač pro opakované zaslání dotazu nad výsledkem minulého dotazu
            timer_repeat_query_delay_actual += timer_repeat_query_delay_change; // prodloužení intervalu
            timer_repeat_query.setDelay(timer_repeat_query_delay_actual);
            timer_repeat_query.setInitialDelay(timer_repeat_query_delay_actual);
            timer_repeat_query.start();
        } // startRepeatQueryTimer

        private void stopRepeatQueryTimer() { // zastaví časovač pro opakované zaslání dotazu nad výsledkem minulého dotazu
            timer_repeat_query.stop();
        } // stopRepeatQueryTimer

        private void timerRepeatQueryPerformed() { // serveru se má znovu poslat dotaz nad výsledkem minulého dotazu
            timer_repeat_query.stop(); // zastavit timer, pokud běží (neměl by)
            repeatQuery(); // (opakovaná) žádost
        }

        private void repeatQuery() { // zašle znovu dotaz nad výsledkem minulého dotazu

            stopRepeatQueryTimer(); // pokud se čekalo na skončení před-předchozího dotazu, tak se to zruší

            int ok = jaaa.kom_net.nastavStromyDleDotazu(repeated_query_transcoded, true, jaaa.lemma_variants_match, jaaa.lemma_comments_match, timer_first_only, timer_invert_match);
            if (ok == 0) { // dotaz úspěšný
                storeToHistory(repeated_query); // uschová dotaz do historie
                jaaa.hlavni_zalozky.setSelectedIndex(2); // zobrazeni zalozky stromu
                timer_repeat_query_counter = 0; // příště počítat znovu od nuly
            }
            else {
                if (ok == 1) { // dotaz nad výsledkem dotazu je třeba znovu opakovat (předchozí dotaz stále ještě nedoběhl)
                    timer_repeat_query_counter++; // počítadlo opakovaných žádostí
                    jaaa.addInfo("DOT",""); // opakovaná žádost - vypiš jen další tečku
                    restartRepeatQueryTimer(); // opět nastartuji časovač
                }
            }
        } // repeatQuery

        //private boolean isSetValue(TAHLine values, String value) { // zjistí, zda v některé z alternativních hodnot je daná hodnota
        //    while (values != null) {
        //		if (values.Value != null) {
        //			if (values.Value.equalsIgnoreCase(value)) {
        //			    return true;
        //			}
        //		}
        //		values = values.Next;
        //	}
        //	return false;
        //} // isSetValue

        //private boolean isSetAttribute(TNode n, int attr_number, String value) { // zjistí, zda v některé ze sad a hodnot je daný atribut nastaven na danou hodnotu
        //	int number_of_sets = n.getNumberOfSets();
        //	TAHLine values;
        //	TValue set;
        //	for (int i=0; i<number_of_sets; i++) { // přes všechny sady
        //		set = n.getSetOfAttributes(i);
        //		if (set != null) { // to by mělo platit vždy
        //		    values = set.AHTable[attr_number]; // nyní mám spoják alternativních hodnot
        //			if (isSetValue(values, value)) {
        //			    return true;
        //			}
        //		}
        //	}
        //	return false;
        //} // isSetAttribute

        //private void displayQueryNodePopupMenu(TNode n) { // zobrazí u vybraného vrcholu popup menu; aktuální hodnoty položek určí podle hodnot u vrcholu
        //    popup_menu_node_transitive_edge.setEnabled(true); // implicitně povoleno
        //	int transitive_number = ngt_global_head.getIndexOfAttribute(Attribute.meta_transitive_parent_edge);
        //	int optional_number = ngt_global_head.getIndexOfAttribute(Attribute.meta_optional_node);
        //	if (isSetAttribute(n,transitive_number,"true")) { // nastavím aktuální hodnoty z údajů v uzlu
        //		popup_menu_node_transitive_edge.setSelected(true);
        //		if (isSetAttribute(n,transitive_number,"false")) { // pokud je současně nastaven např. v jiné sadě na false
        //		    popup_menu_node_transitive_edge.setEnabled(false); // nelze nastavovat pomocí tohoto menu
        //		}
        //	}
        //	else popup_menu_node_transitive_edge.setSelected(false);
        //	if (isSetAttribute(n,optional_number,"true")) { // nastavím aktuální hodnoty z údajů v uzlu
        //		popup_menu_node_optional_node.setSelected(true);
        //		if (isSetAttribute(n,optional_number,"false")) { // pokud je současně nastaven např. v jiné sadě na false
        //		    popup_menu_node_optional_node.setEnabled(false); // nelze nastavovat pomocí tohoto menu
        //		}
        //	}
        //	else popup_menu_node_optional_node.setSelected(false);

        //	popup_menu_node.show(query_tree_view, n.getX(), n.getY());
        //} // displayQueryNodePopupMenu()

        private void storeToHistory(String query) { // uschová dotaz do historie dotazů
            combo_query_history.removeItem(query); // kdyby už byl v historii, tak ho nejdřív vyndám
            combo_query_history.insertItemAt(query,0); // a znovu vložím (na začátek) -  to aby se neopakoval
            combo_query_history.setSelectedIndex(0); // a vyberu ho, aby nebyl vybraný (a do textového pole se nezkopíroval) jiný
        } // storeToHistory

        private void colorize(JCheckBox box) {
            boolean state = box.isSelected();
            if (state) {
                box.setForeground(Color.red);
            }
            else {
                box.setForeground(box_orig_foreground);
            }
        }

        private void selectAllTrees() { // vybere všechny stromy
            jaaa.setWaitCursor();

            stopRepeatQueryTimer(); // pokud se čekalo na skončení před-předchozího dotazu, tak se to zruší
            timer_invert_match = false; // uchovám nastavení pro případné opakování dotazu timerem; u výběru všech stromů by true nemělo smysl
            check_invert_match.setSelected(false); // a vynuluji checkbox na základní hodnotu - tedy neinvertování příštího dotazu
            colorize(check_invert_match); // obarvi checkbox spravnou barvou
            boolean ok = jaaa.kom_net.nastavVsechnyStromy();
            if (ok) { // pokud uspesne nastavení
                debug("\nA query: all trees");
                jaaa.zalozka_trees.strom_view.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_DIM);
                jaaa.hlavni_zalozky.setSelectedIndex(2); // zobrazeni zalozky stromu
            }
            jaaa.setDefaultCursor();
        } // selectAllTrees

        private void stopTheQuery() { // zastaví provádění dotazu
            jaaa.setWaitCursor();
            stopRepeatQueryTimer(); // pokud se čekalo na skončení před-předchozího dotazu, tak se to zruší

            jaaa.kom_net.stopTheQuery();
            jaaa.zalozka_trees.displayStatistics();

            jaaa.setDefaultCursor();
        } // stopTheQuery

        private void selectTreesByTheQuery(boolean above_result) { // vybere stromy dle dotazu
            // pokud above_result == true, pak nad výsledkem minulého dotazu, jinak nad vybranými soubory
            jaaa.setWaitCursor();

            stopRepeatQueryTimer(); // pokud se čekalo na skončení před-předchozího dotazu, tak se to zruší

            String s = pane_query.getText().trim(); // odstranění bílých znaků na obou koncích řetězce

            //debug ("\nDotaz: " + s + ", tj. ");
            int i,c,j;
            //for (i=0; i<s.length(); i++) {
            //	c=s.charAt(i);
            //	j=(int)c;
            //	debug (j + " ");
            //} // to byl ladicí výpis

            String s2; // sem se prelozi dotaz do kodovani utf-8 (uz ne isolatin2)
            /*if (jaaa.coding_in_queries == CharCode.coding_pseudo) s2 = new String(CharCode.unicodeToIsolatin2(CharCode.pseudoToUnicode(s)));
            else s2 = new String(CharCode.unicodeToIsolatin2(s));*/

            if (jaaa.coding_in_queries == CharCode.coding_pseudo) s2 = new String(CharCode.pseudoToUnicode(s));
            else s2 = s;

            //debug ("\nDotaz v isolatin2: " + s2 + ", tj. ");
            //for (i=0; i<s2.length(); i++) {
            //	c=s2.charAt(i);
            //	j=(int)c;
            //	debug (j + " ");
            //} // to byl ladicí výpis
            timer_invert_match = check_invert_match.isSelected(); // uchovám nastavení pro případné opakování dotazu timerem
            timer_first_only = check_first_only.isSelected(); // uchovám nastavení pro případné opakování dotazu timerem
            check_invert_match.setSelected(false); // a vynuluji checkbox na základní hodnotu - tedy neinvertování příštího dotazu
            colorize(check_invert_match); // obarvi checkbox spravnou barvou
            //check_first_only.setSelected(false); // a vynuluji checkbox na základní hodnotu - tedy hledání všech výskytů příštího dotazu
            //colorize(check_first_only);
            int ok = jaaa.kom_net.nastavStromyDleDotazu(s2, above_result, jaaa.lemma_variants_match, jaaa.lemma_comments_match, timer_first_only, timer_invert_match);
            if (ok == 0) { // dotaz úspěšný
                storeToHistory(s); // uschová dotaz do historie
                debug("\nA query: " + s);
                // jelikož se stále zobrazuje nalezený strom z minulého dotazu, nastavím barevné schéma DIM
                jaaa.zalozka_trees.strom_view.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_DIM);
                jaaa.hlavni_zalozky.setSelectedIndex(2); // zobrazeni zalozky stromu
            }
            else {
                if (ok == 1) { // dotaz nad výsledkem dotazu je třeba opakovat (předchozí dotaz ještě nedoběhl)
                    repeated_query = new String(s); // uložím dotaz
                    repeated_query_transcoded = new String(s2); // uložím překódovaný dotaz
                    startRepeatQueryTimer(); // nastartuji časovač
                }
            }
            jaaa.setDefaultCursor();
        } // selectTreesByTheQuery


/* ###zk začátek aplikačního kódu */
        private void queryLoadDialog() {

            if (properties_saved_queries == null) loadSavedQueries(); // první volání - nahraji dotazy z disku

            QuerySelectionDialog sd = new QuerySelectionDialog(jaaa,properties_saved_queries.getSection("saved queries"),i18n,"QUERY_LOAD_DIALOG_", ngt_global_head);
            sd.setCoreferencePatterns(query_forest_view.getReferencePatterns());
            //debug("\nOtvírám dialogové okno pro výběr dotazu...");
            boolean selected = sd.showOpenDialog();
            if (selected) { // uživatel vybral položku
                //debug("\nVybrán dotaz se jménem " + sd.getSelectedName());
            }
            else { // uživatel zrušil výběr
                //debug("\nUživatel zrušil výběr.");
                return;
            }
            String query_value = sd.getSelectedValue();
            queryUndoPush(pane_query.getText(),pane_query.getCaretPosition());
            pane_query.setText(query_value);
            queryShowTree();

        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void querySaveDialog() {

            String query_value = pane_query.getText();
            if (query_value.length() == 0) return; // není co ukládat

            if (properties_saved_queries == null) loadSavedQueries(); // první volání - nahraji dotazy z disku

            QuerySelectionDialog sd = new QuerySelectionDialog(jaaa,properties_saved_queries.getSection("saved queries"),i18n,"QUERY_SAVE_DIALOG_", ngt_global_head);
            sd.setCoreferencePatterns(query_forest_view.getReferencePatterns());
            //debug("\nOtvírám dialogové okno pro výběr dotazu...");
            boolean selected = sd.showSaveDialog();
            if (selected) { // uživatel vybral položku
                //debug("\nVybrán dotaz se jménem " + sd.getSelectedName() + " a obsahem \n" + sd.getSelectedValue());
            }
            else { // uživatel zrušil výběr
                //debug("\nUživatel zrušil výběr.");
                return;
            }
            String query_name = sd.getSelectedName();
            String query_comment = sd.getSelectedComment();
            if (query_comment == null) query_comment = "";
            Property p = new Property("saved queries",query_name,query_value,query_comment);
            properties_saved_queries.setProperty(p);
        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void queryHistoryLoadDialog() {

            if (properties_saved_query_histories == null) loadSavedQueryHistories(); // první volání - nahraji historie z disku

            ItemSelectionDialog sd = new ItemSelectionDialog(jaaa,properties_saved_query_histories,i18n,"QUERY_HISTORY_LOAD_DIALOG_");
            //debug("\nOtvírám dialogové okno pro výběr historie dotazů...");
            boolean selected = sd.showOpenDialog();
            if (selected) { // uživatel vybral položku
                //debug("\nVybrána historie se jménem " + sd.getSelectedName());
            }
            else { // uživatel zrušil výběr
                //debug("\nUživatel zrušil výběr.");
                return;
            }
            String history_name = sd.getSelectedName();
            PropertiesSection history = properties_saved_query_histories.getSection(history_name); // vezmu si tu vybranou sekci
            Iterator iterator = history.getIteratorOverValues(); // iterátor přes prvky historie - dotazy
            combo_query_history.removeAllItems(); // smažu bývalou historii
            Property query; // sem budu načítat jednotlivé dotazy
            String query_value; // sem jejich obsahy
            while (iterator.hasNext()) { // přes všechny dotazy
                query = (Property)iterator.next(); // další dotaz
                query_value = query.getValue(); // obsah property - vlastní dotaz
                combo_query_history.addItem(query_value); // zařadím tento dotaz do historie
            }
        }
/* ###kk konec aplikačního kódu */

/* ###zk začátek aplikačního kódu */
        private void queryHistorySaveDialog() {

            int number_of_queries = combo_query_history.getItemCount(); // počet prvků v ukládané historii
            if (number_of_queries == 0) return; // není co ukládat

            if (properties_saved_query_histories == null) loadSavedQueryHistories(); // první volání - nahraji historie z disku

            ItemSelectionDialog sd = new ItemSelectionDialog(jaaa,properties_saved_query_histories,i18n,"QUERY_HISTORY_SAVE_DIALOG_");
            //debug("\nOtvírám dialogové okno pro výběr historie dotazů...");
            boolean selected = sd.showSaveDialog();
            if (selected) { // uživatel vybral položku
                //debug("\nVybrána historie se jménem " + sd.getSelectedName() + " a obsahem \n" + sd.getSelectedValue());
            }
            else { // uživatel zrušil výběr
                //debug("\nUživatel zrušil výběr.");
                return;
            }
            // historie se uloží jako jedna sekce
            String history_name = sd.getSelectedName(); // toto se uloží jako jméno sekce
            String history_comment = sd.getSelectedComment(); // komentář sekce
            if (history_comment == null) history_comment = "";

            String query_value; // zde se bude vždy uchovávat jeden dotaz
            String query_name; // zde se bude generovat jméno dotazu
            Property query; // zde se vytvoří vždy jedna property pro jeden dotaz v historii
            for (int i=0; i<number_of_queries; i++) { // přes všechny dotazy v historii
                query_value = (String)combo_query_history.getItemAt(i); // jeden dotaz z historie
                query_name = saved_history_query_prefix + (number_of_queries - i); // vytvořím jméno dotazu (s pořadovým číslem jako odlišujícím prvkem); sestupné pořadí zachová pořádí v combo boxu
                query = new Property(history_name,query_name,query_value); // vytvořím property uchovávající jeden dotaz
                properties_saved_query_histories.setProperty(query); // uložím property
            }
            properties_saved_query_histories.getSection(history_name).setComment(history_comment); // na závěr přidám komentář
        }
/* ###kk konec aplikačního kódu */

        /**
         * Returns to the previous state of the query
         */
        private void queryUndo() {
            String state = "";
            int caret_position = 0;
            boolean uspech = false;

            if (!stack_query_undo.empty()) {
                try {
                    caret_position = ((Integer)stack_query_undo.pop()).intValue();
                }
                catch (Exception e) {
                    debug("\nException " + e + " při operaci undo.");
                    caret_position = 0;
                }
            }
            if (!stack_query_undo.empty()) {
                try {
                    state = (String)stack_query_undo.pop();
                    uspech = true;
                }
                catch (Exception e) {
                    debug("\nException " + e + " při operaci undo.");
                }
            }
            if (uspech) {
                //debug("\nPanelQuery.queryUndo: retrieving query " + state + " with caret position " + caret_position);
                pane_query.setText(state);
                pane_query.setCaretPosition(caret_position);
                pane_query.requestFocus();
            }
        }

        /**
         * Adds the current state of the query to the undo stack
         */
        private void queryUndoPush(String state, int caret_position) {
            //debug("\nPanelQuery.queryUndoPush: storing query " + state + " with caret position " + caret_position);
            int size = stack_query_undo.size();
            if (size > 1) { // pokud už tam je něco vloženo, tak nejprve zkontroluji, jestli nevkládám to samé
                if (state.equalsIgnoreCase((String)stack_query_undo.get(size-2))) { // je tam to samé na vrcholu
                    if (caret_position == (Integer)stack_query_undo.get(size-1)) { // stejné včetně pozice vrcholu
                        //debug ("\n" + state + " = " + stack_query_undo.get(size-2));
                        return; // neudělám tedy nic
                    }
                }
            }
            stack_query_undo.push(state);
            stack_query_undo.push(new Integer(caret_position));
            if (stack_query_undo.size() > stack_query_undo_size_max) { // odříznu první položku
                //debug("\nOřezávám zásobník undo dotazu (zespoda)");
                stack_query_undo.removeElementAt(0);
                stack_query_undo.removeElementAt(0);
            }
        }

        private void queryShowTree() {
            try {
                NGForest query_forest = new NGForest(jaaa);
                query_forest.setHead(ngt_global_head);
                String q = pane_query.getText();
                String log_exp = getAndOrFromTextQuery(q);
                if (log_exp.length()>0) { // there was something before the first node in the text form of the query - presumably AND or OR
                    and_or = getAndOrFromString(log_exp);
                    //debug("\nPanelQuery.queryShowTree: Text dotazu začínal něčím jiným než uzlem: " + log_exp.trim());
                    //debug("\nPanelQuery.queryShowTree: Z toho se nastavil logický výraz na: " + and_or);
                }
                querySetAndOr(and_or); // pokud uživatel nevložil informaci o logickém spojení případných více stromů dotazu, udělám to za něj
                q = pane_query.getText();
                //debug("\nPanelQuery.queryShowTree: Dotaz po zkontrolování a případném doplnění logického výrazu: " + q);
                String pure_query = getPureQuery(q);
                query_forest.readForest(pure_query.toCharArray(), 0, ngt_global_head.getSize());
                DefaultListModel query_selected_attributes = new DefaultListModel();
                vyberAtributyPouziteVDotazu(query_forest, query_selected_attributes);
                query_forest.setVybraneAtributy(query_selected_attributes);
                query_forest_view.setForest(query_forest);
                int order = queryGetDepthOrderOfNode(new StringBuffer(q),pane_query.getCaretPosition()); // zjistí se, na kterém vrcholu v dotazu je kurzor
                query_forest_view.setChosenNodeByDepthOrder(order); // ten se pak vybere ve stromu
                query_forest_view.getTreeProperties().setShowAttrNames(true); // chci zobrazovat jména atributů
                query_forest_view.getTreeProperties().setShowNullValues(false); // nevyplněné atributy ať nezabírají místo
                query_forest_view.getTreeProperties().setShowMultipleSets(true); // zobrazovat alternativní sady atributů
		        query_forest_view.getTreeProperties().setDirection(jaaa.nodes_ordering_in_trees); // řazení uzlů ve stromě dotazu
                query_forest_view.getTreeProperties().setShowHiddenNodes(true) ; // zobrazovat skryté vrcholy v dotazu
                query_forest_view.getTreeProperties().setHighlightOptionalNodes(true); // zvýraznovat optional uzly
                query_forest_view.getTreeProperties().setHighlightTransitiveEdges(true); // zvýraznovat tranzitivní hrany
                query_forest_view.getTreeProperties().setHighlightZeroOccurrenceNodes(true); // zvýraznovat zero-occurrence uzly

                query_forest_view.repaint();
                query_tree_view_scroll_pane.revalidate();
                pane_query.requestFocus();
            }
            catch (Exception e) {
                debug("\nNeočekávaná chyba při zobrazování stromu dotazu: " + e);
            }
            fillComboRefNodeName(combo_ref_factory_node_name); // aktualizuji seznam použitých jmen vrcholů
            setTextFieldFirstFreeDefaultNodeName(); // nabídnu první nepoužité defaultní jméno pro pojmenování vrcholu
        }

        /**
         * Coppies a created reference to the text box for editing value of an attribute. If overwrite == true, the previous value is replaced; otherwise the new value is inserted
         */
        private void referenceCopy(boolean overwrite) {
            String node_name, attr_name, char_order;
            CaretControlEnabledComboBoxEditor editor;
            try {
                node_name = combo_ref_factory_node_name.getSelectedItem().toString();
                attr_name = combo_ref_factory_attr_name.getSelectedItem().toString();
                char_order = combo_ref_factory_char_order.getSelectedItem().toString();
            }
            catch (Exception e) { // nepodařilo se získat položky pro referenční odkaz
                return;
            }
            if (node_name.length()==0) { // není určeno jméno uzlu - referenci nevytvořím
                return;
            }
            String reference = NGTreeHead.NODE_REFERENCE_START + node_name + NGTreeHead.NODE_REFERENCE_ATTR_NAME_DELIMITER + attr_name;
            if (!char_order.equalsIgnoreCase(NGTreeHead.NODE_REFERENCE_CHARACTER_ORDER_UNSPECIFIED)) { // reference je omezena na jeden znak
                reference += NGTreeHead.NODE_REFERENCE_CHARACTER_ORDER_DELIMITER + char_order;
            }
            reference += NGTreeHead.NODE_REFERENCE_END;
            if (overwrite) {
                setTextToValuesCombo(reference);
                //attr_values_user_combo.setCaretPosition(reference.length());
                editor = (CaretControlEnabledComboBoxEditor)attr_values_user_combo.getEditor();
                editor.setCaretPosition(reference.length());

            }
            else {
                editor = (CaretControlEnabledComboBoxEditor)attr_values_user_combo.getEditor();
                int position = editor.getCaretPosition();
                StringBuffer value = new StringBuffer(attr_values_user_combo.getEditor().getItem().toString());
                value.insert(position,reference);
                setTextToValuesCombo(value.toString());
                editor.setCaretPosition(position+reference.length());
            }
        } // referenceCopy

        private void setTextToValuesCombo(String value) {
          attr_values_user_combo.getEditor().setItem(value);
        }

        private void addValueToValuesCombo(String value) {
          boolean found = false;
          for (int i = 0; i<attr_values_user_combo.getItemCount(); i++) {
            if (attr_values_user_combo.getItemAt(i).toString().compareTo(value) == 0) {
              found = true;
            }
          }
          if (found == false) {
            attr_values_user_combo.addItem(value);
            attr_values_user_combo.setSelectedItem(value);
          }
        }

        /**
         * Adds to the selected_attributes list attributes occured at least once in the query
         */
        private void vyberAtributyPouziteVDotazu(NGForest query_forest, DefaultListModel selected_attributes) {
            // projdu to přes všechny atributy, s každým z nich projdu celým stromem;
            // tím pádem budu mít ty atributy seřazené podle jejich pořadí v globální hlavičce;
            // to sice není nutné, ale je to pěkné
            String name; // jméno atributu
            int number_of_attributes = ngt_global_head.getSize(); // maximální počet atributů
            for (int i = 0; i < number_of_attributes; i++) { // přes všechny atributy
                name = ngt_global_head.getAttributeAt(i).toString();
                //debug("\nHledám atribut " + name + " na pozici " + i);
                for (NGTree tree : query_forest.getTrees()) {
                    if (isDefinedInSubtree(tree.getRoot(), i)) { // pokud je definován, přidám ho k vybraným
                        selected_attributes.addElement(name);
                        //debug(" - nalezen.");
                        break; // nepokračuji dalším stromem - mohl bych ho nechtěně přidat víckrát
                    }
                    //else debug(" - nenalezen.");
                }
            }
        }

        /**
         * Returns true if attribute with the specified order in the head is at least once specified in the subtree of the node, including the node
         */
        private boolean isDefinedInSubtree(TNode node, int attribute_order) {
            boolean found = false; // dosud jsem ten atribut nenašel
            while (!found && node != null) {
                found = isDefinedInNode(node, attribute_order);
                if (!found) found = isDefinedInSubtree(node.first_son, attribute_order); // pokud nebyl zde, hledám v podstromu
                node = node.brother; // a kdyžtak pokračuji bratrem
            }
            return found;
        }

        /**
         * Returns true if attribute_name is specified in the given node
         */
        private boolean isDefinedInNode(TNode node, int attribute_order) {
            boolean found = false; // zatím nenalezen v žádné sadě atributů
            TValue values = node.values;
            TAHLine[] set; // jedna sada atributů
            TAHLine attribute; // jeden atribut

            while (!found && values != null) { // dokud jsem nenalezl a je tam další sada atributů
                try { // kdyby byl problém s polem
                    set = values.AHTable; // vezmu pole hodnot atributů v této sadě
                    attribute = set[attribute_order];
                    while (!found && attribute != null) { // tohle je přes různé hodnoty jednoho atributu?
                        if (attribute.Value != null && attribute.Value.length()>0) {
                            //debug ("- trefa, hodnota = " + attribute.Value);
                            found = true;
                        }
                        attribute = attribute.Next;
                    }
                }
                catch (Exception e) {
                    debug("\nPanelQuery.isDefinedInNode: Neočekávaná chyba " + e + "při prohledávání definovaných atributů.");
                }
                values = values.Next;
            }
            return found;
        }

        /**
         * Sets the selected name of attribute to the query
         */
        private void queryNameSet(String attribute) {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            int target_position = -1;
            int begin_position;
            int length = query.length();
            boolean before = false; // signalizuje, je-li potřeba vložit čárku před jméno atributu
            boolean insert = false; // signalizuje, je-li potřeba vložit jméno atributu

            try { // kvůli nepředvídanému překročení hranic řetězce

                if (query.toString().equalsIgnoreCase("")) { // dotaz je dosud prázdný, vytvořím nový strom
                    //debug("\nDotaz je prázdný - vytvářím nový strom");
                    queryNewForest();
                    query = new StringBuffer(pane_query.getText());
                    length = query.length();
                    target_position = 1;
                    before=false;
                    insert=true;
                    //debug(" - vytvořen.");
                }
                else { // dosud nenalezeno vhodné místo pro umístění atributu
                    begin_position=target_position=toBeginOfNode(query,position,length,false); // přesunu se na začátek vrcholu
                    if (target_position != -1) { // byl nalezen začátek vrcholu
                        target_position = findAttribute(query,begin_position,length,attribute); // vyskytuje se již toto jméno?
                        if (target_position == -1) { // pokud jméno nebylo nalezeno, je tedy potřeba je vložit
                            target_position = toEndOfNode(query,begin_position,length); // přesunu se na konec vrcholu
                            if (target_position == length) { // to je hloupá situace, kdy není uzavírací závorka vrcholu; dodělám ji
                                query.insert(target_position,"]");
                                length++;
                            }
                            if ((query.charAt(target_position-1) == ',' || query.charAt(target_position-1) == '[')
                            && noOddBackslashes(query,target_position-1)) {
                                before = false;
                            }
                            else {
                                before = true;
                            }
                            insert = true;
                        }
                        else { // jméno nalezeno, již tedy dříve vloženo; target_position je vhodně nastaveno
                            before=false;
                            insert=false;
                        }
                    }
                }

                if (target_position == -1) { // nenašlo se vhodné místo pro umístění atributu
                    return;
                }

                if (before) { // je potřeba vložit nejprve čárku
                    //debug("\nVkládám čárku.");
                    query.insert(target_position++, ",");
                }
                if (insert) { // je potřeba vložit jméno atributu
                    //debug("\nVkládám atribut a znaménko relace.");
                    String relation = "="; // defaultní relace
                    if (button_select_relation_eq.isSelected()) { // zvolena relace rovnost
                        relation = "=";
                    }
                    else if (button_select_relation_neq.isSelected()) { // zvolena relace nerovnost
                        relation = "!=";
                    }
                    else if (button_select_relation_lt.isSelected()) { // zvolena relace mensi nez
                        relation = "<";
                    }
                    else if (button_select_relation_lteq.isSelected()) { // zvolena relace mensi nebo rovno
                        relation = "<=";
                    }
                    else if (button_select_relation_gt.isSelected()) { // zvolena relace vetsi nez
                        relation = ">";
                    }
                    else if (button_select_relation_gteq.isSelected()) { // zvolena relace vetsi nebo rovno
                        relation = ">=";
                    }
                    query.insert(target_position, attribute + relation);
                    target_position += attribute.length() + relation.length();
                }
                button_select_relation_eq.setSelected(true); // vždy nastavuji zpět rovnítko jako příští relaci
                pane_query.setText(query.toString()); // nový dotaz zobrazím
                pane_query.setCaretPosition(target_position); // umístím kurzor na novou pozici

            }
            catch (Exception e) {
                debug("\nChyba " + e + " při vkládání jména atributu v tvorbě dotazu (fce queryNameSet).");
            }
            pane_query.requestFocus();
        }

        private void queryNameRemove(String attribute) {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            int target_position = -1;
            int begin_position;
            int length = query.length();

            try { // kvůli nepředvídanému překročení hranic řetězce

                if (query.toString().equalsIgnoreCase("")) { // dotaz je dosud prázdný, nedělám nic
                    return;
                }
                else { // hledám atribut
                    begin_position=target_position=toBeginOfNode(query,position,length,false); // přesunu se na začátek vrcholu
                    if (target_position != -1) { // byl nalezen začátek vrcholu
                        target_position = findAttribute(query,begin_position,length,attribute); // vyskytuje se již toto jméno?
                        if (target_position != -1) { // pokud jméno bylo nalezeno, může se smazat
                            // nejprve odstraním hodnoty
                            removeOldValues(query,target_position,length);
                            length = query.length(); // aktualizuji délku
                            // nyní se může odstranit atribut
                            int shift = removeNameOfAttribute(query,target_position,length);
                            length = query.length(); // opět aktualizuji délku
                            //debug("\nlength = " + length + ", target_position = " + target_position + ", shift = " + shift + ".");

                            pane_query.setText(query.toString()); // nový dotaz zobrazím

                            target_position -= shift;
                            if (target_position < length) {
                                if (query.charAt(target_position)=='[') target_position++; // umístím kurzor za závorku
                            }
                            pane_query.setCaretPosition(target_position); // umístím kurzor na novou pozici
                        }
                    }
                }
            }
            catch (Exception e) {
                debug("\nChyba " + e + " při odstraňování atributu v tvorbě dotazu (fce queryNameRemove).");
            }
            pane_query.requestFocus();
        } // queryNameRemove


        /**
         * Removes a name of an attribute on an actual place in the query
         */
        private int removeNameOfAttribute(StringBuffer query,int position,int length) {
            int number = 0; // počet znaků ke smazání
            position--; // jdu na rovnítko
            boolean found = false;
            while (position>=0) { // dokud můžu hledat začátek atributu
                if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // nalezeno
                    found = true;
                    break;
                }
                if (query.charAt(position)==',' && noOddBackslashes(query,position)) { // nalezeno
                    found = true;
                    break;
                }
                //debug("\nNalezen znak '" + query.charAt(position) + "' ze jména atributu.");
                number++;
                position--;
            }
            if (found) position++; // vrátím se před závorku nebo čárku
            //debug("\nOdstraněno " + number + "znaků ze jména atributu.");
            query.replace(position,position+number,""); // smažu vše najednou
            length = query.length();
            position--; // podívám se na znak před
            if (query.charAt(position) == ',') { // odstraním ještě čárku před
                query.replace(position,position+1,"");
                number++;
            }
            else if (query.charAt(position) == '[' && position < length - 1) { // nejsem-li znak před koncem
                if (query.charAt(++position) == ',') { // odstraním čárku za
                    query.replace(position,position+1,"");
                    number++;
                }
            }
            return number;
        }

        private int queryMoveToNodeByDepthOrder(StringBuffer query, int order) { // vrací pozici vrcholu (v dotazu) s daným pořadím při průchodu do hloubky
            //debug("\nHledám " + order + ". vrchol v dotazu " + query);
            if (order <= 0) return -1; // root je vrchol č.1, další jsou vyšší
            int position = 0; // začnu procházet dotaz od začátku
            int actual_order = 0; // budu si počítat vrcholy
            int target_position = -1; // zatím nenalezena správná pozice
            int length = query.length();
            while (position < length) { // dokud nejsem na konci
                if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // jsem na začátku vrcholu
                    if (position == 0 || (position > 0 && query.charAt(position-1) != '|')) { // není-li to jen alternativa
                        // skutečně jsem na začátku dalšího vrcholu
                        actual_order++;
                        if (actual_order == order) { // nalezen správný vrchol
                            target_position = position + 1; // umístím se za tu závorku
                            if (target_position == length) { // pokud jsem ale na konci dotazu, nějaká chyba
                                target_position = -1;
                            }
                            break; // nalezeno, vyskakuji z while cyklu
                        }
                    }

                }
                position++;
            }
            return target_position;
        }

        private int queryGetDepthOrderOfNode(StringBuffer query, int target_position) { // zjistí se, na kolikátém vrcholu v dotazu je kurzor (pořadí při průchodu do hloubky)
            int order = 0; // počáteční hodnota
            int position = 0; // začnu procházet zleva
            boolean inside_node = false; // signalizuje, zda jsem zrovna v hranicích vrcholu nebo ne
            int length = query.length();
            if (target_position <= 0 || target_position >= length) return 0; // tam není žádný vrchol

            while (position < target_position) { // dokud nejsem na správné pozici
                if (position == target_position) break; // nalezeno
                if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // jsem na začátku vrcholu
                    inside_node = true; // to se může uplatnit až po průběhu tohoto cyklu, čili na správném místě position+1
                    if (position == 0 || (position > 0 && query.charAt(position-1) != '|')) { // není-li to jen alternativa
                        // skutečně jsem na začátku dalšího vrcholu
                        order++;
                    }

                }
                else if (query.charAt(position)==']' && noOddBackslashes(query,position)) { // jsem na konci vrcholu
                    if (position+1 >= length || query.charAt(position+1) != '|') { // není-li to jen alternativa
                        // skutečně jsem na konci vrcholu
                        inside_node = false; // opustil jsem vrchol - resp. opustím po následujícím position++
                    }

                }
                position++;
            }

            if (!inside_node) { // pozice nebyla uvnitř vrcholu
                order = 0;
            }
            //debug ("\nKurzor v dotazu je na " + order + ". vrcholu při průchodu do hloubky.");
            return order;
        }

        private int toBeginOfNode(StringBuffer query, int position, int length, boolean or_matters) { // přesunu se na začátek vrcholu (z jeho prostředku)
            // pokud nenajdu začátek, vrátím -1
            // pokud or_matters == true, pak mi vadí být před alternativním vrcholem - hledám nejlevější z nich
            position --; // pro případ, že jsem na konci dotazu nebo těsně před vrcholem; tady se mohu dostat i na -2, ale to je později ošetřeno
            while (position>=0) { // dokud můžu hledat
                if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // nalezeno
                    if (!or_matters || position==0) {
                        //debug("\nZačátek uzlu nalezen na pozici" + position + ".");
                        position++; // mám být za otevírací závorkou
                        break;
                    }
                    else if (!(query.charAt(position-1)=='|' && noOddBackslashes(query,position-1))) { // vadí mi být před alternativou, ale nejsem tam
                        //debug("\nZačátek uzlu nalezen na pozici" + position + ".");
                        position++; // mám být za otevírací závorkou
                        break;
                    }
                }
                else if (query.charAt(position)==']' && noOddBackslashes(query,position)) { // tady končí předchozí uzel
                    if (!or_matters || (position+1 < length && query.charAt(position+1) != '|') || position+1 >= length) {
                        // nevadí mi být před alternativou nebo před ní nejsem nebo jsem na konci dotazu
                        //debug("\nZačátek uzlu nenalezen.);
                        position=-1;
                        break;
                    }
                }
                position--;
            }
            if (position < -1) position = -1; // začátek nenalezen
            return position;
        }

    public void setCoreferencePatterns(DefaultListModel patterns) {
        query_forest_view.setReferencePatterns(patterns);
    }

    /**
     * Changes multiple tree query logical meaning between AND and OR
     * @return
     */
    private void queryAndOr() {
        if (and_or) {
            and_or = false;
        }
        else {
            and_or = true;
        }
        querySetAndOr(and_or);
    }

    /**
     * Returns the query without AND or OR at its beginning
     * @param query
     * @return
     */
    private String getPureQuery(String query) {
        int first_node_pos = query.indexOf('['); // jdu na začátek prvního uzlu
        if (first_node_pos <= 0) { // nenalezen
            return query; // vracím beze změny
        }
        // před prvním uzlem dotazu něco bylo - předpokládám, že AND nebo OR, tak to smažu
        return query.substring(first_node_pos); // smažu vše před prvním uzlem (tedy případné AND nebo OR)
    } // getPureQuery

    /**
     * Returns the AND or OR from the beginning of the query
     * @param query
     * @return
     */
    private String getAndOrFromTextQuery(String query) {
        int first_node_pos = query.indexOf('['); // jdu na začátek prvního uzlu
        if (first_node_pos <= 0) { // nenalezen
            return ""; // vracím prazdny retezec
        }
        // před prvním uzlem dotazu něco bylo - předpokládám, že AND nebo OR, tak to smažu
        return query.substring(0,first_node_pos).trim(); // vezmu vše před prvním uzlem (tedy případné AND nebo OR) včetně případného konce řádku
    } // getAndOrFromTextQuery

    /**
     * Return false if the given string starts with OR; otherwise return true
     * @param and_or_string
     * @return
     */
    private boolean getAndOrFromString(String and_or_string) {
        if (and_or_string.startsWith("OR")) {
            return false;
        }
        return true;
    }

    /**
     * Sets multiple tree query logical meaning to AND (true) or OR (false)
     * @return
     */
    private void querySetAndOr(boolean and_or) {
        button_query_and_or.setText(getButtonAndOrLabel(and_or));
        String query = pane_query.getText(); // vezmu dotaz
        int caret_position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
        String pure_query = getPureQuery(query);
        int shortened_of = query.length() - pure_query.length();
        caret_position-=shortened_of; // pozici kurzoru posunu zpět o smazané znaky (tedy zůstane na místě)

        NGForest forest = new NGForest(jaaa); // vytvořím nový les, ve kterém zparsuji zkrácený dotaz
        forest.setHead(ngt_global_head);
        forest.readForest(pure_query.toCharArray(),0,ngt_global_head.getSize());

        int number_of_trees = forest.getNumberOfTrees();
        if (number_of_trees < 2) { // pro méně než dva stromy logickou spojku nepíšu
            if (shortened_of > 0) { // ale v dotazu byla
                pane_query.setText(pure_query); //
                pane_query.setCaretPosition(caret_position);
            }
            pane_query.requestFocus();
            return;
        }
        if (and_or) {
            query = "AND" + (char)ServerCommunication.EOL + pure_query;
            caret_position += 4;
        }
        else {
            query = "OR" + (char)ServerCommunication.EOL + pure_query;
            caret_position += 3;
        }
        pane_query.setText(query);
        pane_query.setCaretPosition(caret_position);
        pane_query.requestFocus();
    } // querySetAndOr

        private boolean isRelationCharacter(char c) { // vrati true, pokud znak je '=','!','<' nebo '>'
            switch (c) {
                case '=': return true;
                case '!': return true;
                case '<': return true;
                case '>': return true;
                default: return false;
            }
        }

        private int findAttribute(StringBuffer query, int position, int length, String attribute) { // vyskytuje se již toto jméno?
            int attr_len = attribute.length();
            StringBuffer attr_buf = new StringBuffer(attribute);
            int target_position = -1;
            int i;
            boolean found;

            while (position <= length - attr_len - 1) { // dokud je v dotazu dost místa pro atribut a rovnítko
                if (noOddBackslashes(query,position)) { // vyloučím nějakou rošťárnu
                    if (query.charAt(position) == ']') { // konec vrcholu, konec nadějí
                        target_position = -1;
                        break;
                    }
                    found = true; // budu optimista
                    for (i=0; i < attr_len; i++) {
                        if (attr_buf.charAt(i) != query.charAt(position+i)) { // řetězce se liší
                            found = false;
                            break;
                        }
                    }
                    if (found == true) { // nalezl jsem atribut, ještě jestli je za ním relační znaménko (tím vyloučím podřetězec apod.)
                        if (isRelationCharacter(query.charAt(position+attr_len)) && noOddBackslashes(query,position+attr_len)) { // teď už si mohu být úplně jist, že jsem to našel
                            target_position = position + attr_len + 1;
                            if (isRelationCharacter(query.charAt(target_position))) { // jde nejspíš o dvojznakové relační znaménko
                                target_position++;
                            }
                            break;
                        }
                    }
                }
                position++;
            }
            return target_position;
        }

        private int toEndOfNode(StringBuffer query, int position, int length) { // přesunu se na konec vrcholu (z jeho prostředku)
            // pokud nenajdu konec, vrátím length
            while (position<length) { // dokud můžu hledat
                if (query.charAt(position)==']' && noOddBackslashes(query,position)) { // nalezeno
                    break;
                }
                position++;
            }
            return position;
        }

        /**
         * Sets the selected value of attribute to the query; if replace is set to true, it replaces old values; otherwise it adds this value as an alternate value
         */
        private void queryValue(String value, boolean replace) {

            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            int target_position = -1;
            int begin_position;
            int length = query.length();
            //boolean insert = false; // signalizuje, je-li potřeba vložit hodnotu atributu
            boolean before = false; // signalizuje, je_li potřeba vložit oddělovač alternativ před hodnotu

            try { // kvůli nepředvídanému překročení hranic řetězce

                if (query.toString().equalsIgnoreCase("")) { // dotaz je dosud prázdný, bez akce
                    return;
                }

                if (position<1 || position >= length) return; // sem rovněž nelze vkládat hodnota atributu

                target_position = toBeginOfValues(query,position,length); // přesunu se za rovnítko, je-li tam někde

                if (target_position == -1) { // hodnoty není možno umístit
                    return;
                }

                if (replace) { // nahrazení předchozích hodnot novou
                    removeOldValues(query,target_position,length);
                    length = query.length(); // aktualizuji délku
                    before = false;
                }

                else { // přidání alternativní hodnoty - podívám se, jestli tam už není
                    if (alreadySet(query,position,value)) return;
                    target_position=toEndOfValues(query,target_position,length); // teď jsem na konci hodnot
                    if (target_position>0) { // to by měl být v každém případě
                        if ((query.charAt(target_position-1) == '|' || isRelationCharacter(query.charAt(target_position-1)))
                        && noOddBackslashes(query,target_position-1)) { // už je tam oddělovač alternativ nebo rovnítko
                            before = false;
                        }
                        else { // musím oddělovač alternativ vložit
                            before = true;
                        }
                    }
                }

                // nyní mohu hodnotu přidat

                if (before) { // má se vložit oddělovač alternativ
                    query.insert(target_position++,"|");
                }

                //debug("\nVkládám hodnotu atributu.");
                query.insert(target_position, value);
                target_position += value.length();

                pane_query.setText(query.toString()); // nový dotaz zobrazím
                pane_query.setCaretPosition(target_position); // umístím kurzor na novou pozici

            }
            catch (Exception e) {
                debug("\nChyba " + e + " při vkládání hodnoty atributu v tvorbě dotazu (fce queryValue).");
            }
            pane_query.requestFocus();
        }

        /**
         * Removes old values of an actual attribute in the query
         */
        private void removeOldValues(StringBuffer query, int position, int length) {
            int number = 0; // počet znaků ke smazání
            int start_position = position; // uschování pozice
            while (position<length) { // dokud můžu hledat konec hodnot
                if (query.charAt(position)==']' && noOddBackslashes(query,position)) { // nalezeno
                    break;
                }
                if (query.charAt(position)==',' && noOddBackslashes(query,position)) { // nalezeno
                    break;
                }
                number++;
                position++;
            }
            //debug("\nOdstraněno " + number + "znaků z hodnot.");
            query.replace(start_position,start_position+number,""); // smažu vše najednou
            return;
        }

        private boolean alreadySet(StringBuffer query,int position,String value) {
            return false;
        }

        private int toBeginOfValues(StringBuffer query, int position, int length) { // přesunu se na začátek hodnot atributu (z prostředku hodnot)
            // pokud nenajdu začátek, vrátím -1
            boolean search_forward = false; // signalizuje, zda hledat také směrem vpřed

            position --; // pro případ, že jsem na konci dotazu nebo těsně před vrcholem; tady se mohu dostat i na -2, ale to je později ošetřeno
            while (position>=0) { // dokud můžu hledat
                if (isRelationCharacter(query.charAt(position)) && noOddBackslashes(query,position)) { // nalezeno
                    //debug("\nZačátek hodnot nalezen na pozici" + position + ".");
                    position++; // mám být za rovnítkem
                    break;
                }
                else if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // nenalezeno - tady začíná uzel; zkusím hledat opačným směrem
                    //debug("\nJsem na začátku uzlu - zkouším hledat směrem vpřed.);
                    search_forward = true;
                    break;
                }
                else if (query.charAt(position)==',' && noOddBackslashes(query,position)) { // nenalezeno - tady začíná atribut; zkusím hledat opačným směrem
                    //debug("\nJsem na začátku atributu - zkouším hledat směrem vpřed.);
                    search_forward = true;
                    break;
                }
                position--;
            }

            if (search_forward) { // mám se pokusit hledat začátek hodnot (rovnítko) dopředně
                while (position<length) { // dokud můžu hledat
                    if (isRelationCharacter(query.charAt(position)) && noOddBackslashes(query,position)) { // nalezeno
                        //debug("\nZačátek hodnot nalezen na pozici" + position + ".");
                        position++; // mám být za rovnítkem
                        if (isRelationCharacter(query.charAt(position))) { // je to nejspíš relační dvojznak
                            position++; // jdu až za něj
                        }
                        break;
                    }
                    else if (query.charAt(position)==']' && noOddBackslashes(query,position)) { // nenalezeno - tady končí uzel
                        //debug("\nJsem na konci uzlu; místo pro hodnoty nenalezeno.);
                        position = -1;
                        break;
                    }
                    position++;
                }
            }

            if (position <= -1 || position >= length) return -1; // začátek hodnot nenalezen
            return position;
        } // toBeginOfValues

        /**
         * Moves to the end of values of a current attribute in the query; returns a new position
         */
        private int toEndOfValues(StringBuffer query, int position, int length) { // přesunu se na konec hodnot atributu (z jeho prostředku)

            while (position<length) { // dokud můžu hledat
                if (query.charAt(position)==']' && noOddBackslashes(query,position)) { // nalezeno
                    break;
                }
                else if (query.charAt(position)==',' && noOddBackslashes(query,position)) { // nalezeno
                    break;
                }
                position++;
            }

            return position;
        }



        /**
         * Creates a new empty tree in the query (erases the current query)
         */
        private void queryNewForest() {
            pane_query.setText("[]");
            pane_query.setCaretPosition(1);
            pane_query.requestFocus();
        }


    /**
     * Inserts a new tree in the query (next to the actual tree)
     */
    private void queryAddTree() {
        StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
        int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
        int length = query.length();
        int root_position = moveToRoot(query,position,length);
        if (root_position == -1) {
            queryNewForest();
            return;
        }
        int new_tree_position = moveAfterBrother(query, root_position, length, true, true);
        if (root_position == -1) {
            queryNewForest();
            return;
        }
        query.insert(new_tree_position, (char)ServerCommunication.EOL);
        query.insert(new_tree_position + 1, "[]");
        pane_query.setText(query.toString()); // nový dotaz zobrazím
        pane_query.setCaretPosition(new_tree_position+2); // umístím kurzor na novou pozici
        querySetAndOr(and_or);
        pane_query.requestFocus();
        
    } // queryAddTree

        private void querySubtree() {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            int target_position = -1;
            int length = query.length();

            if (justAfterBrother(query,position,length,true,true)) { // jsem-li bezprostředně za vrcholem
                target_position = position;
            }

            if (target_position == -1) { // nejsem na místě vhodném pro vložení podstromu; možná uprostřed vrcholu nebo disjunkce vrcholů?
                target_position = moveAfterBrother(query,position,length,true,true);
            }

            if (justAfterSubtree(query,target_position,length)) { // vrchol už měl podstrom
                target_position = -1;
            }

            if (target_position == -1) return; // nenašlo se vhodné místo pro vložení podstromu

            // a nyni se vlozi spravny retezec:
            query.insert(target_position,"([])");
            pane_query.setText(query.toString()); // nový dotaz zobrazím
            pane_query.setCaretPosition(target_position+2); // umístím kurzor na novou pozici
            pane_query.requestFocus();
        }

        /**
         * Inserts a new father of the actual node in the query
         */
        private void queryFather() {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            int length = query.length();
            int target_position = -1; // sem se bude vkládat vrchol
            int target_position_2 = -1; // sem se bude vkládat konec podstromu
            // tady musi byt nalezeni spravne pozice - nastavi se promenna target_position
            // také se nastaví proměnné after a before a has_predecessor

            try { // kvůli nepředvídanému překročení hranic řetězce

                target_position = toBeginOfNode(query, position, length, true) - 1;

                target_position_2 = moveAfterBrother(query, position, length, true, true);

                if (target_position == -1) return; // nebyla nalezena vhodná pozice; takže se otec nevytvoří
                if (target_position_2 == -1) return; // nebyla nalezena vhodná pozice; takže se otec nevytvoří

                // a nyni se vlozi spravny retezec:
                query.insert(target_position_2,")"); // nejprve dozadu, aby se vložením nejprve dopředu neposunul zbytek řetězce
                query.insert(target_position,"[](");

                pane_query.setText(query.toString()); // nový dotaz zobrazím
                pane_query.setCaretPosition(target_position+1); // umístím kurzor na novou pozici
            }
            catch (Exception e) {
                debug("\nChyba " + e + " při vytváření otce v továrně dotazů (fce queryFather).");
            }
            pane_query.requestFocus();
        } // queryFather

        /**
         * Inserts a brother on the right position around actual node in a query
         */
        private void queryBrother() {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            boolean before = true; //false; // signalizuje, zda vložit separátor před nového bratra
            boolean after = false; // signalizuje, zda vložit separátor za nového bratra
            boolean has_predecessor = true; // signalizuje, zda aktuální vrchol má předchůdce, takže vytvořením bratra zůstane dotaz stromem
            int length = query.length();
            int target_position = -1; // sem se bude vkládat vrchol
            // tady musi byt nalezeni spravne pozice - nastavi se promenna target_position
            // také se nastaví proměnné after a before a has_predecessor

            try { // kvůli nepředvídanému překročení hranic řetězce

                /*if (justBeforeBrother(query,position,length,true)) { // jsem bezprostředně před budoucím bratrem
                    debug ("\nJsem bezprostředně před budoucím bratrem.");
                    after = true;
                    target_position = position;
                }
                else if (justBeforeSeparator(query,position,length,',')) { // jsem bezprostředně před čárkou před bratrem
                    debug ("\nJsem bezprostředně před separátorem před budoucím bratrem.");
                    after = false;
                    target_position = position;
                }

                if (justAfterBrother(query,position,length,true,true)) { // jsem bezprostředně za budoucím bratrem
                    debug ("\nJsem bezprostředně za budoucím bratrem.");
                    before = true;
                    target_position = position;
                }
                else if (justAfterSubtree(query,position,length)) { // jsem bezprostředně za podstromem budoucího bratra
                    debug ("\nJsem bezprostředně za podstromem budoucího bratra.");
                    before = true;
                    target_position = position;
                }
                else if (justAfterSeparator(query,position,length,',')) { // jsem bezprostředně za čárkou za bratrem
                    debug ("\nJsem bezprostředně za separátorem za budoucím bratrem.");
                    before = false;
                    target_position = position;
                }*/

                //if (target_position == -1) { // nejsem na místě vhodném pro vložení bratra; možná uprostřed vrcholu nebo disjunkce vrcholů?
                target_position = moveAfterBrother(query, position, length, true, true);
                //debug ("\nposition = " + position + ", target_position = " + target_position);
                //debug ("\nquery = '" + query + "'");

                //if (target_position != -1) before = true;
                //}

                if (target_position == -1) return; // nebyla nalezena vhodná pozice; takže bratr se nevytvoří

                // teď už je vhodná doba zjistit has_predecessor
                has_predecessor = hasPredecessor(query,position, length); // klidně by tu mohlo být i position
                //debug ("\nhas_predecessor = " + has_predecessor);
                // ale řešit se bude až později - po vložení textu

                // a nyni se vlozi spravny retezec:
                query.insert(target_position,"[]");
                //debug ("\nposition = " + position + ", target_position = " + target_position);
                //debug ("\nquery = '" + query + "'");
                length+=2;
                if (before) { // má-li se vložit separátor před vložený vrchol
                    query.insert(target_position++,","); // a čárku kdyžtak rovnou přeskočím, ať jsem každopádně ihned před závorkou
                    length++;
                }
                if (after) { // má-li se vložit separátor za vložený vrchol
                    query.insert(target_position+2,",");
                    length++;
                }
                //debug ("\nposition = " + position + ", target_position = " + target_position);
                //debug ("\nquery = '" + query + "'");

                if (!has_predecessor) {
                    //createPredecessor(query, target_position+1, length); // teď kdyžtak vložím předchůdce
                    createPredecessor(query, position, length); // teď kdyžtak vložím předchůdce
                    target_position += 3; // někde vlevo přibyly tři znaky: [](
                    //debug ("\nposition = " + position + ", target_position = " + target_position);
                    //debug ("\nquery = '" + query + "'");
                }

                pane_query.setText(query.toString()); // nový dotaz zobrazím
                pane_query.setCaretPosition(target_position+1); // umístím kurzor na novou pozici
            }
            catch (Exception e) {
                debug("\nChyba " + e + " při vytváření bratra v továrně dotazů (fce queryBrother).");
            }
            pane_query.requestFocus();
        } // queryBrother

        private void queryOrNode() {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            boolean before = false; // signalizuje, zda vložit separátor před vloženou alternativu
            boolean after = false; // signalizuje, zda vložit separátor za vloženou alternativu
            int target_position = -1;
            int length = query.length();

            try { // kvůli nepředvídanému překročení hranic řetězce
                if (justAfterBrother(query,position,length,false,false)) { // jsem-li bezprostředně za vrcholem
                    target_position = position;
                    before = true;
                }
                if (justAfterSeparator(query,position,length,'|')) { // jsem-li bezprostředně za vrcholem za separátorem
                    target_position = position;
                    before = false;
                }

                if (justBeforeBrother(query,position,length,false)) { // jsem-li bezprostředně před vrcholem
                    target_position = position;
                    after = true;
                }
                if (justBeforeSeparator(query,position,length,'|')) { // jsem-li bezprostředně před vrcholem před separátorem
                    target_position = position;
                    after = false;
                }

                if (target_position == -1) { // nejsem na místě vhodném pro vložení alternativy; možná uprostřed vrcholu?
                    target_position = moveAfterBrother(query,position,length,false,false); // přemístím se až za disjunkci, protože tu fci už mám
                    before = true;
                    after = false;
                }

                if (target_position == -1) return; // nenašlo se vhodné místo pro vložení alternativy

                // a nyni se vlozi spravny retezec:
                if (before) { // je potřeba vložit oddělovač alternativy
                    query.insert(target_position,"|");
                    target_position++;
                }
                query.insert(target_position,"[]");
                if (after) { // je potřeba vložit oddělovač alternativy
                    query.insert(target_position+2,"|");
                }
                pane_query.setText(query.toString()); // nový dotaz zobrazím
                pane_query.setCaretPosition(target_position+1); // umístím kurzor na novou pozici
                pane_query.requestFocus();
            }
            catch (Exception e) {
                debug("\nChyba " + e + " při vytváření alternativního vrcholu v továrně dotazů (fce queryOrNode).");
            }
        } // queryOrNode

        private void queryRemoveNode() {
            StringBuffer query = new StringBuffer(pane_query.getText()); // vezmu dotaz
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            int start_position = -1; // začátek odstraňovaného vrcholu
            int end_position = -1; // konec odstraňovaného vrcholu
            int length = query.length();

            try { // kvůli nepředvídanému překročení hranic řetězce

                start_position = toBeginOfNode(query, position, length, true) - 1;
                end_position = moveAfterBrother(query, position, length, true, true);
                //debug("\nqueryRemoveNode: 1: dotaz vypadá takto: " + query);
                //debug("\nqueryRemoveNode: vrchol se nachází mezi pozicemi: " + start_position + ", " + end_position);
                if (start_position >= 0 && end_position >= 0) {
                    query.replace(start_position, end_position, "");
                    //debug("\nqueryRemoveNode: 2: dotaz zatím vypadá takto: " + query);
                    if (start_position>0 && end_position<length) { // mohu-li se koukat před a za smazaný vrchol
                        if (query.charAt(start_position-1) == '('
                        && query.charAt(start_position) == ')') { // vrchol neměl bratra a byl začátkem podstromu
                            query.replace(--start_position,start_position+2,"");
                            //debug("\nqueryRemoveNode: odstranuji kulaté závorky.");
                        }
                    }
                    //debug("\nqueryRemoveNode: 3: dotaz zatím vypadá takto: " + query);

                    length = query.length(); // aktualizuji délku
                    position = start_position;

                    // nyní se odstraní případné přebytečné čárky
                    if (position>0) { // mohu se dívat před pozici
                        if (query.charAt(position-1)==',') { // jsem za čárkou
                            //debug("\nqueryRemoveNode: přesouvám se před čárku.");
                            position--; // přesunu se před ni
                        }
                    }
                    //debug("\nqueryRemoveNode: 4: dotaz zatím vypadá takto: " + query);

                    if (position+1 < length) { // mohu se dívat na pozici a o jedno místo doprava
                        if (query.charAt(position)==',' && query.charAt(position+1)==',') { // jsou tam dvě čárky
                            query.replace(position,position+1,""); // jednu z nich odstraním
                            length = query.length();
                            //debug("\nqueryRemoveNode: odstranuji jednu ze dvou čárek.");
                        }
                    }
                    // teď vím, že je tam maximálně jedna čárka - na pozici kurzoru
                    //debug("\nqueryRemoveNode: 5: dotaz zatím vypadá takto: " + query);

                    if (position < length && query.charAt(position)==',') { // skutečně tam ta čárka je
                        if (position == 0 // jsem-li na začátku dotazu
                        || position+1 >= length) { // jsem-li těsně před koncem dozazu
                            //debug("\nqueryRemoveNode: odstranuji zbývající čárku.");
                            query.replace(position,position+1,""); // čárku odstraním
                            length = query.length();
                        }
                        else {
                            if ((query.charAt(position-1)!=']' && query.charAt(position-1)!=')')
                            || (query.charAt(position+1)!='[' && query.charAt(position+1)!='(')) { // nejsem mezi vrcholy
                                //debug("\nqueryRemoveNode: odstranuji zbývající čárku.");
                                query.replace(position,position+1,""); // čárku odstraním
                                length = query.length();
                            }
                        }
                    }
                    if (position>0) position--; // abych byl pokud možno uvnitř uzlu, případně na prvním ze dvou oddělovačů řádků
                    if (position < length && query.charAt(position)==ServerCommunication.EOL) { // na pozici kurzoru je oddělovač řádků
                        if (position+1<length) { // nejsem na konci vstupu (tam se o to postará na konci funkce volání trim
                            if (query.charAt(position+1)==ServerCommunication.EOL) { // na pozici vpravo od kurzoru je také oddělovač řádků
                                query.replace(position,position+1,""); // odstraním jeden z nových řádků
                                if (position>0) position--; // abych byl pokud možno uvnitř uzlu
                            }
                        }
                    }
                }
                //debug("\nqueryRemoveNode: 6: dotaz nakonec vypadá takto: " + query);
                pane_query.setText(query.toString().trim()); // nový dotaz zobrazím
                pane_query.setCaretPosition(position); // umístím kurzor na novou pozici
                querySetAndOr(and_or);
                pane_query.requestFocus();
            }
            catch (Exception e) {
                debug("\nChyba " + e + " při odstraňování vrcholu v továrně dotazů (fce queryRemoveNode).");
            }
        } // queryRemoveNode


        private int findFirstFreeIndex(JComboBox combo, String node_name) { // nalezne první volný číselný suffix pro node_name v combo boxu
            int index=0; // začnu hledat od jedničky
            int size = combo.getModel().getSize();
            int i;
            String member;
            boolean found;
            while (true) { // jednou to určitě najdu
                index ++; // zkouším o jedna větší index
                found = false;
                for (i=0; i<size; i++) { // přes všechny prvky combo boxu
                    member = combo.getModel().getElementAt(i).toString();
                    //debug("\nPanelQuery.findFirstFreeIndex: comparing member of combo box " + member + " with the name " + default_node_name + index);
                    if (member.equals(default_node_name + index)) { // toto jméno je již obsazeno
                        found = true;
                        break; // už nemusím hledat, našel jsem
                    }
                }
                if (found) continue; // zkusím další index
                // defaultní jméno vrcholu s tímto indexem ještě v combo boxu není
                return index;
            }
        } // findFirstFreeIndex

        private String getFirstFreeDefaultNodeName() { // vrátí první neobsazené defaultní jméno pro vrchol
            int index = findFirstFreeIndex(combo_ref_factory_node_name, default_node_name);
            return default_node_name + index;
        }

        private void setTextFieldFirstFreeDefaultNodeName() { // nastaví textové pole pro nabízené jméno vrcholu prvním neobsazeným defaultím jménem
            String next_name = getFirstFreeDefaultNodeName();
            text_query_factory_node_name.setText(next_name);
        }

        private void queryNameNode() {
            int position = pane_query.getCaretPosition(); // vezmu pozici kurzoru
            StringBuffer query = new StringBuffer(pane_query.getText());
            position = toBeginOfNode(query, position, query.length(), true); // zjistím pozici začátku vrcholu, abych nebyl v případných alternativních sadách
            if (position >= 0) {
                pane_query.setCaretPosition(position); // přesunu se na ten začátek vrcholu
            }
            button_select_relation_eq.setSelected(true); // uzel se pojmenovává jedině s rovnítkem
            queryNameSet("_name");
            queryValue(text_query_factory_node_name.getText(), true);
            fillComboRefNodeName(combo_ref_factory_node_name); // aktualizuji seznam použitých jmen vrcholů
            setTextFieldFirstFreeDefaultNodeName();
        } // queryNameNode


        /**
         * Returns a position just after an actual node or a disjunction of nodes; if not successful, returns -1
         */
        private int moveAfterBrother(StringBuffer query, int position, int length, boolean or_matters, boolean subtree_matters) {
            // if subtree_matters == true, pak jde až za případný podstrom
            // pokud procházím podstromem, ignoruji vše; opět hledám až po podstromu
            // if or_matters == true, pak jdu až za případné alternativní vrcholy
            int subtree_deep = 0;
            int target_position = -1;
            //debug("\nJsem ve funkci moveAfterBrother.");
            //debug("\nDotaz = " + query);
            while (position < length) {
                //debug("\nJeden průchod cyklem ve funkci moveAfterBrother; znak na aktuální pozici = " + query.charAt(position));
                if (query.charAt(position) == '(' && noOddBackslashes(query,position)) { // zanořuji se do podstromu
                    subtree_deep++;
                    //debug("\nZanořuji se do podstromu ve funkci moveAfterBrother.");
                }
                if (query.charAt(position) == ')' && noOddBackslashes(query,position)) { // vynořuji se z podstromu
                    subtree_deep--;
                    //debug("\nVynořuji se z podstromu ve funkci moveAfterBrother.");
                    if (subtree_deep < 0) {
                        //debug("\nChyba při vynořování z podstromu - postrádám začátek podstromu.");
                        break; // vyskakuji z cyklu, dobré místo už nenajdu
                    }
                }
                if (subtree_deep == 0) { // vše v pořádku, nejsem v podstromu
                    if ((query.charAt(position) == ']' || query.charAt(position) == ')')
                    && noOddBackslashes(query,position)) { // kandidát na konec vrcholu
                        if (justAfterBrother(query,position+1,length,or_matters,subtree_matters)) { // jsem bezprostředně za vrcholem
                            //debug ("\nPřesunul jsem ukazatel bezprostředně za vrchol.");
                            target_position = position+1;
                            break; // místo vyhovuje, vyskakuji z cyklu
                        }
                        else if (justAfterSubtree(query,position+1,length)) { // jsem bezprostředně za podstromem
                            //debug ("\nPřesunul jsem ukazatel bezprostředně za podstrom.");
                            target_position = position+1;
                            break; // místo vyhovuje, vyskakuji z cyklu
                        }
                    }
                }
                position++; // hledám dál
            }
            if (target_position < -1) target_position = -1;
            return target_position;
        }

        /**
         * Returns a position just before the first brother of the actual node; if not successful, returns -1
        */
        private int moveBeforeFirstBrother(StringBuffer query, int position) {
            // pokud nenajdu začátek, vrátím -1
            position --; // pro případ, že jsem na konci dotazu nebo těsně před vrcholem; tady se mohu dostat i na -2, ale to je později ošetřeno
            while (position>=0) { // dokud můžu hledat
                if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // nalezen začátek vrcholu
                    if (position==0) {
                        //debug("\nZačátek prvního bratra nalezen na pozici " + position + ".");
                        break;
                    }
                    else if (!((query.charAt(position-1)=='|' || query.charAt(position-1)==',') && noOddBackslashes(query,position-1))) { // vadí mi být za alternativou nebo bratrem ale nejsem tam
                        //debug("\nZačátek prvního bratra nalezen na pozici" + position + ".");
                        break;
                    }
                }
                position--; // hledám dále vlevo
            }
            if (position < -1) position = -1; // začátek nenalezen
            return position;
        }

    /**
     * Returns a position just before the first brother of the actual node; if not successful, returns -1
    */
    private int moveToRoot(StringBuffer query, int position, int length) {
        // pokud nenajdu začátek, vrátím -1
        if (length < 1) {
            return -1;
        }
        position --; // pro případ, že jsem na konci dotazu nebo těsně před vrcholem
        if (position <= 0) {
            position = 1;
        }
        while (position>=0) { // dokud můžu hledat
            if (query.charAt(position)=='[' && noOddBackslashes(query,position)) { // nalezen začátek vrcholu
                if (position==0) {
                    //debug("\nKořen nalezen na pozici " + position + ".");
                    position++; // chci být uvnitř vrcholu
                    break;
                }
                else if (!((query.charAt(position-1)=='|' || query.charAt(position-1)==',' || query.charAt(position-1)=='(') && noOddBackslashes(query,position-1))) { // vadí mi být za alternativou nebo bratrem ale nejsem tam
                    //debug("\nKořen nalezen na pozici" + position + ".");
                    position++;
                    break;
                }
            }
            position--; // hledám dále vlevo
        }
        if (position < -1) position = -1; // začátek nenalezen
        return position;
    }

        /**
         * returns true, if number of continuous sequence of '|' before the given position is not odd. Otherwise returns false
         */
        private boolean noOddBackslashes(StringBuffer query, int position) {
            int number = 0;
            position--; // dívám se tedy doleva od udané pozice
            while (position>0 && query.charAt(position) == '\\' ) { // dokud trvá souvislá sekvence escape znaků
                position--;
                number++;
            }
            if (evenNumber(number)) { // sudé číslo
                return true;
            }
            else { // liché číslo
                return false;
            }
        }

        private boolean evenNumber(int number) {
            if (number/2 * 2 == number) { // sudé číslo
                //debug ("\nKontrola sudosti čísla " + number + " - sudé.");
                return true;
            }
            else { // liché číslo
                //debug ("\nKontrola sudosti čísla " + number + " - liché.");
                return false;
            }
        }

        private boolean justBeforeBrother(StringBuffer query, int position, int length, boolean or_matters) {
            if (position<length) { // mohu se koukat, co je za kurzorem
                if (query.charAt(position) == '[' && noOddBackslashes(query,position)) { // jsem bezprostředně před vrcholem
                    if (position > 0) { // mohu-li se dívat doleva
                        if (!or_matters || // pokud mi nevadí být uprostřed disjunkce vrcholů nebo
                        (query.charAt(position-1) != '|' && noOddBackslashes(query,position-1))) { // nejsem uprostřed disjunkce vrcholů
                            return true;
                        }
                    }
                    else { // jsem na začátku dotazu
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean justAfterBrother(StringBuffer query, int position, int length, boolean or_matters, boolean subtree_matters) {
            // if subtree_matches == true, vadí mi být před podstromem
            // if or_matters == true, vadí mi být před alternativním vrcholem
            if (position>0) { // mohu se koukat, co je před kurzorem
                if (query.charAt(position-1) == ']'
                && noOddBackslashes(query,position-1)) { // jsem bezprostředně za vrcholem nebo jeho podstromem
                    if (position < length) { // mohu-li se dívat doprava
                        if (or_matters && query.charAt(position) == '|') { // pokud mi vadí být uprostřed disjunkce vrcholů a jsem tam
                            return false;
                        }
                        else if (subtree_matters && query.charAt(position) == '(') { // jsem před podstromem a vadí mi to
                            return false;
                        }
                        return true;
                    }
                    else { // jsem na konci dotazu
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean justAfterSubtree(StringBuffer query, int position, int length) {
            if (position>0) { // mohu se koukat, co je před kurzorem
                if (query.charAt(position-1) == ')'
                && noOddBackslashes(query,position-1)) { // jsem bezprostředně za podstromem
                    return true;
                }
            }
            return false;
        }

        private boolean justBeforeSeparator(StringBuffer query, int position, int length, char separator) {
            if (position<length) { // mohu se koukat, co je za kurzorem
                if (query.charAt(position) == separator && noOddBackslashes(query,position)) { // jsem bezprostředně před čárkou
                    if (justBeforeBrother(query,position+1,length,false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean justAfterSeparator(StringBuffer query, int position, int length, char separator) {
            if (position>0) { // mohu se koukat, co je před kurzorem
                if (query.charAt(position-1) == separator && noOddBackslashes(query,position-1)) { // jsem bezprostředně za čárkou
                    if (justAfterBrother(query,position-1,length,false,false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Checks whether a node on a specified position has a predecessor
         */
        private boolean hasPredecessor(StringBuffer query,int position, int length) {
            // pokusím se najít otevírací kulatou závorku vlevo před otevírací hranatou; když tam nebude, usoudím, že předchůdce neexistuje
            // ovšem když po cestě narazím na kulatou závorku zavírací, musím nejprve najít otevírací k ní a potom pokračovat
            boolean found;
            int subtree_deep = 1; // když se mi tohle nalezením levé závorky vynuluje, je to ta správná závorka
            position--; // dívám se tedy doleva od udané pozice
            found = false;
            if (position >= length - 1) {
                position--; // pro případ, že jsem na konci dotazu
                if (query.charAt(position+1) == ')' && noOddBackslashes(query,position+1)) { // musím ale ošetřit možnost, že jsem přeskočil uzavírací závorku
                    subtree_deep++;
                }
            }
            while (position>0) { // dokud je šance něco najít
                if (query.charAt(position) == ')' && noOddBackslashes(query,position)) { // zanořuji se do nějakého předchozího podstromu
                    //debug("\nZanořuji se do podstromu ve fci hasPredecessor.");
                    subtree_deep++;
                }
                else if (query.charAt(position) == '(' && noOddBackslashes(query,position)) { // vynořuji se z nějakého předchozího podstromu
                    //debug("\nVynořuji se z podstromu ve fci hasPredecessor.");
                    subtree_deep--;
                }

                if  (query.charAt(position+1)=='['
                && query.charAt(position) == '('
                && query.charAt(position-1) == ']'
                && noOddBackslashes(query,position-1)
                ) { // pokud jsem našel posloupnost znaků '](['
                    if (subtree_deep == 0) {
                        found = true;
                        break;
                    }
                }
                //debug("\nposition = " + position);
                position--;
            }
            if (found == false) { // předchůdce nenalezen
                //debug ("\npředchůdce nenalezen");
                return false;
            }
            else { // předchůdce tam nejspíš je
                //debug ("\npředchůdce nalezen");
                return true;
            }
        }

        /**
         * Creates a predecessor of the root of the query (used if creating a brother of the root of the query)
         */
        private void createPredecessor(StringBuffer query,int position, int length) {
            //debug ("\nVytvářím předchůdce jediného vrcholu.");
            int start_of_node = moveBeforeFirstBrother(query, position);
            //debug ("\nPrvní bratr nalezen na pozici " + start_of_node);
            int end_of_brothers = -1;
            while ((position = moveAfterBrother(query, position, length, true, true)) >= 0) { // chci za vsechny bratry
                //debug ("\nPoslední bratr nalezen na pozici " + position);
                end_of_brothers = position;
            }
            if (end_of_brothers >= 0 && end_of_brothers <= length) {
                query.insert(end_of_brothers,")");
                query.insert(start_of_node,"[](");
                //debug ("\nPředchůdce vytvořen.");
            }
            return;
        }

/* ###zk začátek aplikačního kódu */
    public int print(Graphics g, PageFormat pf, int page_index) throws PrinterException {

        int distance_text_tree = 8;

        if (page_index > 0) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D)g; // pro větší možnosti, např. scale

        String font_family = query_print.getPrintProperties().getFontFamily();
        int font_size = query_print.getPrintProperties().getFontSize();
        Font font_text, font_strom;
        font_text = new Font(font_family, Font.PLAIN, font_size);

        g2.setFont(font_text);
        g2.setPaint(Color.black);
        g2.translate((int)im_x,(int)im_y); // posunu levý horní roh do tisknutelné oblasti

        String kodovany;
        kodovany = new String(text_query_printed);

        // nyní vytisknu dotaz tak, aby byl rozdělen do více řádků, když je příliš dlouhý

        FontRenderContext frc = g2.getFontRenderContext();
        AttributedString attribString = new AttributedString(kodovany);
        //attribString.addAttribute(TextAttribute.FOREGROUND, Color.blue, 0, kodovany.length()); // Start and end indexes.
        Font font = new Font(query_print.getPrintProperties().getFontFamily(), Font.PLAIN, font_size);
        attribString.addAttribute(TextAttribute.FONT, font, 0, kodovany.length());
        AttributedCharacterIterator styledText = attribString.getIterator();
        // let styledText be an AttributedCharacterIterator containing at least
        // one character
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);
        float wrappingWidth = (float)im_width * 0.97f; // hausnumero, aby mi to nelezlo přes okraj vpravo
        float x,y;
        x = 0f;
        y = 0f;
        while (measurer.getPosition() < kodovany.length()) {
            TextLayout layout = measurer.nextLayout(wrappingWidth);
            y += (layout.getAscent());
            float dx = layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance());
            layout.draw(g2, x + dx, y);
            y += layout.getDescent() + layout.getLeading();
        }

        g2.translate(0,(int)y + distance_text_tree); // posunu levý horní roh pod vytisknutý textový dotaz

        // a nyní vytisknu strom

        font_strom = new Font(font_family, Font.PLAIN, font_size);
        g2.setFont(font_strom);

        // teď spočítám velikost zbývající tisknutelné plochy
        double t_x = im_width;
        double t_y = im_height - (double)y - (double)distance_text_tree;

        query_print.print(g2,t_x,t_y); // vytisknutí vlastního stromu; vejít se musí do t_x * t_y

        return PAGE_EXISTS;
    } // print


    public void printQuery(boolean dialog) { // vytiskne strom dotazu na tiskárnu; vyvolá dialogové okno, pokud dialog=true
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
        //debug ("\n   center = " + print_properties.getCenter());
        //debug ("\n   keep ratio = " + print_properties.getKeepRatio());
        //debug ("\n   background = " + print_properties.getBackground());

        //debug("\nJsem po dialogu pro tisk a print = " + print);
        if (print) { // pokud to uživatel nezrušil
            query_printed = query_forest_view.getForest().getClone();
            query_print.setForest(query_printed);
            if (query_print.getPrintProperties().getBlackWhite()) { // má se tisknout černobíle
                //debug("\nSetting black and white for printing trees.");
                query_print.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_BLACK_AND_WHITE);
            }
            else { // má se tisknout barevně
                //debug("\nSetting colors for printing trees.");
                query_print.getTreeProperties().setColorScheme(NGTreeProperties.COLOR_SCHEME_DEFAULT);
            }
            im_height = query_print.getPrintProperties().getPageFormat().getImageableHeight();
            im_width = query_print.getPrintProperties().getPageFormat().getImageableWidth();
            im_x = query_print.getPrintProperties().getPageFormat().getImageableX();
            im_y = query_print.getPrintProperties().getPageFormat().getImageableY();

            printer_job.setPrintable(this, query_print.getPrintProperties().getPageFormat());
            if (dialog) print = printer_job.printDialog();

            if (print) { // pokud stále nezrušeno uživatelem, tak už opravdu budu tisknout
                if (query_print.getPrintProperties().getBackground()) { // vytiskne se v novém vlákně
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
    } // printQuery

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

} // class PanelQuery

