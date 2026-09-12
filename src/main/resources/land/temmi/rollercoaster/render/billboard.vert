attribute vec3 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform vec2 u_targetSize;
uniform vec3 u_billboardRight;
uniform vec3 u_billboardUp;
varying vec2 v_uv;

void main() {
    vec3 center = u_worldTrans[3].xyz;
    float width = length(u_worldTrans[0].xyz);
    float height = length(u_worldTrans[1].xyz);
    vec3 worldPosition = center + u_billboardRight * a_position.x * width
        + u_billboardUp * a_position.y * height;
    vec4 clip = u_projViewTrans * vec4(worldPosition, 1.0);
    vec2 pixels = (clip.xy / clip.w * 0.5 + 0.5) * u_targetSize;
    pixels = floor(pixels) + 0.5;
    clip.xy = (pixels / u_targetSize * 2.0 - 1.0) * clip.w;
    gl_Position = clip;
    v_uv = a_texCoord0;
}
