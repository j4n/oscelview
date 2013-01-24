#!/usr/bin/env python
from OSC import OSCServer
import sys, time
import threading

listen_address = ('localhost',  7110)
server = OSCServer(listen_address)
server.timeout = 0
print server
run = True

def printing_handler(addr, tags, stuff, source):
    msg_string = "%s [%s] %s" % (addr,  tags,  str(stuff))
    sys.stdout.write("OSCServer Got: '%s' from %s\n" % (msg_string,  source))
    print msg_string
#    msg = OSCMessage("/printed")
#    msg.append(msg_string)
#    return msg
for addr in server.getOSCAddressSpace():
    print addr

#server.addDefaultHandlers()
server.addMsgHandler("/print", printing_handler)

# Start OSCServer
print "\nStarting OSCServer. Use ctrl-C to quit."
st = threading.Thread( target = server.serve_forever )
st.start()

try:
    while True:
        time.sleep(30)
except KeyboardInterrupt:
    print "\nClosing Server."
    server.close()
    st.join()
    print "Done\n"
    sys.exit(0)

# what we are expecting:
# 
#/user/1 fff 0.538935 0.388257 1.625569
#/new_skel i 1
#/joint sifff "head" 1 0.525326 0.125740 1.560653
#/joint sifff "neck" 1 0.528710 0.240041 1.633263
#/joint sifff "l_shoulder" 1 0.593798 0.240170 1.665221
#/joint sifff "l_elbow" 1 0.683203 0.306789 1.710689
#/joint sifff "l_hand" 1 0.694053 0.168238 1.565233
#/joint sifff "r_shoulder" 1 0.463622 0.239911 1.601305
#/joint sifff "r_elbow" 1 0.364084 0.288804 1.590542
#/joint sifff "r_hand" 1 0.340634 0.137830 1.499758
#/joint sifff "torso" 1 0.528559 0.354930 1.633449
#/joint sifff "l_hip" 1 0.564872 0.469891 1.651539
#/joint sifff "l_knee" 1 0.578119 0.714963 1.640230
#/joint sifff "l_foot" 1 0.602688 0.934798 1.712727
#/joint sifff "r_hip" 1 0.491942 0.469746 1.615731
#/joint sifff "r_knee" 1 0.479989 0.705186 1.639544
#/joint sifff "r_foot" 1 0.476240 0.915310 1.711533
#/joint sifff "head" 1 0.518250 0.122592 1.563950
#/joint sifff "neck" 1 0.520766 0.240922 1.638120
#/joint sifff "l_shoulder" 1 0.585376 0.241941 1.669528
#/joint sifff "l_elbow" 1 0.680072 0.298008 1.715785
#/joint sifff "l_hand" 1 0.686509 0.154935 1.581096
#/joint sifff "r_shoulder" 1 0.456155 0.239902 1.606713
#/joint sifff "r_elbow" 1 0.357034 0.286876 1.594318
#/joint sifff "r_hand" 1 0.334126 0.133106 1.509748
#/joint sifff "torso" 1 0.519834 0.356538 1.637346
#/joint sifff "l_hip" 1 0.558572 0.472780 1.655856
#/joint sifff "l_knee" 1 0.574116 0.714026 1.646794
#/joint sifff "l_foot" 1 0.602660 0.931185 1.707684
#/joint sifff "r_hip" 1 0.479231 0.471529 1.617287
#/joint sifff "r_knee" 1 0.477765 0.706613 1.639394
