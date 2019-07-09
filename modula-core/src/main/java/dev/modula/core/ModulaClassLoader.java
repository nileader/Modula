// SPDX-License-Identifier: Apache-2.0
// Copyright (c) 2015 Modula Authors. All rights reserved.
package dev.modula.core;

import dev.modula.util.ClassLoaderUtil;

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
     * Normalized set of shared package prefixes (each ends with '.').<br/>
     * e.g. <br/>
     * dev.modula.core. <br/>
     * dev.modula.util. <br/>
     * dev.modula.demo. <br/>
     * <br/>
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
        this.sharedPackages = ClassLoaderUtil.normalizeSharedPackages(spec.getSharedPackages());
        this.exportedClasses = Collections.unmodifiableSet(spec.getExportedClasses());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // Step 1: check if loaded?
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                if (resolve) resolveClass(loadedClass);
                return loadedClass;
            }

            // Step 2: if shared package，use parent classloader(Main app classload) to load
            if (isSharedPackage(name)) {
                return super.loadClass(name, resolve);
            }

            // Step 3: others, use module self classloader
            try {
                return findClass(name);
            } catch (ClassNotFoundException e) {
                throw new ClassNotFoundException("Can't load class "+ name +" from module",e);
            }
        }
    }

    /**
     * Attempts to find and define a class in module, first adapter jar,then d8y.
     *
     * @param name the fully qualified class name
     * @return the loaded and defined class
     * @throws ClassNotFoundException if the class is not found in any JAR
     * @throws SecurityException if the class is in the adapter JAR but not exported or shared
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // Step3.1. 从 adapter JAR 中加载
        byte[] bytes = loadClassData(adapterJarFile, name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length, new ProtectionDomain(adapterCodeSource, null));
        }

        // Step3.2. 从 dependency JARs 中加载（私有依赖）
        for (int i = 0; i < dependencyJarFiles.size(); i++) {
            JarFile jar = dependencyJarFiles.get(i);
            byte[] _bytes = loadClassData(jar, name);
            if (_bytes != null) {
                CodeSource cs_dep = dependencyCodeSources.get(i);
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
        for (String sharedPkg : sharedPackages) {
            if (packageName.startsWith(sharedPkg)) {
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

    public void close() throws IOException {
        adapterJarFile.close();
        for (JarFile jar : dependencyJarFiles) {
            jar.close();
        }
    }

}