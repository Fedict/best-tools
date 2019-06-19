#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *				
#FILENAMES===============================================================
# region = 'B'
# filename = filenameDic[region]['municipality']
# filename = filenameDic[region]['street']
# filename = filenameDic[region]['address']
# filename = filenameDic[region]['postalInfo']
# filename = filenameDic[region]['partOfMunicipality']

# filename = fLstMunicipalities
# filename = fLstStreets
# filename = fLstAddresses
# filename = fLstPostalInfo
# filename = fLstPartOfMunicipalities

# filename = fDicMunicipalities
# filename = fDicStreets
# filename = fDicAddresses
# filename = fDicPostalInfo
# filename = fDicPartOfMunicipalities

#XML STATISTICS===
#xmlType in ['address', 'municipality', 'partOfMunicipality', 'street', 'postalInfo' ]
# xmlStatistics('street')


#CREATION MUNICIPALITY===

# lst = makeNationalListMunicipalities()
# printLst(lst)

# dicM = makeDicMunicipalities(fLstMunicipalities)
# printDic(dicM)

# dicM = getDicM(fDicMunicipalities)
# printDicMunicipalitiesStatistics(dicM)

# dicM = getDicM(fDicMunicipalities)
# outputMunicipalities(dicM)

#CREATION PARTOFMUNICIPALITY=
# lst = makeNationalListPartOfMunicipalities()
# printLst(lst)

# dicPM = makeDicPartOfMunicipalities(fLstPartOfMunicipalities)
# printDic(dicPM)

# dicPM = makeDicPartOfMunicipalities(fLstPartOfMunicipalities)
# printDicPartOfMunicipalitiesStatistics(dicPM)

# dicPM = makeDicPartOfMunicipalities(fLstPartOfMunicipalities)
# outputPartOfMunicipalities(dicPM)

#CREATION STREET=========
# lst = makeRegionalListStreets('B')
# printLst(lst)

# lst = makeNationalListStreets()
# printLst(lst)

# dicM = getDicM(fDicMunicipalities)
# dicS = makeDicStreets(dicM)
# printDic(dicS)	

# dicS = getDicS(fDicStreets)
# printDicStreetsStatistics(dicS)

# dicM = getDicM(fDicMunicipalities)
# dicS = getDicS(fDicStreets)
# outputStreets(dicS) #straight to file

#CREATION POSTALINFO=====
# lst = makeRegionalListPostalInfo('B')
# printLst(lst)

# lst = makeNationalListPostalInfo()
# printLst(lst)

# dicP = makeDicPostalInfo(fLstPostalInfo)
# printDic(dicP)

# dicP = makeDicPostalInfo(fLstPostalInfo)
# printDicPostalInfoStatistics(dicP)

# dicP = makeDicPostalInfo(fLstPostalInfo)
# outputPostalInfo(dicP)

#CREATION ADDRESS-LST====
# dicM = getDicM(fDicMunicipalities)
# dicS = getDicS(fDicStreets)
# dicP = makeDicPostalInfo(fLstPostalInfo)
# dicPM = makeDicPartOfMunicipalities(fLstPartOfMunicipalities)
# lst = makeNationalListAddresses(dicM, dicS, dicPM, dicP)
# for item in lst:
	# print(item)

# lst = makeRegionalListAddresses('W',dicM, dicS, dicPM, dicP)	
# for item in lst:
	# print(item)

# #sxx = set of
# sHs, sBx, sHsBx = getNumbers(fLstAddresses)
# print('Housenumber', len(sHs))
# print(sHs)
# print('Boxnumber', len(sBx))
# print(sBx)
# print('Housenumber_Boxnumber', len(sHsBx))
# print(sHsBx)

#CREATION ADDRESS-DIC====
#create LST first, redirect the output from storeDicA 
# dicA = makeDicAddresses()
# storeDicA(dicA)

# dicA = getDicA(fDicAddresses)
# printDicAddressesStatistics(dicA)

#printDic(getPartOfMunicipalities(fDicAddresses))

#CREATE EXTRACT FROM ADDRESS-DIC (TEST PURPOSE: EVERY 100TH ADDRESS) =====
# dicA = getSlowDicA(fDicAddresses)
# dicA2 = {}
# i=0
# for R, dicR in dicA.items():
	# dicA2[R] = {}
	# for aCode, dicAddress in dicR.items():
		# first = True
		# for v, dicV in dicAddress.items():
			# if i % 100 == 0:
				# if first:
					# dicA2[R][aCode] = {}
					# first = False
				# dicA2[R][aCode][v] = dicV
			# break
		# i += 1
# storeDicA(dicA2)

#CREATE EXTRACT FROM ADDRESS-DIC (TEST PURPOSE: ALL ADRESSES OF A GIVEN STREET) =====
# dicA = getSlowDicA(fDicAddresses)
# dicA2 = {}
# R = 'F' #Flanders
# idM = '23002' #Asse
# idS = '24377' #Mollestraat
# dicA2 = {}
# dicA2[R] = {}
# for aCode, dicAddress in dicA[R].items():
	# first = True
	# for v, dicV in dicAddress.items():
		# if dicA[R][aCode][v]['idM'] == idM and dicA[R][aCode][v]['idS'] == idS:
			# if first:
				# dicA2[R][aCode] = {}
				# first = False
			# dicA2[R][aCode][v] = dicV
# storeDicA(dicA2)

#CREATE BEST DIC =====
# dicAddress = getSlowDicA(fDicAddresses)
# dicBEST = {}
# for R, dicR in dicAddress.items():
	# for A, dicA in dicR.items():
		# V = getLastVersion(dicR, A)
		# idA = R + '_' + A + '_' + V
		# idM = dicA[V]['idM']
		# idS = dicA[V]['idS']
		# hs = dicA[V]['hs']
		# bx = dicA[V]['bx']
		# if bx == '':
			# nrs = R + '__' + hs
		# else:
			# nrs = R + '__' + hs + '__' + bx
		# if R not in dicBEST: dicBEST[R] = {}
		# if idM not in dicBEST[R]: dicBEST[R][idM] = {}
		# if idS not in dicBEST[R][idM]: dicBEST[R][idM][idS] = {}
		# dicBEST[R][idM][idS][nrs] = idA
# storeDicA(dicBEST)


	
#READ=======================================================================
#READ MUNICIPALITY===

# showPartOfFile(filename,214801)

# print(dicM)

# dicM = getDicM(fDicMunicipalities)
# printLst(convertMunicipality(dicM, 'Etterbeek'))
# printLst(convertMunicipality(dicM, 'Asse'))
# printLst(convertMunicipality(dicM, 'Neufchâteau'))

# dicM = getDicM(fDicMunicipalities)
# print(getRegion(dicM, '23002', 'Asse'))

# dicM = getDicM(fDicMunicipalities)
# region = 'B'
# printDic(dicM[region])
# lastVersion = getLastVersion(dicM[region], '7500')
# print(dicM[region]['7500'][lastVersion])		

# dicM = getDicM(fDicMunicipalities)
# R = 'B'
# idM = '8600'
# vM = getLastVersion(dicM[R],idM)
# print(R+'-'+idM, dicM[R][idM])
# print('last version ' + vM, dicM[R][idM][vM])

#READ PARTOFMUNICIPALITY=
# region = 'W'
# file=open(filenameDic[region]['partOfMunicipality'],"r", encoding=PREFERRED_ENCODING) #PREFERRED_ENCODING: redirection caused problems
# line = file.readline()
# cntFound=0
# while line:
	# print(line.strip())
	# cntFound+=1
	# line = file.readline()
# file.close()
# print(cntFound, 'lines')

#READ STREET=========
# showPartOfFile(filename,214801)


# print(dicS['F']['24376']['-']['nlM'])

# #find exceptions on status
# strCheck = '<com:status>'
# strDefault = 'current'
# for region in ['B', 'F', 'W']:
	# filename = filenameDic[region]['street']
	# filterExceptions(filename, strCheck, strDefault)	

# #find exceptions on streetnameType 
# strCheck = 'streetnameType'
# strDefault = '>streetname<'
# for region in ['B', 'F', 'W']:
	# filename = filenameDic[region]['street']
	# filterExceptions(filename, strCheck, strDefault)	

# lookupStringInFile(filenameDic['W']['street'], 'hamlet')
# dicS = getDicS(fDicStreets)
# lst = []
# for R, Rd in dicS.items():
	# for id, idd in Rd.items():
		# for v, vd in idd.items():
			# if vd['sType'] != 'streetname':
				# str = 'idS:'+id + ','
				# for key, val in vd.items():
					# if key in ['frM', 'nlM', 'deM']:
						# str = val + ':' + str
					# else:	
						# str = str + key + ':' + val + ','
				# lst.append(str[:-1])
# for item in sorted(lst):
	# print(item)

# Test
# with open(fDicStreets) as f:
	# print(f.read(1))

# dicS = getDicS(fDicStreets)
# printLst(convertStreet(dicS, 'Mollemseweg'))
# printLst(convertStreet(dicS, 'Rue du Devoir'))
# printLst(convertStreet(dicS, 'Korte Heizelstraat'))

# dicM = getDicM(fDicMunicipalities)
# dicS = getDicS(fDicStreets)
# printLst(getListMunicipalityStreets(dicM, dicS, 'Asse'))
# printLst(getListMunicipalityStreets(dicM, dicS, 'Aalst'))
# printLst(getListMunicipalityStreets(dicM, dicS, 'Andenne'))
# printLst(getListMunicipalityStreets(dicM, dicS, 'Etterbeek'))

# lookupStringInFile(filenameDic['B']['street'], 'homonym')
# lookupStringInFile(filenameDic['F']['street'], 'homonym')
# lookupStringInFile(filenameDic['W']['street'], 'homonym')
# showPartOfFile(filenameDic['F']['street'],1710497)


# lookupStringInFile(filenameDic['F']['street'], 'homonym')
# dicS = getDicS(fDicStreets)
# lst = []
# for R, Rd in dicS.items():
	# for id, idd in Rd.items():
		# for v, vd in idd.items():
			# if vd['ho'] != '':
				# print(vd['ho'])
			# if vd['ho'] != '':
				# str = 'idS:'+id
				# for key, val in vd.items():
					# if key in ['frM', 'nlM', 'deM']:
						# str = val + ':' + str
					# else:	
						# str = str + ',' + key + ':' + val
				# lst.append(str)
# for item in sorted(lst):
	# print(item)

# dicS = getDicS(fDicStreets)
# lst = []
# cnt = 0
# for R, Rd in dicS.items():
	# for id, idd in Rd.items():
		# for v, vd in idd.items():
			# if 'nl' in vd:
				# if vd['ho'] != '':
					# print(R,id,v,vd)
					# cnt += 1
# print(cnt, 'homonyms')

# file=open(fLstStreets,"r", encoding=PREFERRED_ENCODING)
# lineCounter=0
# try:
	# line = file.readline()
# except:
	# print(lineCounter,"Unexpected error:")
# cntOccurrences=0
# s = "'ho': ''"
# while line:
	# lineCounter += 1
	# if s not in line:
		# cntOccurrences += 1
		# print(lineCounter, line.rstrip().lstrip())
	# try:
		# line = file.readline()
	# except:
		# print(lineCounter, "Unexpected error:")
# s = '"'+s+'"'
# print('Found', cntOccurrences, 'occurrences of', s, 'in', fLstStreets, '(total number of lines:', lineCounter,')')
# file.close()
	
#lookupStringInFile(filenameDic['F']['street'], "37526")	
#showPartOfFile(filenameDic['F']['street'],656116)
# print('---------------------------------------------------')
# showPartOfFile(filenameDic['F']['street'],44092)

# lookupStringInFile(fLstStreets, 'hamlet')	
# lookupStringInFile(fOutStreets, 'F,Asse')	
# lookupStringInFile(filenameDic['W']['street'], '<com:streetnameType>')
# lookupStringInFile(fDicStreets, '454')	
	
#READ POSTALINFO=====

#lookupStringInFile(filenameDic['B']['postalInfo'], '>1040<')

# region = 'W'
# file=open(filenameDic[region]['postalInfo'],"r", encoding=PREFERRED_ENCODING)
# line = file.readline()
# cntFound=0
# while line:
	# print(line.strip())
	# cntFound+=1
	# line = file.readline()
# file.close()
# print(cntFound, 'lines')

# #list of postcodes
# dicP = makeDicPostalInfo(fLstPostalInfo)
# lst = []
# for R, dicR in dicP.items():
	# for id, dicId in dicR.items():
		# lastVersion = getLastVersion(dicR, id)
		# strNl = ''
		# strFr = ''
		# strDe = ''
		# if 'nl' in dicId[lastVersion]:
			# strNl = dicId[lastVersion]['nl']
		# if 'fr' in dicId[lastVersion]:
			# strFr = dicId[lastVersion]['fr']
		# if 'de' in dicId[lastVersion]:
			# strDe = dicId[lastVersion]['de']
		# str = R + '-' + id + '-' + strNl + ',' + strFr + ',' + strDe
		# lst.append(str)
# for item in sorted(lst):
	# print(item)

#READ ADDRESS-XML=======
#lookupStringInFile(filenameDic['B']['address'], '82456244')
#showPartOfFile(filenameDic['B']['address'], 10923345)

#READ ADDRESS-LST=======
# showPartOfFile(filename,214801)

#lookupStringInFile(fLstAddresses, 'duplex op 1ste en 2de ')

# filename = fLstAddresses
# lookupStringInFile(filename, "'idA': '586565'")
# lookupStringInFile(filename, "'idS': '24377'") #Mollestraat
# lookupStringInFile(filename, "7700056") #personal address 2265425

# filename = ''
# showPartOfFile(filename,214801)

# file=open(fLstAddresses,"r", encoding=PREFERRED_ENCODING)
# line = file.readline()
# cntFound=0
# cntF = 0
# while line:
	# if "'R': 'F'" in line:
		# cntF += 1
		# print(line)
		# # if  "'vM': ''" in line:
			# # #print(line)
			# # cntFound+=1
	# # dic = {}
	# # dic = ast.literal_eval(line)
	# # print(dic['idA'], dic['M'], dic['S'], dic['hs'], dic['bx'], dic['lan'])
	# line = file.readline()
# file.close()
# print('Found', cntF, cntFound)

# file=open(fLstAddresses,"r", encoding=PREFERRED_ENCODING)
# line = file.readline()
# total = 0
# cntF = 0
# cntB = 0
# cntW = 0
# cntUnknown = 0
# while line:
	# if "'R': 'F'" in line:
		# cntF += 1
	# elif "'R': 'W'" in line:
		# cntW += 1
	# elif "'R': 'B'" in line:
		# cntB += 1
	# else:
		# cntUnknown += 1
	# line = file.readline()
# file.close()
# total = cntF + cntB + cntW + cntUnknown
# print('Total - B - F - W - unknown')
# print('{:,} : {:,} - {:,} - {:,} - {:,}'.format(total, cntB, cntF, cntW, cntUnknown))

# file=open(fAddresses,"r", encoding=PREFERRED_ENCODING)
# i=0
# line = file.readline()
# while i<2000:
	# print(line)
	# line = file.readline()
	# i +=1
# file.close()

#READ ADDRESS-DIC=SLOW=====
# time measurement: loading of dicA takes 11 minutes - accessing dicA is <1 second
# ts0 = time.time()
# print('reading full Address dictionary..')
# dicA = getSlowDicA(fDicAddresses)
# ts1 = time.time()
# print(ts1 - ts0, 'accessing dictionary')
# print(dicA['F']['2265425'])
# ts2 = time.time()
# print(ts2 - ts1, 'accessing dictionary')
# print(dicA['W']['586565'])
# ts3 = time.time()
# print(ts3 - ts2, 'end')


#For each region, count ISSUE in names of Municipality, PartOfMunicipality, StreetName, PostalInfo 
# print('reading full Address dictionary..')
# dicA = getSlowDicA(fDicAddresses)
# print('accessing dictionary')
# cnt=0
# cntS = 0
# cntS_B = 0
# cntS_F = 0
# cntS_W = 0
# cntM = 0
# cntM_B = 0
# cntM_F = 0
# cntM_W = 0
# cntP = 0
# cntP_B = 0
# cntP_F = 0
# cntP_W = 0
# cntPM = 0
# cntPM_B = 0
# cntPM_F = 0
# cntPM_W = 0
# for R, dicR in dicA.items():
	# for idA, dicId in dicR.items():
		# for v, dicV in dicId.items():
			# S = dicV['Snl']+dicV['Sfr']+dicV['Sde']
			# M = dicV['Mnl']+dicV['Mfr']+dicV['Mde']
			# PM = dicV['PMnl']+dicV['PMfr']+dicV['PMde']
			# P = dicV['Pnl']+dicV['Pfr']+dicV['Pde']
			# if 'ISSUE' in S:
				# cntS += 1
				# if R == 'B':
					# cntS_B +=1
				# if R == 'F':
					# cntS_F +=1
				# if R == 'W':
					# cntS_W +=1
			# if 'ISSUE' in M:
				# cntM += 1
				# if R == 'B':
					# cntM_B +=1
				# if R == 'F':
					# cntM_F +=1
				# if R == 'W':
					# cntM_W +=1
			# if 'ISSUE' in PM:
				# cntPM += 1
				# if R == 'B':
					# cntPM_B +=1
				# if R == 'F':
					# cntPM_F +=1
				# if R == 'W':
					# cntPM_W +=1
			# if 'ISSUE' in P:
				# cntP += 1
				# if R == 'B':
					# cntP_B +=1
				# if R == 'F':
					# cntP_F +=1
				# if R == 'W':
					# cntP_W +=1
# print('ISSUES (total-B-F-W)')
# print('Mun:', cntM, cntM_B, cntM_F, cntM_W)
# print('PartOfMun:', cntPM, cntPM_B, cntPM_F, cntPM_W)
# print('Street:', cntS, cntS_B, cntS_F, cntS_W)
# print('PostalInfo:', cntP, cntP_B, cntP_F, cntP_W)

#Unknown streetid's in 'F' 
# print('reading full Address dictionary..')
# ts0 = time.time()
# dicA = getSlowDicA(fDicAddresses)
# ts1 = time.time()
# print(ts1 - ts0, 'seconds')
# print('accessing dictionary')
# lst = []
# dic = {}
# for idA, dicId in dicA['F'].items():
	# for v, dicV in dicId.items():
		# S = dicV['Snl']+dicV['Sfr']+dicV['Sde']
		# if 'ISSUE' in S:
			# M = dicV['Mnl']
			# idS = dicV['idS']
			# s= M + '-' + idS + '-' + idA
			# lst.append(s)
			# st = dicV['st']
			# if st in dic:
				# dic[st] += 1
			# else:
				# dic[st] = 1
# ts2 = time.time()
# print(ts2 - ts1, 'seconds')
# print('ISSUE in streetname: M-idS-idA')
# print(sorted(lst))
# print('ISSUE in streetname: stats')
# total = 0
# for k,v in dic.items():
	# print('status', k, ':', v, 'occurrences')
	# total += v
# print('total:', total)

#houseNr and boxNr statistics 
# print('reading full Address dictionary..')
# ts0 = time.time()
# dicA = getSlowDicA(fDicAddresses)
# ts1 = time.time()
# print(ts1 - ts0, 'seconds')
# print('accessing dictionary')
# dicBx = {'total':0 ,'B':0 , 'F':0 , 'W':0}
# dicHs = {'total':0 ,'B':0 , 'F':0 , 'W':0}
# for R, dicR in dicA.items():
	# for idA, dicId in dicR.items():
		# for v, dicV in dicId.items():
			# hs = dicV['hs']
			# if hs != '':
				# dicHs[R] += 1
				# dicHs['total'] += 1
			# bx = dicV['bx']
			# if bx != '':
				# dicBx[R] += 1
				# dicBx['total'] += 1
# ts2 = time.time()
# print(ts2 - ts1, 'seconds')
# print('Stats housenumber')
# printDic(dicHs)
# print('Stats boxnumber')
# printDic(dicBx)

#analysis of housenrs and boxnrs
# print('reading full Address dictionary..')
# ts0 = time.time()
# dicA = getSlowDicA(fDicAddresses)
# ts1 = time.time()
# print(ts1 - ts0, 'seconds')
# print('accessing dictionary')
# dicHs = {'Nat':{} ,'B':{}, 'F':{} , 'W':{}}
# dicBx = {'Nat':{} ,'B':{}, 'F':{} , 'W':{}}
# for R, dicR in dicA.items():
	# for idA, dicId in dicR.items():
		# for v, dicV in dicId.items():
			# hs = dicV['hs']
			# if hs in dicHs['Nat']:
				# dicHs['Nat'][hs] += 1
			# else:
				# dicHs['Nat'][hs] = 1
			# if hs in dicHs[R]:
				# dicHs[R][hs] += 1
			# else:
				# dicHs[R][hs] = 1
				
			# bx = dicV['bx']
			# if hs in dicBx['Nat']:
				# dicBx['Nat'][hs] += 1
			# else:
				# dicBx['Nat'][hs] = 1
			# if hs in dicBx[R]:
				# dicBx[R][hs] += 1
			# else:
				# dicBx[R][hs] = 1
# ts2 = time.time()
# print(ts2 - ts1, 'seconds')

# dicHs = {'Nat':{'12':137, '15':200, '13':56} ,'B':{}, 'F':{} , 'W':{}}
# sorted_Hs_Nat = sorted(dicHs['Nat'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Hs_Nat)

#Full analysis of housenrs and boxnrs
# print('reading full Address dictionary..')
# dicA = getSlowDicA(fDicAddresses)
# dicHs = {'Nat':{} ,'B':{}, 'F':{} , 'W':{}}
# dicBx = {'Nat':{} ,'B':{}, 'F':{} , 'W':{}}
# for R, dicR in dicA.items():
	# for idA, dicId in dicR.items():
		# for v, dicV in dicId.items():
			# hs = dicV['hs']
			# if hs in dicHs['Nat']:
				# dicHs['Nat'][hs] += 1
			# else:
				# dicHs['Nat'][hs] = 1
			# if hs in dicHs[R]:
				# dicHs[R][hs] += 1
			# else:
				# dicHs[R][hs] = 1
				
			# bx = dicV['bx']
			# if bx in dicBx['Nat']:
				# dicBx['Nat'][bx] += 1
			# else:
				# dicBx['Nat'][bx] = 1
			# if bx in dicBx[R]:
				# dicBx[R][bx] += 1
			# else:
				# dicBx[R][bx] = 1
# print('accessing dictionary')
# print('Hs=Nat============================================================================================')
# sorted_Hs_Nat = sorted(dicHs['Nat'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Hs_Nat)
# print('Hs=B============================================================================================')
# sorted_Hs_B = sorted(dicHs['B'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Hs_B)
# print('Hs=F============================================================================================')
# sorted_Hs_F = sorted(dicHs['F'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Hs_F)
# print('Hs=W============================================================================================')
# sorted_Hs_W = sorted(dicHs['W'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Hs_W)
# print('Bx=Nat============================================================================================')
# sorted_Bx_Nat = sorted(dicBx['Nat'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Bx_Nat)
# print('Bx=B============================================================================================')
# sorted_Bx_B = sorted(dicBx['B'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Bx_B)
# print('Bx=F============================================================================================')
# sorted_Bx_F = sorted(dicBx['F'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Bx_F)
# print('Bx=W============================================================================================')
# sorted_Bx_W = sorted(dicBx['W'].items(), key=operator.itemgetter(1))
# printTuples(sorted_Bx_W)

# Make sample of housenrs and boxnrs
# print('reading full Address dictionary..')
# ts0 = time.time()
# dicA = getSlowDicA(fDicAddresses)
# ts1 = time.time()
# print(ts1 - ts0, 'seconds')
# print('accessing dictionary')
# lst = []
# for R, dicR in dicA.items():
	# lim = len(dicR)
	# limHs = 100
	# limBx = 125
	# if R == 'B':
		# limBx = 50
	# i=0
	# iHs=0
	# iBx=0
	# lstKeys = []
	# for item in dicR.keys():
		# lstKeys.append(item)
	# print('***', R, lim, limHs, limBx, i, len(lstKeys))
	# while iHs < limHs and i<lim:
	# #loop for hs alone
		# address = dicR[lstKeys[i]]
		# lstAddressVersions = []
		# for item in address.keys():
			# lstAddressVersions.append(item)
		# addressData = address[lstAddressVersions[0]]
		# hs = addressData['hs']
		# bx = addressData['bx']
		# if bx == '':	#Flanders: first part without bx, second part with bx !
			# lst.append(R + '__' + hs )
			# iHs += 1
		# i += 1
	# print('***', R, lim, limHs, limBx, i, len(lstKeys))
	# while iBx < limBx and i<lim:
	# #loop for hs+bx
		# address = dicR[lstKeys[i]]
		# lstAddressVersions = []
		# for item in address.keys():
			# lstAddressVersions.append(item)
		# addressData = address[lstAddressVersions[0]]
		# hs = addressData['hs']
		# bx = addressData['bx']
		# if bx != '':
			# lst.append(R + '__' + hs + '__' + bx)
			# iBx += 1
		# i += 1
# ts2 = time.time()
# print(ts2 - ts1, 'seconds')
# for item in sorted(lst):
	# print(item)

# Get housenrs and boxnrs from an Address datadictionary extract (e.g. dic with addresses of a given street)
# dicAddress = getSlowDicA('DIC_AddressesMollestraat.txt')
# lst = []
# for R, dicR in dicAddress.items():
	# for A, dicA in dicR.items():
		# V = getLastVersion(dicR, A)
		# hs = dicA[V]['hs']
		# bx = dicA[V]['bx']
		# if bx == '':
			# lst.append(R + '__' + hs)
		# else:
			# lst.append(R + '__' + hs + '__' + bx)
# for item in sorted(lst):
	# print(item)


#max length
# print('reading full Address dictionary..')
# dicA = getSlowDicA(fDicAddresses)
# print('accessing dictionary')
# dic = {'M':0,'PM':0,'S':0,'P':0,'hs':0,'bx':0}
# dicName = {'M':'','PM':'','S':'','P':'','hs':'', 'bx':''} 
# for R, dicR in dicA.items():
	# for idA, dicId in dicR.items():
		# for v, dicV in dicId.items():
			# Mnl = dicV['Mnl']
			# Mfr = dicV['Mfr']
			# Mde = dicV['Mde']
			# PMnl = dicV['PMnl']
			# PMfr = dicV['PMfr']
			# PMde = dicV['PMde']
			# Snl = dicV['Snl']
			# Sfr = dicV['Sfr']
			# Sde = dicV['Sde']
			# Pnl = dicV['Pnl']
			# Pfr = dicV['Pfr']
			# Pde = dicV['Pde']
			# hs = dicV['hs']
			# bx = dicV['bx']
			# if len(Mnl) > dic['M']:
				# dic['M'] = len(Mnl)
				# dicName['M'] = Mnl
			# if len(Mfr) > dic['M']:
				# dic['M'] = len(Mfr)
				# dicName['M'] = Mfr
			# if len(Mde) > dic['M']:
				# dic['M'] = len(Mde)
				# dicName['M'] = Mde
			# if len(PMnl) > dic['PM']:
				# dic['PM'] = len(PMnl)
				# dicName['PM'] = PMnl
			# if len(PMfr) > dic['PM']:
				# dic['PM'] = len(PMfr)
				# dicName['PM'] = PMfr
			# if len(PMde) > dic['PM']:
				# dic['PM'] = len(PMde)
				# dicName['PM'] = PMde
			# if len(Snl) > dic['S']:
				# dic['S'] = len(Snl)
				# dicName['S'] = Snl
			# if len(Sfr) > dic['S']:
				# dic['S'] = len(Sfr)
				# dicName['S'] = Sfr
			# if len(Sde) > dic['S']:
				# dic['S'] = len(Sde)
				# dicName['S'] = Sde
			# if len(Pnl) > dic['P']:
				# dic['P'] = len(Pnl)
				# dicName['P'] = Pnl
			# if len(Pfr) > dic['P']:
				# dic['P'] = len(Pfr)
				# dicName['P'] = Pfr
			# if len(Pde) > dic['P']:
				# dic['P'] = len(Pde)
				# dicName['P'] = Pde
			# if len(hs) > dic['hs']:
				# dic['hs'] = len(hs)
				# dicName['hs'] = hs
			# if len(bx) > dic['bx']:
				# dic['bx'] = len(bx)
				# dicName['bx'] = bx
# print('Max length statistics')
# print('Municipality','PartOfMunicipality', 'Street', 'Postalname', 'houseNr', 'boxNr')
# printDic(dic)
# printDic(dicName)


#READ ADDRESS-DIC=FAST=====
# print('reading limited Address dictionary..')
# dicA = getFastDicA(fDicAddresses)

#check if there are addresses with >1 version in the address id
# file=open(fDicAddresses,"r", encoding=PREFERRED_ENCODING)
# line = file.readline()
# cnt = 0
# while line:
	# cnt += 1
	# if line.count('idS') > 1:
	# #if ("B-506628" in line) or ("F-232815" in line) or ("W-8255" in line):
		# print(line)
	# line = file.readline()
# file.close()
# print('{:,} lines'.format(cnt))


# print('reading limited Address dictionary..')
# dicA = getFastDicA(fDicAddresses)

# file=open(fDicAddresses,"r", encoding=PREFERRED_ENCODING)
# line = file.readline()
# while line:
	# if ("Auderghem" in line) and ("Anderlecht" in line):
		# print(line)
	# line = file.readline()
# file.close()

# print('reading limited Address dictionary..')
# dicA = getFastDicA(fDicAddresses)
# print('mapping address..')
# mapAddress(dicA, 'F','Asse', 'Mollemseweg','12','')
# mapAddress(dicA, 'F','Asse', 'Mollestraat','8','48')
		
# READ BEST DIC =====
# dicBEST = getSlowDicA('DIC_BEST.txt')
# print(dicBEST['F']['35011']['55274']['F__12'] , 'F_2265425_-')

#OTHER===(relation between Mun - PartOfMun - PostalInfo======================================

# dicM = getDicM(fDicMunicipalities)
# #print(makeString(getDataLastVersion('F', '23002', dicM)))	

# dicPM = makeDicPartOfMunicipalities(fLstPartOfMunicipalities)
# #print(makeString(getDataLastVersion('W', '1499', dicPM)))	

# dicP = makeDicPostalInfo(fLstPostalInfo)
# #print(makeString(getDataLastVersion('F', '1500', dicP)))

# dicA = getSlowDicA(fDicAddresses)
# cnt= 0 
# lst = []
# for R, dicR in dicA.items():
	# for objId, dicId in dicR.items():
		# maxVersion = getLastVersion(dicR, objId)
		# if maxVersion != '?':
			# strM = makeString(getDataLastVersion(R, dicId[maxVersion]['idM'], dicM))
			# strPM = makeString(getDataLastVersion(R, dicId[maxVersion]['idPM'], dicPM))
			# strP = makeString(getDataLastVersion(R, dicId[maxVersion]['idP'], dicP))
			# triple = (strM, strPM, strP)
			# if triple not in lst:
				# lst.append(triple)
		# else:
			# print('VERSION ISSUE ', R,objId, dicId)
# dic_M_PM = {}
# dic_M_P = {}
# dic_PM_M = {}
# dic_PM_P = {}
# dic_P_M = {}
# dic_P_PM = {}
# for triple in lst:
	# m, pm, p = triple
	
	# if m not in dic_M_PM:
		# dic_M_PM[m] = []
	# if pm not in dic_M_PM[m]:
		# dic_M_PM[m].append(pm)
	
	# if m not in dic_M_P:
		# dic_M_P[m] = []
	# if p not in dic_M_P[m]:
		# dic_M_P[m].append(p)
	
	# if pm not in dic_PM_M:
		# dic_PM_M[pm] = []
	# if m not in dic_PM_M[pm]:
		# dic_PM_M[pm].append(m)
	
	# if pm not in dic_PM_P:
		# dic_PM_P[pm] = []
	# if p not in dic_PM_P[pm]:
		# dic_PM_P[pm].append(p)
	
	# if p not in dic_P_M:
		# dic_P_M[p] = []
	# if m not in dic_P_M[p]:
		# dic_P_M[p].append(m)
	
	# if p not in dic_P_PM:
		# dic_P_PM[p] = []
	# if pm not in dic_P_PM[p]:
		# dic_P_PM[p].append(pm)
# print('******* M > PM---------')	
# printDic(dic_M_PM)
# print('******* M > P---------')	
# printDic(dic_M_P)
# print('******* PM > M---------')	
# printDic(dic_PM_M)
# print('******* PM > P---------')	
# printDic(dic_PM_P)
# print('******* P > M---------')	
# printDic(dic_P_M)
# print('******* P > PM---------')	
# printDic(dic_P_PM)

# filename = 'MAP_BoxNrsRR.txt'
# dic = getDic(filename)
# print(dic["F24207_238"])



# showStringInFile(filename, 'F24250_2A')
# showStringInFile(filename, '72025')
# showStringInFile(filename, '72029')

# lookupStringInFile('FlandersAddress.xml', '5313610')	
# lookupStringInFile('FlandersAddress.xml', '4867353')	
#showPartOfFile(filenameDic['B']['street'],42210)
#KBO source file===================================================================================================
#PREPARE INPUTFILE FROM FOD KBO-FILE===================================================================	
#KEY,OND_NR,C_TYPE_ADRES,D_BEGINDATUM,D_EINDDATUM,C_NIS_GEMEENTECODE,GEMEENTENAAM_F,GEMEENTENAAM_NL,GEMEENTENAAM_D,C_POSTCODE,C_STRAATCODE,STRAATNAAM_VOLL,STRAATNAAM_F,STRAATNAAM_NL,STRAATNAAM_D,HUISNUMMER,BUSNUMMER
# def checkEnddate(inputFile):
	# fileIn = open(inputFile,"r", encoding=PREFERRED_ENCODING)
	# line = fileIn.readline()
	# cnt = 1
	# cntActual = 0
	# cnt2019 = 0
	# cntHistoric = 0
	# while line:
		# lst = line.split(",")
		# year = lst[4][1:5]	#enddate
		# if year == "9999":
			# cntActual +=1
		# elif year > "2018":
			# cnt2019 += 1
		# else:
			# cntHistoric +=1
		
		
		# line = fileIn.readline() 
		# cnt += 1
	# print(cnt)
	# print("Actual", cntActual)
	# print("2019", cnt2019)
	# print("Historic", cntHistoric)
	# fileIn.close()
# def checkStreetNames(inputFile):
	# fileIn = open(inputFile,"r", encoding=PREFERRED_ENCODING)
	# line = fileIn.readline()
	# cnt = 0
	# cntName = 0
	# cntNameF = 0
	# cntNameD = 0
	# cntNameG = 0
	# while line:
		# lst = line.split(",")
		# name = lst[11]
		# nameF = lst[12]
		# nameD = lst[13]
		# nameG = lst[14]
		# if name != "":
			# cntName +=1
		# if nameF != "":
			# cntNameF +=1
		# if nameD != "":
			# cntNameD +=1
		# if nameG != "":
			# cntNameG +=1

		
		
		# line = fileIn.readline() 
		# cnt += 1
	# print(cnt)
	# print("name", cntName)
	# print("nameF", cntNameF)
	# print("nameD", cntNameD)
	# print("nameG", cntNameG)
	# fileIn.close()
# def checkStreetCode(inputFile):
	# fileIn = open(inputFile,"r", encoding=PREFERRED_ENCODING)
	# line = fileIn.readline()
	# cnt = 0
	# cntStreetCode = 0
	# while line:
		# lst = line.split(",")
		# streetCode = lst[10]
		# if streetCode != "":
			# cntStreetCode +=1
		
		# line = fileIn.readline() 
		# cnt += 1
	# print(cnt)
	# print("cntStreetCode", cntStreetCode)

	# fileIn.close()
#filename = "KBO.txt"
#checkEnddate(filename)
#checkStreetNames(filename)
#checkStreetCode(filename)

def cntLinesInFile(filename):
	file=open(filename,"r", encoding=PREFERRED_ENCODING)
	lineCounter=0
	try:
		line = file.readline()
	except:
		print(lineCounter,"Unexpected error:")
	while line:
		lineCounter += 1
		try:
			line = file.readline()
		except:
			print(lineCounter, "Unexpected error:")
	file.close()
	return lineCounter
filename = "ConsolidatedResult_B.txt"
print('Found', cntLinesInFile(filename), 'lines in', filename)
