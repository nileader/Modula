package dev.modula.samplemodule.one.impl;

import ch.qos.logback.classic.LoggerContext;
import dev.modula.samplemodule.one.api.GreetingService;

import java.io.File;

public class GreetingServiceImpl implements GreetingService {

    @Override
    public String greet() {

        // 获取当前类的 ClassLoader
        ClassLoader loader = getClass().getClassLoader();

        // 获取 logback 核心类的来源 JAR
        String logbackJar = getJarPath(LoggerContext.class);

        return String.format(
                "The module d8y must【Logback 1.2.11】, load result: 【 Loader: %s | Form JAR: %s]】",
                loader.getClass().getSimpleName(),
                logbackJar
        );
    }

    private String getJarPath(Class<?> clazz) {
        try {
            String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
            return new File(path).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

}
