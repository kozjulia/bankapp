FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/notifications-*.jar app.jar

EXPOSE 8006

ENTRYPOINT ["java", "-jar", "app.jar"]