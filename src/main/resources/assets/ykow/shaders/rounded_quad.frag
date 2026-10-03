#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform vec4 color;
uniform vec2 size;
uniform vec4 radius; // top-left, top-right, bottom-right, bottom-left
uniform float smoothness;

float roundedBoxSDF(vec2 centerPosition, vec2 size, vec4 radius) {
    radius.xy = (centerPosition.x > 0.0) ? radius.xy : radius.zw;
    radius.x = (centerPosition.y > 0.0) ? radius.x : radius.y;
    vec2 q = abs(centerPosition) - size + radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
}

void main() {
    vec2 centerPosition = (TexCoord - 0.5) * size;
    float distance = roundedBoxSDF(centerPosition, size * 0.5, radius);
    
    float alpha = 1.0 - smoothstep(0.0, smoothness, distance);
    FragColor = vec4(color.rgb, color.a * alpha);
}
