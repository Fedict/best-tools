#-------------------------------------#
# Python script for BEST address      #
# Author: Marc Bruyland (FOD BOSA)    #
# Contact: marc.bruyland@bosa.fgov.be #
# June 2019                           #
#-------------------------------------#
from BEST_Lib import *	


bx="0002"
print(bx, transformBoxNr(bx))
bx="2/4"
print(bx, transformBoxNr(bx))
bx="2_4"
print(bx, transformBoxNr(bx))
bx="b2"
print(bx, transformBoxNr(bx))
bx="b002"
print(bx, transformBoxNr(bx))
bx="BUS2"
print(bx, transformBoxNr(bx))
bx="BU2"
print(bx, transformBoxNr(bx))
bx="BU002"
print(bx, transformBoxNr(bx))
bx="BUS0002"
print(bx, transformBoxNr(bx))
bx="A-2"
print(bx, transformBoxNr(bx))
bx="A-02"
print(bx, transformBoxNr(bx))
bx="B002"
print(bx, transformBoxNr(bx))
