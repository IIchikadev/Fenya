#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Saturation;
uniform float Threshold;
uniform float Glow;
uniform float HistoryMix;
uniform vec2 Texel;
in vec2 uv;
out vec4 fragColor;
void main() {
    vec3 color = texture(Sampler0, uv).rgb;
    if (Glow > 0.0) {
        vec3 light = vec3(0.0);
        float weight = 0.0;
        for (int x = -3; x <= 3; x++) for (int y = -3; y <= 3; y++) {
            float w = exp(-float(x*x+y*y) / 5.0);
            vec3 c = texture(Sampler0, clamp(uv + vec2(x,y)*Texel*3.0, vec2(0.0), vec2(1.0))).rgb;
            light += max(c - vec3(Threshold), vec3(0.0)) * w;
            weight += w;
        }
        color += light / weight * Glow;
    }
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    color = mix(vec3(luma), color, Saturation);
    if (HistoryMix > 0.0) color = mix(color, texture(Sampler1, uv).rgb, HistoryMix);
    fragColor = vec4(color, 1.0);
}
