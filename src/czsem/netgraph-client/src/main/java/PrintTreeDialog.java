import java.awt.*;
import java.awt.print.*; // printing on a printer
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.trees.*;


/**
 * Title: PrintTreeDialog - print properties selection
 * Description: Dialog window allowing user to select print properties
 * Copyright: Copyright (c) 2001
 * Company: Charles University in Prague, CKL
 * @author Jiří Mírovský
 * @version 1.0
 */

/**
 * Class PrintTreeDialog displays a dialog window with options for printing a result tree to a printer.
 * Available fonts and printers are retrieved from the system and offered to the user.
 */
public class PrintTreeDialog extends JDialog implements ActionListener, WindowListener {
	JComboBox combo_font_size; // výběr velikosti fontu
	JComboBox combo_font_family; // výběr rodiny fontu
	//JComboBox combo_character_coding; // výběr kódování znaků
	JCheckBox check_center; // tlačítko pro centrování stromu na stránce
	JCheckBox check_keep_ratio; // tlačítko pro zachování poměru stran při zmenšování stromu
	JCheckBox check_background; // tlačítko pro tisk na pozadí (ve vlastním vlákně)
    JCheckBox check_black_white; // tlačítko pro černobílý tisk
    JButton button_page; // tlačítko pro otevření dialogu s nastavením stránky
	JButton button_print; // tlačítko pro otevření dialogu tisku s výběrem tiskárny
	JButton button_cancel; // zrušení akce
	JButton button_help; // tlačítko pro zobrazení nápovědy k tisku
	boolean return_value;
	PrintTreeProperties properties; // vlastnosti tisku
	PrinterJob printer_job; // job pro tisk na tiskárnu

	ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
	ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám

	// ještě chybí výběr typu kódování, výběr jemnosti při grafickém kódování
	// v budoucnu tisk více stromů na stránce

	PrintTreeDialog (JFrame parent, String title, boolean modal, PrintTreeProperties p_properties,
	                 ShowMessagesAble p_mess, ResourceBundle p_i18n) {
	    super (parent, title, modal);
		this.setSize(400,300);
		this.addWindowListener(this);

		mess = p_mess;
		i18n = p_i18n;

		properties = p_properties; // uchovám si ukazatel na properties i přes konec konstruktoru

        button_page = new JButton (i18n.getString("DIALOG_PRINT_TREE_PAGE_SETUP"));
        button_cancel = new JButton (i18n.getString("DIALOG_PRINT_TREE_CANCEL"));
        button_help = new JButton (i18n.getString("DIALOG_PRINT_TREE_HELP"));
        button_print = new JButton (i18n.getString("DIALOG_PRINT_TREE_PRINT"));
	    button_page.addActionListener(this);
	    button_cancel.addActionListener(this);
	    button_help.addActionListener(this);
	    button_print.addActionListener(this);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		JLabel label_font_size = new JLabel (i18n.getString("DIALOG_PRINT_TREE_FONT_SIZE_LABEL"));
		String[] font_sizes = {"8","10","12","14"};
		combo_font_size = new JComboBox(font_sizes);
		combo_font_size.setEditable(true);
		combo_font_size.addActionListener(this);
		combo_font_size.getModel().setSelectedItem(new Integer(properties.getFontSize()));

		JLabel label_font_family = new JLabel (i18n.getString("DIALOG_PRINT_TREE_FONT_FAMILY_LABEL"));
		String[] font_families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                //{"Dialog", "DialogInput", "Monospaced", "Serif", "SansSerif",};
		combo_font_family = new JComboBox(font_families);
		combo_font_family.setEditable(false);
		combo_font_family.addActionListener(this);
		combo_font_family.getModel().setSelectedItem(properties.getFontFamily());

		/*JLabel label_character_coding = new JLabel (i18n.getString("DIALOG_PRINT_TREE_CHARACTER_CODING_LABEL"));
		String[] character_codings = {
			i18n.getString("UNICODE"),
			i18n.getString("ASCII"),
			i18n.getString("SEMI_GRAPHICS"),
			i18n.getString("GRAPHICS"),
		};*/
		/*combo_character_coding = new JComboBox(character_codings);
		combo_character_coding.setEditable(false);
		combo_character_coding.addActionListener(this);
		combo_character_coding.setSelectedIndex(properties.getCharacterCoding());*/

	    check_center = new JCheckBox(i18n.getString("DIALOG_PRINT_TREE_CENTER_LABEL"),properties.getCenter());
	    check_keep_ratio = new JCheckBox(i18n.getString("DIALOG_PRINT_TREE_KEEP_RATIO_LABEL"),properties.getKeepRatio());
	    check_background = new JCheckBox(i18n.getString("DIALOG_PRINT_TREE_BACKGROUND_LABEL"),properties.getBackground());
        check_black_white = new JCheckBox(i18n.getString("DIALOG_PRINT_TREE_BLACK_WHITE_LABEL"),properties.getBackground());
	    check_center.addActionListener(this);
		check_keep_ratio.addActionListener(this);
		check_background.addActionListener(this);
		check_background.setEnabled(true);
        check_black_white.addActionListener(this);
        check_black_white.setEnabled(true);

	    // výběr fontu
		JPanel fontFamilyPane = new JPanel();
		fontFamilyPane.setLayout(new BoxLayout(fontFamilyPane, BoxLayout.X_AXIS));
		fontFamilyPane.add(Box.createRigidArea(new Dimension(5,0)));
		fontFamilyPane.add(label_font_family);
		fontFamilyPane.add(Box.createRigidArea(new Dimension(5,0)));
		fontFamilyPane.add(combo_font_family);
		fontFamilyPane.add(Box.createHorizontalGlue());
		JPanel fontSizePane = new JPanel();
		fontSizePane.setLayout(new BoxLayout(fontSizePane, BoxLayout.X_AXIS));
		fontSizePane.add(Box.createRigidArea(new Dimension(5,0)));
		fontSizePane.add(label_font_size);
		fontSizePane.add(Box.createRigidArea(new Dimension(5,0)));
		fontSizePane.add(combo_font_size);
		fontSizePane.add(Box.createHorizontalGlue());
		/*JPanel characterCodingPane = new JPanel();
		characterCodingPane.setLayout(new BoxLayout(characterCodingPane, BoxLayout.X_AXIS));
		characterCodingPane.add(Box.createRigidArea(new Dimension(5,0)));
		characterCodingPane.add(label_character_coding);
		characterCodingPane.add(Box.createRigidArea(new Dimension(3,0)));
		characterCodingPane.add(combo_character_coding);
		characterCodingPane.add(Box.createHorizontalGlue());*/

	    JPanel fontSelectionPane = new JPanel();
		fontSelectionPane.setBorder(BorderFactory.createTitledBorder(i18n.getString("DIALOG_PRINT_TREE_FONT_SELECTION")));
		fontSelectionPane.setLayout(new BoxLayout(fontSelectionPane, BoxLayout.Y_AXIS));
		fontSelectionPane.add(Box.createRigidArea(new Dimension(0,5)));
	    fontSelectionPane.add(fontFamilyPane);
        fontSelectionPane.add(Box.createRigidArea(new Dimension(0,2)));
	    fontSelectionPane.add(fontSizePane);
        fontSelectionPane.add(Box.createRigidArea(new Dimension(0,3)));
	    //fontSelectionPane.add(characterCodingPane);
		//fontSelectionPane.add(Box.createRigidArea(new Dimension(0,5)));

		// další nastavení

	    JPanel centerPane = new JPanel();
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.X_AXIS));
		centerPane.add(Box.createRigidArea(new Dimension(5,0)));
		centerPane.add(check_center);
		centerPane.add(Box.createHorizontalGlue());
	    JPanel keepRatioPane = new JPanel();
		keepRatioPane.setLayout(new BoxLayout(keepRatioPane, BoxLayout.X_AXIS));
		keepRatioPane.add(Box.createRigidArea(new Dimension(5,0)));
		keepRatioPane.add(check_keep_ratio);
		keepRatioPane.add(Box.createHorizontalGlue());
        JPanel blackWhitePane = new JPanel();
        blackWhitePane.setLayout(new BoxLayout(blackWhitePane, BoxLayout.X_AXIS));
        blackWhitePane.add(Box.createRigidArea(new Dimension(5,0)));
        blackWhitePane.add(check_black_white);
        blackWhitePane.add(Box.createHorizontalGlue());
	    JPanel backgroundPane = new JPanel();
		backgroundPane.setLayout(new BoxLayout(backgroundPane, BoxLayout.X_AXIS));
		backgroundPane.add(Box.createRigidArea(new Dimension(5,0)));
		backgroundPane.add(check_background);
		backgroundPane.add(Box.createHorizontalGlue());

		JPanel propertiesPane = new JPanel();
		propertiesPane.setLayout(new BoxLayout(propertiesPane, BoxLayout.Y_AXIS));
		propertiesPane.add(Box.createRigidArea(new Dimension(0,5)));
		propertiesPane.add(centerPane);
		propertiesPane.add(keepRatioPane);
        propertiesPane.add(blackWhitePane);
		propertiesPane.add(backgroundPane);
		propertiesPane.add(Box.createRigidArea(new Dimension(0,5)));
		propertiesPane.setBorder(BorderFactory.createTitledBorder(i18n.getString("DIALOG_PRINT_TREE_OTHER_PROPERTIES")));;

	    JPanel mainPane = new JPanel();
		mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
		mainPane.add(Box.createRigidArea(new Dimension(0,5)));
		mainPane.add(fontSelectionPane, BorderLayout.NORTH);
		mainPane.add(Box.createRigidArea(new Dimension(0,5)));
		mainPane.add(propertiesPane, BorderLayout.CENTER);
		mainPane.add(Box.createRigidArea(new Dimension(0,5)));

		//Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonPane.add(button_page);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(button_print);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(button_cancel);
		//buttonPane.add(button_help); // kvůli modalitě okna to dobře nejde

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();

		contentPane.add(mainPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
	}

	public boolean show(PrinterJob p_printer_job) { // zobrazení dialogového okna
		printer_job = p_printer_job;
	    this.setVisible(true);
		return return_value;
	}

	private void debug (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
	    if (mess != null) {
		    mess.debug(message);
		}
	}

	private void inform (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
	    if (mess != null) {
		    mess.inform (message);
		}
	}

	// zpracování událostí

    public void windowClosing(WindowEvent e) { // reakce na zavření okna uživatelem
	    // debug ("\nUživatel zavírá dialogové okno tisku.");
		return_value = false;
	    this.dispose();
	}
	public void windowDeactivated(WindowEvent e) {return;}
	public void windowActivated(WindowEvent e) {return;}
	public void windowDeiconified(WindowEvent e) {return;}
	public void windowIconified(WindowEvent e) {return;}
	public void windowClosed(WindowEvent e) {return;}
	public void windowOpened(WindowEvent e) {return;}

	public void actionPerformed (ActionEvent e) { // akce
	   	Object zdroj = e.getSource();

    	if (zdroj == button_print) { // potvrzeni tisku
			// debug ("\nStisknuto tlačítko 'button_print' (tisk)");
			return_value = true;
			this.dispose();
		}

    	else if (zdroj == button_page) { // vyvolání okna pro nastavení papíru
			// debug ("\nStisknuto tlačítko 'button_page' (nastavení papíru k tisku)");
			PageFormat pfnew = printer_job.pageDialog(properties.getPageFormat());
			properties.setPageFormat(pfnew);
		}

    	else if (zdroj == button_cancel) { // zruseni tisku
			// debug ("\nStisknuto tlačítko 'button_cancel' (zrušení akce tisku)");
			return_value = false;
			this.dispose();
		}

    	else if (zdroj == button_help) { // vyvolání nápovědy k tisku
			// debug ("\nStisknuto tlačítko 'button_help' (nápověda k tisku)");
           // zobrazNapovedu (client_install_url + "/doc/netgraph_manual.html#chap3");
		}

		else if (zdroj == check_center) { properties.setCenter(check_center.isSelected()); }
		else if (zdroj == check_keep_ratio) { properties.setKeepRatio(check_keep_ratio.isSelected()); }
		else if (zdroj == check_background) { properties.setBackground(check_background.isSelected()); }
        else if (zdroj == check_black_white) { properties.setBlackWhite(check_black_white.isSelected()); }
		else if (zdroj == combo_font_family) { properties.setFontFamily(combo_font_family.getSelectedItem().toString()); }
		else if (zdroj == combo_font_size) {
			String s = combo_font_size.getSelectedItem().toString();
			Integer i = new Integer(s);
			properties.setFontSize(i.intValue());
		}
		/*else if (zdroj == combo_character_coding) {
			int coding = combo_character_coding.getSelectedIndex();
		    properties.setCharacterCoding(coding);
			if (coding == CharCode.coding_semi_graphics) { // musím nastavit pevnou šířku a určitou velikost písma
			    //debug("\nMusím nastavit pevnou šířku fontu a velikost 10");
				combo_font_size.getModel().setSelectedItem("10");
				combo_font_family.getModel().setSelectedItem("MonoSpaced");
				combo_font_family.setEnabled(false);
				combo_font_size.setEnabled(false);
				combo_font_size.setEditable(false);
			}
			else {
				combo_font_family.setEnabled(true);
				combo_font_size.setEnabled(true);
				combo_font_size.setEditable(true);
				repaint();
			}
		}*/
    }	// actionPerformed
} // class PrintTreeDialog
