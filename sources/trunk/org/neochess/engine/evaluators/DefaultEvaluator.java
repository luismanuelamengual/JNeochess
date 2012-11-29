
package org.neochess.engine.evaluators;

import java.util.HashMap;
import java.util.Map;
import org.neochess.engine.Board;
import org.neochess.util.BoardUtils;

public class DefaultEvaluator extends Evaluator
{
    private final static int PHASENUMBER = 8;
    private final static int[][] Passed = { { 0, 48, 48, 120, 144, 192, 240, 0}, {0, 240, 192, 144, 120, 48, 48, 0} };
    private final static int[] _isolani_normal = { 12, 10, 8, 6, 6, 8, 10, 12 };
    private final static int[] _isolani_weaker = { -2, -4, -6, -8, -8, -6, -4, -2 };  
    private final static long[] nn = { 0x4200000000000000L, 0x0000000000000042L };
    private final static long[] bb = { 0x2400000000000000L, 0x0000000000000024L };
    private final static long[] _d2e2 = { 0x0018000000000000L, 0x0000000000001800L };
    private final static long[] _brank7 = { 0x000000000000FF00L, 0x00FF000000000000L };
    private final static long[] _brank8 = { 0x00000000000000FFL, 0xFF00000000000000L };
    private final static long[] _brank67 = { 0x0000000000FFFF00L, 0x00FFFF0000000000L };
    private final static long[] _brank58 = { 0x00000000FFFFFFFFL, 0xFFFFFFFF00000000L };
    private final static long[] sideVertical = { 0xFFFFFFFF00000000L, 0x00000000FFFFFFFFL, };
    private final static long[] sideHorizontal = { 0xF0F0F0F0F0F0F0F0L, 0x0F0F0F0F0F0F0F0FL };
    private final static long BOX_01 = 0x00003C3C3C3C0000L;
    private final static long BOX_012 = 0x007E7E7E7E7E7E00L;
    private final static int[] _pawnCoverture = { -60, -30, 0, 5, 30, 30, 30, 30, 30 };
    private final static int[] _safetyFactor = { 7, 8, 8, 7, 6, 5, 4, 2, 0, };
    private final static int[] _rank7 = { 6, 1 };
    private final static int[] _rank8 = { 7, 0 };
    private static long[][] _squarePawnMask = new long[2][64];
    private static long[][] _passedPawnMask = new long[2][64];
    private static long[] _isolaniPawnMask = new long[8];
    private final static long[] _initialKnights = { BoardUtils.squareBit[Board.B1] | BoardUtils.squareBit[Board.G1], BoardUtils.squareBit[Board.B8] | BoardUtils.squareBit[Board.G8] };
    private final static long[] _initialBishops = { BoardUtils.squareBit[Board.C1] | BoardUtils.squareBit[Board.F1], BoardUtils.squareBit[Board.C8] | BoardUtils.squareBit[Board.F8] };
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
    
    private final static int Outpost[][] =
    {
        { 
          0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 1, 1, 1, 1, 0, 0,
          0, 1, 1, 1, 1, 1, 1, 0,
          0, 0, 1, 1, 1, 1, 0, 0,
          0, 0, 0, 1, 1, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0 
      },
      { 
          0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 1, 1, 0, 0, 0,
          0, 0, 1, 1, 1, 1, 0, 0,
          0, 1, 1, 1, 1, 1, 1, 0,
          0, 0, 1, 1, 1, 1, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0,
          0, 0, 0, 0, 0, 0, 0, 0 
      }
    };
    
    private static void _initPassedPawnMasks ()
    {
        byte square;
        for (square = 0; square < 64; square++)
        {
            _passedPawnMask[Board.WHITE][square] = 0;
            _passedPawnMask[Board.BLACK][square] = 0;
        }
        for (square = 0; square < 64; square++)
        {
            _passedPawnMask[Board.WHITE][square] = BoardUtils.ray[square][7];
            if (Board.getSquareFile(square) != 0) _passedPawnMask[Board.WHITE][square] |= BoardUtils.ray[square-1][7];
            if (Board.getSquareFile(square) != 7) _passedPawnMask[Board.WHITE][square] |= BoardUtils.ray[square+1][7];
        }
        for (square = 0; square < 64; square++)
        {
            _passedPawnMask[Board.BLACK][square] = BoardUtils.ray[square][4];
            if (Board.getSquareFile(square) != 0) _passedPawnMask[Board.BLACK][square] |= BoardUtils.ray[square-1][4];
            if (Board.getSquareFile(square) != 7) _passedPawnMask[Board.BLACK][square] |= BoardUtils.ray[square+1][4];
        }
    }
    
    private static void _initIsolaniPawnMask ()
    {
        _isolaniPawnMask[0] = BoardUtils.fileBits[1];
        _isolaniPawnMask[7] = BoardUtils.fileBits[6];
        for (int i = 1; i <= 6; i++) 
            _isolaniPawnMask[i] = BoardUtils.fileBits[i-1] | BoardUtils.fileBits[i+1];
    }
    
    private static void _initSquarePawnMask ()
    {
        byte sq;
        int len, i, j;
        for (sq = 0; sq < 64; sq++)
        {
            len = 7 - Board.getSquareRank(sq);
            i = Math.max(sq & 56, sq - len);
            j = Math.min(sq | 7, sq + len);
            while (i <= j)
            {
                _squarePawnMask[Board.WHITE][sq] |= (BoardUtils.squareBit[i] | BoardUtils.fromtoRay[i][i|56]);
                i++;
            }

            len = Board.getSquareRank(sq);
            i = Math.max(sq & 56, sq - len);
            j = Math.min(sq | 7, sq + len);
            while (i <= j)
            {
                _squarePawnMask[Board.BLACK][sq] |= (BoardUtils.squareBit[i] | BoardUtils.fromtoRay[i][i&7]);
                i++;
            }
        }
        for (sq = Board.A2; sq <= Board.H2; sq++)
            _squarePawnMask[Board.WHITE][sq] = _squarePawnMask[Board.WHITE][sq+8];
        for (sq = Board.A7; sq <= Board.H7; sq++)
            _squarePawnMask[Board.BLACK][sq] = _squarePawnMask[Board.BLACK][sq-8];
    }
    
    static 
    {
        _initPassedPawnMasks ();
        _initIsolaniPawnMask ();
        _initSquarePawnMask ();
    }
    
    private Map<String, Integer> scores;
    private long _passedPawns[] = new long[2];
    private long _weakedPawns[] = new long[2];
    private long pinned;
    private byte kingSquare[] = new byte[2];
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
        scores.put("SCORE_KNIGHTONRIM", -13);
        scores.put("SCORE_OUTPOSTKNIGHT", 10);
        scores.put("SCORE_PINNEDKNIGHT", -30);
        scores.put("SCORE_KNIGHTTRAPPED", -250);
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
        kingSquare[Board.WHITE] = board.getKingSquare(Board.WHITE);
        kingSquare[Board.BLACK] = board.getKingSquare(Board.BLACK);
        pinned = board.getPins();
        
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
        byte xside = Board.getOppositeSide(side);
        int pawnCounter[] = new int[8];
        long[][] pieces = board.getPieces();
        long sidePawns = pieces[side][Board.PAWN];
        long xsidePawns = pieces[xside][Board.PAWN];
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
            pawnMoves = BoardUtils.moveArray[side == Board.WHITE? Board.PAWN:Board.BPAWN][square];
            if (((pawnMoves & sidePawns) != 0) && ((pawnMoves & xsidePawns) != 0))
                score += getScore("SCORE_PAWNBASEATAK");
            
            //Incrementar la cantidad de peones de la columna
            pawnCounter[Board.getSquareFile(square)]++;
        }
        
        for ( int fileindex = 0; fileindex <= 7; fileindex++ )
        {
            //Peones doblados
            if (pawnCounter[fileindex] > 1) score += getScore("SCORE_DOUBLEDPAWNS");
            
            //Peones isolados
            if ((pawnCounter[fileindex] > 0) && ((sidePawns & _isolaniPawnMask[fileindex]) == 0))
            {
                if ((BoardUtils.fileBits[fileindex] & xsidePawns) == 0) 
                    score += (getScore("SCORE_ISOLATEDPAWNS") + _isolani_weaker[fileindex]) * pawnCounter[fileindex];
                else 
                    score += (getScore("SCORE_ISOLATEDPAWNS") + _isolani_normal[fileindex]) * pawnCounter[fileindex];
                _weakedPawns[side] |= (sidePawns & BoardUtils.fileBits[fileindex]);
            }
        }
        
        //Favorecer el tener peones en el centro
        score += BoardUtils.getBitCount(sidePawns & _centerFiles) * getScore("SCORE_CENTERPAWNS");
        
        //Penalizar tener 8 peones
        if (BoardUtils.getBitCount(sidePawns) == 8) score += getScore("SCORE_ALLPAWNS");

        //Tener peones bloqueados
        int counter = 0;
        if (side == Board.WHITE)         
            counter = BoardUtils.getBitCount((sidePawns >>> 8) & xsidePawns & BOX_012);
        else 
            counter = BoardUtils.getBitCount((sidePawns << 8) & xsidePawns & BOX_012);
        if (counter > 1) 
            score += counter * getScore("SCORE_LOCKEDPAWNS");
        
        //Calcular ataques al Rey
        byte xsideKingSquare = kingSquare[xside];
        long sideQueens = pieces[side][Board.QUEEN];
        if (side == Board.WHITE && (sideQueens != 0) && ((BoardUtils.squareBit[board.C6] | BoardUtils.squareBit[board.F6]) & sidePawns) != 0)
        {
            if (sidePawns != 0 && BoardUtils.squareBit[board.F6] != 0 && xsideKingSquare > board.H6 && BoardUtils.distance[xsideKingSquare][board.G7]==1) score += getScore("SCORE_PAWNNEARKING");
            if (sidePawns != 0 && BoardUtils.squareBit[board.C6] != 0 && xsideKingSquare > board.H6 && BoardUtils.distance[xsideKingSquare][board.B7]==1) score += getScore("SCORE_PAWNNEARKING");    
        }
        else if (side == Board.BLACK && (sideQueens != 0) && ((BoardUtils.squareBit[board.C3] | BoardUtils.squareBit[board.F3]) & sidePawns) != 0)
        {
            if (sidePawns != 0 && BoardUtils.squareBit[board.F3] != 0 && xsideKingSquare < board.A3 && BoardUtils.distance[xsideKingSquare][board.G2]==1) score += getScore("SCORE_PAWNNEARKING");
            if (sidePawns != 0 && BoardUtils.squareBit[board.C3] != 0 && xsideKingSquare < board.A3 && BoardUtils.distance[xsideKingSquare][board.B2]==1) score += getScore("SCORE_PAWNNEARKING");    
        }
        
        //Calcular peones bloqueados en e2, d2, e7, d7
        movers = board.getBlocker();
        if (side == Board.WHITE && (((sidePawns & _d2e2[Board.WHITE]) >>> 8) & movers) != 0) score += getScore("SCORE_BLOCKDEPAWNS");
        if (side == Board.BLACK && (((sidePawns & _d2e2[Board.BLACK]) << 8) & movers) != 0) score += getScore("SCORE_BLOCKDEPAWNS");
        
        //Peones pasados fuera del alcance del ray
        if (_passedPawns[side] != 0 && pieces[xside][Board.PAWN] == 0)
        {
            movers = _passedPawns[side];
            while (movers != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[square];
                if (board.getSideToMove() == side)
                    if ((_squarePawnMask[side][square] & pieces[xside][Board.KING]) == 0)
                        score += getScore("SCORE_QUEEN") * Passed[side][Board.getSquareRank(square)] / 550;
                else if ((BoardUtils.moveArray[Board.KING][kingSquare[xside]] & _squarePawnMask[side][square]) == 0)
                    score += getScore("SCORE_QUEEN") * Passed[side][Board.getSquareRank(square)] / 550;
            }
        }

        //Favorecer tormeta de peones si los reyes han enrocado en direcciones opuestas
        movers = pieces[side][Board.PAWN];
        if (Math.abs(Board.getSquareFile(kingSquare[side]) - Board.getSquareFile(kingSquare[xside])) >= 4 && _phase < 6)
        {
            byte xsideKingFile = Board.getSquareFile(kingSquare[xside]);
            long pawnsInKingsColumns = (_isolaniPawnMask[xsideKingFile] | BoardUtils.fileBits[xsideKingFile]) & movers;
            while (pawnsInKingsColumns != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(pawnsInKingsColumns);
                pawnsInKingsColumns &= BoardUtils.squareBitX[square];
                score += 10 * (5 - BoardUtils.distance[square][kingSquare[xside]]);
            }
        }
        
        return score;
    }
    
    public int evaluateKnights (Board board, byte side)
    {
        byte xside, sq;
        int score, tempScore;
        long[][] pieces = board.getPieces();
        long knights, enemyPawns;
        if (pieces[side][Board.KNIGHT] == 0)
            return 0;
        
        xside = Board.getOppositeSide(side);
        score = tempScore = 0;
        knights = pieces[side][Board.KNIGHT];
        enemyPawns = pieces[xside][Board.PAWN]; 
        if ((knights & pinned) != 0)        
            score += getScore("SCORE_PINNEDKNIGHT") * BoardUtils.getBitCount(knights & pinned);
        while (knights != 0)
        {
            sq = (byte)BoardUtils.getLeastSignificantBit(knights);
            knights &= BoardUtils.squareBitX[sq];
            tempScore = evaluateControl(board,sq,side);
            if ( (BoardUtils.squareBit[sq] & BoardUtils.rings[3]) != 0)
                tempScore += getScore("SCORE_KNIGHTONRIM");
            if (Outpost[side][sq] == 1 && ((enemyPawns & _isolaniPawnMask[Board.getSquareFile(sq)] & _passedPawnMask[side][sq]) == 0))
            {
                tempScore += getScore("SCORE_OUTPOSTKNIGHT");
                if ((BoardUtils.moveArray[xside == Board.WHITE? Board.PAWN : Board.BPAWN][sq] & pieces[side][Board.PAWN]) != 0)
                    tempScore += getScore("SCORE_OUTPOSTKNIGHT");
            }
            if ((BoardUtils.moveArray[Board.KNIGHT][sq] & _weakedPawns[xside]) != 0)
                tempScore += getScore("SCORE_ATAKWEAKPAWN");
            score += tempScore;
        }
        return score;
    }
    
    private int evaluateControl (Board board, byte square, byte side)
    {
        byte enemyKing = kingSquare[1^side];
        byte friendlyKing = kingSquare[side];
        long controlled = board.getSquareXAttacks(square, side);
        int score = 0;
        score += (4 * BoardUtils.getBitCount(controlled & BoardUtils.boxes[0]));
        score += BoardUtils.getBitCount(controlled & BoardUtils.distMap[enemyKing][2]);
        score += BoardUtils.getBitCount(controlled & BoardUtils.distMap[friendlyKing][2]);
        score += (4 * BoardUtils.getBitCount(controlled));
        return score;
    }
}
