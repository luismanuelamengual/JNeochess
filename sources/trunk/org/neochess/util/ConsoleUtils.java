
package org.neochess.util;

import java.util.ArrayList;
import java.util.List;

public abstract class ConsoleUtils
{
    public static List<String> parseCommand (final String command)
    {
        List<String> tokens = new ArrayList<String>(0);
        try
        {
            int tokenStart = -1;
            boolean inCommaToken = false;
            for (int index = 0; index < command.length(); index++)
            {
                char commandChar = command.charAt(index);
                if (commandChar == '\\')
                {
                    index++;
                }
                else
                {
                    if (!inCommaToken)
                    {
                        if (Character.isWhitespace(commandChar))
                        {
                            if (tokenStart != -1)
                            {
                                tokens.add(command.substring(tokenStart, index).trim());
                                tokenStart = -1;
                            }
                        }
                        else if (commandChar == '"')
                        {
                            if (tokenStart != -1)
                                tokens.add(command.substring(tokenStart, index).trim());
                            tokenStart = index+1;
                            inCommaToken = true;
                        }
                        else if (tokenStart == -1)
                        {
                            tokenStart = index;
                        }
                    }
                    else
                    {
                        if (commandChar == '"')
                        {
                            tokens.add(command.substring(tokenStart, index).trim());
                            inCommaToken = false;
                            tokenStart = -1;
                        }
                    }
                }
            }
            if (tokenStart != -1 && !inCommaToken)
                tokens.add(command.substring(tokenStart).trim());
        }
        catch (Exception exception) {}
        return tokens;
    }
}
