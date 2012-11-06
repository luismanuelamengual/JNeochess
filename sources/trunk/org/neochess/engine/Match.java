
package org.neochess.engine;

import java.util.ArrayList;
import java.util.List;
import org.neochess.engine.Board.Move;
import org.neochess.general.Disposable;

public class Match implements Disposable
{
    public static final byte STATE_NOTSTARTED = 0;
    public static final byte STATE_PLAYING = 1;
    public static final byte STATE_FINISHED_DRAW = 2;
    public static final byte STATE_FINISHED_WHITEWIN = 3;
    public static final byte STATE_FINISHED_BLACKWIN = 4;
    
    private List<Board> historyBoards;
    private List<Move> historyMoves;
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private Clock whiteClock;
    private Clock blackClock;
    private byte state;
    
    public Match ()
    {
        historyBoards = new ArrayList<Board>();
        historyMoves = new ArrayList<Move>();
        board = new Board();
    }
    
    public void dispose ()
    {
        for (Board historyBoard : historyBoards)
            historyBoard.dispose();
        historyBoards.clear();
        historyBoards = null;
        historyMoves.clear();
        historyMoves = null;
        board.dispose();
        board = null;
        if (whitePlayer != null)
        {
            whitePlayer.dispose();
            whitePlayer = null;
        }
        if (blackPlayer != null)
        {
            blackPlayer.dispose();
            blackPlayer = null;
        }
        if (whiteClock != null)
        {
            whiteClock.dispose();
            whiteClock = null;
        }
        if (blackClock != null)
        {
            blackClock.dispose();
            blackClock = null;
        }
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
        return getPlayer(getSideToMove());
    }
    
    public byte getSideToMove ()
    {
        return board.getSideToMove();
    }
    
    public int getPly ()
    {
        return historyBoards.size();
    }
    
    public void setState (byte state)
    {
        this.state = state;
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
    
    public Clock getClock (byte side)
    {
        return (side == Board.WHITE)? whiteClock : blackClock;
    }
    
    public Clock getTurnClock ()
    {
        return getClock(getSideToMove());
    }
    
    public long getRemainingTime (byte side)
    {
        Clock clock = getClock(side);
        return (clock != null)? clock.getRemainingTime() : -1;
    }
    
    public boolean isTimeUp (byte side)
    {
        return (getRemainingTime(side) == 0);
    }
    
    protected synchronized void clearHistory ()
    {
        historyBoards.clear();
        historyMoves.clear();
    }
    
    protected synchronized void setStartupBoard ()
    {
        board.setStartupPosition();
    }
    
    protected synchronized void clearBoard ()
    {
        board.clear();
    }
    
    protected synchronized void stopClocks ()
    {
        if (whiteClock != null)
            whiteClock.stop();
        if (blackClock != null)
            blackClock.stop();
    }
    
    protected synchronized void resetClocks ()
    {
        stopClocks();
        if (whiteClock != null)
            whiteClock.reset();
        if (blackClock != null)
            blackClock.reset();
    }
    
    public synchronized void reset ()
    {
        clearBoard();
        clearHistory();
        resetClocks();
        setState(STATE_NOTSTARTED);
    }
    
    public synchronized void start ()
    {
        reset();
        setStartupBoard();
        setState(STATE_PLAYING);
        if (whiteClock != null)
            whiteClock.start();
    }
    
    public boolean isDrawByRepetition ()
    {
        boolean isDrawn = false;
        int counter = 1;
        long actualHash = board.getHash();
        if (historyBoards.size() > 1)
        {
            for(int i = historyBoards.size() - 2; i >= Math.max(0, historyBoards.size() - 50); i--)
            {
                Board boardToTest = historyBoards.get(i);
                if (boardToTest.getHash() == actualHash)
                {
                    counter++;
                    if (counter == 3)
                    {
                        isDrawn = true;
                        break;
                    }   
                }
            }
        }
        return isDrawn;
    }
    
    public synchronized boolean makeMove (Move move)
    {
        boolean moveMade = false;
        updateState ();
        if (state == STATE_PLAYING)
        {
            if (move != null && board.isMoveValid(move))
            {
                byte sideMoving = getSideToMove();
                historyMoves.add(move);
                historyBoards.add(board.clone());
                board.makeMove(move);
                Clock sideClock = getClock(sideMoving);
                if (sideClock != null)
                {
                    sideClock.stop();
                    sideClock.addIncrement();
                }
                updateState();
                if (state == STATE_PLAYING)
                {
                    Clock oppositeSideClock = getClock(getSideToMove());
                    if (oppositeSideClock != null)
                        oppositeSideClock.start();
                }
                moveMade = true;
            }
        }
        return moveMade;
    }

    public synchronized boolean unmakeMove ()
    {
        boolean moveUnmade = false;
        updateState ();
        if (state == STATE_PLAYING)
        {
            if (historyBoards.size() > 0)
            {
                byte sideMoving = getSideToMove();
                Board lastHistoryBoard = historyBoards.get(historyBoards.size() - 1);
                Move lastMove = historyMoves.get(historyMoves.size() - 1);
                board.copy(lastHistoryBoard);
                historyBoards.remove(lastHistoryBoard);
                historyMoves.remove(lastMove);
                lastHistoryBoard.dispose();
                Clock sideClock = getClock(sideMoving);
                if (sideClock != null)
                    sideClock.stop();
                updateState ();
                Clock oppositeSideClock = getClock(getSideToMove());
                if (oppositeSideClock != null)
                    oppositeSideClock.start();
                moveUnmade = true;
            }
        }
        return moveUnmade;
    }
    
    public void updateState ()
    {
        if (state == STATE_PLAYING)
        {
            if (board.inCheckMate())
            {
                setState((getSideToMove() == Board.WHITE)? STATE_FINISHED_BLACKWIN : STATE_FINISHED_WHITEWIN); 
                stopClocks ();
            }
            else if (board.inStaleMate())
            {
                setState(STATE_FINISHED_DRAW);
                stopClocks ();
            }
            else if (isDrawByRepetition())
            {
                setState(STATE_FINISHED_DRAW);
                stopClocks ();
            }
            else if (isTimeUp(getSideToMove()))
            {
                setState((getSideToMove() == Board.WHITE)? STATE_FINISHED_BLACKWIN : STATE_FINISHED_WHITEWIN);
                stopClocks ();
            }
        }
    }
}
