FROM openjdk:17
RUN apt-get update && apt-get install -y curl
ARG JAR_FILE=api/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]