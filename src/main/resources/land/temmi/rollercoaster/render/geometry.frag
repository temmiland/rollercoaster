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
uniform vec4 u_diffuseColor;
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

vec3 lightFactor() {
    vec3 normal = normalize(v_normal);
    vec3 result = u_ambientLight.rgb * u_ambientLight.a;
    result += u_sunLight.rgb * u_sunLight.a * max(dot(normal, normalize(u_sunDirection)), 0.0);
    for (int i = 0; i < 8; i++) {
        if (i >= u_pointCount) break;
        vec3 toLight = u_pointPosition[i] - v_worldPosition;
        float distanceToLight = length(toLight);
        float range = max(0.001, u_pointParams[i].x);
        float attenuation = max(0.0, 1.0 - distanceToLight / range);
        float cone = 1.0;
        if (u_pointParams[i].y > 0.5) {
            float angle = dot(normalize(-toLight), normalize(u_pointDirection[i]));
            cone = smoothstep(u_pointParams[i].w, u_pointParams[i].z, angle);
        }
        result += u_pointLight[i].rgb * u_pointLight[i].a * attenuation * attenuation
            * cone * max(dot(normal, normalize(toLight)), 0.0);
    }
    return min(result, vec3(1.0));
}

void main() {
    vec4 color = u_diffuseColor;
#ifdef vertexColor
    color *= v_color;
#endif
#ifdef diffuseTexture
    color *= texture2D(u_diffuseTexture, v_uv);
#endif
    gl_FragColor = vec4(color.rgb * lightFactor(), 1.0);
}
