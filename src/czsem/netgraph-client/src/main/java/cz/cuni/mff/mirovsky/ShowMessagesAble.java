package cz.cuni.mff.mirovsky;

/**
 * An interface for a class capable of displaying debug and info messages to the users.
 */
public interface ShowMessagesAble {

	/**
	* Shows a debug message
	*/
	public void debug(String message);

	/**
	* Shows an information for a user
	*/
	public void inform(String message);

        /**
         * Adds a non-localized information for a user; the previous info is not deleted before
         * @param message String to be displayed
         */
        public void addInfoNotLocalized(String message);


}
