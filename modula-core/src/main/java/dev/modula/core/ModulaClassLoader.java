// SPDX-License-Identifier: Apache-2.0
// Copyright (c) 2015 Modula Authors. All rights reserved.
package dev.modula.core;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A custom class loader that loads classes from an adapter JAR and its dependency JARs
 * with strict isolation and controlled sharing.
 *
 * <p>Classes are loaded from the following sources in order:
 * <ol>
 *   <li>Adapter JAR: only exported classes or classes in shared packages are accessible.</li>
 *   <li>Dependency JARs: all classes are private to the module and not visible externally.</li>
 * </ol>
 *
 * <p>Each JAR is associated with its own {@link CodeSource}, ensuring accurate class origin metadata.</p>
 */
public final class ModulaClassLoader extends ClassLoader {
    /**
     * The JAR file containing the module's adapter implementation.
     */
    private final JarFile adapterJarFile;

    /**
     * The code source corresponding to the adapter JAR, used for defining classes from it.
     */
    private final CodeSource adapterCodeSource;

    /**
     * The list of JAR files containing the module's private dependencies.
     */
    private final List<JarFile> dependencyJarFiles;

    /**
     * The list of code sources corresponding to each dependency JAR.
     */
    private final List<CodeSource> dependencyCodeSources;

    /**
     * Normalized set of shared package prefixes (each ends with '.').
     * Classes in these packages may be loaded by the parent class loader.
     */
    private final Set<String> sharedPackages;

    /**
     * Set of fully qualified class names that are explicitly exported by the module.
     */
    private final Set<String> exportedClasses;

    /**
     * Constructs a new {@code ModulaClassLoader} for the given module specification.
     *
     * @param spec the module specification
     * @param parent the parent class loader
     * @throws RuntimeException if any JAR file cannot be opened
     */
    public ModulaClassLoader(ModuleSpec spec, ClassLoader parent) {
        super(parent);
        try {
            // Adapter JAR
            URL adapterUrl = spec.getAdapterJar().toUri().toURL();
            this.adapterJarFile = new JarFile(spec.getAdapterJar().toFile());
            this.adapterCodeSource = new CodeSource(adapterUrl, (Certificate[]) null);
            // Dependency JARs
            this.dependencyJarFiles = new ArrayList<>();
            this.dependencyCodeSources = new ArrayList<>();
            for (Path dep : spec.getDependencyJars()) {
                JarFile _jar = new JarFile(dep.toFile());
                URL depUrl = dep.toUri().toURL();
                dependencyJarFiles.add(_jar);
                dependencyCodeSources.add(new CodeSource(depUrl, (java.security.cert.Certificate[]) null));
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot open JAR", e);
        }
        this.sharedPackages = normalizeSharedPackages(spec.getSharedPackages());
        this.exportedClasses = Collections.unmodifiableSet(spec.getExportedClasses());
    }

    /**
     * Normalizes package names by ensuring each ends with a dot ('.').
     *
     * @param packages the original package names
     * @return a new set of normalized package names
     */
    private static Set<String> normalizeSharedPackages(Set<String> packages) {
        Set<String> result = new HashSet<>();
        for (String pkg : packages) {
            if (!pkg.endsWith(".")) {
                pkg = pkg + ".";
            }
            result.add(pkg);
        }
        return result;
    }

    /**
     * Attempts to find and define a class by searching first in the adapter JAR
     * (if allowed by export/shared rules), then in dependency JARs.
     *
     * @param name the fully qualified class name
     * @return the loaded and defined class
     * @throws ClassNotFoundException if the class is not found in any JAR
     * @throws SecurityException if the class is in the adapter JAR but not exported or shared
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 1. 从 adapter JAR 中加载（仅限 exported 或 shared）
        byte[] bytes = loadClassData(adapterJarFile, name);
        if (bytes != null) {
            if (exportedClasses.contains(name) || isSharedPackage(name)) {
                return defineClass(name, bytes, 0, bytes.length, new ProtectionDomain(adapterCodeSource, null));
            } else {
                throw new SecurityException("Class not accessible: " + name);
            }
        }
        // 2. 从 dependency JARs 中加载（私有依赖）
        for (int i = 0; i < dependencyJarFiles.size(); i++) {
            JarFile jar = dependencyJarFiles.get(i);
            CodeSource cs_dep = dependencyCodeSources.get(i);
            byte[] _bytes = loadClassData(jar, name);
            if (_bytes != null) {
                return defineClass(name, _bytes, 0, _bytes.length, new ProtectionDomain(cs_dep, null));
            }
        }
        throw new ClassNotFoundException(name);
    }

    /**
     * Checks whether the given class belongs to a shared package.
     *
     * @param className the fully qualified class name
     * @return {@code true} if the class's package is shared, {@code false} otherwise
     */
    private boolean isSharedPackage(String className) {
        int lastDot = className.lastIndexOf('.');
        String packageName = (lastDot == -1) ? "" : className.substring(0, lastDot) + ".";
        for (String shared : sharedPackages) {
            if (packageName.startsWith(shared)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Loads the bytecode of a class from the given JAR file.
     *
     * @param jarFile the JAR file to read from
     * @param className the fully qualified class name
     * @return the class bytecode as a byte array, or {@code null} if not found
     * @throws RuntimeException if an I/O error occurs
     */
    private byte[] loadClassData(JarFile jarFile, String className) {
        String path = className.replace('.', '/') + ".class";
        JarEntry entry = jarFile.getJarEntry(path);
        if (entry == null) return null;
        try (InputStream is = jarFile.getInputStream(entry);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read class: " + className, e);
        }
    }
}