#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *
	
#PREPARE INPUTFILE FROM AAPD-FILE===================================================================	
def createInputFile(inputFile, outputFile):
	fileOut = open(outputFile,"w", encoding=PREFERRED_ENCODING)
	fileIn = open(inputFile,"r", encoding=ENCODING_AAPD)

	line = fileIn.readline() #headerline
	cnt = 1
	line = fileIn.readline()
	while line:
		transformedLine = transformLine(cnt, line)
		fileOut.write(transformedLine)
		line = fileIn.readline() 
		cnt += 1
		if cnt % 100000 == 0:
			print(cnt, line.strip())
	print(cnt)
	fileIn.close()
	fileOut.close()	
	
def transformLine(cnt, line):
#0 CaPaKey,1 AdMuKey,2 PostKey,3 CaStKey,4 CaAdNu,5 CaAdX,6 CaAdY
	lineOri = line
	delimiter = ','
	result = ""
	line = line.strip()
	#print(line)
	#source file contained '"2A, 4, 4A"' (issue with quote + issue with delimiter)
	if '"' in line:
		lst2 = line.split('"')
		if len(lst2) == 3:
			lineA = lst2[0]
			lineB = lst2[1].replace(",", ";")	#solves issue with delimiter (transformed source line now contains '"2A; 4; 4A"')
			lineC = lst2[2]
			line = lineA + lineB + lineC		#solves issue with quote (gone now)
	lst = line.split(delimiter)
	#print(lst)
	#print('-------------------------------------------')
	if len(lst) != 7:
		print(7 - len(lst), 'missing fields in line', cnt)
		print(lineOri)
		print(line)
		print(lst)
		print('-----------------------------------------')
		
	key = "%07d" % (cnt)
	key = key + "_" + lst[0]
	idM_SRC = lst[1]
	M = ""
	if idM_SRC in dicMun:
		Mnl = dicMun[idM_SRC]['Mnl']
		Mfr = dicMun[idM_SRC]['Mfr']
		Mde = dicMun[idM_SRC]['Mde']
	else:
		Mnl = "not in AAPD table"
		Mfr = "not in AAPD table"
		Mde = "not in AAPD table"
	
	P = lst[2]
	idS_SRC = lst[3]
	S = ""
	Sok = False
	if idM_SRC in dicStreet:
		if idS_SRC in dicStreet[idM_SRC]:
			Snl = dicStreet[idM_SRC][idS_SRC]['Snl']
			Sfr = dicStreet[idM_SRC][idS_SRC]['Sfr']
			Sde = dicStreet[idM_SRC][idS_SRC]['Sde']
			Sok = True
	if not Sok:
		Snl = "not in AAPD table"
		Sfr = "not in AAPD table"
		Sde = "not in AAPD table"
		
	hs = lst[4].strip()
	bx = ""	
	
	result = createAddressDicToMap(key, idM_SRC, M, Mnl, Mfr, Mde, P, idS_SRC, S, Snl, Sfr, Sde, hs, bx)
	return result
		
print("dicMun from AAPD ..")
dicMun = createMunDicAAPD(SRC_AAPD_MUNICIPALITY_TABLE)
print("dicStreet from AAPD ..")
dicStreet = createStreetDicAAPD(SRC_AAPD_STREET_TABLE)
#===PREPARE INPUTFILE FROM AAPD-FILE===================================================================	
#create an input file based on the file given by AAPD
print("creating AAPD input file..")
createInputFile(SRC_AAPD_ORI, SRC_AAPD_IN)

