# Overview

Developed and tested with Java JDK 11, using Netbeans / Maven.
Build the parent project to create all the tools.

Building without tests

`mvn clean install -DskipTests`

# Testing

Webservice testing requires a docker instance and 20 GB of disk space

## Tools

- [Unzip tool](unzip): recursively unzip the BeST-zip file.
- [Converter tool](converter): convert XML files to CSV
- [Automation tool](automation) (internal use only)

## Helper modules

- [DAO](dao)
- [XmlReader](xmlreader)
