#ifdef GL_ES
#define VARYING_PRECISION mediump
#else
#define VARYING_PRECISION
#endif
attribute vec3 a_position;
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
varying VARYING_PRECISION vec3 v_worldPosition;
varying VARYING_PRECISION vec3 v_normal;
#ifdef vertexNormal
attribute vec3 a_normal;
#endif
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
    vec4 worldPosition = u_worldTrans * vec4(a_position, 1.0);
    v_worldPosition = worldPosition.xyz;
#ifdef vertexNormal
    v_normal = normalize(mat3(u_worldTrans) * a_normal);
#else
    v_normal = vec3(0.0, 1.0, 0.0);
#endif
    gl_Position = u_projViewTrans * worldPosition;
#ifdef vertexColor
    v_color = a_color;
#endif
#ifdef diffuseTexture
    v_uv = a_texCoord0 * u_uvTransform.zw + u_uvTransform.xy;
#endif
}
