#!/bin/bash

# 博客系统生产环境启动脚本
# 使用方法: ./start-prod.sh

# 检查是否提供了环境变量文件路径
ENV_FILE="${1:-.env}"

if [ ! -f "$ENV_FILE" ]; then
    echo "错误: 环境变量文件 $ENV_FILE 不存在"
    echo "使用方法: ./start-prod.sh [环境变量文件路径]"
    echo "请参考 .env.example 创建环境变量文件"
    exit 1
fi

# 加载环境变量
echo "正在加载环境变量文件: $ENV_FILE"
export $(cat $ENV_FILE | grep -v '^#' | xargs)

# 检查必需的环境变量
if [ -z "$DATABASE_PASSWORD" ]; then
    echo "错误: DATABASE_PASSWORD 未设置"
    exit 1
fi

if [ -z "$JWT_SECRET" ]; then
    echo "错误: JWT_SECRET 未设置"
    exit 1
fi

# 检查 JAR 文件是否存在
JAR_FILE="target/blog-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: JAR 文件不存在: $JAR_FILE"
    echo "请先运行: ./mvnw clean package"
    exit 1
fi

# 启动应用
echo "正在启动应用..."
java -jar -Dspring.profiles.active=prod $JAR_FILE
