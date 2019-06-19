#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
	
#START OF PROGRAM =============================================================================================================
inputFile = SRC_KBO_RESULT

#4 addresses contained bytes that could not be written to the csv file

f=open(inputFile,"rb")
data=f.read()
f.close()
#print(data) #here you can see special characters as byte sequences

data=data.replace(b'\xc2\x92',b'')
data=data.replace(b'\xc2\x9e',b'')
data=data.replace(b'\xc2\x96',b'')

f=open(inputFile,"wb")
f.write(data)
f.close()

createMunicipalityFiles(inputFile, ENCODING_CSV)


