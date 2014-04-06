import java.io.*;
import java.net.*;
import java.util.*;

class Server extends Thread {
  private ServerSocket server;
  // TODO: use map instead of two lists
  private List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
  private List<String> usernames = Collections.synchronizedList(new ArrayList<>());

  public Server(final int port) {
    try {
      server = new ServerSocket(port);
    } catch(IOException e) {
      System.out.print("Close other servers before running.");
      System.exit(1);
    }
    System.out.println("Server running on port " + port + ".");
    start();
  }

  public void forwardMessages(Socket client) {
    clients.add(client);

    Runnable listener = new Runnable() {
      String name;
      boolean alive = true;
      DataInputStream clientIn;
      DataOutputStream clientOut;
      String msg;

      public void run() {
        final Socket c = clients.get(clients.size() - 1);
        try {
          clientIn = new DataInputStream(c.getInputStream());
          clientOut = new DataOutputStream(c.getOutputStream());
        } catch(IOException e) {
          e.printStackTrace(System.out);
        }
        while (alive) {
          try {
            if(name == null)
              clientOut.writeUTF("Server: Please enter a username.");
            msg = clientIn.readUTF();
            if(msg.length() > 0 && msg.length() < 40 && name == null && !usernames.contains(msg.split(" ")[0])) {
              name = msg.split(" ")[0];
              usernames.add(name);
              clientOut.writeUTF("Server: Welcome " + name);
              continue;
            }
          } catch(Exception e) {
            alive = false;
            System.out.println("Client " + name + " with IP " + c.getInetAddress().getHostAddress() + " disconnected.");
            continue;
          }
          for(Socket recipient : clients) {
            try {
              DataOutputStream recipientOut = new DataOutputStream(recipient.getOutputStream());
              recipientOut.writeUTF((name == null ? ("Client " + (clients.indexOf(c) + 1)) : name) + ": " + msg);
            } catch(IOException e) {
              continue;
            }
          }
        }
      }
    };
    Thread listenerThread = new Thread(listener, "messagelistener to " + client.getInetAddress().getHostAddress());
    System.out.println("Attached " + listenerThread.getName());
    listenerThread.start();
  }

  public void run() {
    while (true) {
      try {
        Socket client = server.accept();
        System.out.println("New client connected " + client.getInetAddress().getHostAddress());
        forwardMessages(client);
      } catch(IOException e) {
        System.out.println("Failed connection to client.");
      }
    }
  }

  public static void main(String[] args) {
    new Server(1337);
  }
}