package cz.cuni.mff.mirovsky.trees;

import java.awt.event.*;

import cz.cuni.mff.mirovsky.ShowMessagesAble;


/**
 * This class provides functions for printing a forest on a screen and for responding mouse-clicks on the forest
 */
public class NGForestDisplay extends NGForestView {

	private int dosah_mysi;  // tolerance pro volbu vrcholu

    /**
     * Creates a new object for displaying forests.
     * @param p_mess an object capable of displaying messages
     */
    public NGForestDisplay(ShowMessagesAble p_mess) { // konstruktor

		super(p_mess);

		dosah_mysi = 10;

    } // konstruktor


	// nalezení a zvýraznění vybraného vrcholu - funkce volána zvnějšku, ne přímo událostí myši
    /**
     * Searches the forest and tries to find a node that is close enough to the place of a mouse click.
     * @param e a mouse event
     * @return depth-first order of the selected node (counted from 1) or 0 if no node was close enough
     */
	public int selectNode (MouseEvent e) { // stisknuto tlačítko myši - označí se příslušný vrchol
	    // vrátí se jeho pořadí při průchodu do hloubky (počítáno od 1)
		// 0 znamená nenalezen
	    int poradi = 0;
    	Object zdroj = e.getSource();
		if (zdroj == this) { // jenom si ověřím, že to je opravdu pro mne
			int x = e.getX();
   		    int y = e.getY();
			if (forest != null) {
			    poradi = setChosenByPosition(x, y, dosah_mysi, show_hidden_nodes); // na který vrchol se kliklo myší?
			}
			else poradi = 0;
			//repaint(); // zobrazí se vybraný vrchol
		}
		return poradi;
	} // selectNode

    private int setChosenByPosition (int x, int y, int dosah_mysi, boolean allow_hidden) { // projde stromy jeden po druhém, od korene do hloubky a hledá vrchol dostatečně blízko souřadnicím x,y
        // allow_hidden určuje, zda smí vybrat skrytý vrchol
        // vrátí pořadí nalezeného vrcholu při průchodu do hloubky; 0 znamená nenalezen; když projde jeden strom, pokračuje dalším, pořadí kořene dalšího
        // stromu je rovno pořadí posledního vrcholu předchozího stromu plus 1
        //debug("\nNGForestDisplay.setChosenByPosition: entering the function with x=" + x + ", y=" + y);
        int right_border = 0; // looking for a tree the x argument falls to
        int width;
        int number_of_nodes = 0;
        int deep_order = 0;
        int tree_number = 0; // počítám stromy, abych mohl nastavit chosen_tree_order
        int horizontal_space_after = Math.round(horizontal_space / 2); // first half of the horizontal space between trees belongs to the left tree
        int horizontal_space_before = horizontal_space - horizontal_space_after; // second half belongs to the right tree
        NGTree last_chosen_tree = forest.getChosenTree(); // there was a chosen node in this tree
        for (NGTree tree: forest.getTrees()) { // over all trees
            tree_number ++;
            width = tree.getWidth() + horizontal_space_after;
            //debug("\nNGForestDisplay.setChosenByPosition: tree width=" + width);
            right_border += width;
            //debug("\nNGForestDisplay.setChosenByPosition: right border=" + right_border);
            if (right_border + dosah_mysi >= x) { // found the right tree
                deep_order = tree.setChosenByPosition(x - (right_border - width), y, dosah_mysi, allow_hidden);
                if (deep_order != 0) { // the node has been found
                    // because of possible different visibility of alternative sets at chosen and not-chosen nodes,
                    // this tree might need to re-calculate its drawing and also the tree that had a node chosen before
                    if (last_chosen_tree != null) {
                        last_chosen_tree.setFlagTreeChanged(true);
                    }
                    tree.setFlagTreeChanged(true);
                    forest.setFlagForestChanged(true);
                    forest.setChosenTreeByOrder(tree_number);                    
                    return number_of_nodes + deep_order;
                }
            }
            number_of_nodes += tree.getNumberOfNodes(); // all nodes of this tree have been skipped because all of them are left from the desired location
            right_border += horizontal_space_before; // the rest of the space between trees
            if (right_border > x + dosah_mysi) { // next tree would be to far right
                break;
            }
        }
        return 0; // the node has not been found
    }

} // class NGForestDisplay
