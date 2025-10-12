FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/blocker-service-*.jar app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]