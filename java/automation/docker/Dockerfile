# syntax=docker/dockerfile:1

# start with downloading / converting data and compiling latest sources

FROM maven:3-openjdk-17-slim as builder
MAINTAINER bart.hanssens@bosa.fgov.be

ARG version=1.2.9

RUN apt-get update && \
  apt-get install -y wget git-core pwgen && \
  useradd -m best

USER best

RUN cd /home/best && \
  git clone https://github.com/fedict/best-tools && \
  cd /home/best/best-tools/java && \
  mvn clean install -DskipTests

# The actual docker with the API + pre-loaded PostGIS database

FROM gcr.io/distroless/java17-debian11
MAINTAINER bart.hanssens@bosa.fgov.be

COPY --from=0 /home/best/best-tools/java/automation/target/automation-*-runner.jar /automation.jar

USER nonroot

CMD ["/automation.jar"]