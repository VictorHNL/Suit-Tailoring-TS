FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B package

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN mkdir -p /app/storage/media && chown -R 10001:10001 /app
COPY --from=build /workspace/target/backend-java-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
