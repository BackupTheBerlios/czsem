
import java.util.ResourceBundle;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.properties.*;

/**
 * This object initializes saving of the result trees to the local disc. It shows a progress bar and allows the user
 * to cancel the saving.
 */
public class ResultTreesLocalSaver implements ActionListener, SavingFinishedListener {

    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    private NGClient jaaa;

    private ProgressDisplayer progress_displayer; // tento objekt se bude starat o zobrazování průběhu ukládání stromů

    NGForestSaver saver_thread; // tento objekt bude ukládat stromy v samostatném vláknu

    private int status; // uchovává informaci o úspěšnosti ukládání stromů

    private String save_result_trees_directory; // uchovává posledně použitý adresář pro ukládání výsledných stromů
    private String save_result_trees_files_prefix; // uchovává posledně použitý prefix souborů pro ukládání výsledných stromů
    private long save_result_trees_range_start; // ukládat nalezené stromy v rozsahu od...
    private long save_result_trees_range_end; // ukládat nalezené stromy v rozsahu do... (0 znamená neomezeně)
    private long save_result_trees_file_size; // maximální počet stromů do jednoho souboru (0 znamená neomezeně)
    private long save_result_trees_count_start; // začít číslovat soubory od daného čísla
    private boolean save_result_trees_original_separation; // rozdělit stromy do souborů podle jejich původního rozdělení?
    private boolean save_result_trees_original_names; // pojmenovat soubory jejich původními jmény?
    private boolean save_result_trees_put_matching_meta_tag; // vložit k matchujícím vrcholům meta tag?
    private boolean save_result_trees_save_multiple_occurrences; // ukládat vícenásobné výskyty dotazu?

    private JDialog cancel_dialog; // dialog pro předčasné ukončení ukládání výsledných stromů na lokální disk a pro zobrazování průběhu ukládání
    private JButton cancel_dialog_cancel_button; // tlačítko pro předčasné ukončení ukládání výsledných stromů na lokální disk

    public ResultTreesLocalSaver (NGClient p_jaaa, ShowMessagesAble p_mess, ResourceBundle p_i18n) {    // implicitní hodnoty pro ukládání výsledných stromů
        mess = p_mess;
        i18n = p_i18n;
        jaaa = p_jaaa;

        save_result_trees_directory = System.getProperty("user.home");
        save_result_trees_files_prefix = "result_trees";
        save_result_trees_file_size = 50; // maximální počet stromů do jednoho souboru (0 znamená neomezeně)
        save_result_trees_put_matching_meta_tag = true; // vložit k matchujícím vrcholům meta tag?
        save_result_trees_save_multiple_occurrences = false; // ukládat vícenásobné výskyty dotazu?
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

// ------ begin PROPERTIES ------

    public String getDestinationDirectory() {
        return save_result_trees_directory;
    }
    public void setDestinationDirectory(String path) {
        save_result_trees_directory = new String(path);
    }

    public String getFilesPrefix() {
        return save_result_trees_files_prefix;
    }
    public void setFilesPrefix(String prefix) {
        save_result_trees_files_prefix = new String(prefix);
    }

    public long getFilesSize() {
        return save_result_trees_file_size;
    }
    public void setFilesSize(long size) {
        save_result_trees_file_size = size;
    }

    public boolean getPutMatchingMetaTag() {
        return save_result_trees_put_matching_meta_tag;
    }
    public void setPutMatchingMetaTag(boolean put) {
        save_result_trees_put_matching_meta_tag = put;
    }

    public boolean getSaveMultipleOccurrences() {
        return save_result_trees_save_multiple_occurrences;
    }
    public void setSaveMultipleOccurrences(boolean save) {
        save_result_trees_save_multiple_occurrences = save;
    }

// ------ end PROPERTIES ------


    public void readGeneralProperties(Properties properties) { // přečtu properties pro tento objekt
        save_result_trees_directory = properties.getStringProperty("directories","directory save result trees", save_result_trees_directory);
        save_result_trees_files_prefix = properties.getStringProperty("local result save","files prefix", save_result_trees_files_prefix);
        save_result_trees_file_size = properties.getLongProperty("local result save","file size", save_result_trees_file_size);
        save_result_trees_put_matching_meta_tag = properties.getBooleanProperty("local result save","put matching meta tag", save_result_trees_put_matching_meta_tag);
        save_result_trees_save_multiple_occurrences = properties.getBooleanProperty("local result save","save multiple occurrences", save_result_trees_save_multiple_occurrences);
    } // readGeneralProperties

    public void writeGeneralProperties(Properties properties) { // zapíše properties z tohoto objektu
        properties.updateProperty("directories","directory save result trees", save_result_trees_directory, "initial directory for saving result trees (string)");
        properties.updateProperty("local result save","files prefix", save_result_trees_files_prefix, "prefix for files for saving trees to local disk (string)");
        properties.updateProperty("local result save","file size", ""+save_result_trees_file_size, "max number of trees per file (long int)");
        properties.updateProperty("local result save","put matching meta tag", ""+save_result_trees_put_matching_meta_tag, "put matching meta tag to the result trees? (true, false)");
        properties.updateProperty("local result save","save multiple occurrences", ""+save_result_trees_save_multiple_occurrences, "save multiple occurrences of the query? (true, false)");
    } // writeGeneralProperties


    public void actionPerformed(ActionEvent e) { // akce (doubleclick nebo mezerník nebo enter)
        Object zdroj = e.getSource();
        if (zdroj == cancel_dialog_cancel_button) { // uživatel zrušil ukládání souborů na lokální disk
            //debug ("\nStisknuto tlačítko 'cancel_dialog_cancel_button' (přerušení ukládání souborů na lokální disk)");
            savingCanceled(this); // ukončí ukládání stromů; předává objekt, ze kterého pokyn k ukončení vzešel
        }
    }


    /**
     * It creates (but does not show) a dialog window allowing user to cancel the saving of the result trees
     */
    private void createCancelDialog(String window_title) {
        cancel_dialog = new JDialog(jaaa, window_title, false); // nebude modální, pak totiž nejde otevřít další okno pro dotaz na přepsání souboru (resp. otevřít jde, ale nereaguje na klikání)
        cancel_dialog.setSize(350,135);
        Container content_pane = cancel_dialog.getContentPane();
        content_pane.setLayout(new BorderLayout());
        JPanel panel_info=new JPanel();
        panel_info.setLayout(new BorderLayout());
        JLabel label=new JLabel(i18n.getString("CANCEL_DIALOG_MESSAGE"));
        label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel_info.add(label,BorderLayout.NORTH);
        InfoBar info_bar=new InfoBar();
        info_bar.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        progress_displayer = info_bar;
        panel_info.add(info_bar,BorderLayout.SOUTH);
        content_pane.add(panel_info, BorderLayout.NORTH);
        cancel_dialog_cancel_button=new JButton(i18n.getString("CANCEL_DIALOG_CANCEL_BUTTON"));
        cancel_dialog_cancel_button.addActionListener(this);
        JPanel panel_button = new JPanel();
        panel_button.add(cancel_dialog_cancel_button);
        content_pane.add(panel_button, BorderLayout.SOUTH);
        int x_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getWidth();
        int y_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getHeight();

        int x_pos = (int)(x_max/2 - cancel_dialog.getSize().getWidth()/2);
        int y_pos = (int)(y_max/2 - cancel_dialog.getSize().getHeight()/2);
        cancel_dialog.setLocation(x_pos, y_pos);
    } // createCancelDialog

    public void savingCanceled(Object source) {
        //System.out.print("\nUkládání stromů na lokální disk bylo zrušeno.");
        debug("\nUkládání stromů na lokální disk bylo zrušeno.");
        if (source == this) { // pokud zdrojem ukončení je tento objekt (tedy bylo stisknuto tlačítko cancel u dialogu s progress barem)
            saver_thread.stopSaving(); // upozorním objekt ukládající stromy, že má skončit
        }
        progress_displayer.stopProgressBar();
        loadAndDisplayFirstTree();
        inform("LOCAL_SAVE_RESULT_TREES_INFO_SAVING_CANCELED");
        cancel_dialog.dispose();
    }

    /**
     * SavingFinishedListener implementation
     */
    public void savingFinished() {
        int load;
        //System.out.print("\nUkládání stromů na lokální disk bylo dokončeno.");
        debug("\nUkládání stromů na lokální disk bylo dokončeno.");
        loadAndDisplayFirstTree();
        progress_displayer.stopProgressBar();
        inform("LOCAL_SAVE_RESULT_TREES_INFO_SAVING_OK");
        cancel_dialog.dispose();
        status= NGForestSaver.SAVING_OK;
    }

    private void loadAndDisplayFirstTree() {
        jaaa.zalozka_trees.loadNextTree(ServerCommunication.GET_TREE_SUBTYPE_FIRST);
    }


    private void startSavingInItsOwnThread() { // uloží výsledek na lokální disk podle nastavených parametrů
        //int ret=overwriteDialog(i18n.getString("LOCAL_SAVE_RESULT_TREES_OVERWRITE_DIALOG_TITLE"), save_result_trees_files_prefix, true);
        jaaa.setWaitCursor(); // nefunguje
        createCancelDialog(i18n.getString("CANCEL_DIALOG_TITLE")); // vytvoří (ale ještě nezobrazí) se modální dialog pro zobrazení průběhu (a případné zrušení) ukládání stromů
        saver_thread = new NGForestSaver(jaaa,mess,i18n, this);

        // předám nastavení parametrů ukládání stromů
        saver_thread.setDestinationDirectory(save_result_trees_directory); // nastavím cílový adresář
        saver_thread.setFilesPrefix(save_result_trees_files_prefix); // nastavený prefix souborů ukládaných pod novými jmény
        saver_thread.setFilesSize(save_result_trees_file_size);
        saver_thread.setRangeStart(save_result_trees_range_start);
        saver_thread.setRangeEnd(save_result_trees_range_end);
        saver_thread.setOriginalSeparation(save_result_trees_original_separation);
        saver_thread.setOriginalNames(save_result_trees_original_names);
        saver_thread.setCountStart(save_result_trees_count_start);
        saver_thread.setPutMatchingMetaTag(save_result_trees_put_matching_meta_tag);
        saver_thread.setSaveMultipleOccurrences(save_result_trees_save_multiple_occurrences);

        progress_displayer.startProgressBar(saver_thread, ProgressDisplayer.STRING, 100);
        saver_thread.startSaving(); // spustí se ukládání stromů
        cancel_dialog.setVisible(true); // nyní se zobrazí modální dialog - chyba lávky - viz další řádky

        // dialog bohužel není modální, takže další kód by běžel hned
        // další kód pokračuje až po zavření dialogu (buď stiskem tlačítka na dialogu nebo skončením práce saveru)
        //int status = saver_thread.getStatus();
        //switch (status) {
        //    case NGForestSaver.SAVING_OK:
        //        inform("LOCAL_SAVE_RESULT_TREES_INFO_SAVING_OK");
        //    break;
        //    case NGForestSaver.SAVING_CANCELED:
        //        inform("LOCAL_SAVE_RESULT_TREES_INFO_SAVING_CANCELED");
        //    break;
        //}
        // jaaa.setDefaultCursor(); - nefunguje
    } // startSavingInItsOwnThread


    public void saveResultDialog() { // zobrazí dialogové okno pro uložení nalezených stromů na lokální disk
        LocalSaveResultTreesDialog save_dialog = new LocalSaveResultTreesDialog(jaaa, i18n.getString("LOCAL_SAVE_RESULT_TREES_DIALOG_TITLE"), true, jaaa, i18n);
        save_dialog.setDestinationDirectory(save_result_trees_directory);
        save_dialog.setFilesPrefix(save_result_trees_files_prefix);
        save_dialog.setFileSize(save_result_trees_file_size);
        save_dialog.setPutMatchingMetaTag(save_result_trees_put_matching_meta_tag);
        save_dialog.setSaveMultipleOccurrences(save_result_trees_save_multiple_occurrences);
        int x_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getWidth();
        int y_max = (int)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().getHeight();

        int x_pos = (int)(x_max/2 - save_dialog.getSize().getWidth()/2);
        int y_pos = (int)(y_max/2 - save_dialog.getSize().getHeight()/2);

        save_dialog.setLocation(x_pos, y_pos);
        boolean proceed = save_dialog.showDialog();
        if (!proceed) { // uživatel zrušil ukládání stromů
            return;
        }
        // dialog nebyl zrušen tlačítkem 'cancel', naopak byl potvrzen tlačítkem 'save'
        save_result_trees_directory = new String(save_dialog.getDestinationDirectory()); // zapamatuji si nastavený cílový adresář
        save_result_trees_files_prefix = new String(save_dialog.getFilesPrefix()); // nastavený prefix souborů ukládaných pod novými jmény
        save_result_trees_file_size = save_dialog.getFileSize();
        save_result_trees_range_start = save_dialog.getRangeStart();
        save_result_trees_range_end = save_dialog.getRangeEnd();
        save_result_trees_original_separation = save_dialog.isSetOriginalSeparation();
        save_result_trees_original_names = save_dialog.isSetOriginalNames();
        save_result_trees_count_start = save_dialog.getCountStart();
        save_result_trees_put_matching_meta_tag = save_dialog.isCheckedPutMatchingMetaTag();
        save_result_trees_save_multiple_occurrences = save_dialog.isCheckedSaveMultipleOccurrences();

        //debug("\nUživatel chce uložit nalezené stromy na lokální disk s tímto nastavením:");
        //debug("\n  - cílový adresář: " + save_result_trees_directory);
        //debug("\n  - prefix jmen souborů: " + save_result_trees_files_prefix);
        //debug("\n  - max. počet stromů na soubor: " + save_result_trees_file_size);
        //debug("\n  - rozsah ukládaných stromů - začátek: " + save_result_trees_range_start);
        //debug("\n  - rozsah ukládaných stromů - konec: " + save_result_trees_range_end);
        //debug("\n  - číslovat soubory od: " + save_result_trees_count_start);
        //debug("\n  - použít původní rozdělení stromů do souborů: " + save_result_trees_original_separation);
        //debug("\n  - použít původní jména souborů: " + save_result_trees_original_names);
        //debug("\n  - vložit meta tag u matchujících vrcholů: " + save_result_trees_put_matching_meta_tag);
        //debug("\n  - ukládat vícenásobné výskyty dotazu: " + save_result_trees_save_multiple_occurrences);

        startSavingInItsOwnThread();
    }


}
