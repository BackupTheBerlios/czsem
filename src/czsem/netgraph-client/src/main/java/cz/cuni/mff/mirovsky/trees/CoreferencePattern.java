package cz.cuni.mff.mirovsky.trees;

import java.awt.Color;
import javax.swing.DefaultListModel;
import cz.cuni.mff.mirovsky.ShowMessagesAble;

/**
 * A class for keeping one pattern of references in trees.
 */

public class CoreferencePattern {

    private ShowMessagesAble mess; // objekt pro výpis hlášek

    private String coreference_name, display_mode;
    private boolean display; // actual setting of (non-)displaying the pattern
    private String start_node_attr_name, end_node_attr_name;
    private DefaultListModel start_node_ignore_values, end_node_ignore_values;
    private boolean start_node_nil_arrow, end_node_nil_arrow;
    private Color general_color;
    private int general_shape;
    private String value_dependent_attr_name;
    private DefaultListModel value_dependent_values, value_dependent_colors, value_dependent_shapes;

    static final public int SHAPE_DASHES_DARC = 0; // line of dashes, arc curved downwards

    static final private String LABEL_NAME="name";
    static final private String LABEL_DISPLAY_MODE="display";
    static final private String VALUE_NEVER="0";
    static final private String VALUE_ALWAYS="1";
    static final private String VALUE_USER_CONTROLLED_DEFAULT_YES="2";
    static final private String VALUE_USER_CONTROLLED_DEFAULT_NO="3";
    static final private String LABEL_START_ATTR="start_attr";
    static final private String LABEL_END_ATTR="end_attr";
    static final private String LABEL_START_IGNORE="start_ignore";
    static final private String LABEL_END_IGNORE="end_ignore";
    static final private String LABEL_START_NIL_ARROW="start_nil_arrow";
    static final private String LABEL_END_NIL_ARROW="end_nil_arrow";
    static final private String LABEL_GENERAL_COLOR="general_color";
    static final private String LABEL_GENERAL_SHAPE="general_shape";
    static final private String LABEL_VALUE_DEPEND_ATTR="value_depend_attr";
    static final private String LABEL_VALUE_DEPEND_PATTERN="value_depend";
    static final private char ATTRS_DIVIDER = ',';
    static final private char PATTERN_DIVIDER = ';';
    static final private char EQUAL_MARK = '=';


    public CoreferencePattern() {
        mess = null;
        initialize();
    }

    /**
    * Creates an empty pattern
    */
   public CoreferencePattern(ShowMessagesAble p_mess) {
       mess = p_mess;
       initialize();
   }

   private void initialize() {
       coreference_name="unnamed";
       display_mode=VALUE_USER_CONTROLLED_DEFAULT_YES;
       display=true;
       start_node_attr_name="";
       end_node_attr_name="";
       start_node_nil_arrow = true;
       end_node_nil_arrow = true;
       start_node_ignore_values = new DefaultListModel();
       end_node_ignore_values = new DefaultListModel();
       general_color = new Color(0,0,0); // black
       general_shape = SHAPE_DASHES_DARC;
       value_dependent_attr_name = "";
       clearValueDependentValues();
   }

   private void debug (String message) { // vypíše hlášku pomocí externího objektu, pokud je nastaven
       if (mess != null) {
           mess.debug(message);
       }
   }

   /**
    * Returns a deep copy of the coreference pattern
    */
   public CoreferencePattern getClone() {
       CoreferencePattern copy = new CoreferencePattern();
       copy.coreference_name = new String(coreference_name);
       copy.display_mode = new String(display_mode);
       copy.display = display;
       copy.start_node_attr_name = new String(start_node_attr_name);
       copy.end_node_attr_name = new String(end_node_attr_name);
       copy.start_node_nil_arrow = start_node_nil_arrow;
       copy.end_node_nil_arrow = end_node_nil_arrow;
       copy.start_node_ignore_values = new DefaultListModel();
       copyDefaultListModel(copy.start_node_ignore_values, start_node_ignore_values); // tohle a další užití této funkce nejsou deep copy, ale nemůže to vadit
       copy.end_node_ignore_values = new DefaultListModel();
       copyDefaultListModel(copy.end_node_ignore_values, end_node_ignore_values);
       copy.general_color = general_color; // tohle není deep copy, ale nemůže to vadit
       copy.general_shape = general_shape;
       copy.value_dependent_attr_name = new String(value_dependent_attr_name);
       copy.value_dependent_values = new DefaultListModel();
       copyDefaultListModel(copy.value_dependent_values, value_dependent_values);
       copy.value_dependent_colors = new DefaultListModel();
       copyDefaultListModel(copy.value_dependent_colors, value_dependent_colors);
       copy.value_dependent_shapes = new DefaultListModel();
       copyDefaultListModel(copy.value_dependent_shapes, value_dependent_shapes);
       return copy;
   } // getClone

   private void copyDefaultListModel(DefaultListModel dst, DefaultListModel src) {
       int size = src.size();
       for (int i=0; i<size; i++) {
           dst.addElement(src.getElementAt(i));
       }
   }

   public String toString() {
       StringBuffer dst = new StringBuffer();
       dst.append(LABEL_NAME + EQUAL_MARK + coreference_name + ATTRS_DIVIDER + " ");
       dst.append(LABEL_DISPLAY_MODE + EQUAL_MARK + display_mode + ATTRS_DIVIDER + " ");
       dst.append(LABEL_START_ATTR + EQUAL_MARK + start_node_attr_name + ATTRS_DIVIDER + " ");
       dst.append(LABEL_END_ATTR + EQUAL_MARK + end_node_attr_name + ATTRS_DIVIDER + " ");
       dst.append(defaultListToString(LABEL_START_IGNORE, start_node_ignore_values));
       dst.append(defaultListToString(LABEL_END_IGNORE, end_node_ignore_values));
       String pom = start_node_nil_arrow?"true":"false";
       dst.append(LABEL_START_NIL_ARROW + EQUAL_MARK + pom + ATTRS_DIVIDER + " ");
       pom = end_node_nil_arrow?"true":"false";
       dst.append(LABEL_END_NIL_ARROW + EQUAL_MARK + pom + ATTRS_DIVIDER + " ");
       dst.append(LABEL_GENERAL_COLOR + EQUAL_MARK + colorToString(general_color) + ATTRS_DIVIDER + " ");
       dst.append(LABEL_GENERAL_SHAPE + EQUAL_MARK + general_shape + ATTRS_DIVIDER + " ");
       if (value_dependent_attr_name.length() > 0) {
           dst.append(LABEL_VALUE_DEPEND_ATTR + EQUAL_MARK + value_dependent_attr_name + ATTRS_DIVIDER + " ");
       }
       dst.append(valueDependentPatternsToString());
       return dst.toString();
   }

   private String defaultListToString(String label, DefaultListModel list) {
       StringBuffer dst = new StringBuffer();
       int size = list.size();
       for (int i=0; i<size; i++) {
           dst.append(label + EQUAL_MARK + list.getElementAt(i) + ATTRS_DIVIDER + " ");
       }
       return dst.toString();
   }

   private String valueDependentPatternsToString() {
       StringBuffer dst = new StringBuffer();
       int size = value_dependent_values.size();
       for (int i=0; i<size; i++) {
           dst.append(LABEL_VALUE_DEPEND_PATTERN + EQUAL_MARK + value_dependent_values.getElementAt(i) + PATTERN_DIVIDER + colorToString((Color)value_dependent_colors.getElementAt(i)) + PATTERN_DIVIDER + ((Integer)value_dependent_shapes.getElementAt(i)).intValue());
           if (i != size-1) dst.append(ATTRS_DIVIDER + " ");
       }
       return dst.toString();
   }

   private String colorToString(Color color) {
       int r = color.getRed();
       int g = color.getGreen();
       int b = color.getBlue();
       int rgb = r * 65536 + g * 256 + b;
       return "#" + Integer.toHexString(rgb);
   }


   // properties set/get functions

    public void setName(String name) {
        coreference_name = name;
    }

    public String getName() {
        return coreference_name;
    }

    public void setDisplayMode(String display) {
        display_mode = display;
        setDisplay(display_mode);
    }

    public String getDisplayMode() {
        return display_mode;
    }

    private void setDisplay(String display_mode) { // it sets the current state of displaying the pattern based on the display_mode
        if (display_mode.compareTo(VALUE_NEVER)==0 || display_mode.compareTo(VALUE_USER_CONTROLLED_DEFAULT_NO)==0) {
            setDisplay(false);
        }
        else {
            setDisplay(true);
        }
    }

    public boolean setDisplay(boolean display) {
        if (isEditable()) {
            this.display = display;
        }
        return this.display;
    }

    public boolean getDisplay() {
        return display;
    }

    /**
     * Changes the state of displaying the pattern if editable
     * @return the resulting state
     */
    public boolean changeDisplay() {
        //System.out.print("\nold status:" + display);
        if (isEditable()) {
            display = display ? false : true;
        }
        //System.out.print("\nnew status:" + display);
        return display;
    }

    /**
     * Informs whether the coreference pattern allows changing its state
     * @return true if change is allowed; false otherwise
     */
    public boolean isEditable() {
        if (display_mode.compareTo(VALUE_NEVER)==0 || display_mode.compareTo(VALUE_ALWAYS)==0) {
            return false;
        }
        return true;
    }

   public void setStartNodeNilArrow(boolean display) {
       start_node_nil_arrow = display;
   }

   public boolean getStartNodeNilArrow() {
       return start_node_nil_arrow;
   }

   public void setEndNodeNilArrow(boolean display) {
       end_node_nil_arrow = display;
   }

   public boolean getEndNodeNilArrow() {
       return end_node_nil_arrow;
   }

   public void setStartAttrNodeName(String name) {
       start_node_attr_name = new String(name);
   }

   public String getStartAttrNodeName() {
       return start_node_attr_name;
   }

   public void setEndAttrNodeName(String name) {
       end_node_attr_name = new String(name);
   }

   public String getEndAttrNodeName() {
       return end_node_attr_name;
   }

   public void addStartNodeIgnoreValue(String value) {
       if (!start_node_ignore_values.contains(value)) {
           start_node_ignore_values.addElement(value);
       }
   }

   public void clearStartNodeIgnoreValues() {
       start_node_ignore_values.clear();
   }

   public DefaultListModel getStartNodeIgnoreValues() {
       return start_node_ignore_values;
   }

   public void addEndNodeIgnoreValue(String value) {
       if (!end_node_ignore_values.contains(value)) {
           end_node_ignore_values.addElement(value);
       }
   }

   public void clearEndNodeIgnoreValues() {
       end_node_ignore_values.clear();
   }

   public DefaultListModel getEndNodeIgnoreValues() {
       return end_node_ignore_values;
   }

   public void setGeneralColor(Color color) {
       general_color = color;
   }

   public Color getGeneralColor() {
       return general_color;
   }

   public void setGeneralShape(int shape) {
       general_shape = shape;
   }

   public int getGeneralShape() {
       return general_shape;
   }

   public void setValueDependentAttrName(String name) {
       value_dependent_attr_name = new String(name);
   }

   public String getValueDependentAttrName() {
       return value_dependent_attr_name;
   }

   public void addValueDependentRule(String value, Color color, int shape) {
       //debug("\nCoreferencePattern.addValueDependentRule: adding pattern: " + value + ", " + color + ", " + shape);
       if (!value_dependent_values.contains(value)) {
           //int pos = value_dependent_values.size();
           //value_dependent_values.setSize(pos+1);
           value_dependent_values.addElement(value);
           //value_dependent_colors.setSize(pos+1);
           value_dependent_colors.addElement(color);
           //value_dependent_shapes.setSize(pos+1);
           value_dependent_shapes.addElement(new Integer(shape));
       }
   }

   public DefaultListModel getValueDependentValues() {
       return value_dependent_values;
   }

   /**
    * Gives a color to be used with a <code>value</code> of the value dependent attribute.
    * If no such rule exists, the general color is given.
    * @param value String
    * @return Color
    */
   public Color getValueDependentColor(String value) {
       //debug("\nCoreferencePattern.getValueDependentColor: Getting value dependent color for value '" + value + "'");
       int pos = value_dependent_values.indexOf(value);
       //debug("\nCoreferencePattern.getValueDependentColor: position of the color is: " + pos);
       if (pos<0) {
           return general_color;
       }
       return (Color)value_dependent_colors.elementAt(pos);
   }

   /**
    * Gives a shape to be used with a <code>value</code> of the value dependent attribute.
    * If no such rule exists, the general shape is given.
    * @param value String
    * @return int
    */
   public int getValueDependentShape(String value) {
       //debug("\nCoreferencePattern.getValueDependentShape: Getting value dependent shape for value '" + value + "'");
       int pos = value_dependent_values.indexOf(value);
       //debug("\nCoreferencePattern.getValueDependentShape: position of the shape is: " + pos);
       if (pos<0) {
           return general_shape;
       }
       return ((Integer)value_dependent_shapes.elementAt(pos)).intValue();
   }

   public void clearValueDependentValues() {
       value_dependent_values = new DefaultListModel();
       value_dependent_colors = new DefaultListModel();
       value_dependent_shapes = new DefaultListModel();
   }

   /**
    * Reads the pattern from String.
    * @param line String
    * Example of the String: "start_attr=coref, end_attr=AID, start_ignore= , start_ignore=null, end_ignore= , end_ignore=null, start_nill_arrow=true, end_nill_arrow=true, general_color=#231812, general_shape=1, value_depend_attr=cortype, value_depend=grammatical;#202020;0, value_depend=textual;#402010;0"
    */
   public void readPatternFromString(String line) {
       initialize(); // clear all previous information
       if (line==null) return;
       String attribute, name, value;
       int position=0;
       while ((attribute=getAttribute(line,position))!=null) {
           position += attribute.length()+1; // skip also the ATTRS_DIVIDER
           name = getName(attribute).trim();
           value = getValue(attribute);
           //debug("\nCoreferencePattern.readPatternFromString: a name: '" + name + "' and a value: '" + value + "'");
           if (name.compareTo(LABEL_NAME)==0) {
               coreference_name = value;
           }
           if (name.compareTo(LABEL_DISPLAY_MODE)==0) {
               setDisplayMode(value);
           }
           if (name.compareTo(LABEL_START_ATTR)==0) {
               start_node_attr_name = value;
           }
           else if (name.compareTo(LABEL_END_ATTR) == 0) {
               end_node_attr_name = value;
           }
           else if (name.compareTo(LABEL_START_IGNORE) == 0) {
               addStartNodeIgnoreValue(value);
           }
           else if (name.compareTo(LABEL_END_IGNORE) == 0) {
               addEndNodeIgnoreValue(value);
           }
           else if (name.compareTo(LABEL_START_NIL_ARROW) == 0) {
               start_node_nil_arrow = value.equalsIgnoreCase("true") ? true : false;
           }
           else if (name.compareTo(LABEL_END_NIL_ARROW) == 0) {
               end_node_nil_arrow = value.equalsIgnoreCase("true") ? true : false;
           }
           else if (name.compareTo(LABEL_GENERAL_COLOR) == 0) {
               if (value.charAt(0)=='#') { // hexadecimal number
                   general_color = new Color(Integer.parseInt(value.substring(1), 16));
                   //int i_color = Integer.parseInt(value.substring(1), 16);
                   //debug("\nCoreferencePattern.readPatternFromString: a number:" + i_color);
               }
               else { // decimal number
                   general_color = new Color(Integer.parseInt(value, 10));
               }
           }
           else if (name.compareTo(LABEL_GENERAL_SHAPE) == 0) {
               setGeneralShape(Integer.parseInt(value));
           }
           else if (name.compareTo(LABEL_VALUE_DEPEND_ATTR) == 0) {
               value_dependent_attr_name = value;
           }
           else if (name.compareTo(LABEL_VALUE_DEPEND_PATTERN) == 0) {
               readValueDependentPattern(value);
           }
       }
   }

   /**
    * Searches for another attribute in a line from a position; returns the attribute - it should be "name=value"
    * @param line String
    * @param position int
    * @return String
    */
   private String getAttribute(String line, int position) {
       int length = line.length();
       if (position >= length) return null;
       int end=line.indexOf(ATTRS_DIVIDER,position);
       if (end == -1) end = length; // the last attribute
       String attribute = line.substring(position,end);
       return attribute;
   }

   /**
    * Gets the attribute name from a String "name=value"
    * @param attribute String
    * @return String
    */
   private String getName(String attribute) {
       int mark = attribute.indexOf(EQUAL_MARK);
       if (mark == -1) {
           debug("\nCoreferencePattern.getName: no EQUAL_MARK in the attribute!");
           return "";
       }
       return attribute.substring(0,mark);
   }

   /**
    * Gets the attribute value from a String "name=value"
    * @param attribute String
    * @return String
    */
   private String getValue(String attribute) {
       int mark = attribute.indexOf(EQUAL_MARK);
       if (mark == -1) {
           debug("\nCoreferencePattern.getValue: no EQUAL_MARK (" + EQUAL_MARK + ") in the attribute!");
           return "";
       }
       return attribute.substring(mark+1);
   }

   /**
    * Parses one value-dependent pattern (eg. "textual;402010;1")
    * @param pattern String
    */
   private void readValueDependentPattern(String pattern) {
       String value, color, shape;
       //debug("\nCoreferencePattern.readValueDependentPattern: parsing the pattern: " + pattern);
       try {
           int end = pattern.indexOf(PATTERN_DIVIDER);
           if (end == -1) {
               debug("\nCoreferencePattern.readValueDependentPattern: no PATTERN_DIVIDER (" + PATTERN_DIVIDER + ") in the pattern '" + pattern + "'!");
               return;
           }
           value = pattern.substring(0, end);
           end++; // skip the PATTERN_DIVIDER
           int end2 = pattern.indexOf(PATTERN_DIVIDER, end);
           if (end2 == -1) {
               debug("\nCoreferencePattern.readValueDependentPattern: no second PATTERN_DIVIDER (" + PATTERN_DIVIDER + ") in the pattern '" + pattern + "'!");
               return;
           }
           color = pattern.substring(end, end2);
           end2++; // skip the second PATTERN_DIVIDER
           if (end2 >= pattern.length()) {
               debug("\nCoreferencePattern.readValueDependentPattern: no shape in the pattern '" + pattern + "'!");
               return;
           }
           shape = pattern.substring(end2);
           //debug("\nCoreferencePattern.readValueDependentPattern: shape is: " + shape);
           int color_int = 0;
           int shape_int = 0;
           if (color.charAt(0) == '#') { // hexadecimal number
               color_int = Integer.parseInt(color.substring(1), 16);
           }
           else { // decimal number
               color_int = Integer.parseInt(color, 10);
           }
           shape_int = Integer.parseInt(shape, 10);
           Color color_Color = new Color(color_int);
           //debug("\nCoreferencePattern.readValueDependentPattern: adding a value dependent rule");
           addValueDependentRule(value, color_Color, shape_int);
       }
       catch (Exception e) {
           debug("\nCoreferencePattern.readValueDependentPattern: an exception '" + e + "' in the pattern '" + pattern + "'!");
       }
   }

}
