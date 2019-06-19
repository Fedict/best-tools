#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *

#===PREPARE StreetCodeMappingFile FROM POL-FILE, dicS and fMapStreets===================================================================	
def treatStreetNameMatchFurtherPOL(R, keyRM, S, bestMatch, pcS, dicMapStreetsRR):
	dicStringsNl = {'Stwg': 'Steenweg', 'Stwg.': 'Steenweg', 'St':'Sint', 'St. ':'Sint', 'Kon':'Koning', 'Kon.':'Koning', 'Burg':'Burgemeester', 'Burg.':'Burgemeester ', 'Luit':'Luitenant', 'Luit.':'Luitenant', 'Dr':'Dokter'}
	dicStringsFr = {'Av':'Avenue', 'Ch':'Chemin', 'Dr':'Docteur', 'Dr.':'Docteur', 'Res':'Residence', 'Res.':'Residence', 'Res,':'Residence', 'Bld':'Boulevard', 'St ':'Saint', 'St':'Saint', 'St-':'Saint', 'Ste':'Sainte' \
	                 , 'Ste ':'Sainte', 'Ste-':'Sainte', 'Lt.':'Lieutenant', 'Sq.': 'Square', 'Pl.':'Place'}
	bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, dicMapStreetsRR[keyRM].keys(), S)
	newS = S.replace('-',' ')
	bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, dicMapStreetsRR[keyRM].keys(), newS)
	if R in ['F', 'B']:
		dicStrings = dicStringsNl
	else:
		dicStrings = dicStringsFr
	for k,v in dicStrings.items():
		if k in S:
			newS = S.replace(k,v)
			bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, dicMapStreetsRR[keyRM].keys(), newS)
			newS = newS.replace('-',' ')
			bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, dicMapStreetsRR[keyRM].keys(), newS)
	for k,v in dicStrings.items():
		if k.upper() in S:
			newS = S.replace(k,v.upper())
			bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, dicMapStreetsRR[keyRM].keys(), newS)
			newS = newS.replace('-',' ')
			bestMatch, pcS = transformStreetNameAndMap(bestMatch, pcS, dicMapStreetsRR[keyRM].keys(), newS)
	return bestMatch, pcS
	
def handleStreetPOL(R, idM_SRC, idS_SRC, S, Snl, Sfr, Sde, dicS, dicMapS): 
	dic = {}

	keyRM = R + idM_SRC
	
	stringToMatch = ""
	if (R == 'F') and (Snl != ""):
		stringToMatch = Snl
	elif (R == 'W') and (Sfr != ""):
		stringToMatch = Sfr
	elif (R == 'B') and (Snl != ""):
		stringToMatch = Snl
	elif (R == 'B') and (Sfr != ""):
		stringToMatch = Sfr
	elif S != "":	
		stringToMatch = S
	elif Snl != "":	
		stringToMatch = Snl
	elif Sfr != "":	
		stringToMatch = Sfr
	elif Sde != "":	
		stringToMatch = Sde

	if (not stringToMatch == "") and (keyRM in dicMapS):
		bestMatch, pcS = matchIt(dicMapS[keyRM].keys(), stringToMatch)
		#print('matchIt', stringToMatch, bestMatch, dicMapS[keyRM][bestMatch], pcS)
		pcS2 = pcS #starting point
		if pcS != 1:
			bestMatch, pcS2 = treatStreetNameMatchFurtherPOL(R, keyRM, stringToMatch, bestMatch, pcS, dicMapS)
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
	
def handleLinePOL(result, dicLine, dicM, dicS, dicMapS):
	M = dicLine['M']
	idM_SRC = dicLine['idM_SRC']
	Mnl = dicLine['Mnl']
	Mfr = dicLine['Mfr']
	Mde = dicLine['Mde']
	M = dicLine['M']
	R = "region unknown"
	if M != "":
		R = getRegion(dicM, idM_SRC, M)
	if R == "region unknown" and Mnl != "":
		R = getRegion(dicM, idM_SRC, Mnl)
	if R == "region unknown" and Mfr != "":
		R = getRegion(dicM, idM_SRC, Mfr)
	if R == "region unknown" and Mde != "":
		R = getRegion(dicM, idM_SRC, Mde)
	if R == "region unknown":
		print("ISSUE: cannot derive the region", dicLine)
		return result
	else:
		if not R in result:
			result[R] = {}
		if not idM_SRC in result[R]:
			result[R][idM_SRC] = {}
		idS_SRC = dicLine['idS_SRC']
		S = dicLine['S']
		Snl = dicLine['Snl']
		Sfr = dicLine['Sfr']
		Sde = dicLine['Sde']
		if not idS_SRC in result[R][idM_SRC]:
			result[R][idM_SRC][idS_SRC] = handleStreetPOL(R, idM_SRC, idS_SRC, S, Snl, Sfr, Sde, dicS, dicMapS)
		return result
	
def createStreetCodeMappingPOL(inputFile, dicM, dicS, dicMapS, enc):
	result = {}
	
	fileIn = open(inputFile,"r", encoding=enc)
	# line = fileIn.readline() #headerline
	line = fileIn.readline() 
	cnt = 0
	while line:
		cnt += 1
		if cnt % 100000 == 0:
			print('line', cnt)
		dicLine = {}
		dicLine = ast.literal_eval(line)
		result = handleLinePOL(result, dicLine, dicM, dicS, dicMapS)
		
		line = fileIn.readline() 

	fileIn.close()
	
	return result

#=========================================================================================================
print('dicM..')
dicM = getDic(fDicMunicipalities)

print('dicS..')
dicS = getDic(fDicStreets)

print('dicMapS..')
dicMapS = getDic(fMapStreetnames)

print('creating streetcode mapping file..')
dic = createStreetCodeMappingPOL(SRC_POL_IN, dicM, dicS, dicMapS, PREFERRED_ENCODING)
saveDic(dic, fMapStreetCode_POLtoBEST)