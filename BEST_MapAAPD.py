from BEST_Lib import *	

start = datetime.datetime.now()

src = "AAPD" 
inFile = SRC_AAPD_IN
outFile = SRC_AAPD_RESULT

print("mapping", src, inFile, outFile)
mapDb(src, inFile, outFile)

end = datetime.datetime.now()
print("start: ", start)
print("end: ", end)
print("duration:", end-start)
