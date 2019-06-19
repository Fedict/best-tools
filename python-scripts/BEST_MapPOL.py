#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

src = "POL" 
inFile = SRC_POL_IN
outFile = SRC_POL_RESULT

start = datetime.datetime.now()

print("mapping", src, inFile, outFile)
mapDb(src, inFile, outFile)

end = datetime.datetime.now()
print("start: ", start)
print("end: ", end)
print("duration:", end-start)
