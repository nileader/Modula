package dev.modula.util;

import java.io.File;

public class ClassLoaderUtil {

    /**
     * Get JAR file full path of this class, to see where class loaded
     */
    public static String getJarPath(Class<?> clazz) {
        try {
            String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
            return new File(path).getName();
        } catch (Exception e) {
            return "unknown";
        }
    }

}