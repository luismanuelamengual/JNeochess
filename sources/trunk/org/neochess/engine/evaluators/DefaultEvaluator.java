
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
    private int phase;
    
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
        phase = PHASENUMBER - (int)(((double)actualMaterial * (double)PHASENUMBER) / (double)originalMaterial);
        phase = Math.max(phase, 0);
        phase = Math.min(phase, PHASENUMBER);

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
    
    private int evaluateMaterial (Board board, int side)
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
    
    public int evaluateDevelopment (Board board, int side)
    {
        int score = 0;
//        int sq;
//        long c;
//        c = (board.pieces[side][knight] & nn[side]) | (board.b[side][bishop] & bb[side]);
//        s = nbits(c) * -8;
//
//        /* If we are castled or beyond the 20th move, no more ScoreDev */
//        if (board.castled[side] || GameCnt >= 38)
//           return (s);
//
//        s += NOTCASTLED;
//
//        /* If the king is moved, nail it, otherwise check rooks */
//        if (Mvboard[board.king[side]] > 0) 
//           s += KINGMOVED;
//
//        /* Discourage rook moves */
//        c = board.b[side][rook];
//        while (c) {
//          sq = leadz(c);
//          CLEARBIT(c, sq);
//          if (Mvboard[sq] > 0)
//            s += ROOKMOVED;
//        }
//
//        /* Penalize a queen that moves at all */
//        if (board.b[side][queen])
//        {
//           sq = leadz (board.b[side][queen]);
//           if (Mvboard[sq] > 0)
//              s += EARLYQUEENMOVE;
//              /* s += Mvboard[sq] * EARLYQUEENMOVE; */
//        }
//
//        /* Discourage repeat minor piece moves */
//        c = board.b[side][knight] | board.b[side][bishop];
//        while (c) {
//          sq = leadz(c);
//          CLEARBIT(c, sq);
//          if (Mvboard[sq] > 1)
//             s += EARLYMINORREPEAT;
//             /* s += Mvboard[sq] * EARLYMINORREPEAT; */
//        }
//
//        /* Discourage any wing pawn moves */
//        /*   c = board.b[side][pawn] & (FileBit[0]|FileBit[1]|FileBit[6]|FileBit[7]); */
//        c = board.b[side][pawn] & ULL(0xc3c3c3c3c3c3c3c3);
//        while (c) {
//          sq = leadz(c);
//          CLEARBIT(c, sq);
//          if (Mvboard[sq] > 0) 
//             s += EARLYWINGPAWNMOVE;
//        }
//
//        /* Discourage any repeat center pawn moves */
//        /*   c = board.b[side][pawn] & (FileBit[2]|FileBit[3]|FileBit[4]|FileBit[5]); */
//        c = board.b[side][pawn] & ULL(0x3c3c3c3c3c3c3c3c);
//        while (c) {
//          sq = leadz(c);
//          CLEARBIT(c, sq);
//          if (Mvboard[sq] > 1) 
//             s += EARLYCENTERPREPEAT;
//        }

        return score;
    }
}
