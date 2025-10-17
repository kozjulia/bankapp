FROM openjdk:21-jdk

WORKDIR /app

COPY ./build/libs/front-*.jar app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]