FROM openjdk:17-jdk-slim

COPY target/backend-rest-externo-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

WORKDIR /app
EXPOSE 8180

CMD ["java", "-jar", "app.jar"]
