#!/bin/bash
# 该脚本需要在 Modula 项目根目录下运行

echo "开始构建 samplemodule-one 模块..."

# 构建整个项目
mvn clean install -DskipTests

# 进入 samplemodule-one 目录并构建
mvn clean package -f modula-samplemodule/samplemodule-one/pom.xml

# 清理目标目录中的旧构建文件
echo "清理 modula-demo-spring-boot 中的旧模块文件..."
rm -rf modula-demo-spring-boot/src/main/resources/modules/samplemodule-one

# 创建新的目录结构
mkdir -p modula-demo-spring-boot/src/main/resources/modules/samplemodule-one/lib

# 复制主模块 JAR 文件到模块根目录
cp modula-samplemodule/samplemodule-one/samplemodule-one-impl/target/samplemodule-one-*.jar modula-demo-spring-boot/src/main/resources/modules/samplemodule-one/

# 复制依赖库到 lib 目录
cp modula-samplemodule/samplemodule-one/samplemodule-one-impl/target/lib/*.jar modula-demo-spring-boot/src/main/resources/modules/samplemodule-one/lib

echo "samplemodule-one 模块构建完成！"
echo "文件已放置到: modula-demo-spring-boot/src/main/resources/modules/samplemodule-one/"
echo "目录结构:"
echo "  modules/samplemodule-one/"
echo "  ├── samplemodule-one-*.jar (主JAR)"
echo "  └── lib/ (依赖JAR目录)"
echo "      ├── dependency1.jar"
echo "      ├── dependency2.jar"
echo "      └── ..."
