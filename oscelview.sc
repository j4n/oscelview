(
var keyHandler;
var circlesize = 10;

~debug = False;
~width = 1024;
~height = 768;

// ~users = Set.new; // currently not used
~skels = Set.new;
~joints = Dictionary.new;
~skelColors = Dictionary.new;

~jointLinks = Set[
	[\l_hand, \l_elbow],
	[\l_elbow, \l_shoulder],
	[\l_shoulder, \neck],
	[\neck, \head],
	[\neck, \r_shoulder],
	[\r_shoulder, \r_elbow],
	[\r_elbow, \r_hand],
	[\l_shoulder, \torso],
	[\r_shoulder, \torso],
	[\torso, \l_hip],
	[\torso, \r_hip],
	[\l_hip, \r_hip],
	[\l_hip, \l_knee],
	[\r_hip, \r_knee],
	[\l_knee, \l_foot],
	[\r_knee, \r_foot]
];

~emphasizedJoints = Set[\head, \l_hand, \r_hand, \l_foot, \r_foot];

w = Window.new("OSCeleton Viewer",Rect(100, 200, ~width, ~height),false);
v = UserView(w, w.view.bounds);
v.background_(Color.grey(0.97));

// compute absolute coords in userview
~getCoords = { | user, joint |
	[
		(((~joints.at(user).at(joint).at(0)))*~width),
		(((~joints.at(user).at(joint).at(1)))*~height)
	];
};

// draw the skeletons
v.drawFunc = {
	if (
		~skels.size > 0,
		{
			(~debug == True).if {
				"users with skels: ".post;
				~skels.post;
			};

			~skels.do { | user |
				Pen.color = ~skelColors.at(user);
				// draw a point for each joint of each user
				(~joints.includesKey(user)).if {
					~joints.at(user).keys.iter.do { | joint |
						var coords = ~getCoords.value(user,joint);
						Pen.addOval(
							Rect(
								coords.at(0)-(circlesize/2),
								coords.at(1)-(circlesize/2),
								circlesize, circlesize;
							);
						);

						if (
							[joint].isSubsetOf(~emphasizedJoints),
							{Pen.perform(\fill);},
							{Pen.perform(\stroke);}
						);

						~jointLinks.do { | jointPair |
							var coordsA = ~getCoords.value(user,jointPair[0]);
							var coordsB = ~getCoords.value(user,jointPair[1]);
							Pen.line(
								coordsA.at(0)@coordsA.at(1),
								coordsB.at(0)@coordsB.at(1);
							);
						};
					};
				}
			};
		},
		{
			(~debug == True).if {
				"no users with skeletons".post;
			}
		}
	);

	(~debug == True).if {
		", framerate: ".post;
		v.frameRate.postln;
	};
};

// OSC Processing:

// handle user add/remove, skel add
// n = OSCFunc(
// 	{
// 		arg msg, time, addr, recvPort;
// 		(~debug == True).if {
// 			"user add ".post;
// 			msg[1].postln;
// 		};
// 		~users.add(msg[1]);
// 	}, '/new_user'
// );

l = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		(~debug == True).if {
			"user del ".post;
			msg[1].postln;
		};
		// ~users.remove(msg[1]);
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
		user.postln;
		~skels.add(user);
		~joints = ~joints.put(user,Dictionary.new);
		~skelColors.put(user, Color.rand( 0.3,0.8));
	}, '/new_skel'
);

// handle joint positions
// /joint sifff "head" 1 0.525326 0.125740 1.560653
u = OSCFunc(
	{
		arg msg, time, addr, recvPort;
		var user = msg[2];
		~skels = ~skels.add(user); // make sure we have that one
		if ((~joints.includesKey(user)).not, { ~joints.put(user,Dictionary.new); });
		~joints.at(user).put(msg[1], [msg[3],msg[4],msg[5]]);
	}, '/joint'
);

keyHandler = { | view, char, modifier, unicode, keycode |
	(unicode == 27).if { // escape key quits
		n.free;
		l.free;
		s.free;
		o.free;    //c remove the OSCresponderNode when you are done.
		v.animate = false; //animation can be paused and resumed
		w.close;
	};

	(char == $d).if {
		if (
			~debug == True,
			{ ~debug = False; },
			{ ~debug = True; }
		)
	};

	(char == $c).if {
		// ~users.clear;
		~skels.clear;
		~joints.clear;
		~skelColors.clear;
	};
};

v.keyDownAction = keyHandler;

v.animate = true;
v.frameRate = 30;
w.front;

s = {
	// add sample data
	// ~users = ~users.add(0);
	~skels = ~skels.add(0);
	~skelColors.put(0, Color.rand( 0.3,0.8));
	~joints = ~joints.put(0,Dictionary.new);
	~joints[0].add(\head       -> [0.5,0.2,0.5]);
	~joints[0].add(\neck       -> [0.5,0.3,0.5]);
	~joints[0].add(\l_shoulder -> [0.6,0.3,0.5]);
	~joints[0].add(\l_elbow    -> [0.7,0.4,0.5]);
	~joints[0].add(\l_hand     -> [0.8,0.5,0.5]);
	~joints[0].add(\r_shoulder -> [0.4,0.3,0.5]);
	~joints[0].add(\r_elbow    -> [0.3,0.4,0.5]);
	~joints[0].add(\r_hand     -> [0.2,0.5,0.5]);
	~joints[0].add(\torso      -> [0.5,0.4,0.5]);
	~joints[0].add(\l_hip      -> [0.6,0.5,0.5]);
	~joints[0].add(\l_knee     -> [0.6,0.7,0.5]);
	~joints[0].add(\l_foot     -> [0.7,0.9,0.5]);
	~joints[0].add(\r_hip      -> [0.4,0.5,0.5]);
	~joints[0].add(\r_knee     -> [0.4,0.7,0.5]);
	~joints[0].add(\r_foot     -> [0.3,0.9,0.5]);
};

//s.value;
)