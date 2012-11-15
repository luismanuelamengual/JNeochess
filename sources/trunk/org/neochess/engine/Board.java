
package org.neochess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.neochess.general.Disposable;

public class Board implements Disposable, Cloneable
{
    public static final byte NOSIDE = -1;
    public static final byte WHITE = 0;
    public static final byte BLACK = 6;
    
    public static final byte EMPTY = -1;
    public static final byte PAWN = 0;
    public static final byte KNIGHT = 1;
    public static final byte BISHOP = 2;
    public static final byte ROOK = 3;
    public static final byte QUEEN = 4;
    public static final byte KING = 5;
    public static final byte WHITEPAWN = 0;
    public static final byte WHITEKNIGHT = 1;
    public static final byte WHITEBISHOP = 2;
    public static final byte WHITEROOK = 3;
    public static final byte WHITEQUEEN = 4;
    public static final byte WHITEKING = 5;
    public static final byte BLACKPAWN = 6;
    public static final byte BLACKKNIGHT = 7;
    public static final byte BLACKBISHOP = 8;
    public static final byte BLACKROOK = 9;
    public static final byte BLACKQUEEN = 10;
    public static final byte BLACKKING = 11;
    
    public static final byte INVALIDSQUARE = -1;
    public static final byte A8 = 0;
    public static final byte B8 = 1;
    public static final byte C8 = 2;
    public static final byte D8 = 3;
    public static final byte E8 = 4;
    public static final byte F8 = 5;
    public static final byte G8 = 6;
    public static final byte H8 = 7;
    public static final byte A7 = 8;
    public static final byte B7 = 9;
    public static final byte C7 = 10;
    public static final byte D7 = 11;
    public static final byte E7 = 12;
    public static final byte F7 = 13;
    public static final byte G7 = 14;
    public static final byte H7 = 15;
    public static final byte A6 = 16;
    public static final byte B6 = 17;
    public static final byte C6 = 18; 
    public static final byte D6 = 19;
    public static final byte E6 = 20;
    public static final byte F6 = 21;
    public static final byte G6 = 22;
    public static final byte H6 = 23;
    public static final byte A5 = 24;
    public static final byte B5 = 25;
    public static final byte C5 = 26;
    public static final byte D5 = 27;
    public static final byte E5 = 28;
    public static final byte F5 = 29;
    public static final byte G5 = 30;
    public static final byte H5 = 31;
    public static final byte A4 = 32;
    public static final byte B4 = 33;
    public static final byte C4 = 34;
    public static final byte D4 = 35;
    public static final byte E4 = 36;
    public static final byte F4 = 37;
    public static final byte G4 = 38;
    public static final byte H4 = 39;
    public static final byte A3 = 40;
    public static final byte B3 = 41;
    public static final byte C3 = 42;
    public static final byte D3 = 43;
    public static final byte E3 = 44;
    public static final byte F3 = 45;
    public static final byte G3 = 46;
    public static final byte H3 = 47;
    public static final byte A2 = 48;
    public static final byte B2 = 49;
    public static final byte C2 = 50;
    public static final byte D2 = 51;
    public static final byte E2 = 52;
    public static final byte F2 = 53;
    public static final byte G2 = 54;
    public static final byte H2 = 55;
    public static final byte A1 = 56;
    public static final byte B1 = 57;
    public static final byte C1 = 58;
    public static final byte D1 = 59;
    public static final byte E1 = 60;
    public static final byte F1 = 61;
    public static final byte G1 = 62;
    public static final byte H1 = 63;
    
    public static final byte FILE_A = 0;
    public static final byte FILE_B = 1;
    public static final byte FILE_C = 2;
    public static final byte FILE_D = 3;
    public static final byte FILE_E = 4;
    public static final byte FILE_F = 5;
    public static final byte FILE_G = 6;
    public static final byte FILE_H = 7;
    public static final byte RANK_1 = 7;
    public static final byte RANK_2 = 6;
    public static final byte RANK_3 = 5;
    public static final byte RANK_4 = 4;
    public static final byte RANK_5 = 3;
    public static final byte RANK_6 = 2;
    public static final byte RANK_7 = 1;
    public static final byte RANK_8 = 0;

    private static final byte NOCASTLE = 0;
    private static final byte WHITECASTLESHORT = 1;
    private static final byte WHITECASTLELONG = 2;
    private static final byte BLACKCASTLESHORT = 4;
    private static final byte BLACKCASTLELONG = 8;
    private static final byte[] CASTLEMASK;
    private static final byte[][][] FIGUREOFFSETS;
    private static final byte[] FIGUREOFFSETSCOUNT;
    private static final byte[] LEFT = {-1,0};
    private static final byte[] RIGHT = {1,0};
    private static final byte[] UP = {0,1};
    private static final byte[] DOWN = {0,-1};
    private static final byte[] LEFT_UP = {-1,1};
    private static final byte[] LEFT_DOWN = {-1,-1};
    private static final byte[] RIGHT_UP = {1,1};
    private static final byte[] RIGHT_DOWN = {1,-1};
    private static final byte[] LEFT2_UP1 = {-2,1};
    private static final byte[] LEFT1_UP2 = {-1,2};
    private static final byte[] LEFT2_DOWN1 = {-2,-1};
    private static final byte[] LEFT1_DOWN2 = {-1,-2};
    private static final byte[] RIGHT2_UP1 = {2,1};
    private static final byte[] RIGHT1_UP2 = {1,2};
    private static final byte[] RIGHT2_DOWN1 = {2,-1};
    private static final byte[] RIGHT1_DOWN2 = {1,-2};
    private static final byte[] UP2 = {0,2};
    private static final byte[] DOWN2 = {0,-2};

    private static final long HASHPIECE[][] = new long[12][64];
    private static final long HASHSIDE;
    private static final long HASHEP[] = new long[64];
    private static final long HASHCASTLEWS;
    private static final long HASHCASTLEWL;
    private static final long HASHCASTLEBS;
    private static final long HASHCASTLEBL;

    private byte[] squares = new byte[64];
    private byte sideToMove = WHITE;
    private byte enPassantSquare = INVALIDSQUARE;
    private byte castleState = NOCASTLE;

    static
    {
        CASTLEMASK = new byte[64];
        for (byte square = A8; square <= H1; square++)
            CASTLEMASK[square] = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[A1] = WHITECASTLESHORT | BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[H1] = WHITECASTLELONG | BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[E1] = BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[A8] = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLESHORT;
        CASTLEMASK[H8] = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLELONG;
        CASTLEMASK[E8] = WHITECASTLESHORT | WHITECASTLELONG;

        FIGUREOFFSETS = new byte[6][8][2];
        FIGUREOFFSETS[KNIGHT] = new byte[][] {LEFT2_UP1, LEFT1_UP2, LEFT2_DOWN1, LEFT1_DOWN2, RIGHT2_UP1, RIGHT1_UP2, RIGHT2_DOWN1, RIGHT1_DOWN2};
        FIGUREOFFSETS[BISHOP] = new byte[][] {LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN};
        FIGUREOFFSETS[ROOK] = new byte[][] {LEFT, RIGHT, UP, DOWN};
        FIGUREOFFSETS[QUEEN] = new byte[][] {LEFT, RIGHT, UP, DOWN, LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN};
        FIGUREOFFSETS[KING] = new byte[][] {LEFT, RIGHT, UP, DOWN, LEFT_UP, LEFT_DOWN, RIGHT_UP, RIGHT_DOWN};
        FIGUREOFFSETSCOUNT = new byte[] {0, 8, 4, 4, 8, 8};

        Random randomGenerator = new Random(0);
        for (byte piece = WHITEPAWN; piece <= BLACKKING; piece++)
            for (byte square = A8; square <= H1; square++)
                HASHPIECE[piece][square] = randomGenerator.nextLong();
        for (byte epSquare = 0; epSquare < 64; epSquare++)
            HASHEP[epSquare] = randomGenerator.nextLong();
        HASHCASTLEWS = randomGenerator.nextLong();
        HASHCASTLEWL = randomGenerator.nextLong();
        HASHCASTLEBS = randomGenerator.nextLong();
        HASHCASTLEBL = randomGenerator.nextLong();
        HASHSIDE = randomGenerator.nextLong();
    }

    public Board ()
    {
    }
    
    public Board (Board copyBoard)
    {
        copy(copyBoard);
    }

    @Override
    public Board clone ()
    {
        return new Board(this);
    }

    public void copy (Board board)
    {
        for (byte square = A8; square <= H1; square++)
            squares[square] = board.squares[square];
        sideToMove = board.sideToMove;
        enPassantSquare = board.enPassantSquare;
        castleState = board.castleState;
    }
    
    public void dispose ()
    {
    }

    public long getHash ()
    {
        long hashValue = 0L;
        for (byte square = A8; square <= H1; square++)
        {
            byte piece = getPiece(square);
            if (piece != EMPTY)
                hashValue ^= HASHPIECE[piece][square];
	}
	if (enPassantSquare != INVALIDSQUARE) hashValue ^= HASHEP[enPassantSquare];
	if ((castleState & WHITECASTLESHORT) > 0) hashValue ^= HASHCASTLEWS;
	if ((castleState & WHITECASTLELONG) > 0) hashValue ^= HASHCASTLEWL;
	if ((castleState & BLACKCASTLESHORT) > 0) hashValue ^= HASHCASTLEBS;
	if ((castleState & BLACKCASTLELONG) > 0) hashValue ^= HASHCASTLEBL;
	if (sideToMove == BLACK) hashValue ^= HASHSIDE;
        return hashValue;
    }

    public static Board createStartupBoard ()
    {
        Board position = new Board();
        position.setStartupPosition();
        return position;
    }

    public void putPiece (byte square, byte piece)
    {
        squares[square] = piece;
    }
    
    public byte getPiece (byte square)
    {
        return squares[square];
    }
    
    public void removePiece (byte square)
    {
        squares[square] = EMPTY;
    }
    
    public void clear ()
    {
        for (byte square = A8; square <= H1; square++)
            squares[square] = EMPTY;
    }
    
    public void setStartupPosition ()
    {
        clear();
        
        //Seteo de piezas blancas
        putPiece (A1, WHITEROOK);
        putPiece (B1, WHITEKNIGHT);
        putPiece (C1, WHITEBISHOP);
        putPiece (D1, WHITEQUEEN);
        putPiece (E1, WHITEKING);
        putPiece (F1, WHITEBISHOP);
        putPiece (G1, WHITEKNIGHT);
        putPiece (H1, WHITEROOK);
        putPiece (A2, WHITEPAWN);
        putPiece (B2, WHITEPAWN);
        putPiece (C2, WHITEPAWN);
        putPiece (D2, WHITEPAWN);
        putPiece (E2, WHITEPAWN);
        putPiece (F2, WHITEPAWN);
        putPiece (G2, WHITEPAWN);
        putPiece (H2, WHITEPAWN);
        
        //Seteo de piezas negras
        putPiece (A8, BLACKROOK);
        putPiece (B8, BLACKKNIGHT);
        putPiece (C8, BLACKBISHOP);
        putPiece (D8, BLACKQUEEN);
        putPiece (E8, BLACKKING);
        putPiece (F8, BLACKBISHOP);
        putPiece (G8, BLACKKNIGHT);
        putPiece (H8, BLACKROOK);
        putPiece (A7, BLACKPAWN);
        putPiece (B7, BLACKPAWN);
        putPiece (C7, BLACKPAWN);
        putPiece (D7, BLACKPAWN);
        putPiece (E7, BLACKPAWN);
        putPiece (F7, BLACKPAWN);
        putPiece (G7, BLACKPAWN);
        putPiece (H7, BLACKPAWN);
        
        //Establecimiento de las variables de estado del tablero
        sideToMove = WHITE;
        enPassantSquare = INVALIDSQUARE;
        castleState = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLESHORT | BLACKCASTLELONG;
    }
    
    public byte getCastleState()
    {
        return castleState;
    }

    public void setCastleState(byte castleState)
    {
        this.castleState = castleState;
    }
    
    public byte getEnPassantSquare()
    {
        return enPassantSquare;
    }

    public void setEnPassantSquare(byte enPassantSquare)
    {
        this.enPassantSquare = enPassantSquare;
    }

    public byte getSideToMove()
    {
        return sideToMove;
    }

    public void setSideToMove(byte sideToMove)
    {
        this.sideToMove = sideToMove;
    }
    
    public byte getKingSquare (byte side)
    {
        byte kingSquare = INVALIDSQUARE;
        byte kingPiece = side == WHITE? WHITEKING : BLACKKING;
        for (byte square = A8; square <= H1; square++)
        {
            if (getPiece(square) == kingPiece)
            {
                kingSquare = square;
                break;
            }
        }
        return kingSquare;
    }
    
    public boolean inCheck ()
    {
        return inCheck(sideToMove);
    }
    
    public boolean inCheck (byte side)
    {
        byte kingSquare = getKingSquare(side);
        return (kingSquare != INVALIDSQUARE)? isSquareAttacked(kingSquare, getOppositeSide(side)) : false;
    }
    
    public boolean inCheckMate ()
    {
        return inCheck() && getLegalMoves().size() == 0;
    }

    public boolean inStaleMate ()
    {
        return !inCheck() && getLegalMoves().size() == 0;
    }
        
    public boolean isSquareAttacked (byte square, byte side)
    {
        for (byte testSquare = A8; testSquare <= H1; testSquare++)
        {
            byte squareSide = getSquareSide(testSquare);
            if (squareSide == side)
            {
                byte squareFigure = getSquareFigure(testSquare);
                if (squareFigure == PAWN)
                {
                    if (side == WHITE)
                    {
                        if (getOffsetSquare(testSquare, (byte)-1, (byte)1) == square)
                            return true;
                        if (getOffsetSquare(testSquare, (byte)1, (byte)1) == square)
                            return true;
                    }
                    else 
                    {
                        if (getOffsetSquare(testSquare, (byte)-1, (byte)-1) == square)
                            return true;
                        if (getOffsetSquare(testSquare, (byte)1, (byte)-1) == square)
                            return true;
                    }
                }
                else
                {
                    for (byte direction = 0; direction < FIGUREOFFSETSCOUNT[squareFigure]; direction++)
                    {
                        byte offsetSquare = testSquare;
                        while (true)
                        {
                            offsetSquare = getOffsetSquare(offsetSquare, FIGUREOFFSETS[squareFigure][direction][0], FIGUREOFFSETS[squareFigure][direction][1]);
                            if (offsetSquare == INVALIDSQUARE)
                                break;
                            if (offsetSquare == square)
                                return true;
                            if (getPiece(offsetSquare) != EMPTY)
                                break;
                            if (squareFigure == KING || squareFigure == KNIGHT)
                                break;
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<Move> getLegalMoves ()
    {
        //Obtención de Movimientos Pseudo Legales
        List<Move> moves = getPseudoLegalMoves();
        
        //Loop por los movimientos a ver si son legales 
        Board testBoard = clone();
        for (int i = (moves.size() - 1); i >= 0; i--)
        {
            testBoard.makeMove(moves.get(i));
            if (testBoard.inCheck(sideToMove))
                moves.remove(i);
            testBoard.copy(this);
        }
        return moves;
    }
    
    public List<Move> getPseudoLegalMoves ()
    {
        List<Move> moves = new ArrayList<Move>();
        
        //Obtención de movimientos normales de peones y piezas grandes
        for (byte testSquare = A8; testSquare <= H1; testSquare++)
        {
            byte squareSide = getSquareSide(testSquare);
            if (squareSide == sideToMove)
            {
                byte squareFigure = getSquareFigure(testSquare);
                if (squareFigure == PAWN)
                {
                    if (sideToMove == WHITE)
                    {
                        byte upLeftSquare = getOffsetSquare(testSquare, LEFT_UP[0], LEFT_UP[1]);
                        if (upLeftSquare != INVALIDSQUARE && (upLeftSquare == enPassantSquare || getSquareSide(upLeftSquare) == BLACK))
                            moves.add (new Move(testSquare, upLeftSquare));
                        
                        byte upRightSquare = getOffsetSquare(testSquare, RIGHT_UP[0], RIGHT_UP[1]);
                        if (upRightSquare != INVALIDSQUARE && (upRightSquare == enPassantSquare || getSquareSide(upRightSquare) == BLACK))
                            moves.add (new Move(testSquare, upRightSquare));
                        
                        byte upSquare = getOffsetSquare(testSquare, UP[0], UP[1]);
                        if (upSquare != INVALIDSQUARE && getPiece(upSquare) == EMPTY)
                        {
                            moves.add (new Move(testSquare, upSquare));
                            if (getSquareRank(testSquare) == RANK_2)
                            {
                                byte up2Square = getOffsetSquare(testSquare, UP2[0], UP2[1]);
                                if (getPiece(up2Square) == EMPTY)
                                    moves.add(new Move(testSquare, up2Square));
                            }
                        }
                    }
                    else
                    {
                        byte downLeftSquare = getOffsetSquare(testSquare, LEFT_DOWN[0], LEFT_DOWN[1]);
                        if (downLeftSquare != INVALIDSQUARE && (downLeftSquare == enPassantSquare || getSquareSide(downLeftSquare) == WHITE))
                            moves.add (new Move(testSquare, downLeftSquare));
                        
                        byte downRightSquare = getOffsetSquare(testSquare, RIGHT_DOWN[0], RIGHT_DOWN[1]);
                        if (downRightSquare != INVALIDSQUARE && (downRightSquare == enPassantSquare || getSquareSide(downRightSquare) == WHITE))
                            moves.add (new Move(testSquare, downRightSquare));
                        
                        byte downSquare = getOffsetSquare(testSquare, DOWN[0], DOWN[1]);
                        if (downSquare != INVALIDSQUARE && getPiece(downSquare) == EMPTY)
                        {
                            moves.add (new Move(testSquare, downSquare));
                            if (getSquareRank(testSquare) == RANK_7)
                            {
                                byte down2Square = getOffsetSquare(testSquare, DOWN2[0], DOWN2[1]);
                                if (getPiece(down2Square) == EMPTY)
                                    moves.add(new Move(testSquare, down2Square));
                            }
                        }
                    }
                }
                else
                {
                    for (byte direction = 0; direction < FIGUREOFFSETSCOUNT[squareFigure]; direction++)
                    {
                        byte offsetSquare = testSquare;
                        while (true)
                        {
                            offsetSquare = getOffsetSquare(offsetSquare, FIGUREOFFSETS[squareFigure][direction][0], FIGUREOFFSETS[squareFigure][direction][1]);
                            if (offsetSquare == INVALIDSQUARE)
                                break;
                            byte capturePiece = getPiece(offsetSquare);
                            if (capturePiece != EMPTY)
                            {
                                if (getPieceSide(capturePiece) == getOppositeSide(sideToMove))
                                    moves.add(new Move(testSquare, offsetSquare));
                                break;
                            }
                            moves.add(new Move(testSquare, offsetSquare));
                            if (squareFigure == KING || squareFigure == KNIGHT)
                                break;
                        }
                    }
                }
            }
        }
        
        //Obtención de movimientos de enroque
        if (sideToMove == WHITE) 
        {
            if ((castleState & (WHITECASTLELONG | WHITECASTLESHORT)) > 0)
            {
                if (!inCheck())
                {
                    if ((castleState & WHITECASTLESHORT) > 0)
                        if (getPiece(F1) == EMPTY && getPiece(G1) == EMPTY && !isSquareAttacked(F1, BLACK) && !isSquareAttacked(G1, BLACK))
                            moves.add(new Move(E1, G1));
                    if ((castleState & WHITECASTLELONG) > 0)
                        if (getPiece(B1) == EMPTY && getPiece(C1) == EMPTY && getPiece(D1) == EMPTY && !isSquareAttacked(C1, BLACK) && !isSquareAttacked(D1, BLACK))
                            moves.add(new Move(E1, C1));
                }
            }
        }
        else 
        {
            if ((castleState & (BLACKCASTLELONG | BLACKCASTLESHORT)) > 0)
            {
                if (!inCheck())
                {
                    if ((castleState & BLACKCASTLESHORT) > 0)
                        if (getPiece(F8) == EMPTY && getPiece(G8) == EMPTY && !isSquareAttacked(F8, WHITE) && !isSquareAttacked(G8, WHITE))
                            moves.add(new Move(E8, G8));
                    if ((castleState & BLACKCASTLELONG) > 0)
                        if (getPiece(B8) == EMPTY && getPiece(C8) == EMPTY && getPiece(D8) == EMPTY && !isSquareAttacked(C8, WHITE) && !isSquareAttacked(D8, WHITE))
                            moves.add(new Move(E8, C8));
                }
            }
        }
        
        return moves;
    }
    
    public List<Move> getCaptureMoves ()
    {
        List<Move> moves = new ArrayList<Move>();
        
        //Obtención de movimientos normales de peones y piezas grandes
        for (byte testSquare = A8; testSquare <= H1; testSquare++)
        {
            byte squareSide = getSquareSide(testSquare);
            if (squareSide == sideToMove)
            {
                byte squareFigure = getSquareFigure(testSquare);
                if (squareFigure == PAWN)
                {
                    if (sideToMove == WHITE)
                    {
                        byte upLeftSquare = getOffsetSquare(testSquare, LEFT_UP[0], LEFT_UP[1]);
                        if (upLeftSquare != INVALIDSQUARE && (upLeftSquare == enPassantSquare || getSquareSide(upLeftSquare) == BLACK))
                            moves.add (new Move(testSquare, upLeftSquare));
                        
                        byte upRightSquare = getOffsetSquare(testSquare, RIGHT_UP[0], RIGHT_UP[1]);
                        if (upRightSquare != INVALIDSQUARE && (upRightSquare == enPassantSquare || getSquareSide(upRightSquare) == BLACK))
                            moves.add (new Move(testSquare, upRightSquare));
                    }
                    else
                    {
                        byte downLeftSquare = getOffsetSquare(testSquare, LEFT_DOWN[0], LEFT_DOWN[1]);
                        if (downLeftSquare != INVALIDSQUARE && (downLeftSquare == enPassantSquare || getSquareSide(downLeftSquare) == WHITE))
                            moves.add (new Move(testSquare, downLeftSquare));
                        
                        byte downRightSquare = getOffsetSquare(testSquare, RIGHT_DOWN[0], RIGHT_DOWN[1]);
                        if (downRightSquare != INVALIDSQUARE && (downRightSquare == enPassantSquare || getSquareSide(downRightSquare) == WHITE))
                            moves.add (new Move(testSquare, downRightSquare));
                    }
                }
                else
                {
                    for (byte direction = 0; direction < FIGUREOFFSETSCOUNT[squareFigure]; direction++)
                    {
                        byte offsetSquare = testSquare;
                        while (true)
                        {
                            offsetSquare = getOffsetSquare(offsetSquare, FIGUREOFFSETS[squareFigure][direction][0], FIGUREOFFSETS[squareFigure][direction][1]);
                            if (offsetSquare == INVALIDSQUARE)
                                break;
                            byte capturePiece = getPiece(offsetSquare);
                            if (capturePiece != EMPTY)
                            {
                                if (getPieceSide(capturePiece) == getOppositeSide(sideToMove))
                                    moves.add(new Move(testSquare, offsetSquare));
                                break;
                            }
                            if (squareFigure == KING || squareFigure == KNIGHT)
                                break;
                        }
                    }
                }
            }
        }
        
        return moves;
    }
    
    public boolean isMoveValid(Move move)
    {
        boolean isValid = false;
        List<Move> moves = getLegalMoves();
        for (Move validMove : moves)
        {
            if (validMove.equals(move))
            {
                isValid = true;
                break;
            }
        }
        return isValid;
    }
    
    public void makeMove(Move move)
    {
        byte movingPiece = getPiece(move.getInitialSquare());
        byte movingFigure = getPieceFigure(movingPiece);

        //Promoción y Captura al Paso
        if (movingFigure == PAWN)
        {
            if (sideToMove == WHITE)
            {
                if (getSquareRank(move.getEndSquare()) == RANK_8)
                    movingPiece = WHITEQUEEN;

                if (move.getEndSquare() == enPassantSquare)
                    removePiece(getOffsetSquare(move.getEndSquare(), DOWN[0], DOWN[1]));
            }
            else
            {
                if (getSquareRank(move.getEndSquare()) == RANK_1)
                    movingPiece = BLACKQUEEN;

                if (move.getEndSquare() == enPassantSquare)
                    removePiece(getOffsetSquare(move.getEndSquare(), UP[0], UP[1]));
            }

            enPassantSquare = (Math.abs(move.getInitialSquare() - move.getEndSquare()) == 16)? (byte)((move.getInitialSquare() + move.getEndSquare()) / 2) : INVALIDSQUARE;
        }
        else
        {
            enPassantSquare = INVALIDSQUARE;
        }
        
        //Enroques
        if (movingFigure == KING)
        {
            if (move.getInitialSquare() == E1)
            {
                switch (move.getEndSquare())
                {
                    case G1:
                        removePiece(H1);
                        putPiece(F1, WHITEROOK);
                        break;
                    case C1:
                        removePiece(A1);
                        putPiece(D1, WHITEROOK);
                        break;
                }
            }
            else if (move.getInitialSquare() == E8)
            {
                switch (move.getEndSquare())
                {
                    case G8:
                        removePiece(H8);
                        putPiece(F8, BLACKROOK);
                        break;
                    case C8:
                        removePiece(A8);
                        putPiece(D8, BLACKROOK);
                        break;
                }
            }
        }
        
        //Generación del movimiento
        putPiece(move.getEndSquare(), movingPiece);
        removePiece(move.getInitialSquare());
        
        //Modificación del estado del enroque
        castleState &= CASTLEMASK[move.getInitialSquare()] & CASTLEMASK[move.getEndSquare()];
        
        //Modificación del color a mover
        sideToMove = getOppositeSide(sideToMove);
    }
    
    public static byte getSquareRank (byte square)
    {
        return (byte)(square >>> 3);
    }
    
    public static byte getSquareFile (byte square)
    {
        return (byte)(square & 7);
    }
    
    public static byte getSquare (byte file, byte rank)
    {
        return (byte)((rank * 8) + file);
    }

    public static byte getSquare (String squareString)
    {
        return getSquare(getFile(squareString.charAt(0)), getRank(squareString.charAt(1)));
    }

    public static String getSquareString (byte square)
    {
        return String.valueOf(getFileChar(Board.getSquareFile(square))) + String.valueOf(getRankChar(Board.getSquareRank(square)));
    }
    
    public static byte getFile (char fileChar)
    {
        byte file = -1;
        switch (fileChar)
        {
            case 'a': file = FILE_A; break;
            case 'b': file = FILE_B; break;
            case 'c': file = FILE_C; break;
            case 'd': file = FILE_D; break;
            case 'e': file = FILE_E; break;
            case 'f': file = FILE_F; break;
            case 'g': file = FILE_G; break;
            case 'h': file = FILE_H; break;
        }
        return file;
    }
    
    public static byte getRank (char rankChar)
    {
        byte rank = -1;
        switch (rankChar)
        {
            case '1': rank = RANK_1; break;
            case '2': rank = RANK_2; break;
            case '3': rank = RANK_3; break;
            case '4': rank = RANK_4; break;
            case '5': rank = RANK_5; break;
            case '6': rank = RANK_6; break;
            case '7': rank = RANK_7; break;
            case '8': rank = RANK_8; break;
        }
        return rank;
    }
    
    public static char getFileChar (byte file)
    {
        switch (file)
        {
            case FILE_A: return 'a';
            case FILE_B: return 'b'; 
            case FILE_C: return 'c'; 
            case FILE_D: return 'd'; 
            case FILE_E: return 'e'; 
            case FILE_F: return 'f';
            case FILE_G: return 'g';
            case FILE_H: return 'h'; 
        }
        return ' ';
    }
    
    public static char getRankChar (byte rank)
    {
        switch (rank)
        {
            case RANK_1: return '1';
            case RANK_2: return '2'; 
            case RANK_3: return '3'; 
            case RANK_4: return '4'; 
            case RANK_5: return '5';
            case RANK_6: return '6'; 
            case RANK_7: return '7'; 
            case RANK_8: return '8'; 
        }
        return ' ';
    }
    
    public static char getFigureChar (byte figure)
    {
        switch (figure)
        {
            case PAWN: return 'P';
            case KNIGHT: return 'N';
            case BISHOP: return 'B';
            case ROOK: return 'R';
            case QUEEN: return 'Q';
            case KING: return 'K';
        }
        return ' ';
    }
    
    public static char getPieceChar (byte piece)
    {
        char figureChar = getFigureChar(getPieceFigure(piece));
        if (piece > BLACK)
            figureChar = (String.valueOf(figureChar).toLowerCase()).charAt(0); 
        return figureChar;
    }

    public static byte getOffsetSquare (byte square, byte horizontalOffset, byte verticalOffset)
    {
        byte file = (byte)(getSquareFile(square) + horizontalOffset);
        byte rank = (byte)(getSquareRank(square) - verticalOffset);
        return (file >= 0 && file < 8 && rank >= 0 && rank < 8)? getSquare(file, rank) : INVALIDSQUARE;
    }
    
    public static byte getOppositeSide (byte side)
    {
        return side == WHITE? BLACK : WHITE;
    }
    
    public static byte getPieceSide (byte piece)
    {
        return piece >= BLACK? BLACK : WHITE;
    }

    public static byte getPieceFigure (byte piece)
    {
        return (piece >= BLACK)? piece -= BLACK : piece;
    }

    public byte getSquareSide (byte square)
    {
        byte squareValue = squares[square];
        return squareValue == EMPTY? NOSIDE : ((squareValue >= BLACK)? BLACK : WHITE);
    }
    
    public byte getSquareFigure (byte square)
    {
        byte figure = squares[square];
        return (figure >= BLACK)? figure -= BLACK : figure;
    }

    public void print()
    {
        print(false);
    }
    
    public void print(boolean flipped)
    {
        System.out.println("  .---.---.---.---.---.---.---.---.");
        for (byte rank = RANK_8; rank <= RANK_1; rank++)
        {
            byte printRank = flipped? (byte)(7 - rank) : rank;
            System.out.print (getRankChar(printRank));
            System.out.print (' ');
            System.out.print('|');
            for (byte file = FILE_A; file <= FILE_H; file++)
            {
                byte printFile = flipped? (byte)(7 - file) : file;
                byte square = getSquare(printFile, printRank);
                byte piece = getPiece(square);
                System.out.print (' ');
                System.out.print (getPieceChar(piece));
                System.out.print (' ');                
                System.out.print('|');
            }
            System.out.println();
            System.out.println("  .---.---.---.---.---.---.---.---.");
        }
        if (!flipped)
            System.out.println("    A   B   C   D   E   F   G   H  ");
        else
            System.out.println("    H   G   F   E   D   C   B   A  ");
    }
    
    public static void main(final String[] args)
    {
        System.gc();
        long iniTime = System.currentTimeMillis();
        Board board = new Board();
        board.setStartupPosition();
        Board testBoard = board.clone();
        for (int i = 0; i < 100000; i++)
        {
            board.makeMove(new Move(E2, E4));
            board.makeMove(new Move(E7, E6));
            List<Move> moves = board.getLegalMoves();
            board.copy(testBoard);
        }
        long endTime = System.currentTimeMillis();
        System.out.println (endTime - iniTime);
    }
}
