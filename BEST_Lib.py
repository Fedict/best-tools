import sys
import operator
import json #issue with quotes as double quote is mandatory for json
import ast #reading dictionaries from a file without problems with quotes
import os
import time
import datetime
from fuzzywuzzy import fuzz
import csv
DELIMITER = '$'

PREFERRED_ENCODING = "utf-8"
ENCODING_RR = "latin-1" 
ENCODING_KBO = "latin-1" 
ENCODING_POL = "utf-8" 
ENCODING_CSV = "ansi"
ENCODING_AAPD = "latin-1" 

THRESHOLD_STREET = 0.9

BEST_B_CONSOLIDATEDRESULT = '_BEST_B_consolidatedResult.txt'
BEST_F_CONSOLIDATEDRESULT = '_BEST_F_consolidatedResult.txt'
BEST_W_CONSOLIDATEDRESULT = '_BEST_W_consolidatedResult.txt'

SRC_RR_B_ORI = './RR/RR_B'
SRC_RR_B_IN = 'RR_B.txt'
SRC_RR_B_RESULT = 'RR_B_result.txt'

SRC_RR_F_ORI = './RR/RR_F'
SRC_RR_F_IN = 'RR_F.txt'
SRC_RR_F_RESULT = 'RR_F_result.txt'

SRC_RR_W_ORI = './RR/RR_W'
SRC_RR_W_IN = 'RR_W.txt'
SRC_RR_W_RESULT = 'RR_W_result.txt'

SRC_KBO_ORI = "./KBO/SRC_KBO.csv"
SRC_KBO_IN = 'KBO.txt'
SRC_KBO_RESULT = 'KBO_result.txt'

SRC_POL_ORI = "./POL/SRC_POL.txt"
SRC_POL_IN = 'POL.txt'
SRC_POL_RESULT = 'POL_result.txt'

SRC_AAPD_ORI = "./AAPD/SRC_AAPD.csv"
SRC_AAPD_IN = "AAPD.txt"
SRC_AAPD_RESULT = "AAPD_result.txt"
SRC_AAPD_MUNICIPALITY_TABLE = "./AAPD/Export_AdMuKey.csv"
SRC_AAPD_STREET_TABLE = "./AAPD/Export_CaSt.csv"

#BEST source xml
filenameDic = \
{'B':{'address':'BrusselsAddress.xml', 'municipality':'BrusselsMunicipality.xml', 'partOfMunicipality':'BrusselsPartOfMunicipality.xml', 'street':'BrusselsStreetName.xml', 'postalInfo': 'BrusselsPostalInfo.xml'}, \
'F':{'address':'FlandersAddress.xml', 'municipality':'FlandersMunicipality.xml', 'partOfMunicipality':'FlandersPartOfMunicipality.xml', 'street':'FlandersStreetName.xml', 'postalInfo': 'FlandersPostalInfo.xml'}, \
'W':{'address':'WalloniaAddress.xml', 'municipality':'WalloniaMunicipality.xml', 'partOfMunicipality':'WalloniaPartOfMunicipality.xml', 'street':'WalloniaStreetName.xml', 'postalInfo': 'WalloniaPostalInfo.xml'}}

#lists
fLstMunicipalities = 'LST_Municipalities.txt'
fLstStreets = 'LST_Streetnames.txt'
fLstAddresses = 'LST_Addresses.txt'
fLstPartOfMunicipalities = 'LST_PartOfMunicipalities.txt'
fLstPostalInfo = 'LST_PostalInfo.txt'

#dictionaries
fDicMunicipalities = 'DIC_Municipalities.txt'
fDicStreets = 'DIC_Streetnames.txt'
fDicAddresses = 'DIC_Addresses.txt'
fDicAddresses100 = 'DIC_Addresses100.txt'
fDicAddressesMollestraat = 'DIC_AddressesMollestraat.txt'
fDicPartOfMunicipalities = 'DIC_PartOfMunicipalities.txt'
fDicPostalInfo = 'DIC_PostalInfo.txt'

#out: text files
fOutMunicipalities = 'OUT_Municipalities.txt'
fOutPartOfMunicipalities = 'OUT_PartOfMunicipalities.txt'
fOutMunicipalitiesPartOfMunicipalities = 'OUT_Municipalities_PartOfMunicipalities.txt'
fOutStreets = 'OUT_Streetnames.txt'
fOutPostalInfo = 'OUT_PostalInfo.txt'
fOutPostcodes = 'OUT_Postcodes.txt'

#stat: text files
fStatMunicipalities = 'STAT_Municipalities.txt'
fStatPartOfMunicipalities = 'STAT_PartOfMunicipalities.txt'
fStatStreets = 'STAT_Streetnames.txt'
fStatPostalInfo = 'STAT_PostalInfo.txt'
fStatAddresses = 'STAT_Addresses.txt'

#map: text files
fMapMunicipalities = 'MAP_Municipalities.txt'
fMapMunicipalitiesRR = 'MAP_MunicipalitiesRR.txt'
fMapMunicipalitiesStreetsRR = 'MAP_MunicipalitiesStreetsRR.txt'
fMapStreetnames = 'MAP_Streets.txt'
fMapStreetnamesRR = 'MAP_StreetsRR.txt'
fMapAddresses = 'MAP_Addresses.txt'
fMapHouseNrs = 'MAP_HouseNrs.txt'
fMapBoxNrs = 'MAP_BoxNrs.txt'
fMapHouseNrsRR = 'MAP_HouseNrsRR.txt'
fMapBoxNrsRR = 'MAP_BoxNrsRR.txt'
fMapMunToR = 'MAP_MunToR.txt'
fMapStreetCode_RRtoBEST = 'MAP_StreetCode_RRtoBEST.txt'
fMapStreetCode_POLtoBEST = 'MAP_StreetCode_POLtoBEST.txt'
fMapStreetCode_AAPDtoBEST = 'MAP_StreetCode_AAPDtoBEST.txt'

fMToP = "DIC_MtoP.txt" 	#Municipality to PostalInfo
fMtoPM = "DIC_MtoPM.txt" #Municipality to PartOfMunicipality
fPtoM = "DIC_PtoM.txt" 	#PostalInfo to Municipality
fPtoPM = "DIC_PtoPM.txt" #PostalInfo to PartOfMunicipality
fPMtoM = "DIC_PMtoM.txt" #PartOfMunicipality to Municipality
fPMtoP = "DIC_PMtoP.txt" #PartOfMunicipality to PostalInfo

warningA1 = 'A1. ISSUE Municipality irrelevant'
warningA2 = 'A2. ISSUE Municipality unknown'
warningB1 = 'B1. ISSUE Street irrelevant'
warningB2 = 'B2. ISSUE Street mapping'
warningC1 = 'C1. ISSUE Nrs (Str.without nrs in BEST)'
warningC2 = 'C2. ISSUE Nrs (Hs <>)'
warningD1 = 'D1. ISSUE Nrs (Hs =, Bx missing in BEST)'
warningD2 = 'D2. ISSUE Nrs (Hs =, Bx <>)'
warningD3 = 'D3. ISSUE Nrs (Hs =, Bx missing in SRC)'

actionOk = 'ok - no action'
actionA1 = 'ok - data can be ignored'
actionB1 = 'ok - data can be ignored'
actionD1 = 'ok - automated addition of missing box nr in BEST'


def printDic(dic):
	for k, v in sorted(dic.items(), key=operator.itemgetter(0)):
		try:
			print('{',"'"+k+"'", ':',v, '}')
		except:
			print('printDic issue')
		
def printLst(lst):
	for item in lst:
		print(item)

def saveDic(dic, filename):
	with open(filename, "wb") as f:
		f.write(json.dumps(dic).encode(PREFERRED_ENCODING))
	# file = open(filename,"w", encoding=PREFERRED_ENCODING)
	# for k, v in dic.items():
		# try:
			# file.write('"' + k + '":"' + v + '"')
		# except:
			# print('BEST_Lib.py, saveLstOfDic issue:', k, v)
	# file.write('}\n')
	# file.close()

def saveLstOfDic(lst, filename):
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	for dic in lst:
		file.write('{')
		first = True
		for k, v in dic.items():
			try:
				if not first:
					file.write(',')
				v = v.replace('"', "'") #cfr streetnames containing double quotes in their names
				file.write('"' + k + '":"' + v + '"')
				first = False
			except:
				print('BEST_Lib.py, saveLstOfDic issue:', k, v)
		file.write('}\n')
	file.close()
		
def printTuples(tuples):
	for tuple in tuples:
		print(tuple)

#FUZZY MAPPING=======================================================================
def matchIt(lst, strToMatch):
	bestMatch = ''
	highest = 0.0
	for item in lst:
		prob = fuzz.ratio(strToMatch, item)/100.0
		if prob >= highest:
			highest = prob
			bestMatch = item
	return (bestMatch, highest)
	
#GENERAL TOOLING=======================================================================
def getBESTStreetName(dicS, R, idS):
	S = ""
	v = getLastVersion(dicS[R],idS)
	result = ""
	if R == 'W':
		if 'fr' in dicS[R][idS][v].keys():
			result = dicS[R][idS][v]['fr']
		elif 'de' in dicS[R][idS][v].keys():
			result = dicS[R][idS][v]['de']
		else:
			print("getBESTStreetName", R, idS)
	else:
		if 'nl' in dicS[R][idS][v].keys():
			result = dicS[R][idS][v]['nl']
		else:
			print("getBESTStreetName", R, idS)
	return result

def getBESTMunicipalityName(dicM, R, idM):
	M = ""
	v = getLastVersion(dicM[R],idM)
	result = ""
	try:
		if R == 'W':
			if 'fr' in dicM[R][idM][v].keys():
				result = dicM[R][idM][v]['fr']
			elif 'de' in dicM[R][idM][v].keys():
				result = dicM[R][idM][v]['de']
			else:
				print("getBESTMunicipalityName", R, idM)
		elif R == 'F':
			if 'nl' in dicM[R][idM][v].keys():
				result = dicM[R][idM][v]['nl']
			else:
				print("getBESTMunicipalityName", R, idM)
		else: 	#Brussels
			if idM > "21000" and idM < "21999":
				if 'nl' in dicM[R][idM][v].keys():
					result = dicM[R][idM][v]['nl']
				else:
					print("getBESTMunicipalityName", R, idM)
			else:
				print("getBESTMunicipalityName not in range Brussels", R, idM)
	except:
		print('ISSUE getBESTMunicipalityName', R, idM)
		result = 'Unknown'
	return result

def xmlStatistics(xmlType):
#xmlType in ['address', 'municipality', 'partOfMunicipality', 'street', 'postalInfo' ]
	result = []
	for region in ['B', 'F', 'W']:
		filename = filenameDic[region][xmlType]
		size = '{:,}'.format(os.path.getsize(filename))
		cnt = 0
		file = open(filename,"r", encoding=PREFERRED_ENCODING)
		line = file.readline()
		while line:
			cnt += 1
			line = file.readline()
		result.append(filename + ':' + '{:,}'.format(cnt) + ' xml lines(' + str(size) + ' bytes)' + '\n')
		file.close()
	return result
			
def lookupStringInFile(filename, s):
#returns a list of linecounters where the string was found
	result = []
	print(filename, s)
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	lineCounter=0
	try:
		line = file.readline()
	except:
		print(lineCounter,"Unexpected error:")
	cntOccurrences=0
	while line:
		lineCounter += 1
		if s in line:
			cntOccurrences += 1
			print(lineCounter, line.rstrip())
			result.append(lineCounter)
		try:
			line = file.readline()
		except:
			print(lineCounter, "Unexpected error:")
	s = '"'+s+'"'
	print('Found', cntOccurrences, 'occurrences of', s, 'in', filename, '(total number of lines:', lineCounter,')')
	file.close()
	return result

def showPartOfFile(filename, location):
	print(filename)
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	lineCounter=0
	while line:
		lineCounter += 1
		if lineCounter > (location - 100) and lineCounter < (location + 100):
			print(lineCounter, line.rstrip())
		line=file.readline()
	file.close()
#lookupStringInFile(filenameDic['W']['street'], '63001')
#showPartOfFile(filenameDic['W']['street'], 1244898)

def showStringInFile(filename, searchString):
	lineNrs = lookupStringInFile(filename, searchString)	
	for lineNr in lineNrs:
		showPartOfFile(filename, lineNr)
		print('===============')
	print('=============================================================')

def filterExceptions(filename, strCheck, strDefault):	
	print(filename)
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	lineCounter=0
	cntOccurrences=0
	while line:
		lineCounter += 1
		if strCheck in line:
			if strDefault not in line:
				cntOccurrences += 1
				print(lineCounter, line.rstrip())
		line=file.readline()
	print('Found', cntOccurrences, 'occurrences of "'+ strCheck + '" in', filename, ', different from default "' + strDefault + '"')
	file.close()

def getLastVersion(dicIn, key):
#assumption: dictionary has structure {key:{versionX:{..}, versionY:{..}, ..}
#output: maxVersion
	dic = {}
	if key in dicIn:
		dic = dicIn[key]
		maxK = ''
		for k,v in dic.items():
			if k > maxK :
				maxK = k 
		return maxK
	else:
		return '?'
def getDataLastVersion(region, objId, dicIn):
#in: dicIn (e.g. Municipality), region, objectIdentifier
#out: quadruple => region, objId, maxVersion, data in form of a dictionary
	if region in dicIn:
		maxVersion = getLastVersion(dicIn[region],objId)
		if maxVersion != '?':
			return(region, objId, maxVersion, dicIn[region][objId][maxVersion])
		else:
			return(region, objId, 'unknown version', 'unknown data')
	else:
		return(region, objId, 'region unknown', 'unknown data')

def makeString(quadruple):
	region, objId, maxVersion, dataDic = quadruple
	strNl = ''
	strFr = ''
	strDe = ''
	if 'nl' in dataDic:
		strNl = dataDic['nl']
	if 'fr' in dataDic:
		strFr = dataDic['fr']
	if 'de' in dataDic:
		strDe = dataDic['de']
	result = region + '-' + strNl + ',' + strFr + ',' + strDe + '(' + objId + ')'
	return result

def makeMultiLanguageList(quadruple):
	region, objId, maxVersion, dataDic = quadruple
	strNl = ''
	strFr = ''
	strDe = ''
	result = []
	if 'nl' in dataDic:
		result.append(dataDic['nl'])
	if 'fr' in dataDic:
		result.append(dataDic['fr'])
	if 'de' in dataDic:
		result.append(dataDic['de'])
	return result

def containsParenthesisPartAtEnd(str):
	if '(' in str and str[-1:] == ')' :
		return True
	else:
		return False
		
def containsComma(str):
	if ',' in str:
		return True
	else:
		return False
	
def removePartAfterComma(str):
	pos = str.find(',')
	result = str[0:pos]
	if result[-1:] == ' ':
		result = result[:-1]
	return result
def removePartBeforeComma(str):
	pos = str.find(',')
	result = str[pos+1:]
	return result
def removePartBeforeDash(str):
	pos = str.find('-')
	result = str[pos+1:]
	return result

def getParenthesisPart(str):
	startPos = str.find('(')
	endPos = str.find(')')
	return str[startPos+1:endPos]
def removeParenthesisPart(str):
	startPos = str.find('(')
	endPos = str.find(')')
	result = str[0:startPos] + str[endPos+1:]
	if result[-1:] == ' ':
		result = result[:-1]
	return result
def putParenthesisPartInFront(str):
	A = removeParenthesisPart(str)
	B = getParenthesisPart(str)
	result = B + ' ' + A
	return result
def cntCharType(str):
	cntDigit = 0
	cntAlpha = 0
	for c in str:
		if c.isdigit():
			cntDigit += 1
		elif c.isalpha():
			cntAlpha += 1
	return cntAlpha, cntDigit	
	
def removeLeadingZeroes(str):
	ix = 0
	l = len(str)
	for i in range(len(str)):
		if str[i] == '0':
			ix = ix + 1
		else:
			break
	return str[ix:]
	
def removeTrailingZeroes(str):
	ix = len(str)
	for c in reversed(str):
		if c != '0':
			break
		ix = ix -1
	return str[0:ix]

def removeSubstring(substr, str):
	length = len(substr)
	index = str.find(substr)
	if index > 0:
		str = str[0:index] + str[index+length:]
	return str

def removeLeadingSubstring(substr, str):
	length = len(substr)
	index = str.find(substr)
	if index == 0:
		str = str[0:index] + str[index+length:]
	return str

#===AAPD dictionaries===================================================================================
def createMunDicAAPD(inputFile):
	dic = {}
	fileIn = open(inputFile,"r", encoding=ENCODING_AAPD)

	line = fileIn.readline() #headerline

	line = fileIn.readline()
	while line:
		lst = line.split(';')
		idM = lst[0]
		Mfr = lst[1]
		Mnl = lst[2]
		Mde = lst[3]
		dic[idM] = {'Mfr': Mfr, 'Mnl':Mnl, 'Mde':Mde}
		line = fileIn.readline() 

	fileIn.close()
	return dic

def createStreetDicAAPD(inputFile):
	dic = {}
	fileIn = open(inputFile,"r", encoding=ENCODING_AAPD)

	line = fileIn.readline() #headerline

	line = fileIn.readline()
	while line:
		lst = line.split(';')
		idM = lst[0]
		idS = lst[1]
		Snl = lst[2]
		Sfr = lst[3]
		Sde = lst[4].strip()
		if not idM in dic:
			dic[idM] = {}
		dic[idM][idS] = {'Sfr': Sfr, 'Snl':Snl, 'Sde':Sde}
		line = fileIn.readline() 

	fileIn.close()
	return dic
#PARSING=======================================================================
def getObjectIdentifier(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:objectIdentifier>','')
	line = line.replace('</com:objectIdentifier>','')
	line = line.rstrip().lstrip()
	return line
	
def getVersionIdentifier(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:versionIdentifier>','')
	line = line.replace('</com:versionIdentifier>','')
	line = line.rstrip().lstrip()
	return line
	
def getLanguage(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:language>','')
	line = line.replace('</com:language>','')
	line = line.rstrip().lstrip()
	return line
	
def getHomonym(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:homonymAddition>','')
	line = line.replace('</com:homonymAddition>','')
	line = line.rstrip().lstrip()
	return line
	
def getSpelling(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:spelling>','')
	line = line.replace('</com:spelling>','')
	line = line.rstrip().lstrip()
	return line

def getStatus(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:status>','')
	line = line.replace('</com:status>','')
	line = line.rstrip().lstrip()
	return line
	
def getValidFrom(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:validFrom>','')
	line = line.replace('</com:validFrom>','')
	line = line.rstrip().lstrip()
	return line
	
def getStreetnameType(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:streetnameType>','')
	line = line.replace('</com:streetnameType>','')
	line = line.rstrip().lstrip()
	return line

def getAddressPosition(line):
	line = line.rstrip().lstrip()
	line = line.replace('</gml:pos>','')
	pos = line.find('>') + 1
	line = line[pos:]
	x, y = line.split(' ')
	return x, y
	
def getAddressSortfield(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:addressSortfield>','')
	line = line.replace('</com:addressSortfield>','')
	line = line.rstrip().lstrip()
	return line

def getBoxNumber(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:boxNumber>','')
	line = line.replace('</com:boxNumber>','')
	line = line.rstrip().lstrip()
	return line

def getHouseNumber(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:houseNumber>','')
	line = line.replace('</com:houseNumber>','')
	line = line.rstrip().lstrip()
	return line

def getOfficiallyAssigned(line):
	line = line.replace('add:','com:')
	line = line.replace('<com:officiallyAssigned>','')
	line = line.replace('</com:officiallyAssigned>','')
	line = line.rstrip().lstrip()
	return line
	
#CONVERT MUNICIPALITY=========================================================
def treatMunicipality(resultList, stateDic, line):
	stateDic["state"] = "Municipality found"
	stateDic["idM"] =  ""
	stateDic["vM"] = ""
	stateDic["lan"] = ""
	stateDic["M"] = ""
	return resultList, stateDic
def treatMunicipalityObjectIdentifier(resultList, stateDic, line):
	stateDic["state"] = "objectIdentifier found"
	stateDic["idM"] = getObjectIdentifier(line)
	return resultList, stateDic
def treatMunicipalityVersionIdentifier(resultList, stateDic, line):
	stateDic["state"] = "versionIdentifier found"
	stateDic["vM"] = getVersionIdentifier(line)
	return resultList, stateDic
def treatMunicipalityLanguage(resultList, stateDic, line):
	stateDic["state"] = "language found"
	stateDic["lan"] = getLanguage(line)
	return resultList, stateDic
def treatMunicipalitySpelling(resultList, stateDic, line):
	stateDic["state"] = "spelling found"
	stateDic["M"] = getSpelling(line)
	resultList, stateDic = addMunicipality(resultList, stateDic)
	return resultList, stateDic
def addMunicipality(resultList, stateDic):
	dic = {}
	dic["R"] = stateDic["R"]
	dic["idM"] = stateDic["idM"]
	dic["vM"] =  stateDic["vM"]
	dic["lan"] =  stateDic["lan"]
	dic["M"] =  stateDic["M"]
	resultList.append(dic)
	stateDic["lan"] = ""
	stateDic["M"] = ""
	return resultList, stateDic
	
def makeRegionalListMunicipalities(region):
#out: list van dictionaries - elke dictionary heeft volgende keys: "region", "objectIdentifier", "versionIdentifier", "language", "spelling"
#out: list van dictionaries - elke dictionary heeft volgende keys: "R", "idM", "vM", "lan", "M"
	resultList = []
	stateDic = {"state": "", "R": region, "idM": "", "vM": "", "lan": "", "M": ""}

	filename = filenameDic[region]["municipality"]
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	lineCounter=0
	line=file.readline()
	while line:
		lineCounter += 1
		if "<tns:Municipality" in line:
			resultList, stateDic = treatMunicipality(resultList, stateDic, line)
		#elif "<com:objectIdentifier>" in line or "<add:objectIdentifier>" in line:
		elif "<com:objectIdentifier>" in line:
			resultList, stateDic = treatMunicipalityObjectIdentifier(resultList, stateDic, line)
		#elif "<com:versionIdentifier>" in line or "<add:versionIdentifier>" in line:
		elif "<com:versionIdentifier>" in line:
			resultList, stateDic = treatMunicipalityVersionIdentifier(resultList, stateDic, line)
		#elif "<com:language>" in line or "<add:language>" in line:
		elif "<com:language>" in line:
			resultList, stateDic = treatMunicipalityLanguage(resultList, stateDic, line)
		#elif "<com:spelling>" in line or "<add:spelling>" in line:
		elif "<com:spelling>" in line:
			resultList, stateDic = treatMunicipalitySpelling(resultList, stateDic, line)
		line=file.readline()
	#print(filename, ":", "{0:,.0f}".format(lineCounter), "xml lines")
	file.close()
	return resultList	
	
def makeNationalListMunicipalities():
#out: list van dictionaries - elke dictionary heeft volgende keys: "region", "objectIdentifier", "versionIdentifier", "language", "spelling"
#out: list van dictionaries - elke dictionary heeft volgende keys: "R", "idM", "vM", "lan", "M"
	resultList = []
	for region in ["B", "F", "W"]:
		resultList.extend(makeRegionalListMunicipalities(region))
	return resultList

def makeDicMunicipalities(fLstMunicipalities):
#in: filename
#out: dictionary { 'B' : {'7500': {'2': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}, '1': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}}, ...
	result = {}
	file=open(fLstMunicipalities,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	while line:
		dicLine = {}
		dicLine = ast.literal_eval(line)
		R = dicLine['R']
		idM = str(dicLine['idM'])
		vM = dicLine['vM']
		if vM == '':
			vM = '-'
		lan = dicLine['lan']
		M = dicLine['M']
		
		if R not in result:
			dicLan = {}
			dicV = {}
			dicM = {}
			
			dicLan[lan] = M
			dicV[vM] = dicLan
			dicM[idM] = dicV
			result[R] = dicM
		elif idM not in result[R]:
			dicLan = {}
			dicV = {}
			
			dicLan[lan] = M
			dicV[vM] = dicLan
			result[R][idM] = dicV
		elif vM not in result[R][idM]:
			dicLan = {}
			dicLan[lan] = M
			result[R][idM][vM] = dicLan
		else:
			result[R][idM][vM][lan] = M

		line=file.readline()
	file.close()
	return result

def getDic(filename):
	with open(filename, 'r') as f:
		dic = json.load(f)
	return dic
	
def getDicM(fDicMunicipalities):
#in: filename
#out: dictionary { 'B' : {'7500': {'2': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}, '1': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}}, ...
	file=open(fDicMunicipalities,"r", encoding=PREFERRED_ENCODING)
	dicM = {}
	line = file.readline()
	while line:
		dic = {}
		dic = ast.literal_eval(line)
		for k,v in dic.items():
			dicM[k] = v
		line = file.readline()
	file.close()
	return dicM

def outputMunicipalities(dicM, filename):
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	lst = []
	for keyR,valR in dicM.items():
		R = keyR
		for keyM, valM in valR.items():
			idM = keyM
			v = getLastVersion(dicM[R], keyM)
			if v != '?':
				if 'nl' in valM[v]:
					Mnl = valM[v]['nl']
					lst.append(keyR + ',' + Mnl + ',nl,' + idM)
				if 'fr' in valM[v]:
					Mfr = valM[v]['fr']
					lst.append(keyR + ',' + Mfr + ',fr,' + idM)
				if 'de' in valM[v]:
					Mde = valM[v]['de']
					lst.append(keyR + ',' + Mde + ',de,' + idM)
	for item in sorted(lst):
			file.write(item + '\n')
	file.close()

def	saveDicMunicipalitiesStatistics(dicMunicipalities, filename): 
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	lst = xmlStatistics('municipality')
	for item in sorted(lst):
		file.write(item)
	file.write('\n')
	lstB = []
	lstF = []
	lstW = []
	for k, v in dicMunicipalities.items():
		region = k
		for k2, v2 in v.items():
			key = k2
			if region == "F" :
				lstF.append(key)
			elif region == "W" :
				lstW.append(key)
			elif region == "B" :
				lstB.append(key)
	setB = set(lstB)
	setF = set(lstF)
	setW = set(lstW)
	total = len(setB)+len(setF)+len(setW)
	file.write("MUNICIPALITY statistics" + "\n")
	file.write("number of unique objectIdentifiers:" +  " B:" + str(len(setB)) + " F:" + str(len(setF)) + " W:" + str(len(setW)) + "\n")
	file.write("total number of unique objectIdentifiers: " + str(total) + "\n")
	file.close()

def getRegion(dicM, idM, municipalityName):
	for region in ['B', 'F', 'W']:
		for id, dicV in dicM[region].items():
			if id == idM :
				for k,v in dicV.items():
					if 'nl' in v:
						if municipalityName == v['nl']:
							return region
					if 'fr' in v:
						if municipalityName == v['fr']:
							return region
					if 'de' in v:
						if municipalityName == v['de']:
							return region
	return "region unknown"

def convertMunicipality(dicM, municipalityName):
	result = []
	R = ''
	idM = ''
	v = ''
	Mnl = ''
	Mfr = ''
	Mde = ''
	str = ''
	
	for keyR,valR in dicM.items():
		for keyM, valM in valR.items():
			for ver, valV in valM.items():
				if 'nl' in valV:
					if valV['nl'] == municipalityName:
						Mnl = valV['nl']
						R = keyR
						idM = keyM
						v = ver
				if 'fr' in valV:
					if valV['fr'] == municipalityName:
						Mfr = valV['fr']
						R = keyR
						idM = keyM
						v = ver
				if 'de' in valV:
					if valV['de'] == municipalityName:
						Mde = valV['de']
						R = keyR
						idM = keyM
						v = ver
				if R != '':
					str = R + "-" + idM + "-Version:" + v + "-" + Mnl + "," + Mfr + ',' + Mde
					result.append(str)
					R = ''
					idM = ''
					v = ''
					Mnl = ''
					Mfr = ''
					Mde = ''
	return result
	
def getIdMLst(dicM, municipalityName):
	lstIdM = []
	lst = convertMunicipality(dicM, municipalityName)
	for item in lst:
		item = item[2:]
		pos = item.find('-')
		idM = item[:pos]
		lstIdM.append(idM)
	return lstIdM

#CREATE link municipality - submunicipalities =====
def makeOutMunicipalitiesPartOfMunicipalities(filename):
	dicA = getDic(fDicAddresses)
	dic = {}
	for R, dicR in dicA.items():
		for A, dicA in dicR.items():
			V = getLastVersion(dicR, A)
			idM = dicA[V]['idM']
			key = R + '-' + idM
			idPM = dicA[V]['idPM']
			if not idPM == "":
				if not key in dic:
					dic[key] = []
				if not idPM in dic[key]:
					dic[key].append(idPM)
					
	saveDic(dic, filename)

#CONVERT PARTOFMUNICIPALITY=========================================================
def treatPartOfMunicipality(resultList, stateDic, line):
	stateDic["state"] = "PartOfMunicipality found"
	stateDic["idPM"] =  ""
	stateDic["vPM"] = ""
	stateDic["lan"] = ""
	stateDic["PM"] = ""
	return resultList, stateDic
def treatPartOfMunicipalityObjectIdentifier(resultList, stateDic, line):
	stateDic["state"] = "objectIdentifier found"
	stateDic["idPM"] = getObjectIdentifier(line)
	return resultList, stateDic
def treatPartOfMunicipalityVersionIdentifier(resultList, stateDic, line):
	stateDic["state"] = "versionIdentifier found"
	stateDic["vPM"] = getVersionIdentifier(line)
	return resultList, stateDic
def treatPartOfMunicipalityLanguage(resultList, stateDic, line):
	stateDic["state"] = "language found"
	stateDic["lan"] = getLanguage(line)
	return resultList, stateDic
def treatPartOfMunicipalitySpelling(resultList, stateDic, line):
	stateDic["state"] = "spelling found"
	stateDic["PM"] = getSpelling(line)
	resultList, stateDic = addPartOfMunicipality(resultList, stateDic)
	return resultList, stateDic
def addPartOfMunicipality(resultList, stateDic):
	dic = {}
	dic["R"] = stateDic["R"]
	dic["idPM"] = stateDic["idPM"]
	dic["vPM"] =  stateDic["vPM"]
	dic["lan"] =  stateDic["lan"]
	dic["PM"] =  stateDic["PM"]
	resultList.append(dic)
	stateDic["lan"] = ""
	stateDic["PM"] = ""
	return resultList, stateDic
	
def makeRegionalListPartOfMunicipalities(region):
#out: list van dictionaries - elke dictionary heeft volgende keys: "region", "objectIdentifier", "versionIdentifier", "language", "spelling"
#out: list van dictionaries - elke dictionary heeft volgende keys: "R", "idPM", "vPM", "lan", "PM"
	resultList = []
	stateDic = {"state": "", "R": region, "idPM": "", "vPM": "", "lan": "", "PM": ""}

	filename = filenameDic[region]["partOfMunicipality"]
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	lineCounter=0
	line=file.readline()
	while line:
		lineCounter += 1
		if "<tns:PartOfMunicipality" in line:
			resultList, stateDic = treatPartOfMunicipality(resultList, stateDic, line)
		#elif "<com:objectIdentifier>" in line or "<add:objectIdentifier>" in line:
		elif "<com:objectIdentifier>" in line:
			resultList, stateDic = treatPartOfMunicipalityObjectIdentifier(resultList, stateDic, line)
		#elif "<com:versionIdentifier>" in line or "<add:versionIdentifier>" in line:
		elif "<com:versionIdentifier>" in line:
			resultList, stateDic = treatPartOfMunicipalityVersionIdentifier(resultList, stateDic, line)
		#elif "<com:language>" in line or "<add:language>" in line:
		elif "<com:language>" in line:
			resultList, stateDic = treatPartOfMunicipalityLanguage(resultList, stateDic, line)
		#elif "<com:spelling>" in line or "<add:spelling>" in line:
		elif "<com:spelling>" in line:
			resultList, stateDic = treatPartOfMunicipalitySpelling(resultList, stateDic, line)
		line=file.readline()
	#print(filename, ":", "{0:,.0f}".format(lineCounter), "xml lines")
	file.close()
	return resultList	
	
def makeNationalListPartOfMunicipalities():
# out: list van dictionaries - elke dictionary heeft volgende keys: "region", "objectIdentifier", "versionIdentifier", "language", "spelling"
# out: list van dictionaries - elke dictionary heeft volgende keys: "R", "idM", "vM", "lan", "M"
	resultList = []
	for region in ["B", "F", "W"]:
		regionalLst = makeRegionalListPartOfMunicipalities(region)
		resultList.extend(regionalLst)
	return resultList

def makeDicPartOfMunicipalities(fLstPartOfMunicipalities):
#in: filename
#out: dictionary { 'B' : {'7500': {'2': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}, '1': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}}, ...
	result = {}
	file=open(fLstPartOfMunicipalities,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	while line:
		dicLine = {}
		dicLine = ast.literal_eval(line)
		R = dicLine['R']
		idPM = str(dicLine['idPM'])
		vPM = dicLine['vPM']
		if vPM == '':
			vPM = '-'
		lan = dicLine['lan']
		PM = dicLine['PM']
		
		if R not in result:
			dicLan = {}
			dicV = {}
			dicPM = {}
			
			dicLan[lan] = PM
			dicV[vPM] = dicLan
			dicPM[idPM] = dicV
			result[R] = dicPM
		elif idPM not in result[R]:
			dicLan = {}
			dicV = {}
			
			dicLan[lan] = PM
			dicV[vPM] = dicLan
			result[R][idPM] = dicV
		elif vPM not in result[R][idPM]:
			dicLan = {}
			dicLan[lan] = PM
			result[R][idPM][vPM] = dicLan
		else:
			result[R][idPM][vPM][lan] = PM

		line=file.readline()
	file.close()
	return result

# def getDicM(fDicMunicipalities):
# #in: filename
# #out: dictionary { 'B' : {'7500': {'2': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}, '1': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}}, ...
	# file=open(fDicMunicipalities,"r", encoding=PREFERRED_ENCODING)
	# dicM = {}
	# line = file.readline()
	# while line:
		# dic = {}
		# dic = ast.literal_eval(line)
		# for k,v in dic.items():
			# dicM[k] = v
		# line = file.readline()
	# file.close()
	# return dicM

def outputPartOfMunicipalities(dicPM, filename):
	lst = []
	#redirection of output failed due to weird characters e.g. '\x91'
	fileOut = open(filename, "w", encoding=PREFERRED_ENCODING)

	for keyR,valR in dicPM.items():
		R = keyR
		for keyPM, valPM in valR.items():
			idPM = keyPM
			v = getLastVersion(dicPM[R], keyPM)
			if v != '?':
				if 'nl' in valPM[v]:
					PMnl = valPM[v]['nl']
					lst.append(keyR + ',' + PMnl + ',nl,' + idPM)
				if 'fr' in valPM[v]:
					PMfr = valPM[v]['fr']
					lst.append(keyR + ',' + PMfr + ',fr,' + idPM)
				if 'de' in valPM[v]:
					PMde = valPM[v]['de']
					lst.append(keyR + ',' + PMde + ',de,' + idPM)
	for item in sorted(lst):
		fileOut.write(item+'\n')
	fileOut.close()
			
def	saveDicPartOfMunicipalitiesStatistics(dicPartOfMunicipalities, filename): 
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	lst = xmlStatistics('partOfMunicipality')
	for item in sorted(lst):
		file.write(item)
	file.write('\n')
	lstB = []
	lstF = []
	lstW = []
	for k, v in dicPartOfMunicipalities.items():
		region = k
		for k2, v2 in v.items():
			key = k2
			if region == "F" :
				lstF.append(key)
			elif region == "W" :
				lstW.append(key)
			elif region == "B" :
				lstB.append(key)
	setB = set(lstB)
	setF = set(lstF)
	setW = set(lstW)
	total = len(setB)+len(setF)+len(setW)
	file.write("PARTOFMUNICIPALITY statistics" + "\n")
	file.write("number of unique objectIdentifiers:" +  " B:" + str(len(setB)) + " F:" + str(len(setF)) + " W:" + str(len(setW)) + "\n")
	file.write("total number of unique objectIdentifiers: " + str(total) + "\n")
	file.close()

def createMappingFileMunToR(dicM):
	dic = {}
	for R, dicR in dicM.items():
		for idM, dicIdM in dicR.items():
			if not idM in dic.keys():
				dic[idM] = R
			else:
				print('double municipality id', idM)
	return dic
	
#CONVERT STREETNAME=======================================================================
def treatStreetname(resultList, stateDic, line):
	stateDic['state'] = 'Streetname found'
	stateDic['idS'] =  ''
	stateDic['vS'] = ''
	stateDic['lan'] = ''
	stateDic['S'] = ''
	stateDic['ho'] = ''
	stateDic['names'] = []
	stateDic['st'] = ''
	stateDic['v'] = ''
	stateDic['sType'] = ''
	stateDic['idM'] = ''
	return resultList, stateDic
def treatStreetHomonym(resultList, stateDic, line):
	stateDic['state'] = 'homonym found'
	stateDic['ho'] = getHomonym(line)
	return resultList, stateDic
def treatStreetObjectIdentifier(resultList, stateDic, line):
	if stateDic['state'] in ['Streetname found','homonym found']:
		stateDic['state'] = 'objectIdentifier found'
		stateDic['idS'] = getObjectIdentifier(line)
	elif stateDic['state'] == 'Municipality found':
		stateDic['state'] = 'objectIdentifierMunicipality found'
		stateDic['idM'] = getObjectIdentifier(line)
	return resultList, stateDic
def treatStreetVersionIdentifier(resultList, stateDic, line):
	stateDic['state'] = 'versionIdentifier found'
	if stateDic['idM'] == '': 	#avoid using the versionIdentifier of Municipality (we need the one of streetnameCode)
		stateDic['vS'] = getVersionIdentifier(line)
	return resultList, stateDic
def treatStreetLanguage(resultList, stateDic, line):
	stateDic['state'] = 'language found'
	stateDic['lan'] = getLanguage(line)
	return resultList, stateDic
def treatStreetSpelling(resultList, stateDic, line):
	stateDic['state'] = 'spelling found'
	#stateDic['S'] = getSpelling(line)
	dic = {}
	dic['ho'] =  stateDic['ho']
	dic['lan'] =  stateDic['lan']
	dic['S'] =  getSpelling(line)
	stateDic['names'].append(dic)
	stateDic['ho'] = ''
	stateDic['lan'] = ''
	#stateDic['S'] = ''
	return resultList, stateDic
def treatStreetStatus(resultList, stateDic, line):
	stateDic['state'] = 'status found'
	stateDic['st'] = getStatus(line)
	return resultList, stateDic
def treatStreetValidFrom(resultList, stateDic, line):
	stateDic['state'] = 'validFrom found'
	stateDic['v'] = getValidFrom(line)
	return resultList, stateDic
def treatStreetStreetnameType(resultList, stateDic, line):
	stateDic['state'] = 'streetnameType found'
	stateDic['sType'] = getStreetnameType(line)
	return resultList, stateDic
def treatStreetMunicipality(resultList, stateDic, line):
	stateDic['state'] = 'Municipality found'
	return resultList, stateDic
def treatStreetnameEnd(resultList, stateDic, line):
	stateDic['state'] = 'Streetname end found'
	resultList, stateDic = addStreet(resultList, stateDic)
	return resultList, stateDic
def addStreet(resultList, stateDic):
	for item in stateDic['names']:
		dic = {}
		dic['R'] = stateDic['R']
		dic['idS'] = stateDic['idS']
		dic['vS'] =  stateDic['vS']
		dic['st'] =  stateDic['st']
		dic['v'] =  stateDic['v']
		dic['sType'] =  stateDic['sType']
		dic['idM'] =  stateDic['idM']
		
		dic['ho'] =  item['ho']
		dic['lan'] =  item['lan']
		dic['S'] =  item['S']
		if dic['st'] not in ['archived', 'Archived', 'retired', 'Retired']:
			resultList.append(dic)
		else:
			print(dic['st'], dic['R'], dic['idS'], dic['lan'], dic['S'])
	return resultList, stateDic
	
def makeRegionalListStreets(region):
#out: list of dictionaries; each dictionary with keys 'R', 'idS', 'vS', 'lan', 'ho', 'S', 'st', 'v', 'sType', 'idM'
	resultList = []
	stateDic = {'state': '', 'R': region, 'idS': '', 'vS': '', 'lan': '', 'S': '', 'ho': '', 'st':'', 'v':'', 'sType':'', 'idM':''}

	filename = filenameDic[region]['street']
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	lineCounter=0
	line=file.readline()
	while line:
		lineCounter += 1
		if "<tns:Streetname" in line:
			resultList, stateDic = treatStreetname(resultList, stateDic, line)
		elif "<com:homonymAddition>" in line :
			resultList, stateDic = treatStreetHomonym(resultList, stateDic, line)
		elif "<com:objectIdentifier>" in line :
			resultList, stateDic = treatStreetObjectIdentifier(resultList, stateDic, line)
		elif "<com:versionIdentifier>" in line :
			resultList, stateDic = treatStreetVersionIdentifier(resultList, stateDic, line)
		elif "<com:language>" in line :
			resultList, stateDic = treatStreetLanguage(resultList, stateDic, line)
		elif "<com:spelling>" in line :
			resultList, stateDic = treatStreetSpelling(resultList, stateDic, line)
		elif "<com:status>" in line :
			resultList, stateDic = treatStreetStatus(resultList, stateDic, line)
		elif "<com:validFrom>" in line :
			resultList, stateDic = treatStreetValidFrom(resultList, stateDic, line)
		elif "<com:streetnameType>" in line :
			resultList, stateDic = treatStreetStreetnameType(resultList, stateDic, line)
		elif "<com:Municipality>" in line :
			resultList, stateDic = treatStreetMunicipality(resultList, stateDic, line)
		elif "</tns:Streetname" in line:
			resultList, stateDic = treatStreetnameEnd(resultList, stateDic, line)
		line=file.readline()
	#print(filename, ':', '{0:,.0f}'.format(lineCounter), 'xml lines')
	file.close()
	return resultList	
	
def makeNationalListStreets():
#out: list of dictionaries; each dictionary with keys 'R', 'idS', 'vS', 'lan', 'S', 'st', 'v', 'sType', 'idM'
	resultList = []
	for region in ["B", "F", "W"]:
		resultList.extend(makeRegionalListStreets(region))
	return resultList

def makeDicStreets(dicM):
#out: dictionary {R:{idS:{vS:{lan:S, 'ho':'', 'st':'', 'v', 'sType':'', idM, lanM:M}}}}
	filename = fLstStreets
	result = {}
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	while line:
		dicLine = {}
		dicLine = ast.literal_eval(line)
		# try:
			# dicLine = ast.literal_eval(line)
		# except:
			# print('ISSUE with parsing', line)
			# break
		R = dicLine['R']
		idS = dicLine['idS']
		vS = dicLine['vS']
		if vS == '':
			vS = '-'
		lan = dicLine['lan']
		S = dicLine['S']
		ho = dicLine['ho']
		# if ho != '':
			# print(S, ho)
		sType = dicLine['sType']
		st = dicLine['st']
		v = dicLine['v']
		idM = dicLine['idM']
		if idM:
			vM = getLastVersion(dicM[R], idM)
			if vM == '?':
				M = 'City unknown'
			elif lan in dicM[R][idM][vM]:	#lan is the language of the streetname
				M = dicM[R][idM][vM][lan]
			else:
				M = idM
		else:
			M = 'City name missing'
		lanM = lan + 'M'
		#print(R,idS,vS,lan,S,idM, M)

		dicV = {}
		dicV[lan] = S
		dicV['ho'] = ho
		dicV['st'] = st
		dicV['v'] = v
		dicV['sType'] = sType
		dicV['idM'] = idM
		dicV[lanM] = M
		
		if R not in result:
			dicS = {}
			dicR = {}
			
			dicS[vS] = dicV
			dicR[idS] = dicS
			result[R] = dicR
		elif idS not in result[R]:
			dicS = {}
			
			dicS[vS] = dicV
			result[R][idS] = dicS
		elif vS not in result[R][idS]:
			result[R][idS][vS] = dicV
		elif lan not in result[R][idS][vS]:
			result[R][idS][vS][lan] = S
			result[R][idS][vS][lanM] = M
		line=file.readline()
	file.close()
	return result

def getDicS(fDicStreets):
	file=open(fDicStreets,"r", encoding=PREFERRED_ENCODING)
	dicS = {}
	line = file.readline()
	while line:
		dic = {}
		dic = ast.literal_eval(line)
		for k,v in dic.items():
			dicS[k] = v
		line = file.readline()
	file.close()
	return dicS
	
def outputStreets(dicS, filename):
	lst = []
	for keyR,valR in dicS.items():
		R = keyR
		for keyS, valS in valR.items():
			idS = keyS
			v = getLastVersion(dicS[R], keyS)
			if v !='?':
				ho = valS[v]['ho']
				idM = ''
				if 'idM' in valS[v]:
					idM = valS[v]['idM']
				if 'nl' in valS[v]:
					S = valS[v]['nl']
					M=''
					if 'nlM' in valS[v]:
						M = valS[v]['nlM']
					str = keyR + ',' + M + ',' + S + ',nl,' + ho + ','+ idM + ',' + idS
					lst.append(str)
				if 'fr' in valS[v]:
					S = valS[v]['fr']
					if 'frM' in valS[v]:
						M = valS[v]['frM']
					str = keyR + ',' + M + ',' + S + ',fr,' + ho + ','+ idM + ',' + idS
					lst.append(str)
				if 'de' in valS[v]:
					S = valS[v]['de']
					if 'deM' in valS[v]:
						M = valS[v]['deM']
					str = keyR + ',' + M + ',' + S + ',de,' + ho + ','+ idM + ',' + idS
					lst.append(str)
	file=open(filename,"w", encoding=PREFERRED_ENCODING)
	for item in sorted(lst):
		file.write(item+'\n') #redirect output was giving issue, therefore write file
	file.close()

def	saveDicStreetsStatistics(dicStreets, filename): 
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	lst = xmlStatistics('street')
	for item in sorted(lst):
		file.write(item)
	file.write('\n')
	lstB = []
	lstF = []
	lstW = []
	for k, v in dicStreets.items():
		region = k
		for k2, v2 in v.items():
			key = k2
			if region == "F" :
				lstF.append(key)
			elif region == "W" :
				lstW.append(key)
			elif region == "B" :
				lstB.append(key)
	setB = set(lstB)
	setF = set(lstF)
	setW = set(lstW)
	total = len(setB)+len(setF)+len(setW)
	file.write('STREET statistics' + '\n')
	file.write("number of unique objectIdentifiers:" +  " B:" + str(len(setB)) + " F:" + str(len(setF)) + " W:" + str(len(setW)) + "\n")
	file.write("total number of unique objectIdentifiers: " + str(total) + "\n")
	file.close()

def convertStreet(dicS, streetName):
	result = []
	R = ''
	idS = ''
	v = ''
	Snl = ''
	Sfr = ''
	Sde = ''
	str = ''
	
	for keyR,valR in dicS.items():
		for keyS, valS in valR.items():
			for ver, valV in valS.items():
				if 'nl' in valV:
					if valV['nl'] == streetName:
						Snl = valV['nl']
						R = keyR
						idS = keyS
						v = ver
				if 'fr' in valV:
					if valV['fr'] == streetName:
						Sfr = valV['fr']
						R = keyR
						idS = keyS
						v = ver
				if 'de' in valV:
					if valV['de'] == streetName:
						Sde = valV['de']
						R = keyR
						idS = keyS
						v = ver
				if R != '':
					str = R + "-" + idS + "-Version:" + v + "-" + Snl + "," + Sfr + ',' + Sde
					result.append(str)
					R = ''
					idS = ''
					v = ''
					Snl = ''
					Sfr = ''
					Sde = ''
	return result

def getListMunicipalityStreets(dicM, dicS, municipalityName):
	result = []
	str = ''
	lstM = getIdMLst(dicM, municipalityName)
	if len(lstM) > 0:
		for idM in lstM:
			R = getRegion(dicM, idM, municipalityName)
			if R != 'unknown region':
				for idS, dicIdS in dicS[R].items():
					for ver, dicVer in dicIdS.items():
						if idM == dicVer['idM']:
							if 'nl' in dicVer:
								str = municipalityName + '-' + dicVer['nl'] + '-' + idM + '-' + idS
								result.append(str)
							if 'fr' in dicVer:
								str = municipalityName + '-' + dicVer['fr'] + '-' + idM + '-' + idS
								result.append(str)
							if 'de' in dicVer:
								str = municipalityName + '-' + dicVer['de'] + '-' + idM + '-' + idS
								result.append(str)
		return sorted(result)
	else:
		str = municipalityName + ' unknown'
		result.append(str)
		return result
				
def addKeysToDic(dic, key1, key2, idS):
# Map street
# Out: dictionary {key1:{key2:value}} where 
#      key1 = R + idM
#      key2 = streetname
#      value = idS
	if key1 not in dic.keys():
		if key2 != '':
			dic[key1]= {}
			dic[key1][key2] = idS
	else:
		if key2 != '':
			dic[key1][key2] = idS
	return dic

def createMappingFileStreets(dicS):
	dic = {}
	for R, dicR in dicS.items():
		for idS, dicIdS in dicR.items():
			v = getLastVersion(dicR, idS)
			dicV = dicIdS[v]
			Snl = ""
			Sfr = ""
			Sde = ""
			idM = ""
			ho = ""
			if 'idM' in dicV.keys():
				idM = dicV['idM']
				if 'nl' in dicV.keys():
					Snl = dicV['nl']
				if 'fr' in dicV.keys():
					Sfr = dicV['fr']
				if 'de' in dicV.keys():
					Sde = dicV['de']
				if 'ho' in dicV.keys():
					ho = dicV['ho']
					if ho != "":
						if Snl != "":
							Snl = Snl + " hom=" + ho
						if Sfr != "":
							Sfr = Sfr + " hom=" + ho
						if Sde != "":
							Sde = Sde + " hom=" + ho
				key1 = R + idM
				dic = addKeysToDic(dic, key1, Snl, idS)
				dic = addKeysToDic(dic, key1, Sfr, idS)
				dic = addKeysToDic(dic, key1, Sde, idS)
	return dic

def removeAccents(s):
	sConverted = s
	sConverted = sConverted.replace('ë','e')
	sConverted = sConverted.replace('é','e')
	sConverted = sConverted.replace('è','e')
	sConverted = sConverted.replace('ê','e')
	sConverted = sConverted.replace('à','a')
	sConverted = sConverted.replace('â','a')
	sConverted = sConverted.replace('ä','a')
	sConverted = sConverted.replace('ù','u')
	sConverted = sConverted.replace('ü','u')
	sConverted = sConverted.replace('û','u')
	sConverted = sConverted.replace('ÿ', 'y')
	sConverted = sConverted.replace('î', 'i')
	sConverted = sConverted.replace('ï', 'i')
	sConverted = sConverted.replace('ç', 'c')
	sConverted = sConverted.replace('ô', 'o')
	sConverted = sConverted.replace('ö', 'o')
	return sConverted
	
def convertStreetsRR(dicIn):
#RR delivers streetnames in uppercase only
	result = {}
	for key, dic in dicIn.items():	#key is concatenation of regio and idM
		for S, idS in dic.items():	#S is streetname, idS is id streetname
			Sconverted = removeAccents(S)
			Sconverted = Sconverted.upper()
			if key not in result.keys():
				result[key] = {}
			result[key][Sconverted] = idS
	return result
#===PREPARE StreetCodeMappingFile FROM RR-FILES, dicS and fMapStreetnamesRR===================================================================	
def transformStreetNameAndMap(bestMatchSoFar, highestProbSoFar, lst, strToMap):
	bestMatch2, prob = matchIt(lst, strToMap)
	if prob > highestProbSoFar:
		highestProbSoFar = prob
		bestMatchSoFar = bestMatch2

	if containsParenthesisPartAtEnd(strToMap):
		strToMap2 = putParenthesisPartInFront(strToMap)
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
			
		strToMap2 = removeParenthesisPart(strToMap)
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
			
	if containsComma(strToMap):
		strToMap2 = removePartAfterComma(strToMap)
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
			
		strToMap2 = removePartBeforeComma(strToMap)
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
	
	if "-" in strToMap:
		strToMap2 = strToMap.replace("-", " ")
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
			
		strToMap2 = removePartBeforeDash(strToMap)
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
			
	if " - " in strToMap:
		lstStrToMap = strToMap.split(' - ')
		strToMap2 = lstStrToMap[0]
		bestMatch2, prob = matchIt(lst, strToMap2)
		if prob > highestProbSoFar:
			highestProbSoFar = prob
			bestMatchSoFar = bestMatch2
			
	return bestMatchSoFar, highestProbSoFar
	
def createStreetCodeMapping(result, region, inputFile, dicS, dicMapStreetsRR):
	print('creating streetcode mapping file for region', region, '..')
	dic = {}
	
	fileIn = open(inputFile,"r", encoding=ENCODING_RR)
	line = fileIn.readline() #headerline
	line = fileIn.readline() 
	cnt = 1
	while line:
		dic = handleLine(dic, region, line, dicS, dicMapStreetsRR)
		line = fileIn.readline() 
		cnt += 1
	fileIn.close()
	
	result[region] = dic
	return result
	
def handleLine(dic, region, line, dicS, dicMapStreetsRR):
	lst = line.split(";")
	cntElements = len(lst)
	ok = True
	if region == 'B': 
		if cntElements != 7:
			ok = False
	else:
		if cntElements != 6:
			ok = False
			
	if line == '63079;4800;2005;GRAND PLACE;28;Rez;'+'\n': #one issue in the file of Wallonia solved this way (1 ';' too much at the end
		ok = True
		
	if ok:
		if region == 'B':
			#0 NIS; 1 POSTCODE;2 STRAATCODE;3 STRAATNAAM_N; 4 STRAATNAAM_F; 5 HUISNR; 6 INDEX
			idM = lst[0]
			idNISM = lst[2]
			S_N = lst[3]
			S_F = lst[4]
		elif region == 'W':
			idM = lst[0]
			idNISM = lst[2]
			S_N = ""
			S_F = lst[3]
		elif region == 'F':
			idM = lst[0]
			idNISM = lst[2]
			S_N = lst[3]
			S_F = ""
		dic = handleStreet(dic, region, idM, idNISM, S_N, S_F, dicS, dicMapStreetsRR)
	else:
		print(region + "-ISSUE with delimiter ';' in line " + line)
	return dic

def handleStreet(dic, R, idM, idNISM, S_N, S_F, dicS, dicMapStreetsRR):
	#print(R, idM, idNISM, S_N, S_F)
	if not idM in dic.keys():
		dic[idM] = {}
	if not idNISM in dic[idM]:
		dic[idM][idNISM] = {}

		keyRM = R + idM
		
		stringToMatch = ""
		if R == 'F':
			stringToMatch = S_N
		elif R == 'W':
			stringToMatch = S_F
		elif R == 'B':
			if not S_N == "":
				stringToMatch = S_N
			else:
				stringToMatch = S_F

		if (not stringToMatch == "") and (keyRM in dicMapStreetsRR):
			bestMatch, pcS = matchIt(dicMapStreetsRR[keyRM].keys(), stringToMatch)
			#print('matchIt', stringToMatch, bestMatch, dicMapStreetsRR[keyRM][bestMatch], pcS)
			pcS2 = pcS #starting point
			if pcS != 1:
				bestMatch, pcS2 = treatStreetNameMatchFurther(R, keyRM, stringToMatch, bestMatch, pcS, dicMapStreetsRR)
			if pcS2 > THRESHOLD_STREET:
				idS = dicMapStreetsRR[keyRM][bestMatch]
				dic[idM][idNISM]['idS'] = idS
				v = getLastVersion(dicS[R],idS)
				for lan in ['nl', 'fr', 'de']:
					key = 'S' + lan
					if lan in dicS[R][idS][v]:
						dic[idM][idNISM][key] = dicS[R][idS][v][lan]
					else:
						dic[idM][idNISM][key] = ""
				dic[idM][idNISM]['pcS'] = pcS
				dic[idM][idNISM]['pcS2'] = pcS2
	return dic

def treatStreetNameMatchFurther(R, keyRM, S, bestMatch, pcS, dicMapStreetsRR):
	dicStringsNl = {'STWG': 'STEENWEG', 'STWG.': 'STEENWEG', 'ST':'SINT', 'ST. ':'SINT', 'KON':'KONING', 'KON.':'KONING', 'BURG':'BURGEMEESTER', 'BURG.':'BURGEMEESTER ', 'LUIT':'LUITENANT', 'LUIT.':'LUITENANT', 'DR':'DOKTER'}
	dicStringsFr = {'AV':'AVENUE', 'CH':'CHEMIN', 'DR':'DOCTEUR', 'DR.':'DOCTEUR', 'RES':'RESIDENCE', 'RES.':'RESIDENCE', 'RES,':'RESIDENCE', 'BLD':'BOULEVARD', 'ST ':'SAINT', 'ST':'SAINT', 'ST-':'SAINT', 'STE':'SAINTE' \
	                 , 'STE ':'SAINTE', 'STE-':'SAINTE', 'LT.':'LIEUTENANT', 'SQ.': 'SQUARE', 'PL.':'PLACE'}
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
	return bestMatch, pcS
	
def createStreetCodeMappingFile(dicS, dicMapStreetsRR):
	result = {}

	result = createStreetCodeMapping(result, "B", SRC_RR_B_ORI, dicS, dicMapStreetsRR)
	result = createStreetCodeMapping(result, "W", SRC_RR_W_ORI, dicS, dicMapStreetsRR)
	result = createStreetCodeMapping(result, "F", SRC_RR_F_ORI, dicS, dicMapStreetsRR)
	return result	
#CONVERT POSTALINFO=========================================================
def treatPostalInfo(resultList, stateDic, line):
	stateDic["state"] = "PostalInfo found"
	stateDic["idP"] =  ""
	stateDic["vP"] = ""
	stateDic["lan"] = ""
	stateDic["P"] = ""
	return resultList, stateDic
def treatPostalInfoObjectIdentifier(resultList, stateDic, line):
	stateDic["state"] = "objectIdentifier found"
	stateDic["idP"] = getObjectIdentifier(line)
	return resultList, stateDic
def treatPostalInfoVersionIdentifier(resultList, stateDic, line):
	stateDic["state"] = "versionIdentifier found"
	stateDic["vP"] = getVersionIdentifier(line)
	return resultList, stateDic
def treatPostalInfoLanguage(resultList, stateDic, line):
	stateDic["state"] = "language found"
	stateDic["lan"] = getLanguage(line)
	return resultList, stateDic
def treatPostalInfoSpelling(resultList, stateDic, line):
	stateDic["state"] = "spelling found"
	stateDic["P"] = getSpelling(line)
	resultList, stateDic = addPostalInfo(resultList, stateDic)
	return resultList, stateDic
def addPostalInfo(resultList, stateDic):
	dic = {}
	dic["R"] = stateDic["R"]
	dic["idP"] = stateDic["idP"]
	dic["vP"] =  stateDic["vP"]
	dic["lan"] =  stateDic["lan"]
	dic["P"] =  stateDic["P"]
	resultList.append(dic)
	stateDic["lan"] = ""
	stateDic["P"] = ""
	return resultList, stateDic
	
def makeRegionalListPostalInfo(region):
#out: list van dictionaries - elke dictionary heeft volgende keys: "region", "objectIdentifier", "versionIdentifier", "language", "spelling"
#out: list van dictionaries - elke dictionary heeft volgende keys: "R", "idP", "vP", "lan", "P"
	resultList = []
	stateDic = {"state": "", "R": region, "idP": "", "vP": "", "lan": "", "P": ""}

	filename = filenameDic[region]["postalInfo"]
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	lineCounter=0
	line=file.readline()
	while line:
		lineCounter += 1
		if "<tns:PostalInfo" in line:
			resultList, stateDic = treatPostalInfo(resultList, stateDic, line)
		#elif "<com:objectIdentifier>" in line or "<add:objectIdentifier>" in line:
		elif "<com:objectIdentifier>" in line:
			resultList, stateDic = treatPostalInfoObjectIdentifier(resultList, stateDic, line)
		#elif "<com:versionIdentifier>" in line or "<add:versionIdentifier>" in line:
		elif "<com:versionIdentifier>" in line:
			resultList, stateDic = treatPostalInfoVersionIdentifier(resultList, stateDic, line)
		#elif "<com:language>" in line or "<add:language>" in line:
		elif "<com:language>" in line:
			resultList, stateDic = treatPostalInfoLanguage(resultList, stateDic, line)
		#elif "<com:spelling>" in line or "<add:spelling>" in line:
		elif "<com:spelling>" in line:
			resultList, stateDic = treatPostalInfoSpelling(resultList, stateDic, line)
		elif "</tns:PostalInfo" in line and stateDic['state'] == "versionIdentifier found" :
			resultList, stateDic = addPostalInfo(resultList, stateDic)
		line=file.readline()
	#print(filename, ":", "{0:,.0f}".format(lineCounter), "xml lines")
	file.close()
	return resultList	
	
def makeNationalListPostalInfo():
# out: list van dictionaries - elke dictionary heeft volgende keys: "region", "objectIdentifier", "versionIdentifier", "language", "spelling"
# out: list van dictionaries - elke dictionary heeft volgende keys: "R", "idP", "vP", "lan", "P"
	resultList = []
	for region in ["B", "F", "W"]:
		resultList.extend(makeRegionalListPostalInfo(region))
	return resultList

def makeDicPostalInfo(fLstPostalInfo):
#in: filename
#out: dictionary { 'B' : {'7500': {'2': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}, '1': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}}, ...
	result = {}
	file=open(fLstPostalInfo,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	while line:
		dicLine = {}
		dicLine = ast.literal_eval(line)
		R = dicLine['R']
		idP = str(dicLine['idP'])
		vP = dicLine['vP']
		if vP == '':
			vP = '-'
		lan = dicLine['lan']
		P = dicLine['P']
		
		if R not in result:
			dicLan = {}
			dicV = {}
			dicP = {}
			
			if lan != '':
				dicLan[lan] = P
			dicV[vP] = dicLan
			dicP[idP] = dicV
			result[R] = dicP
		elif idP not in result[R]:
			dicLan = {}
			dicV = {}
			
			if lan != '':
				dicLan[lan] = P
			dicV[vP] = dicLan
			result[R][idP] = dicV
		elif vP not in result[R][idP]:
			dicLan = {}
			if lan != '':
				dicLan[lan] = P
			result[R][idP][vP] = dicLan
		elif lan != '':
			result[R][idP][vP][lan] = P

		line=file.readline()
	file.close()
	return result

# def getDicM(fLstPostalInfo):
# #in: filename
# #out: dictionary { 'B' : {'7500': {'2': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}, '1': {'fr': 'Etterbeek', 'nl': 'Etterbeek'}}, ...
	# file=open(fDicMunicipalities,"r", encoding=PREFERRED_ENCODING)
	# dicM = {}
	# line = file.readline()
	# while line:
		# dic = {}
		# dic = ast.literal_eval(line)
		# for k,v in dic.items():
			# dicM[k] = v
		# line = file.readline()
	# file.close()
	# return dicM

def outputPostalInfo(dicP, filename):
	lst = []
	#redirection of output failed due to weird characters e.g. '\x91'

	for keyR,valR in dicP.items():
		R = keyR
		for keyP, valP in valR.items():
			idP = keyP
			v = getLastVersion(dicP[R], keyP)
			if v != '?':
				if 'nl' in valP[v]:
					Pnl = valP[v]['nl']
					lst.append(keyR + ',' + Pnl + ',nl,' + idP)
				if 'fr' in valP[v]:
					Pfr = valP[v]['fr']
					lst.append(keyR + ',' + Pfr + ',fr,' + idP)
				if 'de' in valP[v]:
					Pde = valP[v]['de']
					lst.append(keyR + ',' + Pde + ',de,' + idP)
				if valP[v] == {}:
					lst.append(keyR + ',' + ',' + ',' + idP)
				
	fileOut = open(filename,"w", encoding=PREFERRED_ENCODING)
	for item in sorted(lst):
		fileOut.write(item+'\n')
	fileOut.close()
	
def outputPostcodes(dicP, filename):
	lst = []
	for R, dicR in dicP.items():
		for id, dicId in dicR.items():
			lastVersion = getLastVersion(dicR, id)
			strNl = ''
			strFr = ''
			strDe = ''
			if 'nl' in dicId[lastVersion]:
				strNl = dicId[lastVersion]['nl']
			if 'fr' in dicId[lastVersion]:
				strFr = dicId[lastVersion]['fr']
			if 'de' in dicId[lastVersion]:
				strDe = dicId[lastVersion]['de']
			str = R + '-' + id + '-' + strNl + ',' + strFr + ',' + strDe
			lst.append(str)
	fileOut = open(filename,"w", encoding=PREFERRED_ENCODING)
	for item in sorted(lst):
		fileOut.write(item+'\n')
	fileOut.close()
			
def	saveDicPostalInfoStatistics(dicPostalInfo, filename): 
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	lst = xmlStatistics('postalInfo')
	for item in sorted(lst):
		file.write(item)
	file.write('\n')
	lstB = []
	lstF = []
	lstW = []
	for k, v in dicPostalInfo.items():
		region = k
		for k2, v2 in v.items():
			key = k2
			if region == "F" :
				lstF.append(key)
			elif region == "W" :
				lstW.append(key)
			elif region == "B" :
				lstB.append(key)
	setB = set(lstB)
	setF = set(lstF)
	setW = set(lstW)
	total = len(setB)+len(setF)+len(setW)
	file.write("POSTALINFO statistics" + '\n')
	file.write("number of unique objectIdentifiers:" +  " B:" + str(len(setB)) + " F:" + str(len(setF)) + " W:" + str(len(setW)) + "\n")
	file.write("total number of unique objectIdentifiers: " + str(total) + "\n")
	file.close()

#CONVERT ADDRESS=======================================================================
def treatAddress(resultList, stateDic, line):
	stateDic['state'] = "Address found"
	stateDic['idA'] = ''
	stateDic['vA'] = ''
	stateDic['x'] = ''
	stateDic['y'] = ''
	stateDic['sF'] = ''
	stateDic['st'] = ''
	stateDic['v'] = ''
	stateDic['bx'] = ''
	stateDic['hs'] = ''
	stateDic['oA'] = ''
	stateDic['idM'] = ''
	stateDic['vM'] = ''
	stateDic['idS'] = ''
	stateDic['vS'] = ''
	stateDic['ho'] = ''
	stateDic['idP'] =''
	stateDic['vP'] = ''
	stateDic['idPM'] = ''
	stateDic['vPM'] = ''
	stateDic['Mnl'] = ''
	stateDic['Mfr'] = ''
	stateDic['Mde'] = ''
	stateDic['Snl'] = ''
	stateDic['Sfr'] = ''
	stateDic['Sde'] = ''
	stateDic['Pnl'] = ''
	stateDic['Pfr'] = ''
	stateDic['Pde'] = ''
	stateDic['PMnl'] = ''
	stateDic['PMfr'] = ''
	stateDic['PMde'] = ''
	return resultList, stateDic
	
def treatAddressObjectIdentifier(resultList, stateDic, line):
	objectIdentifier = getObjectIdentifier(line)
	if stateDic['state'] == 'Address found':
		stateDic['idA'] = objectIdentifier
		stateDic['state'] = "ObjectIdentifier Address found"
	elif stateDic['state'] == 'Streetname found':
		stateDic['idS'] = objectIdentifier
		stateDic['state'] = "ObjectIdentifier Streetname found"
	elif stateDic['state'] == 'Municipality found':
		stateDic['idM'] = objectIdentifier
		stateDic['state'] = "ObjectIdentifier Municipality found"
	elif stateDic['state'] == 'PostalInfo found':
		stateDic['idP'] = objectIdentifier
		stateDic['state'] = "ObjectIdentifier PostalInfo found"
	elif stateDic['state'] == 'PartOfMunicipality found':
		stateDic['idPM'] = objectIdentifier
		stateDic['state'] = "ObjectIdentifier PartOfMunicipality found"
	return resultList, stateDic
	
def treatAddressVersionIdentifier(resultList, stateDic, line):
	versionIdentifier = getVersionIdentifier(line)
	if stateDic['state'] == 'ObjectIdentifier Address found':
		stateDic['vA'] = versionIdentifier
		stateDic['state'] = "VersionIdentifier Address found"
	elif stateDic['state'] == 'ObjectIdentifier Streetname found':
		stateDic['vS'] = versionIdentifier
		stateDic['state'] = "VersionIdentifier Streetname found"
	elif stateDic['state'] == 'ObjectIdentifier Municipality found':
		stateDic['vM'] = versionIdentifier
		stateDic['state'] = "VersionIdentifier Municipality found"
	elif stateDic['state'] == 'ObjectIdentifier PostalInfo found':
		stateDic['vP'] = versionIdentifier
		stateDic['state'] = "VersionIdentifier PostalInfo found"
	elif stateDic['state'] == 'ObjectIdentifier PartOfMunicipality found':
		stateDic['vPM'] = versionIdentifier
		stateDic['state'] = "VersionIdentifier PartOfMunicipality found"
	return resultList, stateDic
	
def treatAddressPosition(resultList, stateDic, line):
	stateDic['x'], stateDic['y'] = getAddressPosition(line)
	stateDic['state'] = "Position found"
	return resultList, stateDic
def treatAddressSortField(resultList, stateDic, line):
	stateDic['sF'] = getAddressSortfield(line)
	stateDic['state'] = "Sortfield found"
	return resultList, stateDic
def treatAddressStatus(resultList, stateDic, line):
	stateDic['st'] = getStatus(line)
	stateDic['state'] = "Status found"
	return resultList, stateDic
def treatAddressValidFrom(resultList, stateDic, line):
	stateDic['v'] = getValidFrom(line)
	stateDic['state'] = "Valid from found"
	return resultList, stateDic
def treatAddressBoxNumber(resultList, stateDic, line):
	stateDic['bx'] = getBoxNumber(line)
	stateDic['state'] = "BoxNumber found"
	return resultList, stateDic
def treatAddressHouseNumber(resultList, stateDic, line):
	stateDic['hs'] = getHouseNumber(line)
	stateDic['state'] = "HouseNumber found"
	return resultList, stateDic
def treatAddressOfficiallyAssigned(resultList, stateDic, line):
	stateDic['oA'] = getOfficiallyAssigned(line)
	stateDic['state'] = "OfficiallyAssigned found"
	return resultList, stateDic
def treatAddressHasStreetname(resultList, stateDic, line):
	stateDic['state'] = "Streetname found"
	return resultList, stateDic
def treatAddressHasMunicipality(resultList, stateDic, line):
	stateDic['state'] = "Municipality found"
	return resultList, stateDic
def treatAddressHasPostalInfo(resultList, stateDic, line):
	stateDic['state'] = "PostalInfo found"
	return resultList, stateDic
def treatAddressPartOfMunicipality(resultList, stateDic, line):
	stateDic['state'] = "PartOfMunicipality found"
	return resultList, stateDic
def addAddress(resultList, stateDic, dicM, dicS, dicPM, dicP, line):
	stateDic['state'] = "Address end found"
	dic = {}
	region = stateDic['R']
	dic['R'] = region
	dic['idA'] = stateDic['idA']
	dic['vA'] = stateDic['vA']
	dic['x'] = stateDic['x']
	dic['y'] = stateDic['y']
	dic['sF'] = stateDic['sF']
	dic['st'] = stateDic['st']
	dic['v'] = stateDic['v']
	dic['bx'] = stateDic['bx']
	dic['hs'] = stateDic['hs']
	dic['oA'] = stateDic['oA']
	idM = stateDic['idM']
	dic['idM'] = idM
	dic['vM'] = stateDic['vM']
	idS = stateDic['idS']
	dic['idS'] = idS
	dic['vS'] = stateDic['vS']
	idP = stateDic['idP']
	dic['idP'] = idP
	dic['vP'] = stateDic['vP']
	idPM = stateDic['idPM']
	dic['idPM'] = idPM
	dic['vPM'] = stateDic['vPM']
	for lan in ['nl', 'fr', 'de']:
		try:
			vM = getLastVersion(dicM[region], idM)
		except:
			vM = '?'
		try:
			vS = getLastVersion(dicS[region], idS)
		except:
			vS = '?'
		try:
			vPM = getLastVersion(dicPM[region], idPM)
		except:
			vPM = '?'
		try:
			vP = getLastVersion(dicP[region], idP)
		except:
			vP = '?'
			
		Mlan = 'M'+lan
		if vM == '?':
			dic[Mlan] = 'vM ISSUE ' + region + ', ' + idM
		elif lan in dicM[region][idM][vM]:
			dic[Mlan] = dicM[region][idM][vM][lan]
		else:
			dic[Mlan] = ''
			
		Slan = 'S'+lan		
		if vS == '?':
			dic[Slan] = 'vS ISSUE ' + region + ', ' + idS
			dic['ho'] = ''
		elif lan in dicS[region][idS][vS]:
			dic[Slan] = dicS[region][idS][vS][lan]
			dic['ho'] = dicS[region][idS][vS]['ho']
		else:
			dic[Slan] = ''

		PMlan = 'PM'+lan
		if region == 'W':	#PartOfMunicipality only exists in Wallonia
			if vPM == '?':
				dic[PMlan] = 'vPM ISSUE ' + region + ', ' + idPM
			elif lan in dicPM[region][idPM][vPM]:
				dic[PMlan] = dicPM[region][idPM][vPM][lan]
			else:
				dic[PMlan] = ''
		else:
			dic[PMlan] = ''
			
		Plan = 'P'+lan		
		if vP == '?':
			dic[Plan] = 'vP ISSUE ' + region + ', ' + idP
		elif lan in dicP[region][idP][vP]:
			dic[Plan] = dicP[region][idP][vP][lan]
		else:
			dic[Plan] = ''
	#printDic(dic)	
	if dic['st'] not in ['archived', 'Archived', 'retired', 'Retired']:
		resultList.append(dic)
	else:
		print(dic['st'], dic['R'], dic['idA'], dic['vA'])
	return resultList, stateDic

def makeRegionalListAddresses(region,dicM, dicS, dicPM, dicP):
#out: list of dictionaries; each dictionary with keys 
# 'region', 'objectIdentifier', 'versionIdentifier', 'addressPositionX', 'addressPositionY',\
# 'addressSortField', 'status', 'validFrom', 'boxNumber', 'houseNumber', 'officiallyAssigned', \
# 'objectIdentifierMunicipality', 'versionIdentifierMunicipality', 'municipality', 'objectIdentifierStreet', 'versionIdentifierStreet', 'homonymAddition', 'streetName', \
# 'objectIdentifierPostalInfo', 'versionIdentifierPostalInfo', 'objectIdentifierPartOfMunicipality', 'versionIdentifierPartOfMunicipality', 'lan'
	resultList = []
	stateDic = {'state': '', 'R': region, 'idA': '', 'vA': '', 'x': '', 'y':'', 'sF': '', 'st':'', 'v':'', 'bx':'', 'hs':'', 'oA':'', \
	            'idM':'', 'vM':'', 'Mnl':'', 'Mfr':'', 'Mde':'', \
	            'idS':'', 'vS':'', 'ho': '','Snl':'', 'Sfr':'', 'Sde':'', \
				'idP':'', 'vP':'','Pnl':'', 'Pfr':'', 'Pde':'', \
				'idPM':'', 'vPM':'','PMnl':'', 'PMfr':'', 'PMde':''}

	filename = filenameDic[region]['address']
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	lineCounter=0
	while line:
	#while line and lineCounter < 1000: #testing purpose
		if "<tns:Address" in line:
			resultList, stateDic = treatAddress(resultList, stateDic, line)
		elif "<com:objectIdentifier>" in line or "<add:objectIdentifier>" in line:
			resultList, stateDic = treatAddressObjectIdentifier(resultList, stateDic, line)
		elif "<com:versionIdentifier>" in line or "<add:versionIdentifier>" in line:
			resultList, stateDic = treatAddressVersionIdentifier(resultList, stateDic, line)
		elif "<gml:pos" in line:
			resultList, stateDic = treatAddressPosition(resultList, stateDic, line)
		elif "<com:addressSortField>" in line or "<add:addressSortField>" in line:
			resultList, stateDic = treatAddressSortField(resultList, stateDic, line)
		elif "<com:status>" in line or "<add:status>" in line:
			resultList, stateDic = treatAddressStatus(resultList, stateDic, line)
		elif "<com:validFrom>" in line or "<add:validFrom>" in line:
			resultList, stateDic = treatAddressValidFrom(resultList, stateDic, line)
		elif "<com:boxNumber>" in line or "<add:boxNumber>" in line:
			resultList, stateDic = treatAddressBoxNumber(resultList, stateDic, line)
		elif "<com:houseNumber>" in line or "<add:houseNumber>" in line:
			resultList, stateDic = treatAddressHouseNumber(resultList, stateDic, line)
		elif "<com:officiallyAssigned>" in line or "<add:officiallyAssigned>" in line:
			resultList, stateDic = treatAddressOfficiallyAssigned(resultList, stateDic, line)
		elif "<com:hasStreetname>" in line or "<add:hasStreetname>" in line:
			resultList, stateDic = treatAddressHasStreetname(resultList, stateDic, line)
		elif "<com:hasMunicipality>" in line or "<add:hasMunicipality>" in line:
			resultList, stateDic = treatAddressHasMunicipality(resultList, stateDic, line)
		elif "<com:hasPostalInfo>" in line or "<add:hasPostalInfo>" in line:
			resultList, stateDic = treatAddressHasPostalInfo(resultList, stateDic, line)
		elif "<com:PartOfMunicipality>" in line or "<add:PartOfMunicipality>" in line:
			resultList, stateDic = treatAddressPartOfMunicipality(resultList, stateDic, line)
		elif "</tns:Address" in line:
			resultList, stateDic = addAddress(resultList, stateDic, dicM, dicS, dicPM, dicP, line)
		line=file.readline()
		lineCounter += 1
	#print(filename, ':', '{0:,.0f}'.format(lineCounter), 'xml lines')
	file.close()
	return resultList	

def makeNationalListAddresses(dicM, dicS, dicPM, dicP):
#out: {'R': region, 'idA': '', 'vA': '', 'x': '', 'y':'', 'sF': '', 'st':'', 'v':'', 'bx':'', 'hs':'', 'oA':'', 
#      'idM':'', 'vM':'', 'M':'', 'idS':'', 'vS':'','ho':'', 'S':'', 'idP':'', 'vP':'', 'idPM':'', 'vPM':'', 'lan'}
	resultList = []
	for region in ['B', 'F', 'W']:
		resultList.extend(makeRegionalListAddresses(region,dicM, dicS, dicPM, dicP))
	return resultList

def makeDicAddresses(filename):
#out: dictionary {R:{idA:{vA:{'x':'', 'y':'', 'sF':'', 'st':'', 'v':'', 'bx':'', 'hs':'', 'oA':'', \
#                    'idM':'', 'vM', 'idS':'', 'vS', 'ho', 'idPM':'', 'vPM', 'idP':'', 'vP', \
#                    'Mnl':'', 'Snl':'', 'PMnl':'', 'Pnl':'','Mfr':'', 'Sfr':'', 'PMfr':'', 'Pfr':'','Mde':'', 'Sde':'', 'PMde':'', 'Pde':'' }}}}
 
	result = {}
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	line=file.readline()
	cnt = 0
	while line:
		dicLine = {}
		dicLine = ast.literal_eval(line)
		R = dicLine['R']
		idA = dicLine['idA']
		vA = dicLine['vA']
		if vA == '':
			vA = '-'
		idM = dicLine['idM']
		idS = dicLine['idS']
		idPM = dicLine['idPM']
		idP = dicLine['idP']
		vM = dicLine['vM']
		vS = dicLine['vS']
		vPM = dicLine['vPM']
		vP = dicLine['vP']
		ho = dicLine['ho']
		x = dicLine['x']
		y = dicLine['y']
		sF = dicLine['sF']
		st = dicLine['st']
		v = dicLine['v']
		bx = dicLine['bx']
		hs = dicLine['hs']
		oA = dicLine['oA']
		
		Mnl = dicLine['Mnl']
		Mfr = dicLine['Mfr']
		Mde = dicLine['Mde']
		
		Snl = dicLine['Snl']
		Sfr = dicLine['Sfr']
		Sde = dicLine['Sde']
		
		PMnl = dicLine['PMnl']
		PMfr = dicLine['PMfr']
		PMde = dicLine['PMde']
		
		Pnl = dicLine['Pnl']
		Pfr = dicLine['Pfr']
		Pde = dicLine['Pde']
			
		dicV = {}
		dicV['idM'] = idM
		dicV['idS'] = idS
		dicV['idPM'] = idPM
		dicV['idP'] = idP
		dicV['vM'] = vM
		dicV['vS'] = vS
		dicV['vPM'] = vPM
		dicV['vP'] = vP
		dicV['x'] = x
		dicV['ho'] = ho
		dicV['y'] = y
		dicV['sF'] = sF
		dicV['st'] = st
		dicV['v'] = v
		dicV['bx'] = bx
		dicV['hs'] = hs
		dicV['oA'] = oA
		dicV['Mnl'] = Mnl
		dicV['Mfr'] = Mfr
		dicV['Mde'] = Mde
		dicV['Snl'] = Snl
		dicV['Sfr'] = Sfr
		dicV['Sde'] = Sde
		dicV['PMnl'] = PMnl
		dicV['PMfr'] = PMfr
		dicV['PMde'] = PMde
		dicV['Pnl'] = Pnl
		dicV['Pfr'] = Pfr
		dicV['Pde'] = Pde
		
		if R not in result:
			dicA = {}
			dicR = {}
		
			dicA[vA] = dicV
			dicR[idA] = dicA
			result[R] = dicR
			
		elif idA not in result[R]:
			dicA = {}
			
			dicA[vA] = dicV
			result[R][idA] = dicA
			
		elif vA not in result[R][idA]:
			result[R][idA][vA] = dicV
		
		else:							#anomaly: more than 1 address with the same R-idA-vA !!!
			cnt+=1
			vA = vA + 'µ' + str(cnt)
			result[R][idA][vA] = dicV
			
		line=file.readline()
	file.close()
	return result

def makeExtractFractionFromDic(dicOri, dividor, filename):
	dic = {}
	i=0
	for k,v in dicOri.items():
		if i % dividor == 0:
			dic[k]=v
		i += 1
	saveDic(dic, filename)		

#CREATE EXTRACT FROM ADDRESS-DIC (TEST PURPOSE: ALL ADRESSES OF A GIVEN STREET) =====
def makeExtractAddressesOfStreet(dicA, R, idM, idS):
	dic = {}
	dic[R] = {}
	for aCode, dicAddress in dicA[R].items():
		first = True
		for v, dicV in dicAddress.items():
			if dicA[R][aCode][v]['idM'] == idM and dicA[R][aCode][v]['idS'] == idS:
				if first:
					dic[R][aCode] = {}
					first = False
				dic[R][aCode][v] = dicV
	filename = "Adresses_" +R + "_" + idM + "_" + idS + ".txt"
	saveDic(dic, filename)

def makeDicsFromDicA(dicA, dicM, dicPM, dicP, fMToP, fMtoPM, fPtoM, fPtoPM, fPMtoM, fPMtoP):
	cnt= 0 
	lst = []
	for R, dicR in dicA.items():
		for objId, dicId in dicR.items():
			maxVersion = getLastVersion(dicR, objId)
			if maxVersion != '?':
				strM = makeString(getDataLastVersion(R, dicId[maxVersion]['idM'], dicM))
				strPM = makeString(getDataLastVersion(R, dicId[maxVersion]['idPM'], dicPM))
				strP = makeString(getDataLastVersion(R, dicId[maxVersion]['idP'], dicP))
				triple = (strM, strPM, strP)
				if triple not in lst:
					lst.append(triple)
			else:
				print('makeDicsFromDicA - VERSION ISSUE ', R,objId, dicId)
	dic_M_PM = {}
	dic_M_P = {}
	dic_PM_M = {}
	dic_PM_P = {}
	dic_P_M = {}
	dic_P_PM = {}
	for triple in lst:
		m, pm, p = triple
		
		if m not in dic_M_PM:
			dic_M_PM[m] = []
		if pm not in dic_M_PM[m]:
			dic_M_PM[m].append(pm)
		
		if m not in dic_M_P:
			dic_M_P[m] = []
		if p not in dic_M_P[m]:
			dic_M_P[m].append(p)
		
		if pm not in dic_PM_M:
			dic_PM_M[pm] = []
		if m not in dic_PM_M[pm]:
			dic_PM_M[pm].append(m)
		
		if pm not in dic_PM_P:
			dic_PM_P[pm] = []
		if p not in dic_PM_P[pm]:
			dic_PM_P[pm].append(p)
		
		if p not in dic_P_M:
			dic_P_M[p] = []
		if m not in dic_P_M[p]:
			dic_P_M[p].append(m)
		
		if p not in dic_P_PM:
			dic_P_PM[p] = []
		if pm not in dic_P_PM[p]:
			dic_P_PM[p].append(pm)
	saveDic(dic_M_PM, fMtoPM)
	saveDic(dic_M_P, fMToP)
	saveDic(dic_PM_M, fPMtoM)
	saveDic(dic_PM_P, fPMtoP)
	saveDic(dic_P_M, fPtoM)
	saveDic(dic_P_PM, fPtoPM)

def storeDicA(dicA):
#out: dictionary {R+'-'+idA:{vA:{'x':'', 'y':'', 'sF':'', 'st':'', 'v':'', 'bx':'', 'hs':'', 'oA':'', 'idM':'', 'vM', 'idS':'', 'vS', 'idPM':'', 'vPM', 'idP':'', 'vP', 'Mnl':'', 'Snl':'', 'Mfr':'', 'Sfr':'', 'Mde':'', 'Sde':'' }}} => 1 line per R+'-'+idA
	for region in ['B', 'F', 'W']:
		if region in dicA:
			for k, v in dicA[region].items():
				key = "{'"+region+'-'+k+"':"
				print(key, dicA[region][k],'}')

def getSlowDicA(fDicAddresses):
#full address dictionary is returned
	file=open(fDicAddresses,"r", encoding=PREFERRED_ENCODING)
	dicA = {}
	line = file.readline()
	lineCounter = 1
	#while line and lineCounter < 100:
	while line:
		#print(line)
		#dic = {}
		#dic = ast.literal_eval(line)
		pos = line.find(':') - 1 
		R, idA = line[2:pos].split('-')
		if R not in dicA:
			dicA[R] = {}
			dicA[R][idA] = ast.literal_eval(line[pos+3:-3])
		elif idA not in dicA[R]:
			dicA[R][idA] = ast.literal_eval(line[pos+3:-3])
		line = file.readline()
		lineCounter +=1
	file.close()
	return dicA
	
def getFastDicA(fDicAddresses):
#partial data dictionary is returned:  R > idA (number part) > string containing {'version':{key:val, key:val, ..}}
	file=open(fDicAddresses,"r", encoding=PREFERRED_ENCODING)
	dicA = {}
	line = file.readline()
	#lineCounter = 1
	# while line and lineCounter < 100:
	while line:
		#print(line)
		#dic = {}
		#dic = ast.literal_eval(line)
		pos = line.find(':') - 1 
		R, idA = line[2:pos].split('-')
		if R not in dicA:
			dicA[R] = {}
			dicA[R][idA] = line[pos+3:-3]
		elif idA not in dicA[R]:
			dicA[R][idA] = line[pos+3:-3]
		line = file.readline()
		#lineCounter +=1
	file.close()
	return dicA

def	saveDicAddressesStatistics(dicA, filename):
	file = open(filename,"w", encoding=PREFERRED_ENCODING)
	lst = xmlStatistics('address')
	for item in sorted(lst):
		file.write(item)
	file.write('\n')
	lB = 0
	lF = 0
	lW = 0
	if 'B' in dicA:
		lB = len(dicA['B'])
	if 'F' in dicA:
		lF = len(dicA['F'])
	if 'W' in dicA:
		lW = len(dicA['W'])
	file.write('ADDRESS statistics' + '\n')
	file.write('number of unique objectIdentifiers: '+  'B:' + '{0:,.0f}'.format(lB) + ' F:' + '{0:,.0f}'.format(lF) + ' W:' + '{0:,.0f}'.format(lW) + '\n')
	file.write('total number of unique objectIdentifiers: '+  '{0:,.0f}'.format(lB+lF+lW))
	file.close()
	
def mapAddress(dicA, region, municipality, streetName, houseNumber, boxNumber):
	print('mapping..')
	if region:
		for idA, str in dicA[region].items():
			dic = {}
			if municipality  and (municipality in str):
				if streetName and (streetName in str):
					if houseNumber and (houseNumber in str):
						if boxNumber:
							if boxNumber in str:
								dic = ast.literal_eval(str)
								for k, v in dic.items():
									if houseNumber == v['hs'] and boxNumber == v['bx']:
										print('EXACT MATCH', region, idA, str)
										break
						else:
							dic = ast.literal_eval(str)
							for k, v in dic.items():
								if houseNumber == v['hs'] and v['bx'] == '':
									print('EXACT MATCH', region, idA, str)
def getPartOfMunicipalities(filename):
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	dicPM = {}
	line = file.readline()
	while line:
		if "'idPM': ''" not in line:
			dic = {}
			dic = ast.literal_eval(line)
			for RidA, dicIdA in dic.items():
				for ver, dicV in dicIdA.items():
					R = RidA[0:1]
					idM = R + '-' + dicV['idM']
					idPM = dicV['idPM']
					if idM not in dicPM:
						dicPM[idM] = []
						dicPM[idM].append(idPM)
					else:
						if idPM not in dicPM[idM]:
							dicPM[idM].append(idPM)	
		line = file.readline()
	file.close()
	return dicPM

def getNumbers(filename):
	file=open(fLstAddresses,"r", encoding=PREFERRED_ENCODING)
	lstBx = []
	lstHs = []
	lstHsBx = []
	i=0
	line = file.readline()
	while line:
		dic = {}
		dic = ast.literal_eval(line)
		R = dic['R']
		hs = dic['hs']
		bx = dic['bx']
		hsbx = hs + '_' + bx
		lstBx.append(R + '_' + bx)
		lstHs.append(R + '_' + hs)
		lstHsBx.append(R + '_' + hsbx)
		line = file.readline()
		i +=1
	file.close()
	sBx = sorted(set(lstBx))
	sHs = sorted(set(lstHs))
	sHsBx = sorted(set(lstHsBx))
	return sHs, sBx, sHsBx

def createMappingFileNumbers(dicA):
# Map house- and boxnumbers
# Out: dictionary {key1:{key2:idA}} where 
     # key1 = R + idS
     # key2 = hs + '__' + bx
     # value = idA
	dic = {}
	for R, dicR in dicA.items():
		for A, dicAddress in dicR.items():
			V = getLastVersion(dicR, A)
			idM = dicAddress[V]['idM']
			idS = dicAddress[V]['idS']
			hs = dicAddress[V]['hs']
			bx = dicAddress[V]['bx']
			key1 = R + idS
			key2 = hs + '__' + bx
			if key1 not in dic.keys():
				dic[key1] = {}
				dic[key1][key2] = R + '_' + A + '_' + V
			else:
				dic[key1][key2] = R + '_' + A + '_' + V
	return dic

def getFrontNumPart(s):
	l = len(s)
	ix = 0
	for i in range(l):
		if s[i].isdigit():
			ix +=1
		else:
			break
	if ix == 0:
		return ""
	else:
		return s[:ix]
def dropFrontNumPart(s):
	l = len(s)
	ix = l
	for i in range(l):
		if not s[i].isdigit():
			ix = i
			break
	if ix == l:
		return ""
	else:
		return s[ix:]
		
def transformHouseNr(hs):
#remove leading zeroes from house numbers starting with at least 2 digits (exception '0')
	hsTransformed = hs
	if len(hs) > 1:
		if hs[0] == '0' and hs[1].isdigit():
			hsTransformed = removeLeadingZeroes(hs)
			if hsTransformed == "":		#case hs = '000'
				hsTransformed = '0'
	return hsTransformed
	
def createMappingFileHouseNrs(dicMapA, isForRR):
# RR: houseNr is always a string with 1 to 4 digits
# BEST: hs_bx = 9A__1, 9B_1
# a RR housenNr will be mapped on nr 9 (the RR box number will later be mapped on A1)
# a non RR houseNr will be mapped on 9A (the box number will later be mapped on 1) 
	dic = {}
	for RidS, dicRidS in dicMapA.items():
		dic[RidS] = {}
		for hsBx, idA in dicRidS.items():
			lst = hsBx.split('__')
			hs = lst[0]
			hsTransformed = transformHouseNr(hs)
			if not isForRR:
				dic[RidS][hsTransformed] = hs					#in the example: '9A':'9A'
			else:
				numPartHs = getFrontNumPart(hsTransformed)
				if numPartHs != "":	
					if numPartHs not in dic[RidS]:
						lstHouseNrs = []
						lstHouseNrs.append(hs)
						dic[RidS][numPartHs] = lstHouseNrs	#in the example: '9':['9A']
					else:
						lstHouseNrs = list(dic[RidS][numPartHs])	#in the example: '9':['9A', '9B']
						if not hs in lstHouseNrs:
							lstHouseNrs.append(hs)
							dic[RidS][numPartHs] = lstHouseNrs
	return dic

def getBoxType(bx):
#result in ['X-n1', 'X-n2', ..,'Xn1', 'Xn2', .., 'nV',  'OTHR']
	result = ''
	cntAlpha, cntDigit = cntCharType(bx)
	if len(bx) > 2:
		if bx[0].isalpha() and bx[1] == "-" and bx[2].isdigit():
			return "X-n" + str(cntDigit)
	if len(bx) > 0:
		if cntAlpha == 1 and bx[0].isalpha():
			result = "Xn" + str(cntDigit)
			return result
	if len(bx) == 2:
		if bx[0].isdigit() and bx[1] == 'V':
			return 'nV'
	return "OTHR"
	
def transformBoxNr(bx):
#remove leading zeroes (exception: '0' and '0.0')
#uppercase
#convert '/' and '_' to '-'
#remove spaces
#remove leading 'BUS, 'BU', 'BTE'
#drop '.0' at the end
#remove '-' from bxType 'X-'
#remove leading zeroes after X in boxType 'X..'
#remove 'V' from boxType 'nV'
#again, remove leading zeroes (exception: '0')

	bxTransformed = bx
	if bxTransformed not in ['0', '0.0']:
		bxTransformed = removeLeadingZeroes(bx)
	bxTransformed = bxTransformed.upper()
	bxTransformed = bxTransformed.replace('/', '-')
	bxTransformed = bxTransformed.replace('_', '-')
	bxTransformed = bxTransformed.replace(' ', '')
	bxTransformed = removeLeadingSubstring('BUS', bxTransformed)
	bxTransformed = removeLeadingSubstring('BU', bxTransformed)
	bxTransformed = removeLeadingSubstring('BTE', bxTransformed)
	
	if bxTransformed[-2:] == '.0':			#drop '.0' at the end
		bxTransformed = bxTransformed[:-2]
	
	bxType = getBoxType(bxTransformed)
	#result in ['X-n1', 'X-n2', ..,'Xn1', 'Xn2', .., 'nV'  'OTHR']
	if bxType[0:2] == "X-":	
		bxTransformed = bxTransformed[0] + bxTransformed[2:]	#remove "-"
	if bxType[0] == "X":
		bxTransformed = bxTransformed[0] + removeLeadingZeroes(bxTransformed[1:])
	if bxType == 'nV':
		bxTransformed = bxTransformed[0]


	if bxTransformed != '0':								#cfr box number in BEST 0.0
		bxTransformed = removeLeadingZeroes(bxTransformed)

	return bxTransformed

def createMappingFileBoxNrs(dicMapA, isForRR):
# RR: houseNr is always a string with 1 to 4 digits
# BEST: hs_bx = 9A__1, 9B_1
# a RR housenNr will be mapped on nr 9 (the RR box number will later be mapped on A1)
# a non RR houseNr will be mapped on 9A (the box number will later be mapped on 1) 
	dic = {}
	for RidS, dicRidS in dicMapA.items():
		for hsBx, idA in dicRidS.items():
			lst = hsBx.split('__')
			hs = lst[0]
			bx = lst[1]
			hsTransformed = transformHouseNr(hs)
			bxTransformed = transformBoxNr(bx)
			
			key = RidS + "_" + hsTransformed
			if key not in dic:
				dic[key] = {}
			if not isForRR:
				bxKey = bxTransformed
				if bxKey == "":
					bxKey = "NO BOX NR"
				if bxKey not in dic[key]:
					dic[key][bxKey] = {}
				dic[key][bxKey]['idA'] = idA	#default + for non RR	in the example: ..9A':{'1':{'idA':..., 'bx': '1'}
				dic[key][bxKey]['bx'] = bx		#default + for non RR
			else:
				numPartHs = getFrontNumPart(hsTransformed)
				nonNumPartHs = dropFrontNumPart(hsTransformed)
				bxNew = nonNumPartHs + bxTransformed
				if numPartHs != "":		#always the case: house number normally starts with a numeric part
					if bxNew == "":
						bxNew = "NO BOX NR"
					if bxNew not in dic[key]:
						dic[key][bxNew] = {}
					dic[key][bxNew]['idA'] = idA	#in the example: ..9A':{'A1':{'idA':..., 'bx': '1'}
					dic[key][bxNew]['bx'] = bx
	return dic	
#=== MAPPING RESULT TO MUNICIPALITYFILES=======================================================================================
def treatAddressForMunicipalityFiles(dic, filewriter):
	key = dic['key']
	idM_SRC = dic['idM_SRC']
	M = dic['M']
	Mnl = dic['Mnl']
	Mfr = dic['Mfr']
	Mde = dic['Mde']
	M2 = dic['M2']
	P = dic['P']
	idS_SRC = dic['idS_SRC']
	S = dic['S']
	Snl = dic['Snl']
	Sfr = dic['Sfr']
	Sde = dic['Sde']
	S2 = dic['S2']
	hs = dic['hs']
	hs2 = dic['hs2']
	bx = dic['bx']
	bx2 = str(dic['bx2'])
	pc = str(dic['pc']).replace('.',',')
	pc2 = str(dic['pc2']).replace('.',',')
	pcM = str(dic['pcM']).replace('.',',')
	pcS = str(dic['pcS']).replace('.',',')
	pcS2 = str(dic['pcS2']).replace('.',',')
	pcHs = str(dic['pcHs']).replace('.',',')
	pcBx = str(dic['pcBx']).replace('.',',')
	idA = dic['idA']
	idM = dic['idM']
	idS = dic['idS']
	warningA = dic['warningA']
	warningB = dic['warningB']
	warningC = dic['warningC']
	warningD = dic['warningD']
	action = dic['action']
	R = dic['R']
	try:
			filewriter.writerow([key,idM_SRC, M, Mnl, Mfr, Mde, M2, P, idS_SRC, S, Snl, Sfr,Sde, S2, hs, hs2, bx, bx2, pc, pc2, pcM, pcS, pcS2, pcHs, pcBx, idA,idM,idS, R, warningA, warningB, warningC, warningD, action])
	except:
		print("ISSUE writing address to csv", dic)
		
def createMunicipalityFiles(inputFile, enc):
	start = datetime.datetime.now()

	dicCsvFile = {}
	fileIn = open(inputFile,"r", encoding=PREFERRED_ENCODING)
	line = fileIn.readline()
	cnt = 1
	while line:
		if cnt % 100000 == 0:
			print('line', cnt)
		issue = False
		
		try:
			dic = ast.literal_eval(line)
		except:
			print("ast.literal_eval", line)
			issue = True
		if not issue:
			region = dic['R'] 										#!!!!!
			idM = dic['idM']
			municipalityName = dic['M2']
			key = region + municipalityName
			if key not in dicCsvFile.keys():
				print(key)
				csvFileName = '_' + region + '-' + municipalityName + '.csv'
				csvfile = open(csvFileName, 'w', newline='', encoding=enc)
				filewriter = csv.writer(csvfile, delimiter=DELIMITER, quotechar='|', quoting=csv.QUOTE_MINIMAL)
				dicCsvFile[key] = {'filewriter':filewriter, 'filename': csvFileName, 'csvFile': csvfile}
				filewriter.writerow(['key','idM_SRC', 'M', 'Mnl', 'Mfr', 'Mde', 'M2', 'P', 'idS_SRC', 'S', 'Snl','Sfr','Sde', 'S2', 'hs', 'hs2', 'bx', 'bx2', \
				'pc', 'pc2', 'pcM', 'pcS', 'pcS2', 'pcHs', 'pcBx', 'idA','idM','idS', 'R', 'warningA', 'warningB', 'warningC', 'warningD', 'action'])
			else:
				filewriter = dicCsvFile[key]['filewriter']
			treatAddressForMunicipalityFiles(dic, filewriter)
		line = fileIn.readline() 
		cnt += 1
	fileIn.close()
	for key in dicCsvFile.keys():
		csvFile = dicCsvFile[key]['csvFile']
		csvFile.close()

	end = datetime.datetime.now()
	print("start: ", start)
	print("end: ", end)
	print("duration:", end-start)
#MAPPING ===================================================================================================================
def storeMap(dic, filename):
	file=open(filename,"w", encoding=PREFERRED_ENCODING)
	for k in sorted(dic.keys()):
		if isinstance(dic[k], dict):
			file.write('{ "' + k + '" : { ')
			first = True
			for k2 in sorted(dic[k].keys()):
				if not first:
					file.write(', ')
				#streetname "Wijk "De Stad""
				s = k2
				if '"' in s:
					s = s.replace('"', "'")
					#print(s)
				file.write('"' + s + '" : "' + dic[k][k2] + '"')
				first = False
			file.write(' } }\n')
		else:	
			file.write('{ "' + k + '" : ' + str(dic[k]) + ' }\n')
	file.close()

	
def getDicMap(fileName):
#in: filename
	file=open(fileName,"r", encoding=PREFERRED_ENCODING)
	result = {}
	line = file.readline()
	while line:
		#print(line)
		dic = {}
		dic = ast.literal_eval(line)
		for k,v in dic.items():
			result[k] = v
		line = file.readline()
	file.close()
	return result

#MAPPING===========================================================================	
def createAddressDicToMap(key, idM_SRC, M, Mnl, Mfr, Mde, P, idS_SRC, S, Snl, Sfr, Sde, hs, bx):
#function used while preparing an input file for the mapping
	sep = '","'
	result = '{"key":"' + key + sep				\
	        + 'idM_SRC":"' + idM_SRC + sep		\
			+ 'M":"' + M + sep					\
			+ 'Mnl":"' + Mnl + sep				\
			+ 'Mfr":"' + Mfr + sep				\
			+ 'Mde":"' + Mde + sep				\
			+ 'P":"' + P + sep					\
			+ 'idS_SRC":"' + idS_SRC + sep		\
			+ 'S":"' + S + sep					\
			+ 'Snl":"' + Snl + sep				\
			+ 'Sfr":"' + Sfr + sep				\
			+ 'Sde":"' + Sde + sep				\
			+ 'hs":"' + hs + sep				\
			+ 'bx":"' + bx + sep				\
			+ 'warningA":"' + sep				\
			+ 'warningB":"' + sep				\
			+ 'warningC":"' + sep				\
			+ 'warningD":"' + sep				\
			+ 'action":"' + 'tbd' + '"}\n'
	return result

def mapDb(src, inputFile, outputFile):
	#fileIn=open(inputFile,"r", encoding=PREFERRED_ENCODING, errors='ignore') #errors='ignore' as issue with xFC characters from latin-1 encoded RR fil
	fileIn=open(inputFile,"r", encoding=PREFERRED_ENCODING)  #modified for FOD ECO
	fileOut=open(outputFile,"w", encoding=PREFERRED_ENCODING)
	
	print('initializing ..')
	dicM = getDic(fDicMunicipalities) 
	dicS = getDic(fDicStreets)
	dicMapMunToR = getDic(fMapMunToR) 
	
	if src in ["RR_B", "RR_W", "RR_F"]:
		dicMapStreets = getDic(fMapStreetnamesRR) #only capital letters in RR
	else:	#default
		dicMapStreets = getDic(fMapStreetnames)
		
	if src == "POL":
		dicMapStreetCode_SRCtoBEST = getDic(fMapStreetCode_POLtoBEST)
	elif src == "AAPD":
		dicMapStreetCode_SRCtoBEST = getDic(fMapStreetCode_AAPDtoBEST)
	else:	#default
		dicMapStreetCode_SRCtoBEST = getDic(fMapStreetCode_RRtoBEST)
		
	if src in ["RR_B", "RR_W", "RR_F"]:
		dicMapHouseNumbers = getDic(fMapHouseNrsRR) #key = initial numeric part of BEST house number
		dicMapBoxNumbers = getDic(fMapBoxNrsRR)		#key = final part after numeric part of BEST house number + BEST box number
	else:
		dicMapHouseNumbers = getDic(fMapHouseNrs)	#key = BEST house number
		dicMapBoxNumbers = getDic(fMapBoxNrs)		#key = BEST box number

	print('mapping ..')
	counters = {'cntAll':0, 'cntOk':0, 'cntA1':0, 'cntA2':0, 'cntB1':0, 'cntB2':0, 'cntC1': 0,'cntC2': 0,'cntD1': 0,'cntD2': 0,'cntD3': 0} 
	address = fileIn.readline()
	while address:
		counters['cntAll'] += 1
		#if counters['cntAll'] > 1000:
		dicAddress = {}
		dicAddress = ast.literal_eval(address.strip())

		dicAddress, counters = getRegionMunicipality(dicAddress, counters, dicM, dicMapMunToR)
		dicAddress, counters = getStreet(dicAddress, counters, dicMapStreets, dicMapStreetCode_SRCtoBEST, dicS)
		dicAddress, counters = getHouseNr(dicAddress, counters, dicMapHouseNumbers, src)
		dicAddress, counters = getBoxNr(dicAddress, counters, dicMapBoxNumbers, src)
		handleDicAddress(fileOut, dicAddress)

		#screen output
		if counters['cntAll'] % 10000 == 0:
			print(address.strip())

		try:
			address = fileIn.readline()
		except:
			print("SERIOUS ERROR at line ", counters['cntAll'])
			break
		# #test
		# if counters['cntAll'] >20000:
			# break
	fileIn.close()
	fileOut.close()

	#screen output
	print(inputFile)
	for k,v in counters.items():
		print(k)
	for k,v in counters.items():
		print(v)

def handleDicAddress(fileOut, dicAddress):
#final structure: 
	fileOut.write('{ ')
	first = True
	for k, v in dicAddress.items():
		if not first:
			fileOut.write(' , ')
		fileOut.write('"' + k + '" : "' + str(v) + '"')
		first = False
	fileOut.write(' }\n')

def isAlreadyCounted(dicAddress, warningType):
	highestWarningType = ""
	if dicAddress['warningA'] != "":
		highestWarningType = "A"
	elif dicAddress['warningB'] != "":
		highestWarningType = "B"
	elif dicAddress['warningC'] != "":
		highestWarningType = "C"
	elif dicAddress['warningD'] != "":
		highestWarningType = "D"
		
	if highestWarningType == "": 	#we have now the first warning => isAlreadyCounted should be False
		return False
	else:							#there was already a warning => isAlreadyCounted should be True
		return True
		
#==========================================================================================================================
#MAPPING PHASE 1/4: get region + municipality
#in: RR address line transformed into a dictionary => (P:postcode, S:streetnameRR)
#out: enriched input dictionary (+ R, idM, M2, pcM) + counters for statistics
#==========================================================================================================================
def	getRegionMunicipality(dicAddress, counters, dicM, dicMapMunToR):
	idM = dicAddress['idM_SRC']
	try:
		dicAddress['idM'] = idM		
		R = dicMapMunToR[idM]
		dicAddress['R'] = R
		dicAddress['M2'] = getBESTMunicipalityName(dicM, R, idM)
		dicAddress['pcM'] = 1				# municipality ok
	except:
		dicAddress['idM'] = ""
		dicAddress['R'] = ""
		dicAddress['M2'] = ""
		dicAddress['pcM'] = 0
		counters['cntA2'] += 1
		dicAddress['warningA'] = warningA2
	return dicAddress, counters

#============================================================================
#MAPPING PHASE 2/4: get street
#in: RR address line transformed into a dictionary (P, S + R, idM, M2, pcM)
#in: dicMapStreets => fMapStreetnamesRR
#out: enriched input dictionary (+ R, idM, M2, pcM + idS, S2, pcS, pcS2) + counters for statistics
#============================================================================
def getStreet(dicAddress, counters, dicMapStreets, dicMapStreetCode_RRtoBEST, dicS):
	pcM = float(dicAddress['pcM'])
	if  pcM == 1.0 :
		dicAddress, counters = getStreetInMunicipality(dicAddress, counters, dicMapStreets, dicMapStreetCode_RRtoBEST, dicS)
	else:
		dicAddress = getStreetNoMunicipality(dicAddress, dicS)
	return 	dicAddress, counters

def getStreetNoMunicipality(dicAddress, dicS):
	# R = dicAddress['key'][:1] #RR
	# dicAddress = updStreet(dicAddress, R, "", "", 0.0, 0.0, dicS)
	dicAddress = updStreet(dicAddress, "", "", "", 0.0, 0.0, dicS)
	return 	dicAddress

def getStreetInMunicipality(dicAddress, counters, dicMapStreets, dicMapStreetCode_RRtoBEST, dicS):
	R = dicAddress['R']
	idM = dicAddress['idM']
	idS_SRC = dicAddress['idS_SRC']
	succeeded = True

	try:
		idS = dicMapStreetCode_RRtoBEST[R][idM][idS_SRC]['idS']
		S2 = getBESTStreetName(dicS, R, idS)
		pcS = dicMapStreetCode_RRtoBEST[R][idM][idS_SRC]['pcS']
		pcS2 = dicMapStreetCode_RRtoBEST[R][idM][idS_SRC]['pcS2']
	except:
		succeeded = False
		idS = ""
		S2 = ""
		pcS = 0.0
		pcS2 = 0.0
		
	# if succeeded and not streetNames_OK(dicAddress, R, idS): #doublecheck
		# succeeded = False
		# idS = ""
		# pcS = 0.0
		# pcS2 = 0.0

	#KBO: sometimes the streetcode was not found in the table derived from (RR+BEST), while a valid streetname was given
	if succeeded == False:
		for item in [dicAddress['S'], dicAddress['Snl'], dicAddress['Sfr'], dicAddress['Sde'] ]:
			if item != "":
				keyRM = R+idM
				match, prob = matchIt(dicMapStreets[keyRM].keys(), item)
				if prob > THRESHOLD_STREET and prob > pcS:
					S2 = match
					idS = dicMapStreets[keyRM][match]
					pcS = prob
					pcS2 = prob
					succeeded = True
					
	dicAddress = updStreet(dicAddress, R, idS, S2, pcS, pcS2, dicS)
	if not succeeded:	
		if not isAlreadyCounted(dicAddress, "B"):
			counters['cntB2'] += 1
		dicAddress['warningB'] = warningB2	
	return dicAddress, counters

def streetNames_OK(dicAddress, R, idS):
	nameBEST = getBESTStreetName(dicS, R, idS)
	namesKBO = [ dicAddress['S'], dicAddress['Snl'], dicAddress['Sfr'], dicAddress['Sde'] ]
	bestMatch, prob = matchIt(namesKBO, nameBEST)
	if prob > THRESHOLD_STREET:
		return True
	else:
		print("ISSUE Streetname (code was found, names don't match)", prob, nameBEST, bestMatch, namesKBO)
		return False
	
def	updStreet(dicAddress, R, idS, S2, pcS, pcS2, dicS):
	dicAddress['idS'] = idS
	dicAddress['S2'] = S2
	dicAddress['pcS'] = pcS		#perfect match probability
	dicAddress['pcS2'] = pcS2	#adjusted probability
	if idS == "":
		dicAddress['S2'] = "Unknown"
	else: 
		dicAddress['S2'] = getBESTStreetName(dicS, R, idS)
	return 	dicAddress
	
#============================================================================
#MAPPING PHASE 3/4: get house number
#in: RR address line transformed into a dictionary (+ R, idM, M2, pcM + idS, S2, pcS, pcS2)
#in: fMapHouseNrs => dicMapHouseNumbers
#out: enriched input dictionary (+ R, idM, M2, pcM + idS, S2, pcS, pcS2 + hs2, pcHs)+ counters for statistics
#============================================================================
def getHouseNr(dicAddress, counters, dicMapHouseNumbers, src):
	pcM = float(dicAddress['pcM'])
	pcS = float(dicAddress['pcS'])
	pcS2 = float(dicAddress['pcS2'])
	pcMS = pcM * pcS2

	if pcM != 1.0 or pcS2 == 0:		#don't continue mapping if municipality or street nok
		dicAddress, counters = getHouseNrNoStreet(dicAddress, counters)
		return dicAddress, counters	

	key = dicAddress['key']
	R = dicAddress['R']
	idS = dicAddress['idS']
	hs = dicAddress['hs']
	hsTransformed = transformHouseNr(hs) #remove leading zeroes from source house number
	
	msg = ""
	keyRS = R + idS
	if keyRS in dicMapHouseNumbers.keys():
		dicAddress, counters = getHouseNrStreetOk(dicMapHouseNumbers,dicAddress, counters, keyRS, hsTransformed, src)
	else:
		dicAddress, counters = getHouseNrStreetWithoutHouseNrs(dicAddress, counters)
		
	return dicAddress, counters

def	updHouseNr(dicAddress, hs2, lstHouseNrs, pcHs):
	dicAddress['hs2'] = hs2
	dicAddress['lstHs2'] = lstHouseNrs
	dicAddress['pcHs'] = pcHs
	return 	dicAddress

def getHouseNrNoStreet(dicAddress, counters):
	dicAddress = updHouseNr(dicAddress, "", [], 0.0)
	return dicAddress, counters	
	
def getHouseNrStreetOk(dicMapHouseNumbers, dicAddress, counters, keyRS, hsTransformed, src):	
	bestMatch, pcHs = matchIt(dicMapHouseNumbers[keyRS].keys(), hsTransformed)
	
	if src in ["RR_B", "RR_W", "RR_F"]:
		lstHouseNrs = dicMapHouseNumbers[keyRS][bestMatch]
		hs2 = ""
	else:
		hs2 = dicMapHouseNumbers[keyRS][bestMatch]
		lstHouseNrs = []

	dicAddress = updHouseNr(dicAddress, hs2, lstHouseNrs, pcHs)

	if pcHs != 1:				#house numbers don't match
		if not isAlreadyCounted(dicAddress, "C"):
			counters['cntC2'] += 1
		dicAddress['warningC'] = warningC2
	return dicAddress, counters
	
def getHouseNrStreetWithoutHouseNrs(dicAddress, counters):
	dicAddress = updHouseNr(dicAddress, "", [], 0.0)
	if not isAlreadyCounted(dicAddress, "C"):
		counters['cntC1'] += 1
	dicAddress['warningC'] = warningC1
	return dicAddress, counters
	
#============================================================================
#MAPPING PHASE 4/4: get box number
#in: RR address line transformed into a dictionary (+ R, idM, M2, pcM + idS, S2, pcS, pcS2 + hs2, pcHs)
#in: fMapBoxNrs => dicMapBoxNumbers
#out: enriched input dictionary (+ R, idM, M2, pcM + idS, S2, pcS, pcS2 + hs2, pcHs + bx2, pcBx, pcBx2, pc, pc2, idA) + counters for statistics
#============================================================================
def addAction(dicAddress):
	dicAddress['action'] = 'tbd'
	warningA = dicAddress['warningA']
	warningB = dicAddress['warningB']
	warningC = dicAddress['warningC']
	warningD = dicAddress['warningD']
	if (dicAddress['pc2'] == 1.0) or (dicAddress['pcM'] == 1 and dicAddress['pcS2'] > THRESHOLD_STREET  and dicAddress['pcHs'] == 1 and dicAddress['pcBx'] == 1):
		dicAddress['action'] = actionOk
	elif warningA == warningA1:
		dicAddress['action'] = actionA1
	elif warningB == warningB1:
		dicAddress['action'] = actionB1
	elif warningD == warningD1 and dicAddress['pcM'] == 1 and dicAddress['pcS2'] > THRESHOLD_STREET  and dicAddress['pcHs'] == 1 and dicAddress['hs'] == dicAddress['hs2']:
	#why addition of the last condition ? cfr case where RR hs=127, bx = A/GV and BEST hs2=127A, bx2=empty (in this case you cannot automatically add a box nr, otherwise you'd have in BEST 127A with A/GV
		dicAddress['action'] = actionD1
	#print('***5***', dicAddress['action'])
	return dicAddress

def getBoxNr(dicAddress, counters, dicMapBoxNumbers, src):
	pcM = 	float(dicAddress['pcM'])
	pcS2 = 	float(dicAddress['pcS2'])
	pcHs = 	float(dicAddress['pcHs'])
	bx = dicAddress['bx']
	if pcM != 1.0 or pcS2 == 0 or pcHs !=1.0:
		dicAddress = getBxNrMappingNotPossible(dicAddress)
	else:
		dicAddress, counters = getBxNrDoMapping(dicAddress, counters, dicMapBoxNumbers, src)
	dicAddress = addAction(dicAddress)
	return dicAddress, counters	

def	updBoxNr(dicAddress, idA, hs2, bx2, pcBx, pcM, pcS, pcS2, pcHs):
	dicAddress['idA'] = idA
	dicAddress['hs2'] = hs2
	dicAddress['bx2'] = bx2
	dicAddress['pcBx'] = pcBx
	dicAddress['pc'] = pcM * pcS * pcHs * pcBx
	dicAddress['pc2'] = pcM * pcS2 * pcHs * pcBx
	return 	dicAddress

def getBxNrMappingNotPossible(dicAddress):
	dicAddress = updBoxNr(dicAddress, "", "", "", 0, 0, 0, 0, 0)
	return dicAddress	
	
def mapBx(R, idS, hs, lstHouseNrs, bxTransformed, dicMapBoxNumbers):
	idA = ""
	bestBx = ""
	highestPcBx = 0
	hs2 = ""
	if len(lstHouseNrs) == 1:
		hs2 = lstHouseNrs[0]
	elif hs in lstHouseNrs:
		hs2 = hs				#cfr case with RR: hs 41, bxTransformed GV and BEST [41, 41B], bxTransformed empty (if not initialized like this you get pcHs=1 and hs2 empty)
	
	for houseNr in lstHouseNrs:
		keyRShs = R + idS + '_' + transformHouseNr(houseNr)
		#print('**************', keyRShs, dicMapBoxNumbers[keyRShs])
		if keyRShs in dicMapBoxNumbers:
			bestMatch, pcBx = matchIt(dicMapBoxNumbers[keyRShs].keys(), bxTransformed)
			bx2 = dicMapBoxNumbers[keyRShs][bestMatch]['bx']
			#print('@@@@@@', dicMapBoxNumbers[keyRShs].keys(),bxTransformed, bestMatch, bx2, pcBx)
			# if pcBx < 1.0:
				# bestMatch, pcBx = transformBoxAndMap(bestMatch, pcBx, dicMapBoxNumbers[keyRShs].keys(), bxTransformed)
				# bx2 = dicMapBoxNumbers[keyRShs][bestMatch]['bx']
				#print('$$$$$$', bx2, pcBx)
			if pcBx > highestPcBx:
				bestBx = bx2
				highestPcBx = pcBx
				idA = dicMapBoxNumbers[keyRShs][bestMatch]['idA']
				hs2 = houseNr
	return idA, hs2, highestPcBx, bestBx		

def getBxNrDoMapping(dicAddress, counters, dicMapBoxNumbers, src):
	#print("***1***", dicAddress)
	pcM = float(dicAddress['pcM'])
	pcS = float(dicAddress['pcS'])
	pcS2 = float(dicAddress['pcS2'])
	pcHs= float(dicAddress['pcHs'])

	key = dicAddress['key']
	R = dicAddress['R']
	idS = dicAddress['idS']
	hs = dicAddress['hs']
	hs2 = dicAddress['hs2']
	lstHouseNrs = dicAddress['lstHs2'] #only relevant for RR
	bx = dicAddress['bx']
	bxTransformed = transformBoxNr(bx)
	
	if bxTransformed == "":
		strToMap = "NO BOX NR"
	else:
		strToMap = bxTransformed
		
	if src in ["RR_B", "RR_W", "RR_F"]:
		idA, hs2, pcBx, bx2 = mapBx(R, idS, hs, lstHouseNrs, strToMap, dicMapBoxNumbers)
		#print("***2***", idA, hs2, pcBx, bx2)
	else:
		idA, hs2, pcBx, bx2 = mapBx(R, idS, hs, [hs2], strToMap, dicMapBoxNumbers)

	dicAddress = updBoxNr(dicAddress, idA, hs2, bx2, pcBx, pcM, pcS, pcS2, pcHs)
	#print("***3***", dicAddress) 		
	if pcBx == 1 and dicAddress['pc2'] > THRESHOLD_STREET :
		counters['cntOk'] += 1
	if pcBx != 1:
		#if bx2 == "NO BOX NR":
		if bx != "" and bx2 == "":						#case: house nr ok, SRC has bx nr, while BEST doesn't have a box nr
			if not isAlreadyCounted(dicAddress, "D"):
				counters['cntD1'] += 1
			dicAddress['warningD'] = warningD1 
			#print("***4***", bx2, warningD1)
		elif bx == "" and bx2 != "":					#case: house nr ok, SRC doesn't have a bx nr, while BEST has a box nr
			if not isAlreadyCounted(dicAddress, "D"):
				counters['cntD3'] += 1
			dicAddress['warningD'] = warningD3 
			#print("***4***", bx2, warningD1)
		else:											#case: house nr ok, SRC and BEST have box nrs, however none of them matches
			if not isAlreadyCounted(dicAddress, "D"):
				counters['cntD2'] += 1
			dicAddress['warningD'] = warningD2
			#print("***4***", bx2, warningD2)

	return dicAddress, counters	