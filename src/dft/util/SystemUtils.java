package dft.util;

import java.io.File;
import java.util.Map;

public class SystemUtils {
    public static String getSystemName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else return env.getOrDefault("HOSTNAME", "Unknown");
    }

    public static String getOs() {
        return System.getProperty("os.name");
    }

    public static File getDownloadsDirectory() {
        String home = System.getProperty("user.home");
        return new File(home + "/Downloads");
    }
}
