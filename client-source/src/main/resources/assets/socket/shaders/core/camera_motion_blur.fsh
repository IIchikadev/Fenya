#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform mat4 MvInverse, ProjInverse, PrevModelView, PrevProjection;
uniform vec3 CameraDelta;
uniform float Strength;
uniform int Samples, Centered, UseDepth, ProtectHand;
in vec2 uv;
out vec4 fragColor;
void main() {
    float sceneDepth = texture(Sampler1, uv).r;
    vec3 original = texture(Sampler0, uv).rgb;
    // Iris compresses first-person hand depth into the near portion of the buffer.
    if (ProtectHand != 0 && sceneDepth < 0.75) {
        fragColor = vec4(original, 1.0);
        return;
    }
    float depth = UseDepth != 0 ? sceneDepth : 0.999;
    vec4 current = ProjInverse * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    current /= current.w;
    vec4 local = MvInverse * current;
    local.xyz += CameraDelta * local.w;
    vec4 previous = PrevProjection * PrevModelView * local;
    vec2 velocity = vec2(0.0);
    if (previous.w > 0.0) velocity = (uv - (previous.xy / previous.w * 0.5 + 0.5)) * Strength;
    float lengthPixels = length(velocity * vec2(textureSize(Sampler0,0)));
    int count = clamp(int(ceil(lengthPixels)), 1, clamp(Samples,4,128));
    velocity *= min(1.0, 0.25 / max(length(velocity),0.00001));
    vec3 color = vec3(0.0);
    for (int i=0; i<128; i++) {
        if (i>=count) break;
        float t = count == 1 ? 0.0 : float(i) / float(count-1);
        if (Centered != 0) t -= 0.5;
        vec2 sampleUv = clamp(uv - velocity*t, vec2(0.0), vec2(1.0));
        bool hand = ProtectHand != 0 && texture(Sampler1, sampleUv).r < 0.75;
        color += hand ? original : texture(Sampler0, sampleUv).rgb;
    }
    fragColor = vec4(color / float(count),1.0);
}
