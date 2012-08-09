
import java.util.EventListener;

/**
 * An interface for a class capable of listening to the event of the tree from the server having been sent.
 */
public interface TreeLoadedListener extends EventListener {

    /**
     * Invoked when a tree has been loaded.
     */
    public void treeLoaded();

    /**
     * Invoked when only statistics hava been loaded.
     */
    public void statisticsLoaded();


}