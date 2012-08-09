package cz.cuni.mff.mirovsky;

/**
 * An interface for a class that displays a progress bar, for starting and stopping the progress bar.
 */
public interface ProgressDisplayer {

    public static final int NONE=0; // konstanty pro určení typu zobrazování hodnoty progress baru
    public static final int PERCENT=1;
    public static final int STRING=2;

    /**
     * It starts the progress bar, sets a source class for it (info_src), type of displaying of
     * progress value (p_string_type) and an interval (in ms) for refreshing of the progress bar (timer_interval)
     */
    public void startProgressBar(ProgressSource info_src, int p_string_type, int timer_interval);

    /**
    * This function stops the progress bar (it finishes the work of the progress bar)
    */
    public void stopProgressBar(); // odstraní progress bar z InfoBaru (a zastaví timer)

}