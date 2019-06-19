#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *				
lst = makeNationalListPartOfMunicipalities()
saveLstOfDic(lst, fLstPartOfMunicipalities)

dicPM = makeDicPartOfMunicipalities(fLstPartOfMunicipalities)
saveDic(dicPM, fDicPartOfMunicipalities)

outputPartOfMunicipalities(dicPM, fOutPartOfMunicipalities)

saveDicPartOfMunicipalitiesStatistics(dicPM, fStatPartOfMunicipalities)


