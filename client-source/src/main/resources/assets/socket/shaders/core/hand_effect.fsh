#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3, Sampler4;
uniform int DepthMask;
uniform vec2 Texel;
uniform vec3 Color;
uniform float Time, Opacity, BlurRadius, Saturation, Brightness, Distortion, Tint, Glow;
uniform int Style, Shimmer;
in vec2 uv;
out vec4 fragColor;
float mask(vec2 p) {
    if(DepthMask==1) {
        float worldDepth=texture(Sampler3,p).r, handDepth=texture(Sampler4,p).r;
        return handDepth<.65 ? smoothstep(.000001,.00001,worldDepth-handDepth) : 0.;
    }
    vec3 delta=abs(texture(Sampler1,p).rgb-texture(Sampler0,p).rgb);
    return smoothstep(.003,.02,max(delta.r,max(delta.g,delta.b)));
}
void main() {
    vec4 original=texture(Sampler2,uv);
    float m=mask(uv);
    float edge=0.;
    for(int i=0;i<8;i++) {
        float a=float(i)*.785398;
        edge=max(edge,abs(mask(uv+vec2(cos(a),sin(a))*Texel*2.)-m));
    }
    vec2 offset=vec2(sin(uv.y*34.+Time*1.3),cos(uv.x*27.+Time))*Distortion;
    vec3 background=texture(Sampler0,clamp(uv+offset,Texel,1.-Texel)).rgb;
    if(BlurRadius>0.) {
        for(int i=0;i<8;i++) {
            float a=float(i)*.785398;
            background+=texture(Sampler0,clamp(uv+vec2(cos(a),sin(a))*Texel*BlurRadius,Texel,1.-Texel)).rgb;
        }
        background/=9.;
    }
    vec3 hand=original.rgb;
    float grey=dot(hand,vec3(.2126,.7152,.0722));
    hand=mix(vec3(grey),hand,Saturation);
    vec3 result=mix(background,hand,Opacity);
    if(Style==0) result=mix(result,Color,Tint);
    else {
        float waves=.5+.5*sin(uv.x*18.+uv.y*25.-Time*2.);
        vec3 spectrum=.5+.5*cos(vec3(0.,2.,4.)+uv.y*8.+Time);
        result=mix(result,Style==2 ? spectrum : Color,Tint);
        result+=Brightness*.14*waves;
    }
    float stripe=Shimmer==1 ? pow(max(0.,sin((uv.x+uv.y)*4.-Time*1.25)),28.)*.3 : 0.;
    fragColor=vec4(mix(original.rgb,result+stripe,m)+Color*edge*Glow*.35,original.a);
}
