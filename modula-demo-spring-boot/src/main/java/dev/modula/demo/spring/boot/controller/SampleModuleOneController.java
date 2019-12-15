package dev.modula.demo.spring.boot.controller;

import dev.modula.samplemodule.one.api.GreetingService;
import dev.modula.spring.boot.ModulaModuleManager;
import ch.qos.logback.classic.LoggerContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class SampleModuleOneController {
    private final ModulaModuleManager moduleManager;

    public SampleModuleOneController(ModulaModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @GetMapping("/samplemodule-one")
    public String greet() {

        // 获取当前类的 ClassLoader
        ClassLoader loader = getClass().getClassLoader();

        // 获取 logback 核心类的来源 JAR
        String logbackJar = getJarPath(LoggerContext.class);

        String resp = String.format(
                "The Spring Boot Application use【Logback 1.2.11】, load result: 【 Loader: %s | Form JAR: %s]】",
                loader.getClass().getSimpleName(),
                logbackJar
        );

        // 从名为 "samplemodule-one" 的模块中获取 GreetingServiceImpl 实例
        GreetingService service = moduleManager.getInstance(
                "samplemodule-one",
                "dev.modula.samplemodule.one.impl.GreetingServiceImpl",
                GreetingService.class
        );
        resp += "<br/>" + service.greet();
        return resp;
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