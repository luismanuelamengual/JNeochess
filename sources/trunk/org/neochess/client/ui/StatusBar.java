
package org.neochess.client.ui;

import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;

public class StatusBar extends JLabel
{
    private Timer timer;
    private TimerTask timerTask;

    public StatusBar()
    {
        timer = new Timer();
        setBorder (javax.swing.BorderFactory.createRaisedBevelBorder());
        setPreferredSize (new Dimension( 28, 28 ));
    }

    public void reportMessage (String message)
    {
        reportMessage (message, 5);
    }

    public void reportMessage (String message, int seconds)
    {
        if (seconds > 0)
        {
            if (timerTask != null)
            {
                timerTask.cancel();
                timerTask = null;
            }
            timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    setText("");
                    timerTask = null;
                }
            };
            timer.schedule(timerTask, seconds*1000);
        }
        setText( " " + message );
    }
}
