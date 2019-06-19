#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
#162 Walloon addresses cannot be written to a csv file (probably caused by BEST character oe eg BOEUF and by accents in streetnames)

#START OF PROGRAM =============================================================================================================
inputFile = SRC_RR_W_RESULT
createMunicipalityFiles(inputFile, ENCODING_CSV)

