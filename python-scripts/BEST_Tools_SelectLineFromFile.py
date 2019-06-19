#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

def showLines(lst, inputFile):
	fileIn=open(inputFile,"r", encoding=PREFERRED_ENCODING)
	
	line = fileIn.readline()
	cnt = 0
	while line:
		cnt += 1
		lineOk = True
		for item in lst:
			if not item in line:
				lineOk = False
		if lineOk: 
			print(cnt, line.strip())
		try:
			line = fileIn.readline()
		except:
			print(cnt)
	print("Total number of lines in", inputFile, ":", cnt)
	fileIn.close()
	
def getParameters(lstSysArgv):
	src = ""
	lst = []
	wars = ""
	iParam = 0
	for item in lstSysArgv:
		if iParam == 1:
			filenameIn = lstSysArgv[iParam]
		elif iParam != 0:
			lst.append(lstSysArgv[iParam])
		iParam += 1
	return lst, filenameIn
#=============================================================================
# inputFile = "KBO.txt"
# #inputFile = "ConsolidatedResult_B.txt"
# showLines(inputFile)

# inputFile = "MAP_BoxNrs.txt"
# dic = getDic(inputFile)
# print(dic['B57022_28'])

# inputFile = "MAP_HouseNrs.txt"
# dic = getDic(inputFile)
# print(dic['B56724'])
#=============================================================================


nParams = len(sys.argv) - 1		#-1 cfr sys.argv[0] contains the name of the python script
if nParams < 2:
	print("required parameters: filename, one or more selection criteria")
	print("example 1: python BEST_Tools_SelectLineFromFile.py BrusselsAddress.xml Nieuwstraat     will extract all lines from file BrusselsAddress.xml that contain 'Nieuwstraat'")
	print("example 2: python BEST_Tools_SelectLineFromFile.py AAPD.txt Nieuwstraat   22  will extract all lines from file AAPD.txt that contain both 'Nieuwstraat' and '22'")
	print("example 3: python BEST_Tools_SelectLineFromFile.py POL.txt Nieuwstraat \\\"hs\\\":\\\"22\\\" will extract all lines from file POL.txt that contain both 'Nieuwstraat' and '\"hs\":\"22\"'")
	quit()
	
lst, filenameIn = getParameters(sys.argv)
# print(nParams, sys.argv)
# print(filenameIn, filenameOut)
# print(src, warnings)

showLines(lst, filenameIn)
	
