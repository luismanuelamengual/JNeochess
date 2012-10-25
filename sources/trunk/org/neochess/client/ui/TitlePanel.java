
package org.neochess.client.ui;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.neochess.util.ColorUtils;
import org.neochess.util.UserInterfaceUtils;

public class TitlePanel extends JPanel 
{    
    public TitlePanel (String title, JPanel panel) 
    {
        setLayout( new BorderLayout(5, 5) );
        add ( new JTitleSubPanel(title), BorderLayout.NORTH );
        add ( panel, BorderLayout.CENTER );
        setBorder(BorderFactory.createEmptyBorder(8, 10, 2, 10));
    }
    
    public class JTitleSubPanel extends JPanel
    {
        public JTitleSubPanel ( String text )
        {
            JLabel label = new JLabel (text);
            label.setFont( new java.awt.Font( "ARIAL", java.awt.Font.BOLD, 12 ) );
            label.setHorizontalAlignment( JLabel.CENTER );
            label.setOpaque(false);
            add( label );
            setBorder(BorderFactory.createEtchedBorder());
        }
        
        @Override
        public void paintComponent ( Graphics screen ) 
        {
            super.paintComponent(screen);
            Color startColor, finishColor;
            startColor = ColorUtils.getLighterColor (UserInterfaceUtils.getColor("InternalFrame.activeTitleBackground"));
            finishColor = Color.WHITE;
            Graphics2D screen2D = (Graphics2D) screen;
            screen2D.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gradientPaint = new GradientPaint(getWidth(), 0, startColor, getWidth(), getHeight(), finishColor);
            screen2D.setPaint(gradientPaint);
            screen2D.fill(new java.awt.geom.Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        }
    }
}
