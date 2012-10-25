
package org.neochess.client.ui;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JDesktopPane;
import org.neochess.client.Application;
import org.neochess.util.ResourceUtils;

public class DesktopPane extends JDesktopPane
{
    private Image backgroundImage;

    public DesktopPane ()
    {
        try
        {
            backgroundImage = ResourceUtils.getImage(Application.getInstance().getResourceImagesPath() + "mainBackground.jpg");
        }
        catch (Exception e) {}
    }

    @Override
    public void paintComponent (Graphics g)
    {
        if (backgroundImage != null)
        {
            int imageWidth = backgroundImage.getWidth(this);
            int imageHeight = backgroundImage.getHeight(this);
            if (imageWidth > 0 && imageHeight > 0)
                for (int posY = 0; posY < getHeight(); posY += imageHeight)
                    for (int posX = 0; posX < getWidth(); posX += imageWidth)
                        g.drawImage (backgroundImage, posX, posY, imageWidth, imageHeight, this);
        }
    }
}
