package cz.cuni.mff.mirovsky.properties;

import java.util.Iterator;

/**
 * This is an interface for a set of items. It makes sure that the class provides certain iterators over the items.
 */

public interface ItemSet {
	public Iterator getIteratorOverNames();
	public Iterator getIteratorOverValues();
	public String getItemValue(String name);
	public String getItemComment(String name);
    public void removeItem(String name);
}