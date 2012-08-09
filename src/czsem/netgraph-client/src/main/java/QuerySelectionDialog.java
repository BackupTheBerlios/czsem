import cz.cuni.mff.mirovsky.properties.*;
import cz.cuni.mff.mirovsky.trees.*;

import java.awt.*;
import java.util.ResourceBundle;
import javax.swing.*;

/**
 * Title: Query selection dialog window
 * Description: Allows user to select a name and a comment of a query
 * Copyright: Copyright (c) 2001
 * Company: Charles University in Prague, CKL
 * @author Jiří Mírovský
 * @version 1.0
 */

/**
 * Class QuerySelectionDialog creates a dialog window for loading/saving a query to a local disc. A list of previously saved
 * queries is displayed and the graphical representation of the query is displayed too. A name of the query and a comment can be entered,
 * an item can be deleted from the list.
 */
public class QuerySelectionDialog extends ItemSelectionDialog {

	private NGForestView query_view;
	//private JTextArea query_text_area_value;
	private NGTreeHead query_head;


    public QuerySelectionDialog(Frame parent_frame, ItemSet p_items_set, NGTreeHead p_query_head) {
        this(parent_frame, p_items_set, null, null, p_query_head);
    }

    public QuerySelectionDialog(Frame parent_frame, ItemSet p_items_set, ResourceBundle p_i18n, String p_prefix, NGTreeHead p_query_head) {
		// parent_frame ... nadřazené okno
		// p_items_set ... zdroj položek
        // p_i18n ... objekt pro přístup k lokalizovaným zprávám

        super(parent_frame, p_items_set, p_i18n, p_prefix); // zavolám konstruktor rodičovského objektu ItemSelectionDialog
		query_head = p_query_head;
    }

    public void setCoreferencePatterns(DefaultListModel patterns) {
        query_view.setReferencePatterns(patterns);
    }

    protected void displayItemValue (JPanel p, String value) { // zobrazí hodnotu prvku v panelu p
		String text_value;
		if (value == null) text_value = "";
		else text_value = value;

		//System.out.print("\nzobrazuji hodnotu: " + text_value);
		//query_text_area_value.setText(text_value); // zobrazím hodnotu prvku

		try {
	        NGForest query_forest = new NGForest(null);
		    query_forest.setHead(query_head);
		    query_forest.readForest(text_value.toCharArray(), 0, query_head.getSize());
			DefaultListModel query_selected_attributes = new DefaultListModel();
			vyberAtributyPouziteVDotazu(query_forest, query_head, query_selected_attributes);
			query_forest.setVybraneAtributy(query_selected_attributes);
			query_view.setForest(query_forest);
			query_view.getTreeProperties().setShowAttrNames(true); // chci zobrazovat jména atributů
			query_view.getTreeProperties().setShowNullValues(false); // nevyplněné atributy ať nezabírají místo
			query_view.getTreeProperties().setShowMultipleSets(true); // zobrazovat alternativní sady atributů
            query_view.getTreeProperties().setShowHiddenNodes(true) ; // zobrazovat skryté vrcholy
			query_view.repaint();
			//query_tree_view_scroll_pane.revalidate();
		}
		catch (Exception e) {
		    System.out.println("\nNeočekávaná chyba při zobrazování stromu dotazu: " + e);
		}

	}
    /**
     * Adds to the selected_attributes list attributes occured at least once in the query; this function is a copy from PanelQuery
     */
    private void vyberAtributyPouziteVDotazu(NGForest query_forest, NGTreeHead ngt_head, DefaultListModel selected_attributes) {
        // projdu to přes všechny atributy, s každým z nich projdu celým lesem;
        // tím pádem budu mít ty atributy seřazené podle jejich pořadí v globální hlavičce;
        // to sice není nutné, ale je to pěkné
        String name; // jméno atributu
        int number_of_attributes = ngt_head.getSize(); // maximální počet atributů
        for (int i = 0; i < number_of_attributes; i++) { // přes všechny atributy
            name = ngt_head.getAttributeAt(i).toString();
            //debug("\nHledám atribut " + name + " na pozici " + i);
            for (NGTree tree : query_forest.getTrees()) {
                if (isDefinedInSubtree(tree.getRoot(), i)) { // pokud je definován, přidám ho k vybraným
                    selected_attributes.addElement(name);
                    //debug(" - nalezen.");
                    break; // nepokračuji dalším stromem - mohl bych ho nechtěně přidat víckrát
                }
                //else debug(" - nenalezen.");
            }
        }
    }

 		/**
		 * Returns true if attribute with the specified order in the head is at least once specified in the subtree of the node, including the node
		 */
		private boolean isDefinedInSubtree(TNode node, int attribute_order) {
		    boolean found = false; // dosud jsem ten atribut nenašel
			while (!found && node != null) {
				found = isDefinedInNode(node, attribute_order);
				if (!found) found = isDefinedInSubtree (node.first_son, attribute_order); // pokud nebyl zde, hledám v podstromu
				node = node.brother; // a kdyžtak pokračuji bratrem
			}
			return found;
		}

		/**
		 * Returns true if attribute_name is specified in the given node
		 */
		private boolean isDefinedInNode(TNode node, int attribute_order) {
		    boolean found = false; // zatím nenalezen v žádné sadě atributů
			TValue values = node.values;
			TAHLine[] set; // jedna sada atributů
			TAHLine attribute; // jeden atribut

			while (!found && values != null) { // dokud jsem nenalezl a je tam další sada atributů
				try { // kdyby byl problém s polem
				    set = values.AHTable; // vezmu pole hodnot atributů v této sadě
					attribute = set[attribute_order];
					while (!found && attribute != null) { // tohle je přes různé hodnoty jednoho atributu?
						if (attribute.Value != null && attribute.Value.length()>0) {
							//debug ("- trefa, hodnota = " + attribute.Value);
							found = true;
						}
					    attribute = attribute.Next;
					}
				}
				catch (Exception e) {
				    debug("\nPanelQuery.isDefinedInNode: Neočekávaná chyba " + e + "při prohledávání definovaných atributů.");
				}
			    values = values.Next;
			}
			return found;
		}


	protected void createDisplayValueArea (JPanel p) { // vytvoří objekty potřebné k zobrazování obsahu prvku
		//query_text_area_value = new JTextArea();
		query_view = new NGForestView(null);
		p.setLayout(new BorderLayout());
		//p.add(query_text_area_value,BorderLayout.SOUTH);
		p.add(query_view,BorderLayout.CENTER);
	}

	private void debug(String s) {
		System.out.print(s);
	}

}
