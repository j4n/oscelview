#!/usr/bin/python

import sys
import socket
import OSC
import os
import shelve
from threading import Thread
import time
import gdbm

class OSCPlayer:
    def __init__(self, filename, host, port):
        self.filename=filename
        self.shelve=shelve.open(self.filename)
        self.host=host
        self.port=port
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def play(self):
        l=len(self.shelve)
        for i in range(l):
            if self.shelve.has_key(str(i)):
                print self.shelve[str(i)]
                data=self.shelve[str(i)][1]
                tmstmp=self.shelve[str(i)][0]
                time.sleep(tmstmp)
                self.s.sendto(data, (self.host, self.port))
        self.shelve.close()        

if __name__ == "__main__":
    print len(sys.argv)
    if len(sys.argv) != 4:
        print "usage: " + sys.argv[0] + " filename host port"
        sys.exit(0)
            
    player=OSCPlayer(sys.argv[1], sys.argv[2], int(sys.argv[3]))
    player.play()

       
    
