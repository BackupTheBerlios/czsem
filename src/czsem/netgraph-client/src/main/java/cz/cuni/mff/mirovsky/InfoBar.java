
package cz.cuni.mff.mirovsky;

// ====================================================================================================
//		class InfoBar			řádkový výpis informací pro uživatele
// ====================================================================================================

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Class InfoBar creates a one-line bar in which information for the user can be displayed. It can also display a progress bar.
 * It is used for displaying messages for the users and as a progress bar during saving the result trees to a local disc.
 */
public class InfoBar extends JPanel implements ActionListener, ProgressDisplayer {

    private JLabel label_info; // místo pro informace
    private JProgressBar progress_bar; // progress bar
    private javax.swing.Timer timer; // pro obnovování informace na progress baru
    private int string_type; // typ zobrazování hodnoty progress baru
    private ProgressSource progress_source; // uchovává odkaz na třídu poskytující hodnoty pro progress bar

    public InfoBar() { // konstruktor
        setLayout(new GridLayout(1,1));
        label_info = new JLabel(" ");
        add(label_info);
    }

    public void setText(String s, boolean mazat) { // vypise informaci; smaže předchozí, pokud mazat = true
        if (mazat) label_info.setText(s);
        else label_info.setText(label_info.getText() + s);
    }

    public void startProgressBar(ProgressSource info_src, int p_string_type, int timer_interval) {
        // informer je třída implementující interface ProgressInformer pro poskytování informací progress baru
        // p_string_type určuje, zda a jak zobrazovat hodnotu progress baru textem
        // timer_interval určuje, po jakých intervalech (v 1/1000 s) se má obnovovat informace progress baru

        //System.out.println("\nSpouštím progress bar");
        progress_source = info_src; // zdroj informací pro progress bar
        string_type = p_string_type; // typ zobrazování hodnoty progress baru
        remove(label_info); // odstraním z InfoBaru textový řádek
        progress_bar = new JProgressBar(progress_source.getProgressMinValue(), progress_source.getProgressMaxValue());
        progress_bar.setBorderPainted(true);
        if (string_type != NONE) {
            progress_bar.setStringPainted(true);
        }
        else {
            progress_bar.setStringPainted(false);
        }
        progress_bar.setValue(progress_source.getProgressMinValue());
        add(progress_bar); // a místo něj tam dám progress bar
        timer = new javax.swing.Timer(timer_interval,this);
        timer.start();
        revalidate();
        repaint();
    }

    public void stopProgressBar() { // odstraní progress bar z InfoBaru (a zastaví timer)
        //System.out.println("\nKončím progress bar");
        timer.stop();
        remove(progress_bar);
        add(label_info);
        revalidate();
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        //System.out.println("\nSpustil se timer pro progress bar v InfoBaru");
        progress_bar.setMinimum(progress_source.getProgressMinValue()); // pro případ, že by se tyto hodnoty (zvláště max) měnily v průběhu operace
        progress_bar.setMaximum(progress_source.getProgressMaxValue());
        progress_bar.setValue(progress_source.getProgressCurrentValue()); // nastavím správné procentuální vyplnění progress baru
        if (string_type == STRING) {
            progress_bar.setString(progress_source.getProgressText()); // pokud mám místo procent psát text, tak napíši i správný text do progress baru
        }
        // (jinak procenta se píší automaticky)

        //if (progress_informer.done()) {
        //    Toolkit.getDefaultToolkit().beep();
        //    timer.stop();
        //    progress_bar.setValue(progress_bar.getMaximum());
        //}
    }


} // class InfoBar



