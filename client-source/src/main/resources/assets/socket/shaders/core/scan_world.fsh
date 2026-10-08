#version 150
uniform sampler2D Sampler0, Sampler1;
uniform mat4 InverseViewProjection;
uniform vec3 Color;
uniform float Width;
uniform int Count;
uniform vec4 Wave0; uniform float Fade0;
uniform vec4 Wave1; uniform float Fade1;
uniform vec4 Wave2; uniform float Fade2;
uniform vec4 Wave3; uniform float Fade3;
uniform vec4 Wave4; uniform float Fade4;
uniform vec4 Wave5; uniform float Fade5;
uniform vec4 Wave6; uniform float Fade6;
uniform vec4 Wave7; uniform float Fade7;
uniform vec4 Wave8; uniform float Fade8;
uniform vec4 Wave9; uniform float Fade9;
uniform vec4 Wave10; uniform float Fade10;
uniform vec4 Wave11; uniform float Fade11;
in vec2 uv;
out vec4 fragColor;
void main() {
 vec4 base=texture(Sampler0,uv);
 float depth=texture(Sampler1,uv).r;
 if(depth>=.999999) { fragColor=base; return; }
 vec4 point=InverseViewProjection*vec4(uv*2.-1.,depth*2.-1.,1.);
 vec3 pos=point.xyz/point.w;
 float light=0.;
 for(int i=0;i<12;i++) {
  if(i>=Count) break;
  vec4 wave=vec4(0.); float fade=0.;
if(i==0) { wave=Wave0; fade=Fade0; }
if(i==1) { wave=Wave1; fade=Fade1; }
if(i==2) { wave=Wave2; fade=Fade2; }
if(i==3) { wave=Wave3; fade=Fade3; }
if(i==4) { wave=Wave4; fade=Fade4; }
if(i==5) { wave=Wave5; fade=Fade5; }
if(i==6) { wave=Wave6; fade=Fade6; }
if(i==7) { wave=Wave7; fade=Fade7; }
if(i==8) { wave=Wave8; fade=Fade8; }
if(i==9) { wave=Wave9; fade=Fade9; }
if(i==10) { wave=Wave10; fade=Fade10; }
if(i==11) { wave=Wave11; fade=Fade11; }
  float distance=length(pos-wave.xyz);
  float delta=abs(distance-wave.w);
  float band=1.-smoothstep(0.,Width,delta);
  float edge=1.-smoothstep(0.,max(.2,Width*.08),delta);
  float lines=.5+.5*sin(pos.y*12.);
  light+=fade*(band*.22+edge*.4+band*lines*.08);
 }
 fragColor=vec4(base.rgb+Color*min(light,.85),base.a);
}
