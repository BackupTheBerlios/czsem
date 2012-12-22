package cz.cuni.mff.mirovsky.trees;

import javax.swing.DefaultListModel;

import cz.cuni.mff.mirovsky.ShowMessagesAble;

/**
 * A class containing a single tree
 */

public class NGTree extends Object { // uchovává jeden strom

    /**
     * a delimiter of pairs of numbers of matching query nodes and result tree nodes. It is used in messages from the server.
     */
    public final static char MATCHING_NODES_DELIMITER = ','; // oddělovač čísel vrcholů matchujících s dotazem ve výsledném stromě
    /**
     * a delimiter of a number of a query node and a number of its matching result tree node. It is used in messages from the server.
     */
    public final static char MATCHING_NODES_QUERY_DELIMITER = ':';  // oddělovač čísel vrcholů dotazu a výsledného stromu, které spolu matchují
    /**
     * a delimiter of query trees in lists of matching nodes. It is used in messages from the server.
     */
    public final static char MATCHING_NODES_TREES_DELIMITER = ';';  // oddělovač stromů dotazu v seznamu čísel vrcholů matchujících s dotazem

    private TNode root; // kořen stromu
    //private NGTreeHead head; // hlavička stromu

    private TNode chosen_node;  // vybrany vrchol
    //public TValue chosen_set;  // zvolena sada
    private int chosen_node_depth_order; // pořadí vybraného vrcholu při průchodu do hloubky (root je 1)

    private int number_of_nodes;  // pocet vsech vrcholu stromu
    private int number_of_hidden;  // pocet skrývaných vrcholů stromu

    private boolean flag; // je potřeba strom překreslit?

    // pomocné proměnné
    private int position; // pozice ve vstupním poli při načítání stromu
    private byte[] source_byte; // vstupní pole při načítání stromu v bajtech
    private char[] source_char; // vstupní pole při načítání stromu v charech
    private int source_type; // 1 <=> bajty, 2 <=> chary
    private int source_length; // délka vstupu
    private int number_of_attributes; // pro načítání vrcholu - očekávaný počet atributů
    private boolean backslash; // pro funkci readChar

    private int source_error; // chyba vstupu
    private final static int error_ok = 0; // typ chyby: předčasný konec vstupu
    private final static int error_premature_end = 1; // typ chyby: předčasný konec vstupu

    private int poradi_prohledavaneho_vrcholu; // promenne pro prohledavani stromu pri hledani vybraneho vrcholu
    private int poradi_nalezeneho_vrcholu;
    private String matchujici_vrcholy; // pro prohledavani stromu pri vybirani vsech matchujicich vrcholu
    private int poradi_hledaneho_vrcholu;

    private String kodovany; // pomocná proměnná pro tisk řetězců v příslušném kódování

    private ShowMessagesAble mess; // objekt pro výpis hlášek

    private int tree_width; // uchovává se u jednotlivých stromů pro nastavení a využití třídou NGForestView
    private int tree_height;

    private int x_start; // uchovává se u jednotlivých stromů pro nastavení a využití třídou NGForestView
    private int y_start; // znamená posunutí počátku souřadnic pro kreslení tohoto stromu

    /**
     * Creates an empty tree
     * @param p_mess an object capable of displaying messages
     */
    public NGTree(ShowMessagesAble p_mess) {
        mess = p_mess;
        root = null;
        //head = null;
        flag = true;
        tree_width = tree_height = 0;
        chosen_node = null;
        //chosen_set = null;
        chosen_node_depth_order = 0;        
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
     * Returns a deep copy of the tree
     * @return deep copy of the tree
     */
    public NGTree getClone() {
        // return (NGTree)super.clone(); // tak to bohužel nejde, protože to není deep copy :-(
        // takže sám:

        NGTree copy = new NGTree(mess);

        copy.root = root.getClone(); // rekurzivně zkopíruj vrcholy

        //copy.head = head.getClone(); // hlavička stromu

        copy.chosen_node_depth_order = chosen_node_depth_order;
        copy.setChosenNodeByDepthOrder(copy.chosen_node_depth_order); // vybrany vrchol; nastaví i vybraný strom

        /*copy.chosen_set = copy.chosen_node.values; // teď nastavím správnou vybranou sadu
        TValue set = chosen_node.values;
        try { // pro případ nějakého nepředvídaného problému
            while (set != chosen_set) {
                copy.chosen_set = copy.chosen_set.Next;
                set = set.Next;
            }
        }
        catch (Exception e) {
            debug ("\nChyba při klonování objektu NGTree - nastavení vybrané sady!");
        }*/

        copy.number_of_nodes = number_of_nodes;
        copy.number_of_hidden = number_of_hidden;
        copy.flag = true; // je potřeba strom přepočítat
        copy.tree_width = tree_width;
        copy.tree_height = tree_height;

        return copy;
    } // getClone

    /**
     * Returns the width of the tree in pixels.
     * @return the width of the tree in pixels
     */
    public int getWidth() {
        return tree_width;
    }

    /**
     * Sets the width of the tree in pixels.
     * @param tree_width the width of the tree in pixels
     */
    public void setWidth(int tree_width) {
        //debug("\nNGTree.setWidth: setting the tree width to: " + tree_width);        
        this.tree_width = tree_width;
    }

    /**
     * Returns the height of the tree in pixels.
     * @return the height of the tree in pixels
     */
    public int getHeight() {
        return tree_height;
    }

    /**
     * Sets the height of the tree in pixels.
     * @param tree_height the height of the tree in pixels
     */
    public void setHeight(int tree_height) {
        this.tree_height = tree_height;
    }

    /**
     * Returns the x-coordinate of the top-left corner of the tree in a forest.
     * @return the x-coordinate of the top-left corner fo the tree in a forest
     */
    public int getXStart() {
        return x_start;
    }

    /**
     * Sets the x-coordinate of the top-left corner of the tree in a forest.
     * @param x_start the x-coordinate of the top-left corner fo the tree in a forest
     */
    public void setXStart(int x_start) {
        this.x_start = x_start;
    }

    /**
     * Returns the y-coordinate of the top-left corner of the tree in a forest.
     * @return the y-coordinate of the top-left corner fo the tree in a forest
     */
    public int getYStart() {
        return y_start;
    }

    /**
     * Sets the y-coordinate of the top-left corner of the tree in a forest.
     * @param y_start the y-coordinate of the top-left corner fo the tree in a forest
     */
    public void setYStart(int y_start) {
        this.y_start = y_start;
    }


    /**
     * Returns FS representation of the tree (with or without the head).
     * @param with_head says if the head should be included in the FS representation
     * @param head the head belonging to the tree
     * @return the FS representation of the tree
     */
    public String toFSString(boolean with_head, NGTreeHead head) {
        if (isEmpty()) return "";
        StringBuffer tree_fs_buffer = new StringBuffer("");

        if (with_head) {
            tree_fs_buffer.append(head.toFSString());
            tree_fs_buffer.append('\n');
        }

        tree_fs_buffer.append(root.toFSString(head,true)); // rekurzivně od kořene

        return tree_fs_buffer.toString();
    }

    /**
     * Signals whether the tree has changed and coordinates of its nodes must be calculated.
     * @param new_flag value true signals that the coordinates need to be calculated
     */
    public void setFlagTreeChanged(boolean new_flag) { // nastaví flag u stromu, že je potřeba přepočítat nakreslení
        flag = new_flag;
    }

    /**
     * Says whether the tree has changed and coordinates of its nodes must be calculated.
     * @return true iff the coordinates need to be calculated
     */
    public boolean getFlagTreeChanged() { // vrátí flag u stromu, zda je potřeba přepočítat nakreslení
        return flag;
    }


    /**
     * Signals whether the tree is empty
     * @return true iff the tree is empty
     */
    public boolean isEmpty() {
      return (root == null);
    }

    /**
     * Returns the root of the tree.
     * @return the root of the tree
     */
    public TNode getRoot() {
        return root;
    }

    /**
     * Returns the total number of nodes in the tree.
     * @return the total number of nodes in the tree
     */
    public int getNumberOfNodes() { // vrátí počet všech vrcholů
      return number_of_nodes;
    }
/*    public void setNumberOfNodes(int p_number) { // nastaví počet všech vrcholů
      number_of_nodes = p_number;
    }
*/
    /**
     * Returns the number of hidden nodes in the tree.
     * @return the number of hidden nodes in the tree
     */
    public int getNumberOfHiddenNodes() { // vrátí počet skrývaných vrcholů
      return number_of_hidden;
    }
/*    public void setNumberOfHiddenNodes(int p_number) { // nastaví počet skrývaných vrcholů
      number_of_hidden = p_number;
    }
*/
    private char readChar() {  // prochazi polem source, vynechava presuny na novy radek a
        //respektuje ocitovani (nastavuje promennou 'backshlash')
        // navratova hodnota: dalsi znak
        if (position >= source_length) {
            source_error = error_premature_end;
            debug("\nNGTree.readChar: předčasný konec vstupu!");
            return ' '; // už nelze číst
        }
        /*if (source_type == 1) { // vstupní pole je v bajtech
            return readCharByte();
        }
        else */if (source_type == 2) { // vstupní pole je v charech
            return readCharChar();
        }
        debug("\nChyba při čtení znaku ve funkci NGTree.readChar()!");
        return ' '; // chyba!
    }

/*    private char readCharByte() {
        byte b;
        int bi;
        while ((char)source_byte[position] == '\\' && (char)source_byte[position+1] == '\r' )
            position += 3;
        b = source_byte[position];
        if ((char)b == '\\' ) {
            backslash = true;
            position += 2;
            bi = source_byte[position-1];
            if (bi < 0) bi += 256; // jednobajtový unsigned char u serveru, zde to čtu do signed byte; proto tahle konverze
            return (char)bi;
        }
        backslash = false;
        position++;
        bi = source_byte[position-1];
        if (bi < 0) bi += 256; // jednobajtový unsigned char u serveru, zde to čtu do signed byte; proto tahle konverze
        //debug("" + (char)bi);
        return (char)bi;
    }
*/
    private char readCharChar() {
        while (source_char[position] == '\\' && source_char[position+1] == '\r') {
            debug("\nPosouvám se o tři pozice");
            position += 3;
        }
        if (source_char[position] == '\\' ) {
            backslash = true;
            position += 2;
            return source_char[position-1];
        }
        backslash = false;
        position++;
        return source_char[position-1];
    }

    private String readWord(NGTreeHead head) {  // nacita z pole source cela slova, tj. posloup. znaku nactenych fci readChar odpovidajici jmenu atributu ci jeho hodnote
        /*if (source_type == 1) { // čtu z pole bajtů - kódování UTF-8
            return readWordUTF8();
        }*/
    // navratova hodnota: nactene slovo
        char c,d;
        StringBuffer str = new StringBuffer("");
        if (position >= source_length) {
            source_error = error_premature_end;
            return str.toString();
        }
        c = readChar();
        //debug("\n" + c);
        while( (c!='|' & c!=']' & c!=',' & c!='=') || (backslash == true) ) {
            if (source_error != error_ok) { // vyskytla se chyba při čtení znaku
                return "";
            }
            if (c == '~') { // možný začátek znaku '~='
                d = readChar(); // zkusím přečíst další, jestli to není '='
                if (source_error != error_ok) { // vyskytla se chyba při čtení znaku
                    return "";
                }
                if (d == '=') { // skutečně to byl dvojznak '~='
                    position --; // vrátím se o jednu pozici, za cyklem se vrátím ještě o jednu, tj. před dvojznak '!='
                    //debug("\nNačteno slovo: " + str + ", ukončeno bylo dvojznakem '!='");
                    break;
                }
                else { // není to dvojznak '~=', vlnka patří normálně do slova
                    position --; // zruším přečtení znaku 'd'
                }            	
            }
            else if (c == '!') { // možný začátek znaku '!='
                d = readChar(); // zkusím přečíst další, jestli to není '='
                if (source_error != error_ok) { // vyskytla se chyba při čtení znaku
                    return "";
                }
                if (d == '=') { // skutečně to byl dvojznak '!='
                    position --; // vrátím se o jednu pozici, za cyklem se vrátím ještě o jednu, tj. před dvojznak '!='
                    //debug("\nNačteno slovo: " + str + ", ukončeno bylo dvojznakem '!='");
                    break;
                }
                else { // není to dvojznak '!=', vykřičník patří normálně do slova
                    position --; // zruším přečtení znaku 'd'
                }
            }
            else if (c == '<') { // '<' nebo možný začátek znaku '<='
                if (head.isAttribute(str.toString())) { // před tímto znakem bylo jméno atributu, čili lze s vysokou pravděpodobností tento znak prohlásit za relační znaménko
                    d = readChar(); // zkusím přečíst další, jestli to není '='
                    position --; // vrátím se o jednu pozici, za cyklem se vrátím ještě o jednu, tj. před znak '<'
                    if (source_error != error_ok) { // vyskytla se chyba při čtení znaku
                        return "";
                    }
                    if (d == '=') { // skutečně to byl dvojznak '<='
                        //debug("\nNačteno slovo: " + str + ", ukončeno bylo dvojznakem '<='");
                        break;
                    }
                    else { // šlo o relaci '<', nenásledovalo '='
                        //debug("\nNačteno slovo: " + str + ", ukončeno bylo znakem '<'");
                        break;
                    }
                }
            }
            else if (c == '>') { // '>' nebo možný začátek znaku '>='
                if (head.isAttribute(str.toString())) { // před tímto znakem bylo jméno atributu, čili lze s vysokou pravděpodobností tento znak prohlásit za relační znaménko
                    d = readChar(); // zkusím přečíst další, jestli to není '='
                    position --; // vrátím se o jednu pozici, za cyklem se vrátím ještě o jednu, tj. před znak '>'
                    if (source_error != error_ok) { // vyskytla se chyba při čtení znaku
                        return "";
                    }
                    if (d == '=') { // skutečně to byl dvojznak '>='
                        //debug("\nNačteno slovo: " + str + ", ukončeno bylo dvojznakem '>='");
                        break;
                    }
                    else { // šlo o relaci '>', nenásledovalo '='
                        //debug("\nNačteno slovo: " + str + ", ukončeno bylo znakem '>'");
                        break;
                    }
                }
            }
            str.append(c);
            if (position >= source_length) {
                source_error = error_premature_end;
                return str.toString();
            }
            c = readChar();
            //debug("\n" + c);
        }
        position--;
        //debug("\nNačteno slovo: " + str);
        //vypisString(str);
        return str.toString();
    }

    private TAHLine[] readNode (NGTreeHead head) { // nacita z pole source obsah jednotl. atr. jedne sady vrcholu.
        //Pomoci fce readWord nacte slovo a z kontextu uhodne,
        // zda se jedna o jmeno atributu (pak zmeni index poradi nacitani) nebo zda jde o hodnotu atributu (dale musi
        // urcit, jestli jde o hodnotu prvni ci nikoliv)
        // navratova hodnota: sada atributu ve forme pole TAHLine
        TAHLine[] sada = new TAHLine[number_of_attributes];
        char c,d;
        int i = 0;
        int p;
        String str;
        TAHLine pom;
        int relation = TAHLine.RELATION_EQ; // pokud bude hodnota prvního atributu uvedena pozičně, znamená to s relací '='


        //debug("\nFce readNode; očekávaný počet atributů: " + pocet_atr);
        if (position >= source_length) return null;
        c = readChar(); // [
        if (c != '[') {
            debug("\nNGTree.readNode: Tenhle znak by měl být [: " + c);
        }
        while (true) {
            if (position >= source_length) {
                source_error = error_premature_end;
                return null;
            }
            str = readWord(head); // čtu jméno atributu nebo pozičně zapsanou hodnotu pozičního atributu
            c = readChar(); // dívám se, co je za tím - podle toho poznám, zda šlo o jméno či hodnotu atributu
            if (c=='=' || c=='!' || c=='~' || c=='<' || c=='>') {  // slo o jmeno atributu
                if (c=='=') { // šlo o relaci rovnítko
                    relation = TAHLine.RELATION_EQ;
                }
                else if (c=='~') { // dedek
                    d = readChar();
                    if (d!='=') { // toto by určitě nemělo nastat
                        debug("\nNGTree.readNode: problém při čtení rovnítka za vlnkou!");
                        return null;
                    }
                    relation = TAHLine.RELATION_REGEXP;                	
                }
                else if (c=='!') { // musím odstranit ještě to rovnítko za vykřičníkem
                    d = readChar();
                    if (d!='=') { // toto by určitě nemělo nastat
                        debug("\nNGTree.readNode: problém při čtení rovnítka za vykřičníkem!");
                        return null;
                    }
                    relation = TAHLine.RELATION_NEQ;
                }
                else if (c=='<') { // případně musím odstranit ještě to rovnítko za menšítkem
                    d = readChar();
                    //debug("\nNGTree.readNode: následující znak po menšítku je: " + d);
                    if (d!='=') { // bylo to jen menšítko
                        relation = TAHLine.RELATION_LT;
                        //debug("\nNGTree.readNode: šlo o znak '<'");
                        position--; // vracím přečtený znak zpět, abych ho mohl příště přečíst
                    }
                    else {
                        //debug("\nNGTree.readNode: šlo o dvojznak '<='");
                        relation = TAHLine.RELATION_LTEQ;
                    }
                }
                else if (c=='>') { // případně musím odstranit ještě to rovnítko za většítkem
                    d = readChar();
                    //debug("\nNGTree.readNode: následující znak po většítku je: " + d);
                    if (d!='=') { // bylo to jen většítko
                        relation = TAHLine.RELATION_GT;
                        //debug("\nNGTree.readNode: šlo o znak '>'");
                        position--; // vracím přečtený znak zpět, abych ho mohl příště přečíst
                    }
                    else {
                        //debug("\nNGTree.readNode: šlo o dvojznak '>='");
                        relation = TAHLine.RELATION_GTEQ;
                    }
                }

                p = head.getIndexOfAttribute(str);
                //debug ("\nIndex atributu " + str + " je " + p);
                if( p != -1 )   // posunuti v hlavicce
                    i = p;
                else debug("\nNGTree.readNode: problém při vyhledávání atributu v globální hlavičce!");
                continue;
            }

            // pridani hodnoty
            //debug ("\nPřidávám atribut " + str + " na pozici " + i + ", sada[" + i + "] = " + sada[i]);
            if( sada[i] == null ) {  // je dosud prazdny
                sada[i] = new TAHLine();
                sada[i].Value = str;
                sada[i].relation = relation;
                sada[i].Next = null;
            }
            else  {  // neni prazdny
                pom = sada[i];

                // nalezeni konce
                while (pom.Next != null)
                    pom = pom.Next;
                pom.Next = new TAHLine();
                pom = pom.Next;
                pom.Value = str;
                pom.relation = relation;
                pom.Next = null;
            }

            if( c == ',' ) { // dalsi atribut
                i++;
                relation = TAHLine.RELATION_EQ; // pokud bude hodnota dalšího atributu uvedena pozičně, znamená to s relací '='
            }

            if( c == '|' ) {
                // alternat. hodnota - bez akce
            };

            if( c == ']' ) { // konec jedné sady vrcholu
                return sada;
            }
        }
    } // readNode

/*    public int readTree (byte[] p_source, int start_position, int p_number_of_attributes) { // nacita ze source kompletni strom ve formatu fs bez hlavicky a reprezentuje ho ve strukture TNode. Pracuje rekurzivne
        // vrací počet přečtených znaků

        // nastavím globální proměnné:

        //debug("\nJsem v readTree(byte...)");
        position = start_position; // pozice ve vstupním poli při načítání stromu
        source_byte = p_source; // vstupní pole
        source_type = 1; // vstupní pole je v bajtech
        source_length = p_source.length;
        number_of_attributes = p_number_of_attributes;
        root = readTree();
        if (source_error != error_ok) {
            debug("\nNGTree.readTree: chyba při načítání stromu.");
            root = null;
        }
        source_byte = null; // už to nepotřebuji
        return position - start_position;
    }
*/

    /**
     * Reads a whole tree from p_source in FS format (without a head).
     * @param head a head belonging to the tree
     * @param p_source the source in FS format
     * @param start_position a position in p_source of the first character to be read
     * @param p_number_of_attributes number of attributes in the head
     * @return the root of the tree
     */
    public int readTree (NGTreeHead head, char[] p_source, int start_position, int p_number_of_attributes) { // nacita ze source kompletni strom ve formatu fs bez hlavicky a reprezentuje ho ve strukture TNode. Pracuje rekurzivne
        // vrací počet přečtených znaků

        // nastavím globální proměnné:
        //debug("\np_source = " + p_source + "\nstart_position = " + start_position + ", number_of_attributes = " + p_number_of_attributes);
        //debug("\nJsem v readTree(char...)");
        position = start_position; // pozice ve vstupním poli při načítání stromu
        source_char = p_source; // vstupní pole
        source_type = 2; // vstupní pole je v charech
        source_length = p_source.length;
        number_of_attributes = p_number_of_attributes;
        root = readTree(head);
        if (source_error != error_ok) {
            debug("\nNGTree.readTree: chyba při načítání stromu.");
            root = null;
        }
        source_char = null; // už to nepotřebuji
        return position - start_position;
    }

    private TNode readTree(NGTreeHead head) { // nacita ze source kompletni strom ve formatu fs bez hlavicky
        // a reprezentuje ho ve strukture TNode.
        // Za otce prvniho nalezeneho vrcholu urci null
        return readTree(head, null);
    }

    private TNode readTree (NGTreeHead head, TNode parent) { // nacita ze source kompletni strom ve formatu fs bez hlavicky
        // a reprezentuje ho ve strukture TNode. Pracuje rekurzivne.
        // Za otce prave nalezeneho vrcholu urci parent

        TAHLine[] v; // sada
        TValue values = null; // zacatek sad
        TValue val = null;
        TNode node, temp;
        TNode last = null;
        String hide_attr;
        char c;

        source_error = error_ok; // zatím žádná chyba

        if (position >= source_length) {
            return null; // jsem na konci vstupu - to nemusí být chyba, ale standardní konec vstupu
        }

        do {
            v = readNode(head);
            if (source_error != error_ok) { // vyskytla se chyba vstupu při načítání vrcholu
                return null;
            }

            if( values == null ) {
                val = new TValue();
                values = val;
            } else {
                val.Next = new TValue();
                val = val.Next;
            }

            val.AHTable = v;
            val.Next = null;
            if (position >= source_length) { // konec vstupu - opět nemusí být chyba
                position ++; // o jedna zvýším, neboť po konci cyklu o jedna snížím
                //debug("\nJsem na konci vstupu.");
                break;
            }
            c = readChar();
        } while (c == '|'); // dokud neprectes vsechny sady jednoho vrcholu

        position--; // znak zpet

        // novy vrchol
        node = new TNode();
        node.values = values;
        node.brother = null;
        node.first_son = null;
        node.parent = parent;
        node.setX(0);
        node.setY(0);
        number_of_nodes++;
        //debug ("\npočet vrcholů: " + number_of_nodes);
        // nastavím důležité atributy
        if (head.W != -1 && values.AHTable[head.W] != null) { // ex. num. atr. sentord (u analytických ord)
            //debug("\nhead.W = " + head.W);
            Integer I;
            try {
                I = new Integer (values.AHTable[head.W].Value);
            }
            catch (NumberFormatException e) {
                I = new Integer(0);
                // zakomentováno kvůli referenčním odkazům - ty u těchto atributů generovaly chybovou hlášku: debug ("\nNGTree.readTree: Chyba při čtení numerického atributu @W! " + e);
            }
            node.poradi_W = I.intValue();
        } else
            node.poradi_W = number_of_nodes - 1;

        if (head.N != -1 && values.AHTable[head.N] != null) { // ex. num. atr. deepord (u analytických opět ord)
            Float D;
            try {
                D = new Float (values.AHTable[head.N].Value);
            }
            catch (NumberFormatException e) {
                D = new Float(-1); // -1 je signálem pro Comparator, že musí udělat něco jinak - asi jde o referenční odkaz, když jsem tady
                // výpis hlášky zakomentován kvůli referenčním odkazům - ty u těchto atributů generovaly chybovou hlášku: debug ("\nNGTree.readTree: Chyba při čtení numerického atributu @N!" + e);
            }
            node.poradi_N = D.floatValue();
        }
        else { // atribut určující pořadí uzlů ve stromu není nastaven
            node.poradi_N = number_of_nodes - 1;
        }

        if (head.H != -1 && values.AHTable[head.H] != null) { // ex. skrývající atribut (TR)
            if (values.AHTable[head.H] != null) {
                hide_attr = values.AHTable[head.H].Value;
                if (hide_attr.equals("") || hide_attr.equals("0") || hide_attr.equals("false")) {
                    node.skryvany = false;
                }
                else {
                  number_of_hidden++;
                  node.skryvany = true;
                }
            }
            else {
              node.skryvany = false;
            }
        } else // neexistuje skrývací atribut
            node.skryvany = false; // u analytických stromů se nic neskrývá

        if (position+1 >= source_length) {
            return node;
        }
        if (readChar() == '(' ) { // začíná zde podstrom
            if (source_error != error_ok) {
                return null;
            }
            do {
                if (position+1 >= source_length) {
                    source_error = error_premature_end;
                    return node;
                }
                temp = readTree(head, node);
                if (last != null)
                    last.brother = temp;
                else
                    node.first_son = temp;
                last = temp;

            } while (readChar() != ')' );
        } else {
            position--; // přečtu si ten znak znovu
        }

        return node;

    } // readTree

    /**
     * It increases number of attributes at all nodes by number.
     * The new attributes are added at the end of the list of attributes.
     * Values of the new attributes are set empty ("").
     * The head of the tree is not changed (must be changed separately).
     * @param number number of attributes to be added
     */
    public void addAttributes(int number) {
        if (number <= 0) return;
        addAttributes(root, number);
        number_of_attributes += number;
    }

    private void addAttributes (TNode n, int number) { // projde strom od vrcholu n do hloubky a u každého přidá number atributů
        if (n == null)
            return;

        n.addAttributes(number);
        addAttributes (n.first_son, number);
        addAttributes (n.brother, number);
    }

    /**
     * It sets the matching meta tag at the matching nodes of the tree.
     * The matching meta tag marks matching nodes after the tree is saved e.g. to local disc.
     * @param head a head belonging to the tree
     */
    public void setMatchingMetaTags(NGTreeHead head) {
        int index_matching_node = head.getIndexOfAttribute("NG_matching_node");
        int index_matching_edge = head.getIndexOfAttribute("NG_matching_edge");
        // System.out.println("Indexy matchujicich meta tagu jsou: " + index_matching_node + ", " + index_matching_edge);
        setMatchingMetaTags(root, index_matching_node, index_matching_edge);
    }

    private void setMatchingMetaTags(TNode n, int index_matching_node, int index_matching_edge) {
        if (n == null)
            return;

        if (n.matching_node || n.matching_edge) { // pokud u tohoto vrcholu je potřeba nastavovat jeden z matchujících atributů
            TValue values = n.values;
            while (values != null) { // nastavím to ve všech sadách, ale nevím teď, jestli je to korektní
                if (n.matching_node) {
                    if (values.AHTable[index_matching_node] == null) {
                        values.AHTable[index_matching_node] = new TAHLine();
                    }
                    values.AHTable[index_matching_node].Value = "true";
                }
                if (n.matching_edge) {
                    if (values.AHTable[index_matching_edge] == null) {
                        values.AHTable[index_matching_edge] = new TAHLine();
                    }
                    values.AHTable[index_matching_edge].Value = "true";
                }
                values = values.Next;
            }
        }

        setMatchingMetaTags (n.first_son, index_matching_node, index_matching_edge);
        setMatchingMetaTags (n.brother, index_matching_node, index_matching_edge);
    }

    /**
     * Returns the actually selected node.
     * @return the selected node
     */
    public TNode getChosenNode() {
        return chosen_node;
    }

    /**
     * Returns depth-first order of the chosen node, counted from 1. If count_hidden is true, hidden nodes are counted as normal nodes.
     * If count_hidden is false, hidden nodes are not counted; if the chosen node is hidden, the order of its nearest non-hidden predecessor is returned.
     * @param count_hidden says if hidden nodes should be counted
     * @return -1 if not found; a positive value (depth-first order counted from 1) if found; 0 if the chosen node is hidden, count_hidden is false and
     * there is no non-hidden node is on the path from it to the root
     */
    public int getChosenNodeDepthOrder(boolean count_hidden) { // count_hidden určuje, zda se budou počítat skryté uzly
        // funkce vrátí chybně nulu, pokud count_hidden je false, kořen stromu je skrytý, jeho první syn je také skrytý
        // a vybraný neskrytý uzel je někde v podstromu toho kořene; to by se ovšem nemělo stát
        if (count_hidden) {
            return chosen_node_depth_order; // vrátím předpočítanou hodnotu používanou v programu
        }
        else { // musím spočítat, kolikátý je, když se nepočítají skryté vrcholy - to je pro substituci proměnné externího příkazu
            if (root == null) { // strom je prázdný
                return -1; // nenalezeno
            }
            int order = getChosenNodeDepthOrderWithoutHidden(root,0);
            if (order <= 0) { // bylo nalezeno, což je signalizováno zápornou hodnotou, popřípadě nulou u skrytých nepočítaných uzlů
                return -order;
            }
            return -1; // nebylo nalezeno, vrátím -1
        }
    }

    private int getChosenNodeDepthOrderWithoutHidden(TNode node, int order_counter) {
        // rekurzivní funkce, hledá pořadí vybraného uzlu při průchodu do hloubky, skryté uzly nepočítá
        // pokud je vybraný skrytý uzel, jeho pořadí je totéž jako pořadí jeho nejbližšího neskrytého předchůdce
        // vrací -x, pokud byl vybraný uzel v podstromu node (včetně) nalezen, x je jeho pořadí v průchodu do hloubky
        // vrací 0, pokud byl vybraný uzel v podstromu node (včetně) nalezen, ale byl to skrytý uzel
        // vrací x, pokud vybraný uzel nebyl v podstromu node (včetně) nalezen; x je dosavadní počet prošlých uzlů
        if (node == null)
            return order_counter ; // ukončující podmínka rekurze, čítač nezměněn
        // zpracování tohoto uzlu
        if (!node.skryvany) order_counter++; // skryté vrcholy se nepočítají
        if (node == chosen_node  ) { // found the chosen node
            if (node.skryvany) { // 0 znamená, že bylo nalezeno, ale ve skrytém uzlu
                return 0;
            }
            return -order_counter; // záporná hodnota signalizuje, že už bylo nalezeno (v neskrytém uzlu)
        }
        // rekurzívní volání
        int order_1 = getChosenNodeDepthOrderWithoutHidden(node.first_son,order_counter);
        if (order_1 < 0) return order_1; // uzel byl nalezen v podstromu prvního syna včetně
        if (order_1 == 0) { // uzel byl nalezen v podstromu prvního syna včetně, ale skrytý
            if (node.skryvany) { // i tento uzel je skrývaný
                return 0; // předám nulu výš
            }
            else { // tento není skrývaný
                return -order_counter; // vrátím pořadí tohoto uzlu jako výsledné pořadí
            }
        }
        int order_2 = getChosenNodeDepthOrderWithoutHidden(node.brother, order_1);
        return order_2;
    } // getChosenNodeDepthOrderWithoutHidden

    /**
     * Marks a node with given order as selected.
     * @param order depth-first order of the node to be selected. The root has order 1.
     */
    public void setChosenNodeByDepthOrder(int order) { // vrchol v pořadí 'order' při průchodu do hloubky se označí jako vybraný
        poradi_prohledavaneho_vrcholu = 0; // začneme od nuly, kořen je 1
        if (order <= 0 || order >= number_of_nodes) { // nebude označen žádný vrchol
            chosen_node = null;
            //chosen_set = null;
            chosen_node_depth_order = 0;
        }
        setChosenNodeByDepthOrder (root, order); // rekurzivně projdu strom
        flag = true; // přepočítám strom pro případ, že byly zobrazeny alternativní sady atributů jen u dříve vybraného vrcholu
    }

    private void setChosenNodeByDepthOrder (TNode n, int order) { // projde strom od vrcholu n do hloubky a hledá vrchol v pořadí 'order'-tý
        // nastavi promennou vybrany
        if (n == null)
            return;

        if (poradi_prohledavaneho_vrcholu == -1) return; // už byl jinde v rekurzi nalezen, jen ukončuji rekurzi

        poradi_prohledavaneho_vrcholu++; // jsem na dalším vrcholu (v akci prohledávání stromu do hloubky)
        // je 'n' on ?
        if (order == poradi_prohledavaneho_vrcholu) {
            chosen_node = n;
            //chosen_set = n.values;
            chosen_node_depth_order = poradi_prohledavaneho_vrcholu;
            poradi_nalezeneho_vrcholu = poradi_prohledavaneho_vrcholu;
            poradi_prohledavaneho_vrcholu = -1; // signál ke konci rekurze
            return;
        }

        setChosenNodeByDepthOrder (n.first_son, order);
        setChosenNodeByDepthOrder (n.brother, order);
    }

    /**
     * Marks a node at given coordinates as selected. It goes through the tree depth-first and if a node
     * is found close enought to the given coordinates, it is marked as selected.
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     * @param dosah_mysi a maximum allowed distance of the node from the coordinates if the node is to be selected
     * @param allow_hidden controls if a hidden node can be selected
     * @return depth-first order of the selected node counted from 1; 0 if not found
     */
    public int setChosenByPosition (int x, int y, int dosah_mysi, boolean allow_hidden) { // projde strom od korene do hloubky a hledá vrchol dostatečně blízko souřadnicím x,y
        // allow_hidden určuje, zda smí vybrat skrytý vrchol
        // vrátí pořadí nalezeného vrcholu při průchodu do hloubky; 0 znamená nenalezen
        poradi_prohledavaneho_vrcholu = 0; // začneme od nuly, kořen je 1
        poradi_nalezeneho_vrcholu = 0; // zatim nenalezen
        nastav_vrchol (root, x, y, dosah_mysi, allow_hidden); // rekurzivně projdu strom
        flag = true; // přepočítám strom pro případ, že byly zobrazeny alternativní sady atributů jen u dříve vybraného vrcholu
        return poradi_nalezeneho_vrcholu;
    }

    /**
     * Sets nodes matching a query according to a given list of matching nodes. The whole lists consists of partial lists
     * of nodes matching individul query trees. The partial lists are separated with MATCHING_NODES_TREES_DELIMITER.
     * Pairs of matching nodes are separated with MATCHING_NODES_DELIMITER.
     * Matching nodes in each pair are separated with MATCHING_NODES_QUERY_DELIMITER.
     * In the pair, the first number is a depth-first order of a result tree node, the second number is a depth-first order of its matching query node, all counted from 1.
     * The result tree nodes in the partial lists must be in ascending order.
     * @param s list of matching nodes
     */
    public void setMatchingNodes(String s) { // nastaví vrcholy matchující s dotazem podle řetězce s obsahujícího jejich seznamy (po jednotlivých stromech dotazu) ve vzestupném pořadí (v průchodu do hloubky)
        // seznamy vrcholů matchujících s jednotlivými stromy dotazu jsou oddělené MATCHING_NODES_TREES_DELIMITER
        // jednotlivé matchující vrcholy jsou odděleny MATCHING_NODES_DELIMITER
        // za každým matchujícím vrcholem je za znakem MATCHING_NODES_QUERY_DELIMITER uvedeno pořadí příslušného matchujícího vrcholu dotazu
        // pořadí vrcholů v jednotlivých seznamech musí být vzestupné!
        // zbylé vrcholy stromu označí za nematchující

        unsetMatchingNodes(root); // nejprve označí všechny vrcholy a hrany za nematchující
        // následně projdu vstupní řetězec po seznamech vrcholů matchujících s jednotlivými stromy dotazu a pro každý z nich zavolám funkci setMatchingNodesOneTree
        StringBuffer one_tree_list = new StringBuffer();
        int length = s.length();
        boolean copy = true; // přepínač mezi režimem přeskakování či kopírování vstupu
        char c;
        for (int position = 0; position < length; position++) {
            c = s.charAt(position);
            switch (c) {
                case MATCHING_NODES_TREES_DELIMITER:
                    matchujici_vrcholy = one_tree_list.toString(); // nastavím globální proměnné
                    poradi_prohledavaneho_vrcholu = -1; // začneme od nuly, kořen bude 0
                    poradi_hledaneho_vrcholu = -1; // zde se bude uchovávat pořadí hledaného vrcholu - zatím neurčeno
                    setMatchingNodesOneTree(root,false); // označím vrcholy a hrany matchující s jedním stromem dotazu; začnu od roota, otec nepatří mezi vrcholy matchující s dotazem nebo do tranzitivní hrany
                    one_tree_list = new StringBuffer(); // vynuluji proměnnou pro seznam vrcholů matchujících s dalším stromem dotazu
                    copy = true;
                break;
                case MATCHING_NODES_DELIMITER:
                    one_tree_list.append(c);
                    copy = true;
                break;
                case MATCHING_NODES_QUERY_DELIMITER:
                    copy = false;
                break;
                default:
                    if (copy) {
                        one_tree_list.append(c);
                    }
                break;
            }
        }
        // nyní zpracuji poslední vytvořený seznam vrcholů matchujících s jedním stromem dotazu
        matchujici_vrcholy = one_tree_list.toString(); // nastavím globální proměnné
        poradi_prohledavaneho_vrcholu = -1; // začneme od nuly, kořen bude 0
        poradi_hledaneho_vrcholu = -1; // zde se bude uchovávat pořadí hledaného vrcholu - zatím neurčeno
        setMatchingNodesOneTree(root,false); // označím vrcholy a hrany matchující s jedním stromem dotazu; začnu od roota, otec nepatří mezi vrcholy matchující s dotazem nebo do tranzitivní hrany
    } // setMatchingNodes

    private void unsetMatchingNodes(TNode n) {
        if (n == null) { // v této větvi už není co prohledávat
            return;
        }
        n.matching_node = false;
        n.matching_edge = false;
        unsetMatchingNodes(n.first_son);
        unsetMatchingNodes(n.brother);
    }

    private boolean setMatchingNodesOneTree(TNode n, boolean parent_matching) { // nastaví vrcholy matchující s dotazem podle řetězce s obsahujícího jejich vzestupné pořadí (v průchodu do hloubky) oddělené čárkou
        // pořadí musí být vzestupné!
        // jakmile nalezne všechny matchující vrcholy, končí
        // parent_matching určuje, jestli předek patří mezi vrcholy matchující s dotazem nebo alespoň do tranzitivní hrany
        // vrací TRUE, pokud první vrchol podstromu nebo někdo z jeho bratrů patří mezi vrcholy matchující s dotazem, případně do tranzitivní hrany,
        // tj. pokud se v podstromu předka vyskytl vrchol matchující s dotazem
        // jinak vrací FALSE
        // používá globální proměnnou matchujici_vrcholy jako zdroj zbyvajících matchujících vrcholů
        // používá globální proměnnou poradi_prohledavaneho_vrcholu pro uchování pořadí prohledávaného vrcholu
        // používá globální proměnnou poradi_hledaneho_vrcholu pro uchování pořadí hledaného vrcholu

        //debug("\nNGTree: setMatchingNodesOneTree: Entering this function.");

        if (n == null) { // v této větvi už není co prohledávat
            //debug("\nNGTree: setMatchingNodes: Leaving this function (the tree is null).");
            return false;
        }

        //debug("\nNGTree: setMatchingNodesOneTree: setting matching nodes from list: " + matchujici_vrcholy);
        boolean matching_node = false; // ukazuje, zda tento vrchol matchoval díky tomuto seznamu matchujících vrcholů

        if (poradi_hledaneho_vrcholu == -1) { // aktuálně hledané číslo neurčeno
            if (matchujici_vrcholy != null) { // je ještě co hledat
                int next_position = matchujici_vrcholy.indexOf(MATCHING_NODES_DELIMITER); // zjistím pozici dalšího čísla
                try {
                    // nejprve oddělím první číslo
                    String match_vrchol;
                    if (next_position > 0) {
                        match_vrchol = matchujici_vrcholy.substring(0,next_position);
                    }
                    else {
                        match_vrchol = matchujici_vrcholy;
                    }
                    // to pak převedu na číslo
                    poradi_hledaneho_vrcholu = Integer.parseInt(match_vrchol); // zjistím další číslo (první ve zbývajícím seznamu)
                }
                catch (Exception e) {
                    //debug("\nNGTree: setMatchingNodes: Chyba při konverzi řetězce na číslo!");
                    poradi_hledaneho_vrcholu = -2; // takže odteď už budu všechny vrcholy označovat jako nematchující
                }
                if (next_position == -1) { // jednalo se o poslední číslo
                    matchujici_vrcholy = null;
                }
                else { // následují další čísla
                    matchujici_vrcholy = matchujici_vrcholy.substring(next_position+1);
                }
                //debug("\nNGTree: setMatchingNodes: nyní hledám vrchol č. " + poradi_hledaneho_vrcholu + ", zbytek vrcholů je: " + matchujici_vrcholy);
            }
            else {
                return false;
            }
        }

        poradi_prohledavaneho_vrcholu++; // jsem na dalším vrcholu (v akci prohledávání stromu do hloubky)
        //debug("\nNGTree: setMatchingNodes: nyní jsem na vrcholu č. " + poradi_prohledavaneho_vrcholu + ", hledám vrchol č. " + poradi_hledaneho_vrcholu);

        // nyní zkontroluji, jestli jsem už našel ten správný uzel
        if (poradi_hledaneho_vrcholu == poradi_prohledavaneho_vrcholu) { // označím vrchol jako matchující s dotazem
            n.matching_node = true;
            matching_node = true;
            //debug("\nZaškrtnut vrchol č. " + poradi_hledaneho_vrcholu + " s lemma = " + n.Values.AHTable[0].Value);
            poradi_hledaneho_vrcholu = -1; // další hledaný vrchol je teprve potřeba určit
        }
        /*else { // není to vrchol matchující s dotazem
            n.matching_node = false;
        }*/


        boolean matching_subtree;
        // zavolám rekurzi na podstromy
        matching_subtree = setMatchingNodesOneTree(n.first_son, parent_matching||matching_node);
        boolean ret_value = setMatchingNodesOneTree(n.brother, parent_matching) || matching_subtree;
        if (parent_matching && (matching_subtree || matching_node)) {
            n.matching_edge = true; // rodičovská hrana patří do části matchující s dotazem
        }
        /*else {
            n.matching_edge = false; // rodičovská hrana nepatří do části matchující s dotazem
        }*/

        //debug("\nNGTree: setMatchingNodes: Leaving this function (the subtree and the brothers has been proceeded).");
        return ret_value||matching_node;

    } // setMatchingNodesOneTree

    /**
     * Searches for the depth-first order of the node in the tree that matches the root of the first tree of the query, counted from 1. If count_hidden is true, hidden nodes are counted as normal nodes.
     * If count_hidden is false, hidden nodes are not counted; if the matching node is hidden, the order of its nearest non-hidden predecessor is returned.
     * @param count_hidden says if hidden nodes should be counted
     * @return the depth-first order of the node (counted from 1) if found; -1 if not found; returns 0 if the matching node is hidden, count_hidden is false and
     * there is no non-hidden node is on the path from it to the root
     */
    public int getFirstMatchingNodeDepthOrder(boolean count_hidden) {
        // funkce vrátí chybně nulu, pokud count_hidden je false, kořen stromu je skrytý, jeho první syn je také skrytý
        // a matchující neskrytý uzel je někde v podstromu toho kořene; to by se ovšem nemělo stát
        if (root == null) {
            return -1;
        }
        int order = getFirstMatchingNodeDepthOrder(root,0,count_hidden);
        if (order <= 0) { // bylo nalezeno, což je signalizováno zápornou hodnotou (nebo nulou pro nepočítané skryté vrcholy)
            return -order;
        }
        return -1; // nebylo nalezeno, vrátím -1
    } // getFirstMatchingNodeDepthOrder

    private int getFirstMatchingNodeDepthOrder(TNode node, int order_counter, boolean count_hidden) {
        // rekurzivní funkce, hledá pořadí (při průchodu do hloubky) uzlu matchujícího s kořenem prvního stromu dotazu, skryté uzly počítá podle proměnné count_hidden
        // pokud matchující uzel je skrytý, jeho pořadí je totéž jako pořadí jeho nejbližšího neskrytého předchůdce
        // vrací -x, pokud byl matchující uzel v podstromu node (včetně) nalezen, x je jeho pořadí v průchodu do hloubky, počítáno od 1
        // vrací 0, pokud byl matchující uzel v podstromu node (včetně) nalezen, ale byl to skrytý uzel
        // vrací x, pokud matchující uzel nebyl v podstromu node (včetně) nalezen; x je dosavadní počet prošlých uzlů
        if (node == null)
            return order_counter ; // ukončující podmínka rekurze, čítač nezměněn
        // zpracování tohoto uzlu
        if (!node.skryvany || count_hidden) order_counter++; // skryté vrcholy se počítají jen pokud je tak řečeno ve volání funkce
        if (node.matching_node) { // found a matching node
            if (node.skryvany && !count_hidden) {
                return 0; // 0 znamená, že bylo nalezeno, ale ve skrytém uzlu, který se nemá počítat
            }
            return -order_counter; // záporná hodnota signalizuje, že už bylo nalezeno
        }
        // rekurzívní volání
        int order_1 = getFirstMatchingNodeDepthOrder(node.first_son,order_counter, count_hidden);
        if (order_1 < 0) return order_1; // uzel už byl nalezen
        if (order_1 == 0) { // uzel byl nalezen v podstromu prvního syna včetně, ale skrytý
            if (node.skryvany && !count_hidden) { // i tento uzel je skrývaný
                return 0; // předám nulu výš
            }
            else { // tento není skrývaný
                return -order_counter; // vrátím pořadí tohoto uzlu jako výsledné pořadí
            }
        }
        int order_2 = getFirstMatchingNodeDepthOrder(node.brother, order_1, count_hidden);
        return order_2;
    } // getFirstMatchingNodeDepthOrder

    private void nastav_vrchol (TNode n, int x, int y, int dosah_mysi, boolean allow_hidden) { // projde strom od vrcholu n do hloubky a hledá vrchol dostatečně blízko souřadnicím x,y
      // allow_hidden určuje, zda smí vybrat skrytý vrchol
        // nastavi promennou vybrany
        if (n == null)
            return;

        poradi_prohledavaneho_vrcholu++; // jsem na dalším vrcholu (v akci prohledávání stromu)
        // je 'n' on ?
        if (allow_hidden || n.skryvany==false) { // skryty vrchol se vezme v uvahu jen kdyz je nastaveno allow_hidden na true (tj. kdyz se skryte vrcholy zobrazuji)
          if (Math.abs(n.getX() - x) < dosah_mysi & Math.abs(n.getY() - y) < dosah_mysi) {
              chosen_node = n;
              //chosen_set = n.values;
              chosen_node_depth_order = poradi_prohledavaneho_vrcholu;
              poradi_nalezeneho_vrcholu = poradi_prohledavaneho_vrcholu;
              return;
          }
        }
        nastav_vrchol (n.first_son, x, y, dosah_mysi, allow_hidden);
        nastav_vrchol (n.brother, x, y, dosah_mysi, allow_hidden);
    }


    private void getSentence (NGTreeHead head, String []field, float [] indexy) {
        sestav_vetu(head, root, field, indexy);
    }

/*
	private void myCopy(char[] dst, int dst_start, char[] src, int src_len) {
		//debug("\n");
		for (int i=0; i<src_len; i++) {
			dst[dst_start+i] = src[i];
			//debug(" " + i + "-" + src[i]);
		}
	}

	private void vypis(char [] p) {
		String p2 = new String(p);
		debug("\nSlovo: " + p2);
		for (int i=0; i<p.length; i++) {
			debug("\n   " + i + " - " + p[i]);
		}
	}

	private String myAppend(String a, String b) {
		char[] a_chars = a.toCharArray();
		char[] b_chars = b.toCharArray();
		//vypis(b_chars);
		int a_len = a.length();
		int b_len = b.length();
		char [] c_chars = new char[a_len + b_len];
		myCopy (c_chars, 0, a_chars, a_len);
		myCopy (c_chars, a_len, b_chars, b_len);
		return new String(c_chars);
	}
*/
    private boolean spaceBetween(String text1, String text2, int text2_position, int number_of_nodes) { // vrací true pokud mezi danými slovy bývá ve větě mezera; jinak false
        // neřeší uvozovky a apostrofy, v úvahu bere jen poslední písmeno prvního slova a první písmeno druhého slova
	    if (text1 == null || text2 == null) {
		    return false;
    	}
        char c1 = text1.charAt(text1.length() - 1); // vezmu poslední znak prvniho slova
        switch (c1) {
            case '(': return false;
            case '[': return false;
            case '{': return false;
        }
        char c2 = text2.charAt(0); // vezmu první znak druheho slova
	    switch (c2) {
		    case ',': return false;
		    case '.': return false;
		    case ':': return false;
		    case ';': return false;
		    case '!': return false;
		    case '?': if (text2_position == number_of_nodes-1) {
                          return false;
                      }
                      else {
                          return true; // uprostred vety pred vykricnikem necham mezeru - hlavne kvuli korpusu CAK
                      }
		    case ')': return false;
		    case ']': return false;
		    case '}': return false;
		    default: return true;
	    }
    } // spaceBefore

    /**
     * Assembles and returns the sentence belonging to the tree.
     * @param head a head belonging to the tree
     * @return a sentence belonging to the tree
     */
    public String getSentenceString (NGTreeHead head) { // vrátí větu jakožto řetězec
        String [] veta = new String[number_of_nodes]; // vytvoření místa pro novou větu
        float [] indexy = new float[number_of_nodes]; // vytvoreni mista pro indexy slov vety - pro zatridovani
        for (int j = 0; j < number_of_nodes; j++) { // vše vynuluji
            veta[j]=null;
            indexy[j]=-1;
        }
        //debug("\nNumber of nodes int the sentence is: " + number_of_nodes);
        getSentence(head, veta, indexy); // získání jednotlivých slov věty (do pole veta)
        StringBuffer vet = new StringBuffer("");
        for (int j = 0; j < number_of_nodes; j++) {
			if (veta[j]!=null) {
				if (j == 0) {
					vet.append(veta[j]);
					//debug("\nInserting String '" + veta[j] + "' to String '" + vet + "' at position " + vet.length());
				}
				else {
					//debug("\nInserting String '" + veta[j] + "' to String '" + vet + "' at position " + vet.length());
                    if (j != 0) { // nejde o první slovo ve větě
                        if (spaceBetween(veta[j-1],veta[j],j,number_of_nodes)) { // má se udělat mezera před slovem
                            vet.append(" "); //vet = myAppend(vet, " ");
                        }
                    }
                    vet.append(veta[j]); //vet = myAppend(vet, veta[j]);
					// Tady to zřejmě samo rozpozná arabský řetězec a přidává to doprava
					//vet.insert(vet.length(), veta[j].toCharArray());
					//debug("\n - the result is: " + vet);
				}
			}
        }
        return vet.toString();
    }

    //private void vypisString (String s) { // vypíše číselně obsah stringu znak po znaku
    //    for (int i=0; i<s.length(); i++) {
    //		debug ("\nznak " + s.charAt(i) + " s kódem " + (int)s.charAt(i));
    //	}
    //}

    private int najdiMisto(float [] indexy, float index) { // najde misto, kam ve vzestupne utridenem poli nezapornych cisel patri index; -1 znamena neobsazene misto
        int length = indexy.length;
        float value;
        for (int i=0; i<length; i++) { // hledam spravne misto pres cele pole
            value = indexy[i];
            if (value == -1) return i; // nasel jsem volne misto
            if (value > index) return i; // dal uz jsou jen vetsi prvky
        }
        debug("\nNGClient.najdiMisto: Problem pri sestavovani vety!");
        return length-1; // vracim posledni prvek pole, ale sem by to nemelo nikdy dojit
    }

    private void zatridSlovo(String [] veta, String slovo, int misto) { // zatridi slovo do vety na dane misto
        int length=veta.length;
        for (int i=length-1; i>misto; i--) {
            veta[i]=veta[i-1];
        }
        veta[misto]=slovo;
    }

    private void zatridIndex(float [] indexy, float index, int misto) { // zatridi index do pole indexu na dane misto
        int length=indexy.length;
        for (int i=length-1; i>misto; i--) {
            indexy[i]=indexy[i-1];
        }
        indexy[misto]=index;
    }

    private void zatridSlovoAIndex(String [] veta, float [] indexy, String slovo, float index) { // zatridi jedno slovo do pole veta podle indexu; zatridi i prislusny index
        int misto = najdiMisto(indexy, index); // najdu misto v poli, kam patri index (a potazmo i slovo)
        zatridSlovo(veta, slovo, misto); // zatridi na spravne misto slovo
        zatridIndex(indexy, index, misto); // zatridi na spravne misto slovo
        return;
    }

    private void sestav_vetu (NGTreeHead head, TNode n, String [] veta, float [] indexy) { // pole veta naplni slovy vety; kazdy prvek = jedno slovo
        if (n == null)
            return;
        float index = n.poradi_W;
        if (index >= 0.0 && index <= number_of_nodes) {
            if (head.V >=0) { // je-li vůbec v hlavičce definován slovní atribut (asi se mělo ověřit už dřív, ale nejpozději je nutno tady)
                if (n.values.AHTable[head.V] != null) { // slovo neni prazdne
                    String slovo = n.values.AHTable[head.V].Value;
                    if (slovo != null) {
                        if (slovo.length()>0) {
                            //vypisString(slovo);
                            zatridSlovoAIndex(veta, indexy, slovo, index);
                        }
                    }
                }
            }
        }
        sestav_vetu(head, n.first_son, veta, indexy);
        sestav_vetu(head, n.brother, veta, indexy);
    } // sestav_vetu

    /**
     * Returns an id of the tree. It is a value of attribute id.
     * @param head a head belonging to the tree
     * @return id of the tree
     */
    public String getId(NGTreeHead head) { // it returns id of the tree
        int attr_index = head.getIndexOfAttribute("id");
        String id=root.getValue(0,attr_index,0);
        //debug("\nNGTree.getId() vrací hodnotu "+ id);
        if (id == null) id="";
        return id;
    }

    /**
     * Returns a new array of all nodes in the tree.
     * @return an array of all nodes in the tree
     */
    public TNode[] getNodesArray() {
        int number_of_nodes = getNumberOfNodes();
        TNode[] array = new TNode[number_of_nodes];
        int number_of_added_nodes = getNodesArray(array, root, 0); // rekurzivně projdu strom a naplním to pole od nulté pozice
        if (number_of_added_nodes != number_of_nodes) { // to by nemělo nastat
            debug("\nNGTree.getNodesArray(): Nesouhlasí počet uzlů stromu a počet uzlů vložených do pole (" + number_of_nodes + ", " + number_of_added_nodes + ")!");
        }
        return array;
    }

    private int getNodesArray(TNode[] array, TNode node, int position) { // rekurzivně projdu strom od vrcholu n do hloubky,
        // plním pole array dalšími a dalšími nalezenými vrcholy od pozice position, vracím počet vložených vrcholů
        if (node == null) {
            return 0;
        }
        int number;
        array[position] = node; // zařadím aktuální vrchol
        int total_number = 1; // celkem jsem tedy zatím zařadil jeden vrchol
        position++; // případný další vrchol se zařadí o pozici dál
        number = getNodesArray(array, node.first_son, position); // projdu rekurzivně do hloubky potomky
        position += number; // o tolik posunu pozici pro další vkládání
        total_number += number; // tolik vrcholů jsem v tomto rekurzivním volání přidal
        number = getNodesArray(array, node.brother, position); // projdu rekurzivně také všechny bratry
        total_number += number; // a tolik vrcholů jsem nyní přidal

        return total_number;
    }

    /**
     * Returns the distance between the node and the root of the tree. The
     * depth of the root is 0.
     * @param node a node in the tree
     * @return the depth of the node
     */
    public int getDepth(TNode node) {
        if (node==root) return 0;
        if (node==null) return -1;
        return (1 + getDepth(node.parent));
    }

    /**
     * Searches for a node in the tree that has a given id.
     * @param id_index an index of id-attribute in the head of the tree
     * @param value a given id
     * @return the node with the given id or null if not found
     */
    public TNode findNodeById(int id_index, String value) {
        TNode node = findNodeById(root, id_index, value);
        return node;
    } // findNodeById

    private TNode findNodeById(TNode node, int id_index, String value) {
        if (node == null)
            return null; // ukončující podmínka rekurze
        // zpracování tohoto uzlu
        String node_value = node.getValue(0, id_index, 0); // chci první hodnotu atributu s indexem attr_index v první sadě (ID pro koreference jsou jen první hodnoty první sady)
        if (node_value != null) {
            if (value.length() > 0) { // tento uzel má identifikátor
                if (value.compareTo(node_value.trim())==0) { // hodnoty se shodují - nalezl jsem koncový uzel
                    return node;
                }
            }
        }
        // rekurzívní volání
        TNode node1 = findNodeById(node.first_son, id_index, value);
        if (node1 != null) return node1; // koncový uzel už byl nalezen
        node1 = findNodeById(node.brother, id_index, value);
        return node1;
    }

} // class NGTree

