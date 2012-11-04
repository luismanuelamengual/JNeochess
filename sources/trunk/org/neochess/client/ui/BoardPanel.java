
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JPanel;
import org.neochess.client.Application;
import org.neochess.engine.Board;
import org.neochess.engine.Board.Move;
import org.neochess.general.Disposable;
import org.neochess.util.ColorUtils;
import org.neochess.util.GraphicsUtils;
import org.neochess.util.ResourceUtils;

public class BoardPanel extends JPanel implements Disposable, MouseListener, MouseMotionListener, MatchFrame.MatchFrameListener
{
    public static final int SQUARESTYLE_PLAIN = 0;
    public static final int SQUARESTYLE_HORIZONTAL = 1;
    public static final int SQUARESTYLE_VERTICAL = 2;
    public static final int SQUARESTYLE_MIXED = 3;
    public static final int SQUARESTYLE_DIAGONALDOWN = 4;
    public static final int SQUARESTYLE_DIAGONALUP = 5;
    public static final int SQUARESTYLE_SQUARED = 6;
    public static final int SQUARESTYLE_CROSSED = 7;
    public static final int SQUARESTYLE_HORIZONALGRADIENT = 8;
    public static final int SQUARESTYLE_VERTICALGRADIENT = 9;
    public static final int SQUARESTYLE_DIAGONALGRADIENT = 10;
    
    private MatchFrame matchFrame;
    private Point draggingPoint = new Point(0,0);
    private byte draggingSquare = Board.EMPTY;
    private boolean humanMoveEnabled = false;
    private boolean squareHighlighted[] = new boolean[64];
    
    private Dimension boardDimension = new Dimension (280, 280);
    private Point boardPosition = new Point(30, 30);
    private int borderWidth = 2;
    private Color lightColor = Color.WHITE;
    private Color darkColor = new Color(189,188,137);
    private Color foreColor = Color.BLACK;
    private int squareStyle = SQUARESTYLE_DIAGONALGRADIENT;
    private Color squareIndicatorColor = ColorUtils.getDarkerColor(darkColor, 10);
    private Color squareHighlightColor = ColorUtils.getDarkerColor(lightColor, 40);
    private Color currentMoveArrowColor = ColorUtils.getAlphaColor(Color.RED, 50);
    private boolean boardFlipped = false;
    private BoardChessSet chessSet = new BoardChessSet();
    private boolean useShadowedImages = true;
    private boolean showBackgroundImage = true;
    private Image backgroundImage = ResourceUtils.getImage(Application.getInstance().getResourceImagesPath() + "chessboardBackground.jpg");
    private boolean showReference = true;
    private boolean showCurrentMoveArrow = true;
    private boolean showSquareIndicator = true;
    
    public BoardPanel (MatchFrame matchFrame)
    { 
        this.matchFrame = matchFrame;
        this.matchFrame.addMatchFrameListener(this);
        int panelWidth = boardDimension.width + Math.min ((boardPosition.x*2), 120);
        int panelHeight = boardDimension.height + Math.min ((boardPosition.y*2), 120);  
        setPreferredSize( new Dimension(panelWidth, panelHeight));
        setDoubleBuffered(true);
        addMouseListener (this);
        addMouseMotionListener (this);
    }

    @Override
    public void dispose()
    {
        removeMouseListener (this);
        removeMouseMotionListener (this);
        removeAll();
        matchFrame.removeMatchFrameListener(this);
        matchFrame = null;
    }

    public boolean isHumanMoveEnabled ()
    {
        return humanMoveEnabled;
    }

    public void setHumanMoveEnabled (boolean humanMoveEnabled)
    {
        this.humanMoveEnabled = humanMoveEnabled;
    }
  
    public boolean isSquareHighlighted (int square)
    {
        return squareHighlighted[square];
    }
    
    public void setSquareHighlight (int square)
    {
        squareHighlighted[square] = true;
        repaint();
    }
    
    public void clearSquareHighlight (int square)
    {
        squareHighlighted[square] = false;
        repaint();
    }
    
    public void clearSquareHighlights ()
    {
        for (int square = 0; square < 64; square++) 
            squareHighlighted[square] = false;
        repaint();
    }
    
    public void update ()
    {
        repaint();
    }
    
    @Override
    public void paint (Graphics screen)
    {
        Graphics2D screen2d = (Graphics2D)screen;
        screen2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        screen2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        screen2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        screen2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        screen2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        screen2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        drawBackground (screen);
        drawBoard (screen);
    }
    
    private void drawBackground (Graphics screen)
    {
        if (showBackgroundImage && backgroundImage != null) 
        {
            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);
            if (imageWidth > 0 && imageHeight > 0)
            {
                for (int posY = 0; posY < getHeight(); posY += imageHeight)
                    for (int posX = 0; posX < getWidth(); posX += imageWidth) 
                        screen.drawImage (backgroundImage, posX, posY, imageWidth, imageHeight, this);
            }
        }
        else
        {
            screen.clearRect(0, 0, getWidth(), getHeight());
        }
    }
    
    private void drawBoard (Graphics screen)
    {    
        if (matchFrame.getDisplayPly() >= 0)
        {
            Board board = matchFrame.getBoard(matchFrame.getDisplayPly());
            for (byte square = 0; square < 64; square++) 
            {
                drawSquare (screen, square);
                if (isSquareHighlighted(square)) 
                    drawSquareHighlight (screen, square, squareHighlightColor, 6);
                drawFigure (screen, square, board.getPiece(square), useShadowedImages);
            }
        }
        
        drawBorder (screen);
        drawBoardReference (screen);
        drawCurrentMoveArrow (screen);
        drawSquareIndicator (screen);
        drawMovingFigure (screen);
    }
    
    private void drawBoardReference (Graphics screen)
    {
        if (showReference == false) 
            return;
        
        screen.setColor(foreColor);
        screen.setFont(new java.awt.Font("TimesNewRoman",Font.BOLD,12)); 
        
        Dimension squareDimension = getBoardSquareDimension();
        byte sideRank;
        byte sideFile;
        int charWidth;
        int charHeight = screen.getFontMetrics().getAscent();
        int xoffset = boardPosition.x + (squareDimension.width/2);
        int yoffset = boardPosition.y + (squareDimension.height/2);
        int charVerticalMargin = 6;
        int charHorizontalMargin = 12;
        
        for (byte file = Board.FILE_A; file <= Board.FILE_H; file++, xoffset += squareDimension.width) 
        {
            sideFile = (!boardFlipped)? file : (byte)(7-file);
            charWidth = screen.getFontMetrics().stringWidth(String.valueOf(Board.getFileChar(sideFile)));
            screen.drawString(String.valueOf(Board.getFileChar(sideFile)), xoffset - (charWidth/2), (boardPosition.y - charVerticalMargin - borderWidth));
            screen.drawString(String.valueOf(Board.getFileChar(sideFile)), xoffset - (charWidth/2), (boardPosition.y + boardDimension.height + charVerticalMargin + charHeight + borderWidth));
        }
        
        for (byte rank = 1; rank <= 8; rank++, yoffset += squareDimension.height) 
        {
            sideRank = (boardFlipped)? rank : (byte)(9-rank);
            charWidth = screen.getFontMetrics().stringWidth( String.valueOf(sideRank) );
            screen.drawString( String.valueOf(sideRank), boardPosition.x - charHorizontalMargin - (charWidth/2) - borderWidth, yoffset + (charHeight/2));
            screen.drawString( String.valueOf(sideRank), boardPosition.x + boardDimension.width + charHorizontalMargin - (charWidth/2) + borderWidth, yoffset + (charHeight/2));
        }
    }
    
    private void drawBorder (Graphics screen)
    {
        if (borderWidth == 0) 
        {
            screen.setColor(darkColor);
            screen.drawRect(boardPosition.x, boardPosition.y, boardDimension.width, boardDimension.height );
        }
        else 
        {
            for (int level = 1; level <= borderWidth; level++) 
                screen.draw3DRect(boardPosition.x-level, boardPosition.y-level, boardDimension.width+(level*2), boardDimension.height+(level*2), true);
        }
    }
    
    private void drawSquare (Graphics screen, byte square)
    {
        Rectangle squareRectangle = getBoardSquareRectangle (square);
        screen.setColor ((((Board.getSquareFile(square) + Board.getSquareRank(square) & 1) == 1)? lightColor : darkColor)); 
        screen.fillRect (squareRectangle.x, squareRectangle.y, squareRectangle.width, squareRectangle.height);
        if ((Board.getSquareFile(square) + Board.getSquareRank(square) & 1) == 1 || squareStyle == SQUARESTYLE_PLAIN) { return; }
        
        int lineSeparation = 3;
        int lineDiagonalSeparation = lineSeparation + 1;
        int crossedDiagonalSeparation = lineSeparation * 2;
        int file = Board.getSquareFile(square);
        int rank = Board.getSquareRank(square);
        int left = squareRectangle.x;
        int top = squareRectangle.y;
        int right = left + squareRectangle.width;
        int bottom = top + squareRectangle.height;
        screen.setColor(lightColor);
        
        if (squareStyle == SQUARESTYLE_HORIZONTAL) 
        {
            for (int posY = squareRectangle.y+(lineSeparation-1); posY < (squareRectangle.y+squareRectangle.height); posY += lineSeparation) 
                screen.drawLine (squareRectangle.x, posY, (squareRectangle.x+squareRectangle.width), posY);
        }
        else if (squareStyle == SQUARESTYLE_VERTICAL) 
        {
            for (int posX = squareRectangle.x; posX < (squareRectangle.x+squareRectangle.width); posX += lineSeparation) 
                screen.drawLine(posX, squareRectangle.y, posX, (squareRectangle.y+squareRectangle.height));
        }
        else if (squareStyle == SQUARESTYLE_MIXED) 
        {
            if (file % 2 == 0 ) 
            {
                for (int posX = squareRectangle.x; posX < (squareRectangle.x+squareRectangle.width); posX += lineSeparation)
                    screen.drawLine(posX, squareRectangle.y, posX, (squareRectangle.y+squareRectangle.height));
            }
            else 
            {
                for (int posY = squareRectangle.y+(lineSeparation-1); posY < (squareRectangle.y+squareRectangle.height); posY += lineSeparation) 
                    screen.drawLine(squareRectangle.x, posY, (squareRectangle.x+squareRectangle.width), posY);
            }
        }
        else if (squareStyle == SQUARESTYLE_DIAGONALDOWN)
        {
            int posX, posY;
            for ( posX = squareRectangle.x, posY = (squareRectangle.y+squareRectangle.height); posX < (squareRectangle.x+squareRectangle.width); posX += lineDiagonalSeparation, posY -= lineDiagonalSeparation ) 
                screen.drawLine(posX, squareRectangle.y, squareRectangle.x+squareRectangle.width, posY);
            for ( posY = squareRectangle.y, posX = (squareRectangle.x+squareRectangle.width); posY < (squareRectangle.y+squareRectangle.height); posY += lineDiagonalSeparation, posX -= lineDiagonalSeparation )
                screen.drawLine(squareRectangle.x, posY, posX, squareRectangle.y+squareRectangle.height);
        }
        else if (squareStyle == SQUARESTYLE_DIAGONALUP) 
        {
            int posX, posY;
            for ( posY = (squareRectangle.y+squareRectangle.height), posX = (squareRectangle.x+squareRectangle.width); posY > squareRectangle.y; posY -= lineDiagonalSeparation, posX -= lineDiagonalSeparation )
                screen.drawLine(squareRectangle.x, posY, posX, squareRectangle.y);
            for ( posX = squareRectangle.x, posY = squareRectangle.y; posX < (squareRectangle.x+squareRectangle.width); posX += lineDiagonalSeparation, posY += lineDiagonalSeparation)
                screen.drawLine(posX, (squareRectangle.y+squareRectangle.height), (squareRectangle.x+squareRectangle.width), posY);
        }
        else if (squareStyle == SQUARESTYLE_SQUARED) 
        {
            for (int posY = squareRectangle.y+(lineSeparation-1); posY < (squareRectangle.y+squareRectangle.height); posY += lineDiagonalSeparation)
                screen.drawLine(squareRectangle.x, posY, (squareRectangle.x+squareRectangle.width), posY);
            for ( int posX = squareRectangle.x; posX < (squareRectangle.x+squareRectangle.width); posX += lineDiagonalSeparation)
                screen.drawLine(posX, squareRectangle.y, posX, (squareRectangle.y+squareRectangle.height));
        }
        else if (squareStyle == SQUARESTYLE_CROSSED) 
        {
            int posX, posY;
            for (posX = squareRectangle.x, posY = (squareRectangle.y+squareRectangle.height); posX < (squareRectangle.x+squareRectangle.width); posX += crossedDiagonalSeparation, posY -= crossedDiagonalSeparation) 
                screen.drawLine( posX, squareRectangle.y, squareRectangle.x+squareRectangle.width, posY);
            for (posY = squareRectangle.y, posX = (squareRectangle.x+squareRectangle.width); posY < (squareRectangle.y+squareRectangle.height); posY += crossedDiagonalSeparation, posX -= crossedDiagonalSeparation)
                screen.drawLine( squareRectangle.x, posY, posX, squareRectangle.y+squareRectangle.height);
            for (posY = (squareRectangle.y+squareRectangle.height), posX = (squareRectangle.x+squareRectangle.width); posY > squareRectangle.y; posY -= crossedDiagonalSeparation, posX -= crossedDiagonalSeparation) 
                screen.drawLine( squareRectangle.x, posY, posX, squareRectangle.y);
            for (posX = squareRectangle.x, posY = squareRectangle.y; posX < (squareRectangle.x+squareRectangle.width); posX += crossedDiagonalSeparation, posY += crossedDiagonalSeparation)    
                screen.drawLine( posX, (squareRectangle.y+squareRectangle.height), (squareRectangle.x+squareRectangle.width), posY);            
        } 
        else if (squareStyle == SQUARESTYLE_HORIZONALGRADIENT) 
        {
            Graphics2D screen2D = (Graphics2D) screen;
            screen2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradientPaint = new GradientPaint(left, (top+bottom)/2, darkColor.brighter(), right, (top+bottom)/2, darkColor);
            screen2D.setPaint(gradientPaint);
            screen2D.fill(new java.awt.geom.Rectangle2D.Double(squareRectangle.x, squareRectangle.y, squareRectangle.width, squareRectangle.height));
        }
        else if (squareStyle == SQUARESTYLE_VERTICALGRADIENT) 
        {
            Graphics2D screen2D = (Graphics2D) screen;
            screen2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);    
            GradientPaint gradientPaint = new GradientPaint((left+right)/2, top, darkColor.brighter(), (left+right)/2, bottom, darkColor);
            screen2D.setPaint(gradientPaint);
            screen2D.fill(new java.awt.geom.Rectangle2D.Double(squareRectangle.x, squareRectangle.y, squareRectangle.width, squareRectangle.height));
        }
        else if (squareStyle == SQUARESTYLE_DIAGONALGRADIENT) 
        {
            Graphics2D screen2D = (Graphics2D) screen;
            screen2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradientPaint = new GradientPaint(squareRectangle.x, squareRectangle.y, darkColor.brighter(), (squareRectangle.x+squareRectangle.width), (squareRectangle.y+squareRectangle.height), darkColor);
            screen2D.setPaint(gradientPaint);
            screen2D.fill(new java.awt.geom.Rectangle2D.Double(squareRectangle.x, squareRectangle.y, squareRectangle.width, squareRectangle.height));
        }
    }
    
    private void drawFigure (Graphics screen, byte square, byte piece, boolean shadowed)
    {
        if (piece == Board.EMPTY || square == draggingSquare) { return; }
        Rectangle squareRectangle = getBoardSquareRectangle (square);
        drawFigure (screen, squareRectangle.x, squareRectangle.y, piece, shadowed);
    }
    
    private void drawFigure (Graphics screen, int x, int y, byte piece, boolean shadowed)
    { 
        if (piece == Board.EMPTY) { return; }
        Dimension squareDimension = getBoardSquareDimension();
        BufferedImage pieceImage = chessSet.getPieceImage ( piece, squareDimension, shadowed);
        screen.drawImage (pieceImage, x, y, this);
    }
    
    private void drawMovingFigure (Graphics screen)
    {
        Board board = matchFrame.getBoard();
        if (draggingSquare == Board.EMPTY) { return; }
        Dimension squareDimension = getBoardSquareDimension();
        int x = draggingPoint.x - (squareDimension.width/2);
        int y = draggingPoint.y - (squareDimension.height/2);
        drawFigure (screen, x, y, board.getPiece(draggingSquare), useShadowedImages); 
    }
    
    private void drawSquareIndicator (Graphics screen)
    {
        if (showSquareIndicator == false || draggingSquare == Board.EMPTY) { return; }
        byte square = getSquareAtPoint(draggingPoint);
        drawSquareHighlight(screen, square, squareIndicatorColor, 3);
    }
    
    private void drawCurrentMoveArrow (Graphics screen)
    {
        if (showCurrentMoveArrow)
        {
            int moveIndex = matchFrame.getDisplayPly() - 1;
            if (moveIndex >= 0)
            {
                Move lastMove = matchFrame.getMove(moveIndex);
                drawMoveArrow(screen, lastMove);
            }
        }
    }
    
    private void drawMoveArrow (Graphics screen, Move move)
    {
        Rectangle initialSquareRectangle = getBoardSquareRectangle(move.getInitialSquare());
        Rectangle endSquareRectangle = getBoardSquareRectangle(move.getEndSquare());
        int arrowSize = (int)(initialSquareRectangle.getWidth()*0.4);
        drawArrow(screen, (int)initialSquareRectangle.getCenterX(), (int)initialSquareRectangle.getCenterY(), (int)endSquareRectangle.getCenterX(), (int)endSquareRectangle.getCenterY(), arrowSize, currentMoveArrowColor);
    }
    
    private void drawArrow (Graphics screen, int x1, int y1, int x2, int y2, int arrowSize, Color arrowColor) 
    {   
        Graphics2D screen2d = (Graphics2D)screen;
        double dx=x2-x1;
        double dy=y2-y1;
        double angle = Math.atan2(dy, dx);
        int len = (int)Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        AffineTransform backupTransform = screen2d.getTransform();
        screen2d.transform(at);
        screen2d.setColor(arrowColor);
        int triangleSize = arrowSize;
        Shape line = new RoundRectangle2D.Double(0, -(arrowSize/2), len - (arrowSize/2), arrowSize, 4, 4);
        Shape triangle = new Polygon(new int[] {len, len-triangleSize, len-triangleSize, len}, new int[] {0, -triangleSize, triangleSize, 0}, 4);
        Area area = new Area(line);
        area.add(new Area(triangle));
        screen2d.fill(area);
        screen2d.setTransform(backupTransform);
    }
    
    private void drawSquareHighlight (Graphics screen, byte square, Color color, int highlightWidth)
    {
        if (square == Board.EMPTY) { return; }
        Rectangle squareRectangle = getBoardSquareRectangle(square);
        screen.setColor(color);
        for (int level = 0; level < highlightWidth; level++) 
            screen.drawRect( squareRectangle.x+level, squareRectangle.y+level, squareRectangle.width-(level*2), squareRectangle.height-(level*2));
    }

    private Dimension getBoardSquareDimension ()
    {
        return new Dimension((int)(boardDimension.width / 8), (int)(boardDimension.height / 8));
    }
    
    private Rectangle getBoardSquareRectangle (byte square)
    {
        square = (!boardFlipped)? square : (byte)(Board.H1 - square);
        Dimension squareDimension = getBoardSquareDimension();
        int file = Board.getSquareFile(square);
        int rank = Board.getSquareRank(square);
        int x = file * squareDimension.width + boardPosition.x;
        int y = rank * squareDimension.height + boardPosition.y; 
        return new Rectangle(x, y, squareDimension.width, squareDimension.height);
    }
   
    private byte getSquareAtPoint (Point point)
    {
        if (isPointOverBoard(point) == false) { return Board.EMPTY; }
        Dimension squareDimension = getBoardSquareDimension ();
        byte file = (byte)((point.x - boardPosition.x) / squareDimension.width);
        byte rank = (byte)((point.y - boardPosition.x) / squareDimension.height);
        byte square = Board.getSquare (file, rank);
        return (!boardFlipped)? square : (byte)(Board.H8 - square);
    }
    
    private boolean isPointOverBoard (Point point)
    {
        return !((point.x <= boardPosition.x) || (point.y <= boardPosition.x) || (point.x >= (boardPosition.x + boardDimension.width)) || (point.y >= (boardPosition.y + boardDimension.height)));
    }
    
    private boolean isSquareActionEnabled (byte square)
    {
        if (square == Board.EMPTY ) { return false; }
        if (matchFrame.getState() == MatchFrame.STATE_PLAYING && humanMoveEnabled && matchFrame.getDisplayPly() == matchFrame.getPly())
        {
            Board board = matchFrame.getBoard();
            byte side = board.getSquareSide(square);
            if (side != Board.NOSIDE)
            {
                if (side == Board.WHITE && matchFrame.getTurnPlayer().equals(matchFrame.getWhitePlayer()))
                    return true;
                else if (side == Board.BLACK && matchFrame.getTurnPlayer().equals(matchFrame.getBlackPlayer()))
                    return true;
            }
        }
        return false;
    }
    
    private void setBoardCursor (int cursortype)
    {
        if (getCursor().getType() != cursortype)
            setCursor(new Cursor(cursortype));
    }

    public void onMatchFinished(MatchFrame match){}
    public void onMatchMove(MatchFrame match, Move move){}
    public void onMatchPositionChanged(MatchFrame match){}
    public void onMatchStarted(MatchFrame match){}
    public void onMatchStateChanged(MatchFrame match, byte state){}
    public void onMatchTakeback(MatchFrame match, Move move){}
    public void onMatchTurnStarted (MatchFrame match, byte side){}
    public void onMatchTurnEnded (MatchFrame match, byte side){}
    
    public void onMatchDisplayPlyChanged (MatchFrame match, int ply)
    {
        update();
    }

    public void onMatchBoardFlipped (MatchFrame match, boolean flipped)
    {
        update();
    }
    
    public void mousePressed (MouseEvent event)
    {
        if (matchFrame.getState() == MatchFrame.STATE_PLAYING && humanMoveEnabled)
        {
            draggingPoint = event.getPoint();
            if (isPointOverBoard(draggingPoint))
            {
                byte square = getSquareAtPoint(draggingPoint);
                if (isSquareActionEnabled(square)) 
                {
                    draggingSquare = square;
                    repaint();
                }
            }
        }
    }
    
    public void mouseMoved (MouseEvent evt) 
    {
        byte square = getSquareAtPoint(evt.getPoint());
        if (isSquareActionEnabled(square)) 
            setBoardCursor (Cursor.HAND_CURSOR);
        else 
            setBoardCursor (Cursor.DEFAULT_CURSOR);
    }
    
    public void mouseDragged (MouseEvent event) 
    {
        draggingPoint = event.getPoint();
        if (isPointOverBoard(draggingPoint)) 
            repaint();
    }
    
    public void mouseReleased (MouseEvent event) 
    { 
        if (draggingSquare != Board.EMPTY) 
        {
            if (matchFrame.getState() == MatchFrame.STATE_PLAYING && humanMoveEnabled)
                matchFrame.makeMove(new Move(draggingSquare, getSquareAtPoint(draggingPoint)));
            draggingSquare = Board.EMPTY;
            mouseMoved (event);
            repaint();
        }
    }
    
    public void mouseClicked (MouseEvent evt) {}
    public void mouseEntered (MouseEvent evt) {}
    public void mouseExited (MouseEvent evt) {}

    public class BoardChessSet 
    {
        public static final int SHADOWSIZE = 6;
        public static final int CHESSSET_STAUNTON = 1;
        public static final int CHESSSET_WOODEN = 2;
        public static final int CHESSSET_DEFAULT = 6;
        public static final int CHESSSET_MAGNETIC = 7;
        public static final int CHESSSET_CUSTOM = 99;
        
        private BufferedImage pieceImage[] = new BufferedImage[12];
        private BufferedImage pieceImageShadow[] = new BufferedImage[12];
        private String[] imageString = { "WPawn.gif", "WKnight.gif", "WBishop.gif", "WRook.gif", "WQueen.gif", "WKing.gif", "BPawn.gif", "BKnight.gif", "BBishop.gif", "BRook.gif", "BQueen.gif", "BKing.gif" };
        private int chessSet;
        private String chessSetCustomPath = "";

        public BoardChessSet (int chessSet) 
        {
            setChessSet ( chessSet );
        }

        public BoardChessSet (String chessSetImagePath) 
        {
            setCustomChessSet (chessSetImagePath);
        }

        public BoardChessSet() 
        {
            setChessSet (CHESSSET_DEFAULT);
        }

        public void setChessSet (int chessSet) 
        {
            String chessSetName;
            this.chessSet = chessSet;
            switch (chessSet) {
                case CHESSSET_WOODEN:
                    chessSetName = "wooden";
                    break;
                case CHESSSET_STAUNTON:
                    chessSetName = "staunton";
                    break;
                case CHESSSET_DEFAULT:
                    chessSetName = "comic";
                    break;
                case CHESSSET_MAGNETIC:
                    chessSetName = "magnetic";
                    break;
                default:
                    chessSetName = "comic";
                    this.chessSet = CHESSSET_DEFAULT;
                    break;
            }
            chessSetCustomPath = Application.getInstance().getResourceImagesPath() + "pieces/" + chessSetName + "/";
            updateChessSetImages ();
        }

        public void setCustomChessSet (String chessSetImagePath)
        {
            chessSet = CHESSSET_CUSTOM;
            chessSetCustomPath = chessSetImagePath;
            updateChessSetImages ();
        }

        public BufferedImage getPieceImage (byte piece)
        {
            return getPieceImage (piece, true);
        }

        public BufferedImage getPieceImage (byte piece, boolean shadowed)
        {
            return getPieceImage (piece, new Dimension(50,50), shadowed);
        }

        public BufferedImage getPieceImage (byte piece, Dimension pieceDimension, boolean shadowed)
        {
            if (pieceImage[piece].getWidth() != pieceDimension.width || pieceImage[piece].getHeight() != pieceDimension.height) 
                updateChessSetImages (pieceDimension);
            return ( shadowed == true )? pieceImageShadow[piece] : pieceImage[piece];
        }

        public void setPieceImage (byte piece, BufferedImage pieceImage)
        {
            this.pieceImage[piece] = pieceImage; 
            pieceImageShadow[piece] = GraphicsUtils.getShadowedImage (pieceImage, SHADOWSIZE, 1).getSubimage (SHADOWSIZE, SHADOWSIZE, pieceImage.getWidth(), pieceImage.getHeight());
        }

        public int getChessSet ()
        {
            return chessSet;
        }

        private void updateChessSetImages ()
        {
            try 
            {
                for (byte piece = Board.WHITEPAWN; piece <= Board.BLACKKING; piece++) 
                    setPieceImage(piece, ResourceUtils.getBufferedImage(chessSetCustomPath + imageString[piece]));
            }
            catch(IOException e) {}
        }

        private void updateChessSetImages (Dimension pieceDimension)
        {
            try 
            {
                for (byte piece = Board.WHITEPAWN; piece <= Board.BLACKKING; piece++) 
                {
                    BufferedImage pieceImage = ResourceUtils.getBufferedImage(chessSetCustomPath + imageString[piece]);
                    pieceImage = GraphicsUtils.toBufferedImage (pieceImage.getScaledInstance ( pieceDimension.width, pieceDimension.height, Image.SCALE_SMOOTH));
                    setPieceImage (piece, pieceImage);
                }
            }
            catch (IOException e) {}
        }
    }
}

