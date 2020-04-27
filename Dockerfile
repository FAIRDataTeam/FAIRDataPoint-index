FROM openjdk:11-jdk-slim

WORKDIR /app

ADD target/fair-metadata-index-0.1.0-SNAPSHOT.jar /app/app.jar

ENTRYPOINT java -jar app.jar
