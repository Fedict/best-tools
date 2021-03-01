# "Empty streets" converter tool

This tool is used to create a list of "missing" streets with an "educated guess" for the postal code.

Since postal codes in BeST are not available at the street level (the postal code is at the address / house number level), 
and because some streets don't have any buildings (parcs, rural roads, …), these streets won't show up in the normal list of streetnames per postal code.

The tool will "guess" the postal code based on the name of the municipality for these empty streets.
This is not perfect: quite a few streets will get the postal code of the "main" municipality (e.g. 2000 Antwerp while 2060 might be more appropriate)
