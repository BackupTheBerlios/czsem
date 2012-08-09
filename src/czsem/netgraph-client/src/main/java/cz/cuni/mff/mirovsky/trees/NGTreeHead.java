package cz.cuni.mff.mirovsky.trees;

import javax.swing.DefaultListModel;

import cz.cuni.mff.mirovsky.*;
/**
 * A class representing a head of a forest. It keeps information about the attributes in the forest.
 */
public class NGTreeHead extends Object {

    /**
     * a name of meta-attribute _name
     */
    public final static String META_ATTR_NODE_NAME = "_name";
    /**
     * a name of meta-attribute _transitive
     */
    public final static String META_ATTR_TRANSITIVE = "_transitive";
    /**
     * value true of meta-attribute _transitive
     */
    public final static String META_ATTR_TRANSITIVE_TRUE = "true";
    /**
     * value exclusive of meta-attribute _transitive
     */
    public final static String META_ATTR_TRANSITIVE_EXCLUSIVE = "exclusive";
    /**
     * a name of meta-attribute _optional
     */
    public final static String META_ATTR_OPTIONAL = "_optional";
    /**
     * value true of meta-attribute _optional
     */
    public final static String META_ATTR_OPTIONAL_TRUE = "true";
    /**
     * a name of meta-attribute _#occurrences
     */
    public final static String META_ATTR_OCCURRENCES = "_#occurrences";
    /**
     * an unspecified character order for references. The reference is to the whole value then.
     */
    public final static String NODE_REFERENCE_CHARACTER_ORDER_UNSPECIFIED = "---";
    /**
     * a character that starts a reference
     */
    public final static String NODE_REFERENCE_START = "{";
    /**
     * a character that ends a reference
     */
    public final static String NODE_REFERENCE_END = "}";
    /**
     * a character that separates first two parts of a reference
     */
    public final static String NODE_REFERENCE_ATTR_NAME_DELIMITER = ".";
    /**
     * a character that separates second two parts of a reference
     */
    public final static String NODE_REFERENCE_CHARACTER_ORDER_DELIMITER = ".";
    
    private DefaultListModel attributes; // list of attributes

    /**
     * an index of numerical attribute in the head (counted from zero). This attribute controls the order of nodes in each tree from left to right.
     */
	public int N;  // index num. atr. v hlavičce (pořadí uzlů v kreslení stromu)
    /**
     * an index of value attribute in the head (counted from zero). This attribute values the sentence is assembled from.
     */
	public int V;  // index orig. atr. v hlavičce (slova pro výpis věty)
    /**
     * an index of word order attribute in the head (counted from zero). This attribute controls the order of tokens in the sentence.
     */
	public int W;  // index num. atr. v hlavičce (pořadí slov pro výpis věty (není-li přítomen, bere se N))
    /**
     * an index of hide attribute in the head (counted from zero). This attribute controls whether a node is hidden or not.
     */
	public int H;  // index hide atr. v hlavičce (pro skrývání v tektogramatických stromech)

	// pomocné proměnné
	private int position; // pozice ve vstupním poli při načítání hlavičky
	private byte[] source_byte; // vstupní pole při načítání hlavičky v bajtech
	private char[] source_char; // vstupní pole při načítání hlavičky v charech
	private int source_type; // 1 <=> bajty, 2 <=> chary
	private int source_length; // délka vstupu
	private boolean backslash; // pro funkci readChar

	private ShowMessagesAble mess; // objekt pro výpis hlášek

    /**
     * Creates a new empty head.
     * @param p_mess an object capable of displaying messages
     */
	public NGTreeHead(ShowMessagesAble p_mess) { // konstruktor
		mess = p_mess;
	    attributes = new DefaultListModel(); // create an empty list
		N = V = W = H = -1;
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
     * Return FS representation of the head.
     * @return FS representation of the head
     */
	public String toFSString() { // převede hlavičku do řetězce

		if (attributes == null) return ""; // není co převádět

        int size = attributes.getSize();
        if (size == 0) return ""; // hlavička je prázdná

		StringBuffer head_string_buffer = new StringBuffer("");
        head_string_buffer.append("@E UTF-8\n"); // na začátek hlavičky dám informaci o kódování
        Attribute attr;
		String attr_string;

		boolean [] set_attributes = new boolean[5]; // pole pro informaci o předchozím nastavení atributů, které smějí být nastaveny v hlavičce jen jednou
		set_attributes[0] = false; // atribut N - pořadí uzlů v kreslení stromu
		set_attributes[1] = false; // atribut V - slova pro výpis věty
		set_attributes[2] = false; // atribut W - pořadí slov pro výpis věty
		set_attributes[3] = false; // atribut H - pro skrývání v tektogramatických stromech
		set_attributes[4] = false; // atribut K - klíčový atribut

		// je potřeba ještě ošetřit, aby se atributy, které mohou být definovány jen jednou, nevyskytly dvakrát
		// - např. ord a dord při smíchaných analytických a tektogramatických stromech

		for (int i = 0; i<size; i++) { // přes všechny prvky hlavičky, tj. přes všechny atributy
			attr = (Attribute)attributes.getElementAt(i);
			attr_string = attr.toFSString(set_attributes);
			if (attr_string.length() > 0) { // pokud atribut patří do fs souboru (nepatří tam metaatributy)
				head_string_buffer.append(attr_string);
                head_string_buffer.append('\n'); // jednotlivé atributy oddělím koncem řádku
		    }
		}
		return head_string_buffer.toString();
	} // toFSString

    /**
     * Returns a list of attributes in the head.
     * @return a list of attributes in the head
     */
	public DefaultListModel getModel() {
	    return attributes;
	}

    /**
     * Returns the number of attributes in the head.
     * @return the number of attributes in the head
     */
	public int getSize() {
	    return attributes.getSize();
	}

    /**
     * Returns a deep clone of the head.
     * @return a deep clone of the head
     */
	public NGTreeHead getClone() { // vrátí deep copy této hlavičky stromu
		Attribute attr;
	    NGTreeHead copy = new NGTreeHead(mess);
		copy.N = N;
		copy.V = V;
		copy.W = W;
		copy.H = H;
        int size = attributes.getSize();
		for (int i = 0; i<size; i++) {
			attr = (Attribute)attributes.getElementAt(i);
		    copy.attributes.addElement(attr.getClone());
		}
		// pomocné proměnné nekopíruji
		return copy;
	}

	/**
	 * Adds an attribute at the end of the list of attributes
     * @param a an attribute to be added
	 */
	public void addAttribute(Attribute a) {
	    attributes.addElement(a);
    }

	/**
	 * Returns an attribute with the specified name.
     * @param name a name of an attribute
     * @return an attribute with the specified name; null if there is not any like that
	 */
	public Attribute getAttribute(String name) {
		int pos;
		pos = getIndexOfAttribute(name);
		Attribute a = null; // default value - attribute not found
		if (pos >= 0) { // attribute found
		    a = (Attribute)attributes.getElementAt(pos);
		}
		return a;
	}

    /**
     * Returns true iff an attribute with the specified name exists.
     * @param name a name of an attribute
     * @return true iff an attribute with the specified name exists
     */
    public boolean isAttribute(String name) {
        int pos;
        pos = getIndexOfAttribute(name);
        if (pos >= 0) { // attribute found
            return true;
        }
        return false;
    }

	/**
	 * Gets an attribute at the specified position; null if there is not any like that
     * @param index the index of the attribute, counted from zero
     * @return an attribute at the specified position; null if there is no such index
	 */
	public Attribute getAttributeAt(int index) {
		return (Attribute)attributes.getElementAt(index);
	}

	/**
	 * Return the index of an attribute with the specified name.
     * @param name a name of an attribute
     * @return the index of an attribute with the specified name; -1 if there is not such attribute
	 */
	public int getIndexOfAttribute(String name) {
		Attribute a;
		int position = -1; // default value - attribute not found
	    int pocet = attributes.getSize();
		for (int i=0; i<pocet; i++) {
			a = (Attribute)attributes.getElementAt(i);
			if (name.compareTo(a.getName()) == 0) { // the strings matches
				position = i;
				break;
			}
		}
		return position;
	}

	/**
	 * Returns a list of possible values for an attribute with the specified name.
     * @param name a name of an attribute
	 * @return a list of possible values for an attribute with the specified name. If the values are not specified, returns null
	 */
	public DefaultListModel getPossibleValues(String name) {
	    DefaultListModel list = null; // default value - no possibly values
		int pos = getIndexOfAttribute(name);
		if (pos >= 0) { // found
		    list = getPossibleValuesAt(pos);
		}
		return list;
	}

	/**
	 * Gets the list of possible values for an attribute at the specified position
	 * Returns null, if the possible values are not specified
	 */
	private DefaultListModel getPossibleValuesAt(int position) {
	    Attribute a = (Attribute)attributes.getElementAt(position);
	    DefaultListModel list = a.getListOfValues();
		return list;
	}

    private char readChar() {  // prochazi polem source, vynechava presuny na novy radek a
		//respektuje ocitovani (nastavuje promennou 'backshlash')
		// navratova hodnota: dalsi znak
		/*if (source_type == 1) { // vstupní pole je v bajtech
		    return readCharByte();
		}
		else */if (source_type == 2) { // vstupní pole je v charech
		    return readCharChar();
		}
		debug("\nChyba při čtení znaku ve funkci NGTreeHead.readChar()!");
		return ' '; // chyba!
	}

	private char readCharChar() {
		while (source_char[position] == '\\' && source_char[position+1] == '\r' )
		    position += 3;
	    if (source_char[position] == '\\' ) {
		    backslash = true;
		    position += 2;
		    //debug("" + CharCode.win1250ToUnicode(String.valueOf(source_char[position-1])));
		    return source_char[position-1];
	    }
	    backslash = false;
		position++;
	    //debug("" + CharCode.win1250ToUnicode(String.valueOf(source_char[position-1])));
	    return source_char[position-1];
    }

	/**
	 * Reads a head from an input char field.
     * @param source a source for reading
     * @param start_position the position of the first character to be read in the source
     * @return number of read characters
	 */
	public int readTreeHead(char[] source, int start_position) {
		//debug("\nJsem v readTreeHead(char...)");
		source_char = source;
		source_type = 2;
		source_length = source.length;
		position = start_position;
		readTreeHead();
		source_char = null; // již to nepotřebuji
		return position-start_position;
	}

    /**
     * Deletes all attributes in the forest head.
     */
    public void deleteAttributes() { // vymazání hlavičky
        attributes.clear(); // vymazání hlavičky
    }

	/**
	 * Reads head from an input field
	 */
	private void readTreeHead() {

		if (getSize() != 0) { // hlavička není prázdná
            deleteAttributes(); // vymazání hlavičky
		}

		// cteni hlavičky
		String attr_name, attr_type, attr_values;
		Attribute attr;

		V = -1;	   // zadne V ani N ani W ani H
		N = -1;
		W = -1;
		H = -1;

		char c;

		while (true) {

			attr_name = new String("");
			while ((c=readChar()) != ' ') { // cteni jmena atr.
				attr_name += String.valueOf (c);
			}

			attr_type = new String("");
			// cteni typu atributu (číslo reprezentované řetězcem číslic):
			while ((c=readChar()) != ' ' && c != '\n' ) {
				attr_type += String.valueOf (c);
			}

			position--; // vrátím se před poslední znak

			// podle bitů v typu rozpoznám ord/dord a origf a sentord a hide atr.
			// jejich index v hlavičce uložen do N, W, V a H
            Integer integertype = new Integer (attr_type);
            int inttype = integertype.intValue();
			if ((inttype & 4) > 0) // bit 2 nastaven
				N = getSize(); // pořadí uzlů ve stromu
			if ((inttype & 32) > 0) // bit 5 nastaven
				W = getSize(); // pořadí slov ve výpisu věty
			if ((inttype & 8) > 0) // bit 3 nastaven
				V = getSize(); // slova pro výpis věty
			if ((inttype & 64) > 0) { // bit 6 nastaven
				H = getSize(); // atribut pro skrývání
			}

			//debug ("\nW = " + W + ", N = " + N + ", V = " + V + ", H = " + H);

			attr_values = new String("");
			while ((c=readChar()) != '\n') { // zbytek atr.
				attr_values += String.valueOf (c);
			}

			attr = new Attribute (attr_name, inttype, attr_values);
			addAttribute (attr);
			//debug ("\n" + attr.getName() + " " + attr.getType() + " " + attr.getValues());

			if (position == source_length) // přečteny všechny atributy
				break;
			if (readChar() == '\n') // přečteny všechny atributy
				break;
			else { // musím se o jednu pozici vrátit, abych poslední znak přečetl znovu
			    position--;
			}

		} // while (true)
	} // readTreeHead

} // class NGTreeHead



