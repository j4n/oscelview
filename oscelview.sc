(

var keyHandler;
var circlesize = 1.5;

// todo comment
// todo fix nil thing on real data
// todo angles are not right: that should be 180-angle
//      or something like that. display is correct though

~debug = True;
~width = 640;
~height = 480;

~kneeOfInterest = 0; // [right,left]

~piconst = 3.14159265359; // sic
~maxlean = 0.4; // empiric maximum value for body lean
~minbend = 0; // empiric maximum value for knee bend in degrees
~maxbend = 45; // empiric maximum value for knee bend in degrees

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
~kneePairs = [[[\r_hip, \r_knee],[\r_knee, \r_foot]],[[\l_hip, \l_knee],[\l_knee, \l_foot]]];

w = Window.new("OSCeleton Viewer",Rect(100, 200, ~width, ~height),false);
v = UserView(w, w.view.bounds);
v.background_(Color.grey(0.97));
v.frameRate = 30;

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
	angles;
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
								(~debug == True) and:
								(
									(~kneePairs[~kneeOfInterest][0].isSubsetOf(jointPair)) or:
									(~kneePairs[~kneeOfInterest][1].isSubsetOf(jointPair))
								),
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

				// parameter mapping sonification
				limbs = ~getLimbs.value(user);
				kneeAngles = ~getKneeAngles.value(user, limbs);
				bodyLean = ~getBodyLean.value(user);

				~trainer.setn(\balance, bodyLean, \mix, bodyLean);

				// the more bend the knees are, the faster & higher the pulse -> smoother sound
				~trainer.setn(\pulse, kneeAngles[~kneeOfInterest].linlin(~minbend,~maxbend,0.25,1));

				(~debug == True).if {
					var r = 50;
					var offset = 20;
					// debug left knee
					var jointAngle = (180 - kneeAngles.at(~kneeOfInterest)) / 180 * ~piconst; // joint angle in radians
					//var jointAngle = kneeAngles.at(~kneeOfInterest);

					Pen.color = ~skelColors.at(user).complementary;
					Pen.addOval(
						Rect(
							offset,
							(offset),
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

					// 0 grad =   offset+r    @ offset
					// 90 grad =  offset+r+r  @ offset+r
					// 180 grad = offset+r    @ offset+(2*r)


					//offset+r + (jointAngle - (~piconst/4)).abs.linlin(0,(~piconst/4),0,r) @ jointAngle.linlin(0,(~piconst/2),offset,offset+(2*r))

					// angle dial - line from the center to a point between the top an the bottom along the right side
					Pen.line(
						(offset+r)@(offset+r), // center
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
	SynthDef( \trainer, { |pulse, balance=0, mix=0.5 |

		// turn balance into respective mixL/R ratios
		// ie. balance = 0:
		//  mixL = mixR =
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
		}!2;

		// balance adjusts how much we hear of the two synths:
		//  the more we lean, the sharper the sound becomes on the opposite side by adjusting the mixture of lo and hi

		Out.ar(0,XFade2.ar(lo[0], hi[0], mix.neg.linlin(~maxlean.neg, ~maxlean, -1,1))*balance.neg.linlin(~maxlean.neg,~maxlean,0,1));
		Out.ar(1,XFade2.ar(lo[1], hi[1], mix.linlin(~maxlean.neg, ~maxlean, -1,1))*balance.linlin(~maxlean.neg,~maxlean,0,1));

	}).add;

	// initial synth setup
	~trainer = Synth(\trainer, [\pulse, 0.0]); // starting exercise sound only occasionally
	~trainer.setn(\pulse, 1, \mix, 0, \balance, 0);  // sound disappears
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