#version 150
uniform vec2 Size;
uniform vec2 Mouse;
uniform float Progress;
uniform vec3 Accent;
in vec2 GuiPosition;
out vec4 fragColor;
void main() {
    float p = clamp(Progress, 0.0, 1.0);
    vec2 center = (floor(GuiPosition / 6.0) + 0.5) * 6.0;
    float proximity = 1.0 - smoothstep(0.0, 100.0, distance(center, Mouse));
    float radius = mix(0.7, 3.0, proximity);
    float dots = 1.0 - smoothstep(radius - 0.45, radius + 0.45, distance(GuiPosition, center));
    float pulse = 2.0 * min(p, 1.0 - p);
    float flashRadius = max(1.0, Size.x / 1.75 * p);
    float flash = (1.0 - smoothstep(flashRadius * 0.75, flashRadius, distance(GuiPosition, Size * 0.5))) * 0.2 * pulse;
    float glow = exp(-distance(GuiPosition, Size * 0.5) / flashRadius) * 0.15 * pulse;
    float dotAlpha = dots * 0.1 * p;
    float alpha = dotAlpha + flash + glow;
    fragColor = vec4((vec3(dotAlpha + flash) + Accent * glow) / max(alpha, 0.00001), alpha);
}
