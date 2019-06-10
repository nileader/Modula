package dev.modula.demo;

import dev.modula.core.ModulaRuntime;
import dev.modula.core.ModuleSpec;
import dev.modula.demo.service.MessageFormatter;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DualLogbackDemo {
    public static void main(String[] args) {

        System.out.println("🚀 Modula Demo: Running TWO versions of Logback in ONE JVM!");
        System.out.println("   Goal: Prove classloader isolation works.");
        System.out.println("----------------------------------------------------------");


        Path modulesDir = Paths.get("modules").toAbsolutePath();

        ModulaRuntime runtime = new ModulaRuntime();

        // Load Logback 1.2.11 module
        ModulaRuntime.IsolatedModule v12Module = runtime.load(
                ModuleSpec.builder()
                        .name("d8y-logback-1.2.11")
                        .adapterJar(modulesDir.resolve("d8y-logback-1.2.11/adapter.jar"))
                        .dependencyJar(modulesDir.resolve("d8y-logback-1.2.11/lib/logback-classic-1.2.11.jar"))
                        .dependencyJar(modulesDir.resolve("d8y-logback-1.2.11/lib/logback-core-1.2.11.jar"))
                        .dependencyJar(modulesDir.resolve("d8y-logback-1.2.11/lib/slf4j-api-1.7.36.jar"))
                        .sharedPackages("java", "dev.modula.demo.service")
                        .exportedClasses("dev.modula.demo.impl.Logback1211Formatter")
                        .build()
        );

        // Load Logback 1.4.14 module
        ModulaRuntime.IsolatedModule v14Module = runtime.load(
                ModuleSpec.builder()
                        .name("d8y-logback-1.4.14")
                        .adapterJar(modulesDir.resolve("d8y-logback-1.4.14/adapter.jar"))
                        .dependencyJar(modulesDir.resolve("d8y-logback-1.4.14/lib/logback-classic-1.4.14.jar"))
                        .dependencyJar(modulesDir.resolve("d8y-logback-1.4.14/lib/logback-core-1.4.14.jar"))
                        .dependencyJar(modulesDir.resolve("d8y-logback-1.4.14/lib/slf4j-api-2.0.13.jar"))
                        .sharedPackages("java", "dev.modula.demo.service")
                        .exportedClasses("dev.modula.demo.impl.Logback1414Formatter")
                        .build()
        );

        MessageFormatter f1 = v12Module.getInstance("dev.modula.demo.impl.Logback1211Formatter", MessageFormatter.class);
        MessageFormatter f2 = v14Module.getInstance("dev.modula.demo.impl.Logback1414Formatter", MessageFormatter.class);

        System.out.println(f1.format("Hello from 1.2.11"));
        System.out.println(f2.format("Hello from 1.4.14"));

        System.out.println("----------------------------------------------------------");
        System.out.println("✅ SUCCESS: Both Logback versions coexist without conflict!");
        System.out.println("   This is ONLY possible because Modula isolates classloaders.");

    }
}