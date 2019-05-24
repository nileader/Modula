package dev.modula.demo.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.LoggingEvent;
import dev.modula.demo.service.MessageFormatter;
import java.io.File;

public class Logback1414Formatter implements MessageFormatter {

    @Override
    public String format(String message) {
        // 获取当前类的 ClassLoader
        ClassLoader loader = getClass().getClassLoader();

        // 获取 logback 核心类的来源 JAR
        String logbackJar = getJarPath(ch.qos.logback.classic.LoggerContext.class);

        return String.format(
                "[✅ Logback 1.4.14 | Loader: %s | Form JAR: %s] %s",
                loader.getClass().getSimpleName(),
                logbackJar,
                message
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