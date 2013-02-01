
/*
f = {
	var lo, hi, in;

	in = Decay2.ar(Impulse.ar(MouseY.kr(0.333, 10, 0)), MouseY.kr(0.001, 0.17, 1), 0.2 ) * BrownNoise.ar(0.1);
	lo = DynKlank.ar(`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[0], {|i|1/20}!20 , 1!20 ], in, freqscale: 25, freqoffset: 100, decayscale: MouseY.kr(0.01, 2, 1) ) * 0.4;
	hi = DynKlank.ar(`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[1], {|i|1/20}!20 , 1!20 ], in, freqscale: 100, freqoffset: 100, decayscale: MouseY.kr(0.01, 2, 1) ) * 0.4;

	XFade2.ar(lo, hi, MouseX.kr(-1, 1));

};
f.play
f.freeAll*/
//{[{ Blip.ar(300,4,0.1) }, { Blip.ar(500,4,0.1) }]}.play
//s.freeAll


(
SynthDef( \trainer, { |pulse = 0, mix = 0|

	var lo, hi, in;

	in = Decay2.ar(Impulse.ar( pulse.linlin(0.0, 1.0, 0.333, 10.0) ), pulse.linexp(0.0, 1.0, 0.001, 0.17), 0.2 ) * BrownNoise.ar(0.1);
	lo = {DynKlank.ar(`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[0], {|i|1/20}!20 , 1!20 ], in, freqscale: 25,  freqoffset: 100,  decayscale: pulse.linexp(0.0, 1.0, 0.01, 2, 1) ) * 0.4}!2;
	hi = {DynKlank.ar(`[ ({|i|if(i==0,{1},{i})}!40).clump(2).flop[1], {|i|1/20}!20 , 1!20 ], in, freqscale: 100, freqoffset: 100,  decayscale: pulse.linexp(0.0, 1.0, 0.01, 2, 1) ) * 0.4}!2;

	Out.ar(0, XFade2.ar(lo, hi, mix.linlin(0.0, 1.0, -1, 1)) );

}).add
)
//send(s)

// start synth

~trainer = Synth(\trainer, [\pulse, 0.0, \mix, 0.5]) // starting exercise sound only occasionally
~trainer.free
// moving upper body down, change ion timbre
~trainer.setn(\pulse, 0.0, \mix, 0.0)

// bending knees change in speed and timbre
~trainer.setn(\pulse, 0.3, \mix, 0.0)
~trainer.setn(\pulse, 0.6, \mix, 0.0)
~trainer.setn(\pulse, 0.9, \mix, 0.0)

// ideal position (knees bent upper body low)
~trainer.setn(\pulse, 1.0, \mix, 0.0)  // sound disappears

// knees bent but upperbody going straight
~trainer.setn(\pulse, 1.0, \mix, 0.2)
~trainer.setn(\pulse, 1.0, \mix, 0.6)
~trainer.setn(\pulse, 1.0, \mix, 1.0)

// set both variables
~trainer.setn(\pulse, 1.0, \mix, 1.0)
~trainer.setn(\pulse, 1.0, \mix, 0.0)

// set them individually
~trainer.set(\pulse, 1)
~trainer.set(\mix, 0.0)
