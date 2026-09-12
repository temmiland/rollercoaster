#ifdef GL_ES
#define VARYING_PRECISION mediump
#else
#define VARYING_PRECISION
#endif
attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
#ifdef vertexColor
attribute vec4 a_color;
varying VARYING_PRECISION vec4 v_color;
#endif
#ifdef diffuseTexture
attribute vec2 a_texCoord0;
uniform vec4 u_uvTransform;
varying VARYING_PRECISION vec2 v_uv;
#endif

void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
#ifdef vertexColor
    v_color = a_color;
#endif
#ifdef diffuseTexture
    v_uv = a_texCoord0 * u_uvTransform.zw + u_uvTransform.xy;
#endif
}
