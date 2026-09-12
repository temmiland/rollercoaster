attribute vec3 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform vec2 u_targetSize;
uniform vec3 u_billboardRight;
uniform float u_heightScale;
uniform vec4 u_uvTransform;
varying vec2 v_uv;

void main() {
    vec3 center = u_worldTrans[3].xyz;
    float width = length(u_worldTrans[0].xyz);
    float height = length(u_worldTrans[1].xyz);
    vec3 worldPosition = center + u_billboardRight * a_position.x * width
        + vec3(0.0, (a_position.y + 0.5) * height * u_heightScale, 0.0);
    vec4 clip = u_projViewTrans * vec4(worldPosition, 1.0);
    vec4 centerClip = u_projViewTrans * vec4(center, 1.0);
    vec2 centerPixels = (centerClip.xy / centerClip.w * 0.5 + 0.5) * u_targetSize;
    centerPixels = floor(centerPixels) + 0.5;
    vec2 centerNdc = centerPixels / u_targetSize * 2.0 - 1.0;
    clip.xy += (centerNdc - centerClip.xy / centerClip.w) * clip.w;
    gl_Position = clip;
    v_uv = a_texCoord0 * u_uvTransform.zw + u_uvTransform.xy;
}
