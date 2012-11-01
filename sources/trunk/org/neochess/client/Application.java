
package org.neochess.client;

import java.io.File;
import javax.swing.JFrame;
import org.neochess.client.ui.MainFrame;

public final class Application
{
    public static final String APPLICATION_NAME = "Neochess";
    public static final String APPLICATION_VERSION = "1.0";
    public static final String APPLICATION_VERSION_BUILD = "1";
    public static final String APPLICATION_VERSION_MODIFIER = "Beta";
    
    private static final Application instance = new Application ();
    private MainFrame mainFrame;
    private Session session;
    private Connection connection;
    
    private Application ()
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
    }
    
    public static Application getInstance()
    {
        return instance;
    }
    
    public static String getHomePath ()
    {
        return System.getProperty("user.home") + File.separatorChar + "Neochess";
    }
    
    public final String getTitle ()
    {
        return APPLICATION_NAME + " " + APPLICATION_VERSION + (APPLICATION_VERSION_MODIFIER.equals("")? "" : (" (" + APPLICATION_VERSION_MODIFIER + ")"));
    }

    public final String getShortTitle ()
    {
        return APPLICATION_NAME + " " + APPLICATION_VERSION;
    }
    
    public final String getResourcePath ()
    {
        return "/org/neochess/client/resources/";
    }

    public final String getResourceImagesPath ()
    {
        return getResourcePath () + "images/";
    }

    public final String getResourceSoundsPath ()
    {
        return getResourcePath () + "sounds/";
    }
    
    public final String getResourceGeneralPath ()
    {
        return getResourcePath () + "general/";
    }
    
    public void start ()
    {
        connection = new Connection();
        session = new Session();
        connection.open();
        mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
    
    public void destroy ()
    {
        if (!session.destroySession())
        {
            if (mainFrame.close())
            {
                mainFrame.dispose();
                mainFrame = null;
                if (connection != null)
                {
                    connection.dispose();
                    connection = null;
                }
                System.exit(0);
            }
        }
    }
    
    public MainFrame getMainFrame ()
    {
        return mainFrame;
    }
    
    public Session getSession ()
    {
        return session;
    }
    
    public Connection getConnection ()
    {
        return connection;
    }
}
