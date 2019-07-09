// 文件路径: modula-spring-boot-starter/src/main/java/dev/modula/spring/boot/ModulaModuleManager.java

package dev.modula.spring.boot;

import dev.modula.core.ModulaRuntime;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ModulaModuleManager {

    private final Map<String, ModulaRuntime.IsolatedModule> modules;

    public ModulaModuleManager(Map<String, ModulaRuntime.IsolatedModule> modules) {
        this.modules = modules;
    }

    public <T> T getInstance(String moduleName, String className, Class<T> interfaceType) {
        ModulaRuntime.IsolatedModule module = modules.get(moduleName);
        if (module == null) {
            throw new IllegalArgumentException("Module not found: " + moduleName);
        }
        return module.getInstance(className, interfaceType);
    }
}