package cz.cuni.mff.mirovsky.properties;

import java.io.*;
import java.util.Iterator;
import java.util.HashMap;

import cz.cuni.mff.mirovsky.ShowMessagesAble;

/**
 * Class PropertiesLoader is used to store and restore properties to/from storage - this is implementation for files.
 */

public class PropertiesLoader {

	private String sections_delimiter = "------------------------------"; // oddělovač sekcí
	private String section_prefix = "Section"; // prefix názvu sekce v souboru
	private String comment_prefix = ";;"; // prefix komentářů v souboru
	private String value_assigner = "="; // přiřazovací znaménko pro hodnoty properties

    private final static String newline_replacement = "<EOL>"; // při ukládání do souboru se případné nové řádky v obsahu property zamění za tohle
    public final static byte EOL = 13;  // ukoncovaci znak radku; pozor! tohle je kopie z ServerCommunication
    public final static char EOL_char = (char)EOL;
    public final static String EOL_String = new String("" + EOL_char);
    private ShowMessagesAble mess; // objekt (dodaný zvnějšku), kterému mají být posílány chybové a informační hlášky

    public PropertiesLoader() {
		initialize (null);
    }

	public PropertiesLoader (ShowMessagesAble p_mess) {
		initialize (p_mess);
	}

	private void initialize (ShowMessagesAble p_mess) { // vlastní konstrukční akce
	    mess = p_mess; // uchovám odkaz na objekt, kterému mají být posílány chybové a info hlášky

	}

	private void debug (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
	    if (mess != null) {
		    mess.debug(message);
		}
		else {
			System.out.print(message);
		}
	}

	private void inform (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
		if (mess != null) {
		    mess.inform (message);
		}
		else {
		    System.out.print(message);
		}
	}

	public void setSectionsDelimiter (String p_delimiter) {
	    sections_delimiter = new String(p_delimiter);
	}
	public String getSectionsDelimiter () {
	    return sections_delimiter;
	}

	public void setSectionPrefix (String p_prefix) {
	    section_prefix = new String(p_prefix);
	}
	public String getSectionPrefix () {
	    return section_prefix;
	}

	public void setCommentPrefix (String p_prefix) {
		comment_prefix = new String(p_prefix);
	}
	public String getCommentPrefix () {
	    return comment_prefix;
	}

	public void setValueAssigner (String p_assigner) {
		value_assigner = new String(p_assigner);
	}
	public String getValueAssigner () {
	    return value_assigner;
	}

	private File getFile(String source_location, String source_name, boolean mkdir) { // vrátí objekt File ukazující na soubor k ukládání properties
		// pokud mkdir == true, vytvoří adresář source_location, když neexistuje
		String user_home = System.getProperty("user.home");
		String file_separator = System.getProperty("file.separator");
		String file_name; // zde bude definitivní jméno souboru

		String hide_mark; // pro unixové prostředí přidám tečku před cestu k souboru nebo před jméno souboru, pokud cesta není určena
		if (file_separator.equals("\\")) { // jsem-li v M$ Windows
			hide_mark = new String("");
		}
		else { // na ostatních systémech přidám tečku před jméno souboru
		    hide_mark = new String(".");
		}

		file_name = new String(); // zatím prázdný řetězec
		String absolute; // zde se vytvoří absolutní cesta k adresáři, kde má být soubor hledán
		if (source_location.length() == 0) { // umístění není určeno, použije se domovský adresář
		    absolute = user_home;
			file_name += hide_mark;
		}
		else if ((new File(source_location)).isAbsolute()) { // umístění je absolutní cesta
			absolute = source_location;
			file_name += hide_mark;
		}
		else { // umístění je relativní cesta - jako výchozí bod se použije domovský adresář
			absolute = user_home + file_separator + hide_mark + source_location;
		}

		File file_abs = new File(absolute);
		if (!file_abs.exists()) { // pokud adresář neexistuje
		    if (mkdir) file_abs.mkdirs();
		}

		file_name += source_name;
		File file = new File(absolute, file_name);
		return file;
	} // getFile

	public Properties loadProperties (String source_location, String source_name) { // vrátí objekt Properties načtený ze souboru podle source_name v adresáři dle source_location
		Properties properties = null; // implicitní návratová hodnota - pro případ neúspěchu

		File file = getFile(source_location, source_name, false);
		//debug ("\npath = " + file.getPath());
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			if (file.exists()) {
				if (file.canRead()) {
			        properties = loadProperties(reader);
				}
				else {
				    debug("\nPropertiesLoader.loadProperties: soubor " + file.getPath() + " nelze číst.");
				}
			}
			else {
			    debug("\nPropertiesLoader.loadProperties: soubor " + file.getPath() + " neexistuje.");
			}
		}
		catch (IOException e) {
		    debug("\nPropertiesLoader.loadProperties: problém při čtení ze souboru: " + e);
		}
		finally { // zavřu soubor, pokud byl otevřen
		    try {
				if (reader != null) {
				    reader.close();
				}
		    }
			catch (IOException e) {
		        debug("\nPropertiesLoader.loadProperties: problém při zavírání souboru: " + e);
			}
		}

		return properties;
	}

	private Properties loadProperties(FileReader reader) throws IOException {
		// nyní vím, že soubor existuje a lze číst
	    Properties properties = new Properties();
		BufferedReader buf_reader = new BufferedReader(reader);
		PropertiesSection section = null; // sem budu načítat vždy jednu sekci
		String line; // sem se bude načítat vždy jeden řádek vstupního souboru
		int length;
		int state; // stav čtení - logická pozice v souboru
		// state == 0 ... začátek souboru - očekávám komentář souboru nebo sekci
		// state == 1 ... začátek sekce - očekávám komentář sekce nebo property; v nejhorším začátek další sekce
		// state == 2 ... vnitřek sekce - očekávám property nebo další sekci

		state = 0;
		while ((line = buf_reader.readLine()) != null) { // dokud nejsem na konci souboru
			length = line.length();
		    if (length==0) continue; // prázdný řádek přeskočím
		    switch (state) {
			    case 0: {
					if (isComment(line)) { // jedná-li se o komentář
						addPropertiesComment(properties,line);
					} else {
						if (isSection(line)) { // jde-li o začátek sekce
							section = new PropertiesSection();
							setSectionName(section,line);
							state = 1;
					    } else {
							if (isProperty(line)) { // jde-li o definici property
								debug("\nPropertiesLoader.loadProperties: definice property není možná mimo sekci: \n" + line);
							} else {
				    			debug ("\nPropertiesLoader.loadProperties: řádek >>> " + line + " <<< nemá podporovaný formát");
							} // else isProperty
						} // else isSection
					} // else isComment
					break;
			    }
				case 1: {
					if (isComment(line)) { // jedná-li se o komentář
						String pure_comment = getPureContent(line,comment_prefix); // odříznu prefix komentáře
						if (isSectionsDelimeter(pure_comment)) break; // oddělovač sekcí nepočítám za komentář sekce; navíc minulá sekce neměla žádnou property
						else {
							addSectionComment(section,pure_comment);
						}
					} else {
						if (isSection(line)) { // jde-li o začátek další sekce
							// je to dost předčasný začátek, minulá sekce ještě neobsahuje žádné property, ale co naděláme
							addSectionToProperties(properties,section);
							section = new PropertiesSection();
							setSectionName(section,line);
					    } else {
							if (isProperty(line)) { // jde-li o definici property
								addPropertyToSection(section,line);
								state = 2;
							} else {
				    			debug ("\nPropertiesLoader.loadProperties: řádek >>> " + line + " <<< nemá podporovaný formát");
							} // else isProperty
						} // else isSection
					} // else isComment
					break;
				}
				case 2: {
					if (isComment(line)) { // jedná-li se o komentář
						String pure_comment = getPureContent(line,comment_prefix);
						if (isSectionsDelimeter(pure_comment)) break; // oddělovač sekcí nepočítám za komentář sekce; navíc minulá sekce neměla žádnou property
						else {
					    	debug ("\nPropertiesLoader.loadProperties: ignoruji komentář uprostřed sekce:\n" + line);
						}
					} else {
						if (isSection(line)) { // jde-li o začátek další sekce
							addSectionToProperties(properties,section);
							section = new PropertiesSection();
							setSectionName(section,line);
							state = 1;
					    } else {
							if (isProperty(line)) { // jde-li o definici property
								addPropertyToSection(section,line);
							} else {
				    			debug ("\nPropertiesLoader.loadProperties: řádek >>> " + line + " <<< nemá podporovaný formát");
							} // else isProperty
						} // else isSection
					} // else isComment
					break;
				}
		    } // switch

			//debug("\n" + line);
		}

		if (section != null) { // je potřeba přidat posledně načítanou sekci k properties
			addSectionToProperties(properties,section);
		}
		return properties;
	}

	private boolean isComment (String line) { // returns true, if line begins with comment_prefix, otherwise returns false
	    return line.startsWith(comment_prefix);
	}

	private boolean isSection (String line) { // returns true, if line begins with section_prefix, otherwise returns false
	    return line.startsWith(section_prefix);
	}

	private boolean isProperty (String line) { // returns true, if line is a definition of a property; otherwise returns false
		// předpokládá, že testy na komentář a sekci již proběhly s negativním výsledkem
		int assigner_position = line.indexOf(value_assigner);
		if (assigner_position == -1) return false; // v řetězci se nevyskytuje znak přiřazení, takže to nemůže být definice property
		String left = line.substring(0,assigner_position).trim();
		if (left.length() == 0) return false; // nalevo od přiřazení není žádný nebílý znak
		String right = line.substring(assigner_position+1).trim();
		if (right.length() == 0) return false; // napravo od přiřazení není žádný nebílý znak
		if (isComment(right)) return false; // napravo od přiřazení je jen komentář
		return true;
	}

	private boolean isSectionsDelimeter (String s) { // returns true, if line is the sections delimiter
	    if (s.equals(sections_delimiter)) return true;
		else return false;
	}

	private String getPureContent(String source, String prefix) { // odříznu prefix z source a bílé znaky z okrajů
	    return source.substring(prefix.length()).trim();
	}

	private void addPropertiesComment (Properties properties, String line) { // nastaví komentář celých properties
		String pure_comment = getPureContent(line,comment_prefix); // odříznu prefix komentáře a bílé znaky okolo
	    properties.setComment(pure_comment);
	}

	private void setSectionName (PropertiesSection section, String line) { // nastaví jméno sekce
		String pure_name = line.substring(section_prefix.length()); // odříznu prefix sekce
		pure_name = pure_name.trim(); // odříznu bílé znaky (oddělující jméno)
		section.setName(pure_name);
	}

	private void addSectionComment (PropertiesSection section, String comment) {
		section.setComment(comment);
	}

	private void addSectionToProperties(Properties properties, PropertiesSection section) { // jen přidám sekci k properties
	    properties.putSection(section);
	}

	private void addPropertyToSection (PropertiesSection section, String line) { // přidá property k sekci
		// pozitivní test na property by měl předcházet, minimálně předpokládá, že proběhl test na komentář a sekci s negativním výsledkem
        line = line.replaceAll(newline_replacement, EOL_String);
        int assigner_position = line.indexOf(value_assigner);
		if (assigner_position == -1) return; // v řetězci se nevyskytuje znak přiřazení, takže to nemůže být definice property
		String left = line.substring(0,assigner_position).trim();
		if (left.length() == 0) return; // nalevo od přiřazení není žádný nebílý znak
		String right = line.substring(assigner_position + value_assigner.length()).trim();
		if (right.length() == 0) return; // napravo od přiřazení není žádný nebílý znak
		if (isComment(right)) return; // napravo od přiřazení je jen komentář
		int comment_position = right.indexOf(comment_prefix);
		String comment;
		String value;
		if (comment_position == -1) {
			comment = "";
			value = right;
		}
		else {
		    comment = right.substring(comment_position + comment_prefix.length()).trim();
			value = right.substring(0,comment_position).trim();
		}
		Property property = new Property(section.getName(),left,value,comment);
		section.setProperty(property);
	}

	//	public Property loadProperty (String source_name, String section_name, String property_name) { // vrátí jednu property z daného souboru
//
//	}

//	public Property loadPropertiesSection (String source_name, String section_name) { // vrátí jednu sadu properties z daného souboru
//
//	}

	public void saveProperties (String source_location, String source_name, Properties properties) { // uloží Properties do souboru; přepíše původní soubor

		if (properties == null) return; // není co ukládat

		File file = getFile(source_location, source_name, true);

		//debug ("\npath = " + file.getPath());
		FileWriter writer = null;
		try {
			file.createNewFile(); // vytvořím prázdný soubor, pokud neexistuje
			writer = new FileWriter(file);
			saveProperties(writer, properties);
			writer.flush();
		}
		catch (IOException e) {
		    debug("\nPropertiesLoader.saveProperties: problém při zápisu do souboru: " + e);
		}
		finally { // zavřu soubor, pokud byl otevřen
		    try {
				if (writer != null) {
				    writer.close();
				}
		    }
			catch (IOException e) {
		        debug("\nPropertiesLoader.saveProperties: problém při zavírání souboru: " + e);
			}
		}
	}

	private void saveProperties(FileWriter writer, Properties properties) throws IOException {
		if (properties == null) return; // není co ukládat

		// nejprve vypíšu komentář k celým properties, pokud je nastaven
		if (properties.getComment().length() > 0) {
		    writer.write(comment_prefix + " " + properties.getComment() + "\n");
		}
		// a pak uložím všechny sekce
		Iterator iterator = properties.getIteratorOverValues();
		PropertiesSection section;
		String section_name;
		boolean first = true; // signalizuji první průchod následujícím cyklem
		while (iterator.hasNext()) { // přes všechny prvky hašovací tabulky, tj. všechny sekce
		    section = (PropertiesSection)iterator.next();
			if (section.isEmpty()) continue; // prázdné sekce neukládám
			if (first) first = false;
			else { // oddělím sekce
			    writer.write("\n\n" + comment_prefix + sections_delimiter + "\n");
			}
			section_name = section.getName();
			saveOneSection(writer,section);
		}
	}

	private void saveOneSection(FileWriter writer, PropertiesSection section) throws IOException {
		writer.write("\n" + section_prefix + " " + section.getName());
		String comment = section.getComment();
		if (comment.length() > 0) {
		    writer.write("\n" + comment_prefix + " " + section.getComment());
		}
		writer.write("\n");
		Iterator iterator = section.getIteratorOverValues();
		Property property;
		while (iterator.hasNext()) { // přes všechny prvky hašovací tabulky, tj. všechny property
		    property = (Property)iterator.next();
			saveOneProperty(writer,property);
		}
	}

	private void saveOneProperty(FileWriter writer, Property property) throws IOException {
		String output = "\n" + property.getName() + " " + value_assigner + " " + property.getValue();
		if (property.getComment().length()>0) { // existuje-li komentář k této property
		    output += "  " + comment_prefix + " " + property.getComment();
		}
        output = output.replaceAll(EOL_String, newline_replacement);
        writer.write(output);
	}

//	public boolean saveProperty (String source_name, Property property) { // uloží Property do souboru; přepíše původní obsah dané property v souboru
//
//	}

//	public boolean savePropertiesSection (String source_name, PropertiesSection section) { // uloží jednu sadu properties do daného souboru
//
//	}

} // class PropertiesLoader
