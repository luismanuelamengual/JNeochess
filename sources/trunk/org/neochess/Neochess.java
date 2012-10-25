
package org.neochess;

import java.io.IOException;
import javax.swing.SwingUtilities;
import org.neochess.client.Application;

public final class Neochess 
{
    public static void main(final String[] args)
    {
        long maxMemory = Runtime.getRuntime().maxMemory() / 1048576;
        if (maxMemory < 127)
        {
            try
            {
                Runtime.getRuntime().exec("java -Xmx128m -jar Neochess.jar");
            }
            catch (IOException ex)
            {
                String errorMessage = "Java Virtual Machine Heap Memory Size is not enough\n\n";
                errorMessage += "Actual Heap Size: " + maxMemory + " MB\n";
                errorMessage += "Required Heap Size: 128 MB\n\n";
                errorMessage += "Please run: \"java -Xmx128m -jar Neochess.jar\" in the command line";
                System.err.append(errorMessage);
            }
            return;
        }

        if (args.length > 0 && args[0].equals("-server"))
        {
            org.neochess.server.Application.getInstance().start();
        }
        else
        {
            SwingUtilities.invokeLater(new Runnable() 
            {
                public void run()
                {
                    Application.getInstance().start();
                }
            });
        }
    }
}
