# Modula

> **A lightweight Java class loading isolation runtime**

Run multiple versions of libraries or plugins in the same JVM **without class conflicts**.

## ✨ Quick Start

## 1. modula-demo-spring-boot

###1.1 Project Overview

`modula-demo-spring-boot` is a demonstration project that shows how to use the `Modula` framework to achieve class loading isolation. This project itself uses `logback 1.2.11` version, while the `samplemodule-one` module that needs to be run in isolation uses `logback 1.4.14` version.

### 1.2. Directory Structure

```
modula-demo-spring-boot/                         <<--d8y logback 1.2.11
└── src/main/resources/
    └── modules/                                 <<--Modules which this spring boot use
        ├── samplemodule-one/                    <<--module one
        │   ├── samplemodule-one-1.0.0.jar       <<--module one's main jar
        │   └── lib/                             <<--module one d8y logback 1.4.14
        │       ├── logback-core-1.4.14.jar
        │       ├── logback-classic-1.4.14.jar
        │       └── other-dependencies.jar
        └── samplemodule-two/     <--module one
            ├── samplemodule-two-1.0.0.jar
            └── lib/
                └── dependencies.jar
```




### 2. Prepare the sample module

Package the sample modules and copy it under the classpath of modula-demo-spring-boot.

```shell
#run shell script at Modula root path
./pkg_samplemodule_to_demo_spring_boot.sh
```

###3. Using the Modula Framework

#### 3.1. Add Dependencies

Add the following dependency to the [pom.xml]() file in the `modula-demo-spring-boot` project:

```xml
<dependency>
    <groupId>dev.modula</groupId>
    <artifactId>modula-spring-boot-starter</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```


#### 3.2. Configure Modula Modules

Configure Modula modules in `application.yml` or `application.properties`:

```yaml
  modula:
    modules:
      - name: "samplemodule-one"
        adapter-jar: "classpath:modules/samplemodule-one/samplemodule-one-impl-0.0.2-SNAPSHOT-adapter.jar"
        dependency-jars:
          - "classpath:modules/samplemodule-one/lib/logback-classic-1.4.14.jar"
          - "classpath:modules/samplemodule-one/lib/logback-core-1.4.14.jar"
          - "classpath:modules/samplemodule-one/lib/slf4j-api-2.0.16.jar"
        shared-packages:
          - "java"
          - "dev.modula.samplemodule.one.api"
        exported-classes:
          - "dev.modula.samplemodule.one.impl.GreetingServiceImpl"
```

#### 3.3. Using Modula in Code

```java
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
```

#### 3.4. Open Browser
Access：http://127.0.0.1:8080/samplemodule-one
Page Output：

```text
The Spring Boot Application use【Logback 1.2.11】, load result: 【 Loader: AppClassLoader | Form JAR: logback-classic-1.2.11.jar]】
The module samplemodule-one use【Logback 1.4.14】, load result: 【 Loader: ModulaClassLoader | Form JAR: logback-classic-1.4.14.jar]】
```


### Important Notes

1. **Module Build Order**: Make sure to run the build script first, then start the `modula-demo-spring-boot` application
2. **Version Compatibility**: Although class loading isolation is implemented, still pay attention to JDK version compatibility
3. **Resource Management**: Release unnecessary `ModuleInstance` in time to avoid memory leaks
4. **Error Handling**: Properly handle exceptions during module loading and execution