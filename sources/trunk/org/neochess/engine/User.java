
package org.neochess.engine;

import org.neochess.general.Disposable;

public class User implements Disposable
{
    protected int id;
    protected String firstName;
    protected String lastName;
    protected String userName;
    protected String password;
    protected String nickName;
    protected int elo;
    protected String imageUrl;
    
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
        nickName = null;
        imageUrl = null;
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
    
    public int getElo ()
    {
        return elo;
    }

    public void setElo (int elo)
    {
        this.elo = elo;
    }

    public String getNickName ()
    {
        return nickName;
    }

    public void setNickName (String name)
    {
        this.nickName = name;
    }

    public String getImageUrl ()
    {
        return imageUrl;
    }

    public void setImageUrl (String imageUrl)
    {
        this.imageUrl = imageUrl;
    }
}
