#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *

def convertDicMapSToUpper(dic):
	result = {}
	for keyRM, dicNames in dic.items():
		if not keyRM in result:
			result[keyRM] = {}
		for name, idS in dicNames.items():
			nameWithoutAccents = removeAccents(name)
			nameUpper = nameWithoutAccents.upper()
			result[keyRM][nameUpper] = idS
	return result
	
#===PREPARE StreetCodeMappingFile FROM AAPD-FILE, dicS and fMapStreets===================================================================	
def treatStreetNameMatchFurtherAAPD(R, keyRM, S, bestMatch, pcS, NAMES, dicStringsNL, dicStringsFR):
	if R in ['F', 'B']:
		dicStrings = dicStringsNL
	else:
		dicStrings = dicStringsFR
		
	bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, S)
	if '-' in S:
		newS = S.replace('-',' ')
		bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, newS)
	for k,v in dicStrings.items():
		if k in S:
			newS = S.replace(k,v)
			bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, newS)
			if '-' in newS:
				newS = newS.replace('-',' ')
				bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, newS)
	if S[-1:] == 'L':	#cfr '..LAAN' is abbreviated by AAPD as '..L'
		newS = S + "AAN"
		bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, newS)
	if S[-2:] == 'DR':	#cfr '..DREEF' is abbreviated by AAPD as '..DR'
		newS = S + "EEF"
		bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, newS)
	if S[-2:] == 'BN':	#cfr '..BAAN' is abbreviated by AAPD as '..BN'
		newS = S[:-2] + "BAAN"
		bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, NAMES, newS)
	return bestMatch, pcS
	
def handleStreetAAPD(R, idM, idS_SRC, S, dicS, dicMapS, dicStringsNL, dicStringsFR): 
# match S (streetname from AAPD) with streetnames in dicMapS (from BEST)
# return a dictionary with the elements found from BEST
	#print("handleStreetAAPD", R, idM, S)
	dic = {}

	keyRM = R + idM
	
	stringToMatch = S

	if (not stringToMatch == "") and (keyRM in dicMapS):
		NAMES = dicMapS[keyRM].keys()
		bestMatch, pcS = matchIt(NAMES, stringToMatch)
		#print('matchIt', stringToMatch, bestMatch, dicMapS[keyRM][bestMatch], pcS)
		pcS2 = pcS #starting point
		if pcS != 1:
			bestMatch, pcS2 = treatStreetNameMatchFurtherAAPD(R, keyRM, stringToMatch, bestMatch, pcS, NAMES, dicStringsNL, dicStringsFR)
		if pcS2 > THRESHOLD_STREET:
			idS = dicMapS[keyRM][bestMatch]
			dic['idS'] = idS
			v = getLastVersion(dicS[R],idS)
			for lan in ['nl', 'fr', 'de']:
				key = 'S' + lan
				if lan in dicS[R][idS][v]:
					dic[key] = dicS[R][idS][v][lan]
				else:
					dic[key] = ""
			dic['pcS'] = pcS
			dic['pcS2'] = pcS2
	return dic

def getRegion2(dicM, idM):
	for region in ['B', 'F', 'W']:
		for id, dicV in dicM[region].items():
			if id == idM :
				return region
	return "region unknown"
	
def handleLineAAPD(result, idM, idS_SRC, S, dicM, dicS, dicMapS, dicStringsNL, dicStringsFR):
# with the data of 1 street, find the region and call the matching function to be applied on S 
# result = the new dictionary to be stored in fMapStreetCode_AAPDtoBEST (=mapping file so you can derive from the AAPD street id the BEST id)
	#print("handleLineAAPD", idM, S)
	R = getRegion2(dicM, idM)
	if R == "region unknown":
		print("ISSUE: cannot derive the region", idM, idS_SRC, S)
		return result
	else:
		if not R in result:
			result[R] = {}
		if not idM in result[R]:
			result[R][idM] = {}
		if not idS_SRC in result[R][idM]:
			#print(R, idM, idS_SRC)
			result[R][idM][idS_SRC] = handleStreetAAPD(R, idM, idS_SRC, S, dicS, dicMapS, dicStringsNL, dicStringsFR)
		return result
	
def createStreetCodeMappingAAPD(dicAAPDStreet, dicM, dicS, dicMapS, enc,dicStringsNL, dicStringsFR):
# pass through all streets in dicAAPDStreet 
# result = the new dictionary to be stored in fMapStreetCode_AAPDtoBEST (=mapping file so you can derive from the AAPD street id the BEST id)
	result = {}
	cnt = 1
	for idM, streets in dicAAPDStreet.items():
		print("Municipality", idM)
		for idS_SRC, names in streets.items():
			S = names['Snl']
			if S == "":
				print('ISSUE: empty Snl')
			else:
				result = handleLineAAPD(result, idM, idS_SRC, S, dicM, dicS, dicMapS, dicStringsNL, dicStringsFR)
			cnt += 1
			if cnt % 10000 == 0:
				print(cnt, idM, S)
	return result

#=========================================================================================================
start = datetime.datetime.now()

print('dicM..')
dicM = getDic(fDicMunicipalities)

print('dicS..')
dicS = getDic(fDicStreets)

print('dicMapS..')
dic = getDic(fMapStreetnames)
dicMapS = convertDicMapSToUpper(dic)

print("dicStreet from AAPD ..")
dicAAPDStreet = createStreetDicAAPD(SRC_AAPD_STREET_TABLE)
#print(dicAAPDStreet)

dicStringsNL = {'BURG.': 'BURGEMEESTER', 'DOM': 'DOMEIN', 'DR': 'DOKTER', 'DR.': 'DOKTER ', 'ESP': 'ESPLANADE', 'RES': 'RESIDENTIE', 'SQ': 'SQUARE', 'STR': 'STRAAT', 'STWG':'STEENWEG'}
dicStringsFR = {'AL': 'ALLEE', 'AV': 'AVENUE', 'BD': 'BOULEVARD', 'CH.': 'CHEMIN', 'CHEE': 'CHAUSSEE', 'CHEM': 'CHEMIN', 'CHP': 'CHAMP', 'CL': 'CLOS', 'DOM': 'DOMAINE', 'DR.': 'DOCTEUR ',\
                'DR': 'DREVE', 'ESP': 'ESPLANADE', 'IMP': 'IMPASSE', 'PAS': 'PASSAGE', 'PL': 'PLACE', 'R ': 'RUE ', 'RES': 'RESIDENCE', 'RTE': 'ROUTE', 'SENT': 'SENTIER', 'SQ': 'SQUARE'}

print('creating streetcode mapping file..')
dic = createStreetCodeMappingAAPD(dicAAPDStreet, dicM, dicS, dicMapS, PREFERRED_ENCODING, dicStringsNL, dicStringsFR)
saveDic(dic, fMapStreetCode_AAPDtoBEST)
end = datetime.datetime.now()
print("start: ", start)
print("end: ", end)
print("duration:", end-start)
