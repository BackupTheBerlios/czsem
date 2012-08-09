package cz.cuni.mff.mirovsky.properties;

/**
 * Interface Item defines functions for an item - a name, a value, a comment.
 */
public interface Item {

	/**
	 * Gets the name of the item
	 */
	public String getName();

	/**
	 * Gets the comment of the item
	 */
	public String getComment();

	/**
	 * Gets the value of the item
	 */
	public String getValue();
}