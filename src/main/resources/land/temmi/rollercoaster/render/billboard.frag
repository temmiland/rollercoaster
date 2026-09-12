#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D u_texture;
uniform vec4 u_ambientLight;
uniform vec3 u_sunDirection;
uniform vec4 u_sunLight;
uniform int u_pointCount;
uniform vec3 u_pointPosition[8];
uniform vec4 u_pointLight[8];
uniform vec2 u_pointParams[8];
varying vec2 v_uv;
varying vec3 v_worldPosition;
varying vec3 v_normal;

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
        result += u_pointLight[i].rgb * u_pointLight[i].a * attenuation * attenuation
            * max(dot(normal, normalize(toLight)), 0.0);
    }
    return min(result, vec3(1.0));
}

void main() {
    vec4 color = texture2D(u_texture, v_uv);
    if (color.a < 0.5) discard;
    gl_FragColor = vec4(color.rgb * lightFactor(), 1.0);
}
