// 文件路径: modula-spring-boot-starter/src/main/java/dev/modula/spring/boot/ModulaAutoConfiguration.java

package dev.modula.spring.boot;

import dev.modula.core.ModuleSpec;
import dev.modula.core.ModulaRuntime;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConditionalOnClass(ModulaRuntime.class)
@EnableConfigurationProperties(ModulaProperties.class)
public class ModulaAutoConfiguration {

    private final Map<String, ModulaRuntime.IsolatedModule> moduleRegistry = new ConcurrentHashMap<>();

    public ModulaAutoConfiguration(ModulaProperties properties) {
        ModulaRuntime runtime = new ModulaRuntime();
        for (ModulaProperties.ModuleDef def : properties.getModules()) {
            ModuleSpec.Builder builder = ModuleSpec.builder()
                    .name(def.getName())
                    .adapterJar(def.getAdapterJar())
                    .sharedPackages(def.getSharedPackages().toArray(new String[0]))
                    .exportedClasses(def.getExportedClasses().toArray(new String[0]));

            for (Path dep : def.getDependencyJars()) {
                builder.dependencyJar(dep);
            }

            ModuleSpec spec = builder.build();
            ModulaRuntime.IsolatedModule module = runtime.load(spec);
            moduleRegistry.put(def.getName(), module);
        }
    }

    @Bean
    public ModulaModuleManager modulaModuleManager() {
        return new ModulaModuleManager(moduleRegistry);
    }
}