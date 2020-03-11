package dev.modula.samplemodule.two.impl;

import ch.qos.logback.classic.LoggerContext;
import dev.modula.samplemodule.two.api.TalkingService;

import java.io.File;

public class TalkingServiceImpl implements TalkingService {

    @Override
    public String talk() {

        // 获取当前类的 ClassLoader
        ClassLoader loader = getClass().getClassLoader();

        // 获取 logback 核心类的来源 JAR
        String logbackJar = getJarPath(LoggerContext.class);
        return String.format(
                "The Module samplemodule-two use【Logback 1.5.0】, load result: 【 Loader: %s | Form JAR: %s]】",
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
