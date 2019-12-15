package dev.modula.demo.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 标准 Spring Boot 启动类，无特殊逻辑。模块加载由 ModulaAutoConfiguration 自动完成。
 */
@SpringBootApplication
public class ModulaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModulaDemoApplication.class, args);
    }
}
