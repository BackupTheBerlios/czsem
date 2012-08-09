import java.util.EventListener;

/**
 * An interface for capturing an event of the saving of results trees having been finished.
 */
public interface SavingFinishedListener extends EventListener {

    /**
     * Invoked when saving of trees is finished.
     */
    public void savingFinished();

    /**
     * Invoked when saving of trees is canceled; an object envoking the function is provided.
     */
    public void savingCanceled(Object source);

}

