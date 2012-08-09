package cz.cuni.mff.mirovsky;

/**
 * An interface for a class that can serve as a source of a progress data for a progress bar.
 */
public interface ProgressSource {

    /**
    * Returns a current value of the progress of the task
    */
    public int getProgressCurrentValue();

    /**
    * Returns a string to be displayed in the progress bar
    */
    public String getProgressText();

    /**
     * Returns a minimum value for the progress bar
     */
    public int getProgressMinValue();

    /**
     * Returns a maximum value for the progress bar
     */
    public int getProgressMaxValue();

}
