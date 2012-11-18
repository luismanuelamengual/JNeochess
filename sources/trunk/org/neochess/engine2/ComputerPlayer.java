
package org.neochess.engine2;

import org.neochess.engine2.openingbooks.OpeningBook;
import org.neochess.engine2.searchagents.SearchAgent;

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
    
    public synchronized int startMoveSearch (Match match)
    {
        int bestMove = -1;
        Board board = match.getBoard();
        int movesMade = match.getPly() / 2;
        
        if (openingBook != null && movesMade <= 10)
            bestMove = openingBook.getMove(board);
        
        if (bestMove == -1)
        {
            Clock clock = match.getTurnClock();
            long searchTime = 30000;
            if (clock != null)
            {
                long remainingTime = clock.getRemainingTime();
                if (remainingTime > 60000)
                {
                    if (movesMade < 30)
                    {
                        long availiableTime = 75 * remainingTime / 100;
                        int movesLimit = 30 - movesMade;
                        searchTime = availiableTime / movesLimit;
                    }
                    else
                    {
                        searchTime = remainingTime / 15;
                    }
                }
                else
                {
                    searchTime = remainingTime / 20;
                }
                
                Clock oppositeClock = match.getClock(Board.getOppositeSide(match.getSideToMove()));
                if (oppositeClock != null)
                {
                    long timeDifference = remainingTime - oppositeClock.getRemainingTime();
                    if (timeDifference > 0)
                        searchTime += 20 * timeDifference / 100;
                    else if (timeDifference < -30000)
                        searchTime = 80 * searchTime / 100;
                }
                if (clock.getIncrement() > 0 && clock.getIncrement() < (remainingTime-searchTime))
                    searchTime += clock.getIncrement();
                
                if (searchTime < 50)
                    searchTime = 50;
                searchTime = (95 * searchTime) / 100;
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
