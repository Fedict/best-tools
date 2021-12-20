# Webservice

Front-end for a PostGIS RDBMS containing BeST-address data.

## Running the unit tests with a local or remote docker

The unit tests for the webservice loads a subset of address data into a dockerized PostGIS,
using [TestContainers](https://testcontainers.org).

If a local docker engine is not available,
a [remote docker](https://docs.docker.com/engine/install/linux-postinstall/#configuring-remote-access-with-systemd-unit-file) 
can be used, though this requires additional configuration to 
[secure the docker engine](https://docs.docker.com/engine/security/protect-access/#use-tls-https-to-protect-the-docker-daemon-socket)

The connection details can be specified in a `.testcontainers.properties` file.
On MS-Windows this file should be put in `C:\Users\%USERNAME%` 

Example:
```
docker.client.strategy=org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy
docker.host=tcp\://example.com\:2376
docker.tls.verify=1
docker.cert.path=C\:\\Data\\docker
```

## Building stand-alone docker image

A stand-alone webservice containing both a REST API and a database with the data can be built using
the multi-stage Dockerfile

This (obviously) a docker engine and about 20 GB of disk space.

### First stage

The multi-stage Dockerfile generates a docker with JDK and other development tools.
This requires about 16 GB of disk space, but is only used to build the second stage
and is not required to run the webservice

`docker build -f Dockerfile .`

In brief:
- the latest version of the source code is pulled from git repository
- tools and the webservice front-end (fat-jar) are built from this source
- the latest ZIP with XML files is downloaded from https://opendata.bosa.be
- unzip tool is used to unzip the file
- dbloader tool is used to create CSV suited for PostGIS

### Second stage

The CSV files are loaded into a PostGIS database, and a JRE + the webservice front-end is added.
The results is a docker image of about 3.5 GB (uncompressed, less than 1 GB compressed)

## Running the docker image

12-16 GB RAM, 4-12 vCores and a 100-1000 Mbps connection are recommended.

```
docker run -p8080:8080 \ 
   --env quarkus.log.file.path=/tmp/quarkus.log \
   --env quarkus.swagger-ui.always-include=true  \
   --env be.bosa.dt.best.webservice.url=http://your-server.example.com:8080 \
   -d  your-tagged-image
```

The `be.bosa.dt.best.webservice.url` is the base URL of your server

The API will be available on `your-server` port 8080 , 
the Swagger/OpenAPI interface on `http://your-server.example.com:8080/q/swagger-ui`