
package org.neochess.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.EventListener;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Connection
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
    
    public Connection ()
    {
        parser = new JSONParser();
        open = false;
    }
    
    public synchronized void open ()
    {
        if (!this.open)
        {
            this.open = true;
            startConnectionThread ();
        }
    }
    
    public synchronized void close ()
    {
        if (this.open)
        {
            this.open = false;
            closeConnection ();
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
        
    private synchronized void startConnectionThread ()
    {
        new Thread()
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
        }.start();
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
    
    public synchronized void sendData (JSONObject json)
    {
        if (output != null)
        {
            String dataToSend = json.toJSONString();
            try
            {
                output.writeUTF(dataToSend);
                fireDataSentEvent(json);
            }
            catch (Exception exception)
            {
                closeConnection();
            }
        }
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
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run ()
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
        });
    }
    
    public void fireConnectionEndedEvent ()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run ()
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
        });
    }
    
    public void fireDataReceivedEvent (final JSONObject json)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run ()
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
        });
    }
    
    public void fireDataSentEvent (final JSONObject json)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run ()
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
        });
    }
    
    public interface ConnectionListener extends EventListener
    {
        public void onConnectionStarted ();
        public void onConnectionEnded ();
        public void onDataReceived (JSONObject json);
        public void onDataSent (JSONObject json);
    }
}
