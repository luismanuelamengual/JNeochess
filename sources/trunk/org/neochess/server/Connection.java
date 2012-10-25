
package org.neochess.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.json.simple.JSONObject;
import org.neochess.server.ClientConnection.ClientConnectionListener;

public class Connection implements ClientConnectionListener
{
    protected EventListenerList listeners = new EventListenerList();
    public static final int PORT = 9000;
    private ServerSocket serverSocket;
    private List<ClientConnection> clients;
    private boolean active;
    private boolean logging;
    private Thread connectionThread;
    
    public Connection ()
    {
        active = false;
        logging = false;        
        clients = new ArrayList<ClientConnection>();
    }

    public boolean isLogging ()
    {
        return logging;
    }

    public void setLogging (boolean logging)
    {
        this.logging = logging;
    }
    
    public void start ()
    {
        startConnectionThread ();
    }
    
    public void stop ()
    {
        stopConnectionThread ();
    }
    
    private void startConnectionThread ()
    {
        connectionThread = new Thread()
        {
            @Override
            public void run()
            {
                active = true;
                Application.getInstance().getLogger().info("Server started !!");
                while (active)
                {
                    try 
                    {
                        serverSocket = new ServerSocket(PORT);
                        while (active)
                        {
                            Socket clientSocket = serverSocket.accept();
                            ClientConnection clientHandler = new ClientConnection(clientSocket);
                            clientHandler.addClientConnectionListener(Connection.this);
                            clientHandler.start();
                        }
                    } 
                    catch (Exception e) 
                    {
                        if (active)
                            try { Thread.sleep(10000); } catch (Exception ex) {}
                    }
                }
                Application.getInstance().getLogger().info("Server stopped !!");
            }
        };
        connectionThread.start();
    }
    
    private void stopConnectionThread ()
    {
        active = false;
        if (serverSocket != null)
        {
            try { serverSocket.close(); } catch (Exception ex) {}
            serverSocket = null;
        }
        if (connectionThread != null)
            try { connectionThread.join(); } catch (Exception ex) {}
    }
    
    public void sendToClient (int clientId, JSONObject json)
    {
        ClientConnection client = getClientConnection(clientId);
        if (client != null)
            sendToClient(client, json);
    }
    
    public void sendToClient (ClientConnection client, JSONObject json)
    {
        if (clients.indexOf(client) >= 0)
            client.sendData(json);
    }
    
    public ClientConnection getClientConnection (int clientId)
    {
        ClientConnection resultClient = null;
        for (ClientConnection client : clients)
        {
            if (client.getClientId() == clientId)
            {
                resultClient = client;
                break;
            }
        }
        return resultClient;
    }

    @Override
    public void onClientStarted (ClientConnection client)
    {
        clients.add(client);
        
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientStarted(client);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing client Started. Ex: " + exception.getMessage());
            }
        }
        
        if (Application.getInstance().getProperties().get("connectionLogging").equals("on"))
            Application.getInstance().getLogger().info("Client [" + client + "] connected !!");
    }

    @Override
    public void onClientEnded (ClientConnection client)
    {
        clients.remove(client);
        
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientEnded(client);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing client Ended. Ex: " + exception.getMessage());
            }
        }
        
        if (Application.getInstance().getProperties().get("connectionLogging").equals("on"))
            Application.getInstance().getLogger().info("Client [" + client + "] disconnected !!");
    }

    @Override
    public void onClientDataReceeived (ClientConnection client, JSONObject json)
    {
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientDataReceeived(client, json);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing package: " + json.toJSONString() + " Ex: " + exception.getMessage());
            }
        }
        
        if (Application.getInstance().getProperties().get("connectionLogging").equals("on"))
            Application.getInstance().getLogger().info("Received from client [" + client + "]: " + json.toJSONString());
    }

    @Override
    public void onClientDataSent (ClientConnection client, JSONObject json)
    {
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientDataSent(client, json);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing sent package: " + json.toJSONString() + " Ex: " + exception.getMessage());
            }
        }
        
        if (Application.getInstance().getProperties().get("connectionLogging").equals("on"))
            Application.getInstance().getLogger().info("Sent to client [" + client + "]: " + json.toJSONString());
    }
    
    public void addConnectionListener(ClientConnectionListener listener)
    {
        listeners.add(ClientConnectionListener.class, listener);
    }

    public void removeConnectionListener(ClientConnectionListener listener)
    {
        listeners.remove(ClientConnectionListener.class, listener);
    }
}
