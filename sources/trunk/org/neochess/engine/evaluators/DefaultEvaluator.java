
package org.neochess.engine.evaluators;

import java.util.Hashtable;
import org.neochess.engine.Board;

public class DefaultEvaluator extends Evaluator
{
    private Hashtable<String, Integer> properties;
    private byte evaluationPhase;
    private byte[] evaluationWhitePawnStructure;
    private byte[] evaluationBlackPawnStructure;
    private int evaluationWhitePawnMaterial;
    private int evaluationWhitePieceMaterial;
    private int evaluationBlackPawnMaterial;
    private int evaluationBlackPieceMaterial;
    
    private static final int figureValue[] = {100, 300, 310, 500, 900, 0};

    private static final int pawnSquareValue[] = 
    {
          0,   0,   0,   0,   0,   0,   0,   0,
          5,  10,  15,  20,  20,  15,  10,   5,
          4,   8,  12,  16,  16,  12,   8,   4,
          3,   6,   9,  12,  12,   9,   6,   3,
          2,   4,   6,   8,   8,   6,   4,   2,
          1,   2,   3,  -6,  -6,   3,   2,   1,
          0,   0,   0, -25, -25,   0,   0,   0,
          0,   0,   0,   0,   0,   0,   0,   0
    };
    
    private static final int knightSquareValue[] = 
    {
        -10, -10, -10, -10, -10, -10, -10, -10,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10, -30, -10, -10, -10, -10, -30, -10
    };
    
    private static final int bishopSquareValue[] = 
    {
        -10, -10, -10, -10, -10, -10, -10, -10,
        -10,   0,   0,   0,   0,   0,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   0,   5,  10,  10,   5,   0, -10,
        -10,   0,   5,   5,   5,   5,   0, -10,
        -10,   5,   0,   0,   0,   0,   5, -10,
        -10, -10, -20, -10, -10, -20, -10, -10
    };
    
    private static final int kingSquareValue[] = 
    {
        -40, -40, -40, -40, -40, -40, -40, -40,
        -40, -40, -40, -40, -40, -40, -40, -40,
        -40, -40, -40, -40, -40, -40, -40, -40,
        -40, -40, -40, -40, -40, -40, -40, -40,
        -40, -40, -40, -40, -40, -40, -40, -40,
        -40, -40, -40, -40, -40, -40, -40, -40,
        -20, -20, -20, -20, -20, -20, -20, -20,
          0,  20,  40, -20,   0, -20,  40,  20
    };
    
    private static final int kingEndingSquareValue[] = 
    {
         0,  10,  20,  30,  30,  20,  10,   0,
         10,  20,  30,  40,  40,  30,  20,  10,
         20,  30,  40,  50,  50,  40,  30,  20,
         30,  40,  50,  60,  60,  50,  40,  30,
         30,  40,  50,  60,  60,  50,  40,  30,
         20,  30,  40,  50,  50,  40,  30,  20,
         10,  20,  30,  40,  40,  30,  20,  10,
          0,  10,  20,  30,  30,  20,  10,   0
    };
    
    private static final byte flip[] = 
    {
         56,  57,  58,  59,  60,  61,  62,  63,
         48,  49,  50,  51,  52,  53,  54,  55,
         40,  41,  42,  43,  44,  45,  46,  47,
         32,  33,  34,  35,  36,  37,  38,  39,
         24,  25,  26,  27,  28,  29,  30,  31,
         16,  17,  18,  19,  20,  21,  22,  23,
          8,   9,  10,  11,  12,  13,  14,  15,
          0,   1,   2,   3,   4,   5,   6,   7
    };
    
    public DefaultEvaluator()
    {
        properties = new Hashtable<String, Integer>();
        evaluationWhitePawnStructure = new byte[8];
        evaluationBlackPawnStructure = new byte[8];
        setProperty("PAWNVALUE", figureValue[Board.PAWN]);
        setProperty("KNIGHTVALUE", figureValue[Board.KNIGHT]);
        setProperty("BISHOPVALUE", figureValue[Board.BISHOP]);
        setProperty("ROOKVALUE", figureValue[Board.ROOK]);
        setProperty("QUEENVALUE", figureValue[Board.QUEEN]);
        setProperty("KINGVALUE", 10000);     
        setProperty("DOUBLEDPAWNSCORE", -22);
        setProperty("ISOLATEDPAWNSCORE", -30);
        setProperty("BACKWARDPAWNSCORE", -18);
        setProperty("PASSEDPAWNSCORE", 20);
        setProperty("ROOKOPENFILESCORE", 15);
        setProperty("ROOKSEMIOPENFILESCORE", 10);
        setProperty("ROOK7THRANKSCORE", 20);
        setProperty("DOUBLEDBISHOPSSCORE", 15);
    }
    
    @Override
    public void dispose ()
    {
        properties.clear();
        properties = null;
        super.dispose();
    }

    public void setProperty (String key, int value)
    {
        properties.put(key, value);
    }

    public int getProperty (String key)
    {
        return properties.get(key);
    }

    @Override
    public int evaluate(Board board)
    {
        int score = 0;        
        retrieveEvaluationData(board);
        byte whiteBishops = 0;
        byte blackBishops = 0;
        for (byte square = Board.A8; square <= Board.H1; square++)
        {
            byte side = board.getSquareSide(square);
            if (side != Board.NOSIDE)
            {
                byte figure = board.getSquareFigure(square);
                switch (figure)
                {
                    case Board.PAWN:
                        score += evaluatePawn(square, side);
                        break;
                    case Board.KNIGHT:
                        score += evaluateKnight(square, side);
                        break;
                    case Board.BISHOP:
                        score += evaluateBishop(square, side);
                        if (side == Board.WHITE)
                            whiteBishops++;
                        else
                            blackBishops++;
                        break;
                    case Board.ROOK:
                        score += evaluateRook(square, side);
                        break;
                    case Board.QUEEN:
                        score += evaluateQueen(square, side);
                        break;
                    case Board.KING:
                        score += evaluateKing(square, side);
                        break;
                }
            }
        }
        if (whiteBishops == 2)
            score += getProperty("DOUBLEDBISHOPSSCORE");
        if (blackBishops == 2)
            score -= getProperty("DOUBLEDBISHOPSSCORE");
        return score;
    }
    
    protected void retrieveEvaluationData (Board board)
    {
        for (int file = Board.FILE_A; file <= Board.FILE_H; file++)
        {
            evaluationWhitePawnStructure[file] = Board.RANK_8;
            evaluationBlackPawnStructure[file] = Board.RANK_1;
        }
        evaluationWhitePawnMaterial = 0;
        evaluationWhitePieceMaterial = 0;
        evaluationBlackPawnMaterial = 0;
        evaluationBlackPieceMaterial = 0;
        
        for (byte square = Board.A8; square <= Board.H1; square++)
        {
            byte side = board.getSquareSide(square);
            byte squareFile = Board.getSquareFile(square);
            byte squareRank = Board.getSquareRank(square);
            if (side != Board.NOSIDE)
            {
                byte figure = board.getSquareFigure(square);
                if (side == Board.WHITE)
                {
                    switch (figure)
                    {
                        case Board.PAWN:
                            evaluationWhitePawnMaterial += figureValue[Board.PAWN];
                            if (squareRank > evaluationWhitePawnStructure[squareFile])
                                evaluationWhitePawnStructure[squareFile] = squareRank;
                            break;
                        default:
                            evaluationWhitePieceMaterial += figureValue[figure];
                            break;
                    }
                }
                else
                {
                    switch (figure)
                    {
                        case Board.PAWN:
                            evaluationBlackPawnMaterial += figureValue[Board.PAWN];
                            if (squareRank < evaluationBlackPawnStructure[squareFile])
                                evaluationBlackPawnStructure[squareFile] = squareRank;
                            break;
                        default:
                            evaluationBlackPieceMaterial += figureValue[figure];
                            break;
                    }
                }
            }
        }
        byte numerOfPhases = 8;
        int originalMaterial = (figureValue[Board.PAWN] * 16) + (figureValue[Board.KNIGHT] * 4) + (figureValue[Board.BISHOP] * 4) + (figureValue[Board.ROOK] * 4) + (figureValue[Board.QUEEN] * 2);
        int actualMaterial = evaluationWhitePawnMaterial + evaluationWhitePieceMaterial + evaluationBlackPawnMaterial + evaluationBlackPieceMaterial;
        evaluationPhase = (byte)(numerOfPhases - Math.floor((actualMaterial * numerOfPhases) / originalMaterial));
    }
    
    protected int evaluatePawn (byte square, byte side)
    {
        int score = getProperty("PAWNVALUE");
        byte squareFile = Board.getSquareFile(square);
        byte squareRank = Board.getSquareRank(square);
        
        if (side == Board.WHITE)
        {
            score += pawnSquareValue[square];
            if (evaluationWhitePawnStructure[squareFile] > squareRank)
                score += getProperty("DOUBLEDPAWNSCORE");
            if ((squareFile == Board.FILE_A || evaluationWhitePawnStructure[squareFile - 1] == Board.RANK_8) && (squareFile == Board.FILE_H || evaluationWhitePawnStructure[squareFile + 1] == Board.RANK_8))
                score += getProperty("ISOLATEDPAWNSCORE");
            else if ((squareFile == Board.FILE_A || evaluationWhitePawnStructure[squareFile - 1] < squareRank) && (squareFile == Board.FILE_H || evaluationWhitePawnStructure[squareFile + 1] < squareRank))
                score += getProperty("BACKWARDPAWNSCORE");
            if ((squareFile == Board.FILE_A || evaluationBlackPawnStructure[squareFile - 1] >= squareRank) && (squareFile == Board.FILE_H || evaluationBlackPawnStructure[squareFile + 1] >= squareRank) && evaluationBlackPawnStructure[squareFile] >= squareRank)
                score += (7 - squareRank) * getProperty("PASSEDPAWNSCORE");
        }
        else
        {
            score += pawnSquareValue[flip[square]];
            if (evaluationBlackPawnStructure[squareFile] < squareRank)
                score += getProperty("DOUBLEDPAWNSCORE");
            if ((squareFile == Board.FILE_A || evaluationBlackPawnStructure[squareFile - 1] == Board.RANK_1) && (squareFile == Board.FILE_H || evaluationBlackPawnStructure[squareFile + 1] == Board.RANK_1))
                score += getProperty("ISOLATEDPAWNSCORE");
            else if ((squareFile == Board.FILE_A || evaluationBlackPawnStructure[squareFile - 1] > squareRank) && (squareFile == Board.FILE_H || evaluationBlackPawnStructure[squareFile + 1] > squareRank))
                score += getProperty("BACKWARDPAWNSCORE");
            if ((squareFile == Board.FILE_A || evaluationWhitePawnStructure[squareFile - 1] <= squareRank) && (squareFile == Board.FILE_H || evaluationWhitePawnStructure[squareFile + 1] <= squareRank) && evaluationWhitePawnStructure[squareFile] <= squareRank)
                score += squareRank * getProperty("PASSEDPAWNSCORE");
        }
        return (side == Board.BLACK)? -score : score;
    }
    
    protected int evaluateKnight (byte square, byte side)
    {
        int score = getProperty("KNIGHTVALUE");
        if (side == Board.WHITE)
            score += knightSquareValue[square];
        else
            score += knightSquareValue[flip[square]];
        return (side == Board.BLACK)? -score : score;
    }
    
    protected int evaluateBishop (byte square, byte side)
    {
        int score = getProperty("BISHOPVALUE");
        if (side == Board.WHITE)
            score += bishopSquareValue[square];
        else
            score += bishopSquareValue[flip[square]];
        return (side == Board.BLACK)? -score : score;
    }
    
    protected int evaluateRook (byte square, byte side)
    {
        int score = getProperty("ROOKVALUE");
        byte squareFile = Board.getSquareFile(square);
        byte squareRank = Board.getSquareRank(square);
        
        if (side == Board.WHITE)
        {
            if (evaluationWhitePawnStructure[squareFile] == Board.RANK_8)
            {
                if (evaluationBlackPawnStructure[squareFile] == Board.RANK_1)
                    score += getProperty("ROOKOPENFILESCORE");
                else
                    score += getProperty("ROOKSEMIOPENFILESCORE");
            }
            if (squareRank == Board.RANK_7)
                score += getProperty("ROOK7THRANKSCORE");
        }
        else
        {
            if (evaluationBlackPawnStructure[squareFile] == Board.RANK_1)
            {
                if (evaluationWhitePawnStructure[squareFile] == Board.RANK_8)
                    score += getProperty("ROOKOPENFILESCORE");
                else
                    score += getProperty("ROOKSEMIOPENFILESCORE");
            }
            if (squareRank == Board.RANK_2)
                score += getProperty("ROOK7THRANKSCORE");
        }
        return (side == Board.BLACK)? -score : score;
    }
    
    protected int evaluateQueen (byte square, byte side)
    {
        int score = getProperty("QUEENVALUE");
        return (side == Board.BLACK)? -score : score;
    }
    
    protected int evaluateKing (byte square, byte side)
    {
        int score = getProperty("KINGVALUE");
        byte squareFile = Board.getSquareFile(square);
        int safetyScore = 0;
        int maxOppositeMaterialForEnding = 900;
        if (side == Board.WHITE)
        {
            if (evaluationBlackPieceMaterial > maxOppositeMaterialForEnding)
            {
                safetyScore += kingSquareValue[square];
                if (squareFile < Board.FILE_D) 
                {
                    safetyScore += evaluatePawnFile(Board.FILE_A, Board.WHITE);
                    safetyScore += evaluatePawnFile(Board.FILE_B, Board.WHITE); 
                    safetyScore += evaluatePawnFile(Board.FILE_C, Board.WHITE) / 2;
                }
                else if (squareFile > Board.FILE_F)
                {
                    safetyScore += evaluatePawnFile(Board.FILE_H, Board.WHITE);
                    safetyScore += evaluatePawnFile(Board.FILE_G, Board.WHITE);
                    safetyScore += evaluatePawnFile(Board.FILE_F, Board.WHITE) / 2;
                }
                else 
                {
                    for (int i = squareFile - 1; i <= squareFile + 1; i++)
                        if (evaluationWhitePawnStructure[i] == Board.RANK_8 && evaluationBlackPawnStructure[i] == Board.RANK_1)
                            safetyScore += -10;
                }
                safetyScore *= evaluationBlackPieceMaterial;
                safetyScore /= 3100;
            }
            else
            {
                safetyScore += kingEndingSquareValue[square];
            }
        }
        else
        {
            if (evaluationWhitePieceMaterial > maxOppositeMaterialForEnding)
            {
                safetyScore += kingSquareValue[flip[square]];
                if (squareFile < Board.FILE_D) 
                {
                    safetyScore += evaluatePawnFile(Board.FILE_A, Board.WHITE);
                    safetyScore += evaluatePawnFile(Board.FILE_B, Board.WHITE); 
                    safetyScore += evaluatePawnFile(Board.FILE_C, Board.WHITE) / 2;
                }
                else if (squareFile > Board.FILE_F)
                {
                    safetyScore += evaluatePawnFile(Board.FILE_H, Board.WHITE);
                    safetyScore += evaluatePawnFile(Board.FILE_G, Board.WHITE);
                    safetyScore += evaluatePawnFile(Board.FILE_F, Board.WHITE) / 2;
                }
                else 
                {
                    for (int i = squareFile - 1; i <= squareFile + 1; i++)
                        if (evaluationWhitePawnStructure[i] == Board.RANK_8 && evaluationBlackPawnStructure[i] == Board.RANK_1)
                            safetyScore += -10;
                }
                safetyScore *= evaluationWhitePieceMaterial;
                safetyScore /= 3100;
            }
            else
            {
                safetyScore += kingEndingSquareValue[flip[square]];
            }
        }
        
        score += safetyScore;
        return (side == Board.BLACK)? -score : score;
    }
    
    protected int evaluatePawnFile (byte file, byte side)
    {
        int score = 0;

        if (side == Board.WHITE)
        {
            if (evaluationWhitePawnStructure[file] == Board.RANK_2);
            else if (evaluationWhitePawnStructure[file] == Board.RANK_3)
                score -= 10;
            else if (evaluationWhitePawnStructure[file] != Board.RANK_8)
                score -= 20;
            else
                score -= 25;
            if (evaluationBlackPawnStructure[file] == Board.RANK_1)
                score -= 15;  
            else if (evaluationBlackPawnStructure[file] == Board.RANK_3)
                score -= 10;  
            else if (evaluationBlackPawnStructure[file] == Board.RANK_4)
                score -= 5;
        }
        else
        {
            if (evaluationBlackPawnStructure[file] == Board.RANK_7);
            else if (evaluationBlackPawnStructure[file] == Board.RANK_6)
                score -= 10;
            else if (evaluationBlackPawnStructure[file] != Board.RANK_1)
                score -= 20;
            else
                score -= 25;
            if (evaluationWhitePawnStructure[file] == Board.RANK_8)
                score -= 15;  
            else if (evaluationWhitePawnStructure[file] == Board.RANK_6)
                score -= 10;  
            else if (evaluationWhitePawnStructure[file] == Board.RANK_5)
                score -= 5;
        }
        
        return score;
    }
}
