#ifdef GL_ES
#define VARYING_PRECISION mediump
#else
#define VARYING_PRECISION
#endif
#ifdef GL_ES
precision mediump float;
#endif
uniform vec4 u_diffuseColor;
#ifdef vertexColor
varying VARYING_PRECISION vec4 v_color;
#endif
#ifdef diffuseTexture
uniform sampler2D u_diffuseTexture;
varying VARYING_PRECISION vec2 v_uv;
#endif

void main() {
    vec4 color = u_diffuseColor;
#ifdef vertexColor
    color *= v_color;
#endif
#ifdef diffuseTexture
    color *= texture2D(u_diffuseTexture, v_uv);
#endif
    gl_FragColor = vec4(color.rgb, 1.0);
}
