package cz.cuni.mff.mirovsky.properties;

import java.util.Iterator;

import cz.cuni.mff.mirovsky.ShowMessagesAble;

/**
 * An object for keeping and changing properties.
 */

public class PropertiesManager {

	private Properties properties_actual; // aktuální stav properties
	private Properties properties_original; // výchozí stav properties

	private ShowMessagesAble mess; // objekt (dodaný zvnějšku), kterému mají být posílány chybové a informační hlášky

    public PropertiesManager() { // konstruktor
		Properties properties = new Properties();
		initialize (properties, null);
    }

	public PropertiesManager (ShowMessagesAble p_mess) { // konstruktor
		Properties properties = new Properties();
		initialize (properties, p_mess);
	}

	public PropertiesManager (Properties properties) { // konstruktor
		initialize (properties, null);
	}

	public PropertiesManager (Properties properties, ShowMessagesAble p_mess) { // konstruktor
		initialize (properties, p_mess);
	}

	private void initialize (Properties properties, ShowMessagesAble p_mess) { // vlastní konstrukční akce
		mess = p_mess; // uchovám odkaz na objekt, kterému mají být posílány chybové a info hlášky
		properties_original = properties.getClone();
		properties_actual = properties_original.getClone();
		//debug("\nPropertiesManager.initialize: Properties manager vytvořen.");
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

	public void setProperties (Properties properties) { // přepíše aktuální properties těmito
		properties_actual = properties.getClone();
	}

	public void setProperty (Property property) { // přepíše jednu Property v aktuálních properties touto
		properties_actual.setProperty(property);
	}

	public void setPropertyValue (String section_name, String property_name, String property_value) { // přepíše jednu Property v aktuálních properties touto
		properties_actual.setPropertyValue(section_name, property_name, property_value);
	}

	public Properties getProperties () { // vrátí aktuální Properties
		return properties_actual;
	}

	public Property getProperty (String section_name, String property_name) { // vrátí jednu property z aktuálních Properties
		return properties_actual.getProperty (section_name, property_name);
	}

	public void setOriginalProperties (Properties properties) { // přepíše defaultní Properties těmito - tj. nastaví výchozí stav
		properties_original = properties.getClone();
	}

	public Properties getOriginalProperties () { // vrátí defaultní Properties
		return properties_original;
	}

	public void restoreOriginalProperties () { // aktuální Properties přepíše defaultními Properties - tj. obnoví výchozí stav
		properties_actual = properties_original.getClone();
	}

	public void storeOriginalProperties () { // defaultní Properties přepíše aktuálními Properties - tj. nastaví výchozí stav podle aktuálního
		setOriginalProperties(properties_actual);
	}

	public static void mergeProperties (Properties target, Properties addition) { // přidá properties addition k properties target; shodné přepíše
        if (target == null || addition == null) return;
        Iterator iterator = addition.getIteratorOverValues();
		PropertiesSection section_target; // k této sekci se bude přidávat...
		PropertiesSection section_addition; // ...tato sekce
		String section_name;
		while (iterator.hasNext()) { // přes všechny prvky hašovací tabulky, tj. všechny sekce
			section_addition = (PropertiesSection)iterator.next();
			section_name = section_addition.getName();
			section_target = target.getSection(section_name);
			if (section_target == null) { // sekce v cílových Properties není
			    target.putSection(section_addition);
			} else { // sekce již v cílových Properties je
			    target.mergeSections(section_target, section_addition);
				//target.putSection(section_target);
			}
		}
	}

	public void mergeToActualProperties (Properties addition) { // přidá addition properties k aktuálním; shodné přepíše
        mergeProperties(properties_actual, addition);
	}

	public PropertiesSection getSection (String section_name) {
	    return properties_actual.getSection(section_name);
	}

	public void removeProperty (Property property) { // odstraní property z aktuálních properties, pokud tam existuje
		properties_actual.removeProperty (property);
	}

	public void removeProperty (String section_name, String property_name) {
		properties_actual.removeProperty(section_name, property_name);
	}

	// následující funkce slouží k ukládání a čtení Properties do/z externího zdroje

	public void loadProperties (String source_location, String source_name) { // načte aktuální properties ze zdroje source_name umístěného v source_location
		PropertiesLoader loader = new PropertiesLoader();
		properties_actual = loader.loadProperties(source_location, source_name);
	}

//	public Property loadProperty (String source_name, String section_name, String property_name) { // vrátí jednu property z daného zdroje
//
//	}

//	public PropertiesSection loadPropertiesSection (String source_name, String section_name) { // vrátí jednu sadu properties z daného zdroje
//
//	}

	public void saveProperties (String source_location, String source_name) { // uloží Properties do zdroje; přepíše původní zdroj
		PropertiesLoader loader = new PropertiesLoader();
		loader.saveProperties(source_location, source_name, properties_actual);
	}

//	public boolean saveProperty (String source_name, Property property) { // uloží Property do zdroje; přepíše původní obsah dané property ve zdroji
//
//	}

//	public boolean savePropertiesSection (String source_name, PropertiesSection section) { // uloží Properties section do zdroje; přepíše původní obsah kolidujících properties
//
//	}


} // class PropertiesManager
