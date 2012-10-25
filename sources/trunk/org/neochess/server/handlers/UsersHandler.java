
package org.neochess.server.handlers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.json.simple.JSONObject;
import org.neochess.engine.User;
import org.neochess.server.Application;
import org.neochess.server.ClientConnection;
import org.neochess.server.Console;
import org.neochess.server.Database;
import org.neochess.server.Database.DatabaseListener;

public class UsersHandler extends Handler implements Console.ConsoleListener, ClientConnection.ClientConnectionListener, DatabaseListener
{
    @Override
    public void start()
    {
        Application.getInstance().getConsole().addConsoleListener(this);
        Application.getInstance().getConnection().addConnectionListener(this);
        Application.getInstance().getDatabase().addDatabaseListener(this);
    }

    @Override
    public void stop()
    {
        Application.getInstance().getConsole().removeConsoleListener(this);
        Application.getInstance().getConnection().removeConnectionListener(this);
        Application.getInstance().getDatabase().removeDatabaseListener(this);
    }

    @Override
    public void onCommandEntered(String command, List<String> commandTokens)
    {
        
    }

    @Override
    public void onClientStarted (ClientConnection client)
    {
    }
    
    @Override
    public void onClientEnded (ClientConnection client)
    {
        logoutUser(client);
    }
    
    @Override
    public void onClientDataReceeived (ClientConnection client, JSONObject json)
    {
        String cmd = String.valueOf(json.get("cmd"));
        if (cmd.equals("createUser"))
        {
            JSONObject params = (JSONObject)json.get("params");
            String errorMessage = getUserErrorMessage(params);
            if (errorMessage.equals(""))
            {
                if (!insertUser(getUserFromObject(params)))
                    errorMessage = "Unexpected error. User could not be created";
            }
            
            JSONObject responseJson = new JSONObject();
            JSONObject responseParams = new JSONObject();
            responseJson.put("cmd", "response");
            responseJson.put("params", responseParams);
            responseParams.put("cmd", cmd);
            if (!errorMessage.equals(""))
            {   
                responseParams.put("status", "failure");
                responseParams.put("errorMessage", errorMessage);
            }
            else
            {
                responseParams.put("status", "success");
            }
            Application.getInstance().getConnection().sendToClient(client, responseJson);
        }
        else if (cmd.equals("updateUser"))
        {
            JSONObject params = (JSONObject)json.get("params");
            String errorMessage = getUserErrorMessage(params);
            if (errorMessage.equals(""))
            {
                if (!updateUser(getUserFromObject(params)))
                    errorMessage = "Unexpected error. User could not be updated";
            }    
            
            JSONObject responseJson = new JSONObject();
            JSONObject responseParams = new JSONObject();
            responseJson.put("cmd", "response");
            responseJson.put("params", responseParams);
            responseParams.put("cmd", cmd);
            if (!errorMessage.equals(""))
            {
                responseParams.put("status", "failure");
                responseParams.put("errorMessage", errorMessage);   
            }
            else
            {
                responseParams.put("status", "success");
            }
            Application.getInstance().getConnection().sendToClient(client, responseJson);
        }
        else if (cmd.equals("loginUser"))
        {
            JSONObject params = (JSONObject)json.get("params");
            String userName = String.valueOf(params.get("userName"));
            String password = String.valueOf(params.get("password"));
            User user = getUserByUserNameAndPassword(userName, password);
            
            JSONObject responseJson = new JSONObject();
            JSONObject responseParams = new JSONObject();
            responseJson.put("cmd", "response");
            responseJson.put("params", responseParams);
            responseParams.put("cmd", cmd);
            if (user != null)
            {    
                responseParams.put("status", "success");
                responseParams.put("user", getObjectFromUser(user));
                loginUser(client, user.getId());
            }
            else
            {
                responseParams.put("status", "failure");
                responseParams.put("errorMessage", "Username or password is Incorrect");
            }
            Application.getInstance().getConnection().sendToClient(client, responseJson); 
        }
        else if (cmd.equals("logoutUser"))
        {
            User user = getUserById(client.getClientId());
            JSONObject responseJson = new JSONObject();
            JSONObject responseParams = new JSONObject();
            responseJson.put("cmd", "response");
            responseJson.put("params", responseParams);
            responseParams.put("cmd", cmd);
            if (user != null)
            {    
                responseParams.put("status", "success");
                logoutUser(client);
            }
            else
            {
                responseParams.put("status", "failure");
                responseParams.put("errorMessage", "User not found !!");
            }
            Application.getInstance().getConnection().sendToClient(client, responseJson); 
        }
    }

    @Override
    public void onClientDataSent (ClientConnection client, JSONObject json)
    {
        
    }

    @Override
    public void onDatabaseOpened (Database database, boolean databaseCreated)
    {
        if (databaseCreated)
            createUserTable ();
    }

    @Override
    public void onDatabaseClosed (Database database)
    {
        
    }
    
    private String getUserErrorMessage (JSONObject json)
    {
        String errorMessage = "";
        int id = json.get("id") != null? ((Long)json.get("id")).intValue() : -1;
        String usernameField = String.valueOf(json.get("userName"));
        String passwordField = String.valueOf(json.get("password"));
        String nicknameField = String.valueOf(json.get("nickName"));
        String passwordrepeatField = String.valueOf(json.get("passwordRepeat"));
        
        if (usernameField.length() == 0) 
        {
            errorMessage = "Username must not be empty !!";
        }
        else if (passwordField.length() == 0) 
        {
            errorMessage = "Password fields must not be empty";
        }
        else if (nicknameField.length() == 0)
        {
            errorMessage = "Nickname is required";
        }
        else if (!usernameField.equals( usernameField.trim()) || !passwordField.equals(passwordField.trim()))
        {
            errorMessage = "Username and Password must not contain spaces";
        }
        else if (!passwordField.equals(passwordrepeatField))
        {
            errorMessage = "The two password fields must be de same";
        }
        else
        {
            User checkUser = getUserByUserName(usernameField);
            if (checkUser != null && checkUser.getId() != id)
                errorMessage = "There is a User with the same Username. Please change it to another one";
        }
        
        return errorMessage;
    }
    
    private void loginUser (ClientConnection client, int userId)
    {
        client.setClientId(userId);
        User user = getUserById(userId);
        Application.getInstance().getLogger().info("User [" + ((user != null)? user.getUserName() : "?") + "] logged In !!");
    }
    
    private void logoutUser (ClientConnection client)
    {
        int userId = client.getClientId();
        if (userId > 0)
        {
            client.setClientId(0);
            User user = getUserById(userId);
            Application.getInstance().getLogger().info("User [" + ((user != null)? user.getUserName() : "?") + "] logged Out !!");
        }
    }
    
    private User getUserFromObject (JSONObject json)
    {
        User user = new User ();
        if (json.get("id") != null)
            user.setId(((Long)json.get("id")).intValue());
        if (json.get("firstName") != null)
            user.setFirstName(String.valueOf(json.get("firstName")));
        if (json.get("lastName") != null)
            user.setLastName(String.valueOf(json.get("lastName")));
        if (json.get("userName") != null)
            user.setUserName(String.valueOf(json.get("userName")));
        if (json.get("nickName") != null)
            user.setNickName(String.valueOf(json.get("nickName")));
        if (json.get("password") != null)
            user.setPassword(String.valueOf(json.get("password")));
        if (json.get("imageUrl") != null)
            user.setImageUrl(String.valueOf(json.get("imageUrl")));
        if (json.get("elo") != null)
            user.setElo(((Long)json.get("elo")).intValue());
        return user;
    }
    
    private JSONObject getObjectFromUser (User user)
    {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("firstName", user.getFirstName());
        json.put("lastName", user.getLastName());
        json.put("userName", user.getUserName());
        json.put("nickName", user.getNickName());
        json.put("password", user.getPassword());
        json.put("imageUrl", user.getImageUrl());
        json.put("elo", user.getElo());
        return json;
    }
    
    private User getUserFromResultSet (ResultSet resultSet) throws SQLException
    {
        User user = new User ();
        user.setId(resultSet.getInt("ID"));
        user.setFirstName(resultSet.getString("FIRSTNAME"));
        user.setLastName(resultSet.getString("LASTNAME"));
        user.setUserName(resultSet.getString("USERNAME"));
        user.setNickName(resultSet.getString("NICKNAME"));
        user.setPassword(resultSet.getString("PASSWORD"));
        user.setImageUrl(resultSet.getString("IMAGEURL"));
        user.setElo(resultSet.getInt("ELO"));
        return user;
    }
    
    private User getUserByUserName (String userName)
    {
        String sql = "SELECT * FROM SYSUSER WHERE USERNAME = '" + userName + "'";
        User user = null;
        try
        {
            Statement statement = Application.getInstance().getDatabase().execute(sql);
            ResultSet resultSet = statement.getResultSet();
            if  (resultSet.next())
                user = getUserFromResultSet(resultSet);
            statement.close();
        }
        catch (Exception ex){}
        return user;
    }
    
    private User getUserByUserNameAndPassword (String userName, String password)
    {
        String sql = "SELECT * FROM SYSUSER WHERE USERNAME = '" + userName + "' AND PASSWORD = '" + password + "'";
        User user = null;
        try
        {
            Statement statement = Application.getInstance().getDatabase().execute(sql);
            ResultSet resultSet = statement.getResultSet();
            if  (resultSet.next())
                user = getUserFromResultSet(resultSet);
            statement.close();
        }
        catch (Exception ex){}
        return user;
    }
    
    private User getUserById (int userId)
    {
        String sql = "SELECT * FROM SYSUSER WHERE ID = " + userId;
        User user = null;
        try
        {
            Statement statement = Application.getInstance().getDatabase().execute(sql);
            ResultSet resultSet = statement.getResultSet();
            if  (resultSet.next())
                user = getUserFromResultSet(resultSet);
            statement.close();
        }
        catch (Exception ex){}
        return user;
    }
    
    private boolean insertUser (User user)
    {
        boolean userInserted = true;
        
        Application.getInstance().getLogger().info("Inserting new User ...");
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO SYSUSER ");
        sql.append("(");
        sql.append("FIRSTNAME, ");
        sql.append("LASTNAME, ");
        sql.append("USERNAME, ");
        sql.append("PASSWORD, ");
        sql.append("NICKNAME, ");
        sql.append("IMAGEURL, ");
        sql.append("ELO ");
        sql.append(") VALUES (" );
        sql.append("'" + user.getFirstName() + "', ");
        sql.append("'" + user.getLastName() + "', ");
        sql.append("'" + user.getUserName() + "', ");
        sql.append("'" + user.getPassword() + "', ");
        sql.append("'" + user.getNickName() + "', ");
        sql.append("'" + user.getImageUrl() + "', ");
        sql.append(user.getElo());
        sql.append(")");
        
        try
        {    
            Statement statement = Application.getInstance().getDatabase().execute(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next())
                user.setId(rs.getInt(1));
            statement.close();
            Application.getInstance().getLogger().info("User [" + user.getUserName() + "] inserted successfully !!");
        } 
        catch (SQLException e)
        {
            Application.getInstance().getLogger().warning("Error inserting User. Sql: " + sql.toString() + " Ex: " + e.getMessage());
            userInserted = false; 
        }
        return userInserted;
    }
    
    private boolean updateUser (User user)
    {
        boolean userUpdated = true;
        
        Application.getInstance().getLogger().info("Updating User [" + user.getUserName() + "] ...");
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE SYSUSER SET ");
        sql.append("FIRSTNAME = '" + user.getFirstName() + "', ");
        sql.append("LASTNAME = '" + user.getLastName() + "', ");
        sql.append("USERNAME = '" + user.getUserName() + "', ");
        sql.append("PASSWORD = '" + user.getPassword() + "', ");
        sql.append("NICKNAME = '" + user.getNickName() + "', ");
        sql.append("IMAGEURL = '" + user.getImageUrl() + "', ");
        sql.append("ELO = " + user.getElo() + " ");
        sql.append("WHERE (ID=" + user.getId() + ")");
        
        try
        {    
            Statement statement = Application.getInstance().getDatabase().execute(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            if (rs != null && rs.next())
                user.setId(rs.getBigDecimal(1).intValue());
            statement.close();
            Application.getInstance().getLogger().info("User [" + user.getUserName() + "] updated successfully !!");
        } 
        catch (SQLException e)
        {
            Application.getInstance().getLogger().warning("Error updating User " + user.getId() + " Sql: " + sql.toString() + " Ex: " + e.getMessage());
            userUpdated = false;
        }
        
        return userUpdated;
    }
    
    private boolean deleteUser (User user)
    {
        boolean userDeleted = true;
        
        Application.getInstance().getLogger().info("Deleting User [" + user.getUserName() + "] ...");
        String sql = "DELETE FROM SYSUSER WHERE ID=" + user.getId();
        try
        {
            Statement statement = Application.getInstance().getDatabase().execute(sql);
            statement.close();
            Application.getInstance().getLogger().info("User [" + user.getUserName() + "] deleted successfully !!");
        }
        catch (Exception e)
        {
            Application.getInstance().getLogger().warning("Error deleting User " + user.getId() + " Sql: " + sql + " Ex: " + e.getMessage());
            userDeleted = false;
        }
        
        return userDeleted;
    }
    
    private void createUserTable ()
    {
        Application.getInstance().getLogger().info("Creating User Table ...");
        StringBuilder sql = new StringBuilder();
        try
        {
            sql.append("CREATE TABLE SYSUSER ");
            sql.append("( ");
            sql.append("ID INT GENERATED ALWAYS AS IDENTITY NOT NULL, ");
            sql.append("FIRSTNAME VARCHAR(30), ");
            sql.append("LASTNAME VARCHAR(30), ");
            sql.append("USERNAME VARCHAR(30), ");
            sql.append("PASSWORD VARCHAR(30), ");
            sql.append("NICKNAME VARCHAR(30), ");
            sql.append("IMAGEURL VARCHAR(30), ");
            sql.append("ELO INT, ");
            sql.append("PRIMARY KEY (ID) ");
            sql.append(") ");
            
            Statement statement = Application.getInstance().getDatabase().execute(sql.toString());
            statement.close();
            Application.getInstance().getLogger().info("User Table created successfully !!");
        } 
        catch (SQLException e)
        {
            Application.getInstance().getLogger().severe("Error creating User Table. Ex: " + e.getMessage());
        }
    }
}
