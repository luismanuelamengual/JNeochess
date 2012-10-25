
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.neochess.general.Disposable;

public class MatchOutputPanel extends JPanel implements Disposable
{
    private MatchFrame matchFrame;
    private JTextPane textPane;
    private JButton cleanButton;

    public MatchOutputPanel(MatchFrame matchFrame)
    {
        this.matchFrame = matchFrame;
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setDoubleBuffered(true);
        cleanButton = new JButton();
        cleanButton.setAction(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                clean();
            }
        });
        cleanButton.setText("Clean Output");

        JScrollPane scrollPane = new JScrollPane(textPane);
        setLayout(new BorderLayout(5, 5));
        add(scrollPane, BorderLayout.CENTER);
        add(cleanButton, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        initializeStyles();
    }

    public void dispose()
    {
        textPane = null;
        cleanButton = null;
        matchFrame = null;
        removeAll();
    }

    public void clean()
    {
        textPane.setText("");
    }

    public void appendText(String message)
    {
        appendText(message, "normal");
    }

    public synchronized void appendText(String message, String styleName)
    {
        try
        {
            StyledDocument doc = (StyledDocument) textPane.getDocument();
            textPane.setEditable(true);
            doc.insertString(doc.getLength(), message + "\n", doc.getStyle(styleName));
            textPane.setEditable(false);
            textPane.setCaretPosition(doc.getLength());
        } 
        catch (Exception e)
        {
        }
    }

    private void initializeStyles()
    {
        Style style;
        StyledDocument doc = (StyledDocument) textPane.getDocument();        
        style = doc.addStyle("normal", null);
        StyleConstants.setFontFamily(style, "Arial");
        StyleConstants.setFontSize(style, 9);
        StyleConstants.setForeground(style, new Color(150, 150, 150));
        style = doc.addStyle("information", null);
        StyleConstants.setFontFamily(style, "Arial");
        StyleConstants.setFontSize(style, 9);
        StyleConstants.setForeground(style, new Color(120, 140, 190));
        style = doc.addStyle("alert", null);
        StyleConstants.setFontFamily(style, "Arial");
        StyleConstants.setFontSize(style, 9);
        StyleConstants.setForeground(style, new Color(255, 150, 150));
    }
}
