
package org.neochess.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neochess.general.Disposable;

public class ClientConnection extends Thread implements Disposable
{
    protected EventListenerList listeners = new EventListenerList();
    private int clientId;
    private String clientAddress;
    private JSONParser parser;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;

    public ClientConnection (Socket socket)
    {
        this.socket = socket;
        this.parser = new JSONParser();
        this.clientId = -1;
        this.clientAddress = this.socket.getInetAddress().getHostAddress();
    }
    
    @Override
    public void dispose()
    {
        closeConnection();
        while (listeners.getListenerCount() > 0)
            removeClientConnectionListener(listeners.getListeners(ClientConnectionListener.class)[0]);
        listeners = null;
        parser.reset();
        parser = null;
    }
    
    @Override
    public void run ()
    {   
        boolean handlerOpen = false;
        try
        {            
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
            handlerOpen = true;
            fireClientStartedEvent();
        }
        catch (Exception ex1)
        {
            Application.getInstance().getLogger().warning("Error starting client. Ex: " + ex1.getMessage());
        }
        
        if (handlerOpen)
        {
            try
            {   
                String dataReceived;
                while ((dataReceived = input.readUTF()) != null) 
                    processDataReceived(dataReceived);
            }
            catch (Exception ex)
            {
            }
            finally
            {
                closeConnection();
                fireClientEndedEvent();
            }
        }
        
        dispose();
    }
    
    public synchronized void closeConnection ()
    {
        if (output != null)
        {
            try { output.close(); } catch (Exception ex) {}
            output = null;
        }
        if (input != null)
        {
            try { input.close(); } catch (Exception ex) {}
            input = null;
        }
        if (socket != null)
        {
            try { socket.close(); } catch (Exception ex) {}
            socket = null;
        }
    }
    
    public synchronized void sendData (JSONObject json)
    {
        if (output != null)
        {
            String dataToSend = json.toJSONString();
            try
            {
                output.writeUTF(dataToSend);
                fireClientDataSentEvent(json);
            }
            catch (Exception exception)
            {
                closeConnection();
            }
        }
    }
    
    private synchronized void processDataReceived (String dataReceived)
    {
        JSONObject json = null;
        try
        {
            json = (JSONObject)parser.parse(dataReceived);   
            fireClientDataReceivedEvent(json);
        }
        catch (ParseException ex) 
        {
            Application.getInstance().getLogger().warning("Error parsing package: " + dataReceived);
        }        
    }

    public int getClientId ()
    {
        return clientId;
    }

    public void setClientId (int userId)
    {
        this.clientId = userId;
    }
    
    public String getClientAddress ()
    {
        return clientAddress;
    }
    
    public void addClientConnectionListener(ClientConnectionListener listener)
    {
        listeners.add(ClientConnectionListener.class, listener);
    }

    public void removeClientConnectionListener(ClientConnectionListener listener)
    {
        listeners.remove(ClientConnectionListener.class, listener);
    }

    private void fireClientStartedEvent ()
    {
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientStarted(this);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing ClientHandler Start. Ex: " + exception.getMessage());
            }
        }
    }

    private void fireClientEndedEvent ()
    {
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientEnded(this);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing ClientHandler End. Ex: " + exception.getMessage());
            }
        }
    }

    private void fireClientDataReceivedEvent (JSONObject json)
    {
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientDataReceeived(this, json);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing package: " + json.toJSONString() + " Ex: " + exception.getMessage());
            }
        }
    }
    
    private void fireClientDataSentEvent (JSONObject json)
    {
        for (ClientConnectionListener listener : listeners.getListeners(ClientConnectionListener.class))
        {
            try
            {
                listener.onClientDataSent(this, json);
            }
            catch (Exception exception)
            {
                Application.getInstance().getLogger().warning("Error processing sent package: " + json.toJSONString() + " Ex: " + exception.getMessage());
            }
        }
    }

    @Override
    public String toString ()
    {
        String clientString = "";
        clientString += (clientId > 0)? clientId : "?";
        clientString += "@";
        clientString += clientAddress;
        return clientString;
    }

    public interface ClientConnectionListener extends EventListener
    {
        public void onClientStarted (ClientConnection client);
        public void onClientEnded (ClientConnection client);
        public void onClientDataReceeived (ClientConnection client, JSONObject json);
        public void onClientDataSent (ClientConnection client, JSONObject json);
    }
}
