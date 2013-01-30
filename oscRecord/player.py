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
        self.filename=filename
        self.shelve=shelve.open(self.filename)

    def __del__(self):
        self.shelve.close()        

    def play(self, host, port, start, end):
        self.host=host
        self.port=port
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

        if (start == -1):
            start = 0
        
        if (end == -1):
            end = len(self.shelve)

        r = range(start,end);
        t = 0
        for i in r:
            if self.shelve.has_key(str(i)):
                tmstmp=self.shelve[str(i)][0]
                data=self.shelve[str(i)][1]
                #print str(i) + " @ " + str(t) + " s +" + str(tmstmp) + " s " #+ str(data[:20])
                print '% 8d @ % 8.4f s + %.4f s - %s' % (i,t,tmstmp,data[:30])
                time.sleep(tmstmp)
                t += tmstmp
                self.s.sendto(data, (self.host, self.port))

    def getreclength(self):
        length = 0;
        for r in self.shelve.values():
            length += r[0]; 
        print "total " + str(len(self.shelve)) + " records, " + str(length) + "s long"

if __name__ == "__main__":
    if len(sys.argv) == 1:
        print "usage: "
        print "    " + sys.argv[0] + " filename # (show info)"
        print "    " + sys.argv[0] + " filename host port # (play)"
        print "    " + sys.argv[0] + " filename host port start,end start,-1# (play subsets)"
        sys.exit(0)
    elif len(sys.argv) >= 2:
        player=OSCPlayer(sys.argv[1])
        player.getreclength();

        if len(sys.argv) >= 4:
            if len(sys.argv) >= 5:
                for i in (range(len(sys.argv) - 4)):
                    [start,end] = map(int,(sys.argv[4+i]).split(','))
                    player.play(sys.argv[2], int(sys.argv[3]), start, end)
            else:
                start = -1
                end = -1
                player.play(sys.argv[2], int(sys.argv[3]), start, end)
