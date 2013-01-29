#!/usr/bin/python

import sys
import socket
import OSC
import os
import shelve
from threading import Thread
import time
import gdbm

class OSCRecorder(Thread):

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def __init__(self, host, port):
        self.offset=time.time()
        self.host=host
        self.port=port
        self.subscriptions=[]
        self.rPort=7110
        Thread.__init__(self)

    def run(self):
        indx=0
        while self.running:
            data, address = self.s.recvfrom(1024)
            tmstmp=time.time()-self.offset
            self.offset=time.time()
            #self.file.write(data)
            #self.file.picke("\n")
            
            self.shelve[str(indx)]=[tmstmp,data]
            indx+=1
            
            print "index", indx, tmstmp
            if indx>=self.evtcount:
                self.running = 0
        self.shelve.close()
        self.unsubscribe()
        #self.shelve=shelve.open("shelve")
         
                

            
    def addSubscription(self, sub):
        self.subscriptions.append(sub)

    def subscribe(self):
        for subscription in self.subscriptions:
            m = OSC.OSCMessage()
            m.setAddress("/sub")
            m.append(self.rPort)
            m.append(subscription)
            oscdata = m.getBinary()
            self.sock.sendto(oscdata, (self.host, self.port))


    def unsubscribe(self):
        for subscription in self.subscriptions:
            m = OSC.OSCMessage()
            m.setAddress("/unsub")
            m.append(self.rPort)
            m.append(subscription)
            oscdata = m.getBinary()
            self.sock.sendto(oscdata, (self.host, self.port))       


    def startRecord(self, filename, evtcount):
        self.evtcount=evtcount
        self.shelve=shelve.open(filename)
        self.s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.s.bind(('', self.rPort))
        self.running=1
        self.start()
        
        
             
if __name__ == "__main__":

    if len(sys.argv) != 5:
        print "./recorder.py filename host port eventcount"
        sys.exit(0)
    
    r=OSCRecorder(sys.argv[2], int(sys.argv[3]))
    r.addSubscription("/user/*")
    r.addSubscription("/new_skel")
    r.addSubscription("/new_user")
    r.addSubscription("/lost_user")
    r.addSubscription("/joint")
    r.subscribe()
    r.startRecord(sys.argv[1], int(sys.argv[4]))
    
    try:
        while r.running:
            time.sleep(0.1)
    except KeyboardInterrupt:
	print "stop running"
        r.running = 0
	print "close shelve"
        r.shelve.close()


    
