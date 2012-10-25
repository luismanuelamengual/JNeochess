
package org.neochess.server;

import java.util.EventListener;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.neochess.util.ConsoleUtils;

public class Console
{
    protected EventListenerList listeners = new EventListenerList();
    private boolean running = false;
    
    public void start ()
    {
        new Thread ()
        {
            @Override
            public void run ()
            {
                running = true;
                while (running)
                {
                    String command = System.console().readLine("#");
                    command = command.trim();
                    if (command.length() > 0)
                    {
                        try
                        {
                            processCommand (command);
                        }
                        catch (Exception ex)
                        {
                            Application.getInstance().getLogger().warning("Error processing console command: " + command);
                        }
                    }
                }
            }
        }.start();
    }
    
    public void stop ()
    {
        running = false;
    }
    
    public void processCommand (String command)
    {
        List<String> commandTokens = ConsoleUtils.parseCommand(command);
        fireConsoleEvent (command, commandTokens);
    }
    
    public void addConsoleListener(ConsoleListener listener)
    {
        listeners.add(ConsoleListener.class, listener);
    }

    public void removeConsoleListener(ConsoleListener listener)
    {
        listeners.remove(ConsoleListener.class, listener);
    }

    public void fireConsoleEvent (String command, List<String> commandTokens)
    {
        for (ConsoleListener listener : listeners.getListeners(ConsoleListener.class))
            listener.onCommandEntered(command, commandTokens);
    }
    
    public interface ConsoleListener extends EventListener
    {
        public void onCommandEntered (String command, List<String> commandTokens);
    }
}
