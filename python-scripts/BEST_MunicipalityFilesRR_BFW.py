#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

#START OF PROGRAM =============================================================================================================
inputFile = SRC_RR_B_RESULT
createMunicipalityFiles(inputFile, ENCODING_RR)

inputFile = SRC_RR_F_RESULT
createMunicipalityFiles(inputFile, ENCODING_CSV)

inputFile = SRC_RR_W_RESULT
createMunicipalityFiles(inputFile, ENCODING_CSV)

