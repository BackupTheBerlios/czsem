
import java.util.ResourceBundle;
import java.io.*;
import javax.swing.*;

import cz.cuni.mff.mirovsky.*;
import cz.cuni.mff.mirovsky.trees.*;


/**
 * Class NGForestSaver saves result trees to a local disc. It communicates with the server and asks it to send
 * the result trees one by one. Optionally, it adds an information about matching nodes to the trees.
 */
public class NGForestSaver extends Thread implements ProgressSource {

    public final static int SAVING_OK = 0;
    public final static int SAVING_CANCELED_BY_INVOKER = 1;
    public final static int SAVING_CANCELED_BY_SAVER = 2;

    public final static int FORMAT_FS = 1;
    // public final static int FORMAT_XML_PDT_1_0 = 2;
    // public final static int FORMAT_XML_PDT_2_0 = 3;

    private ShowMessagesAble mess; // objekt pro tisk zprav pro uzivatele
    private ResourceBundle i18n; // objekt pro přístup k lokalizovaným zprávám
    private NGClient jaaa;

    private String directory; // adresář pro ukládání výsledných stromů
    private String files_prefix; // prefix souborů pro ukládání výsledných stromů
    private long range_start; // ukládat nalezené stromy v rozsahu od...
    private long range_end; // ukládat nalezené stromy v rozsahu do... (0 znamená neomezeně)
    private long file_size; // maximální počet stromů do jednoho souboru (0 znamená neomezeně)
    private long count_start; // začít číslovat soubory od daného čísla
    private boolean original_separation; // rozdělit stromy do souborů podle jejich původního rozdělení
    private boolean original_names; // pojmenovat soubory jejich původními jmény?
    private boolean put_matching_meta_tag; // vložit k matchujícím vrcholům meta tag?
    private boolean save_multiple_occurrences; // ukládat vícenásobné výskyty dotazu?

    private int format_for_saving; // format, ve kterem stromy ukladat (fs, xml, ...); zatim pouze FORMAT_FS

    private boolean overwrite_all = false; // nechci defaultně přemazávat existující soubory bez dotazu

    private NGForest forest_for_saving; // aktuální les
    private NGTreeHead head_for_saving; // hlavicka pro ukladani

    private long number_of_saved_forests; // počet doposud uložených lesů
    private long number_of_actual_forest; // pořadí aktuálního lesa (může se lišit od předchozího, protože se může ukládat až např. od pátého lesa)
    private long number_of_saved_forests_in_one_file; // počet doposud uložených lesů do aktuálního souboru
    private String last_file_name; // jméno předchozího souboru pro ukládání
    private String last_orig_file_name; // jméno předchozího souboru, ze kterého byl ukládaný les (včetně přípony a cesty)
    private long count_actual;

    private String last_id; // id posledně ukládaného stromu (pro porovnání s aktuálně načteným stromem)

    private int saving_result_status; // informuje o úspěšnosti ukládání

    private OutputStream stream_for_saving; // stream pro ukládání stromů
    private boolean new_stream; // signalizuje změnu streamu pro ukládání stromů

    private Attribute matching_node;
    private Attribute matching_edge;

    int val;
    int max_zdrzuj = 30;

    private boolean stop_request; // pokud se tato proměnná nastaví na true, bude tento objekt vědět, že má ukončit ukládání stromů

    private int saving_state=0; // stav procesu ukládání stromů: 0 = začíná se s procesem ukládání, 1 = návrat k prvnímu stromu, 2 = postup k prvnímu stromu rozsahu, 3 = ukládání

    SavingFinishedListener saving_finished_listener;

    public NGForestSaver(NGClient p_jaaa, ShowMessagesAble p_mess, ResourceBundle p_i18n, SavingFinishedListener p_saving_finished_listener) {
        super();

        mess = p_mess;
        i18n = p_i18n;
        jaaa = p_jaaa;
        saving_finished_listener = p_saving_finished_listener;
        matching_node = new Attribute("NG_matching_node", 17, "true|false"); // je výčtový a pro jistotu poziční
        matching_edge = new Attribute("NG_matching_edge", 17, "true|false"); // je výčtový a pro jistotu poziční
        // změní-li se tu jména těchto dvou matchujících atributů, je nutno změnit to také v NGTree.setMatchigMetaTags()

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
        return directory;
    }
    public void setDestinationDirectory(String path) {
        directory = new String(path);
    }

    public String getFilesPrefix() {
        return files_prefix;
    }
    public void setFilesPrefix(String prefix) {
        files_prefix = new String(prefix);
    }

    public long getFilesSize() {
        return file_size;
    }
    public void setFilesSize(long size) {
        file_size = size;
    }

    public boolean getPutMatchingMetaTag() {
        return put_matching_meta_tag;
    }
    public void setPutMatchingMetaTag(boolean put) {
        put_matching_meta_tag = put;
    }

    public boolean getSaveMultipleOccurrences() {
        return save_multiple_occurrences;
    }
    public void setSaveMultipleOccurrences(boolean save) {
        save_multiple_occurrences = save;
    }

    public long getRangeStart() {
        return range_start;
    }
    public void setRangeStart(long start) {
        range_start = start;
    }

    public long getRangeEnd() {
        return range_end;
    }
    public void setRangeEnd(long end) {
        range_end = end;
    }

    public boolean isSetOriginalSeparation() {
        return original_separation;
    }
    public void setOriginalSeparation(boolean set) {
        original_separation = set;
    }

    public boolean isSetOriginalNames() {
        return original_names;
    }
    public void setOriginalNames(boolean set) {
        original_names = set;
    }

    public long getCountStart() {
        return count_start;
    }
    public void setCountStart(long start) {
        count_start = start;
    }

    public boolean isSetOverwriteAll() {
        return overwrite_all;
    }
    public void setOverwriteAll(boolean set) {
        overwrite_all = set;
    }

    // ------ end PROPERTIES ------


    /**
     * ProgressSource implementation
     */
    public int getProgressCurrentValue() {

        return (int)jaaa.kom_net.getNumberOfActualOccurrence(); // tohle není správně pro případ, že ukládám každý strom jen jednou!!! Stejně tak v tom případě nesouhlasí getProgressMaxValue()
        // rovněž tu může dojít k přetečení, ale ProgressBar přijímá jen integerové hodnoty
    }

    /**
     * ProgressSource implementation
     */
    public String getProgressText() {
        return ""+getProgressCurrentValue()+"/"+getProgressMaxValue();
    }

    /**
     * ProgressSource implementation
     */
    public int getProgressMinValue() {
        return 0; // the minimum number of saved trees
    }

    /**
     * ProgressSource implementation
     */
    public int getProgressMaxValue() {
        long value;
        //if (put_matching_meta_tag) { // pokud ukládat lesy i s označením, které vrcholy matchovaly s dotazem; potom budu ukládat jeden les i několikrát - podle počtu výskytů dotazu
            value = jaaa.kom_net.getNumberOfFoundOccurences();
            if (saving_state == 2 || saving_state == 3) { // pokud už postupuji ve stromech vpřed
                if (range_end > 0 && range_end < value) { // pokud rozsah lesů není až k poslednímu nalezenému
                    value = range_end;
                }
            }
        //}
        //else { // jeden strom budu ukládat jen jednou bez ohledu na počet výskytů dotazu v něm
        //    value = jaaa.kom_net.getNumberOfFoundTrees();
        //    // !!! tady je problém s getProgressCurrentValue() !!!
        //}
        return (int)value; // tady je možné přetečení při velkém počtu stromů! Ale ProgressBar může hodnoty jen v integerech
    }

    public void run() {
        saveResult();
        switch (saving_result_status) {
            case SAVING_CANCELED_BY_SAVER:
                saving_finished_listener.savingCanceled(this); // předávám objekt, který vyvolal zrušení ukládání
            break;
            case SAVING_CANCELED_BY_INVOKER:
            break;
            case SAVING_OK:
                saving_finished_listener.savingFinished();
            break;
        }
        return;
    }


    public int getStatus() { // vrací informaci o úspěšnosti ukládání
        return saving_result_status;
    }


    private String deleteSuffix(String file_name) { // oddstraní suffix (za poslední tečkou včetně) ze jména souboru, pokud tam je
        int suffix_position = file_name.lastIndexOf('.'); // zjistím pozici poslední tečky
        if (suffix_position == -1) suffix_position = file_name.length(); // pokud tam nebyla, vezmu pozici za celým názvem
        return file_name.substring(0,suffix_position); // a vrátím všechno až do té pozice nevčetně
    }

    private String getFileName() { // vrátí jméno souboru, do kterého se má uložit aktuální strom, bez přípony
        String orig_name;
        String name;
        if (original_separation) { // pokud se mají stromy rozdělovat do souborů podle původního umístění v souborech
            orig_name=forest_for_saving.getFileName(); // takto získám celou cestu v korpusu na serveru
            File file = new File(orig_name); // přečtu to do objektu File
            name = file.getName(); // a ten mi dá samotné jméno souboru
            if (original_names) { // pokud se mají použít původní jména souborů
                return deleteSuffix(name); // vrátím to bez přípony
            }
            else { // pouze original_separation
                if (orig_name.compareTo(last_orig_file_name) != 0) { // pokud je tento strom z jiného souboru než předchozí strom
                    count_actual++; // pořadí souboru o jedna větší
                    name = files_prefix.concat("_" + count_actual); // k prefixu přidám pořadí souboru
                    last_orig_file_name = orig_name;
                    return name;
                }
                else { // je ze stejného souboru jako předchozí strom
                    name = files_prefix.concat("_" + count_actual); // k prefixu přidám pořadí souboru
                    return name;
                }
            }
        }
        else { // nové rozdělení stromů do souborů
            if (file_size == 0) { // neomezená velikost souboru
                return files_prefix;
            }
            else {
                if (number_of_saved_forests == 0 || number_of_saved_forests_in_one_file == file_size) { // dosáhl jsem maximální velikosti souboru
                    number_of_saved_forests_in_one_file = 0;
                    count_actual++; // pořadí souboru o jedna větší
                    name = files_prefix + "_" + count_actual; // k prefixu přidám pořadí souboru
                    return name;
                }
                else { // mohu zapisovat tam co předtím
                    name = files_prefix.concat("_" + count_actual); // k prefixu přidám pořadí souboru
                    return name;
                }
            }
        }
    } // getFileName

    private File getFile(String file_name) { // vrátí objekt File ukazující na soubor s daným jménem
        // pokud už existuje, vyzve uživatele k potvrzení smazání vrátí null, pokud uživatel nechce smazat
        // jinak ho smaže

        String absolute = directory + File.separator + file_name;

        File dir = new File(directory);

        if (! dir.exists()) {
             dir.mkdirs();
        }

        File file = new File(absolute);

        if (file.exists()) { // soubor již existuje
            if (!overwrite_all) {
                int overwrite = overwriteDialog(i18n.getString("LOCAL_SAVE_RESULT_TREES_OVERWRITE_DIALOG_TITLE"),file_name,true);
                if (overwrite == 0) { // cancel - zrušit ukládání
                    stop_request = true;
                    return null;
                }
                if (overwrite == 2) { // yes to all
                    overwrite_all = true;
                }
                // overwrite == 1 znamená přepsat, takže pouze normálně pokračuju
            }
        }
        return file;
    } // getFile


    private OutputStream getStream(File file) { // vrátí výstupní proud pro daný soubor
        FileOutputStream stream = null;
        try {
            file.createNewFile(); // vytvoří prázdný soubor, pokud neexistuje
            stream = new FileOutputStream(file);
        }
        catch (IOException e) {
            debug ("\nNGForestSaver.getStream: An error occured during opening the file " + file + ":" + e);
        }
        return stream;
    } // getStream

    private void setStream() {
        // nastaví stream pro ukládání následujícího stromu (glob. proměnná stream_for_saving);
        // podle nastavených parametrů zkontroluji,
        // zda už není v dosavadndním streamu/filu uloženo dostatečně mnoho stromů, kdyžtak otevřu nový
        // soubor, zkontroluji, zda neexistuje, kdyžtak se zeptám uživatele na přepsání;
        // nastavuje též glob. prom. new_stream, která upozorňuje, pokud jsem v této funkci otevřel
        // nový stream
        String file_name;
        File file;

        file_name = getFileName(); // získám správné jméno souboru bez přípony

        if (format_for_saving == FORMAT_FS) {
            file_name = file_name + ".fs";
        }

        if (stream_for_saving == null) { // první volání této funkce
            file = getFile(file_name); // získám soubor
            if (file == null) { // soubor se nezískal, např. uživatel nechtěl přepsat existující soubor
              saving_result_status = SAVING_CANCELED_BY_SAVER;
              stream_for_saving = null;
              stop_request = true;
              return;
            }
            stream_for_saving = getStream(file);
            last_file_name = file_name;
            new_stream = true;
            return;
        }

        if (file_name.compareTo(last_file_name) != 0) { // pokud se má ukládat do nového souboru
            writeTail(); // zapise zaverecne informace do souboru (pro Tred)
            closeStream(); // uzavře dosavadní soubor pro ukládání
            file = getFile(file_name); // získám soubor
            if (file == null) { // soubor se nezískal, např. uživatel nechtěl přepsat existující soubor
              saving_result_status = SAVING_CANCELED_BY_SAVER;
              stream_for_saving = null;
              stop_request = true;
              return;
            }
            stream_for_saving = getStream(file);
            last_file_name = file_name;
            new_stream = true;
            return;
        }

        // jinak se ponechá dosavadní stream pro ukládání
        new_stream = false;
        return;
    } // setStream

    private void initializeSaving() { // pripravi globalni promenne pro ukladani stromu
        format_for_saving = FORMAT_FS; // nic jineho v teto verzi nepodporuji
        head_for_saving = null; // zatim zadna hlavicka
        number_of_saved_forests = 0;
        number_of_saved_forests_in_one_file = 0;
        stream_for_saving = null;
        new_stream = true;
        last_file_name = "";
        last_orig_file_name = "";
        count_actual = count_start - 1; // při prvním použití se zvedne o jedničku na správnou startovací hodnotu
        number_of_actual_forest = 0;
        saving_state = 0;
        last_id = "";
    }

    private void finishSaving() { // zrusi nektere informace z prubehu ukladani
        overwrite_all = false; // pro priste se bude opet predpokladat, ze se nema prepisovat automaticky
        // nemuze to byt ale v initializeSaving, protoze chci, aby to slo nastavit predem pred ukladanim zvenku
    }

    private void addMatchingMetaTag(NGTreeHead head) {
        if (head.getIndexOfAttribute("NG_matching_node") == -1) { // pokud tam dosud není
            head.addAttribute(matching_node);
        }
        if (head.getIndexOfAttribute("NG_matching_edge") == -1) { // pokud tam dosud není
            head.addAttribute(matching_edge);
        }
    }

    private void setHead() { // nastavi globalni promennou head_for_saving na spravnou hlavicku pro ukladany soubor
        if (original_separation) { // pouzit hlavicku z puvodniho souboru
            if (put_matching_meta_tag) { // má se vložit meta atribut informující o matchování s dotazem
                head_for_saving = forest_for_saving.getHead().getClone(); // hlavičku budu měnit, tak ji radši naklonuji
                addMatchingMetaTag(head_for_saving);
            }
            else { // atribut o matchování s dotazem se nemá vkládat
                head_for_saving = forest_for_saving.getHead(); // snad neni potreba tu hlavicku klonovat
            }
        }
        else { // pouzije se globalni hlavicka
            if (head_for_saving == null) { // pokud jeste nebyla nastavena
                head_for_saving = jaaa.zalozka_query.ngt_global_head.getClone();
                if (put_matching_meta_tag) { // má se vložit meta atribut informující o matchování s dotazem
                    addMatchingMetaTag(head_for_saving);
                }
            }
        }
    } // setHead

    private void writeHead() { // zapise spravnou hlavicku do stream_for_saving
        String txt = "! hlavicka neurcena !\n";
        if (format_for_saving == FORMAT_FS) {
            txt = head_for_saving.toFSString();
        }
        writeString(txt);
    }

    private void writeTail() { // zapise styl pro Tred pro spravne zobrazeni stromu v Tredu do stream_for_saving
        StringBuffer txt = new StringBuffer("! ocas neurcen !\n");
        String attr;
        String orig_file_tail;
        String checked_attribute, checked_attribute_with_space;
        if (format_for_saving == FORMAT_FS) {
            txt = new StringBuffer("");
            txt.append("\n");
            orig_file_tail = jaaa.kom_net.getFileTail(); // vezmu to, co uz bylo v souboru za stromy
            txt.append(orig_file_tail); // a dam to tady na konec souboru
            txt.append("\n"); // to ale neni vsechno, co tam dam
            DefaultListModel attrs = jaaa.zalozka_trees.getVybraneAtributy();
            int size = attrs.getSize();
            for (int i=0; i<size; i++) { // pres vsechny vybrane atributy
                attr = (String)attrs.getElementAt(i);
                checked_attribute = "//Tred:Custom-Attribute:node:${" + attr + "}";
                checked_attribute_with_space = "//Tred:Custom-Attribute:node: ${" + attr + "}";
                if (orig_file_tail.indexOf(checked_attribute) == -1) { // checking of this attribute has not appeared in the original tail of the file
                    if (orig_file_tail.indexOf(checked_attribute_with_space) == -1) {
                        txt.append(checked_attribute + "\n");
                    }
                }
            }
            // a jeste zaridim barevne zvyrazneni uzlu, ktere matchovaly s dotazem
            txt.append("//Tred:Custom-Attribute:style:<? \"#{Line-fill:green}\" if $${NG_matching_edge} eq 'true' ?>\n");
            txt.append("//Tred:Custom-Attribute:style:<? \"#{Oval-fill:green}\" if $${NG_matching_node} eq 'true' ?>\n");
        }
        writeString(txt.toString());
    }

    private void writeForest() { // uloží forest_for_saving do stream_for_saving (bez hlavicky)
        if (put_matching_meta_tag) {
            if (forest_for_saving.getHead().getIndexOfAttribute("NG_matching_node") == -1) {
                if (forest_for_saving.getHead().getIndexOfAttribute("NG_matching_edge") == -1) { // oba atributy chybějí
                    forest_for_saving.addAttributes(2);
                    forest_for_saving.getHead().addAttribute(matching_node);
                    forest_for_saving.getHead().addAttribute(matching_edge);
                }
                else { // chybí jen NG_matching_node
                    forest_for_saving.addAttributes(1);
                    forest_for_saving.getHead().addAttribute(matching_node);
                }
            }
            else {
                if (forest_for_saving.getHead().getIndexOfAttribute("NG_matching_edge") == -1) { // chybí jen NG_matching_edge
                    forest_for_saving.addAttributes(1);
                    forest_for_saving.getHead().addAttribute(matching_edge);
                }
            }
            forest_for_saving.setMatchingMetaTags();
        }
        String txt = "\n! les neurcen !\n";
        if (format_for_saving == FORMAT_FS) {
            txt = "\n" + forest_for_saving.toFSString(false);
        }
        writeString(txt);
    }

    private void writeString(String text) {
        try {
            byte[] bytes = text.getBytes("UTF-8");
            for (int i=0; i<bytes.length; i++) {
                stream_for_saving.write(bytes[i]); // zapíše text do výstupního proudu v kódování UTF-8
            }
        }
        catch (Exception e) {
            debug ("\nNGForestSaver.writeString: An error occured during sending the data to the output stream." + e);
        }
    }

    private void closeStream() { // uzavře soubor po uložení stromů
      if (stream_for_saving == null) return;
        String txt = "";
        if (format_for_saving == FORMAT_FS) {
            txt = "\n"; // celý soubor musí být zakončen právě jedním novým řádkem
        }

        try {
            writeString(txt);
            stream_for_saving.close();
        }
        catch (Exception e) {
            debug("\nNGForestSaver.closeFile: An error occured during closing the file: " + e);
        }
    } // closeStream

    private String getTreeId() { // vrátí id prvního stromu právě načteného lesa forest_for_saving
        return forest_for_saving.getId();
    }

    private void saveResult() {
        //debug("\nUživatel chce uložit nalezené stromy na lokální disk s tímto nastavením:");
        //debug("\n  - cílový adresář: " + directory);
        //debug("\n  - prefix jmen souborů: " + files_prefix);
        //debug("\n  - max. počet stromů na soubor: " + file_size);
        //debug("\n  - rozsah ukládaných stromů - začátek: " + range_start);
        //debug("\n  - rozsah ukládaných stromů - konec: " + range_end);
        //debug("\n  - číslovat soubory od: " + count_start);
        //debug("\n  - použít původní rozdělení stromů do souborů: " + original_separation);
        //debug("\n  - použít původní jména souborů: " + original_names);
        //debug("\n  - vložit meta tag u matchujících vrcholů: " + put_matching_meta_tag);
        //debug("\n  - ukládat vícenásobné výskyty dotazu: " + save_multiple_occurrences);


        initializeSaving(); // vynulování všech informací o průběhu ukládání

        while (readNextForest()) { // dokud je co ukladat; strom k zapsani je v glob. prom. tree_for_saving
            if (stop_request) {
                break;
            }
            number_of_actual_forest++;
            if (number_of_actual_forest < range_start) { // ještě jsem se nedostal k začátku požadovaného rozsahu ukládaných stromů
                saving_state = 2; // signalizuje, že postupuji vpřed k prvnímu stromu rozsahu
                //debug("\nPřeskakuji strom.");
                // se stromem nedělám nic a jdu na další strom
            }
            else { // už se má ukládat
                saving_state = 3; // signalizuje, že už opravdu ukládám stromy
                if (! save_multiple_occurrences) { // jestliže se jeden strom má ukládat pouze jednou, i když v něm byl dotaz nalezen vícekrát
                    String id = getTreeId(); // získám id právě načteného stromu
                    if (last_id.compareTo(id) == 0 && last_id.length()>0) { // pokud id tohoto stromu je stejné jako id minulého stromu a není to prázdný řetězec
                        continue; // strom ignoruji a jdu na další strom
                    }
                    last_id = id;
                }

                setStream(); // takto získám přímo místo, kam rovnou mohu posílat strom (nastaví se glob. prom. stream_for_saving)
                // nekde by se z toho mel udelat BufferedOutputStream, at to jde rychleji
                if (stop_request) {
                    break;
                }
                if (new_stream) {
                    setHead();
                    writeHead();
                }
                writeForest();
                number_of_saved_forests++;
                number_of_saved_forests_in_one_file++;

                if (range_end != 0 && number_of_actual_forest >= range_end) {
                    break; // už byly uloženy všechny stromy z požadovaného rozsahu
                }
            }
            if (stop_request) {
                break;
            }
        } // while next tree
        writeTail(); // uloží informace o tom, jak soubor zobrazovat v Tredu (které atributy vypsat, které uzly a hrany zvýraznit)
        closeStream();
        finishSaving(); // zruší některé informace z průběhu ukládání
    } // saveResult


    private boolean readNextForest() { // precte ze serveru dalsi strom; vrati false, pokud uz byly ulozeny vsechny stromy, jinak true
        // nastavi promennou tree_for_saving; ceka, dokud server nema k dispozici dalsi strom

        int load;

        if (saving_state == 0) { // nastavim server na prvni strom (mohl byt nekde u dalsich stromu) a prectu ho
            // nejprve dolezu zpět k prvnímu stromu
            saving_state = 1; // signalizuje, že jsem ve fázi návratu k prvnímu stromu
            if (jaaa.kom_net.getServerVersion().compareToIgnoreCase("1.75")<0) { // pro kompatibilitu se staršími servery
                do {
                    //debug ("\npokus o načtení předchozího stromu ...");
                    load = jaaa.kom_net.loadPrevTree(ServerCommunication.GET_TREE_SUBTYPE_OCCURENCE);
                    if (stop_request) {
                        break;
                    }
                    //debug (" návratová hodnota == " + load);
                } while (load == 0); // dokud se daří načíst předchozí strom
            }
            else if (jaaa.kom_net.getServerVersion().compareToIgnoreCase("1.82")<0) { // novejší servery do verze 1.82 už umějí skočit rovnou na první strom
                load = jaaa.kom_net.loadPrevTree(ServerCommunication.GET_TREE_SUBTYPE_FIRST);
            }
            else { // od verze 1.83 se na první strom leze jinak (funkcí loadNextTree) - kvůli možnosti mazání stromů z výsledku
                do {
                    //debug ("\npokus o načtení prvního stromu ...");
                    load = jaaa.kom_net.loadNextTree(null,ServerCommunication.GET_TREE_SUBTYPE_FIRST, false);
                    if (stop_request) {
                        break;
                    }
                    //debug (" návratová hodnota == " + load);
                    if (load == 1) { // prvni strom jeste neni pripraven
                        try {
                            sleep(1000); // dam serveru aspon vterinu sanci najit prvni strom; kdyz nebude fungovat sleep, dam sem zdrzuj
                        }
                        catch (InterruptedException e) {
                            debug("\nNGForestSaver.readNextForest: Sleep interrupted.");
                        }
                    }
                } while (load == 1); // dokud není první strom připraven
            }
            // teď je první strom načten
            if (stop_request) {
                return false;
            }
            forest_for_saving = jaaa.kom_net.getForest();
            if (forest_for_saving == null) return false;
            return true;
        }
        else { // prectu dalsi strom
            do {
                //debug ("\npokus o načtení dalšího stromu ...");
                load = jaaa.kom_net.loadNextTree(null,ServerCommunication.GET_TREE_SUBTYPE_OCCURENCE, false);
                if (stop_request) {
                    break;
                }
                //debug (" návratová hodnota == " + load);
                if (load == 1) { // dalsi strom jeste neni pripraven
                    try {
                        sleep(1000); // dam serveru aspon vterinu sanci najit dalsi strom; kdyz nebude fungovat sleep, dam sem zdrzuj
                    }
                    catch (InterruptedException e) {
                        debug("\nNGForestSaver.readNextForest: Sleep interrupted.");
                    }
                }
            } while (load == 1); // dokud není další strom připraven
            if (stop_request) {
                return false;
            }
            if (load == 2) { // zadny dalsi strom uz neni
                return false;
            }
            forest_for_saving = jaaa.kom_net.getForest();
            if (forest_for_saving == null) return false;
            return true;
        }
    } // readNextForest


    public void startSaving() { // spustďż˝uklďż˝ďż˝ďż˝strom na lokďż˝nďż˝disk
        stop_request = false; // pokud bude z vnďż˝ku nastaveno na true, poznďż˝novďż˝vlďż˝no, e mďż˝skonďż˝t
        start();
    }

    public void stopSaving() { // upozorní tento objekt, že má skončit s ukládáním stromů
        stop_request = true;
        saving_result_status = SAVING_CANCELED_BY_INVOKER;
    }

    /**
     * It displayes a dialog window asking user to confirm overwriting of a file or
     * cancel the saving. It can show the button "Yes to all" if wanted.
     * The function returns 1 if "Yes" button was pressed,
     * 0 if "Cancel" button was pressed and 2 if "Yes to all" button was pressed.
     */
    private int overwriteDialog(String window_title, String file_name, boolean all_button) {

        Object[] options;
        Object[] options_all = {i18n.getString("OVERWRITE_DIALOG_YES_BUTTON"),
                                i18n.getString("OVERWRITE_DIALOG_CANCEL_BUTTON"),
                                i18n.getString("OVERWRITE_DIALOG_ALL_BUTTON")};
        Object[] options_not_all = {i18n.getString("OVERWRITE_DIALOG_YES_BUTTON"),
                                    i18n.getString("OVERWRITE_DIALOG_CANCEL_BUTTON")};
        if (all_button) {
            options = options_all;
        }
        else {
            options = options_not_all;
        }

        int n = JOptionPane.showOptionDialog(jaaa,
                i18n.getString("OVERWRITE_DIALOG_MESSAGE_PREFIX")
                + " " + file_name + " "
                + i18n.getString("OVERWRITE_DIALOG_MESSAGE_SUFFIX"),
                window_title,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1]);

        if (n==JOptionPane.CLOSED_OPTION) return 0; // zavření okna = cancel
        if (n==JOptionPane.CANCEL_OPTION) return 2; // třetí tlačítko je pro "Yes to all"
        if (n==JOptionPane.NO_OPTION) return 0; // druhé tlačítko je pro "Cancel"
        if (n==JOptionPane.OK_OPTION) return 1; // první tlačítko je pro "Yes"

        return n;
    } // overwriteDialog



}
