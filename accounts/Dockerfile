
FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/accounts-*.jar app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "app.jar"]