
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.json.simple.JSONObject;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.neochess.client.Application;
import org.neochess.client.Connection.ConnectionListener;
import org.neochess.client.Session;
import org.neochess.client.Session.SessionListener;
import org.neochess.engine.Clock;
import org.neochess.engine.ComputerPlayer;
import org.neochess.engine.HumanPlayer;
import org.neochess.engine.searchagents.DefaultSearchAgent;
import org.neochess.general.Disposable;
import org.neochess.util.ResourceUtils;

public final class MainFrame extends JFrame implements ActionListener, Disposable, SessionListener, WindowListener, ConnectionListener
{
    private DesktopPane desktopPane;
    private StatusBar statusBar;
    private String skin;
    
    public MainFrame()
    {
        setSkin ("NebulaBrickWallSkin");
        setTitle (Application.getInstance().getTitle());
        setIconImage (ResourceUtils.getImage(Application.getInstance().getResourceImagesPath() + "main.png"));
        setSize(new java.awt.Dimension(800, 600));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout (new BorderLayout());
        addWindowListener(this);
        Application.getInstance().getSession().addSessionListener(this);
        Application.getInstance().getConnection().addConnectionListener(this);
        desktopPane = createDesktopPane();
        statusBar = createStatusBar();
        setJMenuBar (createMenuBar());
        getContentPane().add(desktopPane, BorderLayout.CENTER);
        getContentPane().add(statusBar, BorderLayout.SOUTH );
        update();
    }
    
    @Override
    public void dispose()
    {
        Application.getInstance().getConnection().removeConnectionListener(this);
        Application.getInstance().getSession().removeSessionListener(this);
        removeWindowListener(this);
        desktopPane = null;
        statusBar = null; 
        super.dispose();
    }
    
    public boolean closeFrames ()
    {
        return closeFrames(false);
    }
    
    public boolean closeFrames (boolean forced)
    {
        boolean allFramesClosed = true;
        List<InternalFrame> frames = getInternalFrames();
        for (InternalFrame frame : frames)
        {
            if (!frame.close(forced))
            {
                allFramesClosed = false;
                break;
            }
        }
        return allFramesClosed;
    }
    
    public boolean close()
    {
        boolean closed = false;
        if (closeFrames())
            closed = true;
        return closed;
    }
    
    public void destroyApplication ()
    {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to exit ?", Application.getInstance().getTitle(), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
            Application.getInstance().destroy();
    }
    
    @Override
    public void windowClosing (WindowEvent we)
    {
        destroyApplication ();
    }
    
    public void setSkin (String skin)
    {
        this.skin = skin;
        SubstanceLookAndFeel.setSkin("org.jvnet.substance.skin." + skin);
    }

    public String getSkin ()
    {
        return skin;
    }
    
    private JMenuBar createMenuBar ()
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createUsersMenu());
        menuBar.add(createPlayMenu());
        menuBar.add(createSkinsMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }
    
    public void update ()
    {
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run ()
            {
                updateMenuBar ();
                updateStatusBar ();
            }
        });
    }
    
    private void updateMenuBar ()
    {
        boolean isConnected = Application.getInstance().getConnection().isConnected();
        List<JMenuItem> menuItems = getMenuItems();
        for (JMenuItem menuItem : menuItems)
        {
            String command = menuItem.getActionCommand(); 
            if (command != null)
            {
                if (command.startsWith("skin-"))
                    ((JRadioButtonMenuItem)menuItem).setSelected(getSkin().equals(command.substring("skin-".length())));
                switch (command)
                {
                    case "file":
                        menuItem.setEnabled(true);
                        break;
                    case "exit":
                        menuItem.setEnabled(true);
                        break;
                    case "users":
                        menuItem.setEnabled(isConnected);
                        break;
                    case "createUser":
                        menuItem.setEnabled(isConnected);
                        break;
                    case "help":
                        menuItem.setEnabled(true);
                        break;
                    case "login":
                        menuItem.setEnabled(isConnected && !Application.getInstance().getSession().isStarted());
                        break;
                    default:
                        menuItem.setEnabled(isConnected && Application.getInstance().getSession().isStarted());
                        break;
                }
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        String action = e.getActionCommand();
        if (action.startsWith("skin-"))
        {
            setSkin(action.substring("skin-".length()));
            updateMenuBar();
        }
        switch (action)
        {
            case "exit":
                destroyApplication ();
                break;
            case "login":
                InternalFrame loginFrame = getInternalFrame(LoginFrame.class);
                if (loginFrame != null)
                    try { loginFrame.setSelected (true); } catch (Exception ex) {}
                else
                    addInternalFrame(new LoginFrame());
                break;
            case "logout":
                Application.getInstance().getSession().destroySession();
                break;
            case "editUser":
                List<InternalFrame> frames = getInternalFrames(UserFrame.class);
                UserFrame userModificationFrame = null;
                for (InternalFrame frame : frames)
                {
                    if (((UserFrame)frame).isUserModificationFrame())
                    {
                        userModificationFrame = ((UserFrame)frame);
                        break;
                    }
                }
                if (userModificationFrame != null)
                    try { userModificationFrame.setSelected (true); } catch (Exception ex) {}
                else
                    addInternalFrame(new UserFrame(Application.getInstance().getSession().getUser()));
                break;
            case "createUser":
                List<InternalFrame> addedframes = getInternalFrames(UserFrame.class);
                UserFrame userCreationFrame = null;
                for (InternalFrame frame : addedframes)
                {
                    if (((UserFrame)frame).isUserCreationFrame())
                    {
                        userCreationFrame = ((UserFrame)frame);
                        break;
                    }
                }
                if (userCreationFrame != null)
                    try { userCreationFrame.setSelected (true); } catch (Exception ex) {}
                else
                    addInternalFrame(new UserFrame());
                break;
            case "practicematch":
                HumanPlayer whitePlayer = new HumanPlayer ();
                whitePlayer.setNickName("Player #1");
                HumanPlayer blackPlayer = new HumanPlayer ();
                blackPlayer.setNickName("Player #2");
                MatchFrame matchFrame = new MatchFrame ();
                matchFrame.setWhitePlayer(whitePlayer);
                matchFrame.setBlackPlayer(blackPlayer);
                addInternalFrame(matchFrame);
                matchFrame.start();
                break;
            case "localmatch":
                HumanPlayer localWhitePlayer = new HumanPlayer ();
                localWhitePlayer.setNickName("Human Player");
                ComputerPlayer localBlackPlayer = new ComputerPlayer (new DefaultSearchAgent());
                localBlackPlayer.setNickName("CPU Player");
                MatchFrame localMatchFrame = new MatchFrame ();
                localMatchFrame.setWhitePlayer(localWhitePlayer);
                localMatchFrame.setBlackPlayer(localBlackPlayer);
                localMatchFrame.setWhiteClock(new Clock(300000));
                localMatchFrame.setBlackClock(new Clock(300000));
                addInternalFrame(localMatchFrame);
                localMatchFrame.start();
                break;
        }
    }
    
    private void updateStatusBar ()
    {
        statusBar.setIcon(ResourceUtils.getImageIcon(Application.getInstance().getResourceImagesPath() + ((Application.getInstance().getConnection().isConnected())?"icons/activated.png":"icons/deactivated.png")));
        statusBar.setText("User: " + (Application.getInstance().getSession().isStarted()? ("\"" + Application.getInstance().getSession().getUser().getFirstName() + " " + Application.getInstance().getSession().getUser().getLastName() + "\"") : ""));
    }
    
    private List<InternalFrame> getInternalFrames()
    {
        List <InternalFrame> frames = new ArrayList<InternalFrame>();
        for (int index = 0; index < desktopPane.getAllFrames().length; index++) 
            if (desktopPane.getAllFrames()[index] instanceof InternalFrame) 
                frames.add((InternalFrame)desktopPane.getAllFrames()[index]);
        return frames;
    }
    
    private void addInternalFrame (InternalFrame frame)
    {
        frame.setLocation (desktopPane.getBounds().width/2 - frame.getWidth()/2, desktopPane.getBounds().height/2 - frame.getHeight()/2);
        desktopPane.add (frame);
    }
    
    private InternalFrame getInternalFrame (Class<? extends InternalFrame> frameClass)
    {
        InternalFrame frameFound = null;
        List <InternalFrame> frames = getInternalFrames();
        for (InternalFrame frame : frames)
        {
            if (frame.getClass().isAssignableFrom(frameClass))
            {
                frameFound = frame;
                break;
            }
        }
        return frameFound;
    }
    
    private List<InternalFrame> getInternalFrames (Class<? extends InternalFrame> frameClass)
    {
        List <InternalFrame> frames = getInternalFrames();
        for (InternalFrame frame : frames)
        {
            if (!frame.getClass().isAssignableFrom(frameClass))
                frames.remove(frame);
        }
        return frames;
    }
    
    private boolean isInternalFrameAdded (Class<? extends InternalFrame> frameClass)
    {
        return getInternalFrame(frameClass) != null;
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
    
    private JRadioButtonMenuItem createRadioButtonMenuItem (String text, String ActionCommand) 
    {
        JRadioButtonMenuItem miNew = new JRadioButtonMenuItem(text);
        miNew.setActionCommand ( ActionCommand );
        miNew.addActionListener(this);
        return miNew;
    }
    
    private JMenu createFileMenu ()
    {
        JMenu menu = new JMenu("File");
        menu.setActionCommand("file");
        menu.add(createMenuItem("Exit", "exit"));
        return menu;
    }
    
    private JMenu createUsersMenu ()
    {
        JMenu menu = new JMenu("Users");
        menu.setActionCommand("users");
        menu.add(createMenuItem("Login User", "login"));
        menu.add(createMenuItem("Logout User", "logout"));
        menu.addSeparator();
        menu.add(createMenuItem("Edit User Account", "editUser"));
        menu.add(createMenuItem("Create User Account", "createUser"));
        return menu;
    }
    
    private JMenu createPlayMenu ()
    {
        JMenu menu = new JMenu("Play");
        menu.setActionCommand("play");
        menu.add(createMenuItem("Practice Match", "practicematch"));
        menu.add(createMenuItem("Local Match", "localmatch"));
        return menu;
    }
    
    private JMenu createHelpMenu ()
    {
        JMenu menu = new JMenu("Help");
        menu.add (createMenuItem("About ...", "about"));
        return menu;
    }
    
    private JMenu createSkinsMenu ()
    {
        JMenu menu = new JMenu("Skins");
        menu.setActionCommand("skins");
        menu.add(createRadioButtonMenuItem("AutumnSkin", "skin-AutumnSkin")); 
        menu.add(createRadioButtonMenuItem("BusinessBlackSteelSkin", "skin-BusinessBlackSteelSkin")); 
        menu.add(createRadioButtonMenuItem("BusinessBlueSteelSkin", "skin-BusinessBlueSteelSkin")); 
        menu.add(createRadioButtonMenuItem("BusinessSkin", "skin-BusinessSkin"));
        menu.add(createRadioButtonMenuItem("ChallengerDeepSkin", "skin-ChallengerDeepSkin"));
        menu.add(createRadioButtonMenuItem("CremeCoffeeSkin", "skin-CremeCoffeeSkin"));
        menu.add(createRadioButtonMenuItem("CremeSkin", "skin-CremeSkin"));
        menu.add(createRadioButtonMenuItem("DustCoffeeSkin", "skin-DustCoffeeSkin"));
        menu.add(createRadioButtonMenuItem("DustSkin", "skin-DustSkin"));
        menu.add(createRadioButtonMenuItem("EmeraldDuskSkin", "skin-EmeraldDuskSkin"));
        menu.add(createRadioButtonMenuItem("MagmaSkin", "skin-MagmaSkin"));
        menu.add(createRadioButtonMenuItem("MistAquaSkin", "skin-MistAquaSkin"));
        menu.add(createRadioButtonMenuItem("MistSilverSkin", "skin-MistSilverSkin"));
        menu.add(createRadioButtonMenuItem("ModerateSkin", "skin-ModerateSkin"));
        menu.add(createRadioButtonMenuItem("NebulaBrickWallSkin", "skin-NebulaBrickWallSkin"));
        menu.add(createRadioButtonMenuItem("NebulaSkin", "skin-NebulaSkin"));
        menu.add(createRadioButtonMenuItem("OfficeBlue2007Skin", "skin-OfficeBlue2007Skin"));
        menu.add(createRadioButtonMenuItem("OfficeSilver2007Skin", "skin-OfficeSilver2007Skin"));
        menu.add(createRadioButtonMenuItem("RavenGraphiteGlassSkin", "skin-RavenGraphiteGlassSkin"));
        menu.add(createRadioButtonMenuItem("RavenGraphiteSkin", "skin-RavenGraphiteSkin"));
        menu.add(createRadioButtonMenuItem("RavenSkin", "skin-RavenSkin"));
        menu.add(createRadioButtonMenuItem("SaharaSkin", "skin-SaharaSkin")); 
        return menu;
    }
    
    private DesktopPane createDesktopPane ()
    {
        return new DesktopPane();
    }

    private StatusBar createStatusBar ()
    {
        return new StatusBar();
    }

    @Override
    public void onSessionStarted (Session session)
    {
        update();   
    }
    
    @Override
    public void onSessionBeforeDestroyed (Session session)
    {
        closeFrames(true);
    }
    
    @Override
    public void onSessionDestroyed (Session session)
    {
        update();
    }

    @Override
    public void onConnectionStarted ()
    {
        update();
    }

    @Override
    public void onConnectionEnded ()
    {
        update();
        if (Application.getInstance().getSession().isStarted())
        {
            SwingUtilities.invokeLater(new Runnable ()
            {
                @Override
                public void run ()
                {
                    ReconnectDialog dialog = new ReconnectDialog(MainFrame.this);
                    dialog.setLocationRelativeTo(desktopPane);
                    dialog.setVisible(true);
                }   
            });
        }
    }

    @Override
    public void onDataReceived (JSONObject json)
    {
        
    }

    @Override
    public void onDataSent (JSONObject json)
    {
    }
    
    public void windowOpened (WindowEvent we){}
    public void windowClosed (WindowEvent we){}
    public void windowIconified (WindowEvent we){}
    public void windowDeiconified (WindowEvent we) {}
    public void windowActivated (WindowEvent we) {}
    public void windowDeactivated (WindowEvent we){}
}
