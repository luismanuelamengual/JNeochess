
package org.neochess.engine;

import org.neochess.engine.openingbooks.OpeningBook;
import org.neochess.engine.searchagents.SearchAgent;

public class ComputerPlayer extends Player
{
    private OpeningBook openingBook;
    private SearchAgent searchAgent;

    public ComputerPlayer (SearchAgent searchAgent)
    {
        this(searchAgent, null);
    }

    public ComputerPlayer (SearchAgent searchAgent, OpeningBook openingBook)
    {
        this.openingBook = openingBook;
        this.searchAgent = searchAgent;
    }

    @Override
    public void dispose ()
    {
        if (openingBook != null)
        {
            openingBook.dispose();
            openingBook = null;
        }
        if (searchAgent != null)
        {
            searchAgent.dispose();
            searchAgent = null;
        }
        super.dispose();
    }
    
    public synchronized Move startMoveSearch (Match match)
    {
        Move bestMove = null;
        Board board = match.getBoard();
        int movesMade = match.getPly() / 2;
        
        if (openingBook != null && movesMade <= 10)
            bestMove = openingBook.getMove(board);
        
        if (bestMove == null)
        {
            long searchTime = 30000;
            Clock clock = match.getTurnClock();
            if (clock != null)
            {
                long remainingTime = clock.getRemainingTime();
                if (remainingTime < 120000)
                {
                    searchTime = remainingTime / 40;
                    searchTime = Math.max(searchTime, 500);
                }
                else
                {
                    long elapsedTime = clock.getTime() - remainingTime;
                    long availiableTime = 80 * clock.getTime() / 100;
                    int maxMoveLimit = 40;
                    if (movesMade < maxMoveLimit && elapsedTime < availiableTime)
                    {
                        int moveLimit = maxMoveLimit - movesMade;
                        long timeLimit = availiableTime - elapsedTime;
                        searchTime = timeLimit / moveLimit;
                    }
                    else
                    {
                        searchTime = elapsedTime / movesMade;
                        
                        Clock oppositeClock = match.getClock(Board.getOppositeSide(match.getSideToMove()));
                        if (oppositeClock != null)
                        {
                            long timeDifference = remainingTime - oppositeClock.getRemainingTime();
                            if (timeDifference > 0)
                                searchTime += 20 * timeDifference / 100;
                            else
                                searchTime = 80 * searchTime / 100;
                        }
                        if (clock.getIncrement() > 0 && clock.getIncrement() < (remainingTime-searchTime))
                            searchTime += clock.getIncrement();
                    }
                    searchTime = Math.max(searchTime, 2000);
                }
            }
            bestMove = searchAgent.startSearch(board, searchTime);
        }
        return bestMove;
    }
    
    public void stopMoveSearch ()
    {
        searchAgent.stopSearch();
    }
}
