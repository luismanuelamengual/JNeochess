
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
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
import org.neochess.engine.Match;
import org.neochess.engine.Move;
import org.neochess.general.Disposable;
import org.neochess.util.ColorUtils;
import org.neochess.util.GraphicsUtils;
import org.neochess.util.ResourceUtils;

public class BoardPanel extends JPanel implements Disposable, MouseListener, MouseMotionListener, ComponentListener, MatchFrame.MatchFrameListener
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
    private ChessSetManager chessSetManager = new ChessSetManager();
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
        setPreferredSize(new Dimension(340, 340));
        setDoubleBuffered(true);
        addMouseListener (this);
        addMouseMotionListener (this);
        addComponentListener(this);
    }

    @Override
    public void dispose()
    {
        removeComponentListener(this);
        removeMouseListener (this);
        removeMouseMotionListener (this);
        removeAll();
        matchFrame.removeMatchFrameListener(this);
        matchFrame = null;
    }

    public Color getCurrentMoveArrowColor() 
    {
        return currentMoveArrowColor;
    }

    public void setCurrentMoveArrowColor(Color currentMoveArrowColor) 
    {
        this.currentMoveArrowColor = currentMoveArrowColor;
    }
    
    public boolean isShowCurrentMoveArrow() 
    {
        return showCurrentMoveArrow;
    }

    public void setShowCurrentMoveArrow(boolean showCurrentMoveArrow) 
    {
        this.showCurrentMoveArrow = showCurrentMoveArrow;
    }

    public boolean isShowSquareIndicator() 
    {
        return showSquareIndicator;
    }

    public void setShowSquareIndicator(boolean showSquareIndicator) 
    {
        this.showSquareIndicator = showSquareIndicator;
    }

    public void setLightColor(Color lightColor) 
    {
        this.lightColor = lightColor;
    }

    public void setDarkColor(Color darkColor) 
    {
        this.darkColor = darkColor;
    }

    public Color getLightColor() 
    {
        return lightColor;
    }

    public Color getDarkColor() 
    {
        return darkColor;
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
    
    public void setChessSet (String chessSet)
    {
        this.chessSetManager.setChessSet(chessSet);
        update ();
    }
    
    public String getChessSet ()
    {
        return this.chessSetManager.getChessSet();
    }
    
    public void setSquareStyle (int squareStyle)
    {
        this.squareStyle = squareStyle;
        update ();
    }
    
    public int getSquareStyle ()
    {
        return squareStyle;
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
            screen.setColor(Color.WHITE);
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
        BufferedImage pieceImage = chessSetManager.getPieceImage ( piece, squareDimension, shadowed);
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
        byte initialSquare = move.getInitialSquare();
        byte endSquare = move.getEndSquare();
        Rectangle initialSquareRectangle = getBoardSquareRectangle(initialSquare);
        Rectangle endSquareRectangle = getBoardSquareRectangle(endSquare);
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
        square = (!boardFlipped)? square : (byte)(Board.H8 - square);
        Dimension squareDimension = getBoardSquareDimension();
        int file = Board.getSquareFile(square);
        int rank = Board.getSquareRank(square);
        int x = file * squareDimension.width + boardPosition.x;
        int y = (7-rank) * squareDimension.height + boardPosition.y; 
        return new Rectangle(x, y, squareDimension.width, squareDimension.height);
    }
   
    private byte getSquareAtPoint (Point point)
    {
        if (isPointOverBoard(point) == false) { return Board.EMPTY; }
        Dimension squareDimension = getBoardSquareDimension ();
        byte file = (byte)((point.x - boardPosition.x) / squareDimension.width);
        byte rank = (byte)(7 - ((point.y - boardPosition.x) / squareDimension.height));
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
        if (matchFrame.getState() == Match.STATE_PLAYING && humanMoveEnabled && matchFrame.getDisplayPly() == matchFrame.getPly())
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
        boardFlipped = match.isBoardFlipped();
        update();
    }
    
    public void mousePressed (MouseEvent event)
    {
        if (matchFrame.getState() == Match.STATE_PLAYING && humanMoveEnabled)
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
            if (matchFrame.getState() == Match.STATE_PLAYING && humanMoveEnabled)
                matchFrame.makeMove(new Move(draggingSquare, getSquareAtPoint(draggingPoint)));
            draggingSquare = Board.EMPTY;
            mouseMoved (event);
            repaint();
        }
    }
    
    @Override
    public void componentResized(ComponentEvent e) 
    {
        Dimension dimension = this.getSize();
        int boardBuffer = 30;
        int preferredBoardSize = Math.min(dimension.height, dimension.width);
        preferredBoardSize -= (boardBuffer * 2);
        preferredBoardSize -= (preferredBoardSize % 8);
        boardPosition = new Point(boardBuffer, boardBuffer);
        boardDimension = new Dimension(preferredBoardSize, preferredBoardSize);
    }
    
    public void mouseClicked (MouseEvent evt) {}
    public void mouseEntered (MouseEvent evt) {}
    public void mouseExited (MouseEvent evt) {}
    @Override public void componentMoved(ComponentEvent e) {}
    @Override public void componentShown(ComponentEvent e) {}
    @Override public void componentHidden(ComponentEvent e) {}

    public class ChessSetManager 
    {
        public static final int SHADOWSIZE = 6;
        
        private BufferedImage pieceImage[] = new BufferedImage[12];
        private BufferedImage pieceImageShadow[] = new BufferedImage[12];
        private String[] imageString = { "WPawn", "WKnight", "WBishop", "WRook", "WQueen", "WKing", "BPawn", "BKnight", "BBishop", "BRook", "BQueen", "BKing" };
        private String chessSet;
        private Dimension chessPiecesDimension;
        private String actualChessSet;
        private Dimension actualChessPiecesDimension;
        
        public ChessSetManager() 
        {
            this ("comic", new Dimension(50, 50));
        }
        
        public ChessSetManager (String chessSet, Dimension chessPiecesDimension) 
        {
            setChessSet(chessSet);
            setChessPiecesDimension(chessPiecesDimension);
        }

        public void setChessSet (String chessSet) 
        {
            this.chessSet = chessSet;   
        }
        
        public String getChessSet ()
        {
            return chessSet;
        }

        public Dimension getChessPiecesDimension ()
        {
            return chessPiecesDimension;
        }

        public void setChessPiecesDimension (Dimension chessPiecesDimension)
        {
            this.chessPiecesDimension = chessPiecesDimension;
        }

        public BufferedImage getPieceImage (byte piece)
        {
            return getPieceImage (piece, true);
        }

        public BufferedImage getPieceImage (byte piece, boolean shadowed)
        {
            return getPieceImage (piece, chessPiecesDimension, shadowed);
        }

        public BufferedImage getPieceImage (byte piece, Dimension pieceDimension, boolean shadowed)
        {
            setChessPiecesDimension(pieceDimension);
            if (actualChessSet == null || !actualChessSet.equals(chessSet) || actualChessPiecesDimension == null || !actualChessPiecesDimension.equals(chessPiecesDimension))
                updateChessSetImages ();
            return ( shadowed == true )? pieceImageShadow[piece] : pieceImage[piece];
        }

        private void setPieceImage (byte piece, BufferedImage pieceImage)
        {
            this.pieceImage[piece] = pieceImage; 
            this.pieceImageShadow[piece] = GraphicsUtils.getShadowedImage (pieceImage, SHADOWSIZE, 1).getSubimage (SHADOWSIZE, SHADOWSIZE, pieceImage.getWidth(), pieceImage.getHeight());
        }
        
        private void updateChessSetImages ()
        {
            String chessSetPath = Application.getInstance().getResourceImagesPath() + "pieces/" + chessSet + "/";
            try 
            {
                for (byte piece = Board.WHITEPAWN; piece <= Board.BLACKKING; piece++) 
                {
                    BufferedImage scaledPieceImage = ResourceUtils.getBufferedImage(chessSetPath + imageString[piece] + ".gif");
                    scaledPieceImage = GraphicsUtils.toBufferedImage (scaledPieceImage.getScaledInstance (chessPiecesDimension.width, chessPiecesDimension.height, Image.SCALE_SMOOTH));
                    setPieceImage (piece, scaledPieceImage);
                }
                
                actualChessSet = chessSet;
                actualChessPiecesDimension = chessPiecesDimension;
            }
            catch (IOException e) 
            {
                actualChessSet = null;
                actualChessPiecesDimension = null;
            }
        }
    }
}

