FROM openjdk:11-jdk-slim

WORKDIR /app

ADD target/fairdatapoint-index.jar /app/app.jar
ADD target/classes/application.yml /app/application.yml

ENTRYPOINT java -jar app.jar --spring.config.location=classpath:/application.yml,file:/app/application.yml
