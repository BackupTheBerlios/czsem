package cz.cuni.mff.mirovsky.trees;

import java.awt.*;

/**
 * A representation of a node in a tree. It points to its father, its first son and its immediate brother.
 */
public class TNode {
    public TValue values = null;
    public TNode brother = null;
    public TNode first_son = null;
    public TNode parent = null;

    public float poradi_W = -1;  // poradi slova ve vete
    public float poradi_N = -1;  // poradi uzlu ve stromu zleva doprava (u arabskych apod. zprava doleva)
    public boolean skryvany = false; // skrývaný vrchol (pro tektogramatické stromy)

    private int x = -1, y = -1;  // souradnice pro kresleni kolecka vrcholu
    private Rectangle rectangle = null; // souradnice plochy zabrane vrcholem vcetne kolecka a popisku

    // následují pomocné proměnné pro zobrazování vrcholu ve stromu
    public boolean matching_node = false; // je to vrchol nalezený jako matchující s vrcholem v dotazu?
    public boolean matching_edge = false; // je rodičovská hrana vrcholu matchující s tranzitivní hranou v dotazu?


    /**
     * Returns a deep copy of the node and its whole subtree. There must not be a cycle in the subtree (otherwise, the function never ends).
     * @return a deep copy of the node and its whole subtree
     */
    public TNode getClone() { // rekurzivní kopírování vrcholů; je-li ve vrcholech cyklus, vede k zacyklení
        // za otce klona tohoto vrcholu bude vzat null
        return getClone(null);
    }

    private TNode getClone(TNode cloned_parent) {
        TNode copy = new TNode();
        copy.skryvany = skryvany;
        copy.x = x;
        copy.y = y;
        copy.rectangle = new Rectangle(rectangle);
        copy.poradi_N = poradi_N;
        copy.poradi_W = poradi_W;
        copy.matching_node = matching_node;
        copy.matching_edge = matching_edge;
        copy.parent = cloned_parent;

        if (values != null) copy.values = values.getClone();
        else copy.values = null;

        if (first_son != null) copy.first_son = first_son.getClone(copy);
        else copy.first_son = null;

        if (brother != null) copy.brother = brother.getClone(cloned_parent);
        else copy.brother = null;

        return copy;
    }

    /**
     * Returns the same string with escape character before all the directive characters
     */
    private String escapeValue(String value) {
        StringBuffer ret = new StringBuffer("");
        int length = value.length();
        char ch;
        for (int i=0; i<length; i++) { // přes celý řetězec
            ch = value.charAt(i);
            if (ch == '[' || ch == ']' || ch == '\\' || ch == ',' || ch == '=' || ch == '\n') {
                ret.append('\\');
            }
            ret.append(ch);
        }
        return ret.toString();
    }

    /**
     * Returns FS representation of the node (recursively or not)
     * @param head a head to the tree
     * @param recursively true iff subtree should be processed too
     * @return  FS representation of the node (recursively or not)
     */
    public String toFSString(NGTreeHead head, boolean recursively) {

        if (values == null) return "";
        StringBuffer node_fs = new StringBuffer("["); // začátek vrcholu

        int size = head.getSize();
        //if (size == 0) return ""; // hlavička je prázdná

        String value;
        int relation; // relace
        TAHLine attr_values; // hodnoty jednoho atributu
        boolean first_set;
        boolean first_attr;
        boolean first_value;

        TValue values = this.values;
        first_set = true;

        while (values != null) { // přes všechny sady atributů

            if (first_set) { // je to první sada atributů pro tento vrchol
                first_set = false;
            }
            else { // není to první sada atributů pro tento vrchol
                node_fs.append("]|["); // oddělím sady atributů
            }

            first_attr = true;

            for (int i=0; i<size; i++) { // přes všechny atributy
                attr_values = values.AHTable[i];
                first_value = true;
                while (attr_values != null) { // přes všechny hodnoty atributu
                    value = attr_values.Value;
                    relation = attr_values.relation;
                    if (value != null) {
                        if (value.length() > 0) { // atribut je u vrcholu definován
                            if (first_attr) { // první definovaný atribut u tohoto vrcholu
                                first_attr = false;
                            }
                            else {
                                if (first_value) node_fs.append(',');
                            }
                            if (first_value) { // první hodnota u tohoto atributu
                                first_value = false;
                                node_fs.append(head.getAttributeAt(i).getName()); // jméno atributu
                                switch (relation) {
                                    case TAHLine.RELATION_EQ:
                                        node_fs.append("=");
                                        break;
                                    case TAHLine.RELATION_NEQ:
                                        node_fs.append("!=");
                                        break;
                                    case TAHLine.RELATION_GT:
                                        node_fs.append(">");
                                        break;
                                    case TAHLine.RELATION_LT:
                                        node_fs.append("<");
                                        break;
                                    case TAHLine.RELATION_GTEQ:
                                        node_fs.append(">=");
                                        break;
                                    case TAHLine.RELATION_LTEQ:
                                        node_fs.append("<=");
                                        break;
                                    case TAHLine.RELATION_REGEXP:
                                        node_fs.append("~=");
                                        break;
                                    default:
                                        node_fs.append("=");
                                }
                            }
                            else { // je to hodnota přidávaná za značkou "|" - "nebo"
                                node_fs.append("|");
                            }
                            value = escapeValue(value); // pokud value obsahuje řídící znaky, opatřím je escape znakem
                            node_fs.append(value);
                        }
                    }
                    attr_values = attr_values.Next; // další hodnota atributu
                }
            } // for přes atributy
            values = values.Next; // další sada atributů
        } // while přes sady atributů

        node_fs.append(']'); // konec vrcholu

        if (recursively) { // má se zpracovat i podstrom
            StringBuffer subtree_fs = new StringBuffer("");
            if (first_son != null) { // jestli vůbec nějaký podstrom je
                subtree_fs.append(first_son.toFSString(head, true));
                TNode son = first_son.brother;
                while (son != null) { // dokud má vrchol dalšího syna
                    subtree_fs.append(',');
                    subtree_fs.append(son.toFSString(head, true));
                    son = son.brother;
                }
                node_fs.append('(');
                node_fs.append(subtree_fs);
                node_fs.append(')');
            }
        }
        return node_fs.toString();
    } // toFSString

    /**
     * Returns the x-position of the displayed node
     * @return the x-position of the displayed node
     */
    public int getX() {
        return x;
    }
    /**
     * Returns the y-position of the displayed node
     * @return the y-position of the displayed node
     */
    public int getY() {
        return y;
    }
    /**
     * Sets the x-position of the displayed node
     * @param x_new the new x-position of the node
     */
    public void setX(int x_new) {
        x = x_new;
    }
    /**
     * Sets the y-position of the displayed node
     * @param y_new the new y-position of the node
     */
    public void setY(int y_new) {
        y = y_new;
    }

    /**
     * Returns the rectangle covering the area of the displayed node along with its labels.
     * @return the rectangle covering the area of the displayed node along with its labels
     */
    public Rectangle getRectangle() {
        return rectangle;
    }
    /**
     * Sets the computed rectangle covering the area occupied by the displayed node and its labels.
     * @param rectangle_new the new rectangle
     */
    public void setRectangle(Rectangle rectangle_new) {
        rectangle = new Rectangle(rectangle_new);
    }

    /**
     * Returns the value of the attribute with the given number of set and the given number of attribute and number of value (counted from 0).
     * @return the value of the attribute with the given number of set and the given number of attribute and number of value (counted from 0); null if not present or specified or out of bounds.
     * Relations are not reflected here!
     */
    public String getValue(int set_number, int attr_number, int value_number) {
        TAHLine vals = getValues(set_number, attr_number);
        int i;
        for (i=0; i<value_number; i++) { // přesunu se na správnou hodnotu
            if (vals != null) vals = vals.Next;
            else break;
        }
        if (vals == null) return null;
        // nyní ukazuji na správnou hodnotu a ta není null

        return vals.Value;
    } // getValue

    /**
     * Returns a linked list of values of the attribute with the given number of set and the given number of attribute (counted from 0).
     * @return a linked list of values of the attribute with the given number of set and the given number of attribute (counted from 0); null if not present or specified or out of bounds
     */
    public TAHLine getValues(int set_number, int attr_number) {
        TValue sets = getSetOfAttributes(set_number);
        if (sets == null) return null;
        // nyní ukazuji na správné místo v sadích a to není null
        TAHLine[] set = sets.AHTable;
        if (set == null) return null;
        // nyní ukazuji na správnou sadu a ta není null
        if (attr_number < 0 || attr_number >= set.length) return null; // pokud jsem mimo pole, končím

        return set[attr_number];
    } // getValues

    /**
     * Returns the set of attributes with the given number (counted from 0).
     * @return the set of attributes with the given number (counted from 0); null if not present or specified or out of bounds
     */
    public TValue getSetOfAttributes(int set_number) {
        TValue val = values;
        int i;
        for (i=0; i<set_number; i++) { // přesunu se na správnou sadu
            if (val != null) val = val.Next;
            else break;
        }

        return val;
    } // getSetOfAttributes

    /**
     * Returns the number of present sets.
     * @return the number of present sets
     */
    public int getNumberOfSets() {
        TValue val = values;
        int i = 0;
        while (val != null) { // dokud neprojdu všechny sady
            val = val.Next;
            i++;
        }
        return i;
    }

    /**
     * It increases number of attributes at the node by the given number.
     * The new attributes are added at the end of the list of attributes.
     * The values of the attributes are set empty ("").
     * @param number number of attributes to be added
     */
    public void addAttributes(int number) {
        if (number <= 0) return;
        values.addAttributes(number);
    }

} // TNode
