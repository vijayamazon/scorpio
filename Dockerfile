FROM openjdk:8u191-jdk-alpine3.8

LABEL maintainer="jibo@outlook.com"

WORKDIR /root

COPY build/libs/ startup.sh /root/

ENTRYPOINT ["/bin/sh", "startup.sh"]
