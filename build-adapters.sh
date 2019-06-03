#!/bin/bash
set -e

mvn clean
mvn compile

CORE_CP="modula-core/target/classes"
DEMO_CP="modula-demo/target/classes"
MAIN_CP="$CORE_CP:$DEMO_CP"

build_adapter() {
  local version=$1
  local module_dir="modules/d8y-logback-$version"
  local src_dir="modules-src/d8y-logback-$version"
  local build_dir="/tmp/build-$version"

  echo "Building adapter for logback $version..."

  # 清理
  rm -rf "$build_dir"
  mkdir -p "$build_dir"

  # 编译（关键：加入 lib/*）
  javac -cp "$MAIN_CP:$module_dir/lib/*" \
        -d "$build_dir" \
        "$src_dir/dev/modula/demo/impl/Logback${version//./}Formatter.java"

  # 打包
  jar cf "$module_dir/adapter.jar" -C "$build_dir" .

  echo "✅ Built: $module_dir/adapter.jar"
}

# 构建两个版本
build_adapter "1.2.11"
build_adapter "1.4.14"