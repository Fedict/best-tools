#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
#PREPARE INPUTFILE FROM FOD ECO-FILE===================================================================	
def createInputFile(inputFile, outputFile):
	fileOut = open(outputFile,"w", encoding=PREFERRED_ENCODING)
	fileIn = open(inputFile,"r", encoding=ENCODING_KBO)

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
#0 OND_NR, 1 C_TYPE_ADRES, 2 D_BEGINDATUM, 3 D_EINDDATUM, 4 C_NIS_GEMEENTECODE, 5 GEMEENTENAAM_F, 6 GEMEENTENAAM_NL, 7 GEMEENTENAAM_D, 8 C_POSTCODE,
#9 C_STRAATCODE, 10 STRAATNAAM_VOLL, 11 STRAATNAAM_F, 12 STRAATNAAM_NL, 13 STRAATNAAM_D, 14 HUISNUMMER, 15 BUSNUMMER
	delimiter = '$'
	result = ""
	lineOri = line
	line = line.strip()
	#print(line)
	if line[9:10] == ",":
		line = '"' + line[:9] + '"' + line[9:]		#missing quotes around enterprise number
	elif line[10:11] == ",":	
		line = '"' + line[:10] + '"' + line[10:]	#missing quotes around enterprise number: there were enterprise numbers with 10 characters in it (normal case is 9 characters)
		
	#print(line)
	while ",," in line:								#some quotes for empty fields were omitted in the source file
		line = line.replace(',,', ',"",')
	#print(line)
	line = line.replace('","', '"' + delimiter + '"')		#comma cannot be used as delimiter as it is used in some names
	#print(line)
	line = line.replace('"', '')
	#print(line)
	lst = line.split(delimiter)
	#print(lst)
	#print('-------------------------------------------')
	if len(lst) != 16:
		print(16 - len(lst), 'missing fields in line', cnt)
		print(lineOri)
		print(line)
		print(lst)
		print('-----------------------------------------')
		
	key = "%07d" % (cnt)
	key = key + "_" + lst[0] + "_" + lst[1]
	idM_SRC = lst[4]
	M = lst[5]
	Mnl = lst[6]
	Mfr = lst[5]
	Mde = lst[7]
	P = lst[8]
	idS_SRC = lst[9]
	S = lst[10]
	Snl = lst[12]
	Sfr = lst[11]
	Sde = lst[13]
	hs = lst[14]
	bx = lst[15]	
	
	return 	createAddressDicToMap(key, idM_SRC, M, Mnl, Mfr, Mde, P, idS_SRC, S, Snl, Sfr, Sde, hs, bx)
		
#===PREPARE INPUTFILE FROM FOD-ECO-FILE===================================================================	
#create an input file based on the file given by FOD Economie
createInputFile(SRC_KBO_ORI, SRC_KBO_IN)


