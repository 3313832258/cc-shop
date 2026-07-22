# CC-Shop 后端服务 Dockerfile
# 多阶段构建，用于所有微服务

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY cc-common/pom.xml cc-common/
COPY cc-gateway/pom.xml cc-gateway/
COPY cc-user/pom.xml cc-user/
COPY cc-product/pom.xml cc-product/
COPY cc-trade/pom.xml cc-trade/
COPY cc-promotion/pom.xml cc-promotion/
COPY cc-shop-admin/pom.xml cc-shop-admin/

# 下载依赖
RUN mvn dependency:go-offline -B

COPY . .
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /app/cc-gateway/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]