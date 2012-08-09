/*
 * ServerConnectionDialog.java
 *
 * Created on 7. červen 2002, 10:29
 */
// ====================================================================================================
//		class ServerConnectionDialog			výběr serveru a vložení jména a hesla uživatelem
// ====================================================================================================

/**
 *
 * @author  jirka
 * @version
 */

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;

import cz.cuni.mff.mirovsky.*;

/**
 * Class ServerConnectionDialog displays a dialog window for the user to enter a name of the server and a port he
 * wants to connect to, as well as a login name and a password of the user.
 */
public class ServerConnectionDialog extends JDialog implements ActionListener, WindowListener {

	private JComboBox servers_combo;
	private JComboBox ports_combo;
    private JTextField login_name_field;
    private JPasswordField password_field;
    private JButton button_connect;
    private JButton button_cancel;
    private boolean return_value;
    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    //JButton button_help;

    /** Creates new ServerConnectionDialog */
    public ServerConnectionDialog(Frame parent, String title, boolean modal,
                             ShowMessagesAble p_mess, ResourceBundle p_i18n) {

  	    super (parent, title, modal);
       	this.setSize(500,280);

        mess = p_mess;
        i18n = p_i18n;

  		JLabel vyzvaServer = new JLabel(i18n.getString("DIALOG_SELECT_SERVER_REQUEST"));
       	vyzvaServer.setAlignmentX(0.5f);
   		button_connect = new JButton (i18n.getString("DIALOG_SELECT_SERVER_CONNECT"));
       	button_cancel = new JButton (i18n.getString("DIALOG_SELECT_SERVER_CANCEL"));
        //button_help = new JButton (i18n.getString("DIALOG_SELECT_SERVER_HELP"));
   		button_connect.addActionListener(this);
       	button_cancel.addActionListener(this);
        //button_help.addActionListener(this);

   		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(this);


        servers_combo = new JComboBox();
       	servers_combo.setEditable(true);
        //servers_combo.addActionListener(...);

        ports_combo = new JComboBox();
    	ports_combo.setEditable(true);
       	//ports_combo.addActionListener(...);


    	JLabel vyzvaLogin = new JLabel(i18n.getString("DIALOG_LOGIN_REQUEST"));
       	vyzvaLogin.setAlignmentX(0.5f);

        login_name_field = new JTextField();
        login_name_field.addActionListener(this);
        password_field = new JPasswordField();
        password_field.addActionListener(this);

       	//Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
  		listPane.add(vyzvaServer);
       	listPane.add(Box.createRigidArea(new Dimension(0,13)));
        listPane.add(servers_combo);
   		listPane.add(Box.createRigidArea(new Dimension(0,5)));
       	listPane.add(ports_combo);
        listPane.add(Box.createRigidArea(new Dimension(0,15)));
   		listPane.add(vyzvaLogin);
       	listPane.add(Box.createRigidArea(new Dimension(0,13)));
   		listPane.add(login_name_field);
       	listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(password_field);
   		listPane.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

		//Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(button_connect);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(button_cancel);
		//buttonPane.add(button_help);

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.NORTH);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
	} // ServerConnectionDialog

 /*   private void changeFocus(final Component source,
        final Component target) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          target.dispatchEvent(
            new FocusEvent(source, FocusEvent.FOCUS_GAINED));
        }
      });
    }*/

	public boolean show(int nic) { // zobrazení dialogového okna
       /* if (login_name_field.getText().length() > 0 && password_field.getPassword().length == 0) {
            changeFocus(servers_combo, password_field);
        }*/
	    setVisible(true);
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

    public void windowClosing(WindowEvent e) { // reakce na zavření okna uživatelem
        chancelAction();
    }

    private void chancelAction() {
        return_value = false;
        clearPassword();
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

    	if (zdroj == button_connect) { // pripojeni k serveru
			//debug ("\nStisknuto tlačítko 'button_connect' (připojení k serveru)");
			return_value = true;
			this.dispose();
		}

    	else if (zdroj == button_cancel) { // zruseni pripojeni k serveru
			//debug ("\nStisknuto tlačítko 'button_cancel' (zrušení akce připojení k serveru)");
			chancelAction();
		}

        else if (zdroj == login_name_field) { // potvrzená změna login name
			//debug ("\nLogin name bylo změněno na " + login_name_field.getText());
            if (login_name_field.getText().equalsIgnoreCase("anonymous")) {
                password_field.setText("anonymous");
            }
            password_field.requestFocus();
        }

        else if (zdroj == password_field) { // potvrzené vložení hesla
            //debug ("\nByl stisknut enter v poli hesla");
            return_value = true;
            this.dispose();
        }

    	//else if (zdroj == button_help) { // vyvolání nápovědy k připojení k serveru
		//	debug ("\nStisknuto tlačítko 'button_help' (nápověda k připojení k serveru)");
		//}

    }	// actionPerformed

    public String getServerName() {
        return servers_combo.getSelectedItem().toString();
    }

    public int getServerPort() {
        return Integer.parseInt(ports_combo.getSelectedItem().toString());
    }

    public String getLoginName() {
        return login_name_field.getText();
    }

    public char[] getPassword() {
        return password_field.getPassword();
    }

    public void setLoginName(String name) {
        login_name_field.setText(name);
    }

    public void setPassword(String password) {
        password_field.setText(password);
    }

    public void addServerName(String server_name) {
        servers_combo.addItem(server_name);
    }

    public boolean isServerNameInCombo(String server_name) {
        int number_of_items = servers_combo.getItemCount();
        for (int i=0; i<number_of_items; i++) {
            if (server_name.equalsIgnoreCase((String)servers_combo.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }

    public void addServerPort(int server_port) {
        ports_combo.addItem(new Integer(server_port));
    }

    public boolean isServerPortInCombo(int server_port) {
        int number_of_items = ports_combo.getItemCount();
        for (int i=0; i<number_of_items; i++) {
            if (server_port == ((Integer)ports_combo.getItemAt(i)).intValue()) {
                return true;
            }
        }
        return false;
    }

    public void clearPassword() {
        setPassword("");
    }

    /**
     * Sets focus to the password field
     */
    public void focusPassword() { // does not work at all
        //password_field.setFocusCycleRoot(true);
        password_field.transferFocus();
        password_field.grabFocus();
        password_field.requestFocus();
        password_field.requestFocusInWindow(); // nic z toho nefunguje
        //debug("\nServerConnectionDialog.focusPassword: Setting focus to the password field.");
    }

    /**
     * Sets focus to the login name field
     */
    public void focusLogin() {
        login_name_field.grabFocus();

        debug("\nServerConnectionDialog.focusLogin: Setting focus to the login name field.");
    }

} // class ServerConnectionDialog
