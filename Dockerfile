FROM cgr.dev/chainguard-private/jdk-fips:openjdk-21-dev
USER root
RUN apk add maven curl
USER java
WORKDIR /app
COPY pom.xml /app/
COPY src /app/src
RUN mvn clean install
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/app/target/spring-private-mysql-0.0.1-SNAPSHOT.jar"]
