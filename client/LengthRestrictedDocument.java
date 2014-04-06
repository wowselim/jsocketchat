import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class LengthRestrictedDocument extends PlainDocument {
    
  private final int limit;
  private JTextField textfield;
  
  public LengthRestrictedDocument(int limit, JTextField textfield) {
    this.limit = limit;
    this.textfield = textfield;
  }
  
  public void insertString(int offs, String str, AttributeSet a)
  throws BadLocationException {
    if (str == null)
    return;
    
    if ((getLength() + str.length()) <= limit) {
      super.insertString(offs, str, a);
    }
  }
}