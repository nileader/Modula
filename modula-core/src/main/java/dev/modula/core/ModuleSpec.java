// SPDX-License-Identifier: Apache-2.0
// Copyright (c) 2015 Modula Authors. All rights reserved.
package dev.modula.core;

import java.nio.file.Path;
import java.util.*;

/**
 * Specification of a module to be loaded by the Modula runtime.
 * <p>
 * A module includes: <br>
 * 1. an adapter JAR (containing the module's implementation and the entrance of module)<br>
 * 2. a list of dependency JARs<br>
 * 3. shared packages<br>
 * 4. exported classes.
 * </p>
 */
public final class ModuleSpec {
    /**
     * The name of the module.
     */
    private final String name;

    /**
     * The path to the adapter JAR file that contains the module's implementation classes.
     */
    private final Path adapterJar;

    /**
     * An unmodifiable list of paths to dependency JAR files required by the module.
     */
    private final List<Path> dependencyJars;

    /**
     * An unmodifiable set of package names that are shared with the parent class loader.<br/>
     * Shared packages allow classes in these packages to be loaded by the parent class loader
     * instead of the module's isolated classloader.
     */
    private final Set<String> sharedPackages;

    /**
     * An unmodifiable set of fully qualified class names that are exported and can be
     * instantiated by external code via {@link ClassFindRuntime.IsolatedModule#getInstance(String, Class)}.
     */
    private final Set<String> exportedClasses;

    private ModuleSpec(Builder builder) {
        this.name = builder.name;
        this.adapterJar = builder.adapterJar;
        this.dependencyJars = Collections.unmodifiableList(new ArrayList<>(builder.dependencyJars));
        this.sharedPackages = Collections.unmodifiableSet(new HashSet<>(builder.sharedPackages));
        this.exportedClasses = Collections.unmodifiableSet(new HashSet<>(builder.exportedClasses));
    }

    /**
     * Returns the name of the module.
     *
     * @return the module name
     */
    public String getName() { return name; }

    /**
     * Returns the path to the adapter JAR file.
     *
     * @return the adapter JAR path
     */
    public Path getAdapterJar() { return adapterJar; }

    /**
     * Returns an unmodifiable list of dependency JAR paths.
     *
     * @return the list of dependency JAR paths
     */
    public List<Path> getDependencyJars() { return dependencyJars; }

    /**
     * Returns an unmodifiable set of shared package names.
     *
     * @return the set of shared packages
     */
    public Set<String> getSharedPackages() { return sharedPackages; }

    /**
     * Returns an unmodifiable set of exported class names.
     *
     * @return the set of exported classes
     */
    public Set<String> getExportedClasses() { return exportedClasses; }

    /**
     * Creates a new {@link Builder} instance for constructing a {@link ModuleSpec}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    /**
     * A builder for constructing immutable {@link ModuleSpec} instances.
     */
    public static final class Builder {
        private String name;
        private Path adapterJar;
        private final List<Path> dependencyJars = new ArrayList<>();
        private final Set<String> sharedPackages = new HashSet<>();
        private final Set<String> exportedClasses = new HashSet<>();

        /**
         * Sets the name of the module.
         *
         * @param name the module name
         * @return this builder instance
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the path to the adapter JAR.
         *
         * @param adapterJar the adapter JAR path
         * @return this builder instance
         */
        public Builder adapterJar(Path adapterJar) {
            this.adapterJar = adapterJar;
            return this;
        }

        /**
         * Adds a dependency JAR path.
         *
         * @param dependencyJar the dependency JAR path
         * @return this builder instance
         */
        public Builder dependencyJar(Path dependencyJar) {
            this.dependencyJars.add(dependencyJar);
            return this;
        }

        /**
         * Adds one or more shared package names.
         *
         * @param packages the package names to share
         * @return this builder instance
         */
        public Builder sharedPackages(String... packages) {
            this.sharedPackages.addAll(Arrays.asList(packages));
            return this;
        }

        /**
         * Adds one or more exported class names.
         *
         * @param classes the fully qualified class names to export
         * @return this builder instance
         */
        public Builder exportedClasses(String... classes) {
            this.exportedClasses.addAll(Arrays.asList(classes));
            return this;
        }

        /**
         * Builds and returns an immutable {@link ModuleSpec} instance.
         *
         * @return the built module spec
         * @throws IllegalStateException if name, adapterJar, or dependencyJars are not set properly
         */
        public ModuleSpec build() {
            if (name == null || adapterJar == null || dependencyJars.isEmpty()) {
                throw new IllegalStateException("name, adapterJar, and at least one dependencyJar are required");
            }
            return new ModuleSpec(this);
        }
    }
}