
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.EventListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.EventListenerList;
import org.neochess.client.Application;
import org.neochess.engine.Board.Move;
import org.neochess.engine.Match;
import org.neochess.engine.Match.MatchListener;
import org.neochess.engine.Player;
import org.neochess.util.ResourceUtils;

public class MatchFrame extends InternalFrame implements MatchListener
{
    protected EventListenerList listeners = new EventListenerList();
    private Match match;
    private int displayPly = -1;
    private boolean boardFlipped = false;
    private BoardPanel boardPanel;
    private MatchMoveListPanel moveListPanel;
    private MatchOutputPanel outputPanel;
    private PlayerPanel topPlayerPanel;
    private PlayerPanel bottomPlayerPanel;
    
    public MatchFrame (Player whitePlayer, Player blackPlayer)
    {
        super();
        setMinimumSize(new Dimension (300, 300));
        setSize(new java.awt.Dimension(500, 400));
        
        this.match = new Match ();
        this.match.addMatchListener(this);
        this.match.setWhitePlayer(whitePlayer);
        this.match.setBlackPlayer(blackPlayer);
        boardPanel = new BoardPanel(this);
        moveListPanel = new MatchMoveListPanel(this);
        outputPanel = new MatchOutputPanel(this);
        topPlayerPanel = new PlayerPanel(this, false);
        bottomPlayerPanel = new PlayerPanel(this, true);
        
        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPanel.setMinimumSize(new Dimension(100, 100));
        tabPanel.setPreferredSize(new Dimension(180, 200));
        tabPanel.addTab("Move List", moveListPanel);
        tabPanel.addTab("Output", outputPanel);
        
        JPanel boardContainerPanel = new JPanel();
        boardContainerPanel.setAutoscrolls(true);
        boardContainerPanel.setLayout(new BorderLayout());
        boardContainerPanel.add(boardPanel, BorderLayout.CENTER);
        boardContainerPanel.add(topPlayerPanel, BorderLayout.NORTH);
        boardContainerPanel.add(bottomPlayerPanel, BorderLayout.SOUTH);
        
        JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPanel.setLeftComponent(boardContainerPanel);
        splitPanel.setRightComponent(tabPanel);
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setResizeWeight(1);
        
        add(splitPanel);        
        setVisible(true);
        pack();
        initializeMatch ();
        startMatch();
    }

    @Override
    public void dispose()
    {
        topPlayerPanel.dispose();
        topPlayerPanel = null;
        bottomPlayerPanel.dispose();
        bottomPlayerPanel = null;
        boardPanel.dispose();
        boardPanel = null;
        moveListPanel.dispose();
        moveListPanel = null;
        outputPanel.dispose();
        outputPanel = null;
        this.match.removeMatchListener(this);
        match.dispose();
        match = null;
        removeAll();
        super.dispose();
    }
    
    @Override
    public boolean close (boolean forced)
    {
        boolean closeFrame = true;
        if (match.getState() == Match.STATE_PLAYING)
        {
            if (forced || JOptionPane.showConfirmDialog(this, "Match in progress !!, Are you sure to close match ?", Application.getInstance().getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
            }
            else
            {
                closeFrame = false;
            }   
        }
        return closeFrame? super.close(forced) : false;
    }

    public boolean makeMove (Move move)
    {
        return match.makeMove(move);
    }

    public byte getState ()
    {
        return match.getState();
    }

    public int getPly ()
    {
        return match.getPly();
    }

    public void startMatch ()
    {
        match.start();
    }

    public Match getMatch()
    {
        return match;
    }

    public BoardPanel getBoardPanel()
    {
        return boardPanel;
    }

    public MatchMoveListPanel getMoveListPanel()
    {
        return moveListPanel;
    }

    public MatchOutputPanel getOutputPanel()
    {
        return outputPanel;
    }
    
    private void initializeMatch ()
    {
        initializeOpeningBookFile();
    }
    
    private void initializeOpeningBookFile ()
    {
        ResourceUtils.copyResourceToFile(Application.getInstance().getResourceGeneralPath() + "OpeningBook.bin", Application.getHomePath() + File.separatorChar + "OpeningBook.bin");
    }
    
    public int getDisplayPly()
    {
        return displayPly;
    }
    
    public void setDisplayPly(int ply)
    {
        if (ply >= 0 && ply <= match.getPly())
        {
            int oldDisplayPly = this.displayPly;
            this.displayPly = ply;
            if (this.displayPly != oldDisplayPly)
                fireMatchDisplayPlyChangedEvent (match, this.displayPly);
        }
    }

    public boolean isBoardFlipped ()
    {
        return boardFlipped;
    }

    public void setBoardFlipped (boolean boardFlipped)
    {
        boolean oldBoardFlipped = this.boardFlipped;
        this.boardFlipped = boardFlipped;
        if (this.boardFlipped != oldBoardFlipped)
            fireMatchBoardFlippedEvent (match, this.boardFlipped);
    }
    
    public void addMatchFrameListener(MatchFrameListener listener)
    {
        listeners.add(MatchFrameListener.class, listener);
    }

    public void removeMatchFrameListener(MatchFrameListener listener)
    {
        listeners.remove(MatchFrameListener.class, listener);
    }
    
    public void onMatchMove (Match match, Move move)
    {
        setDisplayPly(match.getPly());
    }
    
    public void onMatchTakeback (Match match, Move move)
    {
        setDisplayPly(match.getPly());
    }
    
    public void onMatchTurnStarted (Match match, byte side)
    {
        if (match.getTurnPlayer().equals(Application.getInstance().getSession().getUser()))
            boardPanel.setHumanMoveEnabled(true);
    }
    
    public void onMatchTurnEnded (Match match, byte side)
    {
        boardPanel.setHumanMoveEnabled(false);
    }
    
    public void onMatchStarted (Match match){}
    public void onMatchFinished (Match match){}
    public void onMatchPositionChanged (Match match){}
    public void onMatchStateChanged (Match match, byte state){}
    
    private void fireMatchDisplayPlyChangedEvent (Match match, int ply)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchDisplayPlyChanged(match, ply);
    }
    
    private void fireMatchBoardFlippedEvent (Match match, boolean flipped)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchBoardFlipped(match, flipped);
    }
    
    public interface MatchFrameListener extends EventListener
    {
        public void onMatchDisplayPlyChanged (Match match, int ply);
        public void onMatchBoardFlipped (Match match, boolean flipped);
    }
}
