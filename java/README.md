# Overview

Developed and tested with Java JDK 11, using Netbeans / Maven.
Build the parent project to create all the tools.

Building without tests

`mvn clean install -DskipTests`

# Testing


## Tools

- [Unzip tool](unzip/README.md): recursively unzip the BeST-zip file.
- [Converter tool](converter/README.md): convert XML files to CSV
- [DBLoader](dbloader/README.md): loads (unzipped) prepare XML files for database
- [Automation tool](automation/README.md) (internal use only)

## Helper modules

- [DAO](dao/README.md)
- [XmlReader](xmlreader/README.md)

## Dockerized service

- [Webservice](webservice/README.md): REST front-end