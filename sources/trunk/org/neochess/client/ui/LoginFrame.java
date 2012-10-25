
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.json.simple.JSONObject;
import org.neochess.client.Application;
import org.neochess.client.Connection.ConnectionListener;
import org.neochess.engine.User;

public class LoginFrame extends InternalFrame implements ConnectionListener
{
    private static final Color COLOR_GRADIENTSTART = new Color(255, 255, 255, 255);
    private static final Color COLOR_GRADIENTEND = new Color(237, 215, 163, 200);
    private static final Color COLOR_CURVESTART = new Color(155, 116, 62, 200);
    private static final Color COLOR_CURVEEND = new Color(225, 215, 157, 0);
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Timer animation;

    public LoginFrame ()
    {
        super();
        setPreferredSize(new Dimension(360, 250));
        setSize(new Dimension(500, 400));
        setTitle(Application.getInstance().getShortTitle() + " - User Login");
        setLayout(new BorderLayout(4, 4));
        setResizable(false);
        CurvesPanel contentPane = new CurvesPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createControlsPanel(), BorderLayout.CENTER);
        getContentPane().add(BorderLayout.CENTER, contentPane);
        setVisible(true);
        pack();
        startAnimation();
        Application.getInstance().getConnection().addConnectionListener(this);
    }

    @Override
    public void dispose ()
    {
        Application.getInstance().getConnection().removeConnectionListener(this);
        stopAnimation();
        usernameField = null;
        passwordField = null;
        removeAll();
        super.dispose();
    }
    
    private void startAnimation ()
    {
        if (animation == null)
        {
            animation = new Timer(50, new ActionListener()
            {
                @Override
                public void actionPerformed (ActionEvent ae)
                {
                    CurvesPanel panel = (CurvesPanel)LoginFrame.this.getContentPane().getComponent(0);
                    panel.repaint();
                }
            });
            animation.start();
        }
    }
    
    private void stopAnimation ()
    {
        if (animation != null)
        {
            animation.stop();
            animation = null;
        }
    }
    
    private JPanel createControlsPanel ()
    {
        Font defaultFont = new Font("Arial", Font.BOLD, 12);
        Color defaultColor = new Color(197, 175, 123);
        Dimension defaultDimension = new Dimension(120, 16);
        JPanel controlPane = new JPanel();
        controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
        controlPane.setBorder(new EmptyBorder(40, 40, 40, 40));
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
        controlPane.add(Box.createVerticalStrut(5));
        controlPane.add(usernamelabel);
        controlPane.add(usernameField);
        controlPane.add(passwordlabel);
        controlPane.add(passwordField);
        controlPane.add(createButtonsPanel());
        controlPane.add(Box.createVerticalStrut(100));
        SwingUtilities.invokeLater(new Runnable() { public void run() { usernameField.requestFocusInWindow(); }});
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

    private class CurvesPanel extends GradientPanel
    {
        private int counter = 0;

        @Override
        public void paintComponent (Graphics g)
        {
            super.paintComponent(g);
            counter++;
            Graphics2D g2 = (Graphics2D) g;
            RenderingHints hints = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHints(hints);
            float width = getWidth();
            float height = getHeight();
            g2.translate(0, -30);
            drawCurve(g2, 20.0f, -10.0f, 20.0f, -10.0f, width / 2.0f - 40.0f, 10.0f, 0.0f, -5.0f, width / 2.0f + 40, 1.0f, 0.0f, 5.0f, 50.0f, 5.0f, false);
            g2.translate(0, 30);
            g2.translate(0, height - 60);
            drawCurve(g2, 30.0f, -15.0f, 50.0f, 15.0f, width / 2.0f - 40.0f, 1.0f, 15.0f, -25.0f, width / 2.0f, 1.0f / 2.0f, 0.0f, 25.0f, 15.0f, 6.0f, false);
            g2.translate(0, -height + 60);
            drawCurve(g2, height - 35.0f, -5.0f, height - 50.0f, 10.0f, width / 2.0f - 40.0f, 1.0f, height - 35.0f, -25.0f, width / 2.0f, 1.0f / 2.0f, height - 20.0f, 25.0f, 25.0f, 4.0f, true);
        }

        private void drawCurve (Graphics2D g2, float y1, float y1_offset, float y2, float y2_offset, float cx1, float cx1_offset, float cy1, float cy1_offset, float cx2, float cx2_offset, float cy2, float cy2_offset, float thickness, float speed, boolean invert)
        {
            float width = getWidth();
            float height = getHeight();
            double offset = Math.sin(counter / (speed * Math.PI));
            float start_x = 0.0f;
            float start_y = y1 + (float) (offset * y1_offset);
            float end_x = width;
            float end_y = y2 + (float) (offset * y2_offset);
            float ctrl1_x = (float) offset * cx1_offset + cx1;
            float ctrl1_y = cy1 + (float) (offset * cy1_offset);
            float ctrl2_x = (float) (offset * cx2_offset) + cx2;
            float ctrl2_y = (float) (offset * cy2_offset) + cy2;
            CubicCurve2D curve = new CubicCurve2D.Double(start_x, start_y, ctrl1_x, ctrl1_y, ctrl2_x, ctrl2_y, end_x, end_y);
            GeneralPath path = new GeneralPath(curve);
            path.lineTo(width, height);
            path.lineTo(0, height);
            path.closePath();
            Area thickCurve = new Area((Shape) path.clone());
            AffineTransform translation = AffineTransform.getTranslateInstance(0, thickness);
            path.transform(translation);
            thickCurve.subtract(new Area(path));
            Color start = COLOR_CURVESTART;
            Color end = COLOR_CURVEEND;
            Rectangle bounds = thickCurve.getBounds();
            GradientPaint painter = new GradientPaint(0, curve.getBounds().y, invert ? end : start, 0, bounds.y + bounds.height, invert ? start : end);
            Paint oldPainter = g2.getPaint();
            g2.setPaint(painter);
            g2.fill(thickCurve);
            g2.setPaint(oldPainter);
        }
    }

    private class GradientPanel extends JPanel
    {
        @Override
        public void paintComponent (Graphics g)
        {
            int height = getHeight();
            Color gradientStart = COLOR_GRADIENTSTART;
            Color gradientEnd = COLOR_GRADIENTEND;
            Graphics2D g2 = (Graphics2D) g;
            GradientPaint painter = new GradientPaint(0, 0, gradientStart, 0, height, gradientEnd);
            g2.setPaint(painter);
            g2.fill(g2.getClip());
        }
    }
}




//public class jcMainFrameLoginFrame extends JInternalFrame {
//    
//    private jcMainFrame _mainFrame;
//    
//    private JList _usersList;
//    
//    /** Creates a new instance of jcLoginPanel */
//    public jcMainFrameLoginFrame ( jcMainFrame mainFrame ) {
//        super(null,true, true, false, true);
//        setMinimumSize( new Dimension (300, 300) );
//        setFrameIcon( jcResource.getImageIcon( "board.png" ) );
//        setDefaultCloseOperation( JInternalFrame.DISPOSE_ON_CLOSE );
//        setTitle ( jcVersion.getShortTitle() + " - User Login" );
//        setLayout( new BorderLayout(4, 4) );
//        setResizable(false);
//        _mainFrame = mainFrame;
//        add( _createScreenerPanel(), BorderLayout.NORTH );
//        add( _createUsersPanel (), BorderLayout.CENTER );
//        add( _createButtonsPanel (), BorderLayout.SOUTH );
//        setVisible(true);
//        pack();
//    }
//    
//    private JPanel _createScreenerPanel ()
//    {
//        JLabel loginImage1 = new JLabel( jcResource.getImageIcon("login1.jpg") );
//        loginImage1.setBorder(new LineBorder(jcUIHelper.getColor("Panel.background"), 4));
//        JLabel loginImage2 = new JLabel( jcResource.getImageIcon("login2.jpg") );
//        loginImage2.setBorder(new LineBorder(jcUIHelper.getColor("Panel.background"), 4));
//        JPanel screener = new JPanel();
//        screener.setLayout( new CardLayout() );
//        screener.add( "login1", loginImage1 );
//        screener.add( "login2", loginImage2 );
//        return screener;
//    }
//    
//    private JList _createUsersPanel ()
//    {
//        _usersList = new JList( jcDatabase.getUsers() );
//        _usersList.setBorder(new LineBorder(jcUIHelper.getColor("Panel.background"), 4));
//        _usersList.setPreferredSize( new Dimension( 400, 80 ));
//        if ( jcDatabase.getUsers().size() > 0 ) {
//            _usersList.setSelectedIndex(0);
//        }
//        return _usersList;
//    }
//    
//    private JPanel _createButtonsPanel ()
//    {
//        JPanel buttonsPanel = new JPanel ();
//        buttonsPanel.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
//        
//        //The Cancel Button
//        JButton buttonCancel = new JButton();
//        buttonCancel.setAction( new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                dispose();
//            }
//        } );
//        buttonCancel.setText("Cancel");
//        buttonsPanel.add( buttonCancel );
//        
//        //The Start Session Button
//        JButton buttonStartSession = new JButton();
//        buttonStartSession.setAction( new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                if ( !_usersList.isSelectionEmpty() ) {
//                    if ( _mainFrame.startSession( jcDatabase.getUsers().get( _usersList.getSelectedIndex() ) ) == true ) {
//                        dispose();
//                    }
//                }
//            }
//        } );
//        buttonStartSession.setText("Start Session");
//        buttonsPanel.add( buttonStartSession );
//        return buttonsPanel;
//    }
//
//}
