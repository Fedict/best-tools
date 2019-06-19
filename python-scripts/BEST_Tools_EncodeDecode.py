#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	

ENC_IN = "latin-1"
ENC_OUT = "utf-8"

filename = "./FOD_Economie/test.txt" 				#latin-1 encoded
print(filename, ENC_IN)
fileIn = open(filename,"r", encoding=ENC_IN)
line = fileIn.readline()
fileIn.close()
for c in line:
	print(c)
for c in bytes(line.encode(ENC_IN)):
	print(c)

filename = "./FOD_Economie/testConverted.txt"		#UTF-8 encoded
fileOut = open(filename,"w", encoding=ENC_OUT)
line2 = line.encode(ENC_OUT).decode(ENC_OUT)
print('converted:', line2, type(line2))
for c in line2:
	print(c)
for c in bytes(line2.encode(ENC_OUT)):
	print(c)
fileOut.write(line2)
fileOut.close()

print(filename, ENC_OUT)
fileIn = open(filename,"r", encoding=ENC_OUT)
line = fileIn.readline()
fileIn.close()
print(line)


# dicS = getDic(fMapStreetnames)
# for item in dicS['B21001'].items():
	# print(item)