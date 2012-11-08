
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.EventListener;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.neochess.client.Application;
import org.neochess.engine.Board;
import org.neochess.engine.Clock;
import org.neochess.engine.ComputerPlayer;
import org.neochess.engine.HumanPlayer;
import org.neochess.engine.Match;
import org.neochess.engine.Move;
import org.neochess.engine.Player;
import org.neochess.engine.User;
import org.neochess.util.ResourceUtils;

public class MatchFrame extends InternalFrame
{
    protected EventListenerList listeners = new EventListenerList();
    
    private Match match;
    private byte sideToMove;
    private byte state;
    private int displayPly = -1;
    private boolean boardFlipped = false;
    private BoardPanel boardPanel;
    private MatchMoveListPanel moveListPanel;
    private MatchOutputPanel outputPanel;
    private PlayerPanel topPlayerPanel;
    private PlayerPanel bottomPlayerPanel;
    
    public MatchFrame ()
    {
        super();
        setMinimumSize(new Dimension (300, 300));
        setSize(new java.awt.Dimension(500, 400));
        
        match = new Match();
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
        initializeOpeningBookFile();
    }

    @Override
    public void dispose()
    {
        match.dispose();
        match = null;
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
        removeAll();
        super.dispose();
    }
    
    @Override
    public boolean close (boolean forced)
    {
        boolean closeFrame = true;
        if (getState() == Match.STATE_PLAYING)
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

    public void setWhitePlayer (Player whitePlayer)
    {
        match.setWhitePlayer(whitePlayer);
    }

    public void setWhiteClock (Clock whiteClock)
    {
        match.setWhiteClock(whiteClock);
    }

    public void setState (byte state)
    {
        byte oldState = this.state;
        this.state = state;
        if (this.state != oldState)
            fireMatchStateChangedEvent (this.state);
    }

    public void setBlackPlayer (Player blackPlayer)
    {
        match.setBlackPlayer(blackPlayer);
    }

    public void setBlackClock (Clock blackClock)
    {
        match.setBlackClock(blackClock);
    }

    public boolean isTimeUp (byte side)
    {
        return match.isTimeUp(side);
    }

    public Player getWhitePlayer ()
    {
        return match.getWhitePlayer();
    }

    public Clock getWhiteClock ()
    {
        return match.getWhiteClock();
    }

    public Player getTurnPlayer ()
    {
        return match.getTurnPlayer();
    }

    public Clock getTurnClock ()
    {
        return match.getTurnClock();
    }

    public byte getState ()
    {
        return state;
    }

    public byte getSideToMove ()
    {
        return sideToMove;
    }

    public long getRemainingTime (byte side)
    {
        return match.getRemainingTime(side);
    }

    public int getPly ()
    {
        return match.getPly();
    }

    public Player getPlayer (byte side)
    {
        return match.getPlayer(side);
    }

    public List<Move> getMoves ()
    {
        return match.getMoves();
    }

    public Move getMove (int ply)
    {
        return match.getMove(ply);
    }

    public Clock getClock (byte side)
    {
        return match.getClock(side);
    }

    public Board getBoard (int ply)
    {
        return match.getBoard(ply);
    }

    public Board getBoard ()
    {
        return match.getBoard();
    }

    public Player getBlackPlayer ()
    {
        return match.getBlackPlayer();
    }

    public Clock getBlackClock ()
    {
        return match.getBlackClock();
    }
    
    public synchronized void reset ()
    {
        match.reset();
    }
    
    public synchronized void start ()
    {
        initializeMatch ();
    }
    
    public synchronized boolean makeMove (Move move)
    {
        boolean moveMade = match.makeMove(move);
        if (moveMade)
        {
            fireMatchMoveEvent(move);
            fireMatchPositionChangedEvent();
            setDisplayPly(getPly());
        }
        processState();
        return moveMade;
    }
    
    public synchronized boolean unmakeMove ()
    {
        Move lastMove = match.getMoves().get(match.getMoves().size()-1);
        boolean moveUnmade = match.unmakeMove();
        if (moveUnmade)
        {
            fireMatchTakebackEvent(lastMove);
            fireMatchPositionChangedEvent();
            setDisplayPly(getPly());
        }
        processState();
        return moveUnmade;
    }
    
    public void updateState ()
    {
        setState (match.updateState());
    }
    
    protected void processState ()
    {
        updateState ();
        switch (state)
        {
            case Match.STATE_NOTSTARTED:
                break;
            case Match.STATE_PLAYING:
                if (match.getSideToMove() != sideToMove)
                    finalizeTurn(sideToMove);
                initializeTurn(match.getSideToMove());
                break;
            case Match.STATE_FINISHED_DRAW:
            case Match.STATE_FINISHED_BLACKWIN:
            case Match.STATE_FINISHED_WHITEWIN:
                if (sideToMove != Board.NOSIDE)
                    finalizeTurn(sideToMove);
                finalizeMatch ();
        }
    }
    
    protected void initializeMatch ()
    {
        match.start();
        updateState ();
        fireMatchStartedEvent ();
        fireMatchPositionChangedEvent();
        processState ();
    }

    protected void finalizeMatch ()
    {
        fireMatchFinishedEvent ();
    }
    
    protected void initializeTurn (byte side)
    {
        sideToMove = side;
        onInitializeTurn (side);
        fireMatchTurnStartedEvent (side);
    }
    
    protected void finalizeTurn (byte side)
    {
        sideToMove = Board.NOSIDE;
        onFinalizeTurn (side);
        fireMatchTurnEndedEvent (side);
    }
    
    protected void onInitializeTurn (byte side)
    {
        Player turnPlayer = getPlayer(side);
        if (turnPlayer != null)
        {
            if (turnPlayer instanceof HumanPlayer || (turnPlayer instanceof User && turnPlayer.equals(Application.getInstance().getSession().getUser())))
            {
                boardPanel.setHumanMoveEnabled(true);
            }
            else if (turnPlayer instanceof ComputerPlayer)
            {
                final ComputerPlayer computerPlayer = (ComputerPlayer)turnPlayer;
                new Thread ()
                {
                    @Override
                    public void run ()
                    {
                        final Move moveSearched = computerPlayer.startMoveSearch(match);
                        SwingUtilities.invokeLater(new Runnable() 
                        {
                            @Override
                            public void run ()
                            {
                                if (match != null)
                                    makeMove(moveSearched);
                            }
                        });
                    }
                }.start();
            }
        }
    }
    
    protected void onFinalizeTurn (byte side)
    {
        boardPanel.setHumanMoveEnabled(false);
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
        if (ply >= 0 && ply <= getPly())
        {
            int oldDisplayPly = this.displayPly;
            this.displayPly = ply;
            if (this.displayPly != oldDisplayPly)
                fireMatchDisplayPlyChangedEvent (this.displayPly);
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
            fireMatchBoardFlippedEvent (this.boardFlipped);
    }
    
    public void addMatchFrameListener(MatchFrameListener listener)
    {
        listeners.add(MatchFrameListener.class, listener);
    }

    public void removeMatchFrameListener(MatchFrameListener listener)
    {
        listeners.remove(MatchFrameListener.class, listener);
    }
    
    private void fireMatchStartedEvent ()
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchStarted(this);
    }
    
    private void fireMatchFinishedEvent ()
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchFinished(this);
    }
    
    private void fireMatchPositionChangedEvent ()
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchPositionChanged(this);
    }
    
    private void fireMatchTurnStartedEvent (byte side)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchTurnStarted(this, side);
    }
    
    private void fireMatchTurnEndedEvent (byte side)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchTurnEnded(this, side);
    }
    
    private void fireMatchMoveEvent (Move move)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchMove(this, move);
    }
    
    private void fireMatchTakebackEvent (Move move)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchTakeback(this, move);
    }
    
    private void fireMatchStateChangedEvent (byte state)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchStateChanged(this, state);
    }
    
    private void fireMatchDisplayPlyChangedEvent (int ply)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchDisplayPlyChanged(this, ply);
    }
    
    private void fireMatchBoardFlippedEvent (boolean flipped)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchBoardFlipped(this, flipped);
    }
    
    public interface MatchFrameListener extends EventListener
    {
        public void onMatchStarted (MatchFrame match);
        public void onMatchFinished (MatchFrame match);
        public void onMatchPositionChanged (MatchFrame match);
        public void onMatchTurnStarted (MatchFrame match, byte side);
        public void onMatchTurnEnded (MatchFrame match, byte side);
        public void onMatchMove (MatchFrame match, Move move);
        public void onMatchTakeback (MatchFrame match, Move move);
        public void onMatchStateChanged (MatchFrame match, byte state);
        public void onMatchDisplayPlyChanged (MatchFrame match, int ply);
        public void onMatchBoardFlipped (MatchFrame match, boolean flipped);
    }
}
