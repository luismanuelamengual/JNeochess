
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import org.neochess.client.Application;
import org.neochess.engine.Board;
import org.neochess.engine.Match;
import org.neochess.engine.Move;
import org.neochess.engine.Player;
import org.neochess.general.Disposable;
import org.neochess.util.ResourceUtils;
import org.neochess.util.UserInterfaceUtils;

public class PlayerPanel extends JPanel implements Disposable, MatchFrame.MatchFrameListener
{
    private MatchFrame matchFrame;
    private boolean isBottomPlayer;
    private JLabel playerInfoLabel;
    private JLabel playerRemainingTimeLabel;
    private String savedImageUrl;
    private Timer timer;
    
    public PlayerPanel (MatchFrame matchFrame, boolean isBottomPlayer)
    { 
        super();
        this.matchFrame = matchFrame;
        this.matchFrame.addMatchFrameListener(this);
        this.isBottomPlayer = isBottomPlayer;
        playerInfoLabel = createPlayerInfoLabel();
        playerRemainingTimeLabel = createPlayerRemainingTimeLabel();
        setLayout(new BorderLayout(1, 1));   
        add(playerInfoLabel, BorderLayout.CENTER);
        add(playerRemainingTimeLabel, BorderLayout.EAST);
        update();
    }

    public void dispose ()
    {
        stopUpdateTimer();
        this.removeAll();
        playerInfoLabel = null;
        playerRemainingTimeLabel = null;
        matchFrame.removeMatchFrameListener(this);
        matchFrame = null;
    }
    
    public void stopUpdateTimer()
    {
        if (timer != null)
        {
            timer.stop();
            timer = null;
        }
    }
    
    public void startUpdateTimer()
    {
        stopUpdateTimer();
        timer = new Timer(1000, new ActionListener()
        {
            public void actionPerformed (ActionEvent ae)
            {
                update();
            }
        });
        timer.start();
    }
    
    private byte getPlayerSide ()
    {
        return matchFrame.isBoardFlipped() != isBottomPlayer? Board.WHITE : Board.BLACK;
    }
    
    public boolean isActivated ()
    {
        return this.matchFrame.getState() == Match.STATE_PLAYING && this.matchFrame.getSideToMove() == getPlayerSide();
    }
    
    @Override
    public void paintComponent (Graphics screen) 
    {
        super.paintComponent(screen);
        Color startColor, finishColor;
        if (isActivated())
        {
            startColor = UserInterfaceUtils.getColor ("Table.selectionBackground");
            finishColor = Color.WHITE;
        }
        else 
        {
            startColor = UserInterfaceUtils.getColor ("Panel.background"); 
            finishColor = Color.WHITE;
        }
            
        Graphics2D screen2D = (Graphics2D) screen;
        screen2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gradientPaint = new GradientPaint(getWidth(), 0, startColor, getWidth(), getHeight(), finishColor);
        screen2D.setPaint(gradientPaint);
        screen2D.fill(new java.awt.geom.Rectangle2D.Double(0, 0, getWidth(), getHeight()));
    }
    
    public void update ()
    {
        byte playerSide = getPlayerSide();
        Color foregroundColor = UserInterfaceUtils.getColor (isActivated()?"Table.selectionForeground":"Panel.foreground");
        Player player = matchFrame.getPlayer(playerSide);
        if (savedImageUrl == null || !savedImageUrl.equals(player.getImageUrl()))
        {
            ImageIcon icon = null;
            if (player.getImageUrl() != null && !player.getImageUrl().equals(""))
            {
                int avatarIndex = 0;
                if (player.getImageUrl().startsWith("{"))
                {
                    avatarIndex = Integer.parseInt(player.getImageUrl().substring(1,2));
                    icon = new ImageIcon(ResourceUtils.getImage(Application.getInstance().getResourceImagesPath() + "avatars/avatar" + avatarIndex + ".gif").getScaledInstance(25, 25, Image.SCALE_SMOOTH));
                }
            }
            playerInfoLabel.setIcon(icon);
            savedImageUrl = player.getImageUrl();
        }
        playerInfoLabel.setText(player.getNickName() + " (" + player.getElo() + ")");
        playerInfoLabel.setForeground(foregroundColor);
        
        long remainingTime = matchFrame.getRemainingTime(playerSide);
        StringBuilder remainingTimeString = new StringBuilder();
        if (remainingTime >= 0)
        {
            int remainingSeconds = (int)(remainingTime / 1000);
            playerRemainingTimeLabel.setForeground((remainingSeconds > 10)? Color.BLACK : new Color( 170, 50, 50 ));
            
            int hours = remainingSeconds / 3600;
            remainingSeconds -= hours * 3600;
            int minutes = remainingSeconds / 60;
            remainingSeconds -= minutes * 60;
            
            if (hours > 0)
            {
                if (hours < 10)
                    remainingTimeString.append('0');
                remainingTimeString.append(hours);
                remainingTimeString.append(':');
            }
            
            if (hours > 0 || minutes > 0)
            {
                if (minutes < 10)
                    remainingTimeString.append('0');
                remainingTimeString.append(minutes);
                remainingTimeString.append(':');
            }
            
            if (remainingSeconds < 10)
                remainingTimeString.append('0');
            remainingTimeString.append(remainingSeconds);
        }
        playerRemainingTimeLabel.setText(remainingTimeString.toString());
        playerRemainingTimeLabel.setForeground(foregroundColor);   
        repaint();
    }
    
    private JLabel createPlayerInfoLabel ()
    {
        JLabel label = new JLabel("", JLabel.LEFT);
        label.setFont (new java.awt.Font( "Arial", java.awt.Font.BOLD, 12));
        label.setPreferredSize (new Dimension(200, 30));
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        return label;
    }
    
    private JLabel createPlayerRemainingTimeLabel ()
    {
        JLabel label = new JLabel("", JLabel.RIGHT);
        label.setFont (new java.awt.Font( "Arial", java.awt.Font.BOLD, 14));
        label.setPreferredSize (new Dimension(90, 30));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        return label;
    }

    public void onMatchTurnStarted (MatchFrame match, byte side)
    {
        startUpdateTimer();
        update();
    }

    public void onMatchTurnEnded (MatchFrame match, byte side)
    {
        stopUpdateTimer();
        update();
    }
    
    public void onMatchDisplayPlyChanged (MatchFrame match, int ply)
    {
        update();
    }

    public void onMatchBoardFlipped (MatchFrame match, boolean flipped)
    {
        update();
    }
    
    public void onMatchStarted (MatchFrame match) {}
    public void onMatchFinished (MatchFrame match) {}
    public void onMatchPositionChanged (MatchFrame match) {}
    public void onMatchMove (MatchFrame match, Move move) {}
    public void onMatchTakeback (MatchFrame match, Move move) {}
    public void onMatchStateChanged (MatchFrame match, byte state) {}
}
