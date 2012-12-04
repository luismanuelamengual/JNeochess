
package org.neochess.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public abstract class ResourceUtils
{
    public static URL getResource(String file)
    {
        return ResourceUtils.class.getResource(file);
    }
    
    public static InputStream getResourceAsStream (String file)
    {
        return ResourceUtils.class.getResourceAsStream(file);
    }
    
    public static Image getImage (String imageFile)
    {
        return Toolkit.getDefaultToolkit().getImage(getResource(imageFile));
    }
    
    public static BufferedImage getBufferedImage (String imageFile) throws IOException
    {
        return ImageIO.read(getResourceAsStream(imageFile));
    }
    
    public static ImageIcon getImageIcon (String iconFile)
    {
        return new ImageIcon(getResource(iconFile));
    }
    
    public static void playSound (String soundFile)
    {
        java.applet.AudioClip clip = java.applet.Applet.newAudioClip(getResource(soundFile));
        clip.play();
    }
    
    public static void copyResourceToFile (String resource, String filename)
    {
        File file = new File(filename);
        if (!file.exists())
        {
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            try
            {
                if (file.getParentFile() != null)
                    file.getParentFile().mkdirs();
                if (file.createNewFile())
                {
                    inputStream = getResourceAsStream(resource);
                    fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len = inputStream.read(buffer);
                    while (len != -1) 
                    {
                        fileOutputStream.write(buffer, 0, len);
                        len = inputStream.read(buffer);
                    }
                }
            }
            catch (Exception ex) {}
            try { inputStream.close(); } catch (Exception ex) {}
            try { fileOutputStream.close(); } catch (Exception ex) {}
        }
    }
}
