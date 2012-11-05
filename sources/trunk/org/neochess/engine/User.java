
package org.neochess.engine;

public class User extends Player
{
    protected int id;
    protected String userName;
    protected String password;
    
    public User ()
    {
    }

    @Override
    public void dispose ()
    {
        userName = null;
        password = null;
        super.dispose();
    }

    public int getId ()
    {
        return id;
    }

    public void setId (int id)
    {
        this.id = id;
    }

    public String getPassword ()
    {
        return password;
    }

    public void setPassword (String password)
    {
        this.password = password;
    }

    public String getUserName ()
    {
        return userName;
    }

    public void setUserName (String userName)
    {
        this.userName = userName;
    }
}
