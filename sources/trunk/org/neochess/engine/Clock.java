
package org.neochess.engine;

public class Clock
{
    private static final int TIMEUP_BUFFER = 5000;
    
    protected long time;
    protected long increment;
    protected long remainingTime;
    protected long startMilliseconds;
    
    public Clock (long time)
    {
        this(time, 0);
    }
    
    public Clock (long time, long increment)
    {
        this.time = time;
        this.increment = increment;
        reset();
    }

    public long getIncrement ()
    {
        return increment;
    }

    public void setIncrement (long increment)
    {
        this.increment = increment;
    }

    public long getTime ()
    {
        return time;
    }

    public void setTime (long time)
    {
        this.time = time;
    }
    
    public final void reset ()
    {
        startMilliseconds = -1;
        remainingTime = time;
    }
    
    public void start ()
    {
        if (startMilliseconds < 0)
            startMilliseconds = System.currentTimeMillis();
    }
    
    public void stop ()
    {
        if (startMilliseconds > 0)
        {
            remainingTime -= (System.currentTimeMillis() - startMilliseconds);
            if (remainingTime > 0)
                remainingTime += increment;
            startMilliseconds = -1;
        }
    }
    
    public long getRemainingTime ()
    {
        long timeRemaining = this.remainingTime;
        if (startMilliseconds > 0)
            timeRemaining -= (System.currentTimeMillis() - startMilliseconds);
        if (timeRemaining <= 0)
            timeRemaining = (timeRemaining > -TIMEUP_BUFFER)? 1 : 0;
        return timeRemaining;
    }
    
    public void setRemainingTime (long remainingTime)
    {
        this.remainingTime = remainingTime;
        if (startMilliseconds > 0)
            startMilliseconds = System.currentTimeMillis();
    }
    
    public boolean isTimeUp()
    {
        return getRemainingTime() > 0;
    }
}
