#version 150
uniform float Time;
uniform float Mode;
uniform float Scale;
uniform float Intensity;
uniform vec3 Tint;
uniform vec2 Camera;
uniform float Aspect;
uniform float Fov;
uniform float Opacity;
uniform vec3 Background;
uniform vec3 Neon1;
uniform vec3 Neon2;
uniform vec3 Neon3;
in vec2 uv;
out vec4 fragColor;
float hash(vec3 p) { return fract(sin(dot(p,vec3(127.1,311.7,74.7)))*43758.5453); }
float noise(vec3 p) {
    vec3 i=floor(p), f=fract(p); f=f*f*(3.0-2.0*f);
    return mix(mix(mix(hash(i),hash(i+vec3(1,0,0)),f.x),mix(hash(i+vec3(0,1,0)),hash(i+vec3(1,1,0)),f.x),f.y),
               mix(mix(hash(i+vec3(0,0,1)),hash(i+vec3(1,0,1)),f.x),mix(hash(i+vec3(0,1,1)),hash(i+vec3(1,1,1)),f.x),f.y),f.z);
}
void main() {
    vec2 p=(uv*2.0-1.0)*vec2(Aspect,1.0)*Fov;
    vec3 d=normalize(vec3(p,1.0));
    float cp=cos(Camera.y),sp=sin(Camera.y),cy=cos(Camera.x),sy=sin(Camera.x);
    d=vec3(d.x,d.y*cp-d.z*sp,d.y*sp+d.z*cp);
    d=vec3(d.x*cy-d.z*sy,d.y,d.x*sy+d.z*cy);
    vec3 q=d*Scale;
    float t=Time*.12;
    float n=noise(q+vec3(t,0,0))*.55+noise(q*2.0-vec3(0,t,0))*.3+noise(q*4.0)*.15;
    vec3 c=Tint*.025;
    if (Mode < .5) c += Tint*pow(n,3.0)*1.8 + Tint.bgr*pow(n,8.0);
    else if (Mode < 1.5) {
        float band=exp(-abs(d.y-.25-.13*sin(d.x*8.0+t*3.0))*12.0);
        c += mix(Tint,vec3(.2,1.0,.55),.5)*band*(.3+.7*n)*max(0.0,sin(q.x*4.0+n*4.0+t));
    } else if (Mode < 2.5) {
        vec3 cell=floor(d*400.0); float star=step(.997,hash(cell));
        c += Tint*star*(.65+.35*sin(t*8.0+hash(cell)*100.0));
    } else if (Mode < 3.5) {
        float wave=sin(q.x+t)+sin(q.y-t)+sin(q.z+sin(t));
        c += Tint*(.5+.5*sin(wave*3.0+n*4.0))*n;
    } else {
        vec3 weights=.5+.5*cos(vec3(0,2,4)+n*10.0+t);
        c += (Neon1*weights.x+Neon2*weights.y+Neon3*weights.z)*pow(n,2.0)*1.5;
    }
    fragColor=vec4(mix(Background,c*Intensity,Opacity),1.0);
}
