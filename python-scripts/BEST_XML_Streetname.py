#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *				

lst = makeNationalListStreets()
saveLstOfDic(lst, fLstStreets)


dicM = getDicM(fDicMunicipalities)
dicS = makeDicStreets(dicM)
saveDic(dicS, fDicStreets)	

saveDicStreetsStatistics(dicS, fStatStreets)

outputStreets(dicS, fOutStreets) 


