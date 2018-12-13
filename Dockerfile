FROM openjdk:8u181-jdk-alpine3.8

LABEL maintainer="jibo@outlook.com"

RUN apk add --no-cache apr curl git openssl tomcat-native

WORKDIR /root

COPY build/libs/ startup.sh /root/

ENTRYPOINT ["/bin/sh", "startup.sh"]
