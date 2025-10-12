FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/gateway-*.jar app.jar

EXPOSE 8010

ENTRYPOINT ["java", "-jar", "app.jar"]