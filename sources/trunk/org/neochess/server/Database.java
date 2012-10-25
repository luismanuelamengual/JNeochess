
package org.neochess.server;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EventListener;
import javax.swing.event.EventListenerList;

public class Database
{
    private static final String DATABASE_DRIVERSTRING = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String DATABASE_DERBYPREFIX = "jdbc:derby:";
    private static final String DATABASE_NAME = Application.getHomePath() + File.separatorChar + "database";
    protected EventListenerList listeners = new EventListenerList();
    private java.sql.Connection connection = null;
    
    public Database ()
    {
    }
    
    public void open ()
    {
        if (connection == null)
        {
            try
            {
                Class.forName(DATABASE_DRIVERSTRING);
                File databaseDirectory = new File(DATABASE_NAME);
                if (!databaseDirectory.exists())
                {
                    Application.getInstance().getLogger().info("Creating database (" + DATABASE_NAME + ") ...");
                    connection = DriverManager.getConnection(DATABASE_DERBYPREFIX + DATABASE_NAME + ";create=true");
                    Application.getInstance().getLogger().info("Database (" + DATABASE_NAME + ") created !!");
                    fireDatabaseOpenedEvent (true);
                }
                else
                {
                    Application.getInstance().getLogger().info("Opening database (" + DATABASE_NAME + ") ...");
                    connection = DriverManager.getConnection(DATABASE_DERBYPREFIX + DATABASE_NAME);
                    Application.getInstance().getLogger().info("Database (" + DATABASE_NAME + ") opened !!");
                    fireDatabaseOpenedEvent (false);
                }
            } 
            catch (Exception ex) 
            {
                Application.getInstance().getLogger().severe("The Database (" + DATABASE_NAME + ") could not be opened/created. Ex: " + ex.getMessage());
                connection = null;
            }
        }
    }
    
    public void close ()
    {
        if (connection != null)
        {
            Application.getInstance().getLogger().info("Closing database (" + DATABASE_NAME + ") ...");
            try { connection = DriverManager.getConnection(DATABASE_DERBYPREFIX + ";shutdown=true"); } catch(SQLException se) {}
            try { connection.close(); } catch(SQLException se) {}
            connection = null;
            fireDatabaseClosedEvent ();
            Application.getInstance().getLogger().info("Database (" + DATABASE_NAME + ") closed !!");
        }
    }
    
    public Statement execute (String sql) throws SQLException 
    {
        return execute(sql, Statement.NO_GENERATED_KEYS);
    }
    
    public Statement execute (String sql, int flags) throws SQLException 
    {
        if (Application.getInstance().getProperties().get("databaseLogging").equals("on"))
            Application.getInstance().getLogger().info("Executing sql \"" + sql + "\"");
        Statement statement = connection.createStatement();
        statement.execute(sql, flags);
        return statement;
    }
    
    public void addDatabaseListener(DatabaseListener listener)
    {
        listeners.add(DatabaseListener.class, listener);
    }

    public void removeDatabaseListener(DatabaseListener listener)
    {
        listeners.remove(DatabaseListener.class, listener);
    }
    
    private void fireDatabaseOpenedEvent (boolean databaseCreated)
    {
        for (DatabaseListener listener : listeners.getListeners(DatabaseListener.class))
            listener.onDatabaseOpened(this, databaseCreated);
    }

    private void fireDatabaseClosedEvent ()
    {
        for (DatabaseListener listener : listeners.getListeners(DatabaseListener.class))
            listener.onDatabaseClosed(this);
    }
    
    public interface DatabaseListener extends EventListener
    {
        public void onDatabaseOpened (Database database, boolean databaseCreated);
        public void onDatabaseClosed (Database database);
    }
}
