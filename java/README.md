# Overview

## Building the java tools

Developed and tested with Java JDK8, using Netbeans / Maven: build the parent pom to create the unzip and convert tools.

Note that unzipping the file and converting the XMLs to CSV/SHP may take a few minutes, and that a minimum of 15 GB free disk space is recommended.


## Unzip

Recursively unzip the BeST-zip file: the big ZIP file contains another set of ZIP files. The output directory must already exist.


Usage:
`java -jar unzip.jar -i <best-latest.zip> -o <output_directory>`

## Convert

After unzipping the BeST-file, this tool can be used to convert the XML to CSV-files or SHP.
It is possible to select one or more regions: B(russels), F(landers) or W(alloon).

Usage:
`java -jar converter.jar -i <directory_unzipped_xml> -B -F -W`