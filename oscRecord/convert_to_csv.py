#!/usr/bin/python
# vim:sw=4:ts=4:sts=4
# convert osc recordings in python shelve format to csv

import sys
import os
import shelve
import time
import gdbm
import csv

class ConvertToCSV:
    def __init__(self, infilename, outfilename):
        self.shelve=shelve.open(infilename)
		self.outfile = open(outfilename, 'wb')
		self.csvwriter = csv.writer(self.outfile, delimiter=',', quotechar="'")

    def convert(self):
        l=len(self.shelve)
        for i in range(l):
            if self.shelve.has_key(str(i)):
                print self.shelve[str(i)][1]
                data=self.shelve[str(i)][1]
                tmstmp=self.shelve[str(i)][0]
				self.csvwriter.writerow([tmstmp,data])
        self.shelve.close()        

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print "usage: " + sys.argv[0] + " input.db ouput.csv"
	    sys.exit(0)
            
    converter=ConvertToCSV(sys.argv[1], sys.argv[2])
    converter.convert()

