# syntax=docker/dockerfile:1
FROM maven:3-openjdk-17-slim as builder
MAINTAINER bart.hanssens@bosa.fgov.be

ARG version=1.2.8

RUN apt-get update && \
  apt-get install -y wget git-core pwgen && \
  useradd -m best

USER best
RUN cd /home/best && \
  git clone https://github.com/fedict/best-tools && \
  cd /home/best/best-tools/java && \
  mvn clean install && \
  wget -q https://opendata.bosa.be/download/best/best-full-latest.zip -O /tmp/best.zip && \
  mkdir -p /tmp/best/data/xml && mkdir -p /tmp/best/data/csv && \ 
  java -jar unzip/target/unzip-${version}.jar -i /tmp/best.zip -o /tmp/best/data/xml && \
  java -Dorg.slf4j.simpleLogger.defaultLogLevel=WARN \
	 -jar dbloader/target/dbloader-${version}.jar -x /tmp/best/data/xml -c /tmp/best/data/csv


FROM debian:11-slim 
MAINTAINER bart.hanssens@bosa.fgov.be

ARG version=1.2.8

ENV LANGUAGE=en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8

ENV PATH=/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/local/sbin:/usr/lib/postgresql/14/bin:/usr/lib/x86_64-linux-gnu

RUN useradd -m best && \
  apt-get update && \ 
  apt-get install -y locales locales-all ca-certificates && \ 
  echo "deb https://httpredir.debian.org/debian sid main" >> /etc/apt/sources.list && \
  apt-get update && \
  apt-get -t sid install -y --no-install-recommends  \
	dumb-init \
	postgresql-14 \
	postgresql-14-postgis-3 postgresql-14-postgis-3-scripts \
 	openjdk-17-jre-headless && \
  apt-get clean && rm -rf /var/lib/apt/* && \
  chown postgres /tmp/*

COPY --from=0 /home/best/best-tools/java/webservice/target/webservice-${version}-runner.jar /home/best/runner.jar
COPY --from=0 --chown=postgres:postgres /tmp/best/data/csv/*.csv /tmp/
COPY postgis/postgresql.conf /etc/postgresql/14/main/postgresql.conf
COPY --chown=postgres:postgres postgis/postgis.sql /tmp/

ENV PGDATA=/var/lib/postgresql/14/main

USER postgres

RUN service postgresql start && \
  cd /tmp && \ 
  psql -f /tmp/postgis.sql && \
  service postgresql stop && \
  rm /tmp/*.csv && rm /tmp/*.sql
STOPSIGNAL SIGINT

WORKDIR /home/best

EXPOSE 8080 

VOLUME /var/lib/postgresql/14/main

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["bash", "-c", "pg_ctl start -o '-c config_file=/etc/postgresql/14/main/postgresql.conf' && java -Djava.net.preferIPv4Stack=true -Xmx2G -jar runner.jar"]
