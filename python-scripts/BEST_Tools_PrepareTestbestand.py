#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

def createTestFile(inputFile, outputFile):
	fileIn=open(inputFile,"r", encoding=PREFERRED_ENCODING)
	fileOut=open(outputFile,"w", encoding=PREFERRED_ENCODING)
	
	address = fileIn.readline()
	cnt = 0
	while address:
		cnt += 1
		if 'B0429918' in address or 'B0404482' in address :
			fileOut.write(address)
		try:
			address = fileIn.readline()
		except:
			print("SERIOUS ERROR at line ", cnt)
			break
	fileIn.close()
	fileOut.close()

inputFile = "RR_B.txt"
outputFile = "RR_B_Test.txt"	
createTestFile(inputFile, outputFile)
