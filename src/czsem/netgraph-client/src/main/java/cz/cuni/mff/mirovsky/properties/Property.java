package cz.cuni.mff.mirovsky.properties;

/**
 * Class Property represents one property - its name, value and comment.
 */

public class Property implements Item{

	private String comment;
	private String name;
	private String value;

	private String section_name;

    public Property() {
		initialize("","","","");
    }

	public Property(String p_section_name, String p_name) {
	    initialize(p_section_name, p_name, "", "");
	}

	public Property(String p_section_name, String p_name, String p_value) {
	    initialize(p_section_name, p_name, p_value, "");
	}

	public Property(String p_section_name, String p_name, String p_value, String p_comment) {
	    initialize(p_section_name, p_name, p_value, p_comment);
	}

	private void initialize(String p_section_name, String p_name, String p_value, String p_comment) {
		comment = new String(p_comment);
		name = new String(p_name);
		value = new String(p_value);
		section_name = new String(p_section_name);
	}

	public Property getClone() {
		Property clone = new Property();
		clone.setComment(comment);
		clone.setName(name);
		clone.setValue(value);
		clone.setSectionName(section_name);
		return clone;
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

	public String getValue() {
		return value;
	}

	public void setValue(String p_value) {
		//System.out.print("\nProperty.setValue: hodnota property nastavena na " + p_value);
		value = new String(p_value);
	}

	public String getSectionName() {
		return section_name;
	}

	public void setSectionName(String p_name) {
		section_name = new String(p_name);
	}

} // class Property
