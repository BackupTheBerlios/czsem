package cz.cuni.mff.mirovsky.trees;

import java.awt.Color;
import javax.swing.DefaultListModel;

import cz.cuni.mff.mirovsky.*;

/**
 * Class Attribute keeps general properties of an attribute of nodes in trees, like a name, possible values, type etc.
 */


public class Attribute {

	private String name; // jméno atributu
	private int type; // typ atributu
	private String values; // povolené hodnoty oddělené mezerou
	private String value; // aktuální hodnota (zřejmě se nepoužívá)
	private boolean displayed; // určuje, zda má být hodnota atributu zobrazována ve stromu
	private Color color; // určuje barvu zobrazení (nepouziva se)
	// názvy meta atributů:
	//public static String meta_optional_node = "_optional";
	//public static String meta_transitive_parent_edge = "_transitive";

    /**
     * Creates an attribute with default properties.
     */
	public Attribute () { // konstruktor
		Attribute5 ("",-1,"",false,Color.black);
	}

    /**
     * Creates an attribute with a given name and default properties.
     * @param p_name the name of the attribute
     */
	public Attribute (String p_name) { // konstruktor
		Attribute5 (p_name,-1,"",false,Color.black);
	}

    /**
     * Creates an attribute with given properties.
     * @param p_name the name of the attribute
     * @param p_type a type of the attribute. Individual bits have the following meaning:
     * <br>&nbsp; &nbsp; 1 - P (positional)
     * <br>&nbsp; &nbsp; 2 - O (obligatory)
     * <br>&nbsp; &nbsp; 4 - N (numeric attribute - controls the order of nodes in the tree from left to right; can only be set once in the head
     * <br>&nbsp; &nbsp; 8 - V (value attribute - the sentence belonging to the tree is assembled from values of this attribute; can only be set once in the head
     * <br>&nbsp; &nbsp; 16 - L (list attribute - all its possible values are listed in the head)
     * <br>&nbsp; &nbsp; 32 - W (word order attribute - the order of tokens in the sentence is controlled by this attribute)
     * <br>&nbsp; &nbsp; 64 - H (hidden - marks hidden nodes; can only be set once in the head)
     * <br>&nbsp; &nbsp; 128 - first of bits controlling the way of displaying the attribute; is not used in Netgraph
     * <p>some values have a special meaning:
     * <br>&nbsp; &nbsp; 0 - K (key attribute - not used in Netgraph; can only be set once in the head)
     */
	public Attribute (String p_name, int p_type) { // konstruktor
		Attribute5 (p_name, p_type, "", false,Color.black);
	}

    /**
     * Creates an attribute with given properties.
     * @param p_name the name of the attribute
     * @param p_type a type of the attribute. Individual bits have the following meaning:
     * <br>&nbsp; &nbsp; 1 - P (positional)
     * <br>&nbsp; &nbsp; 2 - O (obligatory)
     * <br>&nbsp; &nbsp; 4 - N (numeric attribute - controls the order of nodes in the tree from left to right; can only be set once in the head
     * <br>&nbsp; &nbsp; 8 - V (value attribute - the sentence belonging to the tree is assembled from values of this attribute; can only be set once in the head
     * <br>&nbsp; &nbsp; 16 - L (list attribute - all its possible values are listed in the head)
     * <br>&nbsp; &nbsp; 32 - W (word order attribute - the order of tokens in the sentence is controlled by this attribute)
     * <br>&nbsp; &nbsp; 64 - H (hidden - marks hidden nodes; can only be set once in the head)
     * <br>&nbsp; &nbsp; 128 - first of bits controlling the way of displaying the attribute; is not used in Netgraph
     * <p>some values have a special meaning:
     * <br>&nbsp; &nbsp; 0 - K (key attribute - not used in Netgraph; can only be set once in the head)
     * @param p_values possible values of the attribute, separated by a space
     */
	public Attribute (String p_name, int p_type, String p_values) { // konstruktor
		Attribute5 (p_name, p_type, p_values, false, Color.black);
	}

    /**
     * Creates an attribute with given properties.
     * @param p_name the name of the attribute
     * @param p_type a type of the attribute. Individual bits have the following meaning:
     * <br>&nbsp; &nbsp; 1 - P (positional)
     * <br>&nbsp; &nbsp; 2 - O (obligatory)
     * <br>&nbsp; &nbsp; 4 - N (numeric attribute - controls the order of nodes in the tree from left to right; can only be set once in the head
     * <br>&nbsp; &nbsp; 8 - V (value attribute - the sentence belonging to the tree is assembled from values of this attribute; can only be set once in the head
     * <br>&nbsp; &nbsp; 16 - L (list attribute - all its possible values are listed in the head)
     * <br>&nbsp; &nbsp; 32 - W (word order attribute - the order of tokens in the sentence is controlled by this attribute)
     * <br>&nbsp; &nbsp; 64 - H (hidden - marks hidden nodes; can only be set once in the head)
     * <br>&nbsp; &nbsp; 128 - first of bits controlling the way of displaying the attribute; is not used in Netgraph
     * <p>some values have a special meaning:
     * <br>&nbsp; &nbsp; 0 - K (key attribute - not used in Netgraph; can only be set once in the head)
     * @param p_values possible values of the attribute, separated by a space
     * @param p_displayed says if the attribute should be displayed at nodes in the tree
     */
	public Attribute (String p_name, int p_type, String p_values, boolean p_displayed) { // konstruktor
		Attribute5 (p_name, p_type, p_values, p_displayed, Color.black);
	}

    /**
     * Creates an attribute with given properties.
     * @param p_name the name of the attribute
     * @param p_type a type of the attribute. Individual bits have the following meaning:
     * <br>&nbsp; &nbsp; 1 - P (positional)
     * <br>&nbsp; &nbsp; 2 - O (obligatory)
     * <br>&nbsp; &nbsp; 4 - N (numeric attribute - controls the order of nodes in the tree from left to right; can only be set once in the head
     * <br>&nbsp; &nbsp; 8 - V (value attribute - the sentence belonging to the tree is assembled from values of this attribute; can only be set once in the head
     * <br>&nbsp; &nbsp; 16 - L (list attribute - all its possible values are listed in the head)
     * <br>&nbsp; &nbsp; 32 - W (word order attribute - the order of tokens in the sentence is controlled by this attribute)
     * <br>&nbsp; &nbsp; 64 - H (hidden - marks hidden nodes; can only be set once in the head)
     * <br>&nbsp; &nbsp; 128 - first of bits controlling the way of displaying the attribute; is not used in Netgraph
     * <p>some values have a special meaning:
     * <br>&nbsp; &nbsp; 0 - K (key attribute - not used in Netgraph; can only be set once in the head)
     * @param p_values possible values of the attribute, separated by a space
     * @param p_displayed says if the attribute should be displayed at nodes in the tree
     * @param p_color color of the attribute (not used in Netgraph)
     */
	public Attribute (String p_name, int p_type, String p_values, boolean p_displayed, Color p_color) { // konstruktor
		Attribute5 (p_name, p_type, p_values, p_displayed, p_color);
	}

    /**
     * Returns a deep copy of the Attribute
     * @return a deep copy of the Attribute
     */
	public Attribute getClone() { // vrátí deep copy tohoto objektu
	    Attribute copy = new Attribute(name,type,values,displayed,color);
		copy.value = new String(value);
		return copy;
	}

	private void Attribute5 (String p_name, int p_type, String p_values, boolean p_displayed, Color p_color) { // vlastní vznik objektu
		name = new String (p_name);
		type = p_type;
		values = new String (p_values);
		displayed = p_displayed;
		value = new String ("");
		color = new Color(p_color.getRGB());
	}

	// popis významů jednotlivých bitů v typu atributu:
	// 1 - P (poziční)
	// 2 - O (povinný)
	// 4 - N (číselný atribut udávající pořadí uzlů ve stromu, smí být definován jen jednou)
	// 8 - V (atribut, který se má vypisovat ve větě)
	// 16 - L (výčtový)
	// 32 - W (určuje pořadí slov ve větě)
	// 64 - H (skrývací)
	// 128 - první z bitů určujících způsob zobrazení

	// popis speciálních významů některých hodnot:
	// 0 - K (klíčový)
	// 7 - N (u analytických stromů atribut ord - určuje pořadí uzlů, implikuje P a O)
	//       (u tectogramatických stromů atr. dord, rovněž implikuje P a O)

    /**
     * Returns the FS representation of the attribute for the FS head.
     * @return the FS representation of the attribute for the FS head
     */
	public String toFSString() { // převede atribut do řetězce pro formát FS
		// následující pole se nevyužije, ale pro volání funkce je potřeba
		boolean [] set_attributes = new boolean[5]; // pole pro informaci o předchozím nastavení atributů, které smějí být nastaveny v hlavičce jen jednou
		set_attributes[0] = false; // atribut N - pořadí pro výpis věty
		set_attributes[1] = false; // atribut V - slova pro výpis věty
		set_attributes[2] = false; // atribut W - pořadí slov v kreslení stromu
		set_attributes[3] = false; // atribut H - pro skrývání v tektogramatických stromech
		set_attributes[4] = false; // atribut K - klíčový atribut

	    return toFSString(set_attributes);
	}

    /**
     * Returns the FS representation of the attribute for the FS head.
     * @param set_attributes an array of attribute types that can only be set once in the head. The array signals whether the
     * attributes have already been set in the head. Their order is: N, V, W, H, K
     * @return the FS representation of the attribute for the FS head
     */
	public String toFSString(boolean [] set_attributes) { // převede atribut do řetězce pro formát FS
		// pole set_attributes určuje, zda atributy, které smějí být v hlavičce jen jednou, byly již nastaveny
		// význam jednotlivých položek viz funkci toFSString() (tj. bez parametrů)

        int type_pracovni = type;

		if (name == null) return "";
		if (name.length() == 0) return "";

		if (name.charAt(0) == '_') return ""; // metaatributy do fs souboru nepatří
		StringBuffer attr_string_buffer = new StringBuffer("");
		if (type_pracovni > 256) type_pracovni-=256; // způsob vykreslování zahazuji
		if (type_pracovni > 128) type_pracovni-=128; // způsob vykreslování zahazuji

//		if ((type_pracovni & 2)==2) { // povinný atribut - tuto vlastnost zahazuji, aby bylo možno kombinovat stromy s různými hlavičkami
//			type_pracovni-=2;
//		}

		boolean empty_output = true; // určuje, zda výstup atributu do FS formátu je dosud prázdný

		if (type_pracovni == 0) { // klíčový atribut - nepoužíváno v PDT 1.0
			if (!set_attributes[4]) { // v hlavičce nebyl tento atribut dosud uveden
				attr_string_buffer.append("@K ");
                attr_string_buffer.append(name);
				set_attributes[4] = true;
				empty_output = false;
			}
		}
		if ((type_pracovni & 1)==1) { // poziční atribut
			type_pracovni--;
            attr_string_buffer.append("@P ");
            attr_string_buffer.append(name);
			empty_output = false;
			if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
		}
		if ((type_pracovni & 2)==2) { // povinný atribut
			type_pracovni-=2;
            attr_string_buffer.append("@O ");
            attr_string_buffer.append(name);
			empty_output = false;
			if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
		}
		if ((type_pracovni & 4)==4) { // číselný atribut
			type_pracovni-=4;
			if (!set_attributes[0]) { // v hlavičce nebyl tento atribut dosud uveden
                attr_string_buffer.append("@N ");
                attr_string_buffer.append(name);
				set_attributes[0] = true;
				empty_output = false;
				if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
			}
			else { // vymažu předchozí záznamy o atributu
			    attr_string_buffer.setLength(0);
				empty_output = true;
			}
		}
		if ((type_pracovni & 8)==8) { // atribut, který se má vypisovat ve větě
			type_pracovni-=8;
			if (!set_attributes[1]) { // v hlavičce nebyl tento atribut dosud uveden
                attr_string_buffer.append("@V ");
                attr_string_buffer.append(name);
				set_attributes[1] = true;
				empty_output = false;
				if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
		    }
			else { // vymažu předchozí záznamy o atributu
			    attr_string_buffer.setLength(0);
				empty_output = true;
			}
		}
		if ((type_pracovni & 16)==16) { // výčtový atribut
			type_pracovni-=16;
            attr_string_buffer.append("@L ");
            attr_string_buffer.append(name);
			if (values != null) {
				attr_string_buffer.append('|');
                attr_string_buffer.append(values.trim().replace(' ','|'));
			}
		    //for (int i=0; i<attr_string.length(); i++) {
			//    System.out.println(attr_string.charAt(i) + " " + (int)attr_string.charAt(i));
			//}
			empty_output = false;
			if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
		}
		if ((type_pracovni & 32)==32) { // atribut pro pořadí slov ve větě
			type_pracovni-=32;
			if (!set_attributes[2]) { // v hlavičce nebyl tento atribut dosud uveden
                attr_string_buffer.append("@W ");
                attr_string_buffer.append(name);
				set_attributes[2] = true;
				empty_output = false;
				if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
			}
			else { // vymažu předchozí záznamy o atributu
			    attr_string_buffer.setLength(0);
				empty_output = true;
			}
		}
		if ((type_pracovni & 64)==64) { // skrývací atribut
			type_pracovni-=64;
			if (!set_attributes[3]) { // v hlavičce nebyl tento atribut dosud uveden
                attr_string_buffer.append("@H ");
                attr_string_buffer.append(name);
				set_attributes[3] = true;
				empty_output = false;
	    		if (type_pracovni != 0) attr_string_buffer.append('\n'); // bude následovat ještě další řádek pro tento atribut
		    }
			else { // vymažu předchozí záznamy o atributu
			    attr_string_buffer.setLength(0);
				empty_output = true;
			}
		}

		if (empty_output) { // pokud žádný z předchozích bloků nevyprodukoval výstup, vytvořím alespoň výstup jakožto pozičního atributu
            attr_string_buffer.append("@P ");
            attr_string_buffer.append(name);
		}

		return attr_string_buffer.toString();
	} // toFSString

    /**
     * Returns the name of the attribute.
     * @return the name of the attribute
     */
	public String getName () {
		return name;
	}

    /**
     * Sets the name of the attribute.
     * @param p_name the name of the attribute
     */
    public void setName (String p_name) {
		name = p_name;
	}

    /**
     * Returns the type of the attribute.
     * @return the type of the attribute
     */
	public int getType () {
		return type;
	}

    /**
     * Returns possible values of the attribute (space-separated).
     * @return possible values of the attribute (space-separated) or null if they have not been set
     */
	public String getValues () {
		return values;
	}

    /**
     * Returns the value of the attribute. It is used in the list of values of attributes of the selected node.
     * @return the value of the attribute
     */
	public String getValue () {
		return value;
	}

    /**
     * Sets the value of the attribute. It is used in the list of values of attributes of the selected node.
     * @param p_value the value of the attribute
     */
    public void setValue (String p_value) {
		value = p_value;
	}

    /**
     * Returns true iff the attribute should be displayed in the trees.
     * @return true iff the attribute should be displayed in the trees
     */
	public boolean getDisplayed () {
		return displayed;
	}

    /**
     * Returns the color in which the attribute should be displayd in the trees. It is not used in Netgraph.
     * @return the color
     */
	public Color getColor () {
		return color;
	}

    /**
     * Sets the color in which the attribute should be displayd in the trees. It is not used in Netgraph.
     * @param p_color the color
     */
	public void setColor (Color p_color) {
		color = new Color(p_color.getRGB());
	}

    /**
     * Sets if the attribute should be displayed in trees.
     * @param p_displayed true iff it should be displayed
     */
    public void setDisplayed (boolean p_displayed) {
		displayed = p_displayed;
	}

    /**
     * Returns the String representation of the attribute. Actually, it returns the name of the attribute
     * @return String representation of the attribute
     */
    public String toString () {
		return name;
	}

	/**
	 * Gets a list of possible values for this attribute.
     * @return the list of possible values if they have been specified. Otherwise returns null.
	 */
	public DefaultListModel getListOfValues() {
	    DefaultListModel list;
		String elem;
		int elem_length;
		int length;
		if (values == null) return null;
	    String trimmed = values.trim();
		if (trimmed.length() == 0) return null;

		list = new DefaultListModel(); // creating an empty list

		while ((elem = firstElement(trimmed)) != null) { // if there is another value
		    elem_length = elem.length();
		    length = trimmed.length();
			if (elem_length != length) {
			    trimmed = trimmed.substring(elem_length,length);
			}
			else trimmed = "";
			trimmed = trimmed.trim(); // remove the first space character (words separator)
		    list.addElement(elem);
		}
		return list;
	}

	/**
	 * Gets the first word in the String s or returns null if there is not any;
	 */
	private String firstElement (String s) {
		String elem;
		int length = s.length();
		if (length == 0) return null;
		int pos = s.indexOf(' ');
		if (pos == -1) pos = length;
		elem = new String(s.substring(0,pos));
		return elem;
	}

} // class Attribute

