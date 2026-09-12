#ifdef GL_ES
#define VARYING_PRECISION mediump
#else
#define VARYING_PRECISION
#endif
#ifdef GL_ES
precision mediump float;
#endif
varying VARYING_PRECISION vec3 v_worldPosition;
varying VARYING_PRECISION vec3 v_normal;
#ifdef directionalShadow
uniform sampler2D u_shadowMap;
uniform float u_shadowTexelSize;
uniform float u_shadowBias;
uniform float u_shadowStrength;
uniform int u_shadowEnabled;
varying VARYING_PRECISION vec4 v_shadowPosition;
#endif
uniform vec4 u_diffuseColor;
uniform vec3 u_emissiveColor;
#ifdef emissiveTexture
uniform sampler2D u_emissiveTexture;
varying VARYING_PRECISION vec2 v_emissiveUv;
#endif
uniform vec4 u_ambientLight;
uniform vec3 u_sunDirection;
uniform vec4 u_sunLight;
uniform int u_pointCount;
uniform vec3 u_pointPosition[8];
uniform vec3 u_pointDirection[8];
uniform vec4 u_pointLight[8];
uniform vec4 u_pointParams[8];
#ifdef vertexColor
varying VARYING_PRECISION vec4 v_color;
#endif
#ifdef diffuseTexture
uniform sampler2D u_diffuseTexture;
varying VARYING_PRECISION vec2 v_uv;
#endif

#ifdef directionalShadow
float decodeDepth(vec4 encoded) {
    return encoded.r + encoded.g / 255.0;
}

float shadowVisibility() {
    if (u_shadowEnabled == 0) return 1.0;
    vec3 projected = v_shadowPosition.xyz / v_shadowPosition.w;
    if (projected.x <= 0.0 || projected.x >= 1.0 || projected.y <= 0.0 || projected.y >= 1.0
        || projected.z <= 0.0 || projected.z >= 1.0) return 1.0;
    float slope = 1.0 - max(dot(normalize(v_normal), normalize(u_sunDirection)), 0.0);
    float bias = u_shadowBias * (1.0 + 4.0 * slope);
    float visibility = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y)) * u_shadowTexelSize;
            float stored = decodeDepth(texture2D(u_shadowMap, projected.xy + offset));
            visibility += projected.z - bias <= stored ? 1.0 : 0.0;
        }
    }
    visibility /= 9.0;
    return mix(1.0 - u_shadowStrength, 1.0, visibility);
}
#endif

vec3 lightFactor() {
    vec3 normal = normalize(v_normal);
    vec3 result = u_ambientLight.rgb * u_ambientLight.a;
    float sunVisibility = 1.0;
#ifdef directionalShadow
    sunVisibility = shadowVisibility();
#endif
    result += u_sunLight.rgb * u_sunLight.a * sunVisibility * max(dot(normal, normalize(u_sunDirection)), 0.0);
    for (int i = 0; i < 8; i++) {
        if (i >= u_pointCount) break;
        vec3 toLight = u_pointPosition[i] - v_worldPosition;
        float distanceToLight = length(toLight);
        float range = max(0.001, u_pointParams[i].x);
        float attenuation = 1.0 - smoothstep(0.0, range, distanceToLight);
        float cone = 1.0;
        if (u_pointParams[i].y > 0.5) {
            float angle = dot(-toLight / max(distanceToLight, 0.0001), normalize(u_pointDirection[i]));
            cone = smoothstep(u_pointParams[i].w, u_pointParams[i].z, angle);
        }
        result += u_pointLight[i].rgb * u_pointLight[i].a * attenuation
            * cone * max(dot(normal, toLight / max(distanceToLight, 0.0001)), 0.0);
    }
    return result;
}

void main() {
    vec4 color = u_diffuseColor;
#ifdef vertexColor
    color *= v_color;
#endif
#ifdef diffuseTexture
    color *= texture2D(u_diffuseTexture, v_uv);
#endif
    vec3 emission = u_emissiveColor;
#ifdef emissiveTexture
    emission *= texture2D(u_emissiveTexture, v_emissiveUv).rgb;
#endif
    gl_FragColor = vec4(color.rgb * lightFactor() + emission, 1.0);
}
