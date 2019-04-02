FROM openjdk:8u201-jdk-alpine3.9

LABEL maintainer="jibo@outlook.com"

WORKDIR /root

COPY build/libs/ startup.sh /root/

ENTRYPOINT ["/bin/sh", "startup.sh"]
