#!/usr/bin/python
# vim:sw=4:ts=4:sts=4:expandtab

import sys
import socket
import OSC
import os
import shelve
from threading import Thread
import time
import gdbm

class OSCPlayer:
    def __init__(self, filename):
        self.filename = filename
        self.shelve = shelve.open(self.filename)
        self.speedup = 1#0 # want to go faster?

    def __del__(self):
        self.shelve.close()        

    def play(self, host, port, start, end):
        self.host = host
        self.port = port
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

        if (start == -1):
            start = 0
        
        if (end == -1):
            end = len(self.shelve)

        r = range(start,end);
        t = self.getreclength(start)
        for i in r:
            if self.shelve.has_key(str(i)):
                tmstmp=self.shelve[str(i)][0]
                data=self.shelve[str(i)][1]
                print '% 8d @ % 8.4f s + %.4f s - %s' % (i,t,tmstmp,data[:30])
                time.sleep(tmstmp/self.speedup)
                t += tmstmp
                self.s.sendto(data, (self.host, self.port))

    def getreclength(self,end=-1):
        length = 0
        if (end == -1):
            end = len(self.shelve)
        for i in range(0,end):
            length += self.shelve[str(i)][0]
        return length

    def printtotal(self):
        print "total " + str(len(self.shelve)) + " records, " + str(self.getreclength()) + "s long"

def printusage():
        print "usage: "
        print "    " + sys.argv[0] + " filename # (show info)"
        print "    " + sys.argv[0] + " filename host port # (play)"
        print "    " + sys.argv[0] + " filename host port start,end start,-1# (play subsets)"

if __name__ == "__main__":
    if len(sys.argv) == 1:
        printusage();
        sys.exit(0)
    elif len(sys.argv) >= 2:
        player=OSCPlayer(sys.argv[1])
        player.printtotal();

        if len(sys.argv) >= 4:
            if len(sys.argv) >= 5:
                # parse time pairs
                for i in (range(len(sys.argv) - 4)):
                    try:
                        [start,end] = map(int,(sys.argv[4+i]).split(','))
                        player.play(sys.argv[2], int(sys.argv[3]), start, end)
                    except ValueError:
                        printusage()
            else:
                start = -1
                end = -1
                player.play(sys.argv[2], int(sys.argv[3]), start, end)
