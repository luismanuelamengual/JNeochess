
package org.neochess.engine.searchagents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.neochess.engine.Board;
import org.neochess.engine.Move;
import org.neochess.engine.evaluators.DefaultEvaluator;
import org.neochess.engine.evaluators.Evaluator;
import org.neochess.util.BoardUtils;

public class DefaultSearchAgent extends SearchAgent
{
    protected final static int MATE = 32000;
    protected final static int INFINITY = 32000;
    protected final static int ASPIRATIONWINDOW_SIZE = 80;
    protected final static int ASPIRATIONWINDOW_TOLERANCE = 255;
    protected final static int MAX_DEPTH = 64;
    protected final static int[] PIECEVALUE = {100, 350, 350, 550, 1100, 10000};

    private Evaluator evaluator;
    private Board searchBoard;
    private boolean searching;
    private boolean searchStopped;
    private long searchMilliseconds;
    private long searchStartMilliseconds;
    private int searchHistory[][];
    private Map<Integer, List<Move>> searchMoves;
    private int searchIteration;
    private Move searchMove;
    protected Move pv[][];
    protected int pv_length[];

    public DefaultSearchAgent ()
    {
        evaluator = new DefaultEvaluator();
        searching = false;
        searchMoves = new HashMap<Integer, List<Move>>();
        searchHistory = new int[64][64];
        pv_length = new int[MAX_DEPTH];
        pv = new Move[MAX_DEPTH][MAX_DEPTH];
    }

    @Override
    public Move startSearch(Board board, long searchMilliseconds)
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
        this.searchMove = null;
        this.searchMoves.clear();
        this.searchBoard = board.clone();
        this.searchMilliseconds = searchMilliseconds;
        this.searchStopped = false;
        this.searchIteration = 0;
        for (byte source = 0; source < 64; source++)
            for (byte destination = 0; destination < 64; destination++)
                searchHistory[source][destination] = 0;
        for (int depth1 = 0; depth1 < MAX_DEPTH; depth1++)
        {
            pv_length[depth1] = 0;
            for (int depth2 = 0; depth2 < MAX_DEPTH; depth2++)
                pv[depth1][depth2] = null;
        }
        searchStartMilliseconds = System.currentTimeMillis();
        this.searchBoard.getLegalMoves(getMoveList());
    }

    protected void finalizeSearch ()
    {
        this.searching = false;
    }
    
    protected List<Move> getMoveList ()    
    {
        return getMoveList(0);
    }
    
    protected List<Move> getMoveList (int ply)
    {
        if (searchMoves.get(ply) == null)
            searchMoves.put(ply, new ArrayList<Move>());
        return searchMoves.get(ply);
    }
    
    protected boolean isTimeUp ()
    {
        return searchStopped || ((System.currentTimeMillis() - searchStartMilliseconds) >= searchMilliseconds);
    }

    protected Move getSearchMove ()
    {
        Move bestMove = searchMove;
        if (bestMove == null)
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

    protected void updatePrincipalVariation (int ply, Move move)
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
            mainline += pv[0][j] + " ";
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
        List<Move> moves = getMoveList(ply);
        if (ply > 0)
        {
            moves.clear();
            board.getPseudoLegalMoves(moves);
        }
        if (moves.size() == 0)
            return -MATE + ply - 2;

        //Ponderación y Ordenamiento de movimientos
        if (ply > 0)
            ponderMoves(moves);
        sortMoves(moves);

        //Iterar sobre los movimientos posibles
        for (Move testMove : moves)
        {
            board.makeMove(testMove);
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
            testMove.setScore(searchResult);
            board.unmakeMove(testMove);
            
            if (searchIteration > 1 && isTimeUp())
                return -MATE;

            if (searchResult > alpha)
            {
                foundPV = true;
                alpha = searchResult;
                updatePrincipalVariation (ply, testMove);
                searchHistory[testMove.getInitialSquare()][testMove.getEndSquare()] += ply*ply;
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
        List<Move> moves = getMoveList(ply);
        moves.clear();
        board.getCaptureMoves(moves);
        if (moves.size() == 0) 
            return quiescResult;

        //Ponderación y Ordenamiento de movimientos
        ponderCaptureMoves(moves);
        sortMoves(moves);
        
        //Iterar sobre los movimientos posibles
        for (Move testMove : moves)
        {
            board.makeMove(testMove);
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
            board.unmakeMove(testMove);
            
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

    protected void ponderMoves (List<Move> moves)
    {
        for (Move testMove : moves)
        {
            int score = 0;
            byte initialSquare = testMove.getInitialSquare();
            byte endSquare = testMove.getEndSquare();
            byte capturedFigure = endSquare == searchBoard.getEnPassantSquare()? Board.PAWN : searchBoard.getSquareFigure(endSquare);
            if (capturedFigure != Board.EMPTY)
            {
                byte sourceFigure = searchBoard.getSquareFigure(initialSquare);
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
    
    protected void ponderCaptureMoves (List<Move> moves)
    {
        for (Move testMove : moves)
        {
            byte initialSquare = testMove.getInitialSquare();
            byte endSquare = testMove.getEndSquare();
            int sourceValue = PIECEVALUE[searchBoard.getSquareFigure(initialSquare)];
            int destinationValue = 0;
            
            if (searchBoard.getSquareFigure(initialSquare) == Board.PAWN && endSquare == searchBoard.getEnPassantSquare())
            {
                destinationValue = PIECEVALUE[Board.PAWN];
            }
            else
            {
                byte endSquareFigure = searchBoard.getSquareFigure(endSquare);
                destinationValue = PIECEVALUE[(endSquareFigure == Board.EMPTY)? Board.QUEEN : endSquareFigure];
            }
            if (sourceValue <= destinationValue)
            {
                testMove.setScore(destinationValue - sourceValue);
            }
            else
            {
                int swapOffValue = SEE(testMove);
                testMove.setScore(swapOffValue < 0? -INFINITY : swapOffValue);
            }
        }
    }
    
    protected void sortMoves (List<Move> moves)
    {
        Collections.sort(moves, Collections.reverseOrder());
    }
    
    protected int SEE (Move move)
    {
        byte f, t, sq, figure, side, xside;
        int n, lastval;
        int[] swaplist = new int[20];
        long b, c, r;
        long[] d, e;
        long[][] pieces = searchBoard.getPieces();
        long[] friends = searchBoard.getFriends();
        byte nsq, nfigure;
        int dir;
        long a;

        f = move.getInitialSquare();
        t = move.getEndSquare();
        side = searchBoard.getSquareSide(f);
        xside = searchBoard.getOppositeSide(side);
        b = searchBoard.getSquareAttackers(t, side);
        c = searchBoard.getSquareAttackers(t, xside);
        b &= BoardUtils.squareBitX[f];
        
        if (BoardUtils.sliderX[searchBoard.getSquareFigure(f)] == 1)
        {
            dir = BoardUtils.directions[t][f];
            a = BoardUtils.ray[f][dir] & searchBoard.getBlocker();
            if (a != 0)
            {
                nsq = (byte)(t < f ? BoardUtils.getLeastSignificantBit(a) : BoardUtils.getMostSignificantBit(a));
                nfigure = searchBoard.getSquareFigure(nsq);
                if ((nfigure == Board.QUEEN) || (nfigure == Board.ROOK && dir > 3) || (nfigure == Board.BISHOP && dir < 4))
                {
                    if ((BoardUtils.squareBit[nsq] & friends[side]) != 0)
                        b |= BoardUtils.squareBit[nsq];
                    else
                        c |= BoardUtils.squareBit[nsq];
                }
            }
        }

        d = pieces[side];
        e = pieces[xside];
        swaplist[0] = ((searchBoard.getSquareFigure(f) == Board.PAWN && t == searchBoard.getEnPassantSquare())? PIECEVALUE[Board.PAWN] : PIECEVALUE[searchBoard.getSquareFigure(t)]);
        lastval = -PIECEVALUE[searchBoard.getSquareFigure(f)];
        n = 1;
        while (true)
        {
            if (c == 0)
            {
                break;
            }
            for (figure = Board.PAWN; figure <= Board.KING; figure++)
            {
                r = c & e[figure];
                if (r != 0)
                {
                    sq = (byte)BoardUtils.getLeastSignificantBit(r);
                    c &= BoardUtils.squareBitX[sq];
                    if (BoardUtils.sliderX[figure] == 1)
                    {
                        dir = BoardUtils.directions[t][sq];
                        a = BoardUtils.ray[sq][dir] & searchBoard.getBlocker();
                        if (a != 0)
                        {
                            nsq = (byte)(t < sq ? BoardUtils.getLeastSignificantBit(a) : BoardUtils.getMostSignificantBit(a));
                            nfigure = searchBoard.getSquareFigure(nsq);
                            if ((nfigure == Board.QUEEN) || (nfigure == Board.ROOK && dir > 3) || (nfigure == Board.BISHOP && dir < 4))
                            {
                                if ((BoardUtils.squareBit[nsq] & friends[xside]) != 0)
                                    c |= BoardUtils.squareBit[nsq];
                                else
                                    b |= BoardUtils.squareBit[nsq];
                            }
                        }
                    }
                    swaplist[n] = swaplist[n - 1] + lastval;
                    n++;
                    lastval = PIECEVALUE[figure];
                    break;
                }
            }

            if (b == 0)
            {
                break;
            }
            for (figure = Board.PAWN; figure <= Board.KING; figure++)
            {
                r = b & d[figure];
                if (r != 0)
                {
                    sq = (byte)BoardUtils.getLeastSignificantBit(r);
                    b &= BoardUtils.squareBitX[sq];
                    if (BoardUtils.sliderX[figure] == 1)
                    {
                        dir = BoardUtils.directions[t][sq];
                        a = BoardUtils.ray[sq][dir] & searchBoard.getBlocker();
                        if (a != 0)
                        {
                            nsq = (byte)(t < sq ? BoardUtils.getLeastSignificantBit(a) : BoardUtils.getMostSignificantBit(a));
                            nfigure = searchBoard.getSquareFigure(nsq);
                            if ((nfigure == Board.QUEEN) || (nfigure == Board.ROOK && dir > 3) || (nfigure == Board.BISHOP && dir < 4))
                            {
                                if ((BoardUtils.squareBit[nsq] & friends[side]) != 0)
                                    b |= BoardUtils.squareBit[nsq];
                                else
                                    c |= BoardUtils.squareBit[nsq];
                            }
                        }
                    }
                    swaplist[n] = swaplist[n - 1] + lastval;
                    n++;
                    lastval = -PIECEVALUE[figure];
                    break;
                }
            }
        }

        --n;
        while (n > 0)
        {
            if ((n & 1) != 0)
            {
                if (swaplist[n] <= swaplist[n - 1])
                    swaplist[n - 1] = swaplist[n];
            }
            else
            {
                if (swaplist[n] >= swaplist[n - 1])
                    swaplist[n - 1] = swaplist[n];
            }
            --n;
        }
        return (swaplist[0]);
    }
}
