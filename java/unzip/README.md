#  Unzip tool

Recursively unzip the BeST-zip file.

The BeST ZIP [full download file](https://opendata.bosa.be/) (+/- 340 MB) contains another set of ZIP files, 
unzipping all of them requires +/- 15 GB disk space. 

The output directory will be created if it does not exist.


Usage:
`java -jar unzip.jar -i <best-latest.zip> -o <output_directory>`