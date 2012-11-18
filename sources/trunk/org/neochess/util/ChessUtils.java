
package org.neochess.util;

import java.util.List;
import org.neochess.engine.Board;
import org.neochess.engine.evaluators.DefaultEvaluator;
import org.neochess.engine.evaluators.Evaluator;
import org.neochess.engine.openingbooks.DefaultOpeningBook;
import org.neochess.engine.openingbooks.OpeningBook;
import org.neochess.engine.searchagents.DefaultSearchAgent;
import org.neochess.engine.searchagents.SearchAgent;

public abstract class ChessUtils
{
    public static int evaluateBoard (Board board)
    {
        Evaluator evaluator = new DefaultEvaluator();
        int score = evaluator.evaluate(board);
        evaluator.dispose();
        return score;
    }
    
    public static int evaluateBoard (Board board, Evaluator evaluator)
    {
        return evaluator.evaluate(board);
    }

    public static int getOpeningBookMove (Board board)
    {
        OpeningBook book = new DefaultOpeningBook();
        int move = book.getMove(board);
        book.dispose();
        return move;
    }

    public static List<Integer> getOpeningBookPossibleMoves (Board board)
    {
        DefaultOpeningBook book = new DefaultOpeningBook();
        List<Integer> moves = book.getMoves(board);
        book.dispose();
        return moves;
    }

    public static int getBestMove (Board board)
    {
        return getBestMove (board, 10000);
    }
    
    public static int getBestMove (Board board, long searchMilliseconds)
    {
        SearchAgent agent = new DefaultSearchAgent();
        OpeningBook book = new DefaultOpeningBook();
        int bestMove = getBestMove (board, agent, book, searchMilliseconds);
        agent.dispose();
        book.dispose();
        return bestMove;
    }
    
    public static int getBestMove (Board board, SearchAgent agent, OpeningBook openingBook, long searchMilliseconds)
    {
        int bestMove = -1;
        if (openingBook != null)
            bestMove = openingBook.getMove(board);
        if (bestMove == -1)
            bestMove = agent.startSearch(board, searchMilliseconds);
        return bestMove;
    }
}
