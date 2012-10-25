
package org.neochess.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.swing.ImageIcon;

public abstract class GraphicsUtils 
{    
    public static GraphicsConfiguration getDefaultConfiguration() 
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }
 
    public static BufferedImage toCompatibleImage(BufferedImage image, GraphicsConfiguration gc) 
    {
        if (gc == null)
            gc = getDefaultConfiguration();
        int w = image.getWidth();
        int h = image.getHeight();
        int transparency = image.getColorModel().getTransparency();
        BufferedImage result = gc.createCompatibleImage(w, h, transparency);
        Graphics2D g2 = result.createGraphics();
        g2.drawRenderedImage(image, null);
        g2.dispose();
        return result;
    }
 
    public static BufferedImage copy(BufferedImage source, BufferedImage target) 
    {
        Graphics2D g2 = target.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        double scalex = (double) target.getWidth()/ source.getWidth();
        double scaley = (double) target.getHeight()/ source.getHeight();
        AffineTransform xform = AffineTransform.getScaleInstance(scalex, scaley);
        g2.drawRenderedImage(source, xform);
        g2.dispose();
        return target;
    }
 
    public static BufferedImage getScaledInstance(BufferedImage image, int width, int height) 
    {
        return getScaledInstance(image, width, height, getDefaultConfiguration());
    } 
    
    public static BufferedImage getScaledInstance(BufferedImage image, int width, int height, GraphicsConfiguration gc) 
    {
        int transparency = image.getColorModel().getTransparency();
        return copy(image, gc.createCompatibleImage(width, height, transparency));
    }  
    
    public static BufferedImage getScaledInstance(BufferedImage image, int width, int height, ColorModel cm) 
    {
        WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
        boolean isRasterPremultiplied = cm.isAlphaPremultiplied();
        return copy(image, new BufferedImage(cm, raster, isRasterPremultiplied, null));
    }
    
    public static BufferedImage toBufferedImage(Image image) 
    {
        if (image instanceof BufferedImage)
            return (BufferedImage)image;
        
        image = new ImageIcon(image).getImage();
        boolean hasAlpha = true;
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try 
        {
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) 
                transparency = Transparency.BITMASK;    
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {}
    
        if (bimage == null) 
        {
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) 
                type = BufferedImage.TYPE_INT_ARGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
    
        Graphics g = bimage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }
    
    public static ConvolveOp getBlurTransformation (int size) 
    {
        float[] data = new float[size * size];
        float value = 1 / (float) (size * size);
        for (int i = 0; i < data.length; i++) 
            data[i] = value;
        return new ConvolveOp(new Kernel(size, size, data));
    }
    
    public static BufferedImage getImageShadow (BufferedImage original, int shadowSize, int shadowOpacity)
    {
        BufferedImage shadowImage = new BufferedImage(original.getWidth() + shadowSize * 2, original.getHeight() + shadowSize * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = shadowImage.createGraphics();
        g2.drawImage(original, null, shadowSize, shadowSize);
        g2.dispose();
        
        Color shadowColor = new Color(0.0f, 0.0f, 0.0f, 0.3f);
        BufferedImage shadowMask = new BufferedImage(shadowImage.getWidth(), shadowImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < shadowImage.getWidth(); x++) 
        {
            for (int y = 0; y < shadowImage.getHeight(); y++) 
            {
                int argb = shadowImage.getRGB(x, y);
                argb = (int) ((argb >> 24 & 0xFF) * shadowOpacity) << 24 | shadowColor.getRGB() & 0x00FFFFFF;
                shadowMask.setRGB(x, y, argb);
            }
        }
        
        BufferedImage shadowMaskedImage = new BufferedImage(shadowImage.getWidth(), shadowImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        getBlurTransformation(shadowSize).filter ( shadowMask, shadowMaskedImage );
        return shadowMaskedImage;
    }
    
    public static BufferedImage getShadowedImage ( BufferedImage original, int shadowSize, int shadowOpacity )
    {
        BufferedImage originalShadow = getImageShadow( original, shadowSize, shadowOpacity );
        BufferedImage shadowedImage = new BufferedImage(originalShadow.getWidth(), originalShadow.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = shadowedImage.createGraphics();
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g2.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
        g2.drawImage( originalShadow, null, 0, 0 );
        g2.drawImage( original, null, shadowSize, shadowSize );
        g2.dispose();
        return shadowedImage;
    }
}

