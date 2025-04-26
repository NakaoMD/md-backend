# Etapa 1: build da aplicação
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagem para rodar a aplicação
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# ✅ Corrigido para interpolar variáveis de ambiente no runtime
ENTRYPOINT ["sh", "-c", "exec java -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar app.jar"]
