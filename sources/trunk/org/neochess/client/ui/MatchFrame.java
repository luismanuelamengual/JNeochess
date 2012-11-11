
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.neochess.client.Application;
import org.neochess.engine.Board;
import org.neochess.engine.Clock;
import org.neochess.engine.ComputerPlayer;
import org.neochess.engine.HumanPlayer;
import org.neochess.engine.Match;
import org.neochess.engine.Move;
import org.neochess.engine.Player;
import org.neochess.engine.User;
import org.neochess.util.ResourceUtils;

public class MatchFrame extends InternalFrame
{
    protected EventListenerList listeners = new EventListenerList();
    
    private Match match;
    private byte sideToMove;
    private byte state;
    private int displayPly = -1;
    private boolean boardFlipped = false;
    private BoardPanel boardPanel;
    private MatchMoveListPanel moveListPanel;
    private MatchOutputPanel outputPanel;
    private PlayerPanel topPlayerPanel;
    private PlayerPanel bottomPlayerPanel;
    
    public MatchFrame ()
    {
        super();
        setMinimumSize(new Dimension (300, 300));
        setSize(new java.awt.Dimension(500, 400));
        
        match = new Match();
        boardPanel = new BoardPanel(this);
        moveListPanel = new MatchMoveListPanel(this);
        outputPanel = new MatchOutputPanel(this);
        topPlayerPanel = new PlayerPanel(this, false);
        bottomPlayerPanel = new PlayerPanel(this, true);
        
        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPanel.setMinimumSize(new Dimension(100, 100));
        tabPanel.setPreferredSize(new Dimension(180, 200));
        tabPanel.addTab("Move List", moveListPanel);
        tabPanel.addTab("Output", outputPanel);
        
        JPanel boardContainerPanel = new JPanel();
        boardContainerPanel.setAutoscrolls(true);
        boardContainerPanel.setLayout(new BorderLayout());
        boardContainerPanel.add(boardPanel, BorderLayout.CENTER);
        boardContainerPanel.add(topPlayerPanel, BorderLayout.NORTH);
        boardContainerPanel.add(bottomPlayerPanel, BorderLayout.SOUTH);
        
        JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPanel.setLeftComponent(boardContainerPanel);
        splitPanel.setRightComponent(tabPanel);
        splitPanel.setOneTouchExpandable(true);
        splitPanel.setResizeWeight(1);
        
        add(splitPanel);
        setJMenuBar (createMenuBar());
        setVisible(true);
        pack();
        initializeOpeningBookFile();
        updateMenuBar();
    }

    @Override
    public void dispose()
    {
        match.dispose();
        match = null;
        topPlayerPanel.dispose();
        topPlayerPanel = null;
        bottomPlayerPanel.dispose();
        bottomPlayerPanel = null;
        boardPanel.dispose();
        boardPanel = null;
        moveListPanel.dispose();
        moveListPanel = null;
        outputPanel.dispose();
        outputPanel = null;
        removeAll();
        super.dispose();
    }
    
    private void updateMenuBar ()
    {
        List<JMenuItem> menuItems = getMenuItems();
        for (JMenuItem menuItem : menuItems)
        {
            String command = menuItem.getActionCommand(); 
            if (command != null)
            {
                if (command.startsWith("chessset-"))
                    ((JRadioButtonMenuItem)menuItem).setSelected(boardPanel.getChessSet().equals(command.substring("chessset-".length())));
            }
        }
    }
    
    private List<JMenuItem> getMenuItems()
    {
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        JMenuBar menuBar = getJMenuBar();
        for (Component component : menuBar.getComponents())
        {
            if (component instanceof JMenu)
            {
                menuItems.add((JMenuItem)component);
                menuItems.addAll(getMenuItems((JMenu)component));
            }
        }
        return menuItems;
    }
    
    private List<JMenuItem> getMenuItems(JMenu menu)
    {
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        for (Component component : menu.getMenuComponents())
        {
            if (component instanceof JMenuItem)
            {
                menuItems.add((JMenuItem)component);
                if (component instanceof JMenu)
                    menuItems.addAll(getMenuItems((JMenu)component));
            }
        }
        return menuItems;
    }   
    
    private MenuElement getMenuElement (String action)
    {
        return getMenuElement (action, this.getJMenuBar());
    }
    
    private MenuElement getMenuElement (String action, MenuElement menuElement)
    {
        MenuElement item = null;
        if (menuElement instanceof JMenuItem)
            if (((JMenuItem)menuElement).getActionCommand().equals(action))
                item = menuElement;
        if (item == null)
        {
            MenuElement[] childElements = menuElement.getSubElements();
            for (MenuElement childElement : childElements)
            {
                item = getMenuElement(action, childElement);
                if (item != null)
                    break;
            }
        }
        return item;
    }
    
    private JMenuBar createMenuBar ()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createMatchMenu());
        menuBar.add(createSettingsMenu());
        return menuBar;
    }
    
    private JMenu createFileMenu ()
    {
        JMenu menu = new JMenu("File");
        menu.setActionCommand("file");
        menu.add(createExitMenuItem());
        return menu;
    }
    
    private JMenu createMatchMenu ()
    {
        JMenu menu = new JMenu("Match");
        menu.setActionCommand("match");
        menu.add(createRepeatMenuItem());
        menu.add(createRematchMenuItem());
        menu.add(createResignMenuItem());
        menu.add(createDrawMenuItem());
        menu.addSeparator();
        menu.add(createForceMoveMenuItem());
        menu.add(createSuggestMoveMenuItem());
        menu.add(createTakebackMenuItem());
        return menu;
    }
    
    private JMenu createSettingsMenu ()
    {
        JMenu menu = new JMenu("Settings");
        menu.setActionCommand("settings");
        menu.add (createChangeChesssetMenu());
        menu.add (createChangeBoardTypeMenu());
        menu.add (createChangeBoardSizeMenuItem ());
        menu.add (createLightSquareColorMenuItem());
        menu.add (createDarkSquareColorMenuItem());
        menu.add (createShowMoveIndicatorMenuItem());
        menu.add (createShowLastMoveIndicatorMenuItem());
        menu.add (createChangeMoveIndicatorColorMenuItem());
        menu.addSeparator();
        menu.add (createBoardFlippedMenuItem());
        menu.add (createSoundsEnabledMenuItem());
        return menu;
    }
    
    private JMenu createChangeChesssetMenu ()
    {
        JRadioButtonMenuItem defaultMenuItem = new JRadioButtonMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                boardPanel.setChessSet("comic");
                updateMenuBar ();
            }   
        });
        defaultMenuItem.setActionCommand ("chessset-comic");
        defaultMenuItem.setText("Default");
        defaultMenuItem.setSelected(true);
        JRadioButtonMenuItem woodenMenuItem = new JRadioButtonMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                boardPanel.setChessSet("wooden");
                updateMenuBar ();
            }   
        });
        woodenMenuItem.setText("Wooden");
        woodenMenuItem.setActionCommand ("chessset-wooden");
        woodenMenuItem.setSelected(false);
        JRadioButtonMenuItem stauntonMenuItem = new JRadioButtonMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                boardPanel.setChessSet("staunton");
                updateMenuBar ();
            }   
        });
        stauntonMenuItem.setText("Staunton");
        stauntonMenuItem.setActionCommand ("chessset-staunton");
        stauntonMenuItem.setSelected(false);
        JRadioButtonMenuItem magneticMenuItem = new JRadioButtonMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                boardPanel.setChessSet("magnetic");
                updateMenuBar ();
            }   
        });
        magneticMenuItem.setText("Magnetic");
        magneticMenuItem.setActionCommand ("chessset-magnetic");
        magneticMenuItem.setSelected(false);
        
        JMenu menu = new JMenu("Change chess set");
        menu.setActionCommand("chessset");
        menu.add(defaultMenuItem);
        menu.add(woodenMenuItem);
        menu.add(stauntonMenuItem);
        menu.add(magneticMenuItem);
        return menu;
    }
    
    private JMenu createChangeBoardTypeMenu ()
    {
        JRadioButtonMenuItem solidBoardTypeMenuItem = new JRadioButtonMenuItem("Solid");
        solidBoardTypeMenuItem.setActionCommand ("solidBoardType");
        solidBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem horizontalStripesBoardTypeMenuItem = new JRadioButtonMenuItem("Horizontal Stripes");
        horizontalStripesBoardTypeMenuItem.setActionCommand ("horizontalStripesBoardType");
        horizontalStripesBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem verticalStripesBoardTypeMenuItem = new JRadioButtonMenuItem("Vertical Stripes");
        verticalStripesBoardTypeMenuItem.setActionCommand ("verticalStripesBoardType");
        verticalStripesBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem horizontalVerticalStripesBoardTypeMenuItem = new JRadioButtonMenuItem("Horizontal/Vertical Stripes");
        horizontalVerticalStripesBoardTypeMenuItem.setActionCommand ("horizontalVerticalStripesBoardType");
        horizontalVerticalStripesBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem diagonalDownBoardTypeMenuItem = new JRadioButtonMenuItem("Diagonal Down Stripes");
        diagonalDownBoardTypeMenuItem.setActionCommand ("diagonalDownBoardType");
        diagonalDownBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem diagonalUpBoardTypeMenuItem = new JRadioButtonMenuItem("Diagonal Up Stripes");
        diagonalUpBoardTypeMenuItem.setActionCommand ("diagonalUpBoardType");
        diagonalUpBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem squaredBoardTypeMenuItem = new JRadioButtonMenuItem("Squared");
        squaredBoardTypeMenuItem.setActionCommand ("squaredBoardType");
        squaredBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem crossedBoardTypeMenuItem = new JRadioButtonMenuItem("Crossed");
        crossedBoardTypeMenuItem.setActionCommand ("crossedBoardType");
        crossedBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem horizontalGradientBoardTypeMenuItem = new JRadioButtonMenuItem("Horizontal Gradient");
        horizontalGradientBoardTypeMenuItem.setActionCommand ("horizontalGradientBoardType");
        horizontalGradientBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem verticalGradientBoardTypeMenuItem = new JRadioButtonMenuItem("Vertical Gradient");
        verticalGradientBoardTypeMenuItem.setActionCommand ("verticalGradientBoardType");
        verticalGradientBoardTypeMenuItem.setSelected(false);
        JRadioButtonMenuItem diagonalGradientBoardTypeMenuItem = new JRadioButtonMenuItem("Diagonal Gradient");
        diagonalGradientBoardTypeMenuItem.setActionCommand ("diagonalGradientBoardType");
        diagonalGradientBoardTypeMenuItem.setSelected(true);
        
        JMenu menu = new JMenu("Change board type");
        menu.setActionCommand("chessset");
        menu.add (solidBoardTypeMenuItem);
        menu.add (horizontalStripesBoardTypeMenuItem);
        menu.add (verticalStripesBoardTypeMenuItem);
        menu.add (horizontalVerticalStripesBoardTypeMenuItem);
        menu.add (diagonalDownBoardTypeMenuItem);
        menu.add (diagonalUpBoardTypeMenuItem);
        menu.add (squaredBoardTypeMenuItem);
        menu.add (crossedBoardTypeMenuItem);
        menu.add (horizontalGradientBoardTypeMenuItem);
        menu.add (verticalGradientBoardTypeMenuItem);
        menu.add (diagonalGradientBoardTypeMenuItem);
        return menu;
    }
    
    private JMenuItem createChangeBoardSizeMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("boardsize");
        menuItem.setText("Change board size");
        return menuItem;
    }
    
    private JMenuItem createShowMoveIndicatorMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("moveindicator");
        menuItem.setText("Show move indicator");
        return menuItem;
    }
    
    private JMenuItem createShowLastMoveIndicatorMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("lastmoveindicator");
        menuItem.setText("Show last move indicator");
        return menuItem;
    }
    
    private JMenuItem createChangeMoveIndicatorColorMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("moveindicatorcolor");
        menuItem.setText("Change move indicator color");
        return menuItem;
    }
    
    private JMenuItem createSoundsEnabledMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("soundenabled");
        menuItem.setText("Sounds enabled");
        return menuItem;
    }
    
    private JMenuItem createBoardFlippedMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("boardflipped");
        menuItem.setText("Board flipped");
        return menuItem;
    }
    
    private JMenuItem createLightSquareColorMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                close();
            }   
        });
        menuItem.setActionCommand("lightSquareColor");
        menuItem.setText("Set light squares color");
        return menuItem;
    }
    
    private JMenuItem createDarkSquareColorMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                close();
            }   
        });
        menuItem.setActionCommand("darkSquareColor");
        menuItem.setText("Set dark squares color");
        return menuItem;
    }
    
    private JMenuItem createExitMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
                close();
            }   
        });
        menuItem.setActionCommand("exit");
        menuItem.setText("Exit");
        return menuItem;
    }
    
    private JMenuItem createRepeatMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("repeat");
        menuItem.setText("Repeat Match");
        return menuItem;
    }
    
    private JMenuItem createRematchMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("rematch");
        menuItem.setText("Rematch");
        return menuItem;
    }
    
    private JMenuItem createResignMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("resign");
        menuItem.setText("Resign");
        return menuItem;
    }
    
    private JMenuItem createForceMoveMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("force");
        menuItem.setText("Force Move");
        return menuItem;
    }
    
    private JMenuItem createSuggestMoveMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("suggest");
        menuItem.setText("Suggest Move");
        return menuItem;
    }
    
    private JMenuItem createTakebackMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("takeback");
        menuItem.setText("Takeback");
        return menuItem;
    }
    
    private JMenuItem createDrawMenuItem ()
    {
        JMenuItem menuItem = new JMenuItem(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent ae)
            {
            }   
        });
        menuItem.setActionCommand("draw");
        menuItem.setText("Offer draw");
        return menuItem;
    }
    
    @Override
    public boolean close (boolean forced)
    {
        boolean closeFrame = true;
        if (getState() == Match.STATE_PLAYING)
        {
            if (forced || JOptionPane.showConfirmDialog(this, "Match in progress !!, Are you sure to close match ?", Application.getInstance().getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
            }
            else
            {
                closeFrame = false;
            }   
        }
        return closeFrame? super.close(forced) : false;
    }

    public void setWhitePlayer (Player whitePlayer)
    {
        match.setWhitePlayer(whitePlayer);
    }

    public void setWhiteClock (Clock whiteClock)
    {
        match.setWhiteClock(whiteClock);
    }

    public void setState (byte state)
    {
        byte oldState = this.state;
        this.state = state;
        if (this.state != oldState)
            fireMatchStateChangedEvent (this.state);
    }

    public void setBlackPlayer (Player blackPlayer)
    {
        match.setBlackPlayer(blackPlayer);
    }

    public void setBlackClock (Clock blackClock)
    {
        match.setBlackClock(blackClock);
    }

    public boolean isTimeUp (byte side)
    {
        return match.isTimeUp(side);
    }

    public Player getWhitePlayer ()
    {
        return match.getWhitePlayer();
    }

    public Clock getWhiteClock ()
    {
        return match.getWhiteClock();
    }

    public Player getTurnPlayer ()
    {
        return match.getTurnPlayer();
    }

    public Clock getTurnClock ()
    {
        return match.getTurnClock();
    }

    public byte getState ()
    {
        return state;
    }

    public byte getSideToMove ()
    {
        return sideToMove;
    }

    public long getRemainingTime (byte side)
    {
        return match.getRemainingTime(side);
    }

    public int getPly ()
    {
        return match.getPly();
    }

    public Player getPlayer (byte side)
    {
        return match.getPlayer(side);
    }

    public List<Move> getMoves ()
    {
        return match.getMoves();
    }

    public Move getMove (int ply)
    {
        return match.getMove(ply);
    }

    public Clock getClock (byte side)
    {
        return match.getClock(side);
    }

    public Board getBoard (int ply)
    {
        return match.getBoard(ply);
    }

    public Board getBoard ()
    {
        return match.getBoard();
    }

    public Player getBlackPlayer ()
    {
        return match.getBlackPlayer();
    }

    public Clock getBlackClock ()
    {
        return match.getBlackClock();
    }
    
    public synchronized void reset ()
    {
        match.reset();
    }
    
    public synchronized void start ()
    {
        initializeMatch ();
    }
    
    public synchronized boolean makeMove (Move move)
    {
        boolean moveMade = match.makeMove(move);
        if (moveMade)
        {
            fireMatchMoveEvent(move);
            fireMatchPositionChangedEvent();
            setDisplayPly(getPly());
        }
        processState();
        return moveMade;
    }
    
    public synchronized boolean unmakeMove ()
    {
        Move lastMove = match.getMoves().get(match.getMoves().size()-1);
        boolean moveUnmade = match.unmakeMove();
        if (moveUnmade)
        {
            fireMatchTakebackEvent(lastMove);
            fireMatchPositionChangedEvent();
            setDisplayPly(getPly());
        }
        processState();
        return moveUnmade;
    }
    
    public void updateState ()
    {
        setState (match.updateState());
    }
    
    protected void processState ()
    {
        updateState ();
        switch (state)
        {
            case Match.STATE_NOTSTARTED:
                break;
            case Match.STATE_PLAYING:
                if (match.getSideToMove() != sideToMove)
                    finalizeTurn(sideToMove);
                initializeTurn(match.getSideToMove());
                break;
            case Match.STATE_FINISHED_DRAW:
            case Match.STATE_FINISHED_BLACKWIN:
            case Match.STATE_FINISHED_WHITEWIN:
                if (sideToMove != Board.NOSIDE)
                    finalizeTurn(sideToMove);
                finalizeMatch ();
        }
    }
    
    protected void initializeMatch ()
    {
        match.start();
        updateState ();
        fireMatchStartedEvent ();
        fireMatchPositionChangedEvent();
        processState ();
    }

    protected void finalizeMatch ()
    {
        fireMatchFinishedEvent ();
    }
    
    protected void initializeTurn (byte side)
    {
        sideToMove = side;
        onInitializeTurn (side);
        fireMatchTurnStartedEvent (side);
    }
    
    protected void finalizeTurn (byte side)
    {
        sideToMove = Board.NOSIDE;
        onFinalizeTurn (side);
        fireMatchTurnEndedEvent (side);
    }
    
    protected void onInitializeTurn (byte side)
    {
        Player turnPlayer = getPlayer(side);
        if (turnPlayer != null)
        {
            if (turnPlayer instanceof HumanPlayer || (turnPlayer instanceof User && turnPlayer.equals(Application.getInstance().getSession().getUser())))
            {
                boardPanel.setHumanMoveEnabled(true);
            }
            else if (turnPlayer instanceof ComputerPlayer)
            {
                final ComputerPlayer computerPlayer = (ComputerPlayer)turnPlayer;
                new Thread ()
                {
                    @Override
                    public void run ()
                    {
                        final Move moveSearched = computerPlayer.startMoveSearch(match);
                        SwingUtilities.invokeLater(new Runnable() 
                        {
                            @Override
                            public void run ()
                            {
                                if (match != null)
                                    makeMove(moveSearched);
                            }
                        });
                    }
                }.start();
            }
        }
    }
    
    protected void onFinalizeTurn (byte side)
    {
        boardPanel.setHumanMoveEnabled(false);
    }

    public BoardPanel getBoardPanel()
    {
        return boardPanel;
    }

    public MatchMoveListPanel getMoveListPanel()
    {
        return moveListPanel;
    }

    public MatchOutputPanel getOutputPanel()
    {
        return outputPanel;
    }
    
    private void initializeOpeningBookFile ()
    {
        ResourceUtils.copyResourceToFile(Application.getInstance().getResourceGeneralPath() + "OpeningBook.bin", Application.getHomePath() + File.separatorChar + "OpeningBook.bin");
    }
    
    public int getDisplayPly()
    {
        return displayPly;
    }
    
    public void setDisplayPly(int ply)
    {
        if (ply >= 0 && ply <= getPly())
        {
            int oldDisplayPly = this.displayPly;
            this.displayPly = ply;
            if (this.displayPly != oldDisplayPly)
                fireMatchDisplayPlyChangedEvent (this.displayPly);
        }
    }

    public boolean isBoardFlipped ()
    {
        return boardFlipped;
    }

    public void setBoardFlipped (boolean boardFlipped)
    {
        boolean oldBoardFlipped = this.boardFlipped;
        this.boardFlipped = boardFlipped;
        if (this.boardFlipped != oldBoardFlipped)
            fireMatchBoardFlippedEvent (this.boardFlipped);
    }
    
    public void addMatchFrameListener(MatchFrameListener listener)
    {
        listeners.add(MatchFrameListener.class, listener);
    }

    public void removeMatchFrameListener(MatchFrameListener listener)
    {
        listeners.remove(MatchFrameListener.class, listener);
    }
    
    private void fireMatchStartedEvent ()
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchStarted(this);
    }
    
    private void fireMatchFinishedEvent ()
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchFinished(this);
    }
    
    private void fireMatchPositionChangedEvent ()
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchPositionChanged(this);
    }
    
    private void fireMatchTurnStartedEvent (byte side)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchTurnStarted(this, side);
    }
    
    private void fireMatchTurnEndedEvent (byte side)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchTurnEnded(this, side);
    }
    
    private void fireMatchMoveEvent (Move move)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchMove(this, move);
    }
    
    private void fireMatchTakebackEvent (Move move)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchTakeback(this, move);
    }
    
    private void fireMatchStateChangedEvent (byte state)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchStateChanged(this, state);
    }
    
    private void fireMatchDisplayPlyChangedEvent (int ply)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchDisplayPlyChanged(this, ply);
    }
    
    private void fireMatchBoardFlippedEvent (boolean flipped)
    {
        for (MatchFrameListener listener : listeners.getListeners(MatchFrameListener.class))
            listener.onMatchBoardFlipped(this, flipped);
    }
    
    public interface MatchFrameListener extends EventListener
    {
        public void onMatchStarted (MatchFrame match);
        public void onMatchFinished (MatchFrame match);
        public void onMatchPositionChanged (MatchFrame match);
        public void onMatchTurnStarted (MatchFrame match, byte side);
        public void onMatchTurnEnded (MatchFrame match, byte side);
        public void onMatchMove (MatchFrame match, Move move);
        public void onMatchTakeback (MatchFrame match, Move move);
        public void onMatchStateChanged (MatchFrame match, byte state);
        public void onMatchDisplayPlyChanged (MatchFrame match, int ply);
        public void onMatchBoardFlipped (MatchFrame match, boolean flipped);
    }
}
