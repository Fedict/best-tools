#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	
	
def print_menu():       ## Your menu design here
	print()
	print(30 * "-" , "MENU" , 30 * "-")
	print("A. Address id")
	print("M. Municipality id")
	print("PM. Part of Municipality id")
	print("P. Postal code")
	print("S. Street id")
	print("Q. Quit")
	print(67 * "-")
 
#=================================================================================================================
def printAddressDetails(idA, R, objId, v, dicA):
	print("Address", idA)
	print("Region", R)
	print("PostalInfo",  dicA['idP'], ", ", dicA['Pnl'], ", ", dicA['Pfr'], ", ", dicA['Pde']) 
	print("Municipality",  dicA['idM'], ", ", dicA['Mnl'], ", ", dicA['Mfr'], ", ",dicA['Mde']) 
	if R == "W":
		print("Part of Municipality",  dicA['idPM'], ", ", dicA['PMnl'], ", ", dicA['PMfr'], ", ",dicA['PMde']) 
	print("Street",  dicA['idS'], ", ", dicA['Snl'], ", ", dicA['Sfr'], ", ", dicA['Sde']) 
	print("House Nr", dicA['hs'], "Box Nr", dicA['bx'])
	print("Coordinates (X,Y)", dicA['x'], ", ", dicA['y'])
	print("Status", dicA['st'])

def printAddress(versionAvailable, idA, R, objId, v, result):
	if versionAvailable:
		dicA = result
		printAddressDetails(idA, R, objId, v, dicA)
	else:
		for version, dicA in result.items():
			printAddressDetails(idA+version, R, objId, version, dicA)

def getFromIdA(idA, dicA):
	if idA == "":
		exit()
	result = {}
	lst = idA.split("_")
	R = lst[0]
	objId = lst[1]
	if len(lst) == 3:
		v = lst[2]
	else:
		v = ""
	if v != "":
		versionAvailable = True
	else:
		versionAvailable = False
	try:
		if versionAvailable:
			result = dicA[R][objId][v]
		else:
			result = dicA[R][objId]
		printAddress(versionAvailable, idA, R, objId, v, result)
	except:
		print("address not found")

#=====================================================================================================================		
def printMunicipality(R, idM, v, dic, dicMtoP, dicMtoPM):
	print("Region", R)
	
	try:
		nameNl = dic['nl']
	except:
		nameNl = ""
		
	try:
		nameFr = dic['fr']
	except:
		nameFr = ""
		
	try:
		nameDe = dic['de']
	except:
		nameDe = ""

	print("Municipality", R + "_" + idM + "_" + v, ", ", nameNl, ", ", nameFr, ", ", nameDe)
	
	key = R+"-"+nameNl+","+nameFr+","+nameDe+"("+idM+")"
	partOfPostalLst = dicMtoP[key]
	for item in partOfPostalLst:
		print("has Postal info", item)
	
	if R == "W":									#PartOfMunicipality only in Wallonia
		partOfMunLst = dicMtoPM[key]
		for item in partOfMunLst:
			print("has Part of Municipality", item)
	
def getFromIdM(idM, dicM, dicMtoP, dicMtoPM):	
	result = {}
	for R in ["B", "F", "W"]:
		try:
			result = dicM[R][idM]
			for v, dicMunicipality in result.items():
				printMunicipality(R, idM, v, dicMunicipality, dicMtoP, dicMtoPM)
		except:
			pass

#=====================================================================================================================		
def printPartOfMunicipality(R, idPM, v, dic, dicPMtoM, dicPMtoP):
	print("Region", R)
	
	try:
		nameFr = dic['fr']
	except:
		nameFr = ""
		
	try:
		nameDe = dic['de']
	except:
		nameDe = ""
	print("Part of Municipality", R + "_" + idPM + "_" + v, ", ", nameFr, ", ", nameDe)
	key = "W-,"+nameFr+","+nameDe+"("+idPM+")"
	munLst = dicPMtoM[key]
	for item in munLst:
		print("In Municipality", item)
	postalInfoLst = dicPMtoP[key]
	for item in postalInfoLst:
		print("has Postal info", item)
	
def getFromIdPM(idPM, dicPM, dicPMtoM, dicPMtoP):	
	result = {}
	try:
		result = dicPM["W"][idPM]							#only in Walloon region
		for v, dicPartOfMunicipality in result.items():
			printPartOfMunicipality("W", idPM, v, dicPartOfMunicipality, dicPMtoM, dicPMtoP)
	except:
		pass

#=====================================================================================================================		
def printPostalInfo(R, idP, v, dic, dicPtoM, dicPtoPM):
	print("Region", R)
	
	try:
		nameNl = dic['nl']
	except:
		nameNl = ""
		
	try:
		nameFr = dic['fr']
	except:
		nameFr = ""
		
	try:
		nameDe = dic['de']
	except:
		nameDe = ""
	print("PostalInfo", R + "_" + idP + "_" + v, ", ", nameNl, ", ", nameFr, ", ", nameDe)

	key = R+"-"+nameNl+","+nameFr+","+nameDe+"("+idP+")"
	lstM = dicPtoM[key]
	for item in lstM:
		print("Has Municipality", item)
	lstPM = dicPtoPM[key]
	for item in lstPM:
		print("Has Part of Municipality", item)
		
def getFromIdP(idP, dicP, dicPtoM, dicPtoPM):	
	result = {}
	for R in ["B", "F", "W"]:
		try:
			result = dicP[R][idP]
			isFound = True
			region = R
			for v, dicPostalInfo in result.items():
				printPostalInfo(region, idP, v, dicPostalInfo, dicPtoM, dicPtoPM)
		except:
				pass

#=====================================================================================================================		
def printStreet(R, idS, v, dic):
	print("Region", R)
	if R == "B":
		print("Municipality", dic['idM'], dic['nlM'], dic['frM'])
		print("Street", R + "_" + idS + "_" + v, ", ", dic['nl'], ", ", dic['fr'])
	elif R == "F":
		print("Municipality", dic['idM'], dic['nlM'])
		print("Street", R + "_" + idS + "_" + v, ", ", dic['nl'])
	elif R == "W":
		print("Municipality", dic['idM'], dic['frM'])
		print("Street", R + "_" + idS + "_" + v, ", ", dic['fr'])
	print("Type", dic['sType'])
	if dic['ho'] == "":
		print("Homonym empty")
	else:
		print("Homonym", dic['ho'])
	print("Status", dic['st'])
	
	
def getFromIdS(idS, dicS):
	result = {}
	for R in ["B", "F", "W"]:
		try:
			result = dicS[R][idS]
			for v, dicStreet in result.items():
				printStreet(R, idS, v, dicStreet)
		except:
			pass

#=====================================================================================================================		
firstA = True
firstM = True
firstPM = True
firstP = True
firstS = True

loop=True      
  
while loop:          ## While loop which will keep going until loop = False
	print_menu()    ## Displays menu
	choice = input("Enter your choice [A,M,PM,P,S,Q]: ").upper()
     
	if choice=="A":     
		if firstA:
			firstA = False
			print("reading address dictionary ..")
			dicA = getDic(fDicAddresses)
			print("address dictionary read")
		print("example with version: B_1433854_1 or F_2265425_- or W_1237523_2019-02-05T22:02:34Z") 
		print("example without version: B_1433854 or F_2265425 or W_1237523") 
		idA = input("idA ") 
		getFromIdA(idA, dicA)
	elif choice=="M":
		if firstM:
			firstM = False
			dicM = getDic(fDicMunicipalities)
			dicMtoP = getDic(fMtoP)
			dicMtoPM = getDic(fMtoPM)
		idM = input("idM (e.g. 21002) ")
		getFromIdM(idM, dicM, dicMtoP, dicMtoPM)
	elif choice=="PM":
		if firstPM:
			firstPM = False
			dicPM = getDic(fDicPartOfMunicipalities)
			dicPMtoM = getDic(fPMtoM)
			dicPMtoP = getDic(fPMtoP)
		idPM = input("idPM (e.g. 1645) ")
		getFromIdPM(idPM, dicPM, dicPMtoM, dicPMtoP)
	elif choice=="P":
		if firstP:
			firstP = False
			dicP = getDic(fDicPostalInfo)
			dicPtoM = getDic(fPtoM)
			dicPtoPM = getDic(fPtoPM)
		idP = input("idM (e.g. 1730) ")
		getFromIdP(idP, dicP, dicPtoM, dicPtoPM)
	elif choice=="S":
		if firstS:
			firstS = False
			dicS = getDic(fDicStreets)
		idS = input("idS (e.g. 58273) ")
		getFromIdS(idS, dicS)
	elif choice=="Q":
		loop=False # This will make the while loop to end as not value of loop is set to False
	else:
		input("Wrong option selection. Enter any key to try again..")



