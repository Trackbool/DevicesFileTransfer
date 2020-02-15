package dft.util;

import java.util.Map;

public class SystemUtils {
    public static String getSystemName()
    {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else return env.getOrDefault("HOSTNAME", "Unknown");
    }
}
