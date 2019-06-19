#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
#PREPARE INPUTFILE FROM POL-FILE===================================================================	
def createInputFile(inputFile, outputFile):
	fileOut = open(outputFile,"w", encoding=PREFERRED_ENCODING)
	fileIn = open(inputFile,"r", encoding=ENCODING_POL)

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
#0 ID, 1 STREETID, 2 REFTABKEY, 3 STREET_NL, 4 STREET_FR, 5 STREET_GER, 6 NUMBER, 7 BOX, 8 RRN,
#9 NIS, 10 COMMUNITY_NL, 11 COMMUNITY_FR, 12 COMMUNITY_GER, 13 POSTAL_NR, 14 POSTAL_NAME
	delimiter = ';'
	result = ""
	lineOri = line
	
	line = line.strip()
	line = line.replace('"', "'") #cfr Rue de Soulme "Les Coquelicots" gives errors in json strings (no double quotes allowed)
	if ";52022;" in line: #Fontaine l'Eveque ipv Fontaine-l'Eveque
		line = line.replace('Fontaine l','Fontaine-l') 
	if ";62093;" in line:	#Saint-Nicolas (Liege) ipv Saint-Nicolas
		stringToReplace = ' (Li' + '\u00E8' + 'ge)'
		line = line.replace(stringToReplace, '')

	lst = line.split(delimiter)
	if len(lst) != 15:
		print(15 - len(lst), 'missing fields in line', cnt)
		print(lineOri)
		print(line)
		print(lst)
		print('-----------------------------------------')
		
	key = "%07d" % (cnt)
	key = key + "_" + lst[0]
	idM_SRC = lst[9]
	M = ""
	Mnl = lst[10]
	Mfr = lst[11]
	Mde = lst[12]
	P = lst[13]
	idS_SRC = lst[1]
	S = ""
	Snl = lst[3]
	Sfr = lst[4]
	Sde = lst[5]
	hs = lst[6]
	if lst[7] == "\\N":
		bx = ""
	else:
		bx = lst[7]
	
	return 	createAddressDicToMap(key, idM_SRC, M, Mnl, Mfr, Mde, P, idS_SRC, S, Snl, Sfr, Sde, hs, bx)
		
#===PREPARE INPUTFILE FROM POLICE-FILE===================================================================	
#create an input file based on the file given by the POLICE
createInputFile(SRC_POL_ORI, SRC_POL_IN)


