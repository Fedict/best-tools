# Overview

## Building the java tools

Developed and tested with Java JDK8, using Netbeans / Maven: build the parent pom to create the unzip and convert tools.

Note that unzipping the file and converting the XMLs to CSV/SHP may take a few minutes, and that a minimum of 15 GB free disk space is recommended.


## Unzip tool

Recursively unzip the BeST-zip file: the big ZIP file contains another set of ZIP files. The output directory must already exist.


Usage:
`java -jar unzip.jar -i <best-latest.zip> -o <output_directory>`

## Converter tool

After unzipping the BeST-file, this tool can be used to convert the XML to CSV-files or SHP.
It is possible to select one or more regions: B(russels), F(landers) or W(alloon).

Usage:
`java -jar converter.jar -i <directory_unzipped_xml> -B -F -W`

## Empty streets tool

After unzipping the BeST-file, this tool can be used to get the list of streets without buildings (parcs, rural roads ...) as CSV.
It is possible to select one or more regions: B(russels), F(landers) or W(alloon).

Usage:
`java -jar emptystreets.jar -i <directory_unzipped_xml> -B -F -W`


## DAO

Helper module containing data objects to be used in other projects

## XmlReader

Helper module containing readers to load XML files into DAO

## DbLoader

Loads (unzipped) BeST XML files into a [PostGIS](https://postgis.net) or [SpatiaLite](https://www.gaia-gis.it/fossil/libspatialite/home) RDBMS, optionally converting Lambert72 coordinates to WGS84/GPS

For PostGIS, the user has to have admin rights to create the PostGIS extensions

Usage:
`java -jar dbloader.jar -x <directory_unzipped_xml> -d jdbc:postgresql://localhost/best?user=foo&password=bar`

For spatialite, the `mod_spatialite` .dll or .so must be in the `PATH` environment variable.
In addition, when converting Lambert coordinates to GPS coordinates, the `PROJ_LIB` environment variable needs to be set to the directory containing proj.db database file, which is part of the precompiled [Windows executables](http://www.gaia-gis.it/gaia-sins/).

Usage:
`java -jar dbloader.jar -x <directory_unzipped_xml> -d jdbc:sqlite:C:/data/best.db -g`
