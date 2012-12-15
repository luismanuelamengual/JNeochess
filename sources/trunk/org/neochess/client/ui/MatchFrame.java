
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
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

public class MatchFrame extends InternalFrame implements ActionListener
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
                {
                    ((JRadioButtonMenuItem)menuItem).setSelected(boardPanel.getChessSet().equals(command.substring("chessset-".length())));
                }
                else if (command.startsWith("boardtype-"))
                {
                    ((JRadioButtonMenuItem)menuItem).setSelected(boardPanel.getSquareStyle() == Integer.parseInt(command.substring("boardtype-".length())));
                }
            }
            
            switch (command)
            {
                case "showMoveIndicator":
                    ((JCheckBoxMenuItem)menuItem).setSelected(boardPanel.isShowSquareIndicator());
                    break;
                case "showLastMoveIndicator":
                    ((JCheckBoxMenuItem)menuItem).setSelected(boardPanel.isShowCurrentMoveArrow());
                    break;
                case "boardFlipped":
                    ((JCheckBoxMenuItem)menuItem).setSelected(this.isBoardFlipped());
                    break;
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        String action = e.getActionCommand();
        if (action.startsWith("chessset-"))
        {
            boardPanel.setChessSet(action.substring("chessset-".length()));
            updateMenuBar ();
        }
        else if (action.startsWith("boardtype-"))
        {
            boardPanel.setSquareStyle(Integer.parseInt(action.substring("boardtype-".length())));
            updateMenuBar ();
        }
        switch (action)
        {
            case "exit":
                close();
                break;
            case "lightSquares":
                Color lightSquaresColor = JColorChooser.showDialog(this, "Select light squares color", boardPanel.getLightColor());
                if (lightSquaresColor != null)
                {
                    boardPanel.setLightColor(lightSquaresColor);
                    boardPanel.update();
                }
                break;
            case "darkSquares":
                Color darkSquaresColor = JColorChooser.showDialog(this, "Select dark squares color", boardPanel.getDarkColor());
                if (darkSquaresColor != null)
                {
                    boardPanel.setDarkColor(darkSquaresColor);
                    boardPanel.update();
                }
                break;
            case "showMoveIndicator":
                boardPanel.setShowSquareIndicator(!boardPanel.isShowSquareIndicator());
                boardPanel.update();
                updateMenuBar ();
                break;
            case "showLastMoveIndicator":
                boardPanel.setShowCurrentMoveArrow(!boardPanel.isShowCurrentMoveArrow());
                boardPanel.update();
                updateMenuBar ();
                break;
            case "lastMoveIndicatorColor":
                Color moveIndicatorColor = JColorChooser.showDialog(this, "Select move Indicator color", boardPanel.getCurrentMoveArrowColor());
                if (moveIndicatorColor != null)
                {
                    boardPanel.setCurrentMoveArrowColor(moveIndicatorColor);
                    boardPanel.update();
                }
                updateMenuBar ();
                break;
            case "boardFlipped":
                setBoardFlipped(!isBoardFlipped());
                updateMenuBar ();
                break;
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
    
    private JMenuItem createMenuItem (String text, String actionCommand) 
    {
        JMenuItem miNew = new JMenuItem(text);
        miNew.setActionCommand (actionCommand);
        miNew.addActionListener(this);
        return miNew;
    }
    
    private JCheckBoxMenuItem createCheckboxMenuItem (String text, String actionCommand) 
    {
        JCheckBoxMenuItem miNew = new JCheckBoxMenuItem(text);
        miNew.setActionCommand (actionCommand);
        miNew.addActionListener(this);
        return miNew;
    }
    
    private JRadioButtonMenuItem createRadioButtonMenuItem (String text, String ActionCommand) 
    {
        JRadioButtonMenuItem miNew = new JRadioButtonMenuItem(text);
        miNew.setActionCommand ( ActionCommand );
        miNew.addActionListener(this);
        return miNew;
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
        menu.add(createMenuItem("Exit", "exit"));
        return menu;
    }
    
    private JMenu createMatchMenu ()
    {
        JMenu menu = new JMenu("Match");
        menu.setActionCommand("match");
        menu.add(createMenuItem("Repeat Match", "repeat"));
        menu.add(createMenuItem("Rematch", "rematch"));
        menu.add(createMenuItem("Resign", "resign"));
        menu.add(createMenuItem("Offer Draw", "offerDraw"));
        menu.addSeparator();
        menu.add(createMenuItem("Force Move", "forceMove"));
        menu.add(createMenuItem("Suggest Move", "suggestMove"));
        menu.add(createMenuItem("Takeback", "tackbackMove"));
        return menu;
    }
    
    private JMenu createSettingsMenu ()
    {
        JMenu menu = new JMenu("Settings");
        menu.setActionCommand("settings");
        menu.add (createChangeChesssetMenu());
        menu.add (createChangeBoardTypeMenu());
        menu.add (createMenuItem("Set light square color", "lightSquares"));
        menu.add (createMenuItem("Set dark square color", "darkSquares"));
        menu.add (createMenuItem("Set last move indicator color", "lastMoveIndicatorColor"));
        menu.add (createCheckboxMenuItem("Show move indicator", "showMoveIndicator"));
        menu.add (createCheckboxMenuItem("Show last move indicator", "showLastMoveIndicator"));
        menu.add (createCheckboxMenuItem("Board flipped", "boardFlipped"));
        return menu;
    }
    
    private JMenu createChangeChesssetMenu ()
    {
        JMenu menu = new JMenu("Change chess set");
        menu.setActionCommand("chessset");
        menu.add(createRadioButtonMenuItem("Default", "chessset-comic"));
        menu.add(createRadioButtonMenuItem("Wooden", "chessset-wooden"));
        menu.add(createRadioButtonMenuItem("Staunton", "chessset-staunton"));
        menu.add(createRadioButtonMenuItem("Magnetic", "chessset-magnetic"));
        return menu;
    }
    
    private JMenu createChangeBoardTypeMenu ()
    {        
        JMenu menu = new JMenu("Change board type");
        menu.setActionCommand("chessset");
        menu.add (createRadioButtonMenuItem("Solid", "boardtype-" + BoardPanel.SQUARESTYLE_PLAIN));
        menu.add (createRadioButtonMenuItem("horizontal Stripes", "boardtype-" + BoardPanel.SQUARESTYLE_HORIZONTAL));
        menu.add (createRadioButtonMenuItem("Vertical Stripes", "boardtype-" + BoardPanel.SQUARESTYLE_VERTICAL));
        menu.add (createRadioButtonMenuItem("Horizontal Vertical Stripes", "boardtype-" + BoardPanel.SQUARESTYLE_MIXED));
        menu.add (createRadioButtonMenuItem("Diagonal Down", "boardtype-" + BoardPanel.SQUARESTYLE_DIAGONALDOWN));
        menu.add (createRadioButtonMenuItem("Diagonal Up", "boardtype-" + BoardPanel.SQUARESTYLE_DIAGONALUP));
        menu.add (createRadioButtonMenuItem("Squared", "boardtype-" + BoardPanel.SQUARESTYLE_SQUARED));
        menu.add (createRadioButtonMenuItem("Crossed", "boardtype-" + BoardPanel.SQUARESTYLE_CROSSED));
        menu.add (createRadioButtonMenuItem("Horizontal Gradient", "boardtype-" + BoardPanel.SQUARESTYLE_HORIZONALGRADIENT));
        menu.add (createRadioButtonMenuItem("VerticaL Gradient", "boardtype-" + BoardPanel.SQUARESTYLE_VERTICALGRADIENT));
        menu.add (createRadioButtonMenuItem("Diagonal Gradient", "boardtype-" + BoardPanel.SQUARESTYLE_DIAGONALGRADIENT));
        return menu;
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
