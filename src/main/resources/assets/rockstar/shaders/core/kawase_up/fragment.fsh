#version 150

#moj_import <rockstar:common.glsl>

in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Resolution;
uniform float Offset;
uniform float Saturation;
uniform float TintIntensity;
uniform vec3 TintColor;

out vec4 OutColor;

vec3 adjustSaturation(vec3 color, float saturation) {
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    return mix(vec3(gray), color, saturation);
}

void main() {
    vec2 texel = Resolution;
    float off = Offset + 0.5;

    vec2 d = texel * off * 1.5;
    vec2 o1 = vec2( d.x,  d.y);
    vec2 o2 = vec2(-d.x,  d.y);
    vec2 o3 = vec2( d.x, -d.y);
    vec2 o4 = vec2(-d.x, -d.y);

    vec3 sum = texture(Sampler0, TexCoord + o1).rgb;
    sum += texture(Sampler0, TexCoord + o2).rgb;
    sum += texture(Sampler0, TexCoord + o3).rgb;
    sum += texture(Sampler0, TexCoord + o4).rgb;

    vec3 color = sum * 0.25;
    color = adjustSaturation(color, Saturation);
    color = mix(color, TintColor, TintIntensity);

    OutColor = vec4(color, 1.0) * FragColor;
}
