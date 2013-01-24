#!/usr/bin/env python
from OSC import OSCServer
import sys, time
import threading
import string

listen_address = ('localhost',  7110)
server = OSCServer(listen_address)
server.timeout = 0
print server
run = True

debug = True

users = set()
skels = set()
positions = {}
joints = {}

def printing_handler(address, tags, stuff, source):
    msg_string = "%s [%s] %s" % (address,  tags,  str(stuff))
#    sys.stdout.write("OSCServer Got: '%s' from %s\n" % (msg_string,  source))
#    msg = OSCMessage("/printed")
#    msg.append(msg_string)
#    return msg

#def skel_handler(address, tags, content, source):
#
def user_handler(address, tags, content, source):
#    printing_handler(address, tags, content, source)
    if len(content) == 1:
        user = content[0]
        if address == '/new_user':
            users.add(user)
            if debug:
                print "new user " + str(user) + ". now tracking: " + ','.join(map(str,users))
        elif address == '/lost_user':
            users.remove(user)
            if skels.issubset([user]):
                skels.remove(user)
            if debug:
                print "lost user " + str(user) + ". now tracking: " + ','.join(map(str,users))
        elif address == '/new_skel': # calibration successful
            #/new_skel i 1
            skels.add(user)
            if debug:
                print "tracking skeleton for user " + ','.join(map(str,users)) + ". skeletons for tracking: " + ','.join(map(str,skels))
    else:
        # must be /user/, track 
        #/user/1 fff 0.538935 0.388257 1.625569
        user = string.split(address,'/')[2]
        positions[user] = content
        if debug:
            print "user " + str(user) + " now at " + ' '.join(map(str,content))

def joint_handler(address, tags, content, source):
    user = content[1]
    joint = content[0]
    joints[user][joint] = content[2:]
    #TODO continue here - OSCServer: KeyError on request from localhost:49366: 1
    if debug:
        print "joint " + joint + " for user " + str(user) + " now at " + ' '.join(map(str,content[2:]))
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



#server.addDefaultHandlers()
#server.addMsgHandler("/print", printing_handler)
#server.addMsgHandler("/new_skel", printing_handler)
server.addMsgHandler("/new_skel", user_handler)
server.addMsgHandler("/new_user", user_handler)
server.addMsgHandler("/lost_user", user_handler)
server.addMsgHandler("/user/1", user_handler)
server.addMsgHandler("/user/2", user_handler)
server.addMsgHandler("/user/3", user_handler)
server.addMsgHandler("/user/4", user_handler)
server.addMsgHandler("/user/5", user_handler)
server.addMsgHandler("/user/6", user_handler)
server.addMsgHandler("/user/7", user_handler)
server.addMsgHandler("/user/8", user_handler)
server.addMsgHandler("/user/9", user_handler)
server.addMsgHandler("/joint", joint_handler)
    
# Start OSCServer
print "\nStarting OSCServer. Use ctrl-C to quit."
st = threading.Thread( target = server.serve_forever )
st.start()

try:
    while True:
        time.sleep(30)
except KeyboardInterrupt:
    print "\nJoining Thread."
    st.join()
    print "\nClosing Server."
    server.close()
    print "Done\n"
    sys.exit(0)

# what we are expecting:
# 
#/user/1 fff 0.538935 0.388257 1.625569
#/new_skel i 1
