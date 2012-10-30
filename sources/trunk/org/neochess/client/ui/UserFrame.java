
package org.neochess.client.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import org.json.simple.JSONObject;
import org.neochess.client.Application;
import org.neochess.client.Connection.ConnectionListener;
import org.neochess.engine.User;
import org.neochess.util.ResourceUtils;

public class UserFrame extends InternalFrame implements ConnectionListener
{
    private User user;
    private JTextField usernameTextField;
    private JPasswordField userpasswordField;
    private JPasswordField userpasswordrepeatField;
    private JTextField firstnameTextField;
    private JTextField lastnameTextField;
    private JTextField nicknameTextField;
    private JComboBox avatarComboBox;

    public UserFrame()
    {
        this(null);
    }

    public UserFrame(User user)
    {
        super();
        this.user = user;
        setTitle(isUserCreationFrame() ? "User Creation" : "User Edition");
        setPreferredSize(new Dimension(440, 300));
        setResizable(true);
        setLayout(new BorderLayout());
        add(createUserAccountPanel(), BorderLayout.NORTH);
        add(createButtonsPanel(), BorderLayout.SOUTH);
        setVisible(true);
        pack();
        Application.getInstance().getConnection().addConnectionListener(this);
    }

    @Override
    public void dispose ()
    {
        Application.getInstance().getConnection().removeConnectionListener(this);
        user = null;
        usernameTextField = null;
        userpasswordField = null;
        userpasswordrepeatField = null;
        firstnameTextField = null;
        lastnameTextField = null;
        nicknameTextField = null;
        avatarComboBox = null;
        super.dispose();
    }

    public boolean isUserCreationFrame()
    {
        return (user == null) ? true : false;
    }

    public boolean isUserModificationFrame()
    {
        return (user != null) ? true : false;
    }

    private ImageIcon[] getAvatarImageIcons()
    {
        ImageIcon[] avatarIcons = new ImageIcon[7];
        for (int index = 0; index < avatarIcons.length; index++)
            avatarIcons[index] = new ImageIcon(ResourceUtils.getImage(Application.getInstance().getResourceImagesPath() + "avatars/avatar" + index + ".gif").getScaledInstance(25, 25, Image.SCALE_SMOOTH));
        return avatarIcons;
    }

    private JPanel createUserAccountPanel()
    {
        JPanel userPanel = new JPanel();   
        userPanel.setLayout(new GridBagLayout());
        userPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 2, 10));
        JLabel usernameLabel = new JLabel("Username: ");
        JLabel userpasswordLabel = new JLabel("Password: ");
        JLabel userpasswordrepeatLabel = new JLabel("Repeat Password: ");
        JLabel firstnameLabel = new JLabel("First Name: ");
        JLabel lastnameLabel = new JLabel("Last Name: ");
        JLabel nicknameLabel = new JLabel("Nickname: ");
        JLabel avatarLabel = new JLabel("Avatar: ");
        usernameTextField = new JTextField();
        userpasswordField = new JPasswordField();
        userpasswordrepeatField = new JPasswordField();
        firstnameTextField = new JTextField();
        lastnameTextField = new JTextField();
        nicknameTextField = new JTextField();
        avatarComboBox = new JComboBox(getAvatarImageIcons());
        usernameLabel.setAlignmentX(JLabel.LEFT);
        userpasswordLabel.setAlignmentX(JLabel.LEFT);
        userpasswordrepeatLabel.setAlignmentX(JLabel.LEFT);
        avatarComboBox.setRenderer(new ComboBoxRenderer());
        firstnameLabel.setAlignmentX(JLabel.LEFT);
        lastnameLabel.setAlignmentX(JLabel.LEFT);
        nicknameLabel.setAlignmentX(JLabel.LEFT);
        avatarLabel.setAlignmentX(JLabel.LEFT);
        avatarComboBox.setAlignmentX(JLabel.LEFT);
        addGridBagComponent(userPanel, usernameLabel, 0, 0);
        addGridBagComponent(userPanel, usernameTextField, 0, 1);
        addGridBagComponent(userPanel, userpasswordLabel, 1, 0);
        addGridBagComponent(userPanel, userpasswordField, 1, 1);
        addGridBagComponent(userPanel, userpasswordrepeatLabel, 2, 0);
        addGridBagComponent(userPanel, userpasswordrepeatField, 2, 1);
        addGridBagComponent(userPanel, firstnameLabel, 3, 0);
        addGridBagComponent(userPanel, firstnameTextField, 3, 1);
        addGridBagComponent(userPanel, lastnameLabel, 4, 0);
        addGridBagComponent(userPanel, lastnameTextField, 4, 1);
        addGridBagComponent(userPanel, nicknameLabel, 5, 0);
        addGridBagComponent(userPanel, nicknameTextField, 5, 1);
        addGridBagComponent(userPanel, avatarLabel, 6, 0);
        addGridBagComponent(userPanel, avatarComboBox, 6, 1);
        if (isUserModificationFrame())
        {
            usernameTextField.setText(user.getUserName());
            userpasswordField.setText(user.getPassword());
            userpasswordrepeatField.setText(user.getPassword());
            firstnameTextField.setText(user.getFirstName());
            lastnameTextField.setText(user.getLastName());
            nicknameTextField.setText(user.getNickName());
            String userImageUrl = user.getImageUrl();
            if (userImageUrl.startsWith("{"))
                avatarComboBox.setSelectedIndex(Integer.parseInt(user.getImageUrl().substring(1, 2)));
        }
        SwingUtilities.invokeLater(new Runnable() { public void run() { usernameTextField.requestFocusInWindow(); }});
        return userPanel;
    }

    private void addGridBagComponent(JPanel panel, Component component, int row, int col)
    {
        if (panel.getLayout() instanceof GridBagLayout)
        {
            GridBagLayout gridBag = (GridBagLayout) panel.getLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = gbc.WEST;
            gbc.insets = new Insets(1, 0, 4, 0);
            gbc.gridx = col;
            gbc.gridy = row;
            gbc.fill = gbc.BOTH;
            gbc.weightx = col == 1? 1 : 0;
            gridBag.setConstraints(component, gbc);
            panel.add(component);
        }
    }

    private JPanel createButtonsPanel()
    {
        final User editionUser = this.user;
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JButton buttonStart = new JButton();
        buttonStart.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    JSONObject userObject = new JSONObject();
                    if (user != null)
                        userObject.put("id", user.getId());
                    userObject.put("firstName", firstnameTextField.getText());
                    userObject.put("lastName", lastnameTextField.getText());
                    userObject.put("userName", usernameTextField.getText());
                    userObject.put("nickName", nicknameTextField.getText());
                    userObject.put("password", String.valueOf(userpasswordField.getPassword()));
                    userObject.put("passwordRepeat", String.valueOf(userpasswordrepeatField.getPassword()));
                    userObject.put("imageUrl", "{" + avatarComboBox.getSelectedIndex() + "}");
                    JSONObject userCommand = new JSONObject();
                    userCommand.put("cmd", isUserCreationFrame()? "createUser" : "updateUser");
                    userCommand.put("params", userObject);
                    Application.getInstance().getConnection().sendData(userCommand);
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(UserFrame.this, "User creation/edition failure: " + ex.getMessage());
                }
            }
        });
        buttonStart.setText(isUserCreationFrame() ? "Create User" : "Update Data");
        buttonsPanel.add(buttonStart);

        JButton buttonCancel = new JButton();
        buttonCancel.setAction(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                close();
            }
        });
        buttonCancel.setText("Cancel");
        buttonsPanel.add(buttonCancel);

        getRootPane().setDefaultButton(buttonStart);
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
            if (sentCmd.equals("createUser") || sentCmd.equals("updateUser"))
            {
                if (params.get("status").equals("success"))
                {
                    if (user != null)
                    {
                        user.setFirstName(firstnameTextField.getText());
                        user.setLastName(lastnameTextField.getText());
                        user.setUserName(usernameTextField.getText());
                        user.setNickName(nicknameTextField.getText());
                        user.setPassword(String.valueOf(userpasswordField.getPassword()));
                        user.setImageUrl("{" + avatarComboBox.getSelectedIndex() + "}");
                    }
                    JOptionPane.showMessageDialog(UserFrame.this, "User created/edited successfully !!");
                    close();
                }
                else
                {
                    JOptionPane.showMessageDialog(UserFrame.this, "User creation/edition failure: " + String.valueOf(params.get("errorMessage")));
                }
            }
        }
    }

    @Override
    public void onDataSent (JSONObject json)
    {
        
    }
    
    private class ComboBoxRenderer extends JLabel implements ListCellRenderer
    {
        public ComboBoxRenderer()
        {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (isSelected)
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            }
            else
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setIcon((ImageIcon) value);
            return this;
        }
    }
}
