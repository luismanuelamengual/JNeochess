
package org.neochess.engine.searchagents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neochess.engine.Board;
import org.neochess.engine.evaluators.DefaultEvaluator;
import org.neochess.engine.evaluators.Evaluator;

public class DefaultSearchAgent extends SearchAgent
{
    protected final static int MATE = 32000;
    protected final static int INFINITY = 32000;
    protected final static int ASPIRATIONWINDOW_SIZE = 80;
    protected final static int ASPIRATIONWINDOW_TOLERANCE = 255;
    protected final static int MAX_DEPTH = 64;
    protected final static int[] PIECEVALUE = {100, 300, 315, 500, 950, 10000};

    private Evaluator evaluator;
    private Board searchBoard;
    private boolean searching;
    private boolean searchStopped;
    private long searchMilliseconds;
    private long searchStartMilliseconds;
    private int searchHistory[][];
    private Map<Integer, List<Integer>> searchMoves;
    private int searchIteration;
    private int searchMove;
    protected int pv[][];
    protected int pv_length[];

    public DefaultSearchAgent ()
    {
        evaluator = new DefaultEvaluator();
        searching = false;
        searchMoves = null;
        searchHistory = new int[64][64];
        pv_length = new int[MAX_DEPTH];
        pv = new int[MAX_DEPTH][MAX_DEPTH];
    }

    @Override
    public int startSearch(Board board, long searchMilliseconds)
    {
        initializeSearch(board, searchMilliseconds);
        startSearchIterations();
        finalizeSearch();
        return getSearchMove();
    }

    @Override
    public void stopSearch()
    {
        searchStopped = true;
    }

    @Override
    public boolean isSearching()
    {
        return searching;
    }

    protected void initializeSearch (Board board, long searchMilliseconds)
    {
        this.searching = true;
        this.searchMove = -1;
        this.searchBoard = board;
        this.searchMilliseconds = searchMilliseconds;
        this.searchStopped = false;
        this.searchMoves = new HashMap<Integer, List<Integer>>();
        this.searchIteration = 0;
        for (byte source = 0; source < 64; source++)
            for (byte destination = 0; destination < 64; destination++)
                searchHistory[source][destination] = 0;
        for (int depth1 = 0; depth1 < MAX_DEPTH; depth1++)
        {
            pv_length[depth1] = 0;
            for (int depth2 = 0; depth2 < MAX_DEPTH; depth2++)
                pv[depth1][depth2] = -1;
        }
        searchStartMilliseconds = System.currentTimeMillis();
        board.getLegalMoves(getMoveList());
    }

    protected void finalizeSearch ()
    {
        this.searching = false;
    }
    
    protected List<Integer> getMoveList ()    
    {
        return getMoveList(0);
    }
    
    protected List<Integer> getMoveList (int ply)
    {
        if (searchMoves.get(ply) == null)
            searchMoves.put(ply, new ArrayList<Integer>());
        return searchMoves.get(ply);
    }

    protected boolean isTimeUp ()
    {
        return searchStopped || ((System.currentTimeMillis() - searchStartMilliseconds) >= searchMilliseconds);
    }

    protected int getSearchMove ()
    {
        int bestMove = searchMove;
        if (bestMove == -1)
            bestMove = getMoveList().get(0);
        return bestMove;
    }

    protected int evaluateBoard (Board board)
    {
        int score = evaluator.evaluate(board);
        if (board.getSideToMove() == Board.BLACK)
            score = -score;
        return score;
    }

    protected void preparePrincipalVariation (int ply)
    {
        pv_length[ply] = ply;
    }

    protected void updatePrincipalVariation (int ply, int move)
    {
        pv[ply][ply] = move;
        for (int j = ply + 1; j < pv_length[ply + 1]; j++)
            pv[ply][j] = pv[ply + 1][j];
        pv_length[ply] = pv_length[ply + 1];
    }

    protected String getMainLine()
    {
        String mainline = "";
        for ( int j = 0; j < pv_length[0]; ++j ) 
            mainline += Board.getMoveString(pv[0][j]) + " ";
        return mainline;
    }

    protected void startSearchIterations ()
    {
        int rootAlpha = Integer.MIN_VALUE;
        int rootBeta = Integer.MAX_VALUE;
        int rootSearchResult = evaluateBoard(searchBoard);
        searchIteration = 0;

        do
        {
            searchIteration++;
            
            if (rootSearchResult > (MATE-ASPIRATIONWINDOW_TOLERANCE))
            {
                rootAlpha = rootSearchResult - 1;
                rootBeta = MATE;
            }
            else if (rootSearchResult < (-MATE+ASPIRATIONWINDOW_TOLERANCE))
            {
                rootAlpha = -MATE;
                rootBeta = rootSearchResult + 1;
            }
            else
            {
                rootAlpha = Math.max (rootSearchResult - ASPIRATIONWINDOW_SIZE, -MATE);
                rootBeta = Math.min (rootSearchResult + ASPIRATIONWINDOW_SIZE, MATE);
            }

            rootSearchResult = alphaBetaSearch (searchBoard, rootAlpha, rootBeta, searchIteration, 0);
            if (!isTimeUp())
            {
                if (rootSearchResult >= rootBeta && rootSearchResult < MATE)
                {
                    rootAlpha = rootBeta;
                    rootBeta = Integer.MAX_VALUE;
                    rootSearchResult = alphaBetaSearch (searchBoard, rootAlpha, rootBeta, searchIteration, 0);
                }
                else if (rootSearchResult <= rootAlpha)
                {
                    rootBeta = rootAlpha;
                    rootAlpha = Integer.MIN_VALUE;
                    rootSearchResult = alphaBetaSearch (searchBoard, rootAlpha, rootBeta, searchIteration, 0);
                }
            }
        } while (!isTimeUp());
    }

    protected int alphaBetaSearch (Board board, int alpha, int beta, int depth, int ply)
    {
        int searchResult;
        boolean foundPV = false;

        //Chequear que el lado a jugar tenga el Rey
        if (board.getKingSquare(board.getSideToMove()) == Board.INVALIDSQUARE)
            return (-MATE + ply - 2);
        
        //Verificar si ya hemos llegado al limite de busqueda
        if (depth == 0)
            return quiescentSearch (board, alpha, beta, ply);
        
        //Preparar la Linea de Variacion Principal
        preparePrincipalVariation (ply);

        //Generar los movimientos
        List<Integer> moves = getMoveList(ply);
        if (ply > 0)
        {
            moves.clear();
            board.getPseudoLegalMoves(moves);
        }
        if (moves.size() == 0)
            return -MATE + ply - 2;

        //Ponderación y Ordenamiento de movimientos
        ponderMoves(board, ply, moves);
        sortMoves(board, ply, moves);

        //Iterar sobre los movimientos posibles
        for (int testMove : moves)
        {
            int moveMade = board.makeMove(testMove);
            if (foundPV)
            {
                searchResult = -alphaBetaSearch (board, -alpha - 1, -alpha, depth - 1, ply + 1);
                if ((searchResult > alpha) && (searchResult < beta))
                    searchResult = -alphaBetaSearch (board, -beta, -alpha, depth - 1, ply + 1);
            }
            else
            {
                searchResult = -alphaBetaSearch (board, -beta, -alpha, depth - 1, ply + 1);
            }
            board.unmakeMove(moveMade);
            
            if (searchIteration > 1 && isTimeUp())
                return -MATE;

            if (searchResult > alpha)
            {
                foundPV = true;
                alpha = searchResult;
                updatePrincipalVariation (ply, testMove);
                searchHistory[Board.getMoveInitialSquare(testMove)][Board.getMoveEndSquare(testMove)] += ply*ply;
                if (ply == 0)
                    searchMove = testMove;
                if (alpha >= beta)
                    break;
            }
        }
        
        return alpha;
    }
    
    protected int quiescentSearch (Board board, int alpha, int beta, int ply)
    {
        int quiescResult;
        byte sideToMove = board.getSideToMove();
        boolean foundPV = false;
        boolean inCheck = board.inCheck();
        
        //Chequear que el lado a jugar tenga el Rey
        if (board.getKingSquare(sideToMove) == Board.INVALIDSQUARE)
             return (-MATE + ply - 2);
        
        //Funcion de evaluacion
        quiescResult = evaluateBoard(board);
        if (quiescResult >= beta && !inCheck) return quiescResult;
        if (quiescResult > alpha) alpha = quiescResult;
        
        //Prepare principalVariation
        preparePrincipalVariation (ply);
        
        //Generacion de movimientos
        List<Integer> moves = getMoveList(ply);
        moves.clear();
        board.getCaptureMoves(moves);
        if (moves.size() == 0) 
            return quiescResult;

        //Ponderación y Ordenamiento de movimientos
        ponderCaptureMoves(board, ply, moves);
        sortMoves (board, ply, moves);
        
        //Iterar sobre los movimientos posibles
        for (int testMove : moves)
        {
            int moveMade = board.makeMove(testMove);
            if (foundPV) 
            {
                quiescResult = -quiescentSearch (board, -alpha - 1, -alpha, ply + 1);
                if ((quiescResult > alpha) && (quiescResult < beta)) 
                    quiescResult = -quiescentSearch (board, -beta, -alpha, ply + 1);
            } 
            else 
            {
                quiescResult = -quiescentSearch (board, -beta, -alpha, ply + 1);
            }
            board.unmakeMove(moveMade);
            
            if (quiescResult > alpha) 
            {
                foundPV = true;
                alpha = quiescResult;
                updatePrincipalVariation (ply, testMove);
                if (quiescResult >= beta) 
                    return quiescResult;
            }
        }
        return alpha;
    }

    protected void ponderMoves (Board board, int ply, List<Integer> moves)
    {
        if (ply > 0)
            establecer el score del ply 0 
        
        for (int testMove : moves)
        {
            int score = 0;
            byte initialSquare = Board.getMoveInitialSquare(testMove);
            byte endSquare = Board.getMoveEndSquare(testMove);
            byte capturedFigure = endSquare == board.getEnPassantSquare()? Board.PAWN : board.getSquareFigure(endSquare);
            if (capturedFigure != Board.EMPTY)
            {
                byte sourceFigure = board.getSquareFigure(initialSquare);
                int toValue = PIECEVALUE[capturedFigure];
                int fromValue = PIECEVALUE[sourceFigure];
                score += PIECEVALUE[capturedFigure] - PIECEVALUE[sourceFigure];
                if (toValue >= fromValue)
                    score += 100;
            }
            score += searchHistory[initialSquare][endSquare];
            testMove.setScore(score);
        }
    }
    
    protected void ponderCaptureMoves (Board board, int ply, List<Integer> moves)
    {
        for (int testMove : moves)
        {
            byte initialSquare = Board.getMoveInitialSquare(testMove);
            byte endSquare = Board.getMoveEndSquare(testMove);
            int sourceValue = PIECEVALUE[board.getSquareFigure(initialSquare)];
            int destinationValue = PIECEVALUE[endSquare == board.getEnPassantSquare()? Board.PAWN : board.getSquareFigure(endSquare)];
            testMove.setScore(destinationValue - sourceValue);
        }
    }
    
    protected void sortMoves (Board board, int ply, List<Integer> moves)
    {
        Collections.sort(moves, Collections.reverseOrder());
    }
}
