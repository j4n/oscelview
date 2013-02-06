(
f = {
	var lo, hi, in;

	in = Decay2.ar(Impulse.ar(MouseY.kr(0.333, 10, 0)), MouseY.kr(0.001, 0.17, 1), 0.2 ) * BrownNoise.ar(0.1);
	//lo = DynKlank.ar(`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[0], {|i|1/20}!20 , 1!20 ], in, freqscale: 25, freqoffset: 100, decayscale: MouseY.kr(0.01, 2, 1) ) * 0.4;
	hi = DynKlank.ar(`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[1], {|i|1/20}!20 , 1!20 ], in, freqscale: 100, freqoffset: 100, decayscale: MouseY.kr(0.01, 2, 1) ) * 0.4;

//	XFade2.ar(lo, hi, MouseX.kr(-1, 1));

};
)
f.play
f.pause
//{[{ Blip.ar(300,4,0.1) }, { Blip.ar(500,4,0.1) }]}.play
s.freeAll()

(
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

	// balance adjusts how much we hear of the two synths
	// mix the mixture of lo and hi

	Out.ar(0,XFade2.ar(lo[0], hi[0], mix.neg.linlin(-1, 1, -0.75, 0.75))*balance.neg.linlin(-1,1,0,1));
	Out.ar(1,XFade2.ar(lo[1], hi[1], mix.linlin(-1, 1, -0.75, 0.75))*balance.linlin(-1,1,0,1));

}).add
)
//send(s)

// start synth

~trainer = Synth(\trainer, [\pulse, 0.0, \mixL, 0.5, \mixR, 0.5, \balance, 0]) // starting exercise sound only occasionally
~trainer = Synth(\trainer, [\pulse, 0.0, \balance, 0]) // starting exercise sound only occasionally
~trainer.setn(\pulse, 1, \balance, 0)  // sound disappears
~trainer.free

b = -0.5;
~trainer.setn(\balance, b, \mix, b)
// our mapping is:
// knee bend -> \pulse
// upper body straightness: \balance & \mix (more high the more leaning)

// multichannel
// moving upper body down, change ion timbre
~trainer.setn(\pulse, 0.7, \balance, 0)

// bending knees change in speed and timbre
~trainer.setn(\pulse, 0.3, \mixL, 0.0, \mixR, 0.0)
~trainer.setn(\pulse, 0.6, \mixL, 0.0, \mixR, 0.0)
~trainer.setn(\pulse, 0.9, \mixL, 0.0, \mixR, 0.0)

// ideal position (knees bent upper body low)
~trainer.setn(\pulse, 1, \mixL, 0.5, \mixR, 1.0)  // sound disappears

// knees bent but upper body going straight
~trainer.setn(\pulse, 1.0, \mixL, 0.2, \mixR, 0.2)
~trainer.setn(\pulse, 1.0, \mixL, 0.6, \mixR, 0.6)
~trainer.setn(\pulse, 1.0, \mixL, 1.0, \mixR, 1.0)

// set both variables
~trainer.setn(\pulse, 1.0, \mixL, 1.0, \mixR, 0.0)
~trainer.setn(\pulse, 1.0, \mixL, 0.0, \mixR, 1.0)

~trainer.setn(\pulse, 1.0, \balance, 1 )


// set them individually
~trainer.set(\pulse, 1)
~trainer.set(\mixL, 0.0)
~trainer.set(\mixR, 0.0)

// singlechannel
// moving upper body down, change ion timbre
~trainer.setn(\pulse, 0.0, \mix, 0.0)

// bending knees change in speed and timbre
~trainer.setn(\pulse, 0.3, \mix, 0.0)
~trainer.setn(\pulse, 0.6, \mix, 0.0)
~trainer.setn(\pulse, 0.9, \mix, 0.0)

// ideal position (knees bent upper body low)
~trainer.setn(\pulse, 1.0, \mix, 0.0)  // sound disappears

// knees bent but upper body going straight
~trainer.setn(\pulse, 1.0, \mix, 0.2)
~trainer.setn(\pulse, 1.0, \mix, 0.6)
~trainer.setn(\pulse, 1.0, \mix, 1.0)

// set both variables
~trainer.setn(\pulse, 1.0, \mix, 1.0)
~trainer.setn(\pulse, 1.0, \mix, 0.0)

// set them individually
~trainer.set(\pulse, 1)
~trainer.set(\mix, 0.0)
