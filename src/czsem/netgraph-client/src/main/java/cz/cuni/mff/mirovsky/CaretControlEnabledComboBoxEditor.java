package cz.cuni.mff.mirovsky;

import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * Class CaretControlEnabledComboboxEditor extends class BasicComboBoxEditor. It adds the possibility to read and control
 * the position of the caret. It is used for creating the textual form of the query.
 */
public class CaretControlEnabledComboBoxEditor extends BasicComboBoxEditor {

  public CaretControlEnabledComboBoxEditor() {
    super();
  }

  public int getCaretPosition() {
    return editor.getCaretPosition();
  }

  public void setCaretPosition(int pos) {
    editor.setCaretPosition(pos);
  }

}
