

// ====================================================================================================
//		class PanelDebug			vypis ladicich informaci
// ====================================================================================================

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Class PanelDebug creates a panel for displaying debug messages from all parts of the program.
 */
public class PanelDebug extends JPanel implements ActionListener {

    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám

    JTextArea text_debug; // textarea pro debugovaci informace
    JButton debug_button_clear; // button to clear all displayed messages

    public PanelDebug(ResourceBundle p_i18n) { // konstruktor
        i18n = p_i18n;

        setLayout(new BorderLayout());
        debug_button_clear=new JButton(i18n.getString("DEBUG_BUTTON_CLEAR"));
        debug_button_clear.setToolTipText(i18n.getString("DEBUG_BUTTON_CLEAR_TOOLTIP"));
        debug_button_clear.addActionListener(this);

        text_debug = new JTextArea(); // textová oblast pro debugovací informace s iniciálním textem
        text_debug.setEditable(false);
        JScrollPane scroll_debug = new JScrollPane(text_debug); // scrollpane pro textovou oblast
        scroll_debug.setBorder(BorderFactory.createTitledBorder(i18n.getString("DEBUG_INFO_BORDER")));
        add(scroll_debug,BorderLayout.CENTER);
        add(debug_button_clear,BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        Object zdroj = e.getSource();

        if (zdroj == debug_button_clear) { // vymazani zprav
            text_debug.setText("");
            //debug ("Stisknuto tlačítko 'debug_button_clear' pro vymazání zobrazených zpráv");
        }
    }

    public void debug(String s) { // vypise ladici informaci do textove oblasti
        text_debug.append(s);
        text_debug.updateUI();
    }

} // class PanelDebug


