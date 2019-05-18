// SPDX-License-Identifier: Apache-2.0
// Copyright (c) 2015 Modula Authors. All rights reserved.
package dev.modula.core;

import java.util.Set;

/**
 * The entry point for loading and managing isolated modules at runtime.
 * It uses a custom class loader to load modules defined by {@link ModuleSpec},
 * providing classpath isolation and controlled class exposure.
 */
public final class ModulaRuntime {
    /**
     * Loads a module based on the given specification and returns an {@link IsolatedModule}
     * that allows instantiation of exported classes.
     *
     * @param spec the module specification
     * @return an isolated module instance
     * @throws RuntimeException if module loading fails
     */
    public IsolatedModule load(ModuleSpec spec) {
        try {
            ModulaClassLoader loader = new ModulaClassLoader(
                    spec,
                    getClass().getClassLoader()
            );
            return new IsolatedModule(loader, spec.getExportedClasses());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load module: " + spec.getName(), e);
        }
    }

    /**
     * Represents an isolated module that provides controlled access to its exported classes.
     * Instances of exported classes can be obtained via {@link #getInstance(String, Class)}.
     */
    public static final class IsolatedModule {
        private final ModulaClassLoader loader;
        private final Set<String> exportedClasses;

        IsolatedModule(ModulaClassLoader loader, Set<String> exportedClasses) {
            this.loader = loader;
            this.exportedClasses = exportedClasses;
        }

        /**
         * Instantiates an exported class by name and casts it to the specified interface type.
         *
         * <p>The class must be listed in the module's exported classes, have a public no-arg constructor,
         * and implement the given interface type.</p>
         *
         * @param className the fully qualified name of the class to instantiate
         * @param interfaceType the expected interface or superclass type
         * @param <T> the type of the interface
         * @return an instance of the class cast to the interface type
         * @throws IllegalArgumentException if the class is not exported
         * @throws RuntimeException if instantiation or casting fails
         */
        @SuppressWarnings("unchecked")
        public <T> T getInstance(String className, Class<T> interfaceType) {
            if (!exportedClasses.contains(className)) {
                throw new IllegalArgumentException("Class not exported: " + className);
            }
            try {
                Class<?> clazz = loader.loadClass(className);
                //System.out.println("[DEBUG]>>> Loaded class: " + clazz.getName() + " by loader: " + clazz.getClassLoader());
                Object instance = clazz.getDeclaredConstructor().newInstance();
                if (!interfaceType.isInstance(instance)) {
                    throw new ClassCastException(className + " does not implement " + interfaceType.getName());
                }
                return (T) instance;
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate " + className, e);
            }
        }
    }
}