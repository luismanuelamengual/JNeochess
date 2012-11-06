
package org.neochess.engine;

import org.neochess.general.Disposable;

public class Move implements Disposable, Comparable<Move>
{
    private byte initialSquare;
    private byte endSquare;
    private int score;

    public Move (String moveString) throws IllegalArgumentException
    {
        if (moveString.length() != 4)
            throw new IllegalArgumentException();
        this.initialSquare = Board.getSquare(moveString.substring(0, 2));
        this.endSquare = Board.getSquare(moveString.substring(2, 4));
    }

    public Move (int hash)
    {
        this((byte)(hash & 0xFF), (byte)((hash & 0xFF00) >> 8));
    }

    public Move (byte initialSquare, byte endSquare)
    {
        this.initialSquare = initialSquare;
        this.endSquare = endSquare;
    }

    public void dispose()
    {
    }

    public boolean equals(Move move)
    {
        return move.initialSquare == initialSquare && move.endSquare  == endSquare;
    }

    public int getScore()
    {
        return score;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public int getHash()
    {
        return initialSquare + (endSquare << 8);
    }

    public byte getInitialSquare()
    {
        return initialSquare;
    }

    public byte getEndSquare()
    {
        return endSquare;
    }

    public int compareTo(Move move)
    {
        return (this.score == move.score)? 0 : (this.score > move.score? 1 : -1);
    }

    public String getSANString (Board board)
    {
        String san = "";
        byte movingPiece = board.getPiece(initialSquare);

        if ((movingPiece == Board.WHITEKING && initialSquare == Board.E1 && endSquare == Board.G1) || (movingPiece == Board.BLACKKING && initialSquare == Board.E8 && endSquare == Board.G8)) 
        {
            san = "O-O";
        }
        else if ((movingPiece == Board.WHITEKING && initialSquare == Board.E1 && endSquare == Board.C1) || (movingPiece == Board.BLACKKING && initialSquare == Board.E8 && endSquare == Board.C8))
        {
            san = "O-O-O";
        }
        else
        {
            byte movingFigure = Board.getPieceFigure(movingPiece);
            if (movingFigure == Board.PAWN) 
            {
                if (board.getPiece(endSquare) != Board.EMPTY || endSquare == board.getEnPassantSquare())
                {
                    san += Board.getFileChar(Board.getSquareFile(initialSquare));
                    san += 'x';
                }
                san += Board.getSquareString(endSquare);
                byte endSquareRank = Board.getSquareRank(endSquare);
                if (endSquareRank == Board.RANK_1 || endSquareRank == Board.RANK_8)
                    san += "=Q";
            }
            else 
            {
                san += Board.getFigureChar(movingFigure);
                if (board.getPiece(endSquare) != Board.EMPTY)
                    san += 'x';                    
                san += Board.getSquareString(endSquare);
            }

            Board testBoard = new Board(board);
            testBoard.makeMove(this);
            if (testBoard.inCheck(testBoard.getSideToMove()))
                san += ((testBoard.getLegalMoves().size() == 0)? '#' : '+');
        }
        return san;
    }

    @Override
    public String toString()
    {
        return Board.getSquareString(initialSquare) + Board.getSquareString(endSquare);
    }
}