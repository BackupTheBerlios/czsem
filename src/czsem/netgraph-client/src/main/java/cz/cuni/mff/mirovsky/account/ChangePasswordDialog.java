/*
 * ChangePasswordDialog.java
 *
 * Created on 11. červen 2002, 10:16
 */
// ====================================================================================================
//		class ChangePasswordDialog			zmena uzivatelova hesla
// ====================================================================================================

/**
 *
 * @author  mirovsky
 * @version 
 */

package cz.cuni.mff.mirovsky.account;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import cz.cuni.mff.mirovsky.*;

/**
 * This class displays a dialog window for changing a users's password.
 */
public class ChangePasswordDialog extends JDialog implements ActionListener {

    private JLabel appeal_old_password;
    private JPasswordField old_password_field;
    private JLabel confirm_warning;
    private JPasswordField new_password_field;
    private JPasswordField confirm_password_field;
    private JButton button_change;
    private JButton button_cancel;
    private boolean return_value;
    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    //JButton button_help;

    /** Creates new ChangePasswordDialog */
    public ChangePasswordDialog(Frame parent, String title, boolean modal,
                             ShowMessagesAble p_mess, ResourceBundle p_i18n) {
                                 
  	    super (parent, title, modal);
       	this.setSize(330,260);

        mess = p_mess;
        i18n = p_i18n;

  		appeal_old_password = new JLabel(i18n.getString("DIALOG_CHANGE_PASSWORD_APPEAL_OLD_PASSWORD"));
       	appeal_old_password.setAlignmentX(0.5f);
   		button_change = new JButton (i18n.getString("DIALOG_CHANGE_PASSWORD_BUTTON_CHANGE"));
       	button_cancel = new JButton (i18n.getString("DIALOG_CHANGE_PASSWORD_BUTTON_CANCEL"));
        //button_help = new JButton (i18n.getString("DIALOG_CHANGE_PASSWORD_HELP"));
   		button_change.addActionListener(this);
       	button_cancel.addActionListener(this);
        //button_help.addActionListener(this);

   		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        old_password_field = new JPasswordField();
        
    	JLabel appeal_new_password = new JLabel(i18n.getString("DIALOG_CHANGE_PASSWORD_APPEAL_NEW_PASSWORD"));
       	appeal_new_password.setAlignmentX(0.5f);
            
        new_password_field = new JPasswordField();
        confirm_password_field = new JPasswordField();

        confirm_warning = new JLabel();
       	confirm_warning.setAlignmentX(0.5f);
        
       	//Lay out the labels and password fields from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
  		listPane.add(appeal_old_password);
       	listPane.add(Box.createRigidArea(new Dimension(0,13)));
        listPane.add(old_password_field);
   		listPane.add(Box.createRigidArea(new Dimension(0,15)));
       	listPane.add(appeal_new_password);
        listPane.add(Box.createRigidArea(new Dimension(0,13)));
   		listPane.add(new_password_field);
       	listPane.add(Box.createRigidArea(new Dimension(0,5)));
   		listPane.add(confirm_password_field);
       	listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(confirm_warning);
       	listPane.add(Box.createRigidArea(new Dimension(0,5)));
   		listPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

		//Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(button_change);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(button_cancel);
		//buttonPane.add(button_help);

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.NORTH);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
	} // ChangePasswordDialog

	public boolean show(String title, boolean first) { // zobrazení dialogového okna
        setTitle(title);
        if (first) { // změna prázdného hesla - první nastavení hesla
            old_password_field.setEnabled(false);
            appeal_old_password.setEnabled(false);
        }
        else { // změna neprázdného hesla
            old_password_field.setEnabled(true);
            appeal_old_password.setEnabled(true);
        }            
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
                
    private boolean equalPasswords(char[] first, char[] second) { // porovná dvě daná hesla
        int length_first = first.length;
        int length_second = second.length;
        if (length_first != length_second) return false;
        for (int i=0; i<length_first; i++) {
            if (first[i] != second[i]) return false;
        }
        return true;
    } // equalPasswords
    
    public void actionPerformed (ActionEvent e) { // akce
	   	Object zdroj = e.getSource();

    	if (zdroj == button_change) { // zmena hesla
			//debug ("\nStisknuto tlačítko 'button_change' (zmena hesla)");
            char[] new_password = new_password_field.getPassword();
            char[] confirmed_password = confirm_password_field.getPassword();
            if (! equalPasswords(new_password, confirmed_password)) { // špatně potvrzené heslo
    			//debug ("\nŠpatně potvrzené heslo.");
                confirm_warning.setText(i18n.getString("DIALOG_CHANGE_PASSWORD_WARNING_WRONG_CONFIRMED_PASSWORD"));
                return;
            }    
            if (new_password.length == 0) { // prázdné heslo
    			//debug ("\nPrázdné heslo není povoleno.");
                confirm_warning.setText(i18n.getString("DIALOG_CHANGE_PASSWORD_WARNING_EMPTY_PASSWORD"));
                return;
            }
            return_value = true;
			this.dispose();
		}

    	else if (zdroj == button_cancel) { // zruseni pripojeni k serveru
			//debug ("\nStisknuto tlačítko 'button_cancel' (zrušení akce změny hesla)");
			return_value = false;
            clearPasswords();
			this.dispose();
		}

    	//else if (zdroj == button_help) { // vyvolání nápovědy ke změně hesla
		//	debug ("\nStisknuto tlačítko 'button_help' (nápověda ke změně hesla)");
		//}

    }	// actionPerformed

    public char[] getOldPassword() {
        return old_password_field.getPassword();
    }
        
    public char[] getNewPassword() {
        return new_password_field.getPassword();
    }

    public void clearPasswords() {
        old_password_field.setText("");
        new_password_field.setText("");
        confirm_password_field.setText("");
        confirm_warning.setText("");
    }
    
} // class ServerConnectionDialog
