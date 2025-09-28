FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/transfer-*.jar app.jar

EXPOSE 8007

ENTRYPOINT ["java", "-jar", "app.jar"]