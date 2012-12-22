package cz.cuni.mff.mirovsky.trees;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.Comparator;

import cz.cuni.mff.mirovsky.ShowMessagesAble;
import cz.cuni.mff.mirovsky.CharCode;


/**
 * A base class for forest viewing (displaying on a screen, printing on a printer)
 */
public class NGForestView extends JComponent {

    /**
     * a forest to be viewed
     */
    protected NGForest forest; // stromy ke kreslení

    /**
     * viewing properties of forests
     */
    protected NGTreeProperties tree_properties; // společné vlastnosti kreslených stromů

    /**
     * reference patterns
     */
    protected DefaultListModel reference_patterns; // jaké a jak malovat reference

    /**
     * the width of the forest (in pixels)
     */
    protected int forest_width;  // rozmery lesa

    /**
     * the height of the forest (in pixels)
     */
    protected int forest_height;

    private boolean emphasize_chosen; // má se zobrazovat zvýrazněně vybraný vrchol?

    /**
     * controls whether hidden nodes should be displayed
     */
    protected boolean show_hidden_nodes; // zobrazovat vrcholy skrývané na tektogramatické rovině?
    private ShowMessagesAble mess; // objekt pro výpis hlášek

    private int nodes_ordering; // razeni uzlu ve stromech

    // pomocne promenne:

    private int[] xradka;  // kresleni stromu
    private int[] level_height; // kresleni stromu - výška úrovně stromu při zhuštěném tisku
    private int xlast; // pro výpočet rozměrů

    /**
     * an auxiliary variable for printing strings
     */
    protected String kodovany; // pomocná proměnná pro tisk řetězců v příslušném kódování

    /**
     * labels of nodes will be shifted this amount to the left in order to have their first character just below the node
     */
    protected int posunuti_textu_doleva = 4; // popisky vrcholu budou posunuty doleva o tolik pixlu
    // aby byl stred prvniho pismene priblizne pod stredem vrcholu

    private Rectangle matching_nodes_rectangle; // obdelnik pro matchujici vrcholy

    // následující proměnné slouží pro kreslení stromu
    private int prumer; // průměr kolečka vrcholu
    private int prumer_multiple; // průměr kolečka vrcholu u multisadových vrcholů
    private int odsaz_multiple; // pomocná pro kreslení multisadového kolečka

    /**
     * the size of the font used for labels of nodes
     */
    protected int font_size; // velikost fontu
    private int okraj_east = 15; // velikost okraje vedle zobrazeneho stromu vpravo
    private int odsazeni; // minimální horizontální mezera mezi vrcholy
    private int space_above_tree; // mezera nad stromem
    private int space_above_text; // odsazení popisku od vrcholu
    private int space_below_text; // mezera pod popiskem
    private int vertical_space_between_texts; // mezera mezi řádky popisků
    private int space_above_divider; // mezera nad oddělovačem sad
    private int space_below_divider; // mezera pod oddělovačem sad
    private int sets_divider_width = 30; // šířka oddělovače sad
    private boolean show_spaces; // mají se zobrazovat prázdné hodnoty atributů?
    private boolean show_names; // mají se zobrazovat jména atributů?
    private boolean show_multiple_sets;
    private boolean show_multiple_values;
    private boolean show_lemma_variants; // odřezávat varianty i komentáře lemmat?
    private boolean show_lemma_comments; // odřezávat komentáře lemmat?

    /**
     * horizontal distance between trees in the forest
     */
    protected int horizontal_space; // horizontální mezera mezi stromy

    // pro kreslení koreferencí:
    final static private float dash1[] = {7.0f};
    final static private float dot_and_dash1[] = {7.0f,5.0f,2.0f,5.0f};
    final static private float dots1[] = {2.0f,5.0f};
    final static private BasicStroke stroke_dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 7.0f, dash1, 0.0f);
    final static private BasicStroke stroke_line = new BasicStroke();
    final static private BasicStroke stroke_dot_and_dashed = new BasicStroke(1.0f, // Width
                               BasicStroke.CAP_BUTT,    // End cap
                               BasicStroke.JOIN_MITER,    // Join style
                               7.0f,                     // Miter limit
                               dot_and_dash1,            // Dash pattern
                               0.0f);                     // Dash phase
    final static private BasicStroke stroke_dotted = new BasicStroke(1.0f, // Width
                               BasicStroke.CAP_BUTT,    // End cap
                               BasicStroke.JOIN_MITER,    // Join style
                               7.0f,                     // Miter limit
                               dots1,                    // Dash pattern
                               0.0f);                     // Dash phase

    final static private int SHAPE_AND_MASK_STROKE = 7; // první tři bity hodnoty shape určují typ čáry
    final static private int SHAPE_AND_MASK_START_ARROW = 8; // čtvrtý bit určuje, zda se má malovat šipkové zakončení u startovního uzlu
    final static private int SHAPE_AND_MASK_END_ARROW = 16; // pátý bit určuje, zda se má malovat šipkové zakončení u koncového uzlu
    final static private int SHAPE_AND_MASK_CURVE_TYPE = 224; // šestý až osmý bit určují tvar křivky

    final static private int STROKE_TYPE_LINE = 0; // typ čáry - jednoduchá čára
    final static private int STROKE_TYPE_DASHED = 1; // typ čáry - čárkovaná čára
    final static private int STROKE_TYPE_DOT_AND_DASHED = 2; // typ čáry - čerchovaná čára
    final static private int STROKE_TYPE_DOTTED = 3; // typ čáry - tečkovaná čára
    // (čtyři hodnoty jsou rezervovány pro různé další typy čar)

    final static private int CURVE_TYPE_STRAIGHT = 32; // tvar křivky - rovná
    final static private int CURVE_TYPE_QUAD_DOWN_START = 64; // tvar křivky - kvadratická křivka s kontrolním bodem dole blíže ke startovnímu uzlu
    final static private int CURVE_TYPE_QUAD_UP_START = 128; // tvar křivky - kvadratická křivka s kontrolním bodem nahoře blíže ke startovnímu uzlu
    // (pět hodnot je rezervováno pro různé další tvary)

    /**
     * Creates an object needed for viewing trees (on a screen or a printer)
     * @param mess an object capable of displaying messages
     */
    public NGForestView(ShowMessagesAble mess) { // konstruktor

        super(); // zavolam konstruktor rodicovskeho objektu, proc ne, ze?
        this.mess = mess;
        forest = null;

        tree_properties = new NGTreeProperties();
        reference_patterns = new DefaultListModel();
        emphasize_chosen = true; // implicitně se má graficky zvýrazňovat vybraný vrchol
        show_hidden_nodes = false; // implicitně se vrcholy, obvykle skrývané na tektogr. rovině, nebudou zobrazovat

        horizontal_space = 20;
    } // konstruktor

    /**
     * Displays a debugging message.
     * @param message a message to be displayed
     */
    protected void debug(String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
        if (mess != null) {
            mess.debug(message);
        }
        else {
            System.out.print(message);
        }
    }

    /**
     * Displays an informative message.
     * @param message a message to be displayed
     */
    protected void inform(String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
        if (mess != null) {
            mess.inform(message);
        }
        else {
            System.out.print(message);
        }
    }

    /**
     * Returns the width of the forest (in pixels)
     * @return the width of the forest (in pixels)
     */
    public int getForestWidth() {
        return forest_width;
    }

    /**
     * Returns the height of the forest (in pixels)
     * @return the height of the forest (in pixels)
     */
    public int getForestHeight() {
        return forest_height;
    }

    /**
     * Sets the horizontal space between trees in the forest.
     * @param space the horizontal space in pixels
     */
    public void setHorizontalSpace(int space) {
        horizontal_space = space;
    }

    /**
     * Returns a rectangle that surrounds all matching nodes in the forest.
     * @return the rectangle
     */
    public Rectangle getMatchingNodesRectangle() {
        // vrati drive spocitany (v paintComponent, resp. ve vypocti_nakresleni) obdelnik obsahujici matchujici vrcholy
        return matching_nodes_rectangle;
    } // getMatchingNodesRectangle


    /**
     * Sets some global variables according to the actual tree propertis
     */
    protected void setGlobalProperties() { // nastaví globální proměnné podle aktuálních tree_properties
        nodes_ordering = tree_properties.getDirection();
        prumer = tree_properties.getDiameter();
        prumer_multiple = tree_properties.getDiameterMulti();
        odsaz_multiple = prumer_multiple / 2;
        font_size = tree_properties.getFontSize();
        odsazeni = tree_properties.getOdsazeni();
        space_above_tree = tree_properties.getSpaceAboveTree(); // mezera nad stromem
        space_above_text = tree_properties.getSpaceAboveText();
        space_below_text = tree_properties.getSpaceBelowText();
        vertical_space_between_texts = tree_properties.getVerticalSpaceBetweenTexts();
        space_above_divider = tree_properties.getSpaceAboveDivider();
        space_below_divider = tree_properties.getSpaceBelowDivider();

        show_spaces = tree_properties.getShowNullValues(); // mají se zobrazovat prázdné hodnoty atributů?
        show_names = tree_properties.getShowAttrNames(); // mají se zobrazovat jména atributů?

        show_multiple_sets = tree_properties.getShowMultipleSets();
        show_multiple_values = tree_properties.getShowMultipleValues();
        show_lemma_variants = tree_properties.getShowLemmaVariants(); // odřezávat varianty i komentáře lemmat?
        show_lemma_comments = tree_properties.getShowLemmaComments(); // odřezávat komentáře lemmat?
        show_hidden_nodes = tree_properties.getShowHiddenNodes(); // zobrazovat skrývané uzly?
    }

    /**
     * Sets the reference patterns.
     * @param patterns a list of reference patterns
     */
    public void setReferencePatterns(DefaultListModel patterns) {
        reference_patterns = patterns;
    }

    /**
     * Returns a list of reference patterns
     * @return a list of reference patterns
     */
    public DefaultListModel getReferencePatterns() {
        return reference_patterns;
    }

    private void printForest(Graphics g) {
        if (forest == null) return;

        // nastavím globální proměnné podle aktuálních properties
        setGlobalProperties();

        Graphics2D g2 = (Graphics2D)g;
        for (NGTree tree : forest.getTrees()) { // přes všechny stromy
            if (tree.getRoot() != null) {
                drawTree(tree, tree.getRoot(), null, tree.getRoot().getX(), tree.getRoot().getY(), g2); // nakreslím strom
            }
        }
    } // printForest

    /**
     * Emphasizes the selected node.
     * @param g Graphics2D object to draw to
     * @param properties tree properties
     */
    public void printChosen(Graphics2D g, NGTreeProperties properties) {
        int x_start, y_start; // pomocné - posun počátku souřadnic

        if (forest == null) return;
        TNode chosen_node = forest.getChosenNode();

        if (chosen_node == null) return;
        if (chosen_node.skryvany == true && show_hidden_nodes == false) { // skryty vrchol, ktery neni kresleny
          //debug("\nA hidden node has been chosen but hidden nodes are not set to be displayed.");
          return;
        }
        x_start = forest.getChosenTree().getXStart(); // posunutí počátku souřadnic pro tento strom
        y_start = forest.getChosenTree().getYStart();
        Color fullcircle;
        Color circle;
        Color circle_multiple;
        int prumer_vybrany = properties.getDiameterChosen();
        int prumer_multiple;
        int odsaz = prumer_vybrany / 2; // posunutí souřadnic při kreslení kolečka vrcholu
        int odsaz_multiple;
        //debug("\nKreslím vybraný vrchol na pozicích " + tree.getChosen().x + ", " + tree.getChosen().y);
        if (chosen_node.skryvany == true) { // budu kreslit skrývaný vybraný vrchol
            fullcircle = properties.getColorFullcircleHiddenChosen();
            circle = properties.getColorCircleHiddenChosen();
            circle_multiple = properties.getColorCircleMultipleHiddenChosen();
        }
        else { // budu kreslit neskrývaný vrchol
            fullcircle = properties.getColorFullcircleChosen();
            circle = properties.getColorCircleChosen();
            circle_multiple = properties.getColorCircleMultipleChosen();
        }
        g.setColor(fullcircle);
        g.fillOval(x_start + chosen_node.getX() - odsaz, y_start + chosen_node.getY() - odsaz, prumer_vybrany, prumer_vybrany);
        g.setColor(circle);
        g.drawOval(x_start + chosen_node.getX() - odsaz, y_start + chosen_node.getY() - odsaz, prumer_vybrany, prumer_vybrany);
        if (properties.getShowMultipleMarkChosen()) { // má-li se vybraný vrchol s více sadami atributů zvýraznit
            if (chosen_node.values != null) { // jsou-li u vrcholu nějaké atributy
                if (chosen_node.values.Next != null) { // jsou-li tam aspoň dvě sady atributů
                    g.setColor(circle_multiple);
                    prumer_multiple = properties.getDiameterMultiChosen();
                    odsaz_multiple = prumer_multiple / 2;
                    g.drawOval(x_start + chosen_node.getX() - odsaz_multiple, y_start + chosen_node.getY() - odsaz_multiple, prumer_multiple, prumer_multiple);
                }
            }
        }
    } // printChosen

    /**
     * PDT-related function that truncates a lemma. It cuts a suffix of the lemma.
     * @param orig_lemma the original lemma
     * @param trunc_type type of truncation:
     * <br>&nbsp; &nbsp; 0 - do not truncate
     * <br>&nbsp; &nbsp; 1 - truncate comments
     * <br>&nbsp; &nbsp; 2 - truncate variants and comments
     * @return the truncated lemma
     */
    protected String truncLemma(String orig_lemma, int trunc_type) { // odstraní z lemmatu vysvětlivky (a případně i varianty), pokud tam jsou
        // trunc_type == 0 ... neodstraňovat nic
        // trunc_type == 1 ... odstranit jen komentáře
        // trunc_type == 2 ... odstranit varianty i komentáře
        //System.out.print("\norig lemma = " + orig_lemma);
        if (trunc_type == 0) return orig_lemma;

        String ret_lemma;
        int index1 = orig_lemma.indexOf('_'); // podtrzitko (vysvetlivky)
        int index2 = orig_lemma.indexOf('-'); // pomlcka (varianty)
        int index3 = orig_lemma.indexOf('`'); // obraceny apostrof (vysvetlivky cisel)

        if (trunc_type == 1) index2 = -1; // jako by tam pomlčka nebyla, když se nemá odstranit varianta

        if (index1 <= 0) {
            if (index2 <=0)
                if (index3 <= 0) ret_lemma = orig_lemma; // není tam žádný z těch znaků nebo jen jeden z nich, a to na první pozici
                else ret_lemma = orig_lemma.substring(0,index3);
            else { // index1 <= 0, index2 > 0
                if (index3 <= 0) ret_lemma = orig_lemma.substring(0,index2);
                else { // index1 <= 0, index2,3 > 0
                    if (index2 < index3) ret_lemma = orig_lemma.substring(0,index2);
                    else ret_lemma = orig_lemma.substring(0,index3);
                }
            }
        }
        else { // index1 > 0
            if (index2 <= 0) {
                if (index3 <= 0) ret_lemma = orig_lemma.substring(0,index1);
                else { // index1 > 0, index2 <= 0, index3 > 0
                    if (index1 < index3) ret_lemma = orig_lemma.substring(0,index1);
                    else ret_lemma = orig_lemma.substring(0,index3);
                }
            }
            else { // index1 > 0, index2 > 0
                if (index3 <= 0) {
                    if (index2 < index1) ret_lemma = orig_lemma.substring(0,index2);
                    else ret_lemma = orig_lemma.substring(0,index1);
                }
                else { // index1,2,3 > 0
                    if (index1 < index2) {
                        if (index3 < index1) ret_lemma = orig_lemma.substring(0,index3);
                        else ret_lemma = orig_lemma.substring(0,index1);
                    }
                    else {
                        if (index3 < index2) ret_lemma = orig_lemma.substring(0,index3);
                        else ret_lemma = orig_lemma.substring(0,index2);
                    }
                }
            }
        }
        //System.out.print("\nret lemma = " + ret_lemma);
        return ret_lemma;
    }

    private void vypocti_vrchol(NGTree tree, TNode n, Graphics g) {
        // n - pocitany vrchol
        int sx; // sirka vrcholu
        int sx_now;
        int sx_left; // sirka vrcholu az po znamenko relace (v případě zobrazování jmen)
        int sx_left_now;
        int sx_right; // sirka vrcholu od znamenka relace (vcetne znamenka v pripade zobrazovani jmen)
        int sx_right_now;
        int sy; // výška vrcholu
        int p;
        int mezery; // pomocná

        int h; // hloubka vrcholu
        h = tree.getDepth(n); // ziskam vzdalenost vrcholu od korene stromu (koren ma hloubku 0)

        String name_of_attr;
        String description;

        TValue values; // jedna sada atributů
        TAHLine value; // jedna hodnota atributů

        if (n == null)
            return;

        if (n == forest.getChosenNode()) {
            if (tree_properties.getShowMultipleSetsChosen()) {
                show_multiple_sets = true;
            }
            if (tree_properties.getShowMultipleValuesChosen()) {
                show_multiple_values = true;
            }
        }

        //width_equal_sign = g.getFontMetrics().stringWidth("="); // zjistím šířku rovnítka
        // y - vertikální umístění vrcholu

        mezery = vertical_space_between_texts * (forest.getVybraneAtributy().getSize() - 1);
        if (mezery<0) mezery=0; // pokud nebyl ani jeden vybrany atribut

        n.setY(h * (space_above_text + font_size*forest.getVybraneAtributy().getSize() + mezery + space_below_text) + space_above_tree); // standardní neúsporné zobrazení stromu

        // sx - vypočítám šířku atributů u tohoto vrcholu - je to šířka nejširšího atributu
        // sy - vypočítám také výšku atributů (v závislosti na show_spaces)
        sx = 0; sy = 0;
        sx_left = 0; sx_right = 0;

        values = n.values; // začnu první sadou atributů

        while (values != null) { // přes všechny sady atributů

            for (int j = 0; j < forest.getVybraneAtributy().getSize(); j++) { // přes všechny vybrané atributy
                name_of_attr = (String)forest.getVybraneAtributy().getElementAt(j);
                p = forest.getHead().getIndexOfAttribute(name_of_attr); // vezmu index dalšího vybraného atributu

                if (p == -1)
                    continue;
                if (values.AHTable[p] == null)
                    continue;

                value = values.AHTable[p];

                description = getDescription(g, name_of_attr, value);

                if (!show_spaces) {
                    if (description.length() == 0) // jestli je hodnota atributu prazdna
                        continue;
                }

                if (sy > 0) { // tento atribut není první
                    sy += vertical_space_between_texts; // přičtu mezeru mezi popisky jednoho vrcholu
                }

                sy += font_size; // tento atribut přispěje do výšky vrcholu

                sx_now = g.getFontMetrics().stringWidth(description);
                //debug("\nŠířka nápisu '" + description + "' je " + pomx);

                if (sx_now > sx) // případná aktualizace šířky vrcholu
                    sx = sx_now;

                if (!show_spaces) {
                  sx_left_now = getDescriptionXShift(g,name_of_attr);
                  if (sx_left_now > sx_left) {
                    sx_left = sx_left_now;
                  }
                  sx_right_now = sx_now - sx_left_now;
                  if (sx_right_now > sx_right) {
                    sx_right = sx_right_now;
                  }
                  sx = sx_left + sx_right;
                }
            } // for

            if (!show_multiple_sets) break; // pokud se má zobrazovat jen jedna sada atributů

            values = values.Next;
            if (values != null) { // je-li tam opravdu další sada atributů
                sy += space_above_divider + 1 + space_below_divider;
            }
        } // while

        if (sy > 0) { // alespoň jeden text u vrcholu
            sy += space_above_text + space_below_text;
        }
        else { // žádný text u vrcholu
            sy = space_below_text; // aspoň minimální odsazení další úrovně stromu
        }

        if (sy > level_height[h+1]) { // tento vrchol zvýšil výšku úrovně h
            level_height[h+1] = sy;
        }

        // x - horizontální umístění vrcholu
        if (xlast > xradka[h]) { // jestliže je v předchozích vrstvách něco víc vpravo než v této vrstvě
            n.setX(xlast + odsazeni);
        }
        else {
            n.setX(xradka[h] + odsazeni);
        }

        int odsaz = prumer/2; // polomer kolecka bezneho vrcholu
        int x_min = n.getX() - odsaz; // ta 8 je hausnumero !!!
        int y_min = n.getY() - odsaz; // stejne tak i tato !!!
        int rect_width = sx + prumer;
        int rect_height = sy - space_below_text + prumer;
        n.setRectangle(new Rectangle(x_min, y_min, rect_width, rect_height)); // nastavím rozměry obdélníku zabraného vrcholem a popisky

        // pokud je vrchol matchující s dotazem, přidám jeho místo do obdélníka zabíraného vrcholy matchujícími s dotazem
        if (n.matching_node == true) { // je to matchujici vrchol
            if (matching_nodes_rectangle == null) { // je to prvni vrchol do tohoto obdelniku
                //debug("\nZakládám obdélník " + n.getRectangle());
                matching_nodes_rectangle = new Rectangle(n.getRectangle());
            }
            else {
                matching_nodes_rectangle.add(n.getRectangle());
                //debug("\nPřidávám obdélník " + n.getRectangle());
            }
        }

        if (show_names) { // posunutí značky vrcholu vpravo, aby rovnítka byla pod ní
            n.setX(n.getX() + sx_left);
        }

        if (show_hidden_nodes || n.skryvany!=true) { // pokud se tento vrchol má zobrazit
            xlast = n.getX();
            xradka[h] = n.getX() + sx - sx_left;
        }

        // nyní upravím celkovou výšku a šířku stromu, pokud jsem tímto vrcholem překročil dosavadní hodnoty
        //if (n.y + vyska_d + vyska_o + vyska_f*vybrane_atributy.getSize() + vyska_p > tree_height)
        //tree_height = n.y + vyska_d + vyska_o + vyska_f*vybrane_atributy.getSize() + vyska_p;


        //if (level_height[h+1] > tree_height)
        //	tree_height = level_height[h+1];
        // výšku stromu nyní spočítám pro standardní neúsporné zobrazení
        if (n.getY() + space_above_text + font_size*forest.getVybraneAtributy().getSize() + mezery > tree.getHeight())
            tree.setHeight(n.getY() + space_above_text + font_size*forest.getVybraneAtributy().getSize() + mezery);

        if (n.getX() + sx - sx_left > tree.getWidth()) {
            tree.setWidth(n.getX() + sx - sx_left);
        }

    } // vypocti_vrchol


    private void vypocti_vrchol_oprav_y(NGTree tree, TNode n, int depth) { // rekurzivně projdu strom a nastavím y-ové umístění podle pole level_height[]
        if (n==null) return;
        n.setY(level_height[depth]);
        if (level_height[depth+1] > tree.getHeight()) tree.setHeight(level_height[depth+1]);

        vypocti_vrchol_oprav_y(tree, n.brother, depth);
        vypocti_vrchol_oprav_y(tree, n.first_son, depth + 1);
    }


    private int getDescriptionXShift(Graphics g, String name_of_attribute) {
        // vrátí posun popisu atributu doleva, aby rovnítko bylo pod vrcholem, mají-li se ovšem zobrazovat jména atributů
        int x_shift;
        if (show_names) { // má-li se zobrazit jméno atributu
            x_shift = g.getFontMetrics().stringWidth(name_of_attribute);
        }
        else {
            x_shift = 0;
        }
        return x_shift;
    }


    private String getDescription(Graphics g, String name_of_attribute, TAHLine value) {
        // vrátí text jednoho atributu vrcholu

        int trunc_lemma; // co se má z lemmatu před zobrazením odříznout
        StringBuffer value_of_attribute;
        StringBuffer description = new StringBuffer("");
        String relation_mark;

        if (show_names) { // má-li se zobrazit jméno atributu
            description.append(name_of_attribute);
            switch(value.relation) {
                case TAHLine.RELATION_EQ:
                    relation_mark = "=";
                    break;
                case TAHLine.RELATION_NEQ:
                    relation_mark = "!=";
                    break;
                case TAHLine.RELATION_GT:
                    relation_mark = ">";
                    break;
                case TAHLine.RELATION_LT:
                    relation_mark = "<";
                    break;
                case TAHLine.RELATION_GTEQ:
                    relation_mark = ">=";
                    break;
                case TAHLine.RELATION_LTEQ:
                    relation_mark = "<=";
                    break;
                case TAHLine.RELATION_REGEXP:
                    relation_mark = "~=";
                    break;
                default:
                    relation_mark = "=";
            }
            description.append(relation_mark);
        }

        if (name_of_attribute.equals("m/lemma")) { // jedná-li se o lemma
            if (!show_lemma_variants) { // nemají-li se zobrazovat varianty ani komentáře
                trunc_lemma = 2; // odseknout varianty i komentáře
                value_of_attribute = new StringBuffer(truncLemma(value.Value, trunc_lemma));
            }
            else {
                if (!show_lemma_comments) { // nemají-li se zobrazovat komentáře
                    trunc_lemma = 1; // odseknout jen komentáře
                    value_of_attribute = new StringBuffer(truncLemma(value.Value, trunc_lemma));
                }
                else {
                    trunc_lemma = 0; // zobrazit celé lemma
                    value_of_attribute = new StringBuffer(value.Value);
                }
            }
        }
        else {
            trunc_lemma = 0;
            value_of_attribute = new StringBuffer(value.Value);
        }

        if (show_multiple_values) { // mají-li se zobrazit všechny alternativní hodnoty atributu
            value = value.Next;
            while (value != null) { // přes všechny hodnoty atributu
                if (trunc_lemma > 0) {
                    value_of_attribute.append('|');
                    value_of_attribute.append(truncLemma(value.Value, trunc_lemma));
                }
                else {
                    value_of_attribute.append('|');
                    value_of_attribute.append(value.Value);
                }
                value = value.Next;
            }
        }

        description.append(value_of_attribute);

        if (value_of_attribute.length()==0) {
            description.setLength(0); // nebudu vypisovat jméno atributu bez hodnoty
        }

        return description.toString();
    } // getDescription


    /**
     * Draws one attribute as a part of a label of a node.
     * @param g a graphic object to draw to
     * @param description a description to be drawn
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    protected void drawDescription(Graphics g, String description, int x, int y) {
        // napíše obsah jednoho atributu k vrcholu

        if (tree_properties.getUseAsciiInTree()) kodovany = CharCode.isolatin2ToAscii(description);
        else kodovany = CharCode.isolatin2ToUnicode(description);
        g.drawString(kodovany, x, y);
    } // drawDescription

    /**
     * Draws one tree of the forest.
     * @param tree the tree to be drawn
     * @param n the root of the tree (or subtree - it is a recursive function)
     * @param parent the father of node n
     * @param otec_x the x-coordinate of the father
     * @param otec_y the y-coordinate of the father
     * @param g2 a graphic object to draw to
     */
    protected void drawTree(NGTree tree, TNode n, TNode parent, int otec_x, int otec_y, Graphics2D g2) {
        // tree je právě kreslený strom
        // n je právě kreslený vrchol (v rekurzi)
        // parent je otec právě kresleného vrcholu (v rekurzi, root má za otce null)
        // otec_x a otec_y jsou souřadnice otce právě kresleného vrcholu (v rekurzi)
        // g je cíl kreslení

        int number_of_printed_values;
        int number_of_printed_sets;
        Color color_fullcircle;
        Color color_circle;
        Color color_circle_multiple;
        Color color_edge;

        String description; // jedna hodnota popisu vrcholu (jeden atribut)
        String name_of_attribute;
        int description_x_shift; // x-ový posun popisu vrcholu vůči vrcholu
        int x, y; // pomocné
        int x_start, y_start; // pomocné - posun počátku souřadnic

        TValue values; // jedna sada atributů
        TAHLine value; // jedna hodnota atributů

        int p;

        if (n == null)
            return;

        boolean transitive_true = false; // signalizuje, zda se bude kreslit plně tranzitivní hrana
        boolean transitive_exclusive = false; // signalizuje, zda se bude kreslit exkluzivně tranzitivní hrana
        // řídí se to podle toho, co první se u vrcholu najde
        if (tree_properties.getHighlightTransitiveEdges()) { // má-li se zvýrazňovat tranzitivní hrana
            int transitive_index = forest.getHead().getIndexOfAttribute(NGTreeHead.META_ATTR_TRANSITIVE);
            if (transitive_index >= 0) { // v hlavičce je meta-atribut _transitive
                int number_of_sets = n.getNumberOfSets();
                for (int i=0; i<number_of_sets; i++) {
                    String transitive_string = n.getValue(i,transitive_index,0); // _transitive může mít jen jednu hodnotu
                    if (transitive_string != null) { // hotdnota je definována
                        if (transitive_string.equals(NGTreeHead.META_ATTR_TRANSITIVE_TRUE)) {
                            transitive_true=true;
                            break;
                        }
                        else if (transitive_string.equals(NGTreeHead.META_ATTR_TRANSITIVE_EXCLUSIVE)) {
                            transitive_exclusive=true;
                            break;
                        }
                    }
                }
            }
        }
        // teď už mám zjištěno, zda se bude kreslit zvýrazněně tranzitivní hrana a jaká

        boolean optional = false; // signalizuje, zda se bude kreslit optional vrchol
        if (tree_properties.getHighlightOptionalNodes()) { // má-li se zvýrazňovat optional vrchol
            int optional_index = forest.getHead().getIndexOfAttribute(NGTreeHead.META_ATTR_OPTIONAL);
            if (optional_index >= 0) { // v hlavičce je meta-atribut _optional
                int number_of_sets = n.getNumberOfSets();
                for (int i=0; i<number_of_sets; i++) {
                    String optional_string = n.getValue(i,optional_index,0); // _optional může mít jen jednu hodnotu
                    if (optional_string != null) { // hotdnota je definována
                        if (optional_string.equals(NGTreeHead.META_ATTR_OPTIONAL_TRUE)) {
                            optional=true;
                            break;
                        }
                        try {
                            int opt = Integer.parseInt(optional_string);
                            if (opt == -1 || opt > 0) {
                                optional = true;
                                break;
                            }
                        }
                        catch (NumberFormatException e) {
                            optional = false; // neco jsem sem napsat musel, prazdny prikaz neumim    
                        }
                    }
                }
            }
        }
        // teď už mám zjištěno, zda se bude kreslit zvýrazněně optional vrchol

        boolean zero_occurrence = false; // signalizuje, zda se bude kreslit vrchol s _#occurrences=0
        if (tree_properties.getHighlightZeroOccurrenceNodes()) { // má-li se zvýrazňovat zero-occurrence vrchol
            int occurrence_index = forest.getHead().getIndexOfAttribute(NGTreeHead.META_ATTR_OCCURRENCES);
            if (occurrence_index >= 0) { // v hlavičce je meta-atribut _#occurrences
                int number_of_sets = n.getNumberOfSets();
                for (int i=0; i<number_of_sets; i++) {
                    String occurrence_string = n.getValue(i,occurrence_index,0); // vezmu jen prvni hodnotu
                    if (occurrence_string != null) { // hotdnota je definována
                        if (occurrence_string.equals("0")) {
                            zero_occurrence=true;
                            break;
                        }
                    }
                }
            }
        }
        // teď už mám zjištěno, zda se bude kreslit zvýrazněně vrchol s _#occurrences=0

        int odsaz = prumer / 2; // posunutí souřadnic při kreslení kolečka vrcholu

        x_start = tree.getXStart(); // posunutí počátku souřadnic pro tento strom
        y_start = tree.getYStart();

        if (n == forest.getChosenNode()) {
            if (tree_properties.getShowMultipleSetsChosen()) {
                show_multiple_sets = true;
            }
            if (tree_properties.getShowMultipleValuesChosen()) {
                show_multiple_values = true;
            }
        }

        if (show_hidden_nodes || n.skryvany!=true) {
            if (n.skryvany) { // budu kreslit hranu ke skrývanému vrcholu
                if (n.matching_edge) { // patřící do podstromu matchujícího s dotazem
                    color_edge = tree_properties.getColorEdgeHiddenMatching();
                }
                else { // nepatřící do podstromu matchujícího s dotazem
                    color_edge = tree_properties.getColorEdgeHidden();
                }
            }
            else {
                if (n.matching_edge) { // patřící do podstromu matchujícího s dotazem
                    color_edge = tree_properties.getColorEdgeMatching();
                }
                else { // nepatřící do podstromu matchujícího s dotazem
                    color_edge = tree_properties.getColorEdge();
                }
            }
            g2.setColor(color_edge);
            Stroke prev_stroke = g2.getStroke(); // uchování dosavadního stylu čáry
            if (transitive_true) {
                g2.setStroke(tree_properties.getStrokeTransitiveEdgeTrue()); // stroke_dotted); // nastavení stylu čáry
            }
            else if (transitive_exclusive) {
                g2.setStroke(tree_properties.getStrokeTransitiveEdgeExclusive()); // stroke_dashed); // nastavení stylu čáry
            }
            if (parent != null) {
                g2.drawLine(x_start + n.getX(), y_start + n.getY(), x_start + otec_x, y_start + otec_y);
            }
            g2.setStroke(prev_stroke); // vrácení stylu čáry do původní podoby
            drawTree(tree, n.first_son, n, n.getX(), n.getY(), g2);
        }

        drawTree(tree, n.brother, parent, otec_x, otec_y, g2);

        // když jsou nakresleny hrany, může se kreslit vrchol (teprve teď, aby zakryl nepřesnosti hran)
        if (show_hidden_nodes || !n.skryvany) {
            if (n.skryvany) { // budu kreslit skrývaný vrchol
                if (n.matching_node) { // matchující s dotazem
                    color_fullcircle = tree_properties.getColorFullcircleHiddenMatching();
                    color_circle = tree_properties.getColorCircleHiddenMatching();
                    color_circle_multiple = tree_properties.getColorCircleMultipleHiddenMatching();
                }
                else { // nematchující s dotazem
                    color_fullcircle = tree_properties.getColorFullcircleHidden();
                    color_circle = tree_properties.getColorCircleHidden();
                    color_circle_multiple = tree_properties.getColorCircleMultipleHidden();
                }
            }
            else { // budu kreslit neskrývaný vrchol
                if (n.matching_node) { // matchující s dotazem
                    color_fullcircle = tree_properties.getColorFullcircleMatching();
                    color_circle = tree_properties.getColorCircleMatching();
                    color_circle_multiple = tree_properties.getColorCircleMultipleMatching();
                }
                else { // nematchující s dotazem
                    color_fullcircle = tree_properties.getColorFullcircle();
                    color_circle = tree_properties.getColorCircle();
                    color_circle_multiple = tree_properties.getColorCircleMultiple();
                }
            }
            g2.setColor(color_fullcircle);
            g2.fillOval(x_start + n.getX() - odsaz, y_start + n.getY() - odsaz, prumer, prumer);
            g2.setColor(color_circle);
            g2.drawOval(x_start + n.getX() - odsaz, y_start + n.getY() - odsaz, prumer, prumer);

            if (optional) { // jde o optional vrchol
                g2.setColor(tree_properties.getColorOptionalNode());
                g2.drawArc(x_start + n.getX() - (int)Math.round(prumer*1.5), y_start + n.getY() - (int)Math.round(prumer*1.5), prumer*3, prumer*3, -30, 60);
                g2.drawArc(x_start + n.getX() - (int)Math.round(prumer*1.5), y_start + n.getY() - (int)Math.round(prumer*1.5), prumer*3, prumer*3, 150, 60);
            }

            // nyní zjistím, zda je potřeba zvýraznit vrchol z důvodu více sad atributů (a případně ho zvýrazním)
            if (tree_properties.getShowMultipleMark()) { // má-li se to zvýrazňovat
                if (n.values != null) { // je-li u vrcholu aspoň jedna sada
                    if (n.values.Next != null) { // a je -li u vrcholu další sada
                        g2.setColor(color_circle_multiple);
                        g2.drawOval(x_start + n.getX() - odsaz_multiple, y_start + n.getY() - odsaz_multiple, prumer_multiple, prumer_multiple);
                    }
                }
            }

            if (zero_occurrence) { // jde o vrchol s _#occurrences=0
                g2.setColor(tree_properties.getColorZeroOccurrenceNode());
                g2.drawLine(x_start + n.getX() - prumer, y_start + n.getY() - prumer, x_start + n.getX() + prumer, y_start + n.getY() + prumer);
                g2.drawLine(x_start + n.getX() + prumer, y_start + n.getY() - prumer, x_start + n.getX() - prumer, y_start + n.getY() + prumer);
            }

            // a popis vrcholu
            number_of_printed_values = 0;
            number_of_printed_sets = 0;

            values = n.values;
            while (values != null) { // než projdu všechny sady atributů
                g2.setColor(tree_properties.getColorWriting());

                for (int j = 0; j < forest.getVybraneAtributy().getSize(); j++) {

                    number_of_printed_values++;

                    name_of_attribute = forest.getVybraneAtributy().getElementAt(j).toString();
                    p = forest.getHead().getIndexOfAttribute(name_of_attribute);

                    if (p == -1) {
                        if (!show_spaces) {
                            number_of_printed_values--;
                        }
                        continue;
                    }

                    value = values.AHTable[p]; // ukážu si na hodnotu atributu

                    if (value == null) {
                        if (!show_spaces) {
                            number_of_printed_values--;
                        }
                        continue;
                    }

                    description = getDescription(g2, name_of_attribute, value);
                    description_x_shift = getDescriptionXShift(g2, name_of_attribute);

                    if (description.length()==0) {
                        if (!show_spaces) {
                            number_of_printed_values--;
                        }
                    }

                    else { // popisek je neprázdný
                        x = n.getX() - description_x_shift - posunuti_textu_doleva;
                        y = n.getY() + space_above_text + number_of_printed_values * font_size
                        + (number_of_printed_values-1) * vertical_space_between_texts
                        + number_of_printed_sets * (1 + space_above_divider + space_below_divider);

                        drawDescription(g2, description, x_start + x, y_start + y);
                    }
                } // for

                if (!show_multiple_sets) break; // pokud nemám zobrazovat víc sad atributů

                if (values.Next != null) { // je-li tam opravdu další sada atributů
                    // nakreslím čáru dělící sady atributů
                    g2.setColor(tree_properties.getColorMultipleSetsDivider());
                    int x_position = n.getX() - posunuti_textu_doleva;
                    if (show_names) x_position = n.getX() - sets_divider_width / 2;
                    int y_position = n.getY() + space_above_text + number_of_printed_values * font_size
                    + number_of_printed_values * vertical_space_between_texts
                    + number_of_printed_sets * (1 + space_above_divider + space_below_divider)
                    + space_above_divider + 1;

                    g2.drawLine(x_start + x_position, y_start + y_position, x_start + x_position + sets_divider_width, y_start + y_position);
                }
                values = values.Next; // přejdu k další sadě atributů
                number_of_printed_sets ++; // zobrazil jsem celou jednu sadu atributů

            } // while
        }
    } // drawTree

    /**
     * It calculates coordinates of all nodes of all trees in the forest.
     * @param g a graphic object to draw to
     */
    public /*protected*/ void vypocti_nakresleni(Graphics g) {
        setGlobalProperties(); // nastavím globální proměnné podle aktuálních properties
        // nastavení velikosti fontu probíhá zde, protože vypocti_nakresleni je volána i z PanelTrees.treeLoaded
        Font f = g.getFont();
        Font f2 = f.deriveFont((float)font_size);
        g.setFont(f2);
        // musí se tu přepočítat nakreslení stromů, které mají svůj flag změny nastavený, a nastavit x_start a y_start u všech stromů
        int x_start = 0; // posunutí počátku souřadnic jednotlivých stromů
        int y_start = 0;
        int forest_width_so_far = 0;
        int forest_height_so_far = 0;
        for (NGTree tree : forest.getTrees()) { // přes jednotlivé stromy
            tree.setXStart(x_start); // nastavím posunutí počátku souřadnic pro tento strom
            tree.setYStart(y_start);
            if (tree.getFlagTreeChanged()) { // tento strom se změnil
                vypocti_nakresleni(tree, tree.getRoot(), g);
                tree.setFlagTreeChanged(false); // nakreslení je vypočítáno, takže není potřeba počítat je příště znovu
            }

            x_start += tree.getWidth();
            forest_width_so_far = x_start;
            x_start += horizontal_space;
            if (forest_height_so_far < tree.getHeight()) { // tento strom je vyšší než všechny předchozí
                forest_height_so_far = tree.getHeight();
            }
            // y_start zůstává stále 0, neboť stromy kreslím vedle sebe
        } // konec cyklu přes stromy
        forest_width = forest_width_so_far;
        forest_height = forest_height_so_far;
        this.setPreferredSize(new Dimension(forest_width + okraj_east, forest_height)); // aby dobře fungoval ScrollPane
        this.revalidate(); // aby fungovaly dobře lištičky, pokud jsem v JScrollPane
        //int w = tree_width + okraj_east;
        //debug("\n\nNGTreeView.vypocti_nakresleni: Setting the size of the component to " + w + " x " + tree_height);
    }

    private void vypocti_nakresleni(NGTree tree, TNode n, Graphics g) {
        int number_of_nodes = tree.getNumberOfNodes();
        xlast = 0;
        xradka = new int[number_of_nodes];
        level_height = new int[number_of_nodes + 1]; // +1 abych se nemusel bát krajních případů

        tree.setHeight(0);
        tree.setWidth(0);

        // současně budu počítat obdélník zabraný matchujícími vrcholy:
        matching_nodes_rectangle = null; // zapominam cokoliv predchozi

        for (int i = 0; i < number_of_nodes; i++) {
            xradka[i] = 0; // vynuluji pravý okraj (šířku) každého potenciálního řádku stromu
            level_height[i] = 0; // vynuluji dolní okraj (výšku od kořene) každé potenciální řádky stromu
        }

        level_height[number_of_nodes] = 0; // toto pole je o jedna delší

        // nyní vytvořím pole všech vrcholů stromu setříděné podle TNode.poradi_N (poradi_N může obecně obsahovat reálná čísla)
        TNode[] nodes_array=tree.getNodesArray(); // vezmu pole všech vrcholů (deep-first)
        TNode[] sorted_nodes_array = insertSort(nodes_array, getNodesInTreeOrderComparator()); // seřadím ho zatřiďováním podle proměnné poradi_N
        // zatřiďování funguje i v dotazech, kde atribut deepord či ord většinou není nastaven; strom se prochází deep-first
        // a uzly dostávají vzestupné hodnoty deepord/ord; podle toho se pak řadí;
        // pokud vrchol obsahuje referenci v atributu deepord/ord, bere se v potaz; porovnání se všemi uzly pak dává výsledek 0 (rovný), kromě
        // uzlu, na který opravdu odkazuje reference; s ním dá Comparator správný výsledek -1,(0), či 1.
        // obyčejné řazení pole, které je v Javě, nešlo použít, protože právě výsledek porovnání 0 u neporovnatelných uzlů to kazil

        if (nodes_ordering == NGTreeProperties.DIRECTION_LEFT_RIGHT) { // levo-prave razeni uzlu ve stromu
            for (int j = 0; j < number_of_nodes; j++) { // vypočítám umístění každého vrcholu
                vypocti_vrchol(tree, sorted_nodes_array[j], g); // vypočtu souřadnice vrcholu a místo, které zabírá svými popisky
            }
        }
        else { // pravo-leve razeni uzlu ve stromu (arabstina)
            for (int j = number_of_nodes-1; j >=0; j--) { // vypočítám umístění každého vrcholu
                vypocti_vrchol(tree, sorted_nodes_array[j], g); // vypočtu souřadnice vrcholu a místo, které zabírá svými popisky
            }
        }

        if (!tree_properties.getShowNullValues()
        || tree_properties.getShowMultipleSets()
        || tree_properties.getShowMultipleSetsChosen()) { // nemají-li prázdná místa zabírat zbytečně místo
            // nebo se mají zobrazovat i alternativní sady (což způsobuje, že nelze napřed odhadnout výšku vrcholů z počtu vybraných atributů)
            //debug("\nPřepočítávám y-ové umístění vrcholů.");
            // musím znovu projít strom a opravit u vrcholů y-ové umístění
            upravVysky(tree); // upravím výšky v level_height na absolutní hodnoty
            tree.setHeight(0); // znovu musím vyhodnotit výšku stromu - bude patrně nižší
            vypocti_vrchol_oprav_y(tree,n,0); // rekurzivně projdu strom a nastavím y-ové umístění podle pole level_height[]
            // též vypočtu správnou výšku stromu
        }

        tree.setHeight(tree.getHeight() + tree_properties.getSpaceBelowTree()); // přidám odsazení stromu od spodního okraje
        //tree.setWidth();

    } // vypocti_nakresleni

    /**
     * Sorts the array of TNodes using insert sort and a given comparator
     * @param unsorted the unsorted array of nodes
     * @param comparator a comparator of nodes
     * @return a sorted array of nodes
     */
    private TNode[] insertSort(TNode[] unsorted, Comparator comparator) {
        int size = unsorted.length;
        boolean inserted;
        TNode[] sorted = new TNode[size];
        for (int i=0; i<size; i++) { // vezmu každý doposud nezařazený prvek
            TNode node1 = unsorted[i];
            inserted = false;
            for (int j=0; j<i; j++) { // a porovnám ho s každým dosud zařazeným prvkem (pokud nenarazím na prvek, který má být větší)
                TNode node2 = sorted[j];
                if (comparator.compare(node1,node2) == -1) { // prvek má být menší než testovaný už zařazený prvek
                    shiftArray(sorted,j,i-1); // odsunu další již dříve zařazené prvky
                    sorted[j]=node1; // na uvolněné místo dám nový prvek
                    inserted = true;
                    break;
                }
                // přidávaný prvek není menší než už dříve zařazený prvek; zkusím další
            }
            if (! inserted) { // pokud jsem s novým prvkem došel až za všechny již zařazené (čili nezařadil jsem ho někam mezi ně)
                sorted[i] = node1;
            }
        }
        return sorted;
    }

    private void shiftArray(Object[] array, int start, int end) {
        for (int i=end; i>=start; i--) {
            array[i+1] = array[i];
        }  
    }

    private void upravVysky(NGTree tree) { // upravím výšky v poli level_height z relativních na absolutní
        level_height[0] = tree_properties.getSpaceAboveTree(); // odsadím strom od horního okraje
        for (int i=0; i<tree.getNumberOfNodes(); i++) { // projdu celé pole výšek
            level_height[i+1] += level_height[i];
        }
    }

    /**
     * Signals whether the whole forest has changed and coordinates of all its trees and their nodes must be calculated.
     * @param flag value true signals that the coordinates need to be calculated
     */
    public void setFlagWholeForestChanged(boolean flag) { // nastaví flag u lesa, že je potřeba přepočítat nakreslení
        if (forest != null) {
            forest.setFlagWholeForestChanged(flag);
        }
    }
    
    /**
     * Says whether the forest has changed and coordinates of (some of) its trees and their nodes must be calculated.
     * @return true iff the coordinates need to be calculated
     */
    public boolean getFlagForestChanged() { // vrátí flag u stromu, zda je potřeba přepočítat nakreslení
        if (forest != null) {
            return forest.getFlagForestChanged();
        }
        else return false;
    }

    /**
     * Sets a forest to be viewed.
     * @param forest a forest to be viewed
     */
    public void setForest(NGForest forest) {
        this.forest = forest;
    }

    /**
     * Gets the forest to be viewed.
     * @return the forest to be viewed
     */
    public NGForest getForest() {
        return forest;
    }

    /**
     * Sets a single tree to be viewed.
     * @param tree a tree to be veiwed
     */
    public void setTree(NGTree tree) {
        forest = new NGForest(mess);
        forest.addTree(tree);
    }

    /**
     * Adds a tree to be viewed.
     * @param tree a tree to be added at the end of the trees of the forest
     */
    public void addTree(NGTree tree) {
        if (forest == null) {
            forest = new NGForest(mess);
        }
        forest.addTree(tree);
    }

    /**
     * Sets properties of the tree.
     * @param new_properties properties of the tree
     */
    public void setTreeProperties(NGTreeProperties new_properties) {
        tree_properties = new_properties;
    }

    /**
     * Returns the properties of the tree.
     * @return the properties of the tree
     */
    public NGTreeProperties getTreeProperties() {
        return tree_properties;
    }

    /**
     * Sets whether or not the hidden nodes should be shown
     * @param show true iff they should be shown
     */
    public void setShowHiddenNodes(boolean show) {
      tree_properties.setShowHiddenNodes(show);
        show_hidden_nodes = show;
    }

    /**
     * Returns whether the hidden nodes are set to be shown
     * @return true iff they are set to be shown
     */
    public boolean getShowHiddenNodes() {
        return show_hidden_nodes;
    }

    /**
     * Sets whether the selected node should be emphasized
     * @param emphasize true iff it should be emphasized
     */
    public void setEmphasizeChosenNode(boolean emphasize) {
        emphasize_chosen = emphasize;
    }

    /**
     * Returns whether the selected node is set to be emphasized
     * @return true iff it is set to be emphasized
     */
    public boolean getEmphasizeChosenNode() {
        return emphasize_chosen;
    }

    /**
     * Changes the state of (non-)displaying the i-th reference
     * @param i the number of the reference, counted from zero
     * @return the new state; it may be unchanged if the reference patterns forbids changing
     */
    public boolean changeReferenceStatus(int i) { // překlopí stav zobrazení i-té reference
        // vrací výsledný stav; může být stejný s původním, pokud koreferenční schéma neumožňuje změnu (pak by ovšem nebylo v menu)
        if (i>=0 && i< reference_patterns.size()) { // nejsem mimo rozsah v seznamu koreferenčních schémat
            ReferencePattern pattern = (ReferencePattern) reference_patterns.elementAt(i);
            return pattern.changeDisplay();
        }
        else { // koreferenční schéma s takovým pořadím neexistuje
            //debug("\nNGForestView.changeReferenceStatus(" + i + "): There is no such coreference pattern!");
            return false; // dummy
        }
    }

    /**
     * Paints the forest.
     * @param g a graphic object to paint to
     */
    public void paintComponent(Graphics g) { // vykreslení komponenty, tj. panelu se stromy

        //System.out.print("\nNGForestView.paintComponent: Entering the function.");
        //debug("\nNGForestView.paintComponent: Entering the function.");
        //setBackground (tree_properties.getColorBackground());
        //super.paintComponent (g); // paint background - to přestalo fungovat

        if (forest == null) { // žádný strom k nakreslení
            drawEmptyForest(g);
            return;
        }

        // nastavení velikosti fontu proběhne tady i ve vypocti_nakresleni, pokud se vypocti_nakresleni zavola (vola se totiz i samostatne z PanelTrees.treeLoaded)
        Font f = g.getFont();
        Font f2 = f.deriveFont((float)font_size);
        g.setFont(f2);

        //int vyska_f = g.getFontMetrics().getHeight(); // výška fontu
        //tree_properties.setFontSize(font_size);

        if (forest.getFlagForestChanged()) { // je potřeba přepočítat nakreslení některých stromů
            //debug("\n  NGForestView.paintComponent: Přepočítávám nakreslení stromů.");
            vypocti_nakresleni(g);
        }

        //debug ("\n  NGForestView.paintComponent: Šířka lesa je " + forest_width + ", výška je " + forest_height + ", nastavuji tedy rozměry komponenty se stromy a volám this.revalidate; následně komponentu vyplním barvou pozadí a nakreslím stromy.");

        this.setPreferredSize(new Dimension(forest_width + okraj_east, forest_height)); // aby dobře fungoval ScrollPane

        this.revalidate(); // pro případ, že jsem v JScrollPane, chci, aby se upravily lištičky

        g.setColor(tree_properties.getColorBackground());
        g.fillRect(0,0,super.getWidth(),super.getHeight()); // setBackground a super.paintComponent nefungovalo

        Graphics2D g2 = (Graphics2D)g;

        printReferences(g2); // koreference se namalují první, aby byly ostatními věcmi překryty

        printForest(g); // namalují se stromy

        if (emphasize_chosen) printChosen(g2, tree_properties); // nakonec se namaluje vybraný vrchol

        // následujících šest řádek slouží pro nakreslení obdélníku kolem matchujících vrcholů pro ladění výpočtu toho obdélníku
        //try {
        //    g.drawRect((int)matching_nodes_rectangle.getMinX(),(int)matching_nodes_rectangle.getMinY(),(int)(matching_nodes_rectangle.getMaxX() - matching_nodes_rectangle.getMinX()), (int)(matching_nodes_rectangle.getMaxY() - matching_nodes_rectangle.getMinY()));
        //}
        //catch (Exception e) {
        //    debug("\nThe exception occured: " + e);
        //}
        //debug("\nNGTreeView.paintComponent: Leaving the function.");

    } // paintComponent

    private void drawEmptyForest(Graphics g) { // namaluje prázdnou plochu
        //g.setColor(tree_properties.getColorBackground());
        //g.fillRect(0,0,0,0); // setBackground a super.paintComponent nefungovalo
        this.setPreferredSize(new Dimension(1,1)); // aby dobře fungoval ScrollPane
        this.revalidate();
    } // drawEmptyForest

    /**
     * Marks a node with given order as selected, as well as the tree the node is from.
     * The root of the first tree has order 1, the first tree has order 1, the first node of each subsequent tree
     * has the order like the last node of the previous tree plus 1.
     * @param order depth-first order of the node to be selected
     */
    public void setChosenNodeByDepthOrder(int order) { // vrchol v pořadí 'order' při průchodu do hloubky se označí jako vybraný
        // kořen 1. stromu má pořadí 1, kořen následujícího stromu má pořadí o jedna větší než poslední vrchol předcházejícího stromu
        if (forest != null) {
            forest.setChosenNodeByDepthOrder(order);
            repaint();
        }
    } // setChosenNodeByDepthOrder

    /**
     * it returns a Comparator for comparison nodes by their order in tree from left to right
     */
    private Comparator getNodesInTreeOrderComparator() {
        NodesInTreeOrderComparator comparator = new NodesInTreeOrderComparator(forest.getHead());
        return comparator;
    }

    /**
     * A comparator of the order of nodes in tree from left to right
     */
    private class NodesInTreeOrderComparator implements Comparator {

        private NGTreeHead head; // the head of the tree the nodes to compare come from
        private int name_index; // a position of meta-attribute _name

        public NodesInTreeOrderComparator(NGTreeHead head) {
            this.head = head;
            name_index = head.getIndexOfAttribute(NGTreeHead.META_ATTR_NODE_NAME);
        }

        public int compare(Object o1, Object o2) {
            TNode node1 = (TNode)o1;
            TNode node2 = (TNode)o2;
            double n1 = node1.poradi_N;
            double n2 = node2.poradi_N;
            //System.out.println("\nNGForestView.Comparator.compare: n1=" + n1 + ", n2=" + n2);
            if (n1 >= 0 && n2 >= 0) {
                if (n1 < n2) return -1;
                if (n1 > n2) return 1;
            }
            // pořadí jednoho z uzlů nebylo double (a proto v NGTree.readTree nastaveno na -1); nejspíš se jedná o referenci
            if (n1 < 0) { // jedná se o vrchol node1
                int comp = compareNodesOneWay(node1, node2);
                if (comp != 0) return comp;
            }
            // uzly se nedaly porovnat jedním směrem (podmínka v dotazu nebyla nastavena z uzlu node1 na uzel node2)
            if (n2 < 0) { // jedná se o vrchol node2
                int comp = compareNodesOneWay(node2, node1);
                if (comp != 0) return -comp;
            }
            // uzly se nedaly porovnat, podmínka na jejich pořadí nebyla nastavena v žádném směru
            return 0;
        }

        /**
         * Checks if there is a condition on order of nodes from node1 to node2; If so, it returns the result of the comparison; otherwise returns 0
         * @param node1
         * @param node2
         * @return
         */
        private int compareNodesOneWay(TNode node1, TNode node2) {
            String n2_name = node2.getValue(0,name_index,0); // jméno uzlu je vždy jen v první sadě a může mít jen jednu hodnotu
            if (n2_name != null) {
                if (n2_name.length()>0) { // jméno uzlu node2 je definováno
                   int number_of_sets = node1.getNumberOfSets();
                    TAHLine node1_set_i_values;
                    for (int set_number=0; set_number<number_of_sets; set_number++) { // přes všechny sady atributů - alternativní vrcholy v dotazu
                        node1_set_i_values = node1.getValues(set_number,head.N); // všechny alternativní reference u pořadí uzlů v této sadě vrcholu node1
                        while (node1_set_i_values != null) { // přes všechny alternativní hodnoty
                            String n1_ref_value = node1_set_i_values.Value;
                            int n1_relation = node1_set_i_values.relation;
                            int comp = compareReferenceAndName(n1_ref_value, n1_relation, n2_name);
                            if (comp != 0) { // podmínka na pořadí nalezena
                                return comp;
                            }
                            node1_set_i_values = node1_set_i_values.Next;
                        }
                    }
                }
            }
            return 0; // z uzlu node1 do uzlu node2 podmínka na pořadí nevedla
        }

        /**
         * Checks if there is a condition on order of nodes given in the reference to a node named target_name; If so, it returns the result of the comparison; otherwise returns 0
         * @param reference
         * @param relation
         * @param target_name
         * @return
         */
        private int compareReferenceAndName(String reference, int relation, String target_name) {
            //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: reference=" + reference + ", relation=" + relation + ", target_name=" + target_name);
            String ref_name = getNameFromCoreference(reference);
            if (ref_name == null) {
                //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: 0 (ref_name=null)");
                return 0;
            }
            if (ref_name.compareTo(target_name) != 0) { // reference nevede k uzlu se jménem target_name
                //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: 0");
                return 0;
            }
            // reference vede k uzlu se jménem target_name
            if (relation == TAHLine.RELATION_GT || relation == TAHLine.RELATION_GTEQ) {
                //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: 1");
                return 1; // výchozí uzel reference má mít větší pořadí
            }
            if (relation == TAHLine.RELATION_LT || relation == TAHLine.RELATION_LTEQ) {
                //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: -1");
                return -1; // výchozí uzel reference má mít menší pořadí
            }
            // znaménkem relace se pořadí nepoznalo (je tam rovnítko); zkusím najít plus nebo mínus a podle toho to odhadnout
            if (reference.indexOf('+') >= 0) {
                //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: 1");
                return 1; // odhaduji, že když je tam rovnítko a něco se přičítá, má to být větší
            }
            if (reference.indexOf('-') >= 0) {
                //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: -1");
                return -1; // odhaduji, že když je tam rovnítko a něco se odečítá, má to být menší
            }
            //System.out.println("\nNGForestView.Comparator.compareReferenceAndName: result: 0");
            return 0;
        }

        /**
         * It expects a coreference string as an argument and returns the name of the node from the coreference
         * If the argument is not a coreference, it returns null.
         * @param coreference_string
         * @return
         */
        private String getNameFromCoreference(String coreference_string) {
            String ret = null;
            int coref_start = coreference_string.indexOf(NGTreeHead.NODE_REFERENCE_START);
            //debug("\ncoref_start = " + coref_start);
            int coref_end = coreference_string.indexOf(NGTreeHead.NODE_REFERENCE_END, coref_start);
            //debug("\ncoref_end = " + coref_end);
            if (coref_start >= 0 && coref_end > coref_start) {
                // hodnota obsahuje začátek a konec koreference
                coref_start += NGTreeHead.NODE_REFERENCE_START.length(); // posunu se za začátek koreference
                int attr_delim = coreference_string.indexOf(NGTreeHead.NODE_REFERENCE_ATTR_NAME_DELIMITER, coref_start);
                //debug("\nattr_delim = " + attr_delim);
                if (attr_delim > coref_start) { // za začátkem koreference před oddělovačem jména atributu něco je
                    ret = coreference_string.substring(coref_start, attr_delim); // to bude jméno uzlu
                }
            }
            //debug("\nNGForestView.getNameFromCoreference: coreference_string='" + coreference_string + "', returning the name of the node: '" + ret + "'");
            return ret;
        }

        public boolean equals (Object o) { // fake
            return o == this;
        }
    }

    /**
     * Prints all references in the actual forest.
     * @param g2 a graphic object to draw to
     */
    protected void printReferences(Graphics2D g2) {
        DefaultListModel patterns = reference_patterns;
        ReferencePattern pattern;
        int size = patterns.size();
        //debug("\nNGTreeView.printReferences: There are " + size + " reference patterns");
        for (int i=0; i<size; i++) {
            pattern = (ReferencePattern)patterns.get(i);
            //debug("\nNGTreeView.printReferences: Considering this reference:\n" + pattern);
            //debug("\nNGTreeView.printReferences: pattern.getDisplay()=" + pattern.getDisplay());
            if (pattern.getDisplay()) { // má se tato reference zobrazovat?
                printReference(g2, pattern);
            }
        }
    }

    private void printReference(Graphics2D g2, ReferencePattern pattern) { // zobrazí všechny reference podle jednoho coreference pattern
        int start_index = forest.getHead().getIndexOfAttribute(pattern.getStartAttrNodeName()); // index atributu koreference u počátečního uzlu koreference
        int id_index = forest.getHead().getIndexOfAttribute(pattern.getEndAttrNodeName()); // index atributu identifikujícího uzel pro koreferenci
        int name_index = forest.getHead().getIndexOfAttribute(NGTreeHead.META_ATTR_NODE_NAME);
        for (NGTree tree : forest.getTrees()) { // vezmu postupně stromy
            printCoreference(tree, tree.getRoot(), g2, pattern, start_index, id_index, name_index); // projde strom rekurzivně a u každého uzlu namaluje případnou koreferenci vedoucí z něj
        }
    } // printReference

    private void printCoreference(NGTree tree, TNode node, Graphics2D g2, ReferencePattern pattern, int start_index, int id_index, int name_index) {
        if (node == null) {
            return; // ukončující podmínka rekurze
        }
        // zpracování tohoto uzlu
        if (!node.skryvany || show_hidden_nodes) { // pokud je vrchol neskryvany nebo se maji zobrazit i skryvane vrcholy
            TNode end_node;
            //String value = node.getValue(0, start_index, 0); // chci první hodnotu atributu s indexem start_index v první sadě (koreference jsou jen první hodnoty první sady)
            TAHLine values = node.getValues(0, start_index); // chci hodnoty atributu s indexem start_index v první sadě (koreference jsou jen u první sady, ale mohou mit vice hodnot)
            while (values != null) { // přes všechny koreferenční odkazy daného typu z tohoto uzlu
                String value = values.Value;
                if (value.length() > 0) { // zdá se, že v tomto uzlu začíná koreference (atribut obsahuje identifikátor cílového uzlu
                    end_node = forest.findNodeById(id_index, value.trim()); // najde uzel, do kterého má koreference vést; pokud není v tomto stromu, vrátí null
                    if (end_node == null && name_index >= 0) { // cíl koreferenčního odkazu nebyl nalezen podle id v tomto stromě a meta-atribut _name je v hlavičce
                        // zkusím najít cíl koreference podle pojmenování meta-atributem _name (v dotazech)
                        // pokud ani tohle nenajdu, pak bude end_node definitivně null a asi se jedná o odkaz do jiného stromu
                        String node_name = getNameFromCoreference(value.trim());
                        if (node_name != null) {
                            end_node = forest.findNodeById(name_index, node_name);
                        }
                    }
                    printOneCoreference(tree, g2, node, end_node, pattern);
                }
                values = values.Next;
            }
        }
        // rekurzívní volání
        printCoreference(tree, node.first_son, g2, pattern, start_index, id_index, name_index);
        printCoreference(tree, node.brother, g2, pattern, start_index, id_index, name_index);
    }

    /**
     * It expects a coreference string as an argument and returns the name of the node from the coreference
     * If the argument is not a coreference, it returns null.
     * @param coreference_string
     * @return
     */
    private String getNameFromCoreference(String coreference_string) {
        // POZOR, vnitrni trida Comparator ma svoji kopii tehle funkce!
        String ret = null;
        int coref_start = coreference_string.indexOf(NGTreeHead.NODE_REFERENCE_START);
        //debug("\ncoref_start = " + coref_start);
        int coref_end = coreference_string.indexOf(NGTreeHead.NODE_REFERENCE_END, coref_start);
        //debug("\ncoref_end = " + coref_end);
        if (coref_start >= 0 && coref_end > coref_start) {
            // hodnota obsahuje začátek a konec koreference
            coref_start += NGTreeHead.NODE_REFERENCE_START.length(); // posunu se za začátek koreference
            int attr_delim = coreference_string.indexOf(NGTreeHead.NODE_REFERENCE_ATTR_NAME_DELIMITER, coref_start);
            //debug("\nattr_delim = " + attr_delim);
            if (attr_delim > coref_start) { // za začátkem koreference před oddělovačem jména atributu něco je
                ret = coreference_string.substring(coref_start, attr_delim); // to bude jméno uzlu
            }
        }
        //debug("\nNGForestView.getNameFromCoreference: coreference_string='" + coreference_string + "', returning the name of the node: '" + ret + "'");
        return ret;
    }

    private void printOneCoreference(NGTree tree, Graphics2D g2, TNode start_node, TNode end_node, ReferencePattern pattern) { // nakreslí jednu koreferenční šipku
        int x_start = tree.getXStart(); // posunutí počátku souřadnic pro daný strom
        int y_start = tree.getYStart();
        int start_x = x_start + start_node.getX(); // souřadnice počátečního vrcholu
        int start_y = y_start + start_node.getY();
        int end_x;
        int end_y;
        if (end_node != null) {
            end_x = x_start + end_node.getX();
            end_y = y_start + end_node.getY();
        }
        else {
            if (pattern.getStartNodeNilArrow()) { // má se kreslit šipka bez koncového vrcholu (do jiné věty)
                end_y = start_y;
                end_x = start_x - 30;
            }
            else { // šipky bez koncových vrcholů se nezobrazují
                return;
            }
        }
        //debug("\nDrawing a coreference from [" + start_x + ", " + start_y + "] to [" + end_x + ", " + end_y + "]");
        Stroke prev_stroke = g2.getStroke(); // uchování dosavadního stylu čáry
        Color prev_color = g2.getColor();
        String value_depend_value = getValueDependentValue(start_node, pattern);
        int shape = determineShape(start_node, pattern, value_depend_value); // tvar a styl šipky
        g2.setStroke(determineStroke(shape)); // nastavení stylu čáry
        g2.setColor(determineColor(start_node, pattern, value_depend_value)); // nastavení barvy čáry
        int curve_type = determineCurve(shape);
        boolean draw_arrowend_start = (shape & SHAPE_AND_MASK_START_ARROW) != 0; // má se malovat zakončení šipky u startovního uzlu?
        boolean draw_arrowend_end = (shape & SHAPE_AND_MASK_END_ARROW) != 0; // má se malovat zakončení šipky u koncového uzlu?
        drawArrow(g2, start_x, start_y, end_x, end_y, end_node==null, curve_type, draw_arrowend_start, draw_arrowend_end);
        g2.setStroke(prev_stroke); // vrácení stylu čáry do původní podoby
        g2.setColor(prev_color);
    }

    private void drawArrow(Graphics2D g2, int start_x, int start_y, int end_x, int end_y, boolean nil, int curve_type, boolean draw_arrowend_start, boolean draw_arrowend_end) {
        Point2D.Float start = new Point2D.Float(start_x, start_y);
        Point2D.Float end = new Point2D.Float(end_x, end_y);
        QuadCurve2D.Float curve = new QuadCurve2D.Float();
        int control_x, control_y;
        double dist_x, dist_y, delta_x, delta_y;
        if (nil) { // šipka bez koncového uzlu nebude zakřivená
            control_x = (start_x + end_x)/2; // kontrolní bod je uprostřed mezi startovním a koncovým uzlem
            control_y = (start_y + end_y)/2;
        }
        else switch (curve_type) {
            case CURVE_TYPE_STRAIGHT: // a straight line
                control_x = (start_x + end_x)/2; // kontrolní bod je uprostřed mezi startovním a koncovým uzlem
                control_y = (start_y + end_y)/2;
                //debug("\nDrawing a straight line.");
                break;
            case CURVE_TYPE_QUAD_DOWN_START: // s quad curve shaped downward with a control point closer to start node
                //double dist = Math.sqrt((end_x - start_x)*(end_x - start_x) + (end_y - start_y)*(end_y - start_y)); // vzdálenost počátečních bodů
                control_x = (start_x * 5 + end_x)/6; // kontrolní bod je blíže k počátečnímu uzlu - šipka bude více prohnutá u počátečního uzlu
                control_y = (start_y * 5 + end_y)/6; // kontrolní bod je zatím na přímé spojnici startu a konce, bude posunut
                dist_x = Math.abs(end_x - start_x);
                dist_y = Math.abs(end_y - start_y);
                delta_x = dist_y/5;
                delta_y = dist_x/5;
                control_x += (start_x < end_x) ? +delta_x : -delta_x;
                control_y += delta_y;
                //debug("\nDrawing a curved line.");
                break;
            case CURVE_TYPE_QUAD_UP_START: // s quad curve shaped upward with a control point closer to start node
                //double dist = Math.sqrt((end_x - start_x)*(end_x - start_x) + (end_y - start_y)*(end_y - start_y)); // vzdálenost počátečních bodů
                control_x = (start_x * 5 + end_x)/6; // kontrolní bod je blíže k počátečnímu uzlu - šipka bude více prohnutá u počátečního uzlu
                control_y = (start_y * 5 + end_y)/6; // kontrolní bod je zatím na přímé spojnici startu a konce, bude posunut
                dist_x = Math.abs(end_x - start_x);
                dist_y = Math.abs(end_y - start_y);
                delta_x = dist_y/5;
                delta_y = dist_x/5;
                control_x += (start_x > end_x) ? +delta_x : -delta_x;
                control_y -= delta_y;
                //debug("\nDrawing a curved line.");
                break;
            default: // a straight line
                control_x = (start_x + end_x)/2; // kontrolní bod je uprostřed mezi startovním a koncovým uzlem
                control_y = (start_y + end_y)/2;
                //debug("\nDrawing a straight line.");
                break;
        }
        Point2D.Float control = new Point2D.Float(control_x, control_y);
        curve.setCurve(start, control, end);
        g2.draw(curve);
        // nyní se nakreslí případná zakončení šipky
        if (draw_arrowend_start) drawArrowEnd(g2, start, control);
        if (draw_arrowend_end) drawArrowEnd(g2, end, control);
    }

    private void drawArrowEnd(Graphics2D g2, Point2D.Float point, Point2D.Float from_direction) {
        //debug("\nNGTreeView.drawArrowEnd: Drawing an arrow end at point " + point + " from direction " + from_direction);
        double point_x = point.getX();
        double point_y = point.getY();
        double from_x = from_direction.getX();
        double from_y = from_direction.getY();
        double delta_x = from_x - point_x;
        double delta_y = from_y - point_y;
        double dist = Math.sqrt(delta_x*delta_x + delta_y * delta_y); // vzdálenost počátečních bodů
        double ratio_x = delta_x/dist;
        double ratio_y = delta_y/dist;
        double point2_x = point_x + ratio_x * 9; // 9 == vzdálenost bodu 2 od hrotu šipky
        double point2_y = point_y + ratio_y * 9;
        double point3_x = point_x + ratio_x * 12; // 12 == vzdálenost bodu 3 od hrotu šipky
        double point3_y = point_y + ratio_y * 12;
        double point3L_x = point3_x + ratio_y * 4; // 4 == vzdálenost krajních bodů od osy šipky
        double point3L_y = point3_y - ratio_x * 4;
        double point3P_x = point3_x - ratio_y * 4; // 4 == vzdálenost krajních bodů od osy šipky
        double point3P_y = point3_y + ratio_x * 4;
        g2.setStroke(stroke_line);

        GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 4);
        filledPolygon.moveTo((int)point_x, (int)point_y);
        filledPolygon.lineTo((int)point3L_x, (int)point3L_y);
        filledPolygon.lineTo((int)point2_x, (int)point2_y);
        filledPolygon.lineTo((int)point3P_x, (int)point3P_y);
        filledPolygon.lineTo((int)point_x, (int)point_y);
        filledPolygon.closePath();
        g2.fill(filledPolygon);

        /*g2.drawLine((int)point_x, (int)point_y, (int)point3L_x, (int)point3L_y);
        g2.drawLine((int)point_x, (int)point_y, (int)point3P_x, (int)point3P_y);
        g2.drawLine((int)point2_x, (int)point2_y, (int)point3L_x, (int)point3L_y);
        g2.drawLine((int)point2_x, (int)point2_y, (int)point3P_x, (int)point3P_y);*/
    }

    /**
     * Sets the color for printing a reference.
     * @param node
     * @param pattern a reference pattern
     * @param value_depend_value the value of the attribute that may influence the color
     * @return the color
     */
    protected Color determineColor(TNode node, ReferencePattern pattern, String value_depend_value) {
        Color color = pattern.getGeneralColor();
        //debug("\nNGTreeView.determineColor: Getting value dependent color for value '" + value_depend_value + "'");
        if (value_depend_value != null) {
            if (value_depend_value.length() > 0) { // atribut má neprázdnou hodnotu
                color = pattern.getValueDependentColor(value_depend_value);
            }
        }
        //debug("\nNGTreeView.determineColor: the color is set to: " + color);
        switch (tree_properties.getColorScheme()) {
            case NGTreeProperties.COLOR_SCHEME_DEFAULT:
                break;
            case NGTreeProperties.COLOR_SCHEME_BLACK_AND_WHITE:
                color = Color.gray;
                break;
            case NGTreeProperties.COLOR_SCHEME_DIM:
                color = Color.gray;
                break;
            default:
                break;
        }
        return color;
    }

    private int determineShape(TNode node, ReferencePattern pattern, String value_depend_value) {
        int shape = pattern.getGeneralShape();
        //debug("\nNGTreeView.determineShape: Getting value dependent shape for value '" + value_depend_value + "'");
        if (value_depend_value != null) {
            if (value_depend_value.length() > 0) { // atribut má neprázdnou hodnotu
                shape = pattern.getValueDependentShape(value_depend_value);
            }
        }
        //debug("\nNGTreeView.determineShape: the shape is set to: " + shape);
        return shape;
    }

    private String getValueDependentValue(TNode node, ReferencePattern pattern) {
        String attr_depend_name = pattern.getValueDependentAttrName();
        int attr_depend_index = forest.getHead().getIndexOfAttribute(attr_depend_name);
        String value = node.getValue(0, attr_depend_index, 0); // první hodnota první sady atributu určujícího typ koreference
        return value;
    }

    private Stroke determineStroke(int shape) {
        int stroke_type = shape & SHAPE_AND_MASK_STROKE; // vyberu ty spravne bity
        Stroke stroke = stroke_line; // defaultni typ cary
        switch (stroke_type) {
            case STROKE_TYPE_LINE:
                stroke = stroke_line;
            break;
            case STROKE_TYPE_DASHED:
                stroke = stroke_dashed;
            break;
            case STROKE_TYPE_DOT_AND_DASHED:
                stroke = stroke_dot_and_dashed;
            break;
            case STROKE_TYPE_DOTTED:
                stroke = stroke_dotted;
            break;
        }
        return stroke;
    }

    private int determineCurve(int shape) {
        int curve_type = shape & SHAPE_AND_MASK_CURVE_TYPE; // vyberu ty spravne bity
        return curve_type;
    }

    /**
     * Sets the font size for labels at nodes.
     * @param size the font size
     */
    public void setFontSize(int size) {
        font_size = size; // změň lokální proměnnou
        tree_properties.setFontSize(size); // uchovej změnu v globálních vlastnostech stromů
        if (forest != null) forest.setFlagWholeForestChanged(true); // ve funkci paintComponent bude pobřeba přepočítat nakreslení stromů
        repaint();
    }

    /**
     * Returns the font size for labels at nodes.
     * @return the font size
     */
    public int getFontSize() {
      return font_size;
    }

} // class NGForestView


