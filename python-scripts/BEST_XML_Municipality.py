#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *				

lst = makeNationalListMunicipalities()
saveLstOfDic(lst, fLstMunicipalities)

dicM = makeDicMunicipalities(fLstMunicipalities)
saveDic(dicM, fDicMunicipalities)

outputMunicipalities(dicM, fOutMunicipalities)

saveDicMunicipalitiesStatistics(dicM, fStatMunicipalities)


