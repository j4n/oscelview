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

    def play(self, host, port):
        self.host=host
        self.port=port
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        l=len(self.shelve)
        t=0
        for i in range(l):
            if self.shelve.has_key(str(i)):
                tmstmp=self.shelve[str(i)][0]
                t += tmstmp
                data=self.shelve[str(i)][1]
                #print str(i) + " @ " + str(t) + " s +" + str(tmstmp) + " s " #+ str(data[:20])
                print '% 8d @ % 8.4f s + %.4f s - %s' % (i,t,tmstmp,data[:30])
                time.sleep(tmstmp)
                self.s.sendto(data, (self.host, self.port))
        self.shelve.close()        

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
        print "    " + sys.argv[0] + " filename host port start end # (play subset)"
        sys.exit(0)
    elif len(sys.argv) >= 2:
        player=OSCPlayer(sys.argv[1])
        player.getreclength();
        if len(sys.argv) >= 4:
            if len(sys.argv) == 6:
                start = sys.argv[4]
                end = sys.argv[5]
            player.play(sys.argv[2], int(sys.argv[3]))

       
    
