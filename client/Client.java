import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import java.io.*;
import java.net.*;
import java.util.*;

class Client extends JFrame implements Runnable {
  private JPanel topPanel;
  private JPanel bottomPanel;
  private JTextArea txtMessages;
  private JScrollPane scrollMessages;
  private JTextField txtInput;
  private JButton btnSend;

  private Socket socket;

  private DataInputStream dataIn;
  private DataOutputStream dataOut;

  private boolean connected = true;

  public Client(String title, int port) {
    this(title, 360, 280, "localhost", port);
  }

  public Client(String title, String host, int port) {
    this(title, 360, 280, host, port);
  }
  
  public Client(String title, int width, int height, String host, int port) {
    super(title);
    prepareFrame(width, height);
    initComponents();
    txtMessages.append("Attempting to connect to " + host + ":" + port);
    try {
      socket = new Socket(host, port);
      dataIn = new DataInputStream(socket.getInputStream());
      dataOut = new DataOutputStream(socket.getOutputStream());
    } catch(Exception e) {
      txtMessages.append("\nConnection to server failed.");
      setDisabled(true);
      connected = false;
    }
    if(connected) {
      txtMessages.append("\nConnection established.");
      new Thread(this, "readerthread").start();
    }
  }
  
  private void prepareFrame(int width, int height) {
    setIconImage(Toolkit.getDefaultToolkit().getImage("res/icon.png"));
    setResizable(false);
    setSize(width, height);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
  
  private void initComponents() {
    setLayout(new BorderLayout());

    topPanel = new JPanel();
    bottomPanel = new JPanel();
    txtMessages = new JTextArea();
    scrollMessages = new JScrollPane(txtMessages);
    txtInput = new JTextField();
    btnSend = new JButton("Send");

    txtMessages.setEditable(false);
    txtMessages.setLineWrap(true);
    txtMessages.setFocusable(false);

    txtInput.setDocument(new LengthRestrictedDocument(140, txtInput));

    // enable autoscrolling for incoming messages
    ((DefaultCaret)txtMessages.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    txtInput.setPreferredSize(new Dimension(getWidth() - 80, 20));
    btnSend.setPreferredSize(new Dimension(60, 20));

    topPanel.setLayout(new BorderLayout());
    bottomPanel.setLayout(new BorderLayout());

    topPanel.add(scrollMessages, BorderLayout.CENTER);
    topPanel.setPreferredSize(new Dimension(630, getHeight() - 60));
    bottomPanel.add(txtInput, BorderLayout.WEST);
    bottomPanel.add(btnSend, BorderLayout.EAST);
    bottomPanel.setPreferredSize(new Dimension(630, 30));
    bottomPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    add(topPanel, BorderLayout.NORTH);
    add(bottomPanel, BorderLayout.SOUTH);
  } 

  public void run() {
    btnSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        sendMessage();
      }
    });

    txtInput.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent evt) {
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }
      }
    });

    try {
      while(connected) {
        txtMessages.append("\n" + dataIn.readUTF());
        new Thread(new AudioPlayer("res/facebook_notification.wav")).start();
      }
    } catch(IOException e) {
      setDisabled(true);
    }
  }

  private void setDisabled(boolean b) {
    txtInput.setEnabled(!b);
    btnSend.setEnabled(!b);
  }

  private void sendMessage() {
    String msg = txtInput.getText();
    if(msg.length() >= 0) {
      try {
        dataOut.writeUTF(txtInput.getText());
        txtInput.setText("");
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
      System.out.println("Error setting look and feel.");
    }

    final String host = JOptionPane.showInputDialog("Enter IP-address of server.");
    final int port = Integer.valueOf(JOptionPane.showInputDialog("Enter port."));

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        Client c = new Client("Chat", 640, 480, host, port);
        c.setVisible(true);
      }
    });
  }
}