
package org.neochess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.neochess.general.Disposable;
import org.neochess.util.BoardUtils;

public class Board implements Disposable, Cloneable
{
    public static final byte NOSIDE = Byte.MAX_VALUE;
    public static final byte WHITE = 0;
    public static final byte BLACK = 1;
    
    public static final byte EMPTY = Byte.MAX_VALUE;
    public static final byte PAWN = 0;
    public static final byte KNIGHT = 1;
    public static final byte BISHOP = 2;
    public static final byte ROOK = 3;
    public static final byte QUEEN = 4;
    public static final byte KING = 5;
    public static final byte BPAWN = 6;
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
    
    public static final byte INVALIDSQUARE = Byte.MAX_VALUE;
    public static final byte A1 = 0;
    public static final byte B1 = 1;
    public static final byte C1 = 2;
    public static final byte D1 = 3;
    public static final byte E1 = 4;
    public static final byte F1 = 5;
    public static final byte G1 = 6;
    public static final byte H1 = 7;
    public static final byte A2 = 8;
    public static final byte B2 = 9;
    public static final byte C2 = 10;
    public static final byte D2 = 11;
    public static final byte E2 = 12;
    public static final byte F2 = 13;
    public static final byte G2 = 14;
    public static final byte H2 = 15;
    public static final byte A3 = 16;
    public static final byte B3 = 17;
    public static final byte C3 = 18;
    public static final byte D3 = 19;
    public static final byte E3 = 20;
    public static final byte F3 = 21;
    public static final byte G3 = 22;
    public static final byte H3 = 23;
    public static final byte A4 = 24;
    public static final byte B4 = 25;
    public static final byte C4 = 26;
    public static final byte D4 = 27;
    public static final byte E4 = 28;
    public static final byte F4 = 29;
    public static final byte G4 = 30;
    public static final byte H4 = 31;
    public static final byte A5 = 32;
    public static final byte B5 = 33;
    public static final byte C5 = 34;
    public static final byte D5 = 35;
    public static final byte E5 = 36;
    public static final byte F5 = 37;
    public static final byte G5 = 38;
    public static final byte H5 = 39;
    public static final byte A6 = 40;
    public static final byte B6 = 41;
    public static final byte C6 = 42;
    public static final byte D6 = 43;
    public static final byte E6 = 44;
    public static final byte F6 = 45;
    public static final byte G6 = 46;
    public static final byte H6 = 47;
    public static final byte A7 = 48;
    public static final byte B7 = 49;
    public static final byte C7 = 50;
    public static final byte D7 = 51;
    public static final byte E7 = 52;
    public static final byte F7 = 53;
    public static final byte G7 = 54;
    public static final byte H7 = 55;
    public static final byte A8 = 56;
    public static final byte B8 = 57;
    public static final byte C8 = 58;
    public static final byte D8 = 59;
    public static final byte E8 = 60;
    public static final byte F8 = 61;
    public static final byte G8 = 62;
    public static final byte H8 = 63;
    
    public static final byte FILE_A = 0;
    public static final byte FILE_B = 1;
    public static final byte FILE_C = 2;
    public static final byte FILE_D = 3;
    public static final byte FILE_E = 4;
    public static final byte FILE_F = 5;
    public static final byte FILE_G = 6;
    public static final byte FILE_H = 7;
    public static final byte RANK_1 = 0;
    public static final byte RANK_2 = 1;
    public static final byte RANK_3 = 2;
    public static final byte RANK_4 = 3;
    public static final byte RANK_5 = 4;
    public static final byte RANK_6 = 5;
    public static final byte RANK_7 = 6;
    public static final byte RANK_8 = 7;
    
    private static final byte NOCASTLE = 0;
    private static final byte WHITECASTLESHORT = 1;
    private static final byte WHITECASTLELONG = 2;
    private static final byte BLACKCASTLESHORT = 4;
    private static final byte BLACKCASTLELONG = 8;
    private static final byte[] CASTLEMASK;
    
    private static final long HASHPIECE[][] = new long[12][64];
    private static final long HASHSIDE;
    private static final long HASHEP[] = new long[64];
    private static final long HASHCASTLEWS;
    private static final long HASHCASTLEWL;
    private static final long HASHCASTLEBS;
    private static final long HASHCASTLEBL;
    
    private byte[] squareSide = new byte[64];
    private byte[] squareFigure = new byte[64];
    private byte epSquare;    
    private byte castleState;
    private byte sideToMove;
    private long[][] pieces = new long[2][6];
    private long friends[] = new long[2];
    private long blocker;
    private long blockerr90;
    private long blockerr45;
    private long blockerr315;  

    static
    {
        CASTLEMASK = new byte[64];
        for (byte square = A1; square <= H8; square++)
            CASTLEMASK[square] = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[A1] = WHITECASTLESHORT | BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[H1] = WHITECASTLELONG | BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[E1] = BLACKCASTLESHORT | BLACKCASTLELONG;
        CASTLEMASK[A8] = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLESHORT;
        CASTLEMASK[H8] = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLELONG;
        CASTLEMASK[E8] = WHITECASTLESHORT | WHITECASTLELONG;

        Random randomGenerator = new Random(0);
        for (byte piece = WHITEPAWN; piece <= BLACKKING; piece++)
            for (byte square = A1; square <= H8; square++)
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
    public void dispose ()
    {
    }
    
    @Override
    public Board clone ()
    {
        return new Board(this);
    }
    
    public void copy (Board board)
    {
        for (byte square = A1; square <= H8; square++)
        {
            squareSide[square] = board.squareSide[square];
            squareFigure[square] = board.squareFigure[square];
        }
        for (byte figure = PAWN; figure <= KING; figure++)
        {
            pieces[WHITE][figure] = board.pieces[WHITE][figure];
            pieces[BLACK][figure] = board.pieces[BLACK][figure];
        }
        friends[WHITE] = board.friends[WHITE];
        friends[BLACK] = board.friends[BLACK];
        blocker = board.blocker;
        blockerr90 = board.blockerr90;
        blockerr45 = board.blockerr45;
        blockerr315 = board.blockerr315;
        epSquare = board.epSquare;
        castleState = board.castleState;
        sideToMove = board.sideToMove;
    }
    
    public long getHash ()
    {
        long hashValue = 0L;
        for (byte square = A1; square <= H8; square++)
        {
            byte piece = getPiece(square);
            if (piece != EMPTY)
                hashValue ^= HASHPIECE[piece][square];
        }
        if (epSquare != INVALIDSQUARE) hashValue ^= HASHEP[epSquare];
        if ((castleState & WHITECASTLESHORT) > 0) hashValue ^= HASHCASTLEWS;
        if ((castleState & WHITECASTLELONG) > 0) hashValue ^= HASHCASTLEWL;
        if ((castleState & BLACKCASTLESHORT) > 0) hashValue ^= HASHCASTLEBS;
        if ((castleState & BLACKCASTLELONG) > 0) hashValue ^= HASHCASTLEBL;
        if (sideToMove == BLACK) hashValue ^= HASHSIDE;
        return hashValue;
    }
    
    public void putPiece (byte square, byte piece)
    {
        byte pieceSide = getPieceSide(piece);
        byte pieceFigure = getPieceFigure(piece);   
        if (squareSide[square] == NOSIDE)
        {
            blocker |= BoardUtils.squareBit[square];
            blockerr90 |= BoardUtils.squareBit90[square];
            blockerr45 |= BoardUtils.squareBit45[square];
            blockerr315 |= BoardUtils.squareBit315[square];
            friends[pieceSide] |= BoardUtils.squareBit[square];
        }
        else
        {
            if (pieceSide != squareSide[square])
            {
                friends[squareSide[square]] &= BoardUtils.squareBitX[square];
                friends[pieceSide] |= BoardUtils.squareBit[square];
            }
            pieces[squareSide[square]][squareFigure[square]] &= BoardUtils.squareBitX[square];
        }
        
        pieces[pieceSide][pieceFigure] |= BoardUtils.squareBit[square];
        squareSide[square] = pieceSide;
        squareFigure[square] = pieceFigure;
    }
    
    public byte getPiece (byte square)
    {   
        return squareSide[square] == NOSIDE? EMPTY : (byte)((squareSide[square]*6) + squareFigure[square]);
    }
    
    public void removePiece (byte square)
    {
        blocker &= BoardUtils.squareBitX[square];
        blockerr90 &= BoardUtils.squareBitX90[square];
        blockerr45 &= BoardUtils.squareBitX45[square];
        blockerr315 &= BoardUtils.squareBitX315[square];
        friends[squareSide[square]] &= BoardUtils.squareBitX[square];
        pieces[squareSide[square]][squareFigure[square]] &= BoardUtils.squareBitX[square];
        squareSide[square] = NOSIDE;
        squareFigure[square] = EMPTY;
    }
    
    public void clear ()
    {
        for (byte square = A1; square <= H8; square++)
        {
            squareSide[square] = NOSIDE;
            squareFigure[square] = EMPTY;
        }   
        for (byte side = WHITE; side <= BLACK; side++)
        {
            friends[side] = BoardUtils.NULLBITBOARD;
            for (byte figure = PAWN; figure <= KING; figure++)
            {
                pieces[side][figure] = BoardUtils.NULLBITBOARD;
            }
        }
        blocker = BoardUtils.NULLBITBOARD;
        blockerr90 = BoardUtils.NULLBITBOARD;
        blockerr45 = BoardUtils.NULLBITBOARD;
        blockerr315 = BoardUtils.NULLBITBOARD; 
        epSquare = INVALIDSQUARE;
        castleState = NOCASTLE;
        sideToMove = NOSIDE;
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
        epSquare = INVALIDSQUARE;
        castleState = WHITECASTLESHORT | WHITECASTLELONG | BLACKCASTLESHORT | BLACKCASTLELONG;
    }
    
    public String getFenPosition ()
    {
        String fen = "";
        for (byte rank = RANK_8; rank >= RANK_1; rank--)
        {
            for (byte file = FILE_A; file <= FILE_H; file++)
            {
                byte square = getSquare(file, rank);
                byte piece = getPiece(square);
                switch (piece)
                {
                    case EMPTY: 
                        int spaceCounter = 1;
                        while ((++file) <= FILE_H)
                        {
                            byte testSquare = getSquare(file, rank);
                            if (getPiece(testSquare) == EMPTY)
                                spaceCounter++;
                            else
                                break;
                        }
                        fen += String.valueOf(spaceCounter);
                        file--;
                        break;
                    default:
                        fen += getPieceChar(piece);
                        break;
                }
            }
            if (rank > RANK_1)
                fen += "/";
        }
        
        fen += " ";
        fen += getSideToMove() == WHITE? "w" : "b";
        fen += " ";
        if (castleState != 0)
        {
            if ((castleState & WHITECASTLESHORT) > 0) fen += "K";
            if ((castleState & WHITECASTLELONG) > 0) fen += "Q";
            if ((castleState & BLACKCASTLESHORT) > 0) fen += "k";
            if ((castleState & BLACKCASTLELONG) > 0) fen += "q";
        }
        else
        {
            fen += "-";
        }
        fen += " ";
        fen += epSquare != INVALIDSQUARE? getSquareString(epSquare) : "-";
        return fen;
    }
    
    public void setFenPosition (String fen) 
    {
        clear();
        byte i,s;
        char c;
        epSquare = INVALIDSQUARE;
        i=0;
        s=56;
        c = fen.charAt(0);
        while (c != ' ') 
        {
            switch (c) 
            {
                case '/': s-=16; break;
                case '1': s+=1;  break;
                case '2': s+=2;  break;
                case '3': s+=3;  break;
                case '4': s+=4;  break;
                case '5': s+=5;  break;
                case '6': s+=6;  break;
                case '7': s+=7;  break;
                case '8': s+=8;  break;		
                case 'p': putPiece (s, BLACKPAWN); s++; break;
                case 'n': putPiece (s, BLACKKNIGHT); s++; break;
                case 'b': putPiece (s, BLACKBISHOP); s++; break;
                case 'r': putPiece (s, BLACKROOK); s++; break;
                case 'q': putPiece (s, BLACKQUEEN); s++; break;
                case 'k': putPiece (s, BLACKKING); s++; break;
                case 'P': putPiece (s, WHITEPAWN); s++; break;
                case 'N': putPiece (s, WHITEKNIGHT); s++; break;
                case 'B': putPiece (s, WHITEBISHOP); s++; break;
                case 'R': putPiece (s, WHITEROOK); s++; break;
                case 'Q': putPiece (s, WHITEQUEEN); s++; break;
                case 'K': putPiece (s, WHITEKING); s++; break;        
            }	
            c = fen.charAt(++i);
        }
        c = fen.charAt(++i);
        if (c == 'w') sideToMove = WHITE;	
        else if (c == 'b') sideToMove = BLACK;	
        i+=2;
        castleState = 0;
        if (i < fen.length()) 
        {
            c = fen.charAt(i);
            while(c!=' ') 
            {
                if ( c == 'K') castleState |= WHITECASTLESHORT;
                else if ( c == 'Q') castleState |= WHITECASTLELONG;
                else if ( c == 'k') castleState |= BLACKCASTLESHORT;
                else if ( c == 'q') castleState |= BLACKCASTLELONG;	
                c = fen.charAt(++i);
            }		
        }
        
        String remainingText = fen.substring(++i);
        epSquare = (!remainingText.equals("-"))? getSquare(remainingText) : INVALIDSQUARE;
    }

    public long getBlocker ()
    {
        return blocker;
    }

    public long getBlockerr315 ()
    {
        return blockerr315;
    }

    public long getBlockerr45 ()
    {
        return blockerr45;
    }

    public long getBlockerr90 ()
    {
        return blockerr90;
    }

    public long[] getFriends ()
    {
        return friends;
    }

    public long[][] getPieces ()
    {
        return pieces;
    }
    
    public byte getSquareSide (byte square)
    {
        return squareSide[square];
    }
    
    public byte getSquareFigure (byte square)
    {
        return squareFigure[square];
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
        return epSquare;
    }

    public void setEnPassantSquare(byte enPassantSquare)
    {
        this.epSquare = enPassantSquare;
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
        return pieces[side][KING] != BoardUtils.NULLBITBOARD? (byte)BoardUtils.getLeastSignificantBit(pieces[side][KING]) : INVALIDSQUARE;
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
    
    public void makeMove (Move move)
    {
        byte initialSquare = move.getInitialSquare();
        byte endSquare = move.getEndSquare();
        byte movingPiece = getPiece(initialSquare);
        byte movingFigure = getPieceFigure(movingPiece);
        byte capturedPiece = getPiece(endSquare);
        move.setFlags(movingPiece | (capturedPiece << 8) | (castleState << 16) | (epSquare << 24));
        if (movingFigure == PAWN)
        {
            if (sideToMove == WHITE)
            {
                if (getSquareRank(endSquare) == RANK_8)
                    movingPiece = WHITEQUEEN;
                else if (endSquare == epSquare)
                    removePiece((byte)(endSquare-8));
            }
            else
            {
                if (getSquareRank(endSquare) == RANK_1)
                    movingPiece = BLACKQUEEN;
                else if (endSquare == epSquare)
                    removePiece((byte)(endSquare+8));
            }
            epSquare = (Math.abs(initialSquare - endSquare) == 16)? (byte)((initialSquare + endSquare) / 2) : INVALIDSQUARE;
        }
        else
        {
            if (movingFigure == KING)
            {
                if (initialSquare == E1)
                {
                    switch (endSquare)
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
                else if (initialSquare == E8)
                {
                    switch (endSquare)
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
            epSquare = INVALIDSQUARE;
        }
        removePiece(initialSquare);
        putPiece(endSquare, movingPiece);
        castleState &= CASTLEMASK[initialSquare] & CASTLEMASK[endSquare];
        sideToMove = getOppositeSide(sideToMove);
    }
    
    public void unmakeMove (Move move)
    {
        byte initialSquare = move.getInitialSquare();
        byte endSquare = move.getEndSquare();
        int flags = move.getFlags();
        byte movingPiece = (byte)(flags & 0xFF);
        byte movingFigure = getPieceFigure(movingPiece);
        byte movingSide = getPieceSide(movingPiece);
        byte capturedPiece = (byte)((flags & 0xFF00) >> 8);
        if (capturedPiece == 255)
            capturedPiece = EMPTY;
        byte lastCastleState = (byte)((flags & 0xFF0000) >> 16);
        byte lastEpSquare = (byte)((flags & 0xFF000000) >> 24);
        if (lastEpSquare == 255)
            lastEpSquare = INVALIDSQUARE;
        
        if (movingFigure == PAWN)
        {
            if (endSquare == lastEpSquare)
            {
                if (movingSide == WHITE)
                    putPiece((byte)(endSquare-8), BLACKPAWN);
                else
                    putPiece((byte)(endSquare+8), WHITEPAWN);
            }
        }
        else if (movingFigure == KING)
        {
            if (initialSquare == E1)
            {
                switch (endSquare)
                {
                    case G1:
                        removePiece(F1);
                        putPiece(H1, WHITEROOK);
                        break;
                    case C1:
                        removePiece(D1);
                        putPiece(A1, WHITEROOK);
                        break;
                }
            }
            else if (initialSquare == E8)
            {
                switch (endSquare)
                {
                    case G8:
                        removePiece(F8);
                        putPiece(H8, BLACKROOK);
                        break;
                    case C8:
                        removePiece(D8);
                        putPiece(A8, BLACKROOK);
                        break;
                }
            }
        }
        if (capturedPiece != EMPTY)
            putPiece(endSquare, capturedPiece);
        else
            removePiece(endSquare);
        putPiece(initialSquare, movingPiece);
        epSquare = lastEpSquare;
        castleState = lastCastleState;
        sideToMove = movingSide;
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
    
    public List<Move> getPseudoLegalMoves ()
    {
        List<Move> moveList = new ArrayList<Move>();
        getPseudoLegalMoves (moveList);
        return moveList;
    }
    
    public void getPseudoLegalMoves (List<Move> moveList)
    {
        byte side = sideToMove;
        byte xside = getOppositeSide(side);
        byte piece, fsq, tsq;
        long movers, moves, captures;
        long[] sidePieces = pieces[side];
        long sideFriends = friends[side];
        long notfriends = ~sideFriends;
        long notblocker = ~blocker;
        
        for (piece = KNIGHT; piece <= KING; piece += 4)
        {
            movers = sidePieces[piece];
            while (movers != 0)
            {
                fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[fsq];
                moves = BoardUtils.moveArray[piece][fsq] & notfriends;
                while (moves != 0)
                {
                    tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                    moves &= BoardUtils.squareBitX[tsq];
                    moveList.add(new Move(fsq, tsq));
                }
            }
        }

        movers = sidePieces[BISHOP];
        while (movers != 0)
        {
            fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[fsq];
            moves = getBishopAttacks(fsq) & notfriends;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move(fsq, tsq));
            }
        }
        
        movers = sidePieces[ROOK];
        while (movers != 0)
        {
            fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[fsq];
            moves = getRookAttacks(fsq) & notfriends;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move(fsq, tsq));
            }
        }
        
        movers = sidePieces[QUEEN];
        while (movers != 0)
        {
            fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[fsq];
            moves = getQueenAttacks(fsq) & notfriends;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move(fsq, tsq));
            }
        }

        captures = (friends[xside] | (epSquare != INVALIDSQUARE? BoardUtils.squareBit[epSquare] : BoardUtils.NULLBITBOARD));
        if (side == WHITE)
        {
            moves = (sidePieces[PAWN] >> 8) & notblocker;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-8), tsq));
            }

            movers = sidePieces[PAWN] & BoardUtils.rankBits[1];
            moves = (movers >> 8) & notblocker;
            moves = (moves >> 8) & notblocker;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-16), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[0];
            moves = (movers >> 7) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-7), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[7]; 		
            moves = (movers >> 9) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-9), tsq));
            }
        }
        else if (side == BLACK)
        {
            moves = (sidePieces[PAWN] << 8) & notblocker;		
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+8), tsq));
            }

            movers = sidePieces[PAWN] & BoardUtils.rankBits[6];
            moves = (movers << 8) & notblocker;
            moves = (moves << 8) & notblocker;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+16), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[7];	
            moves = (movers << 7) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+7), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[0];
            moves = (movers << 9) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+9), tsq));
            }
        }
        
        if (side == WHITE) 
        {
            if ((castleState & WHITECASTLESHORT) > 0)
                if (squareSide[F1] == EMPTY && squareSide[G1] == EMPTY && !inCheck() && !isSquareAttacked(F1, BLACK) && !isSquareAttacked(G1, BLACK))
                    moveList.add(new Move(E1, G1));
            if ((castleState & WHITECASTLELONG) > 0)
                if (squareSide[B1] == EMPTY && squareSide[C1] == EMPTY && squareSide[D1] == EMPTY && !inCheck() && !isSquareAttacked(C1, BLACK) && !isSquareAttacked(D1, BLACK))
                    moveList.add(new Move(E1, C1));
        }
        else 
        {
            if ((castleState & BLACKCASTLESHORT) > 0)
                if (squareSide[F8] == EMPTY && squareSide[G8] == EMPTY && !inCheck() && !isSquareAttacked(F8, WHITE) && !isSquareAttacked(G8, WHITE))
                    moveList.add(new Move(E8, G8));
            if ((castleState & BLACKCASTLELONG) > 0)
                if (squareSide[B8] == EMPTY && squareSide[C8] == EMPTY && squareSide[D8] == EMPTY && !inCheck() && !isSquareAttacked(C8, WHITE) && !isSquareAttacked(D8, WHITE))
                    moveList.add(new Move(E8, C8));
        }
    }
    
    public List<Move> getCaptureMoves ()
    {
        List<Move> moveList = new ArrayList<Move>();
        getCaptureMoves (moveList);
        return moveList;
    }
    
    public void getCaptureMoves (List<Move> moveList)
    {
        byte side = sideToMove;
        byte xside = getOppositeSide(side);
        byte piece, fsq, tsq;
        long movers, moves, captures;
        long[] sidePieces = pieces[side];
        long enemy = friends[xside];
        
        for (piece = KNIGHT; piece <= KING; piece += 4)
        {
            movers = sidePieces[piece];
            while (movers != 0)
            {
                fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[fsq];
                moves = BoardUtils.moveArray[piece][fsq] & enemy;
                while (moves != 0)
                {
                    tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                    moves &= BoardUtils.squareBitX[tsq];
                    moveList.add(new Move(fsq, tsq));
                }
            }
        }

        movers = sidePieces[BISHOP];
        while (movers != 0)
        {
            fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[fsq];
            moves = getBishopAttacks(fsq) & enemy;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move(fsq, tsq));
            }
        }
        
        movers = sidePieces[ROOK];
        while (movers != 0)
        {
            fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[fsq];
            moves = getRookAttacks(fsq) & enemy;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move(fsq, tsq));
            }
        }
        
        movers = sidePieces[QUEEN];
        while (movers != 0)
        {
            fsq = (byte)BoardUtils.getLeastSignificantBit(movers);
            movers &= BoardUtils.squareBitX[fsq];
            moves = getQueenAttacks(fsq) & enemy;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move(fsq, tsq));
            }
        }

        captures = (friends[xside] | (epSquare != INVALIDSQUARE? BoardUtils.squareBit[epSquare] : BoardUtils.NULLBITBOARD));
        if (side == WHITE)
        {
            movers = sidePieces[PAWN] & BoardUtils.rankBits[6];			
            moves = (movers >> 8) & ~blocker;			
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-8), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[0];
            moves = (movers >> 7) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-7), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[7]; 		
            moves = (movers >> 9) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq-9), tsq));
            }
        }
        else if (side == BLACK)
        {
            movers = sidePieces[PAWN] & BoardUtils.rankBits[1];			
            moves = (movers << 8) & ~blocker;		
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+8), tsq));
            }
            
            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[7];	
            moves = (movers << 7) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+7), tsq));
            }

            movers = sidePieces[PAWN] & ~BoardUtils.fileBits[0];
            moves = (movers << 9) & captures;
            while (moves != 0)
            {
                tsq = (byte)BoardUtils.getLeastSignificantBit(moves);
                moves &= BoardUtils.squareBitX[tsq];
                moveList.add(new Move((byte)(tsq+9), tsq));
            }
        }
    }
    
    public List<Move> getEscapeMoves ()
    {
        List<Move> moveList = new ArrayList<Move>();
        getEscapeMoves (moveList);
        return moveList;
    }
    
    public void getEscapeMoves (List<Move> moveList)
    {
        byte side, xside;
        byte kingsq, chksq, sq, sq1, epsq;
        int dir;
        long checkers, b, c, p, escapes;
        escapes = 0;
        side = getSideToMove();
        xside = getOppositeSide(side);
 
        kingsq = getKingSquare(side);
        checkers = getSquareAttackers(kingsq, xside);
        p = pieces[side][PAWN];

        if (BoardUtils.getBitCount(checkers) == 1) 
        {
            chksq = (byte)BoardUtils.getLeastSignificantBit(checkers);
            b = getSquareAttackers(chksq, side);
            b &= ~pieces[side][KING];
            while (b != 0) 
            {
                sq = (byte)BoardUtils.getLeastSignificantBit(b);
                b &= BoardUtils.squareBitX[sq];
                if (!isPinningKing(sq, side)) 
                    moveList.add(new Move(sq, chksq));
            }

            if (getEnPassantSquare() > -1) 
            {
                epsq = getEnPassantSquare();
                if (epsq + (side == WHITE ? -8 : 8) == chksq) 
                {
                    b = BoardUtils.moveArray[xside == WHITE?PAWN:BPAWN][epsq] & p;
                    while (b != 0) 
                    {
                        sq = (byte)BoardUtils.getLeastSignificantBit(b);
                        b &= BoardUtils.squareBitX[sq];
                        if (!isPinningKing(sq, side)) 
                            moveList.add(new Move(sq, epsq));
                    }
                }
            }

            if (BoardUtils.slider[getSquareFigure(chksq)] == 1) 
            {
                c = BoardUtils.fromtoRay[kingsq][chksq] & BoardUtils.squareBitX[chksq];
                while (c != 0) 
                {
                    sq = (byte)BoardUtils.getLeastSignificantBit(c);
                    c &= BoardUtils.squareBitX[sq];
                    b = getSquareAttackers(sq, side);
                    b &= ~(pieces[side][KING] | p);

                    if (side == WHITE && sq > H2) 
                    {
                        if ((BoardUtils.squareBit[sq - 8] & p) != 0) 
                            b |= BoardUtils.squareBit[sq - 8];
                        
                        if (getSquareRank(sq) == 3 && squareSide[sq - 8] == NOSIDE && ((BoardUtils.squareBit[sq - 16] & p) != 0)) 
                            b |= BoardUtils.squareBit[sq - 16];
                    }
                    if (side == BLACK && sq < H7) {
                        if ((BoardUtils.squareBit[sq + 8] & p) != 0) 
                            b |= BoardUtils.squareBit[sq + 8];
                        if (getSquareRank(sq) == 4 && squareSide[sq + 8] == NOSIDE && ((BoardUtils.squareBit[sq + 16] & p) != 0)) 
                            b |= BoardUtils.squareBit[sq + 16];
                    }
                    while (b != 0) 
                    {
                        sq1 = (byte)BoardUtils.getLeastSignificantBit(b);
                        b &= BoardUtils.squareBitX[sq1];
                        if (!isPinningKing(sq1, side)) 
                            moveList.add(new Move(sq1, sq));
                    }
                }
            }
        }

        if (checkers != 0) 
            escapes = BoardUtils.moveArray[KING][kingsq] & ~friends[side];
        
        while (checkers != 0) 
        {
            chksq = (byte)BoardUtils.getLeastSignificantBit(checkers);
            checkers &= BoardUtils.squareBitX[chksq];
            dir = BoardUtils.directions[chksq][kingsq];
            if (BoardUtils.slider[getSquareFigure(chksq)] == 1) 
                escapes &= ~BoardUtils.ray[chksq][dir];
        }
        while (escapes != 0) 
        {
            sq = (byte)BoardUtils.getLeastSignificantBit(escapes);
            escapes &= BoardUtils.squareBitX[sq];
            if (!isSquareAttacked(sq, xside)) 
                moveList.add(new Move(kingsq, sq));
        }
    }
    
    public List<Move> getLegalMoves ()
    {
        List<Move> moveList = new ArrayList<Move>();
        getLegalMoves(moveList);
        return moveList;
    }
    
    public void getLegalMoves (List<Move> moveList)
    {
        getPseudoLegalMoves(moveList);
        for (int i = (moveList.size() - 1); i >= 0; i--)
        {
            Move move = moveList.get(i);
            makeMove(move);
            if (inCheck(getOppositeSide(sideToMove)))
                moveList.remove(i);
            unmakeMove(move);
        }
    }
    
    public boolean isSquareAttacked (byte square, byte side)
    {   
        long[] sidePieces = pieces[side];
        byte oppositeSide = getOppositeSide(side);
        if ((sidePieces[KNIGHT] & BoardUtils.moveArray[KNIGHT][square]) != 0) return true;
        if ((sidePieces[KING] & BoardUtils.moveArray[KING][square]) != 0) return true;
        if ((sidePieces[PAWN] & BoardUtils.moveArray[oppositeSide == WHITE? PAWN : BPAWN][square]) != 0) return true;
        
        long[] c = BoardUtils.fromtoRay[square];
        long b = (sidePieces[BISHOP] | sidePieces[QUEEN]) & BoardUtils.moveArray[BISHOP][square];
        long d = ~b & blocker;
        int t; 
        while (b != 0)
        {
            t = BoardUtils.getLeastSignificantBit(b);
            if ((c[t] & d) == 0)
               return (true);
            b &= BoardUtils.squareBitX[t];
        }
        b = (sidePieces[ROOK] | sidePieces[QUEEN]) & BoardUtils.moveArray[ROOK][square];
        d = ~b & blocker;
        while (b != 0)
        {
            t = BoardUtils.getLeastSignificantBit(b);
            if ((c[t] & d) == 0)
              return (true);
            b &= BoardUtils.squareBitX[t];
        }
        return (false);
    }
    
    public long getSquareAttackers (byte square, byte side)
    {
        byte xside = getOppositeSide(side);
        long[] sidePieces;
        long[] slideMoves;
        long moves, attackers;
        byte t;
        sidePieces = pieces[side];
        attackers = (sidePieces[KNIGHT] & BoardUtils.moveArray[KNIGHT][square]); 	
        attackers |= (sidePieces[KING] & BoardUtils.moveArray[KING][square]); 	
        attackers |= (sidePieces[PAWN] & BoardUtils.moveArray[xside==WHITE?PAWN:BPAWN][square]);
        slideMoves = BoardUtils.fromtoRay[square];
        moves = (sidePieces[BISHOP] | sidePieces[QUEEN]) & BoardUtils.moveArray[BISHOP][square];
        while (moves != 0)
        {
            t = (byte)BoardUtils.getLeastSignificantBit(moves);
            moves &= BoardUtils.squareBitX[t];
            if ((slideMoves[t] & blocker & BoardUtils.squareBitX[t]) == 0)
                attackers |= BoardUtils.squareBit[t];
        }
        moves = (sidePieces[ROOK] | sidePieces[QUEEN]) & BoardUtils.moveArray[ROOK][square];
        while (moves != 0)
        {
            t = (byte)BoardUtils.getLeastSignificantBit(moves);
            moves &= BoardUtils.squareBitX[t];
            if ((slideMoves[t] & blocker & BoardUtils.squareBitX[t]) == 0)
                attackers |= BoardUtils.squareBit[t];
        }
        return attackers;
    }
    
    public long getSquareXAttackers (byte square, byte side)
    {
        byte xside = getOppositeSide(side);
        long[] sidePieces, xsidePieces, slideMoves; 
        long moves, attackers, blocker;
        byte t;
        sidePieces = pieces[side];
        xsidePieces = pieces[xside];
        attackers = (sidePieces[KNIGHT] & BoardUtils.moveArray[KNIGHT][square]); 
        attackers |= (sidePieces[KING] & BoardUtils.moveArray[KING][square]); 
        slideMoves = BoardUtils.fromtoRay[square];
        moves = (sidePieces[PAWN] & BoardUtils.moveArray[xside==WHITE?PAWN:BPAWN][square]);
        blocker = this.blocker;
        blocker &= ~(sidePieces[BISHOP] | sidePieces[QUEEN] | xsidePieces[BISHOP] | xsidePieces[QUEEN] | moves);
        moves |= (sidePieces[BISHOP] | sidePieces[QUEEN]) & BoardUtils.moveArray[BISHOP][square];
        while (moves != 0)
        {
            t = (byte)BoardUtils.getLeastSignificantBit(moves);
            moves &= BoardUtils.squareBitX[t];
            if ((slideMoves[t] & blocker & BoardUtils.squareBitX[t]) == 0)
                attackers |= BoardUtils.squareBit[t];
        }
        moves = (sidePieces[ROOK] | sidePieces[QUEEN]) & BoardUtils.moveArray[ROOK][square];
        blocker = this.blocker;
        blocker &= ~(sidePieces[ROOK] | sidePieces[QUEEN] | xsidePieces[ROOK] | xsidePieces[QUEEN]);
        while (moves != 0)
        {
            t = (byte)BoardUtils.getLeastSignificantBit(moves);
            moves &= BoardUtils.squareBitX[t];
            if ((slideMoves[t] & blocker & BoardUtils.squareBitX[t]) == 0)
                attackers |= BoardUtils.squareBit[t];
        }
        return attackers;
    }
    
    public long getSquareAttacks (byte square, byte figure, byte side)
    {
        switch (figure)
        {
            case PAWN:
                return BoardUtils.moveArray[side==WHITE?PAWN:BPAWN][square];
            case KNIGHT:
                return BoardUtils.moveArray[KNIGHT][square];
            case BISHOP:
                return getBishopAttacks(square);
            case ROOK:
                return getRookAttacks(square);
            case QUEEN:
                return getQueenAttacks(square);
            case KING:
                return BoardUtils.moveArray[KING][square];
        } 
        return 0;
    }
    
    public long getSquareXAttacks (byte square, byte side)
    {
        long[] sidePieces;
        long attacks, rays, blocker;
        int piece, dir, blocksq;
        sidePieces = pieces[side];
        piece = squareFigure[square];
        blocker = this.blocker;
        attacks = 0;
        switch (piece)
        {
            case PAWN:
                attacks = BoardUtils.moveArray[side==WHITE?PAWN:BPAWN][square];
                break;
            case KNIGHT:
                attacks = BoardUtils.moveArray[KNIGHT][square];
                break;
            case BISHOP:
            case QUEEN:
                blocker &= ~(sidePieces[BISHOP] | sidePieces[QUEEN]);
                for (dir = BoardUtils.raybeg[BISHOP]; dir < BoardUtils.rayend[BISHOP]; dir++)
                {
                    rays = BoardUtils.ray[square][dir] & blocker;
                    if (rays == BoardUtils.NULLBITBOARD)
                    {
                        rays = BoardUtils.ray[square][dir];
                    }
                    else
                    {
                        blocksq = (BoardUtils.dirpos[dir] == 1? BoardUtils.getMostSignificantBit(rays) : BoardUtils.getLeastSignificantBit(rays));
                        rays = BoardUtils.fromtoRay[square][blocksq];
                    }
                    attacks |= rays;
                }
                if (piece == BISHOP) 
                    break;
                blocker = this.blocker;
            case ROOK:
                blocker &= ~(sidePieces[ROOK] | sidePieces[QUEEN]);
                for (dir = BoardUtils.raybeg[ROOK]; dir < BoardUtils.rayend[ROOK]; dir++)
                {
                    rays = BoardUtils.ray[square][dir] & blocker;
                    if (rays == BoardUtils.NULLBITBOARD)
                    {
                        rays = BoardUtils.ray[square][dir];
                    }
                    else
                    {
                        blocksq = (BoardUtils.dirpos[dir] == 1? BoardUtils.getMostSignificantBit(rays) : BoardUtils.getLeastSignificantBit(rays));
                        rays = BoardUtils.fromtoRay[square][blocksq];
                    }
                    attacks |= rays;
                }
                break;
            case KING:
                attacks = BoardUtils.moveArray[KING][square];
                break;
        }
        return (attacks);
    }
    
    public long getBishopAttacks (byte square) 
    {
        long bishopAttack45 = BoardUtils.bishop45Atak[square][(int)((blockerr45 >>> BoardUtils.shift45[square]) & BoardUtils.mask45[square])];
        long bishopAttack315 = BoardUtils.bishop315Atak[square][(int)((blockerr315 >>> BoardUtils.shift315[square]) & BoardUtils.mask315[square])];
        return bishopAttack45 | bishopAttack315;
    }
    
    public long getRookAttacks (byte square) 
    {
        long rookAttack00 = BoardUtils.rook00Atak[square][(int)((blocker >>> BoardUtils.shift00[square]) & 0xFF)];
        long rookAttack90 = BoardUtils.rook90Atak[square][(int)((blockerr90 >>> BoardUtils.shift90[square]) & 0xFF)];
        return rookAttack00 | rookAttack90;
    }
    
    public long getQueenAttacks (byte square) 
    {
        return getRookAttacks(square) | getBishopAttacks(square);
    }
    
    public long[][] getAttacks ()
    {
        byte side;
        byte square;
        long movers;
        long[] sidePieces;
        long[][] atacks = new long[2][6];
        
        for (side = WHITE; side <= BLACK; side++)
        {
            sidePieces = pieces[side];
            atacks[side][KNIGHT] = 0;
            movers = sidePieces[KNIGHT];
            while (movers != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[square];
                atacks[side][KNIGHT] |= BoardUtils.moveArray[KNIGHT][square];
            }

            atacks[side][BISHOP] = 0;
            movers = sidePieces[BISHOP];
            while (movers != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[square];
                atacks[side][BISHOP] |= getBishopAttacks(square);
            }

            atacks[side][ROOK] = 0;
            movers = sidePieces[ROOK];
            while (movers != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[square];
                atacks[side][ROOK] |= getRookAttacks(square);
            }

            atacks[side][QUEEN] = 0;
            movers = sidePieces[QUEEN];
            while (movers != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[square];
                atacks[side][ROOK] |= getQueenAttacks(square);
            }

            atacks[side][KING] = 0;
            movers = sidePieces[KING];
            while (movers != 0)
            {
                square = (byte)BoardUtils.getLeastSignificantBit(movers);
                movers &= BoardUtils.squareBitX[square];
                atacks[side][KING] |= BoardUtils.moveArray[KING][square];
            }

            atacks[side][PAWN] = 0;
            if (side == WHITE)
            {
                movers = pieces[WHITE][PAWN] & ~BoardUtils.fileBits[0];
                atacks[side][PAWN] |= (movers >> 7);
                movers = pieces[WHITE][PAWN] & ~BoardUtils.fileBits[7];
                atacks[side][PAWN] |= (movers >> 9);
            }
            else
            {
                movers = pieces[BLACK][PAWN] & ~BoardUtils.fileBits[0];
                atacks[side][PAWN] |= (movers << 9);
                movers = pieces[BLACK][PAWN] & ~BoardUtils.fileBits[7];
                atacks[side][PAWN] |= (movers << 7);
            }
        }
        return atacks;
    }
    
    private boolean isPinningKing (byte sq, byte side)
    {
        int xside;
        int KingSq, dir, sq1;
        long b;
        KingSq = getKingSquare(side);
        if ((dir = BoardUtils.directions[KingSq][sq]) == -1) 
            return false;
        xside = 1 ^ side;
        if ((BoardUtils.fromtoRay[KingSq][sq] & BoardUtils.squareBitX[sq] & blocker) != 0) 
            return false;
        b = (BoardUtils.ray[KingSq][dir] ^ BoardUtils.fromtoRay[KingSq][sq]) & blocker;
        if (b == 0) 
            return false;
        sq1 = (sq > KingSq ? BoardUtils.getLeastSignificantBit(b) : BoardUtils.getMostSignificantBit(b));
        if (dir <= 3 && ((BoardUtils.squareBit[sq1] & (pieces[xside][QUEEN] | pieces[xside][BISHOP])) != 0)) 
            return true;
        if (dir >= 4 && ((BoardUtils.squareBit[sq1] & (pieces[xside][QUEEN] | pieces[xside][ROOK])) != 0)) 
            return true;
        return false;
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
        if (piece > WHITEKING)
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
        return (byte)(1^side);
    }
    
    public static byte getPieceSide (byte piece)
    {
        return (byte)(piece / 6);
    }

    public static byte getPieceFigure (byte piece)
    {
        return (byte)(piece % 6);
    }
    
    public boolean checkState ()
    {
        boolean stateOk = true;
        for (byte square = A1; square <= H8; square++)
        {
            byte squareSide = this.squareSide[square];
            byte squareFigure = this.squareFigure[square];
            long squareBitBoard = BoardUtils.squareBit[square];
            for (byte side = WHITE; side <= BLACK; side++)
            {
                boolean friendsBitSet = (friends[side] & squareBitBoard) != 0;
                if (side == squareSide)
                {
                    if (!friendsBitSet)
                        stateOk = false;
                }
                else
                {
                    if (friendsBitSet)
                        stateOk = false;
                }
                
                for (byte figure = PAWN; figure <= KING; figure++)
                {
                    boolean pieceBitSet = (pieces[side][figure] & squareBitBoard) != 0;
                    if (side == squareSide && figure == squareFigure)
                    {
                        if (!pieceBitSet)
                            stateOk = false;
                    }
                    else
                    {
                        if (pieceBitSet)
                            stateOk = false;
                    }
                }
            }
            
            if (squareSide != NOSIDE)
            {
                if ((blocker & squareBitBoard) == 0)
                    stateOk = false;
                if ((blockerr45 & BoardUtils.squareBit45[square]) == 0)
                    stateOk = false;
                if ((blockerr90 & BoardUtils.squareBit90[square]) == 0)
                    stateOk = false;
                if ((blockerr315 & BoardUtils.squareBit315[square]) == 0)
                    stateOk = false;
            }
            else
            {
                if ((blocker & squareBitBoard) != 0)
                    stateOk = false;
                if ((blockerr45 & BoardUtils.squareBit45[square]) != 0)
                    stateOk = false;
                if ((blockerr90 & BoardUtils.squareBit90[square]) != 0)
                    stateOk = false;
                if ((blockerr315 & BoardUtils.squareBit315[square]) != 0)
                    stateOk = false;
            }
        }
        return stateOk;
    }
    
    public static void main(final String[] args)
    {
    }
}
