
package org.neochess.util;

import java.util.List;
import org.neochess.engine.Board;
import org.neochess.engine.Board.Move;
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

    public static Move getOpeningBookMove (Board board)
    {
        OpeningBook book = new DefaultOpeningBook();
        Move move = book.getMove(board);
        book.dispose();
        return move;
    }

    public static List<Move> getOpeningBookPossibleMoves (Board board)
    {
        DefaultOpeningBook book = new DefaultOpeningBook();
        List<Move> moves = book.getMoves(board);
        book.dispose();
        return moves;
    }

    public static Move getBestMove (Board board)
    {
        return getBestMove (board, 10000);
    }
    
    public static Move getBestMove (Board board, long searchMilliseconds)
    {
        SearchAgent agent = new DefaultSearchAgent();
        OpeningBook book = new DefaultOpeningBook();
        Move bestMove = getBestMove (board, agent, book, searchMilliseconds);
        agent.dispose();
        book.dispose();
        return bestMove;
    }
    
    public static Move getBestMove (Board board, SearchAgent agent, OpeningBook openingBook, long searchMilliseconds)
    {
        Move bestMove = null;
        if (openingBook != null)
            bestMove = openingBook.getMove(board);
        if (bestMove == null)
            bestMove = agent.startSearch(board, searchMilliseconds);
        return bestMove;
    }
}
