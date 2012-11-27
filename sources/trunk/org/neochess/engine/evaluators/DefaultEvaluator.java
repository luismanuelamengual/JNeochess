
package org.neochess.engine.evaluators;

import java.util.HashMap;
import java.util.Map;
import org.neochess.engine.Board;
import org.neochess.util.BoardUtils;

public class DefaultEvaluator extends Evaluator
{
    private final static int PHASENUMBER = 8;
    private final static int _isolani_normal[] = { 12, 10, 8, 6, 6, 8, 10, 12 };
    private final static int _isolani_weaker[] = { -2, -4, -6, -8, -8, -6, -4, -2 };  
    private final static long nn[] = { 0x4200000000000000L, 0x0000000000000042L };
    private final static long bb[] = { 0x2400000000000000L, 0x0000000000000024L };
    private final static long _d2e2[] = { 0x0018000000000000L, 0x0000000000001800L };
    private final static long _brank7[] = { 0x000000000000FF00L, 0x00FF000000000000L };
    private final static long _brank8[] = { 0x00000000000000FFL, 0xFF00000000000000L };
    private final static long _brank67[] = { 0x0000000000FFFF00L, 0x00FFFF0000000000L };
    private final static long _brank58[] = { 0x00000000FFFFFFFFL, 0xFFFFFFFF00000000L };
    private final static long sideVertical[] = { 0xFFFFFFFF00000000L, 0x00000000FFFFFFFFL, };
    private final static long sideHorizontal[] = { 0xF0F0F0F0F0F0F0F0L, 0x0F0F0F0F0F0F0F0FL };
    private final static long BOX_01 = 0x00003C3C3C3C0000L;
    private final static long BOX_012 = 0x007E7E7E7E7E7E00L;
    private final static int _pawnCoverture[] = { -60, -30, 0, 5, 30, 30, 30, 30, 30 };
    private final static int _safetyFactor[] = { 7, 8, 8, 7, 6, 5, 4, 2, 0, };
    private final static int _rank7[] = { 6, 1 };
    private final static int _rank8[] = { 7, 0 };
    private static long _passedPawnMask[][] = new long[2][64];
    private static long _isolaniPawnMask[] = new long[8];
    private final static long _initialKnights[] = { BoardUtils.squareBit[Board.B1] | BoardUtils.squareBit[Board.G1], BoardUtils.squareBit[Board.B8] | BoardUtils.squareBit[Board.G8] };
    private final static long _initialBishops[] = { BoardUtils.squareBit[Board.C1] | BoardUtils.squareBit[Board.F1], BoardUtils.squareBit[Board.C8] | BoardUtils.squareBit[Board.F8] };
    private final static long _centerFiles = BoardUtils.fileBits[Board.FILE_D] | BoardUtils.fileBits[Board.FILE_E];
    private final static int _pawnSquareValue[][] = 
    {
        {  0,  0,  0,  0,  0,  0,  0,  0,
           5,  5,  5,-15,-15,  5,  5,  5,
          -2, -2, -2,  6,  6, -2, -2, -2,
           0,  0,  0, 25, 25,  0,  0,  0,
           2,  2, 12, 16, 16, 12,  2,  2,
           4,  8, 12, 16, 16, 12,  4,  4,
           4,  8, 12, 16, 16, 12,  4,  4,
           0,  0,  0,  0,  0,  0,  0,  0
        },
        {  0,  0,  0,  0,  0,  0,  0,  0,
           4,  8, 12, 16, 16, 12,  4,  4,
           4,  8, 12, 16, 16, 12,  4,  4,
           2,  2, 12, 16, 16, 12,  2,  2,
           0,  0,  0, 25, 25,  0,  0,  0,
          -2, -2, -2,  6,  6, -2, -2, -2,
           5,  5,  5,-15,-15,  5,  5,  5,
           0,  0,  0,  0,  0,  0,  0,  0
        }
    };
    
    private final static int _scoreKing[] =
    {
       24, 24, 24, 16, 16,  0, 32, 32,
       24, 20, 16, 12, 12, 16, 20, 24,
       16, 12,  8,  4,  4,  8, 12, 16,
       12,  8,  4,  0,  0,  4,  8, 12,
       12,  8,  4,  0,  0,  4,  8, 12,
       16, 12,  8,  4,  4,  8, 12, 16,
       24, 20, 16, 12, 12, 16, 20, 24,
       24, 24, 24, 16, 16,  0, 32, 32
    };

    private final static int _scoreKingFinalist[] =
    {
       0,  6, 12, 18, 18, 12,  6,  0,
       6, 12, 18, 24, 24, 18, 12,  6,
      12, 18, 24, 32, 32, 24, 18, 12,
      18, 24, 32, 48, 48, 32, 24, 18,
      18, 24, 32, 48, 48, 32, 24, 18,
      12, 18, 24, 32, 32, 24, 18, 12,
       6, 12, 18, 24, 24, 18, 12,  6,
       0,  6, 12, 18, 18, 12,  6,  0
    };
    
    private Map<String, Integer> scores;
    private long _passedPawns[] = new long[2];
    private long _weakedPawns[] = new long[2];
    private int _phase;
    
    public DefaultEvaluator ()
    {
        scores = new HashMap<String, Integer>();
        scores.put("SCORE_PAWN", 100);
        scores.put("SCORE_KNIGHT", 300);
        scores.put("SCORE_BISHOP", 300);
        scores.put("SCORE_ROOK", 500);
        scores.put("SCORE_QUEEN", 950);
        scores.put("SCORE_KING", 10000);
        scores.put("SCORE_MINORNOTDEVELOPED", -28);
        scores.put("SCORE_NOTCASTLED", -20);
        scores.put("SCORE_KINGMOVED", -20);
        scores.put("SCORE_EARLYQUEENMOVE", -80);
        scores.put("SCORE_EARLYMINORREPEAT", -15);
        scores.put("SCORE_EARLYCENTERPREPEAT", -12);
        scores.put("SCORE_EARLYWINGPAWNMOVE", -9);
        scores.put("SCORE_DOUBLEDPAWNS", -20);
        scores.put("SCORE_ISOLATEDPAWNS", -15);
        scores.put("SCORE_ALLPAWNS", -10);
        scores.put("SCORE_CENTERPAWNS", 17);
        scores.put("SCORE_BACKWARDPAWNS", -9);
        scores.put("SCORE_PASSEDPAWNS", 15);
        scores.put("SCORE_PAWNBASEATAK", -18);
        scores.put("SCORE_LOCKEDPAWNS", -10);
        scores.put("SCORE_ATAKWEAKPAWN", 8);
        scores.put("SCORE_DOUBLEDBISHOPS", 15);
        scores.put("SCORE_ROOKHALFFILE", 5);
        scores.put("SCORE_ROOKOPENFILE", 8);
        scores.put("SCORE_QUEENNOTPRESENT", -25);
        scores.put("SCORE_GOPEN", -30);
        scores.put("SCORE_HOPEN", -600); 
        scores.put("SCORE_KINGOPENFILE", -10);
        scores.put("SCORE_KINGENEMYOPENFILE", -6);
        scores.put("SCORE_KINGDEFENCEDEFICIT", -50);
        scores.put("SCORE_PAWNNEARKING", 40);
        scores.put("SCORE_BLOCKDEPAWNS", -40);
        scores.put("SCORE_ROOK7RANK", 30);
        scores.put("SCORE_RUPTURE", -20);
    }
    
    public void setScore (String key, int value)
    {
        scores.put(key, value);
    }
    
    public int getScore (String key)
    {
        return scores.get(key);
    }
    
    @Override
    public int evaluate (Board board) 
    {
        int materialWhite = evaluateMaterial(board, Board.WHITE);
        int materialBlack = evaluateMaterial(board, Board.BLACK);
        int originalMaterial = ((getScore("SCORE_PAWN")*16)+(getScore("SCORE_KNIGHT")*4)+(getScore("SCORE_BISHOP")*4)+(getScore("SCORE_ROOK")*4)+getScore("SCORE_QUEEN"))*4;
        int actualMaterial = materialWhite + materialBlack - (2*getScore("SCORE_KING"));
        _phase = PHASENUMBER - (int)(((double)actualMaterial * (double)PHASENUMBER) / (double)originalMaterial);
        _phase = Math.max(_phase, 0);
        _phase = Math.min(_phase, PHASENUMBER);

        int score = 0;
        score += (materialWhite - materialBlack);
        score += (evaluateDevelopment(board, Board.WHITE) - evaluateDevelopment(board, Board.BLACK));
        score += (evaluatePawns(board, Board.WHITE) - evaluatePawns(board, Board.BLACK));
        score += (evaluateKnights(board, Board.WHITE) - evaluateKnights(board, Board.BLACK));
        score += (evaluateBishops(board, Board.WHITE) - evaluateBishops(board, Board.BLACK));
        score += (evaluateRooks(board, Board.WHITE) - evaluateRooks(board, Board.BLACK));
        score += (evaluateQueens(board, Board.WHITE) - evaluateQueens(board, Board.BLACK));
        score += (evaluateKing(board, Board.WHITE) - evaluateKing(board, Board.BLACK));
        return score;
    }   
    
    private int evaluateMaterial (Board board, byte side)
    {
        int score = 0;
        for (byte square = Board.A1; square <= Board.H8; square++)
        {
            if (board.getSquareSide(square) == side)
            {
                switch (board.getSquareFigure(square))
                {
                    case Board.PAWN: score += getScore("SCORE_PAWN"); break;
                    case Board.KNIGHT: score += getScore("SCORE_KNIGHT"); break;
                    case Board.BISHOP: score += getScore("SCORE_BISHOP"); break;
                    case Board.ROOK: score += getScore("SCORE_ROOK"); break;
                    case Board.QUEEN: score += getScore("SCORE_QUEEN"); break;
                    case Board.KING: score += getScore("SCORE_KING"); break;
                    default: continue;
                }
            }
        }
        return score;
    }
    
    public int evaluateDevelopment (Board board, byte side)
    {
        int score = 0;
        int sq;
        long[][] pieces = board.getPieces();
        long movers = (pieces[side][Board.KNIGHT] & nn[side]) | (pieces[side][Board.BISHOP] & bb[side]);
        score = BoardUtils.getBitCount(movers) * -8;
        return score;
    }
    
    public int evaluatePawns (Board board, byte side)
    {
        byte square, testsquare, score = 0;
        int pawnCounter[] = new int[8];
        int xside = Board.getOppositeSide(side);
        long sidePieces = board.getFriends()[side];
        long xsidePieces = board.getFriends()[xside];
        long sidePawns = board.getPieces()[side][Board.PAWN];
        long xsidePawns = board.getPieces()[xside][Board.PAWN];
        long pawnMoves;
        long movers = sidePawns;
        
        _passedPawns[side] = 0;
        _weakedPawns[side] = 0;
        
        while (movers != 0) 
        {
            square = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[square];
            score += _pawnSquareValue[side][square];

            //Verificar si es un peon pasado
            if ((xsidePawns & _passedPawnMask[side][square]) == 0)
            {
                if ( (side == Board.WHITE && (BoardUtils.fromtoRay[square][square|56] & sidePawns) == 0) || (side == Board.BLACK && (BoardUtils.fromtoRay[square][square&7] & sidePawns) == 0)) 
                {
                    _passedPawns[side] |= BoardUtils.squareBit[square];
                    score += (getScore("SCORE_PASSEDPAWNS") * ((side == Board.WHITE)? board.getSquareRank(square) : (7-board.getSquareRank(square)) ) * _phase) / 12;
                }
            }
            
            //Verificar si es un peon debil
            testsquare = (byte)(square + (side == Board.WHITE ? 8 : -8));
            if (((_passedPawnMask[xside][testsquare] & ~BoardUtils.fileBits[Board.getSquareFile(square)] & sidePawns) == 0) && board.getSquareFigure(testsquare) != Board.PAWN)
            {
                int nbits1 = BoardUtils.getBitCount(sidePawns & BoardUtils.moveArray[xside == Board.WHITE? Board.PAWN:Board.BPAWN][testsquare]);
                int nbits2 = BoardUtils.getBitCount(xsidePawns & BoardUtils.moveArray[side == Board.WHITE? Board.PAWN:Board.BPAWN][testsquare]);
                if (nbits1 < nbits2) 
                {
                    _weakedPawns[side] |= BoardUtils.squareBit[square];
                    score += getScore("SCORE_BACKWARDPAWNS");
                }
            }
            
            //Ataque al peon base
            pawnMoves = ncBoardContext.getSliceBitBoard(ncBoardContext.getPawnSideType(side), square).value;
            if ( ( ( pawnMoves & sidePawns ) != 0 ) && ( ( pawnMoves & xsidePawns ) != 0 ) )
                score += _score[SCORE_PAWNBASEATAK];
            
            //Incrementar la cantidad de peones de la columna
            pawnCounter[ ncBoard.getSquareFile(square) ]++;
        }
        
        for ( int fileindex = 0; fileindex <= 7; fileindex++ )
	{
            //Peones doblados
            if (pawnCounter[fileindex] > 1) score += _score[SCORE_DOUBLEDPAWNS];
            
            //Peones isolados
            if ((pawnCounter[fileindex] > 0) && ((sidePawns & _isolaniPawnMask[fileindex]) == ncBitBoard.NULLBITBOARD))
            {
                if (( ncBitBoard.fileBit[fileindex] & xsidePawns) == ncBitBoard.NULLBITBOARD) 
                    score += (_score[SCORE_ISOLATEDPAWNS] + _isolani_weaker[fileindex]) * pawnCounter[fileindex];
                else 
                    score += (_score[SCORE_ISOLATEDPAWNS] + _isolani_normal[fileindex]) * pawnCounter[fileindex];
                _weakedPawns[sideOffset] |= (sidePawns & ncBitBoard.fileBit[fileindex]);
            }
	}
        
        //Favorecer el tener peones en el centro
        score += ncBitBoard.getBitCount(sidePawns&_centerFiles) * _score[SCORE_CENTERPAWNS];
        
        if ( _computerSide == side )
        {
            //Penalizar tener 8 peones
            if (ncBitBoard.getBitCount(sidePawns) == 8) score += _score[SCORE_ALLPAWNS];

            //Tener peones bloqueados
            int counter = 0;
            if (side == ncGlobals.WHITE) {
                counter = ncBitBoard.getBitCount( (sidePawns >>> 8) & xsidePawns & BOX_012 );
            }
            else {
                counter = ncBitBoard.getBitCount( (sidePawns << 8) & xsidePawns & BOX_012 );
            }
            if (counter > 1) {
                score += counter * _score[SCORE_LOCKEDPAWNS];
            }
        }
        
        //Calcular ataques al Rey
        int kingSquare = board.getKingSquare(xside);
        long sideQueens = board.getBitBoard(ncBoardContext.BITBOARD_QUEEN).value & sidePieces;
        if (side == ncGlobals.WHITE && (sideQueens != 0) && ((ncBitBoard.squareBit[board.c6] | ncBitBoard.squareBit[board.f6]) & sidePawns) != 0)
        {
            if (sidePawns != 0 && ncBitBoard.squareBit[board.f6] != 0 && kingSquare > board.h6 && ncBoardContext.distance[kingSquare][board.g7]==1) score += _score[SCORE_PAWNNEARKING];
            if (sidePawns != 0 && ncBitBoard.squareBit[board.c6] != 0 && kingSquare > board.h6 && ncBoardContext.distance[kingSquare][board.b7]==1) score += _score[SCORE_PAWNNEARKING];    
        }
        else if (side == ncGlobals.BLACK && (sideQueens != 0) && ((ncBitBoard.squareBit[board.c3] | ncBitBoard.squareBit[board.f3]) & sidePawns) != 0)
        {
            if (sidePawns != 0 && ncBitBoard.squareBit[board.f3] != 0 && kingSquare < board.a3 && ncBoardContext.distance[kingSquare][board.g2]==1) score += _score[SCORE_PAWNNEARKING];
            if (sidePawns != 0 && ncBitBoard.squareBit[board.c3] != 0 && kingSquare < board.a3 && ncBoardContext.distance[kingSquare][board.b2]==1) score += _score[SCORE_PAWNNEARKING];    
        }
        
        //Calcular peones bloqueados en e2, d2
        movers.value = board.getBitBoard( ncBoardContext.BITBOARD_OCCUPIED ).value;
        if (side == ncGlobals.WHITE && (((sidePawns & _d2e2[WHITEOFFSET]) >>> 8) & movers.value) != 0) score += _score[SCORE_BLOCKDEPAWNS];
        if (side == ncGlobals.BLACK && (((sidePawns & _d2e2[BLACKOFFSET]) << 8) & movers.value) != 0) score += _score[SCORE_BLOCKDEPAWNS];
        
        return score;
    }
}
