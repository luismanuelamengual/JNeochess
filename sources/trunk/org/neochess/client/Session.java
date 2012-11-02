
package org.neochess.client;

import java.io.IOException;
import org.neochess.engine.User;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import org.json.simple.JSONObject;
import org.neochess.general.Disposable;

public class Session implements Disposable
{
    protected EventListenerList listeners = new EventListenerList();
    private static User user;
    
    public void dispose()
    {
        while (listeners.getListenerCount() > 0)
            removeSessionListener(listeners.getListeners(SessionListener.class)[0]);
        listeners = null;
        destroySession ();
    }
    
    public boolean restartSession () throws Exception
    {
        if (isStarted())
            loginUser(user.getUserName(), user.getPassword());
        return isStarted();
    }
    
    public boolean startSession (String userName, String password) throws Exception
    {
        destroySession ();
        if (!isStarted())
        {
            loginUser(userName, password);
            fireSessionStartedEvent ();
        }
        return isStarted();
    }
    
    public boolean destroySession ()
    {
        if (isStarted())
        {
            fireSessionBeforeDestroyedEvent ();
            logoutUser();
            fireSessionDestroyedEvent ();
        }
        return isStarted();
    }
    
    public boolean isStarted ()
    {
        return user != null;
    }
    
    private void loginUser (String userName, String password) throws Exception
    {
        JSONObject jsonObject = new JSONObject();
        JSONObject paramsObject = new JSONObject();
        jsonObject.put("cmd", "loginUser");
        jsonObject.put("params", paramsObject);
        paramsObject.put("userName", userName);
        paramsObject.put("password", password);
        JSONObject responseObject = Application.getInstance().getConnection().sendDataAndWaitForResponse(jsonObject);
        if (responseObject != null)
        {
            JSONObject params = (JSONObject)responseObject.get("params");
            if (params.get("status").equals("success"))
            {
                JSONObject userObject = (JSONObject)params.get("user");
                User user = new User ();
                if (userObject.get("id") != null)
                    user.setId(((Long)userObject.get("id")).intValue());
                if (userObject.get("firstName") != null)
                    user.setFirstName(String.valueOf(userObject.get("firstName")));
                if (userObject.get("lastName") != null)
                    user.setLastName(String.valueOf(userObject.get("lastName")));
                if (userObject.get("userName") != null)
                    user.setUserName(String.valueOf(userObject.get("userName")));
                if (userObject.get("nickName") != null)
                    user.setNickName(String.valueOf(userObject.get("nickName")));
                if (userObject.get("password") != null)
                    user.setPassword(String.valueOf(userObject.get("password")));
                if (userObject.get("imageUrl") != null)
                    user.setImageUrl(String.valueOf(userObject.get("imageUrl")));
                if (userObject.get("elo") != null)
                    user.setElo(((Long)userObject.get("elo")).intValue());
                this.user = user;
            }
            else
            {
                throw new IOException(String.valueOf(params.get("errorMessage")));
            }
        }
        else
        {
            throw new IOException("Error de conexión con el servidor");
        }
    }
    
    private void logoutUser ()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cmd", "logoutUser");
        try { Application.getInstance().getConnection().sendData(jsonObject); } catch (Exception ex) {}
        if (this.user != null)
        {
            this.user.dispose();
            this.user = null;
        }
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
