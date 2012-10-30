
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.json.simple.JSONObject;
import org.neochess.client.Application;
import org.neochess.client.Connection.ConnectionListener;
import org.neochess.engine.User;
import org.neochess.util.GraphicsUtils;
import org.neochess.util.ResourceUtils;
import org.neochess.util.UserInterfaceUtils;

public class LoginFrame extends InternalFrame implements ConnectionListener
{
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame ()
    {
        super();
        setTitle(Application.getInstance().getShortTitle() + " - User Login");
        setLayout(new BorderLayout(4, 4));
        setResizable(false);
        add(createScreenerPanel(), BorderLayout.CENTER);
        add(createControlsPanel(), BorderLayout.SOUTH);
        setVisible(true);
        pack();
        Application.getInstance().getConnection().addConnectionListener(this);
    }

    @Override
    public void dispose ()
    {
        Application.getInstance().getConnection().removeConnectionListener(this);
        usernameField = null;
        passwordField = null;
        removeAll();
        super.dispose();
    }
    
    private JPanel createScreenerPanel ()
    {
        ImageIcon icon1 = new ImageIcon();
        ImageIcon icon2 = new ImageIcon();
        try { icon1.setImage(GraphicsUtils.getScaledInstance(ResourceUtils.getBufferedImage(Application.getInstance().getResourceImagesPath() + "login1.jpg"), 380, 240)); } catch (Exception ex1) {}
        try { icon2.setImage(GraphicsUtils.getScaledInstance(ResourceUtils.getBufferedImage(Application.getInstance().getResourceImagesPath() + "login2.jpg"), 380, 240)); } catch (Exception ex1) {}        
        JLabel loginImage1 = new JLabel(icon1);
        loginImage1.setBorder(new LineBorder(UserInterfaceUtils.getColor("Panel.background"), 4));
        JLabel loginImage2 = new JLabel(icon2);
        loginImage2.setBorder(new LineBorder(UserInterfaceUtils.getColor("Panel.background"), 4));
        JPanel screener = new JPanel();
        CardLayout layout = new CardLayout();
        screener.setLayout(layout);
        screener.add("login1", loginImage1);
        screener.add("login2", loginImage2);
        Random randomGenerator = new Random();
        int randomNumber = randomGenerator.nextInt(2);
        layout.show(screener, randomNumber == 0? "login1" : "login2");
        return screener;
    }
    
    private JPanel createControlsPanel ()
    {
        Font defaultFont = new Font("Arial", Font.BOLD, 12);
        Color defaultColor = new Color(197, 175, 123);
        Dimension defaultDimension = new Dimension(120, 16);
        JPanel controlPane = new JPanel();
        controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
        controlPane.setBorder(new EmptyBorder(0, 30, 0, 30));
        controlPane.setOpaque(false);
        JLabel usernamelabel = new JLabel("Username");
        usernamelabel.setFont(defaultFont);
        usernamelabel.setPreferredSize(defaultDimension);
        usernamelabel.setAlignmentX(LEFT_ALIGNMENT);
        usernamelabel.setForeground(defaultColor);
        JLabel passwordlabel = new JLabel("Password");
        passwordlabel.setFont(defaultFont);
        passwordlabel.setPreferredSize(defaultDimension);
        passwordlabel.setAlignmentX(LEFT_ALIGNMENT);
        passwordlabel.setForeground(defaultColor);
        usernameField = new JTextField(16);
        usernameField.setPreferredSize(defaultDimension);
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField = new JPasswordField(16);
        passwordField.setPreferredSize(defaultDimension);
        passwordField.setAlignmentX(LEFT_ALIGNMENT);
        controlPane.add(usernamelabel);
        controlPane.add(usernameField);
        controlPane.add(passwordlabel);
        controlPane.add(passwordField);
        controlPane.add(createButtonsPanel());
        SwingUtilities.invokeLater(new Runnable() {@Override public void run() { usernameField.requestFocusInWindow(); }});
        return controlPane;
    }

    private JPanel createButtonsPanel ()
    {
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttonsPanel.setAlignmentX(LEFT_ALIGNMENT);
        JButton buttonStartSession = new JButton();
        buttonStartSession.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject();
                    JSONObject paramsObject = new JSONObject();
                    jsonObject.put("cmd", "loginUser");
                    jsonObject.put("params", paramsObject);
                    paramsObject.put("userName", usernameField.getText());
                    paramsObject.put("password", String.valueOf(passwordField.getPassword()));
                    Application.getInstance().getConnection().sendData(jsonObject);
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog (LoginFrame.this, "Login Failure: " + ex.getMessage());
                }
            }
        });
        buttonStartSession.setText("Ok");
        buttonsPanel.add(buttonStartSession);
        getRootPane().setDefaultButton(buttonStartSession);
        JButton buttonCancel = new JButton();
        buttonCancel.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed (ActionEvent e)
            {
                dispose();
            }
        });
        buttonCancel.setText("Cancel");
        buttonsPanel.add(buttonCancel);

        return buttonsPanel;
    }

    @Override
    public void onConnectionStarted ()
    {
        
    }

    @Override
    public void onConnectionEnded ()
    {
        
    }

    @Override
    public void onDataReceived (JSONObject json)
    {
        String cmd = String.valueOf(json.get("cmd"));
        if (cmd.equals("response"))
        {
            JSONObject params = (JSONObject)json.get("params");
            String sentCmd = String.valueOf(params.get("cmd"));
            if (sentCmd.equals("loginUser"))
            {
                if (params.get("status").equals("success"))
                {
                    JSONObject userObject = (JSONObject)params.get("user");
                    User user = new User ();
                    if (userObject.get("id") != null)
                        user.setId(((Long)userObject.get("id")).intValue());
                    if (userObject.get("firstName") != null)
                        user.setFirstName(String.valueOf(userObject.get("firstName")));
                    if (userObject.get("lastName") != null)
                        user.setLastName(String.valueOf(userObject.get("lastName")));
                    if (userObject.get("userName") != null)
                        user.setUserName(String.valueOf(userObject.get("userName")));
                    if (userObject.get("nickName") != null)
                        user.setNickName(String.valueOf(userObject.get("nickName")));
                    if (userObject.get("password") != null)
                        user.setPassword(String.valueOf(userObject.get("password")));
                    if (userObject.get("imageUrl") != null)
                        user.setImageUrl(String.valueOf(userObject.get("imageUrl")));
                    if (userObject.get("elo") != null)
                        user.setElo(((Long)userObject.get("elo")).intValue());
                    Application.getInstance().getSession().startSession(user);
                    close();
                }
                else
                {
                    JOptionPane.showMessageDialog(LoginFrame.this, "User login failure: " + String.valueOf(params.get("errorMessage")));
                }
            }
        }
    }

    @Override
    public void onDataSent (JSONObject json)
    {
        
    }
}
