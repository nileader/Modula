package dev.modula.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

    /**
     * Normalizes package names by ensuring each ends with a dot ('.').<br/>
     *
     * @param packages the original package names.<br/>
     *                 e.g. <br/>
     *                 dev.modula.core <br/>
     *                 dev.modula.util. <br/>
     *                 dev.modula.demo <br/>
     * @return a new set of normalized package name set, each ends with a dot ('.').<br>
     *                 e.g. <br/>
     *                 dev.modula.core. <br/>
     *                 dev.modula.util. <br/>
     *                 dev.modula.demo. <br/>
     */
    public static Set<String> normalizeSharedPackages(Set<String> packages) {
        Set<String> result = new HashSet<>();
        for (String pkg : packages) {
            if (!pkg.endsWith(".")) {
                pkg = pkg + ".";
            }
            result.add(pkg);
        }
        return result;
    }

}