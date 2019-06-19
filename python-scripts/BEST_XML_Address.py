#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
print('getDicMunicipalities..')			
dicM = getDic(fDicMunicipalities)
print('getDicStreets..')			
dicS = getDic(fDicStreets)
print('getDicPostalInfo..')			
dicP = getDic(fDicPostalInfo)
print('getDicPartOfMunicipalities..')			
dicPM = getDic(fDicPartOfMunicipalities)

print('makeNationalListAddresses..')
lst = makeNationalListAddresses(dicM, dicS, dicPM, dicP)               
print('saveLstOfDic..')
saveLstOfDic(lst, fLstAddresses)										

print('makeDicAddresses..')
dicA = makeDicAddresses(fLstAddresses)
print('saveDicA..')
saveDic(dicA, fDicAddresses)

print('saveDicAddressesStatistics..')
saveDicAddressesStatistics(dicA, fStatAddresses)

print('makeExtractFractionFromDic..')
makeExtractFractionFromDic(dicA, 100, fDicAddresses100) #test file

print('makeDicsFromDicA..')
makeDicsFromDicA(dicA, dicM, dicPM, dicP, fMToP, fMtoPM, fPtoM, fPtoPM, fPMtoM, fPMtoP) #links between Municipality, PartOfMunicipality, PostalInfo

#out of memory error
print('makeOutMunicipalitiesPartOfMunicipalities..')
makeOutMunicipalitiesPartOfMunicipalities(fOutMunicipalitiesPartOfMunicipalities) #link between Municipality and PartOfMunicipality (for Wallonia)