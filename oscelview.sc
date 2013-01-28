(
var keyHandler;
var circlesize = 10;

~width = 1024;
~height = 768;

~users = Set.new;
~skels = Set.new;
~joints = Dictionary.new;
~skelColors = Dictionary.new;

s = {
	// add sample data
	~users = ~users.add(0);
	~skels = ~skels.add(0);
	~skelColors.put(0, Color.rand( 0.3,0.8));
	~joints = ~joints.put(0,Dictionary.new);
	~joints[0].add(\head       -> [0.5,0.8,0.5]);
	~joints[0].add(\neck       -> [0.5,0.7,0.5]);
	~joints[0].add(\l_shoulder -> [0.4,0.7,0.5]);
	~joints[0].add(\l_elbow    -> [0.3,0.6,0.5]);
	~joints[0].add(\l_hand     -> [0.2,0.5,0.5]);
	~joints[0].add(\r_shoulder -> [0.6,0.7,0.5]);
	~joints[0].add(\r_elbow    -> [0.7,0.6,0.5]);
	~joints[0].add(\r_hand     -> [0.8,0.5,0.5]);
	~joints[0].add(\torso      -> [0.5,0.6,0.5]);
	~joints[0].add(\l_hip      -> [0.4,0.5,0.5]);
	~joints[0].add(\l_knee     -> [0.4,0.3,0.5]);
	~joints[0].add(\l_foot     -> [0.3,0.1,0.5]);
	~joints[0].add(\r_hip      -> [0.6,0.5,0.5]);
	~joints[0].add(\r_knee     -> [0.6,0.3,0.5]);
	~joints[0].add(\r_foot     -> [0.7,0.1,0.5]);
};

//s.value;

w = Window.new("OSCeleton Viewer",Rect(100, 200, ~width, ~height),false);
v = UserView(w, w.view.bounds);
v.background_(Color.grey(0.97));

// compute absolute coords in userview
~getCoords = { | user, joint |
	[
		((1-(~joints.at(user).at(joint).at(0)))*~width),
		((1-(~joints.at(user).at(joint).at(1)))*~height)
	];
};

// draw the skeletons
v.drawFunc = {
	if (
		~skels.size > 0,
		{
			"users with skels: ".post;
			~skels.post;

			~skels.do { | user |
				Pen.color = ~skelColors.at(user);
				// draw a point for each joint of each user
				~joints.at(user).keys.iter.do { | joint |
					Pen.addOval(
						Rect(
							~getCoords.value(user,joint).at(0),
							~getCoords.value(user,joint).at(1),
							circlesize, circlesize;
						);
					);
					Pen.perform(\fill);
				}
			}
		},
		{ "no users with skeletons".post;}
	);
	// TODO link the joints
	", framerate: ".post;
	v.frameRate.postln;
};

// OSC Processing:

//OSCFunc.trace(true); // Turn posting on
//OSCFunc.trace(false); // Turn posting off

// from OSC Communication Guide - receive from variable source port
// (on NetAddr.localAddr )

// handle user add/remove, skel add
n = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		"user add ".post;
		~users.add(msg[1]).postln;
	}, '/new_user'
);

l = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		"user del ".post;
		~users.remove(msg[1]).postln;
		~skels.remove(msg[1]);
		~joints.remove(msg[1]);
		~skelColors.remove(msg[1]);
	}, '/lost_user'
);

s = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		var user = msg[1];
		"skel add ".post;
		~skels.add(user);
		~joints = ~joints.put(msg[1],Dictionary.new);
		~skelColors.put(user, Color.rand( 0.3,0.8));
	}, '/new_skel'
);

// handle joint positions
// /joint sifff "head" 1 0.525326 0.125740 1.560653

u = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		~joints.at(msg[1]).put(msg[0], [msg[2],msg[3],msg[4]]);
	}, '/joint'
);

keyHandler = { | view, char, modifier, unicode, keycode |
	n.free;
	l.free;
	s.free;
	o.free;    // remove the OSCresponderNode when you are done.
	v.animate = false; //animation can be paused and resumed
	w.close;
};
v.keyDownAction = keyHandler;

v.animate = true;
v.frameRate = 30;
w.front;

)