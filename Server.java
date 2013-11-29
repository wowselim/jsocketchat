import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {
  ServerSocket server;
  
  final List<Socket> clients = new CopyOnWriteArrayList<>();
  final List<Thread> listeners = new CopyOnWriteArrayList<>();
  
  public Server() {
    try {
      server = new ServerSocket(1337);
      System.out.println("Server running on port " + server.getLocalPort() + ".");
      start();
    } catch(IOException e) {
      e.printStackTrace(System.out);
    }                                                    
  }
  
  public void run() {
    Runnable listener = new Runnable() {
      public void run() {
        while (true) { 
          try {
            clients.add(server.accept());
            System.out.println("New client connected");
            System.out.println("Number of clients: " + clients.size());
            attachMessageListener(clients.get(clients.size() - 1));
          } catch(IOException e) {
            continue;
          }
        }
      }
    };
    Thread task = new Thread(listener);
    task.setName("Client Listener");
    listeners.add(task);
    listeners.get(listeners.size() - 1).start();
  }
  
  public void attachMessageListener(Socket client) {
    final Socket clientIO = client;
    Runnable msgListener = new Runnable() {
      public void run() {
        DataInputStream in = null;
        DataOutputStream out = null;
        while (true) {
          try {
            in = new DataInputStream(clientIO.getInputStream());
            out = new DataOutputStream(clientIO.getOutputStream());
            String message = in.readUTF();
            broadcastMessage("Client " + (clients.indexOf(clientIO) + 1) + ": " + message);
          } catch(IOException e) {
            try {
              clientIO.close();
            } catch(IOException ex) {
              
            }
            listeners.set(clients.indexOf(clientIO), null);
            listeners.set(clients.indexOf(clientIO) + 1 ,null);
            System.out.println("Client has disconnected.");
            clients.set(clients.indexOf(clientIO), null);
            break;
          }
        }
      }
    };
    Thread task = new Thread(msgListener);
    task.setName("Message Listener");
    listeners.add(task);
    listeners.get(listeners.size() - 1).start();
  }
  
  public void broadcastMessage(String msg) {
    for (Socket client : clients) {
      try {
        DataInputStream in = new DataInputStream(client.getInputStream());
        DataOutputStream out = new DataOutputStream(client.getOutputStream());
        out.writeUTF(msg);
      } catch(Exception e) {
        continue;
      }
    }
  }
  
  public static void main(String[] args) {
    new Server();
  }
}