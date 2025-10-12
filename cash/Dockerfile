
FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/cash-*.jar app.jar

EXPOSE 8003

ENTRYPOINT ["java", "-jar", "app.jar"]