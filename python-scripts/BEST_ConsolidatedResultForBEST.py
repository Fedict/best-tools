#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *
	
regions = ["B", "F", "W"]
inFileDic = {"RR_B": SRC_RR_B_RESULT, "RR_F": SRC_RR_F_RESULT, "RR_W": SRC_RR_W_RESULT, "KBO": SRC_KBO_RESULT, "POL": SRC_POL_RESULT, "AAPD": SRC_AAPD_RESULT}
#inFileDic = {"RR_B": SRC_RR_B_RESULT, "KBO": SRC_KBO_RESULT} #test
#inFileDic = {"RR_B": SRC_RR_B_RESULT} #test
outFileDic = {"B": BEST_B_CONSOLIDATEDRESULT, "F": BEST_F_CONSOLIDATEDRESULT, "W": BEST_W_CONSOLIDATEDRESULT}

def consolidateLine(dicOut, srcName, R, idM, idS, hs, hs2, bx, bx2, warning):
	key = R+idM+idS+hs+hs2+bx+bx2+warning[0:2]
	if not key in dicOut[R]:
		dicOut[R][key] = {"R":"", "idM": "", "idS": "", "hs": "", "hs2": "", "bx": "", "bx2": "", "warning": "", "RR": "", "KBO": "", "POL": "", "AAPD": ""}
	dicOut[R][key]['R'] = R
	dicOut[R][key]['idM'] = idM
	dicOut[R][key]['idS'] = idS
	dicOut[R][key]['hs'] = hs
	dicOut[R][key]['hs2'] = hs2
	dicOut[R][key]['bx'] = bx
	dicOut[R][key]['bx2'] = bx2
	dicOut[R][key]['warning'] = warning
	if srcName in ["RR_B", "RR_F", "RR_W"]:
		dicOut[R][key]["RR"] = "TRUE"
	if srcName == "KBO":
		dicOut[R][key]["KBO"] = "TRUE"
	if srcName == "POL":
		dicOut[R][key]["POL"] = "TRUE"
	if srcName == "AAPD":
		dicOut[R][key]["AAPD"] = "TRUE"
	return dicOut

def consolidateResultFile(dicOut, srcName, srcResultFile):
	fileIn = open(srcResultFile,"r", encoding=PREFERRED_ENCODING)
	cnt = 1
	line = fileIn.readline()
	while line:
		dicLine = {}
		dicLine = ast.literal_eval(line)
		warningC = dicLine['warningC']
		warningD = dicLine['warningD']
		if warningC != "" or warningD != "":
			R = dicLine['R']
			idM = str(dicLine['idM'])
			idS = str(dicLine['idS'])
			hs = dicLine['hs']
			hs2 = dicLine['hs2']
			bx = dicLine['bx']
			bx2 = dicLine['bx2']
			warning = warningC + warningD 					#either warningC or warningD differs from empty string
			dicOut = consolidateLine(dicOut, srcName, R, idM, idS, hs, hs2, bx, bx2, warning)
		line = fileIn.readline() 
		cnt += 1
		if cnt % 100000 == 0:
			print(srcName, cnt)
	print(cnt)
	fileIn.close()
	return dicOut

start = datetime.datetime.now()
print("start: ", start)
dicOut = {}				#dicOut is a dictionary that will contain a dictionary for each region with a consolidation of the number issues
for R in regions:
	dicOut[R] = {}
for srcName, srcResultFile in inFileDic.items():
	dicOut = consolidateResultFile(dicOut, srcName, srcResultFile)

for R in regions:
	fileName = outFileDic[R]
	fileOut = open(fileName,"w", encoding=PREFERRED_ENCODING)
	for key, dic in dicOut[R].items():
		line = ""
		for k, v in dic.items():
			line = line + '"' + k + '":"' + v + '",'
		if line != "":
			line = line[:-1] + '\n'
		try:	
			fileOut.write(line)
		except:
			print('ISSUE writing file: BEST_ConsolidatedResultForBEST.py', k, v)
	fileOut.close()
	
end = datetime.datetime.now()
print("start: ", start)
print("end: ", end)
print("duration:", end-start)
