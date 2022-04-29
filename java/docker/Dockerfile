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
  mvn clean install -DskipTests && \
  wget -q https://opendata.bosa.be/download/best/best-full-latest.zip -O /tmp/best.zip && \
  mkdir -p /tmp/best/data/xml && mkdir -p /tmp/best/data/csv && \ 
  java -jar unzip/target/unzip-${version}.jar -i /tmp/best.zip -o /tmp/best/data/xml && \
  java -Djava.util.logging.level=WARNING \
	 -jar dbloader/target/dbloader-${version}.jar -x /tmp/best/data/xml -c /tmp/best/data/csv

# The actual docker with the API + pre-loaded PostGIS database

FROM debian:11-slim 
MAINTAINER bart.hanssens@bosa.fgov.be

ARG version=1.2.9

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
 	openjdk-17-jre-headless \
	pwgen && \
  apt-get clean && rm -rf /var/lib/apt/* && \
  chown postgres /tmp/*

COPY --from=0 /home/best/best-tools/java/webservice/target/webservice-${version}-runner.jar /home/best/runner.jar
COPY --from=0 --chown=postgres:postgres /tmp/best/data/csv/*.csv /tmp/
COPY --from=0 /home/best/best-tools/java/docker/postgis/postgresql.conf /etc/postgresql/14/main/postgresql.conf
COPY --from=0 /home/best/best-tools/java/docker/postgis/pg_hba.conf /etc/postgresql/14/main/pg_hba.conf

COPY --from=0 --chown=postgres:postgres /home/best/best-tools/java/docker/postgis/postgis.sql /tmp/

ENV PGDATA=/var/lib/postgresql/14/main

USER postgres

# Load data into Postgresql database and change default password for postgres user

RUN service postgresql start && \
  cd /tmp && \ 
  psql --set=apiversion="${version}" -f /tmp/postgis.sql && \
  psql -c "ALTER USER postgres PASSWORD '$(pwgen -s 16 1)';" && \
  service postgresql stop && \
  rm /tmp/*.csv && rm /tmp/*.sql
STOPSIGNAL SIGINT

# Copy the more restrictive access configuration

WORKDIR /home/best

EXPOSE 8080 

#VOLUME /var/lib/postgresql/14/main

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["bash", "-c", "pg_ctl start -o '-c config_file=/etc/postgresql/14/main/postgresql.conf' && java -Djava.net.preferIPv4Stack=true -Xmx2G -jar runner.jar"]
