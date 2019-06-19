#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *				

lst = makeNationalListPostalInfo()
saveLstOfDic(lst, fLstPostalInfo)

dicP = makeDicPostalInfo(fLstPostalInfo)
saveDic(dicP, fDicPostalInfo)

saveDicPostalInfoStatistics(dicP, fStatPostalInfo)

outputPostalInfo(dicP, fOutPostalInfo)
outputPostcodes(dicP, fOutPostcodes)

