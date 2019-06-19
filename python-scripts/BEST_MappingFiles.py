#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

print('dicS..')
dicS = getDic(fDicStreets)


print('dicMapS..')
dicMapS = createMappingFileStreets(dicS)
print('saveDic(dicMapS, fMapStreetnames)..')
saveDic(dicMapS, fMapStreetnames)

print('dicMapS_RR..')
dicMapStreetsRR = convertStreetsRR(dicMapS)
print('saveDic(dicMapStreetsRR, fMapStreetnamesRR)..')
saveDic(dicMapStreetsRR, fMapStreetnamesRR)

print('dicA..')
dicA = getDic(fDicAddresses)

print('dicMapA..')
dicMapA = createMappingFileNumbers(dicA)
print('saveDic(dicMapA, fMapAddresses)..')
saveDic(dicMapA, fMapAddresses)


print('dicMapHs..')
isForRR = False
dicMapHs = createMappingFileHouseNrs(dicMapA, isForRR)
print('saveDic(dicMapHs, fMapHouseNrs)..')
saveDic(dicMapHs, fMapHouseNrs)

print('dicMapHs for RR ..')
isForRR = True
dicMapHs = createMappingFileHouseNrs(dicMapA, isForRR)
print('saveDic(dicMapHs, fMapHouseNrsRR)..')
saveDic(dicMapHs, fMapHouseNrsRR)

print('dicMapBx..')
isForRR = False
dicMapBx = createMappingFileBoxNrs(dicMapA, isForRR)
print('saveDic(dicMapBx, fMapBoxNrs)..')
saveDic(dicMapBx, fMapBoxNrs)

print('dicMapBx for RR ..')
isForRR = True
dicMapBx = createMappingFileBoxNrs(dicMapA, isForRR)
print('saveDic(dicMapBx, fMapBoxNrsRR)..')
saveDic(dicMapBx, fMapBoxNrsRR)

print('dicM..')
dicM = getDic(fDicMunicipalities)

print('dicMapMunToR..')
dicMapMunToR = createMappingFileMunToR(dicM)
print('saveDic(dicMapMunToR, fMapMunToR)..')
saveDic(dicMapMunToR, fMapMunToR)
	
print('creating streetcode mapping file..')
dic = createStreetCodeMappingFile(dicS, dicMapStreetsRR)
saveDic(dic, fMapStreetCode_RRtoBEST)