
package org.neochess.server.handlers;

import java.util.List;
import org.neochess.server.Application;
import org.neochess.server.Console;

public class GeneralHandler extends Handler implements Console.ConsoleListener
{
    @Override
    public void start()
    {
        Application.getInstance().getConsole().addConsoleListener(this);
    }

    @Override
    public void stop()
    {
        Application.getInstance().getConsole().removeConsoleListener(this);
    }

    @Override
    public void onCommandEntered(String command, List<String> commandTokens)
    {
        if (commandTokens.get(0).equals("exit"))
        {
            Application.getInstance().destroy();
        }
        else if (commandTokens.get(0).equals("set"))
        {
            Application.getInstance().getProperties().set(commandTokens.get(1), commandTokens.get(2));
        }
        else if (commandTokens.get(0).equals("get"))
        {
            System.console().printf("%s\n", Application.getInstance().getProperties().get(commandTokens.get(1)));
        }
    }
}
