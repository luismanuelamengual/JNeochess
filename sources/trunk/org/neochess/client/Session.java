
package org.neochess.client;

import org.neochess.engine.User;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

public class Session
{
    protected EventListenerList listeners = new EventListenerList();
    private static boolean started = false;
    private static User user;
    
    public boolean startSession (User user)
    {
        if (!this.started)
        {
            this.started = true;
            this.user = user;
            fireSessionStartedEvent ();
        }
        return this.started;
    }
    
    public boolean destroySession ()
    {
        if (this.started)
        {
            fireSessionBeforeDestroyedEvent ();
            if (this.user != null)
            {
                this.user.dispose();
                this.user = null;
            }
            this.started = false;
            fireSessionDestroyedEvent ();
        }
        return this.started;
    }
    
    public boolean isStarted ()
    {
        return started;
    }
    
    public static User getUser()
    {
        return user;
    }
    
    public void addSessionListener(SessionListener listener)
    {
        listeners.add(SessionListener.class, listener);
    }

    public void removeSessionListener(SessionListener listener)
    {
        listeners.remove(SessionListener.class, listener);
    }

    private void fireSessionStartedEvent ()
    {
        for (SessionListener listener : listeners.getListeners(SessionListener.class))
            listener.onSessionStarted(this);
    }
    
    private void fireSessionBeforeDestroyedEvent ()
    {
        for (SessionListener listener : listeners.getListeners(SessionListener.class))
            listener.onSessionBeforeDestroyed(this);
    }
    
    private void fireSessionDestroyedEvent ()
    {
        for (SessionListener listener : listeners.getListeners(SessionListener.class))
            listener.onSessionDestroyed(this);
    }
    
    public interface SessionListener extends EventListener
    {
        public void onSessionStarted (Session session);
        public void onSessionBeforeDestroyed (Session session);
        public void onSessionDestroyed (Session session);
    }
}
