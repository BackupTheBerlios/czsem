/*
 * LocalSaveResultTreesDialog.java
 *
 * Created on 11. září 2002, 20:22
 */

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.io.*;
import cz.cuni.mff.mirovsky.*;

/**
 * A class that displays a dialog window with options for saving result trees to a local disc.
 */
public class LocalSaveResultTreesDialog extends javax.swing.JDialog implements ActionListener, WindowListener {
    private boolean return_value;
    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám

    private String file_names_directory; // výchozí adresář pro procházení prefixů jmen souborů

    /** Creates new form LocalSaveResultTreesDialog */
    public LocalSaveResultTreesDialog(java.awt.Frame parent, String title, boolean modal, ShowMessagesAble p_mess, ResourceBundle p_i18n) {
        super(parent, title, modal);
        this.addWindowListener(this);
        mess = p_mess;
        i18n = p_i18n;
        initComponents();
        radio_new_separation.setSelected(true);
        originalFilesSetEnabled(false);
        radio_save_all_trees.setSelected(true);
        saveRangeSetEnabled(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    public boolean showDialog() { // zobrazení dialogového okna
        this.setVisible(true); // volání zděděné funkce
        return return_value;
    }

    private void debug (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
        if (mess != null) {
            mess.debug(message);
        }
    }

    //private void inform (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
    //  if (mess != null) {
    //      mess.inform (message);
    //  }
    //}

    public void setDestinationDirectory(String directory) { // nastaví adresář pro ukládání stromů
        text_destination_directory.setText(directory);
        file_names_directory = directory; // adresář pro procházení jmen souborů nastavím na stejnou hodnotu
    } // setDestinationDirectory

    public String getDestinationDirectory() { // vrátí nastavený adresář pro ukládání stromů
        return text_destination_directory.getText();
    } // getDestinationDirectory

    public void setFilesPrefix(String prefix) { // nastaví prefix jmen souborů pro ukládání stromů
        text_names_prefix.setText(prefix);
    } // setFilesPrefix

    public String getFilesPrefix() { // vrátí nastavený prefix jmen souborů pro ukládání stromů
        return text_names_prefix.getText();
    } // getFilesPrefix

    public long getRangeStart() { // vrátí nastavený začátek rozsahu ukládaných souborů
        try {
            return Long.parseLong(text_range_from.getText());
        }
        catch (NumberFormatException e) {
            debug("\nLocalSaveResultTreesDialog: getRangeStart: Cannot convert the value '" + text_range_from.getText() + "' to long int; return value set to 1");
            return (long) 1;
        }
    } // getRangeStart

    public long getRangeEnd() { // vrátí nastavený konec rozsahu ukládaných souborů; 0 znamená bez omezení

        if (radio_save_all_trees.isSelected()) return (long) 0; // neomezený rozsah

        try {
            return Long.parseLong(text_range_to.getText());
        }
        catch (NumberFormatException e) {
            debug("\nLocalSaveResultTreesDialog: getRangeEnd: Cannot convert the value '" + text_range_to.getText() + "' to long int; return value set to 0 (unlimited range)");
            return (long) 0;
        }
    } // getRangeEnd

    public long getFileSize() { // vrátí nastavený maximální počet stromů v jednom souboru; 0 znamená bez omezení

        if (radio_unlimited_size.isSelected()) return (long) 0; // neomezený počet stromů

        try {
            return Long.parseLong(text_number_of_trees.getText());
        }
        catch (NumberFormatException e) {
            debug("\nLocalSaveResultTreesDialog: getFileSize: Cannot convert the value '" + text_number_of_trees.getText() + "' to long int; return value set to 50");
            return (long) 50;
        }
    } // getFileSize

    public void setFileSize(long size) { // nastaví maximální počet stromů v jednom souboru
        text_number_of_trees.setText(""+size);
    } // setFileSize

    public boolean isSetOriginalSeparation() {
        return radio_original_separation.isSelected();
    }

    public boolean isSetNewSeparation() {
        return radio_new_separation.isSelected();
    }

    public boolean isSetOriginalNames() {
        return radio_original_names.isSelected();
    }

    public boolean isSetNewNames() {
        return radio_new_names.isSelected();
    }

    public long getCountStart() { // vrátí nastavený začátek počítání souborů

        try {
            return Long.parseLong(text_count_from.getText());
        }
        catch (NumberFormatException e) {
            debug("\nLocalSaveResultTreesDialog: getCountStart: Cannot convert the value '" + text_count_from.getText() + "' to long int; return value set to 1");
            return (long) 1;
        }
    } // getCountStart

    public boolean isCheckedPutMatchingMetaTag() {
        return check_query_match_meta_tag.isSelected();
    }

    public void setPutMatchingMetaTag(boolean checked) {
        check_query_match_meta_tag.setSelected(checked);
    }

    public boolean isCheckedSaveMultipleOccurrences() {
        return check_save_multiple_occurrences.isSelected();
    }

    public void setSaveMultipleOccurrences(boolean checked) {
        check_save_multiple_occurrences.setSelected(checked);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        group_original_new_names = new javax.swing.ButtonGroup();
        group_size_of_files = new javax.swing.ButtonGroup();
        group_original_new_separation = new javax.swing.ButtonGroup();
        group_range = new javax.swing.ButtonGroup();
        panel_save_appeal = new javax.swing.JPanel();
        saveAppealLabel = new javax.swing.JLabel();
        panel_destination_directory_outer = new javax.swing.JPanel();
        panel_destination_directory_inner = new javax.swing.JPanel();
        label_destination_directory = new javax.swing.JLabel();
        text_destination_directory = new javax.swing.JTextField();
        button_browse_destination_directory = new javax.swing.JButton();
        panel_save_options_outer = new javax.swing.JPanel();
        panel_save_options_middle = new javax.swing.JPanel();
        panel_save_options_inner = new javax.swing.JPanel();
        panel_original_separation = new javax.swing.JPanel();
        radio_original_separation = new javax.swing.JRadioButton();
        panel_original_separation_options = new javax.swing.JPanel();
        radio_original_names = new javax.swing.JRadioButton();
        radio_new_names = new javax.swing.JRadioButton();
        panel_new_separation = new javax.swing.JPanel();
        radio_new_separation = new javax.swing.JRadioButton();
        panel_new_separation_options = new javax.swing.JPanel();
        panel_maximum_size_label = new javax.swing.JPanel();
        label_maximum_size = new javax.swing.JLabel();
        panel_size_options = new javax.swing.JPanel();
        panel_unlimited_size = new javax.swing.JPanel();
        radio_unlimited_size = new javax.swing.JRadioButton();
        panel_number_of_trees = new javax.swing.JPanel();
        radio_number_of_trees = new javax.swing.JRadioButton();
        text_number_of_trees = new javax.swing.JTextField();
        panel_names_prefix = new javax.swing.JPanel();
        label_names_prefix = new javax.swing.JLabel();
        text_names_prefix = new javax.swing.JTextField();
        button_browse_names_prefix = new javax.swing.JButton();
        panel_count_from = new javax.swing.JPanel();
        label_count_from = new javax.swing.JLabel();
        text_count_from = new javax.swing.JTextField();
        panel_range_outer = new javax.swing.JPanel();
        panel_range_middle = new javax.swing.JPanel();
        panel_range_inner = new javax.swing.JPanel();
        panel_save_all_trees = new javax.swing.JPanel();
        radio_save_all_trees = new javax.swing.JRadioButton();
        panel_save_range = new javax.swing.JPanel();
        radio_save_range = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        label_range_from = new javax.swing.JLabel();
        text_range_from = new javax.swing.JTextField();
        label_range_to = new javax.swing.JLabel();
        text_range_to = new javax.swing.JTextField();
        panel_add_query_match_meta_tag = new javax.swing.JPanel();
        check_query_match_meta_tag = new javax.swing.JCheckBox();
        check_save_multiple_occurrences = new javax.swing.JCheckBox();
        panel_ok_cancel = new javax.swing.JPanel();
        button_save = new javax.swing.JButton();
        button_cancel = new javax.swing.JButton();

        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        addWindowListener(this);

        panel_save_appeal.setLayout(new java.awt.BorderLayout());

        panel_save_appeal.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 7, 5, 7)));
        saveAppealLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        saveAppealLabel.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_GENERAL_APPEAL"));
        saveAppealLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        panel_save_appeal.add(saveAppealLabel, java.awt.BorderLayout.NORTH);

        getContentPane().add(panel_save_appeal);

        panel_destination_directory_outer.setLayout(new java.awt.BorderLayout());

        panel_destination_directory_outer.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(7, 7, 0, 7)));
        panel_destination_directory_inner.setLayout(new java.awt.BorderLayout());

        label_destination_directory.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_DESTINATION_DIRECTORY"));
        label_destination_directory.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        panel_destination_directory_inner.add(label_destination_directory, java.awt.BorderLayout.WEST);

        text_destination_directory.setText(" ");

        panel_destination_directory_inner.add(text_destination_directory, java.awt.BorderLayout.CENTER);

        button_browse_destination_directory.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_BUTTON_BROWSE_DIRECTORIES"));
        button_browse_destination_directory.addActionListener(this);

        panel_destination_directory_inner.add(button_browse_destination_directory, java.awt.BorderLayout.EAST);

        panel_destination_directory_outer.add(panel_destination_directory_inner, java.awt.BorderLayout.NORTH);

        getContentPane().add(panel_destination_directory_outer);

        panel_save_options_outer.setLayout(new java.awt.BorderLayout());

        panel_save_options_outer.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(7, 7, 0, 7)));
        panel_save_options_middle.setLayout(new java.awt.BorderLayout());

        panel_save_options_middle.setBorder(new javax.swing.border.EtchedBorder());
        panel_save_options_inner.setLayout(new javax.swing.BoxLayout(panel_save_options_inner, javax.swing.BoxLayout.Y_AXIS));

        panel_save_options_inner.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        panel_original_separation.setLayout(new java.awt.BorderLayout());

        radio_original_separation.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_ORIGINAL_SEPARATION"));
        group_original_new_separation.add(radio_original_separation);
        radio_original_separation.addActionListener(this);

        panel_original_separation.add(radio_original_separation, java.awt.BorderLayout.NORTH);

        panel_original_separation_options.setLayout(new javax.swing.BoxLayout(panel_original_separation_options, javax.swing.BoxLayout.Y_AXIS));

        panel_original_separation_options.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 30, 0, 0)));
        radio_original_names.setSelected(true);
        radio_original_names.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_ORIGINAL_NAMES"));
        group_original_new_names.add(radio_original_names);
        radio_original_names.addActionListener(this);

        panel_original_separation_options.add(radio_original_names);

        radio_new_names.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_NEW_NAMES"));
        group_original_new_names.add(radio_new_names);
        radio_new_names.addActionListener(this);

        panel_original_separation_options.add(radio_new_names);

        panel_original_separation.add(panel_original_separation_options, java.awt.BorderLayout.CENTER);

        panel_save_options_inner.add(panel_original_separation);

        panel_new_separation.setLayout(new java.awt.BorderLayout());

        panel_new_separation.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 0, 0, 0)));
        radio_new_separation.setSelected(true);
        radio_new_separation.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_GIVEN_FILE_SIZE"));
        group_original_new_separation.add(radio_new_separation);
        radio_new_separation.addActionListener(this);

        panel_new_separation.add(radio_new_separation, java.awt.BorderLayout.NORTH);

        panel_new_separation_options.setLayout(new java.awt.BorderLayout());

        panel_new_separation_options.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 30, 0, 0)));
        panel_maximum_size_label.setLayout(new javax.swing.BoxLayout(panel_maximum_size_label, javax.swing.BoxLayout.Y_AXIS));

        panel_maximum_size_label.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 0, 0, 3)));
        label_maximum_size.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_FILE_SIZE"));
        label_maximum_size.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        panel_maximum_size_label.add(label_maximum_size);

        panel_new_separation_options.add(panel_maximum_size_label, java.awt.BorderLayout.WEST);

        panel_size_options.setLayout(new javax.swing.BoxLayout(panel_size_options, javax.swing.BoxLayout.Y_AXIS));

        panel_unlimited_size.setLayout(new java.awt.BorderLayout());

        radio_unlimited_size.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_UNLIMITED_FILE_SIZE"));
        group_size_of_files.add(radio_unlimited_size);
        radio_unlimited_size.addActionListener(this);

        panel_unlimited_size.add(radio_unlimited_size, java.awt.BorderLayout.NORTH);

        panel_size_options.add(panel_unlimited_size);

        panel_number_of_trees.setLayout(new javax.swing.BoxLayout(panel_number_of_trees, javax.swing.BoxLayout.X_AXIS));

        radio_number_of_trees.setSelected(true);
        radio_number_of_trees.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_NUMBER_OF_TREES"));
        group_size_of_files.add(radio_number_of_trees);
        radio_number_of_trees.addActionListener(this);

        panel_number_of_trees.add(radio_number_of_trees);

        text_number_of_trees.setText("50");
        text_number_of_trees.setPreferredSize(new java.awt.Dimension(65, 19));
        panel_number_of_trees.add(text_number_of_trees);

        panel_size_options.add(panel_number_of_trees);

        panel_new_separation_options.add(panel_size_options, java.awt.BorderLayout.CENTER);

        panel_new_separation.add(panel_new_separation_options, java.awt.BorderLayout.CENTER);

        panel_save_options_inner.add(panel_new_separation);

        panel_names_prefix.setLayout(new java.awt.BorderLayout());

        panel_names_prefix.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 0, 0, 0)));
        label_names_prefix.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_FILE_NAME_PREFIX"));
        label_names_prefix.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        panel_names_prefix.add(label_names_prefix, java.awt.BorderLayout.WEST);

        text_names_prefix.setText("result_trees");
        panel_names_prefix.add(text_names_prefix, java.awt.BorderLayout.CENTER);

        button_browse_names_prefix.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_BUTTON_BROWSE_FILES"));
        button_browse_names_prefix.addActionListener(this);

        panel_names_prefix.add(button_browse_names_prefix, java.awt.BorderLayout.EAST);

        panel_save_options_inner.add(panel_names_prefix);

        panel_count_from.setLayout(new javax.swing.BoxLayout(panel_count_from, javax.swing.BoxLayout.X_AXIS));

        label_count_from.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_START_COUNTING_FROM"));
        label_count_from.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        panel_count_from.add(label_count_from);

        text_count_from.setText("1");
        panel_count_from.add(text_count_from);

        panel_save_options_inner.add(panel_count_from);

        panel_save_options_middle.add(panel_save_options_inner, java.awt.BorderLayout.CENTER);

        panel_save_options_outer.add(panel_save_options_middle, java.awt.BorderLayout.CENTER);

        getContentPane().add(panel_save_options_outer);

        panel_range_outer.setLayout(new java.awt.BorderLayout());

        panel_range_outer.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(7, 7, 0, 7)));
        panel_range_middle.setLayout(new java.awt.BorderLayout());

        panel_range_middle.setBorder(new javax.swing.border.EtchedBorder());
        panel_range_inner.setLayout(new javax.swing.BoxLayout(panel_range_inner, javax.swing.BoxLayout.Y_AXIS));

        panel_range_inner.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));
        panel_save_all_trees.setLayout(new java.awt.BorderLayout());

        radio_save_all_trees.setSelected(true);
        radio_save_all_trees.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_SAVE_ALL_TREES"));
        group_range.add(radio_save_all_trees);
        radio_save_all_trees.addActionListener(this);

        panel_save_all_trees.add(radio_save_all_trees, java.awt.BorderLayout.NORTH);

        panel_range_inner.add(panel_save_all_trees);

        panel_save_range.setLayout(new javax.swing.BoxLayout(panel_save_range, javax.swing.BoxLayout.X_AXIS));

        radio_save_range.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_SAVE_RANGE"));
        group_range.add(radio_save_range);
        radio_save_range.addActionListener(this);

        panel_save_range.add(radio_save_range);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        label_range_from.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_RANGE_FROM"));
        label_range_from.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
        jPanel1.add(label_range_from);

        text_range_from.setText("1");
        text_range_from.setPreferredSize(new java.awt.Dimension(50, 19));
        jPanel1.add(text_range_from);

        label_range_to.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_RANGE_TO"));
        label_range_to.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        jPanel1.add(label_range_to);

        text_range_to.setText("50");
        text_range_to.setPreferredSize(new java.awt.Dimension(50, 19));
        jPanel1.add(text_range_to);

        panel_save_range.add(jPanel1);

        panel_range_inner.add(panel_save_range);

        panel_range_middle.add(panel_range_inner, java.awt.BorderLayout.WEST);

        panel_range_outer.add(panel_range_middle, java.awt.BorderLayout.NORTH);

        getContentPane().add(panel_range_outer);

        panel_add_query_match_meta_tag.setLayout(new java.awt.BorderLayout());

        panel_add_query_match_meta_tag.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(7, 7, 0, 7)));
        check_query_match_meta_tag.setSelected(true);
        check_query_match_meta_tag.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_ADD_MATCH_META_TAG"));
        panel_add_query_match_meta_tag.add(check_query_match_meta_tag, java.awt.BorderLayout.NORTH);
        check_save_multiple_occurrences.setSelected(false);
        check_save_multiple_occurrences.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_SAVE_MULTIPLE_OCCURRENCES"));
        panel_add_query_match_meta_tag.add(check_save_multiple_occurrences, java.awt.BorderLayout.SOUTH);

        getContentPane().add(panel_add_query_match_meta_tag);

        panel_ok_cancel.setLayout(new javax.swing.BoxLayout(panel_ok_cancel, javax.swing.BoxLayout.X_AXIS));

        panel_ok_cancel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 7, 7, 7)));
        button_save.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_BUTTON_SAVE"));
        button_save.addActionListener(this);

        panel_ok_cancel.add(button_save);

        button_cancel.setText(i18n.getString("LOCAL_SAVE_RESULT_TREES_BUTTON_CANCEL"));
        button_cancel.addActionListener(this);

        panel_ok_cancel.add(button_cancel);

        getContentPane().add(panel_ok_cancel);

        pack();
    }//GEN-END:initComponents

    public void actionPerformed(java.awt.event.ActionEvent e) {
        Object source = e.getSource();

        if (source == button_browse_destination_directory) { // výběr cílového adresáře
            button_browse_destination_directoryActionPerformed(e);
        }

        else if (source == radio_original_separation) { // výběr voleb pro původní rozdělení stromů do souborů
            radio_original_separationActionPerformed(e);
        }

        else if (source == radio_new_separation) { // výběr voleb pro nové rozdělení stromů do souborů
            radio_new_separationActionPerformed(e);
        }

        else if (source == radio_new_names) { // výběr voleb pro nové pojmenování souborů
            radio_new_namesActionPerformed(e);
        }

        else if (source == radio_original_names) { // výběr volby pro původní pojmenování souborů
            radio_original_namesActionPerformed(e);
        }

        else if (source == radio_unlimited_size) { // výběr volby pro uložení všech stromů do jednoho souboru
            radio_unlimited_sizeActionPerformed(e);
        }

        else if (source == radio_number_of_trees) { // výběr voleb pro omezení počtu stromů na jeden soubor
            radio_number_of_treesActionPerformed(e);
        }

        else if (source == button_browse_names_prefix) { // výběr prefixu souborů
            button_browse_names_prefixActionPerformed(e);
        }

        else if (source == radio_save_all_trees) { // výběr volby k uložení všech nalezených stromů
            radio_save_all_treesActionPerformed(e);
        }

        else if (source == radio_save_range) { // výběr volby k uložení rozsahu nalezených stromů
            radio_save_rangeActionPerformed(e);
        }

        else if (source == button_save) { // odeslání dialogu tlačítkem 'save'
            button_saveActionPerformed(e);
        }

        else if (source == button_cancel) { // zrušení dialogu tlačítkem 'cancel'
            button_cancelActionPerformed(e);
        }
    } // actionPerformed


    private void button_saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_saveActionPerformed
        //debug ("\nStisknuto tlačítko 'button_save' (potvrzení uložení stromů na lokální disk)");
        return_value = true;
        closeDialog();
    }//GEN-LAST:event_button_saveActionPerformed

    private void button_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_cancelActionPerformed
        //debug ("\nStisknuto tlačítko 'button_cancel' (zrušení ukládání stromů)");
        return_value = false;
        closeDialog();
    }//GEN-LAST:event_button_cancelActionPerformed

    public void windowClosing(WindowEvent e) { // reakce na zavření okna uživatelem
        debug ("\nUživatel zavírá dialogové okno pro uložení nalezených stromů na lokální disk.");
        return_value = false;
        closeDialog();
    }
	public void windowDeactivated(WindowEvent e) {return;}
	public void windowActivated(WindowEvent e) {return;}
	public void windowDeiconified(WindowEvent e) {return;}
	public void windowIconified(WindowEvent e) {return;}
	public void windowClosed(WindowEvent e) {return;}
	public void windowOpened(WindowEvent e) {return;}


    private void radio_save_rangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_save_rangeActionPerformed
        saveRangeSetEnabled(true);
    }//GEN-LAST:event_radio_save_rangeActionPerformed

    private void saveRangeSetEnabled(boolean enable) {
        label_range_from.setEnabled(enable);
        text_range_from.setEnabled(enable);
        label_range_to.setEnabled(enable);
        text_range_to.setEnabled(enable);
    }

    private void radio_save_all_treesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_save_all_treesActionPerformed
        saveRangeSetEnabled(false);
    }//GEN-LAST:event_radio_save_all_treesActionPerformed

    private void radio_new_separationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_new_separationActionPerformed
        originalFilesSetEnabled(false);
        givenSizeSetEnabled(true);
        fileNamesSetEnabled(true);
        if (radio_unlimited_size.isSelected()) {
            countFromSetEnabled(false);
        }
        else {
            countFromSetEnabled(true);
        }

    }//GEN-LAST:event_radio_new_separationActionPerformed

    private void givenSizeSetEnabled(boolean enable) {
        label_maximum_size.setEnabled(enable);
        radio_unlimited_size.setEnabled(enable);
        radio_number_of_trees.setEnabled(enable);
        if (enable) {
            if (radio_number_of_trees.isSelected()) {
                text_number_of_trees.setEnabled(true);
            }
        }
        else {
            text_number_of_trees.setEnabled(false);
        }
    }

    private void originalFilesSetEnabled(boolean enable) {
        radio_original_names.setEnabled(enable);
        radio_new_names.setEnabled(enable);
    }

    private void radio_original_separationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_original_separationActionPerformed
        givenSizeSetEnabled(false);
        if (radio_original_names.isSelected()) {
            fileNamesSetEnabled(false);
            countFromSetEnabled(false);
        }
        else {
            countFromSetEnabled(true);
        }
        originalFilesSetEnabled(true);
    }//GEN-LAST:event_radio_original_separationActionPerformed

    private void radio_number_of_treesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_number_of_treesActionPerformed
        text_number_of_trees.setEnabled(true);
        countFromSetEnabled(true);
    }//GEN-LAST:event_radio_number_of_treesActionPerformed

    private void radio_unlimited_sizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_unlimited_sizeActionPerformed
        text_number_of_trees.setEnabled(false);
        countFromSetEnabled(false);
    }//GEN-LAST:event_radio_unlimited_sizeActionPerformed

    private void countFromSetEnabled(boolean enable) {
        label_count_from.setEnabled(enable);
        text_count_from.setEnabled(enable);
    }

    private void fileNamesSetEnabled(boolean enable) {
        label_names_prefix.setEnabled(enable);
        text_names_prefix.setEnabled(enable);
        button_browse_names_prefix.setEnabled(enable);
    }

    private void radio_original_namesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_original_namesActionPerformed
        fileNamesSetEnabled(false);
        countFromSetEnabled(false);
    }//GEN-LAST:event_radio_original_namesActionPerformed

    private void button_browse_names_prefixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_browse_names_prefixActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(i18n.getString("SAVE_RESULT_TREES_FILE_NAMES_SELECTION_TITLE"));
        chooser.setCurrentDirectory(new File(file_names_directory));
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
        ExampleFileFilter filter = new ExampleFileFilter();
        filter.addExtension("fs");
        //filter.addExtension("gif");
        filter.setDescription("PDT fs files");
        chooser.setFileFilter(filter);
        int result = chooser.showDialog(this,i18n.getString("SAVE_RESULT_TREES_FILE_NAMES_SELECTION_APPROVE_LABEL"));
        if (result == JFileChooser.APPROVE_OPTION) {
            String prefix = chooser.getSelectedFile().getName();
            // nyní odtrhnu příponu .fs, pokud tam je
            String prefix_final = prefix;
            if (prefix.endsWith(".fs")) {
                prefix = prefix.substring(0,prefix.length()-3);
                prefix_final = new String(prefix);
            }
            // nyní odtrhnu číslování za podtržítkem, pokud tam je.
            char c = prefix.charAt(prefix.length()-1);
            long number = 0; // sem budu načítat případné číslo na konci jména souboru
            long multip = 1; // první případně čtená číslice reprezentuje řád jedniček
            boolean shorted = false;
            while (Character.isDigit(c)) {
                number = number + multip * Character.getNumericValue(c);
                multip = multip * 10; // příště bude řád o jedna vyšší
                prefix = prefix.substring(0,prefix.length()-1);
                shorted=true;
                c = prefix.charAt(prefix.length()-1);
            }
            if (shorted) { // pokud jsem odříznul nějaké číslice
                if (prefix.endsWith("_")) { // byly to číslice za podtržítkem
                    prefix = prefix.substring(0,prefix.length()-1);
                    prefix_final = prefix;
                    number++; // zvýším získané číslo o jedničku
                    text_count_from.setText(""+number);
                }
            }
            text_names_prefix.setText(prefix_final);
            file_names_directory = chooser.getSelectedFile().getParent(); // adresář pro procházení jmen souborů nastavím na poslední procházený adresář
        }
    }//GEN-LAST:event_button_browse_names_prefixActionPerformed

    private void radio_new_namesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radio_new_namesActionPerformed
        fileNamesSetEnabled(true);
        countFromSetEnabled(true);
    }//GEN-LAST:event_radio_new_namesActionPerformed

    private void button_browse_destination_directoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_browse_destination_directoryActionPerformed
        JFileChooser chooser = new JFileChooser();
        // chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setDialogTitle(i18n.getString("SAVE_RESULT_TREES_DIRECTORY_SELECTION_TITLE"));
        chooser.setCurrentDirectory(new File(getDestinationDirectory()));
        // Note: source for ExampleFileFilter can be found in FileChooserDemo,
        // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
        //ExampleFileFilter filter = new ExampleFileFilter();
        //filter.addExtension("fs");
        //filter.addExtension("gif");
        //filter.setDescription("PDT fs files");
        //chooser.setFileFilter(filter);
        int result = chooser.showDialog(this,i18n.getString("SAVE_RESULT_TREES_DIRECTORY_SELECTION_APPROVE_LABEL"));
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            String dir;
            //if (selected.isFile()) {
            //    selected = selected.getParentFile();
            //    if (selected != null) { // uvedená cesta měla nadřazený adresář
            //        dir = selected.getAbsolutePath();
            //    }
            //    else {
            //        dir = chooser.getSelectedFile().getAbsolutePath(); // záchranná akce - vezmu prostě celé to, co uživatel vybral
            //    }
            //}
            //else {
                dir = selected.getAbsolutePath();
            //}
            text_destination_directory.setText(dir);
            file_names_directory = dir; // adresář pro procházení jmen souborů nastavím na stejnou hodnotu
        }
    }//GEN-LAST:event_button_browse_destination_directoryActionPerformed

    private void text_destination_directoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_destination_directoryActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_text_destination_directoryActionPerformed

    /** Closes the dialog */
    private void closeDialog() {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup group_range;
    private javax.swing.JRadioButton radio_save_all_trees;
    private javax.swing.JPanel panel_count_from;
    private javax.swing.JButton button_browse_destination_directory;
    private javax.swing.JPanel panel_save_options_outer;
    private javax.swing.JLabel label_destination_directory;
    private javax.swing.JButton button_browse_names_prefix;
    private javax.swing.JButton button_save;
    private javax.swing.JTextField text_destination_directory;
    private javax.swing.JLabel saveAppealLabel;
    private javax.swing.JRadioButton radio_unlimited_size;
    private javax.swing.JPanel panel_new_separation;
    private javax.swing.JPanel panel_save_appeal;
    private javax.swing.JLabel label_names_prefix;
    private javax.swing.JTextField text_number_of_trees;
    private javax.swing.JPanel panel_maximum_size_label;
    private javax.swing.JPanel panel_save_all_trees;
    private javax.swing.JPanel panel_save_options_inner;
    private javax.swing.ButtonGroup group_size_of_files;
    private javax.swing.JCheckBox check_query_match_meta_tag;
    private javax.swing.JCheckBox check_save_multiple_occurrences;
    private javax.swing.JPanel panel_names_prefix;
    private javax.swing.JPanel panel_ok_cancel;
    private javax.swing.JPanel panel_new_separation_options;
    private javax.swing.JRadioButton radio_save_range;
    private javax.swing.JPanel panel_range_outer;
    private javax.swing.JTextField text_range_from;
    private javax.swing.JPanel panel_destination_directory_outer;
    private javax.swing.ButtonGroup group_original_new_names;
    private javax.swing.JPanel panel_size_options;
    private javax.swing.JTextField text_names_prefix;
    private javax.swing.JPanel panel_unlimited_size;
    private javax.swing.JPanel panel_original_separation;
    private javax.swing.JPanel jPanel1;
    private javax.swing.ButtonGroup group_original_new_separation;
    private javax.swing.JPanel panel_range_middle;
    private javax.swing.JRadioButton radio_original_separation;
    private javax.swing.JTextField text_count_from;
    private javax.swing.JLabel label_maximum_size;
    private javax.swing.JPanel panel_add_query_match_meta_tag;
    private javax.swing.JPanel panel_number_of_trees;
    private javax.swing.JLabel label_range_to;
    private javax.swing.JButton button_cancel;
    private javax.swing.JRadioButton radio_new_names;
    private javax.swing.JRadioButton radio_number_of_trees;
    private javax.swing.JRadioButton radio_original_names;
    private javax.swing.JPanel panel_save_options_middle;
    private javax.swing.JPanel panel_range_inner;
    private javax.swing.JPanel panel_destination_directory_inner;
    private javax.swing.JPanel panel_original_separation_options;
    private javax.swing.JLabel label_range_from;
    private javax.swing.JPanel panel_save_range;
    private javax.swing.JTextField text_range_to;
    private javax.swing.JRadioButton radio_new_separation;
    private javax.swing.JLabel label_count_from;
    // End of variables declaration//GEN-END:variables

}
