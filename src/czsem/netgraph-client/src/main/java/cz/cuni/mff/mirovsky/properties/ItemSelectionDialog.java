package cz.cuni.mff.mirovsky.properties;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.Iterator;
import java.util.ResourceBundle;

/**
 * This class displays a dialog window that allows the user to select an item from a list of items or enter a new name and comment of an item.
 */

public class ItemSelectionDialog extends JDialog implements ListSelectionListener, MouseListener, ActionListener, WindowListener {
    private JPanel panel_main = new JPanel();

	private JList items_list; // zde se budou zobrazovat všechny položky seznamu
	private DefaultListModel items_list_model; // zde budou ty položky uloženy

	private boolean show_edit_line_item_name; // umožnit uživateli vložit název položky?
	private boolean enable_edit_line_item_comment; // umožnit uživateli vložit komentář položky?
	private boolean show_delete_button; // umožnit uživateli odstraňovat položky?

	private boolean check_overwrite; // ptát se uživatele před přepsáním již existujícího dotazu?
	// (automaticky nastaveno na true při ukládání, na false při otevírání)

	private String selected_name; // k uchování uživatelova výběru
	private String selected_value;
    private String selected_comment;

	private boolean return_value; // návratová hodnota pro uživatele, aby poznal, zda bylo vybráno nebo byl výběr zrušen

	private ItemSet items_set; // toto je ukazatel na properties/section pro tento dialog

    private JPanel panel_head = new JPanel();

	private JTextArea item_text_area_value; // prostor k zobrazování obsahu prvku při implicitním textovém zobrazení
    private JTextField text_field_item_name = new JTextField();
    private JLabel label_user_appeal = new JLabel(); // prostor k zobrazování komentáře prvku

	private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    private String prefix; // prefix odkazů na lokalizované zprávy

    private JPanel panel_work = new JPanel();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel panel_work_west = new JPanel();
    private JPanel panel_work_center = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private JPanel panel_item_display_value = new JPanel();
    private JPanel panel_items = new JPanel();
    private JPanel panel_item_display_comment = new JPanel();
    private JScrollPane item_display_value_scroll_pane = new JScrollPane();
    private JTextField item_text_field_comment;
    private JScrollPane item_display_comment_scroll_pane = new JScrollPane();
    private JPanel panel_item_display;
    private JPanel panel_buttons = new JPanel();
    private JButton button_select = new JButton();
    private JButton button_cancel = new JButton();
    private JPanel panel_items_list = new JPanel();
    private JScrollPane items_list_scroll_pane = new JScrollPane();
    private JButton button_delete = new JButton();
    private JPanel check_preview_panel = new JPanel();

    protected boolean check_preview_default = true;
    private JCheckBox check_preview = new JCheckBox(); // zde se zaškrtává, zda zobrazovat obsah položek či ne

//  public ItemSelectionDialog() {
//      this(null, null);
//  }

    public ItemSelectionDialog(Frame parent_frame, ItemSet p_items_set) {
        this(parent_frame, p_items_set, null, null);
    }

    public ItemSelectionDialog(Frame parent_frame, ItemSet p_items_set, ResourceBundle p_i18n, String p_prefix) {
		// parent_frame ... nadřazené okno
		// p_items_set ... zdroj položek
        // p_i18n ... objekt pro přístup k lokalizovaným zprávám

        super(parent_frame, "", true); // zavolám konstruktor rodičovského objektu JDialog; true znamená ano, modální

		items_set = p_items_set; // nastavení globálních proměnných podle parametrů
		i18n = p_i18n;
		prefix = p_prefix;

		super.setTitle(getWindowTitle()); // titulek okna

		show_edit_line_item_name = true; // během tvorby objektu je použit i editační řádek pro jméno prvku
		show_delete_button = true; // během tvorby objektu je použito i tlačítko pro odstraňování prvku

		selected_name = ""; // implicitní výstupní hodnoty
		selected_value = "";
		selected_comment = "";

		items_list_model = new DefaultListModel();

		fillItemsListModel(items_list_model,p_items_set); // naplní seznam položkami z vloženého seznamu
		items_list = new JList(items_list_model);

		items_list_scroll_pane = new JScrollPane(items_list);

        createDisplayValueArea(panel_item_display_value); // vytvoří se GUI objekty pro zobrazování obsahu prvku

		try {
            jbInit();
            pack();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

		items_list.addListSelectionListener(this);
		items_list.addMouseListener(this);
		button_select.addActionListener(this);
		button_cancel.addActionListener(this);
		button_delete.addActionListener(this);
		text_field_item_name.addActionListener(this);
		item_text_field_comment.addActionListener(this);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // zavření okna si ohlídám a zruším výběr položky
		this.addWindowListener(this);
        check_preview.addActionListener(this);
    }

    void jbInit() throws Exception {
        item_text_field_comment = new JTextField();
        panel_item_display = new JPanel();
        panel_main.setLayout(new BorderLayout());
        panel_head.setLayout(new BorderLayout());
        label_user_appeal.setHorizontalAlignment(SwingConstants.CENTER);
        label_user_appeal.setText(getUserAppeal());
		label_user_appeal.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        panel_work.setLayout(borderLayout1);
        panel_work_center.setLayout(borderLayout2);
        panel_work_west.setLayout(borderLayout3);
        panel_items.setLayout(new BorderLayout());
        panel_item_display_comment.setLayout(new BorderLayout());
        panel_item_display.setLayout(new BorderLayout());
        button_select.setText(getActionButtonLabel());
        button_cancel.setText(getCancelButtonLabel());
        panel_items_list.setBorder(BorderFactory.createTitledBorder(getListTitle()));
        panel_items_list.setLayout(new BorderLayout());
        button_delete.setText(getDeleteButtonLabel());
        button_delete.setToolTipText(getDeleteButtonToolTip());
        check_preview.setText(getCheckPreviewTitle());
        check_preview.setSelected(check_preview_default);
        check_preview_panel.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        panel_head.add(label_user_appeal, BorderLayout.NORTH);
        panel_head.add(text_field_item_name, BorderLayout.SOUTH);
        getContentPane().add(panel_main);
        panel_main.add(panel_head,  BorderLayout.NORTH);
        panel_main.add(panel_work,  BorderLayout.CENTER);// aby se při objevení lišty příliš nezmenšil viewport
        panel_work.add(panel_work_west,  BorderLayout.WEST);
        panel_work.add(panel_work_center,  BorderLayout.CENTER);
        panel_item_display.add(item_display_value_scroll_pane, BorderLayout.CENTER);
        panel_item_display.add(item_display_comment_scroll_pane, BorderLayout.SOUTH);
        panel_item_display_comment.add(item_text_field_comment, BorderLayout.CENTER);
        item_display_value_scroll_pane.setBorder(BorderFactory.createTitledBorder(getDisplayAreaTitle()));
        item_display_value_scroll_pane.getViewport().add(panel_item_display_value, null);
        item_display_comment_scroll_pane.getViewport().add(panel_item_display_comment, null);
        item_display_comment_scroll_pane.getViewport().setPreferredSize(new Dimension(100,25)); // aby se při objevení lišty příliš nezmenšil viewport
        item_display_comment_scroll_pane.setBorder(BorderFactory.createTitledBorder(getCommentTitle()));
        panel_items.add(panel_item_display, BorderLayout.CENTER);
        panel_work_center.add(panel_buttons,  BorderLayout.SOUTH);
        panel_buttons.add(button_select, null);
        panel_buttons.add(button_cancel, null);
        panel_work_center.add(panel_items,  BorderLayout.CENTER);
        panel_items_list.add(items_list_scroll_pane, BorderLayout.CENTER);
        panel_items_list.add(button_delete, BorderLayout.SOUTH);
        check_preview_panel.add(check_preview, BorderLayout.NORTH);
        panel_work_west.add(check_preview_panel,  BorderLayout.SOUTH);
        panel_work_west.add(panel_items_list,  BorderLayout.CENTER);

    }

	public boolean showOpenDialog() {
        setEnabledEditCommentLine(false); // při nahrávání není možno měnit komentář
        showEditNameLine(false); // ani není možno vkládat jméno prvku
        check_overwrite = false; // není co přepsat, takže není na co se uživatele ptát
	    this.setVisible(true);
		return return_value;
	}

	public boolean showSaveDialog() {
        setEnabledEditCommentLine(true); // umožním měnit komentář prvku
        showEditNameLine(true); // umožním vložit jméno prvku
        check_overwrite = true; // zeptám se uživatele na potvrzení před přepsáním
	    this.setVisible(true);
		return return_value;
	}

	private void fillItemsListModel (DefaultListModel model, ItemSet items) {
		if (items == null) return;
		Iterator iterator = items.getIteratorOverNames();
		String item_name;
		while (iterator.hasNext()) { // přečtu všechny prvky
			item_name = (String)iterator.next(); // další prvek
			model.addElement(item_name);
		}
	}

	public void valueChanged (ListSelectionEvent e) {
		Object zdroj = e.getSource();
		if (zdroj == items_list) { // vybrání položky - je potřeba její obsah zobrazit
            displayItem();
            return;
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object zdroj = e.getSource();
		if (zdroj == button_select) { // výběr jména prvku
		    performSelection();
		}
		else if (zdroj == button_cancel) { // zrušení výběru
		    cancelSelection();
		}
		else if (zdroj == button_delete) { // odstranění prvku ze seznamu
		    deleteSelectedItem();
		}
        else if (zdroj == text_field_item_name) { // jméno prvku vloženo a potvrzeno
            performSelection();
        }
        else if (zdroj == item_text_field_comment) { // jméno prvku vloženo a potvrzeno
            performSelection();
        }
        else if (zdroj == check_preview) { // uživatel změnil nastavení zobrazování preview
            checkPreviewChanged();
        }
	}

    public void mouseClicked (MouseEvent e) { // kliknutí myší

		Object zdroj = e.getSource();
    	int pocet_kliku = e.getClickCount();

		if (zdroj == items_list) {
		    if (pocet_kliku == 2) { // výběr jména prvku
				performSelection();
		    }
	    }
	}

    public void mousePressed (MouseEvent e) {return;}
    public void mouseExited (MouseEvent e) {return;}
    public void mouseReleased (MouseEvent e) {return;}
    public void mouseEntered (MouseEvent e) {return;}

    public void windowClosing(WindowEvent e) { // reakce na zavření okna uživatelem - zrušení výběru
	    cancelSelection();
    }

	public void windowDeactivated(WindowEvent e) {return;}
	public void windowActivated(WindowEvent e) {return;}
	public void windowDeiconified(WindowEvent e) {return;}
	public void windowIconified(WindowEvent e) {return;}
	public void windowClosed(WindowEvent e) {return;}
	public void windowOpened(WindowEvent e) {return;}



	private void cancelSelection() { // uživatel zrušil výběr
		selected_name = "";
		selected_value = "";
		return_value = false; // výběr zrušen
		this.setVisible(false);
	}

    private void displayItem() { // zobrazí obsah prvku a komentář prvku
        if (items_list.isSelectionEmpty()) {
            displayItemValue(panel_item_display_value, null); // smazat plochu pro zobrazení položky
            displayItemComment(panel_item_display_comment, null);
            return;
        }
        String item_name = (String)items_list.getSelectedValue();
        //System.out.print ("\nSingle click na seznamu jmen prvků - vybráno jméno " + item_name);
        if (check_preview.isSelected()) { // má se zobrazovat obsah prvku
            String item_value = items_set.getItemValue(item_name);
            //System.out.print ("\nObsah té položky je:\n" + item_value);
            displayItemValue(panel_item_display_value, item_value);
        }
        else { // nemá se zobrazovat obsah prvku
            displayItemValue(panel_item_display_value, null); // tak smažu plochu, kdyby tam už předtím něco bylo
        }
        String item_comment = items_set.getItemComment(item_name);
        displayItemComment(panel_item_display_comment, item_comment);
        text_field_item_name.setText(item_name);
    } // displayItem

    private void deleteSelectedItem() { // odstraní vybraný prvek ze seznamu prvků
        if (items_set == null) return; // není odkud mazat

        int index = items_list.getSelectedIndex();
        if (index == -1) return; // není vybrán žádný prvek

        String name = (String)items_list_model.getElementAt(index); // získám jméno vybraného prvku
        items_set.removeItem(name); // odstraním ho z dodaného seznamu

        items_list_model.removeElementAt(index); // smazání prvku ze zobrazeného seznamu
        displayItemValue(panel_item_display_value,null); // smazání zobrazeného obsahu prvku
		displayItemComment(panel_item_display_comment, null); // smazání zobrazeného komentáře prvku
    }

	private void performSelection() { // uživatel provedl výběr
		if (!show_edit_line_item_name && items_list.isSelectionEmpty()) { // pokud není vybrán ani zapsán žádný prvek
			return;
		}

		String item_name = text_field_item_name.getText();
		//System.out.print ("\nDouble click na seznamu jmen prvků - na prvku " + item_name);
		String item_value = "";
		if (items_set != null) item_value = items_set.getItemValue(item_name);
   		//System.out.print ("\nObsah té položky je:\n" + item_value);
		if (check_overwrite) {
		    if (items_list_model.contains(item_name)) { // je-li dané jméno již v seznamu jmen
			    int overwrite = checkOverwrite(item_name);
				if (overwrite != JOptionPane.YES_OPTION) return; // nepřepsat, tzn. nic nedělat
			}
		}
		selected_name = item_name;
   		selected_value = item_value;
        selected_comment = item_text_field_comment.getText();
		return_value = true; // výběr proveden
    	this.setVisible(false);
	}

	private int checkOverwrite (String item_name) { // dotáži se uživatele, zda chce přepsat již existující prvek
        JFrame frame = new JFrame ();
        Object[] options = {getCheckOverwriteYesButtonLabel(),
                            getCheckOverwriteCancelButtonLabel()};
        int n = JOptionPane.showOptionDialog(frame,
                  getCheckOverwriteAppeal(),
                  getCheckOverwriteWindowTitle(),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.QUESTION_MESSAGE,
                  null,     //don't use a custom Icon
                  options,  //the titles of buttons
                  options[0]); //default button title
        return n;
    } // checkOverwrite

    private void checkPreviewChanged() {
        displayItem(); // zavolám funkci na zobrazení prvku
        return;
    } // checkPreviewChanged

	// následující funkce slouží k nastavení vzhledu dialogového okna

	public Dimension getPreferredSize() {
		return new Dimension(500,400);
	}

    public void setPreview(boolean preview) { // nastaví hodnotu přepínače preview
        check_preview.setSelected(preview);
    }

	protected String getLocalizedString(String text_name, String default_value) {
		// vrátí lokalizovaný text ze zdroje i18n s prefixem prefix a suffixem text_name;
		// pokud se nezdaří, vrátí default_value

		String text;
		try {
		    text = i18n.getString(prefix + text_name);
		}
		catch (Exception e) {
		    text = default_value; // použije se, pokud není definován i18n nebo pokud v něm není hledaný prvek
		}

		return text;
	}

	protected String getCheckOverwriteCancelButtonLabel() {
		return getLocalizedString("QUESTION_OVERWRITE_BUTTON_NO","cancel");
	}

	protected String getCheckOverwriteYesButtonLabel() {
		return getLocalizedString("QUESTION_OVERWRITE_BUTTON_YES","overwrite");
	}

	protected String getCheckOverwriteWindowTitle() {
		return getLocalizedString("QUESTION_OVERWRITE_TITLE","Overwrite?");
	}

	protected String getCheckOverwriteAppeal() {
		return getLocalizedString("QUESTION_OVERWRITE_APPEAL","An item with the specified name already exists. Overwrite?");
	}

	protected String getCancelButtonLabel() {
		return getLocalizedString("BUTTON_CANCEL","cancel");
	}

	protected String getActionButtonLabel() {
		return getLocalizedString("BUTTON_SELECT","select");
	}

	protected String getDeleteButtonLabel() {
		return getLocalizedString("BUTTON_DELETE","delete");
	}

	protected String getDeleteButtonToolTip() {
		return getLocalizedString("BUTTON_DELETE_TOOLTIP","remove the selected item from the list of items");
	}

	protected String getDisplayAreaTitle() {
		return getLocalizedString("TITLE_ITEM_CONTENT","item content:");
	}

	protected String getWindowTitle() {
		return getLocalizedString("TITLE_WINDOW","item selection");
	}

	protected String getUserAppeal() {
		return getLocalizedString("USER_APPEAL","Select an item name.");
	}

	protected String getListTitle() {
		return getLocalizedString("TITLE_LIST_ITEMS","items:");
	}

	protected String getCommentTitle() {
		return getLocalizedString("TITLE_COMMENT","comment:");
	}

    protected String getCheckPreviewTitle() {
        return getLocalizedString("CHECK_PREVIEW","preview");
    }

	protected void displayItemValue (JPanel p, String value) { // zobrazí hodnotu prvku v panelu p
        //System.out.print("\nJsem ve funkci displayItemValue.");
		String text_value;
		if (value == null) text_value = "";
		else text_value = value;
        //System.out.print(" Mám zobrazit:\n" + text_value);
		item_text_area_value.setText(text_value); // zobrazím hodnotu prvku
	}

	protected void displayItemComment (JPanel p, String comment) { // zobrazí komentář prvku v panelu p
		String text_comment;
		if (comment == null) text_comment = "";
		else text_comment = comment;
		item_text_field_comment.setText(text_comment); // zobrazím komentář prvku
		item_text_field_comment.revalidate(); // aby se upravily lišty
		item_display_comment_scroll_pane.revalidate(); // aby se upravily lišty
	}

	protected void createDisplayValueArea (JPanel p) { // vytvoří objekty potřebné k zobrazování obsahu prvku
	    item_text_area_value = new JTextArea();
        item_text_area_value.setEditable(false);
		p.setLayout(new BorderLayout());
		p.add(item_text_area_value,BorderLayout.CENTER);
	}

	public void showEditNameLine(boolean p_show) {
        if (p_show) {
            if (show_edit_line_item_name) { // je již takto nastaveno
                return;
            }
            else { // nastavím a zobrazím
        		show_edit_line_item_name = true;
                panel_head.add(text_field_item_name);
            }
        }
        else {
            if (! show_edit_line_item_name) { // není potřeba nic měnit
                return;
            }
            else {
                panel_head.remove(text_field_item_name);
            }
        }
	}

	public void showDeleteButton(boolean p_show) {
        if (p_show) {
            if (show_edit_line_item_name) { // je již takto nastaveno
                return;
            }
            else { // nastavím a zobrazím
        		show_delete_button = true;
                panel_items_list.add(button_delete);
            }
        }
        else {
            if (! show_delete_button) { // není potřeba nic měnit
                return;
            }
            else {
                panel_items_list.remove(button_delete);
            }
        }
	}

    public void setEnabledEditCommentLine(boolean p_enable) {
        enable_edit_line_item_comment = p_enable;
        if (p_enable) item_text_field_comment.setEnabled(true);
        else item_text_field_comment.setEnabled(false);
    }

	// následující funkce slouží pro získání výsledku uživatelova výběru

	public String getSelectedName() {
		return selected_name;
	}

	public String getSelectedValue() {
		return selected_value;
	}

	public String getSelectedComment() {
		return selected_comment;
	}

}