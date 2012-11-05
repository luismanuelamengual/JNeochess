
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
import javax.swing.event.EventListenerList;
import org.neochess.client.Application;
import org.neochess.engine.Board;
import org.neochess.engine.Board.Move;
import org.neochess.engine.Clock;
import org.neochess.engine.User;
import org.neochess.engine.Player;
import org.neochess.util.ResourceUtils;

public class MatchFrame extends InternalFrame
{
    public static final byte STATE_NOTSTARTED = 0;
    public static final byte STATE_PLAYING = 1;
    public static final byte STATE_FINISHED_DRAW = 2;
    public static final byte STATE_FINISHED_WHITEWIN = 3;
    public static final byte STATE_FINISHED_BLACKWIN = 4;
    
    protected EventListenerList listeners = new EventListenerList();
    
    private List<Board> historyBoards;
    private List<Move> historyMoves;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Clock whiteClock;
    private Clock blackClock;
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
        if (getState() == STATE_PLAYING)
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

    public Player getBlackPlayer ()
    {
        return blackPlayer;
    }

    public void setBlackPlayer (Player blackPlayer)
    {
        this.blackPlayer = blackPlayer;
    }

    public Player getWhitePlayer ()
    {
        return whitePlayer;
    }

    public void setWhitePlayer (Player whitePlayer)
    {
        this.whitePlayer = whitePlayer;
    }
    
    public Player getPlayer (byte side)
    {
        return (side == Board.WHITE)? whitePlayer : blackPlayer;
    }
    
    public Player getTurnPlayer ()
    {
        return (sideToMove == Board.WHITE)? whitePlayer : ((sideToMove == Board.BLACK)?blackPlayer : null);
    }
    
    public byte getSideToMove ()
    {
        return sideToMove;
    }
    
    public int getPly ()
    {
        return historyBoards.size();
    }
    
    public void setState (byte state)
    {
        byte oldState = this.state;
        this.state = state;
        if (this.state != oldState)
            fireMatchStateChangedEvent (this.state);
    }

    public byte getState ()
    {
        return state;
    }
    
    public Board getBoard()
    {
        return board;
    }

    public Board getBoard (int ply)
    {
        return (ply == getPly())? getBoard() : historyBoards.get(ply);
    }

    public Move getMove (int ply)
    {
        return historyMoves.get(ply);
    }

    public List<Move> getMoves()
    {
        return historyMoves;
    }

    public Clock getBlackClock ()
    {
        return blackClock;
    }

    public void setBlackClock (Clock blackClock)
    {
        this.blackClock = blackClock;
    }

    public Clock getWhiteClock ()
    {
        return whiteClock;
    }

    public void setWhiteClock (Clock whiteClock)
    {
        this.whiteClock = whiteClock;
    }
    
    public Clock getTurnClock ()
    {
        return (sideToMove == Board.WHITE)? whiteClock : ((sideToMove == Board.BLACK)?blackClock : null);
    }

    public void start ()
    {
        if (state == STATE_NOTSTARTED)
            initializeMatch ();
    }
    
    public void restart ()
    {
        state = STATE_NOTSTARTED;
        start();
    }

    protected void initializeMatch ()
    {
        fireMatchStartedEvent ();
        clearHistory ();
        setStartupBoard();
        setState(STATE_PLAYING);
        processState ();
    }

    protected void finalizeMatch ()
    {
        fireMatchFinishedEvent ();
    }
    
    protected void clearHistory ()
    {
        historyBoards.clear();
        historyMoves.clear();
    }
    
    protected void setStartupBoard ()
    {
        board.setStartupPosition();
        fireMatchPositionChangedEvent ();
    }

    protected void processState ()
    {
        switch (state)
        {
            case STATE_NOTSTARTED:
                break;
            case STATE_PLAYING:
                if (board.getSideToMove() != sideToMove)
                    finalizeTurn(sideToMove);
                initializeTurn(board.getSideToMove());
                break;
            case STATE_FINISHED_DRAW:
            case STATE_FINISHED_BLACKWIN:
            case STATE_FINISHED_WHITEWIN:
                if (sideToMove != Board.NOSIDE)
                    finalizeTurn(sideToMove);
                finalizeMatch ();
        }
    }
  
    protected void initializeTurn (byte side)
    {
        sideToMove = side;
        Clock turnClock = getTurnClock();
        if (turnClock != null)
            turnClock.start();
        fireMatchTurnStartedEvent (side);
    }
    
    protected void finalizeTurn (byte side)
    {
        Clock turnClock = getTurnClock();
        sideToMove = Board.NOSIDE;
        if (turnClock != null)
            turnClock.stop();
        fireMatchTurnEndedEvent (side);
    }
    
    public long getRemainingTime (byte side)
    {
        Clock clock = (side == Board.WHITE)? whiteClock : blackClock;
        return (clock != null)? clock.getRemainingTime() : -1;
    }
    
    public boolean isTimeUp (byte side)
    {
        return (getRemainingTime(side) == 0);
    }
    
    public boolean checkTime ()
    {
        boolean isTimeOk = true;
        if (state == STATE_PLAYING && sideToMove != Board.NOSIDE && isTimeUp(sideToMove))
        {
            setState(sideToMove == Board.WHITE? STATE_FINISHED_BLACKWIN : STATE_FINISHED_WHITEWIN);
            processState();
            isTimeOk = false;
        }
        return isTimeOk;
    }
    
    public boolean makeMove (Move move)
    {
        boolean moveMade = false;
        if (state == STATE_PLAYING)
        {
            if (checkTime() && move != null && board.isMoveValid(move))
            {
                historyMoves.add(move);
                historyBoards.add(board.clone());
                board.makeMove(move);
                setDisplayPly(getPly());
                fireMatchMoveEvent (move);                
                fireMatchPositionChangedEvent ();
                updateState();
                processState();
                moveMade = true;
            }
        }
        return moveMade;
    }

    protected void unmakeMove ()
    {
        if (state == STATE_PLAYING)
        {
            if (checkTime() && historyBoards.size() > 0)
            {
                Board lastHistoryBoard = historyBoards.get(historyBoards.size() - 1);
                Move lastMove = historyMoves.get(historyMoves.size() - 1);
                board.copy(lastHistoryBoard);
                lastHistoryBoard.dispose();
                historyBoards.remove(lastHistoryBoard);
                historyMoves.remove(lastMove);
                setDisplayPly(getPly());
                fireMatchTakebackEvent (lastMove);
                fireMatchPositionChangedEvent ();
                updateState ();
                processState ();
            }
        }
    }
    
    protected void updateState ()
    {
        if (state == STATE_PLAYING)
        {
            if (board.inCheckMate())
            {
                setState((board.getSideToMove() == Board.WHITE)? STATE_FINISHED_BLACKWIN : STATE_FINISHED_WHITEWIN); 
            }
            else if (board.inStaleMate())
            {
                setState(STATE_FINISHED_DRAW);
            }
        }
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
