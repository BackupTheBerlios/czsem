package cz.cuni.mff.mirovsky.trees;

/**
 * This class keeps a set of values of attributes at a node. It is linked into a linked list to keep all alternative sets of values of attributes.
 */
public class TValue {
    /** an array representing a set of values of attributes of a node */
	public TAHLine AHTable[]; // vlastní sada atributů
    /** a pointer to the next (alternative) set of values of attributes of a node */
	public TValue Next; // odkaz na další sadu

    /**
     * Creates a new empty object for keeping the set of values of attributes of a node.
     */
    public TValue (){
        AHTable = null;
        Next = null;
    }

    /**
     * Returns a deep copy of the set of values of attributes, recursively with all alternative sets in the linked list.
     * @return a deep copy of the set of values of attributes and the whole linked list
     */
    public TValue getClone() { // rekurzivně projde spoják a vrátí deep copy
		TValue copy = new TValue();

		// pole atributů:
		int length = AHTable.length;
		copy.AHTable = new TAHLine[length];
		for (int i=0; i<length; i++) {
			if (AHTable[i] != null) {
		        copy.AHTable[i] = AHTable[i].getClone();
			}
			else {
				copy.AHTable[i] = null;
			}
		}

		// zbytek spojáku:
		TValue src_next = this.Next;
		if (src_next != null) { // je tu další sada atributů
			copy.Next = src_next.getClone();
		}
		else {
			copy.Next = null;
		}
	    return copy;
	}

    private void copyAHTable(TAHLine[] new_table, TAHLine[] old_table) { // zkopíruje staré hodnoty do nové tabulky
        int old_size = old_table.length;
        for (int i=0; i<old_size; i++) {
            new_table[i] = old_table[i];
        }
    }

    /**
     * It increases number of attributes at the node by the number.
     * The new attributes are added at the end of the list of attributes.
     * The values of the attributes are set empty.
     * Attributes are added to all sets in the linked list.
     * @param number number of attributes to be added
     */
    public void addAttributes(int number) {
        if (number <= 0) return;
        int size = AHTable.length; // dosavadní počet atributů
        int new_size = size + number;
        TValue tval = this;
        while (tval != null) { // přes všechny sady atributů
            TAHLine[] AHTable_new = new TAHLine[new_size];
            copyAHTable(AHTable_new, AHTable); // zkopíruji původní hodnoty
            for (int i=size; i<new_size; i++) { // nastavím hodnoty na nových pozicích
                AHTable_new[i] = new TAHLine();
            }
            AHTable = AHTable_new;
            tval = tval.Next; // a vezmu další sadu atributů
        }
    }
} // TValue

