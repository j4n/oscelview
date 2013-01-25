(
var width = 1024, height = 768;
var keyHandler;

w = Window.new("OSCeleton Viewer",Rect(100, 200, width, height),false);
v = UserView(w, w.view.bounds);
v.background_(Color.grey);

keyHandler = { | view, char, modifier, unicode, keycode |
	w.close;
};

v.keyDownAction = keyHandler;

w.front;

)