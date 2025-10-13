FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/exchange-service-*.jar app.jar

EXPOSE 8004

ENTRYPOINT ["java", "-jar", "app.jar"]