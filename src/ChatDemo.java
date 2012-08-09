import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.*;

public class ChatDemo {

  private class IOCallbackHandler implements IOCallback {
    private boolean isConnected = false;
    
    @Override
    public void onMessage(JSONObject json, IOAcknowledge ack) {
        try {
            System.out.println("\nServer said:" + json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String data, IOAcknowledge ack) {
        System.out.println("\nServer said: " + data);
    }

    @Override
    public void onError(SocketIOException socketIOException) {
        System.err.println("\nAn Error occured");
        socketIOException.printStackTrace();
    }

    @Override
    public void onDisconnect() {        
        System.out.println("\nConnection terminated.");
        isConnected = false;
    }

    @Override
    public void onConnect() {
        //System.out.println("\nConnection established ok");
        isConnected = true;
    }

    @Override
    public void on(String event, IOAcknowledge ack, Object... args) {        
        try {
          if ("registered".equals(event)){            
            JSONArray messageArray = new JSONArray(args);
            JSONObject messageObj = messageArray.getJSONObject(0);
            System.out.println(messageObj.getString("message"));          
          }
          else if ("userlist".equals(event)){
          System.out.println("User list has changed: ");  
            JSONArray usersArray = new JSONArray(args);
            JSONObject usersObj = usersArray.getJSONObject(0);
            JSONArray users = usersObj.getJSONArray("users");            
            for (int i=0; i< users.length(); i++) {
              System.out.println(users.get(i));
            }
        }else if ("message".equals(event)){          
          JSONArray messageArray = new JSONArray(args);
          JSONObject messageObj = messageArray.getJSONObject(0);
          System.out.println("New message: " + messageObj.getString("message"));          
        }
        } catch (JSONException e) {           
          e.printStackTrace();
        }
        System.out.print("chat > ");
    }
    
    public boolean isConnected() {
      return isConnected;
    }
  };
  
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: + ChatDemo <socketio server>");
      System.exit(1);
    }
    
    System.out.println("FeedHenry websocket chat java demo, connecting..");

    try {
      String server = args[0];
      SocketIO socket = new SocketIO(server);

      // Set all Logging to Warning level
      Enumeration<String> logs = LogManager.getLogManager().getLoggerNames();
      while (logs.hasMoreElements()) {
        String log = logs.nextElement();
        LogManager.getLogManager().getLogger(log).setLevel(Level.WARNING);
      }

      ChatDemo cd = new ChatDemo();
      IOCallbackHandler ioHandler = cd.new IOCallbackHandler();
      socket.connect(ioHandler);

      // Wait until we're connected
      while (ioHandler.isConnected() == false) {
        Thread.sleep(500);
      }

      // Now that we're connected, read input from stdin and send to websocket server
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        while (str != null) {
          System.out.print("chat > ");
          str = in.readLine().trim();
          if (!"".equals(str)) {
            JSONObject message = new JSONObject();
            message.put("message", str);
            socket.send(message);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
