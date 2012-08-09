package cz.cuni.mff.mirovsky.trees;

/**
 * Keeps a value of an attribute in a set of attributes at a node. It is linked in a linked list to keep all alternative values of the attribute at the node.
 */
public class TAHLine {
    /** one value of the attribute */
    public String Value;
    /** a relation between the name of the attribute and the value */
    public int relation;
    /** a pointer to the next (alternative) value of the attribute */
    public TAHLine Next;

    /** relation equal (=) */
    public static final int RELATION_EQ = 0;   // relace rovnost
    /** relation not equal (!=) */
    public static final int RELATION_NEQ = 1;   // relace nerovnost
    /** relation less than (<) */
    public static final int RELATION_LT = 2;  // relace menší než
    /** relation greater than (>) */
    public static final int RELATION_GT = 3;   // relace větší než
    /** relation less than or equal (<=) */
    public static final int RELATION_LTEQ = 4;   // relace menší nebo rovno
    /** relation greater than or equal (>=) */
    public static final int RELATION_GTEQ = 5;   // relace větší nebo rovno


    /**
     * Creates a new empty object for keeping a value of an attribute
     */
    public TAHLine() {
        Value = "";
        relation = RELATION_EQ;
        Next = null;
    }

    /**
     * Returns a deep copy of the object.
     * @return a deep copy
     */
	public TAHLine getClone() { // vrátí deep copy tohoto objektu
	    TAHLine copy = new TAHLine();
		copy.Value = new String(Value);
        copy.relation = relation;

		// zbytek spojáku:
		TAHLine src_next = this.Next;
		if (src_next != null) { // je tu další hodnota
			copy.Next = src_next.getClone();
		}
		else {
			copy.Next = null;
		}
	    return copy;
	}
} // class TAHLine
