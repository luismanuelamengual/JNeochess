
package org.neochess.engine2;

import org.neochess.general.Disposable;


public class Player implements Disposable
{
    protected String firstName;
    protected String lastName;
    protected String nickName;
    protected String imageUrl;
    protected int elo;
    
    @Override
    public void dispose ()
    {
        firstName = null;
        lastName = null;
        nickName = null;
        imageUrl = null;
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
