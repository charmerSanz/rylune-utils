#version 330
uniform sampler2D InSampler;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform MotionConfig { vec4 Params; };
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec2 stepUV = Params.xy * Params.z / InSize;
    vec4 center = texture(InSampler, texCoord);
    vec4 sum = center * 2.0;
    float weight = 2.0;
    // 3, 5 or 7 taps with a center-weighted kernel.
    int pairs = int(Params.w);
    for(int i=1; i<=pairs; i++){
        float t = float(i)/float(pairs);
        float w = 1.0-0.35*t;
        vec2 a = clamp(texCoord + stepUV*t, vec2(0.0), vec2(1.0));
        vec2 b = clamp(texCoord - stepUV*t, vec2(0.0), vec2(1.0));
        sum += (texture(InSampler,a)+texture(InSampler,b))*w;
        weight += 2.0*w;
    }
    fragColor=vec4((sum/weight).rgb,center.a);
}
