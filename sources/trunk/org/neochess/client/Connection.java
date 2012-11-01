
package org.neochess.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.neochess.general.Disposable;

public class Connection implements Disposable
{
    protected EventListenerList listeners = new EventListenerList();
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9000;
    
    private JSONParser parser;
    private boolean open;
    private boolean connected;
    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private Thread connectionThread;
    
    public Connection ()
    {
        parser = new JSONParser();
        open = false;
    }
    
    public void dispose ()
    {
        close ();
        while (listeners.getListenerCount() > 0)
            removeConnectionListener(listeners.getListeners(ConnectionListener.class)[0]);
        listeners = null;
        parser = null;
    }
    
    public void open ()
    {
        if (!this.open)
        {
            this.open = true;
            startConnectionThread ();
        }
    }
    
    public void close ()
    {
        if (this.open)
        {
            this.open = false;
            stopConnectionThread ();
        }
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
        
    private void startConnectionThread ()
    {
        connectionThread = new Thread()
        {
            @Override
            public void run()
            {
                while (open)
                {
                    connected = false;
                    try
                    {      
                        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                        output = new DataOutputStream(socket.getOutputStream());
                        input = new DataInputStream(socket.getInputStream());
                        fireConnectionStartedEvent();
                        connected = true;
                        while (open)
                        {
                            String dataReceived;
                            while ((dataReceived = input.readUTF()) != null) 
                                processDataReceived(dataReceived);
                        }
                    }
                    catch (Exception ex1) {}
                    closeConnection();
                    if (connected)
                    {
                        connected = false;
                        fireConnectionEndedEvent();
                    }
                    if (open)
                        try { Thread.sleep(5000); } catch (Exception connectionException) {}
                }
            }
        };
        connectionThread.start();
    }
    
    private void stopConnectionThread ()
    {
        closeConnection ();
        if (connectionThread != null)
        {
            try { connectionThread.join(); } catch (InterruptedException ex){}
            connectionThread = null;
        }
    }
    
    public boolean isConnected ()
    {
        return connected;
    }
    
    public Socket getSocket ()
    {
        return socket;
    }
    
    private synchronized void processDataReceived (String dataReceived)
    {
        JSONObject json = null;
        try
        {
            json = (JSONObject)parser.parse(dataReceived);   
            fireDataReceivedEvent(json);
        }
        catch (ParseException ex) 
        {
            System.err.println ("Error de parseo en dato entrante: " + dataReceived);
        } 
    }
    
    public synchronized boolean sendData (JSONObject json) throws IOException
    {
        boolean dataSent = false;
        if (output != null)
        {
            String dataToSend = json.toJSONString();
            try
            {
                output.writeUTF(dataToSend);
                fireDataSentEvent(json);
                dataSent = true;
            }
            catch (IOException exception)
            {
                closeConnection();
                throw exception;
            }
        }
        return dataSent;
    }
    
    public JSONObject sendDataAndWaitForResponse (final JSONObject json) throws IOException
    {
        return sendDataAndWaitForResponse(json, 10000);
    }
    
    public JSONObject sendDataAndWaitForResponse (final JSONObject json, int waitMilliseconds) throws IOException
    {
        final JSONObject responseJson = new JSONObject();
        ConnectionListener listener = new ConnectionListener()
        {
            @Override public void onConnectionStarted (){}
            @Override public void onConnectionEnded (){}
            @Override public void onDataSent (JSONObject json){}
            @Override
            public void onDataReceived (JSONObject json)
            {
                if (json.get("cmd").equals("response"))
                {
                    JSONObject paramsObject = (JSONObject)json.get("params");
                    if (paramsObject.get("cmd").equals(paramsObject.get("cmd")))
                    {
                        responseJson.put("cmd", json.get("cmd"));
                        responseJson.put("params", json.get("params"));
                        synchronized (Connection.this) { Connection.this.notify(); }
                    }
                }
            }
        };
        this.addConnectionListener(listener);
        try
        {
            if (sendData(json))
                try { synchronized (this) {this.wait(10000);} } catch (Exception ex) {}
        }
        catch (IOException ex)
        {
            throw ex;
        }
        finally
        {
            this.removeConnectionListener(listener);
        }
        return responseJson.get("cmd") != null? responseJson : null;
    }
    
    public void addConnectionListener(ConnectionListener listener)
    {
        listeners.add(ConnectionListener.class, listener);
    }

    public void removeConnectionListener(ConnectionListener listener)
    {
        listeners.remove(ConnectionListener.class, listener);
    }

    public void fireConnectionStartedEvent ()
    {
        for (ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
        {
            try
            {
                listener.onConnectionStarted();
            }
            catch (Exception exception)
            {
                System.err.append("Error in processing ConnectioStartedEvent. Ex: " + exception.getMessage());
            }
        }
    }
    
    public void fireConnectionEndedEvent ()
    {
        for (ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
        {
            try
            {
                listener.onConnectionEnded();
            }
            catch (Exception exception)
            {
                System.err.append("Error in processing ConnectioEndedEvent. Ex: " + exception.getMessage());
            }
        }
    }
    
    public void fireDataReceivedEvent (final JSONObject json)
    {
        for (ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
        {
            try
            {
                listener.onDataReceived(json);
            }
            catch (Exception exception)
            {
                System.err.append("Error receiving data: " + json.toJSONString() + " Ex: " + exception.getMessage());
            }
        }       
    }
    
    public void fireDataSentEvent (final JSONObject json)
    {
        for (ConnectionListener listener : listeners.getListeners(ConnectionListener.class))
        {
            try
            {
                listener.onDataSent(json);
            }
            catch (Exception exception)
            {
                System.err.append("Error sending data: " + json.toJSONString() + " Ex: " + exception.getMessage());
            }
        }
    }
    
    public interface ConnectionListener extends EventListener
    {
        public void onConnectionStarted ();
        public void onConnectionEnded ();
        public void onDataReceived (JSONObject json);
        public void onDataSent (JSONObject json);
    }
}
