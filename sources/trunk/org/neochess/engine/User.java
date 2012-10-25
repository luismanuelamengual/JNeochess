
package org.neochess.engine;

import org.neochess.general.Disposable;

public class User extends HumanPlayer implements Disposable
{
    protected int id;
    protected String firstName;
    protected String lastName;
    protected String userName;
    protected String password;

    public User ()
    {
    }

    @Override
    public void dispose ()
    {
        firstName = null;
        lastName = null;
        userName = null;
        password = null;
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

    public String getLastName ()
    {
        return lastName;
    }

    public void setLastName (String lastName)
    {
        this.lastName = lastName;
    }

    public String getFirstName ()
    {
        return firstName;
    }

    public void setFirstName (String firstName)
    {
        this.firstName = firstName;
    }
}
