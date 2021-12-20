# DbLoader tool

Loads (unzipped) BeST XML files into a [PostGIS](https://postgis.net),
the user has to have admin rights to create the PostGIS extensions

Usage:
`java -jar dbloader.jar -x <directory_unzipped_xml> -d jdbc:postgresql://localhost/best?user=foo&password=bar`

Alternatively, the data can be written to a few CSV table files that can be loaded e.g. by Postgres' psql COPY command

Usage:
`java -jar dbloader.jar -x <directory_unzipped_xml> -c <directory_output_csv>`