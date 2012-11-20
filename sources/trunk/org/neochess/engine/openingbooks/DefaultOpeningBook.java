
package org.neochess.engine.openingbooks;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.neochess.engine.Board;
import org.neochess.engine.Move;

public class DefaultOpeningBook extends OpeningBook
{
    private String filename;

    public DefaultOpeningBook ()
    {
        this("OpeningBook.bin");
    }

    public DefaultOpeningBook (String filename)
    {
        this.filename = filename;
    }

    public void createOpeningBook (String sourceFilename)
    {
        System.out.println ("Creating Opening Book ....");
        File openingBookSourceFile = new File (sourceFilename);
        if (openingBookSourceFile.exists())
        {
            Board board = new Board();
            try
            {
                String line;
                BufferedReader openingBookBufferedReader = new BufferedReader(new FileReader(openingBookSourceFile));
                while ((line = openingBookBufferedReader.readLine()) != null)
                {
                    board.setStartupPosition();
                    System.out.println ("-----------");
                    String[] moveTokens = line.split(" ");
                    for (String moveToken : moveTokens)
                    {
                        Move move = new Move(moveToken);
                        if (board.isMoveValid(move))
                        {
                            addOpeningBookEntry(board, move);
                            board.makeMove(move);
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                System.out.println ("Opening Book created successfully !!");
            }
            catch (Exception ex)
            {
                System.out.println ("Error while processing the Opening Book");
            }
        }
        else
        {
            System.out.println ("Opening Book not found !!");
        }
    }

    public void addOpeningBookEntry (Board board, Move move)
    {
        long boardHash = board.getHash();
        List<Move> moves = getMoves(board);
        boolean moveFound = false;
        for (Move openingBookMove : moves)
        {
            if (openingBookMove.equals(move))
            {
                moveFound = true;
                break;
            }
        }

        if (!moveFound)
        {
            FileOutputStream fos = null;
            DataOutputStream dos = null;
            try
            {
                fos = new FileOutputStream(filename, true);
                dos = new DataOutputStream(fos);
                dos.writeLong(boardHash);
                dos.writeInt(move.getHash());
                dos.flush();
                System.out.println ("Book entry added: " + boardHash + " => " + move);
            }
            catch (Exception ex)
            {
                System.out.println ("Book entry save failure !!");
            }
            try { dos.close(); } catch (Exception ex) {}
            try { fos.close(); } catch (Exception ex) {}
            fos = null;
            dos = null;
        }
        else
        {
            System.out.println ("Book entry already entered !!");
        }
    }

    public List<Move> getMoves (Board board)
    {
        long boardHash = board.getHash();
        List<Move> moves = new ArrayList<Move>();
        FileInputStream fis = null;
        DataInputStream dis = null;
        try
        {
            fis = new FileInputStream(filename);
            dis = new DataInputStream(fis);
            while (true)
            {
                long testBoardHash = dis.readLong();
                int testMoveHash = dis.readInt();
                if (boardHash == testBoardHash)
                    moves.add(new Move(testMoveHash));
            }
        }
        catch (Exception ex) {}
        try { dis.close(); } catch (Exception ex) {}
        try { fis.close(); } catch (Exception ex) {}
        fis = null;
        dis = null;
        return moves;
    }

    @Override
    public Move getMove (Board board)
    {
        Move move = null;
        List<Move> moves = getMoves(board);
        Random randomGenerator = new Random(System.currentTimeMillis());
        if (moves.size() > 0)
            move = moves.get(randomGenerator.nextInt(moves.size()));
        return move;
    }
}
