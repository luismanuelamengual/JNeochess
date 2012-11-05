
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.neochess.client.Application;
import org.neochess.util.GraphicsUtils;
import org.neochess.util.ResourceUtils;
import org.neochess.util.UserInterfaceUtils;

public class LoginFrame extends InternalFrame
{
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame ()
    {
        super();
        setTitle(Application.getInstance().getShortTitle() + " - User Login");
        setLayout(new BorderLayout(4, 4));
        setResizable(false);
//        add(createScreenerPanel(), BorderLayout.CENTER);
//        add(createControlsPanel(), BorderLayout.SOUTH);
        
        add(createControlsPanel(), BorderLayout.CENTER);
        setVisible(true);
        pack();
    }

    @Override
    public void dispose ()
    {
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
        JPanel controlPane = new JPanel();
        controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
        controlPane.setBorder(new EmptyBorder(0, 30, 0, 30));
        controlPane.setOpaque(false);
        JLabel usernamelabel = new JLabel("Username");
        usernamelabel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel passwordlabel = new JLabel("Password");
        passwordlabel.setAlignmentX(LEFT_ALIGNMENT);
        usernameField = new JTextField(16);
        usernameField.setAlignmentX(LEFT_ALIGNMENT);
        passwordField = new JPasswordField(16);
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
                    Application.getInstance().getSession().startSession(usernameField.getText(), String.valueOf(passwordField.getPassword()));
                    close();
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(LoginFrame.this, "User login failure: " + ex.getMessage());
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
}
