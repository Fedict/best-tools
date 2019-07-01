# Open Summer of Code 2019 Deliverables

* Format Converters
* Various Tools
* Dev Communication

## Intro

BeST - Belgian Street and Addresses: the full list of all addresses in Belgium + their geographical coordinates.
Some python scripts and java tools can be found in this repo.

## Format Converters

Currently the addresses are available in (fairly large) XML files, but organisations often prefer other file formats.
[Download, +/- 250 MB zip](https://opendata.bosa.be/index.nl.html)

Required, conversions to

* CSV
* GeoPackage

Nice to have:

* GeoJSON
* Shapefile
* KML
* OpenStreetMap file

## Various Tools

Required

* Matcher tool, given a CSV file, try to match the addresses in this file with the official BeST addresses.

Nice to have:

* Tool to get a list of similar streetnames in two different cities.
When municipalities merge, they have to be sure that the new municipality does not contain duplicate street names (e.g. Church Street is quite common).
Either exact same, or similar (e.g. Church Lane / Churh Street, Churchill Street...)

## Dev Communication

Required:

* CSV
* GeoPackage
