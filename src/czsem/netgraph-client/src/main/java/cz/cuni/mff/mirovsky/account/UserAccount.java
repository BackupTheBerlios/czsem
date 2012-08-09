/*
 * UserAccount.java
 *
 * Created on 3. červen 2002, 12:14
 */

package cz.cuni.mff.mirovsky.account;

import java.util.ResourceBundle;

import cz.cuni.mff.mirovsky.*;

/**
 * Class UserAccount keeps information about the user account - the authentication and authorization information.
 */
public class UserAccount extends java.lang.Object {

    public static final int account_type_anonymous = 0;
    public static final int account_type_user = 1;
    
    private static final char server_communication_fields_delimiter = (char)13;
    private static final char server_communication_label_delimiter = ':';
    private static final String account_type_label = "account_type";
    private static final String root_directory_label = "root_directory";
    private static final String max_number_of_trees_label = "max_number_of_trees";
    private static final String client_save_trees_permission_label = "client_save_trees_permission";
    private static final String change_password_permission_label = "change_password_permission";
    private static final String user_name_label = "user_name";
    private static final String yes_value = "yes";
    private static final String no_value = "no";
    private static final String user_account_type_server_value = "user";
    private static final String anonymous_account_type_server_value = "anonymous";

    private static final String default_login_name = "anonymous";
    private static final long default_max_number_of_trees = 1000;
    private static final int default_account_type = account_type_anonymous;
    private static final String default_root_directory = "default";
    private static final boolean default_client_save_trees_permission = false;
    private static final boolean default_change_password_permission = false;
    private static final String default_user_name = "anybody";    
    
    private String login_name;    
    private int account_type;    
    private String root_directory;    
    private long max_number_of_trees;    
    private boolean client_save_trees_permission;    
    private boolean change_password_permission;    
    private String user_name;

    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    
    /** Creates new UserAccount */
    public UserAccount(ShowMessagesAble p_mess, ResourceBundle p_i18n) {
        mess = p_mess;
        i18n = p_i18n;
        
        setDefaultValues();
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

    public void setDefaultValues() {
        login_name = default_login_name;
        account_type = default_account_type;
        root_directory = default_root_directory;
        max_number_of_trees = default_max_number_of_trees;
        client_save_trees_permission = default_client_save_trees_permission;
        change_password_permission = default_change_password_permission;
        user_name = default_user_name;
    }
    
    public String getLoginName() {
        return login_name;
    }
    
    public void setLoginName(String name) {
        login_name = new String(name);
    }
    
    public int getAccountType() {
        return account_type;
    }
    
    public String getRootDirectory() {
        return root_directory;
    }
    
    public long getMaxNumberOfTrees() {
        return max_number_of_trees;
    }
    
    public boolean getClientSaveTreesPermission() {
        return client_save_trees_permission;
    }
    
    public boolean getChangePasswordPermission() {
        return change_password_permission;
    }
    
    public String getUserName() {
        return user_name;
    }

    private String readFieldFromBytes(byte [] src, int position) {
        String field = "";
        int pos = position;
        int ch;
        char chch;
        ch = src[pos++];
        while (ch != server_communication_fields_delimiter) {
            chch = (char)ch;
            field += chch;
            ch = src[pos++];
        }
        if (pos - 1 == position) { // the only read character was the delimiter, so it is end of fields
            return null;
        }
        return field;
    } // readFieldFromBytes
    
    private String readLabel(String field) throws ServerCommunicationFormatErrorException {
        int pos = field.indexOf(server_communication_label_delimiter);
        if (pos == -1) throw new ServerCommunicationFormatErrorException("account authorization information bad format");
        return field.substring(0,pos);
    } // readLabel

    private String readValue(String field) throws ServerCommunicationFormatErrorException {
        int pos = field.indexOf(server_communication_label_delimiter);
        if (pos == -1) throw new ServerCommunicationFormatErrorException("account authorization information bad format");
        return field.substring(pos+1);
    } // readValue
    
    public int readFromBytes(byte [] src, int start) throws ServerCommunicationFormatErrorException {
        String label, field, value;
        int position = start;
        while ((field = readFieldFromBytes(src,position)) != null) { // over all fields specified in the message from server
            // after the last field is one more field delimiter
            position += field.length() + 1; // count the fields delimiter as well
            label = readLabel(field);
            value = readValue(field);
            //debug("\npřečteno: " + label + " = " + value);
            if (label.equalsIgnoreCase(account_type_label)) {
                if (value.equalsIgnoreCase(user_account_type_server_value)) {
                    account_type = account_type_user;
                }
                else {
                    account_type = account_type_anonymous;
                }
            }
            else if (label.equalsIgnoreCase(root_directory_label)) {
                root_directory = value;
            }
            else if (label.equalsIgnoreCase(max_number_of_trees_label)) {
                try {
                    max_number_of_trees = Long.parseLong(value);
                }
                catch (Exception e) {
                    max_number_of_trees = default_max_number_of_trees;
                }
            }
            else if (label.equalsIgnoreCase(client_save_trees_permission_label)) {
                if (value.equalsIgnoreCase(yes_value)) {
                    client_save_trees_permission = true;
                }
                else {
                    client_save_trees_permission = false;
                }
            }    
            else if (label.equalsIgnoreCase(change_password_permission_label)) {
                if (value.equalsIgnoreCase(yes_value)) {
                    change_password_permission = true;
                }
                else {
                    change_password_permission = false;
                }
            }    

            else if (label.equalsIgnoreCase(user_name_label)) {
                user_name = value;
            }
            //else { // unknown field
            //    
            //}
        } // while over all fields specified in the message
        return position + 1; // count the last delimiter as well
    } // readFromBytes
} // class UserAccount
