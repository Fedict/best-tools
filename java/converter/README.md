# Converter tool

After unzipping the BeST-file, this tool can be used to convert the XML to CSV-files
It is possible to select one or more regions: B(russels), F(landers) or W(alloon).

Usage:
`java -jar converter.jar -i <directory_unzipped_xml> -B -F -W`

Note that unzipping the file and converting the XMLs to CSV may take a few minutes, 
a minimum of 15 GB free disk space is recommended.

# Results

## Addresses file per Region

These files are also used by OpenAddresses.io

|Field|Description|
|----|----|
|EPSG:3812_x|x-coordinate in Lambert 2008 (0 if unknown)|
|EPSG:3812_y|y-coordinate in Lambert 2008 (0 if unknown)|
|EPSG:4326_lat|latitude in WGS84/GPS|
|EPSG:4326_lon|longitude in WGS84/GPS|
|address_id|ID of the address (only unique within the Region)|
|box_number|box number, if any|
|house_number|house number|
|municipality_id|ID of the municipality, unique NIS/INS-code|
|municipality_name_de|name of the municipality in German, if available|
|municipality_name_fr|name of the municipality in French, if available|
|municipality_name_nl|name of the municipality in Dutch, if available|
|postcode|postal code, as assigned by belgian bPost|
|postname_fr|name of postal code or sub-municipality in French (not available in Brussels)|
|postname_nl|name of postal code or sub-municipality in Dutch (not available in Brussels)|
|street_id|ID of the street (only unique within the Region)|
|streetname_de|name of the street in German, if available|
|streetname_fr|name of the street in French, if available|
|streetname_nl|name of the street in Dutch, if available|
|region_code|region code|
|status|status of the address|
|-----|----|

## Streets with at least one address, per postal code

Based on the postal code(s) of the addresses in a street.

## Streets without address, per postal code

Since in BeSt-address the postal codes are linked to an address, not to a street, 
it's not possible to be sure about the postal code if there are no addresses in this street
(e.g. park, country road ...).

Hence the postal codes are "best guesses", and may be incorrect.

