
package org.neochess.engine;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.event.EventListenerList;
import org.neochess.engine.Board.Move;
import org.neochess.general.Disposable;

public class Match implements Disposable
{
    public static final byte STATE_NOTSTARTED = 0;
    public static final byte STATE_PLAYING = 1;
    public static final byte STATE_FINISHED_DRAW = 2;
    public static final byte STATE_FINISHED_WHITEWIN = 3;
    public static final byte STATE_FINISHED_BLACKWIN = 4;

    protected EventListenerList listeners = new EventListenerList();
    private int id;
    private List<Board> historyBoards;
    private List<Move> historyMoves;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private byte sideToMove;
    private byte state;
    private boolean rated;
    private Clock whiteClock;
    private Clock blackClock;

    public Match ()
    {
        this.historyBoards = new ArrayList<Board>();
        this.historyMoves = new ArrayList<Move>();
        sideToMove = Board.NOSIDE;
        board = new Board();
        rated = true;
        state = STATE_NOTSTARTED;
    }
    
    public void dispose()
    {
        while(listeners.getListenerCount() > 0)
            removeMatchListener((MatchListener)listeners.getListenerList()[0]);
        listeners = null;
        board.dispose();
        board = null;
        historyBoards.clear();
        historyBoards = null;
        historyMoves.clear();
        historyMoves = null;
        whitePlayer = null;
        blackPlayer = null;
        whiteClock = null;
        blackClock = null;
    }

    public int getId ()
    {
        return id;
    }

    public void setId (int id)
    {
        this.id = id;
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
            fireMatchStateChangedEvent (this, this.state);
    }

    public boolean isRated ()
    {
        return rated;
    }

    public void setRated (boolean rated)
    {
        this.rated = rated;
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
        fireMatchStartedEvent (this);
        clearHistory ();
        setStartupBoard();
        setState(STATE_PLAYING);
        processState ();
    }

    protected void finalizeMatch ()
    {
        fireMatchFinishedEvent (this);
    }
    
    protected void clearHistory ()
    {
        historyBoards.clear();
        historyMoves.clear();
    }
    
    protected void setStartupBoard ()
    {
        board.setStartupPosition();
        fireMatchPositionChangedEvent (this);
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
        fireMatchTurnStartedEvent (this, side);
    }
    
    protected void finalizeTurn (byte side)
    {
        Clock turnClock = getTurnClock();
        sideToMove = Board.NOSIDE;
        if (turnClock != null)
            turnClock.stop();
        fireMatchTurnEndedEvent (this, side);
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
                fireMatchMoveEvent (this, move);                
                fireMatchPositionChangedEvent (this);
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
                fireMatchTakebackEvent (this, lastMove);
                fireMatchPositionChangedEvent (this);
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
                if (board.getSideToMove() == Board.WHITE)
                    setState(STATE_FINISHED_BLACKWIN);
                else
                    setState(STATE_FINISHED_WHITEWIN);
            }
            else if (board.inStaleMate())
            {
                setState(STATE_FINISHED_DRAW);
            }
        }
    }

    public void addMatchListener(MatchListener listener)
    {
        listeners.add(MatchListener.class, listener);
    }

    public void removeMatchListener(MatchListener listener)
    {
        listeners.remove(MatchListener.class, listener);
    }

    private void fireMatchStartedEvent (Match match)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchStarted(match);
    }
    
    private void fireMatchFinishedEvent (Match match)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchFinished(match);
    }
    
    private void fireMatchPositionChangedEvent (Match match)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchPositionChanged(match);
    }
    
    private void fireMatchTurnStartedEvent (Match match, byte side)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchTurnStarted(match, side);
    }
    
    private void fireMatchTurnEndedEvent (Match match, byte side)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchTurnEnded(match, side);
    }
    
    private void fireMatchMoveEvent (Match match, Move move)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchMove(match, move);
    }
    
    private void fireMatchTakebackEvent (Match match, Move move)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchTakeback(match, move);
    }
    
    private void fireMatchStateChangedEvent (Match match, byte state)
    {
        for (MatchListener listener : listeners.getListeners(MatchListener.class))
            listener.onMatchStateChanged(match, state);
    }
    
    public interface MatchListener extends EventListener
    {
        public void onMatchStarted (Match match);
        public void onMatchFinished (Match match);
        public void onMatchPositionChanged (Match match);
        public void onMatchTurnStarted (Match match, byte side);
        public void onMatchTurnEnded (Match match, byte side);
        public void onMatchMove (Match match, Move move);
        public void onMatchTakeback (Match match, Move move);
        public void onMatchStateChanged (Match match, byte state);
    }
}
