# best-tools

Various BeST (Belgian Streets and Addresses) conversion tools

## Java 
### Unzip

Unzip the various zip files from nested ZIP

### Converter

Convert the BeST XML files to CSV and/or Shapefiles.
Requires Java 8 or 11 runtime and +/- 20 GB disk space, 8 GB RAM

Usage: java -Xmx2536m -jar converter -BWF -i indir -o outdir

## Postgres
### Standalone loader

Java based command line tool, loads XML data into a PostgreSQL (+ PostGIS) RDBMS

### Load.pl

Perl script to load CSV into a PostGIS database

## Python scripts

This folder contains an overview document, python scripts and a number of data files.
Useful for extracting information out of the BEST full download xml files.
Also contains scripts to map address data from various sources onto BEST addresses.  
