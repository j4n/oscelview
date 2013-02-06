(

var keyHandler;
var circlesize = 1.5;

~debug = False;
~width = 1024;
~height = 768;

~piconst = 3.14159265359; // sic

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
	[\l_knee, \l_foot],
	[\r_hip, \r_knee],
	[\r_knee, \r_foot]
];
~emphasizedJoints = Set[\head, \l_hand, \r_hand, \l_foot, \r_foot];

w = Window.new("OSCeleton Viewer",Rect(100, 200, ~width, ~height),false);
v = UserView(w, w.view.bounds);
v.background_(Color.grey(0.97));
v.frameRate = 10;

// compute absolute coords in userview
~getCoords = { | user, joint |
	[
		(((~joints.at(user).at(joint).at(0)))*~width),
		(((~joints.at(user).at(joint).at(1)))*~height)
	];
};

~norm = { |vect|
	var norm = 0;
	vect.do { | item, index |
		norm = norm + item.squared;
	};
	norm.sqrt;
};

~dot = { | v1, v2 |
	var product = 0;
	var comp = v1.size;
	comp.do { | i |
		product = product + (v1.at(i) * v2.at(i));
	};
	product;
};

~getAngle = { | v1, v2 |
	(
		(~dot.value(v1,v2)/
		(~norm.value(v1)*~norm.value(v2)))
	).acos;
};


~getAngleDegrees = { | v1, v2 |
	~getAngle.value(v1,v2) * 180 / ~piconst;
};

~getLimbs = { | user |
	var limbs = Dictionary.newFrom(List[
		\rThigh, ~joints.at(user).at(\r_hip) - ~joints.at(user).at(\r_knee),
		\rShank, ~joints.at(user).at(\r_knee) - ~joints.at(user).at(\r_foot),
		\lThigh, ~joints.at(user).at(\l_hip) - ~joints.at(user).at(\l_knee),
		\lShank, ~joints.at(user).at(\l_knee) - ~joints.at(user).at(\l_foot)
	]);
	limbs;
};

~getBodyLean = { | user |
	// ok whatever, just q&d:
	// what we really want is the x difference between the neck and the torso
	// scaled by should width
	var scale = (~joints.at(user).at(\r_shoulder).at(0) - ~joints.at(user).at(\l_shoulder).at(0))/2;
	var xdiff = ~joints.at(user).at(\neck).at(0) - ~joints.at(user).at(\torso).at(0);
	var lean = xdiff/scale;
	lean;
};

~getKneeAngles = { | user, limbs |
	var angles;

	angles = [
		~getAngleDegrees.value(limbs.at(\rThigh),limbs.at(\rShank)),
		~getAngleDegrees.value(limbs.at(\lThigh),limbs.at(\lShank))
	];

	(~debug == True).if {
		" knee angles r/l: ".post;
		angles.post;
	};
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
				var bodyLean, limbs, kneeAngles;
				Pen.color = ~skelColors.at(user);
				// draw a point for each joint of each user
				(~joints.includesKey(user)).if {
					~joints.at(user).keys.iter.do { | joint |
						var coords = ~getCoords.value(user,joint);
						var cs = (circlesize * ((5-~joints.at(user).at(joint).at(2)))).squared;
						/*(~joints.at(user).includesKey(\l_hip)).if ({
							~joints.at(user).at(\l_hip).at(2).post;
							" ".post;
							(~joints.at(user).at(\l_knee).at(2)).post;
							" ".post;
							(~joints.at(user).at(\l_knee).at(2) - (~joints.at(user).at(\l_hip).at(2))).postln;
						});*/

						Pen.addOval(
							Rect(
								coords.at(0)-(cs/2),
								coords.at(1)-(cs/2),
								cs, cs;
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

							// debug left knee
							if (
								(~debug == True) and: (([\l_hip, \l_knee].isSubsetOf(jointPair)) or: ([\l_knee, \l_foot].isSubsetOf(jointPair))),
								{
									Pen.width = 5;
									Pen.color = ~skelColors.at(user).complementary;
								},
								{
									Pen.width = 1;
									Pen.color = ~skelColors.at(user);
								}
							);

							Pen.line(
								coordsA.at(0)@coordsA.at(1),
								coordsB.at(0)@coordsB.at(1);
							);
						    Pen.fillStroke;

						};
					};
				};

				limbs = ~getLimbs.value(user);
				kneeAngles = ~getKneeAngles.value(user, limbs);
				bodyLean = ~getBodyAngle.value(user);
				// bodyLean.postln;

				(~debug == True).if {
					var r = 50;
					var offset = 20;
					// debug left knee
					var jointAngle = (180 - kneeAngles.at(1)) / 180 * ~piconst; // joint angle in radians

					Pen.color = ~skelColors.at(user).complementary;
					Pen.addOval(
						Rect(
							offset,
							(offset*user),
							2*r, 2*r;
						);
					);
					Pen.perform(\stroke);

					// vertical dial
					Pen.line(
						(offset+r)@(offset+r),
						(offset+r)@offset
					);
				    Pen.perform(\stroke);

					// angle dial
					Pen.line(
						(offset+r)@(offset+r),
						((offset+r)+(jointAngle.sin*r))@((offset+r)-((jointAngle.cos*r)))
					);
				    Pen.perform(\stroke);
					Pen.color = ~skelColors.at(user);
				};
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
		if ((~skelColors.includesKey(user)).not, { ~skelColors.put(user,Color.rand( 0.3,0.8)); });
		~joints.at(user).put(msg[1], [msg[3],msg[4],msg[5]]);
	}, '/joint'
);

Server.default = s = Server.internal;
// s.quit;
// s.boot;
s.waitForBoot{

	SynthDef( \trainer, { |pulse, mixL, mixR |

		var lo, hi, in;

		// two resonator banks, one high one low in frequency
		// being excited at a variable \pulse rat that also affects the resonance frequency
		// and combined at ratio \mix between the high/low frequency
		in = Decay2.ar(
			Impulse.ar( pulse.linlin(0.0, 1.0, 0.333, 10.0) ),
			pulse.linexp(0.0, 1.0, 0.001, 0.17), 0.2
		) * BrownNoise.ar(0.1);

		lo = {
			DynKlank.ar(
				`[ ( { |i|if(i==0,{1},{i})}!40).clump(2).flop[0],
					{ |i|1/20}!20 ,
					1!20 ],
				in,
				freqscale: 25,
				freqoffset: 100,
				decayscale: pulse.linexp(0.0, 1.0, 0.01, 2, 1)
			) * 0.4
		}!2;

		hi = {
			DynKlank.ar(
				`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[1],
					{|i|1/20}!20 , 1!20 ],
				in,
				freqscale: 100,
				freqoffset: 100,
				decayscale: pulse.linexp(0.0, 1.0, 0.01, 2, 1)
			) * 0.4
		};

		Out.ar(0, XFade2.ar(lo, hi, mixL.linlin(0.0, 1.0, -1, 1)));
		Out.ar(1, XFade2.ar(lo, hi, mixR.linlin(0.0, 1.0, -1, 1)));

	}).add;

	// initial synth setup
	~trainer = Synth(\trainer, [\pulse, 0.0, \mixL, 0.5, \mixR, 0.5]); // starting exercise sound only occasionally
	~trainer.setn(\pulse, 1, \mixL, 0.5, \mixR, 1.0);  // sound disappears
};

keyHandler = { | view, char, modifier, unicode, keycode |
	(unicode == 27).if { // escape key quits
		n.free;
		l.free;
		s.free;
		o.free;    //c remove the OSCresponderNode when you are done.
		v.animate = false; //animation can be paused and resumed
		~trainer.free;
		s.freeAll;
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
		~trainer.free;
	};
};

v.keyDownAction = keyHandler;

v.animate = true;
v.frameRate = 30;
w.front;
)
(
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