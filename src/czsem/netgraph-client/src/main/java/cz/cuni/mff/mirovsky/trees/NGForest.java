package cz.cuni.mff.mirovsky.trees;

import java.util.ArrayList;

import cz.cuni.mff.mirovsky.ShowMessagesAble;

import javax.swing.*;

/**
 * A class keeping a forest of trees. It is usually only one tree, in case of multi-tree query it consists of several trees.
 */
public class NGForest {

    /**
     * a line break in messages with the server. It is a copy of the value from object ServerCommunication; if changed there, it must be changed here, too.
     */
    public final static byte EOL = 13;  // ukoncovaci znak radku ve zprave; to je tu okopírované ze ServerCommunication, protoze se mi nedari na to odkazat
    /**
     * an alternative line break in messages with the server. It is a copy of the value from object ServerCommunication; if changed there, it must be changed here, too.
     */
    public final static byte EOL_2 = 10;  // dalsi mozny ukoncovaci znak radku ve vstupu
    /**
     * an end-of-message character in messages with the server. It is a copy of the value from object ServerCommunication; if changed there, it must be changed here, too.
     */
    public final static byte EOM = 0;  // ukoncovaci znak zpravy; rovnez okopirovane ze ServerCommunication

    private ArrayList<NGTree> trees; // jednotlivé stromy
    private NGTreeHead head; // společná hlavička stromů

    private String file_name; // sem se načte jméno souboru, ve kterém byl aktuální les nalezen
    private int forest_number; // sem se načte pořadí aktuálního lesa v souboru, kde byl nalezen

    private boolean flag_forest_changed; // je potřeba les překreslit?

    private NGTree chosen_tree; // vybraný strom
    private int chosen_tree_order; // pořadí vybraného stromu (počítáno od 1)

    private ShowMessagesAble mess; // objekt pro výpis hlášek

    private DefaultListModel vybrane_atributy; // vybrané atributy

    /**
     * Creates a new empty forest.
     * @param mess an object capable of displaying messages
     */
    public NGForest(ShowMessagesAble mess) {
        this.mess = mess;
        trees = new ArrayList<NGTree>();
        file_name = new String(""); // nalezený les je ze souboru s tímto jménem
        forest_number = 0;
        head = null;
        chosen_tree = null;
        chosen_tree_order = 0;
        vybrane_atributy = new DefaultListModel(); // prázdný seznam atributů
        flag_forest_changed = true;        
    }

    private void debug (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
        if (mess != null) {
            mess.debug(message);
        }
    }

    //private void inform (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
    //    if (mess != null) {
    //	    mess.inform (message);
    //	}
    //}

    /**
     * Returns a deep copy of the forest.
     * @return deep copy of the forest
     */
    public NGForest getClone() {

        NGForest copy = new NGForest(mess);

        copy.file_name = new String(file_name);
        copy.forest_number = forest_number;
        
        copy.head = head.getClone(); // hlavička stromu

        for (NGTree tree : trees) {
            copy.addTree(tree.getClone());
        }

        /* koreferencni schemata se ted uchovavaji v ServerCommunication
        copy.reference_patterns = new DefaultListModel();
        int size = reference_patterns.size();
        ReferencePattern pattern;
        for (int i=0; i<size; i++) {
            pattern = (ReferencePattern)reference_patterns.getElementAt(i);
            copy.addCoreferencePattern(pattern.getClone());
        }*/
        
        // vybrane_atributy:
        copy.vybrane_atributy = copyListModel(vybrane_atributy);

        copy.flag_forest_changed = true; // je potřeba strom přepočítat

        // pomocné proměnné nekopíruji

        return copy;
    } // getClone

    /**
     * Returns FS representation of the forest (with or without the head).
     * @param with_head says if the head should be included in the FS representation
     * @return the FS representation of the forest
     */
    public String toFSString(boolean with_head) {
        StringBuffer forest_fs_buffer = new StringBuffer("");

        for (NGTree tree : trees) {
            if (tree.isEmpty()) continue;
            forest_fs_buffer.append(tree.toFSString(with_head, head));
        }

        return forest_fs_buffer.toString();
    } // toFSString

    /**
     * Returns an id of the first tree in the forest.
     * @return an id of the first tree in the forest or an emtpy String if there are no trees.
     */
    public String getId() { // it returns id of the first tree
        String id;
        NGTree first_tree = getFirstTree();
        if (first_tree == null) {
            id = "";
        }
        else {
            id = first_tree.getId(head);
        }
        return id;
    }

    /**
     * Returns the number of trees in the forest.
     * @return the number of trees in the forest
     */
    public int getNumberOfTrees() {
        return trees.size();
    } // getNumberOfTrees

    private NGTree getFirstTree() {
        if (trees.size() > 0) {
            return trees.get(0);
        }
        else {
            return null;
        }
    } // getFirstTree

    /**
     * Adds a tree as the new last tree in the forest.
     * @param tree a tree to be added
     */
    public void addTree(NGTree tree) {
        trees.add(tree);
        chosen_tree = tree;
        chosen_tree_order = trees.size();
    }

    /**
     * Signals whether the whole forest has changed and coordinates of all its trees and their nodes must be calculated.
     * @param flag value true signals that the coordinates need to be calculated
     */
    public void setFlagWholeForestChanged(boolean flag) { // nastaví flag u lesa, že je potřeba přepočítat nakreslení
        flag_forest_changed = flag;
        for (NGTree tree : trees) {
            tree.setFlagTreeChanged(true); // tady je potřeba nastavit jednotlivé flagy všech stromů, když nevím, o který strom šlo
        }
    }

    /**
     * Signals whether a tree in the forest has changed and coordinates the tree and its nodes and coordinates of the subsequent trees must be calculated.
     * @param flag value true signals that the coordinates need to be calculated
     */
    public void setFlagForestChanged(boolean flag) { // nastaví flag u lesa, že je potřeba přepočítat nakreslení
        flag_forest_changed = flag;
    }

    /**
     * Says whether the forest has changed and coordinates of (some of) its trees and their nodes must be calculated.
     * @return true iff the coordinates need to be calculated
     */
    public boolean getFlagForestChanged() { // vrátí flag u lesa, zda je potřeba přepočítat nakreslení
        return flag_forest_changed;
    }

    /**
     * Returns the head of the forest.
     * @return the head of the forest
     */
    public NGTreeHead getHead() {
        return head;
    }

    /**
     * Sets a head of the forest.
     * @param head a head
     */
    public void setHead(NGTreeHead head) {
        this.head = head;
    }

    /**
     * Returns the trees of the forest in ArrayList.
     * @return the trees in ArrayList
     */
    public ArrayList<NGTree> getTrees() {
        return trees;
    }

    /**
     * Returns a list of selected attributes. Selected attributes are displayed at nodes in the tree.
     * @return a list of selected attributes
     */
    public DefaultListModel getVybraneAtributy() { // vrátí seznam vybraných atributů
        return vybrane_atributy;
    }

    /**
     * Sets a list of selected attributes. Selected attributes are displayed at nodes in the tree.
     * @param p_vybrane_atributy the list of selected attributes
     */
    public void setVybraneAtributy(DefaultListModel p_vybrane_atributy) { // nastaví seznam vybraných atributů
        vybrane_atributy = copyListModel(p_vybrane_atributy);
    }


    private DefaultListModel copyListModel (DefaultListModel src) { // vytvoří kopii
        DefaultListModel target = new DefaultListModel();
        for (int i=0; i<src.getSize(); i++) {
            target.addElement(src.getElementAt(i));
        }
        return target;
    }

    /**
     * Returns the total number of all nodes in all trees in the forest.
     * @return the total number of all nodes in all trees in the forest
     */
    public int getNumberOfNodes() { // vrátí počet všech vrcholů sečtený ze všech stromů
        int number_of_nodes = 0;
        for (NGTree tree: trees) {
            number_of_nodes += tree.getNumberOfNodes();
        }
        return number_of_nodes;
    }

    /**
     * Returns the total number of hidden nodes in all trees in the forest.
     * @return the total number of hidden nodes in all trees in the forest
     */
    public int getNumberOfHiddenNodes() { // vrátí počet všech skrytých vrcholů sečtený ze všech stromů
        int number_of_hidden_nodes = 0;
        for (NGTree tree: trees) {
            number_of_hidden_nodes += tree.getNumberOfHiddenNodes();
        }
        return number_of_hidden_nodes;
    }

    /**
     * It increases number of attributes at all nodes of all trees by number.
     * The new attributes are added at the end of the list of attributes.
     * Values of the new attributes are set empty ("").
     * The head of the forest is not changed (must be changed separately).
     * @param number number of attributes to be added
     */
    public void addAttributes(int number) {
        if (number <= 0) return;
        for (NGTree tree : trees) {
            tree.addAttributes(number);
        }
    }

    /**
     * It sets the matching meta tag at the matching nodes of the trees of the forest.
     * The matching meta tag marks matching nodes after the forest is saved e.g. to local disc.
     */
    public void setMatchingMetaTags() {
        for (NGTree tree : trees) {
            tree.setMatchingMetaTags(head);
        }
    }

    /**
     * Returns the actually selected node.
     * @return the selected node
     */
    public TNode getChosenNode() {
        if (chosen_tree == null) return null;
        return chosen_tree.getChosenNode();
    }

    /**
     * Returns the actually selected tree.
     * @return the selected tree
     */
    public NGTree getChosenTree() {
        if (chosen_tree != null) {
            return chosen_tree;
        }
        if (trees.size() != 0) { // v lese je alespoň jeden strom
            setChosenTreeByOrder(1);
            return chosen_tree;
        }
        return null;
    }

    /**
     * Return the file name the forest comes from.
     * @return the name of the file the forest comes from
     */
    public String getFileName() { // vrátí jméno souboru, ze kterého je tento les
      return new String(file_name);
    }

    /**
     * Sets the name of the file the forest comes from.
     * @param p_name the name of the file the forest comes from
     */
    public void setFileName(String p_name) { // nastaví jméno souboru, ze kterého je tento les
      file_name = new String (p_name);
    }

    /**
     * Returns the number of the forest in the file it comes from. It is counted from 1.
     * @return the number of the forest in the file it comes from
     */
    public int getForestNumber() { // vrátí pořadí lesa v souboru, kde byl nalezen
      return forest_number;
    }

    /**
     * Sets the number of the forest in the file it comes from. It is counted from 1.
     * @param number the number of the forest in the file it comes from
     */
    public void setForestNumber(int number) { // nastaví pořadí lesa v souboru, kde byl nalezen
      forest_number = number;
    }

    /**
     * Marks a node with given order as selected, as well as the tree the node is from.
     * The root of the first tree has order 1, the first tree has order 1, the first node of each subsequent tree
     * has the order like the last node of the previous tree plus 1.
     * @param order depth-first order of the node to be selected
     */
    public void setChosenNodeByDepthOrder(int order) { // vrchol v pořadí 'order' při průchodu do hloubky se označí jako vybraný
        // kořen 1. stromu má pořadí 1, kořen následujícího stromu má pořadí o jedna větší než poslední vrchol předcházejícího stromu
        int nodes_so_far = 0;
        int nodes_one_tree;
        int tree_number = 0; // počítám stromy, abych mohl nastavit chosen_tree_order
        NGTree last_chosen_tree = getChosenTree(); // there was a chosen node in this tree
        for (NGTree tree : trees) {
            tree_number ++;
            nodes_one_tree = tree.getNumberOfNodes();
            nodes_so_far += nodes_one_tree;
            if (nodes_so_far >= order) { // hledaný vrchol je v tomto stromě
                tree.setChosenNodeByDepthOrder(order - (nodes_so_far - nodes_one_tree));
                // because of possible different visibility of alternative sets at chosen and not-chosen nodes,
                // this tree might need to re-calculate its drawing and also the tree that had a node chosen before
                if (last_chosen_tree != null) {
                    last_chosen_tree.setFlagTreeChanged(true);
                }
                tree.setFlagTreeChanged(true);
                chosen_tree = tree;
                chosen_tree_order = tree_number;
                setFlagForestChanged(true);
                return;
            }
        }
        // sem se dojde, když se vrchol s daným pořadím nenašel
        if (last_chosen_tree != null) {
            last_chosen_tree.setChosenNodeByDepthOrder(0); // zruší se minule vybraný uzel
            last_chosen_tree.setFlagTreeChanged(true);
        }
        chosen_tree = null;
        chosen_tree_order = 0;
    } // setChosenByDepthOrder

    /**
     * Returns depth-first order of the chosen node, counted from 1. If count_hidden is true, hidden nodes are counted as normal nodes.
     * If count_hidden is false, hidden nodes are not counted; if the chosen node is hidden, the order of its nearest non-hidden predecessor is returned.
     * The root of the first tree is 1, the order of the root of any other tree is 1 bigger than the order of the last node of the previous tree.
     * @param count_hidden says if hidden nodes should be counted
     * @return -1 if not found; a positive value (depth-first order counted from 1) if found; 0 if the chosen node is hidden, count_hidden is false and
     * there is no non-hidden node is on the path from it to the root (if in the first tree, otherwise returns the order of the last node in the previous tree)
     */
    public int getChosenNodeDepthOrder(boolean count_hidden) { // 0 znamená, že není vybrán žádný vrchol; count_hidden určuje, zda se budou počítat skryté uzly
        // kořen prvního stromu má pořadí 1, kořen každého dalšího stromu má pořadí o jedna větší než poslední vrchol předchozího stromu
        if (chosen_tree_order == 0) return 0;
        int depth_order = 0; // sem to spočítám
        for (NGTree tree : trees) {
            if (tree == chosen_tree) { // došel jsem k vybranému stromu
                int order = tree.getChosenNodeDepthOrder(count_hidden);
                if (order != 0) { // to by mělo být vždy různé od nuly
                    return depth_order + order;    
                }
                else { // sem by to nemělo dojít; pokud ano, tak něco nevyšlo; vracím nulu - žádný vybraný vrchol
                    return 0;
                }
            }
            depth_order += tree.getNumberOfNodes();
        }
        return 0; // sem by to nemělo dojít; pokud ano, tak něco nevyšlo; vracím nulu - žádný vybraný vrchol
    } // getChosenNodeDepthOrder

    /**
     * Returns depth-first order (counted from 1) of the node matching the root of the first tree of the query. If count_hidden is true, hidden nodes are counted as normal nodes.
     * If count_hidden is false, hidden nodes are not counted; if the matching node is hidden, the order of its nearest non-hidden predecessor is returned.
     * The root of the first tree is 1, the order of the root of any other tree is 1 bigger than the order of the last node of the previous tree.
     * @param count_hidden says if hidden nodes should be counted
     * @return -1 if not found; a positive value (depth-first order counted from 1) if found; 0 if the matching node is hidden, count_hidden is false and
     * there is no non-hidden node is on the path from it to the root (if in the first tree, otherwise returns the order of the last node in the previous tree)
     */
    public int getFirstMatchingNodeDepthOrder(boolean count_hidden) { // 0 znamená, že nematchuje žádný vrchol (nemělo by se stát)
        // count_hidden udává, zda se do pořadí mají počítat skryté uzly
        // kořen prvního stromu má pořadí 1, kořen každého dalšího stromu má pořadí o jedna větší než poslední vrchol předchozího stromu
        int depth_order = 0; // sem to spočítám
        for (NGTree tree : trees) {
            //if (tree == chosen_tree) { // došel jsem k vybranému stromu
                int order = tree.getFirstMatchingNodeDepthOrder(count_hidden);
                if (order != 0) { // pokud je různé od nuly, našlo se
                    return depth_order + order;
                }
                //else { // sem by to nemělo dojít; pokud ano, tak něco nevyšlo; vracím nulu - žádný vrchol matchující s dotazem
                //    return 0;
                //}
            //}
            depth_order += tree.getNumberOfNodes();
        }
        return 0; // sem by to nemělo dojít; pokud ano, tak něco nevyšlo; vracím nulu - žádný vybraný vrchol
    } // getFirstMatchingNodeDepthOrder

    /**
     * Returns the order of the selected tree (the tree with the selected node), counted from 1.
     * @return the order of the selected tree
     */
    public int getChosenTreeOrder() {
        return chosen_tree_order;
    }

    /**
     * Sets the tree with a given order as selected. The order of the first tree is 1.
     * @param order the order of the tree to be selected
     */
    public void setChosenTreeByOrder(int order) { // nastaví strom s daným pořadím jako zvolený strom (počítáno od 1)
        int i = 1;
        for (NGTree tree : trees) {
            if (i == order) {
                chosen_tree = tree;
            }
            i++;
        }
    }

    /**
     * Searches for a node in the forest that has a given id.
     * @param id_index an index of the id-attribute in the head of the forest
     * @param value an identifier to be searched for
     * @return the node with the given id or null if not found
     */
    public TNode findNodeById(int id_index, String value) {
        TNode node;
        for (NGTree tree : trees) {
            node = tree.findNodeById(id_index, value);
            if (node != null) { // the node has been found
                return node;
            }
        }
        return null; // the node has not been found
    } // findNodeById

    /**
     * Reads a forest from p_source in FS format (without a head). The head must have already been set.
     * @param p_source the source in FS format
     * @param start_position a position in p_source of the first character to be read
     * @param p_number_of_attributes number of attributes in the head
     * @return the number of read characters
     */
    public int readForest (char[] p_source, int start_position, int p_number_of_attributes) { // nacita ze source kompletni les ve formatu fs bez hlavicky
        // vrací počet přečtených znaků
        //debug ("\nNGForest.readForest: entering the function");
        //debug ("\nNGForest.readForest: the source forest length is " + p_source.length + ", start_position is " + start_position);
        int read_chars = 0;
        trees = new ArrayList<NGTree>();
        while (start_position + read_chars < p_source.length) { // dokud je vstup, hledám začátek prvního stromu
            if (p_source[start_position+read_chars] == '[') { // začíná tu první strom
                break; // jdu číst stromy
            }
            else { // vše před prvním stromem přeskočím (AND nebo OR)
                read_chars++;    
            }
        }
        while (start_position + read_chars < p_source.length) { // dokud je vstup
            //debug ("\nNGForest.readForest: going to read a tree...");
            while(p_source[start_position+read_chars] == EOL || p_source[start_position+read_chars] == EOL_2) {
                read_chars++; // přeskočím oddělovače řádků, kdyby tam byly
            }
            NGTree tree = new NGTree(mess);
            read_chars += tree.readTree(head, p_source, start_position + read_chars, p_number_of_attributes);
            //debug ("\nNGForest.readForest: ...done; number of read chars so far: " + read_chars);
            if (!tree.isEmpty()) { // načtený strom není prázdný, čili načetl se strom
                addTree(tree);
            }
        }
        if (trees.size() == 0) { // nenačetl se žádný strom
            chosen_tree = null; // nastavit na první strom!
            chosen_tree_order = 0;
        }
        flag_forest_changed = true; // každopádně předpokládám, že došlo k nějaké změně
        return read_chars;
    }


/*    public int readCoreferences (byte[] p_source, int start_position) { // nacita ze source koreference
        // vrací počet přečtených znaků
        // očekávám co řádka, to jedna koreference; seznam bude ukončen prázdnou řádkou. Znak konce řádky je ServerCommunication.EOL
        //debug("\nJsem v readCoreferences(byte...)");
        clearCoreferencePatterns();
        ReferencePattern pattern = new ReferencePattern(mess);
        int position = start_position; // pozice ve vstupním poli při načítání koreferencí
        int source_length = p_source.length;
        StringBuffer line = new StringBuffer();
        int line_length = readLine(line, p_source, position);
        //debug("\nline_length = " + line_length);
        position += line_length;
        while (line_length > 1) { // another coreference pattern has been read
            //debug("\nNačtena řádka: " + line);
            pattern.readPatternFromString(line.toString());
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
    

/*    private int readLine(StringBuffer target, byte[] source, int start_position) { // čte bajty z source do StringBufferu, dokud nepřečte ServerCommunication.EOL
        int position = start_position;
        int length = source.length;
        char ch;
        byte b;
        //debug("\nNGTree.readLine: the source length is " + length + ", the position is " + position);
        while ((b = source[position]) != EOM) {
            //debug("\nznak " + b);
            //System.out.print("\nA character has been read: '" + b + "'");
            position ++;
            if (b == EOL) break; // konec řádky
            ch = (char)b;
            target.append(ch);
        }
        return position - start_position;
    }
*/

}
