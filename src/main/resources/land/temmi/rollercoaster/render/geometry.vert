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
#ifdef directionalShadow
uniform mat4 u_shadowMatrix;
varying VARYING_PRECISION vec4 v_shadowPosition;
#endif
#ifdef vertexNormal
attribute vec3 a_normal;
uniform mat3 u_normalMatrix;
#endif
#ifdef vertexColor
attribute vec4 a_color;
varying VARYING_PRECISION vec4 v_color;
#endif
#if defined(diffuseTexture) || defined(emissiveTexture)
attribute vec2 a_texCoord0;
#endif
#ifdef emissiveTexture
uniform vec4 u_emissiveUvTransform;
varying VARYING_PRECISION vec2 v_emissiveUv;
#endif
#ifdef diffuseTexture
uniform vec4 u_uvTransform;
varying VARYING_PRECISION vec2 v_uv;
#endif

void main() {
    vec4 worldPosition = u_worldTrans * vec4(a_position, 1.0);
    v_worldPosition = worldPosition.xyz;
#ifdef directionalShadow
    v_shadowPosition = u_shadowMatrix * worldPosition;
#endif
#ifdef vertexNormal
    v_normal = normalize(u_normalMatrix * a_normal);
#else
    v_normal = vec3(0.0, 1.0, 0.0);
#endif
    gl_Position = u_projViewTrans * worldPosition;
#ifdef emissiveTexture
    v_emissiveUv = a_texCoord0 * u_emissiveUvTransform.zw + u_emissiveUvTransform.xy;
#endif
#ifdef vertexColor
    v_color = a_color;
#endif
#ifdef diffuseTexture
    v_uv = a_texCoord0 * u_uvTransform.zw + u_uvTransform.xy;
#endif
}
