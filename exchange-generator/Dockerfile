FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/exchange-generator-*.jar app.jar

EXPOSE 8005

ENTRYPOINT ["java", "-jar", "app.jar"]