FROM litongjava/jdk:17.0.12-stable-slim

# 设置工作目录
WORKDIR /app

# 复制 jar 文件到容器中
COPY .env /app/.env
COPY target/study11-backend-1.0.0.jar /app/

# 运行 jar 文件
CMD ["java", "-jar", "study11-backend-1.0.0.jar", "--app.env=prod"]