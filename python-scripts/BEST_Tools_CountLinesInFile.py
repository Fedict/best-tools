#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *				

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

nParams = len(sys.argv) - 1		#-1 cfr sys.argv[0] contains the name of the python script
if nParams < 1:
	print("required parameters: filename")
	print("example:   python BEST_Tools_CountLinesInFile.py test.txt")
	quit()

filename = sys.argv[1]
print('Found', '{0:,.0f}'.format(cntLinesInFile(filename)), 'lines in', filename)
