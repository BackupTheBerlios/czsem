package cz.cuni.mff.mirovsky.properties;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Class Properties keeps a set of properties sections, stored together e.g. in one file.
 */

public class Properties implements ItemSet {

	private HashMap sections; // tabulka sekcí
	private String default_section_name; // jméno defaultní sekce properties
	private String comment; // úvodní komentář k celým properties

	public Properties() {
		initialize("");
    }

	public Properties(String p_comment) {
		initialize(p_comment);
    }

	private void initialize(String p_comment) {
		sections = new HashMap();
		default_section_name = new String("Other"); // implicitní jméno sekce pro property s nespecifikovanou sekcí
		comment = new String(p_comment);
	}

	public Properties getClone() {
		Properties clone = new Properties();
		clone.setComment(comment);
		clone.setDefaultSectionName(default_section_name);
		Iterator iterator = getIteratorOverValues();
		PropertiesSection section;
		while (iterator.hasNext()) { // zkopíruji všechny prvky hašovací tabulky, tj. všechny sekce
		    section = (PropertiesSection)iterator.next();
			clone.putSection(section);
		}
		return clone;
	}

	public Property getProperty (String section_name, String property_name) {
		if (section_name == null) section_name = default_section_name;
		PropertiesSection section = (PropertiesSection)sections.get(section_name);
		if (section == null) {
			// System.out.print("\nProperties.getProperty: sekce " + section_name + " neexistuje!");
			return null;
		}
		Property property = section.getProperty(property_name);
		return property;
	}



	public int getIntProperty(String section, String name, int default_value) {
		Property p;
		int value;
		try {
		    p = getProperty(section, name);
			if (p != null) value = Integer.parseInt(p.getValue());
			else {
			    return default_value;
			}
		}
		catch (Exception e) {
			System.out.print("\nChyba při převodu řetězce na číslo: " + e);
		    return default_value;
		}
		return value;
	} // getIntProperty

	public long getLongProperty(String section, String name, long default_value) {
		Property p;
		long value;
		try {
		    p = getProperty(section, name);
			if (p != null) value = Long.parseLong(p.getValue());
			else {
			    return default_value;
			}
		}
		catch (Exception e) {
			System.out.print("\nChyba při převodu řetězce na číslo: " + e);
		    return default_value;
		}
		return value;
	} // getLongProperty

        public boolean getBooleanProperty(String section, String name, boolean default_value) {
		Property p;
		boolean value;
	    p = getProperty(section, name);
		if (p != null) value = (p.getValue().compareToIgnoreCase("true") == 0);
		else {
		    return default_value;
		}
		return value;
	} // getBooleanProperty

	public String getStringProperty(String section, String name, String default_value) {
		Property p;
		String value;
	    p = getProperty(section, name);
		if (p != null) value = p.getValue();
		else {
		    return default_value;
		}
		return value;
	} // getStringProperty


	public void setProperty (Property property) { // k sekci přidá tuto property (starou nahradí)
		if (property == null) return;
		String section_name = property.getSectionName();
		if (section_name == null) section_name = default_section_name;
		PropertiesSection section = (PropertiesSection)sections.get(section_name);
		if (section == null) {
			section = new PropertiesSection(section_name);
		}
		section.setProperty(property); // k sekci přidám tuto property (popř. nahradím starou)
		sections.put(section_name, section); // starou sekci nahradím novou
		//System.out.print("\nProperties.setProperty: sekce: " + section_name + ", property: " + property.getName() + ", obsah: " + property.getValue());
	}

	public void setPropertyValue (String section_name, String property_name, String property_value) { // nastaví hodnotu property v sekci
		if (section_name == null) section_name = default_section_name;
		PropertiesSection section = (PropertiesSection)sections.get(section_name);
		if (section == null) {
			//System.out.print("\nProperties.setPropertyValue: vytvářím novou sekci se jménem " + section_name);
			section = new PropertiesSection(section_name);
		}
		Property property = section.getProperty(property_name);
		if (property == null) {
			//System.out.print("\nProperties.setPropertyValue: vytvářím novou property");
			property = new Property();
			property.setName(property_name);
			property.setSectionName(section_name);
		}
		property.setValue(property_value);
		section.setProperty(property); // k sekci přidám tuto property (popř. nahradím starou)
		sections.put(section_name, section); // starou sekci nahradím novou
	}

	public void updateProperty(String section_name, String property_name, String value, String default_comment) { // nastaví specifikovanou property; pokud u ní dosud není komentář, použije specifikovaný
		setPropertyValue(section_name, property_name, value);
		Property p = getProperty(section_name, property_name);
		if (p.getComment().length() <= 0) { // dosud žádný komentář (test proto, abych nepřepsal případný komentář do souboru ručně dopsaný uživatelem
		    p.setComment(default_comment);
		}
	} // updateProperty


	public void removeProperty (Property property) { // odstraní property z aktuálních properties, pokud tam existuje
	    String property_name = property.getName();
		String section_name = property.getSectionName();
		removeProperty (section_name, property_name);
	}

	public void removeProperty (String p_section_name, String property_name) {
		if (property_name == null) return; // není co ukládat
		if (property_name.length() == 0) return; // rovněž
		String section_name; // při absenci jména sekce použiji defaultní sekci
	    if (p_section_name == null) section_name = getDefaultSectionName();
		else {
			if (p_section_name.length() == 0) section_name = getDefaultSectionName();
			else section_name = p_section_name;
		}
		PropertiesSection section = getSection(section_name);
		if (section == null) return; // sekce daného jména neexistuje
		section.removeProperty(property_name);
	}

    public PropertiesSection getSection (String section_name) {
		if (section_name == null || section_name.length()==0) section_name = default_section_name;
		PropertiesSection section = (PropertiesSection)sections.get(section_name);
		return section;
	}

    public void putSection (PropertiesSection section) { // vloží sekci k tabulce properties; případnou starší se stejným jménem nahradí
		if (section == null) return;
		String section_name = section.getName();
		if (section_name.length()==0) {
		    section_name = default_section_name;
			section.setName(section_name);
		}
		PropertiesSection section_clone = section.getClone();
		sections.put(section_clone.getName(), section_clone);
	}

	public static void mergeSections(PropertiesSection target, PropertiesSection addition) {
        if (target == null || addition == null) return;
        Iterator iterator = addition.getIteratorOverValues();
		Property property; // tato property se bude přidávat
		while (iterator.hasNext()) { // přes všechny prvky hašovací tabulky, tj. všechny přidávané property
			property = (Property)iterator.next();
		    target.setProperty(property);
		}
	}

	public Iterator getIteratorOverNames () {
		return sections.keySet().iterator();
	}

	public Iterator getIteratorOverValues () {
		return sections.values().iterator();
	}

	public String getDefaultSectionName() {
		return default_section_name;
	}

	public void setDefaultSectionName(String p_name) {
		default_section_name = new String(p_name);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String p_comment) {
		comment = new String(p_comment);
	}

	public void removeSection (String section_name) { // odstraní sekci, pokud je v properties; jinak nic
	    if (section_name == null) return;
		if (section_name.length() == 0) return; // v obou případech není co odstraňovat
		//Property property = getProperty(property_name);
		//if (property == null) return; // property nebyla v sekci
		sections.remove(section_name);
	}


	// následující funkce implementují funkce interfacu ItemSet

	public String getItemValue(String item_name) { // vrátí "hodnotu" sekce s daným jménem
	    PropertiesSection item = getSection(item_name);
		if (item == null) return "";
		return item.getValue();
	}

	public String getItemComment(String item_name) { // vrátí komentář sekce s daným jménem
		PropertiesSection item = getSection(item_name);
		if (item == null) return "";
		return item.getComment();
	}

    public void removeItem(String item_name) { // smaže danou sekci z properties
        removeSection(item_name);
    }


} // class Properties
