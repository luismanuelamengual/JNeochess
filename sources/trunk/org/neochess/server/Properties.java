
package org.neochess.server;

import java.util.HashMap;
import java.util.Map;

public class Properties
{
    private Map<String, String> map;
    
    public Properties ()
    {
        map = new HashMap<String, String>();
    }
    
    public void set(String key, String value)
    {
        map.put(key, value);
    }
    
    public String get(String key)
    {
        String value = map.get(key);
        return value != null? value : "";
    }
}
