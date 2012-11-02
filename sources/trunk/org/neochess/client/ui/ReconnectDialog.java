
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.json.simple.JSONObject;
import org.neochess.client.Application;
import org.neochess.client.Connection.ConnectionListener;
import org.neochess.general.Disposable;

public class ReconnectDialog extends JDialog implements ConnectionListener, Disposable
{
    private JLabel messageLabel;
    private Timer updateTimer;
    private int remainingSeconds;
    
    public ReconnectDialog (Frame frame)
    {
        super(frame, "", true);
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension (280, 130));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setTitle(Application.getInstance().getShortTitle());
        add(createMessageLabel(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.SOUTH);
        pack();
        Application.getInstance().getConnection().addConnectionListener(this);
        startTimer();
    }
    
    public void dispose()
    {
        Application.getInstance().getConnection().removeConnectionListener(this);
        stopTimer();
        messageLabel = null;
        removeAll();
        super.dispose();
    }
    
    private JLabel createMessageLabel ()
    {
        messageLabel = new JLabel();
        messageLabel.setAlignmentX(JLabel.CENTER);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        return messageLabel;
    }
    
    private void startTimer ()
    {
        remainingSeconds = 45;
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() 
        {
            @Override
            public void run ()
            {
                messageLabel.setText("Reconnecting (" + remainingSeconds + ") ...");
                remainingSeconds--;
                if (remainingSeconds < 0)
                {
                    Application.getInstance().getSession().destroySession();
                    dispose();
                }
             }
        }, 100, 1000);
    }
    
    private void stopTimer ()
    {
        if (updateTimer != null)
        {
            updateTimer.cancel();
            updateTimer = null;
        }
    }
    
    private JPanel createButtonsPanel ()
    {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton closeSessionButton = new JButton("Close session");
        closeSessionButton.addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                Application.getInstance().getSession().destroySession();
                dispose();
            }
        });
        buttonsPanel.add(closeSessionButton);
        return buttonsPanel;
    }
    
    @Override
    public void onConnectionStarted ()
    {
        try
        {
            Application.getInstance().getSession().restartSession();
        }
        catch (Exception ex)
        {
            Application.getInstance().getSession().destroySession();
        }
        dispose();
    }

    @Override
    public void onConnectionEnded ()
    {
    }

    @Override
    public void onDataReceived (JSONObject json)
    {
    }

    @Override
    public void onDataSent (JSONObject json)
    {
    }
}
