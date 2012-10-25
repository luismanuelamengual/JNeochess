
package org.neochess.util;

import java.awt.Color;
import javax.swing.UIManager;

public class UserInterfaceUtils
{
    public static Color getColor (String key) 
    {
        return (Color)UIManager.getDefaults().getColor(key);
    }
}
