#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

nParams = len(sys.argv) - 1		#-1 cfr sys.argv[0] contains the name of the python script
if nParams < 1:
	print("required parameters: key [RR]")
	print("key for house numbers: R+idS")
	print("key for box numbers: R+idS_hs")
	print("RR is an optional parameter: this will allow to use a deviating mapping file specially for the RR")
	print("example 1:   BEST_Tools_GetNumbers.py B56724     will extract all house nrs for street id 56724 in region B")
	print("example 1:   BEST_Tools_GetNumbers.py B57022_28  will extract all box nrs for house nr 28 in street id 57022 in region B")
	quit()

key = sys.argv[1]
useRR = False
if nParams == 2:
	param2 = sys.argv[2]
	if param2 == "RR":
		useRR = True

print("reading dictionary..")
if "_" in key:
	if useRR:
		dic = getDic(fMapBoxNrsRR)
	else:
		dic = getDic(fMapBoxNrs)
	print(key, dic[key])
else:
	if useRR:
		dic = getDic(fMapHouseNrsRR)
	else:
		dic = getDic(fMapHouseNrs)
	print(key, dic[key])
