#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

src = "KBO" 
inFile = SRC_KBO_IN
outFile = SRC_KBO_RESULT

start = datetime.datetime.now()

print("mapping", src, inFile, outFile)
mapDb(src, inFile, outFile)

end = datetime.datetime.now()
print("start: ", start)
print("end: ", end)
print("duration:", end-start)
