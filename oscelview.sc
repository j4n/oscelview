(
var keyHandler;
var circlesize = 10;

~width = 1024;
~height = 768;

~joints = Dictionary.new;
~joints.add(\head       -> [0.5,0.8,0.5]);
~joints.add(\neck       -> [0.5,0.7,0.5]);
~joints.add(\l_shoulder -> [0.4,0.7,0.5]);
~joints.add(\l_elbow    -> [0.3,0.6,0.5]);
~joints.add(\l_hand     -> [0.2,0.5,0.5]);
~joints.add(\r_shoulder -> [0.6,0.7,0.5]);
~joints.add(\r_elbow    -> [0.7,0.6,0.5]);
~joints.add(\r_hand     -> [0.8,0.5,0.5]);
~joints.add(\torso      -> [0.5,0.6,0.5]);
~joints.add(\l_hip      -> [0.4,0.5,0.5]);
~joints.add(\l_knee     -> [0.4,0.3,0.5]);
~joints.add(\l_foot     -> [0.3,0.1,0.5]);
~joints.add(\r_hip      -> [0.6,0.5,0.5]);
~joints.add(\r_knee     -> [0.6,0.3,0.5]);
~joints.add(\r_foot     -> [0.7,0.1,0.5]);

w = Window.new("OSCeleton Viewer",Rect(100, 200, ~width, ~height),false);
v = UserView(w, w.view.bounds);
v.background_(Color.grey);
~getCoords = { | joint |
	//j= \head;
//	[((1-(~joints.at(j).at(0))) *~width), ((1-(~joints.at(j).at(1)))*~height)];
	[((1-(~joints.at(joint).at(0)))*~width), ((1-(~joints.at(joint).at(1)))*~height)];
};

//~getCoords.value(\head);

v.drawFunc = {
	Pen.color = Color.rand;

 	// draw a point for each joint
	~joints.keys.iter.do { | joint |
		Pen.addOval(Rect(~getCoords.value(joint).at(0), ~getCoords.value(joint).at(1), circlesize, circlesize));
		Pen.perform(\fill);
 	}

	// link the joints

};

keyHandler = { | view, char, modifier, unicode, keycode |
	w.close;
};

v.keyDownAction = keyHandler;

w.front;

)