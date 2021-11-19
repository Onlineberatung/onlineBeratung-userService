FROM --platform=linux/arm64/v8 ubuntu:20.04

ARG DEBIAN_FRONTEND=noninteractive
EXPOSE 8080

RUN apt update \
    && apt upgrade -y \
    && apt install -y openjdk-11-jre git \
    && apt clean

RUN mkdir -pv /app && cd /app && \
    git clone https://github.com/spring-guides/gs-spring-boot.git && \
    cd /app/gs-spring-boot/initial && ./gradlew build

WORKDIR /app/gs-spring-boot/initial

VOLUME ["/tmp","/log"]
ARG JAR_FILE
COPY ./UserService.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
