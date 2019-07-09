// 文件路径: modula-spring-boot-starter/src/main/java/dev/modula/spring/boot/ModulaProperties.java

package dev.modula.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "modula")
public class ModulaProperties {

    private List<ModuleDef> modules = new ArrayList<>();

    public List<ModuleDef> getModules() {
        return modules;
    }

    public void setModules(List<ModuleDef> modules) {
        this.modules = modules;
    }

    public static class ModuleDef {
        private String name;
        private Path adapterJar;
        private List<Path> dependencyJars = new ArrayList<>();
        private Set<String> sharedPackages = new HashSet<>();
        private Set<String> exportedClasses = new HashSet<>();

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Path getAdapterJar() { return adapterJar; }
        public void setAdapterJar(Path adapterJar) { this.adapterJar = adapterJar; }

        public List<Path> getDependencyJars() { return dependencyJars; }
        public void setDependencyJars(List<Path> dependencyJars) { this.dependencyJars = dependencyJars; }

        public Set<String> getSharedPackages() { return sharedPackages; }
        public void setSharedPackages(Set<String> sharedPackages) { this.sharedPackages = sharedPackages; }

        public Set<String> getExportedClasses() { return exportedClasses; }
        public void setExportedClasses(Set<String> exportedClasses) { this.exportedClasses = exportedClasses; }
    }
}