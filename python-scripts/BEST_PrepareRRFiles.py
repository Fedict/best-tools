#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
#PREPARE INPUTFILE FROM RR-FILE===================================================================	
def createInputFile(region, inputFile, outputFile):
	fileOut = open(outputFile,"w", encoding=PREFERRED_ENCODING)
	createInputFileRegion(region, inputFile, fileOut)
	fileOut.close()	
def createInputFileRegion(region, inputFile, fileOut):
	print(inputFile)
	fileIn = open(inputFile,"r", encoding=ENCODING_RR)
	line = fileIn.readline() #headerline
	cnt = 1
	line = fileIn.readline() 
	while line:
		transformedLine = transformLine(region, cnt, line)
		fileOut.write(transformedLine)
		line = fileIn.readline() 
		if cnt % 100000 == 0:
			print(cnt, line.strip())
		cnt += 1
	fileIn.close()
def transformLine(region, cnt, line):
	line = line.strip()
	line = line.replace('\\', '/')
	lst = line.split(";")
	cntElements = len(lst)
	ok = True
	if region == 'B': 
		if cntElements != 7:
			ok = False
	else:
		if cntElements != 6:
			ok = False
	if line == '63079;4800;2005;GRAND PLACE;28;Rez;': #one issue in the file of Wallonia solved this way (1 ';' too much at the end
		ok = True
	if ok:
		if region == 'B':
			#NIS;POSTCODE;STRAATCODE;STRAATNAAM_N;STRAATNAAM_F;HUISNR;INDEX
			key = region + "%07d" % (cnt)
			idM_SRC = lst[0]
			M = ""
			Mnl = ""
			Mfr = ""
			Mde = ""
			P = lst[1]
			idS_SRC = lst[2]
			S = ""
			Snl = lst[3]
			Sfr = lst[4]
			Sde = ""
			hs = lst[5]
			bx = lst[6]	
		else: 
			#INS;CODE_POSTAL;CODE_RUE;RUE;NUMERO;INDEX
			key = region + "%07d" % (cnt)
			idM_SRC = lst[0]
			M = ""
			Mnl = ""
			Mfr = ""
			Mde = ""
			P = lst[1]
			idS_SRC = lst[2]
			S = ""
			if region == 'F':
				Snl = lst[3]
				Sfr = ""
			else: #Wallonia
				Sfr = lst[3]
				Snl = ""
			Sde = ""
			hs = lst[4]
			bx = lst[5]	
	else:
		print(region + "-ISSUE with delimiter ';' in line " + line)

	return 	createAddressDicToMap(key, idM_SRC, M, Mnl, Mfr, Mde, P, idS_SRC, S, Snl, Sfr, Sde, hs, bx)

#===PREPARE INPUTFILE FROM RR-FILE===================================================================	
#RR create an input file based on the 3 files given by RR (total time for running the mapping was 10hr30 - therefore we split it up in separate files for the regions
createInputFile("B", SRC_RR_B_ORI, SRC_RR_B_IN)
createInputFile("W", SRC_RR_W_ORI, SRC_RR_W_IN)
createInputFile("F", SRC_RR_F_ORI, SRC_RR_F_IN)

