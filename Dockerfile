FROM openjdk:11-jdk-slim

WORKDIR /app

ADD target/fairdatapoint-index.jar /app/app.jar

ENTRYPOINT java -jar app.jar
