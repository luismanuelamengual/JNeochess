
package org.neochess.util;

import java.awt.Color;

public abstract class ColorUtils 
{
    public static Color getDarkerColor (Color color)
    {
        return getDarkerColor (color, 15);
    }
    
    public static Color getDarkerColor (Color color, int delta)
    {
        int red = ( color.getRed() > delta ) ? color.getRed() - delta : 0;
        int green = ( color.getGreen() > delta ) ? color.getGreen() - delta : 0;
        int blue = ( color.getBlue() > delta ) ? color.getBlue() - delta : 0;
        return new Color( red, green, blue );
    }
    
    public static Color getLighterColor (Color color)
    {
        return getLighterColor (color, 15);
    }
    
    public static Color getLighterColor (Color color, int delta)
    {
        int red = ( color.getRed() < (255-delta) ) ? color.getRed() + delta : 255;
        int green = ( color.getGreen() < (255-delta) ) ? color.getGreen() + delta : 255;
        int blue = ( color.getBlue() < (255-delta) ) ? color.getBlue() + delta : 255;
        return new Color( red, green, blue );
    }
    
    public static Color getAlphaColor (Color color, int alpha)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
