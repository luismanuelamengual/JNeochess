
package org.neochess.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.neochess.server.handlers.GeneralHandler;
import org.neochess.server.handlers.Handler;
import org.neochess.server.handlers.UsersHandler;

public class Application
{
    private static final Application instance = new Application ();
    private Connection connection; 
    private Properties properties;
    private Database database;
    private Console console;
    private Logger logger;
    private List<Handler> handlers;
    
    private Application ()
    {
        logger = createLogger();
        properties = new Properties();
        database = new Database();
        connection = new Connection();
        console = new Console();
        handlers = new ArrayList<Handler>();
        handlers.add (new GeneralHandler());
        handlers.add (new UsersHandler());
    }
    
    public void destroy ()
    {
        stop();
        if (handlers != null)
        {
            for (Handler handler : handlers)
                try { handler.stop(); } catch (Exception ex) {}
            handlers.clear();
            handlers = null;
        }
        if (database != null)
        {
            try { database.close(); } catch (Exception ex) {}
            database = null;
        }
        if (console != null)
        {
            try { console.stop(); } catch (Exception ex) {}
            console = null;
        }
        if (connection != null)
        {
            try { connection.stop(); } catch (Exception ex) {}
            connection = null;
        }
        System.exit(0);
    }
    
    public void start ()
    {
        getLogger().info("Initializing Server ...");
        for (Handler handler : handlers)
            handler.start();
        console.start();
        connection.start();
        database.open();
        getLogger().info("Server initialized !!");
    }
    
    public void stop ()
    {
        getLogger().info("Finalizing Server ...");
        console.stop();
        database.close();
        connection.stop();
        for (Handler handler : handlers)
            handler.stop();
        getLogger().info("Server finalized !!");
    }
        
    private Logger createLogger ()
    {
        Logger logger = Logger.getLogger("org.neochess.server.Logger");
        try
        {    
            File dir = new File(getHomePath() + File.separatorChar + "log");
            dir.mkdir();
            FileHandler handler = new FileHandler(dir.getPath() + File.separatorChar + "serverLog.txt", 1024000, 1, true);
            handler.setFormatter(new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(handler);
        } 
        catch (Exception e){e.printStackTrace();}
        return logger;
    }
    
    public static Application getInstance ()
    {
        return instance;
    }
    
    public static String getHomePath ()
    {
        return System.getProperty("user.home") + File.separatorChar + "Neochess";
    }
    
    public Connection getConnection ()
    {
        return connection;
    }
    
    public Database getDatabase ()
    {
        return database;
    }
    
    public Console getConsole ()
    {
        return console;
    }
    
    public Logger getLogger ()
    {   
        return logger;
    }
    
    public Properties getProperties ()
    {
        return properties;
    }
}
