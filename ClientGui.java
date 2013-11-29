import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.net.Socket;
import java.io.*;

public class ClientGui extends JFrame {
  private JTextArea jTextArea1 = new JTextArea("");
  private JScrollPane jTextArea1ScrollPane = new JScrollPane(jTextArea1);
  private JButton jButton1 = new JButton();
  private JTextField jTextField1 = new JTextField();
  
  Socket clientSocket = null;
  DataOutputStream out = null;
  DataInputStream in = null;
  
  public ClientGui(String title) { 
    super(title);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) { System.exit(0); }
    });
    int frameWidth = 343; 
    int frameHeight = 250;
    setSize(frameWidth, frameHeight);
    setLocationRelativeTo(null);
    setResizable(false);
    Panel cp = new Panel(null);
    add(cp);
    
    jTextArea1ScrollPane.setBounds(8, 8, 321, 177);
    DefaultCaret caret = (DefaultCaret)jTextArea1.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    jTextArea1.setEditable(false);
    jTextArea1.setLineWrap(true);
    cp.add(jTextArea1ScrollPane);
    jButton1.setBounds(272, 192, 57, 25);
    jButton1.setText("Send");
    jButton1.setMargin(new Insets(2, 2, 2, 2));
    jButton1.addActionListener(new ActionListener() { 
      public void actionPerformed(ActionEvent evt) { 
        jButton1_ActionPerformed(evt);
      }
    });
    cp.add(jButton1);
    jTextField1.setBounds(8, 192, 257, 25);
    jTextField1.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent e) {
        
      }
      
      public void keyReleased(KeyEvent e) {
        
      }
      
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          try {
            out.writeUTF(jTextField1.getText());
          } catch(Exception ex) {
            
          }
          jTextField1.setText("");
        }
      }
    });
    jTextField1.setDocument(new LengthRestrictedDocument(140));
    cp.add(jTextField1);
    setTitle("Chat");
    
    setVisible(true);
  }
  
  public void jButton1_ActionPerformed(ActionEvent evt) {
    try {
      out.writeUTF(jTextField1.getText());
    } catch(Exception e) {
      
    }
    jTextField1.setText("");
  }
  
  public void appendText(String text) {
    this.jTextArea1.append(text + "\n");
  }
  
  public static void main(String[] args) throws Exception {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
      
    }
    final ClientGui cg = new ClientGui("ClientGui");
    
    cg.clientSocket = new Socket("localhost", 1337);
    cg.out = new DataOutputStream(cg.clientSocket.getOutputStream());
    cg.in = new DataInputStream(cg.clientSocket.getInputStream());
    Runnable reader = new Runnable() {
      public void run() {
        while (true) { 
          try {
            cg.appendText(cg.in.readUTF());
          } catch(Exception e) {
          }
        }
      }
    };
    new Thread(reader).start();
  }
  
  
  public final class LengthRestrictedDocument extends PlainDocument {
    
    private final int limit;
    
    public LengthRestrictedDocument(int limit) {
      this.limit = limit;
    }
    
    @Override
    public void insertString(int offs, String str, AttributeSet a)
    throws BadLocationException {
      if (str == null)
      return;
      
      if ((getLength() + str.length()) <= limit) {
        super.insertString(offs, str, a);
      }
    }
  }
}
