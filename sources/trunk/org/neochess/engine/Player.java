
package org.neochess.engine;

import org.neochess.general.Disposable;

public class Player implements Disposable
{
    protected String nickName;
    protected int elo;
    protected String imageUrl;

    public Player ()
    {
        this("Player", 1600);
    }
    
    public Player (String name, int elo)
    {
        this.nickName = name;
        this.elo = elo;
    }

    public void dispose ()
    {
        nickName = null;
        imageUrl = null;
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
