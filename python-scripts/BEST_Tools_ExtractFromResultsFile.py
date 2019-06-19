#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *
import sys
	
#----------------------------------------------------------------------------
def getFileNamesForExtract(lstSysArgv):
	src = ""
	warnings = []
	wars = ""
	iParam = 0
	for item in lstSysArgv:
		if iParam == 1:
			src = lstSysArgv[iParam]
		elif iParam != 0:
			if lstSysArgv[iParam] == "idA":
				warnings.append(actionOk)
			elif lstSysArgv[iParam] == "A":
				warnings.append(warningA1)
				warnings.append(warningA2)
			elif lstSysArgv[iParam] == "B":
				warnings.append(warningB1)
				warnings.append(warningB2)
			elif lstSysArgv[iParam] == "C":
				warnings.append(warningC1)
				warnings.append(warningC2)
			elif lstSysArgv[iParam] == "D":
				warnings.append(warningD1)
				warnings.append(warningD2)
				warnings.append(warningD3)
			elif lstSysArgv[iParam] == "A1":
				warnings.append(warningA1)
			elif lstSysArgv[iParam] == "A2":
				warnings.append(warningA2)
			elif lstSysArgv[iParam] == "B1":
				warnings.append(warningB1)
			elif lstSysArgv[iParam] == "B2":
				warnings.append(warningB2)
			elif lstSysArgv[iParam] == "C1":
				warnings.append(warningC1)
			elif lstSysArgv[iParam] == "C2":
				warnings.append(warningC2)
			elif lstSysArgv[iParam] == "D1":
				warnings.append(warningD1)
			elif lstSysArgv[iParam] == "D2":
				warnings.append(warningD2)
			elif lstSysArgv[iParam] == "D3":
				warnings.append(warningD3)
				
			wars += lstSysArgv[iParam]
		iParam += 1

	filenameIn = "Unknown"
	if src == "RR_B":
		filenameIn = SRC_RR_B_RESULT
	elif src == "RR_F":
		filenameIn = SRC_RR_F_RESULT
	elif src == "RR_W":
		filenameIn = SRC_RR_W_RESULT
	elif src == "KBO":
		filenameIn = SRC_KBO_RESULT
	elif src == "POL":
		filenameIn = SRC_POL_RESULT
	elif src == "AAPD":
		filenameIn = SRC_AAPD_RESULT
	filenameOut = filenameIn[:-4] + '_Extract_' + wars + ".txt"
	return src, warnings, filenameIn, filenameOut
	
def writeExtractFromResultFile(src, warnings, filenameIn, filenameOut):
#in: lst with structure [{k1:v1, k2:v2, ...}, ...]
#in: filename
#result: file with filename contains data from lst (1 line for each {k1:v1, k2:v2, ...})
	fileIn = open(filenameIn,"r", encoding=PREFERRED_ENCODING)
	fileOut = open(filenameOut,"w", encoding=PREFERRED_ENCODING)
	
	cnt = 0
	line = fileIn.readline()
	while line:
		cnt += 1
		
		keepLine = False
		if actionOk in warnings:
			if actionOk in line:
				keepLine = True
		else:
			for warning in warnings:
				if warning in line:
					keepLine = True
		if keepLine:
			try:	
				fileOut.write(line)
			except:
				print('ISSUE writing file:', filename, line)
				
		line = fileIn.readline() 
		if cnt % 100000 == 0:
			print(src, cnt)
	fileIn.close()
	fileOut.close()

#==========================================================================================================================
start = datetime.datetime.now()
print("start: ", start)

nParams = len(sys.argv) - 1		#-1 cfr sys.argv[0] contains the name of the python script
if nParams < 2:
	print("required parameters: src, one or more selection criteria")
	print("src in ['AAPD', 'RR_B', 'RR_F', 'RR_W', 'KBO', 'POL']")
	print("selection criteria: either 'idA' for selecting correct address id's")
	print("selection criteria: or ['A', 'A1', 'A2', 'B', 'B1', 'B2, 'C', 'C1', 'C2', 'D', 'D1', 'D2', 'D3'] for selecting one or more warnings")
	print("example 1:   BEST_Tools_ExtractFromResultsFile.py POL idA      will extract correct BEST address id's from the results file of the Police")
	print("example 2:   BEST_Tools_ExtractFromResultsFile.py POL A B2     will extract warnings A1, A2 and B2 from the results file of the Police")
	quit()
src, warnings, filenameIn, filenameOut = getFileNamesForExtract(sys.argv)
# print(nParams, sys.argv)
# print(filenameIn, filenameOut)
# print(src, warnings)

writeExtractFromResultFile(src, warnings, filenameIn, filenameOut)
	
end = datetime.datetime.now()
print("start: ", start)
print("end: ", end)
print("duration:", end-start)
