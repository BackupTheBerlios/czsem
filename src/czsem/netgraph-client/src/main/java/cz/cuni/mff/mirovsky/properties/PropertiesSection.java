package cz.cuni.mff.mirovsky.properties;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Class PropertiesSection keeps a set of related properties.
 */

public class PropertiesSection implements Item, ItemSet {

	private String comment;
	private String name;
	private HashMap properties;


	public PropertiesSection() {
		initialize("","");
	}

	public PropertiesSection(String p_name) {
		initialize(p_name,"");
	}

	public PropertiesSection(String p_name, String p_comment) {
		initialize(p_name, p_comment);
	}

	private void initialize(String p_name, String p_comment) {
	    comment = new String(p_comment);
		name = new String(p_name);
		properties = new HashMap();
	}

	public PropertiesSection getClone() {
		PropertiesSection clone = new PropertiesSection(name);
		clone.setComment(comment);
		Iterator iterator = getIteratorOverValues();
		Property property;
		while (iterator.hasNext()) { // zkopíruji všechny prvky hašovací tabulky, tj. všechny property
		    property = (Property)iterator.next();
			clone.setProperty(property);
		}
		return clone;
	}

	public Iterator getIteratorOverNames() {
		return properties.keySet().iterator();
	}

	public Iterator getIteratorOverValues() {
		return properties.values().iterator();
	}

	public boolean isEmpty() { // vrátí true, pokud sekce neobsahuje žádné properties
	    return properties.isEmpty();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String p_comment) {
		comment = new String(p_comment);
	}

	public String getName() {
		return name;
	}

	public void setName(String p_name) {
		name = new String(p_name);
	}

	public Property getProperty (String name) {
		Property p = (Property)properties.get(name);
		return p;
	}

	public void setProperty (Property property) {
		if (property == null) return;
		String name = property.getName();
		properties.put(name,property);
	}

	public void setPropertyValue (String property_name, String property_value) {
	    Property property = new Property(property_name, property_value);
		properties.put(property_name, property);
	}

	public void removeProperty (String property_name) { // odstraní property, pokud je v sekci; jinak nic
	    if (property_name == null) return;
		if (property_name.length() == 0) return; // v obou případech není co odstraňovat
		//Property property = getProperty(property_name);
		//if (property == null) return; // property nebyla v sekci
		properties.remove(property_name);
	}

	// následující funkce implementují funkce interfaců

	public String getValue() { // vrátí hodnoty všech properties oddělené znakem newline
		String s = new String();
		Iterator iterator = this.getIteratorOverValues();
		Property property;
		while (iterator.hasNext()) {
			property = (Property)iterator.next();
			s += property.getValue() + "\n";
		}
		return s;
	}

	public String getItemValue(String item_name) { // vrátí hodnotu property s daným jménem
		Property p = getProperty(item_name);
		if (p == null) return "";
		return p.getValue();
	}

	public String getItemComment(String item_name) { // vrátí komentář k property s daným jménem
	    Property p = getProperty(item_name);
		if (p == null) return "";
		return p.getComment();
	}

    public void removeItem(String item_name) { // smaže danou property ze sekce
        removeProperty(item_name);
    }

} // class PropertiesSection
