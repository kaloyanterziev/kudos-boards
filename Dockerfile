FROM openjdk:17-jdk-slim-buster
WORKDIR /app

COPY build/libs/kudos-boards-0.0.1-SNAPSHOT.jar build/

WORKDIR build
ENTRYPOINT java -jar kudos-boards-0.0.1-SNAPSHOT.jar